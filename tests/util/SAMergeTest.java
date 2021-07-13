import org.bzdev.util.*;
import java.util.*;
import org.bzdev.lang.UnexpectedExceptionError;
import org.bzdev.math.rv.UniformIntegerRV;
import org.bzdev.math.*;
import java.io.IOException;

public class SAMergeTest {

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
			out.append("i = " + i + "\n");
			out.append("index1 = " + index1);
			out.append(", index2 = " + index2);
			out.append(", " + sequence[index1+k] + " > "
				   + sequence[index2+k]);
			out.append("\n");
		    }
		    return false;
		} else if (sequence[index1+k] < sequence[index2+k]) {
		    break;
		}
	    }
	}
	return true;
    }

    public static void main(String argv[]) throws Exception {
	
	UniformIntegerRV irv = new UniformIntegerRV(0, true, 10, false);
	UniformIntegerRV lenrv = new UniformIntegerRV(1, true, 1024, true);

	System.out.println("Test saving and restoring a suffix array");

	int[] ifsequence;
	System.out.println("testing SuffixArray.Integer");
	for (int icase = 0; icase < 100000; icase++) {
	    ifsequence = new int [lenrv.next()];
	    for (int i = 0; i < ifsequence.length; i++) {
		ifsequence[i] = irv.next();
	    }
	    SuffixArray.Integer sa1 = new SuffixArray.Integer(ifsequence, 10);
	    int[] array1 = sa1.getArray();
	    SuffixArray.Integer sa2 = new
		SuffixArray.Integer(ifsequence, array1);
	    int[] array2 = sa2.getArray();
	    if (array1.length != array2.length) {
		throw new Exception ("array lengths");
	    }
	    for (int i = 0; i < array1.length; i++) {
		if (array1[i] != array2[i]) {
		    throw new Exception("arrays not equal");
		}
	    }
	}

	short[] sfsequence;
	System.out.println("testing SuffixArray.Short");
	for (int icase = 0; icase < 100000; icase++) {
	    sfsequence = new short [lenrv.next()];
	    for (int i = 0; i < sfsequence.length; i++) {
		sfsequence[i] = (short)(int)irv.next();
	    }
	    SuffixArray.Short sa1 = new SuffixArray.Short(sfsequence, 10);
	    int[] array1 = sa1.getArray();
	    SuffixArray.Short sa2 = new SuffixArray.Short(sfsequence, array1);
	    int[] array2 = sa2.getArray();
	    if (array1.length != array2.length) {
		throw new Exception ("array lengths");
	    }
	    for (int i = 0; i < array1.length; i++) {
		if (array1[i] != array2[i]) {
		    throw new Exception("arrays not equal");
		}
	    }
	}

	byte[] bfsequence;
	System.out.println("testing SuffixArray.Byte");
	for (int icase = 0; icase < 100000; icase++) {
	    bfsequence = new byte [lenrv.next()];
	    for (int i = 0; i < bfsequence.length; i++) {
		bfsequence[i] = (byte)(int)irv.next();
	    }
	    SuffixArray.Byte sa1 = new SuffixArray.Byte(bfsequence, 10);
	    int[] array1 = sa1.getArray();
	    SuffixArray.Byte sa2 = new SuffixArray.Byte(bfsequence, array1);
	    int[] array2 = sa2.getArray();
	    if (array1.length != array2.length) {
		throw new Exception ("array lengths");
	    }
	    for (int i = 0; i < array1.length; i++) {
		if (array1[i] != array2[i]) {
		    throw new Exception("arrays not equal");
		}
	    }
	}

	byte[] ubfsequence;
	System.out.println("testing SuffixArray.UnsignedByte");
	for (int icase = 0; icase < 100000; icase++) {
	    ubfsequence = new byte [lenrv.next()];
	    for (int i = 0; i < ubfsequence.length; i++) {
		ubfsequence[i] = (byte)(int)irv.next();
	    }
	    SuffixArray.UnsignedByte sa1 = new
		SuffixArray.UnsignedByte(ubfsequence, 10);
	    int[] array1 = sa1.getArray();
	    SuffixArray.UnsignedByte sa2 = new
		SuffixArray.UnsignedByte(ubfsequence, array1);
	    int[] array2 = sa2.getArray();
	    if (array1.length != array2.length) {
		throw new Exception ("array lengths");
	    }
	    for (int i = 0; i < array1.length; i++) {
		if (array1[i] != array2[i]) {
		    throw new Exception("arrays not equal");
		}
	    }
	}

	byte[] utfsequence;
	System.out.println("testing SuffixArray.UTF");
	for (int icase = 0; icase < 100000; icase++) {
	    utfsequence = new byte [lenrv.next()];
	    for (int i = 0; i < utfsequence.length; i++) {
		utfsequence[i] = (byte)(int)irv.next();
	    }
	    SuffixArray.UTF sa1 = new SuffixArray.UTF(utfsequence);
	    int[] array1 = sa1.getArray();
	    SuffixArray.UTF sa2 = new SuffixArray.UTF(utfsequence, array1);
	    int[] array2 = sa2.getArray();
	    if (array1.length != array2.length) {
		throw new Exception ("array lengths");
	    }
	    for (int i = 0; i < array1.length; i++) {
		if (array1[i] != array2[i]) {
		    throw new Exception("arrays not equal");
		}
	    }
	}

	char[] cfsequence;
	System.out.println("testing SuffixArray.Char");
	for (int icase = 0; icase < 100000; icase++) {
	    cfsequence = new char [lenrv.next()];
	    for (int i = 0; i < cfsequence.length; i++) {
		cfsequence[i] = (char)(int)irv.next();
	    }
	    SuffixArray.Char sa1 = new SuffixArray.Char(cfsequence, 10);
	    int[] array1 = sa1.getArray();
	    SuffixArray.Char sa2 = new SuffixArray.Char(cfsequence, array1);
	    int[] array2 = sa2.getArray();
	    if (array1.length != array2.length) {
		throw new Exception ("array lengths");
	    }
	    for (int i = 0; i < array1.length; i++) {
		if (array1[i] != array2[i]) {
		    throw new Exception("arrays not equal");
		}
	    }
	}

    }
}
