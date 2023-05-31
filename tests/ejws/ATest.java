import org.bzdev.ejws.*;
import org.bzdev.ejws.maps.*;
import org.bzdev.util.ErrorMessage;
import java.io.File;
import java.net.URI;

public class ATest {
    public static void main(String argv[]) throws Exception {
	// ErrorMessage.setStackTrace(true);
	String realm = "realm";
	System.out.println(realm);
	EjwsBasicAuthenticator auth =
	    new EjwsBasicAuthenticator(realm);
	auth.add("foo", "foo");
	EmbeddedWebServer ews = new EmbeddedWebServer(8080, 48, 2, null);
	ews.add("/", DirWebMap.class, new File("../../BUILD/api/"), auth,
		true, true, true);
	FileHandler handler = (FileHandler) ews.getHttpHandler("/");
	handler.setLoginAlias("login.html");
	handler.setLogoutAlias("logout.html",
			       new URI("https://www.google.com/"));

	auth.setLoginFunction((p, t) -> {
		System.out.println("login: " + p.getUsername());
	    });

	auth.setAuthorizedFunction((p, t) -> {
		System.out.println("logged in as " + p.getUsername());
	    });

	auth.setLogoutFunction((p, t) -> {
		System.out.println("logout: " + p.getUsername());
	    });
	ews.setTracer("/", System.out);
	ews.start();
    }
}
