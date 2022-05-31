import org.bzdev.obnaming.*;
import java.net.URL;
import java.util.Enumeration;
import org.bzdev.scripting.ScriptingContext;

public class LTest {
    public static void main(String argv[]) throws Exception  {
	String service =
	    "META-INF/services/org.bzdev.obnaming.NamedObjectFactory";
	Enumeration<URL> resources = ClassLoader.getSystemResources(service);
	System.out.println("registered named object factory resources: ");
	while (resources.hasMoreElements()) {
	    URL url = resources.nextElement();
	    System.out.println("    " + url);
	}
	System.out.println();

	// ScriptingContext.grantPermissionFor(ATestNamer.class);

	try {
	    System.setSecurityManager(new SecurityManager());
	} catch (UnsupportedOperationException eu) {}

	ATestNamer anamer = new ATestNamer();
	NamedObjectFactory f = new LTestFactory(anamer);

	f.set("value1", 100);
	NamedObjectOps object = f.createObject("object");
	System.out.println("object.getName() = " + object.getName());

	System.out.println("try to create with a null constructor");
	NamedObjectFactory ff = new LTestFactory(null);
	System.out.println(ff.getClass().getName());
	System.exit(0);
    }
}
