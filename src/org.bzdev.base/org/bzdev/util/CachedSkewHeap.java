package org.bzdev.util;

/**
 * Priority queue based on skew heaps.
 * This class maintains two skew heaps: a 'main' one and a soldiery
 * one for recently added entries.  The subsidiary heap is merged into
 * the main heap when the heap is polled, or when some size limit is
 * reached, leaving an emu subsidiary heap.  Since the subsidiary heap
 * is typically much smaller than the main heap, this speeds up
 * insertions because the time complexity to merge a heap of size n
 * with a heap of size m is log n + log m. whereas the time complexity
 * to add m entries to a heap of size m &lt;&lt; n is approximately m
 * log n.  There is also a cache - a pointer to an entry to which new
 * entries can be safely appended when it can appear after the cache.
 * The cache, if not null, is an entry in the subsidiary heap.
 * <P>
 * To use this class, one should first create a subclass of
 * {@link CachedSkewHeap.Entry} as described in the documentation for
 * that class. If this subclass is named HeapEntry, one then uses that
 * subclass as the type parameter for CachedSkewHeap.
 * For example,
 * <BLOCKQUOTE><PRE><CODE>
 * public class OurHeap extends CachedSkewHeap&lt;HeapEntry&gt; {
 *      protected int
 *      compareEntries(CachedSkewHeap.Entry&lt;HeapEntry&gt; e1,
 *                     CachedSkewHeap.Entry&lt;HeapEntry&gt; e2) {
 *        HeapEntry he1 = (HeapEntry)e1;
 *        HeapEntry he2 = (HeapEntry)e2;
 *        // now compare he1 and he2.
 *        ...
 *      }
 * }
 * </CODE></PRE></BLOCKQUOTE>
 * <P>
 * While some methods are similar to ones provided by
 * {@link java.util.PriorityQueue}, the usage for this class is more
 * restrictive, and in particular it does not provide all the methods
 * one would expect from a class in the Java collections framework.
 * @param <E> the type of a heap entry
 */
public abstract class CachedSkewHeap<E extends CachedSkewHeap.Entry<E>> {

    private int esize = 0;
    private int nesize = 0;

    Entry<E> entries = null;
    Entry<E> newEntries = null; // uses the cache.
    Entry<E> cache = null;

    /**
     * CachedSkewHeap entry.
     * Subclasses will provide the data needed by a comparison
     * function to determine which entry a priority queue should
     * produce first. The name of a subclass should also appear
     * in a type parameter as shown in the following example:
     * <BLOCKQUOTE><PRE><CODE>
     * class TestEntry extends CachedSkewHeap.Entry<TestEntry> {
     *   ...
     * }
     * </CODE></PRE></BLOCKQUOTE>
     * @param <E> the type of a subclass of this class
     */
    public abstract static class Entry<E extends Entry>
    {
	Entry<E> leftPQEntry = null;
	Entry<E> rightPQEntry = null;
	Entry<E> parentPQEntry = null;
    }

    /**
     * Compare two entries for order.
     * The heap will return entries with the lowest order first.
     * @return less than 0, 0, or greater than 0 when entry1's order is
     *         lower than, equal to, or greater than  entry2's order
     *         respectively.
     */
    protected abstract int compareEntries(Entry<E> entry1, Entry<E> entry2);

    /**
     * Check if this heap is empty.
     * @return true if it is empty; false otherwise
     */
    public boolean isEmpty() {
	return entries == null && newEntries == null;
    }


    private void mergeCached(Entry larger) {
	Entry smaller = newEntries;
	// long itest;
	int itest;
	if (larger == null) {
	    return;
	}
	nesize++;
	if (smaller == null) {
	    if (larger != null && larger.leftPQEntry == null) {
		cache = larger;
	    } else {
		cache = null;
	    }
	    newEntries = larger;
	    return;
	}
	if (cache != null) {
	    /*cache.value < larger.value*/ 
	    itest = compareEntries(cache, larger);
	    /*
	    itest = cache.time - larger.time;
	    if (itest == 0 && (cache.tpriority != larger.tpriority)) {
		itest = (cache.tpriority > larger.tpriority)? 1: -1;
	    }
	    if (itest == 0) itest = cache.instance - larger.instance;
	    */
	    if (itest <= 0) {
		cache.leftPQEntry = larger;
		larger.parentPQEntry = cache;
		if (larger.leftPQEntry == null) {
		    cache = larger;
		} else {
		    cache = null;
		}
		return;
	    }
	} else {
	    cache = larger;	// cleared at end if doesn't work.
	}
	Entry tmp;
	/*larger.value <= smaller.value*/
	itest = compareEntries(larger, smaller);
	/*
	itest = larger.time - smaller.time;
	if (itest == 0 && (larger.tpriority != smaller.tpriority)) {
	    itest = (larger.tpriority > smaller.tpriority)? 1: -1;
	}
	if (itest == 0) itest = larger.instance - smaller.instance;
	*/
	if (itest < 0) {
	    tmp = larger;
	    larger = smaller;
	    smaller = tmp;
	}
	Entry last = smaller;
	Entry result = last;
	smaller = smaller.rightPQEntry;
	for(;;) {
	    if (smaller == null) {
		smaller = larger;
		smaller.parentPQEntry = last;
		last.rightPQEntry = last.leftPQEntry;
		last.leftPQEntry = smaller;
		break;
	    }
	    /*larger.value <= tmp.value*/
	    itest = compareEntries(larger, smaller);
	    /*
	    itest = larger.time - smaller.time;
	    if (itest == 0 && (larger.tpriority != smaller.tpriority)) {
		itest = (larger.tpriority > smaller.tpriority)? 1:  -1;
	    }
	    if (itest == 0) itest = larger.instance - smaller.instance;
	    */
	    if (itest < 0) {
		tmp = larger;
		larger = smaller;
		smaller = tmp;
	    }
	    smaller.parentPQEntry = last;
	    last.rightPQEntry = last.leftPQEntry;
	    last.leftPQEntry = smaller;
	    if (larger == null) break;
	    last = smaller;
	    smaller = smaller.rightPQEntry;
	}
	if (cache != null && cache.leftPQEntry != null) cache = null;
	newEntries = result;
	return;
    }

    /**
     * Merge events on the event queue.
     * This method will modify the event queue so that new entries can
     * be added quickly.  It is useful in special cases such as when a
     * potentially large number of new events will be scheduled in
     * order of increasing or decreasing priority level and before the
     * heap is polled.  It has no effect immediately after an event
     * was removed from the event queue, either by calling {@link #poll()}
     * or {@link #remove(Entry)}.
     */
    public void merge() {
	 entries = merge(entries, newEntries);
	 esize += nesize;
	 nesize = 0;
	 newEntries = null;
	 cache = null;
    }

    /**
     * Find the number of entries in this heap.
     * @return the number of entries
     */
    public long size() {
	return esize + nesize;
    }

    /**
     * Find the entry in this heap with the lowest order
     * @return the entry with the lowest priority.
     */
    public E peek() {
	// either entries or newEntries, depending on which has the
	// earliest time stamp; null if the queue is empty.
	if (newEntries == null) return (E)entries;
	if (entries == null) return (E)newEntries;
	int test = compareEntries(entries, newEntries);
	/*
	long test = entries.time - newEntries.time;
	if (test == 0 && (entries.tpriority != newEntries.tpriority)) {
	    test = (entries.tpriority > newEntries.tpriority)? 1: -1;
	}
	if (test == 0) test = entries.instance - newEntries.instance;
	*/
	// boolean flag = entries.value < newEntries.value;
	boolean flag = test <= 0;

	return flag? (E)entries: (E)newEntries;
    }

    /**
     * Find the entry with the lowest order and remove it from the queue.
     * @return the heap entry with the lowest order
     */
    public E poll() {
	merge();
	Entry<E> entry = entries;
	if (entries != null) {
	    esize--;
	    if (entries.leftPQEntry != null) {
		entries.leftPQEntry.parentPQEntry = null;
		if (entries.rightPQEntry != null) {
		    entries.rightPQEntry.parentPQEntry = null;
		}
	    }
	    entries = merge(entries.leftPQEntry, entries.rightPQEntry);
	    entry.leftPQEntry = null;
	    entry.rightPQEntry = null;
	}
	return (E)entry;
    }

    /**
     * Add an entry to this heap.
     * @param entry the entry to add
     * @return true if the entry was added; false otherwise.
     */
    public boolean add(Entry<E> entry) {
	if (entry == null || entry.parentPQEntry != null
			  || entry.leftPQEntry != null
			  || entry.rightPQEntry != null) {
	    return false;
	}
	mergeCached(entry);
	if (entry != cache) {
	    if (nesize > 64 && nesize > (esize >> 4)) {
		merge();
	    }
	}
	return true;
    }

    /**
     * Remove an entry from the heap.
     * <P>
     * The entry must not be one in a different heap.
     * @param entry the entry
     * @return true on success; false otherwise
     */
    public boolean remove(Entry<E> entry) {
	if (entry == null || (entry.parentPQEntry == null
			      && entry.leftPQEntry == null
			      && entry.rightPQEntry == null)) {
	    return false;
	}
	if (entry == entries) {
	    esize--;
	    // if (cache == entry) cache = null;
	    if (entries.leftPQEntry != null) 
		entries.leftPQEntry.parentPQEntry = null;
	    if (entries.rightPQEntry != null)
		entries.rightPQEntry.parentPQEntry = null;
	    entries = merge(entries.leftPQEntry, entries.rightPQEntry);
	    entry.parentPQEntry = null;
	    entry.leftPQEntry = null;
	    entry.rightPQEntry = null;
	} else if (entry == newEntries) {
	    if (cache == entry) cache = null;
	    nesize--;
	    if (newEntries.leftPQEntry != null)
		newEntries.leftPQEntry.parentPQEntry = null;
	    if (newEntries.rightPQEntry != null)
		newEntries.rightPQEntry.parentPQEntry = null;
	    newEntries = merge(newEntries.leftPQEntry, newEntries.rightPQEntry);
	    if (cache != null && cache.leftPQEntry != null) cache = null;
	} else {
	    merge(); // so we can handled esize and nsize properly & clear cache
	    Entry parent = entry.parentPQEntry;
	    if (parent == null) {
		if (entry == entries) {
		    esize--;
		    if (entries.leftPQEntry != null)
			entries.leftPQEntry.parentPQEntry = null;
		    if (entries.rightPQEntry != null)
			entries.rightPQEntry.parentPQEntry = null;
		    entries = merge(entries.leftPQEntry, entries.rightPQEntry);
		    // entry.parentPQEntry = null;
		    entry.leftPQEntry = null;
		    entry.rightPQEntry = null;
		    return true;
		}
		return false;
	    }
	    esize--;
	    // if (cache == entry) cache = parent;
	    if (parent.leftPQEntry == entry) {
		parent.leftPQEntry 
		    = merge(entry.leftPQEntry, entry.rightPQEntry);
		if (parent.leftPQEntry != null) {
		    parent.leftPQEntry.parentPQEntry = parent;
		} else {
		    parent.leftPQEntry = parent.rightPQEntry;
		    parent.rightPQEntry = null;
		}
	    } else {
		parent.rightPQEntry
		    = merge(entry.leftPQEntry, entry.rightPQEntry);
		if (parent.rightPQEntry != null) {
		    parent.rightPQEntry.parentPQEntry = parent;
		}
	    }
	}
	entry.parentPQEntry = null;
	entry.leftPQEntry = null;
	entry.rightPQEntry = null;
	return true;
    }

    private Entry merge(Entry smaller, 
			Entry larger) 
    {
	if (larger == null) {
	    return smaller;
	}
	if (smaller == null) {
	    return larger;
	}
	Entry tmp;
	// long itest;
	int itest;
	boolean test;
	/* larger.value < smaller.value*/
	itest = compareEntries(larger, smaller);
	/*
	itest = larger.time - smaller.time;
	if (itest == 0 && (larger.tpriority != smaller.tpriority)) {
	    itest = (larger.tpriority > smaller.tpriority)? 1: -1;
	}
	if (itest == 0) itest = larger.instance - smaller.instance;
	*/
	if (itest < 0 ) {
	    tmp = larger;
	    larger = smaller;
	    smaller = tmp;
	}
	Entry last = smaller;
	Entry result = last;
	smaller = smaller.rightPQEntry;
	for(;;) {
	    if (smaller == null) {
		smaller = larger;
		smaller.parentPQEntry = last;
		last.rightPQEntry = last.leftPQEntry;
		last.leftPQEntry = smaller;
		break;
	    }
	    // larger.value <= tmp.value
	    itest = compareEntries(larger, smaller);
	    /*
	    itest = larger.time - smaller.time;
	    if (itest == 0 && larger.tpriority != smaller.tpriority) {
		itest = (larger.tpriority > smaller.tpriority)? 1: -1;
	    }
	    if (itest == 0) itest = larger.instance - smaller.instance;
	    */
	    if (itest <= 0) {
		tmp = larger;
		larger = smaller;
		smaller  = tmp;
	    }
	    smaller.parentPQEntry = last;
	    last.rightPQEntry = last.leftPQEntry;
	    last.leftPQEntry = smaller;
	    // merge(smaller, larger) via loop.
	    if (larger == null) break;
	    last = smaller;
	    smaller = smaller.rightPQEntry;
	}
	return result;
    }
}

//  LocalWords:  lt CachedSkewHeap HeapEntry BLOCKQUOTE PRE OurHeap
//  LocalWords:  compareEntries Subclasses TestEntry itest tpriority
//  LocalWords:  tmp newEntries boolean esize nsize parentPQEntry
