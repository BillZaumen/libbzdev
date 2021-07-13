package org.bzdev.anim2d;
import org.bzdev.obnaming.annotations.FactoryParmManager;
import org.bzdev.obnaming.annotations.PrimitiveParm;
import org.bzdev.obnaming.annotations.KeyedCompoundParm;
import org.bzdev.obnaming.annotations.CompoundParmType;
import org.bzdev.obnaming.annotations.CompoundParm;

import org.bzdev.lang.Callable;

import java.util.Map;
import java.util.TreeMap;
import java.util.HashMap;


/**
 * Abstract GraphView factory.
 * This class extends the class DirectedObject2DFactory.
 * The class GraphViewFactory creates a full implementation.
 * The additional parameters this factory provides are
 * <ul>
 *   <li> "xFrameFraction" - the fractional distance from the graph's
 *        left offset to its right offset at which the point specifying the
 *        X coordinate of a GraphView's location appears. This point's
 *        graph coordinate space coordinates are (initialX, initialY).
 *   <li> "yFrameFraction" - the fractional distance from the graph's
 *        lower offset to its upper offset at which the point specifying the
 *        Y coordinate of the graph's location appears.  This point's
 *        graph coordinate space coordinates are (initialX, initialY).
 *   <li> "scaleX" - the scale factor for the X direction (the amount by
 *        which to multiple a distance in graph coordinate space along the
 *        X axis to get the corresponding distance in user space).
 *   <li> "scaleY" -  the scale factor for the X direction (the amount by
 *        which to multiple a distance in graph coordinate space along the
 *        Y axis to get the corresponding distance in user space).
 *   <li> "zoom" - the zoom level, which must be a positive real number.
 *   <li> "timeline" - a table of entries with integer keys providing
 *        the following parameters in addition to those provided by the
 *        class {@link DirectedObject2DFactory DirectedObject2DFactory}:
 *        <ul>
 *           <li> "timeline.zoomMode" - a selector of type
 *                GraphView.ZoomMode indicating which of the following
 *                4 parameters are valid.  The values are
 *                <CODE>SET_VALUE</CODE> (indicating the
 *                "timeline.zoom" is valid), <CODE>SET_RATE</CODE>
 *                (indicating that "timeline.zoomRate is valid), or
 *                <CODE>SET_TARGET</CODE> (indicating that
 *                "timeline.zoomTarget" and "timeline.zoomInterval"
 *                are valid).  The default value, <CODE>KEEP</CODE>,
 *                indicates that nothing will be changed.
 *           <li> "timeline.zoom" - the zoom level, which must be a
 *                 positive real number, to be set at a specified time (see
 *                 {@link DirectedObject2DFactory DirectedObject2DFactory}).
 *                 This parameter is valid if "timeline.zoomMode" is set
 *                 to <CODE>SET_VALUE</CODE>;
 *           <li> "timeline.zoomRate" - the zoom rate, to be set at a
 *                 specified time.  The value of zoom will vary as
 *                 exp(timeline.zoomRate * (t - time) where t &gt; time and
 *                 where time is value specified by "timeline.time" (see
 *                 {@link DirectedObject2DFactory DirectedObject2DFactory}).
 *                 This parameter is valid if "timeline.zoomMode" is set
 *                 to <CODE>SET_RATE</CODE>;
 *           <li> "timeline.zoomTarget" - the desired zoom level, which
 *                 must be a positive real number.  This parameter is
 *                 valid if "timeline.zoomMode" is set to
 *                 <CODE>SET_TARGET</CODE>;
 *           <li> "timeline.zoomInterval" - the interval, starting at a
 *                 time specified by "timeline.time" (see
 *                 {@link DirectedObject2DFactory DirectedObject2DFactory})
 *                 over which the zoom level should change from its
 *                 value at the the time specified by
 *                 "timeline.time" to the value specified by
 *                 "timeline.zoomTarget.  This parameter is valid
 *                 if "timeline.zoomMode" is set to <CODE>SET_TARGET</CODE>;
 *        </ul>
 * </ul>
 * The parameters that are inherited from DirectedObject2DFactory and its
 * superclasses are:
 * <ul>
 *     <li> "initialX" - the initial X coordinate of the object in graph
 *           coordinate space.
 *     <li> "initialY" - the initial Y coordinate of the object in graph
 *           coordinate space.
 *     <li> "timeline" - an integer-keyed set of values that define
 *           changes in an object as it traverses a path, extended by the
 *          "timeline" parameters defined above.
 *          <ul>
 *             <li> "timeline.time" - the time at which timeline parameters
 *                  are to change (typically measured in seconds, not
 *                  simulation ticks).
 *             <li> "timeline.x" - the X coordinate of the reference point
 *                  at the specified time, provided in graph coordinate
 *                  space units. If this parameter is not defined for the key,
 *                  the previous value is not changed.
 *             <li> "timeline.y" - the Y coordinate of the reference point
 *                  at the specified time, provided in graph coordinate space
 *                  units. If this parameter is not defined for the key,
 *                  the previous value is not changed.
 *             <li> "timeline.path" - the path that the object should follow.
 *                  If 'animation' is the current Animation2D,
 *                  animation.nullPath() will provide a constant path that
 *                  indicates that the path should no longer be used. If no
 *                  value is given, the value is not changed.
 *             <li> "timeline.t0" - the initial time interval from the
 *                  time at which the path is set to the time at which
 *                  the path traversal starts
 *             <li> "timeline.t0" - the initial time interval from the time at
 *                  which the path is set to wait before the object can move.
 *             <li> "timeline.u0" - the initial value of the path's parameter
 *                  (the   default is 0.0).
 *              <li> "timeline.velocity" - the velocity along the path.
 *              <li> "timeline.acceleration" - the acceleration along the path.
 *              <li> "timeline.distanceFunction" - a SimFunction object
 *                   associated with this animation giving the
 *                   distance along the path as a function of time. If
 *                   'animation' is the current Animation2D,
 *                   {@link Animation2D#nullFunction() animation.nullFunction()}
 *                   will provide a function that indicates that the
 *                   current function should no longer be used. If no
 *                   value is given, the function is not changed.
 *              <li> "timeline.angleFunction" - a SimFunction object
 *                   associated with this animation giving the angle
 *                   of the object as it moves along the path as a
 *                   function of time. If 'animation' is the current
 *                   Animation2D,
 *                   {@link Animation2D#nullFunction() animation.nullFunction()}
 *                   will provide a function that indicates that the
 *                   current function should no longer be used. If no
 *                   value is given, the function is not changed.  The
 *                   value returned is in radians, not degrees. If a
 *                   path is currently defined, the time argument
 *                   treats an argument of zero as the time indicated
 *                   by the path's "timeline.t0" parameter. Otherwise
 *                   the argument refers to simulation time. The argument
 *                   uses double-precision time units (not ticks).
 *           <li> "timeline.traceSetMode" - indicates how the parameter
 *                "timeline.traceSets" is interpreted. the values are
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
 *                       and replace those with the ones specified by
 *                       the timeline.traceSets parameter.
 *                </ul>
 *            <li> "timeline.traceSets" - a parameter representing a set
 *                of TraceSet objects (the three-argument 'add' method
 *                is used to add entries).
 *          </ul>
 *   <li> "traceSets" - a set of TraceSets a SimObject will use
 *        for tracing.  One should use the add and remove factory
 *        methods as this parameter refers to a set of values.
 * </ul>
 * <P>
 * Note that some of the parameters defined by superclasses are not shown.
 * These have been removed because they are not appropriate for a GraphView.
 */
@FactoryParmManager(value = "GraphViewParmManager",
		    tipResourceBundle = "*.lpack.GraphViewTips",
		    labelResourceBundle = "*.lpack.GraphViewLabels",
		    docResourceBundle = "*.lpack.GraphViewDocs")
abstract public class AbstrGraphViewFactory<Obj extends GraphView>
    extends DirectedObject2DFactory<Obj>
{
    @PrimitiveParm("xFrameFraction") double xf = 0.5;
    @PrimitiveParm("yFrameFraction") double yf = 0.5;

    @PrimitiveParm(value="scaleX", lowerBound="0.0", lowerBoundClosed=false)
	double scaleX = 1.0;
    @PrimitiveParm(value="scaleY", lowerBound="0.0", lowerBoundClosed=false)
	double scaleY = 1.0;

    @PrimitiveParm(value="zoom", lowerBound="0.0", lowerBoundClosed=false)
	double zoom = 1.0;

    @CompoundParmType(tipResourceBundle = "*.lpack.GVTimeLineTips",
		      labelResourceBundle = "*.lpack.GVTimeLineLabels",
		      docResourceBundle = "*.lpack.GVTimeLineDocs")
    static class  PathTimeLine {

	@PrimitiveParm("zoomMode")
	    GraphView.ZoomMode mode = GraphView.ZoomMode.KEEP;

	@PrimitiveParm(value="zoom", lowerBound = "0.0", lowerBoundClosed=false)
	    double zoom = 1.0;

	@PrimitiveParm("zoomRate")
	    double zoomRate = 0.0;

	@PrimitiveParm(value="zoomTarget",
		       lowerBound = "0.0",
		       lowerBoundClosed=false)
	    double zoomTarget = 1.0;

	@PrimitiveParm(value="zoomInterval", lowerBound = "0.0")
	    double zoomInterval = 4.0;
    }

    @KeyedCompoundParm("timeline")
    Map<Integer,PathTimeLine> gvTimelineMap
	= new HashMap<Integer,PathTimeLine>();

    GraphViewParmManager<Obj> pm;

    /**
     * Constructor.
     * @param a2d the animation
     */
    protected AbstrGraphViewFactory(Animation2D a2d) {
	super(a2d);
	setDefaultAngleRelative(false);
	removeParm("refPointMode");
	removeParm("refPointName");
	removeParm("refPointX");
	removeParm("refPointY");
	removeParm("refPointFractX");
	removeParm("refPointFractY");
	removeParm("zorder");
	mayIgnoreTimelineAngle();
	removeParm("timeline.zorder");

	pm = new GraphViewParmManager<Obj>(this);
	initParms(pm, AbstrGraphViewFactory.class);
    }

    @Override
    public void clear() {
	super.clear();
	pm.setDefaults(this);
    }

    @Override
    protected void addToTimelineRequest(final Obj object, int key,
					double time)
    {
	super.addToTimelineRequest(object, key, time);
	PathTimeLine ptl = gvTimelineMap.get(key);

	Callable callable;
	switch(ptl.mode) {
	case SET_VALUE:
	    final double zoom = ptl.zoom;
	    callable = new Callable() {
		    public void call() {
			object.setZoom(zoom);
		    }
		};
	    break;
	case SET_RATE:
	    final double zoomRate = ptl.zoomRate;
	    callable = new Callable() {
		    public void call() {
			object.setLogZoomRate(zoomRate);
		    }
		};
	    break;
	case SET_TARGET:
	    final double zoomTarget = ptl.zoomTarget;
	    final double zoomInterval = ptl.zoomInterval;
	    callable = new Callable() {
		    public void call() {
			object.setLogZoomRate(zoomTarget, zoomInterval);
		    }
		};
	    break;
	default:
	    callable = null;
	    break;
	}
	if (callable != null) {
	    addToTimelineResponse(callable);
	}
    }

    @Override
    protected void initObject(final Obj object) {
	super.initObject(object);
	// super.initObject will normally set zorder to a default
	// value, so we need to override that explicitly (the
	// zorder parameter is hidden but that does not stop the
	// initialization).
	object.setZorder(Long.MIN_VALUE, true);
	object.initialize(object.getX(), object.getY(),
			  xf, yf, scaleX, scaleY, zoom);

    }
}

//  LocalWords:  GraphView DirectedObject DFactory GraphViewFactory
//  LocalWords:  ul li xFrameFraction GraphView's initialX initialY
//  LocalWords:  yFrameFraction scaleX scaleY zoomMode zoomRate
//  LocalWords:  zoomTarget zoomInterval superclasses nullPath zorder
//  LocalWords:  distanceFunction SimFunction nullFunction traceSets
//  LocalWords:  angleFunction traceSetMode TraceSet SimObject
//  LocalWords:  GraphViewParmManager refPointMode refPointName
//  LocalWords:  refPointX refPointY refPointFractX refPointFractY
//  LocalWords:  initObject
