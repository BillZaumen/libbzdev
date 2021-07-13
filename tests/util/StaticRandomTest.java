import java.util.Random;
import org.bzdev.math.StaticRandom;
import org.bzdev.math.rv.*;

public class StaticRandomTest {

    public static void main(String argv[]) throws Exception {
	int i;
	int n = 0;
	int N = 1<<17; 		// want power of two to make FFT fast.

	int NL = 1 << 21;
	java.io.PrintStream out = null;

	RandomVariable<Double> rv1 = new 
	    org.bzdev.math.rv.GaussianRV(0.0, 1.0);
	try {
	    RandomVariable<Double> rv = 
		StaticRandom
		.newRandomVariable(org.bzdev.math.rv.GaussianRV.class,
				   0.0, 1.0);
	    // System.out.println("got here in main");
	    System.out.println("rv.next() = " + rv.next());
	    System.out.println("rv.next() = " + rv.next());
	    System.out.println("rv.next() = " + rv.next());
	    System.out.println("rv.next() = " + rv.next());
	    System.out.println("rv.next() = " + rv.next());
	    System.out.println("rv.next() = " + rv.next());
	    System.out.println("rv.next() = " + rv.next());
	    System.out.println("rv.next() = " + rv.next());
	    System.out.println("rv.next() = " + rv.next());
	} catch (Exception ei) {
	    ei.printStackTrace();
	    System.exit(1);
	}

	if (argv.length > 0) {
	    int ind = 0;
	    while (ind < argv.length) {
		if (argv[ind].equals("-n")) {
		    n = Integer.parseInt(argv[++ind]);
		} else if (argv[ind].equals("-m")) {
		    StaticRandom.maximizeQuality();
		} else if (argv[ind].equals("-N")) {
		    N = Integer.parseInt(argv[++ind]);
		} else if (ind != argv.length - 1) {
		    System.err.println("command-line error");
		    System.exit(1);
		} else {
		    try {
			out = new java.io.PrintStream(argv[argv.length-1], 
						      "UTF-8");
		    } catch (Exception e) {
			System.err.println(e.getMessage());
			System.exit(1);
		    }
		}
		ind++;
	    }
	}

	double gaussian[] = new double[N];
	double poisson[] = new double[N];

	double gmean = 0.0;
	double gvar = 0.0;
	double gsdev = 0.0;

	for (i = 0; i < N; i++) {
	    double x;
	    gaussian[i] = x =  StaticRandom.nextGaussian();
	    gmean += x;
	    gvar += x*x;
	}

	gmean /= N;
	gvar /= N;
	gvar -= gmean*gmean;
	gsdev = Math.sqrt(gvar);
	System.out.println("gaussian: mean = " +gmean +", sdev = " +gsdev);
	System.out.println(" ... expected close to 0.0, 1.0");

	int NN = 1<<15;
	System.out.println("poissonInt test:");
	for (int j = 1; j < 200; j++) {
	    double ppmean = 0.0;
	    double ppvar = 0.0;
	    for (i = 0; i < N; i++) {
		double x = StaticRandom.poissonInt((double)j);
		ppmean += x;
		ppvar += x*x;
	    }
	    ppmean /= N;
	    ppvar /= N;
	    ppvar -= ppmean*ppmean;
	    if (j == 32) System.out.println("j reached 32");
	    if (j < 32) {
		if (Math.abs(ppmean - ppvar) > .20) {
		    System.out.println("j = " + j + " mean (Poisson) = "
				       + ppmean
				       + ", variance = " + ppvar);
		}
	    } else {
		if (Math.abs(ppmean - ppvar) > 1.0) {
		    System.out.println("j = " + j + " mean (Poisson) = "
				       + ppmean
				       + ", variance = " + ppvar);
		}
	    }
	}
	{
	    System.out.println("try poissonInt() with j = 1<<15");
	    int j = 1 << 15;
	    double ppmean = 0.0;
	    double ppvar = 0.0;
	    for (i = 0; i < N; i++) {
		double x = StaticRandom.poissonInt((double)j);
		ppmean += x;
		ppvar += x*x;
	    }
	    ppmean /= N;
	    ppvar /= N;
	    ppvar -= ppmean*ppmean;
	    if (j == 32) System.out.println("j reached 32");
	    if (j < 32) {
		if (Math.abs(ppmean - ppvar) > .20) {
		    System.out.println("j = " + j + " mean (Poisson) = "
				       + ppmean
				       + ", variance = " + ppvar);
		}
	    } else {
		if (Math.abs(ppmean - ppvar) > 1.0) {
		    System.out.println("j = " + j + " mean (Poisson) = "
				       + ppmean
				       + ", variance = " + ppvar);
		}
	    }
	}

	System.out.println("poissonLong test:");
	for (int j = 1; j < 200; j++) {
	    double ppmean = 0.0;
	    double ppvar = 0.0;
	    for (i = 0; i < N; i++) {
		double x = StaticRandom.poissonLong((double)j);
		ppmean += x;
		ppvar += x*x;
	    }
	    ppmean /= N;
	    ppvar /= N;
	    ppvar -= ppmean*ppmean;
	    if (j == 32) System.out.println("j reached 32");
	    if (j < 32) {
		if (Math.abs(ppmean - ppvar) > .20) {
		    System.out.println("j = " + j + " mean (Poisson) = "
				       + ppmean
				       + ", variance = " + ppvar);
		}
	    } else {
		if (Math.abs(ppmean - ppvar) > 1.0) {
		    System.out.println("j = " + j + " mean (Poisson) = "
				       + ppmean
				       + ", variance = " + ppvar);
		}
	    }
	}

	System.out.println("poissonDouble test:");
	for (int j = 1; j < 200; j++) {
	    double ppmean = 0.0;
	    double ppvar = 0.0;
	    for (i = 0; i < N; i++) {
		double x = StaticRandom.poissonDouble((double)j);
		ppmean += x;
		ppvar += x*x;
	    }
	    ppmean /= N;
	    ppvar /= N;
	    ppvar -= ppmean*ppmean;
	    if (j == 32) System.out.println("j reached 32");
	    if (j < 32) {
		if (Math.abs(ppmean - ppvar) > .20) {
		    System.out.println("j = " + j + " mean (Poisson) = "
				       + ppmean
				       + ", variance = " + ppvar);
		}
	    } else {
		if (Math.abs(ppmean - ppvar) > 1.0) {
		    System.out.println("j = " + j + " mean (Poisson) = "
				       + ppmean
				       + ", variance = " + ppvar);
		}
	    }
	}

	System.out.println("poissonInt test with tables:");
	for (int j = 1; j < 200; j++) {
	    double ppmean = 0.0;
	    double ppvar = 0.0;
	    for (i = 0; i < N; i++) {
		double x = StaticRandom.poissonInt((double)j, true);
		ppmean += x;
		ppvar += x*x;
	    }
	    ppmean /= N;
	    ppvar /= N;
	    ppvar -= ppmean*ppmean;
	    if (j == 32) System.out.println("j reached 32");
	    if (j < 32) {
		if (Math.abs(ppmean - ppvar) > .20) {
		    System.out.println("j = " + j + " mean (Poisson) = "
				       + ppmean
				       + ", variance = " + ppvar);
		}
	    } else {
		if (Math.abs(ppmean - ppvar) > 1.0) {
		    System.out.println("j = " + j + " mean (Poisson) = "
				       + ppmean
				       + ", variance = " + ppvar);
		}
	    }
	}


	double pmean = 0.0;
	double pvar = 0.0;
	double psdev = 0.0;
	for (i = 0; i < N; i++) {
	    double x = -(java.lang.Math.log(StaticRandom.nextDouble()));
	    poisson[i] = x;
	    pmean += x;
	    pvar += x*x;
	}
	pmean /= N;
	pvar /= N;
	pvar -= pmean*pmean;
	psdev = Math.sqrt(pvar);
	System.out.println("exp: mean = " +pmean +", sdev = " +psdev);
	System.out.println("... epxected close to 1.0, 1.0");

	double duniform[] = new double[N];
	double dumean = 0.0;
	double duvar = 0.0;
	double dusdev = 0.0;
	for (i = 0; i < N; i++) {
	    double x = StaticRandom.nextDouble();
	    duniform[i] = x;
	    dumean += x;
	    duvar += x*x;
	}
	dumean /= N;
	duvar /= N;
	duvar -= dumean*dumean;
	dusdev = Math.sqrt(duvar);
	System.out.println("uniform(double): mean = " +dumean 
			   +", sdev = " +dusdev);
	System.out.println("... expecting 0.5, 0.288675135");

	double funiform[] = new double[N];
	double fumean = 0.0;
	double fuvar = 0.0;
	double fusdev = 0.0;
	for (i = 0; i < N; i++) {
	    double x = StaticRandom.nextDouble();
	    funiform[i] = x;
	    fumean += x;
	    fuvar += x*x;
	}
	fumean /= N;
	fuvar /= N;
	fuvar -= fumean*fumean;
	fusdev = Math.sqrt(fuvar);
	System.out.println("uniform(float): mean = " +fumean 
			   +", sdev = " +fusdev);
	System.out.println("... expecting 0.5, 0.288675135");

	int bdata[] = new int[N];
	int fcount = 0, tcount = 0;
	for (i = 0; i < N; i++) {
	    boolean b = StaticRandom.nextBoolean();
	    if (b) {
		bdata[i] = 1;
		tcount++;
	    } else {
		bdata[i] = 0;
		fcount++;
	    }
	}
	System.out.println("tcount = " +tcount +", fcount = " +fcount);
	System.out.println("expecting " + N/2.0 +", "+ N/2.0);


	int iuniform[] = new int[N];
	double imean = 0.0;
	for (i = 0; i < N; i++) {
	    int x = StaticRandom.nextInt();
	    iuniform[i] = x;
	    imean += x;
	}
	imean /= N;
	System.out.println("uniform(int): mean as fraction = "
			   + (imean / Double.MAX_VALUE));
	System.out.println("mean should be close to zero relative to 2^32");

	long luniform[] = new long[N];
	double lmean = 0.0;
	for (i = 0; i < N; i++) {
	    long x = StaticRandom.nextLong();
	    luniform[i] = x;
	    lmean += x;
	}
	lmean /= N;
	System.out.println("uniform(long): mean as fraction = " 
			   + (lmean / Double.MAX_VALUE));
	System.out.println("mean should be close to zero relative to 2^64");

	double  expSum = 0.0;
	double  expSumSq = 0.0;
	for (i = 0; i < N; i++) {
	    double x = StaticRandom.nextDoubleExpDistr(1.0);
	    expSum += x;
	    expSumSq += x*x;
	}
	double expMean = expSum/N;
	double expVar = expSumSq/N - expMean * expMean;
	System.out.println("expMean = " + expMean + ", expVar = " + expVar);
	System.out.println("nextDoubleExpDistr(1.0, N)/expSum = "
			   + StaticRandom.nextDoubleExpDistr(1.0, N) / expSum);
	expSum = 0.0;
	for (i = 0; i < 29; i++) {
	    double x = StaticRandom.nextDoubleExpDistr(1.0);
	    expSum += x;
	}
	System.out.println("Now with N = 29 ...");
	System.out.println("nextDoubleExpDistr(1.0, 29)/expSum = "
			   + StaticRandom.nextDoubleExpDistr(1.0, 29) / expSum);

	long iaSum = 0;
	long iaSumSq = 0;
	for (i = 0; i < N; i++) {
	    long x = StaticRandom.nextPoissonIATime(1000.0);
	    iaSum += x;
	    iaSumSq += x*x;
	}
	long iaMean = iaSum/N;
	long iaVar = iaSumSq/N - iaMean * iaMean;

	System.out.println("iaMean = " + iaMean + ", iaVar = " + iaVar);
	System.out.println("nextPoissonIATime(1.0, N)  = "
			   + StaticRandom.nextPoissonIATime(1000.0, N)
			   + ",  iaSum = " +iaSum);
	iaSum = 0;
	for (i = 0; i < 29; i++) {
	    long x = StaticRandom.nextPoissonIATime(1000.0);
	    iaSum += x;
	}
	System.out.println("nextPoissonIATime(1.0, 29)  = "
			   + StaticRandom.nextPoissonIATime(1000.0, 29)
			   + ",  iaSum = " +iaSum);

	int nuniform[] = new int[N];
	double nmean = 0.0;
	if (n > 0) {
	    int histogram[] = new int[n];
	    for (i = 0; i < N; i++) {
		int x = StaticRandom.nextInt(n);
		nuniform[i] = x;
		nmean += x;
		histogram[x%n]++;
	    }
	    nmean /= N;
	    System.out.println("uniform([0,n)): mean = " +nmean);
	    for (i = 0; i < n; i++) {
		System.out.println("count[" +i +"] = " +histogram[i]);
	    }
	}

	if (out != null) {
	    out.println("gaussian  exp  edata duniform  dudata "
			+"funiform fudata  bdata idata ldata ndata");
	    for (i = 0; i < N; i++) {
		out.println(gaussian[i] +" " + poisson[i]
			    +" " +(poisson[i] - pmean)
			    +" " +duniform[i] +" " +(duniform[i] - 0.5)
			    +" " +funiform[i] +" " +(funiform[i] - 0.5)
			    +" " +bdata[i]
			    +" " +iuniform[i]
			    +" " +luniform[i]
			    +" " +(nuniform[i] - nmean));
	    }
	}
	System.exit(0);
    }
}
