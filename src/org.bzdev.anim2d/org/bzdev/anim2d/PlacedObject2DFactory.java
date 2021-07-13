package org.bzdev.anim2d;
import org.bzdev.obnaming.annotations.FactoryParmManager;
import org.bzdev.obnaming.annotations.PrimitiveParm;
import org.bzdev.obnaming.annotations.KeyedCompoundParm;
import org.bzdev.obnaming.annotations.CompoundParmType;
import org.bzdev.obnaming.annotations.CompoundParm;

import org.bzdev.lang.Callable;
import java.util.Map;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.LinkedList;

import org.bzdev.graphs.RefPointName;

//@exbundle org.bzdev.anim2d.lpack.Animation2D

/**
 * Abstract factory for PlacedAnimationObject2D.
 * This factory inherits two parameters from its superclass:
 * <ul>
 *    <li> "zorder" - the initial stacking order (objects with smaller
 *         zorder values are rendered first).  The zorder is defined
 *         as a long integer. The default value is 0. If two objects
 *         have the same zorder value the rendering order is
 *         technically indeterminate, but in practice the object
 *         created first will be rendered first (valid for the first
 *         2<sup>63</sup>-1 objects created).
 *    <li> "visible" - true if the object is visible; false if it is not.
 *         The default value is "true".
 *     <li> "timeline" - an integer-keyed set of values that define
 *           changes in the object's configuration. This class provides
 *           additional timeline parameters as described below.
 *          <ul>
 *             <li> "timeline.time" - the time at which timeline parameters
 *                  are to change (typically measured in seconds, not
 *                  simulation ticks). This parameter is must be provided.
 *           <li> "timeline.traceSetMode" - indicates how the parameter
 *                "timeline.traceSets" is interpretted. the values are
 *                enumeration constants of type
 *                {@link org.bzdev.devqsim.TraceSetMode} and are used as
 *                follows:
 *                <ul>
 *                  <li> <code>KEEP</code> - keep the existing trace sets,
 *                       adding additional ones specified by the
 *                       parameter "timeline.traceSets".
 *                  <li> <code>REMOVE</code> - remove the trace sets specified
 *                       by the parameter "timeline.traceSets".
 *                  <li> <code>REPLACE</code> - remove all existing trace sets
 *                       and repalce those with the ones specified by
 *                       the timeline.traceSets parameter.
 *                </ul>
 *           <li> "timeline.traceSets" - a parameter representing a set
 *                of TraceSet objects (the three-argument 'add' method
 *                is used to add entries).
 *             <li> "timeline.visible" -the visibility of the object.
 *                  This parameter has the value true if visible,
 *                  false if not visible.
 *                  If this parameter is not defined for the key,
 *                  the previous value is not changed.
 *             <li> "timeline.zorder" - the zorder for the object.
 *                  If this parameter is not defined for the key,
 *                  the previous value is not changed.
 *          </ul>
 *   <li> "traceSets" - a set of TraceSets a SimObject will use
 *        for tracing.  One should use the add and remove factory
 *        methods as this parameter refers to a set of values.
 * </ul>
 * <P>
 * The additional parameters supported are the following:
 * <ul>
 *    <li> "refPointMode" - the configuration mode. For each value,
 *          there are one or two parameters that can be set.  The
 *          configuration-mode values are
 *          <ul>
 *             <li> <CODE>BY_NAME</CODE> - for this value the
 *                  following parameter must be used:
 *                  <ul>
 *                     <li> "refPointName" - a symbolic name for a
 *                          reference point (<CODE>UPPER_LEFT</CODE>,
 *                          <CODE>UPPER_CENTER</CODE>,
 *                          <CODE>UPPER_RIGHT</CODE>,
 *                          <CODE>LOWER_LEFT</CODE>,
 *                          <CODE>CENTER_LEFT</CODE>,
 *                          <CODE>CENTER</CODE>,
 *                          <CODE>CENTER_RIGHT</CODE>,
 *                          <CODE>LOWER_CENTER</CODE>, or
 *                          <CODE>LOWER_RIGHT</CODE>) relative to the
 *                          object's bounding box.
 *                  </ul>
 *             <li> <CODE>BY_COORD</CODE>
 *                  <ul>
 *                     <li> "refPointX" - the X coordinate of the reference
 *                          point in graph coordinate space (the default is
 *                          0.0, and by convention should correspond to a
 *                          centered object or some distinguished feature).
 *                          The object will be drawn at this point when given
 *                          coordinates of (0,0, 0.0) in graph coordinate space.
 *                     <li> "refPointY" - the Y coordinate of the reference
 *                          point in graph coordinate space (the default is
 *                          0.0, and by convention should correspond to a
 *                          centered object or some distinguished feature).
 *                          The object will be drawn at this point when given
 *                          coordinates of (0,0, 0.0) in graph coordinate space.
 *                  </ul>
 *             <li> <CODE>BY_FRACTION</CODE>
 *                  <ul>
 *                     <li> "refPointFractX" - the X position of the
 *                          reference point as a fractional distance between
 *                          the smallest X value of the object's bounding box
 *                          and the largest X value of the object's bounding
 *                          box, with the smallest value indicated by 0.0 and
 *                          the largest value by 1.0.
 *                     <li> "refPointFractY" - the Y position of the
 *                          reference point as a fractional distance between
 *                          the smallest Y value of the object's bounding box
 *                          and the largest Y value of the object's bounding
 *                          box, with the smallest value indicated by 0.0 and
 *                          the largest value by 1.0.
 *                  </ul>
 *          </ul>
 *     <li> "initialX" - the initial X coordinate of the object in graph
 *           coordinate space.
 *     <li> "initialY" - the initial Y coordinate of the object in graph
 *           coordinate space.
 *     <li> "initialAngle" - the initial angle of the object in graph
 *          coordinate space, measured in degrees.
 *     <li> "timeline" - additional "timeline" parameters are provided:
 *          <ul>
 *             <li> "timeline.x" - the X coordinate of the reference point
 *                  at the specified time, provided in graph coordinate
 *                  space units. If this parameter is not defined for the key,
 *                  the previous value is not changed.
 *             <li> "timeline.y" - the Y coordinate of the reference point
 *                  at the specified time, provided in graph coordinate space
 *                  units. If this parameter is not defined for the key,
 *                  the previous value is not changed.
 *             <li> "timeline.angle" - the angle about the reference point
 *                  at the specified time, with 0.0 referring to a reference
 *                  direction (increasing angles are in the counterclockwise
 *                  direction with 0.0 being a default, typically relative
 *                  to a the coordinate system or to a path the object
 *                  follows). If this parameter is not defined for the key,
 *                  the previous value is not changed.  The angle is measured
 *                  in degrees, not radians.
 *          </ul>
 * </ul>
 *
 */

@FactoryParmManager(value = "PlacedObject2DParmManager",
		    tipResourceBundle = "*.lpack.PlacedObjTips",
		    labelResourceBundle = "*.lpack.PlacedObjLabels",
		    docResourceBundle = "*.lpack.PlacedObjDocs")
public abstract class PlacedObject2DFactory<Obj extends PlacedAnimationObject2D>
    extends AnimationObject2DFactory<Obj>
{
    static String errorMsg(String key, Object... args) {
	return Animation2D.errorMsg(key, args);
    }

    Animation2D animation;

    /**
     * ReferencePoint2D factory configuration mode.
     * This is used to determine which variables are used in the
     * configuration.
     */
    public static enum RefPointMode {
	/**
	 * Configure by name.
	 * The location of the reference point is determined by
	 * the "refPointName" parameter.
	 */
	BY_NAME,
	/**
	 * Configure by (x, y) coordinates.
	 * The location of the reference point is determined by the
	 * parameters "refPointX" and "refPointY".
	 */
	BY_COORD,
	/**
	 * Configure by fraction.
	 * The location of the reference point is determined by
	 * the parameters "refPointFractX" and "refPointFractY"
	 */
	BY_FRACTION
    }

    @PrimitiveParm("refPointMode")
	RefPointMode refPointMode = RefPointMode.BY_COORD;

    @PrimitiveParm("refPointName") RefPointName refPointName =
	RefPointName.CENTER;

    @PrimitiveParm("refPointX") double refPointX = 0.0;

    @PrimitiveParm("refPointY") double refPointY = 0.0;

    @PrimitiveParm(value = "refPointFractX", lowerBound = "0.0",
		   upperBound = "1.0")
	double refPointFractX = 0.5;

    @PrimitiveParm(value = "refPointFractY", lowerBound = "0.0",
		   upperBound = "1.0")
	double refPointFractY = 0.5;


    @PrimitiveParm("initialX") double initialX = 0.0;
    @PrimitiveParm("initialY") double initialY = 0.0;
    @PrimitiveParm("initialAngle") double initialAngle = 0.0;
    // @PrimitiveParm("initiallyVisible") boolean initiallyVisible = false;

    @CompoundParmType(tipResourceBundle = "*.lpack.PlacedObjTimelineTips",
		    labelResourceBundle = "*.lpack.PlacedObjTimelineLabels")
    static class Timeline {
	// @PrimitiveParm("time") Double time = null;
	@PrimitiveParm("x") Double x = null;
	@PrimitiveParm("y") Double y = null;
	@PrimitiveParm("angle") Double angle = null;
	// @PrimitiveParm("visible") Boolean visible = null;
	// @PrimitiveParm("zorder") Long zorder = null;
    }

    @KeyedCompoundParm("timeline")
	Map<Integer,Timeline> timelineMap = new TreeMap<Integer,Timeline>();

    PlacedObject2DParmManager<Obj> pm;

    /**
     * Constructor.
     * @param animation the animation
     */
    protected PlacedObject2DFactory(Animation2D animation) {
	super(animation);
	this.animation = animation;
	pm = new PlacedObject2DParmManager<Obj>(this);
	initParms(pm, PlacedObject2DFactory.class);
    }

    @Override
    public void clear() {
	super.clear();
	pm.setDefaults(this);
    }

    // private ArrayList<Callable>subclassTimeline = new ArrayList<>();

    // static class NoTimeException extends RuntimeException {}

    boolean mustUseTimelineAngle = true;

    /**
     * Assert that the timeline parameter "angle" is not used.
     * This should normally be called by a subclass during
     * initialization but in any case before the factory's
     * parameters are set.  It should rarely be necessary to use
     * this method as most instances of PlacedObject2D have an
     * angle.  The current exception is the GraphView factory
     * as graph views cannot currently be rotated.
     */
    protected void mayIgnoreTimelineAngle() {
	mustUseTimelineAngle = false;
    }

    /**
     * Request subclasses to add entries to the timeline.
     * Each subclass adding entries should create a Callable that
     * performs any necessary operations at the specified time, and
     * that Callable should be returned via a call to
     * addToTimelineResponse. Each subclass that implements
     * addToTimelineRequest must call
     * super.addToTimelineRequest(object, key, time)
     * as the first statement in addToTimelineRequest.
     * @param object the object being configured.
     * @param key the timeline key
     * @param time the time for with the timeline entry
     */
    @Override
    protected void addToTimelineRequest(Obj object, int key, double time) {
	super.addToTimelineRequest(object, key, time);
	Timeline entry = timelineMap.get(key);
	Double x = entry.x;
	Double y = entry.y;
	Double angle = entry.angle;
	boolean tmp = false;
	if (x != null || y != null || (mustUseTimelineAngle && angle != null)) {
	    if (x == null || y == null
		|| (mustUseTimelineAngle && angle == null)) {
		throw new IllegalStateException(errorMsg("nullParms"));
	    }
	    tmp = true;
	}
	final boolean setpos = tmp;
	final Obj obj = object;
	final double xx = (x == null)? 0.0: x;
	final double yy = (y == null)? 0.0: y;
	final double aa = (angle == null)? 0.0: angle;

	addToTimelineResponse(new Callable() {
		public void call() {
		    if (setpos) obj.setPosition(xx, yy, aa);
		    // if (setVis) obj.setVisible(vis);
		    // if (setZo) obj.setZorder(zo);
		}
	    });
    }

    // LinkedList<TimedCallListEntry> ctlist = new LinkedList<>();

    @Override
    protected void initObject(final Obj object) {
	super.initObject(object);

	switch (refPointMode) {
	case BY_NAME:
	    object.setRefPointByName(refPointName);
	    break;
	case BY_COORD:
	    object.setRefPoint(refPointX, refPointY);
	    break;
	case BY_FRACTION:
	    object.setRefPointByFraction(refPointFractX, refPointFractY);
	    break;
	default:
	    throw new IllegalStateException(errorMsg("badRefPointMode"));
	}

	object.setPosition(initialX, initialY, Math.toRadians(initialAngle));
    }
}
