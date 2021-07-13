import org.bzdev.ejws.*;
import org.bzdev.ejws.maps.*;
import org.bzdev.util.ErrorMessage;
import java.io.File;
import java.io.FileInputStream;

// See https://gist.github.com/wsargent/11062032
// for how to generate a certificate authority using keytool.
// We apparently need that to create a trust anchor.
//
// Also https://docs.oracle.com/cd/E19798-01/821-1841/gjrgy/
// for another example.
//
// Also for another example where a client certificate is created.
// https://gist.github.com/wsargent/11023607

// https://gist.github.com/granella/01ba0944865d99227cf080e97f4b3cb6

// Example using derby with ssl (shows how to use system properties)
// https://docs.oracle.com/javadb/10.8.3.0/adminguide/cadminsslclient.html

public class STest1 {
    public static void main(String argv[]) throws Exception {
	// ErrorMessage.setStackTrace(true);
	EmbeddedWebServer ews = new
	    EmbeddedWebServer(8080, 48, 10,
			      (new EmbeddedWebServer.SSLSetup())
			      .keystore(new FileInputStream("keystore.jks"))
			      .truststore(new FileInputStream("cacerts.jks")));
	ews.add("/", DirWebMap.class, new File("../../BUILD/api/"), null,
		true, true, true);
	ews.start();
    }
}
