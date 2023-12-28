import org.bzdev.net.*;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.Signature;
import java.net.*;
import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.File;

import java.security.KeyStore;
import java.security.KeyFactory;
import java.security.cert.*;
import java.security.spec.*;

public class SecureBasicTest {

    private static void  checkKeystore() throws Exception {
	KeyStore ks = KeyStore.getInstance(new File ("ecstore.pfx"),
					   "password".toCharArray());
	System.out.println("key type = "
			   + ks.getKey("key", "password".toCharArray())
			   .getClass());

	PrivateKey key1 = (PrivateKey)
	    ks.getKey("key", "password".toCharArray());
	System.out.println("key1.getAlgorithm() = " + key1.getAlgorithm());
	Certificate kcert = ks.getCertificate("key");
	PublicKey key2 = kcert.getPublicKey();
	System.out.println("key2.getAlgorithm() = " + key2.getAlgorithm());
    }

    public static void initialTest() throws Exception {

	SecureBasicUtilities ops = new SecureBasicUtilities();
	String thepw = "testPassword";

	char[] password = ops.createPassword(null, thepw.toCharArray());
	byte[] sigarray = ops.decodePassword(password);

	if (!ops.checkPassword(sigarray, null, thepw)) {
	    throw new Exception();
	}
	String passwordStr = new String(password);
	sigarray = ops.decodePassword(passwordStr);
	if (!ops.checkPassword(sigarray, null, thepw)) {
	    throw new Exception();
	}

	byte[] pwBytes = passwordStr.getBytes("utf-8");
	sigarray = ops.decodePassword(pwBytes);
	if (!ops.checkPassword(sigarray, null, thepw)) {
	    throw new Exception();
	}

	// passwordStr = ":foo:" + passwordStr;
	sigarray = ops.decodePassword(passwordStr);
	if (!ops.checkPassword(sigarray, null, thepw)) {
	    throw new Exception();
	}
	// if (ops.isLogout(passwordStr)) throw new Exception();

	password = passwordStr.toCharArray();
	sigarray = ops.decodePassword(password);
	if (!ops.checkPassword(sigarray, null, thepw)) {
	    throw new Exception();
	}

	pwBytes = passwordStr.getBytes("utf-8");
	sigarray = ops.decodePassword(pwBytes);
	if (!ops.checkPassword(sigarray, null, thepw)) {
	    throw new Exception();
	}

	sigarray = ops.decodePassword(passwordStr);
	if (!ops.checkPassword(sigarray, null, thepw)) {
	    throw new Exception();
	}
	password = passwordStr.toCharArray();
	sigarray = ops.decodePassword(password);
	if (!ops.checkPassword(sigarray, null, thepw)) {
	    throw new Exception();
	}

	pwBytes = passwordStr.getBytes("utf-8");
	sigarray = ops.decodePassword(pwBytes);
	if (!ops.checkPassword(sigarray, null, thepw)) {
	    throw new Exception();
	}
    }

    public static void main(String argv[]) throws Exception {

	initialTest();

	String realm = SecureBasicUtilities
	    .encodeRealm("realm", SecureBasicUtilities.Mode.DIGEST);
	System.out.println ("DIGEST: realm = \""
			    + SecureBasicUtilities.iconedRealm(realm) + "\"");
	if (SecureBasicUtilities.getMode(realm)
	    != SecureBasicUtilities.Mode.DIGEST) {
	    throw new Exception();
	}
	realm = SecureBasicUtilities
	    .encodeRealm("realm",
			 SecureBasicUtilities.Mode.SIGNATURE_WITHOUT_CERT);
	System.out.println ("SIGNATURE (no CERT): realm = \""
			    + SecureBasicUtilities.iconedRealm(realm) + "\"");
	if (SecureBasicUtilities.getMode(realm)
	    != SecureBasicUtilities.Mode.SIGNATURE_WITHOUT_CERT) {
	    throw new Exception();
	}
	realm = SecureBasicUtilities
	    .encodeRealm("realm",
			 SecureBasicUtilities.Mode.SIGNATURE_WITH_CERT);
	System.out.println ("SIGNATURE (CERT): realm = \""
			    + SecureBasicUtilities.iconedRealm(realm) + "\"");
	if (SecureBasicUtilities.getMode(realm)
	    != SecureBasicUtilities.Mode.SIGNATURE_WITH_CERT) {
	    throw new Exception();
	}
	realm = SecureBasicUtilities
	    .encodeRealm("realm", SecureBasicUtilities.Mode.PASSWORD);
	System.out.println ("PASSWORD: realm = \"" + realm + "\"");
	if (SecureBasicUtilities.getMode(realm)
	    != SecureBasicUtilities.Mode.PASSWORD) {
	    throw new Exception();
	}

	System.out.println("Elliptic Curves:");
	String curves = java.security.Security
	    .getProviders("AlgorithmParameters.EC")[0]
	    .getService("AlgorithmParameters", "EC")
	    .getAttribute("SupportedCurves");
	System.out.println("    " + curves);

	// test read of private key from openssl

	byte[] bytes = null;
	KeyFactory kf = KeyFactory.getInstance("EC");

	// check that PemDecode produces the right file.
	byte[] tbytes = (new FileInputStream("public.der")).readAllBytes();
	byte[] tbytes2 = PemDecoder.decode(new FileInputStream("public.pem"))
	    .getBytes();
	if (tbytes.length != tbytes2.length) {
	    System.out.println("tbytes.length = " + tbytes.length
			       + ", tbytes2.length = " + tbytes2.length);
	    throw new Exception();
	}
	for (int i = 0 ; i < tbytes.length; i++) {
	    if (tbytes[i] != tbytes2[i]) throw new Exception();
	}

	bytes = (new FileInputStream("public.pem")).readAllBytes();


	X509EncodedKeySpec kspec2 = new X509EncodedKeySpec(tbytes, "EC");

	PublicKey testPublicKey = kf.generatePublic(kspec2);
	System.out.println("testPublicKey: algorithm = "
			   + testPublicKey.getAlgorithm());
	checkKeystore();

	SecureBasicUtilities ksops =
	    new SecureBasicUtilities(new File("keypair.keystore"),
				     null,
				     "key",
				     "password".toCharArray());
	PrivateKey key1 = ksops.getPrivateKey();
	PublicKey key2 = ksops.getPublicKey();

	String[] pems = ksops.getPEMStrings();
	System.out.println(pems[0]);
	System.out.println(pems[1]);

	// Check that the public and private keys work by signing a
	// message directly.
	Signature s1 = Signature.getInstance("SHA256withECDSA", "SunEC");
	s1.initSign(key1);

	byte[] msg = "This is a test".getBytes("UTF-8");

	s1.update(msg);
	byte[] sig = s1.sign();
	System.out.println("sig size = " + sig.length);
	
	Signature s2 = Signature.getInstance("SHA256withECDSA", "SunEC");
	s2.initVerify(key2);
	s2.update(msg);
	boolean valid = s2.verify(sig);
	System.out.println("valid = " + valid);
	
	File file = new File("secbasic.p12");
	if (file.exists()) file.delete();
	String[] result0 =
	    SecureBasicUtilities.createPEMPair(file,
					 null, null,
					 "key",
					 "CN=nobody@nobody.com",
					 "password".toCharArray());
	SecureBasicUtilities ksops01 = new SecureBasicUtilities(result0[0]);
	SecureBasicUtilities ksops02 = new SecureBasicUtilities(result0[1]);

	PrivateKey pk1 = ksops01.getPrivateKey();
	PublicKey pk2 = ksops02.getPublicKey();

	pems = ksops01.getPEMStrings();
	if (!pems[0].equals(result0[0])) {
	    throw new Exception();
	}
	pems = ksops02.getPEMStrings();
	if (!pems[1].equals(result0[1])) {
	    throw new Exception();
	}

	ksops = new SecureBasicUtilities(file,
				   null,
				   "key",
				   "password".toCharArray());

	key1 = ksops.getPrivateKey();
	key2 = ksops.getPublicKey();

	SecureBasicUtilities tksops = new SecureBasicUtilities(file,
						   null,
						   "key",
						   "password".toCharArray());
	PrivateKey ttkey1 = tksops.getPrivateKey();
	PublicKey ttkey2 = tksops.getPublicKey();

	if (!key1.equals(ttkey1)) throw new Exception("ttkey1");
	if (!key2.equals(ttkey2)) throw new Exception("ttkey2");

	if (!key1.equals(pk1)) throw new Exception("pk1");
	if (!key2.equals(pk2)) throw new Exception("pk2");


	key1 = ksops01.getPrivateKey();

	s1 = Signature.getInstance("SHA256withECDSA", "SunEC");
	s1.initSign(key1);

	s1.update(msg);
	sig = s1.sign();
	System.out.println("sig size = " + sig.length);
	
	s2 = Signature.getInstance("SHA256withECDSA", "SunEC");
	s2.initVerify(key2);
	s2.update(msg);
	valid = s2.verify(sig);
	System.out.println("valid = " + valid);
	pems = ksops.getPEMStrings();
	SecureBasicUtilities sbops = new SecureBasicUtilities(pems[0]);
	if (!pems[0].equals(sbops.getPEMStrings()[0])) {
		throw new Exception();
	}
	sbops = new SecureBasicUtilities(pems[1]);
	if (!pems[1].equals(sbops.getPEMStrings()[1])) {
		throw new Exception();
	}

	if (!pems[0].equals(result0[0]) || !pems[1].equals(result0[1])) {
	    System.out.println("mismatch ...");
	    System.out.print(pems[0]);
	    System.out.print(result0[0]);
	    System.out.print(pems[1]);
	    System.out.print(result0[1]);
	    throw new Exception();
	}

	/*

	InputStream is = new FileInputStream("private.pem");
	PKCS8EncodedKeySpec kspec = new
	    PKCS8EncodedKeySpec(PemDecoder.decode(is).getBytes(), "EC");
	PrivateKey testPrivateKey = kf.generatePrivate(kspec);
	System.out.println("testPrivateKey: algorithm = "
			   + testPrivateKey.getAlgorithm());
	*/


	String[] result = SecureBasicUtilities.createPEMPair();

	String privatePem = result[0];
	String publicPem = result[1];

	System.out.print(privatePem);
	System.out.print(publicPem);
	SecureBasicUtilities ops1 = new SecureBasicUtilities(privatePem);

	SecureBasicUtilities ops2 = new SecureBasicUtilities(publicPem);

	byte[] testData = "this is a test".getBytes("UTF-8");
	Signature signer = ops1.getSigner();
	signer.update(testData);
	byte[] tdsig = signer.sign();
	Signature verifier = ops2.getVerifier();
	verifier.update(testData);
	if (verifier.verify(tdsig)) {
	    System.out.println("tdsig valid for testData");
	} else {
	    throw new Exception();
	}

	PrivateKey privateKey = ops1.getPrivateKey();
	PublicKey publicKey = ops2.getPublicKey();

	System.out.println("privateKey format = " + privateKey.getFormat());

	URL url = new URL("https://google.com");
	URLConnection urlc = url.openConnection();
	urlc.connect();
	if (urlc instanceof HttpsURLConnection) {
	    Certificate cert = ((HttpsURLConnection) urlc)
		.getServerCertificates()[0];
	    String  password = new
		String(ops1.createPassword(cert, "password".toCharArray()));
	    System.out.println("password = " + password);
	    Thread.currentThread().sleep(2000);
	    byte[] sigbytes = SecureBasicUtilities.decodePassword(password);
	    int tdiff = SecureBasicUtilities.getTimeDiff(sigbytes);
	    boolean status = ops2.checkPassword(sigbytes, cert, "password");
	    System.out.println("tdiff = " + tdiff +",  status = " + status);
	}
    }
}
