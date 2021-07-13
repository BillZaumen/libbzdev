package org.bzdev.math.rv;

/**
 * Random variable that generates the a sequence of random variables, each
 * of which generates a sequence of double-precision numbers.  The type
 * parameter DRV is set to the type of random variable that is to be
 * generated, which will usually be a subclass of DoubleRandomVariable.
 */
abstract public class DoubleRandomVariableRV<DRV extends DoubleRandomVariable>
    extends RandomVariableRVN<Double, DRV> 
{
    protected int cmp(Double x, Double y) {
	if (x == y) return 0;
	else if (x < y) return -1;
	else return 1;
    }

    public void tightenMinimumS(String x, boolean b)
	throws IllegalArgumentException
    {
	tightenMinimum(Double.valueOf(x), b);
    }
    public void tightenMaximumS(String x, boolean b)
	throws IllegalArgumentException
    {
	tightenMaximum(Double.valueOf(x), b);
    }


    // Needed to avoid type warnings due to type erasure.

    public void tightenMinimum(Double x, boolean b) {
	super.tightenMinimum(x, b);
    }

    public void tightenMaximum(Double x, boolean b) {
	super.tightenMinimum(x,b);
    }

    public void setMinimum(Double min, boolean closed) {
	super.setMinimum(min, closed);
    }
    public void setMaximum(Double min, boolean closed) {
	super.setMaximum(min, closed);
    }


    // needed to avoid type-erasure issues
    public DRV next() {return super.next();}
}

//  LocalWords:  DRV DoubleRandomVariable
