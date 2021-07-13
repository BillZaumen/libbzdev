import org.bzdev.ejws.*;
import org.bzdev.ejws.maps.*;
import org.bzdev.net.WebDecoder;
import org.bzdev.util.ErrorMessage;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Map;

public class Test10 {
    public static void main(String argv[]) throws Exception {
	ErrorMessage.setStackTrace(true);
	EmbeddedWebServer ews = new EmbeddedWebServer(8080, 48, 2, null);

	ews.add("/data/", DirWebMap.class, new File("ewstest"), null,
		true, true, true);

	ews.add("/post/", PostMap.class, null, null,
		true, true, true);

	FileHandler fh = (FileHandler)ews.getHttpHandler("/post");
	fh.setTracer(System.out);

	ews.start();

    }
}
