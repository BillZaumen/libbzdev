package org.bzdev.net;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import javax.net.ssl.*;
import java.security.cert.*;
import java.security.GeneralSecurityException;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.Key;
import java.security.PublicKey;
import java.security.PrivateKey;

import java.security.spec.*;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.zip.CRC32;

import org.bzdev.net.PemEncoder;
import org.bzdev.net.PemDecoder;
import org.bzdev.net.HeaderOps;
import org.bzdev.lang.UnexpectedExceptionError;

//@exbundle org.bzdev.net.lpack.Net

/**
 * Operations for basic authentication with digital signatures and
 * timestamps.
 * <P>
 * This is a support class for an authentications scheme dubbed Secure
 * Basic Authentication.  As a protocol, secure basic authentication
 * is identical to basic authentication as described in RFC 7617.
 * The differences are in how passwords are created and compared, and
 * in how realms are named.  Generally, a secure-basic-authentication
 * password is a URL-safe, base 64 encoding of a sequence of
 * bytes. The first four bytes is a timestamp of a 32-bit two's
 * complement integer, stored in little-endian byte order, providing
 * the time at which the password was created as the number of seconds
 * since 1970-01-01T00:00:00Z. The next four bytes is a 32-bit CRC of
 * the first 4 bytes of the sequence followed by the password as an array
 * of bytes using the UTF-8 character encoding.
 * The remainder of the sequence is either
 * <OL>
 *   <LI> a SHA-256 message digest of (1) the first eight bytes of the sequence
 *        and (2) a password using the UTF-8 character encoding.
 *   <LI> a digital signature of (1) the first eight bytes of the sequence
 *        and (2) a password using the UTF-8 character encoding.
 *   <LI> a digital signature of (1) the first eight bytes of the sequence,
 *        (2) the DER encoding of the public key provided in an
 *        SSL certificate, and (3) a password using the UTF-8 character
 *        encoding.
 * </OL>
 * In all cases, both sides of the connection must use the same
 * password.  When a digital signature is used, the server must store
 * a user's public key and the name of the algorithm used to create the
 * signature.  To distinguish these cases, the realm (described in
 * RFC 7616) is prefaced with the following:
 * <OL>
 *   <LI> <STRONG>[D]</STRONG>. This corresponds to Case 1 above. The
 *        method {@link SecureBasicUtilities#iconedRealm(String)} will
 *        replace this sequence with the Unicode character whose
 *        codepoint is 0x231A, which looks like a watch.
 *   <LI> <STRONG>[S]</STRONG>.  This corresponds to Case 2 above. The
 *        method {@link SecureBasicUtilities#iconedRealm(String)} will
 *        replace this sequence with the Unicode character whose
 *        codepoint is 0x1F58A, which looks like a pen.
 *   <LI> <STRONG>[SC]</STRONG>. This corresponds to Case 3 above.
 *        The method {@link SecureBasicUtilities#iconedRealm(String)} will
 *        replace this sequence with the Unicode character whose
 *        codepoint is 0x1F85, followed by the Unicode character whose
 *        code point is 0x1F512. This combination looks like a pen
 *        followed by a lock.
 * </OL>
 * These emoji are followed optionally by a space (but a space is mandatory
 * if the realm for some reason starts with a space).  Emoji are used
 * for two reasons:
 * <UL>
 *  <LI> it is very unlikely for anyone to use an emoji as part of the
 *       name of a realm.
 *  <LI> emoji are easily distinguished from text.  It is easy to create
 *       a "helper" application that can create the password used in
 *       secure basic authentication and paste that password into a
 *       password field provided by a browser or other program that
 *       does not support secure basic authentication, and the emoji
 *       will allow a user to determine which type of authentication is
 *       expected.
 * </UL>
 * Case 3 is the most secure. When an SSL or TLS connection is
 * established, a server will provide a client with the server's
 * certificate and the SSL or TLS protocol will ensure that the server
 * that provided the certificate has that certificate's private
 * key. This can stop a variety of man-in-the-middle and spoofing
 * attempts, at least to the point of inadvertently disclosing login
 * credentials: in either case the certificate will be different,
 * which means that the password that is generated will not be useful.
 * A CRC is used because this provides a cheap way of rejecting
 * authentication attempts that have the wrong password.
 * <P>
 * A client can create a PEM-encoded  key pair by calling
 * {@link SecureBasicUtilities#createPEMPair()}, which returns an array
 * containing two strings. The first is a PEM encoded private key
 * and the second is a PEM encoded public key.  A user's PEM encoded public
 * key can be provided to a server along with a password.  To create
 * a password a client will call the constructor
 * {@link SecureBasicUtilities#SecureBasicUtilities(String)}, using
 * its PEM-encoded private key as an argument, and then the method
 * {@link SecureBasicUtilities#createPassword(Certificate, char[])} to
 * create the password to be given to the server. In this case the
 * first argument is the certificate provided by the server and the
 * second argument is the user's password.
 * <P>
 * A server will call {@link SecureBasicUtilities#SecureBasicUtilities(String)}
 * using the PEM-encoded public key for a client as an argument.  It
 * will decode the password provided by the user by calling
 * {@link SecureBasicUtilities#decodePassword(String)}, and then
 * {@link SecureBasicUtilities#getTimeDiff(byte[])} to check a timestamp.
 * Then it will call
 * {@link SecureBasicUtilities#checkPassword(byte[],Certificate,String)}
 * with the decoded password as its first argument, the server's certificate
 * as its second argument, and the stored password for the user as its
 * third argument.
 * <P>
 * Finally, there are several 'utility' methods for handling realms,
 * and both a constructor and a method for the case where a key store
 * is used to hold the keys.
 * <P>
 * NOTE: For compatibility with openssl, one should use the keytool
 * program, or
 * {@link SecureBasicUtilities#createPEMPair(File,String,String,String,String,char[])},
 * to generate a key pair as a PKCS #12 file will then be created.
 * The openssl equivalent to
 * <BLOCKQUOTE><PRE><CODE>
 * keytool -genkey -keyalg EC -groupname secp256r1 \
 *         -sigalg SHA256withECDSA -dname CN=nobody@nowhere.com \
 *         -alias key -keypass password -storepass password \
 *         -keystore ecstore.pfx
 * </CODE></PRE></BLOCKQUOTE>
is
 * <BLOCKQUOTE><PRE><CODE>
 *  openssl ecparam -name prime256v1 -genkey -noout -out eckey.pem
 *  openssl req -new -x509 -key eckey.pem -out eccert.pem -days 360
 *  openssl pkcs12 -export -inkey eckey.pem -in eccert.pem \
 *          -name key -out ecstore.pfx
 * </CODE></PRE></BLOCKQUOTE>
 * although the choice of a signature algorithm (used to self sign) may
 * be different.  To add to the confusion, for the elliptic curve used
 * in this example, keytool prefers the name secp256r1 whereas openssl
 * prefers prime256v1.  When openssl is given the name secp256r1, it will
 * indicate that is is using prime256v1, whereas when keytool is given
 * the name prime256v1, it generates an error message. Also keytool must
 * use the same password for the file as for each entry it stores if the
 * file is to be compatible with openssl.
 */
public class SecureBasicUtilities {

    static String
	errorMsg(java.lang.String key, java.lang.Object... args)
    {
	return NetErrorMsg.errorMsg(key, args);
    }

    private static final Charset utf8 = Charset.forName("UTF-8");


    private static final char WATCH = '\u231A';
    private static final char[]  WATCH_ARRAY = {WATCH};
    private static final String WATCH_STRING = new String(WATCH_ARRAY);

    private static final char[] PEN = Character.toChars(0x1F58A);
    private static final String PEN_STRING = new String(PEN);

    private static final char[] LOCK = Character.toChars(0x1F512);
    private static final char  PEN_LOCK_ARRAY[] = {
	PEN[0], PEN[1], LOCK[0], LOCK[1]
    };
    private static final String PEN_LOCK_STRING =
	new String(PEN_LOCK_ARRAY);

    /**
     *  Type of the generated password.
     */
    public static enum Mode {
	/**
	 * The generated password contains a SHA-256 digest of a four
	 * byte timestamp (seconds from 1970-01-01T00:00:00Z in
	 * little-endian order) and a user-supplied password using
	 * UTF-8 encoding. The digest is encoded using the URL-safe
	 * base 64 encoding.
	 */
	DIGEST,
	/**
	 * The generated password contains a digital signature of a
	 * four byte timestamp (seconds from 1970-01-01T00:00:00Z in
	 * little-endian order) and a user-supplied password using
	 * UTF-8 encoding.  The signature is encoded using the
	 * URL-safe base 64 encoding.
	 */
	SIGNATURE_WITHOUT_CERT,

	/**
	 * The generated password contains a digital signature of a
	 * four byte timestamp (seconds from 1970-01-01T00:00:00Z in
	 * little-endian order), the DER encoding of the public key
	 * provided by a server in an SSL/TLS certificate, and finally
	 * a user-supplied password using UTF-8 encoding.  The
	 * signature is encoded using the URL-safe base 64 encoding.
	 */
	SIGNATURE_WITH_CERT,
	/**
	 * The generated password is the user-supplied password
	 */
	PASSWORD
    }

    /**
     * Get the mode for from a string representing an encoded realm.
     * When the return value is not {@link Mode#PASSWORD},
     * an encoded realm will have started with one or two emojis,
     * optionally followed by a space.
     * @param realm the encoded realm.
     * @return the mode {@link Mode#DIGEST},
     *         {@link Mode#SIGNATURE_WITHOUT_CERT},
     *         {@link Mode#SIGNATURE_WITH_CERT}, or
     *         {@link Mode#PASSWORD}
     * @see Mode
     */
    public static Mode getMode(String realm) {
	if (realm == null) return Mode.PASSWORD;
	int len = realm.length();
	if (len > 3) {
	    if (realm.startsWith("[D]")) {
		return Mode.DIGEST;
	    } else if (realm.startsWith("[S]")) {
		return Mode.SIGNATURE_WITHOUT_CERT;
	    } else if (len > 4 && realm.startsWith("[SC]")) {
		return Mode.SIGNATURE_WITH_CERT;
	    }
	}
	return Mode.PASSWORD;
    }

    /**
     * Get the encoded realm for from a string representing a realm.
     * When the mode value is not {@link Mode#PASSWORD},
     * the encoded realm will have started with one or two emojis,
     * followed by a space:
     * <UL>
     *  <LI> for {@link Mode#DIGEST}, the encoded string starts with
     *       the Unicode character whose code point is 0x231A. This
     *       symbol looks like a watch to indicate that the digest
     *       uses a time field in addition to a signature.
     *  <LI> for {@link Mode#SIGNATURE_WITHOUT_CERT}, the encoded
     *       string starts with the Unicode character whose code point
     *       is 0x1F58A. This symbol looks like a pen to indicate that
     *       a digital signature used.
     *  <LI> for {@link Mode#SIGNATURE_WITH_CERT}, the encoded
     *       string starts with the Unicode character whose code point
     *       is 0x1F58A, followed by the Unicode character whose code point
     *       is 0x1F512. The first symbol looks like a pen to indicate that
     *       a digital signature used, and the second looks like a lock
     *       to indicate that the public key from an SSL/TLS certificate
     *       is included in the signature.
     * </UL>
     * @param realm the realm
     * @param mode the mode determining the type of encoding
     *        ({@link Mode#DIGEST},
     *        {@link Mode#SIGNATURE_WITHOUT_CERT},
     *        {@link Mode#SIGNATURE_WITH_CERT}, or
     *        {@link Mode#PASSWORD})
     * @return the encoded realm
     * @see Mode
     */
    public static String encodeRealm(String realm, Mode mode) {
	if (realm == null) realm = "";
	if (mode == null) return realm;
	switch (mode) {
	case DIGEST:
	    return "[D]" + realm;
	case SIGNATURE_WITHOUT_CERT:
	    return "[S]" + realm;
	case SIGNATURE_WITH_CERT:
	    return  "[SC]" + realm;
	case PASSWORD:
	    return realm;
	default:
	    throw new UnexpectedExceptionError();
	}
    }

    /**
     * Get the realm for from a string representing an encoded realm.
     * When the value of the mode used when the realm was encoded is
     * not {@link Mode#PASSWORD}, the encoded realm will have started
     * with one or two emojis, followed optionally by a space.
     * @param realm the realm
     * @return the encoded realm
     * @see SecureBasicUtilities#encodeRealm(String,Mode)
     */
    public static String decodeRealm(String realm) {
	if (realm == null) realm = "";
	int len = realm.length();
	if (len > 2) {
	    if (realm.startsWith("[D]")) return realm.substring(3);
	    else if (realm.startsWith("[S]")) return realm.substring(3);
	    else if (len > 3 && realm.startsWith("[SC]")) {
		return realm.substring(4);
	    }
	}
	return realm;
    }

    public static String iconedRealm(String realm) {
	if (realm == null) realm = "";
	int len = realm.length();
	if (len > 2) {
	    if (realm.startsWith("[D]")) {
		return WATCH_STRING + realm.substring(3);
	    } else if (realm.startsWith("[S]")) {
		return PEN_STRING + " " + realm.substring(3);
	    } else if (len > 3 && realm.startsWith("[SC]")) {
		return PEN_LOCK_STRING + realm.substring(4);
	    }
	}
	return realm;

    }

    /**
     * Get the type of an instance of {@link SecureBasicUtilities}.
     * The type indicates whether or not a private and/or
     * public key is available.
     */
    public static enum Type {
	/**
	 * An instance of {@link SecureBasicUtilities} contains
	 * a private key used for verifying a signature.
	 */
	PUBLIC,
	/**
	 * An instance of {@link SecureBasicUtilities} contains
	 * a private key used for creating a signature.
	 */
	PRIVATE,
	/**
	 * An instance of {@link SecureBasicUtilities} contains
	 * both a private key used for verifying a signature and
	 * a private key used for creating a signature.
	 */
	BOTH,
	/**
	 * An instance of {@link SecureBasicUtilities} contains neither
	 * a public key nor a private key because a message digest
	 * is used instead of a digital signature.
	 *
	 */
	NONE
    }

    private Type type;

    private static final String DEFAULT_SIGALG =  "SHA256withECDSA";
    private static final String DEFAULT_CURVE = "secp256r1";

    PrivateKey privateKey;
    PublicKey publicKey;
    String encryptionAlgorithm;
    String signatureAlgorithm;

    /**
     * Get the type of for this instance of {@link SecureBasicUtilities}.
     * The type determines if whether or not a public key and/or a
     * private key is available.
     * @return the type ({@link Type#PUBLIC},{@link Type#PRIVATE}
     *         {@link Type#BOTH}, or {@link Type#NONE}
     * @see Type
     */
    public Type getType() {return type;}

    /**
     * Get the private key for this instance of {@link SecureBasicUtilities}.
     * @return the private key; null if there is none
     */
    public PrivateKey getPrivateKey() {return privateKey;}

    /**
     * Get the public key for this instance of {@link SecureBasicUtilities}.
     * @return the public key; null if there is none
     */
    public PublicKey getPublicKey() {return publicKey;}

    /**
     * Get the encryption algorithm used for the public and/or private
     * keys for this instance of {@link SecureBasicUtilities}.
     * @return the encryption algorithm
     */
    public String getEncryptionAlgorithm() {return encryptionAlgorithm;}

    /**
     * Get the signature algorithm used for the public and/or private
     * keys for this instance of {@link SecureBasicUtilities}.
     * @return the signature algorithm
     */
    public String getSignatureAlgorithm() {return signatureAlgorithm;}

    /**
     * Get an initialized {@link Signature} for signing.
     * The caller should use the {@link Signature} methods named
     * <CODE>update</CODE> and <CODE>sign</CODE> to create the
     * signature.
     * @return an initialized {@link Signature}; null if there this
     *   object was not created with a private key
     * @throws GeneralSecurityException if the private key is not valid
     * @see Signature
     */
    public Signature getSigner() throws GeneralSecurityException {
	if (privateKey == null) return null;
	String provider = sigpmap.get(signatureAlgorithm);
	Signature signature = (provider == null)?
	    Signature.getInstance(signatureAlgorithm):
	    Signature.getInstance(signatureAlgorithm, provider);
	signature.initSign(privateKey);
	return signature;
    }

    /**
     * Get an initialized {@link Signature} for verification.
     * The caller should use the {@link Signature} methods named
     * <CODE>update</CODE> and <CODE>verify</CODE> to verify the
     * signature.
     * @return an initialized {@link Signature}; null if there this
     *   object was not created with a public key
     * @throws GeneralSecurityException if the public key is not valid
     * @see Signature
     */
    public Signature getVerifier() throws GeneralSecurityException {
	if (publicKey == null) return null;
	String provider = sigpmap.get(signatureAlgorithm);
	Signature signature = (provider == null)?
	    Signature.getInstance(signatureAlgorithm):
	    Signature.getInstance(signatureAlgorithm, provider);
	signature.initVerify(publicKey);
	return signature;
    }


    // used by various  constructors.
    private void initFromPEM(InputStream is)
	throws IOException, IllegalArgumentException,
	       GeneralSecurityException
    {
	PemDecoder.Result result = PemDecoder.decode(is);
	String pemtype = result.getType();
	if (pemtype.equalsIgnoreCase("CERTIFICATE")) {
	    signatureAlgorithm = result.getHeaders()
		.getFirst("signature-algorithm");
	    try (InputStream in = new
		 ByteArrayInputStream(result.getBytes())) {
		CertificateFactory cf =
		    CertificateFactory.getInstance("X.509");
		publicKey = cf.generateCertificate(in).getPublicKey();
		encryptionAlgorithm = publicKey.getAlgorithm();
		type = Type.PUBLIC;
	    }
	} else if (pemtype.endsWith(" PUBLIC KEY")) {
	    encryptionAlgorithm = pemtype.substring(0, pemtype.indexOf(" "));
	    signatureAlgorithm = result.getHeaders()
		.getFirst("signature-algorithm");
	    String provider = pmap.get(encryptionAlgorithm);
	    publicKey = ((provider == null)?
			 KeyFactory.getInstance(encryptionAlgorithm):
			 KeyFactory.getInstance(encryptionAlgorithm,
						provider))
		.generatePublic(new X509EncodedKeySpec(result.getBytes()));
	    type = Type.PUBLIC;
	} else if (pemtype.endsWith(" PRIVATE KEY")) {
	    encryptionAlgorithm = pemtype.substring(0, pemtype.indexOf(" "));
	    signatureAlgorithm = result.getHeaders()
		.getFirst("signature-algorithm");
	    PKCS8EncodedKeySpec spec = new
		PKCS8EncodedKeySpec(result.getBytes(), encryptionAlgorithm);
	    String provider = pmap.get(encryptionAlgorithm);
	    privateKey = ((provider == null)?
			 KeyFactory.getInstance(encryptionAlgorithm):
			 KeyFactory.getInstance(encryptionAlgorithm,
						provider))
		.generatePrivate(spec);
	    type = Type.PRIVATE;
	} else {
	    String msg = errorMsg("PemType", pemtype);
	    throw new IOException(msg);
	}
    }

    /**
     * Constructor for the message-digest case.
     */
    public SecureBasicUtilities() {
	type = Type.NONE;
    }

    /**
     * Constructor given a string containing PEM-encoded data.
     * The encoded string will start with a header,
     * <CODE> signature-algorithm</CODE>, that provides the name of
     * a signature algorithm used in creating or verifying digital
     * signatures. This header's textual representation consists of
     * a line starting with the string "signature-algorithm:", followed
     * by optional white space and the name of the signature algorithm.
     * The default signature algorithm is named  SHA256withECDSA.
     * The header will be followed by PEM encoded data whose
     * type is either <CODE>CERTIFICATE</CODE>,
     * <CODE>EC PUBLIC KEY</CODE>, or <CODE>EC PRIVATE KEY</CODE>
     * (while EC is the default and is preferred for performance
     * reasons, some other algorithm such as RSA can be used instead.
     * can be used if desired, in which case 'EC' should be replaced
     * with the name of the algorithm).
     * <P>
     * Note: when this constructor is used to read a private key, the
     * key must be DER encoded as is done with
     * {@link PrivateKey#getEncoded()}: Java makes it difficult to
     * directly read a private key, preferring the use of a key store
     * instead.
     * @param pem the PEM string.
     */
    public SecureBasicUtilities(String pem)
	throws IOException, IllegalArgumentException,
	       GeneralSecurityException

    {
	this(new ByteArrayInputStream(pem.getBytes("UTF-8")));
    }

    /**
     * Constructor given an input stream.
     * The input stream will start with a header,
     * <CODE> signature-algorithm</CODE>, that provides the name of
     * a signature algorithm used in creating or verifying digital
     * signatures. This header's textual representation consists of
     * a line starting with the string "signature-algorithm:", followed
     * by optional whitespace and the name of the signature algorithm.
     * The header will be followed by PEM encoded data whose
     * type is either <CODE>CERTIFICATE</CODE>,
     * <COdE>EC PUBLIC KEY</CODE>, or <CODE>EC PRIVATE KEY</CODE>
     * (while EC is the default and is preferred for performance
     * reasons) some other algorithm such as RSA can be used instead.
     * can be used if desired). The data must be UTF-8 encoded.
     * @param is the input stream
     */
    public SecureBasicUtilities(InputStream is)
	throws IOException, IllegalArgumentException,
	       GeneralSecurityException
    {
	initFromPEM(is);
    }


    /**
     * Constructor given a key store.
     * The key store should use the PKCS#12 format, but this is not
     * required. The same password has to be used for the keystore itself
     * and for the private key corresponding to the alias.
     * <P>
     * This constructor is particularly useful if the keys are created
     * using a program such as openssl: with the exception of some special
     * cases, Java makes it difficult to read in private keys without using
     * a key store.
     * @param file the file containing the key store
     * @param sigalg the signature algorithm (e.g., SHA256withECDSA);
     *        null for the default
     * @param alias a name used to look up a key or certificate
     * @param pwarray an array of characters providing a password
     */
    public SecureBasicUtilities(File file, String sigalg,
			  String alias,
			  char[] pwarray)
	throws IOException, IllegalArgumentException,
	       GeneralSecurityException
    {
	if (sigalg == null) sigalg = DEFAULT_SIGALG;
	if (alias != null) {
	    KeyStore ks = KeyStore.getInstance(file, pwarray);
	    Key key = ks.getKey(alias, pwarray);
	    if (key instanceof PrivateKey) {
		privateKey = (PrivateKey)key;
	    } else {
		throw new IllegalArgumentException();
	    }
	    Certificate cert = ks.getCertificate(alias);
	    if (cert == null) {
		throw new IllegalArgumentException(errorMsg("nocert", alias));
	    }
	    publicKey = cert.getPublicKey();
	    if (!publicKey.getAlgorithm().equals(privateKey.getAlgorithm())) {
		throw new IllegalArgumentException();
	    }
	    signatureAlgorithm = sigalg;
	    type = Type.BOTH;
	} else {
	    initFromPEM(new FileInputStream(file));
	    if (!this.signatureAlgorithm.equals(sigalg)) {
		String alg1 = this.signatureAlgorithm;
		String alg2 = sigalg;
		String msg = errorMsg("sigalgs", alg1, alg2);
		throw new IllegalArgumentException(msg);
	    }
	}
    }

    /*
     * Initialize with set of preferred providers.
     */
    private static HashMap<String,String> sigpmap = new HashMap<>();
    static {
	sigpmap.put("Sha256withECDSA", "SunEC");
    }

    private static HashMap<String,String> pmap = new HashMap<>();
    static {
	sigpmap.put("EC", "SunEC");
    }

    private static final String DIGEST = "SHA-256";
    private static final int DIGEST_LENGTH = 32;

    /**
     * Create a password based on digital signatures or message digests.
     * This method should not be used when the user-supplied password is
     * used as is.
     * @param cert a certificate; null when a certificate is not used.
     * @param password the user-supplied password
     * @return the encoded password
     */
    public char[] createPassword(Certificate cert,
				 char[] password)
	throws GeneralSecurityException,
	       UnsupportedEncodingException
    {
	int len;
	byte[] sigbytes;
	CharBuffer cbuf = CharBuffer.wrap(password);
	ByteBuffer bbuf = utf8.encode(cbuf);
	byte[] pwbytes = new byte[bbuf.limit() - bbuf.position()];
	bbuf.get(pwbytes);
	long time = Instant.now().getEpochSecond() & 0xFFFFFFFFL;
	long tm = time;
	byte[] tarray = new byte[4];
	for (int i = 0; i < 4; i++) {
	    tarray[i] = (byte)(tm & 0x000000FF);
	    tm = tm >> 8;
	}

	CRC32 crc32 = new CRC32();
	crc32.update(tarray, 0, tarray.length);
	crc32.update(pwbytes, 0, pwbytes.length);
	long crc = crc32.getValue();
	if (type == Type.NONE) {
	    sigbytes = new byte[8+DIGEST_LENGTH];
	    // byte[] pwbytes = password.getBytes(utf8);
	    /*
	     * Store in little endian byte order so the most rapidly
	     * varying value is first.
	     */
	    for (int i = 0; i < 4; i++) {
		sigbytes[i] = (byte)(time & 0x000000FF);
		time = time >> 8;
	    }
	    for (int i = 4; i < 8; i++) {
		sigbytes[i] = (byte)(crc & 0x000000FF);
		crc = crc >> 8;
	    }

	    MessageDigest md = MessageDigest.getInstance("SHA-256");
	    md.update(sigbytes, 0, 8);
	    md.update(pwbytes);
	    md.digest(sigbytes, 8, DIGEST_LENGTH);
	    len = DIGEST_LENGTH + 8;
	} else {
	    String provider = sigpmap.get(signatureAlgorithm);
	    Signature signature = (provider == null)?
		Signature.getInstance(signatureAlgorithm):
		Signature.getInstance(signatureAlgorithm, provider);
	    signature.initSign(privateKey);
	    // need 512 for RSA with a key size of 4096, so 1024
	    // should be large enough even if the key size doubled.
	    sigbytes = new byte[1024+8];
	    /*
	     * Store in little endian byte order so the most rapidly
	     * varying value is first.
	     */
	    for (int i = 0; i < 4; i++) {
		sigbytes[i] = (byte)(time & 0x000000FF);
		time = time >> 8;
	    }
	    for (int i = 4; i < 8; i++) {
		sigbytes[i] = (byte)(crc & 0x000000FF);
		crc = crc >> 8;
	    }
	    signature.update(sigbytes, 0, 8);
	    if (cert != null) {
		signature.update(cert.getPublicKey().getEncoded());
	    }
	    signature.update(pwbytes);
	    len = signature.sign(sigbytes, 8, sigbytes.length - 8);
	    len += 8;
	}
	Base64.Encoder encoder = Base64.getUrlEncoder();
	ByteBuffer target = ByteBuffer.wrap(sigbytes, 0, len);
	target = encoder.encode(target);
	CharBuffer pwbuf = utf8.decode(target);
	char[] array = new char[pwbuf.limit() - pwbuf.position()];
	pwbuf.get(array);
	return array;
	/*
	if (target.hasArray()) {
	    byte[] array = target.array();
	    int start = target.arrayOffset();
	    len = target.limit() - start;
	    return new String(array, start, len, utf8);
	} else {
	    byte[] array = new byte[target.limit() - target.position()];
	    target.get(array);
	    return new String(array, utf8);
	}
	*/
    }

    /*
     * Get the user name, possibly encoded in the password.
     * Normally the user name provided as an argument is returned.
     * If that user name is either null, an empty string, or a
     * string containing only whitepace, and the password starts
     * with a colon (":"), and contains a following colon, the character
     * between those two colons are returned as the user name. Otherwise
     * the specified user name is returned.
     * <P>
     * Note: Passwords generated by
     * {@link #createPassword(Cerficate,char[])} will never contain
     * a colon due to the use of URL-safe base 64 encoding.
     * @param userName the user name
     * @param password the password
     * @return the user name
    public static String decodeUserName(String userName, String password) {
	if (userName == null || userName.trim().length() == 0) {
	    if (password != null && password.charAt(0) == ':') {
		int index = password.indexOf(':', 1);
		if (index > 0) {
		    return password.substring(1, index);
		} else {
		    return userName;
		}
	    } else {
		return userName;
	    }
	} else {
	    return userName;
	}
    }
    */

    /*
     * Get the user name, possibly encoded in the password, with the
     * password represented as an array of characters.
     * Normally the user name provided as an argument is returned.
     * If that user name is either null, an empty string, or a
     * string containing only whitepace, and the password starts
     * with a colon (":"), and contains a following colon, the character
     * between those two colons are returned as the user name. Otherwise
     * the specified user name is returned.
     * <P>
     * Note: Passwords generated by
     * {@link #createPassword(Cerficate,char[])} will never contain
     * a colon due to the use of URL-safe base 64 encoding.
     * @param userName the user name
     * @param password the password
     * @return the user name
      public static String decodeUserName(String userName, char[] password) {
	if (userName == null || userName.trim().length() == 0) {
	    if (password != null && password[0] == ':') {
		int index = 1;
		while (index < password.length) {
		    if (password[index] == ':') break;
		    index++;
		}
		if (index == password.length) {
		    return userName;
		} else {
		    return new String(password, 1, index-1);
		}
	    } else {
		return userName;
	    }
	} else {
	    return userName;
	}
    }
    */
    /*
     * Get the user name, possibly encoded in the password, with the
     * password represented as an array of bytes.
     * Normally the user name provided as an argument is returned.
     * If that user name is either null, an empty string, or a
     * string containing only whitepace, and the password starts
     * with a colon (":"), and contains a following colon, the character
     * between those two colons are returned as the user name. Otherwise
     * the specified user name is returned.
     * <P>
     * Note: Passwords generated by
     * {@link #createPassword(Cerficate,char[])} will never contain
     * a colon due to the use of URL-safe base 64 encoding.
     * @param userName the user name
     * @param password the password
     * @return the user name
    public static String decodeUserName(String userName, byte[] password) {
	if (userName == null || userName.trim().length() == 0) {
	    if (password != null && password[0] == (byte)':') {
		int index = 1;
		while (index < password.length) {
		    if (password[index] == (byte)':') break;
		    index++;
		}
		if (index == password.length) {
		    return userName;
		} else {
		    return new String(password, 1, index-1, utf8);
		}
	    } else {
		return userName;
	    }
	} else {
	    return userName;
	}
    }
     */

    /**
     * Decode an encoded password represented as a string.
     * This method removes base 64, URL encoding.
     * @param password the encoded password
     * @return the bytes that were base 64, URL encoded
     */
    public static byte[] decodePassword(String password) {
	Base64.Decoder decoder = Base64.getUrlDecoder();
	/*
	if (password != null && password.charAt(0) == ':') {
	    int index = password.indexOf(':', 1);
	    if (index > 0) {
		password = password.substring(index+1);
	    }
	}
	*/
	try {
	    return decoder.decode(password.getBytes(utf8));
	} catch (Exception e) {
	    return null;
	}
    }


    /*
    public static boolean isLogout(String password) {
	if (password != null && password.charAt(0) == ':') {
	    int index = password.indexOf(':', 1);
	    if (index > 0) {
		password = password.substring(index+1);
	    }
	}
	return password.equals(":logout:");
    }

    public static boolean isLogout(byte[] password) {
	if (password[0] == (byte)':') {
	    int index = 1;
	    while (index < password.length) {
		if (password[index] == (byte)':') break;
		index++;
	    }
	    if (index < password.length) {
		index++;
		byte[] tmp = new byte[password.length - index];
		int i = 0;
		while (index < password.length) {
		    tmp[i] = (byte)password[index];
		    i++;
		    index++;
		}
		password = tmp;
	    } else {
		return false;
	    }
	}
	if (password.length == 8
	    && password[0] == ':'
	    && password[1] == 'l'
	    && password[2] == 'o'
	    && password[3] == 'g'
	    && password[4] == 'o'
	    && password[5] =='u'
	    && password[6] == 't'
	    && password[7] == ':') {
	    return true;
	}
	return false;
    }

    public static boolean isLogout(char[] password) {
	if (password[0] == ':') {
	    int index = 1;
	    while (index < password.length) {
		if (password[index] == (byte)':') break;
		index++;
	    }
	    if (index < password.length) {
		index++;
		char[] tmp = new char[password.length - index];
		int i = 0;
		while (index < password.length) {
		    tmp[i] = password[index];
		    i++;
		    index++;
		}
		password = tmp;
	    } else {
		return false;
	    }
	}
	if (password.length == 8
	    && password[0] == ':'
	    && password[1] == 'l'
	    && password[2] == 'o'
	    && password[3] == 'g'
	    && password[4] == 'o'
	    && password[5] == 'u'
	    && password[6] == 't'
	    && password[7] == ':') {
	    return true;
	}
	return false;
    }
    */


    /**
     * Decode an encoded password represented as an array of bytes.
     * THis method removes base 64, URL encoding
     * @param password the encoded password using the US ASCII subset
     *        of UTF-8
     * @return the bytes that were base 64, URL encoded
     */
    public static byte[] decodePassword(byte[] password) {
	if (password == null || password.length == 0) {
	    return new byte[0];
	}
	Base64.Decoder decoder = Base64.getUrlDecoder();
	/*
	if (password[0] == (byte)':') {
	    int index = 1;
	    while (index < password.length) {
		if (password[index] == (byte)':') break;
		index++;
	    }
	    if (index < password.length) {
		index++;
		byte[] tmp = new byte[password.length - index];
		int i = 0;
		while (index < password.length) {
		    tmp[i] = (byte)password[index];
		    i++;
		    index++;
		}
		password = tmp;
	    } else {
		return new byte[0];
	    }
	}
	*/
	try {
	    return decoder.decode(password);
	} catch (Exception e) {
	    return null;
	}
    }

    /**
     * Decode an encoded password represented as an array of chars.
     * THis method removes base 64, URL encoding
     * @param password the encoded password using the US ASCII subset
     *        of UTF-8
     * @return the bytes that were base 64, URL encoded
     */
    public static byte[] decodePassword(char[] password) {
	Base64.Decoder decoder = Base64.getUrlDecoder();
	/*
	if (password[0] == ':') {
	    int index = 1;
	    while (index < password.length) {
		if (password[index] == ':') break;
		index++;
	    }
	    if (index < password.length) {
		index++;
		char[] tmp = new char[password.length - index];
		int i = 0;
		while (index < password.length) {
		    tmp[i] = password[index];
		    i++;
		    index++;
		}
		password = tmp;
	    } else {
		return new byte[0];
	    }
	}
	*/
	byte[] encoded = new byte[password.length];
	for (int i = 0; i < password.length; i++) {
	    encoded[i] = (byte) password[i];
	}
	try {
	    return decoder.decode(encoded);
	} catch (Exception e) {
	    return null;
	} finally {
	    for (int i = 0; i < password.length; i++) {
		encoded[i] = 0;
	    }
	}
    }


    /**
     * Given a decoded password, find the time difference
     * between the current time and the password's timestamp.
     * A small negative value can occur when clocks are not
     * synchronized.  When the exact time is used, the value
     * should be non-negative as the password will be checked
     * after it is created.
     * @param sigarray the decoded password
     * @return the difference between the current time and the
     *         password's timestamp in units of seconds
     * @see #decodePassword(String)
     */
    public static int getTimeDiff(byte[] sigarray) {
	if (sigarray == null || sigarray.length < 4) return Integer.MAX_VALUE;
	int t = 0;
	for (int i = 3; i > 0; i--) {
	    t |= sigarray[i] & 0xFF;
	    t = t << 8;
	}
	t |= sigarray[0] & 0xFF;
	int t0 = (int)(Instant.now().getEpochSecond() & 0xFFFFFFFFL);
	return t0 - t;
    }

    private static long getCRC32(byte[] sigarray) {
	long value = 0;
	for (int i = 7; i > 4; i--) {
	    value |= sigarray[i] & 0xFF;
	    value = value << 8;
	}
	value |= sigarray[4] & 0xFF;
	return value;
    }

    /**
     * Determine if a password is valid.
     * @param sigarray the decoded password
     * @param cert the certificate used when a password is created;
     *        null for digest authentication
     * @return true if the password is valid; false otherise
     * @see #decodePassword(String)
     */
    public boolean checkPassword(byte[] sigarray,
				 Certificate cert,
				 String password)

	throws GeneralSecurityException
    {
	if (sigarray == null) return false;
	if (sigarray.length <= 8) return false;
	byte[] pwbytes = password.getBytes(utf8);
	CRC32 crc32 = new CRC32();
	crc32.update(sigarray, 0, 4);
	crc32.update(pwbytes, 0, pwbytes.length);
	long crc = crc32.getValue();
	long crc2 = getCRC32(sigarray);
	if (crc != crc2) return false;
	if (type == Type.NONE) {
	    int len = DIGEST_LENGTH + 8;
	    if (sigarray.length != len) {
		return false;
	    }
	    byte[] sigbytes = new byte[8+DIGEST_LENGTH];
	    MessageDigest md = MessageDigest.getInstance("SHA-256");
	    md.update(sigarray, 0, 8);
	    md.update(pwbytes);
	    md.digest(sigbytes, 8, DIGEST_LENGTH);
	    boolean ok = true;
	    for (int i = 8; i < len; i++) {
		if (sigbytes[i] != sigarray[i]) return false;
	    }
	    return true;
	} else {
	    String provider = sigpmap.get(signatureAlgorithm);
	    Signature sig = (provider == null)?
		Signature.getInstance(signatureAlgorithm):
		Signature.getInstance(signatureAlgorithm, provider);
	    sig.initVerify(publicKey);
	    sig.update(sigarray, 0, 8);
	    if (cert != null) {
		sig.update(cert.getPublicKey().getEncoded());
	    }
	    sig.update(pwbytes);
	    return sig.verify(sigarray, 8, sigarray.length - 8);
	}
    }

    /**
     * Get PEM-encoded strings representing public and private keys
     * with the signature algorithm as a header.
     * @return an array whose first element contains the PEM-encoded
     *         private key and whose second element contsins the PEM
     *         encoded public key, with either null if the key is not
     *         present
     */
    public String[] getPEMStrings()
    {
	return getPEMStrings(privateKey, publicKey, signatureAlgorithm);
    }

    private static String[] getPEMStrings(PrivateKey privateKey,
				       PublicKey publicKey,
				       String sigalg)
	throws IllegalStateException
    {
	StringBuilder pkbuf = new StringBuilder();
	if (privateKey != null && publicKey != null) {
	    String alg = privateKey.getAlgorithm();
	    if (!alg.equals(publicKey.getAlgorithm())) {
		throw new IllegalStateException(errorMsg("keyalgs"));
	    }
	}
	try {
	    PemEncoder encoder =new PemEncoder(pkbuf);
	    String string1 = null;
	    if (privateKey != null) {
		encoder.addHeader("signature-algorithm", sigalg);
		encoder.encode(privateKey.getAlgorithm() + " PRIVATE KEY",
			       privateKey.getEncoded());
		string1 = pkbuf.toString();
		pkbuf.setLength(0);
	    }
	    String string2 = null;
	    if (publicKey != null) {
		encoder.addHeader("signature-algorithm", sigalg);
		encoder.encode(publicKey.getAlgorithm() + " PUBLIC KEY",
			       publicKey.getEncoded());
		string2 = pkbuf.toString();
	    }
	    return new String[] {string1, string2};
	} catch (IOException eio) {
	    String msg = errorMsg("keypairError");
	    throw new IllegalStateException(msg, eio);
	}
    }

    /**
     * Generate two PEM encoded strings for a key pair, each with
     * a header specifying a signature algorithm.
     * The header will be "signature-algorithm: SHA256withECDSA" and
     * the type of the PEM headers will be "EC PRIVATE KEY" and "EC
     * PUBLIC KEY".
     * @return an array whose first element contains the PEM-encoded
     *         private key and whose second element contsins the PEM
     *         encoded public key
     */
    public static String[] createPEMPair()
    {
	try {
	    return createPEMPair(null, null);
	} catch (GeneralSecurityException e) {
	    throw new UnexpectedExceptionError(e);
	}
    }

    /**
     * Generate two PEM encoded strings for a key pair, specifying
     * an elliptic curve and a signature algorithm.
     * The header will be "signature-algorithm: " followed by the name
     * of the signature algorithm, and the type of the PEM headers
     * will be "EC PRIVATE KEY" and "EC PUBLIC KEY". The elliptic
     * curve names and the signature-algorithm names are those recognized
     * by Java.
     * @param pspec the name of the elliptic curve, null for the
     *        default (secp256r1)
     * @param sigalg the name of the signature algorithm, null for
     *        the default (SHA256withECDSA)
     * @return an array whose first element contains the PEM-encoded
     *         private key and whose second element contsins the PEM
     *         encoded public key
     */
    public static String[] createPEMPair(String pspec,
					 String sigalg)
	throws IllegalArgumentException, GeneralSecurityException
    {

	if (pspec == null) pspec = DEFAULT_CURVE;
	if (sigalg == null) sigalg = DEFAULT_SIGALG;

	KeyPairGenerator gen = KeyPairGenerator.getInstance("EC", "SunEC");
	ECGenParameterSpec spec = new ECGenParameterSpec(pspec);
	gen.initialize(spec);
	KeyPair kp = gen.genKeyPair();
	PrivateKey privateKey = kp.getPrivate();
	PublicKey publicKey = kp.getPublic();
	return getPEMStrings(privateKey, publicKey, sigalg);
    }

    /**
     * Generate two PEM encoded strings for a key pair, specifying
     * an elliptic curve and a signature algorithm, storing the keys
     * in a PKCS #12 file.
     * The header will be "signature-algorithm: " followed by the name
     * of the signature algorithm, and the type of the PEM headers
     * will be "EC PRIVATE KEY" and "EC PUBLIC KEY". The elliptic
     * curve names and the signature-algorithm names are those recognized
     * by Java.
     * @param keystoreFile the PKCS #12 file, which will be created if
     *        necessary
     * @param pspec the name of the elliptic curve, null for the
     *        default (secp256r1)
     * @param sigalg the name of the signature algorithm, null for
     *        the default (SHA256withECDSA)
     * @param alias an identifier used to name the key
     * @param dn a distinguished name for the certificate corresponding
     *        to the public key (for example, CN=nobody@nowhere.com).   
     * @param pwarray a character array containing the password to use
     *        for the key-store file and to recover the private key given
     *        the alias
     * @return an array whose first element contains the PEM-encoded
     *         private key and whose second element contsins the PEM
     *         encoded public key
     */
    public static String[] createPEMPair(File keystoreFile,
					 String pspec,
					 String sigalg,
					 String alias,
					 String dn,
					 char[] pwarray)
	throws IllegalArgumentException, GeneralSecurityException,
	       IOException
    {
	if (pspec == null) pspec = DEFAULT_CURVE;
	if (sigalg == null) sigalg = DEFAULT_SIGALG;
	String keystore = keystoreFile.getCanonicalPath();
	try {
	    ProcessBuilder pb = new ProcessBuilder
		("keytool",
		 "-genkey",
		 "-keyalg", "EC",
		 "-groupname", pspec,
		 "-sigalg", sigalg,
		 "-dname", dn,
		 "-keypass", new String(pwarray),
		 "-storepass", new String(pwarray),
		 "-validity", "" + (365*100 + 25),
		 "-alias", alias,
		 "-keystore", keystore);
	    pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
	    pb.redirectError(ProcessBuilder.Redirect.INHERIT);
	    Process p = pb.start();
	    int status = p.waitFor();
	    if (status != 0) {
		String msg = errorMsg("keypairError");
		throw new IllegalStateException(msg);
	    }
	    KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
	    ks.load(new FileInputStream(keystore), pwarray);
	    PrivateKey privateKey = (PrivateKey) ks.getKey(alias, pwarray);
	    Certificate cert = ks.getCertificate(alias);
	    if (cert == null) {
		throw new GeneralSecurityException();
	    }
	    PublicKey publicKey = cert.getPublicKey();
	    return getPEMStrings(privateKey, publicKey, sigalg);
	} catch (InterruptedException e) {
	    String msg = errorMsg("keystoreError");
	    throw new IllegalStateException(msg, e);
	} catch (IOException e) {
	    String msg = errorMsg("keystoreError");
	    throw new IllegalStateException(msg, e);
	} catch (GeneralSecurityException e) {
	    String msg = errorMsg("keystoreError");
	    throw new IllegalStateException(msg, e);
	}	
    }
}

//  LocalWords:  exbundle endian OL SHA UTF DER SSL codepoint TLS PEM
//  LocalWords:  SecureBasicUtilities createPEMPair createPassword CN
//  LocalWords:  decodePassword getTimeDiff checkPassword CRC openssl
//  LocalWords:  iconedRealm keytool PKCS BLOCKQUOTE PRE genkey secp
//  LocalWords:  keyalg groupname sigalg withECDSA dname keypass pfx
//  LocalWords:  storepass keystore ecstore ecparam noout eckey pem
//  LocalWords:  req eccert pkcs inkey encodeRealm PemType RSA
//  LocalWords:  PrivateKey getEncoded
