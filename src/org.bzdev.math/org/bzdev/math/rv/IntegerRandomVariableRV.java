package org.bzdev.math.rv;

/**
 * Random variable that generates the a sequence of random variables, each
 * of which generates a sequence of integers (of type Integer).  The type
 * parameter IRV is set to the type of random variable that is to be
 * generated, which will usually be a subclass of IntegerRandomVariable.
 */
abstract public class IntegerRandomVariableRV<IRV extends IntegerRandomVariable>
    extends RandomVariableRVN<Integer, IRV>
{
    protected int cmp(Integer x, Integer y) {
	return x - y;
    }

    // Needed to avoid type warnings due to type erasure.

    public void tightenMinimumS(String s, boolean b)
	throws IllegalArgumentException
    {
	tightenMinimum(Integer.valueOf(s), b);
    }

    public void tightenMaximumS(String s, boolean b)
	throws IllegalArgumentException
    {
	tightenMaximum(Integer.valueOf(s), b);
    }

    public void tightenMinimum(Integer x, boolean b) {
	super.tightenMinimum(x, b);
    }

    public void tightenMaximum(Integer x, boolean b) {
	super.tightenMinimum(x,b);
    }

    public void setMinimum(Integer min, boolean closed) {
	super.setMinimum(min, closed);
    }
    public void setMaximum(Integer min, boolean closed) {
	super.setMaximum(min, closed);
    }

    // needed to avoid type-erasure issues
    public IRV next() {return super.next();}
}

//  LocalWords:  IRV IntegerRandomVariable
