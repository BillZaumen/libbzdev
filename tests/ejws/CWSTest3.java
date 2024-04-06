import java.io.*;
import java.net.*;
import java.util.Properties;
import java.util.Set;
import org.bzdev.ejws.*;
import org.bzdev.util.*;

public class CWSTest3 {

    public static void main(String argv[]) throws Exception {
	File config = new File(argv[0]);

	Set<String> extraProps = Set.of("foo", "bar");
	Set<String> extraKeys = Set.of("key1", "key2");


	ConfigurableWS server = new ConfigurableWS(extraProps, extraKeys,
						   config, null);

	Properties props = server.getProperties();
	JSArray contexts = server.getContexts();
	JSObject remainder = server.getRemainder();

	System.out.println("foo = " + props.getProperty("foo"));
	System.out.println("bar = " + props.getProperty("bar"));
			   
	System.out.println("second prefix: "
			   + contexts.get(1, JSObject.class)
			   .get("prefix", String.class));

	System.out.println("key1 = " + remainder.get("key1", String.class));
	System.out.println("key2 = " + remainder.get("key2", String.class));
	System.exit(1);

    }
}
