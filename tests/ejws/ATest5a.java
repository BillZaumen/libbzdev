import org.bzdev.ejws.*;
import org.bzdev.ejws.maps.*;
import org.bzdev.util.ErrorMessage;
import java.io.File;
import java.net.URL;

public class ATest5a {
    public static void main(String argv[]) throws Exception {
	// ErrorMessage.setStackTrace(true);
	EmbeddedWebServer ews = new EmbeddedWebServer(8080, 48, 2, null);
	ews.add("/",
		RedirectWebMap.class,
		new URL("https://www.youtube.com/watch?v=tzQuuoKXVq0"),
		null,
		true, true, true);
	ews.setTracer("/", System.out);
	ews.start();
    }
}
