package org.bzdev.math;
import org.bzdev.math.spi.FFTProvider;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Method;
import java.security.*;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

//@exbundle org.bzdev.math.lpack.Math

/**
 * Fast Fourier Transform.
 * This class uses an SPI (Service Provider Interface) to support
 * multiple FFT implementations.
 * The default implementation can be set by setting the
 * system property org.bzdev.math.fft.provider to the name of the
 * provider. If this is not set, the default name is bzdevFFT and
 * is equivalent to
 * <blockquote><pre><code>
 *    java -Dorg.bzdev.math.fft=bzdevFFT ...
 * </CODE></PRE></blockquote>
 * <P>
 * The method {@link FFT#getServiceName(int,boolean,FFT.LMode)} can
 * be used to select a service provider. Alternatively, the method
 * {@link FFT#serviceProviders()} will provide a set of all the
 * available service-provider names. The methods
 * {@link FFT#newInstance(int)} and {@link FFT#newInstance(int,FFT.Mode)}
 * will create an instance of FFT using the default service provider.
 * The first argument for these methods should be a length returned by
 * {@link FFT#getLength(int)} or {@link FFT#getLength(String,int)}. The
 * integer argument for these methods is a desired array length and
 * the value returned is the smallest length an FFT provider supports
 * that is at least as large as the length specified by the integer
 * argument. Similarly, the methods
 * {@link FFT#newInstance(String,int)} and
 * {@link FFT#newInstance(String,int,FFT.Mode)} will create an instance of
 * FFT using a service provider provider whose name matches the these
 * method's first argument.
 * <P>
 * {@link org.bzdev.math.spi.FFTProvider} describes how to write an FFT
 * provider. The <A HREF="doc-files/DefaultFFT.txt">default provider</A>
 * requires that the array lengths are powers of 2.
 */
public abstract class FFT {

    static String errorMsg(String key, Object... args) {
	return MathErrorMsg.errorMsg(key, args);
    }

    private static FFTProvider defaultProvider =
	new org.bzdev.providers.math.fft.DefaultFFTProvider();


    private static LinkedHashMap<String,FFTProvider>
	map = new LinkedHashMap<>();

    static {
	AccessController.doPrivileged
	    (new PrivilegedAction<Void>() {
		    public Void run() {
			String defaultProviderName =
			    System.getProperty("org.bzdev.math.fft");
			ServiceLoader<FFTProvider> loader =
			    ServiceLoader.load(FFTProvider.class);
			for (FFTProvider provider: loader) {
			    String name = provider.getFFTName();
			    if (name == null) continue;
			    if (!map.containsKey(name)) {
				if (provider.getClass().getName()
				    .equals(defaultProviderName)) {
				    defaultProvider = provider;
				}
				map.put(name, provider);
			    }
			}
			return (Void) null;
		    }
		});
    }

    /**
     * Get the service-provider names of all FFT providers on the
     * class path.
     * @return a set of service-provider names 
     */
    public static Set<String> serviceProviders() {
	return Collections.unmodifiableSet(map.keySet());
    }

    /**
     * FFT length mode.
     * This enumeration type is used to control a search for
     * an FFT provider.
     * @see FFT#getServiceName(int,boolean,LMode)
     */
    public enum LMode {
	/**
	 * Indicate that a length argument represents the
	 * desired length for the real and imaginary arrays
	 * used as inputs or outputs to a transform.
	 * Subject to other constraints, 
	 * {@link FFT#getServiceName(int,boolean,LMode)} will
	 * choose an FFT provider that can provide a transform
	 * with a length as large or larger than the desired
	 * length, but as close to the desired length as possible.
	 */
	DESIRED_LENGTH,
	/**
	 * Indicate that the length argument represents a
	 * a length that the FFT provider must support.
	 * Subject to other constraints, 
	 * {@link FFT#getServiceName(int,boolean,LMode)} will
	 * choose an FFT provider that can provide a transform
	 * with a length with the specified value.
	 */
	EXACT_LENGTH,
	/**
	 * Indicate that a length argument represents the
	 * maximum length for the real and imaginary arrays
	 * used as inputs or outputs to a transform.
	 * Subject to other constraints, 
	 * {@link FFT#getServiceName(int,boolean,LMode)} will
	 * choose an FFT provider that can provide a transform
	 * with a length as large or larger than the maximum
	 * length.
	 */
	MAX_LENGTH
    }

    /**
     * Get the name of an FFT provider.
     * This name can be used as the first argument of the methods
     * {@link #newInstance(String,int)} and
     * {@link #newInstance(String,int,Mode)}.
     * When the lmode argument has the value {@link FFT.LMode#MAX_LENGTH}
     * and the inplace argument's value is <code>true</code>, the
     * FTP provider must support in-place transforms for all of the
     * array lengths it supports. When the lmode argument's value is
     * {@link FFT.LMode#DESIRED_LENGTH} and the inplace argument's value
     * is <code>true</code>, the FFT provider must support in-place
     * transforms for the lowest supported length larger than the specified
     * length. When the lmode argument's value is {@link FFT.LMode#EXACT_LENGTH}
     * and the in place argument's value is <code>true</code>, the FFT
     * provider must support in-place transforms for the length that
     * was specified.
     * @param length the maximum or desired length of the arrays
     *       storing the real or imaginary components used for input
     *       or output.
     * @param inplace true if the user requires an FFT implementation
     *        that computes a transform or its inverse in place; false
     *        otherwise
     * @param lmode {@link FFT.LMode#DESIRED_LENGTH} if the length argument
     *        represents a desired length; {@link FFT.LMode#MAX_LENGTH} if
     *        the length argument is an upper bound on the array lengths;
     *        {@link FFT.LMode#EXACT_LENGTH} if the length argument represents
     *        a specific length
     * @return the name of an FFT service; null if none meet the constraints
     *         imposed by the arguments
     */
    public static String getServiceName(int length, boolean inplace,
					LMode lmode)
    {
	if (lmode == null)
	    throw new IllegalArgumentException(errorMsg("nullArg"));
	int lastLength = Integer.MAX_VALUE;
	String choice;
	String llname = null;
	for (Map.Entry<String,FFTProvider> entry: map.entrySet()) {
	    String name = entry.getKey();
	    FFTProvider provider = entry.getValue();
	    boolean ok = true;
	    switch (lmode) {
	    case MAX_LENGTH:
		if (inplace) ok = provider.inplaceSupported();
		if (provider.getMaxLength() < length) ok = false;
		if (ok) return name;
		break;
	    case EXACT_LENGTH:
		if (provider.getMaxLength() < length
		    || provider.getLength(length) != length) {
		    ok = false;
		}
		if (inplace) {
		    if (ok && provider.inplaceSupported(length)) {
			return name;
		    }
		} else if (ok) {
		    return name;
		}
		break;
	    case DESIRED_LENGTH:
		if (inplace) ok = provider.inplaceSupported
				 (provider.getLength(getLength(length)));
		if (provider.getMaxLength() < length) ok = false;
		if (ok) {
		    int len = provider.getLength(length);
		    if (len < lastLength || llname == null) {
			lastLength = len;
			llname = name;
		    }
		}
		break;
	    }
	}
	return llname;
    }

    /**
     * Get the largest array length that some FFT provider supports
     * subject to an optional in-place constraint
     * @param inplace true if an FFT must be able to use the same
     *        vectors for input and ouput; false otherwise.
     * @return the array length limit
     */
    public static int getMaxLength(boolean inplace) {
	int length = 0;
	for (Map.Entry<String,FFTProvider> entry: map.entrySet()) {
	    String name = entry.getKey();
	    FFTProvider provider = entry.getValue();
	    int plen = provider.getMaxLength(inplace);
	    if (plen > length) length = plen;
	}
	return length;
    }

    /**
     * Factory class for creating FFT instances that satisfy
     * various criteria. FFT factories can be configured to
     * cache previous results, with the cache having a finite
     * size. Constructing an FFT requires a potentially large
     * number of sin and cosine computations.  For some
     * applications, it is useful to store previously computed
     * FFTs.
     */
    public static class Factory {
	boolean useDefault = true;
	boolean useName = false;
	String serviceName = null;
	int length = FFT.getMaxLength();
	boolean inplace;
	LMode lmode = null;

	private int cacheSize = 0;
	 LinkedHashMap<Integer,FFT> cache =
	     new LinkedHashMap<Integer,FFT>() {
		 protected boolean removeEldestEntry(Map.Entry<Integer,FFT>
						     eldest)
		 {
		     return (size() > cacheSize);
		 }
	     };

	/**
	 * Set the cache size.
	 * <P>
	 * Calling this method will clear the cache, even if the argument
	 * matches the existing cache size.
	 * @param size the new cache size; 0 or negative for no caching
	 */
	public synchronized void setCacheSize(int size) {
	    cache.clear();
	    cacheSize = size;
	}


	/**
	 * Clear the cache.
	 * This will free up memory without changing the cache size.
	 */
	public synchronized void clearCache() {
	    cache.clear();
	}


	/**
	 * Specify the length, in-place, and length-mode that may be
	 * used by a factory.
	 * <P>
	 * This method replaces any configuration set by prior calls
	 * to it, and by prior calls to {@link #setName(String)}.
	 * If the third parameter is {@link FFT.LMode#MAX_LENGTH},
	 * this method will look up the service provider; otherwise the
	 * decision will be made during a call to {@link #newInstance(int)}.
	 * <P>
	 * Calling this method will clear the cache.
	 * @param length the maximum length of the arrays
	 *       storing the real or imaginary components used for
	 *       input or output; 0 or negative for modes other than
	 *        {@link FFT.LMode#MAX_LENGTH} if the length is ignored
	 * @param inplace true if the user requires an FFT
	 *        implementation that computes a transform or its
	 *        inverse in place; false otherwise
	 * @param mode {@link FFT.LMode#DESIRED_LENGTH} if the length
	 *        argument represents a desired length;
	 *        {@link FFT.LMode#MAX_LENGTH} if the length argument
	 *        is an upper bound on the array lengths;
	 *        {@link FFT.LMode#EXACT_LENGTH} if the length argument
	 *        represents a specific length
	 * @return true on success; false if there is no FFT service
	 *         that matches the specified parameters
	 * @exception IllegalArgumentException an argument was illegal
	 */
	public synchronized boolean setParameters(int length,
						  boolean inplace,
						  LMode mode)
	    throws IllegalArgumentException
	{
	    cache.clear();
	    if (mode == null) {
		throw new IllegalArgumentException
		    (errorMsg("nullFFTMode"));
	    }
	    if (mode == LMode.MAX_LENGTH) {
		if  (length <= 0) {
		    throw new IllegalArgumentException
			(errorMsg("firstArgNotPositive", length));
		}
		serviceName = FFT.getServiceName(length, inplace, mode);
		if (serviceName == null) return false;
	    } else {
		serviceName = null;
	    }
	    this.length = length;
	    this.inplace = inplace;
	    this.lmode = mode;
	    useDefault = false;
	    useName = false;
	    return true;
	}

	/**
	 * Set the name of the FFT service provider to use.
	 * Calling this method replaces any configuration set by
	 * prior calls to this method and by prior calls to
	 * {@link #setParameters(int,boolean,FFT.LMode)}.
	 * <P>
	 * Calling this method will also clear the cache.
	 * @param name the name of the provider; null if the default
	 *        provider  is to be used.
	 * @return true if the name is a valid name; false otherwise
	 */
	public synchronized boolean setName(String name) {
	    cache.clear();
	    if (name == null) {
		serviceName = null;
		useDefault = true;
		useName = false;
		lmode = null;
		length = FFT.getMaxLength();
		return true;
	    } else if (!FFT.map.containsKey(name)) {
		return false;
	    } else {
		serviceName = name;
		useName = true;
		useDefault = false;
		length = FFT.getMaxLength(name);
		lmode = null;
		return true;
	    }
	}

	/**
	 *  Get the maximum array length the service that meets
	 * this factory's constraints will suppport.
	 * @return the maximum length
	 */
	public int getMaxLength() {
	    if (useDefault) {
		return FFT.getMaxLength();
	    } else if (lmode == null) {
		return FFT.getMaxLength(serviceName);
	    } else {
		if (lmode == LMode.MAX_LENGTH) {
		    return FFT.getMaxLength(serviceName);
		} else {
		    return FFT.getMaxLength(inplace);
		}
	    }
	}

	/**
	 * Get the supported length for the fast Fourier transform
	 * that this factory's {@link #newInstance(int)} method will
	 * accept, given a desired length. The value returned is the
	 * array length for arguments passed to an FFT's transform
	 * and inverse methods.
	 * @param len the desired length
	 * @return the smallest supported length larger than or equal
	 *         to the desired length
	 */
	public int getLength(int len) {
	    if (len < 0 || len > length) {
		throw new IllegalArgumentException
		    (errorMsg("closedOutOfRangeI", 0, len, length));
	    }
	    if (useDefault) {
		return FFT.getLength(len);
	    } else if (lmode == null) {
		return FFT.getLength(serviceName, len);
	    } else {
		if (lmode == LMode.MAX_LENGTH) {
		    return FFT.getLength(serviceName, len);
		} else {
		    String sn = FFT.getServiceName(len, inplace, lmode);
		    if (sn == null) {
			throw new IllegalArgumentException
			    (errorMsg("noService"));
		    } else {
			return FFT.getLength(sn, len);
		    }
		}
	    }
	}

	/**
	 * Create a new instance of a Fast Fourier transform with the
	 * default normalization mode {@link FFT.Mode#SYMMETRIC}.
	 * The argument must be an array size supported by the FFT provider,
	 * and can be computed using the method {@link #getLength(int)}.
	 * @param len the array size for the transform that will be
	 *        created
	 * @see #getLength(int)
	 * @return the new FFT
	 */
	public FFT newInstance(int len) {
	    return newInstance(len, Mode.SYMMETRIC);
	}

	/**
	 * Create a new instance of a Fast Fourier transform with a
	 * specified normalization mode.
	 * The len must be an array size supported by the FFT provider.
	 * @param len the array size for the transform that will be
	 *        created
	 * @param m the normalization mode ({@link Mode#NORMAL},
	 *        {@link Mode#SYMMETRIC}, or {@link Mode#REVERSED})
	 * @return the new FFT
	 */
	public FFT newInstance(int len,  Mode m) {
	    if (len < 0 || ((length > 0) && (len > length))) {
		throw new IllegalArgumentException
		    (errorMsg("closedOutOfRangeI", 0, len, length));
	    }
	    FFT newFFT;
	    synchronized(this) {
		if (cacheSize > 0) {
		    FFT existingFFT = cache.get(len);
		    if (existingFFT != null) {
			newFFT = FFT.newInstance(existingFFT, m);
			cache.remove(existingFFT);
			cache.put(len, newFFT);
			return newFFT;
		    }
		}
	    }
	    if (m == null) m = Mode.SYMMETRIC;
	    if (useDefault) {
		newFFT = FFT.newInstance(len, m);
	    } else if (lmode == null) {
		newFFT = FFT.newInstance(serviceName, len, m);
	    } else {
		if (lmode == LMode.MAX_LENGTH) {
		    newFFT = FFT.newInstance(serviceName, len, m);
		} else {
		    String sn = FFT.getServiceName(len, inplace, lmode);
		    if (sn == null) {
			throw new IllegalArgumentException
			    (errorMsg("noService"));
		    } else {
			newFFT = FFT.newInstance(sn, len, m);
		    }
		}
	    }
	    synchronized(this) {
		if (cacheSize > 0) {
		    cache.put(len, newFFT);
		}
	    }
	    return newFFT;
	}
    }

    /**
     * FFT normalization mode.
     * <P>
     * <script>
     * MathJax = {
     *	  tex: {
     *	      inlineMath: [['$', '$'], ['\\(', '\\)']],
     *	      displayMath: [['$$', '$$'], ['\\[', '\\]']]}
     * };
     * </script>
     * <script id="MathJax-script" async
     *	    src="https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-chtml.js">
     * </script>
     * There are multiple conventions regarding how Fourier transforms
     * are defined.  For an FFT, these affect how a transform is
     * normalized. 
     */
    public enum Mode {
	/**
	 *  The transform is defined by
	 *  $X_k = \sum_{n=0}^{N-1} x_ne^{-2\pi kni/N}$,
	 *  <!-- X<sub>k</sub> = &sum;<sub>n</sub>x<sub>n</sub>e<sup>-i2&pi;kn/N</sup>,-->
	 * and the inverse transform is defined by
	 *  $x_n = \frac1N \sum_{k=0}^{N-1} X_ke^{2\pi kni/N}$,
	 *  <!-- x<sub>n</sub> = (1/N)&sum;<sub>k</sub>X<sub>k</sub>e<sup>i2&pi;kn/N</sup>, -->
	 * where the indices have values in the range [0, N-1).
	 * This is particularly useful when using an FFT to compute a
	 * convolution or cross correlation.
	 */
	NORMAL,
	/**
	 *  The transform is defined by
	 *  $X_k = \frac1{\sqrt{N}}\sum_{n=0}^{N-1} x_ne^{-2\pi kni/N}$,
	 *  <!-- X<sub>k</sub> = (1/&radic;<SPAN style="text-decoration: overline">N</SPAN>)&sum;<sub>n</sub>x<sub>n</sub>e<sup>-i2&pi;kn/N</sup>, -->
	 * and the inverse transform is defined by
	 *  $x_n = \frac1{\sqrt{N}}\sum_{k=0}^{N-1} X_ke^{2\pi kni/N}$,
	 *  <!-- x<sub>n</sub> = (1/&radic;<SPAN style="text-decoration: overline">N</SPAN>)&sum;<sub>k</sub>X<sub>k</sub>e<sup>i2&pi;kn/N</sup>, -->
	 * where the indices have values in the range [0, N-1).
	 * This is useful when one wants the norm of a vector and its
	 * transform to be identical, as is typically the case in
	 * physics applications.
	 */
	SYMMETRIC,
	/**
	 *  The transform is defined by
	 *  $X_k = \frac1N \sum_{n=0}^{N-1} x_ne^{-2\pi kni/N}$,
	 *  <!-- X<sub>k</sub> = (1/N)&sum;<sub>n</sub>x<sub>n</sub>e<sup>-i2&pi;kn/N</sup>, -->
	 * and the inverse transform is defined by
	 *  $x_n = \sum_{k=0}^{N-1} X_ke^{2\pi kni/N}$,
	 *  <!-- x<sub>n</sub> = &sum;<sub>k</sub>X<sub>k</sub>e<sup>i2&pi;kn/N</sup>, -->
	 * where the indices have values in the range [0, N-1).
	 * This is useful for statistics applications due to the
	 * definition of the characteristic function.
	 */
	REVERSED,
    };

    Mode mode = Mode.SYMMETRIC;

    /**
     * Get the closest length supported by the default FFT provider
     * for a specified length.
     * @param n the specified length
     * @return the closest length that is supported and that is larger
     *         than the specified length
     */
    public static int getLength(int n) {
	return defaultProvider.getLength(n);
    }

    /**
     * Get the closest length supported by an FFT provider
     * for a specified length.
     * @param name the name of the FFT provider
     * @param n the specified length
     * @return the closest length that is supported and that is larger
     *         than the specified length
     */
    public static int getLength(String name, int n)
	throws IllegalArgumentException
    {
	if (!map.containsKey(name)) {
	    throw new IllegalArgumentException
		(errorMsg("serviceName", "FFT", name));
	}
	FFTProvider provider = map.get(name);
	return provider.getLength(n);
    }

    /**
     * Determine if the default provider supports in-place
     * transforms for all supported input array lengths.
     * @return true if the default provider supports in-place
     * transforms; false otherwise
     */
    public static boolean inplaceSupported() {
	return defaultProvider.inplaceSupported();
    }

    /**
     * Determine if the default provider supports in-place
     * transforms for a specific input array length.
     * @param n the length of the input and output arrays
     * @return true if the default provider supports in-place
     * transforms; false otherwise
     */
    public static boolean inplaceSupported(int n) {
	return defaultProvider.inplaceSupported(n);
    }

    /**
     * Determine if an FFT provider supports in-place
     * transforms for all supported input array lengths.
     * @param name the name of the provider
     * @return true if the provider supports in-place
     * transforms; false otherwise
     */
    public static boolean inplaceSupported(String name)
	throws IllegalArgumentException
    {
	if (!map.containsKey(name)) {
	    throw new IllegalArgumentException
		(errorMsg("serviceName", "FFT", name));
	}
	FFTProvider provider = map.get(name);
	return provider.inplaceSupported();
    }

    /**
     * Determine if an FFT provider supports in-place
     * transforms for a specific supported input array length.
     * @param name the name of the provider
     * @param n the length of the input and output arrays
     * @return true if the provider supports in-place
     * transforms; false otherwise
     */
    public static boolean inplaceSupported(String name, int n)
	throws IllegalArgumentException
    {
	if (!map.containsKey(name)) {
	    throw new IllegalArgumentException
		(errorMsg("serviceName", "FFT", name));
	}
	FFTProvider provider = map.get(name);
	return provider.inplaceSupported(n);
    }

    /**
     * Determine if this FFT instance supports in-place
     * transforms.
     * @return true if this FFT in-place
     *         transforms; false otherwise
     */
    public abstract boolean inplace();

    /**
     * Get the maximum length that the default FFT provider supports.
     * @return the maximum length
     */
    public static int getMaxLength() {
	return defaultProvider.getMaxLength();
    }

    /**
     * Get the maximum length that an FFT provider supports.
     * @param name the name of the FFT provider
     * @return the maximum length
     */
    public static int getMaxLength(String name) {
	if (!map.containsKey(name)) {
	    throw new IllegalArgumentException
		(errorMsg("serviceName", "FFT", name));
	}
	FFTProvider provider = map.get(name);
	return provider.getMaxLength();
    }

    static final Mode defaultMode = Mode.SYMMETRIC;

    /**
     * Create a new instance of the FFT class using the default FFT
     * provider with the default mode ({@link FFT.Mode#SYMMETRIC}).
     * The length must be one that the FFT provider supports.
     * @param n the length of the input and output arrays
     * @return a new FFT
     * @see FFT.Mode
     * @see #getLength(int)
     */
    public static FFT newInstance(int n) {
	return newInstance(null, n, null);
    }

    /**
     * Create a new instance of the FFT class using the default
     * FFT provider with a specified mode. The length must be one
     * that the FFT provider supports.
     * @param n the length of the input and output arrays
     * @param m the mode ({@link Mode#NORMAL}, {@link Mode#SYMMETRIC},
     *        or {@link Mode#REVERSED})
     * @return the new FFT
     * @see FFT.Mode
     * @see #getLength(int)
     */
    public static FFT newInstance(int n, Mode m) {
	return newInstance(null, n, m);
    }

    /**
     * Create a new instance of the FFT class using a named
     * FFT provider with the default mode. The length must be one
     * that the FFT provider supports.
     * @param name the name of the FFT provider
     * @param n the length of the input and output arrays
     * @return the new FFT
     * @see FFT.Mode
     * @see #getLength(String,int)
     */
    public static FFT newInstance(String name, int n) {
	return newInstance(name, n, null);
    }


    /**
     * Create a new instance of the FFT class using a named
     * FFT provider with a specified mode. The length must be one
     * that the FFT provider supports.
     * @param name the name of the FFT provider
     * @param n the length of the input and output arrays
     * @param m the mode ({@link Mode#NORMAL}, {@link Mode#SYMMETRIC},
     *        or {@link Mode#REVERSED})
     * @return the new FFT
     * @see FFT.Mode
     * @see #getLength(String,int)
     */
    public static FFT newInstance(String name, int n, Mode m) {
	if (m  == null) m = defaultMode;
	FFTProvider provider =(name == null)? defaultProvider:
	    map.get(name);

	Class<? extends FFT> clazz = provider.getFFTClass();
	if (clazz == null) {
	    throw new IllegalArgumentException(errorMsg("spError"));
	} else {
	    try {
		Constructor<? extends FFT> constructor =
		    clazz.getConstructor(int.class, Mode.class);
		return constructor.newInstance(n, m);
	    } catch (NoSuchMethodException e) {
		String msg = errorMsg("spError");
		throw new RuntimeException(msg, e);
	    } catch (InstantiationException ee) {
		String msg = errorMsg("spError");
		throw new RuntimeException(msg, ee);
	    } catch (IllegalAccessException eee) {
		String msg = errorMsg("spError");
		throw new RuntimeException(msg, eee);
	    } catch (InvocationTargetException eeee) {
		String msg = errorMsg("spError");
		throw new RuntimeException(msg, eeee);
	    }
	}
    }

    /**
     * Create a new instance of the FFT class based on an existing FFT
     * instance but with a new mode.
     * @param fft an existing FFT instance
     * @param m the mode ({@link Mode#NORMAL}, {@link Mode#SYMMETRIC},
     *        or {@link Mode#REVERSED})
     * @return the new FFT
     * @see FFT.Mode
     */
    public static FFT newInstance(FFT fft, Mode m) {
	if (fft == null) {
	    throw new IllegalArgumentException(errorMsg("spError"));
	} else {
	    Class<? extends FFT> clazz = fft.getClass();
	    try {
		Constructor<? extends FFT> constructor =
		    clazz.getConstructor(clazz, Mode.class);
		return constructor.newInstance(fft, m);
	    } catch (NoSuchMethodException e) {
		String msg = errorMsg("spError");
		throw new RuntimeException(msg, e);
	    } catch (InstantiationException ee) {
		String msg = errorMsg("spError");
		throw new RuntimeException(msg, ee);
	    } catch (IllegalAccessException eee) {
		String msg = errorMsg("spError");
		throw new RuntimeException(msg, eee);
	    } catch (InvocationTargetException eeee) {
		String msg = errorMsg("spError");
		throw new RuntimeException(msg, eeee);
	    }
	}
    }


    /**
     * Constructor.
     * @param m the mode for the transform
     */
    protected FFT(Mode m) {
	if (m == null) m = defaultMode;
	mode = m;
    }

    /**
     * Get the mode for this transform
     * @return the mode
     * @see FFT.Mode
     */
    public final Mode getMode() {return mode;}

    /**
     * Get the length that this instance of FFT supports.
     * @return the length of the input and output arrays that
     *         are passed to
     *         {@link #transform(double[],double[], double[],double[])}
     *         or
     *         {@link #inverse(double[],double[], double[],double[])}
     */
    public abstract int getLength();

    /**
     * Compute an FFT.
     * The transform is defined by
     * X<sub>k</sub> = F&sum;<sub>n &isin;[0,N)</sub> x<sub>n</sub>e<sup>-i2&pi;kn/N</sup>.
     * where
     * <UL>
     *   <LI> F = 1 when the transform was created with a mode of
     *        {@link Mode#NORMAL}.
     *   <LI> F =1/&radic;<SPAN style="text-decoration: overline">N</SPAN>
     *        when the transform was created with a mode of
     *        {@link Mode#SYMMETRIC}.
     *   <LI>  F = 1/N when the transform was created with a mode of
     *        {@link Mode#REVERSED}.
     *   <LI> N is the length of the argument arrays
     * </UL>
     * <P>
     * The arrays xiReal and xoReal may be the same array, as may
     * the arrays xiImag and xoImag.  If the transform is not an in-place
     * transform, the implementation must specifically handle this case.
     * @param xiReal the real components of the input values
     * @param xiImag the imaginary components of the input values
     * @param xoReal the real components of the output values
     * @param xoImag the imaginary components of the output values
     * @exception IllegalArgumentException an array does not have
     *            the required length
     */
    public abstract void transform(double[] xiReal, double[] xiImag,
				   double[] xoReal, double[] xoImag)
	throws IllegalArgumentException;

    /**
     * Compute an inverse FFT.
     * The inverse transform is defined by
     * x<sub>n</sub> = F&sum;<sub>k &isin;[0,N)</sub> X<sub>k</sub>e<sup>i2&pi;kn/N</sup>.
     * where
     * <UL>
     *   <LI> F = 1/N when the transform was created with a mode of
     *        {@link Mode#NORMAL}.
     *   <LI> F =1/&radic;<SPAN style="text-decoration: overline">N</SPAN>
     *        when the transform was created with a mode of
     *        {@link Mode#SYMMETRIC}.
     *   <LI>  F = 1 when the transform was created with a mode of
     *        {@link Mode#REVERSED}.
     *   <LI> N is the length of the argument arrays
     * </UL>
     * <P>
     * The arrays xiReal and xoReal may be the same array, as may
     * the arrays xiImag and xoImag.  If the transform is not an in-place
     * transform, the implementation must specifically handle this case.
     * @param xiReal the real components of the input values
     * @param xiImag the imaginary components of the input values
     * @param xoReal the real components of the output values
     * @param xoImag the imaginary components of the output values
     * @exception IllegalArgumentException an array does not have
     *            the required length
     */
    public abstract void inverse(double[] xiReal, double[] xiImag,
				 double[] xoReal, double[] xoImag)
	throws IllegalArgumentException;

    /**
     * Convolution.
     * For continuous, real-valued functions, a convolution is defined as
     * (f&lowast;g)(t) = &int; f(&tau;)g(t-&tau;) d&tau;.
     * The discrete analog of this equation is
     *  C<sub>k</sub> = &sum;f<sub>i</sub>g<sub>(k-i)</sub>
     * The values for f<sub>i</sub> and g<sub>k-i</sub> are
     * assumed to be zero except when i&isin;[0,N) and (k-i)&isin;[-L,M-L)
     * where L is the value of zeroOffset.
     * For convenience g can be represented by a vector of length
     * N<sub>g</sub>, the signed index covers the range [-z,N<sub>g</sub>-z)
     * while an unsigned index covers the range [0, N<sub>g</sub>).
     * <P>
     * See http://www.aip.de/groups/soe/local/numres/bookfpdf/f13-1.pdf
     * for an explanation of how this convolution can be computed using
     * an FFT.
     * @param f a vector of values of length N
     * @param g a vector of values of length M with an offset z so that
     *         signed indices cover the range [-z, M-z).
     * @param zeroOffset the offset into the array g corresponding to
     *        a signed index of 0
     * @param result a vector of values of length N
     * @exception IllegalArgumentException an array size was incorrect
     *            or an argument was out of range
     */
    public static void convolve(double[] f, double[] g, int zeroOffset,
				double[] result)
	throws IllegalArgumentException
    {
	convolve(null, f, g, zeroOffset, result);
    }

    /**
     * Convolution specifying an FFT factory.
     * For continuous, real-valued functions, a convolution is defined as
     * (f&lowast;g)(t) = &int; f(&tau;)g(t-&tau;) d&tau;.
     * The discrete analog of this equation is
     *  C<sub>k</sub> = &sum;f<sub>i</sub>g<sub>(k-i)</sub>
     * The values for f<sub>i</sub> and g<sub>k-i</sub> are
     * assumed to be zero except when i&isin;[0,N) and (k-i)&isin;[-L,M-L)
     * where L is the value of zeroOffset.
     * For convenience g can be represented by a vector of length
     * N<sub>g</sub>, the signed index covers the range [-z,N<sub>g</sub>-z)
     * while an unsigned index covers the range [0, N<sub>g</sub>).
     * <P>
     * See http://www.aip.de/groups/soe/local/numres/bookfpdf/f13-1.pdf
     * for an explanation of how this convolution can be computed using
     * an FFT.
     * @param fftf the FFT factory used to create the FFT implementation
     * @param f a vector of values of length N
     * @param g a vector of values of length M with an offset z so that
     *         signed indices cover the range [-z, M-z).
     * @param zeroOffset the offset into the array g corresponding to
     *        a signed index of 0
     * @param result a vector of values of length N
     * @exception IllegalArgumentException an array size was incorrect
     *            or an argument was out of range
     */
    public static void convolve(Factory fftf,
				double[] f, double[] g, int zeroOffset,
				double[] result)
	throws IllegalArgumentException
    {
	if (result.length < ((long)(f.length))) {
	    throw new IllegalArgumentException(errorMsg("tooSmall", 3));
	}
	if (zeroOffset < 0 || zeroOffset > g.length-1) {
	    throw new IllegalArgumentException(errorMsg("zeroOffset"));
	}
	int padding = Math.max(zeroOffset, g.length-zeroOffset);
	long plenl = (long)(f.length) + padding;
	if (plenl > ((fftf == null)? FFT.getMaxLength():
		     fftf.getMaxLength())) {
	    throw new IllegalArgumentException(errorMsg("maxFFTLength"));
	}
	int plen = (int) plenl;
	int length = (fftf == null)?getLength(plen): fftf.getLength(plen);
	double[] tmp1r = new double[length];
	double[] tmp1i = new double[length];
	double[] tmp2r = new double[length];
	double[] tmp2i = new double[length];
	FFT fft = (fftf == null)? FFT.newInstance(length, Mode.NORMAL):
	    fftf.newInstance(length, Mode.NORMAL);
	if (fft == null) {
	    throw new IllegalArgumentException
		(errorMsg("noFFT"));
	}
	if (fft.inplace()) {
	    System.arraycopy(f, 0, tmp1r, 0, f.length);
	    fft.transform(tmp1r, tmp1i, tmp1r, tmp1i);
	    System.arraycopy(g, 0, tmp2r, tmp2r.length - zeroOffset,
			     zeroOffset);
	    System.arraycopy(g, zeroOffset, tmp2r, 0, g.length - zeroOffset);
	    fft.transform(tmp2r, tmp2i, tmp2r, tmp2i);
	    for (int i = 0; i < length; i++) {
		double tmpr = tmp1r[i]*tmp2r[i] - tmp1i[i]*tmp2i[i];
		double tmpi = tmp1r[i]*tmp2i[i] + tmp1i[i]*tmp2r[i];
		tmp1r[i] = tmpr;
		tmp1i[i] = tmpi;
	    }
	    fft.inverse(tmp1r, tmp1i, tmp1r, tmp1i);
	} else {
	    double[] tmp3r = new double[length];
	    double[] tmp3i = new double[length];
	    System.arraycopy(f, 0, tmp1r, 0, f.length);
	    fft.transform(tmp1r, tmp1i, tmp2r, tmp2i);
	    java.util.Arrays.fill(tmp1r, 0);
	    java.util.Arrays.fill(tmp1i, 0);
	    System.arraycopy(g, 0, tmp1r, tmp1r.length - zeroOffset,
			     zeroOffset);
	    System.arraycopy(g, zeroOffset, tmp1r, 0, g.length - zeroOffset);
	    fft.transform(tmp1r, tmp1i, tmp3r, tmp3i);
	    for (int i = 0; i < length; i++) {
		double tmpr = tmp3r[i]*tmp2r[i] - tmp3i[i]*tmp2i[i];
		double tmpi = tmp3r[i]*tmp2i[i] + tmp3i[i]*tmp2r[i];
		tmp2r[i] = tmpr;
		tmp2i[i] = tmpi;
	    }
	    fft.inverse(tmp2r, tmp2i, tmp1r, tmp1i);
	}
	for (int i = 0; i < f.length; i++) {
	    result[i] = tmp1r[i];
	}
    }

    /**
     * Cyclic Convolution.
     * For continuous, real-valued functions, a convolution is defined as
     * (f&lowast;g)(t) = &int; f(&tau;)g(t-&tau;) d&tau;.
     * The discrete analog of this equation is
     *  C<sub>k</sub> = &sum;f<sub>i</sub>g<sub>(k-i)</sub>
     * The values for f<sub>i</sub> and g<sub>k-i</sub> are
     * assumed to be zero except when i&isin;[0,N) and (k-i)&isin;[-L,M-L)
     * where L is the value of zeroOffset.
     * For convenience g can be represented by a vector of length
     * N<sub>g</sub>, the signed index covers the range [-z,N<sub>g</sub>-z)
     * while an unsigned index covers the range [0, N<sub>g</sub>).
     * <P>
     * See http://www.aip.de/groups/soe/local/numres/bookfpdf/f13-1.pdf
     * for an explanation of how this convolution can be computed using
     * an FFT.
     * @param f a vector of values of length N
     * @param g a vector of values of length M with an offset z so that
     *         signed indices cover the range [-z, M-z).
     * @param zeroOffset the offset into the array g corresponding to
     *        a signed index of 0
     * @param result a vector of values of length N
     * @exception IllegalArgumentException an array size was incorrect
     *            or an argument was out of range
     */
    public static void cyclicConvolve(double[] f, double[] g, int zeroOffset,
				      double[] result)
	throws IllegalArgumentException
    {
	cyclicConvolve(null, f, g, zeroOffset, result);
    }

    /**
     * Cyclic Convolution, specifying an FFT factory.
     * For continuous, real-valued functions, a convolution is defined as
     * (f&lowast;g)(t) = &int; f(&tau;)g(t-&tau;) d&tau;.
     * The discrete analog of this equation is
     *  C<sub>k</sub> = &sum;f<sub>i</sub>g<sub>(k-i)</sub>
     * The values for f<sub>i</sub> and g<sub>k-i</sub> are
     * assumed to be zero except when i&isin;[0,N) and (k-i)&isin;[-L,M-L)
     * where L is the value of zeroOffset.
     * For convenience g can be represented by a vector of length
     * N<sub>g</sub>, the signed index covers the range [-z,N<sub>g</sub>-z)
     * while an unsigned index covers the range [0, N<sub>g</sub>).
     * <P>
     * See http://www.aip.de/groups/soe/local/numres/bookfpdf/f13-1.pdf
     * for an explanation of how this convolution can be computed using
     * an FFT.
     * @param fftf the FFT factory used to create the FFT implementation
     * @param f a vector of values of length N
     * @param g a vector of values of length M with an offset z so that
     *         signed indices cover the range [-z, M-z).
     * @param zeroOffset the offset into the array g corresponding to
     *        a signed index of 0
     * @param result a vector of values of length N
     * @exception IllegalArgumentException an array size was incorrect
     *            or an argument was out of range
     */
    public static void cyclicConvolve(Factory fftf,
				      double[] f, double[] g, int zeroOffset,
				      double[] result)
	throws IllegalArgumentException
    {
	if (result.length < ((long)(f.length))) {
	    throw new IllegalArgumentException(errorMsg("tooSmall", 4));
	}
	if (zeroOffset < 0 || zeroOffset > g.length-1) {
	    throw new IllegalArgumentException(errorMsg("zeroOffset"));
	}
	int tail = g.length - zeroOffset;
	int padding = Math.max(zeroOffset, g.length-zeroOffset);
	long plenl;
	boolean exactFit = (f.length == ((fftf == null)?
					 FFT.getLength(f.length):
					 fftf.getLength(f.length)));
	if (exactFit) {
	    plenl = f.length;
	} else {
	    plenl = (long)(f.length) + 2*((long)padding);
	}
	if (plenl > ((fftf == null)? FFT.getMaxLength():
		     fftf.getMaxLength())) {
	    throw new IllegalArgumentException(errorMsg("maxFFTLength"));
	}
	int plen = (int)plenl;
	    int length = (fftf == null)? getLength(plen): fftf.getLength(plen);
	double[] tmp1r = new double[length];
	double[] tmp1i = new double[length];
	double[] tmp2r = new double[length];
	double[] tmp2i = new double[length];
	FFT fft = (fftf == null)? FFT.newInstance(length, Mode.NORMAL):
	    fftf.newInstance(length, Mode.NORMAL);
	if (fft == null) {
	    throw new IllegalArgumentException
		(errorMsg("noFFT"));
	}
	if (fft.inplace()) {
	    // System.arraycopy(f, 0, tmp1r, 0, f.length);
	    if (exactFit) {
		System.arraycopy(f, 0, tmp1r, 0, length);
	    } else {
		System.arraycopy(f, 0, tmp1r, 0, f.length);
		System.arraycopy(f, f.length-tail, tmp1r, length-tail, tail);
		System.arraycopy(f, 0, tmp1r, f.length, zeroOffset);
	    }
	    fft.transform(tmp1r, tmp1i, tmp1r, tmp1i);
	    System.arraycopy(g, 0, tmp2r, tmp2r.length - zeroOffset,
			     zeroOffset);
	    System.arraycopy(g, zeroOffset, tmp2r, 0,
			     g.length - zeroOffset);
	    fft.transform(tmp2r, tmp2i, tmp2r, tmp2i);
	    for (int i = 0; i < length; i++) {
		double tmpr = tmp1r[i]*tmp2r[i] - tmp1i[i]*tmp2i[i];
		double tmpi = tmp1r[i]*tmp2i[i] + tmp1i[i]*tmp2r[i];
		tmp1r[i] = tmpr;
		tmp1i[i] = tmpi;
	    }
	    fft.inverse(tmp1r, tmp1i, tmp1r, tmp1i);
	} else {
	    double[] tmp3r = new double[length];
	    double[] tmp3i = new double[length];
	    // System.arraycopy(f, 0, tmp1r, 0, f.length);
	    if (exactFit) {
		System.arraycopy(f, 0, tmp1r, 0, length);
	    } else {
		System.arraycopy(f, 0, tmp1r, 0, f.length);
		System.arraycopy(f, f.length-tail, tmp1r, length-tail, tail);
		System.arraycopy(f, 0, tmp1r, f.length, zeroOffset);
	    }
	    fft.transform(tmp1r, tmp1i, tmp2r, tmp2i);
	    java.util.Arrays.fill(tmp1r, 0);
	    java.util.Arrays.fill(tmp1i, 0);
	    System.arraycopy(g, 0, tmp1r, tmp1r.length - zeroOffset,
			     zeroOffset);
	    System.arraycopy(g, zeroOffset, tmp1r, 0,
			     g.length - zeroOffset);
	    fft.transform(tmp1r, tmp1i, tmp3r, tmp3i);
	    for (int i = 0; i < length; i++) {
		double tmpr = tmp3r[i]*tmp2r[i] - tmp3i[i]*tmp2i[i];
		double tmpi = tmp3r[i]*tmp2i[i] + tmp3i[i]*tmp2r[i];
		tmp2r[i] = tmpr;
		tmp2i[i] = tmpi;
	    }
	    fft.inverse(tmp2r, tmp2i, tmp1r, tmp1i);
	}
	for (int i = 0; i < f.length; i++) {
	    result[i] = tmp1r[i];
	}
    }


    /**
     * Cross correlation.
     * For continuous, real-valued functions, a cross correlation
     * is defined as (f&#9733;g)(t) = &int; f(&tau;)g(t+&tau;) d&tau;.
     * The discrete analog of this equation is
     *  C<sub>k</sub> = &sum;f<sub>i</sub>g<sub>(k+i)</sub>
     * The values for f<sub>i</sub> and g<sub>k+i</sub> are
     * assumed to be zero except within the range of indices
     * [0,N) and [0,M) respectively,
     * where N is the length of the vector f and M is
     * the length of the vector g. For the result vector, index 0
     * corresponds to k = 1-N, and its index N+M-2 corresponds to
     * k = M-1. The index M-1 corresponds to k = 0.

     * <P>
     * See http://www.aip.de/groups/soe/local/numres/bookfpdf/f13-1.pdf
     * for an explanation of how s convolution can be computed using
     * an FFT. This applies as well to a cross correlation, however,
     * the position of the array g is shifted compared to the position
     * used in a convolution.
     * @param f a vector of values of length N
     * @param g a vector of values of length M.
     * @param result a vector of values of length N + M - 1 that will
     *        contain the cross correlation of f and g
     * @exception IllegalArgumentException an array size was incorrect
     */
    public static void crossCorrelate(double[] f, double[] g, double[] result)
	throws IllegalArgumentException
    {
	crossCorrelate(null, f, g, result);
    }

    /**
     * Cross correlation specifying an FFT factory.
     * For continuous, real-valued functions, a cross correlation
     * is defined as (f&#9733;g)(t) = &int; f(&tau;)g(t+&tau;) d&tau;.
     * The discrete analog of this equation is
     *  C<sub>k</sub> = &sum;f<sub>i</sub>g<sub>(k+i)</sub>
     * The values for f<sub>i</sub> and g<sub>k+i</sub> are
     * assumed to be zero except within the range of indices
     * [0,N) and [0,M) respectively,
     * where N is the length of the vector f and M is
     * the length of the vector g. For the result vector, index 0
     * corresponds to k = 1-N, and its index N+M-2 corresponds to
     * k = M-1. The index M-1 corresponds to k = 0.
     * <P>
     * See http://www.aip.de/groups/soe/local/numres/bookfpdf/f13-1.pdf
     * for an explanation of how s convolution can be computed using
     * an FFT. This applies as well to a cross correlation, however,
     * the position of the array g is shifted compared to the position
     * used in a convolution.
     * @param fftf the FFT factory used to create the FFT implementation
     * @param f a vector of values of length N
     * @param g a vector of values of length M.
     * @param result a vector of values of length N + M - 1 that will
     *        contain the cross correlation of f and g
     * @exception IllegalArgumentException an array size was incorrect
     */
    public static void crossCorrelate(Factory fftf,
				      double[] f, double[] g, double[] result)
	throws IllegalArgumentException
    {
	if (result.length < ((long)(f.length)) + ((long)g.length) - 1) {
	    throw new IllegalArgumentException(errorMsg("tooSmall", 3));
	}

	long plenl = (long)(f.length) + 2*(long)(g.length);
	if (plenl > ((fftf == null)? FFT.getMaxLength(): fftf.getMaxLength()))
	    throw new IllegalArgumentException(errorMsg("maxFFTLength"));
	int plen = (int) plenl;
	int length = (fftf == null)? getLength(plen): fftf.getLength(plen);
	double[] tmp1r = new double[length];
	double[] tmp1i = new double[length];
	double[] tmp2r = new double[length];
	double[] tmp2i = new double[length];
	FFT fft = (fftf == null)? FFT.newInstance(length, Mode.NORMAL):
	    fftf.newInstance(length, Mode.NORMAL);
	if (fft == null) {
	    throw new IllegalArgumentException
		(errorMsg("noFFT"));
	}
	if (fft.inplace()) {
	    System.arraycopy(f, 0, tmp1r, 0, f.length);
	    fft.transform(tmp1r, tmp1i, tmp1r, tmp1i);
	    System.arraycopy(g, 0, tmp2r, f.length-1, g.length);
	    fft.transform(tmp2r, tmp2i, tmp2r, tmp2i);
	    for (int i = 0; i < length; i++) {
		// multiplication of the complex conjugate of tmp1 with
		// tmp2.
		double tmpr = tmp1r[i]*tmp2r[i] + tmp1i[i]*tmp2i[i];
		double tmpi = tmp1r[i]*tmp2i[i] - tmp1i[i]*tmp2r[i];
		tmp1r[i] = tmpr;
		tmp1i[i] = tmpi;
	    }
	    fft.inverse(tmp1r, tmp1i, tmp1r, tmp1i);
	} else {
	    double[] tmp3r = new double[length];
	    double[] tmp3i = new double[length];
	    System.arraycopy(f, 0, tmp1r, 0, f.length);
	    fft.transform(tmp1r, tmp1i, tmp2r, tmp2i);
	    java.util.Arrays.fill(tmp1r, 0);
	    java.util.Arrays.fill(tmp1i, 0);
	    System.arraycopy(g, 0, tmp1r, f.length-1, g.length);
	    fft.transform(tmp1r, tmp1i, tmp3r, tmp3i);
	    for (int i = 0; i < length; i++) {
		// multiplication of the complex conjugate of tmp2 with
		// tmp3.
		double tmpr = tmp2r[i]*tmp3r[i] + tmp2i[i]*tmp3i[i];
		double tmpi = tmp2r[i]*tmp3i[i] - tmp2i[i]*tmp3r[i];
		tmp2r[i] = tmpr;
		tmp2i[i] = tmpi;
	    }
	    fft.inverse(tmp2r, tmp2i, tmp1r, tmp1i);
	}
	for (int i = 0; i < result.length; i++) {
	    result[i] = tmp1r[i];
	}
    }

    /**
     * Cyclic cross correlation.
     * For continuous, real-valued functions, a cross correlation
     * is defined as (f&#9733;g)(t) = &int; f(&tau;)g(t+&tau;) d&tau;.
     * The discrete analog of this equation is
     *  C<sub>k</sub> = &sum;f<sub>i</sub>g<sub>(k+i)</sub>
     * The values for f<sub>i</sub> and g<sub>k+i</sub> are
     * assumed to be zero except within the range of indices
     * [0,N) and [0,M) respectively,
     * where N is the length of the vector f and M is
     * the length of the vector g. For the result vector, index 0
     * corresponds to k = 1-N, and its index N+M-2 corresponds to
     * k = M-1. The index M-1 corresponds to k = 0.
     * <P>
     * See http://www.aip.de/groups/soe/local/numres/bookfpdf/f13-1.pdf
     * for an explanation of how s convolution can be computed using
     * an FFT. This applies as well to a cross correlation, however,
     * the position of the array g is shifted compared to the position
     * used in a convolution.
     * @param f a vector of values of length N
     * @param g a vector of values of length M.
     * @param result a vector of values of length N that will
     *        contain the cross correlation of f and g
     * @exception IllegalArgumentException an array size was incorrect
     */
    public static void cyclicCrossCorrelate(double[] f, double[] g,
					    double[] result)
	throws IllegalArgumentException
    {
	cyclicCrossCorrelate(null, f, g, result);
    }

    /**
     * Cyclic cross correlation, specifying an FFT factory.
     * For continuous, real-valued functions, a cross correlation
     * is defined as (f&#9733;g)(t) = &int; f(&tau;)g(t+&tau;) d&tau;.
     * The discrete analog of this equation is
     *  C<sub>k</sub> = &sum;f<sub>i</sub>g<sub>(k+i)</sub>
     * The values for f<sub>i</sub> and g<sub>k+i</sub> are
     * assumed to be zero except within the range of indices
     * [0,N) and [0,M) respectively,
     * where N is the length of the vector f and M is
     * the length of the vector g. For the result vector, index 0
     * corresponds to k = 1-N, and its index N+M-2 corresponds to
     * k = M-1. The index M-1 corresponds to k = 0.
     * <P>
     * See http://www.aip.de/groups/soe/local/numres/bookfpdf/f13-1.pdf
     * for an explanation of how s convolution can be computed using
     * an FFT. This applies as well to a cross correlation, however,
     * the position of the array g is shifted compared to the position
     * used in a convolution.
     * @param fftf the FFT factory used to create the FFT implementation
     * @param f a vector of values of length N
     * @param g a vector of values of length M.
     * @param result a vector of values of length N that will
     *        contain the cross correlation of f and g
     * @exception IllegalArgumentException an array size was incorrect
     */
    public static void cyclicCrossCorrelate(Factory fftf,
					    double[] f, double[] g,
					    double[] result)
	throws IllegalArgumentException
    {
	if (f.length < g.length) {
	    throw new IllegalArgumentException
		(errorMsg("lengthError2", 1, 2));
	}
	if (result.length < f.length) {
	    throw new IllegalArgumentException
		(errorMsg("lengthError2", 3, 1));
	}
	long plenl;
	boolean exactFit = (f.length == ((fftf == null)?
					 FFT.getLength(f.length):
					 fftf.getLength(f.length)));
	if (exactFit) {
	    plenl = f.length;
	} else {
	    plenl = (long)(f.length) + 2*(long)(g.length);
	}
	if (plenl > ((fftf == null)? FFT.getMaxLength(): fftf.getMaxLength()))
	    throw new IllegalArgumentException(errorMsg("maxFFTLength"));
	int plen = (int)plenl;
	int length = (fftf == null)? getLength(plen): fftf.getLength(plen);
	double[] tmp1r = new double[length];
	double[] tmp1i = new double[length];
	double[] tmp2r = new double[length];
	double[] tmp2i = new double[length];
	FFT fft = (fftf == null)? FFT.newInstance(length, Mode.NORMAL):
	    fftf.newInstance(length, Mode.NORMAL);
	if (fft == null) {
	    throw new IllegalArgumentException
		(errorMsg("noFFT"));
	}
	if (fft.inplace()) {
	    // System.arraycopy(f, 0, tmp1r, 0, f.length);
	    if (exactFit) {
		// System.arraycopy(f, 0, tmp1r, 0, f.length);
		System.arraycopy(f, f.length-g.length, tmp1r, 0, g.length);
		System.arraycopy(f, 0, tmp1r, g.length, f.length-g.length);
		int excess = 2*g.length - length;
		if (excess <= 0) {
		    System.arraycopy(g, 0, tmp2r, g.length, g.length);
		} else {
		    System.arraycopy(g, 0, tmp2r, g.length, g.length-excess);
		    System.arraycopy(g, g.length-excess, tmp2r, 0, excess);
		}
	    } else {
		System.arraycopy(f, 0, tmp1r, g.length, f.length);
		System.arraycopy(f, f.length-g.length, tmp1r, 0, g.length);
		System.arraycopy(f, 0, tmp1r, f.length+g.length, g.length);
		System.arraycopy(g, 0, tmp2r, g.length, g.length);
	    }
	    fft.transform(tmp1r, tmp1i, tmp1r, tmp1i);
	    fft.transform(tmp2r, tmp2i, tmp2r, tmp2i);
	    for (int i = 0; i < length; i++) {
		// multiplication of the complex conjugate of tmp1 with
		// tmp2.
		double tmpr = tmp1r[i]*tmp2r[i] + tmp1i[i]*tmp2i[i];
		double tmpi = tmp1r[i]*tmp2i[i] - tmp1i[i]*tmp2r[i];
		tmp1r[i] = tmpr;
		tmp1i[i] = tmpi;
	    }
	    fft.inverse(tmp1r, tmp1i, tmp1r, tmp1i);
	} else {
	    double[] tmp3r = new double[length];
	    double[] tmp3i = new double[length];
	    if (exactFit) {
		System.arraycopy(f, 0, tmp1r, 0, f.length);
		int excess = 2*g.length - length;
		if (excess <= 0) {
		    System.arraycopy(g, 0, tmp2r, g.length, g.length);
		} else {
		    System.arraycopy(g, 0, tmp2r, g.length, g.length-excess);
		    System.arraycopy(g, g.length-excess, tmp2r, 0, excess);
		}
	    } else {
		System.arraycopy(f, 0, tmp1r, g.length, f.length);
		System.arraycopy(f, f.length-g.length, tmp1r, 0, g.length);
		System.arraycopy(f, 0, tmp1r, f.length+g.length, g.length);
		System.arraycopy(g, 0, tmp1r, g.length, g.length);
	    }
	    fft.transform(tmp1r, tmp1i, tmp2r, tmp2i);
	    java.util.Arrays.fill(tmp1r, 0);
	    java.util.Arrays.fill(tmp1i, 0);
	    fft.transform(tmp1r, tmp1i, tmp3r, tmp3i);
	    for (int i = 0; i < length; i++) {
		// multiplication of the complex conjugate of tmp2 with
		// tmp3.
		double tmpr = tmp2r[i]*tmp3r[i] + tmp2i[i]*tmp3i[i];
		double tmpi = tmp2r[i]*tmp3i[i] - tmp2i[i]*tmp3r[i];
		tmp2r[i] = tmpr;
		tmp2i[i] = tmpi;
	    }
	    fft.inverse(tmp2r, tmp2i, tmp1r, tmp1i);
	}
	int offset = length - f.length;
	for (int i = 0; i < f.length; i++) {
	    result[i] = tmp1r[offset+i];
	}
    }
}

//  LocalWords:  exbundle SPI FFT bzdevFFT blockquote pre boolean len
//  LocalWords:  getServiceName LMode serviceProviders newInstance
//  LocalWords:  getLength lmode inplace radic overline spError FFT's
//  LocalWords:  xiReal xiImag xoReal xoImag IllegalArgumentException
//  LocalWords:  nullArg setName setParameters serviceName isin tmp
//  LocalWords:  lowast zeroOffset maxFFTLength arraycopy plen fft
//  LocalWords:  initialPadding lengthError nullFFTMode noService
//  LocalWords:  firstArgNotPositive closedOutOfRangeI fftf tooSmall
//  LocalWords:  noFFT
