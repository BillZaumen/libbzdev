package org.bzdev.util;

//@exbundle org.bzdev.util.lpack.Util

/**
 * Class for interleaving arrays.
 * This class contains only static methods.
 * Given arrays a1, a2, a3, the method ArrayMerger.merge(a1, a2, a3)
 * will produce an array whose elements are {a1[0], a2[0], a3[0],
 * a1[1], a2[1], a3[1], a1[2], a2[2], a3[2], ...., a1[n], a2[n], a3[n]}
 * when a1, a2, and a3 have lengths of n+1.  Another method allows
 * shorter arrays to be generated by providing a range of indices from
 * which the elements of the arrays should be extracted. for example,
 * ArrayMerger.merge(1, 3, a1, a2, a3) will produce an array whose
 * elements are {a1[1], a2[1], a3[1], a1[2], a2[2], a3[2]}.
 * <P>
 * One use of this class is to combine the arrays of (one dimensional)
 * control points that various splines can generate and create an
 * array formatted as required by methods used by {@link java.awt.geom.Path2D}
 * or {@link org.bzdev.geom.Path3D} and their subclasses.
 */
public class ArrayMerger {

    private static String errorMsg(String key, Object... args) {
	return UtilErrorMsg.errorMsg(key, args);
    }

    /**
     * Merge multiple arrays of double.
     * The elements are placed into the output array starting
     * with the first element of the first argument, then the
     * first element of the second argument, etc., until an
     * element has been used from each argument array. This
     * is then repeated for the second element of each argument
     * array, etc.
     * @return an array containing the elements of the argument
     *         arrays
     */

    public static double[] merge(double[]... args)
	throws IllegalArgumentException
    {
	if (args.length == 0) return new double[0];
	return merge(0, args[0].length, args);
    }

    /**
     * Merge a range of elements from multiple arrays of double.
     * The elements are placed into the output array starting
     * with the element whose index is 'start' for the first argument, then the
     * element whose index is 'start' for the second argument, etc., until an
     * element has been used from each argument array. This
     * is then repeated for the next element of each argument
     * array, etc. as long as the elements' indices are less than 'end'.
     * @param start the starting offset into the arrays (inclusive)
     * @param end the ending offset into the arrays (exclusive)
     * @param args a variable number of arrays
     * @return an array in which the elements of args are interleaved
     */
    public static double[] merge(int start, int end, double[]... args)
	throws IllegalArgumentException
    {
	double[] results = new double[args.length * (end - start)];
	for (int j = 0; j < args.length; j++) {
	    if (args[j].length < end) {
		throw new IllegalArgumentException(errorMsg("arglength", end));
	    }
	}

	int index = 0;
	for (int i = start; i < end; i++) {
	    for (int j = 0; j < args.length; j++) {
		results[index++] = args[j][i];
	    }
	}
	return results;
    }

    /**
     * Merge multiple arrays of int.
     * The elements are placed into the output array starting
     * with the first element of the first argument, then the
     * first element of the second argument, etc., until an
     * element has been used from each argument array. This
     * is then repeated for the second element of each argument
     * array, etc.
     * @return an array containing the elements of the argument
     *         arrays
     */

    public static int[] merge(int[]... args)
	throws IllegalArgumentException
    {
	if (args.length == 0) return new int[0];
	return merge(0, args[0].length, args);
    }

    /**
     * Merge a range of elements from multiple arrays of int.
     * The elements are placed into the output array starting
     * with the element whose index is 'start' for the first argument, then the
     * element whose index is 'start' for the second argument, etc., until an
     * element has been used from each argument array. This
     * is then repeated for the next element of each argument
     * array, etc. as long as the elements' indices are less than 'end'.
     * @param start the starting offset into the arrays (inclusive)
     * @param end the ending offset into the arrays (exclusive)
     * @param args a variable number of arrays
     * @return an array in which the elements of args are interleaved
     */
    public static int[] merge(int start, int end, int[]... args)
	throws IllegalArgumentException
    {
	int[] results = new int[args.length * (end - start)];
	for (int j = 0; j < args.length; j++) {
	    if (args[j].length < end) {
		throw new IllegalArgumentException(errorMsg("arglength", end));
	    }
	}

	int index = 0;
	for (int i = start; i < end; i++) {
	    for (int j = 0; j < args.length; j++) {
		results[index++] = args[j][i];
	    }
	}
	return results;
    }

    /**
     * Merge multiple arrays of short.
     * The elements are placed into the output array starting
     * with the first element of the first argument, then the
     * first element of the second argument, etc., until an
     * element has been used from each argument array. This
     * is then repeated for the second element of each argument
     * array, etc.
     * @return an array containing the elements of the argument
     *         arrays
     */

    public static short[] merge(short[]... args)
	throws IllegalArgumentException
    {
	if (args.length == 0) return new short[0];
	return merge(0, args[0].length, args);
    }

    /**
     * Merge a range of elements from multiple arrays of short.
     * The elements are placed into the output array starting
     * with the element whose index is 'start' for the first argument, then the
     * element whose index is 'start' for the second argument, etc., until an
     * element has been used from each argument array. This
     * is then repeated for the next element of each argument
     * array, etc. as long as the elements' indices are less than 'end'.
     * @param start the starting offset into the arrays (inclusive)
     * @param end the ending offset into the arrays (exclusive)
     * @param args a variable number of arrays
     * @return an array in which the elements of args are interleaved
     */
    public static short[] merge(int start, int end, short[]... args)
	throws IllegalArgumentException
    {
	short[] results = new short[args.length * (end - start)];
	for (int j = 0; j < args.length; j++) {
	    if (args[j].length < end) {
		throw new IllegalArgumentException(errorMsg("arglength", end));
	    }
	}

	int index = 0;
	for (int i = start; i < end; i++) {
	    for (int j = 0; j < args.length; j++) {
		results[index++] = args[j][i];
	    }
	}
	return results;
    }

    /**
     * Merge multiple arrays of long.
     * The elements are placed into the output array starting
     * with the first element of the first argument, then the
     * first element of the second argument, etc., until an
     * element has been used from each argument array. This
     * is then repeated for the second element of each argument
     * array, etc.
     * @return an array containing the elements of the argument
     *         arrays
     */

    public static long[] merge(long[]... args)
	throws IllegalArgumentException
    {
	if (args.length == 0) return new long[0];
	return merge(0, args[0].length, args);
    }

    /**
     * Merge a range of elements from multiple arrays of long.
     * The elements are placed into the output array starting
     * with the element whose index is 'start' for the first argument, then the
     * element whose index is 'start' for the second argument, etc., until an
     * element has been used from each argument array. This
     * is then repeated for the next element of each argument
     * array, etc. as long as the elements' indices are less than 'end'.
     * @param start the starting offset into the arrays (inclusive)
     * @param end the ending offset into the arrays (exclusive)
     * @param args a variable number of arrays
     * @return an array in which the elements of args are interleaved
     */
    public static long[] merge(int start, int end, long[]... args)
	throws IllegalArgumentException
    {
	long[] results = new long[args.length * (end - start)];
	for (int j = 0; j < args.length; j++) {
	    if (args[j].length < end) {
		throw new IllegalArgumentException(errorMsg("arglength", end));
	    }
	}

	int index = 0;
	for (int i = start; i < end; i++) {
	    for (int j = 0; j < args.length; j++) {
		results[index++] = args[j][i];
	    }
	}
	return results;
    }

    /**
     * Merge multiple arrays of float.
     * The elements are placed into the output array starting
     * with the first element of the first argument, then the
     * first element of the second argument, etc., until an
     * element has been used from each argument array. This
     * is then repeated for the second element of each argument
     * array, etc.
     * @return an array containing the elements of the argument
     *         arrays
     */

    public static float[] merge(float[]... args)
	throws IllegalArgumentException
    {
	if (args.length == 0) return new float[0];
	return merge(0, args[0].length, args);
    }

    /**
     * Merge a range of elements from multiple arrays of float.
     * The elements are placed into the output array starting
     * with the element whose index is 'start' for the first argument, then the
     * element whose index is 'start' for the second argument, etc., until an
     * element has been used from each argument array. This
     * is then repeated for the next element of each argument
     * array, etc. as long as the elements' indices are less than 'end'.
     * @param start the starting offset into the arrays (inclusive)
     * @param end the ending offset into the arrays (exclusive)
     * @param args a variable number of arrays
     * @return an array in which the elements of args are interleaved
     */
    public static float[] merge(int start, int end, float[]... args)
	throws IllegalArgumentException
    {
	float[] results = new float[args.length * (end - start)];
	for (int j = 0; j < args.length; j++) {
	    if (args[j].length < end) {
		throw new IllegalArgumentException(errorMsg("arglength", end));
	    }
	}

	int index = 0;
	for (int i = start; i < end; i++) {
	    for (int j = 0; j < args.length; j++) {
		results[index++] = args[j][i];
	    }
	}
	return results;
    }

    /**
     * Merge multiple arrays of char.
     * The elements are placed into the output array starting
     * with the first element of the first argument, then the
     * first element of the second argument, etc., until an
     * element has been used from each argument array. This
     * is then repeated for the second element of each argument
     * array, etc.
     * @return an array containing the elements of the argument
     *         arrays
     */

    public static char[] merge(char[]... args)
	throws IllegalArgumentException
    {
	if (args.length == 0) return new char[0];
	return merge(0, args[0].length, args);
    }

    /**
     * Merge a range of elements from multiple arrays of char.
     * The elements are placed into the output array starting
     * with the element whose index is 'start' for the first argument, then the
     * element whose index is 'start' for the second argument, etc., until an
     * element has been used from each argument array. This
     * is then repeated for the next element of each argument
     * array, etc. as long as the elements' indices are less than 'end'.
     * @param start the starting offset into the arrays (inclusive)
     * @param end the ending offset into the arrays (exclusive)
     * @param args a variable number of arrays
     * @return an array in which the elements of args are interleaved
     */
    public static char[] merge(int start, int end, char[]... args)
	throws IllegalArgumentException
    {
	char[] results = new char[args.length * (end - start)];
	for (int j = 0; j < args.length; j++) {
	    if (args[j].length < end) {
		throw new IllegalArgumentException(errorMsg("arglength", end));
	    }
	}

	int index = 0;
	for (int i = start; i < end; i++) {
	    for (int j = 0; j < args.length; j++) {
		results[index++] = args[j][i];
	    }
	}
	return results;
    }

    /**
     * Merge multiple arrays of byte.
     * The elements are placed into the output array starting
     * with the first element of the first argument, then the
     * first element of the second argument, etc., until an
     * element has been used from each argument array. This
     * is then repeated for the second element of each argument
     * array, etc.
     * @return an array containing the elements of the argument
     *         arrays
     */

    public static byte[] merge(byte[]... args)
	throws IllegalArgumentException
    {
	if (args.length == 0) return new byte[0];
	return merge(0, args[0].length, args);
    }

    /**
     * Merge a range of elements from multiple arrays of byte.
     * The elements are placed into the output array starting
     * with the element whose index is 'start' for the first argument, then the
     * element whose index is 'start' for the second argument, etc., until an
     * element has been used from each argument array. This
     * is then repeated for the next element of each argument
     * array, etc. as long as the elements' indices are less than 'end'.
     * @param start the starting offset into the arrays (inclusive)
     * @param end the ending offset into the arrays (exclusive)
     * @param args a variable number of arrays
     * @return an array in which the elements of args are interleaved
     */
    public static byte[] merge(int start, int end, byte[]... args)
	throws IllegalArgumentException
    {
	byte[] results = new byte[args.length * (end - start)];
	for (int j = 0; j < args.length; j++) {
	    if (args[j].length < end) {
		throw new IllegalArgumentException(errorMsg("arglength", end));
	    }
	}

	int index = 0;
	for (int i = start; i < end; i++) {
	    for (int j = 0; j < args.length; j++) {
		results[index++] = args[j][i];
	    }
	}
	return results;
    }

    /**
     * Concatenate int arrays to produce a new array
     * @param args the arrays
     * @return the concatenation of the argument arrays
     * @exception IllegalArgumentException the array size would be too large
     *            (this tests that the array size fits in a 32-bit integer;
     *             A JVM may impose lower limits).
     */
    public static int[] concat(int[]... args)
	throws IllegalArgumentException
    {
	long len = 0;
	for (int i = 0; i < args.length; i++) {
	    len += args[i].length;
	}
	int size = (int)len;
	if ((long) size == len ) {
	    int[] result = new int[size];
	    int index = 0;
	    for (int i = 0; i < args.length; i++) {
		int[] array = args[i];
		int alen = array.length;
		System.arraycopy(array, 0, result, index, alen);
		index += alen;
	    }
	    return result;
	} else {
	    throw new IllegalArgumentException(errorMsg("arraylen", len));
	}
    }

    /**
     * Concatenate byte arrays to produce a new array
     * @param args the arrays
     * @return the concatenation of the argument arrays
     * @exception IllegalArgumentException the array size would be too large
     *            (this tests that the array size fits in a 32-bit integer;
     *             A JVM may impose lower limits).
     */
    public static byte[] concat(byte[]... args)
	throws IllegalArgumentException
    {
	long len = 0;
	for (int i = 0; i < args.length; i++) {
	    len += args[i].length;
	}
	int size = (int)len;
	if ((long) size == len ) {
	    byte[] result = new byte[size];
	    int index = 0;
	    for (int i = 0; i < args.length; i++) {
		byte[] array = args[i];
		int alen = array.length;
		System.arraycopy(array, 0, result, index, alen);
		index += alen;
	    }
	    return result;
	} else {
	    throw new IllegalArgumentException(errorMsg("arraylen", len));
	}
    }

    /**
     * Concatenate short arrays to produce a new array
     * @param args the arrays
     * @return the concatenation of the argument arrays
     * @exception IllegalArgumentException the array size would be too large
     *            (this tests that the array size fits in a 32-bit integer;
     *             A JVM may impose lower limits).
     */
    public static short[] concat(short[]... args)
	throws IllegalArgumentException
    {
	long len = 0;
	for (int i = 0; i < args.length; i++) {
	    len += args[i].length;
	}
	int size = (int)len;
	if ((long) size == len ) {
	    short[] result = new short[size];
	    int index = 0;
	    for (int i = 0; i < args.length; i++) {
		short[] array = args[i];
		int alen = array.length;
		System.arraycopy(array, 0, result, index, alen);
		index += alen;
	    }
	    return result;
	} else {
	    throw new IllegalArgumentException(errorMsg("arraylen", len));
	}
    }

    /**
     * Concatenate long arrays to produce a new array
     * @param args the arrays
     * @return the concatenation of the argument arrays
     * @exception IllegalArgumentException the array size would be too large
     *            (this tests that the array size fits in a 32-bit integer;
     *             A JVM may impose lower limits).
     */
    public static long[] concat(long[]... args)
	throws IllegalArgumentException
    {
	long len = 0;
	for (int i = 0; i < args.length; i++) {
	    len += args[i].length;
	}
	int size = (int)len;
	if ((long) size == len ) {
	    long[] result = new long[size];
	    int index = 0;
	    for (int i = 0; i < args.length; i++) {
		long[] array = args[i];
		int alen = array.length;
		System.arraycopy(array, 0, result, index, alen);
		index += alen;
	    }
	    return result;
	} else {
	    throw new IllegalArgumentException(errorMsg("arraylen", len));
	}
    }

    /**
     * Concatenate float arrays to produce a new array
     * @param args the arrays
     * @return the concatenation of the argument arrays
     * @exception IllegalArgumentException the array size would be too large
     *            (this tests that the array size fits in a 32-bit integer;
     *             A JVM may impose lower limits).
     */
    public static float[] concat(float[]... args)
	throws IllegalArgumentException
    {
	long len = 0;
	for (int i = 0; i < args.length; i++) {
	    len += args[i].length;
	}
	int size = (int)len;
	if ((long) size == len ) {
	    float[] result = new float[size];
	    int index = 0;
	    for (int i = 0; i < args.length; i++) {
		float[] array = args[i];
		int alen = array.length;
		System.arraycopy(array, 0, result, index, alen);
		index += alen;
	    }
	    return result;
	} else {
	    throw new IllegalArgumentException(errorMsg("arraylen", len));
	}
    }

    /**
     * Concatenate char arrays to produce a new array
     * @param args the arrays
     * @return the concatenation of the argument arrays
     * @exception IllegalArgumentException the array size would be too large
     *            (this tests that the array size fits in a 32-bit integer;
     *             A JVM may impose lower limits).
     */
    public static char[] concat(char[]... args)
	throws IllegalArgumentException
    {
	long len = 0;
	for (int i = 0; i < args.length; i++) {
	    len += args[i].length;
	}
	int size = (int)len;
	if ((long) size == len ) {
	    char[] result = new char[size];
	    int index = 0;
	    for (int i = 0; i < args.length; i++) {
		char[] array = args[i];
		int alen = array.length;
		System.arraycopy(array, 0, result, index, alen);
		index += alen;
	    }
	    return result;
	} else {
	    throw new IllegalArgumentException(errorMsg("arraylen", len));
	}
    }

    /**
     * Concatenate double arrays to produce a new array
     * @param args the arrays
     * @return the concatenation of the argument arrays
     * @exception IllegalArgumentException the array size would be too large
     *            (this tests that the array size fits in a 32-bit integer;
     *             A JVM may impose lower limits).
     */
    public static double[] concat(double[]... args)
	throws IllegalArgumentException
    {
	long len = 0;
	for (int i = 0; i < args.length; i++) {
	    len += args[i].length;
	}
	int size = (int)len;
	if ((long) size == len ) {
	    double[] result = new double[size];
	    int index = 0;
	    for (int i = 0; i < args.length; i++) {
		double[] array = args[i];
		int alen = array.length;
		System.arraycopy(array, 0, result, index, alen);
		index += alen;
	    }
	    return result;
	} else {
	    throw new IllegalArgumentException(errorMsg("arraylen", len));
	}
    }

}

//  LocalWords:  exbundle ArrayMerger subclasses args arglength JVM
//  LocalWords:  IllegalArgumentException arraylen
