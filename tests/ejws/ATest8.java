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
import java.util.Properties;
// import org.bzdev.swing.ConfigPropertyEditor;
import org.bzdev.util.ConfigPropUtilities;

// use to test SBL
public class ATest8 {
    public static void main(String argv[]) throws Exception {
	// ErrorMessage.setStackTrace(true);
	String realm = "realm";
	System.out.println(realm);
	File sblFile = new File(argv[1]);
	String key = argv[2];
	String alias = (argv.length > 3)? argv[3]: null;
	
	InetSocketAddress saddr = new InetSocketAddress("0.0.0.0", 8080);

	EmbeddedWebServer ews = new
	    EmbeddedWebServer(saddr.getAddress(),
			      8080, 48, 10,
			      (new EmbeddedWebServer.SSLSetup("TLS"))
			      .keystore(new FileInputStream("thelio-ks.jks"))
			      .truststore(new FileInputStream
					  ("thelio-ts.jks")));

	Certificate[][] certs = ews.getCertificates();
	System.out.println("Number of certificates = " + certs[0].length);

	EjwsSecureBasicAuth auth = new
	    EjwsSecureBasicAuth(ews, realm, EjwsSecureBasicAuth.getMode(certs));
	if (alias != null) {
	    auth.setCertificateChain(new FileInputStream("thelio-ks.jks"),
				     "changeit".toCharArray(), alias);
	}

	File dir = new File(argv[0]);

	// We want to make it easy for the time limit to expire
	auth.setTimeLimits(-60 , 120, 3600);
	System.out.println("auth mode = " + auth.getMode());

	Properties props = ConfigPropUtilities
	    .newInstance(sblFile, "application/vnd.bzdev.sblauncher");
	String user = ConfigPropUtilities
	    .getProperty(props, key + ".user");
	// String password = ConfigPropUtilities
	//    .getProperty(props, key + ".password");
	String password = "test-password";
	String publicKeyPem = ConfigPropUtilities
	    .getProperty(props, "base64.keypair.publicKey");

	System.out.println("user = " + user);
	System.out.println("password = " + password);
	System.out.println("publicKeyPem = " + publicKeyPem);

	auth.setTracer(System.out);
	URI logoutURI = (argv.length == 1 || !argv[1].startsWith("--"))?
	    new URI("https://www.google.com"): new URI(argv[1]);

	ews.add("/", DirWebMap.class, dir, auth, true, true, true)
	    .addWelcome("/index.html")
	    .setLoginAlias("login.html", "", true)
	    .setLogoutAlias("logout.html", logoutURI);
	auth.add(user, publicKeyPem, password);
	/*
	WebMap webmap = ews.getWebMap("/");
	webmap.addWelcome("/index.html");

	FileHandler handler = (FileHandler) ews.getHttpHandler("/");
	handler.setLoginAlias("login.html", "", true);

	handler.setLogoutAlias("logout.html", logoutURI);
	*/
	auth.setLoginFunction((p, t) -> {
		System.out.println("login: " + p.getUsername());
	    });

	auth.setAuthorizedFunction((p, t) -> {
		System.out.println("logged in: " + p.getUsername());
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
