package org.bzdev.devqsim;
import org.bzdev.obnaming.*;
import org.bzdev.lang.Callable;

import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.LinkedList;


//@exbundle org.bzdev.obnaming.lpack.ParmParser

/**
 * SimObjectFactory base class.
 * The only parameters are the following:
 * <ul>
 *   <li> "timeline" - an integer-keyed set of values that define
 *         changes in the object's configuration at specified pionts
 *         in simulation time.  Subclasses may optionally provide additional
 *         parameters as described below.  The default parameters are:
 *        <ul>
 *           <li> "timeline.time" - the time at which timeline parameters
 *                are to change. This parameter must be provided if a
 *                timeline entry exists.  The units are those used by the
 *                double-precession time unit for the simulation (for
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
 *           <li> "timeline.traceSets" - a parameter representing a set
 *                of TraceSet objects (the three-argument 'add' method
 *                is used to add entries).
 *        </ul>
 *   <li> "traceSets" - a set of trace sets (class TraceSet) a SimObject will use
 *        for tracing.  One should use the add and remove factory
 *        methods as this parameter refers to a set of values. This
 *        parameter provides the initial value for an object's trace sets.
 *        If this parameter or the corresponding timeline parameters are
 *        not used, an object will not have any trace sets.
 * </ul>
 * This class also provides support for subclasses that add
 * parameters to timelines.  To add parameters to a timeline using
 * annotations, a subclass will define an inner class (the name is
 * arbitrary, but Timeline is a good choice). The following example
 * shows how to' add a parameter named "foo" with a value that is a
 * double-precision number.  One starts with an inner class definition
 * and a corresponding map for a keyed compound parameter with an
 * integer key and name of "timeline":
 * <blockquote>
 * <code><pre>
 *        {@literal @}CompoundParmType(tipResourceBundle = "*.lpack.TIP_NAME",
 *                          labelResourceBundle = "*.lpack.LABEL_NAME")
 *        static class TimelineEntry {
 *          {@literal @}PrimitiveParm("foo") Double foo = null;
 *        }
 *        {@literal @}KeyedCoumpoundParm("timeline")
 *        Map&lt;Integer,TimelineEntry&gt; timeline =
 *             new HashMap&lt;Integer,Timeline&gt;();
 * </pre></code>
 * </blockquote>
 * The result is that a new parameter named "timeline.foo" will be
 * defined.  The "timeline" parameter alone can be used to remove
 * an entry or clear the tables.  To implement this behavior, the
 * annotation processor creates a {@link org.bzdev.obnaming.Parm}
 * entry with the name "timeline" but uses a multi-argument constructor
 * for its {@link org.bzdev.obnaming.ParmParser} entry.
 *<P>
 * Then one overrides a protected method named addToTimelineRequest
 * which is required to call the same method with the same arguments
 * on its superclass (nearly always, this should be the first statement):
 * <blockquote>
 * <code><pre>
 *        {@literal @}Override
 *        protected void addToTimelineRequest(final OBJ obj,
 *                                           int key,
 *                                           double time)
 *        {
 *            super.addToTimelineRequest(obj, key, time);
 *            TimelineEntry entry = timelineMap.get(key);
 *            final boolean hasFoo = (entry.foo != null);
 *            final double foo = (hasFoo? entry.foo: 0.0);
 *            addToTimelineResponse(new Callable() {
 *                public void call() {
 *                    if (hasFoo) obj.setFoo(foo);
 *                }
 *            });
 *        }
 * </pre></code>
 * </blockquote>
 * Each addToTimelineRequest method that wishes to provide some code that
 * will run at the specified time must call addToTimelineResponse
 * with an argument that implements the interface org.bzdev.lang.Callable.
 * In the example, the test of the value hasFoo results in no call to
 * setFoo if the parameter was not defined. Be sure that the clear()
 * method will clear the timeline table or unpredictable behavior may
 * occur (this is done automatically if the timeline table is annotated
 * for use with a parameter manager).
 * While super.addToTimelineRequest must always be called in
 * an overriding addToTimelineRequest method, the call to addToTimelineResponse
 * may be omitted if one can determine that there is nothing for it to do.
 * As a debugging hint, if addToTimelineRequest is not being called when
 * a call is expected, check the initObject methods to make sure that
 * each overrriden method calls super.initObject on the overridden method's
 * argument:
 * <blockquote><code><pre>
 *        {@literal @}Override
 *        protected void initObject(OBJ object) {
 *             super.initObject(object);
 *             ...
 *        }
 * </pre></code></blockquote>
 * <P>
 * In the example above, the class TimelineEntry was annotated with the
 * CompoundParmType annotation. Classes with this annotation cannot
 * have members with a CompoundParm annotation: for example
 * <blockquote>
 * <code><pre>
 *        {@literal @}CompoundParmType(tipResourceBundle = "*.lpack.TIP_NAME",
 *                          labelResourceBundle = "*.lpack.LABEL_NAME")
 *        static class TimelineEntry {
 *          {@literal @}PrimitiveParm("foo") Double foo = null;
 *          {@literal @}CompoundParm("msgFontColor")
 *               ColorParm msgFontColor = new ColorParm();
 *        }
 * </pre></code>
 * </blockquote>
 * is illegal.  To get the desired effect, use
 * <blockquote>
 * <code><pre>
 *        {@literal @}CompoundParmType(tipResourceBundle = "*.lpack.TIP_NAME",
 *                          labelResourceBundle = "*.lpack.LABEL_NAME")
 *        static class TimelineEntry {
 *          {@literal @}PrimitiveParm("foo") Double foo = null;
 *          {@literal @}PrimitiveParm("msgFontColor.red")
 *             Integer red = null;
 *          {@literal @}PrimitiveParm("msgFontColor.green")
 *             Integer green = null;
 *          {@literal @}PrimitiveParm("msgFontColor.blue")
 *             Integer blue = null;
 *          {@literal @}PrimitiveParm("msgFontColor.alpha")
 *             Integer alpha = null;
 *          {@literal @}PrimitiveParm("msgFontColor.css")
 *             Integer css = null;
 *        }
 * </pre></code>
 * </blockquote>
 * and to allow msgFontColor to be restored to its default value,
 * add a Parm instance explicitly in the constructor:
 * <blockquote>
 * <code><pre>
 *    initParm(new Parm("timeline.msgFontColor", int.class, null
 *			 new ParmParser() {
 *			     public void clear(int key) {
 *				 TimelineEntry tle = timeline.get(key);
 *				 if (tle != null) {
 *				     tle.red = null;
 *				     tle.green = null;
 *				     tle.blue = null;
 *				     tle.alpha = null;
 *				 }
 *			     }
 *			 },
 *			 null,
 *			 null, true, null, true),
 *		 THIS_FACTORY_CLASS_NAME.class);
 * </pre></code>
 * </blockquote>
 * If msgFontColor is considered to be missing, the red, green, blue, and
 * alpha fields will all be null. When overriding
 * {@link #addToTimelineRequest(SimObject,int,double)}, one should
 * set the color only if at least one of these four fields is not null.
 */
public abstract class SimObjectFactory<
    F extends SimObjectFactory<F, S, OBJ>,
    S extends Simulation,
    OBJ extends SimObject>
    extends NamedObjectFactory<F, Simulation, SimObject, OBJ> {

    private static String errorMsg(String key, Object... args) {
	return Simulation.errorMsg(key, args);
    }

    S sim;

    Set<TraceSet> traceSets = new LinkedHashSet<TraceSet>();


    static class Timeline {
	Double time = null;
	TraceSetMode traceSetMode = TraceSetMode.KEEP;
	Set<TraceSet> traceSets = new LinkedHashSet<TraceSet>();
    }
    Map<Integer,Timeline> timelineMap = new TreeMap<Integer,Timeline>();

    static Class<?> carray[] = {int.class, TraceSet.class};
    static ParmKeyType qname = new ParmKeyType(carray,  true);

    Parm parms[] = {
	new Parm("traceSets", TraceSet.class, null,
		 new ParmParser() {
		    public void parse(org.bzdev.obnaming.NamedObjectOps value) {
			if (value instanceof TraceSet) {
			    traceSets.add((TraceSet) value);
			} else {
			    throw new IllegalArgumentException
				(errorMsg("wrongType1", getParmName()));
				/*("wrong type");*/
			}
		    }
		    public void clear() {
			traceSets.clear();
		    }
		    public void clear(org.bzdev.obnaming.NamedObjectOps value) {
			if (value instanceof TraceSet) {
			    traceSets.remove(value);
			}
		    }
		 },
		 null,
		 null, true, null, true),
	new Parm("timeline", int.class, null,
		 new ParmParser() {
		     public void parse(int key) {
			 // int mapKey = key;
			 SimObjectFactory.Timeline mapValue =
			     SimObjectFactory.this.timelineMap.get(key);
			 if (mapValue == null) {
			     mapValue = new
				 SimObjectFactory.Timeline();
			     timelineMap.put(key, mapValue);
			 }
		     }
		     public void clear() {
			 timelineMap.clear();
		     }
		     public void clear(int key) {
			 timelineMap.remove(key);
		     }
		}, null),
	new Parm("timeline.time", int.class, null,
		 new ParmParser()
		 {
		     public void parse(int key, double value) {
			 try {
			     SimObjectFactory.this.add("timeline", key);
			 } catch (Exception e) {}
			 SimObjectFactory.Timeline mapValue =
			     timelineMap.get(key);
			 if (mapValue == null) {
			     mapValue = new SimObjectFactory.Timeline();
			     timelineMap.put(key, mapValue);
			 }
			 mapValue.time = value;
		     }
		     public void clear(int key) {
			 SimObjectFactory.Timeline mapValue =
			     timelineMap.get(key);
			 if (mapValue != null) {
			     mapValue.time = null;
			 }
		     }
		 },
		 java.lang.Double.class,
		 null, true, null, true),
	new Parm("timeline.traceSetMode",
		 int.class, null,
		 new ParmParser() {
		     public void parse(int key, java.lang.Enum<?> value) {
			 if (value instanceof TraceSetMode) {
			     TraceSetMode val = (TraceSetMode) value;
			     try {add("timeline", key);}
			     catch (Exception e) {}
			     Timeline mapValue = timelineMap.get(key);
			     if (mapValue == null) {
				 mapValue = new Timeline();
				 timelineMap.put(key, mapValue);
			     }
			     mapValue.traceSetMode = val;
			 }
		     }
		     public void clear(int key) {
			 Timeline mapValue = timelineMap.get(key);
			 if (mapValue != null) {
			     mapValue.traceSetMode = TraceSetMode.KEEP;
			 }
		     }
		 },
		 TraceSetMode.class,
		 null, true, null, true),
	new Parm("timeline.traceSets", qname, null,
		 new ParmParser() {
		     public void parse(Object[] qvalue) {
			 if (qvalue.length != 2) {
                             throw new IllegalArgumentException
				 (errorMsg("not2subkeys", getParmName()));
			 }
			 if (qvalue[0] instanceof java.lang.Integer
			     && qvalue[1] instanceof TraceSet)
			     {
				 int mapKey =
				     (int)(java.lang.Integer) qvalue[0];
				 TraceSet value =
				     (TraceSet) qvalue[1];
				 try {add("timeline", mapKey);}
				 catch (Exception e) {}
				 Timeline mapValue =
				     timelineMap.get(mapKey);
				 if (mapValue == null) {
				     mapValue = new Timeline();
				     timelineMap.put(mapKey, mapValue);
				 }
				 mapValue.traceSets.add(value);
			     }
		     }
		     public void parse(String qvalue) {
			 String[] parms = qvalue.split("\\.");
			 // String key = parms[0];
			 int mapKey = Integer.valueOf(parms[0]);
			 java.lang.String value = parms[1];
			 try {add("timeline", mapKey);}
			 catch (Exception e) {}
			 Timeline mapValue =
			     timelineMap.get(mapKey);
			 if (mapValue == null) {
			     mapValue = new Timeline();
			     timelineMap.put(mapKey, mapValue);
			 }
			 mapValue.traceSets.add(sim.getObject(value,
							      TraceSet.class));
		     }
		     public void clear(Object[] qvalue) {
			 switch(qvalue.length) {
			 case 1:
			     if (qvalue[0] instanceof java.lang.Integer) {
				 int mapKey =
				     (int) qvalue[0];
				 Timeline mapValue =
				     timelineMap.get(mapKey);
				 if (mapValue != null) {
				     mapValue.traceSets.clear();
				 }
			     } else {
				 String k = keyString(qvalue);
				 String n = getParmName();
				 throw new IllegalArgumentException
				     (errorMsg("wrongKeyType", k, n));
			     }
			     break;
			 case 2:
			     if (qvalue[0] instanceof java.lang.Integer
				 && qvalue[1] instanceof TraceSet)
				 {
				     int mapKey =
					 (int)(java.lang.Integer) qvalue[0];
				     TraceSet value =
					 (TraceSet) qvalue[1];
				     Timeline mapValue =
					 timelineMap.get(mapKey);
				     if (mapValue != null) {
					 mapValue.traceSets.remove(value);
				     }
				 } else {
				 String k = keyString(qvalue);
				 String n = getParmName();
				 throw new IllegalArgumentException
				     (errorMsg("wrongSubkeyType", k, n));
			     }
			     break;
			 default:
			     throw new IllegalArgumentException
				 (errorMsg("wrongNumberOfKeys", getParmName()));
			 }
		     }
		     public void clear(String qvalue) {
			 String[] parms = qvalue.split("\\.");
			 int mapKey = Integer.valueOf(parms[0]);
			 if (parms.length > 1) {
			     java.lang.String value = parms[1];
			     Timeline mapValue =
				 timelineMap.get(mapKey);
			     if (mapValue != null) {
				 mapValue.traceSets
				     .remove(sim.getObject(value,
							   TraceSet.class));
			     }
			 } else {
			     Timeline mapValue =
				 timelineMap.get(mapKey);
			     if (mapValue != null) {
				 mapValue.traceSets.clear();
			     }
			 }
		     }
		 },
		 null,
		 null, true, null, true)
    };

    /**
     * Constructor.
     * @param namer the simulation used to name objects.
     */
    protected SimObjectFactory(S namer) {
	super(namer);
	sim = namer;
	initParms(parms, SimObjectFactory.class);
	addLabelResourceBundle("*.lpack.SimObjectLabels",
			       SimObjectFactory.class);
	addTipResourceBundle("*.lpack.SimObjectTips",
			     SimObjectFactory.class);
	addDocResourceBundle("*.lpack.SimObjectDocs",
			     SimObjectFactory.class);
    }

    @Override
    public void clear() {
	super.clear();
	traceSets.clear();
	timelineMap.clear();
    }

    static class TimedCallListEntry {
	Callable c;
	long time;

	public TimedCallListEntry(Callable c, long time) {
	    this.c = c;
	    this.time = time;
	}
    }

    LinkedList<TimedCallListEntry> ctlist = new LinkedList<>();
    private ArrayList<Callable>subclassTimeline = new ArrayList<>();


    /**
     * Get the simulation associated with a named-object factory.
     * @return the simulation
     */
    public S getSimulation() {
	return sim;
    }


    @Override
	protected void initObject(OBJ object) {
	for (TraceSet tset: traceSets) {
	    object.addTraceSet(tset);
	}
	ctlist.clear();
	for (Map.Entry<Integer,Timeline> entry: timelineMap.entrySet()) {
	    int key = entry.getKey();
	    Timeline timelineEntry = entry.getValue();
	    Double time = timelineEntry.time;
	    if (time == null) {
		throw new 
		    IllegalStateException(errorMsg("noTimeForTimeline", key));
	    }
	    double xtime = (double)time;
	    long itime = Math.round(xtime * sim.getTicksPerUnitTime());
	    subclassTimeline.clear();
	    addToTimelineRequest(object, key, xtime);
	    final Callable[] callList =
		subclassTimeline.toArray(new Callable[subclassTimeline.size()]);
	    subclassTimeline.clear();

	    ctlist.add(new TimedCallListEntry(new Callable() {
		    public void call() {
			for (Callable c: callList) {
			    c.call();
			}
		    }
		}, itime));
	}
	for (TimedCallListEntry ctle: ctlist) {
	    sim.scheduleCall(ctle.c, ctle.time);
	}
   }

    /**
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
     * @param time the time for the timeline entry
     */
    protected void addToTimelineRequest(OBJ object, int key, double time) {
	Timeline tle = timelineMap.get(key);
	if (tle != null && (tle.traceSets.size() != 0
			    || tle.traceSetMode != TraceSetMode.KEEP)) {
	    int sz = tle.traceSets.size();
	    final TraceSet[] traceSets =  (sz == 0)? null:
		new TraceSet[tle.traceSets.size()];
	    if (sz > 0) {
		tle.traceSets.toArray(traceSets);
	    }

	    final OBJ obj = object;

	    Callable callable = null;
	    switch (tle.traceSetMode) {
	    case KEEP:
		callable = new Callable() {
			public void call() {
				for (TraceSet ts: traceSets) {
				    obj.addTraceSet(ts);
				}

			}
		    };
		break;
	    case REMOVE:
		if (sz > 0) {
		    callable = new Callable() {
			    public void call() {
				for (TraceSet ts: traceSets) {
				    obj.removeTraceSet(ts);
				}
			    }
			};
		}
		break;
	    case REPLACE:
		if (sz == 0) {
		    callable = new Callable() {
			    public void call() {
				obj.clearTraceSets();
			    }
			};
		} else {
		    callable = new Callable() {
			    public void call() {
				obj.clearTraceSets();
				for (TraceSet ts: traceSets) {
				    obj.addTraceSet(ts);
				}
			    }
			};
		}
		break;
	    }
	    addToTimelineResponse(callable);
	}
    }

    /**
     * Allows a subclass to provide a Callable in response to a superclass
     * calling addToTimelineRequest.  The callable will be executed at the
     * time associated with the timeline entry indexed by the key passed to
     * {@link #addToTimelineRequest}.
     * @param callable the Callable provided by the subclass.
     */
    protected final void addToTimelineResponse(Callable callable) {
	if (/*subclassTimeline != null && */ callable != null) {
	    subclassTimeline.add(callable);
	}
    }
}

//  LocalWords:  exbundle SimObjectFactory ul li timeline traceSets
//  LocalWords:  traceSetMode TraceSet SimObject pre lt
//  LocalWords:  NamedObjectFactory timelines blockquote HashMap Parm
//  LocalWords:  CompoundParmType tipResourceBundle PrimitiveParm tle
//  LocalWords:  labelResourceBundle KeyedCoumpoundParm multi boolean
//  LocalWords:  addToTimelineRequest superclass timelineMap hasFoo
//  LocalWords:  addToTimelineResponse setFoo CompoundParm ColorParm
//  LocalWords:  msgFontColor initParm ParmParser TimelineEntry parms
//  LocalWords:  wrongType mapKey subkeys wrongKeyType namer
//  LocalWords:  wrongSubkeyType wrongNumberOfKeys noTimeForTimeline
//  LocalWords:  subclassTimeline
