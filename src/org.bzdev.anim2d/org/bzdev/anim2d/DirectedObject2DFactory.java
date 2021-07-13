package org.bzdev.anim2d;
import org.bzdev.geom.BasicSplinePath2D;
import org.bzdev.devqsim.SimFunction;

import org.bzdev.obnaming.annotations.FactoryParmManager;
import org.bzdev.obnaming.annotations.PrimitiveParm;
import org.bzdev.obnaming.annotations.KeyedCompoundParm;
import org.bzdev.obnaming.annotations.CompoundParmType;
import org.bzdev.obnaming.annotations.CompoundParm;

import org.bzdev.lang.Callable;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * Abstract DirectedObject2D factory.
 *
 * This factory extends the parameters provided by its superclass
 * by adding new timeline subparameters. As with the timeline parameters
 * of its superclass, these parameters use an integer-valued key to
 * distinguish various timeline entries.

 * These subparameters are as follows (all apply at the time specified by
 * "timeline.time" defined in AnimationObject2DFactory):
 * <ul>
 *   <li> "timeline.path" - the path that the object should follow.
 *        If 'animation' is the current Animation2D,
 *        {@link Animation2D#nullPath() animation.nullPath()}
 *        will provide a constant path that indicates that the path
 *        should no longer be used. If no value is given, the value is
 *        not changed.
 *   <li> "timeline.u0" - the initial value of the path's parameter (the
 *        default is 0.0)
 *   <li> "timeline.t0" - the initial time interval from the time at
 *        which the path is set to wait before the object can move.
 *   <li> "timeline.velocity" - the velocity along the path
 *   <li> "timeline.acceleration" - the acceleration along the path
 *   <li> "timeline.angleRelative" - true if "timeline.pathAngle" gives
 *        the angle relative to a tangent of the path, false if it does not,
 *        no change if not defined.
 *   <li> "timeline.pathAngle" - the angle in degrees of the object with
 *        respect to the path (unchanged if not defined).
 *   <li> "timeline.angularVelocity" - the angular velocity used in
 *        determining the path angle as a function of time (if not defined,
 *        the value does not change), in units of degrees per unit animation
 *        time.
 *   <li> "timeline.angularAcceleration" - the angular acceleration used in
 *        determining the path angle as a function of time (if not defined,
 *        the value does not change), in units of
 *        degrees/(unit-animation-time)<sup>2</sup>.
 *   <li> "timeline.distanceFunction" - a SimFunction object associated with
 *        this animation giving the distance along the path as a function of
 *        time. If 'animation' is the current Animation2D,
 *        {@link Animation2D#nullFunction() animation.nullFunction()}
 *        will provide a function that indicates that the current
 *        function should no longer be used. If no value is given, the
 *        function is not changed.
 *   <li> "timeline.angleFunction" - a SimFunction object associated
 *        with this animation giving the angle of the object as it
 *        moves along the path as a function of time. If 'animation'
 *        is the current Animation2D,
 *        {@link Animation2D#nullFunction() animation.nullFunction()}
 *        will provide a function that indicates that the current
 *        function should no longer be used. If no value is given, the
 *        function is not changed.  The value returned is in radians,
 *        not degrees. If a path is currently defined, the time argument
 *        treats an argument of zero as the time indicated by the
 *        path's "timeline.t0" parameter. Otherwise the argument refers
 *        to simulation time. The argument uses double-precision time
 *        units (not ticks).
 * </ul>
 * <P>
 * As a convenience, the parameters defined in superclasses are listed:
 * <ul>
 *   <li> "traceSets" - a set of TraceSets a SimObject will use
 *        for tracing.  One should use the add and remove factory
 *        methods as this parameter refers to a set of values.
 *    <li> "zorder" - the initial stacking order (objects with smaller
 *         zorder values are rendered first).  The zorder is defined
 *         as a long integer. The default value is 0. If two objects
 *         have the same zorder value the rendering order is
 *         technically indeterminate, but in practice the object
 *         created first will be rendered first (valid for the first
 *         2<sup>63</sup>-1 objects created).
 *    <li> "visible" - true if the object is visible; false if it is not.
 *         The default value is "true".
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
 *                          object's bounding box.  </ul>
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
 *     <li> "timeline" - an integer-keyed set of values that define
 *           changes in an object's configuration.
 *          <ul>
 *             <li> "timeline.time" - the time at which timeline parameters
 *                  are to change (typically measured in seconds, not
 *                  simulation ticks).  This parameter must be provided.
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
 *                  the previous value is not changed. The angle is measured
 *                  in degrees.
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
 *             <li> "timeline.visible" -the visibility of the object (true
 *                  if visible; false if not visible.
 *                  If this parameter is not defined for the key,
 *                  the previous value is not changed.
 *             <li> "timeline.zorder" - the zorder for the object.
 *                  If this parameter is not defined for the key,
 *                  the previous value is not changed.
 *          </ul>
 * </ul>
 */
@FactoryParmManager(value = "DirectedObject2DParmManager",
		    tipResourceBundle = "*.lpack.DirObjTips",
		    labelResourceBundle = "*.lpack.DirObjLabels")
abstract public class DirectedObject2DFactory<Obj extends DirectedObject2D>
    extends PlacedObject2DFactory<Obj>
{

    @CompoundParmType(tipResourceBundle = "*.lpack.DirObjTimeLineTips",
		      labelResourceBundle = "*.lpack.DirObjTimeLineLabels",
		      docResourceBundle = "*.lpack.DirObjTimeLineDocs")
    static class TimelineEntry {

	@PrimitiveParm("path") AnimationPath2D path = null;

	@PrimitiveParm("u0") double u0 = 0.0;

	@PrimitiveParm("t0") double t0 = 0.0;

	@PrimitiveParm(value = "velocity")
	    Double pathVelocity = null;

	@PrimitiveParm("acceleration") Double pathAccel = null;

	@PrimitiveParm("angleRelative") Boolean angleRelative = null;

	@PrimitiveParm("pathAngle") Double angle = null;

	@PrimitiveParm("angularVelocity") Double angularVelocity = null;

	@PrimitiveParm("angularAcceleration") Double angularAccel = null;

	@PrimitiveParm("distanceFunction") SimFunction distFunction = null;

	@PrimitiveParm("angleFunction") SimFunction angleFunction = null;
   }

    @KeyedCompoundParm("timeline")
    Map<Integer,TimelineEntry> timelineMap =
	new HashMap<Integer,TimelineEntry>();


    private boolean defaultAngleRelative = true;

    /**
     * Set the default value for the angleRelative timeline entry.
     * If not called, the value will be <code>true</code>.  For some
     * subclasses (e.g., AbstractGraphViewFactory), the default should
     * be <code>false</code>.
     * @param value the value for the default
     */
    protected void setDefaultAngleRelative(boolean value) {
	defaultAngleRelative = value;
    }


    Animation2D animation;

    DirectedObject2DParmManager<Obj> pm;

    /**
     * Constructor.
     * @param a2d the animation
     */
    protected DirectedObject2DFactory(Animation2D a2d) {
	super(a2d);
	animation = a2d;
	pm = new DirectedObject2DParmManager<Obj>(this);
	initParms(pm, DirectedObject2DFactory.class);
    }

    @Override
    public void clear() {
	super.clear();
	pm.setDefaults(this);
    }

    /**
     * Request subclasses to add entries to the timeline.
     * Each subclass adding entries should create a Callable that performs any
     * necessary changes, which should be returned via a call to
     * addToTimelineResponse, and each subclass that implements
     * addToTimelineRequest must call super.addToTimelineRequest(object, key).
     * @param object the object being configured.
     * @param key the timeline key
     */
    @Override
    protected void addToTimelineRequest(final Obj object, int key,
					final double time)
    {
	super.addToTimelineRequest(object, key, time);
	TimelineEntry tle = timelineMap.get(key);
	if (tle != null) {
	    final boolean setPath = (tle.path != null);
	    final AnimationPath2D path = (tle.path == animation.nullPath())?
		null: tle.path;
	    final boolean angleRelative = (tle.angleRelative == null)?
		defaultAngleRelative: tle.angleRelative;
	    final boolean setv = (tle.pathVelocity != null);
	    final double v = (setv)? tle.pathVelocity: 0.0;
	    final boolean seta = (tle.pathAccel != null);
	    final double a = seta? tle.pathAccel: 0.0;

	    final double u0 = tle.u0;
	    final double t0 = tle.t0;
	    final double angle = (tle.angle != null)?
		Math.toRadians(tle.angle): 0.0;
	    final boolean setdf = (tle.distFunction != null);
	    final SimFunction df =
		(tle.distFunction == animation.nullFunction())? null:
		tle.distFunction;
	    final boolean setangf = (tle.angleFunction != null);
	    final SimFunction angf =
		(tle.angleFunction == animation.nullFunction())? null:
		tle.angleFunction;
	    final boolean setw = (tle.angularVelocity != null);
	    final double w = setw? Math.toRadians(tle.angularVelocity): 0.0;
	    final boolean setaa = (tle.angularAccel != null);
	    final double aa = setaa? Math.toRadians(tle.angularAccel): 0.0;
	    addToTimelineResponse(new Callable() {
		    public void call() {
			if (setPath) {
			    object.setPath(path, u0, angle,
					   angleRelative, t0);
			}
			if (setv) object.setPathVelocity(v);
			if (seta) object.setPathAcceleration(a);
			if (setw) {
			    if (object.hasPath()) {
				object.setPathAngularVelocity(w);
			    } else {
				object.setAngularVelocity(w);
			    }
			}
			if (setaa) {
			    if (object.hasPath()) {
				object.setPathAngularAcceleration(aa);
			    } else {
				object.setAngularAcceleration(aa);
			    }
			}
			if (setdf) object.setDistanceBySF(df);
			if (setangf) {
			    if (object.hasPath()) {
				object.setPathAngleBySF(angf);
			    } else {
				object.setAngleBySF(angf);
			    }
			}
		    }
		});
	}
    }
}
