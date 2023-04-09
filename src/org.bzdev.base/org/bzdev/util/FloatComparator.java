package org.bzdev.util;

/**
 * Comparator for the float primitive type.
 * This interface is basically a copy of the interface
 * {@link java.util.Comparator} but specialized to compare 32-bit
 * integers passed to its {@link FloatComparator#compare(float,float)}
 * using primitive types (<code>float</code> instead of <code>Float</code>).
 * The method descriptions were copied, with slight modifications, from
 * those in {@link java.util.Comparator} as the intention is to provide
 * a similar interface.
 * <P>
 * The reason for providing this class is that Java does not allow one
 * to use a primitive type as a type parameter:
 * <code>Comparator&lt;float&gt;</code> will generate a compile-time error,
 * although <code>Comparator&lt;Float&gt;</code> can be used in some cases
 * due to autoboxing.
 * @see java.util.Comparator
 */
public interface FloatComparator {
   /**
     * Compares its two arguments for order.  Returns a negative integer,
     * zero, or a positive integer as the first argument is less than, equal
     * to, or greater than the second.<p>
     *
     * In the foregoing description, the notation
     * <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the mathematical
     * <i>signum</i> function, which is defined to return one of <tt>-1</tt>,
     * <tt>0</tt>, or <tt>1</tt> according to whether the value of
     * <i>expression</i> is negative, zero or positive.<p>
     *
     * The implementor must ensure that <tt>sgn(compare(x, y)) ==
     * -sgn(compare(y, x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
     * implies that <tt>compare(x, y)</tt> must throw an exception if and only
     * if <tt>compare(y, x)</tt> throws an exception.)<p>
     *
     * The implementor must also ensure that the relation is transitive:
     * <tt>((compare(x, y)&gt;0) &amp;&amp; (compare(y, z)&gt;0))</tt> implies
     * <tt>compare(x, z)&gt;0</tt>.<p>
     *
     * Finally, the implementor must ensure that <tt>compare(x, y)==0</tt>
     * implies that <tt>sgn(compare(x, z))==sgn(compare(y, z))</tt> for all
     * <tt>z</tt>.<p>
     *
     * It is generally the case, but <i>not</i> strictly required that
     * <tt>(compare(x, y)==0) == (x.equals(y))</tt>.  Generally speaking,
     * any comparator that violates this condition should clearly indicate
     * this fact.  The recommended language is "Note: this comparator
     * imposes orderings that are inconsistent with equals."
     *
     * @param o1 the first float to be compared.
     * @param o2 the second float to be compared.
     * @return a negative integer, zero, or a positive integer as the
     *         first argument is less than, equal to, or greater than the
     *         second.
     */
    int compare (float o1, float o2);

   /**
    * Indicates whether some other object is &quot;equal to&quot; this
    * comparator.  This method must obey the general contract of
    * {@link Object#equals(Object)}.  Additionally, this method can return
    * <tt>true</tt> <i>only</i> if the specified object is also a comparator
    * and it imposes the same ordering as this comparator.  Thus,
    * <code>comp1.equals(comp2)</code> implies that <tt>sgn(comp1.compare(o1,
    * o2))==sgn(comp2.compare(o1, o2))</tt> for every object reference
    * <tt>o1</tt> and <tt>o2</tt>.<p>
    *
    * Note that it is <i>always</i> safe <i>not</i> to override
    * <tt>Object.equals(Object)</tt>.  However, overriding this method may,
    * in some cases, improve performance by allowing programs to determine
    * that two distinct comparators impose the same order.
    *
    * @param   obj   the reference object with which to compare.
    * @return  <code>true</code> only if the specified object is also
    *          a comparator and it imposes the same ordering as this
    *          comparator.
    * @see Object#equals(Object)
    * @see Object#hashCode()
    */
    boolean equals(Object obj);
}

//  LocalWords:  FloatComparator autoboxing tt sgn signum implementor
//  LocalWords:  hashCode
