import org.bzdev.ejws.*;
import org.bzdev.ejws.maps.*;
import org.bzdev.net.HttpMethod;
import emodel.Adapter;

public class Server {

    public static void main(String argv[]) throws Exception {

        System.setProperty("java.awt.headless", "true");
	EmbeddedWebServer ews = new EmbeddedWebServer(8080, 10, 20, null);

	ews.add("/", ResourceWebMap.class, "/", null, true, false, true);

	WebMap map = ews.getWebMap("/");
	map.addWelcome("model.html");

	ews.add("/servlet/adapter/", ServletWebMap.class,
		new ServletWebMap.Config(new Adapter(),
					 null, true,
					 HttpMethod.GET,
					 HttpMethod.HEAD,
					 HttpMethod.OPTIONS,
					 HttpMethod.TRACE),
		null, true, false, true);

	ews.setTracer("/", System.out, true);
	ews.setTracer("/servlet/adapter/", System.out, true);

	ews.start();
    }
}
