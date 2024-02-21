import org.bzdev.ejws.*;
import org.bzdev.ejws.maps.*;
import java.io.File;
import com.sun.net.httpserver.*;
import java.util.Properties;

public class Test3 {
    public static void main(String argv[]) throws Exception {
	EmbeddedWebServer ews = new EmbeddedWebServer(8080);

	Properties properties = new Properties();
	properties.put("foo.txt", "this is a test 1".getBytes("UTF-8"));
	properties.put("foo/bar.txt", "this is a test 2".getBytes("UTF-8"));
	properties.put("foo/foo.txt", "this is a test 3".getBytes("UTF-8"));
	properties.put("foo/bar/foo.txt", "this is a test 4".getBytes("UTF-8"));

	ews.add("/", PropertiesWebMap.class, properties, null,
		true, true, true);
	for (String prefix: ews.getPrefixes()) {
	    System.out.println("saw prefix \"" + prefix + "\"");
	}

	if (ews.containsPrefix("/")) {
	    HttpHandler handler = ews.getHttpHandler("/");
	    System.out.println("Handler class: " + handler.getClass());
	}
	ews.start();
    }
}
