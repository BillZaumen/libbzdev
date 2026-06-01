package org.bzdev.ejws;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.bzdev.lang.UnexpectedExceptionError;

/**
 * Get an authorization code.
 * This code can be used to determine if a user should be
 * immediately made active.  In this case, the user will most
 * likely be sent a link for setting up an account. That link
 * will include the authentication code.  For example,
 * <BLOCKQUOTE><CODE>
 * &lt;https://example.com/site/login?user=user1%40example.com&amp;
 * uploadtype=password&amp;
 * authcode=a4123914091a471f9adf4a4370072f2d&gt;
 * </CODE></BLOCKQUOTE>
 * <P>
 * The code is computed using the MD5 message digest, mainly
 * so that the code is not too long. While less secure than
 * more recent message digest, the use case is one in which it
 * would be unlikely for a third party to even find an example
 * of a user name and the corresponding authorization code.
 * @see EjwsAuthenticator#createAuthCode(String)
 */
public class AuthCode {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    byte[] secret;
    MessageDigest digest;

    static char[] nibbles = {
	'0', '1', '2', '3', '4', '5', '6', '7',
	'8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };





    /**
     * Constructor.
     * The argument should be random string containing at least
     * 16 pairs of hexidecimal digits.
     * @param secret a string known only to the user
     */
    public AuthCode(String secret) {
	this.secret = secret.getBytes(UTF8);
	try {
	    digest = MessageDigest.getInstance("MD5");
	} catch (NoSuchAlgorithmException e) {
	    throw new UnexpectedExceptionError(e);
	}
	digest.update(secret.getBytes(UTF8));
    }

    /**
     * Get an authorization code.
     * <P>
     * The return value is case sensitive.
     * @param uname a user name or email address
     * @return the authorization code.
     */
    public String getCode(String uname) {
	try {
	    MessageDigest d = (MessageDigest)digest.clone();
	    byte[] bytes = d.digest(uname.getBytes(UTF8));
	    char[] chars = new char[2*bytes.length];
	    int j = 0;
	    for (int i = 0; i < bytes.length; i++, j += 2) {
		byte b = bytes[i];
		byte low = (byte)(b & 0xf);
		byte high = (byte)((b >>> 4) & 0xf);
		chars[j] = nibbles[high];
		chars[j+1] = nibbles[low];
	    }
	    return new String(chars);
	} catch (CloneNotSupportedException ec) {
	    throw new UnexpectedExceptionError(ec);
	}
    }
}
