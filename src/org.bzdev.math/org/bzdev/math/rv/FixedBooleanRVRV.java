package org.bzdev.math.rv;

//@exbundle org.bzdev.math.rv.lpack.RV


/**
 * Random variable that generates a sequence of FixedBooleanRV random
 * variables.
 */
public class FixedBooleanRVRV
    extends BooleanRandomVariableRV<FixedBooleanRV>
{
    static String errorMsg(String key, Object... args) {
	return RVErrorMsg.errorMsg(key, args);
    }

    BooleanRandomVariable value;

    /**
     * Constructor.
     * @param rv the random variable used to provide the value for each
     *        FixedBooleanRV generated.
     * @exception RandomVariableException the argument could not be cloned
     */
    public FixedBooleanRVRV(BooleanRandomVariable rv)
	throws RandomVariableException
    {
	try {
	    rv = (BooleanRandomVariable)(rv.clone());
	} catch (CloneNotSupportedException e) {
	    String msg = errorMsg("noClone", value.getClass().getName());
	    throw new RandomVariableException(msg, e);
	}
	value = rv;
	determineIfOrdered(rv);
    }

    public FixedBooleanRV next() {
	return new FixedBooleanRV(value.next());
    }
    
    @SuppressWarnings("unchecked")
    public Object clone() throws CloneNotSupportedException {
	FixedBooleanRVRV obj = (FixedBooleanRVRV) super.clone();
	obj.value = (BooleanRandomVariable)(value.clone());
	return obj;
    }
}

//  LocalWords:  exbundle FixedBooleanRV rv RandomVariableException
//  LocalWords:  noClone
