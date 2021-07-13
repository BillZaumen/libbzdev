import org.bzdev.util.CopyUtilities;
import java.util.ArrayList;

public class CopyTest {

    public static void main (String argv[]) throws Exception {
	ArrayList<Double> dlist = new ArrayList<>();
	dlist.add(1.0);
	dlist.add(2.0);
	dlist.add(3.0);
	dlist.add(4.0);
	dlist.add(5.0);
	double[] darray = CopyUtilities.toDoubleArray(dlist);
	for (double d: darray) {
	    System.out.println(d);
	}
	System.out.println("--------");
	darray = CopyUtilities.toDoubleArray(dlist, 1, 3);
	for (double d: darray) {
	    System.out.println(d);
	}
	System.out.println("-------------");
	ArrayList<Float> flist = new ArrayList<>();
	flist.add(1.0F);
	flist.add(2.0F);
	flist.add(3.0F);
	flist.add(4.0F);
	flist.add(5.0F);
	float[] farray = CopyUtilities.toFloatArray(flist);
	for (float f: farray) {
	    System.out.println(f);
	}
	System.out.println("--------");
	farray = CopyUtilities.toFloatArray(flist, 1, 3);
	for (double f: farray) {
	    System.out.println(f);
	}
	System.out.println("-------------");
	ArrayList<Integer> ilist = new ArrayList<>();
	ilist.add(1);
	ilist.add(2);
	ilist.add(3);
	ilist.add(4);
	ilist.add(5);
	int[] iarray = CopyUtilities.toIntArray(ilist);
	for (int i: iarray) {
	    System.out.println(i);
	}
	System.out.println("--------");
	iarray = CopyUtilities.toIntArray(ilist, 1, 3);
	for (int i: iarray) {
	    System.out.println(i);
	}

	System.out.println("-------------");
	ArrayList<Long> jlist = new ArrayList<>();
	jlist.add(1L);
	jlist.add(2L);
	jlist.add(3L);
	jlist.add(4L);
	jlist.add(5L);
	long[] jarray = CopyUtilities.toLongArray(jlist);
	for (long j: jarray) {
	    System.out.println(j);
	}
	System.out.println("--------");
	jarray = CopyUtilities.toLongArray(jlist, 1, 3);
	for (long j: jarray) {
	    System.out.println(j);
	}

	System.out.println("-------------");
	ArrayList<Byte> blist = new ArrayList<>();
	blist.add((byte)1);
	blist.add((byte)2);
	blist.add((byte)3);
	blist.add((byte)4);
	blist.add((byte)5);
	byte[] barray = CopyUtilities.toByteArray(blist);
	for (byte b: barray) {
	    System.out.println(b);
	}
	System.out.println("--------");
	barray = CopyUtilities.toByteArray(blist, 1, 3);
	for (byte b: barray) {
	    System.out.println(b);
	}

	System.out.println("-------------");
	ArrayList<Character> clist = new ArrayList<>();
	clist.add('a');
	clist.add('b');
	clist.add('c');
	clist.add('d');
	clist.add('e');
	char[] carray = CopyUtilities.toCharArray(clist);
	for (char c: carray) {
	    System.out.println(c);
	}
	System.out.println("--------");
	carray = CopyUtilities.toCharArray(clist, 1, 3);
	for (char c: carray) {
	    System.out.println(c);
	}

	System.out.println("-------------");
	ArrayList<Boolean> zlist = new ArrayList<>();
	zlist.add(true);
	zlist.add(false);
	zlist.add(true);
	zlist.add(false);
	zlist.add(true);
	boolean[] zarray = CopyUtilities.toBooleanArray(zlist);
	for (boolean z: zarray) {
	    System.out.println(z);
	}
	System.out.println("--------");
	zarray = CopyUtilities.toBooleanArray(zlist, 1, 3);
	for (boolean z: zarray) {
	    System.out.println(z);
	}
	System.exit(0);
    }
}
