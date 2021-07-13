import java.awt.*;
import java.net.*;
import org.bzdev.ejws.*;
import org.bzdev.ejws.maps.*;
import java.io.File;

public class Test0 {
    public static void main(String argv[]) throws Exception {
	// org.bzdev.util.ErrorMessage.setStackTrace(true);
	EmbeddedWebServer ews = new EmbeddedWebServer(0, null);
	System.out.println("using port " + ews.getPort());

	ews.add("/", DirWebMap.class, new File("../../BUILD/api/"), null,
		true, true, true);
	ews.start();
	URI uri = new URI("http://localhost:" + ews.getPort() + "/");
	Desktop.getDesktop().browse(uri);
    }
}
