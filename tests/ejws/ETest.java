import com.sun.net.httpserver.*;
import java.awt.*;
import java.net.*;
import org.bzdev.ejws.*;
import org.bzdev.ejws.maps.*;

public class ETest {
    static int port = 8080;

    static EmbeddedWebServer ews = null;
    static URI manualURI;
    public static synchronized void startWebServer()
	throws Exception
    {
	if (ews == null) {
	    // ews = new EmbeddedWebServer(port, 40, 1, false);
	    ews = new EmbeddedWebServer(port, 40, 1);
	    if (port == 0) port = ews.getPort();
	    ews.add("/", ResourceWebMap.class, "manual/",
		    null, true, false, true);
	    WebMap wmap = ews.getWebMap("/");
	    if (wmap != null) {
		wmap.addWelcome("index.html");
		wmap.addMapping("html", "text/html; charset=utf-8");

	    }
	    // ews.setTracer("/", System.out);
	    manualURI = new URL("http://localhost:"
				+ port +"/epts.html").toURI();
	    ews.start();
	}
    }

    private static void showManualInBrowser() {
	try {
	    startWebServer();
	    Desktop.getDesktop().browse(manualURI);
	    (new Thread(new Runnable() {
		    public void run() {
			try {
			    Thread.sleep(60000);
			    System.out.println("stopping server");
			    ews.stop(10);
			    Thread.sleep(10000);
			    System.out.println("restarting server");
			    ews.start();
			    Thread.sleep(60000);
			    System.out.println("stopping server");
			    ews.stop(10);
			} catch (Exception e) {}
		    }
		})).start();
	} catch (Exception e) {
	    // setModeline(localeString("cannotOpenBrowser"));
	    e.printStackTrace();
	    System.exit(1);
	}
    }

    public static void main(String argv[]) throws Exception {
	showManualInBrowser();
    }
}
