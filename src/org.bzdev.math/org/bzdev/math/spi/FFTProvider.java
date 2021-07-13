package org.bzdev.math.spi;
import org.bzdev.math.FFT;

/**
 * FFT service provider interface.
 * Classes that implement this interface will provide a name used
 * by various FFT methods to create a specific FFT implementation or
 * to determine such an implementation's properties. In addition,
 * the provider's class name must appear in a resource named
 * META-INF/services/org.bzdev.math.spi.FFTProvider that appears in
 * a class library's JAR file.
 * For a modular JAR file, module-info.java should contain the following
 * clauses in its module declaration:
 * <BLOCKQUOTE><CODE><PRE>
 *     uses org.bzdev.math.spi.FFTProvider;
 *     provides org.bzdev.math.spi.FFTProvider with ...;
 * </PRE></CODE></BLOCKQUOTE>
 * where "<CODE>...</CODE>" is a comma-separated list of the fully
 * qualified class names of the FFT providers supplied by this module.
 * <P>
 * Note that the method {@link FFTProvider#getFFTClass()} does not take
 * any arguments. A consequence of this is that each provider must be
 * paired with a single FFT class.
 */
public interface FFTProvider {

    /**
     * Get the name of this provider.
     * The name bzdevFFT is reserved for use by a default FFT
     * implementation provided with this library. The implementation
     * of a class that implements this interface is responsible for
     * avoiding name conflicts.
     * @return the provider's name
     */
    String getFFTName();

    /**
     * Get the class for the FFT implementation supported by this
     * provider.  The class returned must be a subclass of
     * {@link org.bzdev.math.FFT} and must have a public two-argument
     * constructor whose first argument is an int giving the array
     * sizes for the input and output arrays passed to the FFT's transform
     * and inverse-transform methods, and whose second argument is
     * a normalization mode whose type is {@link org.bzdev.math.FFT.Mode}.
     * The class returned must also have a two-argument constructor
     * whose first argument's type is that of the class returned and
     * whose second argument is a normalization mode whose type is
     * {@link org.bzdev.math.FFT.Mode}. This second constructor may
     * simply look up the length for an existing FFT but may also
     * arrange for the new FFT instance to share some of its internal
     * data structures with the existing instance.
     * @return the class of the FFT implementation associated with this
     *         provider
     */
    Class<? extends FFT> getFFTClass();

    /**
     * Get the maximum length for transform and inverse-transform
     * array arguments.
     * @return the maximum length supported by this provider.
     */
    int getMaxLength();

    /**
     * Get the maximum length for transform and inverse-transform
     * array arguments depending on whether in-place transforms are
     * required.
     * <P>
     * Unless overridden by a class implementing this interface,
     * the value returned is the same as the value returned by
     * {@link #getMaxLength()}.
     * @param inplace true if in-place transforms must be supported;
     *        false if in-place transforms are not required
     * @return the maximum length supported by this provider.
     */
    default int getMaxLength(boolean inplace) {
	return getMaxLength();
    }

    /**
     * Get the smallest length for transform and inverse-transform
     * array arguments that is at least as large as a specified
     * minimum length.
     * @param  n the minimum
     * @return the smallest length supported by this provider that is
     *         at least as large as the minimum length argument n
     */
    int getLength(int n);

    /**
     * Determine if the FFT associated with this provider supports
     * in-place transforms and inverse transforms for all of its
     * supported array lengths.
     * @return true if in-place transforms and inverse transforms are
     *         supported; false otherwise
     */
    boolean inplaceSupported();

    /**
     * Determine if the FFT associated with this provider supports
     * in-place transforms and inverse transforms for a specified
     * array length.
     * <P>
     * If not overridden, this method returns the value returned
     * by {@link #inplaceSupported()}. If the argument is not a
     * supported array length, the next highest array length that
     * is supported is used. If the array length is out of range
     * (negative or larger than the value returned by
     * {@link #getMaxLength()}), the returned value is arbitrary.
     * @param n the specified array length
     * @return true if this provider supports in-place transforms for
     *         the array length n; false if it does not
     */
    default boolean inplaceSupported(int n) {
	return inplaceSupported();
    }
}

//  LocalWords:  FFT FFTProvider getFFTClass bzdevFFT FFT's fft
//  LocalWords:  getMaxLength inplace inplaceSupported
