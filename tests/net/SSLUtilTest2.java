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

public class SSLUtilTest2 {


    public static void main(String argv[]) throws Exception {

	SSLUtilities.installTrustManager("TLS",
					 null, null,
					 (cert) -> {
					     return true;
					 });
	SSLUtilities.allowLoopbackHostname();
	String host = "localhost";
	int port = 8080;
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
	System.out.println("localhost cert = " + ((cert == null)? null:
						  cert.getType()));
    }
}
