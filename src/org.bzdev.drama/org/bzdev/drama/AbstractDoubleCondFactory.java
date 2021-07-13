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
 * Simulation factory for subclasses of DoubleCondition.
 * <P>
 * Generally, there will be a class hierarchy of abstract factories
 * matching the class hierarchy for subclasses of the class a factory
 * creates, each responsible for providing parameters for the
 * corresponding subclass. In addition, there will typically be a
 * factory that is not abstract for each class that can be created.
 * <P>
 * The factory parameters this factory provides are the same as the parameters
 * provided by its subclass {@link DoubleConditionFactory}:
 * <P>
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/drama/DoubleConditionFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/drama/DoubleConditionFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 */
@FactoryParmManager(value = "DoubleConditionParmManager",
		    tipResourceBundle = "*.lpack.ConditionTips",
		    labelResourceBundle = "*.lpack.ConditionLabels")
abstract public class AbstractDoubleCondFactory<Obj extends DoubleCondition>
    extends AbstractConditionFactory<Obj>
{
    private boolean initInitialValueParmCalled = false;
    private double DEFAULT_INITIAL_VALUE = 0.0;
    private double initialValue = DEFAULT_INITIAL_VALUE;

    /**
     * Add the initialValue parameter.
     * This should be called by a subclass' constructor immediately after
     * the superclass' constructor is called.
     * @param min the minimum value; null if there is no lower bound
     * @param minClosed true if min is included in the range of acceptable
     *        values; false otherwise
     * @param max the maximum value; null if there is no upper bound
     * @param maxClosed true if max is included in the range of acceptable
     *        values; false otherwise
     * @param defaultInitialValue the default initial value.
     */
    protected void initInitialValueParm(Double min, boolean minClosed,
					Double max, boolean maxClosed,
					double defaultInitialValue)
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
			  public void parse(double value)
			      throws IllegalArgumentException
			  {
			      initialValue = value;
			  }
			  public void clear() {
			      initialValue = DEFAULT_INITIAL_VALUE;
			  }
		      },
		      double.class,
		      min, minClosed, max, maxClosed),
		 AbstractDoubleCondFactory.class);
    }

    @CompoundParmType(tipResourceBundle = "*.lpack.TimelineCondTips",
		      labelResourceBundle = "*.lpack.TimelineCondLabels",
		      docResourceBundle = "*.lpack.TimelineCondDocs")
    static class TimelineEntry {
	@PrimitiveParm("value") Double value = null;
    }

    @KeyedCompoundParm("timeline")
    Map<Integer,TimelineEntry> timelineMap =
	new HashMap<Integer,TimelineEntry>();


    DoubleConditionParmManager<Obj> pm;


    /**
     * Constructor.
     * Subclasses must call this constructor.
     * @param sim the simulation
     */
    protected AbstractDoubleCondFactory (DramaSimulation sim)
    {
	super(sim);
	pm = new DoubleConditionParmManager<Obj>(this);
	initParms(pm, AbstractDoubleCondFactory.class);
	// initFactory();
    }

    public void clear() {
	super.clear();
	if (initInitialValueParmCalled) {
	    initialValue = DEFAULT_INITIAL_VALUE;
	}
	pm.setDefaults(this);
    }

    private void initFactory() {
	addLabelResourceBundle("*.lpack.ConditionLabels",
			       AbstractDoubleCondFactory.class);
	addTipResourceBundle("*.lpack.ConditionTips",
			     AbstractDoubleCondFactory.class);
    }

    @Override
    protected void addToTimelineRequest(final Obj object, int key,
					final double time)
    {
	super.addToTimelineRequest(object, key, time);
	TimelineEntry tle = timelineMap.get(key);
	if (tle != null && tle.value != null) {
	    final double value = tle.value;
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

//  LocalWords:  subclasses DoubleCondition DoubleConditionFactory px
//  LocalWords:  IFRAME SRC steelblue HREF DoubleConditionParmManager
//  LocalWords:  initialValue superclass minClosed maxClosed
//  LocalWords:  defaultInitialValue initInitialValueParm initFactory
