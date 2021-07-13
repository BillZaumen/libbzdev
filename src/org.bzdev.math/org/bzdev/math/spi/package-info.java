/**
 * Mathematics package service provider interfaces.
 * <P>
 * This package contains service provider interfaces for
 * third-party classes. Currently  there is a single provider:
 * for fast Fourier transforms. There is a build-in provider
 * to supply a default implementation, but users may want to
 * add other providers: some can take advantage of graphics
 * co-processors or allow FTT's for different data-set sizes.
 * The default FFT provider is based on a public-domain
 * implementation provided by Orlando Selenu, but with significant
 * changes to improve its performance. These changes are documented
 * in the file 
 * <A href="../../providers/math/fft/doc-files/DefaultFFT.txt">DefaultFFT.txt</A>.
 */
package org.bzdev.math.spi;

//  LocalWords:  FTT's FFT Selenu href
