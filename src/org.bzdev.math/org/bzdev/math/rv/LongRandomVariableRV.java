package org.bzdev.math.rv;

/**
 * Random variable that generates the a sequence of random variables, each
 * of which generates a sequence of integers (of type Long).  The type
 * parameter LRV is set to the type of random variable that is to be
 * generated, which will usually be a subclass of LongRandomVariable.
 */
abstract public class LongRandomVariableRV<LRV extends LongRandomVariable>
    extends RandomVariableRVN<Long, LRV> 
{
    protected int cmp(Long x, Long y) {
	if (x == y) return 0;
	else if (x < y) return -1;
	else return 1;
    }

    // Needed to avoid type warnings due to type erasure.

    public void tightenMinimumS(String s, boolean b)
	throws IllegalArgumentException
    {
	tightenMinimum(Long.valueOf(s), b);
    }

    public void tightenMaximumS(String s, boolean b)
	throws IllegalArgumentException
    {
	tightenMaximum(Long.valueOf(s), b);
    }

    public void tightenMinimum(Long x, boolean b) {
	super.tightenMinimum(x, b);
    }

    public void tightenMaximum(Long x, boolean b) {
	super.tightenMinimum(x,b);
    }

    public void setMinimum(Long min, boolean closed) {
	super.setMinimum(min, closed);
    }
    public void setMaximum(Long min, boolean closed) {
	super.setMaximum(min, closed);
    }

    // needed to avoid type-erasure issues
    public LRV next() {return super.next();}
}

//  LocalWords:  LRV LongRandomVariable
