import org.bzdev.math.*;
import org.bzdev.math.rv.*;

public class FFTTest {
    public static void main(String argv[]) throws Exception {

	{
	    int N = 32;
	    int M = 17;
	    double[] f = new double[N];
	    double[] g = new double[M];
	    double[] result = new double[N];
	    double[] expected = new double[N];
	    for (int i = 0; i < N; i++) {
		f[i] = i+1.0;
	    }
	    for (int i = 0; i < M; i++) {
		g[i] = i+1.0;
	    }

	    for (int n = 0; n < N; n++) {
		for (int m  = 0; m < f.length; m++) {
		    int mpn = m+n + N;
		    mpn = mpn % N;
		    if (mpn >= g.length) continue;
		    expected[n] += f[m]*g[mpn];
		}
	    }

	    FFT.cyclicCrossCorrelate(f, g, result);

	    boolean error = false;
	    for (int i = 0; i < N; i++) {
		if (Math.abs(expected[i] - result[i]) > 1.e-10) {
		    System.out.println("at i = " + i + ", expected[i] = "
				       + expected[i] + ", result[i] = "
				       + result[i]
				       + ", ratio = "
				       + (expected[i] / result[i]));
		    error = true;
		}
	    }

	    if (error) {
		System.out.println("N = " + N + ", M = " + M);
		System.exit(1);
	    }
	}

	{
	    int N = 33;
	    int M = 20;
	    int gz = 0;
	    double[] f = new double[N];
	    double[] g = new double[M];
	    double[] result = new double[N];
	    double[] expected = new double[N];
	    for (int i = 0; i < N; i++) {
		f[i] = i+1;
	    }
	    for (int i = 0; i < M; i++) {
		g[i] = i+1;
	    }

	    for (int k = 0; k < N; k++) {
		for (int i = 0; i < N; i++) {
		    int l = k - i + gz + N;
		    l = l % N;
		    if (l >= g.length) continue;
		    expected[k] += f[i]*g[l];
		}
	    }
	    FFT.cyclicConvolve(f, g, gz, result);

	    boolean error = false;
	    for (int i = 0; i < N; i++) {
		if (Math.abs(expected[i] - result[i]) > 1.e-10) {
		    error = true;
		}
	    }
	    if (error) {
		for (int i = 0; i < N; i++) {
		    System.out.println("at i = " + i
				       + ", expected[i] = "
				       + expected[i]
				       + ", result[i] = " + result[i]
				       + ", ratio = "
				       + (expected[i] / result[i]));
		}
		System.out.println("... failed when N ="
				   + N + ", M = " + M
				   + ", gz = " + gz);
	    }
	    if (error) System.exit(1);
	}

	java.security.SecureRandom r = new java.security.SecureRandom();

	for (int i = 20; i < 40; i++) {
	    System.out.format("FFT.getLength(%d) = %d\n",
			      i, FFT.getLength(i));
	}

	FFT.Factory fftf = new FFT.Factory();

	FFT fft0 = fftf.newInstance(32);
	if (fft0.getLength() != 32 && fft0.getMode() != FFT.Mode.SYMMETRIC) {
	    System.out.println("mode = " + fft0.getMode()
			       + ",length = " + fft0.getLength());
	    System.exit(1);
	}
	fft0 = fftf.newInstance(32, FFT.Mode.NORMAL);
	if (fft0.getLength() != 32 && fft0.getMode() != FFT.Mode.NORMAL) {
	    System.out.println("mode = " + fft0.getMode()
			       + ",length = " + fft0.getLength());
	    System.exit(1);
	}
	fft0 = fftf.newInstance(32, FFT.Mode.REVERSED);
	if (fft0.getLength() != 32 && fft0.getMode() != FFT.Mode.REVERSED) {
	    System.out.println("mode = " + fft0.getMode()
			       + ",length = " + fft0.getLength());
	    System.exit(1);
	}
	fftf.setName("bzdevFFT");
	fft0 = fftf.newInstance(32);
	if (fft0.getLength() != 32 && fft0.getMode() != FFT.Mode.SYMMETRIC) {
	    System.out.println("mode = " + fft0.getMode()
			       + ",length = " + fft0.getLength());
	    System.exit(1);
	}
	fft0 = fftf.newInstance(32, FFT.Mode.SYMMETRIC);
	if (fft0.getLength() != 32 && fft0.getMode() != FFT.Mode.SYMMETRIC) {
	    System.out.println("mode = " + fft0.getMode()
			       + ",length = " + fft0.getLength());
	    System.exit(1);
	}
	fft0 = fftf.newInstance(32, FFT.Mode.NORMAL);
	if (fft0.getLength() != 32 && fft0.getMode() != FFT.Mode.NORMAL) {
	    System.out.println("mode = " + fft0.getMode()
			       + ",length = " + fft0.getLength());
	    System.exit(1);
	}
	fft0 = fftf.newInstance(32, FFT.Mode.REVERSED);
	if (fft0.getLength() != 32 && fft0.getMode() != FFT.Mode.REVERSED) {
	    System.out.println("mode = " + fft0.getMode()
			       + ",length = " + fft0.getLength());
	    System.exit(1);
	}

	fftf.setName(null);
	fft0 = fftf.newInstance(32);
	if (fft0.getLength() != 32 && fft0.getMode() != FFT.Mode.SYMMETRIC) {
	    System.out.println("mode = " + fft0.getMode()
			       + ",length = " + fft0.getLength());
	    System.exit(1);
	}
	fft0 = fftf.newInstance(32, FFT.Mode.SYMMETRIC);
	if (fft0.getLength() != 32 && fft0.getMode() != FFT.Mode.SYMMETRIC) {
	    System.out.println("mode = " + fft0.getMode()
			       + ",length = " + fft0.getLength());
	    System.exit(1);
	}
	fft0 = fftf.newInstance(32, FFT.Mode.NORMAL);
	if (fft0.getLength() != 32 && fft0.getMode() != FFT.Mode.NORMAL) {
	    System.out.println("mode = " + fft0.getMode()
			       + ",length = " + fft0.getLength());
	    System.exit(1);
	}
	fft0 = fftf.newInstance(32, FFT.Mode.REVERSED);
	if (fft0.getLength() != 32 && fft0.getMode() != FFT.Mode.REVERSED) {
	    System.out.println("mode = " + fft0.getMode()
			       + ",length = " + fft0.getLength());
	    System.exit(1);
	}

	boolean status = fftf.setParameters(32, true, FFT.LMode.DESIRED_LENGTH);
	if (status == false) {
	    throw new Exception("bad status");
	}
	fft0 = fftf.newInstance(32);
	if (fft0.getLength() != 32 && fft0.getMode() != FFT.Mode.SYMMETRIC) {
	    System.out.println("mode = " + fft0.getMode()
			       + ",length = " + fft0.getLength());
	    System.exit(1);
	}
	fft0 = fftf.newInstance(32, FFT.Mode.SYMMETRIC);
	if (fft0.getLength() != 32 && fft0.getMode() != FFT.Mode.SYMMETRIC) {
	    System.out.println("mode = " + fft0.getMode()
			       + ",length = " + fft0.getLength());
	    System.exit(1);
	}
	fft0 = fftf.newInstance(32, FFT.Mode.NORMAL);
	if (fft0.getLength() != 32 && fft0.getMode() != FFT.Mode.NORMAL) {
	    System.out.println("mode = " + fft0.getMode()
			       + ",length = " + fft0.getLength());
	    System.exit(1);
	}
	fft0 = fftf.newInstance(32, FFT.Mode.REVERSED);
	if (fft0.getLength() != 32 && fft0.getMode() != FFT.Mode.REVERSED) {
	    System.out.println("mode = " + fft0.getMode()
			       + ",length = " + fft0.getLength());
	    System.exit(1);
	}

	status = fftf.setParameters(32, true, FFT.LMode.MAX_LENGTH);
	if (status == false) {
	    throw new Exception("bad status");
	}
	fft0 = fftf.newInstance(32);
	if (fft0.getLength() != 32 && fft0.getMode() != FFT.Mode.SYMMETRIC) {
	    System.out.println("mode = " + fft0.getMode()
			       + ",length = " + fft0.getLength());
	    System.exit(1);
	}
	fft0 = fftf.newInstance(32, FFT.Mode.SYMMETRIC);
	if (fft0.getLength() != 32 && fft0.getMode() != FFT.Mode.SYMMETRIC) {
	    System.out.println("mode = " + fft0.getMode()
			       + ",length = " + fft0.getLength());
	    System.exit(1);
	}
	fft0 = fftf.newInstance(32, FFT.Mode.NORMAL);
	if (fft0.getLength() != 32 && fft0.getMode() != FFT.Mode.NORMAL) {
	    System.out.println("mode = " + fft0.getMode()
			       + ",length = " + fft0.getLength());
	    System.exit(1);
	}
	fft0 = fftf.newInstance(32, FFT.Mode.REVERSED);
	if (fft0.getLength() != 32 && fft0.getMode() != FFT.Mode.REVERSED) {
	    System.out.println("mode = " + fft0.getMode()
			       + ",length = " + fft0.getLength());
	    System.exit(1);
	}

	status = fftf.setParameters(32, true, FFT.LMode.EXACT_LENGTH);
	if (status == false) {
	    throw new Exception("bad status");
	}
	fft0 = fftf.newInstance(32);
	if (fft0.getLength() != 32 && fft0.getMode() != FFT.Mode.SYMMETRIC) {
	    System.out.println("mode = " + fft0.getMode()
			       + ",length = " + fft0.getLength());
	    System.exit(1);
	}
	fft0 = fftf.newInstance(32, FFT.Mode.SYMMETRIC);
	if (fft0.getLength() != 32 && fft0.getMode() != FFT.Mode.SYMMETRIC) {
	    System.out.println("mode = " + fft0.getMode()
			       + ",length = " + fft0.getLength());
	    System.exit(1);
	}
	fft0 = fftf.newInstance(32, FFT.Mode.NORMAL);
	if (fft0.getLength() != 32 && fft0.getMode() != FFT.Mode.NORMAL) {
	    System.out.println("mode = " + fft0.getMode()
			       + ",length = " + fft0.getLength());
	    System.exit(1);
	}
	fft0 = fftf.newInstance(32, FFT.Mode.REVERSED);
	if (fft0.getLength() != 32 && fft0.getMode() != FFT.Mode.REVERSED) {
	    System.out.println("mode = " + fft0.getMode()
			       + ",length = " + fft0.getLength());
	    System.exit(1);
	}

	status = fftf.setParameters(31, true, FFT.LMode.EXACT_LENGTH);
	if (status == false) {
	    throw new Exception("bad status");
	}
	try {
	    fft0 = fftf.newInstance(31);
	    throw new Exception("missing exception");
	} catch (IllegalArgumentException e) {}

	for (int ind = 0; ind < 6; ind++) {
	    int gz = ind;
	    double[] f = new double[20];
	    double[] g = new double[6];
	    double[] result = new double[20];
	    double[] expected = new double[20];
	    for (int i = 0; i < 20; i++) {
		f[i] = i+1;
	    }
	    for (int i = 0; i < 6; i++) {
		g[i] = i+1;
	    }

	    for (int k = 0; k < 20; k++) {
		for (int i = 0; i < 20; i++) {
		    int l = k - i + gz + 20;
		    l = l % 20;
		    if (l >= g.length) continue;
		    expected[k] += f[i]*g[l];
		}
	    }
	    FFT.cyclicConvolve(f, g, gz, result);

	    boolean error = false;
	    System.out.println("gz = " + gz);
	    for (int i = 0; i < 20; i++) {
		if (Math.abs(expected[i] - result[i]) > 1.e-10) {
		    error = true;
		}
		System.out.println("at i = " + i
				   + ", expected[i] = "
				   + expected[i]
				   + ", result[i] = " + result[i]
				   + ", ratio = "
				   + (expected[i] / result[i]));
	    }
	    if (error) System.exit(1);
	}

	for (int n = 2; n < (1 << 14); n = n << 1) {
	    double[] xr = new double[n];
	    double[] xi = new double[n];
	    double[] xr1 = new double[n];
	    double[] xi1 = new double[n];
	    double[] xr2 = new double[n];
	    double[] xi2 = new double[n];
	    System.out.println("n = " + n);

	    for (int i = 0; i < n; i++) {
		xr[i] = r.nextDouble();
		xi[i] = r.nextDouble();
		xr1[i] = xr[i];
		xi1[i] = xi[i];
	    }
	    // brute force for testing
	    // use an addition algorithm for accuracy.
	    Adder.Kahan adder1 = new Adder.Kahan();
	    Adder.Kahan.State state1 = adder1.getState();
	    Adder.Kahan adder2 = new Adder.Kahan();
	    Adder.Kahan.State state2 = adder2.getState();
	    for (int k = 0; k < n; k++) {
		adder1.reset();
		adder2.reset();
		for (int i = 0; i < n; i++) {
		    double arg = -(2.0*Math.PI*i*k)/n;
		    double ci = Math.cos(arg);
		    double si = Math.sin(arg);
		    double term1 = xr[i]*ci - xi[i]*si;
		    double term2 = xr[i]*si + xi[i]*ci;
		    double y1 = term1 - state1.c;
		    double y2 = term2 - state2.c;
		    double t1 = state1.total + y1;
		    double t2 = state2.total + y2;
		    state1.c = (t1 - state1.total) - y1;
		    state2.c = (t2 - state2.total) - y2;
		    state1.total = t1;
		    state2.total = t2;
		    // xr2[k] += xr[i]*ci - xi[i]*si;
		    // xi2[k] += xr[i]*si + xi[i]*ci;
		}
		xr2[k] = adder1.getSum();
		xi2[k] = adder2.getSum();
	    }
	    double norm = Math.sqrt(n);
	    for (int k = 0; k < n; k++) {
		xr2[k] /= norm;
		xi2[k] /= norm;
	    }
	    double[] base = FFTbase.fft(xr, xi, false);

	    System.out.println("n = " + n + ", FFT.getLength(n) = "
			       + FFT.getLength(n));

	    FFT fft = FFT.newInstance(n);
	    System.out.println("test transform");

	    fft.transform(xr1, xi1, xr1, xi1);

	    for (int k = 0; k < n; k++) {
		int kk = k * 2;
		if (Math.abs(xr1[k] - base[kk]) > 1.e-15 ||
		    Math.abs(xi1[k] - base[kk+1]) > 1.e-15) {
		    System.out.format
			("[base] differ (%g, %g) != base (%g, %g)\n",
			 xr1[k], xi1[k], base[kk], base[kk+1]);
		    
		}
		if (Math.abs(xr1[k] - xr2[k]) > 1.e-10 ||
		    Math.abs(xi1[k] - xi2[k]) > 1.e-10) {
		    System.out.format("differ (%g, %g) != (%g, %g)\n",
				      xr1[k], xi1[k], xr2[k], xi2[k]);
		    System.exit(1);
		}
	    }
	    System.out.println("test inverse");
	    fft.inverse(xr1, xi1, xr1, xi1);
	    for (int k = 0; k < n; k++) {
		if (Math.abs(xr1[k] - xr[k]) > 1.e-15 ||
		    Math.abs(xi1[k] - xi[k]) > 1.e-15) {
		    System.out.format("[inverse] differ (%g, %g) != (%g, %g)\n",
				      xr1[k], xi1[k], xr[k], xi[k]);
		    System.exit(1);
		}
	    }
	}
	for (int n = 1 << 14; n < (1 << 24); n = n << 1) {
	    System.out.println("n = " + n);
	    double[] xr = new double[n];
	    double[] xi = new double[n];
	    double[] xr1 = new double[n];
	    double[] xi1 = new double[n];
	    for (int i = 0; i < n; i++) {
		xr[i] = r.nextDouble();
		xi[i] = r.nextDouble();
		xr1[i] = xr[i];
		xi1[i] = xi[i];
	    }
	    double[] base = FFTbase.fft(xr, xi, false);
	    long start = System.nanoTime();
	    FFT fft = FFT.newInstance(n);
	    long time =  System.nanoTime();
	    System.out.println("fft setup took " +((time - start)/1000)
			       + " us");
	    System.out.println("test transform");

	    start = System.nanoTime();
	    fft.transform(xr1, xi1, xr1, xi1);
	    time = System.nanoTime();
	    System.out.println("transform took " +((time - start)/1000)
			       + " us");
	    for (int k = 0; k < n; k++) {
		int kk = k * 2;
		if (Math.abs(xr1[k] - base[kk]) > 1.e-15 ||
		    Math.abs(xi1[k] - base[kk+1]) > 1.e-15) {
		    System.out.format("differ (%g, %g) != base (%g, %g)\n",
				      xr1[k], xi1[k], base[kk], base[kk+1]);
		    System.exit(1);
		}
	    }
	    start = System.nanoTime();
	    fft.inverse(xr1, xi1, xr1, xi1);
	    time = System.nanoTime();
	    System.out.println("inverse took " +((time - start)/1000)
			       + " us");
	    for (int k = 0; k < n; k++) {
		if (Math.abs(xr1[k] - xr[k]) > 1.e-14 ||
		    Math.abs(xi1[k] - xi[k]) > 1.e-14) {
		    System.out.format("differ (%g, %g) != (%g, %g)\n",
				      xr1[k], xi1[k], xr[k], xi[k]);
		    if (Math.abs(xr1[k] - xr[k]) > 1.e-13 ||
			Math.abs(xi1[k] - xi[k]) > 1.e-13) {
			System.exit(1);
		    }
		}
	    }
	}

	System.out.println("check normalization");
	{
	    double[] xr = new double[128];
	    double[] xi = new double[128];
	    for (int i = 0; i < 128; i++) {
		xr[i] = r.nextDouble();
		xi[i] = r.nextDouble();
	    }
	    double[] xr1 = new double[128];
	    double[] xi1 = new double[128];
	    for (int i = 0; i < 128; i++) {
		xr1[i] = xr[i];
		xi1[i] = xi[i];
	    }
	    FFT fft = FFT.newInstance(128, FFT.Mode.NORMAL);
	    fft.transform(xr1, xi1, xr1, xi1);
	    fft.inverse(xr1, xi1, xr1, xi1);
	    for (int i = 0; i < 128; i++) {
		if (Math.abs(xr[i] - xr1[i]) > 1.e-10
		    || Math.abs(xi[i] - xi1[i]) > 1.e-10) {
		    System.out.println("bad result for mode NORMAL");
		    System.exit(1);
		}
	    }

	    for (int i = 0; i < 128; i++) {
		xr1[i] = xr[i];
		xi1[i] = xi[i];
	    }
	    fft = FFT.newInstance(128, FFT.Mode.SYMMETRIC);
	    fft.transform(xr1, xi1, xr1, xi1);
	    fft.inverse(xr1, xi1, xr1, xi1);
	    for (int i = 0; i < 128; i++) {
		if (Math.abs(xr[i] - xr1[i]) > 1.e-10
		    || Math.abs(xi[i] - xi1[i]) > 1.e-10) {
		    System.out.println("bad result for mode SYMMETRIC");
		    System.exit(1);
		}
	    }


	    for (int i = 0; i < 128; i++) {
		xr1[i] = xr[i];
		xi1[i] = xi[i];
	    }
	    fft = FFT.newInstance(128, FFT.Mode.REVERSED);
	    fft.transform(xr1, xi1, xr1, xi1);
	    fft.inverse(xr1, xi1, xr1, xi1);
	    for (int i = 0; i < 128; i++) {
		if (Math.abs(xr[i] - xr1[i]) > 1.e-10
		    || Math.abs(xi[i] - xi1[i]) > 1.e-10) {
		    System.out.println("bad result for mode REVERSED");
		    System.exit(1);
		}
	    }
	}

	fftf.setName("bzdevFFT");
	fftf.setCacheSize(1);

	System.out.println("test convolution");

	int r2cnt = 0;
	for (int N = 20; N < 256; N++) {
	    for (int M = 20; M < 256; M++) {
		for (int ind = 0; ind < 7; ind++) {
		    int gz = M-1;
		    switch (ind) {
		    case 0: gz = 0; break;
		    case 1: gz = M/6; break;
		    case 2: gz = M/3; break;
		    case 3: gz = M/2; break;
		    case 4: gz = 2*M/3; break;
		    case 5: gz = 5*M/6; break;
		    }
		    double[] f = new double[N];
		    double[] g = new double[M];
		    double[] result = new double[N];
		    double[] result2 = new double[N];
		    double[] expected = new double[N];
		    for (int i = 0; i < N; i++) {
			f[i] = r.nextDouble();
		    }
		    for (int i = 0; i < M; i++) {
			g[i] = r.nextDouble();;
		    }

		    for (int k = 0; k < N; k++) {
			for (int i = 0; i < N; i++) {
			    int l = k - i + gz;
			    if (l < 0 || l >= g.length) continue;
			    expected[k] += f[i]*g[l];
			}
		    }
		    FFT.convolve(f, g, gz, result);
		    if ((r2cnt++) < 10) {
			FFT.convolve(fftf, f, g, gz, result2);
			for (int i = 0; i < result.length; i++) {
			    if (result[i] != result2[i]) {
				System.out.println("result and result2 differ");
				System.exit(1);
			    }
			}
		    }

		    for (int i = 0; i < N; i++) {
			if (Math.abs(expected[i] - result[i]) > 1.e-10) {
			    System.out.println("at i = " + i
					       + ", expected[i] = "
					       + expected[i]
					       + ", result[i] = " + result[i]
					       + ", ratio = "
					       + (expected[i] / result[i]));
			    System.out.println("... failed when N ="
					       + N + ", M = " + M
					       + ", gz = " + gz);
			    System.exit (1);
			}
		    }
		}
	    }
	}

	System.out.println("test cyclic convolution");

	r2cnt = 0;
	for (int N = 20; N < 256; N++) {
	    for (int M = 20; M <= N; M++) {
		for (int ind = 0; ind < 7; ind++) {
		    int gz = M-1;
		    switch (ind) {
		    case 0: gz = 0; break;
		    case 1: gz = M/6; break;
		    case 2: gz = M/3; break;
		    case 3: gz = M/2; break;
		    case 4: gz = 2*M/3; break;
		    case 5: gz = 5*M/6; break;
		    }
		    double[] f = new double[N];
		    double[] g = new double[M];
		    double[] result = new double[N];
		    double[] result2 = new double[N];
		    double[] expected = new double[N];
		    for (int i = 0; i < N; i++) {
			f[i] = r.nextDouble();
		    }
		    for (int i = 0; i < M; i++) {
			g[i] = r.nextDouble();;
		    }

		    for (int k = 0; k < N; k++) {
			for (int i = 0; i < N; i++) {
			    int l = k - i + gz + N;
			    l = l % N;
			    if (l >= g.length) continue;
			    expected[k] += f[i]*g[l];
			}
		    }
		    FFT.cyclicConvolve(f, g, gz, result);
		    if ((r2cnt++) < 10) {
			FFT.cyclicConvolve(fftf, f, g, gz, result2);
			for (int i = 0; i < result.length; i++) {
			    if (result[i] != result2[i]) {
				System.out.println("result and result2 differ");
				System.exit(1);
			    }
			}
		    }
		    for (int i = 0; i < N; i++) {
			if (Math.abs(expected[i] - result[i]) > 1.e-10) {
			    System.out.println("at i = " + i
					       + ", expected[i] = "
					       + expected[i]
					       + ", result[i] = " + result[i]
					       + ", ratio = "
					       + (expected[i] / result[i]));
			    System.out.println("... failed when N ="
					       + N + ", M = " + M
					       + ", gz = " + gz);
			    System.exit (1);
			}
		    }
		}
	    }
	}

	System.out.println("test cross correlation");

	r2cnt = 0;
	for (int N = 20; N < 256; N++) {
	    for (int M = N/3; M < 256; M++) {
		double[] f = new double[N];
		double[] g = new double[M];
		double[] result = new double[N+M-1];
		double[] result2 = new double[N+M-1];
		double[] expected = new double[N + M - 1];
		if  (N == 20 && M == 6) {
		    for (int i = 0; i < N; i++) {
			f[i] = 1.0;
		    }
		    for (int i = 0; i < M; i++) {
			g[i] = 1.0;
		    }
		} else {
		    for (int i = 0; i < N; i++) {
			f[i] = r.nextDouble();
		    }
		    for (int i = 0; i < M; i++) {
			g[i] = r.nextDouble();
		    }
		}
		int nlower = -f.length + 1;
		int nupper = g.length;

		for (int n = nlower; n < nupper; n++) {
		    for (int m  = 0; m < f.length; m++) {
			int mpn = m+n;
			if (mpn < 0 || mpn >= g.length) continue;
			expected[n - nlower] += f[m]*g[m+n];
		    }
		}

		FFT.crossCorrelate(f, g, result);
		if ((r2cnt++) < 10) {
		    FFT.crossCorrelate(fftf, f, g, result2);
		    for (int i = 0; i < result.length; i++) {
			if (result[i] != result2[i]) {
			    System.out.println("result and result2 differ");
			    System.exit(1);
			}
		    }
		}
		boolean error = false;
		for (int i = 0; i < N+M-1; i++) {
		    if (Math.abs(expected[i] - result[i]) > 1.e-10) {
			System.out.println("at i = " + i + ", expected[i] = "
					   + expected[i] + ", result[i] = "
					   + result[i]
					   + ", ratio = "
					   + (expected[i] / result[i]));
			error = true;
		    }
		}
		if (error) System.exit(1);
	    }
	}

	System.out.println("test cyclic cross correlation");

	{
	    double[] f = new double[32];
	    double[] g = new double[6];
	    double[] result = new double[32];
	    double[] expected = new double[32];
	    for (int i = 0; i < 32; i++) {
		f[i] = i + 1;
	    }
	    for (int i = 0; i < 6; i++) {
		g[i] = i + 1;
	    }
	    for (int n = 0; n < 32; n++) {
		for (int m  = 0; m < 32; m++) {
		    int mpn = (m+n) % 32;
		    if (mpn >= g.length) continue;
		    expected[n] += f[m]*g[mpn];
		}
	    }
	    FFT.cyclicCrossCorrelate(f, g, result);
	    boolean error = false;
	    for (int i = 0; i < 32; i++) {
		if (Math.abs(expected[i] - result[i]) > 1.e-10) {
		    error = true;
		    System.out.println("at i = " + i + ", expected[i] = "
				       + expected[i] + ", result[i] = "
				       + result[i]
				       + ", ratio = "
				       + (expected[i] / result[i]));
		}
	    }
	    if (error) System.exit(1);
	}

	r2cnt = 0;
	for (int N = 20; N < 256; N++) {
	    for (int M = N/3; M < N; M++) {
		double[] f = new double[N];
		double[] g = new double[M];
		double[] result = new double[N];
		double[] result2 = new double[N];
		double[] expected = new double[N];
		if  (N == 20 && M == 6) {
		    for (int i = 0; i < N; i++) {
			f[i] = i+1.0;
		    }
		    for (int i = 0; i < M; i++) {
			g[i] = i+1.0;
		    }
		} else {
		    for (int i = 0; i < N; i++) {
			f[i] = r.nextDouble();
		    }
		    for (int i = 0; i < M; i++) {
			g[i] = r.nextDouble();
		    }
		}

		for (int n = 0; n < N; n++) {
		    for (int m  = 0; m < f.length; m++) {
			int mpn = m+n + N;
			mpn = mpn % N;
			if (mpn >= g.length) continue;
			expected[n] += f[m]*g[mpn];
		    }
		}

		FFT.cyclicCrossCorrelate(f, g, result);
		if ((r2cnt++) < 10) {
		    FFT.cyclicCrossCorrelate(fftf, f, g, result2);
		    for (int i = 0; i < result.length; i++) {
			if (result[i] != result2[i]) {
			    System.out.println("result and result2 differ");
			    System.exit(1);
			}
		    }
		}

		boolean error = false;
		for (int i = 0; i < N; i++) {
		    if (Math.abs(expected[i] - result[i]) > 1.e-10) {
			System.out.println("at i = " + i + ", expected[i] = "
					   + expected[i] + ", result[i] = "
					   + result[i]
					   + ", ratio = "
					   + (expected[i] / result[i]));
			error = true;
		    }
		}

		if (error) {
		    System.out.println("N = " + N + ", M = " + M);
		    System.exit(1);
		}
	    }
	}


	System.out.println("auto-correlation test");
	{
	    double f[] = new double[200];
	    for (int i = 0; i < 200; i++) {
		f[i] = r.nextDouble() - 0.5;
	    }
	    double result[] = new double[400-1];
	    FFT.crossCorrelate(f, f, result);
	    int zind = 200-1;
	    for (int i = -3; i < 4; i++) {
		System.out.format("result[%d] = %g\n", zind+i,
				  result[zind+i]);
	    }
	}
    }
}
