package org.bzdev.net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.GeneralSecurityException;
import java.security.cert.*;
import java.util.Base64;
import java.util.Properties;
import java.util.Set;
import java.util.HashSet;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.net.ssl.*;
import org.bzdev.util.ConfigPropUtilities;
import org.bzdev.util.ErrorMessage;

//@exbundle org.bzdev.net.lpack.Net

/**
 * Class for configuring SSL/TLS.
 * This class consists of static methods. It will let one
 * add an additional trust store (useful for testing or servers
 * that are running locally), allow the loopback interface to be
 * used in an SSL connection, and configure when
 * self-signed certificates are accepted.
 * <P>
 * Error messages may appear on standard error, but how and where these
 * messages are displayed can be changed by using methods provided by
 * {@link org.bzdev.util.ErrorMessage} and
 * {link org.bzdev.swing.SwingErrorMessage}.

 */
public class SSLUtilities {

    private static String
	errorMsg(java.lang.String key, java.lang.Object... args)
    {
	return NetErrorMsg.errorMsg(key, args);
    }

    private static String localeString(String key) {
	return NetErrorMsg.errorMsg(key);
    }

    /*
    private static final char[] EMPTY_CHAR_ARRAY = new char[0];

    private static char[] decryptToCharArray(String value, char[] gpgpw)
	throws GeneralSecurityException
    {
	if (value == null || gpgpw == null) return EMPTY_CHAR_ARRAY;
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
						   "--batch", "-d");
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
    */

    /**
     * Configure SSL using data stored in an SBL file.
     * SBL files can be created and edited using the program
     * <STRONG>sbl</STRONG>, which is normally installed with this
     * class library.
     * @param type the type for an {@link SSLContext} (e.g., SSL or TLS)
     * @param sblFile a file in SBL format
     * @param passphraseSupplier a {@link Supplier} that will provide a
     *        GPG pass phrase; null for a default
     * @see org.bzdev.swing.ConfigPropertyEditor#gpgPassphraseSupplier
     */
    public static void
	configureUsingSBL(String type,
			  File sblFile,
			  Supplier<char[]> passphraseSupplier)
	throws IOException, GeneralSecurityException, CertificateException
    {
	/*
	Properties props = new Properties();
	props.load(new FileInputStream(sblFile));
	*/
	Properties props = ConfigPropUtilities
	    .newInstance(sblFile, "application/vnd.bzdev.sblauncher");
	// String fn = props.getProperty("trustStore.file");
	String fn = ConfigPropUtilities.getProperty(props, "trustStore.file");
	File trustKeyStore = (fn == null)? null: new File(fn);
	// String s = props.getProperty("trust.selfsigned", "false");
	String s = ConfigPropUtilities.getProperty(props, "trust.selfsigned");
	if (s == null) s = "false";
	boolean allowSelfSigned = s.trim().toLowerCase().equals("true");
	s = ConfigPropUtilities.getProperty(props, "trust.allow.loopback");
	if (s == null) s = "false";
	boolean allowLoopback = s.trim().toLowerCase().equals("true");

	if (trustKeyStore != null) {
	    /*
	    if (passphraseSupplier == null) {
		passphraseSupplier = defaultPassphraseSupplier;
	    }
	    char[] gpgpw = passphraseSupplier.get();
	    */
	    char[] gpgpw = ConfigPropUtilities
		.getGPGPassphrase(passphraseSupplier);

	    // String epw = props.getProperty("ebase64.trustStore.password");
	    char[] trustpw = ConfigPropUtilities
		.getDecryptedProperty(props, "ebase64.trustStore.password",
				      gpgpw);

	    for (int i = 0; i < gpgpw.length; i++) {
		gpgpw[i] = '\0';
	    }
	    if (trustpw == null) {
		throw new GeneralSecurityException(errorMsg("epwExpected"));
	    }

	    installTrustManager(type, trustKeyStore, trustpw,
				/* decryptToCharArray(epw, gpgpw),*/
				allowSelfSigned? (cert) -> {return true;}:
				null);
	}
	if (allowLoopback) {
	    allowLoopbackHostname();
	}
    }


    /**
     * Configure SSL so that it will use a custom trust store in addition
     * to the default.
     * If a certificate is not valid based on the key stores but is
     * self signed, the argument acceptSelfSigned will be used to
     * to determine if the certificate should be accepted. This argument
     * is a functional interface with a "test" method that takes the
     * certificate as an argument, and will return true if the certificate
     * should be accepted and false if it should not be accepted.
     * An implementation of this functional interface may provide a dialog
     * box to query the user and may cache values.
     * @param type the type for an {@link SSLContext} (e.g., SSL or TLS)
     * @param trustKeyStore the file for a custom trust key store; null if
     *        there is none
     * @param password the password for the trust key store
     * @param acceptSelfSigned a function that determines if a self-signed
     *        certificate should be accepted
     * @see javax.net.ssl.SSLContext#getInstance(String)
     */
    public static TrustManager[]
	installTrustManager(String type,
			    File trustKeyStore,
			    char[] password,
			    Predicate<X509Certificate>acceptSelfSigned)
	throws IOException, GeneralSecurityException, CertificateException
    {
	TrustManagerFactory tmf = TrustManagerFactory
	    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
	tmf.init((KeyStore) null);
	X509TrustManager tmptm  = null;
	for (TrustManager tm: tmf.getTrustManagers()) {
	    if (tm instanceof X509TrustManager) {
		tmptm = (X509TrustManager) tm;
		break;
	    }
	}
	final X509TrustManager stdTM = tmptm;


	tmptm = null;
	if (trustKeyStore != null) {
	    FileInputStream myKeys = new FileInputStream(trustKeyStore);
	    KeyStore myTrustStore = KeyStore
		.getInstance(KeyStore.getDefaultType());
	    myTrustStore.load(myKeys, password);
	    tmf = TrustManagerFactory
		.getInstance(TrustManagerFactory.getDefaultAlgorithm());
	    tmf.init(myTrustStore);

	    for (TrustManager tm : tmf.getTrustManagers()) {
		if (tm instanceof X509TrustManager) {
		    tmptm = (X509TrustManager) tm;
		    break;
		}
	    }
	}
	final X509TrustManager ourTM = tmptm;

	X509TrustManager customTm = new X509TrustManager() {
		@Override
		public X509Certificate[] getAcceptedIssuers() {
		    // If you're planning to use client-cert auth,
		    // merge results from "stdTM" and "ourTM".
		    X509Certificate[] certs1 =
			stdTM.getAcceptedIssuers();
		    X509Certificate[] certs2 = null;
		    if (ourTM != null) {
			certs2 = ourTM.getAcceptedIssuers();
		    }
		    Set<X509Certificate> set = new HashSet<X509Certificate>();
		    for (X509Certificate cert: certs1) {
			set.add(cert);
		    }

		    if (certs2 != null) {
			for (X509Certificate cert: certs2) {
			    set.add(cert);
			}
		    }
		    X509Certificate[] certs = new X509Certificate[set.size()];
		    int i = 0;
		    for (X509Certificate cert: set) {
			certs[i++] = cert;
		    }
		    return certs;
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain,
					       String authType)
		    throws CertificateException
		{
		    try {
			stdTM.checkServerTrusted(chain, authType);
		    } catch (CertificateException e) {
			try {
			    if (ourTM == null) {
				throw e;
			    }
			    ourTM.checkServerTrusted(chain, authType);
			} catch (CertificateException ee) {
			    if (acceptSelfSigned != null) {
				if (chain.length > 1) throw ee;
				X509Certificate cert = chain[0];
				try {
				    cert.checkValidity();
				    cert.verify(cert.getPublicKey());
				} catch (GeneralSecurityException eee) {
				    String m = eee.getMessage();
				    String msg = errorMsg("certNotValid", m);
				    ErrorMessage.format("%s", msg);
				    // System.err.println(msg);
				    throw new
					CertificateException(msg, eee);
				}
				if (!acceptSelfSigned.test(cert)) {
				    String msg = errorMsg("selfSigned");
				    ErrorMessage.format("%s", msg);
				    throw ee;
				}
			    } else {
				String m = ee.getMessage();
				String msg = errorMsg("certNotValid", m);
				ErrorMessage.format("%s", msg);
				throw ee;
			    }
			}
		    }
		}

		@Override
		public void checkClientTrusted(X509Certificate[] chain,
					       String authType)
		    throws CertificateException
		{
		    try {
			stdTM.checkClientTrusted(chain, authType);
		    } catch (CertificateException e) {
			if (ourTM == null) {
			    throw e;
			}
			ourTM.checkClientTrusted(chain, authType);
		    }
		}
	    };

	SSLContext sslContext = SSLContext.getInstance(type);
	TrustManager[] tms = new TrustManager[] { customTm };
	sslContext.init(null, tms, null);

	// You don't have to set this as the default context,
	// it depends on the library you're using.
	SSLContext.setDefault(sslContext);
	return tms;
    }

    private static HostnameVerifier defaultHNV =
	    HttpsURLConnection.getDefaultHostnameVerifier();

    /**
     * Install a custom host-name verifier that will additionally accept
     * the loopback interface's host name.
     */
    public static void allowLoopbackHostname() {
	
	HostnameVerifier ourHNV = new HostnameVerifier() {
		String loopback = InetAddress.getLoopbackAddress()
		    .getHostName();
		public boolean verify(String hostname, SSLSession session) {
		    boolean result = defaultHNV.verify(hostname, session);
		    if (result == false) {
			if (hostname.equals(loopback)) return true;
		    }
		    return result;
		}
	    };
	HttpsURLConnection.setDefaultHostnameVerifier(ourHNV);
    }

    /**
     * Remove the custom host name verifier set by calling
     * {@link #allowLoopbackHostname()} and restore the system default that
     * was in effect when this class was initialized.
     */
    public static void disallowLoopbackHostname() {
	HttpsURLConnection.setDefaultHostnameVerifier(defaultHNV);
    }
}

//  LocalWords:  SSL TLS acceptSelfSigned SSLContext trustKeyStore pb
//  LocalWords:  getInstance auth stdTM ourTM verifier loopback gpgpw
//  LocalWords:  exbundle decryptToCharArray GeneralSecurityException
//  LocalWords:  ByteArrayInputStream GPG ProcessBuilder gpg pinentry
//  LocalWords:  fd redirectError ByteArrayOutputStream baos os msg
//  LocalWords:  OutputStream getOutputStream OutputStreamWriter utf
//  LocalWords:  transferTo getMessage InputStream getInputStream SBL
//  LocalWords:  waitFor exitValue gpgFailed Charset forName
//  LocalWords:  defaultPassphraseSupplier readPassword localeString
//  LocalWords:  enterPW sbl sblFile passphraseSupplier fn trustStore
//  LocalWords:  gpgPassphraseSupplier FileInputStream getProperty
//  LocalWords:  trustStroe selfsigned epw epwExpected
//  LocalWords:  allowLoopbackHostname
