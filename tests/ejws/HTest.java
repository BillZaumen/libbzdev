import org.bzdev.ejws.*;
import org.bzdev.ejws.maps.*;
import org.bzdev.net.WebDecoder;
import org.bzdev.net.HttpSessionOps;
import org.bzdev.util.ErrorMessage;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;

public class HTest {
    public static void main(String argv[]) throws Exception {
	ErrorMessage.setStackTrace(true);
	EmbeddedWebServer ews = new EmbeddedWebServer(8080, 48, 2, null);

	HttpSessionOps<String> sessionOps = new HttpSessionOps() {
		Set<String> set = new HashSet();
		@Override
		public void remove(String sid) {
		    System.out.println("removing " + sid);
		    set.remove(sid);
		}

		@Override
		public boolean contains(String sid) {
		    return set.contains(sid);
		}
		
		@Override
		public void add(String sid) {
		    System.out.println("adding " + sid);
		    set.add(sid);
		}
		@Override
		public String get(String sid) {
		    return (set.contains(sid)? sid: null);
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
