package testpkg;

import org.bzdev.scripting.*;
import org.bzdev.math.rv.*;
import java.util.HashSet;

public class Test {

    public static class TC<T> {
	T getnull() {return null;}
    }

    public static void main(String argv[]) throws Exception {
	testpkg.ATestNamer anamer = new testpkg.ATestNamer();

	System.out.println("verifying that it works when in a package");

	if (!(anamer instanceof ScriptingContext)) {
	    System.out.println("anamer is not a scripting context");
	    System.exit(1);
	}

	testpkg.ATestObject aobj1 = new testpkg.ATestObject1(anamer, 
							     "name1",
							     true);

	System.out.println(anamer.getObject("name1").getName());

	System.exit(0);
    }
}
