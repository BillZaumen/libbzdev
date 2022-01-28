import org.bzdev.ejws.*;
import org.bzdev.ejws.maps.*;
import java.io.File;
import com.sun.net.httpserver.*;

public class Test7 {
    public static void main(String argv[]) throws Exception {
	boolean nowebxml = (argv.length == 0);
	EmbeddedWebServer ews = new EmbeddedWebServer(8080, 48, 2, null);

	ews.add("/", ZipWebMap.class, new File("example.zip"),
		null,
		nowebxml, true, true);
	for (String prefix: ews.getPrefixes()) {
	    System.out.println("saw prefix \"" + prefix + "\"");
	}
	ews.start();
    }
}
