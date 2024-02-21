import org.bzdev.ejws.*;
import org.bzdev.ejws.maps.*;
import org.bzdev.util.ErrorMessage;
import java.io.File;

public class Test9 {
    public static void main(String argv[]) throws Exception {
	ErrorMessage.setStackTrace(true);
	EmbeddedWebServer ews = new EmbeddedWebServer(8080, 48, 2);

	ews.add("/", DirWebMap.class, new File("ewstest"), null,
		true, true, true);

	FileHandler fh = (FileHandler)ews.getHttpHandler("/");
	fh.setTracer(System.out);

	ews.start();
    }
}
