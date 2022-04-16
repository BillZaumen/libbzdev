import org.bzdev.ejws.*;
import org.bzdev.ejws.maps.*;
import org.bzdev.util.ErrorMessage;
import java.io.File;

public class ATest {
    public static void main(String argv[]) throws Exception {
	// ErrorMessage.setStackTrace(true);
	EjwsBasicAuthenticator auth = new EjwsBasicAuthenticator("realm");
	auth.add("foo", "foo");
	EmbeddedWebServer ews = new EmbeddedWebServer(8080, 48, 2, null);
	ews.add("/", DirWebMap.class, new File("../../BUILD/api/"), auth,
		true, true, true);
	ews.setTracer("/", System.out);
	ews.start();
    }
}
