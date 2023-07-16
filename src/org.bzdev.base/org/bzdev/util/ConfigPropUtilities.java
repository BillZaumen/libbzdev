package org.bzdev.util;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Console;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//@exbundle org.bzdev.util.lpack.Util


/**
 * Support class for processing {@link org.bzdev.swing.ConfigPropertyEditor}
 * files.
 * These files use the format described by
 * {@link Properties#load(java.io.Reader)}.
 * The values for keys that start with
 * <UL>
 *   <LI></STRONG>ebase64.</STRONG> are encrypted using GPG and then
 *       encoded as printable ascii characters using the basic Base64
 *       encoding using the alphabet specified in Table 1 of RFC 4648.
 *   <LI></STRONG>base64.</STRONG> are encoded as printable ascii
 *       characters using the basic Base64 encoding using the
 *       alphabet specified in Table 1 of RFC 4648.
 * </UL>
 * For all other keys, the sequence <STRONG>$(KEY)</STRONG> is
 * replaced with property value for the key <STRONG>KEY</STRONG>. This
 * replacement occurs recursively, with the recursion terminating when
 * the value does not specify a replacement or KEY starts with
 * "<STRONG>base64.</STRONG>" or "<STRONG>ebase64.</STRONG>".
 * <P>
 * The motivation is that sometimes values are repeated and it is
 * both tedius and error-prone to replace each instance when there is
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
 * Subsitition loops are not supported:
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

    private static Charset UTF8 = Charset.forName("UTF-8");

    private static char[] decryptToCharArray(String value, char[] gpgpw)
	throws GeneralSecurityException
    {
	if (value == null || gpgpw == null) return null;
	byte[] data = Base64.getDecoder().decode(value);
	ByteArrayInputStream is = new ByteArrayInputStream(data);

	try {
	    // Need to use --batch, etc. because when this runs in
	    // a dialog box, we don't have access to a terminal and
	    // GPG agent won't ask for a passphrase.
	    ProcessBuilder pb = new ProcessBuilder("gpg",
						   "--pinentry-mode",
						   "loopback",
						   "--passphrase-fd", "0",
						   "--batch", "-d"/*,
						   tmpf.getCanonicalPath()*/);
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
     */
    public static Properties newInstance(File f) throws IOException {
	Properties props = new Properties();
	InputStream is = new FileInputStream(f);
	props.load(is);
	is.close();
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
     */
    public static Properties newInstance(File f,
					 String mediaType)
	throws IOException
    {
	if (f == null) {
	    throw new IOException("noFileSpecified");
	}
	Reader r = new InputStreamReader(new FileInputStream(f),
					 UTF8);
	String comment = "#(!M.T " + mediaType.trim().toLowerCase() + ")\r\n";
	char[] cbuf1 = comment.toCharArray();
	char[] cbuf2 = new char[cbuf1.length];
	int n = r.read(cbuf2);
	if (n != cbuf2.length) {
	    throw new IOException(errorMsg("wrongMediaType", f.toString()));
	}
	for  (int i = 0; i < n; i++) {
	    if (i < 7) {
		if (cbuf1[i] != cbuf2[i]) {
		    throw new IOException(errorMsg("wrongMediaType",
						   f.toString()));
		}
	    } else {
		if (cbuf1[i] != Character.toLowerCase(cbuf2[i])) {
		    throw new IOException(errorMsg("wrongMediaType",
						   f.toString()));
		}
	    }
	}
	Properties properties = new Properties();
	properties.load(r);
	r.close();
	return properties;
    }




    private static final String B64KEY_START = "base64.";
    private static final String EB64KEY_START = "ebase64.";

    /**
     * Get the GPG passphrase.
     * The default supplier obtains the passphrase from the system
     * console.
     * @param supplier a {@link Supplier} that will provide the
     *        passphrase; null for a default
     * @return the passphrase
     */
    public static char[] getGPGPassphrase(Supplier<char[]> supplier) {
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
	if (key.startsWith(EB64KEY_START)) {
	    String encrypted = props.getProperty(key);
	    if (encrypted == null) return EMPTY_CHAR_ARRAY;
	    return decryptToCharArray(encrypted, passphrase);
	} else {
	    return getProperty(props, key).toCharArray();
	}
    }

    private static final Pattern keyPattern =
	Pattern.compile("[a-zA-Z][a-zA-Z0-9_]*([.][a-zA-Z][a-zA-Z0-9_]*)*");
    static final Pattern pattern =
	Pattern.compile(Pattern.quote("$(")
			+ "([a-zA-Z][a-zA-Z0-9_]*([.][a-zA-Z][a-zA-Z0-9_]*)*)"
			+ Pattern.quote(")"));


    /**
     * Get the value stored in an instance of {@link Properties} under
     * a given key.
     * Values whose keys start with "base64." are decoded using a Base-64
     * decoder. Otherwise, the sequence "$(KEY)", where KEY is some key,
     * is replaced with the value stored for KEY in the value provided
     * for the given key.
     * <P>
     * There are no checks for loops: the class
     * {@link org.bzdev.swing.ConfigPropertyEditor} will normally be
     * used to create the property file, and this class will check for
     * loops.
     * @param props the instance of {@link Properties} storing key-value
     *        pairs.
     * @param key the key
     * @return the value for the given key
     */
    public static String getProperty(Properties props, String key)
	throws IllegalStateException
    {
	if (key.startsWith(EB64KEY_START)) {
	    throw new IllegalStateException(errorMsg("needPassphrase", key));
	} else if (key.startsWith(B64KEY_START)) {
	    String value = props.getProperty(key);
	    if (value == null) return "";
	    byte[] data = Base64.getDecoder().decode(value);
	    ByteArrayInputStream is = new ByteArrayInputStream(data);
	    StringBuilder sb = new StringBuilder();
	    try {
		CopyUtilities.copyStream(is, sb, UTF8);
	    } catch (IOException eio) {}
	    return sb.toString();
	} else {
	    String value = props.getProperty(key);
	    if (value == null) return "";
	    Matcher matcher = pattern.matcher(value);
	    int index = 0;
	    StringBuilder sb = null;
	    while (matcher.find(index)) {
		if (sb == null) sb = new StringBuilder();
		int start = matcher.start();
		int end = matcher.end();
		String pkey = value.substring(start+2, end-1);
		sb.append(value.substring(index, start));
		String pval = getProperty(props, pkey);
		if (pval != null) {
		    sb.append(pval);
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
}
