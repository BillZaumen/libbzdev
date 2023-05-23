import org.bzdev.ejws.*;
import org.bzdev.ejws.maps.*;
import org.bzdev.util.ErrorMessage;
import java.io.File;
import java.io.FileInputStream;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.Certificate;

public class ATest4 {
    public static void main(String argv[]) throws Exception {
	// ErrorMessage.setStackTrace(true);
	String realm = "realm";
	System.out.println(realm);
	String publicKeyPem = Files.readString(Paths.get(argv[0]));
	System.out.println(publicKeyPem);

	InetSocketAddress saddr = new InetSocketAddress("0.0.0.0", 8080);
	EmbeddedWebServer ews = new
	    EmbeddedWebServer(saddr.getAddress(),
			      8080, 48, 10,
			      (new EmbeddedWebServer.SSLSetup("TLS"))
			      .keystore(new FileInputStream("thelio-ks.jks"))
			      .truststore(new FileInputStream
					  ("thelio-ts.jks")));

	EjwsSecureBasicAuth auth = null;
	if (argv.length == 1 || !argv[1].equals("--noauth")) {
	    Certificate[] certs = ews.getCertificates();
	    System.out.println("Number of certificates = " + certs.length);
	    auth = new EjwsSecureBasicAuth(realm, certs);
	    // We want to make it easy for the time limit to expire
	    auth.setTimeLimits(-2 , 30, 45);
	    System.out.println("auth mode = " + auth.getMode());
	    auth.add("foo", publicKeyPem, "foo");
	    auth.setTracer(System.out);
	}

	ews.add("/", DirWebMap.class, new File("../../BUILD/api/"), auth,
		true, true, true);

	FileHandler handler = (FileHandler) ews.getHttpHandler("/");
	handler.setLoginAlias("login.html");
	handler.setLogoutAlias("logout.html",
			       new URI("https://www.google.com"));

	auth.setLoginFunction((p, t) -> {
		System.out.println("login: " + p.getUsername());
	    });

	auth.setLogoutFunction((p, t) -> {
		System.out.println("logout: " + p.getUsername());
	    });

	// CloseWaitService cws = new CloseWaitService(120, 30, saddr);
	// cws.start();

	ews.setTracer("/", System.out);
	ews.start();
    }
}
