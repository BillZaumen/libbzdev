import org.bzdev.util.*;
import org.bzdev.math.StaticRandom;
import org.bzdev.math.rv.*;
import java.io.*;
import java.util.*;

public class LcplrTest {

    static Integer[] ia(int[] array) {
	Integer[] result = new Integer[array.length];
	for (int i = 0; i < array.length; i++) {
	    result[i] = Integer.valueOf(array[i]);
	}
	return result;
    }

    static void dotest(String name, int[] sequence, int[] subsequence,
		       boolean printArray)
	throws Exception
    {
	System.out.println();
	System.out.println(name + ":");
	SuffixArray.Integer sai = new SuffixArray.Integer(sequence, 128);
	int[] array = sai.getArray();
	if (printArray) {
	    System.out.println();
	    System.out.println("suffix array:");
	    System.out.println();
	    for (int i = 0; i < array.length; i++) {
		int start = array[i];
		System.out.format("    array[%d] = %d (", i, start);
		if (start == sequence.length) {
		    System.out.println(")");
		} else {
		    System.out.print("" + sequence[start]);
		    for (int j = 1; j < subsequence.length; j++) {
			if (start + j < sequence.length) {
			    System.out.print(", " + sequence[start+j]);
			}
		    }
		    if (start + subsequence.length < sequence.length) {
			System.out.println(", ...)");
		    } else {
			System.out.println(")");
		    }
		}
	    }
	    System.out.println();
	}
	
	int low = sai.findSubsequence(subsequence, false);
	System.out.println("low = " + low);
	int high = sai.findSubsequence(subsequence, true);
	System.out.format("subsequence index in [%d,%d]\n",
			  low, high);
	int ind = sai.findSubsequence(subsequence);
	System.out.println("subsequence index = " + ind );
	sai.useLCP();
	sai.useLCPLR();
	System.out.println("sai.useLCPLR() called");
	int ourind = sai.findSubsequence(subsequence);
	System.out.println("subsequence index = " + ourind);
	if ((ourind < 0 && ind >= 0) || (ourind > 0 && ind < 0)
	    || ourind < low || ourind > high) {
	    System.out.println("ind = " + ind + ", ourind = " + ourind);
	    if (ourind < 0) {
		System.out.format("for correct case: array[%d] = %d\n",
				  ind, array[ind]);
		System.out.print("correct subsequence  = "
				 + sequence[array[ind]]);
		for (int i = 1; i < subsequence.length; i++) {
		    System.out.format(", %d", sequence[array[ind]+i]);
		}
		System.out.println();
	    }
	    throw new Exception("unexpected value");
	}
	int ourlow = sai.findSubsequence(subsequence, false);
	System.out.println("first subsequence index = " + ourlow);
	if (low != ourlow) throw new Exception("unexpected value: "
						 + ourlow + " != " + low);
	int ourhigh = sai.findSubsequence(subsequence, true);
	System.out.println("last subsequence index = " + ourhigh);
	if (high != ourhigh) throw new Exception("unexpected value: "
						 + ourhigh + " != " + high);
    }

    public static void main(String argv[]) throws Exception {
	String string = "mississippi";
	char[] sequence = string.toCharArray();
	SuffixArray.Char sa = new SuffixArray.Char(sequence, 128);
	SuffixArray.Char sa2 = new SuffixArray.Char(sequence, 128);
	int[] array = sa.getArray();
	int[] lcp = sa.getLCP();
	for (int i = 0; i < array.length; i++) {
	    System.out.format("array[%d] = %d, lcp = %d,  suffix = \"%s\"\n",
			      i, array[i], lcp[i],
			      string.substring(array[i]));
	    
	}
	sa.useLCPLR();
	for (int i = 1; i < array.length; i++) {
	    for (int j = i+1; j < array.length; j++) {
		int len1 = sa.lcpLength(array[i], array[j]);
		int len2 = sa2.lcpLength(array[i], array[j]);
		if (len1 != len2) {
		    System.out.format("at [%d, %d], len1 = %d, len2 = %d\n",
				      i, j, len1, len2);
		}
	    }
	}

	int ifsequence[] = {7, 1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 5,
			   1, 2, 5, 4, 1, 2, 3, 4, 1, 5, 3, 4,
			   1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 4, 8, 0};

	int ifsubsequence[] = {1, 2, 3, 4};
	SuffixArray.Integer sai = new SuffixArray.Integer(ifsequence, 10);
	array = sai.getArray();
	int low = sai.findSubsequence(ifsubsequence, false);
	int high = sai.findSubsequence(ifsubsequence, true);
	System.out.format("subsequence index in [%d,%d]\n",
			  low, high);
	System.out.println("subsequence index = "
			   + sai.findSubsequence(ifsubsequence));
	sai.useLCP();
	System.out.println("sai.useLCPLR() called");
	sai.useLCPLR();
	System.out.println("subsequence index = "
			   + sai.findSubsequence(ifsubsequence));

	low = sai.findSubsequence(ifsubsequence, false);
	high = sai.findSubsequence(ifsubsequence, true);
	System.out.println("first subsequence index = " + low
			   + ": starts at offset " + array[low]);
	System.out.println("last subsequence index = " + high
			   + ": starts at offset " + array[high]);

	// observed failures from random test cases:
	// ifsequence = 
	// ifsubsequence = 
	
	int ifsequence1[] = {
		34, 45, 76, 29, 57, 107, 52, 47, 27, 1, 107, 76, 29,
		57, 107, 52, 36, 16, 91, 18, 81, 12, 92, 12, 69, 0,
		85, 65, 108, 72, 112, 75, 92, 94, 116, 63, 29, 70, 9,
		82, 71, 58, 67, 76, 29, 57, 107, 52, 73, 121, 48, 96,
		88, 84, 39, 74, 107, 37, 24, 0, 95, 71, 22, 54, 4, 9,
		0, 75, 88, 62, 35, 72, 5, 7, 96, 76, 28, 13, 63, 77,
		124, 16, 50, 40, 71, 67, 114, 106, 23, 76, 29, 57,
		107, 52, 8, 51, 27, 22, 76, 29, 57, 107, 52, 115, 115,
		31, 56, 10, 14, 23, 88, 92, 8, 98, 46, 9, 42, 39, 116,
		38, 26, 89, 123, 66, 76, 29, 57, 107, 52, 109, 110,
		91, 107, 22, 18, 100, 67, 52, 36, 60, 108, 90, 12, 62,
		76, 29, 57, 107, 52, 83, 97, 4, 40, 57, 68, 99, 28,
		79, 69, 1, 13, 36, 64, 70, 50, 0, 65, 123, 32, 94, 72,
		80, 91, 110, 76, 76, 29, 57, 107, 52, 56, 90, 74, 122,
		8, 116, 99, 70, 57, 8, 33, 112, 76, 29
	};
	int ifsubsequence1[] = {76, 29, 57, 107, 52};

	dotest("ifsequence1", ifsequence1, ifsubsequence1, false);

	int ifsequence2[] = {
	    114, 83, 86, 82, 51, 13, 63, 88, 109, 71, 64, 41, 110,
	    46, 70, 2, 49, 75, 16, 96, 0, 115, 12, 12, 69, 108,
	    122, 1, 125, 38, 94, 125, 114, 37, 33, 108, 110, 7,
	    68, 8, 10, 50, 73, 27, 11, 25, 108, 23, 12, 73, 16,
	    72, 114, 16, 9, 28, 48, 27, 10, 86, 83, 107, 69, 42,
	    113, 65, 94, 19, 90, 111, 99, 108, 99, 108, 21, 63,
	    71, 116, 42, 87, 77, 61, 13, 22, 71, 112, 111, 55, 84,
	    54, 52, 65, 41, 34, 94, 32, 39, 51, 69, 120, 32, 13,
	    22, 23, 63, 47, 90, 7, 59, 83, 67, 82, 11, 123, 22,
	    54, 0, 17, 77, 111, 21, 46, 108, 91, 40, 101, 28, 109,
	    65, 75, 78, 38, 107, 87, 9, 16, 72, 114, 16, 57, 56,
	    48, 103, 106, 116, 64, 46, 33, 100, 73, 3, 47, 60, 99,
	    25, 20, 98, 30, 58, 64, 81, 18, 85, 91, 83, 61, 54,
	    117, 126, 51, 107, 80, 98, 114, 82, 62, 47, 123, 80,
	    65, 94, 100, 73, 57, 66, 40, 4, 47, 76, 86, 118, 15,
	    12, 14, 104, 22, 53, 32, 20, 51, 2, 85, 69, 3, 34,
	    117, 3, 35, 78, 72, 40, 117, 112, 119, 113, 17, 82,
	    41, 8, 89, 87, 55, 86, 114, 10, 40, 98, 76, 70, 1, 32,
	    64, 42, 121, 8, 126, 16, 32, 121, 2, 66, 15, 104, 63,
	    68, 64, 73, 97, 41, 79, 5, 126, 63, 16, 72, 114, 16,
	    15, 45, 80, 26, 75, 106, 86, 49, 98, 22, 19, 34, 43,
	    16, 72, 114, 16, 90, 63, 126, 92, 64, 38, 68, 87, 91,
	    122, 31, 101, 123, 114, 76, 30, 9, 16, 72, 114, 16,
	    81, 43, 83, 61, 67, 96, 116, 24, 36, 83, 69, 108, 31,
	    26, 41, 73, 9, 74, 31, 91, 59, 114, 62, 87, 20, 66,
	    83, 117, 31, 16, 72, 114, 16, 50, 12, 93, 62, 16, 72,
	    114, 16, 109, 23, 75, 101, 54, 119, 106, 20, 36, 84,
	    112, 107, 16, 72, 114, 16, 74, 30, 93, 81, 103, 113,
	    39, 49, 63, 95, 33, 43, 18, 65, 126, 105, 34, 113,
	    118, 24, 40, 26, 104, 98, 85, 65, 99, 37, 69, 23, 80,
	    67, 26, 124, 45, 49, 110, 94, 92, 38, 105, 43, 89, 23,
	    36, 66, 111, 36, 82, 109, 100, 108, 7, 124, 65, 71,
	    80, 1, 21, 121, 47, 122, 54, 106, 63, 80, 49, 77, 69,
	    77, 23, 63, 72, 90, 93, 54
	};

	int ifsubsequence2[] = {16, 72, 114, 16};

	dotest("ifsequence2", ifsequence2, ifsubsequence2, false);

	int ifsequence3[] = {
	    27, 63, 15, 107, 12, 27, 63, 27, 63, 15, 107, 12, 39,
	    85, 85, 107, 12, 39, 85, 40, 39, 27, 63
	};
	int ifsubsequence3[] = {27, 63, 15, 107, 12, 39, 85};

	dotest("ifsequence3", ifsequence3, ifsubsequence3, false);


	int ifsequence4[] = {
		20, 125, 37, 51, 126, 5, 96, 62, 25, 109, 100, 113,
		36, 73, 50, 93, 72, 12, 71, 94, 119, 50, 93, 72, 50,
		50, 93, 72, 12, 71, 74, 69, 124, 83, 60, 120, 112,
		126, 50, 50, 93, 72, 12, 71, 101, 50, 93, 72, 12
	};
	int ifsubsequence4[] = {
	    50, 93, 72, 12, 71
	};

	dotest("ifsequence4", ifsequence4, ifsubsequence4, false);

	int ifsequence5[] = {
	    126, 51, 126, 75, 118, 10, 126, 53, 126, 126, 27, 95,
	    84, 89, 85, 68, 126, 45, 33, 0, 126, 50, 5, 126, 126,
	    126, 126, 63, 61, 41, 26, 61
	};
	int ifsubsequence5[] = {126};

	dotest("ifsequence5", ifsequence5, ifsubsequence5, false);

	int ifsequence6[] = {
	    34, 12, 61, 12, 12, 61, 12, 12, 61, 12, 61, 101, 12,
	    61, 12, 12, 61, 12, 12, 50, 53, 12, 61, 12, 61, 12, 12
	};
	int ifsubsequence6[] = {12, 61, 12};

	dotest("ifsequence6", ifsequence6, ifsubsequence6, true);

	int ifsequence7[] = {
	    115, 44, 110, 1, 107, 15, 90, 30, 125, 72, 111, 76,
	    38, 102, 108, 78, 106, 38, 1, 107, 15, 90, 30, 125,
	    72, 12, 1, 107, 15, 90
	};
	int ifsubsequence7[] = {1, 107, 15, 90, 30, 125, 72};

	dotest("ifsequence7", ifsequence7, ifsubsequence7, true);

	int ifsequence8[] = {
	    7, 43, 7, 125, 7, 43, 7, 125, 43, 7, 125, 125, 7, 125,
	    7, 43, 7, 125, 7, 7
	};
	int ifsubsequence8[] = {7, 43, 7, 125};

	dotest("ifsequence8", ifsequence8, ifsubsequence8, true);

	int ifsequence9[] = {
	    36, 70, 52, 34, 90, 2, 114, 115, 6, 44, 37, 114, 115,
	    6, 2, 114, 115, 6, 44, 37, 23, 40, 88, 56, 22, 120,
	    55, 5, 87, 2, 114, 115, 6, 44, 37, 51, 72, 60, 85, 2,
	    114, 115, 6, 44, 37, 118, 64, 42, 54, 3, 70, 31, 61,
	    75, 11, 25, 70, 98, 68, 2, 114, 115, 6, 44
	};
	int ifsubsequence9[] = {2, 114, 115, 6, 44, 37};

	dotest("ifsequence9", ifsequence9, ifsubsequence9, false);

	System.out.println();
	IntegerRandomVariable lenrv = new UniformIntegerRV(16, 512);
	IntegerRandomVariable srv = new UniformIntegerRV(0, 127);
	IntegerRandomVariable sublenrv = new UniformIntegerRV(1, 10);
	
	for (int i = 0; i < 100000; i++) {
	    if (i % 10000 == 0) System.out.println("i = " + i);
	    int len = lenrv.next();
	    int[] isequence = new int[len];
	    int sublen = sublenrv.next();
	    int[] subsequence = new int[sublen];
	    for (int j = 0; j < len; j++) {
		isequence[j] = srv.next();
	    }
	    for (int j = 0 ; j < sublen; j++) {
		subsequence[j] = srv.next();
	    }
	    int n = StaticRandom.nextInt(16);
	    for (int j = 0; j < n; j++) {
		int offset = StaticRandom.nextInt(len);
		int max = offset + sublen;
		int newlen = sublen;
		if (max > len) newlen -= (max - len);
		System.arraycopy(subsequence, 0, isequence, offset, newlen);
	    }
	    // log the test case in case something fails
	    /*
	    PrintWriter writer = new PrintWriter("sequence.txt", "UTF-8");
	    writer.println("sequence:");
	    writer.println();
	    writer.print(isequence[0]);
	    for (int j = 1; j < isequence.length; j++) {
		writer.print(", " + isequence[j]);
	    }
	    writer.println();
	    writer.println();
	    writer.println("subsequence:");
	    writer.println();
	    writer.print(subsequence[0]);
	    for (int j = 1; j < subsequence.length; j++) {
		writer.print(", " + subsequence[j]);
	    }
	    writer.flush();
	    writer.close();
	    */
	    // System.out.println("wrote output file");
	    sai = new SuffixArray.Integer(isequence, 127);
	    // System.out.println("created suffix array");
	    low = sai.findSubsequence(subsequence, false);
	    high = sai.findSubsequence(subsequence, true);
	    // System.out.println("configuring LCP and LCP-LR");
	    sai.useLCP();
	    sai.useLCPLR();
	    // System.out.println("using LCPLR");
	    int ourind = sai.findSubsequence(subsequence);
	    if ((ourind < low) || (ourind > high)) {
		System.out.format("ourind = %d not in [%d, %d]\n",
				  ourind, low, high);
		throw new Exception("findSubsequence failed");
	    }
	}

	System.out.println();
	System.out.println("SECOND RANDOM TESTS");
	System.out.println();

	lenrv = new UniformIntegerRV(8, 100);
	int[] subseq = {1,2,3,4};
	IntegerRandomVariable reprv = new UniformIntegerRV(0, 10);
	srv = new UniformIntegerRV(0,6);
	for (int i = 0; i < 100000; i++) {
	    if (i % 10000 == 0) System.out.println("i = " + i);
	    int len = lenrv.next();
	    int[] isequence = new int[len];

	    for (int j = 0; j < len; j++) {
		isequence[j] = srv.next();
	    }

	    int start = StaticRandom.nextInt(len - 1);
	    int rep = reprv.next();
	    for (int j = 0; j < 2*rep; j++) {
		if (start >= isequence.length) {
		    break;
		}
		isequence[start++] = subseq[j%2];
	    }
	    start = StaticRandom.nextInt(len - 1);
	    rep = reprv.next();
	    for (int j = 0; j < 3*rep; j++) {
		if (start >= isequence.length) {
		    break;
		}
		isequence[start++] = subseq[j%3];
	    }

	    start = StaticRandom.nextInt(len - 1);
	    rep = reprv.next();
	    for (int j = 0; j < 4*rep; j++) {
		if (start >= isequence.length) {
		    break;
		}
		isequence[start++] = subseq[j%4];
	    }
	    // log the test case in case something fails
	    /*
	    PrintWriter writer = new PrintWriter("sequence.txt", "UTF-8");
	    writer.println("sequence:");
	    writer.println();
	    writer.print(isequence[0]);
	    for (int j = 1; j < isequence.length; j++) {
		writer.print(", " + isequence[j]);
	    }
	    writer.println();
	    writer.println();
	    writer.println("subseq:");
	    writer.println();
	    writer.print(subseq[0]);
	    for (int j = 1; j < subseq.length; j++) {
		writer.print(", " + subseq[j]);
	    }
	    writer.flush();
	    writer.close();
	    */
	    // System.out.println("wrote output file");
	    sai = new SuffixArray.Integer(isequence, 6);
	    // System.out.println("created suffix array");
	    low = sai.findSubsequence(subseq, false);
	    high = sai.findSubsequence(subseq, true);
	    // System.out.println("configuring LCP and LCP-LR");
	    sai.useLCP();
	    sai.useLCPLR();
	    // System.out.println("using LCPLR");
	    int ourind = sai.findSubsequence(subseq);
	    if ((ourind < low) || (ourind > high)) {
		System.out.format("ourind = %d not in [%d, %d]\n",
				  ourind, low, high);
		throw new Exception("findSubsequence failed");
	    }
	}

	System.out.println("short-array tests");

	for (int i = 0; i < 100000; i++) {
	    if (i % 10000 == 0) System.out.println("i = " + i);
	    int len = lenrv.next();
	    short[] isequence = new short[len];
	    int sublen = sublenrv.next();
	    short[] subsequence = new short[sublen];
	    for (int j = 0; j < len; j++) {
		isequence[j] = (short)(int)srv.next();
	    }
	    for (int j = 0 ; j < sublen; j++) {
		subsequence[j] = (short)(int)srv.next();
	    }
	    int n = StaticRandom.nextInt(16);
	    for (int j = 0; j < n; j++) {
		int offset = StaticRandom.nextInt(len);
		int max = offset + sublen;
		int newlen = sublen;
		if (max > len) newlen -= (max - len);
		System.arraycopy(subsequence, 0, isequence, offset, newlen);
	    }
	    // log the test case in case something fails
	    /*
	    PrintWriter writer = new PrintWriter("sequence.txt", "UTF-8");
	    writer.println("sequence:");
	    writer.println();
	    writer.print(isequence[0]);
	    for (int j = 1; j < isequence.length; j++) {
		writer.print(", " + isequence[j]);
	    }
	    writer.println();
	    writer.println();
	    writer.println("subsequence:");
	    writer.println();
	    writer.print(subsequence[0]);
	    for (int j = 1; j < subsequence.length; j++) {
		writer.print(", " + subsequence[j]);
	    }
	    writer.flush();
	    writer.close();
	    */
	    // System.out.println("wrote output file");
	    SuffixArray.Short sas = new SuffixArray.Short(isequence, 127);
	    // System.out.println("created suffix array");
	    low = sas.findSubsequence(subsequence, false);
	    high = sas.findSubsequence(subsequence, true);
	    // System.out.println("configuring LCP and LCP-LR");
	    sas.useLCP();
	    sas.useLCPLR();
	    // System.out.println("using LCPLR");
	    int ourind = sas.findSubsequence(subsequence);
	    if ((ourind < low) || (ourind > high)) {
		System.out.format("ourind = %d not in [%d, %d]\n",
				  ourind, low, high);
		throw new Exception("findSubsequence failed");
	    }
	}

	System.out.println();
	System.out.println("SECOND RANDOM TESTS");
	System.out.println();

	lenrv = new UniformIntegerRV(8, 100);
	short[] ssubseq = {1,2,3,4};
	srv = new UniformIntegerRV(0,6);
	for (int i = 0; i < 100000; i++) {
	    if (i % 10000 == 0) System.out.println("i = " + i);
	    int len = lenrv.next();
	    short[] isequence = new short[len];

	    for (int j = 0; j < len; j++) {
		isequence[j] = (short)(int)srv.next();
	    }

	    int start = StaticRandom.nextInt(len - 1);
	    int rep = reprv.next();
	    for (int j = 0; j < 2*rep; j++) {
		if (start >= isequence.length) {
		    break;
		}
		isequence[start++] = ssubseq[j%2];
	    }
	    start = StaticRandom.nextInt(len - 1);
	    rep = reprv.next();
	    for (int j = 0; j < 3*rep; j++) {
		if (start >= isequence.length) {
		    break;
		}
		isequence[start++] = ssubseq[j%3];
	    }

	    start = StaticRandom.nextInt(len - 1);
	    rep = reprv.next();
	    for (int j = 0; j < 4*rep; j++) {
		if (start >= isequence.length) {
		    break;
		}
		isequence[start++] = ssubseq[j%4];
	    }
	    // log the test case in case something fails
	    /*
	    PrintWriter writer = new PrintWriter("sequence.txt", "UTF-8");
	    writer.println("sequence:");
	    writer.println();
	    writer.print(isequence[0]);
	    for (int j = 1; j < isequence.length; j++) {
		writer.print(", " + isequence[j]);
	    }
	    writer.println();
	    writer.println();
	    writer.println("subseq:");
	    writer.println();
	    writer.print(subseq[0]);
	    for (int j = 1; j < subseq.length; j++) {
		writer.print(", " + subseq[j]);
	    }
	    writer.flush();
	    writer.close();
	    */
	    // System.out.println("wrote output file");
	    SuffixArray.Short sas = new SuffixArray.Short(isequence, 6);
	    // System.out.println("created suffix array");
	    low = sas.findSubsequence(ssubseq, false);
	    high = sas.findSubsequence(ssubseq, true);
	    // System.out.println("configuring LCP and LCP-LR");
	    sas.useLCP();
	    sas.useLCPLR();
	    // System.out.println("using LCPLR");
	    int ourind = sas.findSubsequence(ssubseq);
	    if ((ourind < low) || (ourind > high)) {
		System.out.format("ourind = %d not in [%d, %d]\n",
				  ourind, low, high);
		throw new Exception("findSubsequence failed");
	    }
	}


	System.out.println("char-array tests");

	for (int i = 0; i < 100000; i++) {
	    if (i % 10000 == 0) System.out.println("i = " + i);
	    int len = lenrv.next();
	    char[] isequence = new char[len];
	    int sublen = sublenrv.next();
	    char[] subsequence = new char[sublen];
	    for (int j = 0; j < len; j++) {
		isequence[j] = (char)(int)srv.next();
	    }
	    for (int j = 0 ; j < sublen; j++) {
		subsequence[j] = (char)(int)srv.next();
	    }
	    int n = StaticRandom.nextInt(16);
	    for (int j = 0; j < n; j++) {
		int offset = StaticRandom.nextInt(len);
		int max = offset + sublen;
		int newlen = sublen;
		if (max > len) newlen -= (max - len);
		System.arraycopy(subsequence, 0, isequence, offset, newlen);
	    }
	    // log the test case in case something fails
	    /*
	    PrintWriter writer = new PrintWriter("sequence.txt", "UTF-8");
	    writer.println("sequence:");
	    writer.println();
	    writer.print(isequence[0]);
	    for (int j = 1; j < isequence.length; j++) {
		writer.print(", " + isequence[j]);
	    }
	    writer.println();
	    writer.println();
	    writer.println("subsequence:");
	    writer.println();
	    writer.print(subsequence[0]);
	    for (int j = 1; j < subsequence.length; j++) {
		writer.print(", " + subsequence[j]);
	    }
	    writer.flush();
	    writer.close();
	    */
	    // System.out.println("wrote output file");
	    SuffixArray.Char sac = new SuffixArray.Char(isequence, 127);
	    // System.out.println("created suffix array");
	    low = sac.findSubsequence(subsequence, false);
	    high = sac.findSubsequence(subsequence, true);
	    // System.out.println("configuring LCP and LCP-LR");
	    sac.useLCP();
	    sac.useLCPLR();
	    // System.out.println("using LCPLR");
	    int ourind = sac.findSubsequence(subsequence);
	    if ((ourind < low) || (ourind > high)) {
		System.out.format("ourind = %d not in [%d, %d]\n",
				  ourind, low, high);
		throw new Exception("findSubsequence failed");
	    }
	}

	System.out.println();
	System.out.println("SECOND RANDOM TESTS");
	System.out.println();

	lenrv = new UniformIntegerRV(8, 100);
	char[] csubseq = {1,2,3,4};
	srv = new UniformIntegerRV(0,6);
	for (int i = 0; i < 100000; i++) {
	    if (i % 10000 == 0) System.out.println("i = " + i);
	    int len = lenrv.next();
	    char[] isequence = new char[len];

	    for (int j = 0; j < len; j++) {
		isequence[j] = (char)(int)srv.next();
	    }

	    int start = StaticRandom.nextInt(len - 1);
	    int rep = reprv.next();
	    for (int j = 0; j < 2*rep; j++) {
		if (start >= isequence.length) {
		    break;
		}
		isequence[start++] = csubseq[j%2];
	    }
	    start = StaticRandom.nextInt(len - 1);
	    rep = reprv.next();
	    for (int j = 0; j < 3*rep; j++) {
		if (start >= isequence.length) {
		    break;
		}
		isequence[start++] = csubseq[j%3];
	    }

	    start = StaticRandom.nextInt(len - 1);
	    rep = reprv.next();
	    for (int j = 0; j < 4*rep; j++) {
		if (start >= isequence.length) {
		    break;
		}
		isequence[start++] = csubseq[j%4];
	    }
	    // log the test case in case something fails
	    /*
	    PrintWriter writer = new PrintWriter("sequence.txt", "UTF-8");
	    writer.println("sequence:");
	    writer.println();
	    writer.print(isequence[0]);
	    for (int j = 1; j < isequence.length; j++) {
		writer.print(", " + isequence[j]);
	    }
	    writer.println();
	    writer.println();
	    writer.println("subseq:");
	    writer.println();
	    writer.print(subseq[0]);
	    for (int j = 1; j < subseq.length; j++) {
		writer.print(", " + subseq[j]);
	    }
	    writer.flush();
	    writer.close();
	    */
	    // System.out.println("wrote output file");
	    SuffixArray.Char sac = new SuffixArray.Char(isequence, 6);
	    // System.out.println("created suffix array");
	    low = sac.findSubsequence(csubseq, false);
	    high = sac.findSubsequence(csubseq, true);
	    // System.out.println("configuring LCP and LCP-LR");
	    sac.useLCP();
	    sac.useLCPLR();
	    // System.out.println("using LCPLR");
	    int ourind = sac.findSubsequence(csubseq);
	    if ((ourind < low) || (ourind > high)) {
		System.out.format("ourind = %d not in [%d, %d]\n",
				  ourind, low, high);
		throw new Exception("findSubsequence failed");
	    }
	}


	System.out.println("byte-array tests");

	for (int i = 0; i < 100000; i++) {
	    if (i % 10000 == 0) System.out.println("i = " + i);
	    int len = lenrv.next();
	    byte[] isequence = new byte[len];
	    int sublen = sublenrv.next();
	    byte[] subsequence = new byte[sublen];
	    for (int j = 0; j < len; j++) {
		isequence[j] = (byte)(int)srv.next();
	    }
	    for (int j = 0 ; j < sublen; j++) {
		subsequence[j] = (byte)(int)srv.next();
	    }
	    int n = StaticRandom.nextInt(16);
	    for (int j = 0; j < n; j++) {
		int offset = StaticRandom.nextInt(len);
		int max = offset + sublen;
		int newlen = sublen;
		if (max > len) newlen -= (max - len);
		System.arraycopy(subsequence, 0, isequence, offset, newlen);
	    }
	    // log the test case in case something fails

	    PrintWriter writer = new PrintWriter("sequence.txt", "UTF-8");
	    writer.println("sequence:");
	    writer.println();
	    writer.print(isequence[0]);
	    for (int j = 1; j < isequence.length; j++) {
		writer.print(", " + isequence[j]);
	    }
	    writer.println();
	    writer.println();
	    writer.println("subsequence:");
	    writer.println();
	    writer.print(subsequence[0]);
	    for (int j = 1; j < subsequence.length; j++) {
		writer.print(", " + subsequence[j]);
	    }
	    writer.flush();
	    writer.close();

	    // System.out.println("wrote output file");
	    SuffixArray.Byte sab = new SuffixArray.Byte(isequence, 127);
	    // System.out.println("created suffix array");
	    low = sab.findSubsequence(subsequence, false);
	    high = sab.findSubsequence(subsequence, true);
	    // System.out.println("configuring LCP and LCP-LR");
	    sab.useLCP();
	    sab.useLCPLR();
	    // System.out.println("using LCPLR");
	    int ourind = sab.findSubsequence(subsequence);
	    if ((ourind < low) || (ourind > high)) {
		System.out.format("ourind = %d not in [%d, %d]\n",
				  ourind, low, high);
		throw new Exception("findSubsequence failed");
	    }
	}

	System.out.println();
	System.out.println("SECOND RANDOM TESTS");
	System.out.println();

	lenrv = new UniformIntegerRV(8, 100);
	byte[] bsubseq = {1,2,3,4};
	srv = new UniformIntegerRV(0,6);
	for (int i = 0; i < 100000; i++) {
	    if (i % 10000 == 0) System.out.println("i = " + i);
	    int len = lenrv.next();
	    byte[] isequence = new byte[len];

	    for (int j = 0; j < len; j++) {
		isequence[j] = (byte)(int)srv.next();
	    }

	    int start = StaticRandom.nextInt(len - 1);
	    int rep = reprv.next();
	    for (int j = 0; j < 2*rep; j++) {
		if (start >= isequence.length) {
		    break;
		}
		isequence[start++] = bsubseq[j%2];
	    }
	    start = StaticRandom.nextInt(len - 1);
	    rep = reprv.next();
	    for (int j = 0; j < 3*rep; j++) {
		if (start >= isequence.length) {
		    break;
		}
		isequence[start++] = bsubseq[j%3];
	    }

	    start = StaticRandom.nextInt(len - 1);
	    rep = reprv.next();
	    for (int j = 0; j < 4*rep; j++) {
		if (start >= isequence.length) {
		    break;
		}
		isequence[start++] = bsubseq[j%4];
	    }
	    // log the test case in case something fails
	    /*
	    PrintWriter writer = new PrintWriter("sequence.txt", "UTF-8");
	    writer.println("sequence:");
	    writer.println();
	    writer.print(isequence[0]);
	    for (int j = 1; j < isequence.length; j++) {
		writer.print(", " + isequence[j]);
	    }
	    writer.println();
	    writer.println();
	    writer.println("subseq:");
	    writer.println();
	    writer.print(subseq[0]);
	    for (int j = 1; j < subseq.length; j++) {
		writer.print(", " + subseq[j]);
	    }
	    writer.flush();
	    writer.close();
	    */
	    // System.out.println("wrote output file");
	    SuffixArray.Byte sab = new SuffixArray.Byte(isequence, 6);
	    // System.out.println("created suffix array");
	    low = sab.findSubsequence(bsubseq, false);
	    high = sab.findSubsequence(bsubseq, true);
	    // System.out.println("configuring LCP and LCP-LR");
	    sab.useLCP();
	    sab.useLCPLR();
	    // System.out.println("using LCPLR");
	    int ourind = sab.findSubsequence(bsubseq);
	    if ((ourind < low) || (ourind > high)) {
		System.out.format("ourind = %d not in [%d, %d]\n",
				  ourind, low, high);
		throw new Exception("findSubsequence failed");
	    }
	}


	System.out.println("String-array tests");

	for (int i = 0; i < 100000; i++) {
	    if (i % 10000 == 0) System.out.println("i = " + i);
	    int len = lenrv.next();
	    char[] isequence = new char[len];
	    int sublen = sublenrv.next();
	    char[] subsequence = new char[sublen];
	    for (int j = 0; j < len; j++) {
		isequence[j] = (char)(int)srv.next();
	    }
	    for (int j = 0 ; j < sublen; j++) {
		subsequence[j] = (char)(int)srv.next();
	    }
	    int n = StaticRandom.nextInt(16);
	    for (int j = 0; j < n; j++) {
		int offset = StaticRandom.nextInt(len);
		int max = offset + sublen;
		int newlen = sublen;
		if (max > len) newlen -= (max - len);
		System.arraycopy(subsequence, 0, isequence, offset, newlen);
	    }
	    // log the test case in case something fails
	    /*
	    PrintWriter writer = new PrintWriter("sequence.txt", "UTF-8");
	    writer.println("sequence:");
	    writer.println();
	    writer.print(isequence[0]);
	    for (int j = 1; j < isequence.length; j++) {
		writer.print(", " + isequence[j]);
	    }
	    writer.println();
	    writer.println();
	    writer.println("subsequence:");
	    writer.println();
	    writer.print(subsequence[0]);
	    for (int j = 1; j < subsequence.length; j++) {
		writer.print(", " + subsequence[j]);
	    }
	    writer.flush();
	    writer.close();
	    */
	    // System.out.println("wrote output file");
	    SuffixArray.String sacs =
		new SuffixArray.String(new String(isequence), 127);
	    // System.out.println("created suffix array");
	    low = sacs.findSubsequence(new String(subsequence), false);
	    high = sacs.findSubsequence(new String(subsequence), true);
	    // System.out.println("configuring LCP and LCP-LR");
	    sacs.useLCP();
	    sacs.useLCPLR();
	    // System.out.println("using LCPLR");
	    int ourind = sacs.findSubsequence(new String(subsequence));
	    if ((ourind < low) || (ourind > high)) {
		System.out.format("ourind = %d not in [%d, %d]\n",
				  ourind, low, high);
		throw new Exception("findSubsequence failed");
	    }
	}

	System.out.println();
	System.out.println("SECOND RANDOM TESTS");
	System.out.println();

	lenrv = new UniformIntegerRV(8, 100);
	char[] scsubseq = {1,2,3,4};
	srv = new UniformIntegerRV(0,6);
	for (int i = 0; i < 100000; i++) {
	    if (i % 10000 == 0) System.out.println("i = " + i);
	    int len = lenrv.next();
	    char[] isequence = new char[len];

	    for (int j = 0; j < len; j++) {
		isequence[j] = (char)(int)srv.next();
	    }

	    int start = StaticRandom.nextInt(len - 1);
	    int rep = reprv.next();
	    for (int j = 0; j < 2*rep; j++) {
		if (start >= isequence.length) {
		    break;
		}
		isequence[start++] = scsubseq[j%2];
	    }
	    start = StaticRandom.nextInt(len - 1);
	    rep = reprv.next();
	    for (int j = 0; j < 3*rep; j++) {
		if (start >= isequence.length) {
		    break;
		}
		isequence[start++] = scsubseq[j%3];
	    }

	    start = StaticRandom.nextInt(len - 1);
	    rep = reprv.next();
	    for (int j = 0; j < 4*rep; j++) {
		if (start >= isequence.length) {
		    break;
		}
		isequence[start++] = scsubseq[j%4];
	    }
	    // System.out.println("wrote output file");
	    SuffixArray.String sasc =
		new SuffixArray.String(new String(isequence), 6);
	    // System.out.println("created suffix array");
	    low = sasc.findSubsequence(new String(scsubseq), false);
	    high = sasc.findSubsequence(new String(scsubseq), true);
	    // System.out.println("configuring LCP and LCP-LR");
	    sasc.useLCP();
	    sasc.useLCPLR();
	    // System.out.println("using LCPLR");
	    int ourind = sasc.findSubsequence(new String(scsubseq));
	    if ((ourind < low) || (ourind > high)) {
		System.out.format("ourind = %d not in [%d, %d]\n",
				  ourind, low, high);
		throw new Exception("findSubsequence failed");
	    }
	}

	System.out.println();
	System.out.println("object tests");
	System.out.println();
			   
	Set<Integer> alphabet = new LinkedHashSet<>();
	for (int i = 0; i < 128; i++) {
	    alphabet.add(i);
	}

	for (int i = 0; i < 100000; i++) {
	    if (i % 10000 == 0) System.out.println("i = " + i);
	    int len = lenrv.next();
	    int[] isequence = new int[len];
	    int sublen = sublenrv.next();
	    int[] subsequence = new int[sublen];
	    for (int j = 0; j < len; j++) {
		isequence[j] = srv.next();
	    }
	    for (int j = 0 ; j < sublen; j++) {
		subsequence[j] = srv.next();
	    }
	    int n = StaticRandom.nextInt(16);
	    for (int j = 0; j < n; j++) {
		int offset = StaticRandom.nextInt(len);
		int max = offset + sublen;
		int newlen = sublen;
		if (max > len) newlen -= (max - len);
		System.arraycopy(subsequence, 0, isequence, offset, newlen);
	    }
	    // log the test case in case something fails
	    SuffixArray.Array<Integer> saI =
		new SuffixArray.Array<Integer>(ia(isequence), alphabet);
	    // System.out.println("created suffix array");
	    low = saI.findSubsequence(ia(subsequence), false);
	    high = saI.findSubsequence(ia(subsequence), true);
	    // System.out.println("configuring LCP and LCP-LR");
	    saI.useLCP();
	    saI.useLCPLR();
	    // System.out.println("using LCPLR");
	    int ourind = saI.findSubsequence(ia(subsequence));
	    if ((ourind < low) || (ourind > high)) {
		System.out.format("ourind = %d not in [%d, %d]\n",
				  ourind, low, high);
		throw new Exception("findSubsequence failed");
	    }
	}

	System.out.println();
	System.out.println("SECOND RANDOM TESTS");
	System.out.println();
	for (int i = 0; i < 100000; i++) {
	    if (i % 10000 == 0) System.out.println("i = " + i);
	    int len = lenrv.next();
	    int[] isequence = new int[len];

	    for (int j = 0; j < len; j++) {
		isequence[j] = srv.next();
	    }

	    int start = StaticRandom.nextInt(len - 1);
	    int rep = reprv.next();
	    for (int j = 0; j < 2*rep; j++) {
		if (start >= isequence.length) {
		    break;
		}
		isequence[start++] = subseq[j%2];
	    }
	    start = StaticRandom.nextInt(len - 1);
	    rep = reprv.next();
	    for (int j = 0; j < 3*rep; j++) {
		if (start >= isequence.length) {
		    break;
		}
		isequence[start++] = subseq[j%3];
	    }

	    start = StaticRandom.nextInt(len - 1);
	    rep = reprv.next();
	    for (int j = 0; j < 4*rep; j++) {
		if (start >= isequence.length) {
		    break;
		}
		isequence[start++] = subseq[j%4];
	    }
	    // log the test case in case something fails
	    // System.out.println("wrote output file");
	    SuffixArray.Array<Integer> saI =
		new SuffixArray.Array<Integer>(ia(isequence), alphabet);
	    // System.out.println("created suffix array");
	    low = saI.findSubsequence(ia(subseq), false);
	    high = saI.findSubsequence(ia(subseq), true);
	    // System.out.println("configuring LCP and LCP-LR");
	    saI.useLCP();
	    saI.useLCPLR();
	    // System.out.println("using LCPLR");
	    int ourind = saI.findSubsequence(ia(subseq));
	    if ((ourind < low) || (ourind > high)) {
		System.out.format("ourind = %d not in [%d, %d]\n",
				  ourind, low, high);
		throw new Exception("findSubsequence failed");
	    }
	}

	System.exit(0);
    }
}

