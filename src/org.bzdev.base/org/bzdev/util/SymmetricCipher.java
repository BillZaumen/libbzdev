package org.bzdev.util;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.CharBuffer;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.security.*;
import java.security.spec.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import org.bzdev.lang.UnexpectedExceptionError;

/**
 * Implementation of a symmetric cipher using passwords.
 * <P>
 * Data is encrypted using 128 bit AES/GCM without padding.
 * The format for the encrypted data consists of an 8 byte
 * field containing a "salt", followed by a 12 byte field
 * containing an initial vector, followed by the AES/GCM
 * encrypted data.  For decryption, the salt is used to
 * reconstruct a secret key from the password, and the
 * initial vector is used to initialize the cipher used
 * for decryption.
 * <P>
 * Use cases include storing private keys from a key pair
 * locally in cases where any reasonable cipher is sufficient
 * and where performance is not an issue.
 * This class is not meant to be a replacement for the Java
 * APIs. For example, it would not be appropriate for encrypting
 * a large file because the whole file would have to be read
 * into memory before the encryption started, requiring the
 * allocation of large arrays.
 */
public class SymmetricCipher {

    public static SecretKey getKeyFromPW(char[] pw, byte[] salt)
	throws GeneralSecurityException
    {
	SecretKeyFactory factory = SecretKeyFactory
	    .getInstance("PBKDF2WithHmacSHA256");
	KeySpec spec = new PBEKeySpec(pw, salt, 1<<16, 128);
	return new SecretKeySpec(factory.generateSecret(spec).getEncoded(),
				 "AES");
    }

    
    private static final int SALTLEN = 8;
    private static final int IVLEN = 12;
    private static final int HDRLEN = SALTLEN + IVLEN;

    private static byte[] getSalt() {
	byte[] iv = new byte[SALTLEN];
	new SecureRandom().nextBytes(iv);
	return iv;
    }


    private static byte[] getIV() {
	byte[] iv = new byte[IVLEN];
	new SecureRandom().nextBytes(iv);
	return iv;
    }

    private static GCMParameterSpec getIV(byte[] iv)
	throws IllegalArgumentException
    {
	return new GCMParameterSpec(128, iv);
    }

    /**
     * Encrypt data given a password.
     * @param password the password
     * @param data the data to encrypt
     * @return the encrypted data
     * @throws GeneralSecurityException if an error occurred
     */
    public static byte[] encrypt(char[] password,
				 byte[] data)
	throws GeneralSecurityException
    {
	byte[] salt = getSalt();
	SecretKey key = getKeyFromPW(password, salt);
	Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
	byte[] ivBytes  = getIV();
	GCMParameterSpec iv = getIV(ivBytes);
	
	cipher.init(Cipher.ENCRYPT_MODE, key, iv);
	byte[] encrypted1 = cipher.update(data, 0, data.length);
	if (encrypted1 == null) encrypted1 = new byte[0];
	byte[] encrypted2 = cipher.doFinal();
	if (encrypted2 == null) encrypted2 = new byte[0];
	int resultLen = HDRLEN + encrypted1.length + encrypted2.length;
	byte[] result = new byte[resultLen];

	System.arraycopy(salt, 0, result, 0, SALTLEN);
	System.arraycopy(ivBytes, 0, result, SALTLEN, IVLEN);
	System.arraycopy(encrypted1, 0, result, HDRLEN, encrypted1.length);
	System.arraycopy(encrypted2, 0, result, HDRLEN + encrypted1.length,
			 encrypted2.length);
	return result;
    }

    /**
     * Encrypt data given a password provided as a
     * {@link java.lang.String String}.
     * Normally one should not provide a password as a string as the
     * string may persist in memory. This method is provided for
     * convenience when an application runs for a trivial amount of time
     * or when used with a scripting language that cannot support
     * char arrays.
     * @param password the password
     * @param data the data to encrypt
     * @return the encrypted data
     * @throws GeneralSecurityException if an error occurred
     */
    public static byte[] encrypt(String password, byte[] data)
	throws GeneralSecurityException
    {
	return encrypt(password.toCharArray(), data);
    }

    /**
     * Encrypt a {@link java.lang.String String} given a password.
     * @param password the password
     * @param string the data to encrypt
     * @return the encrypted data
     * @throws GeneralSecurityException if a security error occurred
     */
    public static byte[] encrypt (char[] password, String string)
	throws GeneralSecurityException
    {
	try {
	    return encrypt(password, string.getBytes("UTF8"));
	} catch (UnsupportedEncodingException e) {
	    throw new UnexpectedExceptionError(e);
	}
    }

    private static final Charset UTF8 = Charset.forName("UTF-8");

    /**
     * Encrypt an array of characters given a password.
     * The character data will be encoded using the UTF-8 charset.
     * @param password the password
     * @param carray the data to encrypt
     * @return the encrypted data
     * @throws GeneralSecurityException if a security error occurred
     */
    public static byte[] encrypt(char[] password, char[]carray)
	throws GeneralSecurityException
    {
	try {
	    CharBuffer cb = CharBuffer.wrap(carray);
	    return encrypt( password, UTF8.encode(cb).array());
	} catch (ReadOnlyBufferException e) {
	    throw new UnexpectedExceptionError(e);
	} catch(UnsupportedOperationException e) {
	    throw new UnexpectedExceptionError(e);
	}
    }

    /**
     * Encrypt an array of characters given a password provided as a string.
     * The character data will be encoded using the UTF-8 charset.
     * @param password the password
     * @param carray the data to encrypt
     * @return the encrypted data
     * @throws GeneralSecurityException if a security error occurred
     */
    public static byte[] encrypt(String password, char[] carray)
	throws GeneralSecurityException
    {
	return encrypt(password.toCharArray(), carray);
    }


    /**
     * Encrypt a {@link java.lang.String String} given a password
     * provided as a {@link java.lang.String String}.
     * Normally one should not provide a password as a string as the
     * string may persist in memory. This method is provided for
     * convenience when an application runs for a trivial amount of time
     * or when used with a scripting language that cannot support
     * char arrays.
     * The string provided as an argument will be encoded using UTF-8 before
     * being encrypted.
     * @param password the password
     * @param string the data to encrypt
     * @return the encrypted data
     * @throws GeneralSecurityException if a security error occurred
     */
    public static byte[] encrypt(String password, String string)
	throws GeneralSecurityException {
	try {
	    return encrypt(password.toCharArray(), string.getBytes("UTF8"));
	} catch (UnsupportedEncodingException e) {
	    throw new UnexpectedExceptionError(e);
	}
    }

    /**
     * Decrypt data given a password.
     * @param password the password
     * @param data encrypted data, encrypted by {@link #encrypt(char[],byte[])}
     * @return the decrypted data
     * @throws GeneralSecurityException if a security error occurred
     */
    public static byte[] decrypt(char[] password,
				 byte[] data)
	throws GeneralSecurityException {
	Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
	byte[] salt = new byte[SALTLEN];
	byte[] ivBytes = new byte[IVLEN];
	System.arraycopy(data, 0, salt, 0, SALTLEN);
	SecretKey key = getKeyFromPW(password, salt);
	System.arraycopy(data, SALTLEN, ivBytes, 0, IVLEN);
	GCMParameterSpec iv = getIV(ivBytes);
	cipher.init(Cipher.DECRYPT_MODE, key, iv);
	byte[] encrypted1 = cipher.update(data, HDRLEN, data.length - HDRLEN);
	byte[] encrypted2 = cipher.doFinal();
	byte[] result = new byte[encrypted1.length + encrypted2.length];
	System.arraycopy(encrypted1, 0, result, 0, encrypted1.length);
	System.arraycopy(encrypted2, 0, result, encrypted1.length,
			 encrypted2.length);
	return result;

    }

    /**
     * Decrypt data given a password  provided as a
     * {@link java.lang.String String}.
     * Normally one should not provide a password as a string as the
     * string may persist in memory. This method is provided for
     * convenience when an application runs for a trivial amount of time
     * or when used with a scripting language that cannot support
     * char arrays but does support byte arrays.
     * @param password the password
     * @param data encrypted data, encrypted by {@link #encrypt(char[],byte[])}
     * @return the decrypted data
     * @throws GeneralSecurityException if a security error occurred
     */
    public static byte[] decrypt(String password, byte[] data)
	throws GeneralSecurityException {
	return decrypt(password.toCharArray(), data);
    }

    /**
     * Decrypt data representing a {@link java.lang.String String}
     * given a password.
     * @param password the password
     * @param data encrypted data, encrypted by {@link #encrypt(char[],byte[])}
     * @return the decrypted string
     * @throws GeneralSecurityException if a security error occurred
     */
    public static String decryptToString(char[] password, byte[] data)
	throws GeneralSecurityException {
	try {
	    return new String(decrypt(password, data), "UTF8");
	} catch (UnsupportedEncodingException e) {
	    throw new UnexpectedExceptionError(e);
	}
    }

    /**
     * Decrypt data representing a {@link java.lang.String String}
     * given a password provided as a {@link java.lang.String String}.
     * Normally one should not provide a password as a string as the
     * string may persist in memory. This method is provided for
     * convenience when an application runs for a trivial amount of time
     * or when used with a scripting language that cannot support
     * char arrays but does support byte arrays.
     * @param password the password
     * @param data encrypted data, encrypted by {@link #encrypt(char[],byte[])}
     * @return the decrypted string
     * @throws GeneralSecurityException if a security error occurred
     */
    public static String decryptToString(String password, byte[] data)
	throws GeneralSecurityException
    {
	try {
	    return new String(decrypt(password.toCharArray(), data), "UTF8");
	} catch (UnsupportedEncodingException e) {
	    throw new UnexpectedExceptionError(e);
	}	    
    }

    /**
     * Decrypt data representing an array of characters given a password.
     * @param password the password
     * @param data encrypted data, encrypted by {@link #encrypt(char[],byte[])}
     * @return the decrypted character
     * @throws GeneralSecurityException if a security error occurred
     */
    public static char[]  decryptToChars(char[] password, byte[] data)
	throws GeneralSecurityException
    {
	try {
	    return UTF8.decode(ByteBuffer.wrap(decrypt(password, data)))
		.array();
	} catch (ReadOnlyBufferException e) {
	    throw new UnexpectedExceptionError(e);
	} catch(UnsupportedOperationException e) {
	    throw new UnexpectedExceptionError(e);
	}
    }

    /**
     * Decrypt data representing an array of characters
     * given a password provided as a {@link java.lang.String String}.
     * Normally one should not provide a password as a string as the
     * string may persist in memory. This method is provided for
     * convenience when an application runs for a trivial amount of time
     * or when used with a scripting language that cannot support
     * char arrays but does support byte arrays.
     * @param password the password
     * @param data encrypted data, encrypted by {@link #encrypt(char[],byte[])}
     * @return the decrypted character array
     * @throws GeneralSecurityException if a security error occurred
     */
    public static char[]  decryptToChars(String password, byte[] data)
	throws GeneralSecurityException
    {
	try {
	    return UTF8.decode(ByteBuffer.wrap(decrypt(password.toCharArray(),
						       data)))
		.array();
	} catch (ReadOnlyBufferException e) {
	    throw new UnexpectedExceptionError(e);
	} catch(UnsupportedOperationException e) {
	    throw new UnexpectedExceptionError(e);
	}
    }
}
