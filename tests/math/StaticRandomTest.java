import java.lang.reflect.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import org.bzdev.math.FFT;
import org.bzdev.math.stats.BasicStats;
import org.bzdev.math.stats.BasicStats.Population;

public class StaticRandomTest {

    static Random generator = new SecureRandom();
    volatile static boolean isSecure = false;

    static Runtime runtime = Runtime.getRuntime();
    static AtomicInteger N;
    volatile static Random normalArray[] = null;
    volatile static Random secureArray[] = null;
    static {
	int n = runtime.availableProcessors();
	N = new AtomicInteger(n);
	normalArray = new Random[n];
	secureArray = new Random[n];
	for (int i = 0; i < n; i++) {
	    normalArray[i] = new Random(generator.nextLong());
	}
	byte[] bytes = new byte[32];
	for (int i = 0; i < n; i++) {
	    generator.nextBytes(bytes);
	    secureArray[i] = new SecureRandom(bytes);
	}
    }

    static AtomicInteger start = new AtomicInteger(0);
    static AtomicInteger end = new AtomicInteger(0);

    volatile static int maxdiff = 0;

    static AtomicInteger resizeCount = new AtomicInteger(0);

    static Random getRandom() {
	int n = N.get();
	int diff = (end.getAndIncrement() - start.get());
	if (diff > n) {
	    synchronized(StaticRandomTest.class) {
		int nn = N.get();
		// check again in case something changed before
		// we synchronized.
		if (diff > nn) {
		    int m = runtime.availableProcessors();
		    resizeCount.getAndIncrement();
		    Random[] array1 = new Random[diff + m];
		    Random[] array2 = new Random[diff + m];
		    System.arraycopy(normalArray, 0, array1, 0,
				     normalArray.length);
		    System.arraycopy(secureArray, 0, array2, 0,
				     secureArray.length);
		    for (int i = normalArray.length; i < array1.length; i++) {
			array1[i] = new Random(generator.nextLong());
		    }
		    byte[] bytes = new byte[32];
		    for (int i = secureArray.length; i < array2.length; i++) {
			generator.nextBytes(bytes);
			array2[i] = new SecureRandom(bytes);
		    }
		    normalArray = array1;
		    secureArray = array2;
		    N.set(array1.length);
		    n = array1.length;
		} else {
		    n = nn;
		}
	    }
	}
	if (diff > maxdiff) maxdiff = diff;
	int index =  diff % n;
	if (index < 0) index += n;
	return isSecure? secureArray[index]: normalArray[index];
    }

    static void releaseRandom() {
	start.getAndIncrement();
    }

    static long[] time;

    static void testThreading() {
	Thread[] tarray = new Thread[1000];
	time = new long[1000];
	
	for (int i = 0; i < 1000; i++) {
	    final int index = i;
	    tarray[i] = new Thread(() -> {
		    time[index] = System.nanoTime();
		    for (int j = 0; j < 1000; j++) {
			Random r = getRandom();
			r.nextLong();
			releaseRandom();
		    }
		    time[index] = System.nanoTime() - time[index];
	    });
	}
	for (int i = 0; i < 1000; i++) {
	    tarray[i].start();
	}
	try {
	    for (int i = 0; i < 1000; i++) {
		tarray[i].join();
	    }
	} catch (InterruptedException e) {
	}

	System.out.println("number of processors = "
			   + runtime.availableProcessors());
	System.out.println("maxdiff = " + maxdiff);
	System.out.println("resizeCount = " + resizeCount.get());
	BasicStats stats = new BasicStats.Population();
	for (int i = 0; i < 1000; i++) {
	    stats.add((double)(time[i]/1000));
	}
	System.out.println("running time = " + stats.getMean()
			    + " += " + stats.getSDev());

	final Random ourRandom = normalArray[0];
	for (int i = 0; i < 1000; i++) {
	    final int index = i;
	    tarray[i] = new Thread(() -> {
		    time[index] = System.nanoTime();
		    for (int j = 0; j < 1000; j++) {
			ourRandom.nextLong();
		    }
		    time[index] = System.nanoTime() - time[index];
	    });
	}
	for (int i = 0; i < 1000; i++) {
	    tarray[i].start();
	}
	try {
	    for (int i = 0; i < 1000; i++) {
		tarray[i].join();
	    }
	} catch (InterruptedException e) {
	}
	stats = new BasicStats.Population();
	for (int i = 0; i < 1000; i++) {
	    if ((i % 100) == 0) System.out.println("time[" + i + "] = "
						   + time[i]/1000);
	    stats.add((double)(time[i]/1000));
	}
	System.out.println("running time (one generator) = " + stats.getMean()
			    + " += " + stats.getSDev());
    }
    

    public static void main(String argv[]) throws Exception {

	testThreading();


	// Test if we can use Secure Random to generate seeds
	// for other instances of Secure Random without creating
	// a correlation between values.

	SecureRandom base = new SecureRandom();

	Random base2 = new Random();
	System.out.println("test timing");

	int M = 1000000;
	long t1 = System.nanoTime();
	for (int i = 0; i < M; i++) {
	    base.nextInt();
	}
	long t2 = System.nanoTime();
	long tau1 = (t2 - t1)/1000;

	t1 = System.nanoTime();
	for (int i = 0; i < M; i++) {
	    base2.nextInt();
	}
	t2 = System.nanoTime();
	long tau2 = (t2 - t1)/1000;

	t1 = System.nanoTime();
	for (int i = 0; i < M; i++) {
	    end.getAndIncrement();
	    start.getAndIncrement();
	}
	t2 = System.nanoTime();
	long tau3 = (t2 - t1)/1000;
	System.out.println("SecureRandom: " + tau1 + " microseconds");
	System.out.println("Random: " + tau2 + " microseconds");
	System.out.println("Atomic operations: " + tau3 + " microseconds");
 
	


	byte[] bytes = new byte[32];
	base.nextBytes(bytes);
	SecureRandom sr1 = new SecureRandom(bytes);
	base.nextBytes(bytes);
	SecureRandom sr2 = new SecureRandom(bytes);
	
	int N = 1<<20;

	double[] array1 = new double[N];
	sr1.nextBytes(bytes);
	for (int i = 0; i < N; i++) {
	    array1[i] = sr1.nextDouble()-0.5;
	}

	double[] array2 = new double[N];
	sr2.nextBytes(bytes);
	for (int i = 0; i < N; i++) {
	    array2[i] = sr2.nextDouble()-0.5;
	}

	double[] carray = new double[N];
	double[] array3 = new double[N];
	FFT.cyclicCrossCorrelate(array1, array1, carray);
	double max = 0.0;
	int ind = -1;
	double min = N;
	int ind2 = -1;
	for (int i = 1; i < N; i++) {
	    double v = Math.abs(carray[i]);
	    if (v > max) {
		max = v;
		ind = i;
	    }
	    if (v < min) {
		min = v;
		ind2 = i;
	    }
	}
	double peakValue = carray[0];
	System.out.println("carray[0] = " + carray[0]);
	if (ind >= 0) System.out.println("array1-array1: max = " + carray[ind]);
	if (ind2 >= 0) System.out.println("array1-array1: min = "
					  + carray[ind2]);

	FFT.cyclicCrossCorrelate(array1, array2, carray);

	BasicStats stats = new BasicStats.Population();
	BasicStats cstats = new BasicStats.Population();
	for (int i = 1; i < N; i++) {
	    stats.add(carray[i]);
	    cstats.add(carray[i]);
	}
	System.out.println("mean = " + stats.getMean()
			   + ", sdev = " + stats.getSDev());

	System.out.println("ratio = " + (peakValue/stats.getSDev())
			   + ", sqrt(N) = " +  Math.sqrt((double)N));

	max = 0.0;
	ind = -1;
	min = N;
	ind2 = -1;
	for (int i = 0; i < N; i++) {
	    double v = Math.abs(carray[i]);
	    if (v > max) {
		max = v;
		ind = i;
	    }
	    if (v < min) {
		min = v;
		ind2 = i;
	    }
	}
	if (ind >= 0) System.out.println("array1-array2: max = " + carray[ind]);
	if (ind2 >= 0) System.out.println("array1-array2: min = "
					  + carray[ind2]);

	stats = new BasicStats.Population();
	for (int i = 0; i < N; i++) {
	    stats.add(carray[i]);
	    cstats.add(carray[i]);
	}
	System.out.println("mean = " + stats.getMean()
			   + ", sdev = " + stats.getSDev());

	System.out.println("combined: " + "mean = " + stats.getMean()
			   + ", sdev = " + stats.getSDev());

    }
}
