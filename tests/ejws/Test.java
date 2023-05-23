import org.bzdev.ejws.*;
import org.bzdev.ejws.maps.*;
import org.bzdev.util.ErrorMessage;
import java.io.File;

public class Test {
    public static void main(String argv[]) throws Exception {
	// ErrorMessage.setStackTrace(true);
	EmbeddedWebServer ews = new EmbeddedWebServer(8080, 48, 2, null);
	ews.add("/", DirWebMap.class, new File("../../BUILD/api/"), null,
		true, true, true);
	FileHandler handler = (FileHandler) ews.getHttpHandler("/");
	handler.setLoginAlias("login.html");
	ews.setTracer("/", System.out);
	ews.start();
    }
}
