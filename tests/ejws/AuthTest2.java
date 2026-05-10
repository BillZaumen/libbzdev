import java.io.*;
import java.net.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateException;
import java.util.Map;
import org.bzdev.ejws.*;
import org.bzdev.ejws.maps.*;


public class AuthTest2 {

    public static void main(String argv[]) throws Exception {

	File gpgdir = new File(argv[0]);
	
	InetSocketAddress saddr = new InetSocketAddress("0.0.0.0", 8080);

	EmbeddedWebServer ews = new
	    EmbeddedWebServer(saddr.getAddress(),
			      8080, 48, 10,
			      (new EmbeddedWebServer.SSLSetup("TLS"))
			      .keystore(new FileInputStream("local-ks.jks"))
			      .truststore(new FileInputStream
					  ("local-ts.jks")));

	EjwsSecureBasicAuth auth = new EjwsSecureBasicAuth(ews, "realm");
	auth.setGPGHome(gpgdir);
	auth.setCertificateChain(new URI("https://google.com"));
	
	for (String id: auth.getTrustedKeyIDs()) {
	    System.out.println("trusted key id: " + id);
	}


	System.out.println("Not Active");
	for (String name: auth.getGPGUsers(false)) {
	    System.out.println(name);
	}
	System.out.println("Active");
	for (String name: auth.getGPGUsers(true)) {
	    System.out.println(name);
	}

	Certificate[] chain = auth.getDefaultCertChain();
	System.out.println("chain.length = " + chain.length);
	for (Certificate cert: chain) {
	    if (cert instanceof X509Certificate) {
		X509Certificate xcert = (X509Certificate) cert;
		System.out.println("... "
				   + xcert.getSubjectX500Principal()
				   .getName("canonical").substring(3));
	    }
	}
    }
}
