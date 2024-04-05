import java.io.*;
import java.net.*;
import org.bzdev.ejws.*;

public class CWSTest {

    public static void main(String argv[]) throws Exception {
	File config = new File(argv[0]);

	ConfigurableWS server = new ConfigurableWS(null, config, null);

	server.start();

	try {
	    if (argv.length >= 2) {
		URL url = new URL(argv[1]);
		URLConnection urlc = url.openConnection();
		InputStream is = urlc.getInputStream();
		is.transferTo(System.out);
		is.close();
	    }
	} catch(Exception e) {
	    System.out.println("*** could not handle " +argv[1]);
	    e.printStackTrace(System.out);
	    System.out.println("*** continuing");
	}
    }
}
