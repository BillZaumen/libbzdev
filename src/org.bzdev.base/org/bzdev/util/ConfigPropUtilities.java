package org.bzdev.util;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Console;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.*;
import org.bzdev.lang.UnexpectedExceptionError;

//@exbundle org.bzdev.util.lpack.Util

/**
 * Support class for processing {@link org.bzdev.swing.ConfigPropertyEditor}
 * files.
 * These files use the format described by
 * {@link Properties#load(java.io.Reader)}.
 * The values for keys that start with
 * <UL>
 *   <LI></STRONG>ebase64.</STRONG> are encrypted using either GPG or
 *       a symmetric cipherand then
 *       encoded as printable ASCII characters using the basic Base-64
 *       encoding using the alphabet specified in Table 1 of RFC 4648.
 *       When a symmetric cipher is used, the Base-64 encoding is prefaced
 *       with the string "===".
 *   <LI></STRONG>base64.</STRONG> are encoded as printable ASCII
 *       characters using the basic Base-64 encoding using the
 *       alphabet specified in Table 1 of RFC 4648.
 * </UL>
 * For all other keys, the sequence <STRONG>$(KEY)</STRONG> is
 * replaced with property value for the key <STRONG>KEY</STRONG>. This
 * replacement occurs recursively, with the recursion terminating when
 * the value does not specify a replacement or KEY starts with
 * "<STRONG>base64.</STRONG>" or "<STRONG>ebase64.</STRONG>".
 * <P>
 * The motivation is that sometimes values are repeated and it is
 * both tedious and error-prone to replace each instance when there is
 * a change.  For example
 * <BLOCKQUOTE><PRE><CODE>
 * foregroundColor = white
 * backgroundColor = rbg(10,10,20)
 * headingColor = $(foregroundColor)
 * textColor = $(foregroundColor)
 * errorColor = red
 * </CODE></PRE></BLOCKQUOTE>
 * allows one to switch from "dark mode" by simply changing two values.
 * <P>
 * Substitution loops are not supported:
 * <BLOCKQUOTE><PRE><CODE>
 * foregroundColor = $(backgroundColor)
 * backgroundColor = $(foregroundColor)
 * headingColor = $(foregroundColor)
 * textColor = $(foregroundColor)
 * errorColor = red
 * </CODE></PRE></BLOCKQUOTE>
 * will fail because the recursion will not terminate. This class does
 * not test for this error, but the class
 * {@link org.bzdev.swing.ConfigPropertyEditor}
 * does check and will not allow a file containing this error to be
 * written.
 * <P>
 * Finally, this class duplicates some of the functionality provided
 * by {@link org.bzdev.swing.ConfigPropertyEditor}.  The reason for
 * the duplication is that
 * <UL>
 *   <LI> the amount of code is small.
 *   <LI> the JAR file containing {@link org.bzdev.swing.ConfigPropertyEditor}
 *        is large enough that it would be wasteful to require its
 *        module when the only functionality needed is that provided
 *        by {@link ConfigPropUtilities}.
 * </UL>
 */
public class ConfigPropUtilities {

    // We need fully qualified names because this class contains
    // inner classes with similar names.
    static String errorMsg(java.lang.String key, java.lang.Object... args)
    {
	return UtilErrorMsg.errorMsg(key, args);
    }

    static String localeString(java.lang.String key)
    {
	return UtilErrorMsg.errorMsg(key);
    }

    private static final Charset UTF8 = Charset.forName("UTF-8");

    private static char[] decryptToCharArray(String value, char[] gpgpw,
					     String gpgdir)
	throws GeneralSecurityException
    {
	if (value == null || gpgpw == null) return null;
	boolean noGPG = value.startsWith("===");
	if (noGPG) {
	    value = value.substring(3);
	}
	byte[] data = Base64.getDecoder().decode(value);
	if (noGPG) {
	    byte[] barray = SymmetricCipher.decrypt(gpgpw, data);
	    return UTF8.decode(ByteBuffer.wrap(barray)).array();
	}

	ByteArrayInputStream is = new ByteArrayInputStream(data);

	try {
	    // Need to use --batch, etc. because when this runs in
	    // a dialog box, we don't have access to a terminal and
	    // GPG agent won't ask for a passphrase.
	    ProcessBuilder pb = (gpgdir == null)?
		new ProcessBuilder("gpg",
				   "--pinentry-mode",
				   "loopback",
				   "--passphrase-fd", "0",
				   "--batch", "-d"):
		new ProcessBuilder("gpg", "--homedir", gpgdir,
				   "--pinentry-mode",
				   "loopback",
				   "--passphrase-fd", "0",
				   "--trust-model", "tofu",
				   "--tofu-default-policy", "good",
				   "--batch", "-d"
				   );
	    // pb.redirectError(ProcessBuilder.Redirect.DISCARD);
	    ByteArrayOutputStream baos = new
		ByteArrayOutputStream(data.length);
	    Process p = pb.start();
	    Thread thread1 = new Thread(()->{
		    try {
			OutputStream os = p.getOutputStream();
			OutputStreamWriter w = new OutputStreamWriter(os);
			w.write(gpgpw, 0, gpgpw.length);
			w.write(System.getProperty("line.separator"));
			w.flush();
			is.transferTo(os);
			w.close();
			os.close();
		    } catch(Exception e) {
			System.err.println(e.getMessage());
		    }
	    });
	    Thread thread2 = new Thread(()->{
		    try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			InputStream is1 = p.getInputStream();
			is1.transferTo(baos);
		    } catch(Exception e) {
			System.err.println(e.getMessage());
		    }
	    });
	    thread2.start();
	    thread1.start();
	    thread1.join();
	    thread2.join();
	    p.waitFor();
	    if (p.exitValue() != 0) {
		String msg = errorMsg("gpgFailed", p.exitValue());
		throw new GeneralSecurityException(msg);
	    }
	    return (Charset.forName("utf-8")
		    .decode(ByteBuffer.wrap(baos.toByteArray())))
		.array();
	} catch (Exception e) {
	    String msg = errorMsg("decryption", e.getMessage());
	    throw new GeneralSecurityException(msg, e);
	}
    }

   private static Supplier<char[]> defaultPassphraseSupplier = () -> {
	Console console = System.console();
	if (console != null) {
	    char[] password = console
		.readPassword(localeString("enterPW2") + ":");
	    if (password == null || password.length == 0) {
		password = null;
	    }
	    return password;
	} else {
	    return null;
	}
    };

    /**
     * Create a new instance of {@link Properties}, loading its keys and
     * values from a file.
     * @param f the file from which to load properties
     * @return a new instance of {@link Properties}
     * @throws IOException if an IO error occurred
     */
    public static Properties newInstance(File f) throws IOException {
	if (f == null) {
	    throw new IOException("noFileSpecified");
	}
	try (InputStream is = new FileInputStream(f)) {
	    return newInstance(is);
	}
    }

    /**
     * Create a new instance of {@link Properties}, loading its keys and
     * values from an input stream.
     * The stream is not automatically closed.
     * @param is the input stream from which to load properties
     * @return a new instance of {@link Properties}
     * @throws IOException if an IO error occurred
     */
    public static Properties newInstance(InputStream is) throws IOException {
	Properties props = new Properties();
	props.load(is);
	// is.close();
	return props;
    }

    /**
     * Create a new instance of {@link Properties}, loading its keys and
     * values from a file, and checking the file's media type.
     * <P>
     * Media types are encoded in the first line of the file, which
     * is expected to be
     * <BLOCKQUOTE><PRE><CODE>
     * #(!M.T MEDIATYPE)
     * </CODE></PRE></BLOCKQUOTE>
     * where MEDIATYPE is the media (or MIME type) as defined in
     * RFC 2045 and subsequent RFCs. 
     * @param f the file from which to load properties
     * @param mediaType the expected media type for the file
     * @return a new instance of {@link Properties}
     * @throws IOException if an IO error occurred
     */
    public static Properties newInstance(File f,
					 String mediaType)
	throws IOException
    {
	if (f == null) {
	    throw new IOException("noFileSpecified");
	}
	try (InputStream is = new FileInputStream(f)) {
	    return newInstance(is, mediaType);
	}
    }

    /**
     * Create a new instance of {@link Properties}, loading its keys and
     * values from an input sream, and checking the stream's media type.
     * <P>
     * Media types are encoded in the first line of the file, which
     * is expected to be
     * <BLOCKQUOTE><PRE><CODE>
     * #(!M.T MEDIATYPE)
     * </CODE></PRE></BLOCKQUOTE>
     * where MEDIATYPE is the media (or MIME type) as defined in
     * RFC 2045 and subsequent RFCs.  The mediatype is converted to lower
     * case for testing.
     * The stream is not automatically closed.
     * @param is the input stream from which to load properties
     * @param mediaType the expected media type for the file
     * @return a new instance of {@link Properties}
     * @throws IOException if an IO error occurred
     */
    public static Properties newInstance(InputStream is,
					 String mediaType)
	throws IOException
    {
	if (is == null) {
	    throw new IOException("noInputStreamSpecified");
	}
	Reader r = new InputStreamReader(is, UTF8);
	String comment = "#(!M.T " + mediaType.trim().toLowerCase() + ")\r\n";

	char[] cbuf1 = comment.toCharArray();
	char[] cbuf2 = new char[cbuf1.length];
	// int n = r.read(cbuf2);
	int n = r.read(cbuf2, 0, cbuf2.length-1);
	if (cbuf2[cbuf2.length-2] == '\r') {
	    n += r.read(cbuf2, n, 1);
	} else if (cbuf2[cbuf2.length-2] == '\n') {
	    cbuf2[n-1] = '\r';
	    if (n < cbuf2.length) {
		cbuf2[n] = '\n';
		n++;
	    }
	}
	if (n != cbuf1.length) {
	    throw new IOException(errorMsg("wrongMediaType"));
	}
	for  (int i = 0; i < n; i++) {
	    if (i < 7) {
		if (cbuf1[i] != cbuf2[i]) {
		    throw new
			IOException(errorMsg("wrongMediaType"));
		}
	    } else {
		if (cbuf1[i] != Character.toLowerCase(cbuf2[i])) {
		    throw new
			IOException(errorMsg("wrongMediaType"));
		}
	    }
	}
	Properties properties = new Properties();
	properties.load(r);
	// r.close();
	return properties;
    }

    /**
     * Create a new {@link Properties} object given Base-64 encoded data.
     * @param b64data a string containing Base64-encoded GZIP data.
     * Media types are encoded in the first line of the file, which
     * is expected to be
     * <BLOCKQUOTE><PRE><CODE>
     * #(!M.T MEDIATYPE)
     * </CODE></PRE></BLOCKQUOTE>
     * where MEDIATYPE is the media (or MIME type) as defined in
     * RFC 2045 and subsequent RFCs.  The mediatype is converted to lower
     * case for testing.
     * @param mediaType the expected media type for the file
     * @return a new instance of {@link Properties}
     * @throws IOException if the media type does not match that of the
     *         Base-64 encoded representation
     */
    public static Properties newInstance(String b64data, String mediaType)
	throws IOException
    {
	InputStream is = new
	    ByteArrayInputStream(Base64.getDecoder().decode(b64data));
	is = new GZIPInputStream(is);
	return newInstance(is, mediaType);
    }

    /**
     * Create a new {@link Properties} object given a byte array that
     * represents an instance of {@link Properties}.
     * Media types are encoded in the first line of the file, which
     * is expected to be
     * <BLOCKQUOTE><PRE><CODE>
     * #(!M.T MEDIATYPE)
     * </CODE></PRE></BLOCKQUOTE>
     * where MEDIATYPE is the media (or MIME type) as defined in
     * RFC 2045 and subsequent RFCs.  The mediatype is converted to lower
     * case for testing.
     * @param data the byte representation
     * @param mediaType the expected media (MIME) type
     * @param gzipped true if GZIP compression is used; false otherwise
     * @return a new instance of {@link Properties}
     */
    public static Properties newInstance(byte[] data, String mediaType,
					 boolean gzipped)
	throws IOException
    {
	InputStream is = new ByteArrayInputStream(data);
	if (gzipped) is = new GZIPInputStream(is);
	return newInstance(is, mediaType);
    }


    private static final String B64KEY_START = "base64.";
    private static final String EB64KEY_START = "ebase64.";

    /**
     * Get the passphrase.
     * The default supplier obtains the passphrase from the system
     * console.
     * @param supplier a {@link Supplier} that will provide the
     *        passphrase; null for a default
     * @return the passphrase
     */
    public static char[] getPassphrase(Supplier<char[]> supplier) {
	if (supplier == null) supplier = defaultPassphraseSupplier;
	return supplier.get();
    }

    public static final char[] EMPTY_CHAR_ARRAY = new char[0];

    /**
     * Get the value, decrypted if necessary, stored in an instance of
     * {@link Properties} under a given key.
     * @param props the properties
     * @param key the key
     * @param passphrase the GPG passphrase for decryption.
     * @return the decrypted value
     * @throws GeneralSecurityException if decryption failed
     */
    public static char[] getDecryptedProperty(Properties props, String key,
					      char[] passphrase)
	throws GeneralSecurityException
    {
	return getDecryptedProperty(props, key, passphrase, null);
    }

    /**
     * Get the value, decrypted if necessary, stored in an instance of
     * {@link Properties} under a given key and a GPG home directory.
     * The GPG home directory is the argument for the GPG --homedir
     * command-line option.
     * <P>
     * When gpgdir is non-null, a GPG TOFU (Trust On First Use) trust
     * model is used.
     * @param props the properties
     * @param key the key
     * @param passphrase the GPG passphrase for decryption
     * @param gpgdir the GPG 'home' directory to use; null for the default
     * @return the decrypted value
     * @throws GeneralSecurityException if decryption failed
     */
    public static char[] getDecryptedProperty(Properties props, String key,
					      char[] passphrase,
					      String gpgdir)
	throws GeneralSecurityException
    {
	if (key.startsWith(EB64KEY_START)) {
	    String encrypted = props.getProperty(key);
	    if (encrypted == null) return EMPTY_CHAR_ARRAY;
	    return decryptToCharArray(encrypted, passphrase, gpgdir);
	} else {
	    return getProperty(props, key).toCharArray();
	}
    }

    private static final Pattern keyPattern =
	Pattern.compile("[a-zA-Z][a-zA-Z0-9_]*([.][a-zA-Z][a-zA-Z0-9_]*)*");
    static final Pattern pattern =
	Pattern.compile(Pattern.quote("$$") + "|" + Pattern.quote("$(")
			+ "([a-zA-Z][a-zA-Z0-9_]*([.][a-zA-Z][a-zA-Z0-9_]*)*)"
			+ Pattern.quote(")"));

    private static Set<String> cyclicKeys = null;

    /**
     * Get keys associated with a cycle.
     * This method will return the keys that appear in a cycle
     * immediately after {@link #getProperty(Properties,String)}
     * throws an exception.
     * <P>
     * This method is intended for debugging, not general use.
     * @return a set of keys
     */
    public static synchronized Set<String> getCyclicKeys() {
	return cyclicKeys;
    }

    /**
     * Get the value stored in an instance of {@link Properties} under
     * a given key.
     * Values whose keys start with "base64." are decoded using a Base-64
     * decoder. Otherwise, the sequence "$(KEY)", where KEY is some key,
     * is replaced with the value stored for KEY in the value provided
     * for the given key.
     * <P>
     * If an error occurs due to a key recursively referencing itself,
     * the method {@link #getCyclicKeys()} can be used to help find the
     * cycle.
     * @param props the instance of {@link Properties} storing key-value
     *        pairs.
     * @param key the key
     * @return the value for the given key; null if the key does not exist
     * @throws IllegalStateException if a key is part of a cyclic reference
     */
    public static synchronized String getProperty(Properties props, String key)
	throws IllegalStateException, IllegalArgumentException
    {
	cyclicKeys = null;
	return getProperty(props, key, new HashSet<String>());
    }
    /**
     * Get the value stored in an instance of {@link Properties} under
     * a given key.
     * Values whose keys start with "base64." are decoded using a Base-64
     * decoder. Otherwise, the sequence "$(KEY)", where KEY is some key,
     * is replaced with the value stored for KEY in the value provided
     * for the given key.
     * @param props the instance of {@link Properties} storing key-value
     *        pairs.
     * @param key the key
     * @param keys a set used to detect cyclic references
     * @return the value for the given key; null if the key does not exist
     * @throws IllegalStateException if a key is part of a cyclic reference
     */
    private static synchronized String getProperty(Properties props,
						   String key,
						   Set<String>keys)
	throws IllegalStateException
    {
	keys.add(key);
	if (key.startsWith(EB64KEY_START)) {
	    return props.getProperty(key);
	} else if (key.startsWith(B64KEY_START)) {
	    String value = props.getProperty(key);
	    if (value == null) return null;
	    byte[] data = Base64.getDecoder().decode(value);
	    ByteArrayInputStream is = new ByteArrayInputStream(data);
	    StringBuilder sb = new StringBuilder();
	    try {
		CopyUtilities.copyStream(is, sb, UTF8);
	    } catch (IOException eio) {}
	    return sb.toString();
	} else {
	    String value = props.getProperty(key);
	    if (value == null) {
		value = getProperty(props, "base64." + key);
		return value;
	    }
	    Matcher matcher = pattern.matcher(value);
	    int index = 0;
	    StringBuilder sb = null;
	    while (matcher.find(index)) {
		if (sb == null) sb = new StringBuilder();
		int start = matcher.start();
		int end = matcher.end();
		if (value.charAt(start+1) == '$') {
		    sb.append("$");
		} else {
		    String pkey = value.substring(start+2, end-1);
		    sb.append(value.substring(index, start));
		    if (keys.contains(pkey)) {
			String msg = errorMsg("circularKeys", pkey);
		        throw new IllegalStateException(msg);
		    } else {
			keys.add(pkey);
		    }
		    String pval = getProperty(props, pkey, keys);
		    keys.remove(pkey);
		    if (pval != null) {
			sb.append(pval);
		    }
		}
		index = end;
	    }
	    if (index > 0) {
		sb.append(value.substring(index));
		return sb.toString();
	    } else {
		return value;
	    }
	}
    }

    private static String encode(String value) {
	ByteArrayOutputStream os = new ByteArrayOutputStream(value.length());
	OutputStreamWriter w = new OutputStreamWriter(os, UTF8);
	try {
	    w.write(value, 0, value.length());
	    w.flush();
	    w.close();
	} catch (IOException eio) {
	    throw new UnexpectedExceptionError(eio);
	}
	byte[] data = os.toByteArray();
	data = Base64.getEncoder().encode(data);
	return new String(data, UTF8);
    }

    private static final String EMPTY_STRING = "";

    /*
    private static String encrypt(String value, String password) {
	return encrypt(value, password, null);
    }
    */
    private static String encrypt(String value, String gpgdir,
				  String[] recipients)
    {
	if (value == null || value.length() == 0) return EMPTY_STRING;
	/*
	boolean useGPG = !(recipients == null || recipients.length == 0);
	if (useGPG == false) {
	    try {
		// gpgdir doubles as the password
		byte[] data = SymmetricCipher.encrypt(gpgdir, value);
		data = Base64.getEncoder().encode(data);
		StringBuilder sb1 = new StringBuilder(256);
		return "===" + new String(data, UTF8);
	    } catch (Exception e) {}
	}
	*/
	if (recipients.length == 0) return EMPTY_STRING;
	LinkedList<String> args = new LinkedList<>();
	args.add("gpg");
	if (gpgdir != null) {
	    args.add("--homedir");
	    args.add(gpgdir);
	    args.add("--trust-model");
	    args.add("tofu");
	    args.add("--tofu-default-policy");
	    args.add("good");
	}
	args.add("-o");
	args.add("-");
	for (String recipient: recipients) {
	    args.add("-r");
	    args.add(recipient);
	}
	args.add("-e");
	ProcessBuilder pb = new ProcessBuilder(args);
	pb.redirectError(ProcessBuilder.Redirect.DISCARD);
	final StringBuilder sb = new StringBuilder(256);
	try {
	    Process p = pb.start();
	    Thread thread = new Thread(() -> {
		    try {
			InputStream is = p.getInputStream();
			byte data2[] = is.readAllBytes();
			p.waitFor();
			data2 = Base64.getEncoder().encode(data2);
			sb.append(new String(data2, UTF8));
		    } catch (Exception e) {
			try {
			    p.waitFor();
			} catch (Exception ee) {
			    e.printStackTrace();
			}
		    }
	    });
	    thread.start();
	    try {
		OutputStream os = p.getOutputStream();
		OutputStreamWriter w = new OutputStreamWriter(os, UTF8);
		w.write(value);
		w.flush();
		w.close();
		thread.join();
		if (p.exitValue() != 0) {
		    System.err.println(errorMsg("gpgFailed", p.exitValue()));
		    return null;
		} else {
		    return sb.toString();
		}
	    } catch (Exception e) {
		thread.join();
		if (p.exitValue() != 0) {
		    System.err.println(errorMsg("gpgFailed", p.exitValue()));
		} else {
		    e.printStackTrace();
		}
		return null;
	    }
	} catch (Exception e) {
	    return null;
	}
    }

    // same code as above because w.write takes a string or an
    // array of characters as an argument.
   private static String encrypt(char[] value, String gpgdir,
				  String[] recipients)
    {
	if (value == null || value.length == 0) return EMPTY_STRING;
	/*
	boolean useGPG = !(recipients == null || recipients.length == 0);
	if (useGPG == false) {
	    try {
		// gpgdir doubles as the password
		byte[] data = SymmetricCipher.encrypt(gpgdir, value);
		data = Base64.getEncoder().encode(data);
		StringBuilder sb1 = new StringBuilder(256);
		return "===" + new String(data, UTF8);
	    } catch (Exception e) {}
	}
	*/
	if (recipients.length == 0) return EMPTY_STRING;
	LinkedList<String> args = new LinkedList<>();
	args.add("gpg");
	if (gpgdir != null) {
	    args.add("--homedir");
	    args.add(gpgdir);
	    args.add("--trust-model");
	    args.add("tofu");
	    args.add("--tofu-default-policy");
	    args.add("good");
	}
	args.add("-o");
	args.add("-");
	for (String recipient: recipients) {
	    args.add("-r");
	    args.add(recipient);
	}
	args.add("-e");
	ProcessBuilder pb = new ProcessBuilder(args);
	pb.redirectError(ProcessBuilder.Redirect.DISCARD);
	final StringBuilder sb = new StringBuilder(256);
	try {
	    Process p = pb.start();
	    Thread thread = new Thread(() -> {
		    try {
			InputStream is = p.getInputStream();
			byte data2[] = is.readAllBytes();
			p.waitFor();
			data2 = Base64.getEncoder().encode(data2);
			sb.append(new String(data2, UTF8));
		    } catch (Exception e) {
			try {
			    p.waitFor();
			} catch (Exception ee) {
			    e.printStackTrace();
			}
		    }
	    });
	    thread.start();
	    try {
		OutputStream os = p.getOutputStream();
		OutputStreamWriter w = new OutputStreamWriter(os, UTF8);
		w.write(value);
		w.flush();
		w.close();
		thread.join();
		if (p.exitValue() != 0) {
		    System.err.println(errorMsg("gpgFailed", p.exitValue()));
		    return null;
		} else {
		    return sb.toString();
		}
	    } catch (Exception e) {
		thread.join();
		if (p.exitValue() != 0) {
		    System.err.println(errorMsg("gpgFailed", p.exitValue()));
		} else {
		    e.printStackTrace();
		}
		return null;
	    }
	} catch (Exception e) {
	    return null;
	}
    }

    private static final int B64KEY_START_LEN = B64KEY_START.length();
    private static final int EB64KEY_START_LEN = EB64KEY_START.length();

    /**
     * Set a property for an instance of {@link Properties}.
     * When decoded, each '$$' will be replaced with a single
     * '$' and substrings of the form "$(KEY)" will be replaced
     * with the value for the specified KEY.
     * @param props the instance of {@link Properties}
     * @param key the property key
     * @param value the property value
     * @throws IllegalArgumentException if the key starts with "ebase64."
     * @see #setProperty(Properties, String, String, String, String[])
     * @see #setProperty(Properties, String, String, char[])
     */
    public static void setProperty(Properties props,
				   String key, String value)
	throws IllegalArgumentException
    {
	setProperty(props, key, value, false);
    }

    /**
     * Set a property for an instance of {@link Properties}.
     * When the argument <CODE>literal</CODE> is false, a '$' must
     * be escaped by replacing it with the pair "$$", and substrings
     * of the form "$(KEY)" are replaced with the value of KEY. A
     * check for circularity is not performed at this point.
     * Each "$" in the value will be replaced with "$$" because of
     * {@link org.bzdev.swing.ConfigPropertyEditor} conventions, unless
     * the key starts with "base64.", in which case the value will be
     * Base-64 encoded.
     * @param props the instance of {@link Properties}
     * @param key the property key
     * @param value the property value
     * @param literal true if the value is a literal string; false if
     *        variable substitution is allowed
     * @throws IllegalArgumentException if the key starts with "ebase64."
     * @see #setProperty(Properties, String, String, String, String[])
     * @see #setProperty(Properties, String, String, char[])
     */
    public static void setProperty(Properties props,
				   String key, String value,
				   boolean literal)
	throws IllegalArgumentException
    {
	if (key.startsWith(EB64KEY_START)) {
	    throw new IllegalArgumentException(errorMsg("notEncryption"));
	}
	if (key.startsWith(B64KEY_START)) {
	    props.setProperty(key, encode(value));
	} else {
	    if (literal) {
		value = value.replace("$", "$$");
	    }
	    props.setProperty(key, value);
	}
    }

    /**
     * Set a property for an instance of {@link Properties} when symmetric
     * encryption is used.
     * <P>
     * Note: When a {@link org.bzdev.swing.ConfigPropertyEditor} is used,
     * either all encrypted entries should use either symmetric encryption
     * or public key encryption, but these should not be mixed. All
     * encrypted entries should use the same password or passphrase.
     * @param props the instance of {@link Properties}
     * @param key the property key
     * @param value the property value
     * @param password the password for symmetric encryption
     * @throws IllegalArgumentException if the password is null or empty,
     *         or if the key does not start with "ebase64."
     * @see #setProperty(Properties, String, String, String, String[])
     * @see #setProperty(Properties, String, String)
     */
    public static void setProperty(Properties props,
				   String key, String value,
				   char[] password)
	throws IllegalArgumentException
    {
	if (password == null || password.length == 0) {
	    throw new IllegalArgumentException(errorMsg("noPassword"));
	}
	if (!key.startsWith(EB64KEY_START)) {
	    throw new IllegalArgumentException(errorMsg("ebase64Key"));
	}
	String result;
	try {
	    byte[] data = SymmetricCipher.encrypt(password, value);
	    data = Base64.getEncoder().encode(data);
	    StringBuilder sb1 = new StringBuilder(256);
	    result =  "===" + new String(data, UTF8);
	} catch (Exception e) {result = null;}
	props.setProperty(key, result);
    }

 /**
     * Set a property for an instance of {@link Properties} when symmetric
     * encryption is used and the value being encrypted is an array of char.
     * <P>
     * Note: When a {@link org.bzdev.swing.ConfigPropertyEditor} is used,
     * either all encrypted entries should use either symmetric encryption
     * or public key encryption, but these should not be mixed. All
     * encrypted entries should use the same password or passphrase.
     * @param props the instance of {@link Properties}
     * @param key the property key
     * @param value the property value
     * @param password the password for symmetric encryption
     * @throws IllegalArgumentException if the password is null or empty,
     *         or if the key does not start with "ebase64."
     * @see #setProperty(Properties, String, String, String, String[])
     * @see #setProperty(Properties, String, String)
     */
    public static void setProperty(Properties props,
				   String key, char[] value,
				   char[] password)
	throws IllegalArgumentException
    {
	if (password == null || password.length == 0) {
	    throw new IllegalArgumentException(errorMsg("noPassword"));
	}
	if (!key.startsWith(EB64KEY_START)) {
	    throw new IllegalArgumentException(errorMsg("ebase64Key"));
	}
	String result;
	try {
	    byte[] data = SymmetricCipher.encrypt(password, value);
	    data = Base64.getEncoder().encode(data);
	    StringBuilder sb1 = new StringBuilder(256);
	    result =  "===" + new String(data, UTF8);
	} catch (Exception e) {result = null;}
	props.setProperty(key, result);
    }

    /**
     * Set a property for an instance of {@link Properties} when
     * GPG encryption is used.
     * The recipient's list must contain strings that GPG can use to
     * look up a public key.
     * <P>
     * Note: When a {@link org.bzdev.swing.ConfigPropertyEditor} is used,
     * either all encrypted entries should use either symmetric encryption
     * or public key encryption, but these should not be mixed. All
     * encrypted entries should use the same password or passphrase.
     * <P>
     * When gpgdir is non-null, a GPG TOFU (Trust On First Use) trust
     * model is used.
     * @param props the instance of {@link Properties}
     * @param key the property key
     * @param value the property value
     * @param gpgdir The GPG home directory used to store public an private
     *        keys; null for the default
     * @param recipients the recipients
     * @throws IllegalArgumentException if there are no recipients or
     *         if the key does not start with "ebase64"
     * @see #setProperty(Properties, String, String, char[])
     * @see #setProperty(Properties, String, String)
     */
    public static void setProperty(Properties props,
				   String key, String value,
				   String gpgdir, String[] recipients)
	throws IllegalArgumentException
    {
	if (key.startsWith(EB64KEY_START)) {
	    if (recipients == null) {
		throw new IllegalArgumentException("noRecipients");
	    } else {
		props.setProperty(key, encrypt(value, gpgdir, recipients));
	    }
	} else {
	    throw new IllegalArgumentException(errorMsg("ebase64Key"));
	}
    }

    /**
     * Set a property for an instance of {@link Properties} when
     * GPG encryption is used and the value being encrypted is an array of char.
     * The recipient's list must contain strings that GPG can use to
     * look up a public key.
     * <P>
     * Note: When a {@link org.bzdev.swing.ConfigPropertyEditor} is used,
     * either all encrypted entries should use either symmetric encryption
     * or public key encryption, but these should not be mixed. All
     * encrypted entries should use the same password or passphrase.
     * <P>
     * When gpgdir is non-null, a GPG TOFU (Trust On First Use) trust
     * model is used.
     * @param props the instance of {@link Properties}
     * @param key the property key
     * @param value the property value
     * @param gpgdir The GPG home directory used to store public an private
     *        keys; null for the default
     * @param recipients the recipients
     * @throws IllegalArgumentException if there are no recipients or
     *         if the key does not start with "ebase64"
     * @see #setProperty(Properties, String, String, char[])
     * @see #setProperty(Properties, String, String)
     */
    public static void setProperty(Properties props,
				   String key, char[] value,
				   String gpgdir, String[] recipients)
	throws IllegalArgumentException
    {
	if (key.startsWith(EB64KEY_START)) {
	    if (recipients == null) {
		throw new IllegalArgumentException("noRecipients");
	    } else {
		props.setProperty(key, encrypt(value, gpgdir, recipients));
	    }
	} else {
	    throw new IllegalArgumentException(errorMsg("ebase64Key"));
	}
    }

    private static class CRLFWriter extends FilterWriter {
	boolean prepend = false;
	boolean append = true;
	public CRLFWriter(Writer w) {
	    super(w);
	    String eol = System.lineSeparator();
	    if (eol.equals("\n")) {
		prepend = true;
	    } else if (eol.equals("\r")) {
		append = true;
	    }
	}
	@Override
	public void write(int c) throws IOException {
	    if (c == '\n' && prepend) {
		out.write('\r');
		out.write(c);
	    } else if (c == '\r' && append) {
		out.write(c);
		out.write('\r');
	    } else {
		out.write(c);
	    }
	}
	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
	    int limit = off + len;
	    for (int i = off; i < limit; i++) {
		write(cbuf[i]);
	    }
	}
	public void write(String str, int off, int len) throws IOException {
	    int limit = off + len;
	    for (int i = off; i < limit; i++) {
		write(str.charAt(i));
	    }
	}
    }

    /**
     * Store a properties file given an output file, using
     * the {@link org.bzdev.swing.ConfigPropertyEditor} format.
     * This method uses {@link Properties#store(Writer,String)}
     * with a writer that uses the UTF-8 character set with
     * CRLF as an end-of-line delimiter.
     * @param props the properties object
     * @param file the file
     * @param mediaType the media (or MIME) type
     * @throws IOException if an IO error occurred
     */
    public static void store(Properties props,
			     File file,
			     String mediaType)
	throws IOException
    {
	OutputStream os = new FileOutputStream(file);
	store(props, os, mediaType);
	os.close();
    }

    /**
     * Store a properties file given an output stream, using
     * the {@link org.bzdev.swing.ConfigPropertyEditor} format.
     * This method uses {@link Properties#store(Writer,String)}
     * with a writer that uses the UTF-8 character set with
     * CRLF as an end-of-line delimiter.
     * @param props the properties object
     * @param os the output stream
     * @param mediaType the media (or MIME) type
     * @throws IOException if an IO error occurred
     */
    public static void store(Properties props,
			     OutputStream os,
			     String mediaType)
	throws IOException
    {
	Writer w = new OutputStreamWriter(os, UTF8);
	w = new CRLFWriter(w);
	props.store(w, "(!M.T " + mediaType.toLowerCase() + ")");
    }

    /**
     * Store a properties file given an output stream, using
     * the {@link org.bzdev.swing.ConfigPropertyEditor} format.
     * The string is produced by in effect first creating a text file
     * using the UTF-8 charset and with a CRLF sequence terminating each
     * line, compressing that file, and then base-64 encoding the
     * compressed file. The text-file format is that produced by
     * {@link Properties#store(Writer,String)}. This byte array is
     * finally base-64 encoded and turned into a string using the UTF-8
     * character set.
     * The properties file will start with a comment
     * "#(!M.T " MEDIATYPE)" where MEDIATYPE is the media type in lower case.
     * @param props the properties object
     * @param mediaType the media (or MIME) type
     * @return a string representation of a Properties object
     */
    public static String store(Properties props, String mediaType)
    {
	try {
	    ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    OutputStream os = new GZIPOutputStream(bos);
	    store(props, os, mediaType);
	    os.flush();
	    os.close();
	    byte[] data = Base64.getEncoder().encode(bos.toByteArray());
	    return new String(data, UTF8);
	} catch (IOException eio) {
	    throw new UnexpectedExceptionError(eio);
	}
    }

    /**
     * Store a properties file as an array of bytes.
     * The byte array is produced by in effect first creating a text
     * file using the UTF-8 charset and with a CRLF sequence
     * terminating each line, optionally compressing that file.  The
     * text-file format is that produced by
     * {@link Properties#store(Writer,String)}.
     * The properties file will start with a comment
     * "#(!M.T " MEDIATYPE)" where MEDIATYPE is the media type in lower case.
     * @param props the properties object
     * @param mediaType the media (or MIME) type
     * @param gzip true if GZIP compression is used; false otherwise
     * @return a byte array storing an instance of {@link Properties}.
     */
    public static byte[] storeBytes(Properties props, String mediaType,
				    boolean gzip)
    {
	try {
	    ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    OutputStream os = gzip? new GZIPOutputStream(bos): bos;
	    store(props, os, mediaType);
	    os.flush();
	    os.close();
	    return bos.toByteArray();
	} catch (IOException eio) {
	    throw new UnexpectedExceptionError(eio);
	}
    }

    /**
     * Encode a list of GPG recipients.
     * The recipients first turned into a series of UTF-8 encoded
     * bytes, separated by a new-line character, and the resulting
     * sequence of bytes is Base64 encoded.
     * @param recipients the recipients
     * @return a string encoding the recipients; null if the argument
     *         is null
     */
    public static String encodeRecipients(List<String> recipients) {
	if (recipients == null) return null;
	ByteArrayOutputStream baos = new
	    ByteArrayOutputStream(32*recipients.size());
	boolean first = true;
	if (recipients.size() == 0) {
	    byte[] barray = {};
	    baos.writeBytes(barray);
	} else {
	    for (String s: recipients) {
		if (s == null) continue;
		s = s.trim();
		if (s.length() == 0) {
		    continue;
		}
		if (first) {
		    first = false;
		} else {
		    baos.write((int)'\n');
		}
		byte[] barray = s.getBytes(UTF8);
		baos.writeBytes(barray);
	    }
	}
	Base64.Encoder enc = Base64.getEncoder();
	return enc.encodeToString(baos.toByteArray());
    }

    /**
     * Encode an array of GPG recipients.
     * The recipients first turned into a series of UTF-8 encoded
     * bytes, separated by a new-line character, and the resulting
     * sequence of bytes is Base64 encoded.
     * @param recipients the recipients
     * @return a string encoding the recipients; null if the argument
     *         is null
     */
    public static String encodeRecipients(String[] recipients) {
	if (recipients == null) return null;
	ByteArrayOutputStream baos = new
	    ByteArrayOutputStream(32*recipients.length);
	boolean first = true;
	if (recipients.length == 0) {
	    byte[] barray = {};
	    baos.writeBytes(barray);
	} else {
	    for (String s: recipients) {
		if (s == null) continue;
		s = s.trim();
		if (s.length() == 0) {
		    continue;
		}
		if (first) {
		    first = false;
		} else {
		    baos.write((int)'\n');
		}
		byte[] barray = s.getBytes(UTF8);
		baos.writeBytes(barray);
	    }
	}
	Base64.Encoder enc = Base64.getEncoder();
	return enc.encodeToString(baos.toByteArray());
    }

    /**
     * Decode a string representing GPG recipients
     * Encoded recipients are first Base-64 decoded and
     * the resulting string is then split at new-line characters
     * to create the array.
     * @param recipients the encoded recipients
     * @return an array each element of which is a recipient; an empty
     *         array if there are no recipients; null if the argument is
     *         null
     * empty array if there are no recipients
     */
    public static String[] decodeRecipients(String recipients) {
	if (recipients == null) return null;
	String decoded =new
	    String(Base64.getDecoder().decode(recipients), UTF8);
	if (decoded.length() == 0) {
	    return new String[0];
	}
	return decoded.split("\n");
    }


}

//  LocalWords:  exbundle GPG BLOCKQUOTE PRE foregroundColor rbg UTF
//  LocalWords:  backgroundColor headingColor textColor errorColor fd
//  LocalWords:  ConfigPropUtilities gpg pinentry loopback tmpf pb zA
//  LocalWords:  getCanonicalPath redirectError gpgFailed utf enterPW
//  LocalWords:  MEDIATYPE mediaType noFileSpecified wrongMediaType
//  LocalWords:  ebase decrypted GeneralSecurityException
//  LocalWords:  needPassphrase
