import org.bzdev.net.*;
import java.io.*;
import java.net.*;

public class SSLUtilTest3 {


    public static void main(String argv[]) throws Exception {

	SSLUtilities.allowLoopbackHostname();
	SSLUtilities.installTrustManager("TLS", null, null,
					 (cert) -> {return true;});

	URL url = new URL("https://localhost:8443/");
	URLConnection urlc = url.openConnection();
	InputStream is = urlc.getInputStream();
	is.transferTo(System.out);
	is.close();
    }
}
