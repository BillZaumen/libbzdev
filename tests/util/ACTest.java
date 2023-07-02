import org.bzdev.util.*;
import java.util.*;
import org.bzdev.math.rv.*;

public class ACTest {

    static void test(String[] patterns, String text) throws Exception {
	ACMatcher matcher = new ACMatcher(patterns);
	int count[] = new int[patterns.length];
	int last[] = new int[patterns.length];
	Arrays.fill(last, -1);
	for (ACMatcher.MatchResult mr: matcher.iterableOver(text)) {
	    int index = mr.getIndex();
	    int start  = mr.getStart();
	    int end = mr.getEnd();
	    String match = text.substring(start, end);
	    if (!match.equals(patterns[index])) {
		throw new Exception();
	    }
	    count[index]++;
	    if (start != last[index]) {
		last[index] = start;
	    } else {
		throw new Exception();
	    }
	}
	SuffixArray.String sa = new SuffixArray.String(text,127);
	for (int i = 0; i < patterns.length; i++) {
	    String pattern = patterns[i];
	    SuffixArray.Range range = sa.findRange(pattern);
	    if (range.size() != count[i]) {
		System.out.println("range.size() == " + range.size()
				   + ", count[ " + i + "] = " + count[i]);
		throw new Exception();
	    }
	}
    }

    
    public static void main(String[] args) throws Exception {

	String patterns[];

	patterns = new String[] {"foo"};
	test(patterns, "");
	test(patterns, "fo");
	test(patterns, "foo");
	test(patterns, "foob");
	test(patterns, "fobo");
	test(patterns, "xfoo");
	test(patterns, "xfo");
	test(patterns, "x");
	test(patterns, "xxx");
	test(patterns, "xxxx");

	patterns = new String[] {"foo", "fo"};
	test(patterns, "");
	test(patterns, "fo");
	test(patterns, "foo");
	test(patterns, "foob");
	test(patterns, "fobo");
	test(patterns, "xfoo");
	test(patterns, "xfo");
	test(patterns, "x");
	test(patterns, "xxx");
	test(patterns, "xxxx");

	patterns = new String[] {"foo", "o"};
	test(patterns, "");
	test(patterns, "fo");
	test(patterns, "foo");
	test(patterns, "foob");
	test(patterns, "fobo");
	test(patterns, "xfoo");
	test(patterns, "xfo");
	test(patterns, "x");
	test(patterns, "xxx");
	test(patterns, "xxxx");

	patterns = new String[] {"foo", "f"};
	test(patterns, "");
	test(patterns, "fo");
	test(patterns, "foo");
	test(patterns, "ffoob");
	test(patterns, "fobo");
	test(patterns, "xfoo");
	test(patterns, "xfo");
	test(patterns, "x");
	test(patterns, "xxx");
	test(patterns, "xxxx");

	patterns = new String[] {"foo", "oo"};
	test(patterns, "");
	test(patterns, "fo");
	test(patterns, "foo");
	test(patterns, "foob");
	test(patterns, "fobo");
	test(patterns, "xfoo");
	test(patterns, "xfo");
	test(patterns, "x");
	test(patterns, "xxx");
	test(patterns, "xxxx");
	

	patterns = new String[] {
	    "foo",
	    "oo",
	    "oba",
	    "bar",
	    "ar",
	    "fo",
	};
	String text = "xyfoobarxfooxbaruubar";

	test(patterns, text);
	
	text = "foofofoo";
	test(patterns, text);

	test(patterns, "xxxxxxxxxxxx");

	IntegerRandomVariable txtrv = new UniformIntegerRV((int)'a', (int)'e');
	IntegerRandomVariable plenrv = new UniformIntegerRV(1, 64);
	StringBuilder sb = new StringBuilder();
	for (int i = 0; i < 10000; i++) {
	    sb.append((char)(int)txtrv.next());
	}
	text = sb.toString();
	IntegerRandomVariable indexrv = new UniformIntegerRV(0,10000);
	IntegerRandomVariable lenrv = new UniformIntegerRV(1, 32);
	
	String[] oldpatterns = patterns;
    
	for (int j = 0; j < 10000; j++) {
	    int plen = plenrv.next();
	    patterns = new String[plen];
	    for (int i = 0; i < plen; i++) {
		int ind1 = indexrv.next();
		int ind2 = ind1 + lenrv.next();
		if (ind2 >= text.length()) {
		    ind2 = text.length();
		}
		patterns[i] = text.substring(ind1, ind2);
	    }
	    test(patterns, text);
	}

	System.gc();
	System.out.println("long case");

	char carray[] = new char[10000000];
	txtrv = new UniformIntegerRV(0, 4);
	for (int i = 0; i < carray.length; i++) {
	    carray[i] = (char)((int)txtrv.next());
	}
	text = new String(carray);
	patterns = new String[64];
	for (int i = 0; i < 64; i++) {
	    int ind1 = indexrv.next();
	    int ind2 = ind1 + lenrv.next();
	    if (ind2 >= text.length()) {
		ind2 = text.length();
	    }
	    patterns[i] = text.substring(ind1, ind2);
	}
	System.out.println("... starting test");
	test(patterns, text);

	long t0 = System.nanoTime();
	ACMatcher matcher = new ACMatcher(patterns);
	matcher.stream(text).forEach((mr) -> {
	    });

	long t1 = System.nanoTime();
	SuffixArray.String sa = new SuffixArray.String(text, 4);
	int count = 0;
	for (int i = 0; i < patterns.length; i++) {
	    count += sa.findRange(patterns[i]).size();
	}
	long t2 = System.nanoTime();
	System.out.println ("Aho Corasick: " + (t1 - t0) + " ns");
	System.out.println ("Suffix Array: " + (t2 - t1) + " ns");
    }
}

