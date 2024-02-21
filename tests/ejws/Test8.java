import org.bzdev.ejws.*;
import org.bzdev.ejws.maps.*;
import java.io.File;
import com.sun.net.httpserver.*;
import java.net.URL;

// Needs an instance running on port 8080 to test this.

public class Test8 {
    public static void main(String argv[]) throws Exception {
	EmbeddedWebServer ews = new EmbeddedWebServer(8081, 48, 2);

	String cdir = System.getProperty("user.dir");

	URL url = new URL("http://localhost:8080");

	ews.add("/", URLWebMap.class,
		new URLWebMap.Params(url, true),
		null,
		true, true, true);
	for (String prefix: ews.getPrefixes()) {
	    System.out.println("saw prefix \"" + prefix + "\"");
	}
	ews.start();
    }
}
