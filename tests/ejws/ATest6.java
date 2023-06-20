import org.bzdev.ejws.*;
import org.bzdev.ejws.maps.*;
import org.bzdev.util.ErrorMessage;
import java.io.File;
import java.io.FileInputStream;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.Certificate;
import java.util.Iterator;
import java.util.Properties;
import org.bzdev.swing.ConfigPropertyEditor;

public class ATest6 {
    public static void main(String argv[]) throws Exception {
	// ErrorMessage.setStackTrace(true);
	String realm = "realm";
	System.out.println(realm);
	File sblFile = new File(argv[0]);
	String key = argv[1];
	
	InetSocketAddress saddr = new InetSocketAddress("0.0.0.0", 8080);

	EmbeddedWebServer ews = new
	    EmbeddedWebServer(saddr.getAddress(),
			      8080, 48, 10,
			      (new EmbeddedWebServer.SSLSetup("TLS"))
			      .keystore(new FileInputStream("thelio-ks.jks"))
			      .truststore(new FileInputStream
					  ("thelio-ts.jks")));

	    Certificate[] certs = ews.getCertificates();
	    System.out.println("Number of certificates = " + certs.length);

	    EjwsSecureBasicAuth auth = new EjwsSecureBasicAuth(realm, certs);

	    // We want to make it easy for the time limit to expire
	    auth.setTimeLimits(-2 , 30, 45);
	    System.out.println("auth mode = " + auth.getMode());

	    ConfigPropertyEditor cpe = new ConfigPropertyEditor() {
		    @Override
		    protected String errorTitle()
		    {return "";}
		    @Override
		    protected String configTitle() {return "";}
		    @Override
		    protected String mediaType() {
			return "application/vnd.bzdev.sblauncher";
		    }
		    @Override
		    protected String extensionFilterTitle() {
			return ("extensionFilterTitle");
		    }
		    @Override
		    protected String extension() {return "sbl";}
		};

	    cpe.loadFile(sblFile);
	    Properties props = cpe.getDecodedProperties();
	    String user = props.getProperty(key + ".user");
	    String password = props.getProperty(key + ".password");
	    String publicKeyPem = props.getProperty("keypair.publicKey");
	    auth.add("foo", publicKeyPem, "foo");
	    auth.setTracer(System.out);

	ews.add("/", DirWebMap.class, new File("../../BUILD/api/"), auth,
		true, true, true);

	FileHandler handler = (FileHandler) ews.getHttpHandler("/");
	handler.setLoginAlias("login.html", "", true);
	URI logoutURI = (argv.length == 1)?
	    new URI("https://www.google.com"): new URI(argv[1]);

	handler.setLogoutAlias("logout.html", logoutURI);

	auth.setLoginFunction((p, t) -> {
		System.out.println("login: " + p.getUsername());
	    });

	auth.setAuthorizedFunction((p, t) -> {
		System.out.println("logged in: " + p.getUsername());
	    });

	auth.setLogoutFunction((p, t) -> {
		System.out.println("logout: " + p.getUsername());
	    });

	// CloseWaitService cws = new CloseWaitService(120, 30, saddr);
	// cws.start();

	ews.setTracer("/", System.out);
	ews.start();
    }
}
