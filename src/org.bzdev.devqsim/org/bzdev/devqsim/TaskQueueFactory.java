package org.bzdev.devqsim;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;

//@exbundle org.bzdev.obnaming.lpack.ParmParser

/**
 * Abstract factory for task queues.
 * This class is the base class for factories that create subclasses
 * of {@link org.bzdev.devqsim.TaskQueue  TaskQueue}.
 * <P>
 * The parameters this factory provides are the same as the parameters
 * provided by its subclass {@link LifoTaskQueueFactory}:
 * <P>
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/LifoTaskQueueFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/LifoTaskQueueFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * <P>
 * @see org.bzdev.devqsim.TaskQueue
 * @see org.bzdev.devqsim.TaskQueue.ReleasePolicy
 * @see org.bzdev.devqsim.QueueDeletePolicy
 */
public abstract class TaskQueueFactory<T, OBJ extends TaskQueue<T>>
    extends DefaultSimObjectFactory<OBJ>
{
    private TaskQueue.ReleasePolicy releasePolicy =
	TaskQueue.ReleasePolicy.CANCELS_IGNORED;

    private QueueDeletePolicy deletePolicy = QueueDeletePolicy.WHEN_EMPTY;

    private boolean canRelease = false;

    Parm[] parms = {
	new Parm("canRelease", null,
		 new ParmParser() {
		     public void parse(boolean value) {
			 canRelease = value;
		     }
		     public void clear() {
			 canRelease = false;
		     }
		 },
		 boolean.class,
		 null, true, null, true),
	new Parm("releasePolicy", null,
		 new ParmParser() {
		     public void parse(java.lang.Enum<?> value) {
			 if (value instanceof TaskQueue.ReleasePolicy) {
			     releasePolicy = (TaskQueue.ReleasePolicy)value;
			 } else {
			     throw new IllegalArgumentException
				 (errorMsg("wrongType1", getParmName()));
				 /*("wrong type");*/
			 }
		     }
		     public void clear() {
			 releasePolicy =
			     TaskQueue.ReleasePolicy.CANCELS_IGNORED;
		     }
		 },
		 TaskQueue.ReleasePolicy.class,
		 null, true, null,true),
	new Parm("deletePolicy", null,
		 new ParmParser() {
		     public void parse(java.lang.Enum<?> value) {
			 if (value instanceof QueueDeletePolicy) {
			     deletePolicy = (QueueDeletePolicy)value;
			 } else {
			     throw new IllegalArgumentException
				 (errorMsg("wrongType1", getParmName()));
				 /*("wrong type");*/
			 }
		     }
		     public void clear() {
			 deletePolicy = QueueDeletePolicy.WHEN_EMPTY;
		     }
		 },
		 QueueDeletePolicy.class,
		 null, true, null,true),
		 
    };

    public void clear() {
	super.clear();
	canRelease = false;
	releasePolicy = TaskQueue.ReleasePolicy.CANCELS_IGNORED;
	deletePolicy = QueueDeletePolicy.WHEN_EMPTY;
    }

    /**
     * Constructor.
     * @param sim the simulation used to name objects.
     */
    protected TaskQueueFactory(Simulation sim) {
	super(sim);
	initParms(parms, TaskQueueFactory.class);
 	addLabelResourceBundle("*.lpack.TaskQueueLabels",
			       SimFunctionFactory.class);
	addTipResourceBundle("*.lpack.TaskQueueTips",
			     SimFunctionFactory.class);
	addDocResourceBundle("*.lpack.TaskQueueDocs",
			     SimFunctionFactory.class);
    }

    @Override
    protected void initObject(OBJ object) {
	super.initObject(object);
	object.setCanRelease(canRelease);
	if (canRelease) {
	    object.setReleasePolicy(releasePolicy);
	}
	object.setDeletePolicy(deletePolicy);
   }
}

//  LocalWords:  exbundle subclasses TaskQueue LifoTaskQueueFactory
//  LocalWords:  IFRAME SRC px steelblue HREF canRelease wrongType
//  LocalWords:  releasePolicy deletePolicy
