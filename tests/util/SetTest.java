import org.bzdev.util.*;
import java.util.*;

public class SetTest {
    public static void main(String argv[]) {
	try {
	    Set<String> s1 = new HashSet<String>();
	    Set<String> s2 = new HashSet<String>();
	    Set<String> s3 = new HashSet<String>();
	    s1.add("a1"); s1.add("a2"); s1.add("a3");
	    s2.add("b1"); s2.add("b2"); s2.add("b3");
	    s3.add("c1"); s3.add("c2"); s3.add("c3");
	    
	    DisjointSetsUnion<String> u1 = new DisjointSetsUnion<String>(s1,s2);
	    u1.addSet(s3);
	    for (String s: u1) {
		System.out.println(s);
	    }
	    System.out.println("---------");

	    SortedSet<String> ss1 = new TreeSet<String>();
	    SortedSet<String> ss2 = new TreeSet<String>();
	    SortedSet<String> ss3 = new TreeSet<String>();
	    ss1.add("a1"); ss1.add("a2"); ss1.add("a3");
	    ss2.add("b1"); ss2.add("b2"); ss2.add("b3");
	    
	    DisjointSortedSetsUnion<String> u2 =
		new DisjointSortedSetsUnion<String>(ss1,ss2);
	    for (String s: u2) {
		System.out.println(s);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	System.exit(0);
    }
}