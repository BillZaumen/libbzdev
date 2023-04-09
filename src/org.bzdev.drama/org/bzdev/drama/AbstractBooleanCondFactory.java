package org.bzdev.drama;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;
import org.bzdev.obnaming.annotations.FactoryParmManager;
import org.bzdev.obnaming.annotations.PrimitiveParm;
import org.bzdev.obnaming.annotations.KeyedCompoundParm;
import org.bzdev.obnaming.annotations.CompoundParmType;
import org.bzdev.lang.Callable;

import java.util.Map;
import java.util.HashMap;

/**
 * Simulation factory for subclasses of BooleanCondition.
 * <P>
 * Generally, there will be a class hierarchy of abstract factories
 * matching the class hierarchy for subclasses of the class a factory
 * creates, each responsible for providing parameters for the
 * corresponding subclass. In addition, there will typically be a
 * factory that is not abstract for each class that can be created.
 * <P>
 * The factory parameters this factory provides are the same as the parameters
 * provided by its subclass {@link BooleanConditionFactory}:
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/drama/BooleanConditionFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/drama/BooleanConditionFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 */
@FactoryParmManager(value = "BooleanConditionParmManager",
		    tipResourceBundle = "*.lpack.ConditionTips",
		    labelResourceBundle = "*.lpack.ConditionLabels")
abstract public class AbstractBooleanCondFactory<Obj extends BooleanCondition>
    extends AbstractConditionFactory<Obj>
{
    private boolean initInitialValueParmCalled = false;
    private boolean DEFAULT_INITIAL_VALUE = false;
    private boolean initialValue = DEFAULT_INITIAL_VALUE;

    /**
     * Add the initialValue parameter.
     * This should be called by a subclass' constructor immediately after
     * the superclass' constructor is called.
     * @param defaultInitialValue the default initial value.
     */
    protected void initInitialValueParm(boolean defaultInitialValue)
    {
	if (initInitialValueParmCalled) {
	    throw new IllegalStateException
		("initInitialValueParm already called");
	}
	initInitialValueParmCalled = true;
	DEFAULT_INITIAL_VALUE = defaultInitialValue;
	initialValue = DEFAULT_INITIAL_VALUE;
	initParm(new
		 Parm("initialValue",
		      null, null,
		      new ParmParser() {
			  public void parse(boolean value)
			      throws IllegalArgumentException
			  {
			      initialValue = value;
			  }
			  public void clear() {
			      initialValue = DEFAULT_INITIAL_VALUE;
			  }
		      },
		      boolean.class,
		      null, false, null, false),
		 AbstractBooleanCondFactory.class);
    }

    @CompoundParmType(tipResourceBundle = "*.lpack.TimelineCondTips",
		      labelResourceBundle = "*.lpack.TimelineCondLabels",
		      docResourceBundle = "*.lpack.TimelineCondDocs")
    static class TimelineEntry {
	@PrimitiveParm("value") Boolean value = null;
    }

    @KeyedCompoundParm("timeline")
    Map<Integer,TimelineEntry> timelineMap =
	new HashMap<Integer,TimelineEntry>();


    BooleanConditionParmManager<Obj> pm;

    /**
     * Constructor.
     * Subclasses must call this constructor.
     * @param sim the simulation
     */
    protected AbstractBooleanCondFactory (DramaSimulation sim)
    {
	super(sim);
	pm = new BooleanConditionParmManager<Obj>(this);
	initParms(pm, AbstractBooleanCondFactory.class);
	/*
	addLabelResourceBundle("*.lpack.ConditionLabels",
			       AbstractBooleanCondFactory.class);
	addTipResourceBundle("*.lpack.ConditionTips",
			     AbstractBooleanCondFactory.class);
	*/
    }

    @Override
    public void clear() {
	super.clear();
	if (initInitialValueParmCalled) {
	    initialValue = DEFAULT_INITIAL_VALUE;
	}
	pm.setDefaults(this);
    }

    @Override
    protected void addToTimelineRequest(final Obj object, int key,
					final double time)
    {
	super.addToTimelineRequest(object, key, time);
	TimelineEntry tle = timelineMap.get(key);
	if (tle != null && tle.value != null) {
	    final boolean value = tle.value;
	    addToTimelineResponse(new Callable() {
		    public void call() {
			object.setValue(value);
		    }
		});
	}
    }

    @Override
    protected void initObject(Obj object) {
	super.initObject(object);
	if (initInitialValueParmCalled) {
	    object.setValue(initialValue);
	}
    }
}

//  LocalWords:  subclasses BooleanCondition BooleanConditionFactory
//  LocalWords:  IFRAME SRC px steelblue HREF initialValue superclass
//  LocalWords:  BooleanConditionParmManager defaultInitialValue
//  LocalWords:  initInitialValueParm addLabelResourceBundle
//  LocalWords:  AbstractBooleanCondFactory addTipResourceBundle
