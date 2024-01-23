import org.bzdev.ejws.*;
import org.bzdev.net.*;
import org.bzdev.ejws.maps.*;
import org.bzdev.util.ErrorMessage;
import java.io.File;
import java.net.URL;

public class DirTest {
    public static void main(String argv[]) throws Exception {
	// ErrorMessage.setStackTrace(true);
	EmbeddedWebServer ews = new EmbeddedWebServer(8080, 48, 2, null);
	ews.setRootColors("white", "black", "green", "green");
	ews.add("/servlet",
		ServletWebMap.class,
		new ServletWebMap.Config(new NullAdapter(), null,
					 true,
					 HttpMethod.GET),
		null, true, false, true);

	ews.add("/Dir", DirWebMap.class,
		new DirWebMap.Config(new File("../../BUILD/api/"),
				     "white", "black", "green", "green"),
		null, true, true, true);
	ews.add("/post/", ServletWebMap.class,
		new ServletWebMap.Config(new NullAdapter(), null,
					 true,
					 HttpMethod.GET, HttpMethod.POST),
		null, true, false, true);

	ews.setTracer("/", System.out);
	ews.start();
    }
}
