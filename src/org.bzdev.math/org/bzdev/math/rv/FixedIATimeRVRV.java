package org.bzdev.math.rv;

//@exbundle org.bzdev.math.rv.lpack.RV

/**
 * Random variable that generates a sequence of FixedIATimeRV instances.
 */
public class FixedIATimeRVRV extends InterarrivalTimeRVRV<FixedIATimeRV> {

    static String errorMsg(String key, Object... args) {
	return RVErrorMsg.errorMsg(key, args);
    }

    LongRandomVariable value;

    // initial min/max for value.
    long vmin;
    long vmax;
    boolean vminClosed;
    boolean vmaxClosed;

    /**
     * Constructor.
     * @param rv the random variable used to provide the value for each
     *        FixedIATimeRV generated.
     */
    public FixedIATimeRVRV(LongRandomVariable rv) {
	try {
	    rv = (LongRandomVariable)rv.clone();
	} catch (CloneNotSupportedException e) {
	    String msg = errorMsg("noClone", value.getClass().getName());
	    throw new 
		RandomVariableException(msg, e);
	}
	value = rv;
	determineIfOrdered(rv);
	value.tightenMinimum(0L, true);
	Long xmin = value.getMinimum();
	Long xmax = value.getMaximum();
	vmin = (xmin == null)? Long.MIN_VALUE: xmin.longValue();
	vmax = (xmax == null)? Long.MAX_VALUE: xmax.longValue();
	Boolean xminClosed = value.getMinimumClosed();
	Boolean xmaxClosed = value.getMaximumClosed();
	vminClosed = (xmin == null)? true: xminClosed.booleanValue();
	vmaxClosed = (xmax == null)? true: xmaxClosed.booleanValue();
    }

    public void setMinimum(Long min, boolean closed)
	throws UnsupportedOperationException, IllegalArgumentException
    {
	super.setMinimum(min, closed);
	if (min == null) {
	    value.setMinimum(vmin, vminClosed);
	} else {
	    long xmin = min.longValue();
	    if (vmin == xmin) {
		value.setMinimum(xmin, (closed && vminClosed));
	    } else if (xmin > vmin) {
		value.setMinimum(xmin, closed);
	    } else {
		value.setMinimum(vmin, vminClosed);
	    }
	}
    }

    public void setMaximum(Long max, boolean closed)
	throws UnsupportedOperationException, IllegalArgumentException
    {
	super.setMaximum(max, closed);
	if (max == null) {
	    value.setMaximum(vmax, vmaxClosed);
	} else {
	    long xmax = max.longValue();
	    if (vmax == xmax) {
		value.setMaximum(xmax, (closed && vmaxClosed));
	    } else if (xmax < vmax) {
		value.setMaximum(xmax, closed);
	    } else {
		value.setMaximum(vmax, vmaxClosed);
	    }
	}
    }

    public void tightenMinimum(Long min, boolean closed)
	throws UnsupportedOperationException, IllegalArgumentException
    {
	super.tightenMinimum(min, closed);
	value.tightenMinimum(min, closed);
    }

    public void tightenMaximum(Long max, boolean closed)
	throws UnsupportedOperationException, IllegalArgumentException
    {
	super.tightenMaximum(max, closed);
	value.tightenMaximum(max, closed);
    }

    protected FixedIATimeRV doNext() throws RandomVariableException {
	return new FixedIATimeRV(value.next());
    }
    
    @SuppressWarnings("unchecked")
    public Object clone() throws CloneNotSupportedException {
	FixedLongRVRV obj = (FixedLongRVRV) super.clone();
	obj.value = (InterarrivalTimeRV)(value.clone());
	return obj;
    }
}

//  LocalWords:  exbundle FixedIATimeRV rv noClone
