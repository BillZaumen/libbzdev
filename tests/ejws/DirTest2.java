import org.bzdev.ejws.*;
import org.bzdev.net.*;
import org.bzdev.ejws.maps.*;
import org.bzdev.util.ErrorMessage;
import java.io.File;
import java.net.URL;

public class DirTest2 {
    public static void main(String argv[]) throws Exception {
	// ErrorMessage.setStackTrace(true);
	EmbeddedWebServer ews = new
	    EmbeddedWebServer(null, 8443, 48, 2,
			      CertManager.newInstance()
			      .setProtocol("TLS")
			      .setDomain("localhost")
			      .setInterval(1)
			      .setKeystoreFile(new File("dirtest2.jks")));

	ews.setRootColors("white", "black", "green", "green");
	ews.add("/Dir", DirWebMap.class,
		new DirWebMap.Config(new File("../../BUILD/api/"),
				     "white", "black", "green", "green"),
		null, true, true, true);
	ews.start();

	System.out.println("starting helper");
	EmbeddedWebServer helper = new EmbeddedWebServer(8080);
	helper.add("/Dir/", RedirectWebMap.class,
		"https://localhost:8443/Dir/",
		null, true, false, true);
	helper.start();
	
    }
}
