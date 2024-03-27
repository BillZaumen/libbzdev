import java.io.File;
import org.bzdev.ejws.*;

public class CWSTest {

    public static void main(String argv[]) throws Exception {
	File config = new File(argv[0]);

	ConfigurableWS server = new ConfigurableWS(null, config, null);

	server.start();
    }

}
