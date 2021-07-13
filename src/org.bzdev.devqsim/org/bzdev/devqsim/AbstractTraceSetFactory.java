package org.bzdev.devqsim;

import org.bzdev.obnaming.annotations.FactoryParmManager;
import org.bzdev.obnaming.annotations.PrimitiveParm;
import org.bzdev.obnaming.annotations.KeyedCompoundParm;
import org.bzdev.obnaming.annotations.CompoundParmType;
import org.bzdev.obnaming.annotations.CompoundParm;
import org.bzdev.lang.Callable;

import java.util.Map;
import java.util.HashMap;

/**
 * Abstract class for TraceSet factories.
 * <P>
 * The parameters are defined fort this class are the same ones defined for
 * {@link TraceSetFactory}:
 * <P>
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/TraceSetFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/TraceSetFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * <P>
 */
@FactoryParmManager(value = "TraceSetParmManager",
		    tipResourceBundle = "*.lpack.TraceSetTips",
		    labelResourceBundle = "*.lpack.TraceSetLabels")
 abstract public class AbstractTraceSetFactory<OBJ extends TraceSet>
    extends DefaultSimObjectFactory<OBJ>
{
    @PrimitiveParm(value="level", lowerBound="-1", lowerBoundClosed=true)
	int level = 0;
    @PrimitiveParm("stackTraceMode") boolean stackTraceMode = false;
    @PrimitiveParm(value = "stackTraceLimit", lowerBound = "0",
		   lowerBoundClosed = true)
	int stackTraceLimit = 0;

    @CompoundParmType(tipResourceBundle = "*.lpack.TimelineTraceSetTips",
		      labelResourceBundle = "*.lpack.TimelineTraceSetLabels")
    static class TimelineEntry {
    @PrimitiveParm(value="level", lowerBound="-1", lowerBoundClosed=true)
	Integer level = null;
    @PrimitiveParm("stackTraceMode") Boolean stackTraceMode = null;
    @PrimitiveParm(value = "stackTraceLimit", lowerBound = "0",
		   lowerBoundClosed = true)
	Integer stackTraceLimit = null;
    }

    @KeyedCompoundParm("timeline")
    Map<Integer,TimelineEntry> timelineMap =
	new HashMap<Integer,TimelineEntry>();

    TraceSetParmManager<OBJ> pm;

    /**
     * Constructor.
     * @param sim the simulation used to name objects.
     */
    protected AbstractTraceSetFactory(Simulation sim) {
	super(sim);
	// it makes no sense to trace a trace set.
	removeParm("traceSets");
	removeParm("timeline.traceSetMode");
	removeParm("timeline.traceSets");
	pm = new TraceSetParmManager<OBJ>(this);
	initParms(pm, AbstractTraceSetFactory.class );
 }

    @Override
    public void clear() {
	super.clear();
	pm.setDefaults(this);
    }


    @Override
    protected void addToTimelineRequest(OBJ object, int key, double time) {
	super.addToTimelineRequest(object, key, time);
	TimelineEntry tle = timelineMap.get(key);
	if (tle != null) {
	    final OBJ obj = object;
	    final boolean hasLevel = (tle.level != null);
	    final boolean hasSTM = (tle.stackTraceMode != null);
	    final boolean hasSTL = (tle.stackTraceLimit != null);

	    final int level = (hasLevel)? tle.level: -1;
	    final boolean stm = (hasSTM)? tle.stackTraceMode: false;
	    final int stl = (hasSTL)? tle.stackTraceLimit: 0;

	    addToTimelineResponse(new Callable() {
		    public void call() {
			if (hasLevel) {
			    obj.setLevel(level);
			}
			if (hasSTM) {
			    obj.setStackTraceMode(stm);
			}
			if (hasSTL) {
			    obj.setStackTraceLimit(stl);
			}
		    }
		});
	}
    }

    @Override
    protected void initObject(OBJ object) {
	super.initObject(object);
	object.setLevel(level);
	object.setStackTraceMode(stackTraceMode);
	object.setStackTraceLimit(stackTraceLimit);
    }
}

//  LocalWords:  TraceSet TraceSetFactory IFRAME SRC px steelblue
//  LocalWords:  HREF TraceSetParmManager stackTraceMode traceSets
//  LocalWords:  stackTraceLimit traceSetMode
