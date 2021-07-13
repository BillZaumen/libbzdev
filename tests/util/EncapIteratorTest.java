import org.bzdev.util.*;
import java.util.*;

public class EncapIteratorTest {
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
	
	EncapsulatingIterator<String,String> eit =
	    new EncapsulatingIterator<String,String>(cs.iterator()) {
	    public String next() {
		String es = encapsulatedNext();
		return es.toLowerCase(Locale.ENGLISH);
	    }
	};

	while(eit.hasNext()) System.out.println(eit.next());
	System.exit(0);
    }
}
