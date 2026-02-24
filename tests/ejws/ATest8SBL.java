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
public class ATest8SBL {
    public static void main(String argv[]) throws Exception {
	// ErrorMessage.setStackTrace(true);
	String realm = "realm";
	System.out.println(realm);
	String recip = argv[1];
 	
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

	EjwsSecureBasicAuth auth = new EjwsSecureBasicAuth(ews, realm);

	File dir = new File(argv[0]);

	// We want to make it easy for the time limit to expire
	auth.setTimeLimits(-60 , 120, 3600);
	System.out.println("auth mode = " + auth.getMode());

	auth.setTracer(System.out);
	URI logoutURI = (argv.length == 1 || !argv[1].startsWith("--"))?
	    new URI("https://www.google.com"): new URI(argv[1]);

	ews.add("/", DirWebMap.class, dir, auth, true, true, true)
	    .addWelcome("/index.html")
	    .setLoginAlias("login.html", "", true)
	    .setLogoutAlias("logout.html", logoutURI);


	String recipients[] = {argv[1]};
	auth.createUser("test-user", "Example", null, recipients)
	    .setDescription("Example")
	    .setURI("login.html")
	    .addUser(false);

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
