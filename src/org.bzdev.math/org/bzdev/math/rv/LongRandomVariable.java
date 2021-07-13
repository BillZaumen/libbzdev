package org.bzdev.math.rv;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.LongConsumer;
import java.util.stream.LongStream;
import java.util.stream.StreamSupport;

/**
 * Base class for Long-valued random numbers.
 * Implements the tightenMinimumS, tighenMaximumS, setRequiredMininum,
 * setRequiredMaximum setMinimum, setMaximum, tightenMinimum, and
 * tightenMaximum methods from the RandomVariable interface, and
 * introduces a protected method, rangeTestFailed, for use by
 * subclasses.
 */

abstract public class LongRandomVariable extends RandomVariable<Long>
{
    private boolean minSet = false;
    private boolean minClosed = true;
    private long min = Long.MIN_VALUE;

    private boolean maxSet = false;
    private boolean maxClosed = true;
    private long max = Long.MAX_VALUE;

    private boolean reqminSet = false;
    private boolean reqminClosed = true;
    private long reqmin = Long.MIN_VALUE;

    private boolean reqmaxSet = false;
    private boolean reqmaxClosed = false;
    private long reqmax = Long.MAX_VALUE;

    private boolean  needRangeTest = false;

    public void setMinimum(Long min, boolean closed)
	throws UnsupportedOperationException, IllegalArgumentException
    {
	if (min == null) {
	    minSet = false;
	} else {
	    minSet = true;
	    this.min = min;
	    minClosed = closed;
	}
	needRangeTest = reqminSet || reqmaxSet || minSet || maxSet;
    }

    protected void setRequiredMinimum(Long min, boolean closed)
	throws UnsupportedOperationException, IllegalArgumentException
    {
	if (min == null) {
	    reqminSet = false;
	} else {
	    reqminSet = true;
	    reqmin = min;
	    reqminClosed = closed;
	}
	needRangeTest = reqminSet || reqmaxSet || minSet || maxSet;
    }

    public void tightenMinimumS(String s, boolean closed)
	throws UnsupportedOperationException, IllegalArgumentException
    {
	tightenMinimum(Long.valueOf(s), closed);
    }

    public void tightenMaximumS(String s, boolean closed)
	throws UnsupportedOperationException, IllegalArgumentException
    {
	tightenMaximum(Long.valueOf(s), closed);
    }

    public void tightenMinimum(Long min, boolean closed)
	throws UnsupportedOperationException, IllegalArgumentException
    {
	if (minSet) {
	    if (min != null) {
		if (this.min < min) {
		    this.min = min;
		} else if (minClosed) {
		    minClosed = closed;
		}
	    }
	} else {
	    setMinimum(min, closed);
	}
	needRangeTest = reqminSet || reqmaxSet || minSet || maxSet;
    }

    public void setMaximum(Long max, boolean closed)
	throws UnsupportedOperationException, IllegalArgumentException
    {
	if (max == null) {
	    maxSet = false;
	} else {
	    maxSet = true;
	    this.max = max;
	    maxClosed = closed;
	}
	needRangeTest = reqminSet || reqmaxSet || minSet || maxSet;
    }

    protected void setRequiredMaximum(Long max, boolean closed)
	throws UnsupportedOperationException, IllegalArgumentException
    {
	if (max == null) {
	    reqmaxSet = false;
	} else {
	    reqmaxSet = true;
	    reqmax = max;
	    reqmaxClosed = closed;
	}
	needRangeTest = reqminSet || reqmaxSet || minSet || maxSet;
    }

    public void tightenMaximum(Long max, boolean closed)
	throws UnsupportedOperationException, IllegalArgumentException
    {
	if (maxSet) {
	    if (max != null) {
		if (this.max > max) {
		    this.max = max;
		} else if (maxClosed) {
		    maxClosed = closed;
		}
	    }
	} else {
	    setMaximum(max, closed);
	}
	needRangeTest = reqminSet || reqmaxSet || minSet || maxSet;
    }

    /**
     * Determine if a range test is needed.
     * @return true if a range test is needed; false if the range check
     *         would always succeed
     */
    protected final boolean rangeTestNeeded() {
	return needRangeTest;
    }

    /**
     * Range test.
     * Subclasses implementing next() should call this method to
     * ensure that a randomly generated value is in the range set
     * by calls to setMinimum and setMaximum.
     * @param value the value to test
     * @return true if the range-test failed; false otherwise
     */
    protected final boolean rangeTestFailed(long value) {
	boolean test1 =
	    (reqminSet && (reqminClosed? (value < reqmin): (value <= reqmin)))
	    || (minSet && (minClosed? (value < min): (value <= min)));
	boolean test2 =
	    (reqmaxSet && (reqmaxClosed? (value > reqmax): (value >= reqmax)))
	    || (maxSet && (maxClosed? (value > max): (value >= max)));
	return (test1 || test2);
    }

    public final Long getMinimum() {
	if (reqminSet) {
	    if (minSet) {
		return (reqmin < min)? Long.valueOf(min): Long.valueOf(reqmin);
	    } else {
		return Long.valueOf(reqmin);
	    }
	} else {
	    if (minSet) {
		return Long.valueOf(min);
	    } else {
		return null;
	    }
	}
    }

    public final Boolean getMinimumClosed() {
	if (reqminSet) {
	    if (minSet) {
		if (reqmin == min) {
		    return Boolean.valueOf(reqminClosed || minClosed);
		} else if (reqmin < min) {
		    return Boolean.valueOf (minClosed);
		} else {
		    return Boolean.valueOf(reqminClosed);
		}
	    } else {
		return Boolean.valueOf(reqminClosed);
	    }
	} else {
	    if (minSet) {
		return Boolean.valueOf(minClosed);
	    } else {
		return null;
	    }
	}
    }

    public final Long getMaximum() {
	if (reqmaxSet) {
	    if (maxSet) {
		return (reqmax > max)? Long.valueOf(max): Long.valueOf(reqmax);
	    } else {
		return Long.valueOf(reqmax);
	    }
	} else {
	    if (maxSet) {
		return Long.valueOf(max);
	    } else {
		return null;
	    }
	}
    }

    public final Boolean getMaximumClosed() {
	if (reqmaxSet) {
	    if (maxSet) {
		if (reqmax == max) {
		    return Boolean.valueOf(reqmaxClosed || maxClosed);
		} else if (reqmax > max) {
		    return Boolean.valueOf (maxClosed);
		} else {
		    return Boolean.valueOf(reqmaxClosed);
		}
	    } else {
		return Boolean.valueOf(reqmaxClosed);
	    }
	} else {
	    if (maxSet) {
		return Boolean.valueOf(maxClosed);
	    } else {
		return null;
	    }
	}
    }

    private Spliterator.OfLong
	getSpliterator (Spliterator<Long> spliterator)
    {
	return new Spliterator.OfLong() {
	    @Override
	    public boolean tryAdvance(Consumer<? super Long> action) {
		return spliterator.tryAdvance(action);
	    }
	    @Override
	    public boolean tryAdvance(LongConsumer action) {
		Consumer<Long> ourConsumer = new Consumer<>() {
			public void accept(Long value) {
			    action.accept((long)value);
			}
		    };
		return spliterator.tryAdvance(ourConsumer);
	    }
	    @Override
	    public int characteristics() {
		return spliterator.characteristics();
	    }
	    @Override
	    public long estimateSize() {
		return spliterator.estimateSize();
	    }
	    @Override
	    public Spliterator.OfLong trySplit() {
		Spliterator<Long> s = spliterator.trySplit();
		return (s == null)? null: getSpliterator(s);

	    }
	};
    }

    @Override
    public Spliterator.OfLong spliterator(long size) {
	return getSpliterator(super.spliterator(size));
    }

    @Override
    public Spliterator.OfLong spliterator() {
	return getSpliterator(super.spliterator());
    }

    /**
     * Get a fixed-length stream of long values.
     * @param size the number of random values to provide
     * @return the stream
     */
    public  LongStream stream(long size) {
	return StreamSupport.longStream(spliterator(size), false);
    }

    /*
     * Get a fixed-length parallel stream of long values.
     * @param size the number of random values to provide
     * @return the stream
     */
    public LongStream parallelStream(long size) {
	return StreamSupport.longStream(spliterator(size), true);
    }

    /*
     * Get an infinite stream of long values.
     * @return the stream
     */
    public LongStream stream() {
	return StreamSupport.longStream(spliterator(), false);
    }

    /*
     * Get an infinite parallel stream of long values.
     * @return the stream
     */
    public LongStream parallelStream() {
	return StreamSupport.longStream(spliterator(), true);
    }


}

//  LocalWords:  tightenMinimumS tighenMaximumS setRequiredMininum
//  LocalWords:  setRequiredMaximum setMinimum setMaximum subclasses
//  LocalWords:  tightenMinimum tightenMaximum RandomVariable
//  LocalWords:  rangeTestFailed
