import org.bzdev.ejws.*;
import org.bzdev.ejws.maps.*;
import org.bzdev.net.WebDecoder;
import org.bzdev.util.ErrorMessage;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Map;

public class ErrorTest {
    public static void main(String argv[]) throws Exception {
	ErrorMessage.setStackTrace(true);
	EmbeddedWebServer ews = new EmbeddedWebServer(8080, 48, 2);

	ews.add("/query/", ErrorMap.class, null, null,
		true, true, true);
	ews.setRootColors("white", "rgb(10,10,32)", null, null);

	ews.start();
	System.out.println("Generates an error response");
	System.out.println("... try HTTP://localhost/8080/query/?io=true");
	System.out.println("... or HTTP://localhost/8080/query/?rt=true");
    }
}
