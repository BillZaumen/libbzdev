import org.bzdev.util.*;
import java.util.*;

public class IETest {
    public static void main(String argv[]) throws Exception{
	Vector<String> vector = new Vector<>();
	vector.add("hello");
	vector.add("goodbye");
	vector.add("finished");

	Enumeration<String> enumeration = vector.elements();
	Iterator<String> iterator = vector.iterator();

	Enumeration<String> e = new IteratorEnumeration<String>(iterator);
	Iterator<String> it = new EnumerationIterator<String>(enumeration);

	while (e.hasMoreElements()) {
	    String string = e.nextElement();
	    System.out.println(string);
	}
	System.out.println("-----------");
	while (it.hasNext()) {
	    String string = it.next();
	    System.out.println(string);
	}
	System.out.println("-----------");
	for (String string: EnumerationIterator.iterable(vector.elements())) {
	    System.out.println(string);
	}
    }
}