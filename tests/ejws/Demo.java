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

// use to test SBL with an actual login page.
public class Demo {
    public static void main(String argv[]) throws Exception {
	// ErrorMessage.setStackTrace(true);
	String realm = "realm";
	System.out.println(realm);
 	
	String host = argv[3];

	String adminEmail = (argv.length > 5)? argv[4]: null;
	String adminFpr = (argv.length > 5)? argv[5]: null;


	InetSocketAddress saddr = new InetSocketAddress("0.0.0.0", 8080);

	String ksname = host + "-ks.jks";
	String tsname = host + "-ts.jks";

	EmbeddedWebServer ews = new
	    EmbeddedWebServer(saddr.getAddress(),
			      8443, 48, 10,
			      (new EmbeddedWebServer.SSLSetup("TLS"))
			      .keystore(new FileInputStream(ksname))
			      .truststore(new FileInputStream(tsname)));

	EjwsSecureBasicAuth auth = new EjwsSecureBasicAuth(ews, realm);
	auth.setGPGHome(new File(argv[2]));
	auth.setSBLDir(new File(System.getProperty("user.dir") + "/sbldir"));
	auth.setTracer(System.out);
	auth.setTruststore(System.getProperty("user.dir") + "/" + ksname);
	auth.setAllowLoopback(true);
	auth.setSelfSigned(true);
	if (adminEmail != null) {
	    auth.addToAdminMap(adminEmail, adminFpr);
	}
	auth.setCanAddAccount(true);
	auth.setDefaultActive(false);


	File tdir = new File(argv[0]);
	File dir = new File(argv[1]);


	System.out.println("auth mode = " + auth.getMode());

	auth.setTracer(System.out);

	URI logoutURI = new URI("https://" + host + ":8443/loginpage.html");

	ews.add("/", DirWebMap.class, tdir, null, true, true, true)
	    .addWelcome("loginpage.html");
	System.out.println("added /");

	ews.add("/docs/", DirWebMap.class, dir, auth, true, true, true)
	    .addWelcome("index.html")
	    .setLoginAlias("login", "", true)
	    .setLogoutAlias("logout", logoutURI)
	    .setAdminAlias("admin");
	System.out.println("added /docs/");
	auth.loadFromDirs();

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
	System.out.println("starting ews");
	ews.start();
    }

}
