package org.bzdev.geom;

import org.bzdev.geom.SplinePathBuilder.WindingRule;
import org.bzdev.geom.SplinePathBuilder.CPointType;
import org.bzdev.geom.SplinePathBuilder.CPoint;

// import org.bzdev.obnaming.annotations.PrimitiveParm;
// import org.bzdev.obnaming.annotations.CompoundParmType;
import org.bzdev.lang.UnexpectedExceptionError;
import org.bzdev.math.RealValuedFunctOps;
import org.bzdev.scripting.ScriptingContext;

import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.script.ScriptException;

//@exbundle org.bzdev.geom.lpack.Geom

/**
 * SplinePath2D builder base class.
 * This class allows a SplinePath2D or its subclass to be specified
 * via a table - an array of entries, each of which specifies a point
 * along the path (including control points) and the type of line segment.
 * <P>

 * This class is not intended for public use. Its documentation is
 * inherited by its superclasses, which is why there are javadoc
 * comments.  The class hierarchy is somewhat unusual because
 * AbstractSplinePathBuilder's two subclassses differ in the type of
 * the objects they create, and those objects are created and
 * initialized the same way.
 * <P>
 * Note: this class is declared to be public even though no classes
 * outside this package create subclasses of it. The reason is that
 * scripting languages (both ESP and Nashorn) will generate errors
 * if the class is not public due to the behavior of the Java reflection
 * API.  In particular, if this class is not public, those scripting-language
 * implementations cannot find public methods such as constantWIND_NON_ZERO()
 * when given an instance of a subclass.
 */
public abstract class AbstractSplinePathBuilder<T extends SplinePath2D>
    extends ScriptingContext
{

    ScriptingContext parent = null;

    /**
     * Constructor.
     */
    protected AbstractSplinePathBuilder() {
	super();
    }

    /**
     * Constructor given a parent.
     * @param parent the parent scripting context.
     */
    protected AbstractSplinePathBuilder(ScriptingContext parent) {
	super(parent);
	this.parent = parent;
    }

    static String errorMsg(String key, Object... args) {
	return GeomErrorMsg.errorMsg(key, args);
    }

    T path = null;

    /**
     * Get the path that was build.
     * @return the path
     */
    public T getPath() {
	if (path == null) path = newSplinePath2D();
	int cpsize = cplist.size();
	if (cpsize > 0) {
	    CPoint[] array = new CPoint[cpsize];
	    cplist.toArray(array);
	    appendAux(array);
	    cplist.clear();
	}
	return path;
    }


    /**
     * Create a new path.
     * This is not a protected method because all non-abstract subclasses are
     * in the current package.
     */
    abstract T newSplinePath2D();

    /**
     * Create a new path given a windingRule.
     * This is not a protected method because all non-abstract subclasses are
     * in the current package.
     * @param windingRule the winding rule as specified by
     *        {@link java.awt.geom.Path2D Path2D}
     */
    abstract T newSplinePath2D(int windingRule);


    /**
     * Get the enum constant
     * {@link SplinePathBuilder.WindingRule#WIND_NON_ZERO}.
     * <P>
     * This is a convenience method used for scripting.
     * @return the enum constant
     */
    public final SplinePathBuilder.WindingRule constantWIND_NON_ZERO() {
	return SplinePathBuilder.WindingRule.WIND_NON_ZERO;
    }

    /**
     * Get the enum constant
     *  {@link SplinePathBuilder.WindingRule#WIND_EVEN_ODD}.
     * <P>
     * This is a convenience method used for scripting.
     * @return the enum constant
     */
    public final SplinePathBuilder.WindingRule constantWIND_EVEN_ODD() {
	return SplinePathBuilder.WindingRule.WIND_EVEN_ODD;
    }

    /**
     * Configure a spline-path builder using a scripting language,
     * using a default winding rule.
     * @param spec a scripting-language object describing the
     *        configuration
     * @exception UnsupportedOperationException this object does not
     *            support scripting
     * @exception IllegalArgumentException an error occurred while
     *             processing the  specification or winding rule.
     */
    public void configure(Object spec)
	throws UnsupportedOperationException, IllegalArgumentException
    {
	configure(SplinePathBuilder.WindingRule.WIND_NON_ZERO, spec);
    }

    private Properties configScriptProperties = null;
    private String configScriptResource =
	"/org/bzdev/geom/SplinePathBuilder.xml";
    /**
     * Configure a spline-path builder using a scripting language,
     * specifying a winding rule.
     * @param windingRule the winding rule
     * @param spec a scripting-language object describing the
     *        configuration
     * @exception UnsupportedOperationException this object does not
     *            support scripting
     * @exception IllegalArgumentException an error occurred while
     *             processing the  specification or winding rule.
     */
    public void configure(Object windingRule, Object spec)
	throws UnsupportedOperationException, IllegalArgumentException
    {
	if (parent == null) {
	    throw new UnsupportedOperationException(errorMsg("scripting"));
	}
	if (configScriptProperties == null) {
	    configScriptProperties = new Properties();
	    try {
		java.security.AccessController.doPrivileged
		    (new java.security.PrivilegedExceptionAction<Void>() {
			    public Void run() throws java.io.IOException {
				java.io.InputStream is =
				    AbstractSplinePathBuilder
				    .class.getResourceAsStream
				    (configScriptResource);
				configScriptProperties.loadFromXML(is);
				return null;
			    }
			});
	    } catch (java.security.PrivilegedActionException e) {
		configScriptProperties = null;
		String msg = errorMsg("noResource", configScriptResource);
		throw new UnsupportedOperationException(msg,
							 e.getException());
	    }
	}
	try {
	    invokePrivateFunction(configScriptProperties,
				  // ScriptingContext.PFMode.SANDBOXED,
				  "configurePathBuilder",
				  this, windingRule, spec);
	} catch (ScriptException e) {
	    String msg = errorMsg("illformedConfig");
	    Throwable ee = e;
	    Throwable cause = e.getCause();
	    while (cause != null
		   && !(cause instanceof IllegalArgumentException
		       || cause instanceof ScriptException)) {
		cause = cause.getCause();
	    }
	    if (cause != null && cause instanceof IllegalArgumentException) {
		ee = cause;
	    }
	    throw new IllegalArgumentException(msg, ee);
	}
    }

    /**
     * Initialize the path with a default winding rule.
     * Also create a new path.
     */
    public void initPath() {
	path = newSplinePath2D();
	cplist.clear();
    }

   /**
     * Initialize the path given a winding rule.
     * Also create a new path.
     * @param windingRule WindingRule.WIND_EVEN_ODD or 
     *        WindingRule.WIND_NON_ZERO.
     */
    public void initPath(WindingRule windingRule) {
	int rule;
	if (windingRule == null) windingRule = WindingRule.WIND_NON_ZERO;
	switch(windingRule) {
	case WIND_EVEN_ODD:
	    rule = Path2D.WIND_EVEN_ODD;
	    break;
	case WIND_NON_ZERO:
	default:
	    rule = Path2D.WIND_NON_ZERO;
	}
	path = newSplinePath2D(rule);
	cplist.clear();
    }

    /**
     * Initialize a path using a default winding rule and an array of
     * control points.
     * @param cpoints the control points along the segments in order
     *        of occurrence.
     */
    public void initPath(CPoint[] cpoints) {
	initPath();
	append(cpoints);
    }

    /**
     * Initialize the path given a winding rule and control points.
     * @param windingRule WindingRule.WIND_EVEN_ODD or 
     *        WindingRule.WIND_NON_ZERO.
     * @param cpoints the control points along the segments in order
     *        of occurrence.
     */
    public void initPath(WindingRule windingRule, CPoint[] cpoints) {
	initPath(windingRule);
	append(cpoints);
    }

    
    ArrayList<CPoint> cplist = new ArrayList<>();

    /**
     * Get the control points for this spline-path builder.
     * If the array that is returned describes a path that is closed,
     * reversing the list elements may result in a different starting
     * point: the exception, where the starting point does not change,
     * occurs when the initial element in the list has a type of
     * {@link CPointType#MOVE_TO}.
     * <P>
     * For a path to be reversed, each element whose type is
     * {@link CPointType#CLOSE} must be followed by an element whose
     * {@link CPointType#MOVE_TO} or {@link CPointType#MOVE_TO_NEXT}
     * unless it is the last element added to a path builder.
     * @param reversed true if the path represented by the control points
     *        should be reversed; false otherwise
     * @param af an affine transform to apply to the control points; null
     *        for the identity transform
     * @return the control points, possibly modified by the arguments
     * @exception IllegalStateException the control points are not
     *            in an allowed order
     */
    public CPoint[] getCPoints(boolean reversed, AffineTransform af) {
	CPoint[] result = new CPoint[cplist.size()];
	if (result.length == 0) return result;
	cplist.toArray(result);
	try {
	    modifyCPoints(result, reversed, af);
	} catch(IllegalArgumentException e) {
	    throw new IllegalStateException(e.getMessage(), e);
	}
	return result;
    }

    /**
     * Modify a list of control points for this spline-path builder.
     * If the array describes a path that is closed, reversing the
     * list elements may result in a different starting point: the
     * exception, where the starting point does not change, occurs when
     * the initial element in the list has a type of
     * {@link CPointType#MOVE_TO}.
     * <P>
     * For a list to be reversed, each element whose type is
     * {@link CPointType#CLOSE} must be followed by an element whose
     * {@link CPointType#MOVE_TO} or {@link CPointType#MOVE_TO_NEXT}.
     * @param list the list to modify
     * @param reversed true if the path represented by the control points
     *        should be reversed; false otherwise
     * @param af an affine transform to apply to the control points; null
     *        for the identity transform
     * @exception UnsupportedOperationException the list does not allow
     *            items to be added or removed.
     * @exception IllegalArgumentException the control-point list does
     *            not have its points in an allowed order
     */
    public static void modifyCPoints(List<CPoint> list, boolean reversed,
				     AffineTransform af)
	throws UnsupportedOperationException, IllegalArgumentException
    {
	if (reversed == false && (af == null || af.isIdentity())) {
	    return;
	}
	CPoint[] result = new CPoint[list.size()];
	result = list.toArray(result);
	modifyCPoints(result, reversed, af);
	list.clear();
	for (CPoint cpt: result) {
	    list.add(cpt);
	}
    }

    /**
     * Modify an array of control points for this spline-path builder.
     * If the array describes a path that is closed, reversing the
     * array elements may result in a different starting point: the
     * exception, where the starting point does not change, occurs when
     * the initial element in the list has a type of
     * {@link CPointType#MOVE_TO}.
     * <P>
     * For a list to be reversed, each element whose type is
     * {@link CPointType#CLOSE} must be followed by an element whose
     * {@link CPointType#MOVE_TO} or {@link CPointType#MOVE_TO_NEXT}.
     * @param array the control point sequence to modify
     * @param reversed true if the path represented by the control points
     *        should be reversed; false otherwise
     * @param af an affine transform to apply to the control points; null
     *        for the identity transform
     * @exception IllegalArgumentException the control-point array does
     *            not have its points in an allowed order
     */
    public static void modifyCPoints(CPoint[] array, boolean reversed,
				  AffineTransform af)
	throws IllegalArgumentException
    {
	if (af != null && !af.isIdentity()) {
	    for (int i = 0; i < array.length; i++) {
		CPoint cpt = array[i];
		switch(cpt.type) {
		case MOVE_TO:
		case SEG_END:
		case CONTROL:
		case SPLINE:
		    array[i] = new CPoint(cpt.type, cpt.x, cpt.y, af);
		    break;
		case SPLINE_FUNCTION:
		    array[i] = new CPoint(cpt.xfOps, cpt.yfOps,
					   cpt.t1, cpt.t2, cpt.n,
					   af);
		    break;
		default:
		    break;
		}
	    }
	}
	if (reversed) {
	    int offset = 0;
	    for (int n = 0; n < array.length; n++) {
		CPoint cpt = array[n];
		if (cpt.type == CPointType.MOVE_TO
		    || cpt.type == CPointType.MOVE_TO_NEXT) {
		    if (offset < n) {
			modifyCPoints(reversed, array, offset, n-offset);
		    }
		    offset = n;
		}
	    }
	    if (offset < array.length) {
		modifyCPoints(reversed, array, offset, array.length - offset);
	    }
	}
    }

    private static void modifyCPoints(boolean reversed, CPoint[] result,
				   int offset, int n)
	throws IllegalArgumentException
    {
	if (reversed) {
	    boolean closed = (result[offset+n-1].type == CPointType.CLOSE);
	    // just leave the closed CPoint in place.
	    if (closed) n--;
	    int n2 = n / 2;
	    int nm1 = n - 1;
	    CPointType types[]  = new CPointType[n];
	    boolean intermediateClose = false;
	    for (int i = 0; i < n2; i++) {
		CPoint tmp = result[offset+i];
		int nm1mi = n - 1 - i;
		if (result[offset+i].type == CPointType.CLOSE
		    || result[offset+nm1mi].type == CPointType.CLOSE) {
		    intermediateClose = true;
		}
		result[offset+i] = result[offset+nm1mi];
		result[offset+nm1mi] = tmp;
	    }
	    if (intermediateClose) {
		// restore the array
		for (int i = 0; i < n2; i++) {
		    CPoint tmp = result[offset+i];
		    int nm1mi = n - 1 - i;
		    result[offset+i] = result[offset+nm1mi];
		    result[offset+nm1mi] = tmp;
		}
		throw new IllegalArgumentException
		    (errorMsg("intermediateClose"));
	    }
	    boolean splineMode = true;
	    boolean stndMode = true; // just SEG_END, CONTROL, & MOVE_TO
	    for (int i = 0; i < nm1; i++) {
		if (result[offset+i].type != CPointType.SPLINE) {
		    CPoint cpnt = result[offset+i];
		    if (cpnt.type == CPointType.SPLINE_FUNCTION) {
			result[offset+i] = new CPoint(cpnt.xfOps, cpnt.yfOps,
						      cpnt.t2, cpnt.t1, cpnt.n);
		    } else {
			// If the curve is a single spline, we can treat
			// it as a special case and create a smooth closed
			// loop. If we get here, this  special case does not
			// apply
			splineMode = false;
		    }
		}
		switch(result[offset+i].type) {
		case MOVE_TO:
		case CONTROL:
		case SEG_END:
		    break;
		default:
		    stndMode = false;
		}
	    }
	    if (closed && splineMode) {
		// Try to keep the same starting point.
		CPoint tmp = result[offset + nm1];
		for (int i = nm1; i > 0; i--) {
		    result[offset+i] = result[offset +i - 1];
		}
		result[offset] = tmp;
		return;
	    } else {
		boolean permuted = false;
		boolean sawControl = false;
		switch(result[offset].type) {
		case SEG_END_PREV:
		    result[offset] = new CPoint(CPointType.MOVE_TO_NEXT);
		    break;
		case SEG_END:
		    result[offset] = new CPoint(CPointType.MOVE_TO,
						result[offset].x,
						result[offset].y);
		    break;
		case CONTROL:
		    sawControl = true;
		case SPLINE:
		case SPLINE_FUNCTION:
		    if (closed
			&& (result[offset+nm1].type == CPointType.MOVE_TO_NEXT
			    || result[offset+nm1].type == CPointType.MOVE_TO)) {
			// permute (cyclical shift)
			permuted = true;
			CPoint prevcpt = result[offset+nm1];
			for (int i = 0; i < n; i++) {
			    CPoint cpt = result[offset+i];
			    result[offset+i] = prevcpt;
			    prevcpt = cpt;
			}
			if (sawControl
			    && result[offset].type == CPointType.MOVE_TO_NEXT) {
			    switch(result[offset+nm1].type) {
			    case SPLINE:
				result[offset] =
				    new CPoint(CPointType.MOVE_TO,
					       result[offset+nm1].x,
					       result[offset+nm1].y);
				break;
			    case SPLINE_FUNCTION:
				result[offset] =
				    new CPoint(CPointType.MOVE_TO,
					       result[offset+nm1].xfOps
					       .valueAt
					       (result[offset+nm1].t2),
					       result[offset+nm1].yfOps
					       .valueAt
					       (result[offset+nm1].t2));
				break;
			    default:
				String argument =
				    result[offset].type.toString();
				throw new IllegalArgumentException
				    (errorMsg("cporder", argument));
			    }
			}
			break;
		    }
		default:
		    throw new IllegalArgumentException
			(errorMsg("cporder", result[offset].type.toString()));
		}
		if (permuted == false) {
		    switch(result[offset+nm1].type) {
		    case MOVE_TO_NEXT:
			result[offset+nm1] =
			    new CPoint(CPointType.SEG_END_PREV);
			break;
		    case MOVE_TO:
			result[offset+nm1] = new CPoint(CPointType.SEG_END,
							result[offset+nm1].x,
							result[offset+nm1].y);
			break;
		    default:
			String arg = result[offset+nm1].type.toString();
			throw new IllegalArgumentException
			(errorMsg("cporder", arg));
		    }
		}
		for (int i = 1; i < nm1; i++) {
		    switch(result[offset+i].type) {
		    case SEG_END_NEXT:
			result[offset+i] = new CPoint(CPointType.SEG_END_PREV);
			break;
		    case SEG_END_PREV:
			result[offset+i] = new CPoint(CPointType.SEG_END_NEXT);
			break;
		    default:
			break;
		    }
		}
	    }
	}
    }

    /**
     * Add a control point or operation to a path.
     * Segments can also be added by calling getPath() and using the
     * methods specific to SplinePath2D.
     * @param cpoint the control point
.
     */
    public void append(CPoint cpoint) {
	cplist.add(cpoint);
    }

    /**
     * Add segments to a path.
     * Segments can also be added by calling getPath() and using the
     * methods specific to SplinePath2D.
     * @param cpoints the control points along the segments in order
     *        of occurrence.
     */
    public void append(CPoint[] cpoints) {
	for (CPoint cpoint: cpoints) {
	    append(cpoint);
	}
    }


   /**
     * Add segments from a list to a path.
     * Segments can also be added by calling getPath() and using the
     * methods specific to SplinePath2D.
     * @param cpoints the control points along the segments in order
     *        of occurrence.
     */
    public void append(List<CPoint> cpoints) {
	for (CPoint cpoint: cpoints) {
	    append(cpoint);
	}
    }


    double lastMoveToX = 0.0;
    double lastMoveToY = 0.0;

    // to detect round-off errors.  While a variable so we can set it,
    // normally this will be treated as a constant.
    private double NUMERICAL_LIMIT = 1.e-10;

    /**
     * Set the numerical limit for equality tests.
     * For a sequence of spline points, the maximum deviation of the x
     * and y coordinates from the point specified in the last MOVE_TO
     * operation is computed.  If the deviation from the last MOVE_TO
     * coordinates of the x or y value of the spline point provided
     * before a CLOSE is less than the maximum deviation multiplied by
     * the numerical limit, the two points are assumed to be identical.
     * @param value the numerical limit, which must be non-negative
     */
    public void setNumericalLimit(double value) {
	if (value < 0) {
	    String msg = errorMsg("lessThanZero", value);
	    throw new IllegalArgumentException(msg);
	}
	NUMERICAL_LIMIT = value;
    }

    /**
     * Get the numerical limit for equality tests.
     * For a sequence of spline points, the maximum deviation of the x
     * and y coordinates from the point specified in the last MOVE_TO
     * operation is computed.  If the deviation from the last MOVE_TO
     * coordinates of the x or y value of the spline point provided
     * before a CLOSE is less than the maximum deviation multiplied by
     * the numerical limit, the two points are assumed to be identical.
     * @return the numerical limit
     * @see #setNumericalLimit(double)
     */
    public double getNumericalLimit() {
	return NUMERICAL_LIMIT;
    }


    private boolean basicMode = false;

    // called when building basic splines.
    void setBasicMode() {basicMode = true;}

    private void appendAux(CPoint[] cpoints) {
	if (cpoints == null || cpoints.length == 0) return;
	if (path == null) initPath();
	Point2D cpt = path.getCurrentPoint();
	CPointType lastType = CPointType.MOVE_TO;
	Point2D cpoint1 = null;
	Point2D cpoint2 = null;
	int lastMoveToIndex = -1;
	int i = 0;
	int j = 0;
	double nextx = 0.0, nexty = 0.0;
	int offset = 0;
	int n;
	int m;
	if (cpt == null) {
	    int delta = 0;
	    if (cpoints[0].type == CPointType.MOVE_TO_NEXT
		&& cpoints.length > 1) {
		if (cpoints[1].type == CPointType.CONTROL) {
		    delta = 1;
		    cpoint1 = new Point2D.Double(cpoints[1].x, cpoints[1].y);
		}
		if (cpoints.length > 1 + delta) {
		    if (cpoints[1+delta].type == CPointType.SPLINE_FUNCTION) {
			lastMoveToX = cpoints[1+delta].xfOps
			    .valueAt(cpoints[1].t1);
			lastMoveToY = cpoints[1+delta].yfOps
			    .valueAt(cpoints[1].t1);
			path.moveTo(lastMoveToX, lastMoveToY);
			offset = 1;
			lastMoveToIndex = 0;
		} else if (cpoints[1+delta].type == CPointType.SPLINE) {
			lastMoveToX = cpoints[1+delta].x;
			lastMoveToY = cpoints[1+delta].y;
			path.moveTo(lastMoveToX, lastMoveToY);
			offset = 1;
			lastMoveToIndex = 0;
		    }
		}
	    } else if (cpoints[0].type != CPointType.MOVE_TO) {
		throw new IllegalArgumentException(errorMsg("missingMOVETO"));
	    } else {
	        lastMoveToX = cpoints[0].x;
		lastMoveToY = cpoints[0].y;
		path.moveTo(lastMoveToX, lastMoveToY);
		lastMoveToIndex = 0;
	    }
	    i++;
	    i += delta;
	}
	// count segments, but a sequence that make up a spline
	// is counted just once.
	int count = 0;
	// count number of times a path is closed, either explicitly
	// or implicitly by using cycleTo.
	int closeCount = 0;
	boolean simpleloop = true;
	boolean closeUsed = false;
	while (i < cpoints.length) {
	    CPointType currentType = cpoints[i].type;
	    switch (lastType) {
	    case SEG_END_NEXT:
		if (currentType != CPointType.SPLINE
		    && currentType != CPointType.SPLINE_FUNCTION) {
		    throw new IllegalArgumentException
			(errorMsg("afterSEGENDNEXT", i));
		}
	    case MOVE_TO:
	    case SEG_END:
		if (currentType == CPointType.CONTROL
		    && i+1 < cpoints.length
		    && (cpoints[i+1].type == CPointType.SPLINE
			|| cpoints[i+1].type == CPointType.SPLINE_FUNCTION)) {
		    cpoint1 = new Point2D.Double(cpoints[i].x, cpoints[i].y);
		    i++;
		    continue;
		}
	    case CLOSE:
		if (basicMode) {
		    if (closeCount > 0) {
			throw new
			    IllegalArgumentException
			    (errorMsg("segAfterClose", i));
		    }
		}
		switch(currentType) {
		case MOVE_TO:
		    cpoint1 = null; cpoint2 = null;
		    simpleloop = true;
		    closeUsed = false;
		    lastMoveToX = cpoints[i].x;
		    lastMoveToY = cpoints[i].y;
		    path.moveTo(lastMoveToX, lastMoveToY);
		    lastType = CPointType.MOVE_TO;
		    offset = 0;
		    lastMoveToIndex = i;
		    i++;
		    break;
		case CONTROL:
		    offset = 0;
		    simpleloop = false;
		    j = i;
		    while (j < cpoints.length) {
			if (cpoints[j].type == CPointType.CONTROL) {
			    j++;
			} else {
			    break;
			}
		    }
		    if (j == cpoints.length) {
			throw new IllegalArgumentException
			    (errorMsg("cpsNotTerm"));
		    }
		    if (cpoints[j].type != CPointType.SEG_END
			&& cpoints[j].type != CPointType.SEG_END_NEXT
			&& cpoints[j].type != CPointType.CLOSE) {
			throw new IllegalArgumentException
			    (errorMsg("cpsNeedsSEGEND", i));
		    }
		    if (cpoints[j].type == CPointType.SEG_END_NEXT) {
			if (j+1 == cpoints.length) {
			    throw new IllegalArgumentException
				(errorMsg("termSEGENDNEXT", j));
			}
			if (cpoints[j+1].type == CPointType.SPLINE_FUNCTION) {
			    nextx = cpoints[j+1].xfOps.valueAt(cpoints[j+1].t1);
			    nexty = cpoints[j+1].yfOps.valueAt(cpoints[j+1].t1);
			} else if (cpoints[j+1].type == CPointType.SPLINE) {
			    nextx = cpoints[j+1].x;
			    nexty = cpoints[j+1].y;
			} else {
			    throw new IllegalArgumentException
				(errorMsg("afterSEGENDNEXT", j+1));
			}
			offset = 1;
		    } else if (cpoints[j].type == CPointType.CLOSE) {
			nextx = lastMoveToX;
			nexty = lastMoveToY;
		    } else {
			nextx = cpoints[j].x;
			nexty = cpoints[j].y;
		    }
		    switch(j-i){
		    case 1:
			path.quadTo(cpoints[i].x, cpoints[i].y,
				    nextx, nexty);
			count++;
			break;
		    case 2:
			path.curveTo(cpoints[i].x, cpoints[i].y,
				     cpoints[i+1].x, cpoints[i+1].y,
				     nextx, nexty);
			count++;
			break;
		    default:
			throw new IllegalArgumentException
			    (errorMsg("cpsTooLong"));
		    }
		    i = j;
		    if (cpoints[j].type == CPointType.CLOSE) {
			lastType = CPointType.SEG_END;
		    } else {
			lastType = cpoints[i].type;
			i++;
		    }
		    break;
		case SEG_END_NEXT:
		    simpleloop = false;
		    if (i == 0) {
			throw new IllegalArgumentException
			    (errorMsg("beforeSEGENDNEXT"));
		    }
		    if (cpoints.length > i+1) {
			// previous point's type is not CONTROL, SPLINE or
			// SPLINE_FUNCTION - that is handled elsewhere.
			int delta = 0;
			if (cpoints[i+1].type == CPointType.CONTROL &&
			    cpoints.length > i+2) {
			    nextx = cpoints[i].x;
			    nexty = cpoints[i].y;
			    cpoint2 = new Point2D.Double(nextx, nexty);
			    simpleloop = false;
			    delta = 1;
			}
			if (cpoints[i+1+delta].type
			    == CPointType.SPLINE_FUNCTION) {
			    nextx = cpoints[i+1+delta].xfOps
				.valueAt(cpoints[i+1+delta].t1);
			    nexty = cpoints[i+1+delta].yfOps
				.valueAt(cpoints[i+1+delta].t1);
			} else if (cpoints[i+1+delta].type
				   == CPointType.SPLINE) {
			    nextx = cpoints[i+1+delta].x;
			    nexty = cpoints[i+1+delta].y;
			} else {
			    throw new IllegalArgumentException
				(errorMsg("afterSEGENDNEXT", i+1+delta));
			}
			path.lineTo(nextx, nexty);
			lastType = CPointType.SEG_END;
			i++;
			offset = 1;
		    } else {
			throw new IllegalArgumentException
			    (errorMsg("afterSEGENDNEXT", i+1));
		    }
		    break;
		case MOVE_TO_NEXT:
		    simpleloop = true;
		    closeUsed = false;
		    cpoint1 = null; cpoint2 = null;
		    int mtndelta = 0;
		    if (cpoints.length > i+1) {
			if (cpoints[i+1].type == CPointType.CONTROL) {
			    mtndelta = 1;
			    cpoint1 = new Point2D
				.Double(cpoints[i+1].x, cpoints[1+1].y);
			}
		    }
		    if (cpoints.length > i + 1 + mtndelta) {
			if (cpoints[i+1+mtndelta].type
			    == CPointType.SPLINE_FUNCTION) {
			    lastMoveToX =
				cpoints[i+1+mtndelta].xfOps
				.valueAt(cpoints[i+1+mtndelta].t1);
			    lastMoveToY =
				cpoints[i+1+mtndelta].yfOps
				.valueAt(cpoints[i+1+mtndelta].t1);
			    path.moveTo(lastMoveToX, lastMoveToY);
			    offset = 1;
			} else if (cpoints[i+1+mtndelta].type
				   == CPointType.SPLINE
				   || cpoints[i+1+mtndelta].type
				   == CPointType.SEG_END) {
			    lastMoveToX = cpoints[i+1+mtndelta].x;
			    lastMoveToY = cpoints[i+1+mtndelta].y;
			    path.moveTo(lastMoveToX, lastMoveToY);
			    offset = 1;
			} else {
			    throw new IllegalArgumentException
				(errorMsg("afterMOVETONEXT"));
			}
		    } else {
			throw new IllegalArgumentException
			    (errorMsg("afterMOVETONEXT"));
		    }
		    lastType = CPointType.MOVE_TO;
		    i++;
		    i += mtndelta;
		    lastMoveToIndex = i;
		    break;
		case SEG_END_PREV:
		    throw new IllegalArgumentException
			     (errorMsg("badSEGENDPREV"));
		case SPLINE_FUNCTION:
		case SPLINE:
		    m = 0;
		    j = i;
		    while (j < cpoints.length) {
			if(cpoints[j].type == CPointType.SPLINE_FUNCTION) {
			    m += cpoints[j].getN();
			    j++;
			} else if (cpoints[j].type == CPointType.SPLINE) {
			    j++;
			} else {
			    break;
			}
		    }
		    if (j == cpoints.length) {
			throw new IllegalArgumentException
			    (errorMsg("cpsNotTerm"));
		    }
		    int delta = 0;
		    if (cpoints[j].type == CPointType.CONTROL) {
			if (j+1 < cpoints.length &&
			    (cpoints[j+1].type == CPointType.SEG_END
			     || cpoints[j+1].type == CPointType.SEG_END_PREV
			     || cpoints[j+1].type == CPointType.CLOSE)) {
			    cpoint2 = new Point2D.Double(cpoints[j].x,
							 cpoints[j].y);
			    simpleloop = false;
			    delta = 1;
			}
		    }
		    boolean segEndNextSeen = false;
		    if (cpoints[j+delta].type == CPointType.CLOSE) {
			n = j - i;
			if (!simpleloop) n++;
		    } else if (cpoints[j+delta].type == CPointType.SEG_END) {
			simpleloop = false;
			n = j + 1 - i;
		    } else if (cpoints[j+delta].type
			       == CPointType.SEG_END_NEXT) {
			simpleloop = false;
			n = j + 1 - i;
			if (j+1+delta == cpoints.length) {
			    throw new IllegalArgumentException
				(errorMsg("termSEGENDNEXT", j+delta));
			}
			if (cpoints[j+delta+1].type
			    == CPointType.SPLINE_FUNCTION) {
			    nextx =
				cpoints[j+delta+1].xfOps
				.valueAt(cpoints[j+delta+1].t1);
			    nexty =
				cpoints[j+delta+1].yfOps
				.valueAt(cpoints[j+delta+1].t1);
			} else if (cpoints[j+delta+1].type
				   == CPointType.SPLINE) {
			    nextx = cpoints[j+delta+1].x;
			    nexty = cpoints[j+delta+1].y;
			}
			segEndNextSeen = true;
		    } else if (cpoints[j+delta].type
			       == CPointType.SEG_END_PREV) {
			n = j - i;
		    } else {
			throw new IllegalArgumentException
			    (errorMsg("spsNotTerm", j+delta));
		    }
		    int npmmo = n+m-offset;
		    int nmin1 = n - 1;
		    double[] xs = new double[npmmo];
		    double[] ys = new double[npmmo];
		    double maxval = 0.0;
		    int index = 0;
		    int index1 = 0;
		    for (int k = 0; k < n; k++) {
			int ipk = i+k;
			if (cpoints[ipk].type == CPointType.CONTROL) ipk++;
			if (cpoints[ipk].type == CPointType.SPLINE_FUNCTION) {
			    int lim = cpoints[ipk].getN();
			    double t1 = cpoints[ipk].t1;
			    double t2 = cpoints[ipk].t2;
			    for (int ind = 0; ind <= lim; ind++) {
				if (index1 < offset) {
				    index1++;
				    continue;
				}
				double t = t1 + ind * ((t2 - t1)/lim);
				double tmp = cpoints[ipk].xfOps.valueAt(t);
				double tmp1 = Math.abs(tmp - lastMoveToX);
				xs[index] = tmp;
				if (tmp1 > maxval) maxval = tmp1;
				tmp = cpoints[ipk].yfOps.valueAt(t);
				tmp1 = Math.abs(tmp - lastMoveToY);
				ys[index] = cpoints[ipk].yfOps.valueAt(t);
				if (tmp1 > maxval) maxval = tmp1;
				index++;
				index1++;
			    }
			} else if (simpleloop == false &&
				   cpoints[ipk].type == CPointType.CLOSE) {
			    closeUsed = true;
			    if (k < offset) continue;
			    xs[index] = lastMoveToX;
			    ys[index] = lastMoveToY;
			    index++;
			    index1++;
			} else {
			    if (k < offset) {
				index1++;
				continue;
			    }
			    double tmp = (k == nmin1 && segEndNextSeen)? nextx:
				cpoints[ipk].x;
			    xs[index] = tmp;
			    double tmp1 = Math.abs(tmp - lastMoveToX);
			    if (tmp1 > maxval) maxval = tmp1;
			    tmp = (k == nmin1 && segEndNextSeen)? nexty:
				cpoints[ipk].y;
			    ys[index] = tmp;
			    tmp1 = Math.abs(tmp - lastMoveToY);
			    if (tmp1 > maxval) maxval = tmp1;
			    index++;
			    index1++;
			}
		    }
		    if (index == npmmo) {
			if (cpoints[j].type == CPointType.CLOSE
			    && lastMoveToIndex == i-1) {
			    // make sure we aren't counting the
			    // end point twice.
			    if (maxval == 0.0) {
				// shouldn't happen as this means
				// nothing moved.  We treat it like
				// the following test, which we can't
				// use due to the division by maxval.
				// We aren't doing anything special for
				// the case where there a series of
				// identical points of length greater
				// than 2.
				index--;
			    } else {
				double tmp =
				    Math.abs(xs[index-1]-lastMoveToX)/maxval;
				double tmp1 =
				    Math.abs(ys[index-1]-lastMoveToY)/maxval;
				if (tmp < NUMERICAL_LIMIT
				    && tmp1 < NUMERICAL_LIMIT) {
				    index--;
				}
			    }
			}
		    }
		    CPointType prevLastType = lastType;
		    lastType = cpoints[j+delta].type;
		    if (closeUsed || lastType == CPointType.SEG_END
			|| lastType == CPointType.SEG_END_PREV
			|| lastType == CPointType.SEG_END_NEXT) {
			if (closeUsed) {
			    if ((cpoints[j-1].type == CPointType.SPLINE
				 && cpoints[j-1].x == lastMoveToX
				 && cpoints[j-1].y == lastMoveToY)
				||
				(cpoints[j-1].type == CPointType.SPLINE_FUNCTION
				 && cpoints[j-1].xfOps.valueAt(cpoints[j-1].t2)
				 == lastMoveToX
				 && cpoints[j-1].yfOps.valueAt(cpoints[j-1].t2)
				 == lastMoveToY)) {
				index--;
			    }
			}
			path.splineTo(xs, ys, index, cpoint1, cpoint2);
			cpoint1 = null; cpoint2 = null;
			if (closeUsed) {
			    path.closePath();
			}
			count++;
		    } else {
			if (prevLastType != CPointType.MOVE_TO ||
			    (i != lastMoveToIndex && i-1 != lastMoveToIndex)) {
			    path.splineTo(xs, ys, index, cpoint1, cpoint2);
			    cpoint1 = null; cpoint2 = null;
			    path.closePath();
			    closeCount++;
			    count++;
			} else  {
			    if (basicMode) {
				if (count > 0) {
				    String msg = errorMsg("illPlacedClose", i);
				    throw new IllegalArgumentException(msg);
				}
			    }
			    path.cycleTo(xs, ys, index);
			    count++;
			}
		    }
		    i = j + delta;
		    if (lastType == CPointType.SEG_END_PREV ||
			lastType == CPointType.SEG_END_NEXT) {
			lastType = CPointType.SEG_END;
		    }
		    offset = segEndNextSeen? 1: 0;
		    i++;
		    break;
		case SEG_END:
		    simpleloop = false;
		    path.lineTo(cpoints[i].x, cpoints[i].y);
		    count++;
		    i++;
		    lastType = CPointType.SEG_END;
		    offset = 0;
		    break;
		case CLOSE:
		    path.closePath();
		    closeCount++;
		    lastType = CPointType.CLOSE;
		    i++;
		    while (i < cpoints.length &&
			   cpoints[i].type == CPointType.CLOSE) i++;
		    break;
		}
		break;
	    default:
		throw new UnexpectedExceptionError();
	    }
	}
    }

    /**
     * Get the enum constant {@link CPointType#CLOSE}.
     * <P>
     * This is a convenience method used for scripting.
     * It is not final because adding a final declaration confuses
     * the Nashorn JavaScript engine.
     * @return the enum constant
     */
    public CPointType constantCLOSE() {return CPointType.CLOSE;}

    /**
     * Create a {@link CPoint} whose type is {@link CPointType#CLOSE}.
     * <P>
     * This is a convenience method used for scripting.
     * @return the {@link CPoint}
     */
    public CPoint createCPointClose() {
	return new CPoint(CPointType.CLOSE);
    }

    /**
     * Get the enum constant {@link CPointType#MOVE_TO}.
     * <P>
     * This is a convenience method used for scripting.
     * It is not final because adding a final declaration confuses
     * the Nashorn JavaScript engine.
     * @return the enum constant
     */
    public CPointType constantMOVE_TO() {return CPointType.MOVE_TO;}

    /**
     * Create a {@link CPoint} whose type is {@link CPointType#MOVE_TO}.
     * <P>
     * This is a convenience method used for scripting.
     * @param x the X coordinate
     * @param y the Y coordinate
     * @return the {@link CPoint}
     */
    public CPoint createCPointMoveTo(double x, double y) {
	return new CPoint(CPointType.MOVE_TO, x, y);
    }

    /**
     * Get the enum constant {@link CPointType#MOVE_TO_NEXT}.
     * <P>
     * This is a convenience method used for scripting.
     * It is not final because adding a final declaration confuses
     * the Nashorn JavaScript engine.
     * @return the enum constant
     */
    public CPointType constantMOVE_TO_NEXT() {
	return CPointType.MOVE_TO_NEXT;
    }

    public CPoint createCPointMoveToNext() {
	return new CPoint(CPointType.MOVE_TO_NEXT);
    }

    /**
     * Get the enum constant {@link CPointType#SEG_END}.
     * <P>
     * This is a convenience method used for scripting.
     * It is not final because adding a final declaration confuses
     * the Nashorn JavaScript engine.
     * @return the enum constant
     */
    public CPointType constantSEG_END() {return CPointType.SEG_END;}


    /**
     * Create a {@link CPoint} whose type is {@link CPointType#SEG_END}.
     * <P>
     * This is a convenience method used for scripting.
     * @param x the X coordinate
     * @param y the Y coordinate
     * @return the {@link CPoint}
     */
    public final CPoint createCPointSegEnd(double x, double y) {
	return new CPoint(CPointType.SEG_END, x, y);
    }

    /**
     * Get the enum constant {@link CPointType#SEG_END_NEXT}.
     * <P>
     * This is a convenience method used for scripting.
     * It is not final because adding a final declaration confuses
     * the Nashorn JavaScript engine.
     * @return the enum constant
     */
    public CPointType constantSEG_END_NEXT() {
	return CPointType.SEG_END_NEXT;
    }

    /**
     * Create a {@link CPoint} whose type is {@link CPointType#SEG_END_NEXT}.
     * <P>
     * This is a convenience method used for scripting.
     * @return the {@link CPoint}
     */
    public CPoint createCPointSegEndNext() {
	return new CPoint(CPointType.SEG_END_NEXT);
    }

    /**
     * Get the enum constant {@link CPointType#SEG_END_PREV}.
     * <P>
     * This is a convenience method used for scripting.
     * It is not final because adding a final declaration confuses
     * the Nashorn JavaScript engine.
     * @return the enum constant
     */
    public CPointType constantSEG_END_PREV() {
	return CPointType.SEG_END_NEXT;
    }

    /**
     * Create a {@link CPoint} whose type is {@link CPointType#SEG_END_PREV}.
     * <P>
     * This is a convenience method used for scripting.
     * @return the {@link CPoint}
     */
    public CPoint createCPointSegEndPrev() {
	return new CPoint(CPointType.SEG_END_PREV);
    }

    /**
     * Get the enum constant {@link CPointType#CONTROL}.
     * <P>
     * This is a convenience method used for scripting.
     * It is not final because adding a final declaration confuses
     * the Nashorn JavaScript engine.
     * @return the enum constant
     */
    public CPointType constantCONTROL() {return CPointType.CONTROL;}

    /**
     * Create a {@link CPoint} whose type is {@link CPointType#CONTROL}.
     * <P>
     * This is a convenience method used for scripting.
     * @param x the X coordinate
     * @param y the Y coordinate
     * @return the {@link CPoint}
     */
    public CPoint createCPointControl(double x, double y) {
	return new CPoint(CPointType.CONTROL, x, y);
    }

    /**
     * Get the enum constant {@link CPointType#SPLINE}.
     * <P>
     * This is a convenience method used for scripting.
     * It is not final because adding a final declaration confuses
     * the Nashorn JavaScript engine.
     * @return the enum constant
     */
    public CPointType constantSPLINE() {return CPointType.SPLINE;}

    /**
     * Create a {@link CPoint} whose type is {@link CPointType#SPLINE}.
     * <P>
     * This is a convenience method used for scripting.
     * @param x the X coordinate
     * @param y the Y coordinate
     * @return the {@link CPoint}
     */
    public CPoint createCPointSpline(double x, double y) {
	return new CPoint(CPointType.SPLINE, x, y);
    }

    /**
     * Get the enum constant {@link CPointType#SPLINE_FUNCTION}.
     * <P>
     * This is a convenience method used for scripting.
     * It is not final because adding a final declaration confuses
     * the Nashorn JavaScript engine.
     * @return the enum constant
     */
    public CPointType constantSPLINE_FUNCTION() {
	return CPointType.SPLINE_FUNCTION;
    }

    /**
     * Create a {@link CPoint} whose type is {@link CPointType#SPLINE_FUNCTION}.
     * <P>
     * This is a convenience method used for scripting.
     * @param xfunct the X coordinate function
     * @param yfunct the Y coordinate function
     * @return the {@link CPoint}
     * @param t1 an end for the range of values at which  the function
     *        will be evaluated
     * @param t2 an end for the range of values at which  the function
     *        will be evaluated
     * @param n the number of segments
     * @exception IllegalArgumentException a function was null or
     *            n was not positive
     */
    public CPoint
	createCPointSplineFunction(RealValuedFunctOps xfunct,
			   RealValuedFunctOps yfunct,
			   double t1, double t2, int n)
    {
	return new CPoint(xfunct, yfunct, t1, t2, n);
    }


}

//  LocalWords:  exbundle SplinePath superclasses javadoc subclassses
//  LocalWords:  AbstractSplinePathBuilder's subclasses windingRule
//  LocalWords:  UnsupportedOperationException noResource cpoints af
//  LocalWords:  IllegalArgumentException configurePathBuilder cpoint
//  LocalWords:  illformedConfig getPath lessThanZero missingMOVETO
//  LocalWords:  setNumericalLimit cycleTo segAfterClose cpsNotTerm
//  LocalWords:  cpsNeedsSEGEND termSEGENDNEXT afterSEGENDNEXT nextx
//  LocalWords:  cpsTooLong beforeSEGENDNEXT afterMOVETONEXT nexty
//  LocalWords:  badSEGENDPREV spsNotTerm segEndNextSeen Nashorn enum
//  LocalWords:  maxval constantWIND SplinePathBuilder CPointType
//  LocalWords:  affine IllegalStateException intermediateClose PREV
//  LocalWords:  cporder illPlacedClose xfunct yfunct
