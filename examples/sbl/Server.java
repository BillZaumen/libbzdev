import java.io.File;
import java.io.FileInputStream;
import java.net.*;
import java.security.cert.Certificate;
import java.util.Properties;
import org.bzdev.ejws.*;
import org.bzdev.ejws.maps.*;
import org.bzdev.util.ConfigPropUtilities;

public class Server {
    public static void main(String argv[]) throws Exception {
	Properties props = ConfigPropUtilities
	    .newInstance(new File("access.sbl"),
			 "application/vnd.bzdev.sblauncher");
	
	InetSocketAddress saddr = new InetSocketAddress("0.0.0.0", 8080);

	EmbeddedWebServer ews = new
	    EmbeddedWebServer(saddr.getAddress(),
			      8080, 48, 10,
			      (new EmbeddedWebServer.SSLSetup("TLS"))
			      .keystore(new FileInputStream("keystore.jks")));
	Certificate[] certs = ews.getCertificates();
	EjwsSecureBasicAuth auth = new EjwsSecureBasicAuth("realm", certs);
	
	String user = ConfigPropUtilities.getProperty(props, "user.user");
	String password = ConfigPropUtilities
	    .getProperty(props, "user.password");
	String publicKey = ConfigPropUtilities
	    .getProperty(props, "base64.keypair.publicKey");
	auth.add(user, publicKey, password);


	File cdir = new File(System.getProperty("user.dir"));
	ews.add("/", DirWebMap.class, cdir, null, true, false, true);
	WebMap webmap = ews.getWebMap("/");
	webmap.addMapping("sbl", "application/vnd.bzdev.sblauncher");
	webmap.addWelcome("access.sbl");

	File dir = new File("/usr/share/doc/libbzdev-doc/api/");
	ews.add("/api/", DirWebMap.class, dir, auth, true, false, true);
	webmap = ews.getWebMap("/api/");
	webmap.addWelcome("index.html");
	ews.setTracer("/", System.out);
	ews.setTracer("/api/", System.out);
	ews.start();
    }
}
