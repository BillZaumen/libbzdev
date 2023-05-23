import org.bzdev.ejws.*;
import org.bzdev.ejws.maps.*;
import org.bzdev.util.ErrorMessage;
import java.io.File;
import java.net.URI;

public class ATest2 {
    public static void main(String argv[]) throws Exception {
	// ErrorMessage.setStackTrace(true);
	String realm = "realm";
	System.out.println(realm);
	EjwsSecureBasicAuth auth =
	    new EjwsSecureBasicAuth(realm);
	System.out.println("auth mode = " + auth.getMode());
	auth.add("foo", "foo");
	auth.setTimeLimits(-2, 30, 45);
	EmbeddedWebServer ews = new EmbeddedWebServer(8080, 48, 2, null);
	ews.add("/", DirWebMap.class, new File("../../BUILD/api/"), auth,
		true, true, true);
	ews.add("/top/", DirWebMap.class,
		new File("../../BUILD/api/factories-api/"), null,
		true, true, true);
	FileHandler handler = ews.getFileHandler("/");
	handler.setLoginAlias("login.html"/*, 10*/);
	handler.setLogoutAlias("logout.html",
			       new URI("https://www.google.com"));

	auth.setLoginFunction((p, t) -> {
		System.out.println("login: " + p.getUsername());
	    });

	auth.setLogoutFunction((p, t) -> {
		System.out.println("logout: " + p.getUsername());
	    });
	// handler.setLogoutURI("/top/index.html");
	ews.setTracer("/", System.out);
	ews.start();
    }
}
