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

// use to test SBL with an actual login page but without SSL
public class Demo2 {
    public static void main(String argv[]) throws Exception {
	// ErrorMessage.setStackTrace(true);
	String realm = "realm";
	System.out.println(realm);
 	
	String host = argv[3];

	String adminEmail = (argv.length > 5)? argv[4]: null;
	String adminFpr = (argv.length > 5)? argv[5]: null;
	System.out.println("adminEmail = " + adminEmail);
	System.out.println("adminFpr = " + adminFpr);


	InetSocketAddress saddr = new InetSocketAddress("0.0.0.0", 8080);

	EmbeddedWebServer ews = new
	    EmbeddedWebServer(saddr.getAddress(), 8080, 48, 10);

	EjwsBasicAuthenticator auth = new
	    EjwsBasicAuthenticator(ews, realm);
	auth.setGPGHome(new File(argv[2]));
	auth.createAuthCode("test secret");
	auth.setSBLStore(new File(System.getProperty("user.dir")
				  + "/sbldata2"));
	auth.setTracer(System.out);
	if (adminEmail != null) {
	    auth.addToAdminMap(adminEmail, adminFpr);
	}
	auth.setCanAddAccount(true);
	auth.setDefaultActive(false);


	File tdir = new File(argv[0]);
	File dir = new File(argv[1]);


	System.out.println("auth mode = " + auth.getMode());
	auth.setTracer(System.out);

	URI logoutURI = new URI("http://" + host + ":8080/loginpage.html");

	ews.add("/", DirWebMap.class, tdir, null, true, true, true)
	    .addWelcome("loginpage.html");
	System.out.println("added /");

	ews.add("/docs/", DirWebMap.class, dir, auth, true, true, true)
	    .addWelcome("index.html")
	    .setLoginAlias("login", "", true)
	    .setLogoutAlias("logout", logoutURI)
	    .setAdminAlias("admin");
	System.out.println("added /docs/");
	auth.add("user", "password");
	auth.makeUserActive("user");
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
