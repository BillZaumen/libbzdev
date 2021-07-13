package org.bzdev.math.rv;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

/**
 * Base class for Integer-valued random numbers.
 * Implements the tightenMinimumS, tighenMaximumS, setRequiredMininum,
 * setRequiredMaximum setMinimum, setMaximum, tightenMinimum, and
 * tightenMaximum methods from the RandomVariable interface, and
 * introduces a protected method, rangeTestFailed, for use by
 * subclasses.
 */

abstract public class IntegerRandomVariable extends RandomVariable<Integer>
{
    private boolean minSet = false;
    private boolean minClosed = false;
    private int min = Integer.MIN_VALUE;

    private boolean maxSet = false;
    private boolean maxClosed = false;
    private int max = Integer.MAX_VALUE;

    private boolean reqminSet = false;
    private boolean reqminClosed = true;
    private int reqmin = Integer.MIN_VALUE;

    private boolean reqmaxSet = false;
    private boolean reqmaxClosed = false;
    private int reqmax = Integer.MAX_VALUE;

    private boolean  needRangeTest = false;

    public void setMinimum(Integer min, boolean closed)
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

    protected void setRequiredMinimum(Integer min, boolean closed)
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
	tightenMinimum(Integer.valueOf(s), closed);
    }


    public void tightenMinimum(Integer min, boolean closed)
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

    public void setMaximum(Integer max, boolean closed)
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

    protected void setRequiredMaximum(Integer max, boolean closed)
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

    public void tightenMaximumS(String s, boolean closed)
	throws UnsupportedOperationException, IllegalArgumentException
    {
	tightenMaximum(Integer.valueOf(s), closed);
    }

    public void tightenMaximum(Integer max, boolean closed)
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
     * Assert that a range test is no longer needed.
     * This should be called only if a subclass can ensure that
     * calls to next() will always be in the specified range.
     * The range test may be reset if setMinimum, setMaximum,
     * tightenMinimum, tightenMaximum, setRequiredMinimum,
     * setRequiredMaximum, tightenMinimumS, or tightenMaximumS
     * are called.
     */
    protected void clearRangeTest() {
	needRangeTest = false;
    }

    /**
     * Range test.
     * Subclasses implementing next() should call this method to
     * ensure that a randomly generated value is in the range set
     * by calls to setMinimum and setMaximum.
     * @param value the value to test
     * @return true if the range-test failed; false otherwise
     */
   protected final boolean rangeTestFailed(int value) {
	boolean test1 =
	    (reqminSet && (reqminClosed? (value < reqmin): (value <= reqmin)))
	    || (minSet && (minClosed? (value < min): (value <= min)));
	boolean test2 =
	    (reqmaxSet && (reqmaxClosed? (value > reqmax): (value >= reqmax)))
	    || (maxSet && (maxClosed? (value > max): (value >= max)));
	return (test1 || test2);
    }

    public final Integer getMinimum() {
	if (reqminSet) {
	    if (minSet) {
		return (reqmin < min)? Integer.valueOf(min):
		    Integer.valueOf(reqmin);
	    } else {
		return Integer.valueOf(reqmin);
	    }
	} else {
	    if (minSet) {
		return Integer.valueOf(min);
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

    public final Integer getMaximum() {
	if (reqmaxSet) {
	    if (maxSet) {
		return (reqmax > max)? Integer.valueOf(max):
		    Integer.valueOf(reqmax);
	    } else {
		return Integer.valueOf(reqmax);
	    }
	} else {
	    if (maxSet) {
		return Integer.valueOf(max);
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

    private Spliterator.OfInt
	getSpliterator (Spliterator<Integer> spliterator)
    {
	return new Spliterator.OfInt() {
	    @Override
	    public boolean tryAdvance(Consumer<? super Integer> action) {
		return spliterator.tryAdvance(action);
	    }
	    @Override
	    public boolean tryAdvance(IntConsumer action) {
		Consumer<Integer> ourConsumer = new Consumer<>() {
			public void accept(Integer value) {
			    action.accept((int)value);
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
	    public Spliterator.OfInt trySplit() {
		Spliterator<Integer> s = spliterator.trySplit();
		return (s == null)? null: getSpliterator(s);

	    }
	};
    }

    @Override
    public Spliterator.OfInt spliterator(long size) {
	return getSpliterator(super.spliterator(size));
    }

    @Override
    public Spliterator.OfInt spliterator() {
	return getSpliterator(super.spliterator());
    }

    /**
     * Get a fixed-length stream of integer values.
     * @param size the number of random values to provide
     * @return the stream
     */
    public  IntStream stream(long size) {
	return StreamSupport.intStream(spliterator(size), false);
    }

    /*
     * Get a fixed-length parallel stream of integer values.
     * @param size the number of random values to provide
     * @return the stream
     */
    public IntStream parallelStream(long size) {
	return StreamSupport.intStream(spliterator(size), true);
    }

    /*
     * Get an infinie stream of integer values.
     * @return the stream
     */
    public IntStream stream() {
	return StreamSupport.intStream(spliterator(), false);
    }

    /*
     * Get an infinite parallel stream of integer values.
     * @return the stream
     */
    public IntStream parallelStream() {
	return StreamSupport.intStream(spliterator(), true);
    }
}

//  LocalWords:  tightenMinimumS tighenMaximumS setRequiredMininum
//  LocalWords:  setRequiredMaximum setMinimum setMaximum subclasses
//  LocalWords:  tightenMinimum tightenMaximum RandomVariable
//  LocalWords:  rangeTestFailed setRequiredMinimum tightenMaximumS
