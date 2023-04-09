package org.bzdev.graphs;
import org.bzdev.lang.MathOps;
import org.bzdev.util.SciFormatter;
import java.util.LinkedList;
import java.awt.Color;
import java.util.TimeZone;
import java.util.SimpleTimeZone;
import java.util.Calendar;
import java.util.GregorianCalendar;

//@exbundle org.bzdev.graphs.lpack.Graphs

/**
 * Base class for axis builders.
 * In addition to some methods shared by all subclasses,
 * this class provides a number of protected fields that
 * contain configuration data used by subclasses. These
 * fields should be treated as read only.
 * <P>
 * The classes {@link Graph.Axis} and {@link Graph.TickSpec}
 * can be used directly to create an axis. The subclasses
 * of {@link AxisBuilder} provide a simpler API for common
 * cases.
 */
public abstract class AxisBuilder<T extends Graph.Axis> {

    static String errorMsg(String key, Object... args) {
	return Graph.errorMsg(key, args);
    }

    /**
     * The graph on which an axis will be drawn.
     * Note: the axis must be drawn explicitly by
     * calling {@link Graph#draw(Graph.Axis)}.
     * This is not done automatically because the
     * caller might need to control the order in
     * which various objects are drawn, including
     * axes.
     */
    protected Graph graph;

    /**
     * The starting X position for the axis in graph coordinate space.
     */
    protected double startX;

    /**
     * The starting Y position for the axis in graph coordinate space.
     */
    protected double startY;

    /**
     * The length of the axis.  This is never negative.
     * The ending coordinates in graph coordinate space are never
     * lower than the starting coordinates.
     */
    protected double length;

    /**
     * The direction of the graph. The values of this field are
     * either {@link Graph.Axis.Dir#HORIZONTAL_INCREASING} or
     * {@link Graph.Axis.Dir#VERTICAL_INCREASING}.
     */
    protected Graph.Axis.Dir direction;

    /**
     * The orientation of the tick marks.  If true, with the
     * axis pointing from its start to its end in
     * graph coordinate space the tick marks in graph coordinate
     * space will lie in the counterclockwise direction. If false,
     * the tick marks lie in the clockwise direction.
     */
    protected boolean counterclockwise;

    /**
     * The length in user space of the offset. The offset is
     * the one in the direction that the tick marks point:
     * <UL>
     *   <LI> For tick marks pointing right, the lower X offset
     *        is used.
     *   <LI> For tick marks pointing left, the upper X offset
     *        is used.
     *   <LI> For tick marks pointing down, the lower Y offset
     *        is used.
     *   <LI> For tick marks pointing up, the upper Y offset
     *        is used.
     * </UL>
     */
    protected double offset;

    /**
     * The value of the graph's range that is closest to the left or lower
     * edge of the graph (excluding offsets). The left edge is used for
     * horizontal axes and the lower edge is used for vertical axes.
     * The value is in graph coordinate space units.
     */
    protected double lower;

    /**
     * The value of the graph's range that is closest to the right or upper
     * edge of the graph (excluding offsets). The right edge is used for
     * horizontal axes and the upper edge is used for vertical axes.
     * The value is in graph coordinate space units.
     */
    protected double upper;

    /**
     * The length of a graph in user space and in the direction of
     * the axis, excluding the offsets. This is the maximum length
     * of an axis if it is to fit in the graph's range.
     */
    protected double userLength; 

    /**
     * The length of the graph in graph coordinate space and in the
     * direction of the axis, excluding the offsets. This is the maximum
     * length of an axis if it is to fit in the graph's range.
     */
    protected double stdLength;

    /**
     * The label for an axis; null if there is no label.
     */
    protected String label = null;

    /**
     * The separation from an axis label and the tick marks
     * or tick-mark labels. The value is in user-space units.
     * The default value is 5.0.
     */
    protected double labelSep = 5.0;

    private final double DEFAULT_WIDTH = 1.5;
    /**
     * The width of the axis. This is in user-space units.
     */
    protected double width = DEFAULT_WIDTH;

    /**
     * The color for an axis.
     * A null value implies that a default color will be used.
     * This parameter controls the color of an axis and tick
     * marks, not the color of labels (the color of labels is
     * specified by a font parameter).
     * @see #setFontParms(Graph.FontParms)
     */
    protected Color color = null;

    /**
     * Set the color of the axis.
     * @param color the color; null for the default
     */
    public void setColor(Color color) {
	this.color = color;
    }

    /**
     * The fontParms for this axis. A null value implies that
     * the graph's font parameters should be used.
     */
    protected Graph.FontParms fontParms = null;

    /**
     * Set the font parameters for this axis builder.
     * The font parameters will determine the font to use
     * and the color of the text.
     * @param parms the font parameters to copy
     */
    public void setFontParms(Graph.FontParms parms) {
	if (parms == null) {
	    fontParms = null;
	}
	try {
	    fontParms = (Graph.FontParms)(parms.clone());
	} catch (CloneNotSupportedException e) {
	     // cannot happen -- fontParms is not null and is defined
	     // to be cloneable.
	     return;
	}
    }

    /**
     * Indicate if tick labels are constrained to be horizontal.
     * True if the labels must be horizontal; false otherwise.
     * The default is <code>true</code>. If <code>false</code>
     * vertical axes are affected but no horizontal axes.
     */
    protected boolean tickLabelsHorizontal = true;

    /**
     * Set whether or not tick labels are constrained to be horizontal.
     * This mode has no effect unless an axis is vertical.
     * The default is <code>true</code>.
     * @param mode true if tick labels must be horizontal;
     *        false if they may be vertical
     */
    public void setTickLabelsHorizontal(boolean mode) {
	tickLabelsHorizontal = mode;
    }

    /**
     * Determine whether or not tick labels are constrained to be
     * horizontal.
     * This mode has no effect unless an axis is vertical.
     * @return true if tick labels must be horizontal;
     *        false if they may be vertical
     */
    public boolean tickLabelsAreHorizontal() {
	return tickLabelsHorizontal;
    }

    /**
     * The axis scale.
     * @see #setAxisScale
     * @see Graph.Axis#setAxisScale(double)
     */
    protected double axisScale = 1.0;

    /**
     * Set the axis scale.
     * The axis value that {@link Graph.Axis#axisValue(long)} returns
     * will be divided by this factor. It does not affect the
     * value returned by {@link Graph.Axis#axisCoord(long)}.
     * The default value is 1.0.
     * <P>
     * The scale factor can be used to change units.  For example,
     * if a graph was configured so that the X axis shows distances
     * in meters, setting this factor to 10<sup>3</sup> will allow
     * the axis to label tick marks in units of kilometers.
     * <P>
     * The value that should be passed to methods such as
     * {@link org.bzdev.graphs.AxisBuilder.Linear#setMaximumExponent(int)} must
     * be based on the value before this scaling factor is applied (e.g,
     * the valuel when this scaling factor is 1.0).
     * @param scaleFactor the scaleFactor
     * @exception IllegalArgumentException if the scale factor is not
     *             a positive double-precision number
     * @see Graph.Axis#setAxisScale(double)
     */
    public void setAxisScale(double scaleFactor)
	throws IllegalArgumentException
    {
	if (scaleFactor < Double.MIN_NORMAL) {
	    String msg = errorMsg("scaleFactor", scaleFactor);
	    throw new IllegalArgumentException(msg);
	}
	axisScale = scaleFactor;
    }

    /**
     * Get the axis scale.
     * @return the value for the axis scale
     * @see Graph.Axis#setAxisScale(double)
     */
    public double getAxisScale() {
	return axisScale;
    }


    /**
     * Constructor.
     * @param g the graph
     * @param startX the X value in graph coordinate space for
     *        the start of the axis.
     * @param startY the Y value in graph coordinate space for
     *        the start of the axis
     * @param length the length of the axis in graph coordinate space units
     * @param horizontal true if the axis is horizontal; false if it is vertical
     * @param label the level for an axis; null if none is provided
     */
    protected AxisBuilder(Graph g,
			  double startX,
			  double startY,
			  double length,
			  boolean horizontal,
			  String label)
    {
	this(g, startX, startY,length, horizontal, false, label);
    }

    /**
     * Constructor with a "flip" option.
     * Normally the tick marks for an axis are below the axis for
     * a horizontal axis and to the left of an axis for a vertical
     * axis. An option to flip the tick marks and labels to the
     * opposite side of the axis is provided by this constructor
     * for cases where an axis appears at the top of a graph or
     * at its right side.
     * @param g the graph
     * @param startX the X value in graph coordinate space for
     *        the start of the axis.
     * @param startY the Y value in graph coordinate space for
     *        the start of the axis
     * @param length the length of the axis in graph coordinate space units
     * @param horizontal true if the axis is horizontal; false if it is vertical
     * @param flip true of the tick marks should be flipped to the opposite
     *        side of the axis; false to use the default side
     * @param label the level for an axis; null if none is provided
     */
    protected AxisBuilder(Graph g,
			  double startX,
			  double startY,
			  double length,
			  boolean horizontal,
			  boolean flip,
			  String label)
    {
	graph = g;
	this.startX = startX;
	this.startY = startY;
	this.length = length;
	this.label = label;

	double xLower = g.getXLower();
	double xUpper = g.getXUpper();
	double yLower = g.getYLower();
	double yUpper = g.getYUpper();
	if (horizontal) {
	    double middle = (yLower + yUpper)/2.0;
	    direction = Graph.Axis.Dir.HORIZONTAL_INCREASING;
	    counterclockwise = flip;
	    offset = flip? g.getYUpperOffset(): g.getYLowerOffset();
	    lower = xLower;
	    upper = xUpper;
	    userLength = g.getWidth()
		- (g.getXLowerOffset() + g.getXUpperOffset());
	    stdLength = Math.abs(xUpper - xLower);
	} else {
	    double middle = (xLower + xUpper)/2.0;
		direction = Graph.Axis.Dir.VERTICAL_INCREASING;
	    if (yLower < yUpper) {
		counterclockwise = !flip;
	    } else {
		counterclockwise = flip;
	    }
	    offset = flip? g.getXUpperOffset(): g.getXLowerOffset();
	    lower = yLower;
	    upper = yUpper;
	    userLength = g.getWidth()
		- (g.getYLowerOffset() + g.getYUpperOffset());
	    stdLength = Math.abs(yUpper - yLower);
	}
    }

    /**
     * Set the line width of an axis.
     * @param width the width in user space; 0.0 for the default
     */
    public void setWidth(double width) {
	if (width < 0.0) {
	    String msg = errorMsg("width", width);
	    throw new IllegalArgumentException(msg);
	}
	if (width == 0.0) width = DEFAULT_WIDTH;
	this.width = width;
    }
    
    /**
     * Get the offset for  a label.
     * The offset is the separation between tick marks and
     * any tick-mark labels and the label for the axis as
     * a whole.
     * @return the offset in user-space units
     */
    public double getLabelOffset() {
	return labelSep;
    }

    /**
     * Set the offset for a label.
     * The offset is the separation between tick marks and
     * any tick-mark labels and the label for the axis as
     * a whole. The default value is 5.0.
     * <P>
     * Note: with horizontal tick-mark labels and vertical
     * axes, the position of the label is set conservatively
     * so that the label cannot overlap a tick mark without
     * regard to the length of the label or which tick marks
     * it might overlap.  In some cases, this can put the
     * label too far to the side to be visually appealing.
     * In such cases, one might prefer to change this value
     * from the default, perhaps setting it to a negative
     * value.
     * @param offset the offset in user-space units
     */
    public void setLabelOffset(double offset) {
	labelSep = offset;
    }

    private static final double DEFAULT_LENGTHS[] = {10.0, 7.5, 5.0, 3.0, 1.5};
    private static final double DEFAULT_WIDTHS[] = {1.2, 1.0, 0.8, 0.65, 0.5};
    private static final double DEFAULT_SEPS[] = {5.0, 4.0, 3.5, 3.0, 3.0};

    private static final double DEFAULT_TICK_SCALING = 1.0;
    private double tickScalingFactor = DEFAULT_TICK_SCALING;
    private boolean linearTickScaling = false;

    /**
     * Set the tick-scaling factor.
     * This factor is used when {@link #setLinearTickScaling(boolean)} is
     * called with an argument whose value is <code>true</code>. The
     * default value is 1.0, indicating no additional scaling.
     * <P>
     * Calling {@link #setLinearTickScaling(boolean) setLinearTickScaling(true)}
     * and this method is useful when one wants to scale the
     * the tick-mark parameters for each level by a constant amount.
     * @param value the tick-scaling factor; 0 for the default
     */
    public void setTickScalingFactor (double value) {
	if (value < 0.0) {
	    String msg = errorMsg("tickScalingFactor", value);
	    throw new IllegalArgumentException(msg);
	}
	if (value == 0.0) {
	    tickScalingFactor = DEFAULT_TICK_SCALING;
	} else {
	    tickScalingFactor = value;
	}
    }

    /**
     * Get the tick-scaling factor.
     * @return the tick-scaling factor
     */
    public double getTickScalingFactor() {
	return tickScalingFactor;
    }

    /**
     * Set the tick-scaling mode
     * Linear tick scaling causes {@link #getTickScaling(double)} to
     * return a constant value set by calling
     * {@link #setTickScalingFactor(double)}.  Otherwise a non-linear
     * function is used.
     * @param mode true when tick scaling is linear; false otherwise
     */
    public void setLinearTickScaling(boolean mode) {
       linearTickScaling = mode;
    }

    /**
     * Determine the tick-scaling mode.
     * @return true if tick-scaling is linear; false otherwise
     */
    public boolean usesLinearTickScaling() {
	return linearTickScaling;
    }

    /**
     * Get the scaling factor applied to tick-mark dimensions for
     * a given axis width.
     * This method is used by {@link #createAxis()}. By default, the
     * dimensions provided for tick marks for an axis are multiplied by
     * the width of the axis to get the dimensions used. This is done
     * so that users can easily enlarge a graph.  When an axis builder
     * is used, the user is generally not enlarging the graph, so
     * a different choice is appropriate.
     * <P>
     * The behavior of this method depends on the mode passed to
     * {@link #setLinearTickScaling(boolean)}.  When the argument to
     * this method is true, a scaling factor is returned. This factor
     * has a default value of 1.0. When the argument is false (the
     * default), the scaling is nonlinear.
     * @param axisWidth the axis width in user-space units
     * @return the scaling factor
     */
    public double getTickScaling(double axisWidth) {
	if (linearTickScaling) {
	    return tickScalingFactor;
	} else {
	    double result = DEFAULT_WIDTH / axisWidth;
	    if (axisWidth > DEFAULT_WIDTH) {
		result *= 1.0 - 0.75 * Math.log(result);
	    }
	    return result;
	}
    }

    /**
     * For each level, this array provides the number by which to
     * multiple the axis width (in user-space units) to obtain the
     * tick-mark length.
     */
    protected double[] levelLengths = DEFAULT_LENGTHS;
    /**
     * For each level, this array provides the number by which to
     * multiple the axis width (in user-space units) to obtain the
     * tick-mark width.
     */
    protected double[] levelWidths = DEFAULT_WIDTHS;

    /**
     * For each level, this number provides the separation in user-space
     * units between tick labels and tick marks.
     */
    protected double[] labelSeps = DEFAULT_SEPS;

    /**
     * Get the number of levels.
     * @return the number of levels
     */
    public int getNumberOfLevels() {
	return levelLengths.length;
    }

    /**
     * Configure levels.
     * The levels are indices into the arrays provided as arguments.
     * If an array is null, the default will be restored. When used,
     * the index of the array will be a level. For the lengths and
     * widths array, the values used will be an array element multiplied
     * by the width of the axis.
     * <P>
     * The levels should be ordered so lower levels correspond to
     * ticks that are further apart.
     * The default values are given by the following arrays:
     * <UL>
     *  <LI> double lengths[] = {10.0, 7.5, 5.0, 3.0, 1.5};
     *  <LI> double widths[] =  {1.2, 1.0, 0.8, 0.65, 0.5};
     *  <LI> double labelSeps[] =  {5.0, 4.0, 3.5, 3.0, 3.0};;
     * </UL>
     * @param lengths the scaling factors for tick lengths
     * @param widths the scaling factors for tick widths
     * @param labelSeps the label separations
     */
    public void configureLevels(double[] lengths, double[] widths,
				double[] labelSeps)
    {
	if (lengths == null || widths == null || labelSeps == null) {
	    if (lengths != null || widths != null || labelSeps != null) {
		String msg = errorMsg("someNullNotAll");
		throw new IllegalArgumentException(msg);
	    }
	} else {
	    if (lengths.length != widths.length
		|| widths.length != labelSeps.length) {
		throw new IllegalArgumentException(errorMsg("arrayLengths"));
	    }
	}

	levelLengths = (lengths == null)? DEFAULT_LENGTHS: lengths.clone();
	levelWidths = (widths == null)? DEFAULT_WIDTHS: widths.clone();
	this.labelSeps =
	    (labelSeps == null)? DEFAULT_SEPS: labelSeps.clone();
    }

    /**
     * Create a new instance of an axis.
     * This is called by implementations of {@link #createAxis()} to create
     * a new instance of an axis. This method is provided to reduce
     * the necessity of reimplementing {@link #createAxis()} in
     * subclasses of (for example) {@link AxisBuilder.Linear} and
     * {@link AxisBuilder.Log} that a user might write.
     * <P>
     * Note: for {@link AxisBuilder.Log}, the tickBase argument for this
     * method is ignored (the value is computed from the other parameters
     * in this case).
     * @param startX the x coordinate of the axis' starting point in
     *        graph coordinate space
     * @param startY the y coordinate of the axis' starting point in
     *        graph coordinate space
     * @param direction the direction of the graph
     * @param length the length of the axis in graph coordinate space
     * @param tickBase the starting coordinate along the axis for
     *        graph ticks, given in graph-coordinate space
     * @param tickIncr the increment between possible tick locations
     *        in graph coordinate space units
     * @param counterclockwise the angular direction to follow to
     *        reach a graph's labels and tick marks
     * @return a new axis
     */
    protected abstract T newAxisInstance(double startX, double startY,
					 Graph.Axis.Dir direction,
					 double length,
					 double tickBase,
					 double tickIncr,
					 boolean counterclockwise);

    /**
     * Create an axis.
     * Users may add additional tick marks if desired.
     * @return the axis that was created with its tick marks added
     */
    public abstract T createAxis();
    
    /**
     * AxisBuilder for a linear axis.

     * This axis builder created a linear axis whose tick marks (if
     * any) mostly have spacings that are powers of 10.  Tick marks
     * will generally have different widths and heights, specified by
     * a level.  The coarsest spacing for possible tick locations is
     * at values of X or Y that is some power of ten 10<sup>M</sup>.
     * the remaining runs of tick marks are spaced at values equal to
     * 10<sup>M-m</sup>, where m is a non-negative integer called the
     * depth.  The value of M is set by using the method
     * {@link AxisBuilder.Linear#setMaximumExponent(int)}. Tick marks
     * at this spacing are specified by calling
     * {@link AxisBuilder.Linear#addTickSpec(int,int,boolean,String)}
     * or {@link AxisBuilder.Linear#addTickSpec(int,int,boolean,String,String)}.
     * with a second argument (the depth argument) set to 0.  More
     * closely spaced tick marks are specified by providing a depth
     * that is larger than 0. The first argument to these methods
     * is a small integer called the level. Increasing the level will
     * generally result in thinner to shorter tick marks.
     * <P>
     * As a special case, the lowest power of 10 (corresponding to
     * the maximum depth used), can be partitioned into n steps, The
     * value of n must be set by calling
     * {@link AxisBuilder.Linear#setNumberOfSteps(int)}.
     * One can then add additional tick marks by calling
     * {@link AxisBuilder.Linear#addTickSpec(int,int,String)}, where
     * the first argument is a level and the second argument is a
     * divisor of n. An example below uses this method to subdivide
     * the lowest power of 10 into quarters.
     * <P>
     * Normally when the depth increases by 1, the level should
     * increase by 1. When the boolean argument
     * for {@link AxisBuilder.Linear#addTickSpec(int,int,boolean,String)}
     * or {@link AxisBuilder.Linear#addTickSpec(int,int,boolean,String,String)}
     * is true, both the specified level and the next highest level
     * (1 + the specified level) are used, so for the next depth,
     * the level should be increased by 2 instead of 1.
     * <P>
     * A call to addTickSpec will result in a sequence of tick marks
     * with a specified spacing, tick width, and tick height.
     * When multiple calls occur, more than one tick specification will
     * apply to a given tick location. The tick mark specification
     * corresponding to the largest spacing will be used. If there is
     * a tie (a rare case in practice), other characteristics of the
     * tick marks are used to resolve the conflict.
     * The String arguments to addTickSpec are format strings
     * appropriate for double-precision vales.
     * As a summary for the use of addTickSpec,
     * <UL>
     *   <LI> for the 4-argument version, the first argument is a level that
     *      determines the dimensions of tick marks (higher levels produce
     *      smaller tick marks), the second argument is a depth (higher
     *      values correspond to closer tick marks), the third argument is
     *      a boolean indicating if marks halfway between the specified
     *      marks should be added (in this case, the next highest level
     *      will be used), and the forth argument is a format string.  An
     *      optional fifth argument can be used for the format of the
     *      middle tick mark. When the third argument is true, two tick
     *      specifications are generated, one with the level specified and
     *      the other with a level that is higher by 1.
     *   <LI> for the 3-argument version, the first argument is a level, the
     *      second argument is a divisor, and the third argument is a
     *      format string.  When a divisor other than 1 is specified, the
     *      method {@link AxisBuilder.Linear#setNumberOfSteps(int)} must
     *      have been called previously with an argument larger than 0. The
     *      divisor must divide this method's argument. The three argument
     *      method allows a subsequence of tick marks to be defined, with
     *      some skipped, with the length of the sequence an positive
     *      integer that is not a prime number.
     * </UL>
     * Be default, 5 levels are
     * supported. One should call the method
     * {@link AxisBuilder#configureLevels(double[],double[],double[])} to set
     * the tick-mark length factor, the tick-mark width factor, and
     * the label separation for each level to different values or to
     * change the number of levels that may be used.
     * <P>
     * To illustrate the effects of the addTickSpec methods, suppose
     * an instance of Graph named graph was created. Also assume
     * an instance of AccessBuilder.Linear named ab was created as
     * follows:
     * <blockquote><pre><code>
     *    AxisBuilder.Linear ab =
     *      new AxisBuilder.Linear(graph, 0.0, 0.0, 10.0, true, LABEL);
     *    ab.setMaximumExponent(0);
     * </CODE></PRE></blockquote>
     * where LABEL is a string providing a label for the graph. The
     * graph will be 10 units long and the most coarsely spaced tick
     * marks will be spaced by 1 unit in graph coordinate space.
     * The following figure shows the effects of successive calls
     * to addTickSpec when the first call's second argument is false.
     * <P style="text-align: center">
     * <img src="doc-files/axis3.png" class="imgBackground" alt="axis example">
     *<P>
     * The following figure shows the effects of successive calls
     * to addTickSpec when the first call's second argument is true.
     * In this case, the first call consumes two levels, so the
     * second call to addTickSpec sets its level to 2.
     * <P style="text-align: center">
     * <img src="doc-files/axis4.png" class="imgBackground" alt="axis example">
     *<P>
     * For the use of a divisor, consider an axis builder created as
     * follows:
     * <blockquote><pre><code>
     *    AxisBuilder.Linear ab =
     *      new AxisBuilder.Linear(graph, 0.0, 0.0, 10.0, true, LABEL);
     *    ab.setMaximumExponent(0);
     *    ab.setNumberOfSteps(4);
     * </CODE></PRE></blockquote>
     * Successive calls to addTickSpec then behave as shown in the
     * next figure:
     * <P style="text-align: center">
     * <img src="doc-files/axis5.png" class="imgBackground" alt="axis example">
     *<P>
     * Finally, the following provides examples of the full sequence of
     * operations needed to create a graph and its axes.
     * <P>
     * Example 1:
     * <blockquote><pre><code>
     *      Graph graph = new Graph(...);
     *      graph.setRanges(...)
     *      graph.setOffsets(...);
     *      AxisBuild.Linear ab
     *         = new AxisBuilder.Linear(graph, 0.0, 0,0, 10.0, true,
     *                                  "X Axis");
     *      xab.setMaximumExponent(0);
     *      xab.addTickSpec(0, 0, true, "%3.0f");
     *      xab.addTickSpec(2, 1, false, null);
     *      graph.draw(xab.createAxis());
     *
     *      AxisBuild.Linear yab =
     *         new AxisBuilder.Linear(graph, 0.0, 0,0, 10.0, false, "Y Axis");
     *      yab.setMaximumExponent(0);
     *      yab.addTickSpec(0, 0, true, "%3.0f");
     *      yab.addTickSpec(2, 1, false, null);
     *      ...
     *      graph.draw(xab.createAxis());
     *      graph.draw(yab.createAxis());
     * </CODE></PRE></blockquote>
     * The code above creates axes with large tick marks at 0.0, 1.0,
     * ... 10.0, a medium-length tick mark at 0.5, 1,5, etc., and
     * small tick marks spaced by 0.1 units in graph coordinate space.
     * <P>
     * Example 2:
     * <blockquote><pre><code>
     *      Graph graph = new Graph(...);
     *      graph.setRanges(...)
     *      graph.setOffsets(...);
     *      AxisBuild.Linear xab
     *        =  new AxisBuilder.Linear(graph, 0.0, 0,0, 12.0, true, "Ruler");
     *      xab.setMaximumExponent(0);
     *      xab.setNumberOfSteps(4);
     *      xab.addTickSpec(0, 0, false, "%3.0f");
     *      xab.addTickSpec(1, 2, null);
     *      xab.addTickSpec(2, 4, null);
     *      graph.draw(xab.createAxis());
     * </CODE></PRE></blockquote>
     * The code above creates an axis layed out like a ruler covering
     * a distance of 12 inches with numbers labeling each inch, and
     * with a shorter tick at half-inch points and the shortest tick
     * at quarter-inch points.
     */
    public static final class Linear extends AxisBuilder<Graph.Axis> {

	private int minExponent;
	private int maxExponent;
	private int nsteps = 1;

	/**
	 * Constructor.
	 * @param g the graph
	 * @param startX the X value in graph coordinate space for
	 *        the start of the axis.
	 * @param startY the Y value in graph coordinate space for
	 *        the start of the axis
	 * @param length the length of the axis in graph coordinate
	 *        space units
	 * @param horizontal true if the axis is horizontal; false if
	 *        it is vertical
	 * @param label the level for an axis; null if none is provided
	 */
	public Linear (Graph g, double startX, double startY,
			  double length, boolean horizontal,
			  String label)
	{
	    super(g, startX, startY, length, horizontal, label);
	}

	/**
	 * Constructor with a "flip" option.
	 * Normally the tick marks for an axis are below the axis for
	 * a horizontal axis and to the left of an axis for a vertical
	 * axis. An option to flip the tick marks and labels to the
	 * opposite side of the axis is provided by this constructor
	 * for cases where an axis appears at the top of a graph or
	 * at its right side.
	 * @param g the graph
	 * @param startX the X value in graph coordinate space for
	 *        the start of the axis.
	 * @param startY the Y value in graph coordinate space for
	 *        the start of the axis
	 * @param length the length of the axis in graph coordinate
	 *        space units
	 * @param horizontal true if the axis is horizontal; false if
	 *         it is vertical
	 * @param flip true of the tick marks should be flipped to
	 *        the opposite side of the axis; false to use the
	 *        default side
	 * @param label the level for an axis; null if none is provided
	 */
	public Linear(Graph g, double startX, double startY,
		      double length, boolean horizontal, boolean flip,
		      String label)
	{
	    super(g, startX, startY, length, horizontal, flip, label);
	}

	/**
	 * Set exponents to indicate the spacing between the set of
	 * tick marks with the widest separation.
	 * Tick marks may occur at the
	 * graph-coordinate-space values  10<sup>r</sup> where r is
	 * an integer. The
	 * maximum exponent is the largest value of r that is used.
	 * These values will frequently have a tick-mark label
	 * associated for them and will usually be the longest tick
	 * marks on an axis.
	 * <P>
	 * If {@link #setNumberOfSteps(int)} is called with an argument n
	 * larger than 1, then the smallest possible spacing between ticks
	 * will be 10<sup>m</sup>/n, where m is the minimum exponent.  The
	 * minimum exponent is computed automatically.
	 * @param maxExponent the maximum allowable exponent for tick
	 *        separations
	 * @see #setNumberOfSteps(int)
	 */
	public void setMaximumExponent(int maxExponent) {
	    this.minExponent = maxExponent;
	    this.maxExponent = maxExponent;
	}

	/**
	 * Set the number of steps.
	 * <P>
	 * By default, ticks will be placed at integral multiples of
	 * a minimum separation that is equal to 10<sup>m</sup> where
	 * m is the minimum exponent, computed by subtracting the
	 * maximum depth from the maximum exponent, which is set
	 * by calling {#setMaximumExponent(int)}. The maximum depth
	 * is the maximum value of the second argument to the methods
	 * {@link #addTickSpec(int,int,boolean,String)} and
	 * {@link #addTickSpec(int,int,boolean,String,String)} that
	 * were called for this axis builder.
	 * <P>
	 * This method specifies the number of steps by which the
	 * minimum separation mentioned in the preceding paragraph
	 * should be subdivided. When set to a value larger than 1,
	 * additional levels of tick marks can be added. The second
	 * argument for {@link #addTickSpec(int,int,String)} will
	 * then determine the steps at which tick marks will be shown.
	 * <P>
	 * The default number of steps is 1. Higher values are useful when
	 * an interval should be divided into subintervals other than
	 * 10. For example, setting the number of steps to 4 will
	 * allow one to create ticks that divide 10<sup>m</sup> into
	 * quarters. A typical use is to set the number of steps to 10
	 * and then call {@link #addTickSpec(int,int,String)} with its
	 * second argument set to 5: this will provide 5 ticks in between
	 * the minimum-separation tick locations used by default.
	 * @param n the number of steps.
	 * @see #setMaximumExponent(int)
	 */
	public void setNumberOfSteps(int n) {
	    if (n < 1) {
		String msg = errorMsg("numberOfSteps", n);
		throw new IllegalArgumentException(msg);
	    }
	    nsteps = n;
	}

	static class TickData {
	    boolean stepped;
	    int divisor;
	    int exponent;
	    int level;
	    int mod;
	    int modtest;
	    String format;

	    TickData(int nsteps, int minExponent,
		      int level, int exponent,String format, boolean middle)
	    {
		stepped = false;
		if (level < 0) {
		    String msg = errorMsg("level", level);
		    throw new IllegalArgumentException(msg);
		}
		this.exponent = exponent;
		this.level = level;
		this.format = format;

		mod = nsteps;
		for (int i = minExponent; i < exponent; i++) {
		    mod *= 10;
		}
		if (middle) {
		    modtest = mod/2;
		} else {
		    modtest = 0;
		}
	    }

	    TickData(int nsteps, int level, int divisor, String format) {
		stepped = true;
		if (divisor < 1) {
		    String msg = errorMsg("divisor", divisor);
		    throw new IllegalArgumentException(msg);
		}
		if (level < 0) {
		    String msg = errorMsg("level", level);
		    throw new IllegalArgumentException(msg);
		}
		this.divisor = divisor;
		this.level = level;
		this.format = format;
		mod = nsteps/divisor;
		if (mod * divisor != nsteps) {
		    String msg = errorMsg("notDiv", divisor, nsteps);
		    throw new IllegalArgumentException(msg);
		}
		modtest = 0;
	    }
	}

	static class TickEntry {
	    boolean useDivisor;
	    int level;
	    int depth;
	    int divisor;
	    boolean middle;
	    String format;
	    String mformat;
	    TickEntry(int level, int depth,
		      boolean middle, String format, String mformat)
	    {
		useDivisor = false;
		this.level = level;
		this.depth = depth;
		this.middle = middle;
		this.format = format;
		this.mformat = mformat;
	    }
	    TickEntry(int level, int divisor, String format) {
		useDivisor = true;
		this.level = level;
		this.divisor = divisor;
		this.format = format;
	    }
	    public void process(LinkedList<TickData> list,
				int minExponent, int maxExponent, int nsteps)
	    {
		if (useDivisor) {
		    list.add(new TickData(nsteps, level, divisor, format));
		} else {
		    int exponent = maxExponent - depth;
		    if (middle) {
			list.add(new TickData(nsteps, minExponent, level,
					      exponent, format, false));
			list.add(new TickData(nsteps, minExponent, level+1,
					      exponent, mformat, true));
		    } else {
			list.add(new TickData(nsteps, minExponent, level,
					      exponent, format, false));
		    }
		}
	    }
	}

	LinkedList<TickEntry> elist = new LinkedList<>();

	/**
	 * Add a tick specification with one format specification.
	 * This method adds a tick specification for sets of ticks spaced by
	 * the distance 10<sup>n</sup> when the "middle" argument is
	 * false and the distance 10<sup>n</sup>/2 when the "middle"
	 * argument is true, where n equals the depth
	 * subtracted from the maximum exponent, and where n is
	 * larger than or equal to the  minimum exponent provided
	 * by a previous call to {@link #setMaximumExponent(int)}.  The level
	 * is an index for the set of parameters configured in a call to
	 * {@link AxisBuilder#configureLevels(double[],double[],double[])}
	 * These arrays determine a tick mark's length, width,
	 * and the separation of the label form its tick mark (when
	 * the format argument is not null).
	 * @param level the level for this set of tick marks
	 *        (level+1 will also be used if middle is true)
	 * @param depth the depth
	 * @param middle false if the tick marks are to be placed
	 *        at the positions k (10<sup>M-m</sup>) and
	 *        true if the tick mark is  placed at the
	 *        positions k (10<sup>M-m</sup>/2), with k an integer
	 *        such that a tick mark touches the axis
	 * @param format the format string used to create a
	 *        label; null if there is no label
	 * @exception IllegalArgumentException the argument n was lower
	 *            then the minimum exponent
	 */
	public void addTickSpec(int level, int depth, boolean middle,
				String format) {
	    addTickSpec(level, depth, middle, format, null);
	}

	/**
	 * Add a tick specification with two formats.
	 * This method adds a tick specification for sets of ticks
	 * spaced by the distance 10<sup>n</sup> when the "middle"
	 * argument is false and the distance 10<sup>n</sup>/2 when
	 * the "middle" argument is true, where n equals the depth
	 * subtracted from the maximum exponent that was set
	 * by a previous call to {@link #setMaximumExponent(int)}.  The level
	 * is the index for a set of parameters configured in a call to
	 * {@link AxisBuilder#configureLevels(double[],double[],double[])}
	 * These arrays determine a tick mark's length, width,
	 * and the separation of the label form its tick mark (when
	 * the format argument is not null).
	 * @param level the level for this set of tick marks
	 *        (level+1 will also be used if middle is true)
	 * @param depth the depth
	 * @param middle false if the tick marks are to be placed
	 *        at the positions m (10<sup>n</sup> and
	 *        true if the tick mark is  placed at the
	 *        positions m (10<sup>n</sup>/2), with m an integer
	 *        such that a tick mark touches the axis
	 * @param format the format string used to create a
	 *        label; null if there is no label
	 * @param mformat the format used to create a label for
	 *        ticks added due to the 'middle' argument being true;
	 *        null if there are none.
	 * @exception IllegalArgumentException the argument n was lower
	 *            then the minimum exponent
	 */
	public void addTickSpec(int level, int depth, boolean middle,
				String format, String mformat) {
	    if (depth < 0) {
		String msg = errorMsg("depth", depth);
		throw new IllegalArgumentException(msg);
	    }
	    int exponent = maxExponent - depth;
	    if (middle) exponent--;
	    if (minExponent > exponent) minExponent = exponent;
	    elist.add(new TickEntry(level, depth, middle, format, mformat));
	}

	/**
	 * Add a tick specification using a divisor.
	 * This method adds a tick specification that places tick marks at
	 * intervals given by 10<sup>m</sup> / n, were m is the
	 * minimum exponent and n is a divisor of nsteps, the
	 * parameter set by calling {@link #setNumberOfSteps(int)}.
	 * Like other methods with this name, the level is
	 * the index for a set of parameters configured in a call to
	 * {@link AxisBuilder#configureLevels(double[],double[],double[])}
	 * These arrays determine a tick mark's length and width
	 * @param level the level for this set of tick marks
	 * @param divisor the divisor
	 * @param format the format string used to create a
	 *        label; null if there is no label
	 * @see #setNumberOfSteps(int)
	 */
	public void addTickSpec(int level, int divisor, String format)
	{
	    elist.add(new TickEntry(level, divisor, format));
	}

	@Override
	protected Graph.Axis newAxisInstance(double startX, double startY,
					     Graph.Axis.Dir direction,
					     double length,
					     double tickBase,
					     double tickIncr,
					     boolean counterclockwise)
	{
	    return new Graph.Axis(startX, startY, direction, length,
				  tickBase, tickIncr, counterclockwise);
	}


	@Override
	public Graph.Axis createAxis() {
	    LinkedList<TickData> list = new LinkedList<>();
	    for (TickEntry entry: elist) {
		entry.process(list, minExponent, maxExponent, nsteps);
	    }
	    double tickIncr = MathOps.pow(10.0, minExponent) / nsteps;
	    double start;
	    switch(direction) {
	    case HORIZONTAL_INCREASING:
	    case HORIZONTAL_DECREASING:
		start = startX;
		break;
	    case VERTICAL_INCREASING:
	    case VERTICAL_DECREASING:
		start = startY;
		break;
	    default:
		throw new org.bzdev.lang.UnexpectedExceptionError();
	    }

	    double tickBase;
	    if (start == 0.0) tickBase = 0.0;
	    if (start > 0) {
		long k = (Math.round(Math.floor(start/tickIncr))/10L)*10L;
		tickBase = k;
	    } else {
		long k = (Math.round(Math.ceil(-start/tickIncr))/10L)*10L;
		tickBase = -k;
	    }

	    Graph.Axis axis = newAxisInstance(startX, startY, direction,
					      length, tickBase,
					      tickIncr, counterclockwise);
	    axis.setAxisScale(axisScale);
	    axis.setLabelOffset(labelSep);
	    axis.setLabel(label);
	    axis.setWidth(width);
	    axis.setTickLabelsHorizontal(tickLabelsHorizontal);
	    if (color != null) axis.setColor(color);
	    if (fontParms != null) axis.setFontParms(fontParms);

	    double scaling = getTickScaling(width);
	    for (TickData tickdata: list) {
		double stringOffset = labelSeps[tickdata.level] * scaling;
		double length = levelLengths[tickdata.level] * scaling;
		double width = levelWidths[tickdata.level] * scaling;
		if (tickdata.stepped) {
		    axis.addTick(new Graph.TickSpec(length, width, tickdata.mod,
						    tickdata.format,
						    stringOffset));
		} else {
		    axis.addTick(new Graph.TickSpec(length, width, tickdata.mod,
						    tickdata.modtest,
						    tickdata.format,
						    stringOffset));
		}
	    }
	    return axis;
	}
    }

    /**
     * AxisBuilder for a log axis.
     * This axis builder creates an axis whose tick marks are
     * spaced on a logarithmic scale.
     * The coarsest spacings on on boundaries that are spaced
     * by factors of 10 so that the logarithms to base 10 have
     * integral values. From 1.0 to 10.0, there are 9 possible
     * locations for tick marks. Each level of additional subdivisions
     * will multiple the possible number of tick marks from 1.0 to
     * 10.0 by factors of 10.
     * <P>
     * For a logarithmic axis, the coordinate of the graph (X or Y)
     * is the logarithm to base 10 of the desired value. Thus, a graph
     * whose X values are on a logarithmic scale for values from 1.0
     * to 100.0 would set the range for the X coordinate from 0.0 to 2.0.
     * <P>
     * The constructors use the same arguments as used by
     * {@link AxisBuilder} and {@link AxisBuilder.Linear}. After
     * constructing an axis builder, one will proceed by calling
     * the methods
     * {@link AxisBuilder.Log#addTickSpec(int,boolean,String)},
     * {@link AxisBuilder.Log#addTickSpec(int,boolean,String,String)},
     * {@link AxisBuilder.Log#addTickSpec(int,int)},
     * {@link AxisBuilder.Log#addTickSpec(int,int,int)},
     * {@link AxisBuilder.Log#addTickSpec(int,int,int,int)},
     * and/or {@link AxisBuilder.Log#addOneTick(int,double)}.
     * Finally one will call {@link AxisBuilder.Log#createAxis()}.
     * The following figure shows the effect of calling these
     * methods:
     * <P style="text-align: center">
     * <img src="doc-files/axis6.png" class="imgBackground" alt="axis example">
     */
    public static class Log extends AxisBuilder<Graph.LogAxis> {
	/**
	 * Constructor.
	 * @param g the graph
	 * @param startX the X value in graph coordinate space for
	 *        the start of the axis.
	 * @param startY the Y value in graph coordinate space for
	 *        the start of the axis
	 * @param length the length of the axis in graph coordinate
	 *        space units
	 * @param horizontal true if the axis is horizontal; false if
	 *        it is vertical
	 * @param label the level for an axis; null if none is provided
	 */
	public Log (Graph g, double startX, double startY,
		    double length, boolean horizontal,
		    String label)
	{
	    super(g, startX, startY, length, horizontal, label);
	}

	/**
	 * Constructor with a "flip" option.
	 * Normally the tick marks for an axis are below the axis for
	 * a horizontal axis and to the left of an axis for a vertical
	 * axis. An option to flip the tick marks and labels to the
	 * opposite side of the axis is provided by this constructor
	 * for cases where an axis appears at the top of a graph or
	 * at its right side.
	 * @param g the graph
	 * @param startX the X value in graph coordinate space for
	 *        the start of the axis.
	 * @param startY the Y value in graph coordinate space for
	 *        the start of the axis
	 * @param length the length of the axis in graph coordinate
	 *        space units
	 * @param horizontal true if the axis is horizontal; false if
	 *         it is vertical
	 * @param flip true of the tick marks should be flipped to
	 *        the opposite side of the axis; false to use the
	 *        default side
	 * @param label the level for an axis; null if none is provided
	 */
	public Log(Graph g, double startX, double startY,
		   double length, boolean horizontal, boolean flip,
		   String label)
	{
	    super(g, startX, startY, length, horizontal, flip, label);
	}

	private String tickLabelFormat = null;
	private String tickLabelMFormat = null;
	private int maxDepth = 0;

	static class LogTickData {
	    boolean top;
	    int level;
	    int depth;
	    boolean middle;
	    int divisor;
	    int cutoff;
	    int mod;
	    int modtest;
	    int limit;
	    boolean usePosition = false;
	    int position = 0;
	    int scaling = 1;

	    LogTickData(int level, int intPosition, int scaling) {
		this.top = false;
		this.level = level;
		this.usePosition = true;
		this.position = intPosition;
		this.scaling = scaling;
	    }

	    LogTickData(int level, int depth, boolean middle) {
		top = true;
		this.level = level;
		this.depth = depth;
		this.middle = middle;
		this.divisor = 1;
	    }

	    LogTickData(int level, int depth, int divisor, int cutoff) {
		this.top = false;
		this.level = level;
		this.depth = depth;
		this.divisor = divisor;
		this.cutoff = cutoff;
	    }
	}

	LinkedList<LogTickData> list = new LinkedList<>();

	/**
	 * Add a top-level tick specification with a format.
	 * This will place tick marks at values that are integral
	 * values of 10.  The format string will be used by the class
	 * {@link org.bzdev.util.SciFormatter} to generate the label.
	 * When the second argument is true, a second set of tick
	 * marks will appear at values equal to 5 multiplied by a
	 * 10 raised to an integral power.
	 * <P>
	 * Note: the level is by default an integer in the range [0, 5).
	 * The range and corresponding dimensions can be changed by
	 * calling
	 * {@link AxisBuilder#configureLevels(double[],double[],double[])}.
	 * For this method, the level 0 is always used, If the second
	 * argument is true, level 1 is also used.
	 * @param level a non-negative integer specifying the dimensions of
	 *        tick marks and the separation between a tick
	 *        mark and any tick-mark label
	 * @param middle true if a tick mark should appear for values
	 *        equal to 5 multiplied by a 10 raised to an integral
	 *        power; false otherwise.
	 * @param format the format string used to construct a tick-mark
	 *        label; null if no label should be shown
	 */
	public void addTickSpec(int level, boolean middle, String format) {
	    addTickSpec(level, middle, format, null);
	}

	/**
	 * Add a top-level tick specification with two formats.
	 * This will place tick marks at values that are integral
	 * values of 10.  The format string will be used by the class
	 * {@link org.bzdev.util.SciFormatter} to generate the label.
	 * When the second argument is true, a second set of tick
	 * marks will appear at values equal to 5 multiplied by a
	 * 10 raised to an integral power. The final argument provides
	 * a format string for this case and will cause a label to
	 * be generated when the string is not null;
	 * <P>
	 * Note: the level is by default an integer in the range [0, 5).
	 * The range and corresponding dimensions can be changed by
	 * calling
	 * {@link AxisBuilder#configureLevels(double[],double[],double[])}.
	 * @param level a non-negative integer specifying the dimensions of
	 *        tick marks and the separation between a tick
	 *        mark and any tick-mark label
	 * @param middle true if a tick mark should appear for values
	 *        equal to 5 multiplied by a 10 raised to an integral
	 *        power; false otherwise.
	 * @param format the format string used to construct a tick-mark
	 *        label; null if no label should be shown
	 * @param mFormat the format string used to construct a tick-mark
	 *        label in the middle of a decade; null if no label should
	 *        be shown
	 */
	public void addTickSpec(int level, boolean middle, String format,
				String mFormat) {
	    if (middle && maxDepth == 0) maxDepth = 1;
	    tickLabelFormat = format;
	    tickLabelMFormat = mFormat;
	    list.add(new LogTickData(level, 0, middle));
	}

	/**
	 * Add a tick specification given a level and a depth.
	 * The level determines the visual appearance of the tick mark
	 * and its separation from an tick-mark labels.  A depth of
	 * zero corresponds to tick marks at values equal to 10
	 * raised to an integral power. Increasing the depth by
	 * 1 reduces the spacing between tick marks by a factor of
	 * 10. Thus, for a depth of 1, tick marks may appear at the values
	 * 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, and 9.0 for the
	 * decade starting at 1.0 and ending at 10.0 and will be
	 * printed at locations determined by computing the logarithm
	 * to base 10 of these values.
	 * <P>
	 * Note: the level is by default an integer in the range [0, 5).
	 * The range and corresponding dimensions can be changed by
	 * calling
	 * {@link AxisBuilder#configureLevels(double[],double[],double[])}.
	 * @param level a non-negative integer specifying the dimensions of
	 *        tick marks and the separation between a tick
	 *        mark and any tick-mark label
	 * @param depth the depth of the tick marks
	 */
	public void addTickSpec(int level, int depth)
	{
	    addTickSpec(level, depth, 1, 0);
	}

	/**
	 * Add a tick specification given a level, depth, and cutoff.
	 * The level determines the visual appearance of the tick mark
	 * and its separation from an tick-mark labels.  A depth of
	 * zero corresponds to tick marks at values equal to 10
	 * raised to an integral power. Increasing the depth by
	 * 1 reduces the spacing between tick marks by a factor of
	 * 10. Thus, for a depth of 1, tick marks may appear at the values
	 * 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, and 9.0 for the
	 * decade starting at 1.0 and ending at 10.0 and will be
	 * printed at locations determined by computing the logarithm
	 * to base 10 of these values. The cutoff limits a sequence of
	 * tick marks in each decade.  For the decade from 1.0 to 10.0,
	 * the cutoff indicates the highest value for which a tick
	 * (associated with this tick specification) will be shown. This
	 * point is independent of the depth argument. Legal values for
	 * the cutoff are 2, 3, 4, 5, 6, 7, 8, and 9.
	 * <P>
	 * Note: the level is by default an integer in the range [0, 5).
	 * The range and corresponding dimensions can be changed by
	 * calling
	 * {@link AxisBuilder#configureLevels(double[],double[],double[])}.
	 * The reason for providing a cutoff is that tick marks higher in
	 * a decade are closer together than those lower in a decade,
	 * and may start running into each other if shown over the full
	 * decade.
	 * @param level a non-negative integer specifying the dimensions of
	 *        tick marks and the separation between a tick
	 *        mark and any tick-mark label
	 * @param depth the depth of the tick marks
	 * @param cutoff the value of the cutoff
	 */
	public void addTickSpec(int level, int depth, int cutoff)
	{
	    addTickSpec(level, depth, 1, cutoff);
	}

	/**
	 * Add a tick specification given a level, depth, divisor, and cutoff.
	 * The level determines the visual appearance of the tick mark
	 * and its separation from an tick-mark labels.  A depth of
	 * zero corresponds to tick marks at values equal to 10
	 * raised to an integral power. Increasing the depth by
	 * 1 reduces the spacing between tick marks by a factor of
	 * 10. If the divisor is equal to 1, it is ignored. For
	 * values higher then 1, The tick-mark spacing indicated by
	 * the depth is partitioned. For a divisor of two, an
	 * additional tick mark at a level of (level + 1) is added to
	 * divide the interval in half
	 * and for a divisor of 5, 4 additional tick marks at a level
	 * of (level + 1)  are added* to divide the interval into fifths.
	 * <P>
	 * Thus, for a depth of 1, tick marks may appear at the values
	 * 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, and 9.0 for the
	 * decade starting at 1.0 and ending at 10.0 and will be
	 * printed at locations determined by computing the logarithm
	 * to base 10 of these values. With a divisor set to 2,
	 * additional tick marks may be shown at 1.5, 2.5, 3.5, 4.5,
	 * 5.5, 6.5, 7.5, 8.5, and 9.5.
	 * <P>
	 * The cutoff limits a sequence of tick marks in each
	 * decade.  For the decade from 1.0 to 10.0, the cutoff
	 * indicates the highest value for which a tick (associated
	 * with this tick specification) will be shown. This point is
	 * independent of the depth argument. Legal values for
	 * the cutoff are 2, 3, 4, 5, 6, 7, 8, and 9.
	 * <P>
	 * Note: the level is by default an integer in the range [0, 5).
	 * The range and corresponding dimensions can be changed by
	 * calling
	 * {@link AxisBuilder#configureLevels(double[],double[],double[])}.
	 * The reason for providing a cutoff is that tick marks higher in
	 * a decade are closer together than those lower in a decade,
	 * and may start running into each other if shown over the full
	 * decade.
	 * @param level a non-negative integer specifying the dimensions of
	 *        tick marks and the separation between a tick
	 *        mark and any tick-mark label
	 * @param depth the depth for the tick mark
	 * @param divisor the divisor
	 * @param cutoff the cutoff
	 */
	public void addTickSpec(int level, int depth, int divisor,
				int cutoff)
	{
	    if (maxDepth < depth) maxDepth = depth;
	    if ((divisor != 1) && maxDepth < (depth+1)) {
		maxDepth = depth+1;
	    }
	    list.add(new LogTickData(level, depth, divisor, cutoff));
	}

	/**
	 * Add a tick specification for one location per decade.
	 * The level determines the visual appearance of the tick mark
	 * and its separation from an tick-mark labels. The position
	 * determines the location of a single tick mark in each decade,
	 * and is specified for the decade from 1.0 to 10.0.  Thus,
	 * a position of 7.5 will place tick marks at 7.5 multiplied
	 * by 10 raised to an integral power.
	 * <P>
	 * The rational for providing this method is that there are
	 * a few corner cases in which the cutoff mechanism will
	 * prevent tick marks from being shown in the desired location.
	 * For example, with a divisor of 5 and a cutoff, one would not
	 * also create a sequence with a divisor of 2 with a higher
	 * cutoff as the two would overlap.
	 * <P>
	 * Note: the level is by default an integer in the range [0, 5).
	 * The range and corresponding dimensions can be changed by
	 * calling
	 * {@link AxisBuilder#configureLevels(double[],double[],double[])}.
	 * @param level a non-negative integer specifying the dimensions of
	 *        tick marks and the separation between a tick
	 *        mark and any tick-mark label
	 * @param position a position within the decade from 1.0 to 10.0
	 */
	public void addOneTick(int level, double position)
	    throws IllegalArgumentException
	{
	    if (position < 1.0 || position >= 10.0) {
		String msg = errorMsg("position", position);
		throw new IllegalArgumentException(msg);
	    }
	    // The ticks start at 1.0, not 0.0, so we need to subtract 1.0
	    // so we'll get the correct increment.
	    position = position - 1.0;

	    int scaling = 1;
	    int i = 1;
	    while (Math.abs(Math.rint(position) - position) > 1.e-10) {
		if (i > 5) {
		    String msg = errorMsg("scalingLimit");
		    throw new IllegalArgumentException(msg);
		}
		scaling *= 10;
		position *= 10.0;
		i++;
	    }
	    if (maxDepth < i) maxDepth = i;
	    int intPosition = (int)Math.round(position);
	    list.add(new LogTickData(level, intPosition, scaling));
	}

	@Override
	protected Graph.LogAxis newAxisInstance(double startX, double startY,
						Graph.Axis.Dir direction,
						double length,
						double tickBase,
						double tickIncr,
						boolean counterclockwise)
	{
	    return new Graph.LogAxis(startX, startY, direction, length,
				     tickIncr, counterclockwise);
	}

	@Override
	public Graph.LogAxis createAxis() {
	    double tickIncr;
	    if (maxDepth == 0) {
		tickIncr = 9.0;
	    } else {
		tickIncr = 1.0 / MathOps.pow(10.0, maxDepth-1);
	    }
	    double start;
	    switch(direction) {
	    case HORIZONTAL_INCREASING:
	    case HORIZONTAL_DECREASING:
		start = startX;
		break;
	    case VERTICAL_INCREASING:
	    case VERTICAL_DECREASING:
		start = startY;
		break;
	    default:
		throw new org.bzdev.lang.UnexpectedExceptionError();
	    }
	    // fifth argument to newAxisInstance is ignored
	    Graph.LogAxis axis = newAxisInstance(startX, startY, direction,
						 length, 0.0, tickIncr,
						 counterclockwise);
	    axis.setAxisScale(axisScale);
	    axis.setLabelOffset(labelSep);
	    axis.setLabel(label);
	    axis.setWidth(width);
	    axis.setTickLabelsHorizontal(tickLabelsHorizontal);
	    if (color != null) axis.setColor(color);
	    if (fontParms != null) axis.setFontParms(fontParms);

	    double sfactor = getTickScaling(width);

	    if (maxDepth == 0) {
		for (LogTickData tickdata: list) {
		    double stringOffset = labelSeps[tickdata.level] * sfactor;
		    double length = levelLengths[tickdata.level] * sfactor;
		    double width = levelWidths[tickdata.level] * sfactor;
		    boolean middle = tickdata.middle;
		    axis.addTick(new Graph.TickSpec(length, width, 1,
						    tickLabelFormat,
							stringOffset));
		}
	    } else {
		for (LogTickData tickdata: list) {
		    double length = levelLengths[tickdata.level] * sfactor;
		    double width = levelWidths[tickdata.level] * sfactor;
		    double sep = labelSeps[tickdata.level] * sfactor;
		    int modfactor = 1;
		    int modfactor2 = 1;
		    for (int i = 1; i < maxDepth; i++) {
			modfactor *= 10;
		    }
		    for (int i = maxDepth; i > tickdata.depth; i--) {
			modfactor2 *= 10;
		    }
		    if (tickdata.top) {
			axis.addTick(new Graph.TickSpec
				     (length, width, 9*modfactor,
				      tickLabelFormat, sep));
			if (tickdata.middle) {
			    length = levelLengths[tickdata.level+1] * sfactor;
			    width = levelWidths[tickdata.level + 1] * sfactor;
			    sep = labelSeps[tickdata.level + 1] * sfactor;
			    axis.addTick(new Graph.TickSpec
					 (length, width, 9*modfactor,
					  4*modfactor, 5*modfactor,
					  tickLabelMFormat, sep));
			}
		    } else if (tickdata.usePosition) {
			int position = tickdata.position;
			int scaling = tickdata.scaling;
			while (scaling < maxDepth) {
			    scaling++;
			    position *= 10;
			}
			int mod = 9*modfactor;
			axis.addTick(new Graph.TickSpec
				     (length, width, mod, position, -1));
		    } else {
			int mod = modfactor2 / tickdata.divisor;
			int modtest;
			modtest = 0;
			int limit = (tickdata.cutoff == 0)? -1:
			    (tickdata.cutoff - 1) * modfactor;
			axis.addTick(new Graph.TickSpec
				     (length, width, mod, modtest, limit));
		    }
		}
	    }
	    return axis;
	}
    }

    /**
     * Spacings between similar tick marks for the class
     * {@link AxisBuilder.ClockTime}.
     */
    public enum Spacing {

	/**
	 * Specifies ticks spaced on second intervals.
	 */
	SECONDS,

	/**
	 * Specifies ticks spaced on five-second intervals.
	 */
	FIVE_SECONDS,

	/**
	 * Specifies ticks spaced on ten-second intervals.
	 */
	TEN_SECONDS,

	/**
	 * Specifies ticks spaced on fifteen-second intervals.
	 */
	FIFTEEN_SECONDS,

	/**
	 * Specifies ticks spaced on thirty-second intervals.
	 */
	THIRTY_SECONDS,

	/**
	 * Specifies ticks space on minute intervals.
	 */
	MINUTES,

	/**
	 * Specifies ticks space on five-minute intervals.
	 */
	FIVE_MINUTES,

	/**
	 * Specifies ticks space on ten-minute intervals.
	 */
	TEN_MINUTES,

	/**
	 * Specifies ticks space on fifteen-minute intervals.
	 */
	FIFTEEN_MINUTES,

	/**
	 * Specifies ticks space on thirty-minute intervals.
	 */
	THIRTY_MINUTES,

	/**
	 * Specifies ticks space on hour intervals.
	*/
	HOURS,

	/**
	 * Specifies ticks space on ten-hour intervals.
	 */
	TEN_HOURS,

	/**
	 * Specifies ticks space on twelve-hour intervals.
	 */
	TWELVE_HOURS,

	/**
	 * Specifies ticks space on twenty-four-hour intervals.
	 */
	DAYS
    }

    /**
     * AxisBuilder for axes that use a combination of hours, minutes,
     * and seconds.  An axis will space tick marks so that hours are
     * divided into 60 minutes and minutes are divided into 60 seconds.
     * The distance is graph coordinate space corresponding to 1 second
     * is set by calling {@link #setOneSecond(double)} (the default
     * GCS distance is 1.0).
     * <P>
     * Ticks are characterized by a spacing and a level. The spacings
     * for this class are described by an enumeration type
     * {@link AxisBuilder.Spacing}. In addition, ticks spaced at
     * one-second intervals can be subdivided into a number of steps
     * with ticks at selective locations (the spacing between visible ticks
     * must be a divisor of the number of steps - if the number of
     * steps is 10, the visible ticks can be at the midpoint, at every
     * other step, or at all steps, with the divisors being 2, 5, or 10
     * respectively). The levels determine the length and width of tick
     * marks. Higher levels correspond to smaller or narrower tick marks.
     * <P>
     * Ticks that can be labeled are integral multiples of one second,
     * and have their format described by a format string.
     * This format string can process up to four arguments:
     * <OL>
     *   <LI> An instance of java.util.Calendar. The time actually
     *        provided in UTC time (or POSIX time), starting at
     *        00:00:00, Thursday, 1 January 1970.  The day, day of the week,
     *        and year should be ignored.
     *        Formatting directives will print the minute, hour, and second
     *        components: the minutes and seconds are given modulo 60
     *        and the hours are given modulo 12 or 24. The directives
     *        for these hours, minutes, and seconds fields are
     *        %1$TH, %1$TM, and %1$TS respectively. In each case, the
     *        field will be two characters long, possibly with leading
     *        zeros.
     *   <LI> a long providing the total time in hours. This is the
     *        total time in minutes, provided as a long, divided by
     *        60L. A useful formatting directive for this value is
     *       %2$02d. This provides two-character fields with leading
     *       zeros, with more characters added for larger values.
     *   <LI> a long providing the total time in minutes. This is
     *        the total time in seconds, provided as a long, divided
     *        by 60L. A useful formatting directive for this value is
     *       %3$02d. This provides two-character fields with leading
     *       zeros, with more characters added for larger values.
     *   <LI> a long providing the total time in seconds. This is the
     *        double-precision time in seconds rounded to the nearest
     *        integer value (only integral values are used when printing
     *        tick labels, so the rounding eliminates floating-point errors).
     *        A useful formatting directive for this value is %4$02d.
     *       This provides two-character fields with leading zeros, with
     *       more characters added for larger values.
     *   <LI> a long providing the total time in days. This is the total
     *        time in hours, provided as a long, divided by 24.
     * </OL>
     * For example, the format string <code>"%3$02d:00"</code> could be
     * used for tick-marks spaced at minute intervals, with values at or
     * above 60 printed as is. By contrast,
     * <code>"%1$TT"</code> will print the time for each tick mark
     * as hours, minutes, and seconds, each two characters long and separated
     * by colons (The Java API documentation states that <code>"%1$TT"</code>
     * is equivalent to <code>"%1$TH:%1$TM:$1$TS"</code>). In this case,
     * the minutes field will have values from 0 to 59.
     * <P>
     * After a constructor is called (its arguments are same as for
     * {@link AxisBuilder.Linear}), the user should call
     * {@link AxisBuilder.ClockTime#setSpacings(AxisBuilder.Spacing,AxisBuilder.Spacing)}
     * to set the minimum and maximum spacings.  Excluding tick
     * spacings set by using a divisor (see below), the minimum and
     * maximum spacings are the smallest and largest spacings used for
     * sequences of ticks.  When two sequences result in a tick at the
     * same location on an axis, ordering rules pick the one that will
     * be shown. Generally, longer ticks and ticks with tick labels
     * are preferred.  Tick marks and tick labels are added by using
     * the following methods:
     * <UL>
     *    <LI> addTickSpec(level, spacing). The argument <code>level</code>
     *         is an <code>int</code> and the argument
     *         <code>spacing</code> is an enumeration whose type is
     *         {@link AxisBuilder.Spacing}.  This method specifies a
     *         sequence of tick marks at intervals determined by its
     *         <code>spacing</code> argument.
     *    <LI> addTickSpec(level, spacing, format).  The argument
     *         <code>level</code> is an <code>int</code> and the
     *         argument <code>spacing</code> is an enumeration whose
     *         type is {@link AxisBuilder.Spacing}. The argument
     *         <code>format</code> is a <code>String</code> specifying
     *         a format as described above.
     *    <LI> addTickSpec(level, divisor). The argument <code>level</code>
     *         is an <code>int</code> and the argument <code>divisor</code>
     *         is also an <code>int</code>. The method
     *         {@link AxisBuilder.ClockTime#setNumberOfSteps(int)} must
     *         be called with an argument <code>n</code>, and
     *         <code>divisor</code> must divide <code>n</code> (i.e,
     *         <code>n</code> must be an integral multiple of
     *         <code>divisor</code> where the multiple is a positive
     *         integer.)  This method has no effect unless the
     *         minimum spacing is set to {@link AxisBuilder.Spacing#SECONDS}.
     * </UL>
     * Finally, {@link AxisBuilder#createAxis()} will be called to create
     * an axis. The method {@link AxisBuilder#setAxisScale(double)} should
     * be used cautiously if at all: it was designed for cases where units
     * change by factors of 10 (or some other factor), not cases in which
     * the units are mixed.
     * <P>
     * As an example, the following code
     * <blockquote><pre><code>
     *  ...
     *  AxisBuilder.ClockTime ab;
     *  ab = new AxisBuilder.ClockTime(graph,
     *                                 0.0, 0.0,
     *                                 MKS.hours(2.0),
     *                                 true,
     *                                 null);
     *
     *  ab.setSpacings(AxisBuilder.Spacing.SECONDS,
     *                 AxisBuilder.Spacing.HOURS);
     *
     *  ab.addTickSpec(0, AxisBuilder.Spacing.HOURS, "%1$TR" );
     *  ab.addTickSpec(1, AxisBuilder.Spacing.THIRTY_MINUTES,
     *                 "%1$TR" );
     *  ab.addTickSpec(1, AxisBuilder.Spacing.TEN_MINUTES, null);
     *  ab.addTickSpec(2, AxisBuilder.Spacing.MINUTES, null);
     *  graph.draw(ab.createAxis());
     *  ...
     * </CODE></PRE></blockquote>
     * will produce the following axis:
     * <P style="text-align: center">
     * <img style="width: 100%; height: auto" src="doc-files/clocktime.png"
     *  class="imgBackground" alt="clock-time axis example">
     * </P>
     */
    public static class ClockTime extends AxisBuilder<Graph.Axis> {

	static TimeZone tz = new SimpleTimeZone(0, "clocktime");

	/**
	 * Constructor.
	 * @param g the graph
	 * @param startX the X value in graph coordinate space for
	 *        the start of the axis.
	 * @param startY the Y value in graph coordinate space for
	 *        the start of the axis
	 * @param length the length of the axis in graph coordinate
	 *        space units
	 * @param horizontal true if the axis is horizontal; false if
	 *        it is vertical
	 * @param label the level for an axis; null if none is provided
	 */
	public ClockTime(Graph g, double startX, double startY,
			 double length, boolean horizontal,
			 String label)
	{
	    super(g, startX, startY, length, horizontal, label);
	}


	/**
	 * Constructor with a "flip" option.
	 * Normally the tick marks for an axis are below the axis for
	 * a horizontal axis and to the left of an axis for a vertical
	 * axis. An option to flip the tick marks and labels to the
	 * opposite side of the axis is provided by this constructor
	 * for cases where an axis appears at the top of a graph or
	 * at its right side.
	 * @param g the graph
	 * @param startX the X value in graph coordinate space for
	 *        the start of the axis.
	 * @param startY the Y value in graph coordinate space for
	 *        the start of the axis
	 * @param length the length of the axis in graph coordinate
	 *        space units
	 * @param horizontal true if the axis is horizontal; false if
	 *         it is vertical
	 * @param flip true of the tick marks should be flipped to
	 *        the opposite side of the axis; false to use the
	 *        default side
	 * @param label the level for an axis; null if none is provided
	 */
	public ClockTime(Graph g, double startX, double startY,
			 double length, boolean horizontal, boolean flip,
			 String label)
	{
	    super(g, startX, startY, length, horizontal, flip, label);
	}


	Spacing minSpacing = Spacing.SECONDS;
	Spacing maxSpacing = Spacing.SECONDS;
	double oneSecond = 1.0;
	int nsteps = 1;

	/**
	 * Set the distance in graph coordinate space corresponding to one
	 * second in time.
	 * @param gcsSecond the distance in graph coordinate space, parallel
	 *        to this axis, that corresponds to one second.
	 */
	public void setOneSecond(double gcsSecond) {
	    oneSecond = gcsSecond;
	}

	/**
	 * Get the distance in graph coordinates space, parallel to this
	 * axis, corresponding to one second in time.
	 * @return the distance in graph coordinate space representing one
	 *         second in time.
	 */
	public double getOneSecond() {
	    return oneSecond;
	}

	/**
	 * Set the minimum and maximum spacings for similar ticks.
	 * Set the minimum  and maximum spacing for sequences of ticks that
	 * may have a label.
	 * <P>
	 * The minimum spacing should be the largest minimum for which
	 * ticks will be shown, excluding sub-second tick spacings: making
	 * the value smaller reduces performance.  The value will typically
	 * be equal to the closest-spaced sequence of ticks whose spacing
	 * is at least one second.
	 * <P>
	 * The maximum spacing determines the point from which a sequence
	 * of possible tick locations will be generated. This value should
	 * be as small as possible. The value will typically be the spacing
	 * between the longest tick marks.
	 * <P>
	 * In both cases, the values are determined by an enumeration type.
	 * Legal values of AxisBuilder.Spacing for the minimum spacing
	 * are SECONDS, MINUTES, and HOURS. For the maximum spacing, the
	 * legal values are SECONDS, MINUTES, HOURS, and DAYS. The minimum
	 * must not represent a longer time interval than the maximum.
	 * @param min the minimum spacing
	 * @param max the maximum spacing.
	 */
	public void setSpacings(Spacing min, Spacing max) {
	    if (min == null) {
		String msg = errorMsg("timeSpacing", min);
		throw new IllegalArgumentException(msg);
	    }
	    if (max == null) {
		String msg = errorMsg("timeSpacing", max);
		throw new IllegalArgumentException(msg);
	    }
	    switch(min) {
	    case SECONDS:
	    case MINUTES:
	    case HOURS:
		break;
	    default:
		String msg1 = errorMsg("timeSpacing", min);
		throw new IllegalArgumentException(msg1);
	    }
	    minSpacing = min;
	    switch (max) {
	    case SECONDS:
	    case MINUTES:
	    case HOURS:
	    case DAYS:
		break;
	    default:
		String msg2 = errorMsg("timeSpacing", max);
		throw new IllegalArgumentException(msg2);
	    }
	    maxSpacing = max;
	}

	/**
	 * Set the number of steps.
	 * The number of steps is the number of subdivisions of
	 * one second at which ticks may appear.
	 * The tick increment will then be getOneSecond()/n.
	 * The default number is 1. Higher values are useful when
	 * an interval should be divided into subintervals other than
	 * 10. For example, setting the number of steps to 4 will
	 * allow one to create ticks that divide one second into
	 * quarters. If the value is 1, or the minimum spacing is
	 * not {@link AxisBuilder.Spacing#SECONDS},
	 * this parameter has no effect. To actually display ticks
	 * allowed by this method, use
	 * {@link #addTickSpec(int,int)};
	 * @param n the number of steps.
	 * @see #setSpacings(AxisBuilder.Spacing,AxisBuilder.Spacing)
	 */
	public void setNumberOfSteps(int n) {
	    if (n < 1) {
		String msg = errorMsg("numberOfSteps", n);
		throw new IllegalArgumentException(msg);
	    }
	    nsteps = n;
	}

	static class ClockTickData {
	    boolean stepped;
	    int divisor;
	    int level;
	    int mod;
	    int modtest;
	    String format;

	    ClockTickData(Spacing minSpacing, Spacing maxSpacing, int nsteps,
			  int level, Spacing spacing, String format)
	    {
		stepped = false;
		if (level < 0) {
		    String msg = errorMsg("level", level);
		    throw new IllegalArgumentException(msg);
		}
		this.level = level;
		this.format = format;
		mod = nsteps;
		switch (spacing) {
		case SECONDS:
		    break;
		case FIVE_SECONDS:
		    mod *= 5;
		    break;
		case TEN_SECONDS:
		    mod *= 10;
		    break;
		case FIFTEEN_SECONDS:
		    mod *= 15;
		    break;
		case THIRTY_SECONDS:
		    mod *= 30;
		    break;
		case MINUTES:
		    mod *= 60;
		    break;
		case FIVE_MINUTES:
		    mod *= 300;
		    break;
		case TEN_MINUTES:
		    mod *= 600;
		    break;
		case FIFTEEN_MINUTES:
		    mod *= 900;
		    break;
		case THIRTY_MINUTES:
		    mod *= 1800;
		    break;
		case HOURS:
		    mod *= 3600;
		    break;
		case TEN_HOURS:
		    mod *= 36000;
		    break;
		case TWELVE_HOURS:
		    mod *= 43200;
		    break;
		case DAYS:
		    mod *= 86400;
		    break;
		}
		switch (minSpacing) {
		case SECONDS:
		    break;
		case MINUTES:
		    mod /= 60;
		    break;
		case HOURS:
		    mod /= 3600;
		    break;
		}
	    }

	    ClockTickData(Spacing minSpacing, Spacing maxSpacing, int nsteps,
			  int level, int divisor)
	    {
		stepped = true;
		if (divisor < 1) {
		    String msg = errorMsg("divisor", divisor);
		    throw new IllegalArgumentException(msg);
		}
		if (level < 0) {
		    String msg = errorMsg("level", level);
		    throw new IllegalArgumentException(msg);
		}
		if (minSpacing != Spacing.SECONDS) {
		    String msg = errorMsg("timeSpacing", minSpacing);
		    throw new IllegalArgumentException(msg);
		}
		this.level = level;
		this.divisor = divisor;
		mod = nsteps/divisor;
		if (mod * divisor != nsteps) {
		    String msg = errorMsg("notDiv", divisor, nsteps);
		    throw new IllegalArgumentException(msg);
		}
		modtest = 0;
	    }
	}

	static class ClockTickEntry {
	    boolean useSpacing;
	    int level;
	    Spacing spacing;
	    String format;
	    int divisor;

	    ClockTickEntry(int level, Spacing spacing, String format)
	    {
		useSpacing = true;
		this.level = level;
		this.spacing = spacing;
		this.format = format;
		this.divisor = 1;
	    }
	    ClockTickEntry(int level, int divisor) {
		useSpacing = false;
		this.level = level;
		this.spacing = Spacing.SECONDS;
		this.format = null;
		this.divisor = divisor;
	    }
	    public void process(LinkedList<ClockTickData> list,
				Spacing minSpacing, Spacing maxSpacing,
				int nsteps)
	    {
		if (useSpacing) {
		    list.add(new ClockTickData(minSpacing, maxSpacing, nsteps,
					       level, spacing, format));
		} else {
		    list.add(new ClockTickData(minSpacing, maxSpacing, nsteps,
					       level, divisor));
		}
	    }
	}

	LinkedList<ClockTickEntry> elist = new LinkedList<>();

	/**
	 * Add a tick specification given a spacing.
	 * @param level the tick level, where larger numbers indicated
	 *        smaller or thinner ticks
	 * @param spacing the spacing between ticks for this sequence
	 */
	public void addTickSpec(int level, Spacing spacing) {
	    elist.add(new ClockTickEntry(level, spacing, null));
	}

	/**
	 * Add a tick specification given a spacing and format.
	 * @param level the tick level, where larger numbers indicated
	 *        smaller or thinner ticks
	 * @param spacing the spacing between ticks for this sequence
	 * @param format the format string for a tick label
	 *        (see the class documentation for
	 *        {@link AxisBuilder.ClockTime}).
	 */
	public void addTickSpec(int level, Spacing spacing, String format) {
	    elist.add(new ClockTickEntry(level, spacing, format));
	}

	/**
	 * Add a tick specification given a divisor.
	 * The divisor indicates the number of intervals separating
	 * ticks. For example, for an axis builder <CODE>cab</CODE>,
	 * calling
	 * <BLOCKQUOTE><PRE><CODE>
	 *       cab.setNumberOfSteps(10);
	 *       cab.addTickSpec(level, 5);
	 * </CODE></PRE></BLOCKQUOTE>
	 * will place ticks at a fifth of a second intervals.
	 * @param level the tick level, where larger numbers indicated
	 *        smaller or thinner ticks
	 * @param divisor a divisor of the number of steps, indicating
	 *        the number of spaces that will separate ticks over
	 *        a one-second interval.
	 */
	public void addTickSpec(int level, int divisor) {
	    elist.add(new ClockTickEntry(level, divisor));
	}

	@Override
	protected Graph.Axis newAxisInstance(double startX, double startY,
					     Graph.Axis.Dir direction,
					     double length,
					     double tickBase,
					     double tickIncr,
					     boolean counterclockwise)
	{
	    return new Graph.Axis(startX, startY, direction, length,
				  tickBase, tickIncr, counterclockwise);
	}

	@Override
	public Graph.Axis createAxis() {
	    double tickIncr;
	    switch(minSpacing) {
	    case SECONDS:
		tickIncr = oneSecond;
		break;
	    case MINUTES:
		tickIncr = 60.0 * oneSecond;
		break;
	    case HOURS:
		tickIncr = 3600.0 * oneSecond;
		break;
	    default:
		String msg1 = errorMsg("timeSpacing", minSpacing);
		throw new IllegalStateException(msg1);
	    }
	    long adiv = 1L;
	    switch(maxSpacing) {
	    case SECONDS:
		adiv = 10L;
		break;
	    case MINUTES:
		adiv = 60;
		break;
	    case HOURS:
		adiv = 3600;
		break;
	    case DAYS:
		adiv = 86400;
		break;
	    default:
		String msg2 = errorMsg("timeSpacing", maxSpacing);
		throw new IllegalStateException(msg2);
	    }

	    if (nsteps > 1) {
		tickIncr /= nsteps;
		adiv *= nsteps;
	    }
	    LinkedList<ClockTickData> list = new LinkedList<>();
	    for (ClockTickEntry entry: elist) {
		entry.process(list, minSpacing, maxSpacing, nsteps);
	    }

	    double start;
	    switch(direction) {
	    case HORIZONTAL_INCREASING:
	    case HORIZONTAL_DECREASING:
		start = startX;
		break;
	    case VERTICAL_INCREASING:
	    case VERTICAL_DECREASING:
		start = startY;
		break;
	    default:
		throw new org.bzdev.lang.UnexpectedExceptionError();
	    }

	    double tickBase;
	    if (start == 0.0) tickBase = 0.0;
	    if (start > 0) {
		long k = (Math.round(Math.floor(start/tickIncr))/adiv)*adiv;
		tickBase = k;
	    } else {
		long k = (Math.round(Math.ceil(-start/tickIncr))/adiv)*adiv;
		tickBase = -k;
	    }

	    Graph.Axis axis = newAxisInstance(startX, startY, direction,
					      length, tickBase,
					      tickIncr, counterclockwise);
	    axis.setAxisScale(axisScale);
	    axis.setLabelOffset(labelSep);
	    axis.setLabel(label);
	    axis.setWidth(width);
	    axis.setTickLabelsHorizontal(tickLabelsHorizontal);
	    if (color != null) axis.setColor(color);
	    if (fontParms != null) axis.setFontParms(fontParms);

	    double scaling = getTickScaling(width);
	    for (ClockTickData tickdata: list) {
		double stringOffset = labelSeps[tickdata.level] * scaling;
		double length = levelLengths[tickdata.level] * scaling;
		double width = levelWidths[tickdata.level] * scaling;
		axis.addTick(new Graph.TickSpec(length, width, tickdata.mod,
						tickdata.format,
						stringOffset) {
			public String getTickLabel(double s, double sc,
						   Graph.Axis axis, long ind) {
			    SciFormatter sf = new SciFormatter();
			    long tseconds = Math.round(s / oneSecond);
			    long tminutes = tseconds/60;
			    long thours = tminutes/60;
			    long tdays = thours/24;
			    Calendar calendar = new GregorianCalendar(tz);
			    calendar.setTimeInMillis(tseconds*1000L);
			    return String.format(getFormat(), calendar,
						 thours, tminutes, tseconds,
						 tdays);
			}
		    });
	    }
	    return axis;
	}
    }
}

//  LocalWords:  TickSpec AxisBuilder API startX startY indices pre
//  LocalWords:  labelSeps addTickSpec boolean configureLevels xab OL
//  LocalWords:  blockquote AxisBuild setMinExonent yab layed nsteps
//  LocalWords:  setMaximumExponent setNumberOfSteps subintervals img
//  LocalWords:  IllegalArgumentException subsequence maxExponent src
//  LocalWords:  mformat AccessBuilder call's addOneTick createAxis
//  LocalWords:  setFontParms FontParms fontParms parms cloneable dir
//  LocalWords:  setLinearTickScaling getTickScaling axisWidth UTC TT
//  LocalWords:  setTickScalingFactor tickBase tickIncr axisValue
//  LocalWords:  newAxisInstance axisCoord scaleFactor setAxisScale
//  LocalWords:  ClockTime POSIX clocktime gcsSecond getOneSecond
//  LocalWords:  setSpacings
