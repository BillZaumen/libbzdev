import org.bzdev.util.*;
import java.util.*;

public class CollectionScannerTest {
    public static void main(String argv[]) throws Exception {
	LinkedList<String> slist = new LinkedList<>();
	HashSet<String> set = new HashSet<>();
	TreeSet<String> tset = new TreeSet<>();
	CollectionScanner<String> cs = new CollectionScanner<>();
	slist.add("hello");
	slist.add("goodbye");
	slist.add("Hello");
	set.add("GoodBye");
	tset.add("Done");

	cs.add(slist);
	cs.add(set);
	cs.add(tset);
	
	for(String s: cs) System.out.println(s);
	System.exit(0);
    }
}