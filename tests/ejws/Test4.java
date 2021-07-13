import org.bzdev.ejws.*;
import org.bzdev.ejws.maps.*;
import java.io.File;
import com.sun.net.httpserver.*;

public class Test4 {
    public static void main(String argv[]) throws Exception {
	EmbeddedWebServer ews = new EmbeddedWebServer(8080, 48, 2, null);

	ews.add("/", ZipWebMap.class, new File("../../BUILD/libbzdev-base.jar"),
		null,
		true, true, true);
	for (String prefix: ews.getPrefixes()) {
	    System.out.println("saw prefix \"" + prefix + "\"");
	}

	/*
	if (ews.containsPrefix("/")) {
	    HttpHandler handler = ews.getHttpHandler("/");
	    System.out.println("Handler class: " + handler.getClass());
	    WebMap map = ews.getWebMap("/");
	    if (map != null) {
		map.addWelcome("index.html");
	    }
	}
	*/
	ews.start();
    }
}
