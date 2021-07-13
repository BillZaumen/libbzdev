package org.bzdev.devqsim;

class SimulationEventQueue {

    private int esize = 0;
    private int nesize = 0;

    SimulationEvent entries = null;
    SimulationEvent newEntries = null; // uses the cache.
    SimulationEvent cache = null;

    private void mergeCached(SimulationEvent larger) {
	SimulationEvent smaller = newEntries;
	long itest;

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
	    itest = cache.time - larger.time;
	    if (itest == 0 && (cache.tpriority != larger.tpriority)) {
		itest = (cache.tpriority > larger.tpriority)? 1: -1;
	    }
	    if (itest == 0) itest = cache.instance - larger.instance;
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
	SimulationEvent tmp;
	/*larger.value <= smaller.value*/
	itest = larger.time - smaller.time;
	if (itest == 0 && (larger.tpriority != smaller.tpriority)) {
	    itest = (larger.tpriority > smaller.tpriority)? 1: -1;
	}
	if (itest == 0) itest = larger.instance - smaller.instance;
	if (itest < 0) {
	    tmp = larger;
	    larger = smaller;
	    smaller = tmp;
	}
	SimulationEvent last = smaller;
	SimulationEvent result = last;
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
	    itest = larger.time - smaller.time;
	    if (itest == 0 && (larger.tpriority != smaller.tpriority)) {
		itest = (larger.tpriority > smaller.tpriority)? 1:  -1;
	    }
	    if (itest == 0) itest = larger.instance - smaller.instance;
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

     void merge() {
	 entries = merge(entries, newEntries);
	 esize += nesize;
	 nesize = 0;
	 newEntries = null;
	 cache = null;
    }

    SimulationEvent peek() {
	// either entries or newEntries, depending on which has the
	// earliest time stamp; null if the queue is empty.
	if (newEntries == null) return entries;
	if (entries == null) return newEntries;
	long test = entries.time - newEntries.time;
	if (test == 0 && (entries.tpriority != newEntries.tpriority)) {
	    test = (entries.tpriority > newEntries.tpriority)? 1: -1;
	}
	if (test == 0) test = entries.instance - newEntries.instance;
	// boolean flag = entries.value < newEntries.value;
	boolean flag = test <= 0;

	return flag? entries: newEntries;
    }

    SimulationEvent poll() {
	merge();
	SimulationEvent event = entries;
	if (entries != null) {
	    esize--;
	    if (entries.leftPQEntry != null) {
		entries.leftPQEntry.parentPQEntry = null;
		if (entries.rightPQEntry != null) {
		    entries.rightPQEntry.parentPQEntry = null;
		}
	    }
	    entries = merge(entries.leftPQEntry, entries.rightPQEntry);
	    event.leftPQEntry = null;
	    event.rightPQEntry = null;
	}
	return event;
    }

    boolean add(SimulationEvent event) {
	mergeCached(event);
	if (event != cache) {
	    if (nesize > 64 && nesize > (esize >> 4)) {
		merge();
	    }
	}
	return true;
    }

    boolean remove(SimulationEvent event) {
	if (event == null) return true;
	if (event == entries) {
	    esize--;
	    // if (cache == event) cache = null;
	    if (entries.leftPQEntry != null) 
		entries.leftPQEntry.parentPQEntry = null;
	    if (entries.rightPQEntry != null)
		entries.rightPQEntry.parentPQEntry = null;
	    entries = merge(entries.leftPQEntry, entries.rightPQEntry);
	    event.parentPQEntry = null;
	    event.leftPQEntry = null;
	    event.rightPQEntry = null;
	} else if (event == newEntries) {
	    if (cache == event) cache = null;
	    nesize--;
	    if (newEntries.leftPQEntry != null)
		newEntries.leftPQEntry.parentPQEntry = null;
	    if (newEntries.rightPQEntry != null)
		newEntries.rightPQEntry.parentPQEntry = null;
	    newEntries = merge(newEntries.leftPQEntry, newEntries.rightPQEntry);
	    if (cache != null && cache.leftPQEntry != null) cache = null;
	} else {
	    merge(); // so we can handled esize and nsize properly & clear cache
	    SimulationEvent parent = event.parentPQEntry;
	    if (parent == null) {
		if (event == entries) {
		    esize--;
		    if (entries.leftPQEntry != null)
			entries.leftPQEntry.parentPQEntry = null;
		    if (entries.rightPQEntry != null)
			entries.rightPQEntry.parentPQEntry = null;
		    entries = merge(entries.leftPQEntry, entries.rightPQEntry);
		    // event.parentPQEntry = null;
		    event.leftPQEntry = null;
		    event.rightPQEntry = null;
		    return true;
		}
		return false;
	    }
	    esize--;
	    // if (cache == event) cache = parent;
	    if (parent.leftPQEntry == event) {
		parent.leftPQEntry 
		    = merge(event.leftPQEntry, event.rightPQEntry);
		if (parent.leftPQEntry != null) {
		    parent.leftPQEntry.parentPQEntry = parent;
		} else {
		    parent.leftPQEntry = parent.rightPQEntry;
		    parent.rightPQEntry = null;
		}
	    } else {
		parent.rightPQEntry
		    = merge(event.leftPQEntry, event.rightPQEntry);
		if (parent.rightPQEntry != null) {
		    parent.rightPQEntry.parentPQEntry = parent;
		}
	    }
	}
	event.parentPQEntry = null;
	event.leftPQEntry = null;
	event.rightPQEntry = null;
	return true;
    }

    private SimulationEvent merge(SimulationEvent smaller, 
				  SimulationEvent larger) 
    {
	if (larger == null) {
	    return smaller;
	}
	if (smaller == null) {
	    return larger;
	}
	SimulationEvent tmp;
	long itest;
	boolean test;
	/* larger.value < smaller.value*/
	itest = larger.time - smaller.time;
	if (itest == 0 && (larger.tpriority != smaller.tpriority)) {
	    itest = (larger.tpriority > smaller.tpriority)? 1: -1;
	}
	if (itest == 0) itest = larger.instance - smaller.instance;
	if (itest < 0 ) {
	    tmp = larger;
	    larger = smaller;
	    smaller = tmp;
	}
	SimulationEvent last = smaller;
	SimulationEvent result = last;
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
	    itest = larger.time - smaller.time;
	    if (itest == 0 && larger.tpriority != smaller.tpriority) {
		itest = (larger.tpriority > smaller.tpriority)? 1: -1;
	    }
	    if (itest == 0) itest = larger.instance - smaller.instance;
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
