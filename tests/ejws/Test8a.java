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

public class Test8a {
    public static void main(String argv[]) throws Exception {
	// ErrorMessage.setStackTrace(true);
	String realm = "realm";
	System.out.println(realm);
	
	EmbeddedWebServer ews = new
	    EmbeddedWebServer(8080, 48, 10);
	EjwsBasicAuthenticator auth = new EjwsBasicAuthenticator(ews, realm);

	File dir = new File(argv[0]);

	// We want to make it easy for the time limit to expire
	auth.setTimeLimit(300);

	String user = "user";
	String password = "password";

	auth.add(user, password);
	auth.setTracer(System.out);

	ews.add("/", DirWebMap.class, dir, auth, true, true, true);
	WebMap webmap = ews.getWebMap("/");
	webmap.addWelcome("/index.html");

	FileHandler handler = (FileHandler) ews.getHttpHandler("/");
	handler.setLoginAlias("login.html", "index.html", true);
	URI logoutURI = new URI("https://www.google.com");
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


	// CloseWaitService cws = new CloseWaitService(120, 30, saddr);
	// cws.start();

	ews.setTracer("/", System.out);
	ews.start();
	System.out.println("ews started");
    }
}
