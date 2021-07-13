import org.bzdev.util.*;
import org.bzdev.math.StaticRandom;
import org.bzdev.math.rv.*;

public class UnsignedSATest {

    public static void main(String argv[]) throws Exception {
	IntegerRandomVariable lenrv = new UniformIntegerRV(16, 512);
	IntegerRandomVariable srv = new UniformIntegerRV(127-8, 127+8);
	IntegerRandomVariable sublenrv = new UniformIntegerRV(1, 10);

	int low;
	int high;

	System.out.println("unsigned-byte-array tests");

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
	    SuffixArray.UnsignedByte sab =
		new SuffixArray.UnsignedByte(isequence, 140);
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

	IntegerRandomVariable reprv = new UniformIntegerRV(0, 10);

	lenrv = new UniformIntegerRV(8, 20);
	byte[] bsubseq = {(byte)126,(byte)127,(byte)128,(byte)129};
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
	    SuffixArray.UnsignedByte sab =
		new SuffixArray.UnsignedByte(isequence, 140);
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

	System.out.println();
 	System.out.println("unsigned-short-array tests");
	System.out.println();

	srv = new UniformIntegerRV((1<<15) - 10, (1<<15) + 10);
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
	    SuffixArray.UnsignedShort sab =
		new SuffixArray.UnsignedShort(isequence, 140);
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

	short[] ssubseq = {(short)126,(short)127,(short)128,(short)129};
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
	    SuffixArray.UnsignedShort sab =
		new SuffixArray.UnsignedShort(isequence, (1<<15)+30);
	    // System.out.println("created suffix array");
	    low = sab.findSubsequence(ssubseq, false);
	    high = sab.findSubsequence(ssubseq, true);
	    // System.out.println("configuring LCP and LCP-LR");
	    sab.useLCP();
	    sab.useLCPLR();
	    // System.out.println("using LCPLR");
	    int ourind = sab.findSubsequence(ssubseq);
	    if ((ourind < low) || (ourind > high)) {
		System.out.format("ourind = %d not in [%d, %d]\n",
				  ourind, low, high);
		throw new Exception("findSubsequence failed");
	    }
	}
	System.out.println();
	System.out.println("UTF test");
	System.out.println();

	String table[] = { "a", "b", "c", "d", "\u0394", "\u03D5",
			   "\u3457", "\u5c70"};
	byte[][] bytes = new byte[table.length][];
	for (int i = 0; i < bytes.length; i++) {
	    bytes[i] = table[i].getBytes("UTF-8");
	}

	String subseq = "a\u0394\u5c70b";

	byte[] uarray = new byte[600];
	srv = new UniformIntegerRV((int)'a', (int)'z' + 1);
	for (int i = 0; i < uarray.length; i++) {
	    uarray[i] = (byte)(int)srv.next();
	}
	srv = new UniformIntegerRV(0, table.length);
	for (int i = 0; i < uarray.length; i += 6) {
	    if (i < 6*50 && i > 8*50) {
		byte[] tbytes = table[srv.next()].getBytes("UFT-8");;
		System.arraycopy(tbytes, 0, uarray, i, tbytes.length);
	    }
	}
	byte[] target = subseq.getBytes("UTF-8");
	System.arraycopy(target, 0, uarray, 300, target.length);

	SuffixArray.UTF sau = new SuffixArray.UTF(uarray);
	System.out.println("sau.findSubsequence(subseq) = "
			   + sau.findSubsequence(subseq));

	System.out.println("sau.findInstance(subseq) = "
			   + sau.findInstance(subseq));
	System.out.println("range length = "
			   + sau.findRange(subseq).size());
   }
}
