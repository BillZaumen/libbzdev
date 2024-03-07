import org.bzdev.ejws.*;
import org.bzdev.ejws.maps.*;
import org.bzdev.util.ErrorMessage;
import java.io.File;
import java.io.IOException;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpsExchange;
import java.util.Base64;
import javax.net.ssl.SSLSession;

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

public class STest {
    public static void main(String argv[]) throws Exception {
	// ErrorMessage.setStackTrace(true);
	EmbeddedWebServer ews = new EmbeddedWebServer
	    (8080, 48, 10, new EmbeddedWebServer.SSLSetup());
	    /*
	    EmbeddedWebServer(8080, 48, 2,
			      new FileInputStream("keystore.jks"),
			      "changeit".toCharArray(),
			      "changeit".toCharArray(),
			      new FileInputStream("cacerts.jks")
			      "changeit".toCharArray(),
			      null,
			      null
			      );
	    */
	if (!ews.usesHTTPS()) {
	    throw new Exception("not HTTPS");
	}

	ews.add("/", DirWebMap.class, new File("../../BUILD/api/"), null,
		true, true, true);

	Filter f = new Filter() {
		public String description() {return "ssh test";}
		public void doFilter(HttpExchange exch, Filter.Chain chain)
		    throws IOException
		{
		    if (exch instanceof HttpsExchange) {
			HttpsExchange sexch = (HttpsExchange) exch;
			SSLSession session = sexch.getSSLSession();
			Base64.Encoder enc = Base64.getEncoder()
			    .withoutPadding();
			System.out.println("SSL ID: " + enc
					   .encodeToString(session.getId())) ;
		    } else {
			System.out.println("exch not HttpsExchange");
		    }

		    chain.doFilter(exch);
		}
	    };

	System.out.println("adding filter: " + ews.addFilter("/", f));
	ews.start();
    }
}
