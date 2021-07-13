package org.bzdev.math.rv;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.Iterator;

/**
 * Random variable that generates the a sequence of random variables, each
 * of which generates a sequence of boolean values (of type Boolean).  The type
 * parameter BRV is set to the type of random variable that is to be
 * generated, which will usually be a subclass of LongRandomVariable.

 */
abstract public class BooleanRandomVariableRV<BRV extends BooleanRandomVariable>
    extends RandomVariableRV<Boolean, BRV>
{
    // to prevent type-erasure issues.
    abstract public BRV next();

    public void tightenMinimumS(String s, boolean closed)
	throws UnsupportedOperationException
    {
	throw new UnsupportedOperationException();
    }

    public void tightenMaximumS(String s, boolean closed)
	throws UnsupportedOperationException
    {
	throw new UnsupportedOperationException();
    }

    public Stream<BRV> stream(long size) {
	int characteristics = Spliterator.IMMUTABLE | Spliterator.SIZED;
	Spliterator<BRV> spliterator =
	    Spliterators.spliterator(new Iterator<BRV>() {
		    long count  = 0;
		    public BRV next() {
			count++;
			return BooleanRandomVariableRV.this.next();
		    }
		    public boolean hasNext() {return count < size;}
		}, size, characteristics);
	return StreamSupport.stream(spliterator, false);
    }

    public Stream<BRV> parallelStream(long size) {
	int characteristics = Spliterator.IMMUTABLE | Spliterator.SIZED;
	Spliterator<BRV> spliterator =
	    Spliterators.spliterator(new Iterator<BRV>() {
		    long count  = 0;
		    public BRV next() {
			count++;
			return BooleanRandomVariableRV.this.next();
		    }
		    public boolean hasNext() {return count < size;}
		}, size, characteristics);
	return StreamSupport.stream(spliterator, true);
    }


}

//  LocalWords:  boolean BRV LongRandomVariable
