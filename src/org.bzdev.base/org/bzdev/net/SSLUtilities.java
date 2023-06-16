package org.bzdev.net;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.security.KeyStore;
import java.security.GeneralSecurityException;
import java.security.cert.*;
import java.util.Set;
import java.util.HashSet;
import java.util.function.Predicate;
import javax.net.ssl.*;

/**
 * Methods for configuring SSL/TLS.
 * These methods 
 */
public class SSLUtilities {

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
    public static void
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
				if (chain.length > 0) throw ee;
				X509Certificate cert = chain[0];
				cert.checkValidity();
				try {
				    cert.verify(cert.getPublicKey());
				} catch (GeneralSecurityException eee) {
				    throw new
					CertificateException(ee.getMessage(),
							     eee);
				}
				if (!acceptSelfSigned.test(cert)) {
				    throw ee;
				}
			    } else {
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
	sslContext.init(null, new TrustManager[] { customTm }, null);

	// You don't have to set this as the default context,
	// it depends on the library you're using.
	SSLContext.setDefault(sslContext);
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

//  LocalWords:  SSL TLS acceptSelfSigned SSLContext trustKeyStore
//  LocalWords:  getInstance auth stdTM ourTM verifier loopback
