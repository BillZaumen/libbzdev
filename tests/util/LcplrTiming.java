import org.bzdev.util.*;
import org.bzdev.math.StaticRandom;
import org.bzdev.math.rv.*;

public class LcplrTiming {

    static final int SIZE = 1 << 24;

    public static int naiveSearch(byte[] log, byte[] target) {
	for (int i = 0; i < log.length; i++) {
	    if (log[i] == target[0]) {
		boolean ok = true;
		for (int j = 1; j < target.length; j++) {
		    if (log[i+j] != target[j]) {
			ok = false;
			break;
		    }
		}
		if (ok) return i;
	    }
	}
	return -1;
    }


    public static void main(String argv[]) {
	System.out.println("initializing");
	byte[] logfile = new byte[SIZE];
	IntegerRandomVariable rv = new UniformIntegerRV(0, 128);

	byte[] target = new byte[256];
	for (int i = 0; i < target.length; i++) {
	    target[i] = (byte)(int)rv.next();
	}
	for (int i = 0; i  < logfile.length; i++) {
	    logfile[i] = target[i%128];
	}

	int start = StaticRandom.nextInt(logfile.length - 128);
	for (int i = 0; i < target.length; i++) {
	    logfile[start + i] = target[i];
	}
	
	System.out.println("building suffix array");
	SuffixArray.Byte sa = new SuffixArray.Byte(logfile, 128);

	System.out.println("First timing test");
	System.gc();
	long count = 2 << 23;
	long time0 = System.nanoTime();
	for (int i = 0; i <  count; i++) {
	    int index = sa.findSubsequence(target);
	}
	long time1 = System.nanoTime();
	System.out.println("Time per lookup is " + (time1-time0)/count);

	System.out.println("setting up LCP-LR");
	sa.useLCPLR();

	System.out.println("Second timing test");
	System.gc();
	time0 = System.nanoTime();
	for (int i = 0; i <  count; i++) {
	    int index = sa.findSubsequence(target);
	}
	time1 = System.nanoTime();
	System.out.println("Time per lookup (LCP-LR) is "
			   + (time1-time0)/count);

	System.out.println("Third timing test");
	time0 = System.nanoTime();
	naiveSearch(logfile, target);
	time1 = System.nanoTime();
	System.out.println("Time per lookup (naive search) is "
			   + (time1-time0));
    }
}
