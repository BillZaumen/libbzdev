import org.bzdev.net.*;
import java.io.*;
import java.net.*;

/*
 * Requires running 'make cwstest3' in ../ejws to set up a server.
 * This verifies that the server is working, at least to the point of
 * serving web pages and that we don't need a truststore to access it if
 * we use alloLoopbackHostname and installTrustManager with no truststore
 * and accept all self-signed certificates.
 */

public class SSLUtilTest4 {


    public static void main(String argv[]) throws Exception {

	SSLUtilities.allowLoopbackHostname();
	SSLUtilities.installTrustManager("TLS", null, null,
					 (cert) -> {return true;});

	URL url = new URL("https://localhost:8443/api/");
	URLConnection urlc = url.openConnection();
	InputStream is = urlc.getInputStream();
	is.transferTo(System.out);
	is.close();
    }
}
