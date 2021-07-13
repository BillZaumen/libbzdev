package org.bzdev.util;
import org.bzdev.lang.MathOps;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.HashMap;
import java.util.NoSuchElementException;
import org.bzdev.lang.UnexpectedExceptionError;

//@exbundle org.bzdev.util.lpack.Util

/**
 * Suffix Array class.
 * Suppose a sequence is represented by an array of integers with each
 * element the member of an alphabet consisting of the values [0, n)
 * for a fixed positive integer n.  The alphabet can be augmented with
 * a sentinel character that is lexically smaller than any member of
 * the alphabet (the choice made for this class is -1 for most types
 * and 0xFFFF for char, which is an unsigned type. The Unicode value
 * 0xFFFF is interpreted as "not a character" and thus should not
 * appear in any reasonable text). A suffix is a subsequence that
 * starts at some index and continues to the end of the sequence.
 * <P>
 * A suffix array is an array of indices that indicate the index for
 * the first element of each suffix, sorted into ascending lexical order.
 * the first element represents an empty sequence, and its value is
 * the length of the array (the element it points to is assumed to be
 * the sentinel character).
 * <P>
 * Suffix arrays have numerous applications including string searching
 * (e.g., exact matches and common substrings), and computational
 * biology.  Because of the importance of this data structure, a
 * considerable amount of work has gone into finding efficient
 * algorithms for generating suffix arrays. The following provides a
 * list of citations for some of this research:
 * <UL>
 *   <LI><A href="https://local.ugene.unipro.ru/tracker/secure/attachment/12144/Linear+Suffix+Array+Construction+by+Almost+Pure+Induced-Sorting.pdf">
 *       Ge Nong, Sen Zhang, and Wai Hong Chan, "Linear suffix array
 *       construction by almost pure induced-sorting", Proceeding of
 *       the 2009 Data Compression Conference, Pages 193-202, IEEE
 *       Computer Society (ISBN: 978-0-7695-3592-0
 *       doi&gt;10.1109/DCC.2009.42)</A> contains a description of an
 *       efficient algorithm for creating suffix arrays.
 *   <LI><A href="https://ge-nong.googlecode.com/files/Two%20Efficient%20Algorithms%20for%20Linear%20Time%20Suffix%20Array%20Construction.pdf">
 *       Ge Nong, Sen Zhang, and Wai Hong Chan, "Two efficient algorithms
 *       for linear time suffix array construction"</A>
 *   <LI><A href="http://zork.net/~st/jottings/sais.html">A walk through the SA-IS
 *       Suffix Array Construction Algorithm</A> provides a Python implementation
 *       of the algorithm and is a useful starting point for an implementation.
 *   <LI><A href="http://alumni.cs.ucr.edu/~rakthant/cs234/01_KLAAP_Linear%20time%20LCP.PDF">
 *       Toru Kasai, Gunho Lee , Hiroki Arimur, Setsuo Arikawa, and Kunsoo Park,
 *       "Linear-time longest-common-prefix computation in suffix arrays and its
 *       applications"</A> provides an algorithm for computing an LCP array
 *       (Longest Common Prefix).
 *   <LI><A href="https://www.cs.helsinki.fi/u/tpkarkka/opetus/11s/spa/lecture10.pdf">
 *        LCP Array Construction</A> and
 *       <A href="http://codeforces.com/blog/entry/12796">
 *       LCP from suffix array</A> has some coding examples.
 * </UL>
 * The coding examples and algorithm descriptions cited above cannot be
 * used literally for the LCP computation because they make differing
 * assumptions as to the format of the suffix array, specifically its
 * first entry.  Some authors start a suffix array with an index for
 * a sentinel character that may not be literally present in the string:
 * this character is considered not be part of the alphabet and is lexically
 * smaller than each 'letter' in the alphabet. In addition, some descriptions
 * of the algorithms start an array index at 1 while others start it at 0.
 * <P>
 * The SuffixArray class is an abstract class, with several subclasses
 * defined as nested classes:
 * <UL>
 *   <LI>SuffixArray.Integer. The sequence is an array of integers.
 *   <LI>SuffixArray.Char. The sequence is an array of characters.
 *   <LI>SuffixArray.Short. The sequence is an array of short integers.
 *   <LI>SuffixArray.UnsignedShort. The sequence is an array of short integers,
 *       treated as unsigned numbers(for a short h, the corresponding
 *       unsigned value is 0xFFFF &amp; h).
 *   <LI>SuffixArray.Byte. The sequence is an array of bytes.
 *   <LI>SuffixArray.UnsignedByte. The sequence is an array of bytes,
 *       treated as unsigned numbers (for a byte b, the corresponding
 *       unsigned value is 0xFF &amp; b).
 *   <LI>SuffixArray.String. The sequence is a string, If an alphabet is
 *       provided, its type must be Set&lt;Character&gt;.
 *   <LI>SuffixArray.Array. The sequence is an array of objects of
 *        type T, where T is a type parameter. An
 *       alphabet is required and its type must be Set&lt;T&gt;.
 * </UL>
 * When an explicit alphabet is used it will be represented as a
 * {@link java.util.Set}, and will be mapped to a sequence of
 * integers, starting at 0, and in the order implied by the set's iterator.
 * To force the use of a particular order, use {@link java.util.LinkedHashSet}
 * or {@link java.util.TreeSet}.
 * Regardless of how the alphabet is defined, the algorithm used to
 * construct the suffix array allocates and uses arrays whose sizes
 * are equal to the size of the alphabet, so keeping the alphabet to
 * the minimum size needed is advantageous.
 * <P>
 * In this package, each subclass of SuffixArray has methods that
 * return the sequence associated with the suffix array. These methods
 * are not provided by the SuffixArray class itself because the type of
 * the value returned differs between subclasses.
 * <P>
 * The methods {@link #getArray()} and {@link #getLCP()} can be used
 * to get arrays that can be used to simulate the traversal of
 * a suffix tree.  The method {@link #getInverse()} allows one to map
 * a suffix (represented as an index into the sequence array or string)
 * into the corresponding index in the suffix array.
 * <P>
 * Subclasses provide additional methods in cases where a method's signature
 * is dependent on types (especially primitive types) associated with that
 * subclass. These classes to not have corresponding abstract methods in
 * {@link SuffixArray}. For the current package, several such methods are
 * provided by all the subclasses of {@link SuffixArray}:
 * <UL>
 *  <LI> getSequence. This method will return the sequence used to create
 *       a suffix array.
 *  <LI> getBWT. This method will compute the Burrows-Wheeler transform
 *       for this suffix array. Aside from its use in data compression,
 *       some search algorithms use this transform.
 *  <LI> findInstance.  This method (there are two variants) will find an
 *       offset into the sequence array where the offset matches the specified
 *       subsequence.
 *  <LI> findRange.  This method (there are two variants) will find a
 *       a range of offsets into the sequence array where each offset
 *       matches the specified subsequence.
 *  <LI> findSubsequence. This method (there are two variants) allows
 *       instances of a subsequence to be found efficiently. If n is the
 *       length of the sequence and m is the length of a subsequence, the
 *       time complexity in practice is O(m log n) if the LCP-LR auxiliary
 *       data structure is not used and is O(m + log n) if the LCP-LR
 *       auxiliary data structure is used.
 *  <LI> inverseBWT. This method (which is a static method) computes the
 *       inverse Burrows-Wheeler transform.
 * </UL>
 * <H2>Auxiliary tables</H2>
 * <P>
 * The implementation supports three auxiliary tables:
 * <UL>
 *   <IT> An inverse table. This table is implemented as an array
 *        with the same length as the suffix array.
 *        A copy of this table can be created by
 *        calling {@link #getInverse()}.  To create this table and
 *        cache it, call {@link #useInverse()}. To remove the cached
 *        table, call {@link #clearCachedInverse()}. To check if a
 *        cached table exists, call {@link #hasInverse()}.
 *   <IT> an LCP (Longest Common Prefix) table. This table is implemented
 *        as an array with the same length as the suffix array. A copy
 *        of this table can be created by calling {@link #getLCP()}.
 *        To create this table and cache it, call {@link #useLCP()}. To
 *        remove the cached table, call {@link #clearCachedLCP()}. To check
 *        if a cached LCP table exists, call {@link #hasLCP()}.
 *        Creating the LCP table requires the inverse table, so if the
 *        index table will be used after the LCP table is created,
 *        call {@link #useInverse()} before calling {@link #useLCP()} or
 *        {@link #getLCP()}.
 *   <IT> an LCP-LR table. This contains a left and right LCP table for
 *        a binary search case and is used to speed up some computations.
 *        The table consists of two protected fields, both arrays and
 *        each as large as the corresponding suffix array. To create
 *        this table, call {@link #useLCPLR()}. To remove the table, call
 *        {@link #clearCachedLCPLR()}. To check if the table exists,
 *        call {@link #hasLCPLR()}.
 * </UL>
 * One should consider removing the inverse, LCP, and LCP-LR auxiliary
 * tables when they are no longer needed, particularly if the suffix
 * array is large, given the size of these tables.
 */
public abstract class SuffixArray {

    // We need fully qualified names because this class contains
    // inner classes with similar names.
    static java.lang.String
	errorMsg(java.lang.String key, java.lang.Object... args)
    {
	return UtilErrorMsg.errorMsg(key, args);
    }

    /**
     * One higher than the length of the sequence array.
     * Subclasses must set this field.
     */
    protected int slenp1;
    /**
     * The suffix array.  Subclasses must set this field.
     */
    protected int[] array;

    /**
     * The Longest Common Prefix (LCP) length array. Subclasses may
     * read this field but should not set it or alter it.
     */
    private int[] lcpArray;

    /**
     * The inverse of the suffix array.
     * Subclasses may read this
     */
    private int[] rank;

    /**
     * Test if the inverse array is currently cached.
     * @return true if the inverse array is cached; false if it has to be
     *         recomputed
     */
    public boolean hasInverse() {
	return (rank != null);
    }

    /**
     * Configure this suffix array so that it has an inverse mapping.
     * This inverse mapping maps the offset into a sequence to the
     * corresponding offset into the suffix array. Calling this method
     * will result in the inverse table being cached.  To clear the
     * cached inverse table, call {@link #clearCachedInverse()}
     */
    public synchronized void useInverse() {
	rank = getInverse();
    }

    /**
     * Get the inverse mapping for this suffix array.
     * The inverse mapping is an array whose index is the index
     * in the sequence array and whose value is the corresponding
     * index in the suffix array.
     * @return the inverse mapping
     */
    public synchronized int[] getInverse() {
	int[] existing = rank;
	if (existing == null) {
	    existing = new int[array.length];
	    for (int i = 0; i < array.length; i++) {
		existing[array[i]] = i;
	    }
	}
	return existing;
    }

    /**
     * Clear the cached inverse mapping.
     * This can be called to reduce memory usage if all external
     * references to the mapping have been removed.  The mapping
     * can be restored if needed, but the time complexity is O(n)
     * where n is the length of the sequence.
     */
    public synchronized void clearCachedInverse() {
	rank = null;
    }

    /**
     * Clear the cached least-common-prefix array.
     * This can be called to reduce memory usage if all external
     * references to the LCP length array have been removed. The array
     * can be restored if needed, but the time complexity is O(n)
     * where n is the length of the sequence.
     */
    public synchronized void clearCachedLCP() {
	lcpArray = null;
    }

    /**
     * Fill the LCP array with the correct values.
     * (the value at index 0 will be set outside of this method).
     * <P>
     * A typical implementation would use the following code:
     * <BLOCKQUOTE><CODE><PRE>
     *  protected void fillLCPArray(int[] ourlcpArray, int[] rank) {
     *      int k = 0;
     *      int n = sequence.length;
     *	    for (int i = 0; i &lt; n; i++) {
     *	        if (rank[i] == n) {
     *		    k = 0;
     *		    continue;
     *	        }
     *	        int j = array[rank[i]+1];
     *	        while (i+k &lt; n &amp;&amp; j+k &lt; n &amp;&amp;
     *		    sequence[i+k] == sequence[j+k]) {
     *		    k++;
     *	        }
     *	        ourlcpArray[rank[i]+1] = k;
     *	        if (k &gt; 0) k--;
     *      }
     *	}
     * </PRE></CODE></BLOCKQUOTE>
     * Subclasses will have different types for the
     * variable <code>sequence</code>.
     * @param ourlcpArray the LCP array to fill
     * @param rank the inverse mapping for this suffix array
     */
    abstract protected void fillLCPArray(int[] ourlcpArray, int[] rank);

    /**
     * Test if the LCP array is currently cached.
     * @return true if the LCP array is cached; false if it has to be
     *         recomputed
     */
    public boolean hasLCP() {
	return (lcpArray != null);
    }

    /**
     * Configure this suffix array to use an LCP (Longest Common Prefix)
     * table. Calling this method will cause the LCP table to be cached.
     * To remove the cached table, call {@link #clearCachedLCP()}.
     */
    public synchronized void useLCP() {
	lcpArray = getLCP();
    }

    /**
     * Get the LCP array corresponding to this suffix array.
     * An LCP (Longest Common Prefix) array provides the length of
     * the longest common prefix for the subsequence represented by an
     * index i in a suffix array and the subsequence at index (i-1).
     * A value of -1 indicates that no value exists (this will be true
     * at index 0, as there is no subsequence at index -1).
     * <P>
     * NOTE: the algorithm used to create the LCP table requires the
     * inverse array (returned by {@link #getInverse()}.  If the inverse
     * mapping will be used later, one should call {@link #useInverse()}
     * before this method is called to avoid creating the inverse mapping
     * multiple times.
     * @return the LCP array.
     */
    public synchronized int[] getLCP() {
	if (lcpArray == null) {
	    boolean clearRank = (rank == null);
	    int[] ourRank = getInverse();
	    int[] ourlcpArray = new int[array.length];
	    ourlcpArray[0] = -1; // signal that no valid entry exists.
	    fillLCPArray(ourlcpArray, ourRank);
	    if (clearRank) rank = null;
	    return ourlcpArray;
	} else {
	    return lcpArray;
	}
    }

    /**
     * The LCP_L table.
     * @see #useLCPLR()
     */
    protected int[] LCP_L = null;

    /**
     * The LCP_R table.
     * @see #useLCPLR()
     */
    protected int[] LCP_R = null;

    /**
     * Determine if this suffix array has an LCP-LR table associated with it.
     * @return true if there is an LCP-LR table; false otherwise
     */
    public boolean hasLCPLR() {
	// check both in case there is a failure (e.g., insufficient
	// memory) that caused an exception to be thrown
	return (LCP_L != null && LCP_R != null);
    }

    /**
     * Remove the cached LCP-LR table, if any, associated with this suffix
     * array.
     * This can be called to reduce memory usage. The table
     * can be restored if needed, but the time complexity is O(n)
     * where n is the length of the sequence.
     */
    public synchronized void clearCachedLCPLR() {
	LCP_L = null;
	LCP_R = null;
    }

    /**
     * Configure a suffix array to use an LCP-LR table.
     * This table contains two entries per offset into the suffix array.
     * The 'left' entry is the minimum LCP value for a range of values
     * lower than the offset, and the 'right' entry is the minimum LCP value
     * for a range of values higher than the offset.  The length of these
     * ranges are such that the offset will be the mid point between a
     * 'low' and 'high' value during a binary search.
     * <P>
     * This table is implemented as two integer arrays, each the same size
     * as an LCP array.  For long sequences, a significant amount of
     * memory will be required.
     * <P>
     * Calling this method will result in the LCP-LR table being cached.
     * To clear the cached table, call {@link #clearCachedLCPLR()}
     */
    public synchronized void useLCPLR() {
	if (LCP_L != null && LCP_R != null) return;
	int[] lcp = getLCP();
	LCP_L = new int[lcp.length];
	LCP_R = new int [lcp.length];
	Arrays.fill(LCP_L, lcp.length);
	Arrays.fill(LCP_R, lcp.length);
	initLCPLR(1, lcp.length-1, true, lcp);
    }

    private void initLCPLR(int low, int high, boolean right, int[] lcp) {
	int diff = high - low;
	if (diff < 2) {
	    if (diff == 1) {
		if (right) {
		    LCP_R[low] = lcp[high];
		} else {
		    LCP_L[high] = lcp[high];
		}
		return;
	    } else {
		throw new IllegalStateException(high + " - " + low + " != 1");
	    }
	}
	// int mid = low + diff2;
	int mid = (high + low) >>> 1;
	initLCPLR(low, mid, false, lcp);
	initLCPLR(mid, high, true, lcp);
	if (right) {
	    LCP_R[low] = Math.min(LCP_L[mid], LCP_R[mid]);
	} else {
	    LCP_L[high] = Math.min(LCP_L[mid], LCP_R[mid]);
	}
    }

    private int lengthFromLCPLR(int low, int high) {
	if (low == 0) return 0;
	if (low == high) return array[0] - array[low];
	return lengthFromLCPLR(low, high, 1, LCP_L.length-1, true,
			   array[0]);
    }


    private int lengthFromLCPLR(int min, int max,  int low, int high,
				boolean right, int maxlen)
    {
	if (low >= min && high <= max) {
	    if (right) {
		return Math.min(LCP_R[low], maxlen);
	    } else {
		return Math.min(LCP_L[high], maxlen);
	    }
	} else if ((right && low > max) || (!right && high < min)) {
	    return maxlen;
	}

	if ((high - low) < 2) {
	    return maxlen;
	}
	int mid = (high + low) >>> 1;
	int r1 = lengthFromLCPLR(min, max, low, mid, false, maxlen);
	int r2 = lengthFromLCPLR(min, max, mid, high, true, maxlen);
	return maxlen = Math.min(r1, r2);
    }

    /**
     * Given two indices into the sequence array, compute the
     * length of a common prefix.
     * <P>
     * This method is abstract because the implementation is dependent
     * on the type of sequence-array elements. It's implementation will
     * typically compare the prefixes element by element until an index
     * is found where the elements differ. The implementation, however,
     * must not be dependent on the existence of either the LCP table
     * or the LCP_LR table.
     * @param index1 the index into the suffix array for the first suffix
     * @param index2 the index into the suffix array for the second suffix
     * @return the length of the LCP for these two suffixes
     */
    protected abstract int commonPrefixLength(int index1, int index2);

    /**
     * Get the length of the longest common prefix (LCP) for
     * two suffixes.
     * The LCP is a substring, beginning at the start of each suffix,
     * that is shared by two suffixes.
     * Each index is the offset into the suffix array, not the
     * sequence.
     * <P>
     * The algorithm used depends on how this suffix array is
     * configured:
     * <UL>
     *  <LI> if this suffix array has an LCP-LR table, an algorithm
     *       whose running time is comparable to that for a binary
     *       search is used.
     *  <LI> if there is an LCP table, but no LCP-LR table, the
     *       minimum LCP value between two offsets into the suffix array
     *       will be computed, but if the difference between the offsets
     *       is larger than the LCP value, the two suffixes are compared
     *       directly.
     *  <LI> if there is no LCP table and no LCP-LR table, the two suffixes
     *       are compared directly.  I.e., the suffixes are scanned until
     *       a mismatch is found or the end of a suffix is reached.
     * </UL>
     * To configure the suffix array so that the LCP-LR table is used,
     * call {@link #useLCPLR()}.  To configure the suffix array so that
     * the LCP table may be used (e.g., when the LCP-LR table exists),
     * call {@link #useLCP()}.
     * @param index1 the index into the suffix array for the first suffix
     * @param index2 the index into the suffix array for the second suffix
     * @return the length of the LCP for these two suffixes
     * @see #useLCP()
     * @see #useLCPLR()
     */
    public int lcpLength(int index1, int index2) {
	if (index1 == index2) {
	    return (array[0] - array[index1]);
	}
	// int i1 = rank[index1];
	// int i2 = rank[index2];
	int i1 = index1;
	int i2 = index2;
	// if (i1 == i2) return (array[0] - i1);

	if (index1 > index2) {
	    int tmp = index1;
	    index1 = index2;
	    index2 = tmp;
	}

	if (LCP_L != null) {
	    return lengthFromLCPLR(index1, index2);
	}
	if (lcpArray == null) {
	    return  commonPrefixLength(array[index1], array[index2]);
	}
	int sep = index2 - index1;
	int limit = lcpArray[index2];
	if (limit <= 0) return 0;
	if (sep > limit) {
	    // faster to do a direct comparison.
	    return commonPrefixLength(array[index1], array[index2]);
	} else {
	    int limitIndex = index2;
	    while ((--index2) > index1) {
		int nl = lcpArray[index2];
		if (nl < limit) {
		    limit = nl;
		}
		if (--sep > limit) {
		    // faster to do a direct comparison. This branch
		    // will be executed only if limit was set to nl,
		    // in which case index2 is the correct length
		    // to use.
		    return Math.min(limit,
				    commonPrefixLength(array[index1],
						       array[index2]));
		}
	    }
	    return limit;
	}
    }

    /**
     * Get the suffix array.
     * The array returned will contain the index for each suffix as it
     * would appear in lexical order in which
     * shortest suffixes appear first when the matching elements are
     * identical.  The first entry is for an empty string and contains
     * the length of the sequence array.
     * <P>
     * The array returned must not be modified.
     * @return the suffix array
     */
    public int[] getArray() {
	return array;
    }


    /**
     * Iterator for a suffix array.
     * This iterator provides a sequence of indices into a sequence
     * and a corresponding length, thus allowing one to iterate
     * through a series of subsequences.
     */
    public abstract static  class Iterator
	implements java.util.Iterator<java.lang.Integer>
    {
	/**
	 * The value, after being updated by a call to doNext(), that
	 * the method {@link #next()} will return.  The index is expected
	 * to be an index into a sequence array (not the suffix array).
	 */
	protected int currentIndex = 0;
	/**
	 * The current length of a suffix or subsequence that starts at
	 * the current index into the sequence array.
	 */
	protected int currentLength = 0;

	/**
	 * True if there are more entries and false otherwise.
	 * A constructor and calls to {@link #doNext()} are  expected to
	 * set this value.
	 */
	protected boolean hasMore = false;

	/**
	 *  Get a new entry.
	 *  This method must update three protected fields as appropriate:
	 *  currentIndex, currentLength, and hasMore. Subclasses will typically
	 *  provide additional fields to track the state of the iteration.
	 *  This method is called by next().
	 */
	protected abstract void doNext();

	/**
	 * Return the next element in the iteration.
	 * @return the next element in the iteration
	 */
	@Override
	public final java.lang.Integer next() {
	    if (hasMore) {
		doNext();
		return currentIndex;
	    } else {
		throw new NoSuchElementException();
	    }
	}

	/**
	 * Return the length of the current suffix or subsequence.
	 * @return the length of the current suffix or subsequence;
	 *         0 if {@link java.util.Iterator#next()} has not been called
	 */
	public int getLength() {
	    return currentLength;
	}

	/**
	 * Test if the iteration has a next element
	 * @return true if the iteration has more elements; false otherwise
	 */
	@Override
	public boolean hasNext() {return hasMore;}

	/**
	 * Remove an element.
	 * This method is required by the {@link java.util.Iterator}
	 * interface, and is not implemented. An
	 * {@link java.lang.UnsupportedOperationException} will be
	 * thrown if this method, which is an optional operation, is called.
	 * @exception UnsupportedOperationException this operation is not
	 *            supported.
	 */
	@Override
	public void remove() throws UnsupportedOperationException {
	    throw new UnsupportedOperationException();
	}

	// so it is not public.
	Iterator() {}
    }

    private class SubSequenceIterator extends Iterator {
	int saIndex = 0;
	int tail;
	int max = array[0];
	int lastSA = array.length-1;
	int[] lcp = getLCP();
	SubSequenceIterator() {
	    super();
	    currentIndex = array[0];
	    tail = max;
	    hasMore = (currentIndex > 0);
	}
	protected void doNext() {
	    if (tail == max) {
		int oldIndex = saIndex;
		saIndex++;
		currentIndex = array[saIndex];
		currentLength = lcp[saIndex];
		tail = currentIndex + currentLength;
	    }
	    tail++;
	    currentLength++;
	    if (saIndex == lastSA && tail == max) {
		hasMore = false;
		lcp = null;
	    }
	}
    }

    /**
     * Get an iterator that will iterator that will provide a sequence
     * of indices and lengths for each subsequence of the sequence
     * corresponding to this suffix array.  Each subsequence is unique.
     * An empty subsequence is not included in the iteration.
     * <P>
     * The iterator's {@link SuffixArray.Iterator#next()} method will
     * return the index into the sequence for the start of a subsequence.
     * If the method {@link SuffixArray.Iterator#getLength()} is
     * called, the iterator will provide the length of the corresponding
     * subsequence. If {@link SuffixArray.Iterator#next()} has not been
     * called, the length is undefined, and
     * {@link SuffixArray.Iterator#getLength()} will return 0.
     * @return the iterator
     */
    public Iterator subsequences() {
	return new SubSequenceIterator() {
	};
    }

    /**
     * Count the number of unique subsequences.
     * <P>
     * Note: the implementation (unless overridden) will create
     * an LCP array, temporarily if the {@link #useLCP()} has not
     * been called.  If the LCP array will be need subsequently,
     * one should call {@link #useLCP()} before this method is
     * called to avoid duplicating the work needed to set up the
     * LCP array.
     * @return the number of subsequences.
     */
    public synchronized long countUniqueSubsequences() {
	int[] lcp = getLCP();
	long total = 0;
	int len = array[0];
	for (int i = 1; i < array.length; i++) {
	    total += len - lcp[i] - array[i];
	}
	lcp = null;
	return total;
    }

    private class RangeIterator extends Iterator {
	int start;
	int end;
	RangeIterator(int len, int start, int end) {
	    currentLength = len;
	    this.start = start;
	    this.end = end;
	    hasMore = (start < end);
	}
	protected void doNext() {
	    currentIndex = array[start];
	    start++;
	    hasMore = (start < end);
	}
    }

    /**
     * Interface representing a range in a suffix array resulting
     * from a search for all instances of a subsequence.
     */
    public interface Range extends Iterable<java.lang.Integer> {
	/**
	 * Get the number of sequences in this range.
	 * @return the number of sequences
	 */
	int size();

	/**
	 * Get the index into a sequence for a subsequence.
	 * The subsequences associated with a range are indexed
	 * starting at 0.  Each has a corresponding index into the
	 * full sequence.
	 * @param ind the subsequence index (non-negative and less
	 *        then the size of this range)
	 * @return the corresponding index into the sequence
	 * @see #size()
	 */
	int subsequenceIndex(int ind);

	/**
	 * Get the length of the subsequence.
	 * @return the subsequence length
	 */
	int subsequenceLength();

	/**
	 * Get an iterator that will return a sequence of indices
	 * into a sequence.
	 * <P>
	 * Note: the iterator is an instance of SuffixArray.Iterator
	 * and the subsequences are in lexical order of the corresponding
	 * suffixes.  This will in general not be the order in which
	 * the subsequences appear in the sequence itself. To put
	 * the subsequences in their order or appearance, use
	 * {@link #toArray()} and sort the array that is returned.
	 *
	 * @return the iterator.
	 */
	Iterator iterator();

	/**
	 * Create a sorted array containing the subsequence indices for this
	 * range.
	 * <P>
	 * This method is provided for convenience: the simplest implementation
	 * just calls {@link #toArray()} to obtain an array and passes
	 * that array to {@link java.util.Arrays#sort(int[])} before
	 * returning the array. For very long sequences where a subsequence
	 * occurs a large number of times, this method will allocate a
	 * significant amount of memory.
	 * <P>
	 * If the order of the sequence indices do not matter, or if
	 * they will be sorted later (e.g., when indices are obtained using
	 * two separate ranges), one should not use this method.
	 * @return the subsequence indices sorted in numerical order
	 *         (lowest first)
	 */
	int[]  sequenceOrder();

	/**
	 * Get an array of subsequence indices.
	 * Each element of the array will be an index into the sequence
	 * corresponding to this object's suffix array.
	 * @return the array of indices into a sequence
	 */
	int[] toArray();

	/**
	 * Get an array of subsequence indices using an existing array.
	 * Each element of the array will be an index into the sequence
	 * corresponding to this object's suffix array.
	 * <P>
	 * The argument array will be ignored if it is too short and a
	 * new array will be allocated.
	 * @param a an array into which the values should be stored
	 * @return the array of indices into a sequence
	 */
	int[] toArray(int[] a);

	/**
	 * Get an array of subsequence indices using an existing array and
	 * an offset into that array.
	 * Each element of the array will be an index into the sequence
	 * corresponding to this object's suffix array.
	 * An exception will be thrown if the array passed as the
	 * first argument is too short.
	 * <P>
	 * One use case occurs when searching for multiple substrings.
	 * This method can be used to put all of the substring indices
	 * into the same array, which can then be sorted so that the
	 * substrings referenced by the subsequence indices appear in
	 * the same order as in the sequence.
	 * @param a an array into which the values should be stored
	 * @param offset the starting index into the array
	 * @exception IllegalArgumentException the array is too short
	 */
	void toArray(int[] a, int offset) throws IllegalArgumentException;

    }

    /**
     * Class representing a range in a suffix array resulting
     * from a search for all instances of a subsequence.
     */
    public class OurRange implements Range {
	int seqlength;
	int length;
	int start;
	int end;
	OurRange(int seqlength, int start, int end) {
	    this.seqlength = seqlength;
	    this.start = start;
	    this.end = end;
	    length = end - start;
	}

	@Override
	public int size() {
	    return length;
	}

	@Override
	public int subsequenceIndex(int ind) {
	    if (ind < 0 || ind >= length) {
		throw new IllegalArgumentException();
	    }
	    return array[start + ind];
	}

	@Override
	public int subsequenceLength() {
	    return seqlength;
	}

	@Override
	public Iterator iterator() {
	    return new RangeIterator(length, start, end);
	}

	@Override
	public int[] sequenceOrder() {
	    int[] result = toArray();
	    Arrays.sort(result);
	    return result;
	}

	@Override
	public int[] toArray() {
	    int[] result = new int[length];
	    for (int i = 0; i < length; i++) {
		result[i] = array[start+i];
	    }
	    return result;
	}

	@Override
	public int[] toArray(int[] a) {
	    if (a.length < length) {
		return toArray();
	    }
	    for (int i = 0; i < length; i++) {
		a[i] = array[start+i];
	    }
	    return a;
	}

	@Override
	public void toArray(int[] a, int offset)
	    throws IllegalArgumentException
	{
	    if (a.length - offset < length) {
		throw new IllegalArgumentException
		    (errorMsg("arrayAndOffset", a.length, offset));
	    }
	    for (int i = 0; i < length; i++) {
		a[offset+i] = array[start+i];
	    }
	}
    }


    static final int result0[] = {0};
    static final int result1[] = {1, 0};


    /**
     * Class providing a suffix array for int-valued sequences
     */
    public static final class Integer extends SuffixArray {

	protected int[] sequence;
	int sequenceLength;

	private int findSubsequenceLCPLR(int[] sarray, int start, int end)
	{
	    if (start >= end) return -1;
	    int low = 1;
	    int high = array.length-1;
	    int s = start;
	    int indl = array[low];
	    int indh = array[high];
	    while (s < end-1
		   && indl < sequenceLength && indh < sequenceLength
		   && (sarray[s] == sequence[indl])
		   && (sarray[s] == sequence[indh])) {
		s++; indl++; indh++;
	    }
	    int s2 = s;
	    int s3 = s;
	    while (indh < sequenceLength  && s2 < end-1 &&
		   (sarray[s2] == sequence[indh])) {
		s2++; indh++;
	    }
	    if (indh == sequenceLength) return -1;
	    if (s2 < end && sarray[s2] > sequence[indh]) return -1;
	    if (s2 == end-1 && sarray[s2] == sequence[indh]) return high;
	    if (s2 < end-1 && indh == sequenceLength - 1
		&& sequence[indh] == sarray[s2]) {
		return -1;
	    }
	    while (indl < sequenceLength  && s3 < end-1 &&
		   (sarray[s3] == sequence[indl])) {
		s3++; indl++;
	    }
	    if (s3 < end && indl < sequenceLength &&
		sarray[s3] < sequence[indl]) {
		return -1;
	    }
	    if (s3 == end-1 && indl != sequenceLength
		&& sarray[s3] == sequence[indl]) {
		return low;
	    }
	    boolean right = (s2 <= s3);
	    if (right) {
		s = s3;
	    } else {
		s = s2;
	    }
	    int k = s - start;
	    int test = -2;	// Signal that test was not done.
	    while ((high - low) > 1) {
		int middle = (low + high) >>> 1;
		int ind = array[middle] + k;
		int val = (ind >= sequenceLength)? -1: sequence[ind];
		int key = sarray[s];
		if (right) {
		    if (k < LCP_L[middle]) {
			low = middle;
		    } else if (k > LCP_L[middle]) {
			high = middle;
			right = true;
		    } else {
			test = ((val == key)? 0: ((val < key)? -1: 1));
			if (test < 0) {
			    low = middle;
			} else if (test > 0) {
			    high = middle;
			    right = false;
			} else {
			    while (test == 0 && s < end-1) {
				k++; ind++; s++;
				if (ind >= sequenceLength) {
				    test = -1;
				    break;
				}
				val = sequence[ind];
				key = sarray[s];
				test = ((val == key)? 0: ((val < key)? -1: 1));
			    }
			    if (test < 0) {
				low = middle;
			    } else if (test > 0) {
				high = middle;
				right = false;
			    } else if (s == end-1) {
				return middle;
			    } else {
				throw new IllegalStateException();
			    }
			}
		    }
		} else {
		    if (k < LCP_R[middle]) {
			high = middle;
		    } else if (k > LCP_R[middle]) {
			low = middle;
			right = false;
		    } else {
			test = ((val == key)? 0: ((val < key)? -1: 1));
			if (test < 0) {
			    low = middle;
			    right = true;
			} else if (test > 0) {
			    high = middle;
			} else {
			    while (test == 0 && s < end-1) {
				k++; ind++; s++;
				if (ind == sequenceLength) {
				    test = -1;
				    break;
				}
				val = sequence[ind];
				key = sarray[s];
				test = ((val == key)? 0: ((val < key)? -1: 1));
			    }
			    if (test < 0) {
				low = middle;
				right = true;
			    } else if (test > 0) {
				high = middle;
			    } else if (s == end-1) {
				return middle;
			    } else {
				throw new IllegalStateException
				    ("search failed but test was zero");
			    }
			}
		    }
		}
	    }
	    if (test == -2) {
		test = 0;
		if (right) {
		    int ind = array[low];
		    for (int i = start; i < end; i++) {
			if (ind >= sequenceLength) {
			    test = -1;
			    break;
			}
			int val = sequence[ind++];
			test = (val < sarray[i])? -1: 1;
			if (test != 0) break;
		    }
		    if (test == 0) return low;
		} else {
		    int ind = array[high];
		    for (int i = start; i < end; i++) {
			if (ind >= sequenceLength) {
			    test = -1;
			    break;
			}
			int val = sequence[ind++];
			test = (val < sarray[i])? -1: 1;
			if (test != 0) break;
		    }
		    if (test == 0) return high;
		}
	    }

	    if (test < 0) {
		// test high
		int ind = array[high];
		for (int i = start; i < end; i++) {
		    if (ind >= sequenceLength) return -1;
		    int val = sequence[ind++];
		    if (val != sarray[i]) return -1;
		}
		return high;
	    } else if (test > 0) {
		// test low
		int ind = array[low];
		for (int i = start; i < end; i++) {
		    if (ind >= sequenceLength) return -1;
		    int val = sequence[ind++];
		    if (val != sarray[i]) return -1;
		}
		return low;
	    }
	    return -1;
	}

	class FindComparator implements IntComparator {
	    int start;
	    int end;
	    int limit;

	    int[] sarray;
	    FindComparator(int[] sarray, int start, int end) {
		this.sarray = sarray;
		this.start = start;
		this.limit = end - start;
	    }

	    public int compare(int o1, int o2) {
		for (int i = 0; i < limit; i++) {
		    if (o1 + i >= sequenceLength) return -1;
		    int val1 = sequence[o1 + i];
		    int val2 = sarray[i];
		    if (val1 < val2) return -1;
		    if (val1 > val2) return 1;
		}
		return 0;
	    }
	}

	/**
	 * Find the index into the sequence associated with a suffix
	 * array for an arbitrary instance of a subsequence.
	 * <P>
	 * Using an LCP-LR table (created by calling
	 * {@link SuffixArray#useLCPLR()}) will change with time
	 * complexity of this method from O(m log n) to O(m + log n),
	 * where m is the length of a subsequence and n is the length
	 * of the sequence array. These, however, are worst-case numbers:
	 * while it can take m steps for a comparison function to
	 * determine that two suffixes differ, the comparison will stop
	 * at the first step at which the suffixes actually differ:
	 * the difference in running time in practice is data-set
	 * dependent.
	 * @param subsequence the subsequence.
	 * @return the index into the sequence; -1 if the subsequence
	 *         cannot be found
	 * @see SuffixArray#getArray()
	 * @see #getSequence()
	 * @see #useLCPLR()
	 */
	public int findInstance(int[] subsequence) {
	    int ind = findSubsequence(subsequence);
	    if (ind == -1) return -1;
	    return array[ind];
	}

	/**
	 * Find the index into the sequence associated with a suffix
	 * array for an arbitrary instance of a subsequence given a
	 * starting and ending index into an array containing the
	 * subsequence.
	 * <P>
	 * Using an LCP-LR table (created by calling
	 * {@link SuffixArray#useLCPLR()}) will change with time
	 * complexity of this method from O(m log n) to O(m + log n),
	 * where m is the length of a subsequence and n is the length
	 * of the sequence array. These, however, are worst-case numbers:
	 * while it can take m steps for a comparison function to
	 * determine that two suffixes differ, the comparison will stop
	 * at the first step at which the suffixes actually differ:
	 * the difference in running time in practice is data-set
	 * dependent.
	 * @param subsequence  array containing the subsequence.
	 * @param start the starting index in the subsequence array (inclusive)
	 * @param end the ending index in the subsequence array (exclusive)
	 * @return the index into the sequence; -1 if the subsequence
	 *         cannot be found
	 * @see SuffixArray#getArray()
	 * @see #getSequence()
	 * @see #useLCPLR()
	 */
	public int findInstance(int[] subsequence, int start, int end) {
	    int ind = findSubsequence(subsequence, start, end);
	    if (ind == -1) return -1;
	    return array[ind];
	}

	/**
	 * Find the suffix-array index of an arbitrary instance of
	 * a subsequence.
	 * This method finds the index into a suffix array corresponding to
	 * a subsequence.
	 * @param subsequence the subsequence.
	 * @return the index into the suffix array; -1 if the subsequence
	 *         cannot be found
	 * @see SuffixArray#getArray()
	 */
	public int findSubsequence(int[] subsequence) {
	    return findSubsequence(subsequence, 0, subsequence.length);
	}

	/**
	 * Find the suffix-array index of an arbitrary instance of a
	 * subsequence given a starting index and ending index for the
	 * subsequence.
	 * This method finds the index into a suffix array corresponding to
	 * a subsequence. The subsequence consists of the elements of
	 * the array sarray with a starting index named start and and
	 * ending index named end.
	 * @param sarray the subsequence array.
	 * @param start the starting index in the subsequence array (inclusive)
	 * @param end the ending index in the subsequence array (exclusive)
	 * @return the index into the suffix array; -1 if the subsequence
	 *         cannot be found
	 * @see SuffixArray#getArray()
	 */
	public int findSubsequence(int[] sarray, int start,
				   int end)
	{
	    if (start >= end) return -1;
	    int first = 1;
	    int last = array.length;
	    if (LCP_L != null && LCP_R != null) {
		return findSubsequenceLCPLR(sarray, start, end);
	    } else {
		FindComparator c = new FindComparator(sarray, start, end);
		int result = PrimArrays.binarySearch(array, first, last, -1, c);
		return (result < 0)? -1: result;
	    }
	}

	/**
	 * Find a subsequence.
	 * This method finds the index into a suffix array corresponding to
	 * a subsequence.
	 * @param subsequence the subsequence.
	 * @param keyflag true if the highest index should be returned; false
	 *        if the lowest index should be returned.
	 * @return the index into the suffix array; -1 if the subsequence
	 *         cannot be found
	 * @see SuffixArray#getArray()
	 */
	public int findSubsequence(int[] subsequence, boolean keyflag) {
	    return findSubsequence(subsequence, 0, subsequence.length, keyflag);
	}

	/**
	 * Find a subsequence given a starting index and ending index for
	 * the subsequence.
	 * This method finds the index into a suffix array corresponding to
	 * a subsequence. The subsequence consists of the elements of
	 * the array sarray with a starting index named start and and
	 * ending index named end.
	 * @param sarray the subsequence array.
	 * @param start the starting index in the subsequence array (inclusive)
	 * @param end the ending index in the subsequence array (exclusive)
	 * @param keyflag true if the highest index should be returned; false
	 *        if the lowest index should be returned.
	 * @return the index into the suffix array; -1 if the subsequence
	 *         cannot be found
	 * @see SuffixArray#getArray()
	 */
	public int findSubsequence(int[] sarray, int start,
				   int end,
				   boolean keyflag)
	{
	    if (start >= end) return -1;
	    int first = 1;
	    int last = array.length;
	    FindComparator c = new FindComparator(sarray, start, end);
	    int i1 = PrimArrays.binarySearch(array, first, last, -1, c, false);
	    if (i1 < 0) return -1;
	    int i2 = PrimArrays.binarySearch(array, i1, last, -1, c, true);
	    if (i2 < 0) return -1;
	    return keyflag? i2: i1;
	}

	/**
	 * Find all instances of a subsequence.
	 * @param subsequence the subsequence
	 * @return the subsequences corresponding to a range in the
	 *         suffix array
	 * @see #getSequence()
	 */
	public Range findRange(int[] subsequence) {
	    return findRange(subsequence, 0, subsequence.length);
	}

	/**
	 * Find  all instances of a subsequence given a starting index
	 * and ending index.
	 * @param sarray the subsequence array.
	 * @param start the starting index in the subsequence array (inclusive)
	 * @param end the ending index in the subsequence array (exclusive)
	 * @return the subsequences corresponding to a range in the
	 *         suffix array
	 * @see #getSequence()
	 */
	public Range findRange(int[] sarray, int start,
				   int end)
	{
	    if (start >= end) return new OurRange(0, 0, 0);
	    int first = 1;
	    int last = array.length;
	    FindComparator c = new FindComparator(sarray, start, end);
	    int i1 = PrimArrays.binarySearch(array, first, last, -1,
					     c, false);
	    if (i1 < 0) return new OurRange(0, 0, 0);
	    int i2 = PrimArrays.binarySearch(array, i1, last, -1,
					     c, true);
	    if (i2 < 0) return new OurRange(0, 0, 0);
	    return new OurRange(end - start, i1, i2+1);
	}


	/**
	 * Get the sequence associated with this suffix array.
	 * The sequence is an array that must not be modified.
	 * @return the sequence that this suffix array describes
	 */
	public int[] getSequence() {return sequence;}

	/**
	 * Get the Burrows-Wheeler Transform  (BWT) of the sequence associated
	 * with this suffix array, with -1 indicating the end-of-text addition
	 * to the alphabet.
	 * The value returned includes the end-of-text symbol in the transform
	 * when the length of the array is one more than the length of the
	 * sequence associated with this suffix array.
	 * @param bwt the array to store the BWT (an array whose length is
	 *        the length of the sequence if the end-of-text symbol does
	 *        not appear in the BWT and one more than the length of the
	 *        sequence if the end-of-text symbol does appear in the
	 *        BWT)
	 * @return the index for the sorted permutation that matches the
	 *         sequence
	 */
	public int getBWT(int[] bwt) {
	    int result = 0;
	    boolean mode = (bwt.length == sequenceLength);
	    if (mode) {
		for (int i = 1; i < array.length; i++) {
		    int index = array[i];
		    if (index == 0) {
			result = i-1;
			bwt[i-1] = sequence[sequenceLength-1];
		    } else {
			bwt[i-1] = sequence[index-1];
		    }
		}
	    } else {
		for (int i = 0; i < array.length; i++) {
		    int index = array[i];
		    if (index == 0) {
			result = i;
			bwt[i] = -1;
		    } else {
			bwt[i] = sequence[index-1];
		    }
		}
	    }
	    return result;
	}

	/**
	 * Compute the inverse Burrows-Wheeler transform.
	 * When the length of of the BWT array is one more than the
	 * length of the result array, the BTW array is assumed to
	 * contain an end-of-text symbol (-1 for this case), and the
	 * index parameter is ignored. If the two arrays have the same
	 * length, all symbols in the BWT array must be in the alphabet
	 * and the index must be provided (it will be the value returned
	 * by a call to {@link #getBWT(int[])}).
	 * @param bwt the Burrows-Wheeler transform
	 * @param result the inverse of the Burrons-Wheeler transform
	 * @param index the index parameter for the Burrows-Wheeler transform
	 * @param n the size of the alphabet.
	 * @exception IllegalArgumentException bwt and result have inconsistent
	 *            lengths
	 */
	public static void inverseBWT(int[] bwt, int[] result,
				      int index, int n)
	    throws IllegalArgumentException
	{
	    if (bwt.length == result.length) {
		int[] K = new int[n];
		int[] C = new int[result.length];
		int[] M = new int[n];
		for (int i = 0; i < result.length; i++) {
		    C[i] = K[bwt[i]];
		    K[bwt[i]] = K[bwt[i]] + 1;
		}
		int sum = 0;
		for (int i = 0; i < n; i++) {
		    M[i] = sum;
		    sum = sum + K[i];
		}
		for (int j = result.length-1; j >= 0; j--) {
		    result[j] = bwt[index];
		    index = C[index] + M[bwt[index]];
		}
	    } else if (bwt.length == result.length + 1) {
		int[] K = new int[n];
		int[] C = new int[result.length];
		int[] M = new int[n];
		int off = 1;
		for (int i = 0; i < result.length; i++) {
		    int ii = i + 1;
		    if (bwt[ii] == -1) {
			C[i] = K[bwt[0]];
			K[bwt[0]] = K[bwt[0]] + 1;
			index = i;
		    } else {
			C[i] = K[bwt[ii]];
			K[bwt[ii]] = K[bwt[ii]] + 1;
		    }
		}
		int sum = 0;
		for (int i = 0; i < n; i++) {
		    M[i] = sum;
		    sum = sum + K[i];
		}
		for (int j = result.length-1; j >= 0; j--) {
		    int ii = index + 1;
		    if (bwt[ii] == -1) {
			result[j] = bwt[0];
			index = C[index] + M[bwt[0]];
		    } else {
			result[j] = bwt[ii];
			index = C[index] + M[bwt[ii]];
		    }
		}
	    } else {
		throw new IllegalArgumentException(errorMsg("BWTlength"));
	    }
	}

	@Override
	protected void fillLCPArray(int[] ourlcpArray, int[] rank) {
	    int k = 0;
	    int n = sequenceLength;
	    for (int i = 0; i < n; i++) {
		if (rank[i] == n) {
		    k = 0;
		    continue;
		}
		int j = array[rank[i]+1];
		while (i+k < n && j+k < n &&
		       sequence[i+k] == sequence[j+k]) {
		    k++;
		}
		ourlcpArray[rank[i]+1] = k;
		if (k > 0) k--;
	    }
	}

	@Override
	protected int commonPrefixLength(int index1, int index2) {
	    int sum = 0;
	    while (index1 < sequenceLength && index2 < sequenceLength
		   && sequence[index1++] == sequence[index2++]) {
		sum++;
	    }
	    return sum;
	}

	private int[] makeSuffixArray() {
	    IntComparator ic = new IntComparator() {
		    public int compare(int index1, int index2) {
			int limit = sequenceLength - index1;
			int olimit = sequenceLength -index2;
			int xlimit = limit;
			if (limit > olimit) xlimit = olimit;
			for (int i = 0; i < xlimit; i++) {
			    if (sequence[index1+i] < sequence[index2+i])
				return -1;
			    if (sequence[index1+i] > sequence[index2+i])
				return 1;
			}
			if (limit < olimit) return -1;
			if (limit > olimit) return 1;
			return 0;
		    }
		};
	    int slenp1 = sequenceLength + 1;
	    int[] suffixArray = new int[slenp1];
	    for (int i = 0; i < slenp1; i++) {
		suffixArray[i] = i;
	    }
	    PrimArrays.sort(suffixArray, ic);
	    return suffixArray;
	}

	private IntComparator ic = new IntComparator() {
		public int compare(int index1, int index2) {
		    int limit = sequenceLength - index1;
		    int olimit = sequenceLength -index2;
		    int xlimit = limit;
		    if (limit > olimit) xlimit = olimit;
		    for (int i = 0; i < xlimit; i++) {
			if (sequence[index1+i] < sequence[index2+i]) return -1;
			if (sequence[index1+i] > sequence[index2+i]) return 1;
		    }
		    if (limit < olimit) return -1;
		    if (limit > olimit) return 1;
		    return 0;
		}
	    };

	private int[] guess(int[] bucketSizes, boolean[] inS, int[] tails) {
	    int[] guessed = new int[slenp1];
	    Arrays.fill(guessed, -1);
	    for (int i = 0; i < sequenceLength; i++) {
		if (i == 0 || !inS[i] || inS[i-1]) continue;
		int element = sequence[i];
		guessed[tails[element]] = i;
		tails[element] -= 1;
	    }
	    guessed[0] = sequenceLength;
	    return guessed;
	}

	// note: the array heads will be modified. The array guessed is used
	// for both input and output.
	private void induceSortL(int[] guessed, int[] bucketSizes,
				 int[] heads, boolean[] inS)
	{
	    for (int i = 0; i < guessed.length; i++) {
		if (guessed[i] == -1) continue;
		int j = guessed[i]-1;
		if (j < 0) continue;
		if (inS[j]) continue;
		int element = sequence[j];
		guessed[heads[element]] = j;
		heads[element]++;
	    }
	}

	// note: the array tails will be modified. The array guessed is used
	// for both input and output.
	private void induceSortS(int[] guessed, int[] bucketSizes,
				 int[] tails, boolean[] inS)
	{
	    for (int i = guessed.length-1; i > -1; i--) {
		int j = guessed[i] -1;
		if (j < 0) continue;
		if (!inS[j]) continue;
		int element = sequence[j];
		guessed[tails[element]] = j;
		tails[element]--;
	    }
	}

	static class SummaryResults {
	    int[] summarySequence;
	    int summaryAlphabetSize;
	    int[] summaryOffsets;
	    SummaryResults(int[] seq, int size, int[] offsets) {
		summarySequence = seq;
		summaryAlphabetSize = size;
		summaryOffsets = offsets;
	    }
	}

	private SummaryResults summarize(int[] guessed, boolean[] inS) {
	    int[] lmsNames = new int[slenp1];
	    Arrays.fill(lmsNames, -1);
	    int current = 0;
	    lmsNames[guessed[0]] = current;
	    int last = guessed[0];
	    for (int i = 1; i < guessed.length; i++) {
		int element = guessed[i];
		if (element == 0 || !inS[element] || inS[element-1]) continue;
		int index1 = last;
		int index2 = element;
		if (index1 == sequenceLength || index2 == sequenceLength) {
		    current++;
		    last = element;
		    lmsNames[element] = current;
		    continue;
		}
		boolean lms1 = (index1 == 0)? false:
		    ((inS[index1] && !inS[index1-1])? true:false);
		boolean lms2 = (index2 == 0)? false:
		    ((inS[index2] && !inS[index2-1])? true:false);
		if (lms1 != lms2) {
		    current++;
		    last = element;
		    lmsNames[element] = current;
		    continue;
		}
		if (sequence[index1] != sequence[index2]) {
		    current++;
		    last = element;
		    lmsNames[element] = current;
		    continue;
		}
		index1++;
		index2++;
		while (true) {
		    lms1 = (index1 == 0)? false:
			((inS[index1] && !inS[index1-1])? true:false);
		    lms2 = (index2 == 0)? false:
			((inS[index2] && !inS[index2-1])? true:false);
		    if (lms1 && lms2) {
			break;
		    }
		    if (lms1 != lms2) {
			current++;
			break;
		    }
		    if (sequence[index1] != sequence[index2]) {
			current++;
			break;
		    }
		    index1++;
		    index2++;
		}
		last = element;
		lmsNames[element] = current;
	    }
	    int limit = 0;
	    for (int i = 0; i < slenp1; i++) {
		if (lmsNames[i] == -1) continue;
		limit++;
	    }
	    int[] summarySequence = new int[limit];
	    int[] summaryOffsets = new int[limit];
	    int j = 0;

	    for (int i = 0; i < slenp1; i++) {
		if (lmsNames[i] == -1) continue;
		summaryOffsets[j] = i;
		summarySequence[j] = lmsNames[i];
		j++;
	    }
	    lmsNames = null;
	    int summaryAlphabetSize = current + 1;
	    return new SummaryResults(summarySequence, summaryAlphabetSize,
				      summaryOffsets);
	}

	private int[] makeSummarySuffixes(int[] summarySequence,
					  int summaryAlphabetSize)
	{
	    if (summaryAlphabetSize == summarySequence.length) {
		int[] result = new int[summarySequence.length + 1];
		Arrays.fill(result, -1);
		result[0] = summarySequence.length;
		for (int i = 0; i < summarySequence.length; i++) {
		    int element = summarySequence[i];
		    result[element+1] = i;
		}
		return result;
	    } else {
		SuffixArray result = new
		    SuffixArray.Integer(summarySequence, summaryAlphabetSize);
		return result.array;
	    }
	}

	private int[] actualLMSSort(int[] bucketSizes, boolean[] inS,
				    int[] tails,
				    int[] summarySuffixes,
				    int[] summaryOffsets)
	{
	    array = new int[slenp1];
	    Arrays.fill(array, -1);
	    for (int i = summarySuffixes.length-1; i > 1; i--) {
		int sIndex = summaryOffsets[summarySuffixes[i]];
		int bIndex = sequence[sIndex];

		array[tails[bIndex]] = sIndex;
		tails[bIndex]--;
	    }
	    array[0] = sequenceLength;
	    return array;
	}

	/**
	 * Constructor.
	 * The maximum reasonable value of n should be no higher than the
	 * length of the sequence and frequently much lower.
	 * The sequence must not be changed after this constructor is
	 * called as long as this suffix array is used.
	 * @param sequence a sequence of integers whose values are in the
	 *        range [0,n)
	 * @param n the size of an alphabet encoded as values in [0,n)
	 *        where n must be a positive integer
	 * @exception IllegalArgumentException n is out of range.
	 */
	public Integer(int[] sequence, int n) {
	    if (n <= 0) {
		throw new IllegalArgumentException(errorMsg("argsOutOfRange"));
	    }
	    this.sequence = sequence;
	    sequenceLength = sequence.length;
	    slenp1 = sequence.length+1;
	    if (sequenceLength < 2) {
		try {
		    if (sequenceLength == 0) {
			array = Cloner.makeClone(SuffixArray.result0);
		    } else if (sequenceLength == 1) {
			array = Cloner.makeClone(SuffixArray.result1);
		    }
		} catch (CloneNotSupportedException e) {
		    throw new UnexpectedExceptionError(e);
		}
		return;
	    } else if (sequenceLength < 22) {
		array = makeSuffixArray();
		return;
	    }

	    boolean[] inS = new boolean[slenp1];
	    inS[sequenceLength] = true;
	    if (sequenceLength > 0) {
		for (int i = sequenceLength-2; i > -1; i--) {
		    if (sequence[i] == sequence[i+1] && inS[i+1]) {
			inS[i] = true;
		    } else if (sequence[i] < sequence[i+1]) {
			inS[i] = true;
		    }
		}
	    }
	    int[] bucketSizes = new int[n];
	    for (int i = 0; i < sequenceLength; i++) {
		int element = sequence[i];
		if (element < 0 || element >= n) {
		    throw new
			IllegalArgumentException(errorMsg("notInAlphabet", i));
		}
		bucketSizes[element]++;
	    }
	    int[] heads = new int[n];
	    int[] tails = new int[n];
	    int ind = 1;
	    for (int i = 0; i < n; i++) {
		heads[i] = ind;
		int size = bucketSizes[i];
		ind += size;
		tails[i] = ind-1;
	    }

	    try {
		int[] guessed = guess(bucketSizes, inS,
				      Cloner.makeClone(tails));
		induceSortL(guessed, bucketSizes, Cloner.makeClone(heads), inS);
		induceSortS(guessed, bucketSizes, Cloner.makeClone(tails), inS);
		SummaryResults summary = summarize(guessed, inS);
		guessed = null;
		int[] summarySuffixes =
		    makeSummarySuffixes(summary.summarySequence,
					summary.summaryAlphabetSize);

		array = actualLMSSort(bucketSizes, inS, Cloner.makeClone(tails),
				      summarySuffixes, summary.summaryOffsets);
		induceSortL(array, bucketSizes, heads, inS);
		induceSortS(array, bucketSizes, tails, inS);
	    } catch (CloneNotSupportedException e) {
		throw new UnexpectedExceptionError(e);
	    }
	}

	/**
	 * Constructor for precomputed suffix arrays.
	 * The maximum reasonable value of n should be no higher than the
	 * length of the sequence and frequently much lower.
	 * The sequence must not be changed after this constructor is
	 * called as long as this suffix array is used.
	 * <P>
	 * The suffix array passed as the third argument must have one
	 * more element than the sequence.
	 * <P>
	 * This constructor is intended for the case where a suffix
	 * array was previously computed and saved in a file. The caller
	 * must assure that the suffix array was computed for the
	 * sequence and that the alphabets are the same.
	 * @param sequence a sequence of integers whose values are in the
	 *        range [0,n)
	 * @param sarray the suffix array
	 * @exception IllegalArgumentException n is out of range or
	 *            the suffix array is not compatible with the sequence.
	 */
	public Integer(int[] sequence, int[] sarray)
	    throws IllegalArgumentException
	{
	    if (sarray.length != sequence.length + 1) {
		throw new IllegalArgumentException("seqArrayNotCompatible");
	    }
	    this.sequence = sequence;
	    this.array = sarray;
	    sequenceLength = sequence.length;
	    slenp1 = sequence.length+1;
	}
    }

    /**
     * Class providing a suffix array for short-valued sequences
     */
    public static final class Short extends SuffixArray {

	short[] sequence;
	int sequenceLength;

	/**
	 * Get the sequence associated with this suffix array.
	 * The sequence is an array that must not be modified.
	 * @return the sequence that this suffix array describes
	 */
	public short[] getSequence() {return sequence;}

	private int findSubsequenceLCPLR(short[] sarray, int start, int end)
	{
	    if (start >= end) return -1;
	    int low = 1;
	    int high = array.length-1;
	    int s = start;
	    int indl = array[low];
	    int indh = array[high];
	    while (s < end-1
		   && indl < sequenceLength && indh < sequenceLength
		   && (sarray[s] == sequence[indl])
		   && (sarray[s] == sequence[indh])) {
		s++; indl++; indh++;
	    }
	    int s2 = s;
	    int s3 = s;
	    while (indh < sequenceLength  && s2 < end-1 &&
		   (sarray[s2] == sequence[indh])) {
		s2++; indh++;
	    }
	    if (indh == sequenceLength) return -1;
	    if (s2 < end && sarray[s2] > sequence[indh]) return -1;
	    if (s2 == end-1 && sarray[s2] == sequence[indh]) return high;
	    if (s2 < end-1 && indh == sequenceLength - 1
		&& sequence[indh] == sarray[s2]) {
		return -1;
	    }
	    while (indl < sequenceLength  && s3 < end-1 &&
		   (sarray[s3] == sequence[indl])) {
		s3++; indl++;
	    }
	    if (s3 < end && indl < sequenceLength &&
		sarray[s3] < sequence[indl]) {
		return -1;
	    }
	    if (s3 == end-1 && indl != sequenceLength
		&& sarray[s3] == sequence[indl]) {
		return low;
	    }
	    boolean right = (s2 <= s3);
	    if (right) {
		s = s3;
	    } else {
		s = s2;
	    }
	    int k = s - start;
	    int test = -2;	// Signal that test was not done.
	    while ((high - low) > 1) {
		int middle = (low + high) >>> 1;
		int ind = array[middle] + k;
		short val = (ind >= sequenceLength)? -1: sequence[ind];
		short key = sarray[s];
		if (right) {
		    if (k < LCP_L[middle]) {
			low = middle;
		    } else if (k > LCP_L[middle]) {
			high = middle;
			right = true;
		    } else {
			test = ((val == key)? 0: ((val < key)? -1: 1));
			if (test < 0) {
			    low = middle;
			} else if (test > 0) {
			    high = middle;
			    right = false;
			} else {
			    while (test == 0 && s < end-1) {
				k++; ind++; s++;
				if (ind >= sequenceLength) {
				    test = -1;
				    break;
				}
				val = sequence[ind];
				key = sarray[s];
				test = ((val == key)? 0: ((val < key)? -1: 1));
			    }
			    if (test < 0) {
				low = middle;
			    } else if (test > 0) {
				high = middle;
				right = false;
			    } else if (s == end-1) {
				return middle;
			    } else {
				throw new IllegalStateException();
			    }
			}
		    }
		} else {
		    if (k < LCP_R[middle]) {
			high = middle;
		    } else if (k > LCP_R[middle]) {
			low = middle;
			right = false;
		    } else {
			test = ((val == key)? 0: ((val < key)? -1: 1));
			if (test < 0) {
			    low = middle;
			    right = true;
			} else if (test > 0) {
			    high = middle;
			} else {
			    while (test == 0 && s < end-1) {
				k++; ind++; s++;
				if (ind == sequenceLength) {
				    test = -1;
				    break;
				}
				val = sequence[ind];
				key = sarray[s];
				test = ((val == key)? 0: ((val < key)? -1: 1));
			    }
			    if (test < 0) {
				low = middle;
				right = true;
			    } else if (test > 0) {
				high = middle;
			    } else if (s == end-1) {
				return middle;
			    } else {
				throw new IllegalStateException
				    ("search failed but test was zero");
			    }
			}
		    }
		}
	    }
	    if (test == -2) {
		test = 0;
		if (right) {
		    int ind = array[low];
		    for (int i = start; i < end; i++) {
			if (ind >= sequenceLength) {
			    test = -1;
			    break;
			}
			short val = sequence[ind++];
			test = (val < sarray[i])? -1: 1;
			if (test != 0) break;
		    }
		    if (test == 0) return low;
		} else {
		    int ind = array[high];
		    for (int i = start; i < end; i++) {
			if (ind >= sequenceLength) {
			    test = -1;
			    break;
			}
			short val = sequence[ind++];
			test = (val < sarray[i])? -1: 1;
			if (test != 0) break;
		    }
		    if (test == 0) return high;
		}
	    }

	    if (test < 0) {
		// test high
		int ind = array[high];
		for (int i = start; i < end; i++) {
		    if (ind >= sequenceLength) return -1;
		    short val = sequence[ind++];
		    if (val != sarray[i]) return -1;
		}
		return high;
	    } else if (test > 0) {
		// test low
		int ind = array[low];
		for (int i = start; i < end; i++) {
		    if (ind >= sequenceLength) return -1;
		    short val = sequence[ind++];
		    if (val != sarray[i]) return -1;
		}
		return low;
	    }
	    return -1;
	}

	class FindComparator implements IntComparator {
	    int start;
	    int end;
	    int limit;

	    short[] sarray;
	    FindComparator(short[] sarray, int start, int end) {
		this.sarray = sarray;
		this.start = start;
		this.limit = end - start;
	    }

	    public int compare(int o1, int o2) {
		for (int i = 0; i < limit; i++) {
		    if (o1 + i >= sequenceLength) return -1;
		    short val1 = sequence[o1 + i];
		    short val2 = sarray[i];
		    if (val1 < val2) return -1;
		    if (val1 > val2) return 1;
		}
		return 0;
	    }
	}

	/**
	 * Find the index into the sequence associated with a suffix
	 * array for an arbitrary instance of a subsequence.
	 * <P>
	 * Using an LCP-LR table (created by calling
	 * {@link SuffixArray#useLCPLR()}) will change with time
	 * complexity of this method from O(m log n) to O(m + log n),
	 * where m is the length of a subsequence and n is the length
	 * of the sequence array. These, however, are worst-case numbers:
	 * while it can take m steps for a comparison function to
	 * determine that two suffixes differ, the comparison will stop
	 * at the first step at which the suffixes actually differ:
	 * the difference in running time in practice is data-set
	 * dependent.
	 * @param subsequence the subsequence.
	 * @return the index into the sequence; -1 if the subsequence
	 *         cannot be found
	 * @see SuffixArray#getArray()
	 * @see #getSequence()
	 * @see #useLCPLR()
	 */
	public int findInstance(short[] subsequence) {
	    int ind = findSubsequence(subsequence);
	    if (ind == -1) return -1;
	    return array[ind];
	}

	/**
	 * Find the index into the sequence associated with a suffix
	 * array for an arbitrary instance of a subsequence given a
	 * starting and ending index into an array containing the
	 * subsequence.
	 * <P>
	 * Using an LCP-LR table (created by calling
	 * {@link SuffixArray#useLCPLR()}) will change with time
	 * complexity of this method from O(m log n) to O(m + log n),
	 * where m is the length of a subsequence and n is the length
	 * of the sequence array. These, however, are worst-case numbers:
	 * while it can take m steps for a comparison function to
	 * determine that two suffixes differ, the comparison will stop
	 * at the first step at which the suffixes actually differ:
	 * the difference in running time in practice is data-set
	 * dependent.
	 * @param subsequence  array containing the subsequence.
	 * @param start the starting index in the subsequence array (inclusive)
	 * @param end the ending index in the subsequence array (exclusive)
	 * @return the index into the sequence; -1 if the subsequence
	 *         cannot be found
	 * @see SuffixArray#getArray()
	 * @see #getSequence()
	 * @see #useLCPLR()
	 */
	public int findInstance(short[] subsequence, int start, int end) {
	    int ind = findSubsequence(subsequence, start, end);
	    if (ind == -1) return -1;
	    return array[ind];
	}

	/**
	 * Find the suffix-array index of an arbitrary instance of
	 * a subsequence.
	 * This method finds the index into a suffix array corresponding to
	 * a subsequence.
	 * @param subsequence the subsequence.
	 * @return the index into the suffix array; -1 if the subsequence
	 *         cannot be found
	 * @see SuffixArray#getArray()
	 */
	public int findSubsequence(short[] subsequence) {
	    return findSubsequence(subsequence, 0, subsequence.length);
	}


	/**
	 * Find the suffix-array index of an arbitrary instance of a
	 * subsequence given a starting index and ending index for the
	 * subsequence.
	 * This method finds the index into a suffix array corresponding to
	 * a subsequence. The subsequence consists of the elements of
	 * the array sarray with a starting index named start and and
	 * ending index named end.
	 * @param sarray the subsequence array.
	 * @param start the starting index in the subsequence array (inclusive)
	 * @param end the ending index in the subsequence array (exclusive)
	 * @return the index into the suffix array; -1 if the subsequence
	 *         cannot be found
	 * @see SuffixArray#getArray()
	 */
	public int findSubsequence(short[] sarray, int start,
				   int end)
	{
	    if (start >= end) return -1;
	    int first = 1;
	    int last = array.length;
	    if (LCP_L != null && LCP_R != null) {
		return findSubsequenceLCPLR(sarray, start, end);
	    } else {
		FindComparator c = new FindComparator(sarray, start, end);
		int result = PrimArrays.binarySearch(array, first, last, 0, c);
		return (result < 0)? -1: result;
	    }
	}

	/**
	 * Find a subsequence.
	 * This method finds the index into a suffix array corresponding to
	 * a subsequence.
	 * @param subsequence the subsequence.
	 * @param keyflag true if the highest index should be returned; false
	 *        if the lowest index should be returned.
	 * @return the index into the suffix array; -1 if the subsequence
	 *         cannot be found
	 * @see SuffixArray#getArray()
	 */
	public int findSubsequence(short[] subsequence, boolean keyflag) {
	    return findSubsequence(subsequence, 0, subsequence.length, keyflag);
	}

	/**
	 * Find a subsequence given a starting index and ending index for
	 * the subsequence.
	 * This method finds the index into a suffix array corresponding to
	 * a subsequence. The subsequence consists of the elements of
	 * the array sarray with a starting index named start and and
	 * ending index named end.
	 * @param subsequence the subsequence array.
	 * @param start the starting index in the subsequence array (inclusive)
	 * @param end the ending index in the subsequence array (exclusive)
	 * @param keyflag true if the highest index should be returned; false
	 *        if the lowest index should be returned.
	 * @return the index into the suffix array; -1 if the subsequence
	 *         cannot be found
	 * @see SuffixArray#getArray()
	 */
	public int findSubsequence(short[] subsequence, int start,
				   int end,
				   boolean keyflag)
	{
	    if (start >= end) return -1;
	    int first = 1;
	    int last = array.length;
	    FindComparator c = new FindComparator(subsequence, start, end);
	    int i1 = PrimArrays.binarySearch(array, first, last, -1,
						     c, false);
	    if (i1 < 0) return -1;
	    int i2 = PrimArrays.binarySearch(array, i1, last, -1,
					     c, true);
	    if (i2 < 0) return -1;
	    return keyflag? i2: i1;
	}

	/**
	 * Find all instances of a subsequence.
	 * @param subsequence the subsequence
	 * @return the subsequences corresponding to a range in the
	 *         suffix array
	 * @see #getSequence()
	 */
	public Range findRange(short[] subsequence) {
	    return findRange(subsequence, 0, subsequence.length);
	}

	/**
	 * Find  all instances of a subsequence given a starting index
	 * and ending index.
	 * @param sarray the subsequence array.
	 * @param start the starting index in the subsequence array (inclusive)
	 * @param end the ending index in the subsequence array (exclusive)
	 * @return the subsequences corresponding to a range in the
	 *         suffix array
	 * @see #getSequence()
	 */
	public Range findRange(short[] sarray, int start, int end)
	{
	    if (start > end) return new OurRange(0, 0, 0);;
	    int first = 1;
	    int last = array.length;
	    FindComparator c = new FindComparator(sarray, start, end);
	    int i1 = PrimArrays.binarySearch(array, first, last, -1,
					     c, false);
	    if (i1 < 0) return new OurRange(0, 0, 0);
	    int i2 = PrimArrays.binarySearch(array, i1, last, -1,
					     c, true);
	    if (i2 < 0) return new OurRange(0, 0, 0);
	    return new OurRange(end - start, i1, i2+1);
	}

	/**
	 * Get the Burrows-Wheeler Transform  (BWT) of the sequence associated
	 * with this suffix array, with -1 indicating the end-of-text addition
	 * to the alphabet.
	 * The value returned includes the end-of-text symbol in the transform
	 * when the length of the array is one more than the length of the
	 * sequence associated with this suffix array.
	 * @param bwt the array to store the BWT (an array whose length is
	 *        the length of the sequence if the end-of-text symbol does
	 *        not appear in the BWT and one more than the length of the
	 *        sequence if the end-of-text symbol does appear in the
	 *        BWT)
	 * @return the index for the sorted permutation that matches the
	 *         sequence
	 */
	public int getBWT(short[] bwt) {
	    int result = 0;
	    boolean mode = (bwt.length == sequenceLength);
	    if (mode) {
		for (int i = 1; i < array.length; i++) {
		    int index = array[i];
		    if (index == 0) {
			result = i-1;
			bwt[i-1] = sequence[sequenceLength-1];
		    } else {
			bwt[i-1] = sequence[index-1];
		    }
		}
	    } else {
		for (int i = 0; i < array.length; i++) {
		    int index = array[i];
		    if (index == 0) {
			result = i;
			bwt[i] = -1;
		    } else {
			bwt[i] = sequence[index-1];
		    }
		}
	    }
	    return result;
	}

	/**
	 * Compute the inverse Burrows-Wheeler transform.
	 * When the length of of the BWT array is one more than the
	 * length of the result array, the BTW array is assumed to
	 * contain an end-of-text symbol (-1 for this case), and the
	 * index parameter is ignored. If the two arrays have the same
	 * length, all symbols in the BWT array must be in the alphabet
	 * and the index must be provided (it will be the value returned
	 * by a call to {@link #getBWT(short[])}).
	 * @param bwt the Burrows-Wheeler transform
	 * @param result the inverse of the Burrons-Wheeler transform
	 * @param index the index parameter for the Burrows-Wheeler transform
	 * @param n the size of the alphabet.
	 * @exception IllegalArgumentException bwt and result have inconsistent
	 *            lengths
	 */
	public static void inverseBWT(short[] bwt, short[] result,
				      int index, int n)
	    throws IllegalArgumentException
	{
	    if (bwt.length == result.length) {
		int[] K = new int[n];
		int[] C = new int[result.length];
		int[] M = new int[n];
		for (int i = 0; i < result.length; i++) {
		    C[i] = K[bwt[i]];
		    K[bwt[i]] = K[bwt[i]] + 1;
		}
		int sum = 0;
		for (int i = 0; i < n; i++) {
		    M[i] = sum;
		    sum = sum + K[i];
		}
		for (int j = result.length-1; j >= 0; j--) {
		    result[j] = bwt[index];
		    index = C[index] + M[bwt[index]];
		}
	    } else if (bwt.length == result.length + 1) {
		int[] K = new int[n];
		int[] C = new int[result.length];
		int[] M = new int[n];
		int off = 1;
		for (int i = 0; i < result.length; i++) {
		    int ii = i + 1;
		    if (bwt[ii] == -1) {
			C[i] = K[bwt[0]];
			K[bwt[0]] = K[bwt[0]] + 1;
			index = i;
		    } else {
			C[i] = K[bwt[ii]];
			K[bwt[ii]] = K[bwt[ii]] + 1;
		    }
		}
		int sum = 0;
		for (int i = 0; i < n; i++) {
		    M[i] = sum;
		    sum = sum + K[i];
		}
		for (int j = result.length-1; j >= 0; j--) {
		    int ii = index + 1;
		    if (bwt[ii] == -1) {
			result[j] = bwt[0];
			index = C[index] + M[bwt[0]];
		    } else {
			result[j] = bwt[ii];
			index = C[index] + M[bwt[ii]];
		    }
		}
	    } else {
		throw new IllegalArgumentException(errorMsg("BWTlength"));
	    }
	}

	@Override
	protected void fillLCPArray(int[] ourlcpArray, int[] rank) {
	    int k = 0;
	    int n = sequenceLength;
	    for (int i = 0; i < n; i++) {
		if (rank[i] == n) {
		    k = 0;
		    continue;
		}
		int j = array[rank[i]+1];
		while (i+k < n && j+k < n &&
		       sequence[i+k] == sequence[j+k]) {
		    k++;
		}
		ourlcpArray[rank[i]+1] = k;
		if (k > 0) k--;
	    }
	}

	@Override
	protected int commonPrefixLength(int index1, int index2) {
	    int sum = 0;
	    while (index1 < sequenceLength && index2 < sequenceLength
		   && sequence[index1++] == sequence[index2++]) {
		sum++;
	    }
	    return sum;
	}

	private int[] makeSuffixArray() {
	    IntComparator ic = new IntComparator() {
		    public int compare(int index1, int index2) {
			int limit = sequenceLength - index1;
			int olimit = sequenceLength -index2;
			int xlimit = limit;
			if (limit > olimit) xlimit = olimit;
			for (int i = 0; i < xlimit; i++) {
			    if (sequence[index1+i] < sequence[index2+i])
				return -1;
			    if (sequence[index1+i] > sequence[index2+i])
				return 1;
			}
			if (limit < olimit) return -1;
			if (limit > olimit) return 1;
			return 0;
		    }
		};
	    int slenp1 = sequenceLength + 1;
	    int[] suffixArray = new int[slenp1];
	    for (int i = 0; i < slenp1; i++) {
		suffixArray[i] = i;
	    }
	    PrimArrays.sort(suffixArray, ic);
	    return suffixArray;
	}

	private IntComparator ic = new IntComparator() {
		public int compare(int index1, int index2) {
		    int limit = sequenceLength - index1;
		    int olimit = sequenceLength -index2;
		    int xlimit = limit;
		    if (limit > olimit) xlimit = olimit;
		    for (int i = 0; i < xlimit; i++) {
			if (sequence[index1+i] < sequence[index2+i]) return -1;
			if (sequence[index1+i] > sequence[index2+i]) return 1;
		    }
		    if (limit < olimit) return -1;
		    if (limit > olimit) return 1;
		    return 0;
		}
	    };

	private int[] guess(int[] bucketSizes, boolean[] inS, int[] tails) {
	    int[] guessed = new int[slenp1];
	    Arrays.fill(guessed, -1);
	    for (int i = 0; i < sequenceLength; i++) {
		if (i == 0 || !inS[i] || inS[i-1]) continue;
		int element = sequence[i];
		guessed[tails[element]] = i;
		tails[element] -= 1;
	    }
	    guessed[0] = sequenceLength;
	    return guessed;
	}

	// note: the array heads will be modified. The array guessed is used
	// for both input and output.
	private void induceSortL(int[] guessed, int[] bucketSizes,
				 int[] heads, boolean[] inS)
	{
	    for (int i = 0; i < guessed.length; i++) {
		if (guessed[i] == -1) continue;
		int j = guessed[i]-1;
		if (j < 0) continue;
		if (inS[j]) continue;
		int element = sequence[j];
		guessed[heads[element]] = j;
		heads[element]++;
	    }
	}

	// note: the array tails will be modified. The array guessed is used
	// for both input and output.
	private void induceSortS(int[] guessed, int[] bucketSizes,
				 int[] tails, boolean[] inS)
	{
	    for (int i = guessed.length-1; i > -1; i--) {
		int j = guessed[i] -1;
		if (j < 0) continue;
		if (!inS[j]) continue;
		int element = sequence[j];
		guessed[tails[element]] = j;
		tails[element]--;
	    }
	}

	static class SummaryResults {
	    int[] summarySequence;
	    int summaryAlphabetSize;
	    int[] summaryOffsets;
	    SummaryResults(int[] seq, int size, int[] offsets) {
		summarySequence = seq;
		summaryAlphabetSize = size;
		summaryOffsets = offsets;
	    }
	}

	private SummaryResults summarize(int[] guessed, boolean[] inS) {
	    int[] lmsNames = new int[slenp1];
	    Arrays.fill(lmsNames, -1);
	    int current = 0;
	    lmsNames[guessed[0]] = current;
	    int last = guessed[0];
	    for (int i = 1; i < guessed.length; i++) {
		int element = guessed[i];
		if (element == 0 || !inS[element] || inS[element-1]) continue;
		int index1 = last;
		int index2 = element;
		if (index1 == sequenceLength || index2 == sequenceLength) {
		    current++;
		    last = element;
		    lmsNames[element] = current;
		    continue;
		}
		boolean lms1 = (index1 == 0)? false:
		    ((inS[index1] && !inS[index1-1])? true:false);
		boolean lms2 = (index2 == 0)? false:
		    ((inS[index2] && !inS[index2-1])? true:false);
		if (lms1 != lms2) {
		    current++;
		    last = element;
		    lmsNames[element] = current;
		    continue;
		}
		if (sequence[index1] != sequence[index2]) {
		    current++;
		    last = element;
		    lmsNames[element] = current;
		    continue;
		}
		index1++;
		index2++;
		while (true) {
		    lms1 = (index1 == 0)? false:
			((inS[index1] && !inS[index1-1])? true:false);
		    lms2 = (index2 == 0)? false:
			((inS[index2] && !inS[index2-1])? true:false);
		    if (lms1 && lms2) {
			break;
		    }
		    if (lms1 != lms2) {
			current++;
			break;
		    }
		    if (sequence[index1] != sequence[index2]) {
			current++;
			break;
		    }
		    index1++;
		    index2++;
		}
		last = element;
		lmsNames[element] = current;
	    }
	    int limit = 0;
	    for (int i = 0; i < slenp1; i++) {
		if (lmsNames[i] == -1) continue;
		limit++;
	    }
	    int[] summarySequence = new int[limit];
	    int[] summaryOffsets = new int[limit];
	    int j = 0;

	    for (int i = 0; i < slenp1; i++) {
		if (lmsNames[i] == -1) continue;
		summaryOffsets[j] = i;
		summarySequence[j] = lmsNames[i];
		j++;
	    }
	    lmsNames = null;
	    int summaryAlphabetSize = current + 1;
	    return new SummaryResults(summarySequence, summaryAlphabetSize,
				      summaryOffsets);
	}

	private int[] makeSummarySuffixes(int[] summarySequence,
					  int summaryAlphabetSize)
	{
	    if (summaryAlphabetSize == summarySequence.length) {
		int[] result = new int[summarySequence.length + 1];
		Arrays.fill(result, -1);
		result[0] = summarySequence.length;
		for (int i = 0; i < summarySequence.length; i++) {
		    int element = summarySequence[i];
		    result[element+1] = i;
		}
		return result;
	    } else {
		SuffixArray result = new
		    SuffixArray.Integer(summarySequence, summaryAlphabetSize);
		return result.array;
	    }
	}

	private int[] actualLMSSort(int[] bucketSizes, boolean[] inS,
				    int[] tails,
				    int[] summarySuffixes,
				    int[] summaryOffsets)
	{
	    array = new int[slenp1];
	    Arrays.fill(array, -1);
	    for (int i = summarySuffixes.length-1; i > 1; i--) {
		int sIndex = summaryOffsets[summarySuffixes[i]];
		int bIndex = sequence[sIndex];

		array[tails[bIndex]] = sIndex;
		tails[bIndex]--;
	    }
	    array[0] = sequenceLength;
	    return array;
	}

	/**
	 * Constructor.
	 * The maximum reasonable value of n should be no higher than the
	 * length of the sequence and frequently much lower.
	 * The sequence must not be changed after this constructor is
	 * called as long as this suffix array is used.
	 * @param sequence a sequence of integers whose values are in the
	 *        range [0,n)
	 * @param n the size of an alphabet encoded as values in [0,n)
	 *        where n is positive and no larger than 2<sup>15</sup>
	 * @exception IllegalArgumentException n is out of range.
	 */
	public Short(short[] sequence, int n) {
	    if (n <= 0 || n > 1 + java.lang.Short.MAX_VALUE) {
		throw new IllegalArgumentException(errorMsg("argsOutOfRange"));
	    }
	    this.sequence = sequence;
	    sequenceLength = sequence.length;
	    slenp1 = sequenceLength+1;
	    if (sequenceLength < 2) {
		try {
		    if (sequenceLength == 0) {
			array = Cloner.makeClone(SuffixArray.result0);
		    } else if (sequenceLength == 1) {
			array = Cloner.makeClone(SuffixArray.result1);
		    }
		} catch (CloneNotSupportedException e) {
		    throw new UnexpectedExceptionError(e);
		}
		return;
	    } else if (sequenceLength < 22) {
		array = makeSuffixArray();
		return;
	    }

	    boolean[] inS = new boolean[slenp1];
	    inS[sequenceLength] = true;
	    if (sequenceLength > 0) {
		for (int i = sequenceLength-2; i > -1; i--) {
		    if (sequence[i] == sequence[i+1] && inS[i+1]) {
			inS[i] = true;
		    } else if (sequence[i] < sequence[i+1]) {
			inS[i] = true;
		    }
		}
	    }
	    int[] bucketSizes = new int[n];
	    for (int i = 0; i < sequenceLength; i++) {
		int element = sequence[i];
		if (element < 0 || element >= n) {
		    throw new
			IllegalArgumentException(errorMsg("notInAlphabet", i));
		}
		bucketSizes[element]++;
	    }
	    int[] heads = new int[n];
	    int[] tails = new int[n];
	    int ind = 1;
	    for (int i = 0; i < n; i++) {
		heads[i] = ind;
		int size = bucketSizes[i];
		ind += size;
		tails[i] = ind-1;
	    }

	    try {
		int[] guessed = guess(bucketSizes, inS,
				      Cloner.makeClone(tails));
		induceSortL(guessed, bucketSizes, Cloner.makeClone(heads), inS);
		induceSortS(guessed, bucketSizes, Cloner.makeClone(tails), inS);
		SummaryResults summary = summarize(guessed, inS);
		guessed = null;
		int[] summarySuffixes =
		    makeSummarySuffixes(summary.summarySequence,
					summary.summaryAlphabetSize);

		array = actualLMSSort(bucketSizes, inS, Cloner.makeClone(tails),
				      summarySuffixes, summary.summaryOffsets);
		induceSortL(array, bucketSizes, heads, inS);
		induceSortS(array, bucketSizes, tails, inS);
	    } catch (CloneNotSupportedException e) {
		throw new UnexpectedExceptionError(e);
	    }
	}
	/**
	 * Constructor for precomputed suffix arrays.
	 * The maximum reasonable value of n should be no higher than the
	 * length of the sequence and frequently much lower.
	 * The sequence must not be changed after this constructor is
	 * called as long as this suffix array is used.
	 * <P>
	 * The suffix array passed as the third argument must have one
	 * more element than the sequence.
	 * <P>
	 * This constructor is intended for the case where a suffix
	 * array was previously computed and saved in a file. The caller
	 * must assure that the suffix array was computed for the
	 * sequence and that the alphabets are the same.
	 * @param sequence a sequence of integers whose values are in the
	 *        range [0,n)
	 * @param sarray the suffix array
	 * @exception IllegalArgumentException n is out of range or
	 *            the suffix array is not compatible with the sequence.
	 */
	public Short(short[] sequence, int[] sarray)
	    throws IllegalArgumentException
	{
	    if (sarray.length != sequence.length + 1) {
		throw new IllegalArgumentException("seqArrayNotCompatible");
	    }
	    this.sequence = sequence;
	    this.array = sarray;
	    sequenceLength = sequence.length;
	    slenp1 = sequence.length+1;
	}
    }

    /**
     * Class providing a suffix array for short-valued sequences
     */
    public static final class UnsignedShort extends SuffixArray {

	short[] sequence;
	int sequenceLength;

	/**
	 * Get the sequence associated with this suffix array.
	 * The sequence is an array that must not be modified.
	 * @return the sequence that this suffix array describes
	 */
	public short[] getSequence() {return sequence;}

	private int findSubsequenceLCPLR(short[] sarray, int start, int end)
	{
	    if (start >= end) return -1;
	    int low = 1;
	    int high = array.length-1;
	    int s = start;
	    int indl = array[low];
	    int indh = array[high];
	    while (s < end-1
		   && indl < sequenceLength && indh < sequenceLength
		   && (sarray[s] == sequence[indl])
		   && (sarray[s] == sequence[indh])) {
		s++; indl++; indh++;
	    }
	    int s2 = s;
	    int s3 = s;
	    while (indh < sequenceLength  && s2 < end-1 &&
		   (sarray[s2] == sequence[indh])) {
		s2++; indh++;
	    }
	    if (indh == sequenceLength) return -1;
	    if (s2 < end && (0xFFFF &sarray[s2])
		> (0xFFFF & sequence[indh])) return -1;
	    if (s2 == end-1 && sarray[s2] == sequence[indh]) return high;
	    if (s2 < end-1 && indh == sequenceLength - 1
		&& sequence[indh] == sarray[s2]) {
		return -1;
	    }
	    while (indl < sequenceLength  && s3 < end-1 &&
		   (sarray[s3] == sequence[indl])) {
		s3++; indl++;
	    }
	    if (s3 < end && indl < sequenceLength &&
		(0xFFFF & sarray[s3]) < (0xFFFF & sequence[indl])) {
		return -1;
	    }
	    if (s3 == end-1 && indl != sequenceLength
		&& sarray[s3] == sequence[indl]) {
		return low;
	    }
	    boolean right = (s2 <= s3);
	    if (right) {
		s = s3;
	    } else {
		s = s2;
	    }
	    int k = s - start;
	    int test = -2;	// Signal that test was not done.
	    while ((high - low) > 1) {
		int middle = (low + high) >>> 1;
		int ind = array[middle] + k;
		int val = (ind >= sequenceLength)? -1:
		    (0xFFFF & sequence[ind]);
		int key = 0xFFFF & sarray[s];
		if (right) {
		    if (k < LCP_L[middle]) {
			low = middle;
		    } else if (k > LCP_L[middle]) {
			high = middle;
			right = true;
		    } else {
			test = ((val == key)? 0: ((val < key)? -1: 1));
			if (test < 0) {
			    low = middle;
			} else if (test > 0) {
			    high = middle;
			    right = false;
			} else {
			    while (test == 0 && s < end-1) {
				k++; ind++; s++;
				if (ind >= sequenceLength) {
				    test = -1;
				    break;
				}
				val = 0xFFFF & sequence[ind];
				key = 0xFFFF & sarray[s];
				test = ((val == key)? 0: ((val < key)? -1: 1));
			    }
			    if (test < 0) {
				low = middle;
			    } else if (test > 0) {
				high = middle;
				right = false;
			    } else if (s == end-1) {
				return middle;
			    } else {
				throw new IllegalStateException();
			    }
			}
		    }
		} else {
		    if (k < LCP_R[middle]) {
			high = middle;
		    } else if (k > LCP_R[middle]) {
			low = middle;
			right = false;
		    } else {
			test = ((val == key)? 0: ((val < key)? -1: 1));
			if (test < 0) {
			    low = middle;
			    right = true;
			} else if (test > 0) {
			    high = middle;
			} else {
			    while (test == 0 && s < end-1) {
				k++; ind++; s++;
				if (ind == sequenceLength) {
				    test = -1;
				    break;
				}
				val = 0xFFFF & sequence[ind];
				key = 0xFFFF & sarray[s];
				test = ((val == key)? 0: ((val < key)? -1: 1));
			    }
			    if (test < 0) {
				low = middle;
				right = true;
			    } else if (test > 0) {
				high = middle;
			    } else if (s == end-1) {
				return middle;
			    } else {
				throw new IllegalStateException
				    ("search failed but test was zero");
			    }
			}
		    }
		}
	    }
	    if (test == -2) {
		test = 0;
		if (right) {
		    int ind = array[low];
		    for (int i = start; i < end; i++) {
			if (ind >= sequenceLength) {
			    test = -1;
			    break;
			}
			int val = 0xFFFF & sequence[ind++];
			test = (val < (0xFFFF & array[i]))? -1: 1;
			if (test != 0) break;
		    }
		    if (test == 0) return low;
		} else {
		    int ind = array[high];
		    for (int i = start; i < end; i++) {
			if (ind >= sequenceLength) {
			    test = -1;
			    break;
			}
			int val = 0xFFFF & sequence[ind++];
			test = (val < (0xFFFF & sarray[i]))? -1: 1;
			if (test != 0) break;
		    }
		    if (test == 0) return high;
		}
	    }

	    if (test < 0) {
		// test high
		int ind = array[high];
		for (int i = start; i < end; i++) {
		    if (ind >= sequenceLength) return -1;
		    int val = sequence[ind++];
		    if (val != sarray[i]) return -1;
		}
		return high;
	    } else if (test > 0) {
		// test low
		int ind = array[low];
		for (int i = start; i < end; i++) {
		    if (ind >= sequenceLength) return -1;
		    int val = sequence[ind++];
		    if (val != sarray[i]) return -1;
		}
		return low;
	    }
	    return -1;
	}

	class FindComparator implements IntComparator {
	    int start;
	    int end;
	    int limit;

	    short[] sarray;
	    FindComparator(short[] sarray, int start, int end) {
		this.sarray = sarray;
		this.start = start;
		this.limit = end - start;
	    }

	    public int compare(int o1, int o2) {
		for (int i = 0; i < limit; i++) {
		    if (o1 + i >= sequenceLength) return -1;
		    int val1 =  0xFFFF & sequence[o1 + i];
		    int val2 = 0xFFFF & sarray[i];
		    if (val1 < val2) return -1;
		    if (val1 > val2) return 1;
		}
		return 0;
	    }
	}

	/**
	 * Find the index into the sequence associated with a suffix
	 * array for an arbitrary instance of a subsequence.
	 * <P>
	 * Using an LCP-LR table (created by calling
	 * {@link SuffixArray#useLCPLR()}) will change with time
	 * complexity of this method from O(m log n) to O(m + log n),
	 * where m is the length of a subsequence and n is the length
	 * of the sequence array. These, however, are worst-case numbers:
	 * while it can take m steps for a comparison function to
	 * determine that two suffixes differ, the comparison will stop
	 * at the first step at which the suffixes actually differ:
	 * the difference in running time in practice is data-set
	 * dependent.
	 * @param subsequence the subsequence.
	 * @return the index into the sequence; -1 if the subsequence
	 *         cannot be found
	 * @see SuffixArray#getArray()
	 * @see #getSequence()
	 * @see #useLCPLR()
	 */
	public int findInstance(short[] subsequence) {
	    int ind = findSubsequence(subsequence);
	    if (ind == -1) return -1;
	    return array[ind];
	}

	/**
	 * Find the index into the sequence associated with a suffix
	 * array for an arbitrary instance of a subsequence given a
	 * starting and ending index into an array containing the
	 * subsequence.
	 * <P>
	 * Using an LCP-LR table (created by calling
	 * {@link SuffixArray#useLCPLR()}) will change with time
	 * complexity of this method from O(m log n) to O(m + log n),
	 * where m is the length of a subsequence and n is the length
	 * of the sequence array. These, however, are worst-case numbers:
	 * while it can take m steps for a comparison function to
	 * determine that two suffixes differ, the comparison will stop
	 * at the first step at which the suffixes actually differ:
	 * the difference in running time in practice is data-set
	 * dependent.
	 * @param subsequence  array containing the subsequence.
	 * @param start the starting index in the subsequence array (inclusive)
	 * @param end the ending index in the subsequence array (exclusive)
	 * @return the index into the sequence; -1 if the subsequence
	 *         cannot be found
	 * @see SuffixArray#getArray()
	 * @see #getSequence()
	 * @see #useLCPLR()
	 */
	public int findInstance(short[] subsequence, int start, int end) {
	    int ind = findSubsequence(subsequence, start, end);
	    if (ind == -1) return -1;
	    return array[ind];
	}

	/**
	 * Find the suffix-array index of an arbitrary instance of
	 * a subsequence.
	 * This method finds the index into a suffix array corresponding to
	 * a subsequence.
	 * @param subsequence the subsequence.
	 * @return the index into the suffix array; -1 if the subsequence
	 *         cannot be found
	 * @see SuffixArray#getArray()
	 */
	public int findSubsequence(short[] subsequence) {
	    return findSubsequence(subsequence, 0, subsequence.length);
	}


	/**
	 * Find the suffix-array index of an arbitrary instance of a
	 * subsequence given a starting index and ending index for the
	 * subsequence.
	 * This method finds the index into a suffix array corresponding to
	 * a subsequence. The subsequence consists of the elements of
	 * the array sarray with a starting index named start and and
	 * ending index named end.
	 * @param sarray the subsequence array.
	 * @param start the starting index in the subsequence array (inclusive)
	 * @param end the ending index in the subsequence array (exclusive)
	 * @return the index into the suffix array; -1 if the subsequence
	 *         cannot be found
	 * @see SuffixArray#getArray()
	 */
	public int findSubsequence(short[] sarray, int start,
				   int end)
	{
	    if (start >= end) return -1;
	    int first = 1;
	    int last = array.length;
	    if (LCP_L != null && LCP_R != null) {
		return findSubsequenceLCPLR(sarray, start, end);
	    } else {
		FindComparator c = new FindComparator(sarray, start, end);
		int result = PrimArrays.binarySearch(array, first, last, 0, c);
		return (result < 0)? -1: result;
	    }
	}

	/**
	 * Find a subsequence.
	 * This method finds the index into a suffix array corresponding to
	 * a subsequence.
	 * @param subsequence the subsequence.
	 * @param keyflag true if the highest index should be returned; false
	 *        if the lowest index should be returned.
	 * @return the index into the suffix array; -1 if the subsequence
	 *         cannot be found
	 * @see SuffixArray#getArray()
	 */
	public int findSubsequence(short[] subsequence, boolean keyflag) {
	    return findSubsequence(subsequence, 0, subsequence.length, keyflag);
	}

	/**
	 * Find a subsequence given a starting index and ending index for
	 * the subsequence.
	 * This method finds the index into a suffix array corresponding to
	 * a subsequence. The subsequence consists of the elements of
	 * the array sarray with a starting index named start and and
	 * ending index named end.
	 * @param subsequence the subsequence array.
	 * @param start the starting index in the subsequence array (inclusive)
	 * @param end the ending index in the subsequence array (exclusive)
	 * @param keyflag true if the highest index should be returned; false
	 *        if the lowest index should be returned.
	 * @return the index into the suffix array; -1 if the subsequence
	 *         cannot be found
	 * @see SuffixArray#getArray()
	 */
	public int findSubsequence(short[] subsequence, int start,
				   int end,
				   boolean keyflag)
	{
	    if (start >= end) return -1;
	    int first = 1;
	    int last = array.length;
	    FindComparator c = new FindComparator(subsequence, start, end);
	    int i1 = PrimArrays.binarySearch(array, first, last, -1,
						     c, false);
	    if (i1 < 0) return -1;
	    int i2 = PrimArrays.binarySearch(array, i1, last, -1,
					     c, true);
	    if (i2 < 0) return -1;
	    return keyflag? i2: i1;
	}

	/**
	 * Find all instances of a subsequence.
	 * @param subsequence the subsequence
	 * @return the subsequences corresponding to a range in the
	 *         suffix array
	 * @see #getSequence()
	 */
	public Range findRange(short[] subsequence) {
	    return findRange(subsequence, 0, subsequence.length);
	}

	/**
	 * Find  all instances of a subsequence given a starting index
	 * and ending index.
	 * @param sarray the subsequence array.
	 * @param start the starting index in the subsequence array (inclusive)
	 * @param end the ending index in the subsequence array (exclusive)
	 * @return the subsequences corresponding to a range in the
	 *         suffix array
	 * @see #getSequence()
	 */
	public Range findRange(short[] sarray, int start, int end)
	{
	    if (start > end) return new OurRange(0, 0, 0);;
	    int first = 1;
	    int last = array.length;
	    FindComparator c = new FindComparator(sarray, start, end);
	    int i1 = PrimArrays.binarySearch(array, first, last, -1,
					     c, false);
	    if (i1 < 0) return new OurRange(0, 0, 0);
	    int i2 = PrimArrays.binarySearch(array, i1, last, -1,
					     c, true);
	    if (i2 < 0) return new OurRange(0, 0, 0);
	    return new OurRange(end - start, i1, i2+1);
	}

	/**
	 * Get the Burrows-Wheeler Transform  (BWT) of the sequence associated
	 * with this suffix array, with -1 indicating the end-of-text addition
	 * to the alphabet.
	 * The value returned includes the end-of-text symbol in the transform
	 * when the length of the array is one more than the length of the
	 * sequence associated with this suffix array.
	 * @param bwt the array to store the BWT (an array whose length is
	 *        the length of the sequence if the end-of-text symbol does
	 *        not appear in the BWT and one more than the length of the
	 *        sequence if the end-of-text symbol does appear in the
	 *        BWT)
	 * @return the index for the sorted permutation that matches the
	 *         sequence
	 */
	public int getBWT(short[] bwt) {
	    int result = 0;
	    boolean mode = (bwt.length == sequenceLength);
	    if (mode) {
		for (int i = 1; i < array.length; i++) {
		    int index = array[i];
		    if (index == 0) {
			result = i-1;
			bwt[i-1] = sequence[sequenceLength-1];
		    } else {
			bwt[i-1] = sequence[index-1];
		    }
		}
	    } else {
		for (int i = 0; i < array.length; i++) {
		    int index = array[i];
		    if (index == 0) {
			result = i;
			bwt[i] = -1;
		    } else {
			bwt[i] = sequence[index-1];
		    }
		}
	    }
	    return result;
	}

	/**
	 * Compute the inverse Burrows-Wheeler transform.
	 * When the length of of the BWT array is one more than the
	 * length of the result array, the BTW array is assumed to
	 * contain an end-of-text symbol (-1 for this case), and the
	 * index parameter is ignored. If the two arrays have the same
	 * length, all symbols in the BWT array must be in the alphabet
	 * and the index must be provided (it will be the value returned
	 * by a call to {@link #getBWT(short[])}).
	 * @param bwt the Burrows-Wheeler transform
	 * @param result the inverse of the Burrons-Wheeler transform
	 * @param index the index parameter for the Burrows-Wheeler transform
	 * @param n the size of the alphabet.
	 * @exception IllegalArgumentException bwt and result have inconsistent
	 *            lengths
	 */
	public static void inverseBWT(short[] bwt, short[] result,
				      int index, int n)
	    throws IllegalArgumentException
	{
	    if (bwt.length == result.length) {
		int[] K = new int[n];
		int[] C = new int[result.length];
		int[] M = new int[n];
		for (int i = 0; i < result.length; i++) {
		    C[i] = K[bwt[i]];
		    K[bwt[i]] = K[bwt[i]] + 1;
		}
		int sum = 0;
		for (int i = 0; i < n; i++) {
		    M[i] = sum;
		    sum = sum + K[i];
		}
		for (int j = result.length-1; j >= 0; j--) {
		    result[j] = bwt[index];
		    index = C[index] + M[bwt[index]];
		}
	    } else if (bwt.length == result.length + 1) {
		int[] K = new int[n];
		int[] C = new int[result.length];
		int[] M = new int[n];
		int off = 1;
		for (int i = 0; i < result.length; i++) {
		    int ii = i + 1;
		    if (bwt[ii] == -1) {
			C[i] = K[bwt[0]];
			K[bwt[0]] = K[bwt[0]] + 1;
			index = i;
		    } else {
			C[i] = K[bwt[ii]];
			K[bwt[ii]] = K[bwt[ii]] + 1;
		    }
		}
		int sum = 0;
		for (int i = 0; i < n; i++) {
		    M[i] = sum;
		    sum = sum + K[i];
		}
		for (int j = result.length-1; j >= 0; j--) {
		    int ii = index + 1;
		    if (bwt[ii] == -1) {
			result[j] = bwt[0];
			index = C[index] + M[bwt[0]];
		    } else {
			result[j] = bwt[ii];
			index = C[index] + M[bwt[ii]];
		    }
		}
	    } else {
		throw new IllegalArgumentException(errorMsg("BWTlength"));
	    }
	}

	@Override
	protected void fillLCPArray(int[] ourlcpArray, int[] rank) {
	    int k = 0;
	    int n = sequenceLength;
	    for (int i = 0; i < n; i++) {
		if (rank[i] == n) {
		    k = 0;
		    continue;
		}
		int j = array[rank[i]+1];
		while (i+k < n && j+k < n &&
		       sequence[i+k] == sequence[j+k]) {
		    k++;
		}
		ourlcpArray[rank[i]+1] = k;
		if (k > 0) k--;
	    }
	}

	@Override
	protected int commonPrefixLength(int index1, int index2) {
	    int sum = 0;
	    while (index1 < sequenceLength && index2 < sequenceLength
		   && sequence[index1++] == sequence[index2++]) {
		sum++;
	    }
	    return sum;
	}

	private int[] makeSuffixArray() {
	    IntComparator ic = new IntComparator() {
		    public int compare(int index1, int index2) {
			int limit = sequenceLength - index1;
			int olimit = sequenceLength -index2;
			int xlimit = limit;
			if (limit > olimit) xlimit = olimit;
			for (int i = 0; i < xlimit; i++) {
			    if ((0xFFFF & sequence[index1+i]) <
				(0xFFFF & sequence[index2+i]))
				return -1;
			    if ((0xFFFF & sequence[index1+i]) >
				(0xFFFF & sequence[index2+i]))
				return 1;
			}
			if (limit < olimit) return -1;
			if (limit > olimit) return 1;
			return 0;
		    }
		};
	    int slenp1 = sequenceLength + 1;
	    int[] suffixArray = new int[slenp1];
	    for (int i = 0; i < slenp1; i++) {
		suffixArray[i] = i;
	    }
	    PrimArrays.sort(suffixArray, ic);
	    return suffixArray;
	}

	private IntComparator ic = new IntComparator() {
		public int compare(int index1, int index2) {
		    int limit = sequenceLength - index1;
		    int olimit = sequenceLength -index2;
		    int xlimit = limit;
		    if (limit > olimit) xlimit = olimit;
		    for (int i = 0; i < xlimit; i++) {
			if ((0xFFFF & sequence[index1+i])
			    < (0xFFFF & sequence[index2+i])) return -1;
			if ((0xFFFF & sequence[index1+i])
			    > (0xFFFF & sequence[index2+i])) return 1;
		    }
		    if (limit < olimit) return -1;
		    if (limit > olimit) return 1;
		    return 0;
		}
	    };

	private int[] guess(int[] bucketSizes, boolean[] inS, int[] tails) {
	    int[] guessed = new int[slenp1];
	    Arrays.fill(guessed, -1);
	    for (int i = 0; i < sequenceLength; i++) {
		if (i == 0 || !inS[i] || inS[i-1]) continue;
		int element = 0xFFFF & sequence[i];
		guessed[tails[element]] = i;
		tails[element] -= 1;
	    }
	    guessed[0] = sequenceLength;
	    return guessed;
	}

	// note: the array heads will be modified. The array guessed is used
	// for both input and output.
	private void induceSortL(int[] guessed, int[] bucketSizes,
				 int[] heads, boolean[] inS)
	{
	    for (int i = 0; i < guessed.length; i++) {
		if (guessed[i] == -1) continue;
		int j = guessed[i]-1;
		if (j < 0) continue;
		if (inS[j]) continue;
		int element = 0xFFFF & sequence[j];
		guessed[heads[element]] = j;
		heads[element]++;
	    }
	}

	// note: the array tails will be modified. The array guessed is used
	// for both input and output.
	private void induceSortS(int[] guessed, int[] bucketSizes,
				 int[] tails, boolean[] inS)
	{
	    for (int i = guessed.length-1; i > -1; i--) {
		int j = guessed[i] -1;
		if (j < 0) continue;
		if (!inS[j]) continue;
		int element = 0xFFFF & sequence[j];
		guessed[tails[element]] = j;
		tails[element]--;
	    }
	}

	static class SummaryResults {
	    int[] summarySequence;
	    int summaryAlphabetSize;
	    int[] summaryOffsets;
	    SummaryResults(int[] seq, int size, int[] offsets) {
		summarySequence = seq;
		summaryAlphabetSize = size;
		summaryOffsets = offsets;
	    }
	}

	private SummaryResults summarize(int[] guessed, boolean[] inS) {
	    int[] lmsNames = new int[slenp1];
	    Arrays.fill(lmsNames, -1);
	    int current = 0;
	    lmsNames[guessed[0]] = current;
	    int last = guessed[0];
	    for (int i = 1; i < guessed.length; i++) {
		int element = guessed[i];
		if (element == 0 || !inS[element] || inS[element-1]) continue;
		int index1 = last;
		int index2 = element;
		if (index1 == sequenceLength || index2 == sequenceLength) {
		    current++;
		    last = element;
		    lmsNames[element] = current;
		    continue;
		}
		boolean lms1 = (index1 == 0)? false:
		    ((inS[index1] && !inS[index1-1])? true:false);
		boolean lms2 = (index2 == 0)? false:
		    ((inS[index2] && !inS[index2-1])? true:false);
		if (lms1 != lms2) {
		    current++;
		    last = element;
		    lmsNames[element] = current;
		    continue;
		}
		if (sequence[index1] != sequence[index2]) {
		    current++;
		    last = element;
		    lmsNames[element] = current;
		    continue;
		}
		index1++;
		index2++;
		while (true) {
		    lms1 = (index1 == 0)? false:
			((inS[index1] && !inS[index1-1])? true:false);
		    lms2 = (index2 == 0)? false:
			((inS[index2] && !inS[index2-1])? true:false);
		    if (lms1 && lms2) {
			break;
		    }
		    if (lms1 != lms2) {
			current++;
			break;
		    }
		    if (sequence[index1] != sequence[index2]) {
			current++;
			break;
		    }
		    index1++;
		    index2++;
		}
		last = element;
		lmsNames[element] = current;
	    }
	    int limit = 0;
	    for (int i = 0; i < slenp1; i++) {
		if (lmsNames[i] == -1) continue;
		limit++;
	    }
	    int[] summarySequence = new int[limit];
	    int[] summaryOffsets = new int[limit];
	    int j = 0;

	    for (int i = 0; i < slenp1; i++) {
		if (lmsNames[i] == -1) continue;
		summaryOffsets[j] = i;
		summarySequence[j] = lmsNames[i];
		j++;
	    }
	    lmsNames = null;
	    int summaryAlphabetSize = current + 1;
	    return new SummaryResults(summarySequence, summaryAlphabetSize,
				      summaryOffsets);
	}

	private int[] makeSummarySuffixes(int[] summarySequence,
					  int summaryAlphabetSize)
	{
	    if (summaryAlphabetSize == summarySequence.length) {
		int[] result = new int[summarySequence.length + 1];
		Arrays.fill(result, -1);
		result[0] = summarySequence.length;
		for (int i = 0; i < summarySequence.length; i++) {
		    int element = summarySequence[i];
		    result[element+1] = i;
		}
		return result;
	    } else {
		SuffixArray result = new
		    SuffixArray.Integer(summarySequence, summaryAlphabetSize);
		return result.array;
	    }
	}

	private int[] actualLMSSort(int[] bucketSizes, boolean[] inS,
				    int[] tails,
				    int[] summarySuffixes,
				    int[] summaryOffsets)
	{
	    array = new int[slenp1];
	    Arrays.fill(array, -1);
	    for (int i = summarySuffixes.length-1; i > 1; i--) {
		int sIndex = summaryOffsets[summarySuffixes[i]];
		int bIndex = 0xFFFF & sequence[sIndex];

		array[tails[bIndex]] = sIndex;
		tails[bIndex]--;
	    }
	    array[0] = sequenceLength;
	    return array;
	}

	/**
	 * Constructor.
	 * The maximum reasonable value of n should be no higher than the
	 * length of the sequence and frequently much lower.
	 * The sequence must not be changed after this constructor is
	 * called as long as this suffix array is used.
	 * @param sequence a sequence of integers whose values are in the
	 *        range [0,n)
	 * @param n the size of an alphabet encoded as values in [0,n)
	 *        where n is positive and no larger than 2<sup>16</sup>-1
	 * @exception IllegalArgumentException n is out of range.
	 */
	public UnsignedShort(short[] sequence, int n)
	    throws IllegalArgumentException
	{
	    if (n <= 0 || n > 0xFFFF) {
		throw new IllegalArgumentException(errorMsg("argsOutOfRange"));
	    }
	    this.sequence = sequence;
	    sequenceLength = sequence.length;
	    slenp1 = sequenceLength+1;
	    if (sequenceLength < 2) {
		try {
		    if (sequenceLength == 0) {
			array = Cloner.makeClone(SuffixArray.result0);
		    } else if (sequenceLength == 1) {
			array = Cloner.makeClone(SuffixArray.result1);
		    }
		} catch(CloneNotSupportedException e) {
		    throw new UnexpectedExceptionError(e);
		}
		return;
	    } else if (sequenceLength < 22) {
		array = makeSuffixArray();
		return;
	    }

	    boolean[] inS = new boolean[slenp1];
	    inS[sequenceLength] = true;
	    if (sequenceLength > 0) {
		for (int i = sequenceLength-2; i > -1; i--) {
		    if (sequence[i] == sequence[i+1] && inS[i+1]) {
			inS[i] = true;
		    } else if ((0xFFFF & sequence[i])
			       < (0xFFFF & sequence[i+1])) {
			inS[i] = true;
		    }
		}
	    }
	    int[] bucketSizes = new int[n];
	    for (int i = 0; i < sequenceLength; i++) {
		int element = 0xFFFF & sequence[i];
		if (element < 0 || element >= n) {
		    throw new
			IllegalArgumentException(errorMsg("notInAlphabet", i));
		}
		bucketSizes[element]++;
	    }
	    int[] heads = new int[n];
	    int[] tails = new int[n];
	    int ind = 1;
	    for (int i = 0; i < n; i++) {
		heads[i] = ind;
		int size = bucketSizes[i];
		ind += size;
		tails[i] = ind-1;
	    }

	    try {
		int[] guessed = guess(bucketSizes, inS,
				      Cloner.makeClone(tails));
		induceSortL(guessed, bucketSizes, Cloner.makeClone(heads), inS);
		induceSortS(guessed, bucketSizes, Cloner.makeClone(tails), inS);
		SummaryResults summary = summarize(guessed, inS);
		guessed = null;
		int[] summarySuffixes =
		    makeSummarySuffixes(summary.summarySequence,
					summary.summaryAlphabetSize);

		array = actualLMSSort(bucketSizes, inS, Cloner.makeClone(tails),
				      summarySuffixes, summary.summaryOffsets);
		induceSortL(array, bucketSizes, heads, inS);
		induceSortS(array, bucketSizes, tails, inS);
	    } catch (CloneNotSupportedException e) {
		throw new UnexpectedExceptionError(e);
	    }
	}
	/**
	 * Constructor for precomputed suffix arrays.
	 * The maximum reasonable value of n should be no higher than the
	 * length of the sequence and frequently much lower.
	 * The sequence must not be changed after this constructor is
	 * called as long as this suffix array is used.
	 * <P>
	 * The suffix array passed as the third argument must have one
	 * more element than the sequence.
	 * <P>
	 * This constructor is intended for the case where a suffix
	 * array was previously computed and saved in a file. The caller
	 * must assure that the suffix array was computed for the
	 * sequence and that the alphabets are the same.
	 * @param sequence a sequence of integers whose values are in the
	 *        range [0,n)
	 * @param sarray the suffix array
	 * @exception IllegalArgumentException n is out of range or
	 *            the suffix array is not compatible with the sequence.
	 */
	public UnsignedShort(short[] sequence, int[] sarray)
	    throws IllegalArgumentException
	{
	    if (sarray.length != sequence.length + 1) {
		throw new IllegalArgumentException("seqArrayNotCompatible");
	    }
	    this.sequence = sequence;
	    this.array = sarray;
	    sequenceLength = sequence.length;
	    slenp1 = sequence.length+1;
	}
    }

    /**
     * Class providing a suffix array for char-valued sequences
     */
    public static final class Char extends SuffixArray {

	char[] sequence;
	int sequenceLength;

	private int findSubsequenceLCPLR(char[] sarray, int start, int end)
	{
	    if (start >= end) return -1;
	    int low = 1;
	    int high = array.length-1;
	    int s = start;
	    int indl = array[low];
	    int indh = array[high];
	    while (s < end-1
		   && indl < sequenceLength && indh < sequenceLength
		   && (sarray[s] == sequence[indl])
		   && (sarray[s] == sequence[indh])) {
		s++; indl++; indh++;
	    }
	    int s2 = s;
	    int s3 = s;
	    while (indh < sequenceLength  && s2 < end-1 &&
		   (sarray[s2] == sequence[indh])) {
		s2++; indh++;
	    }
	    if (indh == sequenceLength) return -1;
	    if (s2 < end && sarray[s2] > sequence[indh]) return -1;
	    if (s2 == end-1 && sarray[s2] == sequence[indh]) return high;
	    if (s2 < end-1 && indh == sequenceLength - 1
		&& sequence[indh] == sarray[s2]) {
		return -1;
	    }
	    while (indl < sequenceLength  && s3 < end-1 &&
		   (sarray[s3] == sequence[indl])) {
		s3++; indl++;
	    }
	    if (s3 < end && indl < sequenceLength &&
		sarray[s3] < sequence[indl]) {
		return -1;
	    }
	    if (s3 == end-1 && indl != sequenceLength
		&& sarray[s3] == sequence[indl]) {
		return low;
	    }
	    boolean right = (s2 <= s3);
	    if (right) {
		s = s3;
	    } else {
		s = s2;
	    }
	    int k = s - start;
	    int test = -2;	// Signal that test was not done.
	    while ((high - low) > 1) {
		int middle = (low + high) >>> 1;
		int ind = array[middle] + k;
		char val = (ind >= sequenceLength)? 0xffff: sequence[ind];
		char key = sarray[s];
		if (right) {
		    if (k < LCP_L[middle]) {
			low = middle;
		    } else if (k > LCP_L[middle]) {
			high = middle;
			right = true;
		    } else {
			test = ((val == key)? 0:
				((val < key || val == -0xffff)? -1: 1));
			if (test < 0) {
			    low = middle;
			} else if (test > 0) {
			    high = middle;
			    right = false;
			} else {
			    while (test == 0 && s < end-1) {
				k++; ind++; s++;
				if (ind >= sequenceLength) {
				    test = -1;
				    break;
				}
				val = sequence[ind];
				key = sarray[s];
				test = ((val == key)? 0:
					((val < key || val == 0xffff)? -1: 1));
			    }
			    if (test < 0) {
				low = middle;
			    } else if (test > 0) {
				high = middle;
				right = false;
			    } else if (s == end-1) {
				return middle;
			    } else {
				throw new IllegalStateException();
			    }
			}
		    }
		} else {
		    if (k < LCP_R[middle]) {
			high = middle;
		    } else if (k > LCP_R[middle]) {
			low = middle;
			right = false;
		    } else {
			test = ((val == key)? 0:
				((val < key || val == 0xffff)? -1: 1));
			if (test < 0) {
			    low = middle;
			    right = true;
			} else if (test > 0) {
			    high = middle;
			} else {
			    while (test == 0 && s < end-1) {
				k++; ind++; s++;
				if (ind == sequenceLength) {
				    test = -1;
				    break;
				}
				val = sequence[ind];
				key = sarray[s];
				test = ((val == key)? 0:
					((val < key || val == 0xffff)? -1: 1));
			    }
			    if (test < 0) {
				low = middle;
				right = true;
			    } else if (test > 0) {
				high = middle;
			    } else if (s == end-1) {
				return middle;
			    } else {
				throw new IllegalStateException
				    ("search failed but test was zero");
			    }
			}
		    }
		}
	    }
	    if (test == -2) {
		test = 0;
		if (right) {
		    int ind = array[low];
		    for (int i = start; i < end; i++) {
			if (ind >= sequenceLength) {
			    test = -1;
			    break;
			}
			char val = sequence[ind++];
			test = (val < sarray[i] || val == 0xffff)? -1: 1;
			if (test != 0) break;
		    }
		    if (test == 0) return low;
		} else {
		    int ind = array[high];
		    for (int i = start; i < end; i++) {
			if (ind >= sequenceLength) {
			    test = -1;
			    break;
			}
			char val = sequence[ind++];
			test = (val < sarray[i] || val == 0xffff)? -1: 1;
			if (test != 0) break;
		    }
		    if (test == 0) return high;
		}
	    }

	    if (test < 0) {
		// test high
		int ind = array[high];
		for (int i = start; i < end; i++) {
		    if (ind >= sequenceLength) return -1;
		    char val = sequence[ind++];
		    if (val != sarray[i]) return -1;
		}
		return high;
	    } else if (test > 0) {
		// test low
		int ind = array[low];
		for (int i = start; i < end; i++) {
		    if (ind >= sequenceLength) return -1;
		    char val = sequence[ind++];
		    if (val != sarray[i]) return -1;
		}
		return low;
	    }
	    return -1;
	}

	/**
	 * Get the sequence associated with this suffix array.
	 * The sequence is an array that must not be modified.
	 * @return the sequence that this suffix array describes
	 */
	public char[] getSequence() {return sequence;}

	class FindComparator implements IntComparator {
	    int start;
	    int end;
	    int limit;

	    char[] sarray;
	    FindComparator(char[] sarray, int start, int end) {
		this.sarray = sarray;
		this.start = start;
		this.limit = end - start;
	    }

	    public int compare(int o1, int o2) {
		for (int i = 0; i < limit; i++) {
		    if (o1 + i >= sequenceLength) return -1;
		    char val1 = sequence[o1 + i];
		    char val2 = sarray[i];
		    if (val1 < val2) return -1;
		    if (val1 > val2) return 1;
		}
		return 0;
	    }
	}

	/**
	 * Find the index into the sequence associated with a suffix
	 * array for an arbitrary instance of a subsequence, with the
	 * subsequence specified by a string.
	 * <P>
	 * Using an LCP-LR table (created by calling
	 * {@link SuffixArray#useLCPLR()}) will change with time
	 * complexity of this method from O(m log n) to O(m + log n),
	 * where m is the length of a subsequence and n is the length
	 * of the sequence array. These, however, are worst-case numbers:
	 * while it can take m steps for a comparison function to
	 * determine that two suffixes differ, the comparison will stop
	 * at the first step at which the suffixes actually differ:
	 * the difference in running time in practice is data-set
	 * dependent.
	 * @param subsequence the subsequence.
	 * @return the index into the sequence; -1 if the subsequence
	 *         cannot be found
	 * @see SuffixArray#getArray()
	 * @see #getSequence()
	 * @see #useLCPLR()
	 */
	public int findInstance(java.lang.String subsequence) {
	    return findInstance(subsequence.toCharArray());
	}


	/**
	 * Find the index into the sequence associated with a suffix
	 * array for an arbitrary instance of a subsequence.
	 * <P>
	 * Using an LCP-LR table (created by calling
	 * {@link SuffixArray#useLCPLR()}) will change with time
	 * complexity of this method from O(m log n) to O(m + log n),
	 * where m is the length of a subsequence and n is the length
	 * of the sequence array. These, however, are worst-case numbers:
	 * while it can take m steps for a comparison function to
	 * determine that two suffixes differ, the comparison will stop
	 * at the first step at which the suffixes actually differ:
	 * the difference in running time in practice is data-set
	 * dependent.
	 * @param subsequence the subsequence.
	 * @return the index into the sequence; -1 if the subsequence
	 *         cannot be found
	 * @see SuffixArray#getArray()
	 * @see #getSequence()
	 * @see #useLCPLR()
	 */
	public int findInstance(char[] subsequence) {
	    int ind = findSubsequence(subsequence);
	    if (ind == -1) return -1;
	    return array[ind];
	}

	/**
	 * Find the index into the sequence associated with a suffix
	 * array for an arbitrary instance of a subsequence given a
	 * starting and ending index into an array containing the
	 * subsequence.
	 * <P>
	 * Using an LCP-LR table (created by calling
	 * {@link SuffixArray#useLCPLR()}) will change with time
	 * complexity of this method from O(m log n) to O(m + log n),
	 * where m is the length of a subsequence and n is the length
	 * of the sequence array. These, however, are worst-case numbers:
	 * while it can take m steps for a comparison function to
	 * determine that two suffixes differ, the comparison will stop
	 * at the first step at which the suffixes actually differ:
	 * the difference in running time in practice is data-set
	 * dependent.
	 * @param subsequence  array containing the subsequence.
	 * @param start the starting index in the subsequence array (inclusive)
	 * @param end the ending index in the subsequence array (exclusive)
	 * @return the index into the sequence; -1 if the subsequence
	 *         cannot be found
	 * @see SuffixArray#getArray()
	 * @see #getSequence()
	 * @see #useLCPLR()
	 */
	public int findInstance(char[] subsequence, int start, int end) {
	    int ind = findSubsequence(subsequence, start, end);
	    if (ind == -1) return -1;
	    return array[ind];
	}

	/**
	 * Find the suffix-array index of an arbitrary instance of
	 * a subsequence with the subsequence specified by a string.
	 * This method finds the index into a suffix array corresponding to
	 * a subsequence.
	 * @param subsequence the subsequence.
	 * @return the index into the suffix array; -1 if the subsequence
	 *         cannot be found
	 * @see SuffixArray#getArray()
	 */
	public int findSubsequence(java.lang.String subsequence) {
	    return findSubsequence(subsequence.toCharArray());
	}

	/**
	 * Find the suffix-array index of an arbitrary instance of
	 * a subsequence.
	 * This method finds the index into a suffix array corresponding to
	 * a subsequence.
	 * @param subsequence the subsequence.
	 * @return the index into the suffix array; -1 if the subsequence
	 *         cannot be found
	 * @see SuffixArray#getArray()
	 */
	public int findSubsequence(char[] subsequence) {
	    return findSubsequence(subsequence, 0, subsequence.length);
	}


	/**
	 * Find the suffix-array index of an arbitrary instance of a
	 * subsequence given a starting index and ending index for
	 * the subsequence.
	 * This method finds the index into a suffix array corresponding to
	 * a subsequence. The subsequence consists of the elements of
	 * the array sarray with a starting index named start and and
	 * ending index named end.
	 * @param sarray the subsequence array.
	 * @param start the starting index in the subsequence array (inclusive)
	 * @param end the ending index in the subsequence array (exclusive)
	 * @return the index into the suffix array; -1 if the subsequence
	 *         cannot be found
	 * @see SuffixArray#getArray()
	 */
	public int findSubsequence(char[] sarray, int start,
				   int end)
	{
	    if (start >= end) return -1;
	    int first = 1;
	    int last = array.length;
	    if (LCP_L != null && LCP_R != null) {
		return findSubsequenceLCPLR(sarray, start, end);
	    } else {
		FindComparator c = new FindComparator(sarray, start, end);
		int result = PrimArrays.binarySearch(array, first, last, 0, c);
		return (result < 0)? -1: result;
	    }
	}

	/**
	 * Find a subsequence that is specified by a string.
	 * This method finds the index into a suffix array corresponding to
	 * a subsequence.
	 * @param subsequence the subsequence.
	 * @param keyflag true if the highest index should be returned; false
	 *        if the lowest index should be returned.
	 * @return the index into the suffix array; -1 if the subsequence
	 *         cannot be found
	 * @see SuffixArray#getArray()
	 */
	public int findSubsequence(java.lang.String subsequence,
				   boolean keyflag) {
	    return findSubsequence(subsequence.toCharArray(), keyflag);
	}

	/**
	 * Find a subsequence.
	 * This method finds the index into a suffix array corresponding to
	 * a subsequence.
	 * @param subsequence the subsequence.
	 * @param keyflag true if the highest index should be returned; false
	 *        if the lowest index should be returned.
	 * @return the index into the suffix array; -1 if the subsequence
	 *         cannot be found
	 * @see SuffixArray#getArray()
	 */
	public int findSubsequence(char[] subsequence, boolean keyflag) {
	    return findSubsequence(subsequence, 0, subsequence.length, keyflag);
	}

	/**
	 * Find a subsequence given a starting index and ending index for
	 * the subsequence.
	 * This method finds the index into a suffix array corresponding to
	 * a subsequence. The subsequence consists of the elements of
	 * the array sarray with a starting index named start and and
	 * ending index named end.
	 * @param subsequence the subsequence array.
	 * @param start the starting index in the subsequence array (inclusive)
	 * @param end the ending index in the subsequence array (exclusive)
	 * @param keyflag true if the highest index should be returned; false
	 *        if the lowest index should be returned.
	 * @return the index into the suffix array; -1 if the subsequence
	 *         cannot be found
	 * @see SuffixArray#getArray()
	 */
	public int findSubsequence(char[] subsequence, int start,
				   int end,
				   boolean keyflag)
	{
	    if (start > end) return -1;
	    int first = 1;
	    int last = array.length;
	    FindComparator c = new FindComparator(subsequence, start, end);
	    int i1 = PrimArrays.binarySearch(array, first, last, -1,
						     c, false);
	    if (i1 < 0) return -1;
	    int i2 = PrimArrays.binarySearch(array, i1, last, -1,
					     c, true);
	    if (i2 < 0) return -1;
	    return keyflag? i2: i1;
	}

	/**
	 * Find all instances of a subsequence, with the subsequence
	 * specified by a string.
	 * @param subsequence the subsequence
	 * @return the subsequences corresponding to a range in the
	 *         suffix array
	 * @see #getSequence()
	 */
	public Range findRange(java.lang.String subsequence) {
	    return findRange(subsequence.toCharArray());
	}

	/**
	 * Find all instances of a subsequence.
	 * @param subsequence the subsequence
	 * @return the subsequences corresponding to a range in the
	 *         suffix array
	 * @see #getSequence()
	 */
	public Range findRange(char[] subsequence) {
	    return findRange(subsequence, 0, subsequence.length);
	}



	/**
	 * Find  all instances of a subsequence given a starting index
	 * and ending index.
	 * @param sarray the subsequence array.
	 * @param start the starting index in the subsequence array (inclusive)
	 * @param end the ending index in the subsequence array (exclusive)
	 * @return the subsequences corresponding to a range in the
	 *         suffix array
	 * @see #getSequence()
	 */
	public Range findRange(char[] sarray, int start,
				   int end)
	{
	    if (start >= end) return new OurRange(0, 0, 0);
	    int first = 1;
	    int last = array.length;
	    FindComparator c = new FindComparator(sarray, start, end);
	    int i1 = PrimArrays.binarySearch(array, first, last, -1,
					     c, false);
	    if (i1 < 0) return new OurRange(0, 0, 0);
	    int i2 = PrimArrays.binarySearch(array, i1, last, -1,
					     c, true);
	    if (i2 < 0) return new OurRange(0, 0, 0);
	    return new OurRange(end - start, i1, i2+1);
	}

	/**
	 * Get the Burrows-Wheeler Transform  (BWT) of the sequence associated
	 * with this suffix array, with -1 indicating the end-of-text addition
	 * to the alphabet.
	 * The value returned includes the end-of-text symbol in the transform
	 * when the length of the array is one more than the length of the
	 * sequence associated with this suffix array.
	 * @param bwt the array to store the BWT (an array whose length is
	 *        the length of the sequence if the end-of-text symbol does
	 *        not appear in the BWT and one more than the length of the
	 *        sequence if the end-of-text symbol does appear in the
	 *        BWT)
	 * @return the index for the sorted permutation that matches the
	 *         sequence
	 */
	public int getBWT(char[] bwt) {
	    int result = 0;
	    boolean mode = (bwt.length == sequenceLength);
	    if (mode) {
		for (int i = 1; i < array.length; i++) {
		    int index = array[i];
		    if (index == 0) {
			result = i-1;
			bwt[i-1] = sequence[sequenceLength-1];
		    } else {
			bwt[i-1] = sequence[index-1];
		    }
		}
	    } else {
		for (int i = 0; i < array.length; i++) {
		    int index = array[i];
		    if (index == 0) {
			result = i;
			bwt[i] = (char)0xffff;
		    } else {
			bwt[i] = sequence[index-1];
		    }
		}
	    }
	    return result;
	}

	/**
	 * Compute the inverse Burrows-Wheeler transform.
	 * When the length of of the BWT array is one more than the
	 * length of the result array, the BTW array is assumed to
	 * contain an end-of-text symbol (0xffff for this case), and the
	 * index parameter is ignored. If the two arrays have the same
	 * length, all symbols in the BWT array must be in the alphabet
	 * and the index must be provided (it will be the value returned
	 * by a call to {@link #getBWT(char[])}).
	 * @param bwt the Burrows-Wheeler transform
	 * @param result the inverse of the Burrons-Wheeler transform
	 * @param index the index parameter for the Burrows-Wheeler transform
	 * @param n the size of the alphabet.
	 * @exception IllegalArgumentException bwt and result have inconsistent
	 *            lengths
	 */
	public static void inverseBWT(char[] bwt, char[] result,
				      int index, int n)
	    throws IllegalArgumentException
	{
	    if (bwt.length == result.length) {
		int[] K = new int[n];
		int[] C = new int[result.length];
		int[] M = new int[n];
		for (int i = 0; i < result.length; i++) {
		    C[i] = K[bwt[i]];
		    K[bwt[i]] = K[bwt[i]] + 1;
		}
		int sum = 0;
		for (int i = 0; i < n; i++) {
		    M[i] = sum;
		    sum = sum + K[i];
		}
		for (int j = result.length-1; j >= 0; j--) {
		    result[j] = bwt[index];
		    index = C[index] + M[bwt[index]];
		}
	    } else if (bwt.length == result.length + 1) {
		int[] K = new int[n];
		int[] C = new int[result.length];
		int[] M = new int[n];
		int off = 1;
		for (int i = 0; i < result.length; i++) {
		    int ii = i + 1;
		    if (bwt[ii] == (char)0xffff) {
			C[i] = K[bwt[0]];
			K[bwt[0]] = K[bwt[0]] + 1;
			index = i;
		    } else {
			C[i] = K[bwt[ii]];
			K[bwt[ii]] = K[bwt[ii]] + 1;
		    }
		}
		int sum = 0;
		for (int i = 0; i < n; i++) {
		    M[i] = sum;
		    sum = sum + K[i];
		}
		for (int j = result.length-1; j >= 0; j--) {
		    int ii = index + 1;
		    if (bwt[ii] == (char)0xffff) {
			result[j] = bwt[0];
			index = C[index] + M[bwt[0]];
		    } else {
			result[j] = bwt[ii];
			index = C[index] + M[bwt[ii]];
		    }
		}
	    } else {
		throw new IllegalArgumentException(errorMsg("BWTlength"));
	    }
	}

	@Override
	protected void fillLCPArray(int[] ourlcpArray, int[] rank) {
	    int k = 0;
	    int n = sequenceLength;
	    for (int i = 0; i < n; i++) {
		if (rank[i] == n) {
		    k = 0;
		    continue;
		}
		int j = array[rank[i]+1];
		while (i+k < n && j+k < n &&
		       sequence[i+k] == sequence[j+k]) {
		    k++;
		}
		ourlcpArray[rank[i]+1] = k;
		if (k > 0) k--;
	    }
	}

	@Override
	protected int commonPrefixLength(int index1, int index2) {
	    int sum = 0;
	    while (index1 < sequenceLength && index2 < sequenceLength
		   && sequence[index1++] == sequence[index2++]) {
		sum++;
	    }
	    return sum;
	}

	private int[] makeSuffixArray() {
	    IntComparator ic = new IntComparator() {
		    public int compare(int index1, int index2) {
			int limit = sequenceLength - index1;
			int olimit = sequenceLength -index2;
			int xlimit = limit;
			if (limit > olimit) xlimit = olimit;
			for (int i = 0; i < xlimit; i++) {
			    if (sequence[index1+i] < sequence[index2+i])
				return -1;
			    if (sequence[index1+i] > sequence[index2+i])
				return 1;
			}
			if (limit < olimit) return -1;
			if (limit > olimit) return 1;
			return 0;
		    }
		};
	    int slenp1 = sequenceLength + 1;
	    int[] suffixArray = new int[slenp1];
	    for (int i = 0; i < slenp1; i++) {
		suffixArray[i] = i;
	    }
	    PrimArrays.sort(suffixArray, ic);
	    return suffixArray;
	}

	private IntComparator ic = new IntComparator() {
		public int compare(int index1, int index2) {
		    int limit = sequenceLength - index1;
		    int olimit = sequenceLength -index2;
		    int xlimit = limit;
		    if (limit > olimit) xlimit = olimit;
		    for (int i = 0; i < xlimit; i++) {
			if (sequence[index1+i] < sequence[index2+i]) return -1;
			if (sequence[index1+i] > sequence[index2+i]) return 1;
		    }
		    if (limit < olimit) return -1;
		    if (limit > olimit) return 1;
		    return 0;
		}
	    };

	private int[] guess(int[] bucketSizes, boolean[] inS, int[] tails) {
	    int[] guessed = new int[slenp1];
	    Arrays.fill(guessed, -1);
	    for (int i = 0; i < sequenceLength; i++) {
		if (i == 0 || !inS[i] || inS[i-1]) continue;
		int element = sequence[i];
		guessed[tails[element]] = i;
		tails[element] -= 1;
	    }
	    guessed[0] = sequenceLength;
	    return guessed;
	}

	// note: the array heads will be modified. The array guessed is used
	// for both input and output.
	private void induceSortL(int[] guessed, int[] bucketSizes,
				 int[] heads, boolean[] inS)
	{
	    for (int i = 0; i < guessed.length; i++) {
		if (guessed[i] == -1) continue;
		int j = guessed[i]-1;
		if (j < 0) continue;
		if (inS[j]) continue;
		int element = sequence[j];
		guessed[heads[element]] = j;
		heads[element]++;
	    }
	}

	// note: the array tails will be modified. The array guessed is used
	// for both input and output.
	private void induceSortS(int[] guessed, int[] bucketSizes,
				 int[] tails, boolean[] inS)
	{
	    for (int i = guessed.length-1; i > -1; i--) {
		int j = guessed[i] -1;
		if (j < 0) continue;
		if (!inS[j]) continue;
		int element = sequence[j];
		guessed[tails[element]] = j;
		tails[element]--;
	    }
	}

	static class SummaryResults {
	    int[] summarySequence;
	    int summaryAlphabetSize;
	    int[] summaryOffsets;
	    SummaryResults(int[] seq, int size, int[] offsets) {
		summarySequence = seq;
		summaryAlphabetSize = size;
		summaryOffsets = offsets;
	    }
	}

	private SummaryResults summarize(int[] guessed, boolean[] inS) {
	    int[] lmsNames = new int[slenp1];
	    Arrays.fill(lmsNames, -1);
	    int current = 0;
	    lmsNames[guessed[0]] = current;
	    int last = guessed[0];
	    for (int i = 1; i < guessed.length; i++) {
		int element = guessed[i];
		if (element == 0 || !inS[element] || inS[element-1]) continue;
		int index1 = last;
		int index2 = element;
		if (index1 == sequenceLength || index2 == sequenceLength) {
		    current++;
		    last = element;
		    lmsNames[element] = current;
		    continue;
		}
		boolean lms1 = (index1 == 0)? false:
		    ((inS[index1] && !inS[index1-1])? true:false);
		boolean lms2 = (index2 == 0)? false:
		    ((inS[index2] && !inS[index2-1])? true:false);
		if (lms1 != lms2) {
		    current++;
		    last = element;
		    lmsNames[element] = current;
		    continue;
		}
		if (sequence[index1] != sequence[index2]) {
		    current++;
		    last = element;
		    lmsNames[element] = current;
		    continue;
		}
		index1++;
		index2++;
		while (true) {
		    lms1 = (index1 == 0)? false:
			((inS[index1] && !inS[index1-1])? true:false);
		    lms2 = (index2 == 0)? false:
			((inS[index2] && !inS[index2-1])? true:false);
		    if (lms1 && lms2) {
			break;
		    }
		    if (lms1 != lms2) {
			current++;
			break;
		    }
		    if (sequence[index1] != sequence[index2]) {
			current++;
			break;
		    }
		    index1++;
		    index2++;
		}
		last = element;
		lmsNames[element] = current;
	    }
	    int limit = 0;
	    for (int i = 0; i < slenp1; i++) {
		if (lmsNames[i] == -1) continue;
		limit++;
	    }
	    int[] summarySequence = new int[limit];
	    int[] summaryOffsets = new int[limit];
	    int j = 0;

	    for (int i = 0; i < slenp1; i++) {
		if (lmsNames[i] == -1) continue;
		summaryOffsets[j] = i;
		summarySequence[j] = lmsNames[i];
		j++;
	    }
	    lmsNames = null;
	    int summaryAlphabetSize = current + 1;
	    return new SummaryResults(summarySequence, summaryAlphabetSize,
				      summaryOffsets);
	}

	private int[] makeSummarySuffixes(int[] summarySequence,
					  int summaryAlphabetSize)
	{
	    if (summaryAlphabetSize == summarySequence.length) {
		int[] result = new int[summarySequence.length + 1];
		Arrays.fill(result, -1);
		result[0] = summarySequence.length;
		for (int i = 0; i < summarySequence.length; i++) {
		    int element = summarySequence[i];
		    result[element+1] = i;
		}
		return result;
	    } else {
		SuffixArray result = new
		    SuffixArray.Integer(summarySequence, summaryAlphabetSize);
		return result.array;
	    }
	}

	private int[] actualLMSSort(int[] bucketSizes, boolean[] inS,
				    int[] tails,
				    int[] summarySuffixes,
				    int[] summaryOffsets)
	{
	    array = new int[slenp1];
	    Arrays.fill(array, -1);
	    for (int i = summarySuffixes.length-1; i > 1; i--) {
		int sIndex = summaryOffsets[summarySuffixes[i]];
		int bIndex = sequence[sIndex];

		array[tails[bIndex]] = sIndex;
		tails[bIndex]--;
	    }
	    array[0] = sequenceLength;
	    return array;
	}

	/**
	 * Constructor.
	 * The maximum reasonable value of n should be no higher than the
	 * length of the sequence and frequently much lower.
	 * The sequence must not be changed after this constructor is
	 * called as long as this suffix array is used.
	 * @param sequence a sequence of integers whose values are in the
	 *        range [0,n) where n is positive and no larger than 0xFFFE
	 *        (2<sup>16</sup>-2)
	 * @param n the size of an alphabet encoded as values in [0,n)
	 *        where n is positive an no larger than 0xFFFF
	 *        (2<sup>16</sup> -1)
	 * @exception IllegalArgumentException n is out of range.
	 */
	public Char(char[] sequence, int n) {
	    if (n <= 0 || n > 0xFFFF) {
		throw new IllegalArgumentException(errorMsg("argsOutOfRange"));
	    }
	    this.sequence = sequence;
	    sequenceLength = sequence.length;
	    slenp1 = sequenceLength+1;
	    if (sequenceLength < 2) {
		try {
		    if (sequenceLength == 0) {
			array = Cloner.makeClone(SuffixArray.result0);
		    } else if (sequenceLength == 1) {
			array = Cloner.makeClone(SuffixArray.result1);
		    }
		} catch(CloneNotSupportedException e) {
		    throw new UnexpectedExceptionError(e);
		}
		return;
	    } else if (sequenceLength < 22) {
		array = makeSuffixArray();
		return;
	    }

	    boolean[] inS = new boolean[slenp1];
	    inS[sequenceLength] = true;
	    if (sequenceLength > 0) {
		for (int i = sequenceLength-2; i > -1; i--) {
		    if (sequence[i] == sequence[i+1] && inS[i+1]) {
			inS[i] = true;
		    } else if (sequence[i] < sequence[i+1]) {
			inS[i] = true;
		    }
		}
	    }
	    int[] bucketSizes = new int[n];
	    for (int i = 0; i < sequenceLength; i++) {
		int element = sequence[i];
		if (element < 0 || element >= n) {
		    throw new
			IllegalArgumentException(errorMsg("notInAlphabet", i));
		}
		bucketSizes[element]++;
	    }
	    int[] heads = new int[n];
	    int[] tails = new int[n];
	    int ind = 1;
	    for (int i = 0; i < n; i++) {
		heads[i] = ind;
		int size = bucketSizes[i];
		ind += size;
		tails[i] = ind-1;
	    }

	    try {
		int[] guessed = guess(bucketSizes, inS,
				      Cloner.makeClone(tails));
		induceSortL(guessed, bucketSizes, Cloner.makeClone(heads), inS);
		induceSortS(guessed, bucketSizes, Cloner.makeClone(tails), inS);
		SummaryResults summary = summarize(guessed, inS);
		guessed = null;
		int[] summarySuffixes =
		    makeSummarySuffixes(summary.summarySequence,
					summary.summaryAlphabetSize);

		array = actualLMSSort(bucketSizes, inS, Cloner.makeClone(tails),
				      summarySuffixes, summary.summaryOffsets);
		induceSortL(array, bucketSizes, heads, inS);
		induceSortS(array, bucketSizes, tails, inS);
	    } catch (CloneNotSupportedException e) {
		throw new UnexpectedExceptionError(e);
	    }
	}
	/**
	 * Constructor for precomputed suffix arrays.
	 * The maximum reasonable value of n should be no higher than the
	 * length of the sequence and frequently much lower.
	 * The sequence must not be changed after this constructor is
	 * called as long as this suffix array is used.
	 * <P>
	 * The suffix array passed as the third argument must have one
	 * more element than the sequence.
	 * <P>
	 * This constructor is intended for the case where a suffix
	 * array was previously computed and saved in a file. The caller
	 * must assure that the suffix array was computed for the
	 * sequence and that the alphabets are the same.
	 * @param sequence a sequence of integers whose values are in the
	 *        range [0,n)
	 * @param sarray the suffix array
	 * @exception IllegalArgumentException n is out of range or
	 *            the suffix array is not compatible with the sequence.
	 */
	public Char(char[] sequence, int[] sarray)
	    throws IllegalArgumentException
	{
	    if (sarray.length != sequence.length + 1) {
		throw new IllegalArgumentException("seqArrayNotCompatible");
	    }
	    this.sequence = sequence;
	    this.array = sarray;
	    sequenceLength = sequence.length;
	    slenp1 = sequence.length+1;
	}
    }

    /**
     * Class providing a suffix array for byte-valued sequences.
     * The size of the alphabet must be 128 or less.
     */
    public static final class Byte extends SuffixArray {

	byte[] sequence;
	int sequenceLength;

	/**
	 * Get the sequence associated with this suffix array.
	 * The sequence is an array that must not be modified.
	 * @return the sequence that this suffix array describes
	 */
	public byte[] getSequence() {return sequence;}

	private int findSubsequenceLCPLR(byte[] sarray, int start, int end)
	{
	    if (start >= end) return -1;
	    int low = 1;
	    int high = array.length-1;
	    int s = start;
	    int indl = array[low];
	    int indh = array[high];
	    while (s < end-1
		   && indl < sequenceLength && indh < sequenceLength
		   && (sarray[s] == sequence[indl])
		   && (sarray[s] == sequence[indh])) {
		s++; indl++; indh++;
	    }
	    int s2 = s;
	    int s3 = s;
	    while (indh < sequenceLength  && s2 < end-1 &&
		   (sarray[s2] == sequence[indh])) {
		s2++; indh++;
	    }
	    if (indh == sequenceLength) return -1;
	    if (s2 < end && sarray[s2] > sequence[indh]) return -1;
	    if (s2 == end-1 && sarray[s2] == sequence[indh]) return high;
	    if (s2 < end-1 && indh == sequenceLength - 1
		&& sequence[indh] == sarray[s2]) {
		return -1;
	    }
	    while (indl < sequenceLength  && s3 < end-1 &&
		   (sarray[s3] == sequence[indl])) {
		s3++; indl++;
	    }
	    if (s3 < end && indl < sequenceLength &&
		sarray[s3] < sequence[indl]) {
		return -1;
	    }
	    if (s3 == end-1 && indl != sequenceLength
		&& sarray[s3] == sequence[indl]) {
		return low;
	    }
	    boolean right = (s2 <= s3);
	    if (right) {
		s = s3;
	    } else {
		s = s2;
	    }
	    int k = s - start;
	    int test = -2;	// Signal that test was not done.
	    while ((high - low) > 1) {
		int middle = (low + high) >>> 1;
		int ind = array[middle] + k;
		int val = (ind >= sequenceLength)? -1: sequence[ind];
		int key = sarray[s];
		if (right) {
		    if (k < LCP_L[middle]) {
			low = middle;
		    } else if (k > LCP_L[middle]) {
			high = middle;
			right = true;
		    } else {
			test = ((val == key)? 0: ((val < key)? -1: 1));
			if (test < 0) {
			    low = middle;
			} else if (test > 0) {
			    high = middle;
			    right = false;
			} else {
			    while (test == 0 && s < end-1) {
				k++; ind++; s++;
				if (ind >= sequenceLength) {
				    test = -1;
				    break;
				}
				val = sequence[ind];
				key = sarray[s];
				test = ((val == key)? 0: ((val < key)? -1: 1));
			    }
			    if (test < 0) {
				low = middle;
			    } else if (test > 0) {
				high = middle;
				right = false;
			    } else if (s == end-1) {
				return middle;
			    } else {
				throw new IllegalStateException();
			    }
			}
		    }
		} else {
		    if (k < LCP_R[middle]) {
			high = middle;
		    } else if (k > LCP_R[middle]) {
			low = middle;
			right = false;
		    } else {
			test = ((val == key)? 0: ((val < key)? -1: 1));
			if (test < 0) {
			    low = middle;
			    right = true;
			} else if (test > 0) {
			    high = middle;
			} else {
			    while (test == 0 && s < end-1) {
				k++; ind++; s++;
				if (ind == sequenceLength) {
				    test = -1;
				    break;
				}
				val = sequence[ind];
				key = sarray[s];
				test = ((val == key)? 0: ((val < key)? -1: 1));
			    }
			    if (test < 0) {
				low = middle;
				right = true;
			    } else if (test > 0) {
				high = middle;
			    } else if (s == end-1) {
				return middle;
			    } else {
				throw new IllegalStateException
				    ("search failed but test was zero");
			    }
			}
		    }
		}
	    }
	    if (test == -2) {
		test = 0;
		if (right) {
		    int ind = array[low];
		    for (int i = start; i < end; i++) {
			if (ind >= sequenceLength) {
			    test = -1;
			    break;
			}
			int val = sequence[ind++];
			test = (val < sarray[i])? -1: 1;
			if (test != 0) break;
		    }
		    if (test == 0) return low;
		} else {
		    int ind = array[high];
		    for (int i = start; i < end; i++) {
			if (ind >= sequenceLength) {
			    test = -1;
			    break;
			}
			int val = sequence[ind++];
			test = (val < sarray[i])? -1: 1;
			if (test != 0) break;
		    }
		    if (test == 0) return high;
		}
	    }
	    if (test < 0) {
		// test high
		int ind = array[high];
		for (int i = start; i < end; i++) {
		    if (ind >= sequenceLength) return -1;
		    byte val = sequence[ind++];
		    if (val != sarray[i]) return -1;
		}
		return high;
	    } else if (test > 0) {
		// test low
		int ind = array[low];
		for (int i = start; i < end; i++) {
		    if (ind >= sequenceLength) return -1;
		    byte val = sequence[ind++];
		    if (val != sarray[i]) return -1;
		}
		return low;
	    }
	    return -1;
	}

	class FindComparator implements IntComparator {
	    int start;
	    int end;
	    int limit;

	    byte[] sarray;
	    FindComparator(byte[] sarray, int start, int end) {
		this.sarray = sarray;
		this.start = start;
		this.limit = end - start;
	    }

	    public int compare(int o1, int o2) {
		for (int i = 0; i < limit; i++) {
		    if (o1 + i >= sequenceLength) return -1;
		    int val1 = sequence[o1 + i];
		    int val2 = sarray[i];
		    if (val1 < val2) return -1;
		    if (val1 > val2) return 1;
		}
		return 0;
	    }
	}

	/**
	 * Find the index into the sequence associated with a suffix
	 * array for an arbitrary instance of a subsequence.
	 * <P>
	 * Using an LCP-LR table (created by calling
	 * {@link SuffixArray#useLCPLR()}) will change with time
	 * complexity of this method from O(m log n) to O(m + log n),
	 * where m is the length of a subsequence and n is the length
	 * of the sequence array. These, however, are worst-case numbers:
	 * while it can take m steps for a comparison function to
	 * determine that two suffixes differ, the comparison will stop
	 * at the first step at which the suffixes actually differ:
	 * the difference in running time in practice is data-set
	 * dependent.
	 * @param subsequence the subsequence.
	 * @return the index into the sequence; -1 if the subsequence
	 *         cannot be found
	 * @see SuffixArray#getArray()
	 * @see #getSequence()
	 * @see #useLCPLR()
	 */
	public int findInstance(byte[] subsequence) {
	    int ind = findSubsequence(subsequence);
	    if (ind == -1) return -1;
	    return array[ind];
	}

	/**
	 * Find the index into the sequence associated with a suffix
	 * array for an arbitrary instance of a subsequence given a
	 * starting and ending index into an array containing the
	 * subsequence.
	 * <P>
	 * Using an LCP-LR table (created by calling
	 * {@link SuffixArray#useLCPLR()}) will change with time
	 * complexity of this method from O(m log n) to O(m + log n),
	 * where m is the length of a subsequence and n is the length
	 * of the sequence array. These, however, are worst-case numbers:
	 * while it can take m steps for a comparison function to
	 * determine that two suffixes differ, the comparison will stop
	 * at the first step at which the suffixes actually differ:
	 * the difference in running time in practice is data-set
	 * dependent.
	 * @param subsequence  array containing the subsequence.
	 * @param start the starting index in the subsequence array (inclusive)
	 * @param end the ending index in the subsequence array (exclusive)
	 * @return the index into the sequence; -1 if the subsequence
	 *         cannot be found
	 * @see SuffixArray#getArray()
	 * @see #getSequence()
	 * @see #useLCPLR()
	 */
	public int findInstance(byte[] subsequence, int start, int end) {
	    int ind = findSubsequence(subsequence, start, end);
	    if (ind == -1) return -1;
	    return array[ind];
	}

	/**
	 * Find the suffix-array index of an arbitrary instance of
	 * a subsequence.
	 * This method finds the index into a suffix array corresponding to
	 * a subsequence.
	 * @param subsequence the subsequence.
	 * @return the index into the suffix array; -1 if the subsequence
	 *         cannot be found
	 * @see SuffixArray#getArray()
	 */
	public int findSubsequence(byte[] subsequence) {
	    return findSubsequence(subsequence, 0, subsequence.length);
	}


	/**
	 * Find the suffix-array index of an arbitrary instance of a
	 * subsequence given a starting index and ending index for
	 * the subsequence.
	 * This method finds the index into a suffix array corresponding to
	 * a subsequence. The subsequence consists of the elements of
	 * the array sarray with a starting index named start and and
	 * ending index named end.
	 * @param sarray the subsequence array.
	 * @param start the starting index in the subsequence array (inclusive)
	 * @param end the ending index in the subsequence array (exclusive)
	 * @return the index into the suffix array; -1 if the subsequence
	 *         cannot be found
	 * @see SuffixArray#getArray()
	 */
	public int findSubsequence(byte[] sarray, int start,
				   int end)
	{
	    if (start >= end) return -1;
	    int first = 1;
	    int last = array.length;

	    if (LCP_L != null && LCP_R != null) {
		return findSubsequenceLCPLR(sarray, start, end);
	    } else {
		FindComparator c = new FindComparator(sarray, start, end);
		int result = PrimArrays.binarySearch(array, first, last, 0, c);
		return (result < 0)? -1: result;
	    }
	}

	/**
	 * Find a subsequence.
	 * This method finds the index into a suffix array corresponding to
	 * a subsequence.
	 * @param subsequence the subsequence.
	 * @param keyflag true if the highest index should be returned; false
	 *        if the lowest index should be returned.
	 * @return the index into the suffix array; -1 if the subsequence
	 *         cannot be found
	 * @see SuffixArray#getArray()
	 */
	public int findSubsequence(byte[] subsequence, boolean keyflag) {
	    return findSubsequence(subsequence, 0, subsequence.length, keyflag);
	}

	/**
	 * Find a subsequence given a starting index and ending index for
	 * the subsequence.
	 * This method finds the index into a suffix array corresponding to
	 * a subsequence. The subsequence consists of the elements of
	 * the array sarray with a starting index named start and and
	 * ending index named end.
	 * @param subsequence the subsequence array.
	 * @param start the starting index in the subsequence array (inclusive)
	 * @param end the ending index in the subsequence array (exclusive)
	 * @param keyflag true if the highest index should be returned; false
	 *        if the lowest index should be returned.
	 * @return the index into the suffix array; -1 if the subsequence
	 *         cannot be found
	 * @see SuffixArray#getArray()
	 */
	public int findSubsequence(byte[] subsequence, int start,
				   int end,
				   boolean keyflag)
	{
	    if (start >= end) return -1;
	    int first = 1;
	    int last = array.length;
	    FindComparator c = new FindComparator(subsequence, start, end);
	    int i1 = PrimArrays.binarySearch(array, first, last, -1,
						     c, false);
	    if (i1 < 0) return -1;
	    int i2 = PrimArrays.binarySearch(array, i1, last, -1,
					     c, true);
	    if (i2 < 0) return -1;
	    return keyflag? i2: i1;
	}

	/**
	 * Find all instances of a subsequence.
	 * @param subsequence the subsequence
	 * @return the subsequences corresponding to a range in the
	 *         suffix array
	 * @see #getSequence()
	 */
	public Range findRange(byte[] subsequence) {
	    return findRange(subsequence, 0, subsequence.length);
	}

	/**
	 * Find  all instances of a subsequence given a starting index
	 * and ending index.
	 * @param sarray the subsequence array.
	 * @param start the starting index in the subsequence array (inclusive)
	 * @param end the ending index in the subsequence array (exclusive)
	 * @return the subsequences corresponding to a range in the
	 *         suffix array
	 * @see #getSequence()
	 */
	public Range findRange(byte[] sarray, int start, int end)
	{
	    if (start >= end) return new OurRange(0, 0, 0);
	    int first = 1;
	    int last = array.length;
	    FindComparator c = new FindComparator(sarray, start, end);
	    int i1 = PrimArrays.binarySearch(array, first, last, -1,
					     c, false);
	    if (i1 < 0) return new OurRange(0, 0, 0);
	    int i2 = PrimArrays.binarySearch(array, i1, last, -1,
					     c, true);
	    if (i2 < 0) return new OurRange(0, 0, 0);
	    return new OurRange(end - start, i1, i2+1);
	}


	/**
	 * Get the Burrows-Wheeler Transform  (BWT) of the sequence associated
	 * with this suffix array, with -1 indicating the end-of-text addition
	 * to the alphabet.
	 * The value returned includes the end-of-text symbol in the transform
	 * when the length of the array is one more than the length of the
	 * sequence associated with this suffix array.
	 * @param bwt the array to store the BWT (an array whose length is
	 *        the length of the sequence if the end-of-text symbol does
	 *        not appear in the BWT and one more than the length of the
	 *        sequence if the end-of-text symbol does appear in the
	 *        BWT)
	 * @return the index for the sorted permutation that matches the sequence
	 */
	public int getBWT(byte[] bwt) {
	    int result = 0;
	    boolean mode = (bwt.length == sequenceLength);
	    if (mode) {
		for (int i = 1; i < array.length; i++) {
		    int index = array[i];
		    if (index == 0) {
			result = i-1;
			bwt[i-1] = sequence[sequenceLength-1];
		    } else {
			bwt[i-1] = sequence[index-1];
		    }
		}
	    } else {
		for (int i = 0; i < array.length; i++) {
		    int index = array[i];
		    if (index == 0) {
			result = i;
			bwt[i] = -1;
		    } else {
			bwt[i] = sequence[index-1];
		    }
		}
	    }
	    return result;
	}


	/**
	 * Compute the inverse Burrows-Wheeler transform.
	 * When the length of of the BWT array is one more than the
	 * length of the result array, the BTW array is assumed to
	 * contain an end-of-text symbol (-1 for this case), and the
	 * index parameter is ignored. If the two arrays have the same
	 * length, all symbols in the BWT array must be in the alphabet
	 * and the index must be provided (it will be the value returned
	 * by a call to {@link #getBWT(byte[])}).
	 * @param bwt the Burrows-Wheeler transform
	 * @param result the inverse of the Burrons-Wheeler transform
	 * @param index the index parameter for the Burrows-Wheeler transform
	 * @param n the size of the alphabet.
	 * @exception IllegalArgumentException bwt and result have inconsistent
	 *            lengths
	 */
	public static void inverseBWT(byte[] bwt, byte[] result,
				      int index, int n)
	    throws IllegalArgumentException
	{
	    if (bwt.length == result.length) {
		int[] K = new int[n];
		int[] C = new int[result.length];
		int[] M = new int[n];
		for (int i = 0; i < result.length; i++) {
		    C[i] = K[bwt[i]];
		    K[bwt[i]] = K[bwt[i]] + 1;
		}
		int sum = 0;
		for (int i = 0; i < n; i++) {
		    M[i] = sum;
		    sum = sum + K[i];
		}
		for (int j = result.length-1; j >= 0; j--) {
		    result[j] = bwt[index];
		    index = C[index] + M[bwt[index]];
		}
	    } else if (bwt.length == result.length + 1) {
		int[] K = new int[n];
		int[] C = new int[result.length];
		int[] M = new int[n];
		int off = 1;
		for (int i = 0; i < result.length; i++) {
		    int ii = i + 1;
		    if (bwt[ii] == -1) {
			C[i] = K[bwt[0]];
			K[bwt[0]] = K[bwt[0]] + 1;
			index = i;
		    } else {
			C[i] = K[bwt[ii]];
			K[bwt[ii]] = K[bwt[ii]] + 1;
		    }
		}
		int sum = 0;
		for (int i = 0; i < n; i++) {
		    M[i] = sum;
		    sum = sum + K[i];
		}
		for (int j = result.length-1; j >= 0; j--) {
		    int ii = index + 1;
		    if (bwt[ii] == -1) {
			result[j] = bwt[0];
			index = C[index] + M[bwt[0]];
		    } else {
			result[j] = bwt[ii];
			index = C[index] + M[bwt[ii]];
		    }
		}
	    } else {
		throw new IllegalArgumentException(errorMsg("BWTlength"));
	    }
	}

	@Override
	protected void fillLCPArray(int[] ourlcpArray, int[] rank) {
	    int k = 0;
	    int n = sequenceLength;
	    for (int i = 0; i < n; i++) {
		if (rank[i] == n) {
		    k = 0;
		    continue;
		}
		int j = array[rank[i]+1];
		while (i+k < n && j+k < n &&
		       sequence[i+k] == sequence[j+k]) {
		    k++;
		}
		ourlcpArray[rank[i]+1] = k;
		if (k > 0) k--;
	    }
	}

	@Override
	protected int commonPrefixLength(int index1, int index2) {
	    int sum = 0;
	    while (index1 < sequenceLength && index2 < sequenceLength
		   && sequence[index1++] == sequence[index2++]) {
		sum++;
	    }
	    return sum;
	}

	private int[] makeSuffixArray() {
	    IntComparator ic = new IntComparator() {
		    public int compare(int index1, int index2) {
			int limit = sequenceLength - index1;
			int olimit = sequenceLength -index2;
			int xlimit = limit;
			if (limit > olimit) xlimit = olimit;
			for (int i = 0; i < xlimit; i++) {
			    if (sequence[index1+i] < sequence[index2+i])
				return -1;
			    if (sequence[index1+i] > sequence[index2+i])
				return 1;
			}
			if (limit < olimit) return -1;
			if (limit > olimit) return 1;
			return 0;
		    }
		};
	    int slenp1 = sequenceLength + 1;
	    int[] suffixArray = new int[slenp1];
	    for (int i = 0; i < slenp1; i++) {
		suffixArray[i] = i;
	    }
	    PrimArrays.sort(suffixArray, ic);
	    return suffixArray;
	}

	private IntComparator ic = new IntComparator() {
		public int compare(int index1, int index2) {
		    int limit = sequenceLength - index1;
		    int olimit = sequenceLength -index2;
		    int xlimit = limit;
		    if (limit > olimit) xlimit = olimit;
		    for (int i = 0; i < xlimit; i++) {
			if (sequence[index1+i] < sequence[index2+i]) return -1;
			if (sequence[index1+i] > sequence[index2+i]) return 1;
		    }
		    if (limit < olimit) return -1;
		    if (limit > olimit) return 1;
		    return 0;
		}
	    };

	private int[] guess(int[] bucketSizes, boolean[] inS, int[] tails) {
	    int[] guessed = new int[slenp1];
	    Arrays.fill(guessed, -1);
	    for (int i = 0; i < sequenceLength; i++) {
		if (i == 0 || !inS[i] || inS[i-1]) continue;
		int element = sequence[i];
		guessed[tails[element]] = i;
		tails[element] -= 1;
	    }
	    guessed[0] = sequenceLength;
	    return guessed;
	}

	// note: the array heads will be modified. The array guessed is used
	// for both input and output.
	private void induceSortL(int[] guessed, int[] bucketSizes,
				 int[] heads, boolean[] inS)
	{
	    for (int i = 0; i < guessed.length; i++) {
		if (guessed[i] == -1) continue;
		int j = guessed[i]-1;
		if (j < 0) continue;
		if (inS[j]) continue;
		int element = sequence[j];
		guessed[heads[element]] = j;
		heads[element]++;
	    }
	}

	// note: the array tails will be modified. The array guessed is used
	// for both input and output.
	private void induceSortS(int[] guessed, int[] bucketSizes,
				 int[] tails, boolean[] inS)
	{
	    for (int i = guessed.length-1; i > -1; i--) {
		int j = guessed[i] -1;
		if (j < 0) continue;
		if (!inS[j]) continue;
		int element = sequence[j];
		guessed[tails[element]] = j;
		tails[element]--;
	    }
	}

	static class SummaryResults {
	    int[] summarySequence;
	    int summaryAlphabetSize;
	    int[] summaryOffsets;
	    SummaryResults(int[] seq, int size, int[] offsets) {
		summarySequence = seq;
		summaryAlphabetSize = size;
		summaryOffsets = offsets;
	    }
	}

	private SummaryResults summarize(int[] guessed, boolean[] inS) {
	    int[] lmsNames = new int[slenp1];
	    Arrays.fill(lmsNames, -1);
	    int current = 0;
	    lmsNames[guessed[0]] = current;
	    int last = guessed[0];
	    for (int i = 1; i < guessed.length; i++) {
		int element = guessed[i];
		if (element == 0 || !inS[element] || inS[element-1]) continue;
		int index1 = last;
		int index2 = element;
		if (index1 == sequenceLength || index2 == sequenceLength) {
		    current++;
		    last = element;
		    lmsNames[element] = current;
		    continue;
		}
		boolean lms1 = (index1 == 0)? false:
		    ((inS[index1] && !inS[index1-1])? true:false);
		boolean lms2 = (index2 == 0)? false:
		    ((inS[index2] && !inS[index2-1])? true:false);
		if (lms1 != lms2) {
		    current++;
		    last = element;
		    lmsNames[element] = current;
		    continue;
		}
		if (sequence[index1] != sequence[index2]) {
		    current++;
		    last = element;
		    lmsNames[element] = current;
		    continue;
		}
		index1++;
		index2++;
		while (true) {
		    lms1 = (index1 == 0)? false:
			((inS[index1] && !inS[index1-1])? true:false);
		    lms2 = (index2 == 0)? false:
			((inS[index2] && !inS[index2-1])? true:false);
		    if (lms1 && lms2) {
			break;
		    }
		    if (lms1 != lms2) {
			current++;
			break;
		    }
		    if (sequence[index1] != sequence[index2]) {
			current++;
			break;
		    }
		    index1++;
		    index2++;
		}
		last = element;
		lmsNames[element] = current;
	    }
	    int limit = 0;
	    for (int i = 0; i < slenp1; i++) {
		if (lmsNames[i] == -1) continue;
		limit++;
	    }
	    int[] summarySequence = new int[limit];
	    int[] summaryOffsets = new int[limit];
	    int j = 0;

	    for (int i = 0; i < slenp1; i++) {
		if (lmsNames[i] == -1) continue;
		summaryOffsets[j] = i;
		summarySequence[j] = lmsNames[i];
		j++;
	    }
	    lmsNames = null;
	    int summaryAlphabetSize = current + 1;
	    return new SummaryResults(summarySequence, summaryAlphabetSize,
				      summaryOffsets);
	}

	private int[] makeSummarySuffixes(int[] summarySequence,
					  int summaryAlphabetSize)
	{
	    if (summaryAlphabetSize == summarySequence.length) {
		int[] result = new int[summarySequence.length + 1];
		Arrays.fill(result, -1);
		result[0] = summarySequence.length;
		for (int i = 0; i < summarySequence.length; i++) {
		    int element = summarySequence[i];
		    result[element+1] = i;
		}
		return result;
	    } else {
		SuffixArray result = new
		    SuffixArray.Integer(summarySequence, summaryAlphabetSize);
		return result.array;
	    }
	}

	private int[] actualLMSSort(int[] bucketSizes, boolean[] inS,
				    int[] tails,
				    int[] summarySuffixes,
				    int[] summaryOffsets)
	{
	    array = new int[slenp1];
	    Arrays.fill(array, -1);
	    for (int i = summarySuffixes.length-1; i > 1; i--) {
		int sIndex = summaryOffsets[summarySuffixes[i]];
		int bIndex = sequence[sIndex];

		array[tails[bIndex]] = sIndex;
		tails[bIndex]--;
	    }
	    array[0] = sequenceLength;
	    return array;
	}

	/**
	 * Constructor.
	 * The maximum reasonable value of n should be no higher than the
	 * length of the sequence and frequently much lower.
	 * The sequence must not be changed after this constructor is
	 * called as long as this suffix array is used.
	 * @param sequence a sequence of integers whose values are in the
	 *        range [0,n)
	 * @param n the size of an alphabet encoded as values in [0,n)
	 *        where n is positive and no larger than 255
	 * @exception IllegalArgumentException an argument was out of range
	 */
	public Byte(byte[] sequence, int n) {
	    if (n < 0 || n > 128) {
		throw new IllegalArgumentException(errorMsg("argsOutOfRange"));
	    }
	    this.sequence = sequence;
	    sequenceLength = sequence.length;
	    slenp1 = sequenceLength+1;
	    if (sequenceLength < 2) {
		try {
		    if (sequenceLength == 0) {
			array = Cloner.makeClone(SuffixArray.result0);
		    } else if (sequenceLength == 1) {
			array = Cloner.makeClone(SuffixArray.result1);
		    }
		} catch(CloneNotSupportedException e) {
		    throw new UnexpectedExceptionError(e);
		}
		return;
	    } else if (sequenceLength < 22) {
		array = makeSuffixArray();
		return;
	    }

	    boolean[] inS = new boolean[slenp1];
	    inS[sequenceLength] = true;
	    if (sequenceLength > 0) {
		for (int i = sequenceLength-2; i > -1; i--) {
		    if (sequence[i] == sequence[i+1] && inS[i+1]) {
			inS[i] = true;
		    } else if (sequence[i] <
			       sequence[i+1]) {
			inS[i] = true;
		    }
		}
	    }
	    int[] bucketSizes = new int[n];
	    for (int i = 0; i < sequenceLength; i++) {
		int element = sequence[i];
		if (element < 0 || element >= n) {
		    throw new
			IllegalArgumentException(errorMsg("notInAlphabet", i));
		}
		bucketSizes[element]++;
	    }
	    int[] heads = new int[n];
	    int[] tails = new int[n];
	    int ind = 1;
	    for (int i = 0; i < n; i++) {
		heads[i] = ind;
		int size = bucketSizes[i];
		ind += size;
		tails[i] = ind-1;
	    }

	    try {
		int[] guessed = guess(bucketSizes, inS,
				      Cloner.makeClone(tails));
		induceSortL(guessed, bucketSizes, Cloner.makeClone(heads), inS);
		induceSortS(guessed, bucketSizes, Cloner.makeClone(tails), inS);
		SummaryResults summary = summarize(guessed, inS);
		guessed = null;
		int[] summarySuffixes =
		    makeSummarySuffixes(summary.summarySequence,
					summary.summaryAlphabetSize);

		array = actualLMSSort(bucketSizes, inS, Cloner.makeClone(tails),
				      summarySuffixes, summary.summaryOffsets);
		induceSortL(array, bucketSizes, heads, inS);
		induceSortS(array, bucketSizes, tails, inS);
	    } catch (CloneNotSupportedException e) {
		throw new UnexpectedExceptionError(e);
	    }
	}
	/**
	 * Constructor for precomputed suffix arrays.
	 * The maximum reasonable value of n should be no higher than the
	 * length of the sequence and frequently much lower.
	 * The sequence must not be changed after this constructor is
	 * called as long as this suffix array is used.
	 * <P>
	 * The suffix array passed as the third argument must have one
	 * more element than the sequence.
	 * <P>
	 * This constructor is intended for the case where a suffix
	 * array was previously computed and saved in a file. The caller
	 * must assure that the suffix array was computed for the
	 * sequence and that the alphabets are the same.
	 * @param sequence a sequence of integers whose values are in the
	 *        range [0,n)
	 * @param sarray the suffix array
	 * @exception IllegalArgumentException n is out of range or
	 *            the suffix array is not compatible with the sequence.
	 */
	public Byte(byte[] sequence, int[] sarray)
	    throws IllegalArgumentException
	{
	    if (sarray.length != sequence.length + 1) {
		throw new IllegalArgumentException("seqArrayNotCompatible");
	    }
	    this.sequence = sequence;
	    this.array = sarray;
	    sequenceLength = sequence.length;
	    slenp1 = sequence.length+1;
	}
    }

    /**
     * Class providing a suffix array for unsigned-byte-valued sequences
     * Java bytes are signed. To create an unsigned value from a byte
     * b, one can use the value (0xFF &amp; b): 0xFF is interpreted as an
     * int and type conversion makes the value of (0xFF &amp; b).
     * The value 255, which is excluded from the alphabet is converted to -1.
     */
    public static class UnsignedByte extends SuffixArray {

	byte[] sequence;
	int sequenceLength;

	/**
	 * Get the sequence associated with this suffix array.
	 * The sequence is an array that must not be modified.
	 * @return the sequence that this suffix array describes
	 */
	public byte[] getSequence() {return sequence;}

	private int findSubsequenceLCPLR(byte[] sarray, int start, int end)
	{
	    if (start >= end) return -1;
	    int low = 1;
	    int high = array.length-1;
	    int s = start;
	    int indl = array[low];
	    int indh = array[high];
	    while (s < end-1
		   && indl < sequenceLength && indh < sequenceLength
		   && (sarray[s] == sequence[indl])
		   && (sarray[s] == sequence[indh])) {
		s++; indl++; indh++;
	    }
	    int s2 = s;
	    int s3 = s;
	    while (indh < sequenceLength  && s2 < end-1 &&
		   (sarray[s2] == sequence[indh])) {
		s2++; indh++;
	    }
	    if (indh == sequenceLength) return -1;
	    if (s2 < end && ((0xFF & sarray[s2]) >
			     (0xFF & sequence[indh]))) return -1;
	    if (s2 == end-1 && sarray[s2] == sequence[indh]) return high;
	    if (s2 < end-1 && indh == sequenceLength - 1
		&& sequence[indh] == sarray[s2]) {
		return -1;
	    }
	    while (indl < sequenceLength  && s3 < end-1 &&
		   (sarray[s3] == sequence[indl])) {
		s3++; indl++;
	    }
	    if (s3 < end && indl < sequenceLength &&
		((0xFF & sarray[s3]) < (0xFF & sequence[indl]))) {
		return -1;
	    }
	    if (s3 == end-1 && indl != sequenceLength
		&& sarray[s3] == sequence[indl]) {
		return low;
	    }
	    boolean right = (s2 <= s3);
	    if (right) {
		s = s3;
	    } else {
		s = s2;
	    }
	    int k = s - start;
	    int test = -2;	// Signal that test was not done.
	    while ((high - low) > 1) {
		int middle = (low + high) >>> 1;
		int ind = array[middle] + k;
		int val = (ind >= sequenceLength)? -1: 0xFF & sequence[ind];
		int key = 0xFF & sarray[s];
		if (right) {
		    if (k < LCP_L[middle]) {
			low = middle;
		    } else if (k > LCP_L[middle]) {
			high = middle;
			right = true;
		    } else {
			test = ((val == key)? 0: ((val < key)? -1: 1));
			if (test < 0) {
			    low = middle;
			} else if (test > 0) {
			    high = middle;
			    right = false;
			} else {
			    while (test == 0 && s < end-1) {
				k++; ind++; s++;
				if (ind >= sequenceLength) {
				    test = -1;
				    break;
				}
				val = 0xFF & sequence[ind];
				key = 0xFF & sarray[s];
				test = ((val == key)? 0: ((val < key)? -1: 1));
			    }
			    if (test < 0) {
				low = middle;
			    } else if (test > 0) {
				high = middle;
				right = false;
			    } else if (s == end-1) {
				return middle;
			    } else {
				throw new IllegalStateException();
			    }
			}
		    }
		} else {
		    if (k < LCP_R[middle]) {
			high = middle;
		    } else if (k > LCP_R[middle]) {
			low = middle;
			right = false;
		    } else {
			test = ((val == key)? 0: ((val < key)? -1: 1));
			if (test < 0) {
			    low = middle;
			    right = true;
			} else if (test > 0) {
			    high = middle;
			} else {
			    while (test == 0 && s < end-1) {
				k++; ind++; s++;
				if (ind == sequenceLength) {
				    test = -1;
				    break;
				}
				val = 0xFF & sequence[ind];
				key = 0xFF & sarray[s];
				test = ((val == key)? 0: ((val < key)? -1: 1));
			    }
			    if (test < 0) {
				low = middle;
				right = true;
			    } else if (test > 0) {
				high = middle;
			    } else if (s == end-1) {
				return middle;
			    } else {
				throw new IllegalStateException
				    ("search failed but test was zero");
			    }
			}
		    }
		}
	    }
	    if (test == -2) {
		test = 0;
		if (right) {
		    int ind = array[low];
		    for (int i = start; i < end; i++) {
			if (ind >= sequenceLength) {
			    test = -1;
			    break;
			}
			int val = 0xFF & sequence[ind++];
			test = (val < (0xFF & sarray[i]))? -1: 1;
			if (test != 0) break;
		    }
		    if (test == 0) return low;
		} else {
		    int ind = array[high];
		    for (int i = start; i < end; i++) {
			if (ind >= sequenceLength) {
			    test = -1;
			    break;
			}
			int val = 0xFF & sequence[ind++];
			test = (val < (0xFF & sarray[i]))? -1: 1;
			if (test != 0) break;
		    }
		    if (test == 0) return high;
		}
	    }
	    if (test < 0) {
		// test high
		int ind = array[high];
		for (int i = start; i < end; i++) {
		    if (ind >= sequenceLength) return -1;
		    byte val = sequence[ind++];
		    if (val != sarray[i]) return -1;
		}
		return high;
	    } else if (test > 0) {
		// test low
		int ind = array[low];
		for (int i = start; i < end; i++) {
		    if (ind >= sequenceLength) return -1;
		    byte val = sequence[ind++];
		    if (val != sarray[i]) return -1;
		}
		return low;
	    }
	    return -1;
	}

	class FindComparator implements IntComparator {
	    int start;
	    int end;
	    int limit;

	    byte[] sarray;
	    FindComparator(byte[] sarray, int start, int end) {
		this.sarray = sarray;
		this.start = start;
		this.limit = end - start;
	    }

	    public int compare(int o1, int o2) {
		for (int i = 0; i < limit; i++) {
		    if (o1 + i >= sequenceLength) return -1;
		    int val1 = 0xFF & sequence[o1 + i];
		    int val2 = 0xFF & sarray[i];
		    if (val1 < val2) return -1;
		    if (val1 > val2) return 1;
		}
		return 0;
	    }
	}

	/**
	 * Find the index into the sequence associated with a suffix
	 * array for an arbitrary instance of a subsequence.
	 * <P>
	 * Using an LCP-LR table (created by calling
	 * {@link SuffixArray#useLCPLR()}) will change with time
	 * complexity of this method from O(m log n) to O(m + log n),
	 * where m is the length of a subsequence and n is the length
	 * of the sequence array. These, however, are worst-case numbers:
	 * while it can take m steps for a comparison function to
	 * determine that two suffixes differ, the comparison will stop
	 * at the first step at which the suffixes actually differ:
	 * the difference in running time in practice is data-set
	 * dependent.
	 * @param subsequence the subsequence.
	 * @return the index into the sequence; -1 if the subsequence
	 *         cannot be found
	 * @see SuffixArray#getArray()
	 * @see #getSequence()
	 * @see #useLCPLR()
	 */
	public int findInstance(byte[] subsequence) {
	    int ind = findSubsequence(subsequence);
	    if (ind == -1) return -1;
	    return array[ind];
	}

	/**
	 * Find the index into the sequence associated with a suffix
	 * array for an arbitrary instance of a subsequence given a
	 * starting and ending index into an array containing the
	 * subsequence.
	 * <P>
	 * Using an LCP-LR table (created by calling
	 * {@link SuffixArray#useLCPLR()}) will change with time
	 * complexity of this method from O(m log n) to O(m + log n),
	 * where m is the length of a subsequence and n is the length
	 * of the sequence array. These, however, are worst-case numbers:
	 * while it can take m steps for a comparison function to
	 * determine that two suffixes differ, the comparison will stop
	 * at the first step at which the suffixes actually differ:
	 * the difference in running time in practice is data-set
	 * dependent.
	 * @param subsequence  array containing the subsequence.
	 * @param start the starting index in the subsequence array (inclusive)
	 * @param end the ending index in the subsequence array (exclusive)
	 * @return the index into the sequence; -1 if the subsequence
	 *         cannot be found
	 * @see SuffixArray#getArray()
	 * @see #getSequence()
	 * @see #useLCPLR()
	 */
	public int findInstance(byte[] subsequence, int start, int end) {
	    int ind = findSubsequence(subsequence, start, end);
	    if (ind == -1) return -1;
	    return array[ind];
	}

	/**
	 * Find the suffix-array index of an arbitrary instance of
	 * a subsequence.
	 * This method finds the index into a suffix array corresponding to
	 * a subsequence.
	 * @param subsequence the subsequence.
	 * @return the index into the suffix array; -1 if the subsequence
	 *         cannot be found
	 * @see SuffixArray#getArray()
	 */
	public int findSubsequence(byte[] subsequence) {
	    return findSubsequence(subsequence, 0, subsequence.length);
	}


	/**
	 * Find the suffix-array index of an arbitrary instance of a
	 * subsequence given a starting index and ending index for
	 * the subsequence.
	 * This method finds the index into a suffix array corresponding to
	 * a subsequence. The subsequence consists of the elements of
	 * the array sarray with a starting index named start and and
	 * ending index named end.
	 * @param sarray the subsequence array.
	 * @param start the starting index in the subsequence array (inclusive)
	 * @param end the ending index in the subsequence array (exclusive)
	 * @return the index into the suffix array; -1 if the subsequence
	 *         cannot be found
	 * @see SuffixArray#getArray()
	 */
	public int findSubsequence(byte[] sarray, int start,
				   int end)
	{
	    if (start >= end) return -1;
	    int first = 1;
	    int last = array.length;

	    if (LCP_L != null && LCP_R != null) {
		return findSubsequenceLCPLR(sarray, start, end);
	    } else {
		FindComparator c = new FindComparator(sarray, start, end);
		int result = PrimArrays.binarySearch(array, first, last, 0, c);
		return (result < 0)? -1: result;
	    }
	}

	/**
	 * Find a subsequence.
	 * This method finds the index into a suffix array corresponding to
	 * a subsequence.
	 * @param subsequence the subsequence.
	 * @param keyflag true if the highest index should be returned; false
	 *        if the lowest index should be returned.
	 * @return the index into the suffix array; -1 if the subsequence
	 *         cannot be found
	 * @see SuffixArray#getArray()
	 */
	public int findSubsequence(byte[] subsequence, boolean keyflag) {
	    return findSubsequence(subsequence, 0, subsequence.length, keyflag);
	}

	/**
	 * Find a subsequence given a starting index and ending index for
	 * the subsequence.
	 * This method finds the index into a suffix array corresponding to
	 * a subsequence. The subsequence consists of the elements of
	 * the array sarray with a starting index named start and and
	 * ending index named end.
	 * @param subsequence the subsequence array.
	 * @param start the starting index in the subsequence array (inclusive)
	 * @param end the ending index in the subsequence array (exclusive)
	 * @param keyflag true if the highest index should be returned; false
	 *        if the lowest index should be returned.
	 * @return the index into the suffix array; -1 if the subsequence
	 *         cannot be found
	 * @see SuffixArray#getArray()
	 */
	public int findSubsequence(byte[] subsequence, int start,
				   int end,
				   boolean keyflag)
	{
	    if (start >= end) return -1;
	    int first = 1;
	    int last = array.length;
	    FindComparator c = new FindComparator(subsequence, start, end);
	    int i1 = PrimArrays.binarySearch(array, first, last, -1,
						     c, false);
	    if (i1 < 0) return -1;
	    int i2 = PrimArrays.binarySearch(array, i1, last, -1,
					     c, true);
	    if (i2 < 0) return -1;
	    return keyflag? i2: i1;
	}

	/**
	 * Find all instances of a subsequence.
	 * @param subsequence the subsequence
	 * @return the subsequences corresponding to a range in the
	 *         suffix array
	 * @see #getSequence()
	 */
	public Range findRange(byte[] subsequence) {
	    return findRange(subsequence, 0, subsequence.length);
	}

	/**
	 * Find  all instances of a subsequence given a starting index
	 * and ending index.
	 * @param sarray the subsequence array.
	 * @param start the starting index in the subsequence array (inclusive)
	 * @param end the ending index in the subsequence array (exclusive)
	 * @return the subsequences corresponding to a range in the
	 *         suffix array
	 * @see #getSequence()
	 */
	public Range findRange(byte[] sarray, int start, int end)
	{
	    if (start >= end) return new OurRange(0, 0, 0);
	    int first = 1;
	    int last = array.length;
	    FindComparator c = new FindComparator(sarray, start, end);
	    int i1 = PrimArrays.binarySearch(array, first, last, -1,
					     c, false);
	    if (i1 < 0) return new OurRange(0, 0, 0);
	    int i2 = PrimArrays.binarySearch(array, i1, last, -1,
					     c, true);
	    if (i2 < 0) return new OurRange(0, 0, 0);
	    return new OurRange(end - start, i1, i2+1);
	}


	/**
	 * Get the Burrows-Wheeler Transform  (BWT) of the sequence associated
	 * with this suffix array, with -1 indicating the end-of-text addition
	 * to the alphabet.
	 * The value returned includes the end-of-text symbol in the transform
	 * when the length of the array is one more than the length of the
	 * sequence associated with this suffix array.
	 * @param bwt the array to store the BWT (an array whose length is
	 *        the length of the sequence if the end-of-text symbol does
	 *        not appear in the BWT and one more than the length of the
	 *        sequence if the end-of-text symbol does appear in the
	 *        BWT)
	 * @return the index for the sorted permutation that matches the sequence
	 */
	public int getBWT(byte[] bwt) {
	    int result = 0;
	    boolean mode = (bwt.length == sequenceLength);
	    if (mode) {
		for (int i = 1; i < array.length; i++) {
		    int index = array[i];
		    if (index == 0) {
			result = i-1;
			bwt[i-1] = sequence[sequenceLength-1];
		    } else {
			bwt[i-1] = sequence[index-1];
		    }
		}
	    } else {
		for (int i = 0; i < array.length; i++) {
		    int index = array[i];
		    if (index == 0) {
			result = i;
			bwt[i] = -1;
		    } else {
			bwt[i] = sequence[index-1];
		    }
		}
	    }
	    return result;
	}


	/**
	 * Compute the inverse Burrows-Wheeler transform.
	 * When the length of of the BWT array is one more than the
	 * length of the result array, the BTW array is assumed to
	 * contain an end-of-text symbol (-1 for this case), and the
	 * index parameter is ignored. If the two arrays have the same
	 * length, all symbols in the BWT array must be in the alphabet
	 * and the index must be provided (it will be the value returned
	 * by a call to {@link #getBWT(byte[])}).
	 * @param bwt the Burrows-Wheeler transform
	 * @param result the inverse of the Burrons-Wheeler transform
	 * @param index the index parameter for the Burrows-Wheeler transform
	 * @param n the size of the alphabet.
	 * @exception IllegalArgumentException bwt and result have inconsistent
	 *            lengths
	 */
	public static void inverseBWT(byte[] bwt, byte[] result,
				      int index, int n)
	    throws IllegalArgumentException
	{
	    if (bwt.length == result.length) {
		int[] K = new int[n];
		int[] C = new int[result.length];
		int[] M = new int[n];
		for (int i = 0; i < result.length; i++) {
		    C[i] = K[bwt[i]];
		    K[bwt[i]] = K[bwt[i]] + 1;
		}
		int sum = 0;
		for (int i = 0; i < n; i++) {
		    M[i] = sum;
		    sum = sum + K[i];
		}
		for (int j = result.length-1; j >= 0; j--) {
		    result[j] = bwt[index];
		    index = C[index] + M[bwt[index]];
		}
	    } else if (bwt.length == result.length + 1) {
		int[] K = new int[n];
		int[] C = new int[result.length];
		int[] M = new int[n];
		int off = 1;
		for (int i = 0; i < result.length; i++) {
		    int ii = i + 1;
		    if (bwt[ii] == -1) {
			C[i] = K[bwt[0]];
			K[bwt[0]] = K[bwt[0]] + 1;
			index = i;
		    } else {
			C[i] = K[bwt[ii]];
			K[bwt[ii]] = K[bwt[ii]] + 1;
		    }
		}
		int sum = 0;
		for (int i = 0; i < n; i++) {
		    M[i] = sum;
		    sum = sum + K[i];
		}
		for (int j = result.length-1; j >= 0; j--) {
		    int ii = index + 1;
		    if (bwt[ii] == -1) {
			result[j] = bwt[0];
			index = C[index] + M[bwt[0]];
		    } else {
			result[j] = bwt[ii];
			index = C[index] + M[bwt[ii]];
		    }
		}
	    } else {
		throw new IllegalArgumentException(errorMsg("BWTlength"));
	    }
	}

	@Override
	protected void fillLCPArray(int[] ourlcpArray, int[] rank) {
	    int k = 0;
	    int n = sequenceLength;
	    for (int i = 0; i < n; i++) {
		if (rank[i] == n) {
		    k = 0;
		    continue;
		}
		int j = array[rank[i]+1];
		while (i+k < n && j+k < n &&
		       sequence[i+k] == sequence[j+k]) {
		    k++;
		}
		ourlcpArray[rank[i]+1] = k;
		if (k > 0) k--;
	    }
	}

	@Override
	protected int commonPrefixLength(int index1, int index2) {
	    int sum = 0;
	    while (index1 < sequenceLength && index2 < sequenceLength
		   && sequence[index1++] == sequence[index2++]) {
		sum++;
	    }
	    return sum;
	}

	private int[] makeSuffixArray() {
	    IntComparator ic = new IntComparator() {
		    public int compare(int index1, int index2) {
			int limit = sequenceLength - index1;
			int olimit = sequenceLength -index2;
			int xlimit = limit;
			if (limit > olimit) xlimit = olimit;
			for (int i = 0; i < xlimit; i++) {
			    if ((0xFF & sequence[index1+i]) <
				(0xFF & sequence[index2+i]))
				return -1;
			    if ((0xFF & sequence[index1+i]) >
				(0xFF & sequence[index2+i]))
				return 1;
			}
			if (limit < olimit) return -1;
			if (limit > olimit) return 1;
			return 0;
		    }
		};
	    int slenp1 = sequenceLength + 1;
	    int[] suffixArray = new int[slenp1];
	    for (int i = 0; i < slenp1; i++) {
		suffixArray[i] = i;
	    }
	    PrimArrays.sort(suffixArray, ic);
	    return suffixArray;
	}

	private IntComparator ic = new IntComparator() {
		public int compare(int index1, int index2) {
		    int limit = sequenceLength - index1;
		    int olimit = sequenceLength -index2;
		    int xlimit = limit;
		    if (limit > olimit) xlimit = olimit;
		    for (int i = 0; i < xlimit; i++) {
			if ((0xFF & sequence[index1+i]) <
			    (0xFF & sequence[index2+i])) return -1;
			if ((0xFF & sequence[index1+i]) >
			    (0xFF & sequence[index2+i])) return 1;
		    }
		    if (limit < olimit) return -1;
		    if (limit > olimit) return 1;
		    return 0;
		}
	    };

	private int[] guess(int[] bucketSizes, boolean[] inS, int[] tails) {
	    int[] guessed = new int[slenp1];
	    Arrays.fill(guessed, -1);
	    for (int i = 0; i < sequenceLength; i++) {
		if (i == 0 || !inS[i] || inS[i-1]) continue;
		int element = 0xFF & sequence[i];
		guessed[tails[element]] = i;
		tails[element] -= 1;
	    }
	    guessed[0] = sequenceLength;
	    return guessed;
	}

	// note: the array heads will be modified. The array guessed is used
	// for both input and output.
	private void induceSortL(int[] guessed, int[] bucketSizes,
				 int[] heads, boolean[] inS)
	{
	    for (int i = 0; i < guessed.length; i++) {
		if (guessed[i] == -1) continue;
		int j = guessed[i]-1;
		if (j < 0) continue;
		if (inS[j]) continue;
		int element = 0xFF & sequence[j];
		guessed[heads[element]] = j;
		heads[element]++;
	    }
	}

	// note: the array tails will be modified. The array guessed is used
	// for both input and output.
	private void induceSortS(int[] guessed, int[] bucketSizes,
				 int[] tails, boolean[] inS)
	{
	    for (int i = guessed.length-1; i > -1; i--) {
		int j = guessed[i] -1;
		if (j < 0) continue;
		if (!inS[j]) continue;
		int element = 0xFF & sequence[j];
		guessed[tails[element]] = j;
		tails[element]--;
	    }
	}

	static class SummaryResults {
	    int[] summarySequence;
	    int summaryAlphabetSize;
	    int[] summaryOffsets;
	    SummaryResults(int[] seq, int size, int[] offsets) {
		summarySequence = seq;
		summaryAlphabetSize = size;
		summaryOffsets = offsets;
	    }
	}

	private SummaryResults summarize(int[] guessed, boolean[] inS) {
	    int[] lmsNames = new int[slenp1];
	    Arrays.fill(lmsNames, -1);
	    int current = 0;
	    lmsNames[guessed[0]] = current;
	    int last = guessed[0];
	    for (int i = 1; i < guessed.length; i++) {
		int element = guessed[i];
		if (element == 0 || !inS[element] || inS[element-1]) continue;
		int index1 = last;
		int index2 = element;
		if (index1 == sequenceLength || index2 == sequenceLength) {
		    current++;
		    last = element;
		    lmsNames[element] = current;
		    continue;
		}
		boolean lms1 = (index1 == 0)? false:
		    ((inS[index1] && !inS[index1-1])? true:false);
		boolean lms2 = (index2 == 0)? false:
		    ((inS[index2] && !inS[index2-1])? true:false);
		if (lms1 != lms2) {
		    current++;
		    last = element;
		    lmsNames[element] = current;
		    continue;
		}
		if (sequence[index1] != sequence[index2]) {
		    current++;
		    last = element;
		    lmsNames[element] = current;
		    continue;
		}
		index1++;
		index2++;
		while (true) {
		    lms1 = (index1 == 0)? false:
			((inS[index1] && !inS[index1-1])? true:false);
		    lms2 = (index2 == 0)? false:
			((inS[index2] && !inS[index2-1])? true:false);
		    if (lms1 && lms2) {
			break;
		    }
		    if (lms1 != lms2) {
			current++;
			break;
		    }
		    if (sequence[index1] != sequence[index2]) {
			current++;
			break;
		    }
		    index1++;
		    index2++;
		}
		last = element;
		lmsNames[element] = current;
	    }
	    int limit = 0;
	    for (int i = 0; i < slenp1; i++) {
		if (lmsNames[i] == -1) continue;
		limit++;
	    }
	    int[] summarySequence = new int[limit];
	    int[] summaryOffsets = new int[limit];
	    int j = 0;

	    for (int i = 0; i < slenp1; i++) {
		if (lmsNames[i] == -1) continue;
		summaryOffsets[j] = i;
		summarySequence[j] = lmsNames[i];
		j++;
	    }
	    lmsNames = null;
	    int summaryAlphabetSize = current + 1;
	    return new SummaryResults(summarySequence, summaryAlphabetSize,
				      summaryOffsets);
	}

	private int[] makeSummarySuffixes(int[] summarySequence,
					  int summaryAlphabetSize)
	{
	    if (summaryAlphabetSize == summarySequence.length) {
		int[] result = new int[summarySequence.length + 1];
		Arrays.fill(result, -1);
		result[0] = summarySequence.length;
		for (int i = 0; i < summarySequence.length; i++) {
		    int element = summarySequence[i];
		    result[element+1] = i;
		}
		return result;
	    } else {
		SuffixArray result = new
		    SuffixArray.Integer(summarySequence, summaryAlphabetSize);
		return result.array;
	    }
	}

	private int[] actualLMSSort(int[] bucketSizes, boolean[] inS,
				    int[] tails,
				    int[] summarySuffixes,
				    int[] summaryOffsets)
	{
	    array = new int[slenp1];
	    Arrays.fill(array, -1);
	    for (int i = summarySuffixes.length-1; i > 1; i--) {
		int sIndex = summaryOffsets[summarySuffixes[i]];
		int bIndex = 0xFF & sequence[sIndex];

		array[tails[bIndex]] = sIndex;
		tails[bIndex]--;
	    }
	    array[0] = sequenceLength;
	    return array;
	}

	/**
	 * Constructor.
	 * The maximum reasonable value of n should be no higher than the
	 * length of the sequence and frequently much lower.
	 * The sequence must not be changed after this constructor is
	 * called as long as this suffix array is used.
	 * @param sequence a sequence of bytes whose values are in the
	 *        range [0,n)
	 * @param n the size of an alphabet encoded as values in [0,n)
	 *        where n is positive and no larger than 255
	 * @exception IllegalArgumentException an argument was out of range
	 */
	public UnsignedByte(byte[] sequence, int n) {
	    if (n < 0 || n > 255) {
		throw new IllegalArgumentException(errorMsg("argsOutOfRange"));
	    }
	    this.sequence = sequence;
	    sequenceLength = sequence.length;
	    slenp1 = sequenceLength+1;
	    if (sequenceLength < 2) {
		try {
		    if (sequenceLength == 0) {
			array = Cloner.makeClone(SuffixArray.result0);
		    } else if (sequenceLength == 1) {
			array = Cloner.makeClone(SuffixArray.result1);
		    }
		} catch(CloneNotSupportedException e) {
		    throw new UnexpectedExceptionError(e);
		}
		return;
	    } else if (sequenceLength < 22) {
		array = makeSuffixArray();
		return;
	    }

	    boolean[] inS = new boolean[slenp1];
	    inS[sequenceLength] = true;
	    if (sequenceLength > 0) {
		for (int i = sequenceLength-2; i > -1; i--) {
		    if (sequence[i] == sequence[i+1] && inS[i+1]) {
			inS[i] = true;
		    } else if ((0xFF & sequence[i]) <
			       (0xFF & sequence[i+1])) {
			inS[i] = true;
		    }
		}
	    }
	    int[] bucketSizes = new int[n];
	    for (int i = 0; i < sequenceLength; i++) {
		int element = 0xFF & sequence[i];
		if (element < 0 || element >= n) {
		    throw new
			IllegalArgumentException(errorMsg("notInAlphabet", i));
		}
		bucketSizes[element]++;
	    }
	    int[] heads = new int[n];
	    int[] tails = new int[n];
	    int ind = 1;
	    for (int i = 0; i < n; i++) {
		heads[i] = ind;
		int size = bucketSizes[i];
		ind += size;
		tails[i] = ind-1;
	    }

	    try {
		int[] guessed = guess(bucketSizes, inS,
				      Cloner.makeClone(tails));
		induceSortL(guessed, bucketSizes, Cloner.makeClone(heads), inS);
		induceSortS(guessed, bucketSizes, Cloner.makeClone(tails), inS);
		SummaryResults summary = summarize(guessed, inS);
		guessed = null;
		int[] summarySuffixes =
		    makeSummarySuffixes(summary.summarySequence,
					summary.summaryAlphabetSize);

		array = actualLMSSort(bucketSizes, inS, Cloner.makeClone(tails),
				      summarySuffixes, summary.summaryOffsets);
		induceSortL(array, bucketSizes, heads, inS);
		induceSortS(array, bucketSizes, tails, inS);
	    } catch (CloneNotSupportedException e) {
		throw new UnexpectedExceptionError(e);
	    }
	}

	/**
	 * Constructor for precomputed suffix arrays.
	 * The maximum reasonable value of n should be no higher than the
	 * length of the sequence and frequently much lower.
	 * The sequence must not be changed after this constructor is
	 * called as long as this suffix array is used.
	 * <P>
	 * The suffix array passed as the third argument must have one
	 * more element than the sequence.
	 * <P>
	 * This constructor is intended for the case where a suffix
	 * array was previously computed and saved in a file. The caller
	 * must assure that the suffix array was computed for the
	 * sequence and that the alphabets are the same.
	 * @param sequence a sequence of integers whose values are in the
	 *        range [0,n)
	 * @param sarray the suffix array
	 * @exception IllegalArgumentException n is out of range or
	 *            the suffix array is not compatible with the sequence.
	 */
	public UnsignedByte(byte[] sequence, int[] sarray)
	    throws IllegalArgumentException
	{
	    if (sarray.length != sequence.length + 1) {
		throw new IllegalArgumentException("seqArrayNotCompatible");
	    }
	    this.sequence = sequence;
	    this.array = sarray;
	    sequenceLength = sequence.length;
	    slenp1 = sequence.length+1;
	}
    }

    /**
     * Class providing a suffix array for byte arrays containing UTF-8
     * encoded characters. The constructor sets the size of the alphabet
     * to an appropriate value for UTF-8.  The sequence is treated as
     * a sequence of unsigned bytes, but several additional methods are
     * added to allow lookups where a substring is specified by providing
     * a {@link java.lang.String} instead of an array of bytes.
     * The methods that use least common prefixes compute lengths in
     * bytes rather than the number of UTF-8 characters in a prefix.
     * <P>
     * This class is provided because of the widespread use of UTF-8.
     */
    public static final class UTF extends SuffixArray.UnsignedByte {
	// per-byte value
	private static final int ALPHABET_SIZE = 0xF8;
	private static final java.lang.String UTF8 = "UTF-8";


	/**
	 * Find the index into the sequence associated with a suffix
	 * array for an arbitrary instance of a subsequence, with the
	 * subsequence specified by a string.
	 * <P>
	 * Using an LCP-LR table (created by calling
	 * {@link SuffixArray#useLCPLR()}) will change with time
	 * complexity of this method from O(m log n) to O(m + log n),
	 * where m is the length of a subsequence and n is the length
	 * of the sequence array. These, however, are worst-case numbers:
	 * while it can take m steps for a comparison function to
	 * determine that two suffixes differ, the comparison will stop
	 * at the first step at which the suffixes actually differ:
	 * the difference in running time in practice is data-set
	 * dependent.
	 * @param subsequence the subsequence.
	 * @return the index into the sequence; -1 if the subsequence
	 *         cannot be found
	 * @see SuffixArray#getArray()
	 * @see #getSequence()
	 * @see #useLCPLR()
	 */
	public int findInstance(java.lang.String subsequence) {
	    try {
		return findInstance(subsequence.getBytes(UTF8));
	    } catch (UnsupportedEncodingException e) {
		// UTF-8 is always supported
		throw new UnexpectedExceptionError(e);
	    }
	}

	/**
	 * Find the suffix-array index of an arbitrary instance of
	 * a subsequence with the subsequence specified by a string.
	 * This method finds the index into a suffix array corresponding to
	 * a subsequence.
	 * @param subsequence the subsequence.
	 * @return the index into the suffix array; -1 if the subsequence
	 *         cannot be found
	 * @see SuffixArray#getArray()
	 */
	public int findSubsequence(java.lang.String subsequence) {
	    try {
		return findSubsequence(subsequence.getBytes(UTF8));
	    } catch (UnsupportedEncodingException e) {
		// UTF-8 is always supported
		throw new UnexpectedExceptionError(e);
	    }
	}

	/**
	 * Find a subsequence that is specified by a string.
	 * This method finds the index into a suffix array corresponding to
	 * a subsequence.
	 * @param subsequence the subsequence.
	 * @param keyflag true if the highest index should be returned; false
	 *        if the lowest index should be returned.
	 * @return the index into the suffix array; -1 if the subsequence
	 *         cannot be found
	 * @see SuffixArray#getArray()
	 */
	public int findSubsequence(java.lang.String subsequence,
				   boolean keyflag) {
	    try {
		return findSubsequence(subsequence.getBytes(UTF8), keyflag);
	    } catch (UnsupportedEncodingException e) {
		// UTF-8 is always supported
		throw new UnexpectedExceptionError(e);
	    }
	}

	/**
	 * Find all instances of a subsequence, with the subsequence
	 * specified by a string.
	 * @param subsequence the subsequence
	 * @return the subsequences corresponding to a range in the
	 *         suffix array
	 * @see #getSequence()
	 */
	public Range findRange(java.lang.String subsequence) {
	    try {
		return findRange(subsequence.getBytes(UTF8));
	    } catch (UnsupportedEncodingException e) {
		// UTF-8 is always supported
		throw new UnexpectedExceptionError(e);
	    }
	}


	/**
	 * Constructor.
	 * The sequence must not be changed after this constructor is
	 * called as long as this suffix array is used.
	 * @param sequence a sequence of bytes whose values form a
	 *        sequence of UTF-8-encoded characters

	 */
	public UTF(byte[] sequence) {
	    super(sequence, ALPHABET_SIZE);
	}

	/**
	 * Constructor for precomputed suffix arrays.
	 * The maximum reasonable value of n should be no higher than the
	 * length of the sequence and frequently much lower.
	 * The sequence must not be changed after this constructor is
	 * called as long as this suffix array is used.
	 * <P>
	 * The suffix array passed as the third argument must have one
	 * more element than the sequence.
	 * <P>
	 * This constructor is intended for the case where a suffix
	 * array was previously computed and saved in a file. The caller
	 * must assure that the suffix array was computed for the
	 * sequence and that the alphabets are the same.
	 * @param sequence a sequence of integers whose values are in the
	 *        range [0,n)
	 * @param sarray the suffix array
	 * @exception IllegalArgumentException n is out of range or
	 *            the suffix array is not compatible with the sequence.
	 */
	public UTF(byte[] sequence, int[] sarray)
	    throws IllegalArgumentException
	{
	    super(sequence, sarray);
	}
    }



    /**
     * Class providing a suffix array for String sequences
     */
    public static final class String extends SuffixArray {

	java.lang.String string;
	char[] sequence = null;
	int sequenceLength;
	HashMap<Character,Character> map = null;

	/**
	 * Get the sequence associated with this suffix array.
	 * @return the sequence that this suffix array describes
	 */
	public java.lang.String getSequence() {return string;}

	private int findSubsequenceLCPLR(char[] sarray, int start, int end)
	{
	    if (start >= end) return -1;
	    int low = 1;
	    int high = array.length-1;
	    int s = start;
	    int indl = array[low];
	    int indh = array[high];
	    while (s < end-1
		   && indl < sequenceLength && indh < sequenceLength
		   && (sarray[s] == sequence[indl])
		   && (sarray[s] == sequence[indh])) {
		s++; indl++; indh++;
	    }
	    int s2 = s;
	    int s3 = s;
	    while (indh < sequenceLength  && s2 < end-1 &&
		   (sarray[s2] == sequence[indh])) {
		s2++; indh++;
	    }
	    if (indh == sequenceLength) return -1;
	    if (s2 < end && sarray[s2] > sequence[indh]) return -1;
	    if (s2 == end-1 && sarray[s2] == sequence[indh]) return high;
	    if (s2 < end-1 && indh == sequenceLength - 1
		&& sequence[indh] == sarray[s2]) {
		return -1;
	    }
	    while (indl < sequenceLength  && s3 < end-1 &&
		   (sarray[s3] == sequence[indl])) {
		s3++; indl++;
	    }
	    if (s3 < end && indl < sequenceLength &&
		sarray[s3] < sequence[indl]) {
		return -1;
	    }
	    if (s3 == end-1 && indl != sequenceLength
		&& sarray[s3] == sequence[indl]) {
		return low;
	    }
	    boolean right = (s2 <= s3);
	    if (right) {
		s = s3;
	    } else {
		s = s2;
	    }
	    int k = s - start;
	    int test = -2;	// Signal that test was not done.
	    while ((high - low) > 1) {
		int middle = (low + high) >>> 1;
		int ind = array[middle] + k;
		char val = (ind >= sequenceLength)? 0xffff: sequence[ind];
		char key = sarray[s];
		if (right) {
		    if (k < LCP_L[middle]) {
			low = middle;
		    } else if (k > LCP_L[middle]) {
			high = middle;
			right = true;
		    } else {
			test = ((val == key)? 0:
				((val < key || val == -0xffff)? -1: 1));
			if (test < 0) {
			    low = middle;
			} else if (test > 0) {
			    high = middle;
			    right = false;
			} else {
			    while (test == 0 && s < end-1) {
				k++; ind++; s++;
				if (ind >= sequenceLength) {
				    test = -1;
				    break;
				}
				val = sequence[ind];
				key = sarray[s];
				test = ((val == key)? 0:
					((val < key || val == 0xffff)? -1: 1));
			    }
			    if (test < 0) {
				low = middle;
			    } else if (test > 0) {
				high = middle;
				right = false;
			    } else if (s == end-1) {
				return middle;
			    } else {
				throw new IllegalStateException();
			    }
			}
		    }
		} else {
		    if (k < LCP_R[middle]) {
			high = middle;
		    } else if (k > LCP_R[middle]) {
			low = middle;
			right = false;
		    } else {
			test = ((val == key)? 0:
				((val < key || val == 0xffff)? -1: 1));
			if (test < 0) {
			    low = middle;
			    right = true;
			} else if (test > 0) {
			    high = middle;
			} else {
			    while (test == 0 && s < end-1) {
				k++; ind++; s++;
				if (ind == sequenceLength) {
				    test = -1;
				    break;
				}
				val = sequence[ind];
				key = sarray[s];
				test = ((val == key)? 0:
					((val < key || val == 0xffff)? -1: 1));
			    }
			    if (test < 0) {
				low = middle;
				right = true;
			    } else if (test > 0) {
				high = middle;
			    } else if (s == end-1) {
				return middle;
			    } else {
				throw new IllegalStateException
				    ("search failed but test was zero");
			    }
			}
		    }
		}
	    }
	    if (test == -2) {
		test = 0;
		if (right) {
		    int ind = array[low];
		    for (int i = start; i < end; i++) {
			if (ind >= sequenceLength) {
			    test = -1;
			    break;
			}
			char val = sequence[ind++];
			test = (val < sarray[i] || val == 0xffff)? -1: 1;
			if (test != 0) break;
		    }
		    if (test == 0) return low;
		} else {
		    int ind = array[high];
		    for (int i = start; i < end; i++) {
			if (ind >= sequenceLength) {
			    test = -1;
			    break;
			}
			char val = sequence[ind++];
			test = (val < sarray[i] || val == 0xffff)? -1: 1;
			if (test != 0) break;
		    }
		    if (test == 0) return high;
		}
	    }
	    if (test < 0) {
		// test high
		int ind = array[high];
		for (int i = start; i < end; i++) {
		    if (ind >= sequenceLength) return -1;
		    char val = sequence[ind++];
		    if (val != sarray[i]) return -1;
		}
		return high;
	    } else if (test > 0) {
		// test low
		int ind = array[low];
		for (int i = start; i < end; i++) {
		    if (ind >= sequenceLength) return -1;
		    char val = sequence[ind++];
		    if (val != sarray[i]) return -1;
		}
		return low;
	    }
	    return -1;
	}

	class FindComparator implements IntComparator {
	    int start;
	    int end;
	    int limit;

	    char[] sarray;
	    FindComparator(char[] sarray, int start, int end) {
		this.sarray = sarray;
		this.start = start;
		this.limit = end - start;
	    }

	    public int compare(int o1, int o2) {
		for (int i = 0; i < limit; i++) {
		    if (o1 + i >= sequenceLength) return -1;
		    char val1 = sequence[o1 + i];
		    char val2 = sarray[i];
		    if (val1 < val2) return -1;
		    if (val1 > val2) return 1;
		}
		return 0;
	    }
	}

	/**
	 * Find the index into the sequence associated with a suffix
	 * array for an arbitrary instance of a subsequence.
	 * <P>
	 * Using an LCP-LR table (created by calling
	 * {@link SuffixArray#useLCPLR()}) will change with time
	 * complexity of this method from O(m log n) to O(m + log n),
	 * where m is the length of a subsequence and n is the length
	 * of the sequence array. These, however, are worst-case numbers:
	 * while it can take m steps for a comparison function to
	 * determine that two suffixes differ, the comparison will stop
	 * at the first step at which the suffixes actually differ:
	 * the difference in running time in practice is data-set
	 * dependent.
	 * @param subsequence the subsequence.
	 * @return the index into the sequence; -1 if the subsequence
	 *         cannot be found
	 * @see SuffixArray#getArray()
	 * @see #getSequence()
	 * @see #useLCPLR()
	 */
	public int findInstance(java.lang.String subsequence) {
	    int ind = findSubsequence(subsequence);
	    if (ind == -1) return -1;
	    return array[ind];
	}

	/**
	 * Find the index into the sequence associated with a suffix
	 * array for an arbitrary instance of a subsequence given a
	 * starting and ending index into a string containing the
	 * subsequence.
	 * <P>
	 * Using an LCP-LR table (created by calling
	 * {@link SuffixArray#useLCPLR()}) will change with time
	 * complexity of this method from O(m log n) to O(m + log n),
	 * where m is the length of a subsequence and n is the length
	 * of the sequence array. These, however, are worst-case numbers:
	 * while it can take m steps for a comparison function to
	 * determine that two suffixes differ, the comparison will stop
	 * at the first step at which the suffixes actually differ:
	 * the difference in running time in practice is data-set
	 * dependent.
	 * @param subsequence  array containing the subsequence.
	 * @param start the starting index in the subsequence array (inclusive)
	 * @param end the ending index in the subsequence array (exclusive)
	 * @return the index into the sequence; -1 if the subsequence
	 *         cannot be found
	 * @see SuffixArray#getArray()
	 * @see #getSequence()
	 * @see #useLCPLR()
	 */
	public int findInstance(java.lang.String subsequence,
				int start, int end)
	{
	    int ind = findSubsequence(subsequence, start, end);
	    if (ind == -1) return -1;
	    return array[ind];
	}

	/**
	 * Find the suffix-array index of an arbitrary instance of
	 * a subsequence.
	 * This method finds the index into a suffix array corresponding to
	 * a subsequence.
	 * @param subsequence the subsequence.
	 * @return the index into the suffix array; -1 if the subsequence
	 *         cannot be found
	 * @see SuffixArray#getArray()
	 */
	public int findSubsequence(java.lang.String subsequence) {
	    return findSubsequence(subsequence, 0, subsequence.length());
	}

	/**
	 * Find the suffix-array index of an arbitrary instance of a
	 * subsequence given a starting index and ending index for
	 * the subsequence.
	 * This method finds the index into a suffix array corresponding to
	 * a subsequence. The subsequence consists of the elements of
	 * the string with a starting index named start and and
	 * ending index named end.
	 * @param subsequence the string containing the subsequence
	 * @param start the starting index in the subsequence string (inclusive)
	 * @param end the ending index in the subsequence string (exclusive)
	 * @return the index into the suffix array; -1 if the subsequence
	 *         cannot be found
	 * @see SuffixArray#getArray()
	 */
	public int findSubsequence(java.lang.String subsequence, int start,
				   int end)
	{
	    if (start >= end) return -1;
	    int first = 1;
	    int last = array.length;
	    CharSequence cseq = subsequence.subSequence(start, end);
	    int slen = cseq.length();
	    char[] sarray = new char[slen];
	    for (int i = 0; i < slen; i++) {
		sarray[i] = cseq.charAt(i);
	    }
	    if (map != null) {
		for (int i = 0; i < sarray.length; i++) {
		    Character ch = map.get(sarray[i]);
		    if (ch == null) {
			return -1;
		    }
		    sarray[i] = ch;
		}
	    }
	    if (LCP_L != null && LCP_R != null) {
		return findSubsequenceLCPLR(sarray, start, end);
	    } else {
		FindComparator c = new FindComparator(sarray, 0, slen);
		int result = PrimArrays.binarySearch(array, first, last, 0, c);
		return (result < 0)? -1: result;
	    }
	}

	/**
	 * Get the char array representing this object's sequence.
	 * The  array that must not be modified.
	 * @return the array of characters for the string used to
	 *         create this suffix array
	 *
	 */
	public char[] getSequenceArray() {return sequence;}

	/**
	 * Find a subsequence.
	 * This method finds the index into a suffix array corresponding to
	 * a subsequence.
	 * @param subsequence the subsequence.
	 * @param keyflag true if the highest index should be returned; false
	 *        if the lowest index should be returned.
	 * @return the index into the suffix array; -1 if the subsequence
	 *         cannot be found
	 * @see SuffixArray#getArray()
	 */
	public int findSubsequence(java.lang.String subsequence,
				   boolean keyflag)
	{
	    return findSubsequence(subsequence, 0, subsequence.length(),
				   keyflag);
	}

	/**
	 * Find a subsequence given a starting index and ending index for
	 * the subsequence.
	 * This method finds the index into a suffix array corresponding to
	 * a subsequence. The subsequence consists of the elements of
	 * the array sarray with a starting index named start and and
	 * ending index named end.
	 * @param subsequence the subsequence
	 * @param start the starting index in the subsequence array (inclusive)
	 * @param end the ending index in the subsequence array (exclusive)
	 * @param keyflag true if the highest index should be returned; false
	 *        if the lowest index should be returned.
	 * @return the index into the suffix array; -1 if the subsequence
	 *         cannot be found
	 * @see SuffixArray#getArray()
	 */
	public int findSubsequence(java.lang.String subsequence, int start,
				   int end,
				   boolean keyflag)
	{
	    if (start >= end) return -1;
	    int first = 1;
	    int last = array.length;
	    CharSequence cseq = subsequence.subSequence(start, end);
	    int slen = cseq.length();
	    char[] sarray = new char[slen];
	    for (int i = 0; i < slen; i++) {
		sarray[i] = cseq.charAt(i);
	    }
	    if (map != null) {
		for (int i = 0; i < sarray.length; i++) {
		    Character ch = map.get(sarray[i]);
		    if (ch == null) {
			return -1;
		    }
		    sarray[i] = ch;
		}
	    }
	    FindComparator c = new FindComparator(sarray, 0, slen);
	    int i1 = PrimArrays.binarySearch(array, first, last, -1,
						     c, false);
	    if (i1 < 0) return -1;
	    int i2 = PrimArrays.binarySearch(array, i1, last, -1,
					     c, true);
	    if (i2 < 0) return -1;
	    return keyflag? i2: i1;
	}

	/**
	 * Find all instances of a subsequence.
	 * @param subsequence the subsequence
	 * @return the subsequences corresponding to a range in the
	 *         suffix array
	 * @see #getSequence()
	 */
	public Range findRange(java.lang.String subsequence) {
	    return findRange(subsequence, 0, subsequence.length());
	}

	/**
	 * Find  all instances of a subsequence given a starting index
	 * and ending index.
	 * @param subsequence the subsequence array.
	 * @param start the starting index in the subsequence array (inclusive)
	 * @param end the ending index in the subsequence array (exclusive)
	 * @return the subsequences corresponding to a range in the
	 *         suffix array
	 * @see #getSequence()
	 */
	public Range findRange(java.lang.String subsequence, int start,
				   int end)
	{
	    if (start >= end) return new OurRange(0, 0, 0);
	    int first = 1;
	    int last = array.length;
	    CharSequence cseq = subsequence.subSequence(start, end);
	    int slen = cseq.length();
	    char[] sarray = new char[slen];
	    for (int i = 0; i < slen; i++) {
		sarray[i] = cseq.charAt(i);
	    }
	    if (map != null) {
		for (int i = 0; i < sarray.length; i++) {
		    Character ch = map.get(sarray[i]);
		    if (ch == null) {
			return new OurRange(0, 0, 0);
		    }
		    sarray[i] = ch;
		}
	    }
	    FindComparator c = new FindComparator(sarray, 0, slen);
	    int i1 = PrimArrays.binarySearch(array, first, last, -1,
					     c, false);
	    if (i1 < 0) return new OurRange(0, 0, 0);
	    int i2 = PrimArrays.binarySearch(array, i1, last, -1,
					     c, true);
	    if (i2 < 0) return new OurRange(0, 0, 0);
	    return new OurRange(slen, i1, i2+1);
	}

	/**
	 * Get the Burrows-Wheeler Transform  (BWT) of the sequence associated
	 * with this suffix array, with 0xffff indicating the end-of-text
	 * addition to the alphabet.
	 * The value returned includes the end-of-text symbol in the transform
	 * when the length of the array is one more than the length of the
	 * sequence associated with this suffix array.
	 * @param bwt the array to store the BWT (an array whose length is
	 *        the length of the sequence if the end-of-text symbol does
	 *        not appear in the BWT and one more than the length of the
	 *        sequence if the end-of-text symbol does appear in the
	 *        BWT)
	 * @return the index for the sorted permutation that matches the
	 *          sequence
	 */
	public int getBWT(char[] bwt) {
	    int result = 0;
	    boolean mode = (bwt.length == sequenceLength);
	    if (mode) {
		for (int i = 1; i < array.length; i++) {
		    int index = array[i];
		    if (index == 0) {
			result = i-1;
			bwt[i-1] = sequence[sequenceLength-1];
		    } else {
			bwt[i-1] = sequence[index-1];
		    }
		}
	    } else {
		for (int i = 0; i < array.length; i++) {
		    int index = array[i];
		    if (index == 0) {
			result = i;
			bwt[i] = (char)0xffff;
		    } else {
			bwt[i] = sequence[index-1];
		    }
		}
	    }
	    return result;
	}

	/**
	 * Compute the inverse Burrows-Wheeler transform.
	 * When the length of of the BWT array is one more than the
	 * length of the result array, the BTW array is assumed to
	 * contain an end-of-text symbol (-1 for this case), and the
	 * index parameter is ignored. If the two arrays have the same
	 * length, all symbols in the BWT array must be in the alphabet
	 * and the index must be provided (it will be the value returned
	 * by a call to {@link #getBWT(char[])}).
	 * @param bwt the Burrows-Wheeler transform
	 * @param result the inverse of the Burrons-Wheeler transform
	 * @param index the index parameter for the Burrows-Wheeler transform
	 * @param n the size of the alphabet.
	 * @exception IllegalArgumentException bwt and result have inconsistent
	 *            lengths
	 */
	public static void inverseBWT(char[] bwt, char[] result,
				      int index, int n)
	    throws IllegalArgumentException
	{
	    if (bwt.length == result.length) {
		int[] K = new int[n];
		int[] C = new int[result.length];
		int[] M = new int[n];
		for (int i = 0; i < result.length; i++) {
		    C[i] = K[bwt[i]];
		    K[bwt[i]] = K[bwt[i]] + 1;
		}
		int sum = 0;
		for (int i = 0; i < n; i++) {
		    M[i] = sum;
		    sum = sum + K[i];
		}
		for (int j = result.length-1; j >= 0; j--) {
		    result[j] = bwt[index];
		    index = C[index] + M[bwt[index]];
		}
	    } else if (bwt.length == result.length + 1) {
		int[] K = new int[n];
		int[] C = new int[result.length];
		int[] M = new int[n];
		int off = 1;
		for (int i = 0; i < result.length; i++) {
		    int ii = i + 1;
		    if (bwt[ii] == (char)0xffff) {
			C[i] = K[bwt[0]];
			K[bwt[0]] = K[bwt[0]] + 1;
			index = i;
		    } else {
			C[i] = K[bwt[ii]];
			K[bwt[ii]] = K[bwt[ii]] + 1;
		    }
		}
		int sum = 0;
		for (int i = 0; i < n; i++) {
		    M[i] = sum;
		    sum = sum + K[i];
		}
		for (int j = result.length-1; j >= 0; j--) {
		    int ii = index + 1;
		    if (bwt[ii] == (char)0xffff) {
			result[j] = bwt[0];
			index = C[index] + M[bwt[0]];
		    } else {
			result[j] = bwt[ii];
			index = C[index] + M[bwt[ii]];
		    }
		}
	    } else {
		throw new IllegalArgumentException(errorMsg("BWTlength"));
	    }
	}

	@Override
	protected void fillLCPArray(int[] ourlcpArray, int[] rank) {
	    int k = 0;
	    int n = sequenceLength;
	    for (int i = 0; i < n; i++) {
		if (rank[i] == n) {
		    k = 0;
		    continue;
		}
		int j = array[rank[i]+1];
		while (i+k < n && j+k < n &&
		       sequence[i+k] == sequence[j+k]) {
		    k++;
		}
		ourlcpArray[rank[i]+1] = k;
		if (k > 0) k--;
	    }
	}

	@Override
	protected int commonPrefixLength(int index1, int index2) {
	    int sum = 0;
	    while (index1 < sequenceLength && index2 < sequenceLength
		   && sequence[index1++] == sequence[index2++]) {
		sum++;
	    }
	    return sum;
	}

	private int[] makeSuffixArray() {
	    IntComparator ic = new IntComparator() {
		    public int compare(int index1, int index2) {
			int limit = sequenceLength - index1;
			int olimit = sequenceLength -index2;
			int xlimit = limit;
			if (limit > olimit) xlimit = olimit;
			for (int i = 0; i < xlimit; i++) {
			    if (sequence[index1+i] < sequence[index2+i])
				return -1;
			    if (sequence[index1+i] > sequence[index2+i])
				return 1;
			}
			if (limit < olimit) return -1;
			if (limit > olimit) return 1;
			return 0;
		    }
		};
	    int slenp1 = sequenceLength + 1;
	    int[] suffixArray = new int[slenp1];
	    for (int i = 0; i < slenp1; i++) {
		suffixArray[i] = i;
	    }
	    PrimArrays.sort(suffixArray, ic);
	    return suffixArray;
	}

	private IntComparator ic = new IntComparator() {
		public int compare(int index1, int index2) {
		    int limit = sequenceLength - index1;
		    int olimit = sequenceLength -index2;
		    int xlimit = limit;
		    if (limit > olimit) xlimit = olimit;
		    for (int i = 0; i < xlimit; i++) {
			if (sequence[index1+i] < sequence[index2+i]) return -1;
			if (sequence[index1+i] > sequence[index2+i]) return 1;
		    }
		    if (limit < olimit) return -1;
		    if (limit > olimit) return 1;
		    return 0;
		}
	    };

	private int[] guess(int[] bucketSizes, boolean[] inS, int[] tails) {
	    int[] guessed = new int[slenp1];
	    Arrays.fill(guessed, -1);
	    for (int i = 0; i < sequenceLength; i++) {
		if (i == 0 || !inS[i] || inS[i-1]) continue;
		int element = sequence[i];
		guessed[tails[element]] = i;
		tails[element] -= 1;
	    }
	    guessed[0] = sequenceLength;
	    return guessed;
	}

	// note: the array heads will be modified. The array guessed is used
	// for both input and output.
	private void induceSortL(int[] guessed, int[] bucketSizes,
				 int[] heads, boolean[] inS)
	{
	    for (int i = 0; i < guessed.length; i++) {
		if (guessed[i] == -1) continue;
		int j = guessed[i]-1;
		if (j < 0) continue;
		if (inS[j]) continue;
		int element = sequence[j];
		guessed[heads[element]] = j;
		heads[element]++;
	    }
	}

	// note: the array tails will be modified. The array guessed is used
	// for both input and output.
	private void induceSortS(int[] guessed, int[] bucketSizes,
				 int[] tails, boolean[] inS)
	{
	    for (int i = guessed.length-1; i > -1; i--) {
		int j = guessed[i] -1;
		if (j < 0) continue;
		if (!inS[j]) continue;
		int element = sequence[j];
		guessed[tails[element]] = j;
		tails[element]--;
	    }
	}

	static class SummaryResults {
	    int[] summarySequence;
	    int summaryAlphabetSize;
	    int[] summaryOffsets;
	    SummaryResults(int[] seq, int size, int[] offsets) {
		summarySequence = seq;
		summaryAlphabetSize = size;
		summaryOffsets = offsets;
	    }
	}

	private SummaryResults summarize(int[] guessed, boolean[] inS) {
	    int[] lmsNames = new int[slenp1];
	    Arrays.fill(lmsNames, -1);
	    int current = 0;
	    lmsNames[guessed[0]] = current;
	    int last = guessed[0];
	    for (int i = 1; i < guessed.length; i++) {
		int element = guessed[i];
		if (element == 0 || !inS[element] || inS[element-1]) continue;

		int index1 = last;
		int index2 = element;
		if (index1 == sequenceLength || index2 == sequenceLength) {
		    current++;
		    last = element;
		    lmsNames[element] = current;
		    continue;
		}
		boolean lms1 = (index1 == 0)? false:
		    ((inS[index1] && !inS[index1-1])? true:false);
		boolean lms2 = (index2 == 0)? false:
		    ((inS[index2] && !inS[index2-1])? true:false);
		if (lms1 != lms2) {
		    current++;
		    last = element;
		    lmsNames[element] = current;
		    continue;
		}
		if (sequence[index1] != sequence[index2]) {
		    current++;
		    last = element;
		    lmsNames[element] = current;
		    continue;
		}
		index1++;
		index2++;
		while (true) {
		    lms1 = (index1 == 0)? false:
			((inS[index1] && !inS[index1-1])? true:false);
		    lms2 = (index2 == 0)? false:
			((inS[index2] && !inS[index2-1])? true:false);
		    if (lms1 && lms2) {
			break;
		    }
		    if (lms1 != lms2) {
			current++;
			break;
		    }
		    if (sequence[index1] != sequence[index2]) {
			current++;
			break;
		    }
		    index1++;
		    index2++;
		}
		last = element;
		lmsNames[element] = current;
	    }
	    int limit = 0;
	    for (int i = 0; i < slenp1; i++) {
		if (lmsNames[i] == -1) continue;
		limit++;
	    }
	    int[] summarySequence = new int[limit];
	    int[] summaryOffsets = new int[limit];
	    int j = 0;

	    for (int i = 0; i < slenp1; i++) {
		if (lmsNames[i] == -1) continue;
		summaryOffsets[j] = i;
		summarySequence[j] = lmsNames[i];
		j++;
	    }
	    lmsNames = null;
	    int summaryAlphabetSize = current + 1;
	    return new SummaryResults(summarySequence, summaryAlphabetSize,
				      summaryOffsets);
	}

	private int[] makeSummarySuffixes(int[] summarySequence,
					  int summaryAlphabetSize)
	{
	    if (summaryAlphabetSize == summarySequence.length) {
		int[] result = new int[summarySequence.length + 1];
		Arrays.fill(result, -1);
		result[0] = summarySequence.length;
		for (int i = 0; i < summarySequence.length; i++) {
		    int element = summarySequence[i];
		    result[element+1] = i;
		}
		return result;
	    } else {
		SuffixArray result = new
		    SuffixArray.Integer(summarySequence, summaryAlphabetSize);
		return result.array;
	    }
	}

	private int[] actualLMSSort(int[] bucketSizes, boolean[] inS,
				    int[] tails,
				    int[] summarySuffixes,
				    int[] summaryOffsets)
	{
	    array = new int[slenp1];
	    Arrays.fill(array, -1);
	    for (int i = summarySuffixes.length-1; i > 1; i--) {
		int sIndex = summaryOffsets[summarySuffixes[i]];
		int bIndex = sequence[sIndex];

		array[tails[bIndex]] = sIndex;
		tails[bIndex]--;
	    }
	    array[0] = sequenceLength;
	    return array;
	}


	/**
	 * Constructor given an alphabet.
	 * <P>
	 * When an alphabet is used, it will be mapped to a sequence
	 * of integers, starting at 0, and in the order implied by the
	 * set's iterator.  To force the use of a particular order,
	 * use {@link java.util.LinkedHashSet} or
	 * {@link java.util.TreeSet}.
	 * @param string a string representing a sequence of characters.
	 * @param alphabet the characters that are used in the string
	 * @exception IllegalArgumentException a character in the string
	 *            was not in the alphabet
	 */
	public String(java.lang.String string,
		       Set<java.lang.Character> alphabet)
	    throws IllegalArgumentException
	{
	    int n = alphabet.size();
	    map = new HashMap<Character,Character>(n);
	    char index = (char)0;
	    for (Character ch: alphabet) {
		map.put(ch, (index++));
	    }
	    init(string, n);
	}

	/**
	 * Constructor given an alphabet size.
	 * The maximum reasonable value of n should be no higher than the
	 * length of the sequence and frequently much lower.
	 * @param string a string representing a sequence of characters.
	 * @param n the size of an alphabet encoded as values in [0,n)
	 *        where n is positive and no larger than 0xFFFF
	 *        (2<sup>16</sup> - 2)
	 * @exception IllegalArgumentException n is out of range.
	 */
	public String(java.lang.String string, int n)
	    throws IllegalArgumentException
	{
	    if (n <= 0 || n > 0xFFFF) {
		throw new IllegalArgumentException(errorMsg("argsOutOfRange"));
	    }
	    init(string, n);
	}

	/**
	 * Constructor given an alphabet size and initial character count.
	 * The initial n entries in the alphabet
	 * will the integer value of characters whose numeric values
	 * are in the set [0,n) in lexical order. If the extend argument
	 * is true, the value of n will be increased with additional
	 * characters having a value that depends on their order of
	 * occurrence in the string.
	 * <P>
	 * As an example of when setting the extend argument to true
	 * is useful is HTML text. If one is primarily interested in
	 * searching for markup, one can set the estimated alphabet
	 * size to 128 and the alphabet will be extended by one for
	 * each additional Unicode character that is used. The integer
	 * codes for these additional characters will be set by their
	 * order of occurrence in the string.
	 * <P>
	 * @param string a string representing a sequence of characters.
	 * @param n the estimated size of an alphabet encoded as values in [0,n)
	 *        where n is positive and no larger than 0xFFFF
	 *        (2<sup>16</sup> - 1)
	 * @param extend true if the alphabet should be extended to include
	 *        all characters in the string; false otherwise
	 * @exception IllegalArgumentException n is out of range.
	 */
	public String(java.lang.String string, int n, boolean extend) {
	    if (n <= 0 || n > 0xFFFF) {
		throw new IllegalArgumentException(errorMsg("argsOutOfRange"));
	    }
	    sequence = string.toCharArray();
	    if (extend) {
		int m = string.length();
		if (m == 0) m = 1;
		int est = Math.abs(Math.round((float)MathOps.log2(n, 0.1)))
		    * Math.abs(Math.round((float)MathOps.log2(m, 0.1)));
		map = new HashMap<Character,Character>(n + est);
		char index = 0;
		for (int i = 0; i < n; i++) {
		    map.put((char)i, index++);
		}
		for (char ch: sequence) {
		    if (ch < n)	continue;
		    if (!map.containsKey(ch)) {
			map.put(ch, (index++));
		    }
		}
		n = map.size();
	    }
	    init(string, n);
	}

	private void init(java.lang.String string, int n)
	    throws IllegalArgumentException
	{
	    this.string = string;
	    sequence = string.toCharArray();
	    sequenceLength = sequence.length;
	    if (map != null) {
		for (int i = 0; i < sequenceLength; i++) {
		    Character ch = map.get(sequence[i]);
		    if (ch == null) {
			throw new IllegalArgumentException
			    (errorMsg("notInAlphabet", i));
		    }
		    sequence[i] = ch;
		}
	    }
	    slenp1 = sequenceLength+1;
	    if (sequenceLength < 2) {
		try {
		    if (sequenceLength == 0) {
			array = Cloner.makeClone(SuffixArray.result0);
		    } else if (sequenceLength == 1) {
			array = Cloner.makeClone(SuffixArray.result1);
		    }
		} catch(CloneNotSupportedException e) {
		    throw new UnexpectedExceptionError(e);
		}
		return;
	    } else if (sequenceLength < 22) {
		array = makeSuffixArray();
		return;
	    }

	    boolean[] inS = new boolean[slenp1];
	    inS[sequenceLength] = true;
	    if (sequenceLength > 0) {
		for (int i = sequenceLength-2; i > -1; i--) {
		    if (sequence[i] == sequence[i+1] && inS[i+1]) {
			inS[i] = true;
		    } else if (sequence[i] < sequence[i+1]) {
			inS[i] = true;
		    }
		}
	    }
	    int[] bucketSizes = new int[n];
	    for (int i = 0; i < sequenceLength; i++) {
		int element = sequence[i];
		if (element < 0 || element >= n) {
		    throw new
			IllegalArgumentException(errorMsg("notInAlphabet", i));
		}
		bucketSizes[element]++;
	    }
	    int[] heads = new int[n];
	    int[] tails = new int[n];
	    int ind = 1;
	    for (int i = 0; i < n; i++) {
		heads[i] = ind;
		int size = bucketSizes[i];
		ind += size;
		tails[i] = ind-1;
	    }

	    try {
		int[] guessed = guess(bucketSizes, inS,
				      Cloner.makeClone(tails));
		induceSortL(guessed, bucketSizes, Cloner.makeClone(heads), inS);
		induceSortS(guessed, bucketSizes, Cloner.makeClone(tails), inS);
		SummaryResults summary = summarize(guessed, inS);
		guessed = null;
		int[] summarySuffixes =
		    makeSummarySuffixes(summary.summarySequence,
					summary.summaryAlphabetSize);

		array = actualLMSSort(bucketSizes, inS, Cloner.makeClone(tails),
				      summarySuffixes, summary.summaryOffsets);
		induceSortL(array, bucketSizes, heads, inS);
		induceSortS(array, bucketSizes, tails, inS);
	    } catch (CloneNotSupportedException e) {
		throw new UnexpectedExceptionError(e);
	    }
	}
    }

    /**
     * Class providing a suffix array for Object sequences.
     */
    public static final class Array<T extends Object> extends SuffixArray {

	T[] sequence = null;
	int sequenceLength;
	HashMap<T,java.lang.Integer> map = null;

	/**
	 * Get the sequence associated with this suffix array.
	 * @return the sequence that this suffix array describes
	 */
	public T[] getSequence() {return sequence;}

	private int iget(T object) {
	    java.lang.Integer integer = map.get(object);
	    if (integer == null) return -1;
	    return integer;
	}

	private int findSubsequenceLCPLR(T[] sarray, int start, int end)
	{
	    if (start >= end) return -1;
	    int low = 1;
	    int high = array.length-1;
	    int s = start;
	    int indl = array[low];
	    int indh = array[high];
	    while (s < end-1
		   && indl < sequenceLength && indh < sequenceLength
		   && (iget(sarray[s]) == iget(sequence[indl]))
		   && (iget(sarray[s]) == iget(sequence[indh]))) {
		s++; indl++; indh++;
	    }
	    int s2 = s;
	    int s3 = s;
	    while (indh < sequenceLength  && s2 < end-1 &&
		   (iget(sarray[s2]) == iget(sequence[indh]))) {
		s2++; indh++;
	    }
	    if (indh == sequenceLength) return -1;
	    if (s2 < end && iget(sarray[s2]) > iget(sequence[indh])) return -1;
	    if (s2 == end-1 && iget(sarray[s2]) == iget(sequence[indh])) {
		return high;
	    }
	    if (s2 < end-1 && indh == sequenceLength - 1
		&& iget(sequence[indh]) == iget(sarray[s2])) {
		return -1;
	    }
	    while (indl < sequenceLength  && s3 < end-1 &&
		   (iget(sarray[s3]) == iget(sequence[indl]))) {
		s3++; indl++;
	    }
	    if (s3 < end && indl < sequenceLength &&
		iget(sarray[s3]) < iget(sequence[indl])) {
		return -1;
	    }
	    if (s3 == end-1 && indl != sequenceLength
		&& iget(sarray[s3]) == iget(sequence[indl])) {
		return low;
	    }
	    boolean right = (s2 <= s3);
	    if (right) {
		s = s3;
	    } else {
		s = s2;
	    }
	    int k = s - start;
	    int test = -2;	// Signal that test was not done.
	    while ((high - low) > 1) {
		int middle = (low + high) >>> 1;
		int ind = array[middle] + k;
		int val = (ind >= sequenceLength)? -1: iget(sequence[ind]);
		int key = iget(sarray[s]);
		if (right) {
		    if (k < LCP_L[middle]) {
			low = middle;
		    } else if (k > LCP_L[middle]) {
			high = middle;
			right = true;
		    } else {
			test = ((val == key)? 0: ((val < key)? -1: 1));
			if (test < 0) {
			    low = middle;
			} else if (test > 0) {
			    high = middle;
			    right = false;
			} else {
			    while (test == 0 && s < end-1) {
				k++; ind++; s++;
				if (ind >= sequenceLength) {
				    test = -1;
				    break;
				}
				val = iget(sequence[ind]);
				key = iget(sarray[s]);
				test = ((val == key)? 0: ((val < key)? -1: 1));
			    }
			    if (test < 0) {
				low = middle;
			    } else if (test > 0) {
				high = middle;
				right = false;
			    } else if (s == end-1) {
				return middle;
			    } else {
				throw new IllegalStateException();
			    }
			}
		    }
		} else {
		    if (k < LCP_R[middle]) {
			high = middle;
		    } else if (k > LCP_R[middle]) {
			low = middle;
			right = false;
		    } else {
			test = ((val == key)? 0: ((val < key)? -1: 1));
			if (test < 0) {
			    low = middle;
			    right = true;
			} else if (test > 0) {
			    high = middle;
			} else {
			    while (test == 0 && s < end-1) {
				k++; ind++; s++;
				if (ind == sequenceLength) {
				    test = -1;
				    break;
				}
				val = iget(sequence[ind]);
				key = iget(sarray[s]);
				test = ((val == key)? 0: ((val < key)? -1: 1));
			    }
			    if (test < 0) {
				low = middle;
				right = true;
			    } else if (test > 0) {
				high = middle;
			    } else if (s == end-1) {
				return middle;
			    } else {
				throw new IllegalStateException
				    ("search failed but test was zero");
			    }
			}
		    }
		}
	    }
	    if (test == -2) {
		test = 0;
		if (right) {
		    int ind = array[low];
		    for (int i = start; i < end; i++) {
			if (ind >= sequenceLength) {
			    test = -1;
			    break;
			}
			int val = iget(sequence[ind++]);
			test = (val < iget(sarray[i]))? -1: 1;
			if (test != 0) break;
		    }
		    if (test == 0) return low;
		} else {
		    int ind = array[high];
		    for (int i = start; i < end; i++) {
			if (ind >= sequenceLength) {
			    test = -1;
			    break;
			}
			int val = iget(sequence[ind++]);
			test = (val < iget(sarray[i]))? -1: 1;
			if (test != 0) break;
		    }
		    if (test == 0) return high;
		}
	    }
	    if (test < 0) {
		// test high
		int ind = array[high];
		for (int i = start; i < end; i++) {
		    if (ind >= sequenceLength) return -1;
		    int val = iget(sequence[ind++]);
		    if (val != iget(sarray[i])) return -1;
		}
		return high;
	    } else if (test > 0) {
		// test low
		int ind = array[low];
		for (int i = start; i < end; i++) {
		    if (ind >= sequenceLength) return -1;
		    int val = iget(sequence[ind++]);
		    if (val != iget(sarray[i])) return -1;
		}
		return low;
	    }
	    return -1;
	}

	class FindComparator implements IntComparator {
	    int start;
	    int end;
	    int limit;

	    T[] sarray;
	    FindComparator(T[] sarray, int start, int end) {
		this.sarray = sarray;
		this.start = start;
		this.limit = end - start;
	    }

	    public int compare(int o1, int o2) {
		for (int i = 0; i < limit; i++) {
		    if (o1 + i >= sequenceLength) return -1;
		    java.lang.Integer val1 = map.get(sequence[o1 + i]);
		    java.lang.Integer val2 = map.get(sarray[i]);
		    if (val1 == null || val2 == null) {
			throw new IllegalArgumentException();
		    }
		    if (val1 < val2) return -1;
		    if (val1 > val2) return 1;
		}
		return 0;
	    }
	}

	/**
	 * Find the index into the sequence associated with a suffix
	 * array for an arbitrary instance of a subsequence.
	 * <P>
	 * Using an LCP-LR table (created by calling
	 * {@link SuffixArray#useLCPLR()}) will change with time
	 * complexity of this method from O(m log n) to O(m + log n),
	 * where m is the length of a subsequence and n is the length
	 * of the sequence array. These, however, are worst-case numbers:
	 * while it can take m steps for a comparison function to
	 * determine that two suffixes differ, the comparison will stop
	 * at the first step at which the suffixes actually differ:
	 * the difference in running time in practice is data-set
	 * dependent.
	 * @param subsequence the subsequence.
	 * @return the index into the sequence; -1 if the subsequence
	 *         cannot be found
	 * @see SuffixArray#getArray()
	 * @see #getSequence()
	 * @see #useLCPLR()
	 */
	public int findInstance(T[] subsequence) {
	    int ind = findSubsequence(subsequence);
	    if (ind == -1) return -1;
	    return array[ind];
	}

	/**
	 * Find the index into the sequence associated with a suffix
	 * array for an arbitrary instance of a subsequence given a
	 * starting and ending index into an array containing the
	 * subsequence.
	 * <P>
	 * Using an LCP-LR table (created by calling
	 * {@link SuffixArray#useLCPLR()}) will change with time
	 * complexity of this method from O(m log n) to O(m + log n),
	 * where m is the length of a subsequence and n is the length
	 * of the sequence array. These, however, are worst-case numbers:
	 * while it can take m steps for a comparison function to
	 * determine that two suffixes differ, the comparison will stop
	 * at the first step at which the suffixes actually differ:
	 * the difference in running time in practice is data-set
	 * dependent.
	 * @param subsequence  array containing the subsequence.
	 * @param start the starting index in the subsequence array (inclusive)
	 * @param end the ending index in the subsequence array (exclusive)
	 * @return the index into the sequence; -1 if the subsequence
	 *         cannot be found
	 * @see SuffixArray#getArray()
	 * @see #getSequence()
	 * @see #useLCPLR()
	 */
	public int findInstance(T[] subsequence, int start, int end) {
	    int ind = findSubsequence(subsequence, start, end);
	    if (ind == -1) return -1;
	    return array[ind];
	}

	/**
	 * Find the suffix-array index of an arbitrary instance of
	 * a subsequence.
	 * This method finds the index into a suffix array corresponding to
	 * a subsequence.
	 * @param subsequence the subsequence.
	 * @return the index into the suffix array; -1 if the subsequence
	 *         cannot be found
	 * @see SuffixArray#getArray()
	 */
	public int findSubsequence(T[] subsequence) {
	    return findSubsequence(subsequence, 0, subsequence.length);
	}


	/**
	 * Find the suffix-array index of an arbitrary instance of a
	 * subsequence given a starting index and ending index for
	 * the subsequence.
	 * This method finds the index into a suffix array corresponding to
	 * a subsequence. The subsequence consists of the elements of
	 * the array sarray with a starting index named start and and
	 * ending index named end.
	 * @param sarray the subsequence array.
	 * @param start the starting index in the subsequence array (inclusive)
	 * @param end the ending index in the subsequence array (exclusive)
	 * @return the index into the suffix array; -1 if the subsequence
	 *         cannot be found
	 * @see SuffixArray#getArray()
	 */
	public int findSubsequence(T[] sarray, int start,
				   int end)
	{

	    if (start >= end) return -1;
	    int first = 1;
	    int last = array.length;
	    if (LCP_L != null && LCP_R != null) {
		return findSubsequenceLCPLR(sarray, start, end);
	    } else {
		FindComparator c = new FindComparator(sarray, start, end);
		int result = PrimArrays.binarySearch(array, first, last, -1, c);
		return (result < 0)? -1: result;
	    }
	}

	/**
	 * Find a subsequence.
	 * This method finds the index into a suffix array corresponding to
	 * a subsequence.
	 * @param subsequence the subsequence.
	 * @param keyflag true if the highest index should be returned; false
	 *        if the lowest index should be returned.
	 * @return the index into the suffix array; -1 if the subsequence
	 *         cannot be found
	 * @see SuffixArray#getArray()
	 */
	public int findSubsequence(T[] subsequence,
				   boolean keyflag)
	{
	    return findSubsequence(subsequence, 0, subsequence.length, keyflag);
	}

	/**
	 * Find a subsequence given a starting index and ending index for
	 * the subsequence.
	 * This method finds the index into a suffix array corresponding to
	 * a subsequence. The subsequence consists of the elements of
	 * the array sarray with a starting index named start and and
	 * ending index named end.
	 * @param subsequence the subsequence array.
	 * @param start the starting index in the subsequence array (inclusive)
	 * @param end the ending index in the subsequence array (exclusive)
	 * @param keyflag true if the highest index should be returned; false
	 *        if the lowest index should be returned.
	 * @return the index into the suffix array; -1 if the subsequence
	 *         cannot be found
	 * @see SuffixArray#getArray()
	 */
	public int findSubsequence(T[] subsequence,
				   int start,
				   int end,
				   boolean keyflag)
	{
	    if (start >= end) return -1;
	    int first = 1;
	    int last = array.length;
	    FindComparator c = new FindComparator(subsequence, start, end);
	    int i1 = PrimArrays.binarySearch(array, first, last, -1,
						     c, false);
	    if (i1 < 0) return -1;
	    int i2 = PrimArrays.binarySearch(array, i1, last, -1,
					     c, true);
	    if (i2 < 0) return -1;
	    return keyflag? i2: i1;
	}

	/**
	 * Find all instances of a subsequence.
	 * @param subsequence the subsequence
	 * @return the subsequences corresponding to a range in the
	 *         suffix array
	 * @see #getSequence()
	 */
	public Range findRange(T[] subsequence) {
	    return findRange(subsequence, 0, subsequence.length);
	}

	/**
	 * Find  all instances of a subsequence given a starting index
	 * and ending index.
	 * @param sarray the subsequence array.
	 * @param start the starting index in the subsequence array (inclusive)
	 * @param end the ending index in the subsequence array (exclusive)
	 * @return the subsequences corresponding to a range in the
	 *         suffix array
	 * @see #getSequence()
	 */
	public Range findRange(T[] sarray, int start, int end)
	{
	    if (start >= end) return new OurRange(0, 0, 0);
	    int first = 1;
	    int last = array.length;
	    FindComparator c = new FindComparator(sarray, start, end);
	    int i1 = PrimArrays.binarySearch(array, first, last, -1,
					     c, false);
	    if (i1 < 0) return new OurRange(0, 0, 0);
	    int i2 = PrimArrays.binarySearch(array, i1, last, -1,
					     c, true);
	    if (i2 < 0) return new OurRange(0, 0, 0);
	    return new OurRange(end - start, i1, i2+1);
	}


	/**
	 * Get the Burrows-Wheeler Transform  (BWT) of the sequence associated
	 * with this suffix array
	 * <P>
	 * Unlike the other subclasses of SuffixArray, in this
	 * case the transform is not of the same type as the
	 * elements of the original sequence. Instead, int-valued
	 * codes are used, as an explicit alphabet is needed in
	 * any case. The end-of-text symbol is -1.
	 * <P>
	 * The value returned includes the end-of-text symbol in the transform
	 * when the length of the array is one more than the length of the
	 * sequence associated with this suffix array.
	 * @param bwt the array to store the BWT (an array whose length is
	 *        the length of the sequence if the end-of-text symbol does
	 *        not appear in the BWT and one more than the length of the
	 *        sequence if the end-of-text symbol does appear in the
	 *        BWT)
	 * @return the index for the sorted permutation that matches the
	 *         sequence
	 */
	public int getBWT(int[] bwt) {
	    int result = 0;
	    boolean mode = (bwt.length == sequenceLength);
	    if (mode) {
		for (int i = 1; i < array.length; i++) {
		    int index = array[i];
		    if (index == 0) {
			result = i-1;
			bwt[i-1] = map.get(sequence[sequenceLength-1]);
		    } else {
			bwt[i-1] = map.get(sequence[index-1]);
		    }
		}
	    } else {
		for (int i = 0; i < array.length; i++) {
		    int index = array[i];
		    if (index == 0) {
			result = i;
			bwt[i] = -1;
		    } else {
			bwt[i] = map.get(sequence[index-1]);
		    }
		}
	    }
	    return result;
	}

	/**
	 * Compute the inverse Burrows-Wheeler transform.
	 * <P>
	 * Unlike the other subclasses of SuffixArray, in this
	 * case the transform is not of the same type as the
	 * elements of the original sequence. Instead, int-valued
	 * codes are used, as an explicit alphabet is needed in
	 * any case.  The end-of-text symbol is -1.
	 * <P>
	 * When the length of of the BWT array is one more than the
	 * length of the result array, the BTW array is assumed to
	 * contain an end-of-text symbol (-1 for this case), and the
	 * index parameter is ignored. If the two arrays have the same
	 * length, all symbols in the BWT array must be in the alphabet
	 * and the index must be provided (it will be the value returned
	 * by a call to {@link #getBWT(int[])}).
	 * @param bwt the Burrows-Wheeler transform
	 * @param result the inverse of the Burrons-Wheeler transform
	 * @param index the index parameter for the Burrows-Wheeler transform
	 * @param alphabet the alphabet
	 * @exception IllegalArgumentException bwt and result have inconsistent
	 *            lengths
	 */
	public static <T extends Object> void inverseBWT(int[] bwt,
							 T[] result,
							 int index,
							 Set<T>alphabet)
	    throws IllegalArgumentException
	{
	    int n = alphabet.size();
	    HashMap<java.lang.Integer,T> imap =
		new
		HashMap<java.lang.Integer,T>(alphabet.size());
	    int ind = 0;
	    for (T obj: alphabet) {
		imap.put(java.lang.Integer.valueOf(ind++), obj);
	    }
	    if (bwt.length == result.length) {
		int[] K = new int[n];
		int[] C = new int[result.length];
		int[] M = new int[n];
		for (int i = 0; i < result.length; i++) {
		    C[i] = K[bwt[i]];
		    K[bwt[i]] = K[bwt[i]] + 1;
		}
		int sum = 0;
		for (int i = 0; i < n; i++) {
		    M[i] = sum;
		    sum = sum + K[i];
		}
		for (int j = result.length-1; j >= 0; j--) {
		    result[j] = imap.get(bwt[index]);
		    index = C[index] + M[bwt[index]];
		}
	    } else if (bwt.length == result.length + 1) {
		int[] K = new int[n];
		int[] C = new int[result.length];
		int[] M = new int[n];
		int off = 1;
		for (int i = 0; i < result.length; i++) {
		    int ii = i + 1;
		    if (bwt[ii] == -1) {
			C[i] = K[bwt[0]];
			K[bwt[0]] = K[bwt[0]] + 1;
			index = i;
		    } else {
			C[i] = K[bwt[ii]];
			K[bwt[ii]] = K[bwt[ii]] + 1;
		    }
		}
		int sum = 0;
		for (int i = 0; i < n; i++) {
		    M[i] = sum;
		    sum = sum + K[i];
		}
		for (int j = result.length-1; j >= 0; j--) {
		    int ii = index + 1;
		    if (bwt[ii] == -1) {
			result[j] = imap.get(bwt[0]);
			index = C[index] + M[bwt[0]];
		    } else {
			result[j] = imap.get(bwt[ii]);
			index = C[index] + M[bwt[ii]];
		    }
		}
	    } else {
		throw new IllegalArgumentException(errorMsg("BWTlength"));
	    }
	}

	@Override
	protected void fillLCPArray(int[] ourlcpArray, int[] rank) {
	    int k = 0;
	    int n = sequenceLength;
	    for (int i = 0; i < n; i++) {
		if (rank[i] == n) {
		    k = 0;
		    continue;
		}
		int j = array[rank[i]+1];
		while (i+k < n && j+k < n &&
		       sequence[i+k].equals(sequence[j+k])) {
		    k++;
		}
		ourlcpArray[rank[i]+1] = k;
		if (k > 0) k--;
	    }
	}

	@Override
	protected int commonPrefixLength(int index1, int index2) {
	    int sum = 0;
	    while (index1 < sequenceLength && index2 < sequenceLength
		   && sequence[index1++].equals(sequence[index2++])) {
		sum++;
	    }
	    return sum;
	}


	private int[] makeSuffixArray() {
	    IntComparator ic = new IntComparator() {
		    public int compare(int index1, int index2) {
			int limit = sequenceLength - index1;
			int olimit = sequenceLength -index2;
			int xlimit = limit;
			if (limit > olimit) xlimit = olimit;
			for (int i = 0; i < xlimit; i++) {
			    if (map.get(sequence[index1+i]).intValue()
				< (int)map.get(sequence[index2+i]).intValue())
				return -1;
			    if ((int)map.get(sequence[index1+i]).intValue()
				> map.get(sequence[index2+i]).intValue())
				return 1;
			}
			if (limit < olimit) return -1;
			if (limit > olimit) return 1;
			return 0;
		    }
		};
	    int slenp1 = sequenceLength + 1;
	    int[] suffixArray = new int[slenp1];
	    for (int i = 0; i < slenp1; i++) {
		suffixArray[i] = i;
	    }
	    PrimArrays.sort(suffixArray, ic);
	    return suffixArray;
	}

	private IntComparator ic = new IntComparator() {
		public int compare(int index1, int index2) {
		    int limit = sequenceLength - index1;
		    int olimit = sequenceLength -index2;
		    int xlimit = limit;
		    if (limit > olimit) xlimit = olimit;
		    for (int i = 0; i < xlimit; i++) {
			if (map.get(sequence[index1+i]).intValue()
			    < map.get(sequence[index2+i]).intValue()) return -1;
			if (map.get(sequence[index1+i]).intValue()
			    > map.get(sequence[index2+i]).intValue()) return 1;
		    }
		    if (limit < olimit) return -1;
		    if (limit > olimit) return 1;
		    return 0;
		}
	    };

	private int[] guess(int[] bucketSizes, boolean[] inS, int[] tails) {
	    int[] guessed = new int[slenp1];
	    Arrays.fill(guessed, -1);
	    for (int i = 0; i < sequenceLength; i++) {
		if (i == 0 || !inS[i] || inS[i-1]) continue;
		int element = map.get(sequence[i]).intValue();
		guessed[tails[element]] = i;
		tails[element] -= 1;
	    }
	    guessed[0] = sequenceLength;
	    return guessed;
	}

	// note: the array heads will be modified. The array guessed is used
	// for both input and output.
	private void induceSortL(int[] guessed, int[] bucketSizes,
				 int[] heads, boolean[] inS)
	{
	    for (int i = 0; i < guessed.length; i++) {
		if (guessed[i] == -1) continue;
		int j = guessed[i]-1;
		if (j < 0) continue;
		if (inS[j]) continue;
		int element = map.get(sequence[j]).intValue();
		guessed[heads[element]] = j;
		heads[element]++;
	    }
	}

	// note: the array tails will be modified. The array guessed is used
	// for both input and output.
	private void induceSortS(int[] guessed, int[] bucketSizes,
				 int[] tails, boolean[] inS)
	{
	    for (int i = guessed.length-1; i > -1; i--) {
		int j = guessed[i] -1;
		if (j < 0) continue;
		if (!inS[j]) continue;
		int element = map.get(sequence[j]).intValue();
		guessed[tails[element]] = j;
		tails[element]--;
	    }
	}

	static class SummaryResults {
	    int[] summarySequence;
	    int summaryAlphabetSize;
	    int[] summaryOffsets;
	    SummaryResults(int[] seq, int size, int[] offsets) {
		summarySequence = seq;
		summaryAlphabetSize = size;
		summaryOffsets = offsets;
	    }
	}

	private SummaryResults summarize(int[] guessed, boolean[] inS) {
	    int[] lmsNames = new int[slenp1];
	    Arrays.fill(lmsNames, -1);
	    int current = 0;
	    lmsNames[guessed[0]] = current;
	    int last = guessed[0];
	    for (int i = 1; i < guessed.length; i++) {
		int element = guessed[i];
		if (element == 0 || !inS[element] || inS[element-1]) continue;
		int index1 = last;
		int index2 = element;
		if (index1 == sequenceLength || index2 == sequenceLength) {
		    current++;
		    last = element;
		    lmsNames[element] = current;
		    continue;
		}
		boolean lms1 = (index1 == 0)? false:
		    ((inS[index1] && !inS[index1-1])? true:false);
		boolean lms2 = (index2 == 0)? false:
		    ((inS[index2] && !inS[index2-1])? true:false);
		if (lms1 != lms2) {
		    current++;
		    last = element;
		    lmsNames[element] = current;
		    continue;
		}
		if (!sequence[index1].equals(sequence[index2])) {
		    current++;
		    last = element;
		    lmsNames[element] = current;
		    continue;
		}
		index1++;
		index2++;
		while (true) {
		    lms1 = (index1 == 0)? false:
			((inS[index1] && !inS[index1-1])? true:false);
		    lms2 = (index2 == 0)? false:
			((inS[index2] && !inS[index2-1])? true:false);
		    if (lms1 && lms2) {
			break;
		    }
		    if (lms1 != lms2) {
			current++;
			break;
		    }
		    if (!sequence[index1].equals(sequence[index2])) {
			current++;
			break;
		    }
		    index1++;
		    index2++;
		}
		last = element;
		lmsNames[element] = current;
	    }
	    int limit = 0;
	    for (int i = 0; i < slenp1; i++) {
		if (lmsNames[i] == -1) continue;
		limit++;
	    }
	    int[] summarySequence = new int[limit];
	    int[] summaryOffsets = new int[limit];
	    int j = 0;

	    for (int i = 0; i < slenp1; i++) {
		if (lmsNames[i] == -1) continue;
		summaryOffsets[j] = i;
		summarySequence[j] = lmsNames[i];
		j++;
	    }
	    lmsNames = null;
	    int summaryAlphabetSize = current + 1;
	    return new SummaryResults(summarySequence, summaryAlphabetSize,
				      summaryOffsets);
	}

	private int[] makeSummarySuffixes(int[] summarySequence,
					  int summaryAlphabetSize)
	{
	    if (summaryAlphabetSize == summarySequence.length) {
		int[] result = new int[summarySequence.length + 1];
		Arrays.fill(result, -1);
		result[0] = summarySequence.length;
		for (int i = 0; i < summarySequence.length; i++) {
		    int element = summarySequence[i];
		    result[element+1] = i;
		}
		return result;
	    } else {
		SuffixArray result = new
		    SuffixArray.Integer(summarySequence, summaryAlphabetSize);
		return result.array;
	    }
	}

	private int[] actualLMSSort(int[] bucketSizes, boolean[] inS,
				    int[] tails,
				    int[] summarySuffixes,
				    int[] summaryOffsets)
	{
	    array = new int[slenp1];
	    Arrays.fill(array, -1);
	    for (int i = summarySuffixes.length-1; i > 1; i--) {
		int sIndex = summaryOffsets[summarySuffixes[i]];
		int bIndex = map.get(sequence[sIndex]).intValue();

		array[tails[bIndex]] = sIndex;
		tails[bIndex]--;
	    }
	    array[0] = sequenceLength;
	    return array;
	}


	/**
	 * Constructor.
	 * The sequence must not be changed after this constructor is
	 * called as long as this suffix array is used.
	 * @param sequence an array representing a sequence of objects.
	 * @param alphabet the objects that make up the sequence
	 * @exception IllegalArgumentException an object in the sequence
	 *            was null or was not in the alphabet
	 */
	public Array (T[] sequence, Set<T>
		       alphabet)
	    throws IllegalArgumentException
	{
	    int n = alphabet.size();
	    map = new HashMap<T,java.lang.Integer>(n);
	    int index = 0;
	    for (T obj: alphabet) {
		map.put(obj, java.lang.Integer.valueOf(index++));
	    }
	    this.sequence = sequence;
	    sequenceLength = sequence.length;
	    init(sequence, n);
	}

	private void init(T[] sequence, int n)
	    throws IllegalArgumentException
	{
	    slenp1 = sequenceLength+1;
	    if (sequenceLength < 2) {
		try {
		    if (sequenceLength == 0) {
			array = Cloner.makeClone(SuffixArray.result0);
		    } else if (sequenceLength == 1) {
			array = Cloner.makeClone(SuffixArray.result1);
		    }
		} catch(CloneNotSupportedException e) {
		    throw new UnexpectedExceptionError(e);
		}
		return;
	    } else if (sequenceLength < 22) {
		array = makeSuffixArray();
		return;
	    }

	    boolean[] inS = new boolean[slenp1];
	    inS[sequenceLength] = true;
	    if (sequenceLength > 0) {
		for (int i = sequenceLength-2; i > -1; i--) {
		    T o1 = sequence[i];
		    T o2 = sequence[i+1];
		    if (o1 == null) throw new IllegalArgumentException
					(errorMsg("nullObject", i));
		    if (o2 == null) throw new IllegalArgumentException
					(errorMsg("nullObject", i+1));
		    java.lang.Integer io1 = map.get(o1);
		    java.lang.Integer io2 = map.get(o2);
		    if (io1 == null) throw new IllegalArgumentException
					(errorMsg("notInAlphabet", i));
		    if (io2 == null) throw new IllegalArgumentException
					(errorMsg("notInAlphabet", i+1));
		    int v1 = io1.intValue();
		    int v2 = io2.intValue();
		    if (v1 == v2 && inS[i+1]) {
			inS[i] = true;
		    } else if (v1 < v2) {
			inS[i] = true;
		    }
		}
	    }
	    int[] bucketSizes = new int[n];
	    for (int i = 0; i < sequenceLength; i++) {
		T element = sequence[i];
		if (element == null) {
		    throw new
			IllegalArgumentException(errorMsg("notInAlphabet", i));
		}
		java.lang.Integer integer = map.get(element);
		if (integer == null) {
		    throw new
			IllegalArgumentException(errorMsg("notInAlphabet", i));
		}
		int ielement = integer;
		bucketSizes[ielement]++;
	    }
	    int[] heads = new int[n];
	    int[] tails = new int[n];
	    int ind = 1;
	    for (int i = 0; i < n; i++) {
		heads[i] = ind;
		int size = bucketSizes[i];
		ind += size;
		tails[i] = ind-1;
	    }

	    try {
		int[] guessed = guess(bucketSizes, inS,
				      Cloner.makeClone(tails));
		induceSortL(guessed, bucketSizes, Cloner.makeClone(heads), inS);
		induceSortS(guessed, bucketSizes, Cloner.makeClone(tails), inS);
		SummaryResults summary = summarize(guessed, inS);
		guessed = null;
		int[] summarySuffixes =
		    makeSummarySuffixes(summary.summarySequence,
					summary.summaryAlphabetSize);

		array = actualLMSSort(bucketSizes, inS, Cloner.makeClone(tails),
				      summarySuffixes, summary.summaryOffsets);
		induceSortL(array, bucketSizes, heads, inS);
		induceSortS(array, bucketSizes, tails, inS);
	    } catch (CloneNotSupportedException e) {
		throw new UnexpectedExceptionError(e);
	    }
	}
    }
}

//  LocalWords:  subsequence indices href pdf Nong Zhang Wai Hong doi
//  LocalWords:  IEEE LCP Toru Gunho Hiroki Arimur Setsuo Arikawa inS
//  LocalWords:  Kunsoo lcpArray substring arrayAndOffset
//  LocalWords:  boolean exbundle SuffixArray lt set's getArray args
//  LocalWords:  getLCP getInverse getSequence getBWT inverseBWT nl
//  LocalWords:  BLOCKQUOTE PRE fillLCPArray ourlcpArray lcpLength
//  LocalWords:  subsequences doNext currentIndex BWT substrings xFF
//  LocalWords:  currentLength hasMore bwt Burrons BWTlength xffff
//  LocalWords:  IllegalArgumentException notInAlphabet nullObject      
//  LocalWords:  findSubsequence errorMsg UtilErrorMsg slenp ourrank
//  LocalWords:  hasInverse clearCachedInverse clearCachedLCP toArray
//  LocalWords:  keyflag sarray findInstance findRange useInverse UTF
//  LocalWords:  useLCP hasLCP useLCPLR clearCachedLCPLR hasLCPLR
//  LocalWords:  getLength UnsupportedOperationException xFFFF xFFFE
//  LocalWords:  lexically subclasses UnsignedShort UnsignedByte
//  LocalWords:  iterator's argsOutOfRange precomputed lookups
//  LocalWords:  seqArrayNotCompatible
