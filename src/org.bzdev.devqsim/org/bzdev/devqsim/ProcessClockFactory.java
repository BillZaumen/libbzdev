package org.bzdev.devqsim;

/**
 * Factory for creating a new instance of ProcessClock.
 * The parameters are defined as follows:
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/ProcessClockFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/ProcessClockFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 */
public final class ProcessClockFactory
    extends AbProcessClockFactory<ProcessClock>
{
    /**
     * Constructor.
     * @param sim the simulation used to name objects.
     */
    public ProcessClockFactory(Simulation sim) {
	super(sim);
    }

    /**
     * Constructor for service provider.
     * This constructor should not be used directly. It is necessary
     * because of the introduction of modules in Java 9, and is
     * used by a service provider that allows factories to be listed,
     * possibly with documentation regarding their parameters. It
     * jst calls the default constructor with a null argument.
     */
    public ProcessClockFactory() {
	this(null);
    }

    @Override
    protected ProcessClock newObject(String name) {
	return new ProcessClock(getSimulation(), name, willIntern());
    }
}

//  LocalWords:  ProcessClock IFRAME SRC px steelblue HREF jst
