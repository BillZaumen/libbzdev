package org.bzdev.util;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.util.Properties;
import java.util.Set;

//@exbundle org.bzdev.util.lpack.Util

/**
 * Convenience class used to create a {@link java.util.Properties} instance
 * formatted to match the {@link org.bzdev.swing.ConfigPropertyEditor}
 * conventions.
 * The "set" methods return the object being modified to allow
 * methods to be chained. For example,
 * <BLOCKQUOTE><PRE><CODE>
 * ConfigProperties props =
 *       new ConfigProperties("application/vnd.bzdev.sblauncher")
 *       .setProperty("user.description", "Example")
 *       .setProperty("user.base", "https://example.com/contents/")
 *       .setProperty("user.uri", "$(user.base)/login.html")
 *       ...;
 * </CODE></PRE></BLOCKQUOTE>
 * <P>
 * Mostly, the methods in this class merely call the corresponding
 * static methods provided by {@link ConfigPropUtilities}.
 */
public class ConfigProperties {

    private Properties props;


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
    private String mediaType;

    /**
     * Constructor.
     * @param mediaType the media (MIME) type
     */
    public ConfigProperties(String mediaType) {
	this.props = new Properties();
	this.mediaType = mediaType;
    }

    private static InputStream getISFromFile(File f) throws IOException {
	if (f == null) {
	    throw new IOException("noFileSpecified");
	}
	return new FileInputStream(f);
    }

    /**
     * Constructor given a file.
     * Media types are encoded in the first line of the file, which
     * is expected to be
     * <BLOCKQUOTE><PRE><CODE>
     * #(!M.T MEDIATYPE)
     * </CODE></PRE></BLOCKQUOTE>
     * where MEDIATYPE is the media (or MIME type) as defined in
     * RFC 2045 and subsequent RFCs.  The mediatype is converted to lower
     * case for testing.
     * @param f the file to load
     * @param mediaType the media (MIME) type
     * @throws IOException if an IO error occurred
     */
    public ConfigProperties(File f, String mediaType)
	throws IOException
    {
	this(getISFromFile(f), mediaType);
    }

    /**
     * Constructor given an input stream.
     * Media types are encoded in the first line of the file, which
     * is expected to be
     * <BLOCKQUOTE><PRE><CODE>
     * #(!M.T MEDIATYPE)
     * </CODE></PRE></BLOCKQUOTE>
     * where MEDIATYPE is the media (or MIME type) as defined in
     * RFC 2045 and subsequent RFCs.  The mediatype is converted to lower
     * case for testing.
     * @param is the input stream
     * @param mediaType the media (MIME) type
     * @throws IOException if an IO error occurred
     */
    public ConfigProperties(InputStream is, String mediaType)
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
	// handle the case where lines are terminated by LF instead of
	// CRLF, which might happen if a file was opened with a text editor.
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
	this.props = new Properties();
	props.load(r);
	this.mediaType = mediaType;
    }

    /**
     * Constructor given a string containing Base64-encoded GZIPed
     * {@link ConfigProperties} file.
     * Media types are encoded in the first line of the file, which
     * is expected to be
     * <BLOCKQUOTE><PRE><CODE>
     * #(!M.T MEDIATYPE)
     * </CODE></PRE></BLOCKQUOTE>
     * where MEDIATYPE is the media (or MIME type) as defined in
     * RFC 2045 and subsequent RFCs.  The mediatype is converted to lower
     * case for testing.
     * @param b64data a Base-64 encoded string representing this object.
     * @param mediaType the media (MIME) type
     * @throws IOException if the media type does not match that of the
     *         Base-64 encoded representation
     */
    public ConfigProperties(String b64data, String mediaType)
	throws IOException
    {
	props = ConfigPropUtilities.newInstance(b64data, mediaType);
	this.mediaType = mediaType;
    }

    /**
     * Get the current property names.
     * @return the property names
     */
    public Set<String> getKeys() {
	return props.stringPropertyNames();
    }

    /**
     * Get the value, decrypted if necessary, stored in this object
     * under a given key.
     * @param key the key
     * @param passphrase the GPG passphrase for decryption.
     * @return the decrypted value
     * @throws GeneralSecurityException if decryption failed
     */
    public char[] getDecryptedProperty(String key, char[] passphrase)
	throws GeneralSecurityException
    {
	return ConfigPropUtilities.getDecryptedProperty(props, key, passphrase);
    }

    /**
     * Get the value, decrypted if necessary, stored this object
     * under a given key and a GPG home directory.
     * The GPG home directory is the argument for the GPG --homedir
     * command-line option.
     * When gpgdir is non-null, a GPG TOFU (Trust On First Use) trust
     * model is used.
     * @param key the key
     * @param passphrase the GPG passphrase for decryption
     * @param gpgdir the GPG 'home' directory to use
     * @return the decrypted value
     * @throws GeneralSecurityException if decryption failed
     */
    public char[] getDecryptedProperty(String key,
				       char[] passphrase,
				       String gpgdir)
	throws GeneralSecurityException
    {
	return ConfigPropUtilities
	    .getDecryptedProperty(props, key, passphrase, gpgdir);
    }

    /**
     * Get the value stored in this object under a given key.
     * Values whose keys start with "base64." are decoded using a Base-64
     * decoder. Otherwise, the sequence "$(KEY)", where KEY is some key,
     * is replaced with the value stored for KEY in the value provided
     * for the given key.
     * <P>
     * If an error occurs due to a key recursively referencing itself,
     * the method {@link ConfigPropUtilities#getCyclicKeys()}
     * can be used to help find the cycle provided that method is
     * called immediately after the exception is thrown.
     * @param key the key
     * @return the value for the given key; null if the key does not exist
     * @throws IllegalStateException if a key is part of a cyclic reference
     */
    public String getProperty(String key)
	throws IllegalStateException
    {
	return ConfigPropUtilities.getProperty(props, key);
    }

    /**
     * Get the value stored in this object under a given key,
     * optionally providing a literal value.
     * <P>
     * This method is provided primarily for use with
     * {@link setProperty(String,String,boolean)}, and is useful when
     * properties have to be copied "as is" between two instances of this
     * class.
     * @param key the key
     * @param literal true if value of the property should be returned as is;
     *        false for the behavior specified by {@link #getProperty(String)}
     * @return the value for the given key; null if the key does not exist
     */
    public String getProperty(String key, boolean literal) {
	if (literal) {
	    return props.getProperty(key);
	} else {
	    return getProperty(key);
	}
    }


     /**
     * Set a property.
     * When decoded, each '$$' will be replaced with a single
     * '$' and substrings of the form "$(KEY)" will be replaced
     * with the value for the specified KEY, unless the key starts
     * with "base64.", in which case the value will be base-64 encoded.
     * for storage.
     * @param key the property key
     * @param value the property value
     * @throws IllegalArgumentException if the key starts with "ebase64."
     * @see #setProperty(String, String, String, String[])
     * @see #setProperty(String, String, char[])
     */
    public ConfigProperties setProperty(String key, String value)
	throws IllegalArgumentException
    {
	ConfigPropUtilities.setProperty(props, key, value, false);
	return this;
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
     * @param key the property key
     * @param value the property value
     * @param literal true if the value is a literal string; false if
     *        variable substitution is allowed
     * @throws IllegalArgumentException if the key starts with "ebase64."
     * @see #setProperty(String, String, String, String[])
     * @see #setProperty(String, String, char[])
     */
    public ConfigProperties setProperty(String key, String value,
					boolean literal)
	throws IllegalArgumentException
    {
	ConfigPropUtilities.setProperty(props, key, value, literal);
	return this;
    }

    /**
     * Get GPG recipients that were stored using the specified key.
     * @param key the property key
     * @return the recipients
     * @throws IllegalStateException if the object stored using the
     *         specified key cannot be decoded
     */
    public String[] getRecipients(String key)
	throws IllegalStateException
    {
	try {
	    return ConfigPropUtilities.decodeRecipients(props.getProperty(key));
	} catch (RuntimeException e) {
	    String msg = errorMsg("illegalRecipients");
	    throw new IllegalStateException(msg, e);
	}
    }


    /**
     * Set GPG recipients that were stored using the specified key.
     * @param key the property key
     * @param recipients the recipients
     * @return this object
     */
    public ConfigProperties setRecipients(String key, String[] recipients) {
	ConfigPropUtilities.setProperty(props, key,
					ConfigPropUtilities
					.encodeRecipients(recipients),
					true);
	return this;
    }

    /**
     * Set a property for this object when symmetric* encryption is used.
     * <P>
     * Note: When a {@link org.bzdev.swing.ConfigPropertyEditor} is used,
     * either all encrypted entries should use either symmetric encryption
     * or public key encryption, but these should not be mixed. All
     * encrypted entries should use the same password or passphrase.
     * @param key the property key
     * @param value the property value
     * @param password the password for symmetric encryption
     * @return this object
     * @throws IllegalArgumentException if the password is null or empty
     * @see #setProperty(String, String, String, String[])
     * @see #setProperty(String, String)
     */
    public ConfigProperties setProperty(String key, String value,
					char[] password)
	throws IllegalArgumentException
    {
	ConfigPropUtilities.setProperty(props, key, value, password);
	return this;
    }

    /**
     * Set a property for this object when symmetric* encryption is used
     * and the value is a char array.
     * <P>
     * Note: When a {@link org.bzdev.swing.ConfigPropertyEditor} is used,
     * either all encrypted entries should use either symmetric encryption
     * or public key encryption, but these should not be mixed. All
     * encrypted entries should use the same password or passphrase.
     * @param key the property key
     * @param value the property value
     * @param password the password for symmetric encryption
     * @return this object
     * @throws IllegalArgumentException if the password is null or empty
     * @see #setProperty(String, String, String, String[])
     * @see #setProperty(String, String)
     */
    public ConfigProperties setProperty(String key, char[] value,
					char[] password)
	throws IllegalArgumentException
    {
	ConfigPropUtilities.setProperty(props, key, value, password);
	return this;
    }

    /**
     * Set a property for this object when GPG encryption is used.
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
     * @param key the property key
     * @param value the property value
     * @param gpgdir The GPG home directory used to store public an private
     *        keys; null for the default
     * @param recipients the recipients
     * @throws IllegalArgumentException if there are no recipients or
     *         if the key does not start with "ebase64"
     * @see #setProperty(String, String, char[])
     * @see #setProperty(String, String)
     */
    public ConfigProperties setProperty(String key, String value,
					String gpgdir, String[] recipients)
    {
	ConfigPropUtilities.setProperty(props, key, value,
					    gpgdir, recipients);
	return this;
    }

    /**
     * Set a property for this object when GPG encryption is used and
     * the value of the property is a char array.
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
     * @param key the property key
     * @param value the property value
     * @param gpgdir The GPG home directory used to store public an private
     *        keys; null for the default
     * @param recipients the recipients
     * @throws IllegalArgumentException if there are no recipients or
     *         if the key does not start with "ebase64"
     * @see #setProperty(String, String, char[])
     * @see #setProperty(String, String)
     */
    public ConfigProperties setProperty(String key, char[] value,
					String gpgdir, String[] recipients)
    {
	ConfigPropUtilities.setProperty(props, key, value,
					    gpgdir, recipients);
	return this;
    }


    /**
     * Store this object, given an output file, using
     * the {@link org.bzdev.swing.ConfigPropertyEditor} format
     * The file is created by using {@link Properties#store(Writer,String)}
     * with a writer that uses the UTF-8 character set with CRLF line
     * separators. The file will start with a comment
     * "#(!M.T " MEDIATYPE)" where MEDIATYPE is the media type in lower case.
     * @param file the file
     * @throws IOException if an IO error occurred
     */
    public ConfigProperties store(File file)
	throws IOException
    {
	ConfigPropUtilities.store(props, file, mediaType);
	return this;
    }    

    /**
     * Store this object given an output stream, using
     * the {@link org.bzdev.swing.ConfigPropertyEditor} format.
     * The output is created by using {@link Properties#store(Writer,String)}
     * with a writer that uses the UTF-8 character set with CRLF line
     * separators. The output will start with a comment
     * "#(!M.T " MEDIATYPE)" where MEDIATYPE is the media type in lower case.
     * @param os the output stream
     * @throws IOException if an IO error occurred
     */
    public ConfigProperties store(OutputStream os)
	throws IOException
    {
	ConfigPropUtilities.store(props, os, mediaType);
	return this;
    }

    /**
     * Store this object as a Base-64 encoded string.
     * A byte array is produced by in effect first creating a text
     * file using the UTF-8 charset and with a CRLF sequence
     * terminating each line, optionally compressing that file.  The
     * text-file format is that produced by
     * {@link Properties#store(Writer,String)}. This byte array is
     * finally base-64 encoded and turned into a string using the UTF-8
     * character set.
     * The properties file will start with a comment
     * "#(!M.T " MEDIATYPE)" where MEDIATYPE is the media type in lower case.
     * @return the Base-64 encoded string.
     */
    public String store() {
	return ConfigPropUtilities.store(props, mediaType);
    }

    /**
     * Store this object as a byte array.
     * The byte array is produced by in effect first creating a text
     * file using the UTF-8 charset and with a CRLF sequence
     * terminating each line, optionally compressing that file.  The
     * text-file format is that produced by
     * {@link Properties#store(Writer,String)}.
     * The properties file will start with a comment
     * "#(!M.T " MEDIATYPE)" where MEDIATYPE is the media type in lower case.
     * @param gzip true if the result is compressed using GZIP
     * @return an array of bytes containing the properties
     */
    public byte[] storeBytes(boolean gzip) {
	return ConfigPropUtilities.storeBytes(props, mediaType, gzip);
    }

}
