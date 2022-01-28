import org.bzdev.ejws.*;
import org.bzdev.ejws.maps.*;
import org.bzdev.util.ErrorMessage;
import java.io.File;

public class Test6 {
    public static void main(String argv[]) throws Exception {
	boolean nowebxml = (argv.length == 0);

	// ErrorMessage.setStackTrace(true);
	EmbeddedWebServer ews = new EmbeddedWebServer(8080, 48, 2, null);

	ews.add("/", DirWebMap.class, new File("example"), null,
		nowebxml, true, true);
	ews.start();
    }
}
