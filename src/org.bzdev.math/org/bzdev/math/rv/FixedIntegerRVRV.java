package org.bzdev.math.rv;

//@exbundle org.bzdev.math.rv.lpack.RV

/**
 * Random variable that generates a clone of the same integer-valued
 * random variable repeatedly.
 */
public class FixedIntegerRVRV
    extends IntegerRandomVariableRV<FixedIntegerRV>
{
    static String errorMsg(String key, Object... args) {
	return RVErrorMsg.errorMsg(key, args);
    }

    IntegerRandomVariable value;

    // initial min/max for value.
    int vmin;
    int vmax;
    boolean vminClosed;
    boolean vmaxClosed;

    /**
     * Constructor.
     * @param rv the random variable used to provide the value for each
     *        FixedIntegerRV generated.
     * @exception RandomVariableException the argument could not be cloned
     */
    public FixedIntegerRVRV(IntegerRandomVariable rv)
	throws RandomVariableException
    {
	try {
	    rv = (IntegerRandomVariable)(rv.clone());
	} catch (CloneNotSupportedException e) {
	    String msg = errorMsg("noClone", value.getClass().getName());
	    throw new RandomVariableException(msg, e);
	}
	value = rv;
	determineIfOrdered(rv);
	Integer xmin = value.getMinimum();
	Integer xmax = value.getMaximum();
	vmin = (xmin == null)? Integer.MIN_VALUE: xmin.intValue();
	vmax = (xmax == null)? Integer.MAX_VALUE: xmax.intValue();
	Boolean xminClosed = value.getMinimumClosed();
	Boolean xmaxClosed = value.getMaximumClosed();
	vminClosed = (xmin == null)? true: xminClosed.booleanValue();
	vmaxClosed = (xmax == null)? true: xmaxClosed.booleanValue();
    }

    public void setMinimum(Integer min, boolean closed)
	throws UnsupportedOperationException, IllegalArgumentException
    {
	super.setMinimum(min, closed);
	if (min == null) {
	    value.setMinimum(vmin, vminClosed);
	} else {
	    int xmin = min.intValue();
	    if (vmin == xmin) {
		value.setMinimum(xmin, (closed && vminClosed));
	    } else if (xmin > vmin) {
		value.setMinimum(xmin, closed);
	    } else {
		value.setMinimum(vmin, vminClosed);
	    }
	}
    }

    public void setMaximum(Integer max, boolean closed)
	throws UnsupportedOperationException, IllegalArgumentException
    {
	super.setMaximum(max, closed);
	if (max == null) {
	    value.setMaximum(vmax, vmaxClosed);
	} else {
	    int xmax = max.intValue();
	    if (vmax == xmax) {
		value.setMaximum(xmax, (closed && vmaxClosed));
	    } else if (xmax < vmax) {
		value.setMaximum(xmax, closed);
	    } else {
		value.setMaximum(vmax, vmaxClosed);
	    }
	}
    }

    public void tightenMinimum(Integer min, boolean closed)
	throws UnsupportedOperationException, IllegalArgumentException
    {
	super.tightenMinimum(min, closed);
	value.tightenMinimum(min, closed);
    }

    public void tightenMaximum(Integer max, boolean closed)
	throws UnsupportedOperationException, IllegalArgumentException
    {
	super.tightenMaximum(max, closed);
	value.tightenMaximum(max, closed);
    }


    protected FixedIntegerRV doNext() throws RandomVariableException {
	return new FixedIntegerRV(value.next());
    }
    
    @SuppressWarnings("unchecked")
    public Object clone() throws CloneNotSupportedException {
	FixedIntegerRVRV obj = (FixedIntegerRVRV) super.clone();
	obj.value = (IntegerRandomVariable)(value.clone());
	return obj;
    }
}

//  LocalWords:  exbundle rv FixedIntegerRV RandomVariableException
//  LocalWords:  noClone
