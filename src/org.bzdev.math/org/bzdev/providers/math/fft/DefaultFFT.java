package org.bzdev.providers.math.fft;


import org.bzdev.math.FFT;
import org.bzdev.math.FFT.Mode;
import org.bzdev.util.SafeFormatter;
import java.util.ResourceBundle;

import java.security.*;

//@exbundle org.bzdev.math.lpack.Math


/**
 * Default FFT implementation.
 * This class implements the Cooley-Tukey FFT algorithm.  it is based
 * on a public-domain implementation provided by Orlando Selenu, but
 * with significant modifications to improve performance, and with a
 * different definition of forward and inverse transforms.  The
 * resource org/bzdev/providers/math/fft/DefaultFFT.txt contains
 * details as to the differences. Essentially, this implementation's
 * constructor creates tables for trigonometric functions and for bit
 * reversals, using multiple threads to speed up the computation.  For
 * small array lengths, these tables are precomputed.
 */
public class DefaultFFT extends FFT {

    private static ResourceBundle exbundle = ResourceBundle.getBundle
	("org.bzdev.math.lpack.Math");

    static String errorMsg(String key, Object... args) {
	return (new SafeFormatter()).format(exbundle.getString(key), args)
	    .toString();
    }


    int n;
    int nu;
    int[] p;
    double[] c;
    double[] s;

    Mode mode = Mode.SYMMETRIC;

    private Runnable r1 = new Runnable() {
	    public void run() {
		int n2 = n / 2;
		int nu1 = nu - 1;
		int ttmnu = 32 - nu;
		int k = 0;
		while (k < n) {
		    if (p[k] == -1) {
			// int r = bitreversalOf(k, nu);
			int r = Integer.reverse(k) >>> ttmnu;
			p[k] = r;
		    }
		    k++;
		}
	    };
	};


    private Runnable r2 = new Runnable() {
	    public void run() {
		int k = 0;
		int n2 = n / 2;
		int nu1 = nu - 1;
		int ttmnu = 32 - nu;
		double delta = -(2.0 * Math.PI) / n;
		k = 0;
		for (int l = 1; l <= nu; l++) {
		    while (k < n) {
			for (int i = 1; i <= n2; i++) {
			    int index = k >> nu1;
			    int pval = p[index];
			    if (pval == -1) {
				// pval = bitreversalOf(index, nu);
				pval = Integer.reverse(index) >>> ttmnu;
				p[index] = pval;
			    }
			    double arg = delta * pval;
			    c[pval] = Math.cos(arg);
			    k++;
			}
			k += n2;
		    }
		    k = 0;
		    nu1--;
		    n2 /= 2;
		}
	    }
	};

    private Runnable r3 = new Runnable() {
	    public void run() {
		int k = 0;
		int n2 = n / 2;
		int nu1 = nu - 1;
		int ttmnu = 32 - nu;
		double delta = -(2.0 * Math.PI) / n;
		k = 0;
		for (int l = 1; l <= nu; l++) {
		    while (k < n) {
			for (int i = 1; i <= n2; i++) {
			    int index = k >> nu1;
			    int pval = p[index];
			    if (pval == -1) {
				// pval = bitreversalOf(index, nu);
				pval = Integer.reverse(index) >>> ttmnu;
				p[index] = pval;
			    }
			    double arg = delta * pval;
			    s[pval] = Math.sin(arg);
			    k++;
			}
			k += n2;
		    }
		    k = 0;
		    nu1--;
		    n2 /= 2;
		}
	    }
	};


        private Runnable r23 = new Runnable() {
	    public void run() {
		int k = 0;
		int n2 = n / 2;
		int nu1 = nu - 1;
		int ttmnu = 32 - nu;
		double delta = -(2.0 * Math.PI) / n;
		k = 0;
		for (int l = 0; l < nu; l++) {
		    while (k < n) {
			for (int i = 0; i < n2; i++) {
			    int index = k >> nu1;
			    int pval = p[index];
			    if (pval == -1) {
				// pval = bitreversalOf(index, nu);
				pval = Integer.reverse(index) >>> ttmnu;
				p[index] = pval;
			    }
			    double arg = delta * pval;
			    c[pval] = Math.cos(arg);
			    s[pval] = Math.sin(arg);
			    k++;
			}
			k += n2;
		    }
		    k = 0;
		    nu1--;
		    n2 /= 2;
		}
	    }
	};

    private static class State {
	State(int n) {
	    p = new int[n];
	    c = new double[n];
	    s = new double[n];
	}
	int[] p;
	double[] c;
	double[] s;
    }

    static final State[] table = new State[15];
    static {
	for (int j = 0; j < 15; j++) {
	    int n = 2 << j;
	    int m = n;
	    int nu = 0;
	    while (m > 1) {
		m = m >> 1;
		nu++;
	    }
	    table[j] = new State(n);
	    int k = 0;
	    int n2 = n / 2;
	    int nu1 = nu - 1;
	    int ttmnu = 32 - nu;
	    double delta = -(2.0 * Math.PI) / n;
	    while (k < n) {
		// int r = bitreversalOf(k, nu);
		int r = Integer.reverse(k) >>> ttmnu;
		table[j].p[k] = r;
		k++;
	    }
	    k = 0;
	    for (int l = 0; l < nu; l++) {
		while (k < n) {
		    for (int i = 0; i < n2; i++) {
			int index = k >> nu1;
			int pval = table[j].p[index];
			double arg = delta * pval;
			table[j].c[pval] = Math.cos(arg);
			table[j].s[pval] = Math.sin(arg);
			k++;
		    }
		    k += n2;
		}
		k = 0;
		nu1--;
		n2 /= 2;
	    }
	}
    }

    @Override
    public int getLength() {return n;}

    @Override
    public boolean inplace() {return true;}

    /**
     * Constructor.
     * The argument n must be a non-negative integral power of 2.
     * @param n the length of the arrays representing the real and imaginary
     *        components of the input and output for a transform
     * @param mode the normalization mode ({@link Mode#NORMAL},
     *        {@link Mode#SYMMETRIC},r {@link Mode#REVERSED})
     */
    public DefaultFFT(int n, Mode mode) throws IllegalArgumentException {
	super(mode);
	if (n < 1) {
	    throw new IllegalArgumentException(errorMsg("tooSmall", 1));
	}
	this.n = n;
	if (n == 1) return;
	int m = n;
	int nu = 0;
	while (m > 1) {
	    if ((m & 1) == 1) {
		throw new IllegalArgumentException(errorMsg("notPowerOf2", 1));
	    }
	    m = m >> 1;
	    nu++;
	}
	this.nu = nu;
	if (nu-1 < table.length) {
	    int index = nu-1;
	    p = table[index].p;
	    c = table[index].c;
	    s = table[index].s;
	} else {
	    p = new int[n];
	    c = new double[n];
	    s = new double[n];
	    java.util.Arrays.fill(p, -1);
	    int nproc = Runtime.getRuntime().availableProcessors();
	    // We could set a minimum value of n before multithreading
	    // is used to account for the cost of starting a thread,
	    // but tests indicated that this is not necessary as the
	    // transition point was below the maximum value for which
	    // tables are precomputed.
	    switch(nproc) {
	    case 1:
		r1.run();
		r23.run();
		break;
	    case 2:
		{
		    Thread t23 = new Thread(r23);
		    t23.start();
		    r1.run();
		    try {
			t23.join();
		    } catch (Exception e) {};
		}
		break;
	    default:
		{
		    Thread t2 = new Thread(r2);
		    Thread t3 = new Thread(r3);
		    t2.start();
		    t3.start();
		    r1.run();
		    try {
			t2.join();
			t3.join();
		    } catch (Exception e) {}
		}
		r1 = null;
		r2 = null;
		r3 = null;
	    }
	}
	this.mode = getMode();
    }

    /**
     * Constructor using an existing FFT.
     * The argument n must be a non-negative integral power of 2.
     * @param fft an existing FFT
     * @param mode the normalization mode ({@link Mode#NORMAL},
     *        {@link Mode#SYMMETRIC},r {@link Mode#REVERSED})
     * @exception the first argument was null
     */
    public DefaultFFT(DefaultFFT fft, Mode mode)
	throws IllegalArgumentException
    {
	super(mode);
	if (fft == null) {
	    throw new IllegalArgumentException(errorMsg("nullArg"));
	}
	this.n = fft.getLength();
	p = fft.p;
	c = fft.c;
	s = fft.s;
	nu = fft.nu;
	this.mode = getMode();
    }

    private void transform(double[] xReal, double[] xImag, boolean forward)
	throws IllegalArgumentException
    {
	if (n == 1) return;
	if (xReal.length < n) {
	    throw new IllegalArgumentException(errorMsg("tooShort", 1));
	}
	if (xImag.length < n) {
	    throw new IllegalArgumentException(errorMsg("tooShort", 2));
	}
	int n2 = n / 2;
	int nu1 = nu - 1;
	int k = 0;
	// First phase
	if (forward) {
	    for (int l = 0; l < nu; l++) {
		while (k < n) {
		    for (int i = 0; i < n2; i++) {
			int index = k >> nu1;
			int pval = p[index];
			double cc = c[pval];
			double ss = -s[pval];
			Double tReal = xReal[k + n2] * cc + xImag[k + n2] * ss;
			double tImag = xImag[k + n2] * cc - xReal[k + n2] * ss;
			xReal[k + n2] = xReal[k] - tReal;
			xImag[k + n2] = xImag[k] - tImag;
			xReal[k] += tReal;
			xImag[k] += tImag;
			k++;
		    }
		    k += n2;
		}
		k = 0;
		nu1--;
		n2 /= 2;
	    }
	} else {
	    for (int l = 0; l < nu; l++) {
		while (k < n) {
		    for (int i = 0; i < n2; i++) {
			int index = k >> nu1;
			int pval = p[index];
			double cc = c[pval];
			double ss = s[pval];
			double tReal = xReal[k + n2] * cc + xImag[k + n2] * ss;
			double tImag = xImag[k + n2] * cc - xReal[k + n2] * ss;
			xReal[k + n2] = xReal[k] - tReal;
			xImag[k + n2] = xImag[k] - tImag;
			xReal[k] += tReal;
			xImag[k] += tImag;
			k++;
		    }
		    k += n2;
		}
		k = 0;
		nu1--;
		n2 /= 2;
	    }
	}
	// Second phase - recombination
	k = 0;
	int r;
	while (k < n) {
	    r = p[k];
	    if (r > k) {
		double tReal = xReal[k];
		double tImag = xImag[k];
		xReal[k] = xReal[r];
		xImag[k] = xImag[r];
		xReal[r] = tReal;
		xImag[r] = tImag;
	    }
	    k++;
	}
	// normalize the output.
	double radice = 1.0;
	switch (mode) {
	case NORMAL:
	    if (forward == false) {
		radice /= n;
	    }
	    break;
	case SYMMETRIC:
	    radice /= Math.sqrt(n);
	    break;
	case REVERSED:
	    if (forward) {
		radice /= n;
	    }
	    break;
	}
	k = 0;
	while (k < n) {
	    xReal[k] *= radice;
	    xImag[k] *= radice;
	    k++;
	}
    }

    @Override
    public void transform(double[] xiReal, double[] xiImag,
			  double[] xoReal, double[] xoImag)
    {
	if (xiReal != xoReal) {
	    System.arraycopy(xiReal, 0, xoReal, 0, n);
	}
	if (xiImag != xoImag) {
	    System.arraycopy(xiImag, 0, xoImag, 0, n);
	}
	transform(xoReal, xoImag, true);
    }

    @Override
    public void inverse(double[] xiReal, double[] xiImag,
			double[] xoReal, double[] xoImag)
    {
	if (xiReal != xoReal) {
	    System.arraycopy(xiReal, 0, xoReal, 0, n);
	}
	if (xiImag != xoImag) {
	    System.arraycopy(xiImag, 0, xoImag, 0, n);
	}
	transform(xoReal, xoImag, false);
    }


    /*
     * reverse the bits of j, restricted to the low
     * order bits up to bit log_2(nu)
    private static int bitreversalOf(int j, int nu) {
	int j2;
	int j1 = j;
	int k = 0;
	for (int i = 1; i <= nu; i++) {
	    j2 = j1 / 2;
	    k = 2 * k + j1 - 2 * j2;
	    j1 = j2;
	}
	return k;
    }
    */
}

