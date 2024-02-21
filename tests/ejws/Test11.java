import org.bzdev.ejws.*;
import org.bzdev.ejws.maps.*;
import org.bzdev.net.WebDecoder;
import org.bzdev.util.ErrorMessage;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Map;

public class Test11 {
    public static void main(String argv[]) throws Exception {
	ErrorMessage.setStackTrace(true);
	EmbeddedWebServer ews = new EmbeddedWebServer(8080, 48, 2);

	ews.add("/post/", EchoMap.class, null, null,
		true, true, true);
	ews.start();
	System.out.println("**** will echo whatever was posted "
			   + "(must be text) ****");
    }
}
