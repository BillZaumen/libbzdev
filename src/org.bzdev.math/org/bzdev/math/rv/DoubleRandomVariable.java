package org.bzdev.math.rv;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.stream.DoubleStream;
import java.util.stream.StreamSupport;

/**
 * Base class for Double-valued random numbers.
 * Implements the tightenMinimumS, tighenMaximumS, setRequiredMininum,
 * setRequiredMaximum setMinimum, setMaximum, tightenMinimum, and
 * tightenMaximum methods from the RandomVariable interface, and
 * introduces a protected method, rangeTestFailed, for use by
 * subclasses.
 */

abstract public class DoubleRandomVariable extends RandomVariable<Double>
{
    // protected DoubleRandomVariable() {}

    private boolean minSet = false;
    private boolean minClosed = true;
    private double min = Double.NEGATIVE_INFINITY;

    private boolean maxSet = false;
    private boolean maxClosed = false;
    private double max = Double.POSITIVE_INFINITY;

    private boolean reqminSet = false;
    private boolean reqminClosed = true;
    private double reqmin = Double.NEGATIVE_INFINITY;

    private boolean reqmaxSet = false;
    private boolean reqmaxClosed = false;
    private double reqmax = Double.POSITIVE_INFINITY;

    private boolean  needRangeTest = false;

    public void setMinimum(Double min, boolean closed) 
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

    protected void setRequiredMinimum(Double min, boolean closed)
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


    public void tightenMinimumS(String min, boolean closed)
	throws UnsupportedOperationException, IllegalArgumentException
    {
	tightenMinimum(Double.valueOf(min), closed);
    }

    public void tightenMinimum(Double min, boolean closed)
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


    public void setMaximum(Double max, boolean closed)
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

    protected void setRequiredMaximum(Double max, boolean closed)
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


    public void tightenMaximumS(String max, boolean closed)
	throws UnsupportedOperationException, IllegalArgumentException
    {
	tightenMaximum(Double.valueOf(max), closed);
    }

    public void tightenMaximum(Double max, boolean closed)
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
    protected final boolean rangeTestFailed(double value) {
	boolean test1 =
	    (reqminSet && (reqminClosed? (value < reqmin): (value <= reqmin)))
	    || (minSet && (minClosed? (value < min): (value <= min)));
	boolean test2 =
	    (reqmaxSet && (reqmaxClosed? (value > reqmax): (value >= reqmax)))
	    || (maxSet && (maxClosed? (value > max): (value >= max)));
	return (test1 || test2);
    }


    public final Double getMinimum() {
	if (reqminSet) {
	    if (minSet) {
		return (reqmin < min)? Double.valueOf(min):
		    Double.valueOf(reqmin);
	    } else {
		return Double.valueOf(reqmin);
	    }
	} else {
	    if (minSet) {
		return Double.valueOf(min);
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

    public final Double getMaximum() {
	if (reqmaxSet) {
	    if (maxSet) {
		return (reqmax > max)? Double.valueOf(max):
		    Double.valueOf(reqmax);
	    } else {
		return Double.valueOf(reqmax);
	    }
	} else {
	    if (maxSet) {
		return Double.valueOf(max);
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

    private Spliterator.OfDouble getSpliterator (Spliterator<Double> spliterator)
    {
	return new Spliterator.OfDouble() {
	    @Override
	    public boolean tryAdvance(Consumer<? super Double> action) {
		return spliterator.tryAdvance(action);
	    }
	    @Override
	    public boolean tryAdvance(DoubleConsumer action) {
		Consumer<Double> ourConsumer = new Consumer<>() {
			public void accept(Double value) {
			    action.accept((double) value);
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
	    public Spliterator.OfDouble trySplit() {
		Spliterator<Double> s = spliterator.trySplit();
		return (s == null)? null: getSpliterator(s);

	    }
	};
    }

    @Override
    public Spliterator.OfDouble spliterator(long size) {
	return getSpliterator(super.spliterator(size));
    }

    @Override
    public Spliterator.OfDouble spliterator() {
	return getSpliterator(super.spliterator());
    }

    /**
     * Get a fixed-length stream of double values.
     * @param size the number of random values to provide
     * @return the stream
     */
    public  DoubleStream stream(long size) {
	return StreamSupport.doubleStream(spliterator(size), false);
    }

    /*
     * Get a fixed-length parallel stream of double values.
     * @param size the number of random values to provide
     * @return the stream
     */
    public DoubleStream parallelStream(long size) {
	return StreamSupport.doubleStream(spliterator(size), true);
    }

    /*
     * Get an infinite stream of double values.
     * @return the stream
     */
    public DoubleStream stream() {
	return StreamSupport.doubleStream(spliterator(), false);
    }

    /*
     * Get an infinite parallel stream of double values.
     * @return the stream
     */
    public DoubleStream parallelStream() {
	return StreamSupport.doubleStream(spliterator(), true);
    }

}

//  LocalWords:  tightenMinimumS tighenMaximumS setRequiredMininum
//  LocalWords:  setRequiredMaximum setMinimum setMaximum subclasses
//  LocalWords:  tightenMinimum tightenMaximum RandomVariable
//  LocalWords:  rangeTestFailed DoubleRandomVariable
