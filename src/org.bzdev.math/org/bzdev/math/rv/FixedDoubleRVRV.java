package org.bzdev.math.rv;

//@exbundle org.bzdev.math.rv.lpack.RV

/**
 * Random variable that generates a clone of the same double-valued
 * random variable repeatedly.
 */
public class FixedDoubleRVRV
    extends DoubleRandomVariableRV<FixedDoubleRV>
{

    static String errorMsg(String key, Object... args) {
	return RVErrorMsg.errorMsg(key, args);
    }

    DoubleRandomVariable value;

    // initial min/max for value.
    double vmin;
    double vmax;
    boolean vminClosed;
    boolean vmaxClosed;

    /**
     * Constructor.
     * @param rv the random variable used to provide the value for each
     *        FixedDoubleRV generated.
     * @exception RandomVariableException the argument could not be cloned
     */
    public FixedDoubleRVRV(DoubleRandomVariable rv)
	throws RandomVariableException
    {
	try {
	    rv = (DoubleRandomVariable)(rv.clone());
	} catch (CloneNotSupportedException e) {
	    String msg = errorMsg("noClone", value.getClass().getName());
	    throw new RandomVariableException(msg, e);
	}
	value = rv;
	determineIfOrdered(rv);
	Double xmin = value.getMinimum();
	Double xmax = value.getMaximum();
	vmin = (xmin == null)? Double.MIN_VALUE: xmin.doubleValue();
	vmax = (xmax == null)? Double.MAX_VALUE: xmax.doubleValue();
	Boolean xminClosed = value.getMinimumClosed();
	Boolean xmaxClosed = value.getMaximumClosed();
	vminClosed = (xmin == null)? true: xminClosed.booleanValue();
	vmaxClosed = (xmax == null)? true: xmaxClosed.booleanValue();
    }

    public void setMinimum(Double min, boolean closed)
	throws UnsupportedOperationException, IllegalArgumentException
    {
	super.setMinimum(min, closed);
	if (min == null) {
	    value.setMinimum(vmin, vminClosed);
	} else {
	    double xmin = min.doubleValue();
	    if (vmin == xmin) {
		value.setMinimum(xmin, (closed && vminClosed));
	    } else if (xmin > vmin) {
		value.setMinimum(xmin, closed);
	    } else {
		value.setMinimum(vmin, vminClosed);
	    }
	}
    }

    public void setMaximum(Double max, boolean closed)
	throws UnsupportedOperationException, IllegalArgumentException
    {
	super.setMaximum(max, closed);
	if (max == null) {
	    value.setMaximum(vmax, vmaxClosed);
	} else {
	    double xmax = max.doubleValue();
	    if (vmax == xmax) {
		value.setMaximum(xmax, (closed && vmaxClosed));
	    } else if (xmax < vmax) {
		value.setMaximum(xmax, closed);
	    } else {
		value.setMaximum(vmax, vmaxClosed);
	    }
	}
    }

    public void tightenMinimum(Double min, boolean closed)
	throws UnsupportedOperationException, IllegalArgumentException
    {
	super.tightenMinimum(min, closed);
	value.tightenMinimum(min, closed);
    }

    public void tightenMaximum(Double max, boolean closed)
	throws UnsupportedOperationException, IllegalArgumentException
    {
	super.tightenMaximum(max, closed);
	value.tightenMaximum(max, closed);
    }

    protected FixedDoubleRV doNext() throws RandomVariableException {
	return new FixedDoubleRV(value.next());
    }
    
    @SuppressWarnings("unchecked")
    public Object clone() throws CloneNotSupportedException {
	FixedDoubleRVRV obj = (FixedDoubleRVRV) super.clone();
	obj.value = (DoubleRandomVariable)(value.clone());
	return obj;
    }
}

//  LocalWords:  exbundle rv FixedDoubleRV RandomVariableException
//  LocalWords:  noClone
