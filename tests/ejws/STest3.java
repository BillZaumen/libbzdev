import org.bzdev.ejws.*;
import org.bzdev.ejws.maps.*;
import org.bzdev.util.ErrorMessage;
import java.io.File;

public class STest3 {
    public static void main(String argv[]) throws Exception {
	// ErrorMessage.setStackTrace(true);
	EmbeddedWebServer ews = new EmbeddedWebServer
	    (8080, 48, 10, new EmbeddedWebServer.SSLSetup(argv[0]));
	ews.add("/", DirWebMap.class, new File("../../BUILD/api/"), null,
		true, true, true);
	ews.start();
    }
}
