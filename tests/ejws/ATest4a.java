import org.bzdev.ejws.*;
import org.bzdev.ejws.maps.*;
import org.bzdev.net.SSLUtilities;
import org.bzdev.util.ErrorMessage;
import org.bzdev.util.ConfigPropUtilities;
import java.io.File;
import java.io.FileInputStream;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.Certificate;
import java.util.Iterator;
import java.util.Properties;
import javax.net.ssl.*;

public class ATest4a {
    public static void main(String argv[]) throws Exception {
	// ErrorMessage.setStackTrace(true);
	String realm = "realm";
	System.out.println(realm);
	
	File sblFile = new File(argv[0]);
	String key = argv[1];
	Properties props = ConfigPropUtilities
	    .newInstance(sblFile, "application/vnd.bzdev.sblauncher");
	String user = ConfigPropUtilities
	    .getProperty(props, key + ".user");
	String password = ConfigPropUtilities
	    .getProperty(props, key + ".password");
	String publicKeyPem = ConfigPropUtilities
	    .getProperty(props, "base64.keypair.publicKey");

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
			      .keystore(new FileInputStream("thelio-ks.jks")));

	EjwsSecureBasicAuth auth = null;
	if (!argv[argv.length-1].equals("--noauth")) {
	    Certificate[][] certs = ews.getCertificates();
	    System.out.println("Number of certificates = " + certs[0].length);
	    auth = new EjwsSecureBasicAuth(ews, realm,
					   EjwsSecureBasicAuth.getMode(certs));
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
	    URI logoutURI = (argv.length == 2 || !argv[1].startsWith("--"))?
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
	System.out.println("server started");

    }
}
