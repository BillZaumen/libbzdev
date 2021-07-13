package org.bzdev.util;
import java.util.Arrays;

/**
 * Operations on primitive arrays.
 * This class provides a few methods that are missing from the
 * class {@link java.util.Arrays}: methods for binary searches
 * and sorts of arrays of the primitive type int with a comparator
 * provided by the caller (the comparator is an instance of
 * {@link IntComparator} in this case).
 * <P>
 * The sort functions use the TimSort algorithm, essentially the
 * same one used in openJDK.  In this case, due to GPL issues,
 * the Android version was used as a starting point as that version
 * uses the Apache License (version 2.0). The licensing information is
 * in the source code for the additional TimSort files. The TimSort
 * classes themselves are not public classes. The reason for the
 * code replication is that TimSort itself uses Java generics, which
 * work with objects but not primitive types.  To sort primitive types
 * with the TimSort algorithm it was necessary to replicate the code,
 * one class for each primitive type.  The changes to the Android
 * implementation were minor - essentially removing the use of generics
 * and substituting a different comparator.
 */
public class PrimArrays {
    /**
     * Binary search of a sorted int array.
     * <P>
     * Note: The key is always the second argument passed to the
     * comparator's compare method.
     * @param a the array to sort
     * @param key the key to search for
     * @param c the comparator used to sort the array
     * @return an index into the array whose value matches the key;
     *         otherwise (-(insertion_point) - 1)
     */
    public static int binarySearch(int[] a, int key, IntComparator c) {
	return binarySearch(a, 0, a.length, key, c);
    }

    /**
     * Binary search of a range in a sorted int array.
     * The range of indices included in the search is
     * [fromIndex, toIndex).
     * <P>
     * Note: The key is always the second argument passed to the
     * comparator's compare method.
     * @param a the array to sort
     * @param fromIndex the lowest index for the search (inclusive)
     * @param toIndex the highest index for the search (exclusive)
     * @param key the key to search for
     * @param c the comparator used to sort the array
     * @return an index into the array whose value matches the key;
     *         otherwise (-(insertion_point) - 1)
     */
    public static int binarySearch(int[] a, int fromIndex, int toIndex,
				   int key, IntComparator c)
    {
	if (c == null) {
	    return Arrays.binarySearch(a, fromIndex, toIndex, key);
	}
	IntTimSort.checkStartAndEnd(a.length, fromIndex, toIndex);
	int low = fromIndex;
	int high = toIndex -1;
	if (c.compare(a[high], key) < 0) return (-toIndex - 1);
	while (low <= high) {
	    int middle = (low + high) >>> 1;
	    int value = a[middle];
	    int test = c.compare(value, key);
	    if (test < 0) {
		low = middle + 1;
	    } else if (test > 0) {
		high = middle - 1;
	    } else {
		return middle;
	    }
	}
	return -low - 1;
    }

    /**
     * Binary search of a sorted int array, choosing the minimum
     * or maximum index whose array value matches a key.
     * <P>
     * Note: The key is always the second argument passed to the
     * comparator's compare method.
     * @param a the array to sort
     * @param key the key to search for
     * @param c the comparator used to sort the array
     * @param keyflag true if the maximum index matching a key is
     *        requested; false if the minimum index matching a key is
     *        requested
     * @return an index into the array whose value matches the key;
     *         otherwise (-(insertion_point) - 1)
     */
    public static int binarySearch(int[] a, int key,
				   IntComparator c, boolean keyflag)
    {
	return binarySearch(a, 0, a.length, key, c, keyflag);
    }

    static final IntComparator intc = new IntComparator() {
	    public int compare(int i1, int i2) {
		if (i1 < i2) return -1;
		if (i1 > i2) return 1;
		return 0;
	    }
	};


    /**
     * Binary search of a range in a sorted int array, choosing the minimum
     * or maximum index whose array value matches a key.
     * The range of indices included in the search is
     * [fromIndex, toIndex).
     * <P>
     * Note: The key is always the second argument passed to the
     * comparator's compare method.
     * @param a the array to sort
     * @param fromIndex the lowest index for the search (inclusive)
     * @param toIndex the highest index for the search (exclusive)
     * @param key the key to search for
     * @param c the comparator used to sort the array
     * @param keyflag true if the maximum index matching a key is
     *        requested; false if the minimum index matching a key is
     *        requested
     * @return an index into the array whose value matches the key;
     *         otherwise (-(insertion_point) - 1)
     */
    public static int binarySearch(int[] a, int fromIndex, int toIndex,
				   int key, IntComparator c, boolean keyflag)
    {
	if (c == null) {
	    c  = intc;
	}
	IntTimSort.checkStartAndEnd(a.length, fromIndex, toIndex);
	int low = fromIndex;
	int high = toIndex - 1;
	if (c.compare(a[high], key) < 0) return (-toIndex - 1);
	while (low <= high) {
	    int middle = (low + high) >>> 1;
	    int test = c.compare(a[middle], key);
	    if (test < 0) {
		low = middle + 1;
	    } else if (test > 0) {
		high = middle - 1;
	    } else {
		if (keyflag) {
		    if (low == middle) {
			test = c.compare(a[high], key);
			if (test == 0) {
			    return high;
			} else {
			    return middle;
			}
		    } else if (high == middle) {
			return middle;
		    } else {
			low = middle;
		    }
		} else {
		    if (low == middle) {
			return middle;
		    } else {
			high = middle;
		    }
		}
	    }
	}
	return -low - 1;
    }


    /**
     * Sort an array whose components are the primitive type int.
     * @param a the array to sort
     * @param c the comparator used to determine the order of elements
     *        in the sorted array
     */
    public static void sort(int[] a, IntComparator c) {
	if (c == null) Arrays.sort(a);
	else IntTimSort.sort(a, c);
    }

    /**
     * Sort a range of an array whose components are the primitive type int.
     * The elements sorted will be those whose indices are in the interval
     * [fromIndex, toIndex).
     * @param a the array to sort
     * @param fromIndex the lowest index for the sort (inclusive)
     * @param toIndex the highest index for the sort (exclusive)
     * @param c the comparator used to determine the order of elements
     *        in the sorted array
     */
    public static void sort(int[] a, int fromIndex, int toIndex,
			    IntComparator c) {
	if (c == null) Arrays.sort(a, fromIndex, toIndex);
	else IntTimSort.sort(a, fromIndex, toIndex, c);
    }

    /**
     * Binary search of a sorted byte array.
     * <P>
     * Note: The key is always the second argument passed to the
     * comparator's compare method.
     * @param a the array to sort
     * @param key the key to search for
     * @param c the comparator used to sort the array
     * @return an index into the array whose value matches the key;
     *         otherwise (-(insertion_point) - 1)
     */
    public static int binarySearch(byte[] a, byte key, ByteComparator c) {
	return binarySearch(a, 0, a.length, key, c);
    }

    /**
     * Binary search of a range in a sorted byte array.
     * The range of indices included in the search is
     * [fromIndex, toIndex).
     * <P>
     * Note: The key is always the second argument passed to the
     * comparator's compare method.
     * @param a the array to sort
     * @param fromIndex the lowest index for the search (inclusive)
     * @param toIndex the highest index for the search (exclusive)
     * @param key the key to search for
     * @param c the comparator used to sort the array
     * @return an index into the array whose value matches the key;
     *         otherwise (-(insertion_point) - 1)
     */
    public static int binarySearch(byte[] a, int fromIndex, int toIndex,
				   byte key, ByteComparator c)
    {
	if (c == null) {
	    return Arrays.binarySearch(a, fromIndex, toIndex, key);
	}
	ByteTimSort.checkStartAndEnd(a.length, fromIndex, toIndex);
	int low = fromIndex;
	int high = toIndex -1;
	if (c.compare(a[high], key) < 0) return (-toIndex - 1);
	while (low <= high) {
	    int middle = (low + high) >>> 1;
	    byte value = a[middle];
	    int test = c.compare(value, key);
	    if (test < 0) {
		low = middle + 1;
	    } else if (test > 0) {
		high = middle - 1;
	    } else {
		return middle;
	    }
	}
	return -low - 1;
    }

    /**
     * Binary search of a sorted byte array, choosing the minimum
     * or maximum index whose array value matches a key.
     * <P>
     * Note: The key is always the second argument passed to the
     * comparator's compare method.
     * @param a the array to sort
     * @param key the key to search for
     * @param c the comparator used to sort the array
     * @param keyflag true if the maximum index matching a key is
     *        requested; false if the minimum index matching a key is
     *        requested
     * @return an index into the array whose value matches the key;
     *         otherwise (-(insertion_point) - 1)
     */
    public static int binarySearch(byte[] a, byte key,
				   ByteComparator c, boolean keyflag)
    {
	return binarySearch(a, 0, a.length, key, c, keyflag);
    }

    static final ByteComparator bytec = new ByteComparator() {
	    public int compare(byte i1, byte i2) {
		if (i1 < i2) return -1;
		if (i1 > i2) return 1;
		return 0;
	    }
	};


    /**
     * Binary search of a range in a sorted byte array, choosing the minimum
     * or maximum index whose array value matches a key.
     * The range of indices included in the search is
     * [fromIndex, toIndex).
     * <P>
     * Note: The key is always the second argument passed to the
     * comparator's compare method.
     * @param a the array to sort
     * @param fromIndex the lowest index for the search (inclusive)
     * @param toIndex the highest index for the search (exclusive)
     * @param key the key to search for
     * @param c the comparator used to sort the array
     * @param keyflag true if the maximum index matching a key is
     *        requested; false if the minimum index matching a key is
     *        requested
     * @return an index into the array whose value matches the key;
     *         otherwise (-(insertion_point) - 1)
     */
    public static int binarySearch(byte[] a, int fromIndex, int toIndex,
				   byte key, ByteComparator c, boolean keyflag)
    {
	if (c == null) {
	    c = bytec;
	}
	ByteTimSort.checkStartAndEnd(a.length, fromIndex, toIndex);
	int low = fromIndex;
	int high = toIndex - 1;
	if (c.compare(a[high], key) < 0) return (-toIndex - 1);
	while (low <= high) {
	    int middle = (low + high) >>> 1;
	    int test = c.compare(a[middle], key);
	    if (test < 0) {
		low = middle + 1;
	    } else if (test > 0) {
		high = middle - 1;
	    } else {
		if (keyflag) {
		    if (low == middle) {
			test = c.compare(a[high], key);
			if (test == 0) {
			    return high;
			} else {
			    return middle;
			}
		    } else if (high == middle) {
			return middle;
		    } else {
			low = middle;
		    }
		} else {
		    if (low == middle) {
			return middle;
		    } else {
			high = middle;
		    }
		}
	    }
	}
	return -low - 1;
    }


    /**
     * Sort an array whose components are the primitive type byte.
     * @param a the array to sort
     * @param c the comparator used to determine the order of elements
     *        in the sorted array
     */
    public static void sort(byte[] a, ByteComparator c) {
	if (c == null) Arrays.sort(a);
	else ByteTimSort.sort(a, c);
    }

    /**
     * Sort a range of an array whose components are the primitive type byte.
     * The elements sorted will be those whose indices are in the interval
     * [fromIndex, toIndex).
     * @param a the array to sort
     * @param fromIndex the lowest index for the sort (inclusive)
     * @param toIndex the highest index for the sort (exclusive)
     * @param c the comparator used to determine the order of elements
     *        in the sorted array
     */
    public static void sort(byte[] a, int fromIndex, int toIndex,
			    ByteComparator c) {
	if (c == null) Arrays.sort(a, fromIndex, toIndex);
	else ByteTimSort.sort(a, fromIndex, toIndex, c);
    }

    /**
     * Binary search of a sorted char array.
     * <P>
     * Note: The key is always the second argument passed to the
     * comparator's compare method.
     * @param a the array to sort
     * @param key the key to search for
     * @param c the comparator used to sort the array
     * @return an index into the array whose value matches the key;
     *         otherwise (-(insertion_point) - 1)
     */
    public static int binarySearch(char[] a, char key, CharComparator c) {
	return binarySearch(a, 0, a.length, key, c);
    }

    /**
     * Binary search of a range in a sorted char array.
     * The range of indices included in the search is
     * [fromIndex, toIndex).
     * <P>
     * Note: The key is always the second argument passed to the
     * comparator's compare method.
     * @param a the array to sort
     * @param fromIndex the lowest index for the search (inclusive)
     * @param toIndex the highest index for the search (exclusive)
     * @param key the key to search for
     * @param c the comparator used to sort the array
     * @return an index into the array whose value matches the key;
     *         otherwise (-(insertion_point) - 1)
     */
    public static int binarySearch(char[] a, int fromIndex, int toIndex,
				   char key, CharComparator c)
    {
	if (c == null) {
	    return Arrays.binarySearch(a, fromIndex, toIndex, key);
	}
	CharTimSort.checkStartAndEnd(a.length, fromIndex, toIndex);
	int low = fromIndex;
	int high = toIndex -1;
	if (c.compare(a[high], key) < 0) return (-toIndex - 1);
	while (low <= high) {
	    int middle = (low + high) >>> 1;
	    char value = a[middle];
	    int test = c.compare(value, key);
	    if (test < 0) {
		low = middle + 1;
	    } else if (test > 0) {
		high = middle - 1;
	    } else {
		return middle;
	    }
	}
	return -low - 1;
    }


    /**
     * Binary search of a sorted char array, choosing the minimum
     * or maximum index whose array value matches a key.
     * <P>
     * Note: The key is always the second argument passed to the
     * comparator's compare method.
     * @param a the array to sort
     * @param key the key to search for
     * @param c the comparator used to sort the array
     * @param keyflag true if the maximum index matching a key is
     *        requested; false if the minimum index matching a key is
     *        requested
     * @return an index into the array whose value matches the key;
     *         otherwise (-(insertion_point) - 1)
     */
    public static int binarySearch(char[] a, char key,
				   CharComparator c, boolean keyflag)
    {
	return binarySearch(a, 0, a.length, key, c, keyflag);
    }

    static final CharComparator charc = new CharComparator() {
	    public int compare(char i1, char i2) {
		if (i1 < i2) return -1;
		if (i1 > i2) return 1;
		return 0;
	    }
	};

    /**
     * Binary search of a range in a sorted char array, choosing the minimum
     * or maximum index whose array value matches a key.
     * The range of indices included in the search is
     * [fromIndex, toIndex).
     * <P>
     * Note: The key is always the second argument passed to the
     * comparator's compare method.
     * @param a the array to sort
     * @param fromIndex the lowest index for the search (inclusive)
     * @param toIndex the highest index for the search (exclusive)
     * @param key the key to search for
     * @param c the comparator used to sort the array
     * @param keyflag true if the maximum index matching a key is
     *        requested; false if the minimum index matching a key is
     *        requested
     * @return an index into the array whose value matches the key;
     *         otherwise (-(insertion_point) - 1)
     */
    public static int binarySearch(char[] a, int fromIndex, int toIndex,
				   char key, CharComparator c, boolean keyflag)
    {
	if (c == null) {
	    c = charc;
	}
	CharTimSort.checkStartAndEnd(a.length, fromIndex, toIndex);
	int low = fromIndex;
	int high = toIndex - 1;
	if (c.compare(a[high], key) < 0) return (-toIndex - 1);
	while (low <= high) {
	    int middle = (low + high) >>> 1;
	    int test = c.compare(a[middle], key);
	    if (test < 0) {
		low = middle + 1;
	    } else if (test > 0) {
		high = middle - 1;
	    } else {
		if (keyflag) {
		    if (low == middle) {
			test = c.compare(a[high], key);
			if (test == 0) {
			    return high;
			} else {
			    return middle;
			}
		    } else if (high == middle) {
			return middle;
		    } else {
			low = middle;
		    }
		} else {
		    if (low == middle) {
			return middle;
		    } else {
			high = middle;
		    }
		}
	    }
	}
	return -low - 1;
    }


    /**
     * Sort an array whose components are the primitive type char.
     * @param a the array to sort
     * @param c the comparator used to determine the order of elements
     *        in the sorted array
     */
    public static void sort(char[] a, CharComparator c) {
	if (c == null) Arrays.sort(a);
	else CharTimSort.sort(a, c);
    }

    /**
     * Sort a range of an array whose components are the primitive type char.
     * The elements sorted will be those whose indices are in the interval
     * [fromIndex, toIndex).
     * @param a the array to sort
     * @param fromIndex the lowest index for the sort (inclusive)
     * @param toIndex the highest index for the sort (exclusive)
     * @param c the comparator used to determine the order of elements
     *        in the sorted array
     */
    public static void sort(char[] a, int fromIndex, int toIndex,
			    CharComparator c) {
	if (c == null) Arrays.sort(a, fromIndex, toIndex);
	else CharTimSort.sort(a, fromIndex, toIndex, c);
    }

    /**
     * Binary search of a sorted short array.
     * <P>
     * Note: The key is always the second argument passed to the
     * comparator's compare method.
     * @param a the array to sort
     * @param key the key to search for
     * @param c the comparator used to sort the array
     * @return an index into the array whose value matches the key;
     *         otherwise (-(insertion_point) - 1)
     */
    public static int binarySearch(short[] a, short key, ShortComparator c) {
	return binarySearch(a, 0, a.length, key, c);
    }

    /**
     * Binary search of a range in a sorted short array.
     * The range of indices included in the search is
     * [fromIndex, toIndex).
     * <P>
     * Note: The key is always the second argument passed to the
     * comparator's compare method.
     * @param a the array to sort
     * @param fromIndex the lowest index for the search (inclusive)
     * @param toIndex the highest index for the search (exclusive)
     * @param key the key to search for
     * @param c the comparator used to sort the array
     * @return an index into the array whose value matches the key;
     *         otherwise (-(insertion_point) - 1)
     */
    public static int binarySearch(short[] a, int fromIndex, int toIndex,
				   short key, ShortComparator c)
    {
	if (c == null) {
	    return Arrays.binarySearch(a, fromIndex, toIndex, key);
	}
	ShortTimSort.checkStartAndEnd(a.length, fromIndex, toIndex);
	int low = fromIndex;
	int high = toIndex -1;
	if (c.compare(a[high], key) < 0) return (-toIndex - 1);
	while (low <= high) {
	    int middle = (low + high) >>> 1;
	    short value = a[middle];
	    int test = c.compare(value, key);
	    if (test < 0) {
		low = middle + 1;
	    } else if (test > 0) {
		high = middle - 1;
	    } else {
		return middle;
	    }
	}
	return -low - 1;
    }

    /**
     * Binary search of a sorted short array, choosing the minimum
     * or maximum index whose array value matches a key.
     * <P>
     * Note: The key is always the second argument passed to the
     * comparator's compare method.
     * @param a the array to sort
     * @param key the key to search for
     * @param c the comparator used to sort the array
     * @param keyflag true if the maximum index matching a key is
     *        requested; false if the minimum index matching a key is
     *        requested
     * @return an index into the array whose value matches the key;
     *         otherwise (-(insertion_point) - 1)
     */
    public static int binarySearch(short[] a, short key,
				   ShortComparator c, boolean keyflag)
    {
	return binarySearch(a, 0, a.length, key, c, keyflag);
    }

    static final ShortComparator shortc = new ShortComparator() {
	    public int compare(short i1, short i2) {
		if (i1 < i2) return -1;
		if (i1 > i2) return 1;
		return 0;
	    }
	};


    /**
     * Binary search of a range in a sorted short array, choosing the minimum
     * or maximum index whose array value matches a key.
     * The range of indices included in the search is
     * [fromIndex, toIndex).
     * <P>
     * Note: The key is always the second argument passed to the
     * comparator's compare method.
     * @param a the array to sort
     * @param fromIndex the lowest index for the search (inclusive)
     * @param toIndex the highest index for the search (exclusive)
     * @param key the key to search for
     * @param c the comparator used to sort the array
     * @param keyflag true if the maximum index matching a key is
     *        requested; false if the minimum index matching a key is
     *        requested
     * @return an index into the array whose value matches the key;
     *         otherwise (-(insertion_point) - 1)
     */
    public static int binarySearch(short[] a, int fromIndex, int toIndex,
				   short key, ShortComparator c,
				   boolean keyflag)
    {
	if (c == null) {
	    c = shortc;
	}
	ShortTimSort.checkStartAndEnd(a.length, fromIndex, toIndex);
	int low = fromIndex;
	int high = toIndex - 1;
	if (c.compare(a[high], key) < 0) return (-toIndex - 1);
	while (low <= high) {
	    int middle = (low + high) >>> 1;
	    int test = c.compare(a[middle], key);
	    if (test < 0) {
		low = middle + 1;
	    } else if (test > 0) {
		high = middle - 1;
	    } else {
		if (keyflag) {
		    if (low == middle) {
			test = c.compare(a[high], key);
			if (test == 0) {
			    return high;
			} else {
			    return middle;
			}
		    } else if (high == middle) {
			return middle;
		    } else {
			low = middle;
		    }
		} else {
		    if (low == middle) {
			return middle;
		    } else {
			high = middle;
		    }
		}
	    }
	}
	return -low - 1;
    }


    /**
     * Sort an array whose components are the primitive type short.
     * @param a the array to sort
     * @param c the comparator used to determine the order of elements
     *        in the sorted array
     */
    public static void sort(short[] a, ShortComparator c) {
	if (c == null) Arrays.sort(a);
	else ShortTimSort.sort(a, c);
    }

    /**
     * Sort a range of an array whose components are the primitive type short.
     * The elements sorted will be those whose indices are in the interval
     * [fromIndex, toIndex).
     * @param a the array to sort
     * @param fromIndex the lowest index for the sort (inclusive)
     * @param toIndex the highest index for the sort (exclusive)
     * @param c the comparator used to determine the order of elements
     *        in the sorted array
     */
    public static void sort(short[] a, int fromIndex, int toIndex,
			    ShortComparator c) {
	if (c == null) Arrays.sort(a, fromIndex, toIndex);
	else ShortTimSort.sort(a, fromIndex, toIndex, c);
    }

    /**
     * Binary search of a sorted long array.
     * <P>
     * Note: The key is always the second argument passed to the
     * comparator's compare method.
     * @param a the array to sort
     * @param key the key to search for
     * @param c the comparator used to sort the array
     * @return an index into the array whose value matches the key;
     *         otherwise (-(insertion_point) - 1)
     */
    public static int binarySearch(long[] a, long key, LongComparator c) {
	return binarySearch(a, 0, a.length, key, c);
    }

    /**
     * Binary search of a range in a sorted long array.
     * The range of indices included in the search is
     * [fromIndex, toIndex).
     * <P>
     * Note: The key is always the second argument passed to the
     * comparator's compare method.
     * @param a the array to sort
     * @param fromIndex the lowest index for the search (inclusive)
     * @param toIndex the highest index for the search (exclusive)
     * @param key the key to search for
     * @param c the comparator used to sort the array
     * @return an index into the array whose value matches the key;
     *         otherwise (-(insertion_point) - 1)
     */
    public static int binarySearch(long[] a, int fromIndex, int toIndex,
				   long key, LongComparator c)
    {
	if (c == null) {
	    return Arrays.binarySearch(a, fromIndex, toIndex, key);
	}
	LongTimSort.checkStartAndEnd(a.length, fromIndex, toIndex);
	int low = fromIndex;
	int high = toIndex -1;
	if (c.compare(a[high], key) < 0) return (-toIndex - 1);
	while (low <= high) {
	    int middle = (low + high) >>> 1;
	    long value = a[middle];
	    int test = c.compare(value, key);
	    if (test < 0) {
		low = middle + 1;
	    } else if (test > 0) {
		high = middle - 1;
	    } else {
		return middle;
	    }
	}
	return -low - 1;
    }

    /**
     * Binary search of a sorted long array, choosing the minimum
     * or maximum index whose array value matches a key.
     * <P>
     * Note: The key is always the second argument passed to the
     * comparator's compare method.
     * @param a the array to sort
     * @param key the key to search for
     * @param c the comparator used to sort the array
     * @param keyflag true if the maximum index matching a key is
     *        requested; false if the minimum index matching a key is
     *        requested
     * @return an index into the array whose value matches the key;
     *         otherwise (-(insertion_point) - 1)
     */
    public static int binarySearch(long[] a, long key,
				   LongComparator c, boolean keyflag)
    {
	return binarySearch(a, 0, a.length, key, c, keyflag);
    }

    static final LongComparator longc = new LongComparator() {
	    public int compare(long i1, long i2) {
		if (i1 < i2) return -1;
		if (i1 > i2) return 1;
		return 0;
	    }
	};


    /**
     * Binary search of a range in a sorted long array, choosing the minimum
     * or maximum index whose array value matches a key.
     * The range of indices included in the search is
     * [fromIndex, toIndex).
     * <P>
     * Note: The key is always the second argument passed to the
     * comparator's compare method.
     * @param a the array to sort
     * @param fromIndex the lowest index for the search (inclusive)
     * @param toIndex the highest index for the search (exclusive)
     * @param key the key to search for
     * @param c the comparator used to sort the array
     * @param keyflag true if the maximum index matching a key is
     *        requested; false if the minimum index matching a key is
     *        requested
     * @return an index into the array whose value matches the key;
     *         otherwise (-(insertion_point) - 1)
     */
    public static int binarySearch(long[] a, int fromIndex, int toIndex,
				   long key, LongComparator c, boolean keyflag)
    {
	if (c == null) {
	    c = longc;
	}
	LongTimSort.checkStartAndEnd(a.length, fromIndex, toIndex);
	int low = fromIndex;
	int high = toIndex - 1;
	if (c.compare(a[high], key) < 0) return (-toIndex - 1);
	while (low <= high) {
	    int middle = (low + high) >>> 1;
	    int test = c.compare(a[middle], key);
	    if (test < 0) {
		low = middle + 1;
	    } else if (test > 0) {
		high = middle - 1;
	    } else {
		if (keyflag) {
		    if (low == middle) {
			test = c.compare(a[high], key);
			if (test == 0) {
			    return high;
			} else {
			    return middle;
			}
		    } else if (high == middle) {
			return middle;
		    } else {
			low = middle;
		    }
		} else {
		    if (low == middle) {
			return middle;
		    } else {
			high = middle;
		    }
		}
	    }
	}
	return -low - 1;
    }


    /**
     * Sort an array whose components are the primitive type long.
     * @param a the array to sort
     * @param c the comparator used to determine the order of elements
     *        in the sorted array
     */
    public static void sort(long[] a, LongComparator c) {
	if (c == null) Arrays.sort(a);
	else LongTimSort.sort(a, c);
    }

    /**
     * Sort a range of an array whose components are the primitive type long.
     * The elements sorted will be those whose indices are in the interval
     * [fromIndex, toIndex).
     * @param a the array to sort
     * @param fromIndex the lowest index for the sort (inclusive)
     * @param toIndex the highest index for the sort (exclusive)
     * @param c the comparator used to determine the order of elements
     *        in the sorted array
     */
    public static void sort(long[] a, int fromIndex, int toIndex,
			    LongComparator c) {
	if (c == null) Arrays.sort(a, fromIndex, toIndex);
	else LongTimSort.sort(a, fromIndex, toIndex, c);
    }

    /**
     * Binary search of a sorted float array.
     * <P>
     * Note: The key is always the second argument passed to the
     * comparator's compare method.
     * @param a the array to sort
     * @param key the key to search for
     * @param c the comparator used to sort the array
     * @return an index into the array whose value matches the key;
     *         otherwise (-(insertion_point) - 1)
     */
    public static int binarySearch(float[] a, float key, FloatComparator c) {
	return binarySearch(a, 0, a.length, key, c);
    }

    /**
     * Binary search of a range in a sorted float array.
     * The range of indices included in the search is
     * [fromIndex, toIndex).
     * <P>
     * Note: The key is always the second argument passed to the
     * comparator's compare method.
     * @param a the array to sort
     * @param fromIndex the lowest index for the search (inclusive)
     * @param toIndex the highest index for the search (exclusive)
     * @param key the key to search for
     * @param c the comparator used to sort the array
     * @return an index into the array whose value matches the key;
     *         otherwise (-(insertion_point) - 1)
     */
    public static int binarySearch(float[] a, int fromIndex, int toIndex,
				   float key, FloatComparator c)
    {
	if (c == null) {
	    return Arrays.binarySearch(a, fromIndex, toIndex, key);
	}
	FloatTimSort.checkStartAndEnd(a.length, fromIndex, toIndex);
	int low = fromIndex;
	int high = toIndex -1;
	if (c.compare(a[high], key) < 0) return (-toIndex - 1);
	while (low <= high) {
	    int middle = (low + high) >>> 1;
	    float value = a[middle];
	    int test = c.compare(value, key);
	    if (test < 0) {
		low = middle + 1;
	    } else if (test > 0) {
		high = middle - 1;
	    } else {
		return middle;
	    }
	}
	return -low - 1;
    }

    /**
     * Binary search of a sorted float array, choosing the minimum
     * or maximum index whose array value matches a key.
     * <P>
     * Note: The key is always the second argument passed to the
     * comparator's compare method.
     * @param a the array to sort
     * @param key the key to search for
     * @param c the comparator used to sort the array
     * @param keyflag true if the maximum index matching a key is
     *        requested; false if the minimum index matching a key is
     *        requested
     * @return an index into the array whose value matches the key;
     *         otherwise (-(insertion_point) - 1)
     */
    public static int binarySearch(float[] a, float key,
				   FloatComparator c, boolean keyflag)
    {
	return binarySearch(a, 0, a.length, key, c, keyflag);
    }

    static final FloatComparator floatc = new FloatComparator() {
	    public int compare(float i1, float i2) {
		if (i1 < i2) return -1;
		if (i1 > i2) return 1;
		return 0;
	    }
	};

    /**
     * Binary search of a range in a sorted float array, choosing the minimum
     * or maximum index whose array value matches a key.
     * The range of indices included in the search is
     * [fromIndex, toIndex).
     * <P>
     * Note: The key is always the second argument passed to the
     * comparator's compare method.
     * @param a the array to sort
     * @param fromIndex the lowest index for the search (inclusive)
     * @param toIndex the highest index for the search (exclusive)
     * @param key the key to search for
     * @param c the comparator used to sort the array
     * @param keyflag true if the maximum index matching a key is
     *        requested; false if the minimum index matching a key is
     *        requested
     * @return an index into the array whose value matches the key;
     *         otherwise (-(insertion_point) - 1)
     */
    public static int binarySearch(float[] a, int fromIndex, int toIndex,
				   float key, FloatComparator c,
				   boolean keyflag)
    {
	if (c == null) {
	    c = floatc;
	}
	FloatTimSort.checkStartAndEnd(a.length, fromIndex, toIndex);
	int low = fromIndex;
	int high = toIndex - 1;
	if (c.compare(a[high], key) < 0) return (-toIndex - 1);
	while (low <= high) {
	    int middle = (low + high) >>> 1;
	    int test = c.compare(a[middle], key);
	    if (test < 0) {
		low = middle + 1;
	    } else if (test > 0) {
		high = middle - 1;
	    } else {
		if (keyflag) {
		    if (low == middle) {
			test = c.compare(a[high], key);
			if (test == 0) {
			    return high;
			} else {
			    return middle;
			}
		    } else if (high == middle) {
			return middle;
		    } else {
			low = middle;
		    }
		} else {
		    if (low == middle) {
			return middle;
		    } else {
			high = middle;
		    }
		}
	    }
	}
	return -low - 1;
    }


    /**
     * Sort an array whose components are the primitive type float.
     * @param a the array to sort
     * @param c the comparator used to determine the order of elements
     *        in the sorted array
     */
    public static void sort(float[] a, FloatComparator c) {
	if (c == null) Arrays.sort(a);
	else FloatTimSort.sort(a, c);
    }

    /**
     * Sort a range of an array whose components are the primitive type float.
     * The elements sorted will be those whose indices are in the interval
     * [fromIndex, toIndex).
     * @param a the array to sort
     * @param fromIndex the lowest index for the sort (inclusive)
     * @param toIndex the highest index for the sort (exclusive)
     * @param c the comparator used to determine the order of elements
     *        in the sorted array
     */
    public static void sort(float[] a, int fromIndex, int toIndex,
			    FloatComparator c) {
	if (c == null) Arrays.sort(a, fromIndex, toIndex);
	else FloatTimSort.sort(a, fromIndex, toIndex, c);
    }

    /**
     * Binary search of a sorted double array.
     * <P>
     * Note: The key is always the second argument passed to the
     * comparator's compare method.
     * @param a the array to sort
     * @param key the key to search for
     * @param c the comparator used to sort the array
     * @return an index into the array whose value matches the key;
     *         otherwise (-(insertion_point) - 1)
     */
    public static int binarySearch(double[] a, double key, DoubleComparator c) {
	return binarySearch(a, 0, a.length, key, c);
    }

    /**
     * Binary search of a range in a sorted double array.
     * The range of indices included in the search is
     * [fromIndex, toIndex).
     * <P>
     * Note: The key is always the second argument passed to the
     * comparator's compare method.
     * @param a the array to sort
     * @param fromIndex the lowest index for the search (inclusive)
     * @param toIndex the highest index for the search (exclusive)
     * @param key the key to search for
     * @param c the comparator used to sort the array
     * @return an index into the array whose value matches the key;
     *         otherwise (-(insertion_point) - 1)
     */
    public static int binarySearch(double[] a, int fromIndex, int toIndex,
				   double key, DoubleComparator c)
    {
	if (c == null) {
	    return Arrays.binarySearch(a, fromIndex, toIndex, key);
	}
	DoubleTimSort.checkStartAndEnd(a.length, fromIndex, toIndex);
	int low = fromIndex;
	int high = toIndex -1;
	if (c.compare(a[high], key) < 0) return (-toIndex - 1);
	while (low <= high) {
	    int middle = (low + high) >>> 1;
	    double value = a[middle];
	    int test = c.compare(value, key);
	    if (test < 0) {
		low = middle + 1;
	    } else if (test > 0) {
		high = middle - 1;
	    } else {
		return middle;
	    }
	}
	return -low - 1;
    }

    /**
     * Binary search of a sorted double array, choosing the minimum
     * or maximum index whose array value matches a key.
     * <P>
     * Note: The key is always the second argument passed to the
     * comparator's compare method.
     * @param a the array to sort
     * @param key the key to search for
     * @param c the comparator used to sort the array
     * @param keyflag true if the maximum index matching a key is
     *        requested; false if the minimum index matching a key is
     *        requested
     * @return an index into the array whose value matches the key;
     *         otherwise (-(insertion_point) - 1)
     */
    public static int binarySearch(double[] a, double key,
				   DoubleComparator c, boolean keyflag)
    {
	return binarySearch(a, 0, a.length, key, c, keyflag);
    }

    static final DoubleComparator doublec = new DoubleComparator() {
	    public int compare(double i1, double i2) {
		if (i1 < i2) return -1;
		if (i1 > i2) return 1;
		return 0;
	    }
	};

    /**
     * Binary search of a range in a sorted double array, choosing the minimum
     * or maximum index whose array value matches a key.
     * The range of indices included in the search is
     * [fromIndex, toIndex).
     * <P>
     * Note: The key is always the second argument passed to the
     * comparator's compare method.
     * @param a the array to sort
     * @param fromIndex the lowest index for the search (inclusive)
     * @param toIndex the highest index for the search (exclusive)
     * @param key the key to search for
     * @param c the comparator used to sort the array
     * @param keyflag true if the maximum index matching a key is
     *        requested; false if the minimum index matching a key is
     *        requested
     * @return an index into the array whose value matches the key;
     *         otherwise (-(insertion_point) - 1)
     */
    public static int binarySearch(double[] a, int fromIndex, int toIndex,
				   double key, DoubleComparator c,
				   boolean keyflag)
    {
	if (c == null) {
	    c = doublec;
	}
	DoubleTimSort.checkStartAndEnd(a.length, fromIndex, toIndex);
	int low = fromIndex;
	int high = toIndex - 1;
	if (c.compare(a[high], key) < 0) return (-toIndex - 1);
	while (low <= high) {
	    int middle = (low + high) >>> 1;
	    int test = c.compare(a[middle], key);
	    if (test < 0) {
		low = middle + 1;
	    } else if (test > 0) {
		high = middle - 1;
	    } else {
		if (keyflag) {
		    if (low == middle) {
			test = c.compare(a[high], key);
			if (test == 0) {
			    return high;
			} else {
			    return middle;
			}
		    } else if (high == middle) {
			return middle;
		    } else {
			low = middle;
		    }
		} else {
		    if (low == middle) {
			return middle;
		    } else {
			high = middle;
		    }
		}
	    }
	}
	return -low - 1;
    }


    /**
     * Sort an array whose components are the primitive type double.
     * @param a the array to sort
     * @param c the comparator used to determine the order of elements
     *        in the sorted array
     */
    public static void sort(double[] a, DoubleComparator c) {
	if (c == null) Arrays.sort(a);
	else DoubleTimSort.sort(a, c);
    }

    /**
     * Sort a range of an array whose components are the primitive type double.
     * The elements sorted will be those whose indices are in the interval
     * [fromIndex, toIndex).
     * @param a the array to sort
     * @param fromIndex the lowest index for the sort (inclusive)
     * @param toIndex the highest index for the sort (exclusive)
     * @param c the comparator used to determine the order of elements
     *        in the sorted array
     */
    public static void sort(double[] a, int fromIndex, int toIndex,
			    DoubleComparator c) {
	if (c == null) Arrays.sort(a, fromIndex, toIndex);
	else DoubleTimSort.sort(a, fromIndex, toIndex, c);
    }
}
//  LocalWords:  IntComparator indices fromIndex toIndex keyflag GPL
//  LocalWords:  TimSort openJDK
