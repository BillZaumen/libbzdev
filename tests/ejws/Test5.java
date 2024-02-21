import org.bzdev.ejws.*;
import org.bzdev.ejws.maps.*;
import java.io.File;
import com.sun.net.httpserver.*;

public class Test5 {
    public static void main(String argv[]) throws Exception {
	EmbeddedWebServer ews = new EmbeddedWebServer(8080, 48, 2);

	ews.add("/", ResourceWebMap.class, "org/bzdev/ejws/",
		null, true, true, true);
	for (String prefix: ews.getPrefixes()) {
	    System.out.println("saw prefix \"" + prefix + "\"");
	}
	ews.start();
    }
}
