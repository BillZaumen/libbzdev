package org.bzdev.devqsim;

/**
 * Class for TraceSet factories.
 * <P>
 * The parameters are defined as follows:
 * <P>
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/TraceSetFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/TraceSetFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * <P>
 */
public class TraceSetFactory extends AbstractTraceSetFactory<TraceSet>
{
    /**
     * Constructor.
     * @param sim the simulation used to name objects.
     */
    public TraceSetFactory(Simulation sim) {
	super(sim);
    }

    /**
     * Constructor for service provider.
     * This constructor should not be used directly. It is necessary
     * because of the introduction of modules in Java 9, and is
     * used by a service provider that allows factories to be listed,
     * possibly with documentation regarding their parameters. It
     * just calls the default constructor with a null argument.
     */
    public TraceSetFactory() {
	this(null);
    }

    protected TraceSet newObject(String name) {
	return new TraceSet(getSimulation(), name, willIntern());
    }
}

//  LocalWords:  TraceSet IFRAME SRC px steelblue HREF
