package org.bzdev.obnaming.misc;
import java.awt.BasicStroke;

/**
 * Create a {@link BasicStroke} pragmatically.
 * This class uses the same design as {@link BasicStrokeParm} but
 * can be used directly instead of by using a factory.
 * <P>
 * The rationale for this class is that
 * {@link org.bzdev.util.ExpressionParser} does not provide
 * floats, just doubles, and the constructors for {@link BasicStroke}
 * use floats.
 */
public class BasicStrokeBuilder {

    BasicStrokeParm parm = new BasicStrokeParm();

    /**
     * Set the line width.
     * The width is in user-space units (that is, in units of points).
     * @param width the line width
     * @return this object
     */
    public BasicStrokeBuilder setWidth(double width) {
	parm.width = width;
	return this;
    }

    /**
     * Get the line width.
     * The width is in user-space units (that is, in units of points).
     * @return  the line width
     */
    public double getWidth() {return parm.width;}

    /**

     * Set the cap for a line.
     * The cap is a decoration applied to the start or end of an
     * enclosed subpath of a line.
     * @param cap the cap
     * @return this object
     */
    public BasicStrokeBuilder setCap(BasicStrokeParm.Cap cap) {
	parm.cap = cap;
	return this;
    }

    /**
     * Get the cap for a line.
     * The cap is a decoration applied to the start or end of an
     * enclosed subpath of a line.
     * @return the current cap
     */
    public BasicStrokeParm.Cap getCap() {return parm.cap;}

    /**
     * Set the join type.
     * The choices are beveled, mitered, and round.
     * @param join decoration used to join two line segments
     * @return this object
     */
    public BasicStrokeBuilder setJoin(BasicStrokeParm.Join join) {
	parm.join = join;
	return this;
    }
 
    /**
     * Get the joint type.
     * The choices are beveled, mitered, and round.
     * @return the decoration used to join two line segments
     */
    public BasicStrokeParm.Join getJoin() {return parm.join;}

    /**
     * Set the miter limit.
     * Mitered joins are trimmed when the ratio of the miter length to the
     * stroke width is greater than the miter limit. Trimming converts the
     * miter decoration to a bevel decoration.
     * @param miterLimit the miter limit
     * @return this object
     */
   public BasicStrokeBuilder setMiterLimit(double miterLimit) {
	parm.miterLimit = miterLimit;
	return this;
    }

    /**
     * Get the miter limit.
     * Mitered joins are trimmed when the ratio of the miter length to the
     * stroke width is greater than the miter limit. Trimming converts the
     * miter decoration to a bevel decoration.
     * @return the miter limit
     */
    public double getMiterLimit() {return parm.miterLimit;}

    /**
     * Set the dash phase.
     * The dash phase is the distance in user space representing the
     * offset into a dash pattern at which to start.
     * @param phase the dash phase
     * @return this object
     */
    public BasicStrokeBuilder setDashPhase(double phase) {
	parm.dashPhase = phase;
	return this;
    }

    /**
     * Get the dash phase.
     * The dash phase is the distance in user space representing the
     * offset into a dash pattern at which to start.
     * @return the dash phase
     */
    public double getDashPhase() {return parm.dashPhase;}

    /**
     * Set the dash increment.
     * Dashed lines are specified by a string consisting of the
     * characters "-" and " ". Each character is replaced with
     * a dash or space whose user-space length is this value.
     * @param dashIncr the dash increment
     * @return this object
     */
    public BasicStrokeBuilder setDashIncr(double dashIncr) {
	parm.dashIncrement = dashIncr;
	return this;
    }

    /**
     * Get the dash increment.
     * Dashed lines are specified by a string consisting of the
     * characters "-" and " ". Each character is replaced with
     * a dash or space whose user-space length is this value.
     * @retrun the dash increment
     */
    public double getDashIncr() {return parm.dashIncrement;}

    /**
     * Set the dash pattern.
     * Dashed lines are specified by a string consisting of the
     * characters "-" and " ".  If the string has a length of zero or
     * consists a sequence of '-' with no spaces, the line is
     * solid. Otherwise the pattern should be represented by a
     * sequence of "-" or " " giving the length of dashes or empty
     * space respectively as multiples of the value of
     * dashIncrement. Such a pattern must start with a '-' and end
     * with a ' '.* Each "-" and each " " represents a component of a
     * segment whose length is dashIncrement.  Thus, if dashIncrement
     * is 10.0, then the pattern "-- - " will consist of a dash of
     * length 20.0, a space of length 10.0, a dash of length 10.0, and
     * a space of length 10.0, with the pattern repeating as needed.
     * Note that a pattern ending with a '-' is not necessary as the
     * dashPhase parameter can be used to create the same effect.
     * @param pattern the dash pattern
     * @return this object
     */
    public BasicStrokeBuilder setDashPattern(String pattern) {
	parm.dashPattern = pattern;
	return this;
    }

    /**
     * Set the dash pattern.
     * Dashed lines are specified by a string consisting of the
     * characters "-" and " ".  If the string has a length of zero,
     * the line is solid. Otherwise the pattern should be represented
     * by a sequence of "-" or " " giving the length of dashes or
     * empty space respectively as multiples of the value of
     * dashIncrement.
     * @return the dash pattern
     */
    public String getDashPattern() {return parm.dashPattern;}

    /**
     * Create a stroke.
     * @return the stroke corresponding to this builder's
     *         configuration.
     */
    public BasicStroke createStroke() {
	return parm.createBasicStroke();
    }
}

//  LocalWords:  BasicStroke BasicStrokeParm subpath miterLimit
//  LocalWords:  dashIncr dashIncrement
