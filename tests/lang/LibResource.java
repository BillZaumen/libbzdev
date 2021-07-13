import org.bzdev.lang.*;
import java.util.Iterator;

public class LibResource {
    static final String osName = 
	System.getProperty("os.name").replaceAll("\\s", "");
    static final String systemArch = 
	System.getProperty("os.arch").replaceAll("\\s", "");
    static final String vers =
	System.getProperty("os.version").replaceAll("\\s", "");

    public static void main(String argv[]) throws Exception {


	ResourceLibLoader rl = new ResourceLibLoader();

	System.out.println(osName + " " + systemArch + " " + vers);

	Iterator<String> it = rl.getResources("foo");
	while (it.hasNext()) {
	    System.out.println(it.next());
	}
    }
}