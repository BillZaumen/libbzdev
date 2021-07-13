import org.bzdev.util.*;
import java.util.*;

public class JavaIdentsTest {

    public static void main(String argv[]) throws Exception {
	for (String token:  idents) {
	    System.out.println(token + ": "
			       + JavaIdents.isValidIdentifier(token, false)
			       + " (Simple)");
	    System.out.println(token + ": "
			       + JavaIdents.isValidIdentifier(token, true)
			       + " (Fully Qualified)");
	}
	String parmlist = "<Integer,int,double,Double,org.foo.Foo>";
	System.out.println("isValidParmList(\"" + parmlist + "\") = "
			   + JavaIdents.isValidTypeParameterList(parmlist));
	parmlist = "<Integer,int,double Double,org.foo.Foo>";
	System.out.println("isValidParmList(\"" + parmlist + "\") = "
			   + JavaIdents.isValidTypeParameterList(parmlist));

	for (String type: types) {
	    System.out.println("type = " + type + ", isValidType = "
			       + JavaIdents.isValidType(type));
	}
    }

    static String idents[] = {
	"foo", "int", "foo.bar", "?", "int.bar", "a b", "1Foo"
    };

    static String types[] = {
	"Integer",
	"List<Integer>",
	"Integer...",
	"List<Integer>...",
	"java.util.Map<Key,T extends Foo>",
	"java.util.Map<S super Key, T extends Foo>",
	"double[]",
	"double[]...",
	"double[] ...",
	"Double Integer",
	"Foo<? extends Integer>",
	"Bar < ? super Integer>",
	"? extends bar",
	"Foo<x extend bar>",
	"Foo<x extends ?>",
	"Foo<x extends bar>"
    };
}
