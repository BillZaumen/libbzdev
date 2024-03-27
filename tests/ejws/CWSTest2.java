import java.io.File;
import org.bzdev.ejws.*;
import org.bzdev.ejws.maps.*;
import org.bzdev.net.*;

public class CWSTest2 {

    public static void main(String argv[]) throws Exception {
	File config = new File(argv[0]);

	ConfigurableWS server = new ConfigurableWS(null, config, null);

	EmbeddedWebServer ews = server.getServer();
	ews.add("/", DirWebMap.class, new File("../../BUILD/api"),
		null, true, true, true);
	ews.add("/api/", DirWebMap.class, new File("../../BUILD/api"),
		null, true, true, true);
	ews.getWebMap("/api/").addWelcome("index.html");
	ews.add("/sfgate/", RedirectWebMap.class,
		"https://www.sfgate.com/",
		null, true, true, true);
	ews.add("/resource/", ResourceWebMap.class,
		"org/bzdev/ejws",
		null, true, true, true);
	ews.add("/zip/", ZipWebMap.class,
		 "example.zip",
		null, true, true, true);
	ews.add("/post/", ServletWebMap.class,
		new ServletWebMap.Config(new NullAdapter(),
					 null, true, HttpMethod.GET,
					 HttpMethod.POST),
		null, true, true, true);

	server.start();
    }
}
