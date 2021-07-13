import org.bzdev.math.*;
import org.bzdev.providers.math.fft.DefaultFFT;
import org.bzdev.math.rv.*;

public class FFTTiming {
    public static void main(String argv[]) throws Exception {
	java.security.SecureRandom r = new java.security.SecureRandom();
	for (int i = 0; i < 200; i++) {
	    for (int n = 1; n < (1 << 16); n = n << 1) {
		double[] xr = new double[n];
		double[] xi = new double[n];
		// FFT fft = FFT.newInstance(n);
		FFT fft = new DefaultFFT(n, FFT.Mode.SYMMETRIC);
		fft.transform(xr, xi, xr, xi);
		FFTbase.fft(xr, xi, false);
	    }
	}
	System.out.println("Warmup period ended");
	for (int n = 1; n < (1 << 24); n = n << 1) {
	    double[] xr = new double[n];
	    double[] xi = new double[n];
	    
	    long time1 = System.nanoTime();
	    FFTbase.fft(xr, xi, false);
	    long time2 = System.nanoTime();
	    // FFT fft = FFT.newInstance(n);
	    FFT fft = new DefaultFFT(n, FFT.Mode.SYMMETRIC);
	    long time3 = System.nanoTime();
	    fft.transform(xr, xi, xr, xi);
	    long time4 = System.nanoTime();

	    System.out.println("For n = " + n + ", Base: " + (time2 - time1)
			       + ", FFT setup: " + (time3 - time2)
			       + ", FFT transform: " + (time4 - time3));
	    
	}
    }
}
