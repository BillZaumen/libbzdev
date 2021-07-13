package org.bzdev.providers.math.fft;
import org.bzdev.math.spi.FFTProvider;
import org.bzdev.math.FFT;

//@exbundle org.bzdev.math.lpack.Math

public class DefaultFFTProvider implements FFTProvider {

    static String errorMsg(String key, Object... args) {
	return DefaultFFT.errorMsg(key, args);
    }

    @Override
    public String getFFTName() {return "bzdevFFT";}

    @Override
    public Class<? extends FFT> getFFTClass() {
	return org.bzdev.providers.math.fft.DefaultFFT.class;
    }

    @Override
    public int getMaxLength() {
	return 1 << 30;
    }

    @Override
    public int getLength(int n) {
	if (n < 0) throw new IllegalArgumentException
		       (errorMsg("argNonNegative", n));
	if (n == 0) return 0;
	int m = 1;

	while (m > 0 && m < n) {
	    m = m << 1;
	}
	if (m < 0) throw new IllegalArgumentException
		       (errorMsg("firstArgTooLarge", n));
	return m;

    }

    @Override
    public boolean inplaceSupported() {
	return true;
    }
}
