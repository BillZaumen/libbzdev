import org.bzdev.lang.ClassFinder;

public class ClassFinderTest {

    public static void main(String argv[]) {

	try {
	    System.setSecurityManager(new SecurityManager());
	    System.out.println("security manager installed");
	} catch (UnsupportedOperationException eu) {}


	System.out.println(ClassFinder
			   .classExists("org.bzdev.graphs.Graph"));
	System.out.println(ClassFinder
			   .classExists("org.bzdev.graphs.Graph$Axis"));

	String name1 = "org.bzdev.graphs.Graph";
	String name2 = "org.bzdev.graphs.Graph.Axis";
	System.out.println(name1 + "  -> " +  ClassFinder.getBinaryName(name1));
	System.out.println(name2 + "  -> " +  ClassFinder.getBinaryName(name2));

    }
}
