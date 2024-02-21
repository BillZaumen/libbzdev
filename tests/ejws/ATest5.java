import org.bzdev.ejws.*;
import org.bzdev.ejws.maps.*;
import org.bzdev.util.ErrorMessage;
import java.io.File;
import java.net.URL;

public class ATest5 {
    public static void main(String argv[]) throws Exception {
	// ErrorMessage.setStackTrace(true);
	String realm = "realm";
	System.out.println(realm);
	EjwsBasicAuthenticator auth =
	    new EjwsBasicAuthenticator(realm);
	auth.add("foo", "foo");
	EmbeddedWebServer ews = new EmbeddedWebServer(8080, 48, 2);
	ews.add("/",
		RedirectWebMap.class, new URL("https://www.sfgate.com"),
		auth,
		true, true, true);
	ews.setTracer("/", System.out);
	ews.start();
    }
}
