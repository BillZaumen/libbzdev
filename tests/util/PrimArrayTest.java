import org.bzdev.util.PrimArrays;
import org.bzdev.util.IntComparator;
import org.bzdev.util.ByteComparator;
import org.bzdev.util.ShortComparator;
import org.bzdev.util.CharComparator;
import org.bzdev.util.LongComparator;
import org.bzdev.util.DoubleComparator;
import org.bzdev.util.FloatComparator;
import org.bzdev.math.rv.UniformIntegerRV;

import java.util.*;

public class PrimArrayTest {

    /*
     * Reverse order, so we can make sure it worked as it
     * is supposed to.
     */
    static IntComparator ic = new IntComparator() {
	    public int compare(int i1, int i2) {
		if (i1 > i2) return -1;
		if (i1 < i2) return 1;
		else return 0;
	    }
	};
    static ByteComparator bc = new ByteComparator() {
	    public int compare(byte i1, byte i2) {
		if (i1 > i2) return -1;
		if (i1 < i2) return 1;
		else return 0;
	    }
	};
    static CharComparator cc = new CharComparator() {
	    public int compare(char i1, char i2) {
		if (i1 > i2) return -1;
		if (i1 < i2) return 1;
		else return 0;
	    }
	};
    static ShortComparator sc = new ShortComparator() {
	    public int compare(short i1, short i2) {
		if (i1 > i2) return -1;
		if (i1 < i2) return 1;
		else return 0;
	    }
	};
    static LongComparator lc = new LongComparator() {
	    public int compare(long i1, long i2) {
		if (i1 > i2) return -1;
		if (i1 < i2) return 1;
		else return 0;
	    }
	};

    static FloatComparator fc = new FloatComparator() {
	    public int compare(float i1, float i2) {
		if (i1 > i2) return -1;
		if (i1 < i2) return 1;
		else return 0;
	    }
	};

    static DoubleComparator dc = new DoubleComparator() {
	    public int compare(double i1, double i2) {
		if (i1 > i2) return -1;
		if (i1 < i2) return 1;
		else return 0;
	    }
	};

    public static void main(String argv[]) throws Exception {

	int values[] = {1, 20, 3, 30, 4, 5, 6, 7};
	int array[] = values.clone();
	System.out.println("array.length = " + array.length);
	PrimArrays.sort(array, ic);
	for (int i: array) {
	    System.out.print(i + " ");
	}
	System.out.println();

	array = values.clone();
	System.out.println("array.length = " + array.length);
	PrimArrays.sort(array, 0, 4, ic);
	for (int i: array) {
	    System.out.print(i + " ");
	}
	System.out.println();

	PrimArrays.sort(array, ic);
	for (int i = 0; i < array.length; i++) {
	    System.out.println(i + " "
			       + PrimArrays.binarySearch(array, array[i], ic));
	}
	int[] keys = values.clone();
	PrimArrays.sort(keys, ic);
	array = new int[values.length*2];
	System.arraycopy(values, 0, array, 0, values.length);
	System.arraycopy(values, 0, array, values.length, values.length);
	PrimArrays.sort(array, ic);

	System.out.println("keyflag = false");
	for (int key: keys) {
	    int index = PrimArrays.binarySearch(array, 0, array.length,
					       key, ic, false);
	    System.out.format("key %d: array[%d] = %d\n", key,
			      index, array[index]);
	}
	System.out.println("keyflag = true");
	for (int key: keys) {
	    int index = PrimArrays.binarySearch(array, 0, array.length,
					       key, ic, true);
	    System.out.format("key %d: array[%d] = %d\n", key,
			      index, array[index]);
	}
	
	UniformIntegerRV run = new UniformIntegerRV(1, 10);
	System.out.println("trying a large number of binary searches");
	for (int c = 0; c < 500; c++) {
	    if (c % 100 == 0) System.out.format("c = %d\n", c);
	    for (int n = 1; n < 1000; n++) {
		array = new int[n];
		int m = 0;
		int mm = 0;
		int i = 1;
		while (m < n) {
		    m += run.next();
		    if (m > n) m = n;
		    while (mm < m) {
			array[mm++] = i;
		    }
		    i++;
		}
		PrimArrays.sort(array, ic);
		for (int key: array) {
		    int index1 = PrimArrays.binarySearch(array, 0, array.length,
							key, ic, false);
		    int index2 = PrimArrays.binarySearch(array, 0, array.length,
							key, ic, true);
		    if (index2 < index1) {
			throw new Exception ("bad index order");
		    }
		    if (array[index1] != key) {
			throw new Exception("array[index1] != key");
		    }
		    if (array[index2] != key) {
			throw new Exception("array[index2] != key");
		    }
		    index1--;
		    index2++;
		    if (index1 > 0 && array[index1] == key) {
			throw new Exception("array[index1] != key");
		    }
		    if (index2 < n && array[index2] == key) {
			throw new Exception("array[index2] != key");
		    }
		}
	    }
	}

	System.out.println("*** byte case");
	byte bvalues[] = {1, 20, 3, 30, 4, 5, 6, 7};
	byte barray[] = bvalues.clone();
	System.out.println("barray.length = " + barray.length);
	PrimArrays.sort(barray, bc);
	for (byte i: barray) {
	    System.out.print(i + " ");
	}
	System.out.println();

	barray = bvalues.clone();
	System.out.println("barray.length = " + barray.length);
	PrimArrays.sort(barray, 0, 4, bc);
	for (byte i: barray) {
	    System.out.print(i + " ");
	}
	System.out.println();

	PrimArrays.sort(barray, bc);
	for (int i = 0; i < barray.length; i++) {
	    System.out.println(i + " "
			       + PrimArrays.binarySearch(barray, barray[i],
							 bc));
	}
	System.out.println("key = 35: index = " +
			   PrimArrays.binarySearch(barray, (byte)35, bc));
	System.out.println("key = 25: index = " +
			   PrimArrays.binarySearch(barray, (byte)25, bc));
	System.out.println("key = 0: index = " +
			   PrimArrays.binarySearch(barray, (byte)0, bc));

	byte[] bkeys = bvalues.clone();
	PrimArrays.sort(bkeys, bc);
	barray = new byte[bvalues.length*2];
	System.arraycopy(bvalues, 0, barray, 0, bvalues.length);
	System.arraycopy(bvalues, 0, barray, bvalues.length, bvalues.length);
	PrimArrays.sort(barray, bc);

	System.out.println("keyflag = false");
	for (byte key: bkeys) {
	    int index = PrimArrays.binarySearch(barray, 0, barray.length,
					       key, bc, false);
	    System.out.format("key %d: barray[%d] = %d\n", key,
			      index, barray[index]);
	}
	System.out.println("keyflag = true");
	for (byte key: bkeys) {
	    int index = PrimArrays.binarySearch(barray, 0, barray.length,
					       key, bc, true);
	    System.out.format("key %d: barray[%d] = %d\n", key,
			      index, barray[index]);
	}

	System.out.println("trying a large number of binary searches");
	for (int c = 0; c < 500; c++) {
	    if (c % 100 == 0) System.out.format("c = %d\n", c);
	    for (int n = 1; n < 1000; n++) {
		barray = new byte[n];
		int m = 0;
		int mm = 0;
		int i = 1;
		while (m < n) {
		    m += (run.next());
		    if (m > n) m = n;
		    while (mm < m) {
			barray[mm++] = (byte)(i);
		    }
		    i++;
		}
		PrimArrays.sort(barray, bc);
		for (byte key: barray) {
		    int index1 = PrimArrays.binarySearch(barray, 0,
							 barray.length,
							 key, bc, false);
		    int index2 = PrimArrays.binarySearch(barray, 0,
							 barray.length,
							 key, bc, true);
		    if (index2 < index1) {
			throw new Exception ("bad index order");
		    }
		    if (barray[index1] != key) {
			throw new Exception("barray[index1] != key");
		    }
		    if (barray[index2] != key) {
			throw new Exception("barray[index2] != key");
		    }
		    index1--;
		    index2++;
		    if (index1 > 0 && barray[index1] == key) {
			throw new Exception("barray[index1] != key");
		    }
		    if (index2 < n && barray[index2] == key) {
			throw new Exception("barray[index2] != key");
		    }
		}
	    }
	}

	System.out.println("*** char case");
	char cvalues[] = {1, 20, 3, 30, 4, 5, 6, 7};
	char carray[] = cvalues.clone();
	System.out.println("carray.length = " + carray.length);
	PrimArrays.sort(carray, cc);
	for (char i: carray) {
	    System.out.print((int)i + " ");
	}
	System.out.println();

	carray = cvalues.clone();
	System.out.println("carray.length = " + carray.length);
	PrimArrays.sort(carray, 0, 4, cc);
	for (char i: carray) {
	    System.out.print((int)i + " ");
	}
	System.out.println();

	PrimArrays.sort(carray, cc);
	for (int i = 0; i < carray.length; i++) {
	    System.out.println((int)i + " "
			       + PrimArrays.binarySearch(carray, carray[i],
							 cc));
	}
	char[] ckeys = cvalues.clone();
	PrimArrays.sort(ckeys, cc);
	carray = new char[cvalues.length*2];
	System.arraycopy(cvalues, 0, carray, 0, cvalues.length);
	System.arraycopy(cvalues, 0, carray, cvalues.length, cvalues.length);
	PrimArrays.sort(carray, cc);

	System.out.println("keyflag = false");
	for (char key: ckeys) {
	    int index = PrimArrays.binarySearch(carray, 0, carray.length,
					       key, cc, false);
	    System.out.format("key %d: carray[%d] = %d\n", (int)key,
			      index, (int)carray[index]);
	}
	System.out.println("keyflag = true");
	for (char key: ckeys) {
	    int index = PrimArrays.binarySearch(carray, 0, carray.length,
					       key, cc, true);
	    System.out.format("key %d: carray[%d] = %d\n", (int)key,
			      index, (int)carray[index]);
	}

	System.out.println("trying a large number of binary searches");
	for (int c = 0; c < 500; c++) {
	    if (c % 100 == 0) System.out.format("c = %d\n", c);
	    for (int n = 1; n < 1000; n++) {
		carray = new char[n];
		int m = 0;
		int mm = 0;
		int i = 1;
		while (m < n) {
		    m += (run.next());
		    if (m > n) m = n;
		    while (mm < m) {
			carray[mm++] = (char)(i);
		    }
		    i++;
		}
		PrimArrays.sort(carray, cc);
		for (char key: carray) {
		    int index1 = PrimArrays.binarySearch(carray, 0,
							 carray.length,
							 key, cc, false);
		    int index2 = PrimArrays.binarySearch(carray, 0,
							 carray.length,
							 key, cc, true);
		    if (index2 < index1) {
			throw new Exception ("bad index order");
		    }
		    if (carray[index1] != key) {
			throw new Exception("carray[index1] != key");
		    }
		    if (carray[index2] != key) {
			throw new Exception("carray[index2] != key");
		    }
		    index1--;
		    index2++;
		    if (index1 > 0 && carray[index1] == key) {
			throw new Exception("carray[index1] != key");
		    }
		    if (index2 < n && carray[index2] == key) {
			throw new Exception("carray[index2] != key");
		    }
		}
	    }
	}
	System.out.println("*** short case");
	short svalues[] = {1, 20, 3, 30, 4, 5, 6, 7};
	short sarray[] = svalues.clone();
	System.out.println("sarray.length = " + sarray.length);
	PrimArrays.sort(sarray, sc);
	for (short i: sarray) {
	    System.out.print(i + " ");
	}
	System.out.println();

	sarray = svalues.clone();
	System.out.println("sarray.length = " + sarray.length);
	PrimArrays.sort(sarray, 0, 4, sc);
	for (short i: sarray) {
	    System.out.print(i + " ");
	}
	System.out.println();

	PrimArrays.sort(sarray, sc);
	for (int i = 0; i < sarray.length; i++) {
	    System.out.println(i + " "
			       + PrimArrays.binarySearch(sarray, sarray[i],
							 sc));
	}
	short[] skeys = svalues.clone();
	PrimArrays.sort(skeys, sc);
	sarray = new short[svalues.length*2];
	System.arraycopy(svalues, 0, sarray, 0, svalues.length);
	System.arraycopy(svalues, 0, sarray, svalues.length, svalues.length);
	PrimArrays.sort(sarray, sc);

	System.out.println("keyflag = false");
	for (short key: skeys) {
	    int index = PrimArrays.binarySearch(sarray, 0, sarray.length,
					       key, sc, false);
	    System.out.format("key %d: sarray[%d] = %d\n", key,
			      index, sarray[index]);
	}
	System.out.println("keyflag = true");
	for (short key: skeys) {
	    int index = PrimArrays.binarySearch(sarray, 0, sarray.length,
					       key, sc, true);
	    System.out.format("key %d: sarray[%d] = %d\n", key,
			      index, sarray[index]);
	}

	System.out.println("trying a large number of binary searches");
	for (int c = 0; c < 500; c++) {
	    if (c % 100 == 0) System.out.format("c = %d\n", c);
	    for (int n = 1; n < 1000; n++) {
		sarray = new short[n];
		int m = 0;
		int mm = 0;
		int i = 1;
		while (m < n) {
		    m += (run.next());
		    if (m > n) m = n;
		    while (mm < m) {
			sarray[mm++] = (short)(i);
		    }
		    i++;
		}
		PrimArrays.sort(sarray, sc);
		for (short key: sarray) {
		    int index1 = PrimArrays.binarySearch(sarray, 0,
							 sarray.length,
							 key, sc, false);
		    int index2 = PrimArrays.binarySearch(sarray, 0,
							 sarray.length,
							 key, sc, true);
		    if (index2 < index1) {
			throw new Exception ("bad index order");
		    }
		    if (sarray[index1] != key) {
			throw new Exception("sarray[index1] != key");
		    }
		    if (sarray[index2] != key) {
			throw new Exception("sarray[index2] != key");
		    }
		    index1--;
		    index2++;
		    if (index1 > 0 && sarray[index1] == key) {
			throw new Exception("sarray[index1] != key");
		    }
		    if (index2 < n && sarray[index2] == key) {
			throw new Exception("sarray[index2] != key");
		    }
		}
	    }
	}

	System.out.println("*** long case");
	long lvalues[] = {1, 20, 3, 30, 4, 5, 6, 7};
	long larray[] = lvalues.clone();
	System.out.println("larray.length = " + larray.length);
	PrimArrays.sort(larray, lc);
	for (long i: larray) {
	    System.out.print(i + " ");
	}
	System.out.println();

	larray = lvalues.clone();
	System.out.println("larray.length = " + larray.length);
	PrimArrays.sort(larray, 0, 4, lc);
	for (long i: larray) {
	    System.out.print(i + " ");
	}
	System.out.println();

	PrimArrays.sort(larray, lc);
	for (int i = 0; i < larray.length; i++) {
	    System.out.println(i + " "
			       + PrimArrays.binarySearch(larray, larray[i],
							 lc));
	}
	long[] lkeys = lvalues.clone();
	PrimArrays.sort(lkeys, lc);
	larray = new long[lvalues.length*2];
	System.arraycopy(lvalues, 0, larray, 0, lvalues.length);
	System.arraycopy(lvalues, 0, larray, lvalues.length, lvalues.length);
	PrimArrays.sort(larray, lc);

	System.out.println("keyflag = false");
	for (long key: lkeys) {
	    int index = PrimArrays.binarySearch(larray, 0, larray.length,
					       key, lc, false);
	    System.out.format("key %d: larray[%d] = %d\n", key,
			      index, larray[index]);
	}
	System.out.println("keyflag = true");
	for (long key: lkeys) {
	    int index = PrimArrays.binarySearch(larray, 0, larray.length,
					       key, lc, true);
	    System.out.format("key %d: larray[%d] = %d\n", key,
			      index, larray[index]);
	}

	System.out.println("trying a large number of binary searches");
	for (int c = 0; c < 500; c++) {
	    if (c % 100 == 0) System.out.format("c = %d\n", c);
	    for (int n = 1; n < 1000; n++) {
		larray = new long[n];
		int m = 0;
		int mm = 0;
		int i = 1;
		while (m < n) {
		    m += (run.next());
		    if (m > n) m = n;
		    while (mm < m) {
			larray[mm++] = (long)(i);
		    }
		    i++;
		}
		PrimArrays.sort(larray, lc);
		for (long key: larray) {
		    int index1 = PrimArrays.binarySearch(larray, 0,
							 larray.length,
							 key, lc, false);
		    int index2 = PrimArrays.binarySearch(larray, 0,
							 larray.length,
							 key, lc, true);
		    if (index2 < index1) {
			throw new Exception ("bad index order");
		    }
		    if (larray[index1] != key) {
			throw new Exception("larray[index1] != key");
		    }
		    if (larray[index2] != key) {
			throw new Exception("larray[index2] != key");
		    }
		    index1--;
		    index2++;
		    if (index1 > 0 && larray[index1] == key) {
			throw new Exception("larray[index1] != key");
		    }
		    if (index2 < n && larray[index2] == key) {
			throw new Exception("larray[index2] != key");
		    }
		}
	    }
	}

	System.out.println("*** float case");
	float fvalues[] = {1.0F, 20.0F, 3.0F, 30.0F, 4.0F, 5.0F, 6.0F, 7.0F};
	float farray[] = fvalues.clone();
	System.out.println("farray.length = " + farray.length);
	PrimArrays.sort(farray, fc);
	for (float i: farray) {
	    System.out.print(i + " ");
	}
	System.out.println();

	farray = fvalues.clone();
	System.out.println("farray.length = " + farray.length);
	PrimArrays.sort(farray, 0, 4, fc);
	for (float i: farray) {
	    System.out.print(i + " ");
	}
	System.out.println();

	PrimArrays.sort(farray, fc);
	for (int i = 0; i < farray.length; i++) {
	    System.out.println(i + " "
			       + PrimArrays.binarySearch(farray, farray[i],
							 fc));
	}
	float[] fkeys = fvalues.clone();
	PrimArrays.sort(fkeys, fc);
	farray = new float[fvalues.length*2];
	System.arraycopy(fvalues, 0, farray, 0, fvalues.length);
	System.arraycopy(fvalues, 0, farray, fvalues.length, fvalues.length);
	PrimArrays.sort(farray, fc);

	System.out.println("keyflag = false");
	for (float key: fkeys) {
	    int index = PrimArrays.binarySearch(farray, 0, farray.length,
					       key, fc, false);
	    System.out.format("key %g: farray[%d] = %g\n", key,
			      index, farray[index]);
	}
	System.out.println("keyflag = true");
	for (float key: fkeys) {
	    int index = PrimArrays.binarySearch(farray, 0, farray.length,
					       key, fc, true);
	    System.out.format("key %g: farray[%d] = %g\n", key,
			      index, farray[index]);
	}

	System.out.println("trying a large number of binary searches");
	for (int c = 0; c < 500; c++) {
	    if (c % 100 == 0) System.out.format("c = %d\n", c);
	    for (int n = 1; n < 1000; n++) {
		farray = new float[n];
		int m = 0;
		int mm = 0;
		int i = 1;
		while (m < n) {
		    m += (run.next());
		    if (m > n) m = n;
		    while (mm < m) {
			farray[mm++] = (float)(i);
		    }
		    i++;
		}
		PrimArrays.sort(farray, fc);
		for (float key: farray) {
		    int index1 = PrimArrays.binarySearch(farray, 0,
							 farray.length,
							 key, fc, false);
		    int index2 = PrimArrays.binarySearch(farray, 0,
							 farray.length,
							 key, fc, true);
		    if (index2 < index1) {
			throw new Exception ("bad index order");
		    }
		    if (farray[index1] != key) {
			throw new Exception("farray[index1] != key");
		    }
		    if (farray[index2] != key) {
			throw new Exception("farray[index2] != key");
		    }
		    index1--;
		    index2++;
		    if (index1 > 0 && farray[index1] == key) {
			throw new Exception("farray[index1] != key");
		    }
		    if (index2 < n && farray[index2] == key) {
			throw new Exception("farray[index2] != key");
		    }
		}
	    }
	}
	System.out.println("*** double case");
	double dvalues[] = {1.0, 20.0, 3.0, 30.0, 4.0, 5.0, 6.0, 7.0};
	double darray[] = dvalues.clone();
	System.out.println("darray.length = " + darray.length);
	PrimArrays.sort(darray, dc);
	for (double i: darray) {
	    System.out.print(i + " ");
	}
	System.out.println();

	darray = dvalues.clone();
	System.out.println("darray.length = " + darray.length);
	PrimArrays.sort(darray, 0, 4, dc);
	for (double i: darray) {
	    System.out.print(i + " ");
	}
	System.out.println();

	PrimArrays.sort(darray, dc);
	for (int i = 0; i < darray.length; i++) {
	    System.out.println(i + " "
			       + PrimArrays.binarySearch(darray, darray[i],
							 dc));
	}
	double[] dkeys = dvalues.clone();
	PrimArrays.sort(dkeys, dc);
	darray = new double[dvalues.length*2];
	System.arraycopy(dvalues, 0, darray, 0, dvalues.length);
	System.arraycopy(dvalues, 0, darray, dvalues.length, dvalues.length);
	PrimArrays.sort(darray, dc);

	System.out.println("keyflag = false");
	for (double key: dkeys) {
	    int index = PrimArrays.binarySearch(darray, 0, darray.length,
					       key, dc, false);
	    System.out.format("key %g: darray[%d] = %g\n", key,
			      index, darray[index]);
	}
	System.out.println("keyflag = true");
	for (double key: dkeys) {
	    int index = PrimArrays.binarySearch(darray, 0, darray.length,
					       key, dc, true);
	    System.out.format("key %g: darray[%d] = %g\n", key,
			      index, darray[index]);
	}

	System.out.println("trying a large number of binary searches");
	for (int c = 0; c < 500; c++) {
	    if (c % 100 == 0) System.out.format("c = %d\n", c);
	    for (int n = 1; n < 1000; n++) {
		darray = new double[n];
		int m = 0;
		int mm = 0;
		int i = 1;
		while (m < n) {
		    m += (run.next());
		    if (m > n) m = n;
		    while (mm < m) {
			darray[mm++] = (double)(i);
		    }
		    i++;
		}
		PrimArrays.sort(darray, dc);
		for (double key: darray) {
		    int index1 = PrimArrays.binarySearch(darray, 0,
							 darray.length,
							 key, dc, false);
		    int index2 = PrimArrays.binarySearch(darray, 0,
							 darray.length,
							 key, dc, true);
		    if (index2 < index1) {
			throw new Exception ("bad index order");
		    }
		    if (darray[index1] != key) {
			throw new Exception("darray[index1] != key");
		    }
		    if (darray[index2] != key) {
			throw new Exception("darray[index2] != key");
		    }
		    index1--;
		    index2++;
		    if (index1 > 0 && darray[index1] == key) {
			throw new Exception("darray[index1] != key");
		    }
		    if (index2 < n && darray[index2] == key) {
			throw new Exception("darray[index2] != key");
		    }
		}
	    }
	}

   }
}
