package org.bzdev.anim2d;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;
import org.bzdev.devqsim.SimObjectFactory;

import org.bzdev.obnaming.annotations.FactoryParmManager;
import org.bzdev.obnaming.annotations.PrimitiveParm;
import org.bzdev.obnaming.annotations.KeyedCompoundParm;
import org.bzdev.obnaming.annotations.CompoundParmType;

import org.bzdev.lang.Callable;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Abstract factory for animation objects.
 * The parameters supported are the following:
 * <ul>
 *   <li> "timeline" - an integer-keyed set of values that define
 *         changes in the object's configuration. Subclasses may provide
 *         additional parameters. The parameters are:
 *        <ul>
 *           <li> "timeline.time" - the time at which timeline parameters
 *                are to change. This parameter must be provided if a
 *                timeline entry exists.  The units are those used by the
 *                double-precision time unit for the simulation (for
 *                animations, this is generally seconds).
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
  *          <li> "timeline.traceSets" - a parameter representing a set
 *                of TraceSet objects (the three-argument 'add' method
 *                is used to add entries).
*            <li> "timeline.visible" -the visibility of the object (true
 *                if visible; false if not visible; not defined for a
 *                given key indicates no change);
 *           <li> "timeline.zorder" - the zorder for the object.
 *                If this parameter is not defined for the key,
 *                the previous value is not changed.
 *        <ul>
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
 * </ul>
 */
@FactoryParmManager(value = "AnimationObject2DParmManager",
		    tipResourceBundle = "*.lpack.AnimObj2DTips",
		    labelResourceBundle = "*.lpack.AnimObj2DLabels")
abstract public class AnimationObject2DFactory<Obj extends AnimationObject2D>
    extends SimObjectFactory<AnimationObject2DFactory<Obj>,Animation2D,Obj>
{
    Animation2D animation;

    /**
     * Get the animation associated with this factory.
     * @return the animation.
     */
    protected Animation2D getAnimation() {return animation;}


    private long DEFAULT_ZORDER = 0;
    private boolean DEFAULT_VISIBILITY = true;

    /**
     * Set the default Z order and visibility for this factory.
     * Most subclasses should not call this method.  For this package,
     * there is one exception: factories that create an
     * AnimationShape2D.  The exception occurs because the
     * AnimationShape2D class was added to simplify creating animation
     * layers when a graphics editor (e.g., the epts program) was used
     * to create a series of paths to represent a boundary. Since
     * instances of AnimationShape2D will frequently be used in an
     * animation layer, the appropriate default visibility for this
     * special case is <CODE>false</CODE>. In nearly all other cases,
     * and all other cases in this package, the appropriate default is
     * <CODE>true</CODE>.
     * @param visibility the default visibility (<CODE>true</CODE> for
     *        visible and <CODE>false</CODE> for not visible)
     * @param zorder the default Z order for objects created by this factory.
     */
    protected void setAnimationObjectDefaults(boolean visibility, long zorder) {
	DEFAULT_ZORDER = zorder;
	DEFAULT_VISIBILITY = visibility;
    }

    long zorder = DEFAULT_ZORDER;
    boolean visible = DEFAULT_VISIBILITY;

    /**
     * Get Z order for a new object.
     * @return the Z order.
     */
    public long getZorder(){return zorder;}

    /**
     * Get the visibility for a new object.
     * @return true if the object should be visible; false otherwise
     */
    public boolean getVisibility() {return visible;}

    @CompoundParmType(tipResourceBundle = "*.lpack.AnimObjTimeLineTips",
		      labelResourceBundle = "*.lpack.AnimObjTimeLineLabels",
		      docResourceBundle = "*.lpack.AnimObjTimeLineDocs")
    static class TimelineEntry {
	// Double time = null;
	@PrimitiveParm("visible") Boolean visible = null;
	@PrimitiveParm("zorder") Long zorder = null;
    }

    @KeyedCompoundParm("timeline")
    Map<Integer,TimelineEntry> timelineMap =
	new HashMap<Integer,TimelineEntry>();

    Parm parms[] = {
	new Parm("zorder", null,
		 new ParmParser() {
		     public void parse(long value)
			 throws IllegalArgumentException
		     {
			 zorder = value;
		     }
		     public void clear() {
			 zorder = DEFAULT_ZORDER;
		     }
		 },
		 long.class),
	new Parm("visible", null,
		 new ParmParser() {
		     public void parse(boolean value)
			 throws IllegalArgumentException
		     {
			 visible = value;
		     }
		     public void clear() {
			 visible = DEFAULT_VISIBILITY;
		     }
		 },
		 boolean.class)
    };

    AnimationObject2DParmManager<Obj> pm;

    /**
     * Constructor.
     * @param a2d the Animation2D associated with this factory
     */
    protected AnimationObject2DFactory(Animation2D a2d) {
	super(a2d);
	animation = a2d;
	initParms(parms, AnimationObject2DFactory.class);
	pm = new AnimationObject2DParmManager<Obj>(this);
	initParms(pm, AnimationObject2DFactory.class);
	/*
	addLabelResourceBundle("*.lpack.AnimObj2DLabels",
			       AnimationObject2DFactory.class);
	addTipResourceBundle("*.lpack.AnimObj2DTips",
			     AnimationObject2DFactory.class);
	*/
    }

    @Override
    public void clear() {
	super.clear();
	zorder = DEFAULT_ZORDER;
	visible = DEFAULT_VISIBILITY;
	pm.setDefaults(this);
	// timelineMap.clear();
    }

    @Override
    protected void initObject(final Obj object) {
	super.initObject(object);
	object.setZorder(zorder, visible);
    }

    /*
     * Request subclasses to add entries to the timeline.
     *
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
	TimelineEntry entry = timelineMap.get(key);
	if (entry != null) {
	    Boolean visible = entry.visible;
	    final boolean setVis = (visible != null);
	    final boolean vis = (visible == null)? false: visible;

	    Long zorder = entry.zorder;
	    final boolean setZo = (zorder != null);
	    final long zo = (zorder == null)? 0: zorder;

	    final Obj obj = object;

	    addToTimelineResponse(new Callable() {
		    public void call() {
			// if (setpos) obj.setPosition(xx, yy, aa);
			if (setVis) obj.setVisible(vis);
			if (setZo) obj.setZorder(zo);
		    }
		});
	}
    }
}

//  LocalWords:  ul li Subclasses traceSetMode traceSets TraceSet yy
//  LocalWords:  zorder SimObject AnimationObject DParmManager epts
//  LocalWords:  subclasses AnimationShape addLabelResourceBundle aa
//  LocalWords:  DFactory addTipResourceBundle timelineMap setpos
//  LocalWords:  addToTimelineResponse addToTimelineRequest
//  LocalWords:  setPosition
