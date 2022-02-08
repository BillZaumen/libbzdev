import org.bzdev.ejws.*;
import org.bzdev.ejws.maps.*;
import org.bzdev.net.WebDecoder;
import org.bzdev.net.HttpSessionOps;
import org.bzdev.util.ErrorMessage;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.HashMap;

public class HTest {
    public static void main(String argv[]) throws Exception {
	ErrorMessage.setStackTrace(true);
	EmbeddedWebServer ews = new EmbeddedWebServer(8080, 48, 2, null);

	HttpSessionOps sessionOps = new EjwsStateTable() {
		@Override
		public void remove(String sid) {
		    super.remove(sid);
		    System.out.println("removing " + sid);
		}
		@Override
		public void put(String sid, Object obj) {
		    super.put(sid, obj);
		    System.out.println("adding " + sid + ", value = " + obj);
		}
	    };

	ews.add("/", HeaderMap.class, sessionOps, null,
		true, true, true);

	ews.addSessionFilter("/", sessionOps);
	ews.setTracer("/", System.out);
	ews.start();
	System.out.println("**** will echo headers ****");
    }
}
