import org.bzdev.util.*;
import java.util.*;
import org.bzdev.lang.UnexpectedExceptionError;
import org.bzdev.math.rv.UniformIntegerRV;
import org.bzdev.math.*;
import java.io.IOException;

public class SuffixArrayTest {

    public static boolean verify(int[] sequence, int[]array, Appendable out)
	throws IOException
    {
	if (array.length != (sequence.length + 1)) {
	    if (out != null)
		out.append("array.length != sequence.length + 1\n");
	    return false;
	}
	if (array[0] != sequence.length) {
	    if (out != null) out.append("array[0] not the sequence length\n");
	    return false;
	}
	for (int i = 1; i < sequence.length; i++) {
	    int index1 = array[i];
	    int index2 = array[i+1];
	    int limit = sequence.length;
	    if (index1 < index2) {
		limit -= index2;
	    } else {
		limit -= index1;
	    }
	    if (limit < 1) return false;
	    for (int k = 0; k < limit; k++) {
		if (sequence[index1+k] > sequence[index2 + k]) {
		    if (out != null) {
			out.append("strings out of order (k = " + k + "): ");
			out.append("index1 = " + index1);
			out.append(", index2 = " + index2);
			out.append("" + sequence[index1+k] + " > "
				   + sequence[index2+k]);
		    }
		    return false;
		} else if (sequence[index1+k] < sequence[index2+k]) {
		    break;
		}
	    }
	}
	return true;
    }

    public static boolean verify(char[] sequence, int[]array, Appendable out)
	throws IOException
    {
	if (array.length != (sequence.length + 1)) {
	    if (out != null) out.append("array.length != sequence.length + 1\n");
	    return false;
	}
	if (array[0] != sequence.length) {
	    if (out != null) out.append("array[0] not the sequence length\n");
	    return false;
	}
	for (int i = 1; i < sequence.length; i++) {
	    int index1 = array[i];
	    int index2 = array[i+1];
	    int limit = sequence.length;
	    if (index1 < index2) {
		limit -= index2;
	    } else {
		limit -= index1;
	    }
	    if (limit < 1) return false;
	    for (int k = 0; k < limit; k++) {
		if (sequence[index1+k] > sequence[index2 + k]) {
		    if (out != null) {
			out.append("strings out of order (k = " + k + "): ");
			out.append("index1 = " + index1);
			out.append(", index2 = " + index2);
			out.append("" + sequence[index1+k] + " > "
				   + sequence[index2+k]);
		    }
		    return false;
		} else if (sequence[index1+k] < sequence[index2+k]) {
		    break;
		}
	    }
	}
	return true;
    }

    public static boolean verify(short[] sequence, int[]array, Appendable out)
	throws IOException
    {
	if (array.length != (sequence.length + 1)) {
	    if (out != null)
		out.append("array.length != sequence.length + 1\n");
	    return false;
	}
	if (array[0] != sequence.length) {
	    if (out != null) out.append("array[0] not the sequence length\n");
	    return false;
	}
	for (int i = 1; i < sequence.length; i++) {
	    int index1 = array[i];
	    int index2 = array[i+1];
	    int limit = sequence.length;
	    if (index1 < index2) {
		limit -= index2;
	    } else {
		limit -= index1;
	    }
	    if (limit < 1) return false;
	    for (int k = 0; k < limit; k++) {
		if (sequence[index1+k] > sequence[index2 + k]) {
		    if (out != null) {
			out.append("strings out of order (k = " + k + "): ");
			out.append("index1 = " + index1);
			out.append(", index2 = " + index2);
			out.append("" + sequence[index1+k] + " > "
				   + sequence[index2+k]);
		    }
		    return false;
		} else if (sequence[index1+k] < sequence[index2+k]) {
		    break;
		}
	    }
	}
	return true;
    }

    public static boolean verify(byte[] sequence, int[]array, Appendable out)
	throws IOException
    {
	if (array.length != (sequence.length + 1)) {
	    if (out != null) out.append("array.length != sequence.length + 1\n");
	    return false;
	}
	if (array[0] != sequence.length) {
	    if (out != null) out.append("array[0] not the sequence length\n");
	    return false;
	}
	for (int i = 1; i < sequence.length; i++) {
	    int index1 = array[i];
	    int index2 = array[i+1];
	    int limit = sequence.length;
	    if (index1 < index2) {
		limit -= index2;
	    } else {
		limit -= index1;
	    }
	    if (limit < 1) return false;
	    for (int k = 0; k < limit; k++) {
		if (sequence[index1+k] > sequence[index2 + k]) {
		    if (out != null) {
			out.append("strings out of order (k = " + k + "): ");
			out.append("index1 = " + index1);
			out.append(", index2 = " + index2);
			out.append("" + sequence[index1+k] + " > "
				   + sequence[index2+k]);
		    }
		    return false;
		} else if (sequence[index1+k] < sequence[index2+k]) {
		    break;
		}
	    }
	}
	return true;
    }

    // Naive implementation - tests show it is a bit faster than SA-IS

    // when the sequence length is less than about 30.
    public static int[] makeSuffixArray(final int[] sequence) {
	IntComparator ic = new IntComparator() {
		public int compare(int index1, int index2) {
		    int limit = sequence.length - index1;
		    int olimit = sequence.length -index2;
		    int xlimit = limit;
		    if (limit > olimit) xlimit = olimit;
		    for (int i = 0; i < xlimit; i++) {
			if (sequence[index1+i] < sequence[index2+i]) return -1;
			if (sequence[index1+i] > sequence[index2+i]) return 1;
		    }
		    if (limit < olimit) return -1;
		    if (limit > olimit) return 1;
		    return 0;
		}
	    };
	int slenp1 = sequence.length + 1;
	int[] suffixArray = new int[slenp1];
	for (int i = 0; i < slenp1; i++) {
	    suffixArray[i] = i;
	}
	PrimArrays.sort(suffixArray, ic);
	return suffixArray;
    }

    private  static class Info  {
	int index;
	int current;
	int next;
    }

    // A time test indicated that the naive algorithm is a better
    // choice for short sequences: when the sequences are long
    // enough for this to be a better choice compared to  the
    // naive algorithm, SA-IS is faster than either.

    public static int[] makeSuffixArray2(final int[] sequence) {
	int len = sequence.length;
	Info[] info = new Info[len];
	int lenm1 = len-1;
	for (int i = 0; i < lenm1; i++) {
	    Info inf = new Info();
	    inf.index = i;
	    inf.next = sequence[i+1];
	    info[i] = inf;
	}
	info[lenm1] = new Info();
	info[lenm1].index = lenm1;
	info[lenm1].next = -1;

	Comparator<Info> ic0 = new Comparator<>() {
		public int compare(Info info1, Info info2) {
		    int index1 = info1.index;
		    int index2 = info2.index;
		    if (sequence[index1] < sequence[index2]) {
			return -1;
		    } else if (sequence[index1] > sequence[index2]) {
			return 1;
		    } else {
			if (info1.next < info2.next) {
			    return -1;
			} else if (info1.next > info2.next) {
			    return 1;
			} else {
			    return 0;
			}
		    }
		}
	    };

	Arrays.sort(info, ic0);

	if (len > 2) {
	    Comparator<Info> ic = new Comparator<>() {
		    public int compare(Info info1, Info info2) {
			if (info1.current < info2.current) {
			    return -1;
			} else if (info1.current > info2.current) {
			    return 1;
			} else {
			    if (info1.next < info2.next) {
				return -1;
			    } else if (info1.next > info2.next) {
				return 1;
			    } else {
				return 0;
			    }
			}
		    }
		};

	    int inverse[] = new int[len];
	    {
		int current = 0;
		int prev = sequence[info[0].index];
		info[0].current = current;
		inverse[info[0].index] = 0;
		for (int i = 1; i < len; i++) {
		    if (sequence[info[i].index] == prev
			&& info[i].next == info[i-1].next) {
			info[i].current = current;
		    } else {
			prev = sequence[info[i].index];
			current++;
			info[i].current = current;
		    }
		    inverse[info[i].index]  = i;
		}
		for (int i = 0; i < len; i++) {
		    int nextIndex = info[i].index + 2;
		    info[i].next = (nextIndex < len) ?
			info[inverse[nextIndex]].current:
			-1;
		}
		Arrays.sort(info, ic);
	    }
	    for (int ind = 4; ind < len; ind <<= 1) {
		int current = 0;
		int prev = info[0].current;
		info[0].current = current;
		inverse[info[0].index] = 0;
		for (int i = 1; i < len; i++) {
		    if (info[i].current == prev
			&& info[i].next == info[i-1].next) {
			info[i].current = current;
		    } else {
			prev = info[i].current;
			current++;
			info[i].current = current;
		    }
		    inverse[info[i].index]  = i;
		}
		for (int i = 0; i < len; i++) {
		    int nextIndex = info[i].index + ind;
		    info[i].next = (nextIndex < len) ?
			info[inverse[nextIndex]].current:
			-1;
		}
		Arrays.sort(info, ic);
	    }
	}

	int slenp1 = len + 1;
	int[] suffixArray = new int[slenp1];
	suffixArray[0] = len;
	for (int i = 1; i < slenp1; i++) {
	    suffixArray[i] = info[i-1].index;
	}
	return suffixArray;
    }

    // test against another implementation found on an internet site
    // ... just so we can do an independent check.
    public static class Suffix implements Comparable<Suffix>
    {
        int index;
        int rank;
        int next;

        public Suffix(int ind, int r, int nr)
        {
            index = ind;
            rank = r;
            next = nr;
        }

        public int compareTo(Suffix s)
        {
            if (rank != s.rank) return Integer.compare(rank, s.rank);
            return Integer.compare(next, s.next);
        }
    }
    public static int[] makeSuffixArray3(int[] s)
    {
        int n = s.length;
        Suffix[] su = new Suffix[n];

        // Store suffixes and their indexes in
        // an array of classes. The class is needed
        // to sort the suffixes alphabetically and
        // maintain their old indexes while sorting
        for (int i = 0; i < n; i++)
        {
            su[i] = new Suffix(i, s[i], 0);
        }
        for (int i = 0; i < n; i++)
            su[i].next = (i + 1 < n ? su[i + 1].rank : -1);

        // Sort the suffixes using the comparison function
        // defined above.
        Arrays.sort(su);

        // At this point, all suffixes are sorted
        // according to first 2 characters.
        // Let us sort suffixes according to first 4
        // characters, then first 8 and so on
        int[] ind = new int[n];


        // This array is needed to get the index in suffixes[]
        // from original index. This mapping is needed to get
        // next suffix.
        for (int length = 4; length < 2 * n; length <<= 1)
        {
            // Assigning rank and index values to first suffix
            int rank = 0, prev = su[0].rank;
            su[0].rank = rank;
            ind[su[0].index] = 0;
            for (int i = 1; i < n; i++)
            {
                // If first rank and next ranks are same as
                // that of previous suffix in array,
                // assign the same new rank to this suffix
                if (su[i].rank == prev &&
                    su[i].next == su[i - 1].next)
                {
                    prev = su[i].rank;
                    su[i].rank = rank;
                }
                else
                {
                    // Otherwise increment rank and assign
                    prev = su[i].rank;
                    su[i].rank = ++rank;
                }
                ind[su[i].index] = i;
            }

            // Assign next rank to every suffix
            for (int i = 0; i < n; i++)
            {
                int nextP = su[i].index + length / 2;
                su[i].next = nextP < n ?
                   su[ind[nextP]].rank : -1;
            }

            // Sort the suffixes according
            // to first k characters
            Arrays.sort(su);
        }

        // Store indexes of all sorted
        // suffixes in the suffix array
        int[] suf = new int[n+1];

	suf[0] = n;
        for (int i = 0; i < n; i++)
            suf[i+1] = su[i].index;

        // Return the suffix array
        return suf;
    }

    public static void main(String argv[]) throws Exception {

	int[] sequence = {'c', 'a', 'b', 'b', 'a', 'g', 'e'};
	int[] array = makeSuffixArray(sequence);
	int[] array2 = makeSuffixArray2(sequence);
	int[] array3 = makeSuffixArray3(sequence);
	System.out.println("suffixes:");
	for (int i = 0; i < array.length; i++) {
	    System.out.print("    ");
	    for (int j = array[i]; j < sequence.length; j++) {
		System.out.print((char)sequence[j]);
	    }
	    System.out.println();
	}
	int[] expecting = {7, 1, 4, 3, 2, 0, 6, 5};
	if (array.length != expecting.length) {
	    System.out.println("length wrong");
	    throw new Exception("length wrong");
	}
	for (int i = 0; i < expecting.length; i++) {
	    if (expecting[i] != array[i]) {
		System.out.format("array: value %d at index %d, "
				  + "expecting %d\n",
				  array[i], i, expecting[i]);
	    }
	    if (expecting[i] != array2[i]) {
		System.out.format("array2: value %d at index %d, "
				  + "expecting %d\n",
				  array2[i], i, expecting[i]);
		throw new Exception("wrong value");
	    }
	    if (expecting[i] != array3[i]) {
		System.out.format("array3: value %d at index %d, "
				  + "expecting %d\n",
				  array3[i], i, expecting[i]);
		throw new Exception("wrong value");
	    }
	}

	SuffixArray.Integer suffixArray = new SuffixArray.Integer(sequence, 256);
	array = suffixArray.getArray();

	SuffixArray.Iterator it = suffixArray.subsequences();
	System.out.println("subsequences:");
	int count = 0;
	while (it.hasNext()) {
	    int index = it.next();
	    int len = it.getLength();
	    System.out.print("    ");
	    for (int i = 0; i < len; i++) {
		char ch = (char)sequence[index+i];
		System.out.print(ch);
	    }
	    System.out.println();
	    count++;
	}

	if (count != suffixArray.countUniqueSubsequences()) {
	    System.out.format("%d prefixes, method indicated %d\n",
			      count, suffixArray.countUniqueSubsequences());
	    throw new Exception("unique-count error");
	}


	if (array.length != expecting.length) {
	    System.out.println("length wrong");
	    throw new Exception("length wrong");
	}
	for (int i = 0; i < expecting.length; i++) {
	    if (expecting[i] != array[i]) {
		System.out.format("length value %d at index %d, "
				  + "expecting %d\n",
				  array[i], i, expecting[i]);
		throw new Exception("wrong value");
	    }
	}

	System.out.println("testing random cases");

	UniformIntegerRV rv = new UniformIntegerRV(0, 24);

	HashSet<Character> alphabet = new LinkedHashSet<Character>(24);
	for (int i = 0; i < 24; i++) {
	    char ch = (char) i;
	    alphabet.add(ch);
	}

	for (int n = 1; n < 100; n++) {
	    sequence = new int[n];
	    for (int i = 0; i < n; i++) {
		sequence[i] = rv.next();
	    }
	    expecting = makeSuffixArray(sequence);
	    array2 = makeSuffixArray2(sequence);
	    array3 = makeSuffixArray3(sequence);
	    suffixArray = new SuffixArray.Integer(sequence, 256);
	    array = suffixArray.getArray();
	    if (array.length != expecting.length) {
		System.out.println("length wrong");
		throw new Exception("length wrong");
	    }
	    for (int i = 0; i < expecting.length; i++) {
		if (expecting[i] != array[i]) {
		    System.out.format("array: value %d at index %d, "
				      + "expecting %d\n",
				      array[i], i, expecting[i]);
		    throw new Exception("wrong value");
		}
		if (expecting[i] != array3[i]) {
		    System.out.format("array3: value %d at index %d, "
				      + "expecting %d\n",
				      array3[i], i, expecting[i]);
		    throw new Exception("wrong value");
		}
		if (expecting[i] != array2[i]) {
		    System.out.format("array2: value %d at index %d, "
				      + "expecting %d\n",
				      array2[i], i, expecting[i]);
		    throw new Exception("wrong value");
		}
	    }
	}

	for (int n = 1; n < 20000; n += 100) {
	    sequence = new int[n];
	    for (int i = 0; i < n; i++) {
		sequence[i] = rv.next();
	    }
	    // System.out.println("testing n = " + n);
	    suffixArray = new SuffixArray.Integer(sequence, 256);
	    array = suffixArray.getArray();
	    expecting = makeSuffixArray(sequence);
	    if (!verify(sequence, array, System.out)) {
		throw new Exception("wrong value or length");
	    }
	}

	for (int n = 1; n < 5000; n ++) {
	    char[] xsequence = new char[n];
	    for (int i = 0; i < n; i++) {
		xsequence[i] = (char)(int)(rv.next());
	    }
	    // System.out.println("testing n = " + n);
	    SuffixArray.Char xsa = new SuffixArray.Char(xsequence, 256);
	    array = xsa.getArray();
	    if (!verify(xsequence, array, System.out)) {
		throw new Exception("wrong value or length");
	    }
	}

	for (int n = 1; n < 5000; n ++) {
	    short[] xsequence = new short[n];
	    for (int i = 0; i < n; i++) {
		xsequence[i] = (short)(int)(rv.next());
	    }
	    // System.out.println("testing n = " + n);
	    SuffixArray.Short xsa = new SuffixArray.Short(xsequence, 256);
	    array = xsa.getArray();
	    if (!verify(xsequence, array, System.out)) {
		throw new Exception("wrong value or length");
	    }
	}

	for (int n = 1; n < 5000; n ++) {
	    byte[] xsequence = new byte[n];
	    for (int i = 0; i < n; i++) {
		xsequence[i] = (byte)(int)(rv.next());
	    }
	    // System.out.println("testing n = " + n);
	    SuffixArray.Byte xsa = new SuffixArray.Byte(xsequence, 128);
	    array = xsa.getArray();
	    if (!verify(xsequence, array, System.out)) {
		throw new Exception("wrong value or length");
	    }
	}

	for (int n = 1; n < 5000; n ++) {
	    char[] xsequence = new char[n];
	    for (int i = 0; i < n; i++) {
		xsequence[i] = (char)(int)(rv.next());
	    }
	    String string = new String(xsequence);
	    // System.out.println("testing n = " + n);
	    SuffixArray.String xsa = new SuffixArray.String(string, 256);
	    array = xsa.getArray();
	    if (!verify(xsequence, array, System.out)) {
		throw new Exception("wrong value or length");
	    }
	}

	for (int n = 1; n < 5000; n ++) {
	    char[] xsequence = new char[n];
	    for (int i = 0; i < n; i++) {
		xsequence[i] = (char)(int)(rv.next());
	    }
	    String string = new String(xsequence);
	    // System.out.println("testing n = " + n);
	    SuffixArray.String xsa = new SuffixArray.String(string, alphabet);
	    array = xsa.getArray();
	    if (!verify(xsequence, array, System.out)) {
		throw new Exception("wrong value or length");
	    }
	}

	HashSet<Object> oalphabet = new LinkedHashSet<Object>();
	for (int i = 0; i < 24; i++) {
	    oalphabet.add(Integer.valueOf(i));
	}

	Object o1 = Integer.valueOf(10);
	Object o2 = Integer.valueOf(10);
	Object o3 = Integer.valueOf(20);
	if (! o1.equals(o2)) {
	    throw new Exception ("equals failure");
	}
	if (((Integer)o3).intValue() < ((Integer)o2).intValue()) {
	    throw new Exception ("ordering failure");
	}
	if (! oalphabet.contains(o1)) {
	    throw new Exception ("alphabet failure");
	}

	for (int n = 1; n < 5000; n += 100) {
	    sequence = new int[n];
	    Object[] osequence = new Object[n];
	    for (int i = 0; i < n; i++) {
		sequence[i] = rv.next();
		osequence[i] = Integer.valueOf(sequence[i]);
	    }
	    // System.out.println("testing n = " + n);
	    SuffixArray.Array<Object> osa =
		new SuffixArray.Array<Object>(osequence, oalphabet);
	    array = osa.getArray();
	    if (!verify(sequence, array, System.out)) {
		throw new Exception("wrong value or length");
	    }
	}

	int nn = 10000000;
	System.out.println("initializing long sequence (n = " + nn  + ")");
	sequence = new int[nn];
	for (int i = 0; i < nn; i++) {
	    sequence[i] = rv.next();
	}
	System.out.println("Trying SA-IS implementation:");
	suffixArray = new SuffixArray.Integer(sequence, 256);
	System.out.println("Verifying");
	System.out.println(verify(sequence, suffixArray.getArray(),
				  System.out));
	System.out.println("Done");

	System.out.println("LCP test");
	String string = "banana";
	sequence = new int[string.length()];
	int k = 0;
	for (char ch: string.toCharArray()) {
	    sequence[k++] = (int)ch;
	}
	suffixArray = new SuffixArray.Integer(sequence, 256);
	for (int entry: suffixArray.getArray()) {
	    System.out.print(entry + " ");
	}
	System.out.println();
	for (int entry: suffixArray.getLCP()) {
	    System.out.print(entry + " ");
	}
	System.out.println();

	System.out.println("Test lcp(x,y)");
	string = "abcdeabcdabcab";
	sequence = new int[string.length()];
	k = 0;
	for (char ch: string.toCharArray()) {
	    sequence[k++] = (int)ch;
	}
	suffixArray = new SuffixArray.Integer(sequence, 256);
	for (int entry: suffixArray.getArray()) {
	    System.out.print(entry + " ");
	}
	System.out.println();
	for (int entry: suffixArray.getLCP()) {
	    System.out.print(entry + " ");
	}
	System.out.println();

	System.out.println("lcpLength(0,5) = " + suffixArray.lcpLength(0, 5));
	System.out.println("lcpLength(0,12) = " + suffixArray.lcpLength(0, 12));
	System.out.println("lcpLength(0,13) = " + suffixArray.lcpLength(0, 13));

	// Example from Burrows and Wheeler's paper
	char csequence[] = { 'a', 'b', 'r', 'a', 'c', 'a'};
	SuffixArray.Char csuffixArray = new SuffixArray.Char(csequence, 256);
	char[] cbwt = new char[csequence.length];
	System.out.println(csuffixArray.getBWT(cbwt));
	string = new String(cbwt);
	if (!string.equals("caraab")) {
	    System.out.println(new String(cbwt));
	    throw new Exception("BTW failed for character array");
	}

	char csequence1[] = { 'a', 'b', 'a', 'a', 'b', 'a'};
	char cresult[] = {'a', 'b', 'b', 'a', (char)0xffff, 'a', 'a'};
	csuffixArray = new SuffixArray.Char(csequence1, 256);
	cbwt = new char[csequence1.length + 1];
	System.out.println(csuffixArray.getBWT(cbwt));
	for (int i = 0; i < cbwt.length; i++) {
	    char ch = cbwt[i];
	    if (ch == (char)0xffff) System.out.print("$");
	    else System.out.print(ch);
	}
	System.out.println();
	for (int i = 0; i < cbwt.length; i++) {
	    if (cbwt[i] != cresult[i]) {
		throw new Exception("BWT failed");
	    }
	}

	string = "mississippi";
	int[] isequence = new int[string.length()];
	for (int i = 0; i < string.length(); i++) {
	    isequence[i] = (int) string.charAt(i);
	}
	SuffixArray.Integer isuffixArray =
	    new SuffixArray.Integer(isequence, 256);
	int[] ibwt = new int[isequence.length];
	int ind = isuffixArray.getBWT(ibwt);
	System.out.println("ind = " + ind);
	System.out.print("ORIG: ");
	for (int i = 0; i < isequence.length; i++) {
	    System.out.print((char)isequence[i]);
	}
	System.out.println();
	System.out.print("BWT:  ");
	for (int i = 0; i < isequence.length; i++) {
	    System.out.print((char)ibwt[i]);
	}
	System.out.println();
	int[] iisequence = new int[isequence.length];
	SuffixArray.Integer.inverseBWT(ibwt, iisequence, ind, 256);
	for (int i = 0; i < isequence.length; i++) {
	    System.out.print((char)iisequence[i]);
	    if (isequence[i] != iisequence[i]) {
		System.out.println();
		throw new Exception("bad inverse");
	    }
	}
	System.out.println();
	System.out.println("try with end-of-text symbols");
	ibwt = new int[string.length()+1];
	ind = isuffixArray.getBWT(ibwt);
	System.out.print("BWT:  ");
	for (int i = 0; i < isequence.length; i++) {
	    if (ibwt[i] == -1) System.out.print('$');
	    else System.out.print((char)ibwt[i]);
	}
	System.out.println();
	iisequence = new int[isequence.length];
	SuffixArray.Integer.inverseBWT(ibwt, iisequence, -1, 256);
	for (int i = 0; i < isequence.length; i++) {
	    System.out.print((char)iisequence[i]);
	    if (isequence[i] != iisequence[i]) {
		System.out.println();
		throw new Exception("bad inverse");
	    }
	}
	System.out.println();

	System.out.println("... short[] case");
	short[] ssequence = new short[string.length()];
	for (int i = 0; i < string.length(); i++) {
	    ssequence[i] = (short) string.charAt(i);
	}
	SuffixArray.Short ssuffixArray =
	    new SuffixArray.Short(ssequence, 256);
	short[] sbwt = new short[ssequence.length];
	ind = ssuffixArray.getBWT(sbwt);
	System.out.println("ind = " + ind);
	System.out.print("ORIG: ");
	for (int i = 0; i < ssequence.length; i++) {
	    System.out.print((char)ssequence[i]);
	}
	System.out.println();
	System.out.print("BWT:  ");
	for (int i = 0; i < ssequence.length; i++) {
	    System.out.print((char)sbwt[i]);
	}
	System.out.println();
	short[] s2sequence = new short[ssequence.length];
	SuffixArray.Short.inverseBWT(sbwt, s2sequence, ind, 256);
	for (int i = 0; i < isequence.length; i++) {
	    System.out.print((char)s2sequence[i]);
	    if (ssequence[i] != s2sequence[i]) {
		System.out.println();
		throw new Exception("bad inverse");
	    }
	}
	System.out.println();
	System.out.println("try with end-of-text symbols");
	sbwt = new short[string.length()+1];
	ind = ssuffixArray.getBWT(sbwt);
	System.out.print("BWT:  ");
	for (int i = 0; i < ssequence.length; i++) {
	    if (sbwt[i] == -1) System.out.print('$');
	    else System.out.print((char)sbwt[i]);
	}
	System.out.println();
	s2sequence = new short[ssequence.length];
	SuffixArray.Short.inverseBWT(sbwt, s2sequence, -1, 256);
	for (int i = 0; i < ssequence.length; i++) {
	    System.out.print((char)s2sequence[i]);
	    if (ssequence[i] != s2sequence[i]) {
		System.out.println();
		throw new Exception("bad inverse");
	    }
	}
	System.out.println();

	System.out.println("... byte[] case");
	byte[] bsequence = new byte[string.length()];
	for (int i = 0; i < string.length(); i++) {
	    bsequence[i] = (byte) string.charAt(i);
	}
	SuffixArray.Byte bsuffixArray =
	    new SuffixArray.Byte(bsequence, 128);
	byte[] bbwt = new byte[bsequence.length];
	ind = bsuffixArray.getBWT(bbwt);
	System.out.println("ind = " + ind);
	System.out.print("ORIG: ");
	for (int i = 0; i < bsequence.length; i++) {
	    System.out.print((char)bsequence[i]);
	}
	System.out.println();
	System.out.print("BWT:  ");
	for (int i = 0; i < bsequence.length; i++) {
	    System.out.print((char)bbwt[i]);
	}
	System.out.println();
	byte[] bbsequence = new byte[bsequence.length];
	SuffixArray.Byte.inverseBWT(bbwt, bbsequence, ind, 256);
	for (int i = 0; i < isequence.length; i++) {
	    System.out.print((char)bbsequence[i]);
	    if (bsequence[i] != bbsequence[i]) {
		System.out.println();
		throw new Exception("bad inverse");
	    }
	}
	System.out.println();
	System.out.println("try with end-of-text symbols");
	bbwt = new byte[string.length()+1];
	ind = bsuffixArray.getBWT(bbwt);
	System.out.print("BWT:  ");
	for (int i = 0; i < bsequence.length; i++) {
	    if (bbwt[i] == -1) System.out.print('$');
	    else System.out.print((char)bbwt[i]);
	}
	System.out.println();
	bbsequence = new byte[bsequence.length];
	SuffixArray.Byte.inverseBWT(bbwt, bbsequence, -1, 256);
	for (int i = 0; i < bsequence.length; i++) {
	    System.out.print((char)bbsequence[i]);
	    if (bsequence[i] != bbsequence[i]) {
		System.out.println();
		throw new Exception("bad inverse");
	    }
	}
	System.out.println();

	System.out.println("... char[] case");
	csequence = new char[string.length()];
	for (int i = 0; i < string.length(); i++) {
	    csequence[i] = (char) string.charAt(i);
	}
	csuffixArray =
	    new SuffixArray.Char(csequence, 256);
        cbwt = new char[csequence.length];
	ind = csuffixArray.getBWT(cbwt);
	System.out.println("ind = " + ind);
	System.out.print("ORIG: ");
	for (int i = 0; i < csequence.length; i++) {
	    System.out.print((char)csequence[i]);
	}
	System.out.println();
	System.out.print("BWT:  ");
	for (int i = 0; i < csequence.length; i++) {
	    System.out.print((char)cbwt[i]);
	}
	System.out.println();
	char[] ccsequence = new char[csequence.length];
	SuffixArray.Char.inverseBWT(cbwt, ccsequence, ind, 256);
	for (int i = 0; i < isequence.length; i++) {
	    System.out.print((char)ccsequence[i]);
	    if (csequence[i] != ccsequence[i]) {
		System.out.println();
		throw new Exception("bad inverse");
	    }
	}
	System.out.println();
	System.out.println("try with end-of-text symbols");
	cbwt = new char[string.length()+1];
	ind = csuffixArray.getBWT(cbwt);
	System.out.print("BWT:  ");
	for (int i = 0; i < csequence.length; i++) {
	    if (cbwt[i] == (char)0xffff) System.out.print('$');
	    else System.out.print((char)cbwt[i]);
	}
	System.out.println();
	ccsequence = new char[csequence.length];
	SuffixArray.Char.inverseBWT(cbwt, ccsequence, -1, 256);
	for (int i = 0; i < csequence.length; i++) {
	    System.out.print((char)ccsequence[i]);
	    if (csequence[i] != ccsequence[i]) {
		System.out.println();
		throw new Exception("bad inverse");
	    }
	}
	System.out.println();

	System.out.println("... string case");
	csequence = new char[string.length()];
	for (int i = 0; i < string.length(); i++) {
	    csequence[i] = (char) string.charAt(i);
	}
	SuffixArray.String stringSuffixArray =
	    new SuffixArray.String(string, 256);
        cbwt = new char[csequence.length];
	ind = stringSuffixArray.getBWT(cbwt);
	System.out.println("ind = " + ind);
	System.out.print("ORIG: ");
	for (int i = 0; i < csequence.length; i++) {
	    System.out.print((char)csequence[i]);
	}
	System.out.println();
	System.out.print("BWT:  ");
	for (int i = 0; i < csequence.length; i++) {
	    System.out.print((char)cbwt[i]);
	}
	System.out.println();
	ccsequence = new char[csequence.length];
	SuffixArray.Char.inverseBWT(cbwt, ccsequence, ind, 256);
	for (int i = 0; i < isequence.length; i++) {
	    System.out.print((char)ccsequence[i]);
	    if (csequence[i] != ccsequence[i]) {
		System.out.println();
		throw new Exception("bad inverse");
	    }
	}
	System.out.println();
	System.out.println("try with end-of-text symbols");
	cbwt = new char[string.length()+1];
	ind = stringSuffixArray.getBWT(cbwt);
	System.out.print("BWT:  ");
	for (int i = 0; i < csequence.length; i++) {
	    if (cbwt[i] == (char)0xffff) System.out.print('$');
	    else System.out.print((char)cbwt[i]);
	}
	System.out.println();
	ccsequence = new char[csequence.length];
	SuffixArray.Char.inverseBWT(cbwt, ccsequence, -1, 256);
	for (int i = 0; i < csequence.length; i++) {
	    System.out.print((char)ccsequence[i]);
	    if (csequence[i] != ccsequence[i]) {
		System.out.println();
		throw new Exception("bad inverse");
	    }
	}
	stringSuffixArray.useLCPLR();

	System.out.println();

	System.out.println("... object test");

	Set<Object>objalphabet = new LinkedHashSet<Object>();
	for (int i = 0; i < 127; i++) {
	    objalphabet.add(Integer.valueOf(i));
	}
	Object[] oisequence = new Object[string.length()];
	for (int i = 0; i < string.length(); i++) {
	    oisequence[i] = Integer.valueOf(string.charAt(i));
	}
	SuffixArray.Array<Object> oisuffixArray =
	    new SuffixArray.Array<Object>(oisequence, objalphabet);
	int[] oibwt = new int[oisequence.length];
	ind = oisuffixArray.getBWT(oibwt);
	System.out.println("ind = " + ind);
	System.out.print("ORIG: ");
	for (int i = 0; i < oisequence.length; i++) {
	    System.out.print((char)(int)(Integer)oisequence[i]);
	}
	System.out.println();
	System.out.print("BWT:  ");
	for (int i = 0; i < isequence.length; i++) {
	    System.out.print((char)oibwt[i]);
	}
	System.out.println();
	Object[] oiisequence = new Object[oisequence.length];
	SuffixArray.Array.inverseBWT(oibwt, oiisequence, ind, objalphabet);
	for (int i = 0; i < isequence.length; i++) {
	    System.out.print((char)(int)(Integer)oiisequence[i]);
	    if (!oisequence[i].equals(oiisequence[i])) {
		System.out.println();
		throw new Exception("bad inverse" + oisequence[i]
				    + " != " + oiisequence[i]);
	    }
	}
	System.out.println();
	System.out.println("try with end-of-text symbols");
	oibwt = new int[string.length()+1];
	ind = oisuffixArray.getBWT(oibwt);
	System.out.print("BWT:  ");
	for (int i = 0; i < isequence.length; i++) {
	    if (oibwt[i] == -1) System.out.print('$');
	    else System.out.print((char)oibwt[i]);
	}
	System.out.println();
	iisequence = new int[isequence.length];
	SuffixArray.Array.inverseBWT(oibwt, oiisequence, -1, objalphabet);
	for (int i = 0; i < isequence.length; i++) {
	    System.out.print((char)(int)(Integer)oiisequence[i]);
	    if (!oisequence[i].equals(oiisequence[i])) {
		System.out.println();
		throw new Exception("bad inverse");
	    }
	}
	System.out.println();

	int ifsequence[] = {1, 2, 8, 4, 7, 5, 2, 2, 8, 10, 2, 8, 9};
	int ifsubsequence[] = {2, 8};

	SuffixArray.Integer ifsa = new SuffixArray.Integer(ifsequence, 11);
	int[] ifA = ifsa.getArray();
	System.out.println("ifA.length = " + ifA.length
			   + ", ifsequence.length = " + ifsequence.length);
	System.out.println("ifA:");
	for (int index: ifA) {
	    if (index == ifsequence.length) {
		System.out.println("    " + index);
	    } else {
		System.out.println("    " + index
				   + " --> " + ifsequence[index]);
	    }
	}
	int if1 = ifsa.findSubsequence(ifsubsequence, false);
	int if2 = ifsa.findSubsequence(ifsubsequence, true);
	System.out.format("ifsa test: first index = %d, last index = %d\n",
			  if1, if2);
	int if1a = ifsa.findSubsequence(ifsubsequence, 0, 1, false);
	int if2a = ifsa.findSubsequence(ifsubsequence, 0, 1, true);
	int if1b = ifsa.findSubsequence(1, ifsubsequence, 1, 2, if1a, if2a+1,
					false);
	int if2b = ifsa.findSubsequence(1, ifsubsequence, 1, 2, if1a, if2a+1,
					true);
	if (if1 != if1b) throw new Exception();
	if (if2 != if2b) throw new Exception();
	int ifsubsequenceL[] = {1};
	int ifsubsequenceU[] = {9};
	int ifsubsequenceM[] = {7, 5, 2};
	int ifsubsequence0[] = {3, 4};
	int ifsubsequence8[] = {8};
	int t1a = ifsa.findSubsequence(ifsubsequenceL, false);
	int t1b = ifsa.findSubsequence(ifsubsequenceL, true);
	int t2a = ifsa.findSubsequence(ifsubsequenceU, false);
	int t2b = ifsa.findSubsequence(ifsubsequenceU, true);
	int t3a = ifsa.findSubsequence(ifsubsequenceM, false);
	int t3b = ifsa.findSubsequence(ifsubsequenceM, true);
	int t4a = ifsa.findSubsequence(ifsubsequence0, false);
	int t4b = ifsa.findSubsequence(ifsubsequence0, true);
	int t5a = ifsa.findSubsequence(ifsubsequence8, false);
	int t5b = ifsa.findSubsequence(ifsubsequence8, true);

	ifsa.useLCP();
	ifsa.useLCPLR();
	int v1 = ifsa.findSubsequence(ifsubsequence, false);
	int v2 = ifsa.findSubsequence(ifsubsequence, true);
	if (if1 != v1 || if2 != v2) {
	    System.out.println("v1 = " + v1 + ", v2 = " + v2);
	    throw new Exception();
	}
	int tt1a = ifsa.findSubsequence(ifsubsequenceL, false);
	int tt1b = ifsa.findSubsequence(ifsubsequenceL, true);
	int tt2a = ifsa.findSubsequence(ifsubsequenceU, false);
	int tt2b = ifsa.findSubsequence(ifsubsequenceU, true);
	int tt3a = ifsa.findSubsequence(ifsubsequenceM, false);
	int tt3b = ifsa.findSubsequence(ifsubsequenceM, true);
	int tt4a = ifsa.findSubsequence(ifsubsequence0, false);
	int tt4b = ifsa.findSubsequence(ifsubsequence0, true);
	int tt5a = ifsa.findSubsequence(ifsubsequence8, false);
	int tt5b = ifsa.findSubsequence(ifsubsequence8, true);
	if (t1a != tt1a) throw new Exception();
	if (t1b != tt1b) throw new Exception();
	if (t2a != tt2a) throw new Exception();
	if (t2b != tt2b) throw new Exception();
	if (t3a != tt3a) throw new Exception();
	if (t3b != tt3b) throw new Exception();
	if (t4a != tt4a) throw new Exception();
	if (t4b != tt4b) throw new Exception();
	if (t5a != tt5a) throw new Exception();
	if (t5b != tt5b) throw new Exception();


	System.out.format("ifsequence: first index = %d, last index = %d\n",
			  ifA[if1], ifA[if2]);
	System.out.format("ifsa: any index = %d\n",
			  ifsa.findSubsequence(ifsubsequence));
	SuffixArray.Range range = ifsa.findRange(ifsubsequence);

	System.out.println("range.size() = " + range.size());
	System.out.println("range.subsequenceLength() = "
			   + range.subsequenceLength());


	for (int ind2: range) {
	    System.out.println("using iterator, subsequence starting at "
			       + ind2);
	}

	for (int ind2: range.toArray()) {
	    System.out.println("using array, subsequence starting at "
			       + ind2);
	}

	for (int ind2: range.toArray(new int[range.size()])) {
	    System.out.println("using our array, subsequence starting at "
			       + ind2);
	}

	for (int ind3 = 0; ind3 < range.size(); ind3++) {
	    System.out.println("using index, subsequence starting at "
			       + range.subsequenceIndex(ind3));
	}

	short sfsequence[] = {1, 2, 8, 4, 7, 5, 2, 2, 8, 10, 2, 8, 9};
	short sfsubsequence[] = {2, 8};

	SuffixArray.Short sfsa = new SuffixArray.Short(sfsequence, 11);
	int[] sfA = sfsa.getArray();
	System.out.println("sfA.length = " + sfA.length
			   + ", sfsequence.length = " + sfsequence.length);
	System.out.println("sfA:");
	for (int index: sfA) {
	    if (index == sfsequence.length) {
		System.out.println("    " + index);
	    } else {
		System.out.println("    " + index
				   + " --> " + sfsequence[index]);
	    }
	}

	int sf1 = sfsa.findSubsequence(sfsubsequence, false);
	int sf2 = sfsa.findSubsequence(sfsubsequence, true);

	short sfsubsequenceL[] = {1};
	short sfsubsequenceU[] = {9};
	short sfsubsequenceM[] = {7, 5, 2};
	short sfsubsequence0[] = {3, 4};
	short sfsubsequence8[] = {8};
	t1a = sfsa.findSubsequence(sfsubsequenceL, false);
	t1b = sfsa.findSubsequence(sfsubsequenceL, true);
	t2a = sfsa.findSubsequence(sfsubsequenceU, false);
	t2b = sfsa.findSubsequence(sfsubsequenceU, true);
	t3a = sfsa.findSubsequence(sfsubsequenceM, false);
	t3b = sfsa.findSubsequence(sfsubsequenceM, true);
	t4a = sfsa.findSubsequence(sfsubsequence0, false);
	t4b = sfsa.findSubsequence(sfsubsequence0, true);
	t5a = sfsa.findSubsequence(sfsubsequence8, false);
	t5b = sfsa.findSubsequence(sfsubsequence8, true);

	sfsa.useLCP();
	sfsa.useLCPLR();
	v1 = sfsa.findSubsequence(sfsubsequence, false);
	v2 = sfsa.findSubsequence(sfsubsequence, true);
	if (sf1 != v1 || sf2 != v2) {
	    System.out.println("v1 = " + v1 + ", v2 = " + v2);
	    throw new Exception();
	}
	tt1a = sfsa.findSubsequence(sfsubsequenceL, false);
	tt1b = sfsa.findSubsequence(sfsubsequenceL, true);
	tt2a = sfsa.findSubsequence(sfsubsequenceU, false);
	tt2b = sfsa.findSubsequence(sfsubsequenceU, true);
	tt3a = sfsa.findSubsequence(sfsubsequenceM, false);
	tt3b = sfsa.findSubsequence(sfsubsequenceM, true);
	tt4a = sfsa.findSubsequence(sfsubsequence0, false);
	tt4b = sfsa.findSubsequence(sfsubsequence0, true);
	tt5a = sfsa.findSubsequence(sfsubsequence8, false);
	tt5b = sfsa.findSubsequence(sfsubsequence8, true);
	if (t1a != tt1a) throw new Exception();
	if (t1b != tt1b) throw new Exception();
	if (t2a != tt2a) throw new Exception();
	if (t2b != tt2b) throw new Exception();
	if (t3a != tt3a) throw new Exception();
	if (t3b != tt3b) throw new Exception();
	if (t4a != tt4a) throw new Exception();
	if (t4b != tt4b) throw new Exception();
	if (t5a != tt5a) throw new Exception();
	if (t5b != tt5b) throw new Exception();

	System.out.format("sfsa test: first index = %d, last index = %d\n",
			  sf1, sf2);
	System.out.format("sfsequence: first index = %d, last index = %d\n",
			  sfA[sf1], sfA[sf2]);
	System.out.format("sfsa: any index = %d\n",
			  sfsa.findSubsequence(sfsubsequence));
	int sf1a = sfsa.findSubsequence(sfsubsequence, 0, 1, false);
	int sf2a = sfsa.findSubsequence(sfsubsequence, 0, 1, true);
	int sf1b = sfsa.findSubsequence(1, sfsubsequence, 1, 2, sf1a, sf2a+1,
					false);
	int sf2b = sfsa.findSubsequence(1, sfsubsequence, 1, 2, sf1a, sf2a+1,
					true);
	if (sf1 != sf1b) throw new Exception();
	if (sf2 != sf2b) throw new Exception();


	range = sfsa.findRange(sfsubsequence);

	System.out.println("range.size() = " + range.size());
	System.out.println("range.subsequenceLength() = "
			   + range.subsequenceLength());


	for (int ind2: range) {
	    System.out.println("using iterator, subsequence starting at "
			       + ind2);
	}

	for (int ind2: range.toArray()) {
	    System.out.println("using array, subsequence starting at "
			       + ind2);
	}

	for (int ind2: range.toArray(new int[range.size()])) {
	    System.out.println("using our array, subsequence starting at "
			       + ind2);
	}

	for (int ind3 = 0; ind3 < range.size(); ind3++) {
	    System.out.println("using index, subsequence starting at "
			       + range.subsequenceIndex(ind3));
	}

	short usfsequence[] = {1, 2, 8, 4, 7, 5, 2, 2, 8, 10, 2, 8, 9};
	short usfsubsequence[] = {2, 8};
	for (int i = 0; i < usfsequence.length; i++) {
	    usfsequence[i] += (short)40000;
	}
	for (int i = 0; i < usfsubsequence.length; i++) {
	    usfsubsequence[i] += (short)40000;
	}

	SuffixArray.UnsignedShort usfsa =
	    new SuffixArray.UnsignedShort(usfsequence, 40011);
	int[] usfA = usfsa.getArray();
	System.out.println("usfA.length = " + usfA.length
			   + ", usfsequence.length = " + usfsequence.length);
	System.out.println("usfA:");
	for (int index: usfA) {
	    if (index == usfsequence.length) {
		System.out.println("    " + index);
	    } else {
		System.out.println("    " + index
				   + " --> " + usfsequence[index]);
	    }
	}

	int usf1 = usfsa.findSubsequence(usfsubsequence, false);
	int usf2 = usfsa.findSubsequence(usfsubsequence, true);

	short usfsubsequenceL[] = {(short)40001};
	short usfsubsequenceU[] = {(short)40009};
	short usfsubsequenceM[] = {(short)40007, (short)40005, (short)40002};
	short usfsubsequence0[] = {(short)40003, (short)40004};
	short usfsubsequence8[] = {(short)40008};
	t1a = usfsa.findSubsequence(usfsubsequenceL, false);
	t1b = usfsa.findSubsequence(usfsubsequenceL, true);
	t2a = usfsa.findSubsequence(usfsubsequenceU, false);
	t2b = usfsa.findSubsequence(usfsubsequenceU, true);
	t3a = usfsa.findSubsequence(usfsubsequenceM, false);
	t3b = usfsa.findSubsequence(usfsubsequenceM, true);
	t4a = usfsa.findSubsequence(usfsubsequence0, false);
	t4b = usfsa.findSubsequence(usfsubsequence0, true);
	t5a = usfsa.findSubsequence(usfsubsequence8, false);
	t5b = usfsa.findSubsequence(usfsubsequence8, true);

	usfsa.useLCP();
	usfsa.useLCPLR();
	v1 = usfsa.findSubsequence(usfsubsequence, false);
	v2 = usfsa.findSubsequence(usfsubsequence, true);
	if (usf1 != v1 || usf2 != v2) {
	    System.out.println("v1 = " + v1 + ", v2 = " + v2);
	    throw new Exception();
	}
	tt1a = usfsa.findSubsequence(usfsubsequenceL, false);
	tt1b = usfsa.findSubsequence(usfsubsequenceL, true);
	tt2a = usfsa.findSubsequence(usfsubsequenceU, false);
	tt2b = usfsa.findSubsequence(usfsubsequenceU, true);
	tt3a = usfsa.findSubsequence(usfsubsequenceM, false);
	tt3b = usfsa.findSubsequence(usfsubsequenceM, true);
	tt4a = usfsa.findSubsequence(usfsubsequence0, false);
	tt4b = usfsa.findSubsequence(usfsubsequence0, true);
	tt5a = usfsa.findSubsequence(usfsubsequence8, false);
	tt5b = usfsa.findSubsequence(usfsubsequence8, true);
	if (t1a != tt1a) throw new Exception();
	if (t1b != tt1b) throw new Exception();
	if (t2a != tt2a) throw new Exception();
	if (t2b != tt2b) throw new Exception();
	if (t3a != tt3a) throw new Exception();
	if (t3b != tt3b) throw new Exception();
	if (t4a != tt4a) throw new Exception();
	if (t4b != tt4b) throw new Exception();
	if (t5a != tt5a) throw new Exception();
	if (t5b != tt5b) throw new Exception();

	System.out.format("usfsa test: first index = %d, last index = %d\n",
			  usf1, usf2);
	System.out.format("usfsequence: first index = %d, last index = %d\n",
			  usfA[usf1], usfA[usf2]);
	System.out.format("usfsa: any index = %d\n",
			  usfsa.findSubsequence(usfsubsequence));
	int usf1a = usfsa.findSubsequence(usfsubsequence, 0, 1, false);
	int usf2a = usfsa.findSubsequence(usfsubsequence, 0, 1, true);
	int usf1b = usfsa.findSubsequence(1, usfsubsequence, 1, 2,
					  usf1a, usf2a+1,
					  false);
	int usf2b = usfsa.findSubsequence(1, usfsubsequence, 1,
					  2, usf1a, usf2a+1,
					  true);
	if (usf1 != usf1b) throw new Exception();
	if (usf2 != usf2b) throw new Exception();


	range = usfsa.findRange(usfsubsequence);

	System.out.println("range.size() = " + range.size());
	System.out.println("range.subsequenceLength() = "
			   + range.subsequenceLength());


	for (int ind2: range) {
	    System.out.println("using iterator, subsequence starting at "
			       + ind2);
	}

	for (int ind2: range.toArray()) {
	    System.out.println("using array, subsequence starting at "
			       + ind2);
	}

	for (int ind2: range.toArray(new int[range.size()])) {
	    System.out.println("using our array, subsequence starting at "
			       + ind2);
	}

	for (int ind3 = 0; ind3 < range.size(); ind3++) {
	    System.out.println("using index, subsequence starting at "
			       + range.subsequenceIndex(ind3));
	}


	char cfsequence[] = {1, 2, 8, 4, 7, 5, 2, 2, 8, 10, 2, 8, 9};
	char cfsubsequence[] = {2, 8};

	SuffixArray.Char cfsa = new SuffixArray.Char(cfsequence, 11);
	int[] cfA = cfsa.getArray();
	System.out.println("cfA.length = " + cfA.length
			   + ", cfsequence.length = " + cfsequence.length);
	System.out.println("cfA:");
	for (int index: cfA) {
	    if (index == cfsequence.length) {
		System.out.println("    " + index);
	    } else {
		System.out.println("    " + index
				   + " --> " + (int)cfsequence[index]);
	    }
	}

	int cf1 = cfsa.findSubsequence(cfsubsequence, false);
	int cf2 = cfsa.findSubsequence(cfsubsequence, true);
	System.out.format("cfsa test: first index = %d, last index = %d\n",
			  cf1, cf2);
	System.out.format("cfsequence: first index = %d, last index = %d\n",
			  cfA[cf1], cfA[cf2]);
	System.out.format("cfsa: any index = %d\n",
			  cfsa.findSubsequence(cfsubsequence));

	int cf1a = cfsa.findSubsequence(cfsubsequence, 0, 1, false);
	int cf2a = cfsa.findSubsequence(cfsubsequence, 0, 1, true);
	int cf1b = cfsa.findSubsequence(1, cfsubsequence, 1, 2, cf1a, cf2a+1,
					false);
	int cf2b = cfsa.findSubsequence(1, cfsubsequence, 1, 2, cf1a, cf2a+1,
					true);
	if (cf1 != cf1b) throw new Exception();
	if (cf2 != cf2b) throw new Exception();

	char cfsubsequenceL[] = {1};
	char cfsubsequenceU[] = {9};
	char cfsubsequenceM[] = {7, 5, 2};
	char cfsubsequence0[] = {3, 4};
	char cfsubsequence8[] = {8};
	t1a = cfsa.findSubsequence(cfsubsequenceL, false);
	t1b = cfsa.findSubsequence(cfsubsequenceL, true);
	t2a = cfsa.findSubsequence(cfsubsequenceU, false);
	t2b = cfsa.findSubsequence(cfsubsequenceU, true);
	t3a = cfsa.findSubsequence(cfsubsequenceM, false);
	t3b = cfsa.findSubsequence(cfsubsequenceM, true);
	t4a = cfsa.findSubsequence(cfsubsequence0, false);
	t4b = cfsa.findSubsequence(cfsubsequence0, true);
	t5a = cfsa.findSubsequence(cfsubsequence8, false);
	t5b = cfsa.findSubsequence(cfsubsequence8, true);

	cfsa.useLCP();
	cfsa.useLCPLR();
	v1 = cfsa.findSubsequence(cfsubsequence, false);
	v2 = cfsa.findSubsequence(cfsubsequence, true);
	if (cf1 != v1 || cf2 != v2) {
	    System.out.println("v1 = " + v1 + ", v2 = " + v2);
	    throw new Exception();
	}
	tt1a = cfsa.findSubsequence(cfsubsequenceL, false);
	tt1b = cfsa.findSubsequence(cfsubsequenceL, true);
	tt2a = cfsa.findSubsequence(cfsubsequenceU, false);
	tt2b = cfsa.findSubsequence(cfsubsequenceU, true);
	tt3a = cfsa.findSubsequence(cfsubsequenceM, false);
	tt3b = cfsa.findSubsequence(cfsubsequenceM, true);
	tt4a = cfsa.findSubsequence(cfsubsequence0, false);
	tt4b = cfsa.findSubsequence(cfsubsequence0, true);
	tt5a = cfsa.findSubsequence(cfsubsequence8, false);
	tt5b = cfsa.findSubsequence(cfsubsequence8, true);
	if (t1a != tt1a) throw new Exception();
	if (t1b != tt1b) throw new Exception();
	if (t2a != tt2a) throw new Exception();
	if (t2b != tt2b) throw new Exception();
	if (t3a != tt3a) throw new Exception();
	if (t3b != tt3b) throw new Exception();
	if (t4a != tt4a) throw new Exception();
	if (t4b != tt4b) throw new Exception();
	if (t5a != tt5a) throw new Exception();
	if (t5b != tt5b) throw new Exception();


	range = cfsa.findRange(cfsubsequence);

	System.out.println("range.size() = " + range.size());
	System.out.println("range.subsequenceLength() = "
			   + range.subsequenceLength());


	for (int ind2: range) {
	    System.out.println("using iterator, subsequence starting at "
			       + ind2);
	}

	for (int ind2: range.toArray()) {
	    System.out.println("using array, subsequence starting at "
			       + ind2);
	}

	for (int ind2: range.toArray(new int[range.size()])) {
	    System.out.println("using our array, subsequence starting at "
			       + ind2);
	}

	for (int ind3 = 0; ind3 < range.size(); ind3++) {
	    System.out.println("using index, subsequence starting at "
			       + range.subsequenceIndex(ind3));
	}


	byte bfsequence[] = {1, 2, 8, 4, 7, 5, 2, 2, 8, 10, 2, 8, 9};
	byte bfsubsequence[] = {2, 8};

	SuffixArray.Byte bfsa = new SuffixArray.Byte(bfsequence, 11);
	int[] bfA = bfsa.getArray();
	System.out.println("bfA.length = " + bfA.length
			   + ", bfsequence.length = " + bfsequence.length);
	System.out.println("bfA:");
	for (int index: bfA) {
	    if (index == bfsequence.length) {
		System.out.println("    " + index);
	    } else {
		System.out.println("    " + index
				   + " --> " + bfsequence[index]);
	    }
	}

	int bf1 = bfsa.findSubsequence(bfsubsequence, false);
	int bf2 = bfsa.findSubsequence(bfsubsequence, true);
	System.out.format("bfsa test: first index = %d, last index = %d\n",
			  bf1, bf2);
	System.out.format("bfsequence: first index = %d, last index = %d\n",
			  bfA[bf1], bfA[bf2]);
	System.out.format("bfsa: any index = %d\n",
			  bfsa.findSubsequence(bfsubsequence));

	int bf1a = bfsa.findSubsequence(bfsubsequence, 0, 1, false);
	int bf2a = bfsa.findSubsequence(bfsubsequence, 0, 1, true);
	int bf1b = bfsa.findSubsequence(1, bfsubsequence, 1, 2, bf1a, bf2a+1,
					false);
	int bf2b = bfsa.findSubsequence(1, bfsubsequence, 1, 2, bf1a, bf2a+1,
					true);
	if (bf1 != bf1b) throw new Exception();
	if (bf2 != bf2b) throw new Exception();

	byte bfsubsequenceL[] = {1};
        byte bfsubsequenceU[] = {9};
	byte bfsubsequenceM[] = {7, 5, 2};
	byte bfsubsequence0[] = {3, 4};
	byte bfsubsequence8[] = {8};
	t1a = bfsa.findSubsequence(bfsubsequenceL, false);
	t1b = bfsa.findSubsequence(bfsubsequenceL, true);
	t2a = bfsa.findSubsequence(bfsubsequenceU, false);
	t2b = bfsa.findSubsequence(bfsubsequenceU, true);
	t3a = bfsa.findSubsequence(bfsubsequenceM, false);
	t3b = bfsa.findSubsequence(bfsubsequenceM, true);
	t4a = bfsa.findSubsequence(bfsubsequence0, false);
	t4b = bfsa.findSubsequence(bfsubsequence0, true);
	t5a = bfsa.findSubsequence(bfsubsequence8, false);
	t5b = bfsa.findSubsequence(bfsubsequence8, true);

	bfsa.useLCP();
	bfsa.useLCPLR();
	v1 = bfsa.findSubsequence(bfsubsequence, false);
	v2 = bfsa.findSubsequence(bfsubsequence, true);
	if (bf1 != v1 || bf2 != v2) {
	    System.out.println("v1 = " + v1 + ", v2 = " + v2);
	    throw new Exception();
	}
	tt1a = bfsa.findSubsequence(bfsubsequenceL, false);
	tt1b = bfsa.findSubsequence(bfsubsequenceL, true);
	tt2a = bfsa.findSubsequence(bfsubsequenceU, false);
	tt2b = bfsa.findSubsequence(bfsubsequenceU, true);
	tt3a = bfsa.findSubsequence(bfsubsequenceM, false);
	tt3b = bfsa.findSubsequence(bfsubsequenceM, true);
	tt4a = bfsa.findSubsequence(bfsubsequence0, false);
	tt4b = bfsa.findSubsequence(bfsubsequence0, true);
	tt5a = bfsa.findSubsequence(bfsubsequence8, false);
	tt5b = bfsa.findSubsequence(bfsubsequence8, true);
	if (t1a != tt1a) throw new Exception();
	if (t1b != tt1b) throw new Exception();
	if (t2a != tt2a) throw new Exception();
	if (t2b != tt2b) throw new Exception();
	if (t3a != tt3a) throw new Exception();
	if (t3b != tt3b) throw new Exception();
	if (t4a != tt4a) throw new Exception();
	if (t4b != tt4b) throw new Exception();
	if (t5a != tt5a) throw new Exception();
	if (t5b != tt5b) throw new Exception();

	range = bfsa.findRange(bfsubsequence);

	System.out.println("range.size() = " + range.size());
	System.out.println("range.subsequenceLength() = "
			   + range.subsequenceLength());


	for (int ind2: range) {
	    System.out.println("using iterator, subsequence starting at "
			       + ind2);
	}

	for (int ind2: range.toArray()) {
	    System.out.println("using array, subsequence starting at "
			       + ind2);
	}

	for (int ind2: range.toArray(new int[range.size()])) {
	    System.out.println("using our array, subsequence starting at "
			       + ind2);
	}

	for (int ind3 = 0; ind3 < range.size(); ind3++) {
	    System.out.println("using index, subsequence starting at "
			       + range.subsequenceIndex(ind3));
	}

	byte ubfsequence[] = {1, 2, 8, 4, 7, 5, 2, 2, 8, 10, 2, 8, 9};
	byte ubfsubsequence[] = {2, 8};

	for (int i = 0; i < ubfsequence.length; i++) {
	    ubfsequence[i] = (byte)(ubfsequence[i] | 0x40);
	}
	for (int i = 0; i < ubfsubsequence.length; i++) {
	    ubfsubsequence[i] = (byte)(ubfsubsequence[i] | 0x40);
	}

	SuffixArray.UnsignedByte ubfsa = new
	    SuffixArray.UnsignedByte(ubfsequence, 0x40 | 11);
	int[] ubfA = ubfsa.getArray();
	System.out.println("ubfA.length = " + ubfA.length
			   + ", ubfsequence.length = " + ubfsequence.length);
	System.out.println("ubfA:");
	for (int index: ubfA) {
	    if (index == ubfsequence.length) {
		System.out.println("    " + index);
	    } else {
		System.out.println("    " + index
				   + " --> " + ubfsequence[index]);
	    }
	}

	int ubf1 = ubfsa.findSubsequence(ubfsubsequence, false);
	int ubf2 = ubfsa.findSubsequence(ubfsubsequence, true);
	System.out.format("ubfsa test: first index = %d, last index = %d\n",
			  ubf1, ubf2);
	System.out.format("ubfsequence: first index = %d, last index = %d\n",
			  ubfA[ubf1], ubfA[ubf2]);
	System.out.format("ubfsa: any index = %d\n",
			  ubfsa.findSubsequence(ubfsubsequence));

	int ubf1a = ubfsa.findSubsequence(ubfsubsequence, 0, 1, false);
	int ubf2a = ubfsa.findSubsequence(ubfsubsequence, 0, 1, true);
	int ubf1b = ubfsa.findSubsequence(1, ubfsubsequence, 1, 2, ubf1a,
					  ubf2a+1,
					  false);
	int ubf2b = ubfsa.findSubsequence(1, ubfsubsequence, 1, 2, ubf1a,
					  ubf2a+1,
					  true);
	if (ubf1 != ubf1b) throw new Exception();
	if (ubf2 != ubf2b) throw new Exception();

	byte ubfsubsequenceL[] = {(byte) (0x40 | 1)};
        byte ubfsubsequenceU[] = {(byte) (0x40 | 9)};
	byte ubfsubsequenceM[] = {(byte) (0x40 | 7),
	    (0x4F | 5), (0x40 | 2)};
	byte ubfsubsequence0[] = {(0x40 | 3), (0x40 |4)};
	byte ubfsubsequence8[] = {(0x40 | 8)};
	t1a = ubfsa.findSubsequence(ubfsubsequenceL, false);
	t1b = ubfsa.findSubsequence(ubfsubsequenceL, true);
	t2a = ubfsa.findSubsequence(ubfsubsequenceU, false);
	t2b = ubfsa.findSubsequence(ubfsubsequenceU, true);
	t3a = ubfsa.findSubsequence(ubfsubsequenceM, false);
	t3b = ubfsa.findSubsequence(ubfsubsequenceM, true);
	t4a = ubfsa.findSubsequence(ubfsubsequence0, false);
	t4b = ubfsa.findSubsequence(ubfsubsequence0, true);
	t5a = ubfsa.findSubsequence(ubfsubsequence8, false);
	t5b = ubfsa.findSubsequence(ubfsubsequence8, true);

	ubfsa.useLCP();
	ubfsa.useLCPLR();
	v1 = ubfsa.findSubsequence(ubfsubsequence, false);
	v2 = ubfsa.findSubsequence(ubfsubsequence, true);
	if (ubf1 != v1 || ubf2 != v2) {
	    System.out.println("v1 = " + v1 + ", v2 = " + v2);
	    throw new Exception();
	}
	tt1a = ubfsa.findSubsequence(ubfsubsequenceL, false);
	tt1b = ubfsa.findSubsequence(ubfsubsequenceL, true);
	tt2a = ubfsa.findSubsequence(ubfsubsequenceU, false);
	tt2b = ubfsa.findSubsequence(ubfsubsequenceU, true);
	tt3a = ubfsa.findSubsequence(ubfsubsequenceM, false);
	tt3b = ubfsa.findSubsequence(ubfsubsequenceM, true);
	tt4a = ubfsa.findSubsequence(ubfsubsequence0, false);
	tt4b = ubfsa.findSubsequence(ubfsubsequence0, true);
	tt5a = ubfsa.findSubsequence(ubfsubsequence8, false);
	tt5b = ubfsa.findSubsequence(ubfsubsequence8, true);
	if (t1a != tt1a) throw new Exception();
	if (t1b != tt1b) throw new Exception();
	if (t2a != tt2a) throw new Exception();
	if (t2b != tt2b) throw new Exception();
	if (t3a != tt3a) throw new Exception();
	if (t3b != tt3b) throw new Exception();
	if (t4a != tt4a) throw new Exception();
	if (t4b != tt4b) throw new Exception();
	if (t5a != tt5a) throw new Exception();
	if (t5b != tt5b) throw new Exception();



	range = ubfsa.findRange(ubfsubsequence);

	System.out.println("range.size() = " + range.size());
	System.out.println("range.subsequenceLength() = "
			   + range.subsequenceLength());


	for (int ind2: range) {
	    System.out.println("using iterator, subsequence starting at "
			       + ind2);
	}

	for (int ind2: range.toArray()) {
	    System.out.println("using array, subsequence starting at "
			       + ind2);
	}

	for (int ind2: range.toArray(new int[range.size()])) {
	    System.out.println("using our array, subsequence starting at "
			       + ind2);
	}

	for (int ind3 = 0; ind3 < range.size(); ind3++) {
	    System.out.println("using index, subsequence starting at "
			       + range.subsequenceIndex(ind3));
	}


	// String case
	// char Sfsequence[] = {1, 2, 8, 4, 7, 5, 2, 2, 8, 10, 2, 8, 9};
	String Sfsequence = "128475228:289";
	String Sfsubsequence = "28";

	SuffixArray.String Sfsa = new SuffixArray.String(Sfsequence, 256);
	int[] SfA = Sfsa.getArray();
	System.out.println("SfA.length = " + SfA.length
			   + ", Sfsequence.length = " + Sfsequence.length());
	System.out.println("SfA:");
	for (int index: SfA) {
	    if (index == Sfsequence.length()) {
		System.out.println("    " + index);
	    } else {
		System.out.println("    " + index
				   + " --> " + Sfsequence.charAt(index));
	    }
	}

	int Sf1 = Sfsa.findSubsequence(Sfsubsequence, false);
	int Sf2 = Sfsa.findSubsequence(Sfsubsequence, true);
	System.out.format("Sfsa test: first index = %d, last index = %d\n",
			  Sf1, Sf2);
	System.out.format("Sfsequence: first index = %d, last index = %d\n",
			  SfA[Sf1], SfA[Sf2]);
	System.out.format("Sfsa: any index = %d\n",
			  Sfsa.findSubsequence(Sfsubsequence));

	int Sf1a = Sfsa.findSubsequence(Sfsubsequence, 0, 1, false);
	int Sf2a = Sfsa.findSubsequence(Sfsubsequence, 0, 1, true);
	int Sf1b = Sfsa.findSubsequence(1, Sfsubsequence, 1, 2, Sf1a, Sf2a+1,
					false);
	int Sf2b = Sfsa.findSubsequence(1, Sfsubsequence, 1, 2, Sf1a, Sf2a+1,
					true);
	if (Sf1 != Sf1b) throw new Exception();
	if (Sf2 != Sf2b) throw new Exception();

	String SfsubsequenceL = "1";

	String SfsubsequenceU = "9";
	String SfsubsequenceM = "752";
	String Sfsubsequence0 = "34";
	String Sfsubsequence8 = "8";
	t1a = Sfsa.findSubsequence(SfsubsequenceL, false);
	t1b = Sfsa.findSubsequence(SfsubsequenceL, true);
	t2a = Sfsa.findSubsequence(SfsubsequenceU, false);
	t2b = Sfsa.findSubsequence(SfsubsequenceU, true);
	t3a = Sfsa.findSubsequence(SfsubsequenceM, false);
	t3b = Sfsa.findSubsequence(SfsubsequenceM, true);
	t4a = Sfsa.findSubsequence(Sfsubsequence0, false);
	t4b = Sfsa.findSubsequence(Sfsubsequence0, true);
	t5a = Sfsa.findSubsequence(Sfsubsequence8, false);
	t5b = Sfsa.findSubsequence(Sfsubsequence8, true);

	Sfsa.useLCP();
	Sfsa.useLCPLR();
	v1 = Sfsa.findSubsequence(Sfsubsequence, false);
	v2 = Sfsa.findSubsequence(Sfsubsequence, true);
	if (Sf1 != v1 || Sf2 != v2) {
	    System.out.println("v1 = " + v1 + ", v2 = " + v2);
	    throw new Exception();
	}
	tt1a = Sfsa.findSubsequence(SfsubsequenceL, false);
	tt1b = Sfsa.findSubsequence(SfsubsequenceL, true);
	tt2a = Sfsa.findSubsequence(SfsubsequenceU, false);
	tt2b = Sfsa.findSubsequence(SfsubsequenceU, true);
	tt3a = Sfsa.findSubsequence(SfsubsequenceM, false);
	tt3b = Sfsa.findSubsequence(SfsubsequenceM, true);
	tt4a = Sfsa.findSubsequence(Sfsubsequence0, false);
	tt4b = Sfsa.findSubsequence(Sfsubsequence0, true);
	tt5a = Sfsa.findSubsequence(Sfsubsequence8, false);
	tt5b = Sfsa.findSubsequence(Sfsubsequence8, true);
	if (t1a != tt1a) throw new Exception();
	if (t1b != tt1b) throw new Exception();
	if (t2a != tt2a) throw new Exception();
	if (t2b != tt2b) throw new Exception();
	if (t3a != tt3a) throw new Exception();
	if (t3b != tt3b) throw new Exception();
	if (t4a != tt4a) throw new Exception();
	if (t4b != tt4b) throw new Exception();
	if (t5a != tt5a) throw new Exception();
	if (t5b != tt5b) throw new Exception();




	range = Sfsa.findRange(Sfsubsequence);

	System.out.println("range.size() = " + range.size());
	System.out.println("range.subsequenceLength() = "
			   + range.subsequenceLength());

	for (int ind2: range) {
	    System.out.println("using iterator, subsequence starting at "
			       + ind2);
	}

	for (int ind2: range.toArray()) {
	    System.out.println("using array, subsequence starting at "
			       + ind2);
	}

	for (int ind2: range.toArray(new int[range.size()])) {
	    System.out.println("using our array, subsequence starting at "
			       + ind2);
	}

	for (int ind3 = 0; ind3 < range.size(); ind3++) {
	    System.out.println("using index, subsequence starting at "
			       + range.subsequenceIndex(ind3));
	}


	Object ofsequence[] = {1, 2, 8, 4, 7, 5, 2, 2, 8, 10, 2, 8, 9};
	Object ofsubsequence[] = {2, 8};

	LinkedHashSet<Object> alph = new LinkedHashSet<>();
	for (int i = 0; i < 11; i++) {
	    alph.add(Integer.valueOf(i));
	}

	SuffixArray.Array<Object> ofsa =
	    new SuffixArray.Array<>(ofsequence, alph);
	int[] ofA = ofsa.getArray();
	System.out.println("ofA.length = " + ofA.length
			   + ", ofsequence.length = " + ofsequence.length);
	System.out.println("ofA:");
	for (int index: ofA) {
	    if (index == ofsequence.length) {
		System.out.println("    " + index);
	    } else {
		System.out.println("    " + index
				   + " --> " + (int)ofsequence[index]);
	    }
	}

	int of1 = ofsa.findSubsequence(ofsubsequence, false);
	int of2 = ofsa.findSubsequence(ofsubsequence, true);
	System.out.format("ofsa test: first index = %d, last index = %d\n",
			  of1, of2);
	System.out.format("ofsequence: first index = %d, last index = %d\n",
			  ofA[of1], ofA[of2]);
	System.out.format("ofsa: any index = %d\n",
			  ofsa.findSubsequence(ofsubsequence));

	int of1a = ofsa.findSubsequence(ofsubsequence, 0, 1, false);
	int of2a = ofsa.findSubsequence(ofsubsequence, 0, 1, true);
	int of1b = ofsa.findSubsequence(1, ofsubsequence, 1, 2, of1a, of2a+1,
					false);
	int of2b = ofsa.findSubsequence(1, ofsubsequence, 1, 2, of1a, of2a+1,
					true);
	if (of1 != of1b) throw new Exception();
	if (of2 != of2b) throw new Exception();

	Object ofsubsequenceL[] = {1};
	Object ofsubsequenceU[] = {9};
	Object ofsubsequenceM[] = {7, 5, 2};
	Object ofsubsequence0[] = {3, 4};
	Object ofsubsequence8[] = {8};
	t1a = ofsa.findSubsequence(ofsubsequenceL, false);
	t1b = ofsa.findSubsequence(ofsubsequenceL, true);
	t2a = ofsa.findSubsequence(ofsubsequenceU, false);
	t2b = ofsa.findSubsequence(ofsubsequenceU, true);
	t3a = ofsa.findSubsequence(ofsubsequenceM, false);
	t3b = ofsa.findSubsequence(ofsubsequenceM, true);
	t4a = ofsa.findSubsequence(ofsubsequence0, false);
	t4b = ofsa.findSubsequence(ofsubsequence0, true);
	t5a = ofsa.findSubsequence(ofsubsequence8, false);
	t5b = ofsa.findSubsequence(ofsubsequence8, true);

	ofsa.useLCP();
	ofsa.useLCPLR();
	v1 = ofsa.findSubsequence(ofsubsequence, false);
	v2 = ofsa.findSubsequence(ofsubsequence, true);
	if (of1 != v1 || of2 != v2) {
	    System.out.println("v1 = " + v1 + ", v2 = " + v2);
	    throw new Exception();
	}
	tt1a = ofsa.findSubsequence(ofsubsequenceL, false);
	tt1b = ofsa.findSubsequence(ofsubsequenceL, true);
	tt2a = ofsa.findSubsequence(ofsubsequenceU, false);
	tt2b = ofsa.findSubsequence(ofsubsequenceU, true);
	tt3a = ofsa.findSubsequence(ofsubsequenceM, false);
	tt3b = ofsa.findSubsequence(ofsubsequenceM, true);
	tt4a = ofsa.findSubsequence(ofsubsequence0, false);
	tt4b = ofsa.findSubsequence(ofsubsequence0, true);
	tt5a = ofsa.findSubsequence(ofsubsequence8, false);
	tt5b = ofsa.findSubsequence(ofsubsequence8, true);
	if (t1a != tt1a) throw new Exception();
	if (t1b != tt1b) throw new Exception();
	if (t2a != tt2a) throw new Exception();
	if (t2b != tt2b) throw new Exception();
	if (t3a != tt3a) throw new Exception();
	if (t3b != tt3b) throw new Exception();
	if (t4a != tt4a) throw new Exception();
	if (t4b != tt4b) throw new Exception();
	if (t5a != tt5a) throw new Exception();
	if (t5b != tt5b) throw new Exception();


	range = ofsa.findRange(ofsubsequence);

	System.out.println("range.size() = " + range.size());
	System.out.println("range.subsequenceLength() = "
			   + range.subsequenceLength());


	for (int ind2: range) {
	    System.out.println("using iterator, subsequence starting at "
			       + ind2);
	}

	for (int ind2: range.toArray()) {
	    System.out.println("using array, subsequence starting at "
			       + ind2);
	}

	for (int ind2: range.toArray(new int[range.size()])) {
	    System.out.println("using our array, subsequence starting at "
			       + ind2);
	}

	for (int ind3 = 0; ind3 < range.size(); ind3++) {
	    System.out.println("using index, subsequence starting at "
			       + range.subsequenceIndex(ind3));
	}


	int ifsequence2[] = {7, 1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 5,
			   1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 4,
			   1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 4, 8, 0};

	int ifsubsequence2[] = {1, 2, 3, 4};
	SuffixArray.Integer ifsa2 = new SuffixArray.Integer(ifsequence2, 11);
	int[] ifA2 = ifsa2.getArray();
	int if21 = ifsa2.findSubsequence(ifsubsequence2, false);
	int if22 = ifsa2.findSubsequence(ifsubsequence2, true);
	System.out.format("ifsa2 test: first index = %d, last index = %d\n",
			  if21, if22);
	System.out.format("ifsequence2: first index = %d, last index = %d\n",
			  ifA2[if21], ifA2[if22]);
	System.out.format("ifsa2: any index = %d\n",
			  ifsa2.findSubsequence(ifsubsequence2));
	SuffixArray.Range range2 = ifsa2.findRange(ifsubsequence2);

	System.out.println("range2.size() = " + range2.size());
	for (int ind2: range2) {
	    System.out.println("using iterator, subsequence2 starting at "
			       + ind2);
	}

	short sfsequence2[] = {7, 1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 5,
			   1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 4,
			   1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 4, 8, 0};

	short sfsubsequence2[] = {1, 2, 3, 4};
	SuffixArray.Short sfsa2 = new SuffixArray.Short(sfsequence2, 11);
	int[] sfA2 = sfsa2.getArray();
	int sf21 = sfsa2.findSubsequence(sfsubsequence2, false);
	int sf22 = sfsa2.findSubsequence(sfsubsequence2, true);
	System.out.format("sfsa2 test: first index = %d, last index = %d\n",
			  sf21, sf22);
	System.out.format("sfsequence2: first index = %d, last index = %d\n",
			  sfA2[sf21], sfA2[sf22]);
	System.out.format("sfa2: any index = %d\n",
			  sfsa2.findSubsequence(sfsubsequence2));

	range2 = sfsa2.findRange(sfsubsequence2);

	System.out.println("range2.size() = " + range2.size());
	for (int ind2: range2) {
	    System.out.println("using iterator, subsequence2 starting at "
			       + ind2);
	}

	byte bfsequence2[] = {7, 1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 5,
			   1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 4,
			   1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 4, 8, 0};

	byte bfsubsequence2[] = {1, 2, 3, 4};
	SuffixArray.Byte bfsa2 = new SuffixArray.Byte(bfsequence2, 11);
	int[] bfA2 = bfsa2.getArray();
	int bf21 = bfsa2.findSubsequence(bfsubsequence2, false);
	int bf22 = bfsa2.findSubsequence(bfsubsequence2, true);
	System.out.format("bfsa2 test: first index = %d, last index = %d\n",
			  bf21, bf22);
	System.out.format("bfsequence2: first index = %d, last index = %d\n",
			  bfA2[bf21], bfA2[bf22]);
	System.out.format("ifa2: any index = %d\n",
			  bfsa2.findSubsequence(bfsubsequence2));

	range2 = bfsa2.findRange(bfsubsequence2);

	System.out.println("range2.size() = " + range2.size());
	for (int ind2: range2) {
	    System.out.println("using iterator, subsequence2 starting at "
			       + ind2);
	}

	char cfsequence2[] = {7, 1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 5,
			   1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 4,
			   1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 4, 8, 0};

	char cfsubsequence2[] = {1, 2, 3, 4};
	SuffixArray.Char cfsa2 = new SuffixArray.Char(cfsequence2, 11);
	int[] cfA2 = cfsa2.getArray();
	int cf21 = cfsa2.findSubsequence(cfsubsequence2, false);
	int cf22 = cfsa2.findSubsequence(cfsubsequence2, true);
	System.out.format("cfsa2 test: first index = %d, last index = %d\n",
			  cf21, cf22);
	System.out.format("cfsequence2: first index = %d, last index = %d\n",
			  cfA2[cf21], cfA2[cf22]);
	System.out.format("cfa2: any index = %d\n",
			  cfsa2.findSubsequence(cfsubsequence2));

	range2 = cfsa2.findRange(cfsubsequence2);

	System.out.println("range2.size() = " + range2.size());
	for (int ind2: range2) {
	    System.out.println("using iterator, subsequence2 starting at "
			       + ind2);
	}

	String Sfsequence2 = "712341234123512341234123412341234123480";

	String Sfsubsequence2 = "1234";
	SuffixArray.String Sfsa2 = new SuffixArray.String(Sfsequence2, 128);
	int[] SfA2 = Sfsa2.getArray();
	int Sf21 = Sfsa2.findSubsequence(Sfsubsequence2, false);
	int Sf22 = Sfsa2.findSubsequence(Sfsubsequence2, true);
	System.out.format("Sfsa2 test: first index = %d, last index = %d\n",
			  Sf21, Sf22);
	System.out.format("Sfsequence2: first index = %d, last index = %d\n",
			  SfA2[Sf21], SfA2[Sf22]);
	System.out.format("Sfa2: any index = %d\n",
			  Sfsa2.findSubsequence(Sfsubsequence2));
	range2 = Sfsa2.findRange(Sfsubsequence2);

	System.out.println("range2.size() = " + range2.size());
	for (int ind2: range2) {
	    System.out.println("using iterator, subsequence2 starting at "
			       + ind2);
	}

	Integer ofsequence2[] = {7, 1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 5,
			   1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 4,
			   1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 4, 8, 0};

	Integer ofsubsequence2[] = {1, 2, 3, 4};

	LinkedHashSet<Integer> alph2 = new LinkedHashSet<>();
	for (int i = 0; i < 11; i++) {
	    alph2.add(Integer.valueOf(i));
	}

	SuffixArray.Array<Integer> ofsa2 =
	    new SuffixArray.Array<Integer>(ofsequence2, alph2);
	int[] ofA2 = ofsa2.getArray();
	int of21 = ofsa2.findSubsequence(ofsubsequence2, false);
	int of22 = ofsa2.findSubsequence(ofsubsequence2, true);
	System.out.format("ofsa2 test: first index = %d, last index = %d\n",
			  of21, of22);
	System.out.format("ofsequence2: first index = %d, last index = %d\n",
			  ofA2[of21], ofA2[of22]);
	System.out.format("ofa2: any index = %d\n",
			  ofsa2.findSubsequence(ofsubsequence2));
	range2 = ofsa2.findRange(ofsubsequence2);

	System.out.println("range2.size() = " + range2.size());
	for (int ind2: range2) {
	    System.out.println("using iterator, subsequence2 starting at "
			       + ind2);
	}

	// Try a large number of cases
	System.out.println("random tests for findSubsequence:");
	UniformIntegerRV irv = new UniformIntegerRV(5, true, 9, true);
	for (int icases = 0; icases < 10000; icases++) {
	    for (int len = 1; len < 4; len++) {
		int[] rsubsequence = new int[len];
		for (int is = 0; is < len; is++) {
		    rsubsequence[is] = is+1;
		}
		for (int rcount = 0; rcount < 2; rcount++) {
		    for (int tlen = 1; tlen < 16; tlen++) {
			if (len*rcount > tlen) continue;
			int[] rsequence = new int[tlen];
			for (int rk = 0; rk < tlen; rk++) {
			    rsequence[rk] = irv.next();
			}
			if (rcount == 1) {
			    int offset = (tlen == len)? 0:
				StaticRandom.nextInt(tlen - len);
			    for (int kk = 0; kk < len; kk++) {
				rsequence[kk + offset] = rsubsequence[kk];
			    }
			}
			SuffixArray.Integer iisa =
			    new SuffixArray.Integer(rsequence, 10);
			int iiupper = iisa.findSubsequence(rsubsequence, true);
			int iilower = iisa.findSubsequence(rsubsequence,
							   false);
			if (iilower != -1) {
			    for (int i = iilower; i <= iiupper; i++) {
				int start = iisa.getArray()[i];
				for (int j = 0; j < rsubsequence.length; j++) {
				    if (rsubsequence[j] !=
					rsequence[start + j]) {
					throw new Exception();
				    }
				}
			    }
			}

			int ourcount = 1 + iiupper - iilower;
			if (iilower == -1 && iiupper == -1) ourcount = 0;
			if (ourcount != rcount) {
			    System.out.format("ii: ourcount %d != rcount %d\n",
					      ourcount, rcount);
			    System.exit(1);
			}
			if (ourcount != iisa.findRange(rsubsequence).size()) {
			    System.out.println("ii: range error");
			    System.exit(1);
			}

			try {
			    iisa.useLCP();
			    iisa.useLCPLR();
			} catch (Exception e) {
			    System.out.println("sequence: ");
			    for (int i = 0; i < rsequence.length; i++) {
				System.out.println("    " + rsequence[i]);
			    }
			    throw e;
			}
			int iiupper2 = iisa.findSubsequence(rsubsequence, true);
			int iilower2 = iisa.findSubsequence(rsubsequence,
							   false);

			if (iiupper2 != iiupper || iilower2 != iilower) {
			    throw new Exception();
			}

			// short case
			short[] srsequence = new short[tlen];
			short[] srsubsequence = new short[len];
			for (int j = 0; j < tlen; j++) {
			    srsequence[j] = (short) rsequence[j];
			}
			for (int j = 0; j < len; j++) {
			    srsubsequence[j] = (short) rsubsequence[j];
			}
			SuffixArray.Short sisa =
			    new SuffixArray.Short(srsequence, 10);
			int siupper = sisa.findSubsequence(srsubsequence, true);
			int silower = sisa.findSubsequence(srsubsequence,
							   false);
			if (silower != iilower || siupper != iiupper) {
			    throw new Exception();
			}
			if (silower != -1) {
			    for (int i = silower; i <= siupper; i++) {
				int start = sisa.getArray()[i];
				for (int j = 0; j < srsubsequence.length; j++) {
				    if (srsubsequence[j] !=
					srsequence[start + j]) {
					throw new Exception();
				    }
				}
			    }
			}

			ourcount = 1 + siupper - silower;
			if (silower == -1 && siupper == -1) ourcount = 0;
			if (ourcount != rcount) {
			    System.out.format("si: ourcount %d != rcount %d\n",
					      ourcount, rcount);
			    System.exit(1);
			}
			if (ourcount != sisa.findRange(srsubsequence).size()) {
			    System.out.println("si: range error");
			    System.exit(1);
			}
			for (int i = 0; i < srsequence.length; i++) {
			    srsequence[i] = (short)( 0xF000 | srsequence[i]);
			}
			for (int i = 0; i < srsubsequence.length; i++) {
			    srsubsequence[i] = (short)
				(0xF000 | srsubsequence[i]);
			}
			sisa.useLCP();
			sisa.useLCPLR();
			int siupper2 = sisa.findSubsequence(srsubsequence,
							    true);
			int silower2 = sisa.findSubsequence(srsubsequence,
							   false);
			if (siupper2 != siupper || silower2 != silower) {
			    throw new Exception();
			}


			SuffixArray.UnsignedShort usisa =
			    new SuffixArray.UnsignedShort(srsequence,
							  0xF000 | 10);
			int usiupper = usisa.findSubsequence(srsubsequence,
							    true);
			int usilower = usisa.findSubsequence(srsubsequence,
							    false);

			if (usilower != -1) {
			    for (int i = usilower; i <= usiupper; i++) {
				int start = usisa.getArray()[i];
				for (int j = 0; j < srsubsequence.length; j++) {
				    if (srsubsequence[j] !=
					srsequence[start + j]) {
					throw new Exception();
				    }
				}
			    }
			}

			if (usilower != iilower || usiupper != iiupper) {
			    throw new Exception();
			}

			ourcount = 1 + usiupper - usilower;
			if (usilower == -1 && usiupper == -1) ourcount = 0;
			if (ourcount != rcount) {
			    System.out.format("usi: ourcount %d != rcount %d\n",
					      ourcount, rcount);
			    System.exit(1);
			}
			if (ourcount != usisa.findRange(srsubsequence).size()) {
			    System.out.println("si: range error");
			    System.exit(1);
			}

			usisa.useLCP();
			usisa.useLCPLR();
			int usiupper2 = usisa.findSubsequence(srsubsequence,
							      true);
			int usilower2 = usisa.findSubsequence(srsubsequence,
							      false);
			if (usiupper2 != usiupper || usilower2 != usilower) {
			    throw new Exception();
			}

			// byte case
			byte[] brsequence = new byte[tlen];
			byte[] brsubsequence = new byte[len];
			for (int j = 0; j < tlen; j++) {
			    brsequence[j] = (byte) rsequence[j];
			}
			for (int j = 0; j < len; j++) {
			    brsubsequence[j] = (byte) rsubsequence[j];
			}
			SuffixArray.Byte bisa =
			    new SuffixArray.Byte(brsequence, 10);
			int biupper = bisa.findSubsequence(brsubsequence, true);
			int bilower = bisa.findSubsequence(brsubsequence,
							   false);
			if (bilower != -1) {
			    for (int i = bilower; i <= biupper; i++) {
				int start = bisa.getArray()[i];
				for (int j = 0; j < brsubsequence.length; j++) {
				    if (brsubsequence[j] !=
					brsequence[start + j]) {
					throw new Exception();
				    }
				}
			    }
			}

			if (bilower != iilower || biupper != iiupper) {
			    throw new Exception();
			}

			ourcount = 1 + biupper - bilower;
			if (bilower == -1 && biupper == -1) ourcount = 0;
			if (ourcount != rcount) {
			    System.out.format("bi: ourcount %d != rcount %d\n",
					      ourcount, rcount);
			    System.exit(1);
			}
			if (ourcount != bisa.findRange(brsubsequence).size()) {
			    System.out.println("si: range error");
			    System.exit(1);
			}

			bisa.useLCP();
			bisa.useLCPLR();
			int biupper2 = bisa.findSubsequence(brsubsequence,
							    true);
			int bilower2 = bisa.findSubsequence(brsubsequence,
							   false);
			if (biupper2 != biupper || bilower2 != bilower) {
			    throw new Exception();
			}

			// unsigned byte case
			SuffixArray.UnsignedByte ubisa =
			    new SuffixArray.UnsignedByte(brsequence, 0x40 | 10);
			int ubiupper = ubisa.findSubsequence(brsubsequence,
							     true);
			int ubilower = ubisa.findSubsequence(brsubsequence,
							     false);
			if (ubilower != -1) {
			    for (int i = ubilower; i <= ubiupper; i++) {
				int start = ubisa.getArray()[i];
				for (int j = 0; j < brsubsequence.length; j++) {
				    if (brsubsequence[j] !=
					brsequence[start + j]) {
					throw new Exception();
				    }
				}
			    }
			}

			if (ubilower != iilower || ubiupper != iiupper) {
			    throw new Exception();
			}

			ourcount = 1 + biupper - bilower;
			if (ubilower == -1 && ubiupper == -1) ourcount = 0;
			if (ourcount != rcount) {
			    System.out.format("bi: ourcount %d != rcount %d\n",
					      ourcount, rcount);
			    System.exit(1);
			}
			if (ourcount != ubisa.findRange(brsubsequence).size()) {
			    System.out.println("si: range error");
			    System.exit(1);
			}

			ubisa.useLCP();
			ubisa.useLCPLR();
			int ubiupper2 = ubisa.findSubsequence(brsubsequence,
							      true);
			int ubilower2 = ubisa.findSubsequence(brsubsequence,
							      false);
			if (ubiupper2 != ubiupper || ubilower2 != ubilower) {
			    throw new Exception();
			}


			// char case
			char[] crsequence = new char[tlen];
			char[] crsubsequence = new char[len];
			for (int j = 0; j < tlen; j++) {
			    crsequence[j] = (char) rsequence[j];
			}
			for (int j = 0; j < len; j++) {
			    crsubsequence[j] = (char) rsubsequence[j];
			}
			SuffixArray.Char cisa =
			    new SuffixArray.Char(crsequence, 10);
			int ciupper = cisa.findSubsequence(crsubsequence, true);
			int cilower = cisa.findSubsequence(crsubsequence,
							   false);
			if (cilower != -1) {
			    for (int i = cilower; i <= ciupper; i++) {
				int start = cisa.getArray()[i];
				for (int j = 0; j < crsubsequence.length; j++) {
				    if (crsubsequence[j] !=
					crsequence[start + j]) {
					throw new Exception();
				    }
				}
			    }
			}

			if (cilower != iilower || ciupper != iiupper) {
			    throw new Exception();
			}

			ourcount = 1 + ciupper - cilower;
			if (cilower == -1 && ciupper == -1) ourcount = 0;
			if (ourcount != rcount) {
			    System.out.format("ci: ourcount %d != rcount %d\n",
					      ourcount, rcount);
			    System.exit(1);
			}
			if (ourcount != cisa.findRange(crsubsequence).size()) {
			    System.out.println("si: range error");
			    System.exit(1);
			}

			cisa.useLCP();
			cisa.useLCPLR();
			int ciupper2 = cisa.findSubsequence(crsubsequence,
							    true);
			int cilower2 = cisa.findSubsequence(crsubsequence,
							   false);
			if (ciupper2 != ciupper || cilower2 != cilower) {
			    throw new Exception();
			}

			// string case
			String Srsequence = new String(crsequence);
			String Srsubsequence = new String(crsubsequence);
			SuffixArray.String Sisa =
			    new SuffixArray.String(Srsequence, 10);
			int Siupper = Sisa.findSubsequence(Srsubsequence, true);
			int Silower = Sisa.findSubsequence(Srsubsequence,
							   false);
			if (Silower != -1) {
			    for (int i = Silower; i <= Siupper; i++) {
				int start = Sisa.getArray()[i];
				for (int j = 0; j < crsubsequence.length; j++) {
				    if (crsubsequence[j] !=
					crsequence[start + j]) {
					throw new Exception();
				    }
				}
			    }
			}
			if (Silower != iilower || Siupper != iiupper) {
			    throw new Exception();
			}


			ourcount = 1 + Siupper - Silower;
			if (Silower == -1 && Siupper == -1) ourcount = 0;
			if (ourcount != rcount) {
			    System.out.format("Si: ourcount %d != rcount %d\n",
					      ourcount, rcount);
			    System.exit(1);
			}
			if (ourcount != Sisa.findRange(Srsubsequence).size()) {
			    System.out.println("si: range error");
			    System.exit(1);
			}

			Sisa.useLCP();
			Sisa.useLCPLR();
			int Siupper2 = Sisa.findSubsequence(Srsubsequence,
							    true);
			int Silower2 = Sisa.findSubsequence(Srsubsequence,
							   false);
			if (Siupper2 != Siupper || Silower2 != Silower) {
			    throw new Exception();
			}


			// array case
			Integer[] Irsequence = new Integer[tlen];
			Integer[] Irsubsequence = new Integer[len];
			LinkedHashSet<Integer> Ialpha = new LinkedHashSet<>();
			for (int j = 0; j < 10; j++) {
			    Ialpha.add(Integer.valueOf(j));
			}
			for (int j = 0; j < tlen; j++) {
			    Irsequence[j] = Integer.valueOf(rsequence[j]);
			}
			for (int j = 0; j < len; j++) {
			    Irsubsequence[j] = Integer.valueOf(rsubsequence[j]);
			}
			SuffixArray.Array<Integer> Iisa =
			    new SuffixArray.Array<Integer>(Irsequence, Ialpha);
			int Iiupper = Iisa.findSubsequence(Irsubsequence, true);
			int Iilower = Iisa.findSubsequence(Irsubsequence,
							   false);

			if (Iilower != -1) {
			    for (int i = Iilower; i <= Iiupper; i++) {
				int start = Iisa.getArray()[i];
				for (int j = 0; j < crsubsequence.length; j++) {
				    if (rsubsequence[j] !=
					rsequence[start + j]) {
					throw new Exception();
				    }
				}
			    }
			}

			if (Iilower != iilower || Iiupper != iiupper) {
			    throw new Exception();
			}

			ourcount = 1 + Iiupper - Iilower;
			if (Iilower == -1 && Iiupper == -1) ourcount = 0;
			if (ourcount != rcount) {
			    System.out.format("Ii: ourcount %d != rcount %d\n",
					      ourcount, rcount);
			    System.exit(1);
			}
			if (ourcount != Iisa.findRange(Irsubsequence).size()) {
			    System.out.println("Ii: range error");
			    System.exit(1);
			}

			Iisa.useLCP();
			Iisa.useLCPLR();
			int Iiupper2 = Iisa.findSubsequence(Irsubsequence,
							    true);
			int Iilower2 = Iisa.findSubsequence(Irsubsequence,
							   false);
			if (Iiupper2 != Iiupper || Iilower2 != Iilower) {
			    throw new Exception();
			}
		    }
		}
	    }
	}
	System.out.println("OK");

	if (false) {
	    System.out.println("Timing test");
	    final int m = 25237;
	    for (int kk = 0; kk < m; kk++) {
		if (kk == m-10) System.gc();
		for (int n = 1; n < 52; n++) {
		    sequence = new int[n];
		    rv = new UniformIntegerRV(0, n);
		    for (int i = 0; i < n; i++) {
			sequence[i] = rv.next();
		    }
		    long t1 = System.nanoTime()			;
		    int[] na = makeSuffixArray(sequence);
		    long t2 = System.nanoTime();
		    suffixArray = new SuffixArray.Integer(sequence, n);
		    long t3 = System.nanoTime();
		    if (kk == m-1) {
			System.out.format("n = %d, times: %d, %d\n",
					  n, t2-t1, t3-t2);
		    }
		}
	    }
	}
	System.exit(0);
    }
}
