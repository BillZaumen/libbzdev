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

public class SSLUtilTest {


    public static void main(String argv[]) throws Exception {

	if (argv.length > 0) {
	    for (String arg: argv) {
		char[] pw = "changeit".toCharArray();
		File trustStore = arg.equals("tstore")?
		    new File("../ejws/thelio-ts.jks"): null;
		    
		if (trustStore != null) {
		    SSLUtilities.installTrustManager("TLS",
						     trustStore, pw,
						     (cert) -> {
							 return true;
						     });
		} else {
		    SSLUtilities.installTrustManager("TLS",
						     null, null,
						     (cert) -> {
							 return false;
						     });
		}
	    }
	}
	String host = "www.google.com";
	int port = 443;
	Certificate cert = null;
	SSLSocketFactory factory = (SSLSocketFactory)
	    SSLSocketFactory.getDefault();
	try (SSLSocket socket = (SSLSocket)
	     factory.createSocket(host, port)) {
	    SSLSession session = socket.getSession();
	    Certificate[] chain = session.getPeerCertificates();
	    cert =  (chain == null || chain.length == 0)? null:
		chain[0];
	} catch (Exception e) {
	    System.err.println(e.getMessage());
	    cert = null;
	}
	System.out.println("google cert = " + ((cert == null)? null:
					       cert.getType()));
	if (cert != null) {
	    (new PemEncoder(System.out)).encode("CERTIFICATE",
						cert.getEncoded());
	}
	host = "www.yahoo.com";
	cert = null;
	factory = (SSLSocketFactory)
	    SSLSocketFactory.getDefault();
	try (SSLSocket socket = (SSLSocket)
	     factory.createSocket(host, port)) {
	    SSLSession session = socket.getSession();
	    Certificate[] chain = session.getPeerCertificates();
	    cert =  (chain == null || chain.length == 0)? null:
		chain[0];
	} catch (Exception e) {
	    System.err.println(e.getMessage());
	    cert = null;
	}
	System.out.println("yahoo cert = " + ((cert == null)? null:
					      cert.getType()));
	if (cert != null) {
	    (new PemEncoder(System.out)).encode("CERTIFICATE",
						cert.getEncoded());
	}

	host = "localhost";
	port = 8080;
	cert = null;
	factory = (SSLSocketFactory)
	    SSLSocketFactory.getDefault();
	try (SSLSocket socket = (SSLSocket)
	     factory.createSocket(host, port)) {
	    SSLSession session = socket.getSession();
	    Certificate[] chain = session.getPeerCertificates();
	    cert =  (chain == null || chain.length == 0)? null:
		chain[0];
	} catch (Exception e) {
	    System.err.println(e.getMessage());
	    cert = null;
	}
	System.out.println("localhost cert = " + ((cert == null)? null:
						  cert.getType()));
    }
}
