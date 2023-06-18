import org.bzdev.ejws.*;
import org.bzdev.ejws.maps.*;
import org.bzdev.util.ErrorMessage;
import java.io.File;
import java.io.FileInputStream;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.Certificate;
import java.util.Iterator;

public class ATest4 {
    public static void main(String argv[]) throws Exception {
	// ErrorMessage.setStackTrace(true);
	String realm = "realm";
	System.out.println(realm);
	String publicKeyPem = Files.readString(Paths.get(argv[0]));
	System.out.println(publicKeyPem);

	InetSocketAddress saddr = new InetSocketAddress("0.0.0.0", 8080);
	System.out.println("server address:" + saddr.getAddress());
	System.out.println("all addresses: ");
	Iterator<NetworkInterface> it1 =
	    NetworkInterface.getNetworkInterfaces().asIterator();
	while (it1.hasNext()) {
	    Iterator<InetAddress> it2 =
		it1.next().getInetAddresses().asIterator();
	    while (it2.hasNext()) {
		System.out.println("... " + it2.next());
	    }
	}


	EmbeddedWebServer ews = new
	    EmbeddedWebServer(saddr.getAddress(),
			      8080, 48, 10,
			      (new EmbeddedWebServer.SSLSetup("TLS"))
			      .keystore(new FileInputStream("thelio-ks.jks"))
			      .truststore(new FileInputStream
					  ("thelio-ts.jks")));

	EjwsSecureBasicAuth auth = null;
	if (!argv[argv.length-1].equals("--noauth")) {
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

	ews.add("/open/", DirWebMap.class, new File("../../BUILD/api/"), null,
		true, true, true);

	FileHandler handler = (FileHandler) ews.getHttpHandler("/");
	if (!argv[argv.length-1].equals("--nologin")) {
	    handler.setLoginAlias("login.html", "", true);
	    URI logoutURI = (argv.length == 1 || !argv[1].startsWith("--"))?
		new URI("https://www.google.com"): new URI(argv[1]);

	    handler.setLogoutAlias("logout.html", logoutURI);

	    auth.setLoginFunction((p, t) -> {
		    System.out.println("login: " + p.getUsername());
		});

	    auth.setAuthorizedFunction((p, t) -> {
		    System.out.println("logged in: " + p.getUsername());
		});

	    auth.setLogoutFunction((p, t) -> {
		    System.out.println("logout: " + p.getUsername());
		});
	}

	// CloseWaitService cws = new CloseWaitService(120, 30, saddr);
	// cws.start();

	ews.setTracer("/", System.out);
	ews.start();
    }
}
