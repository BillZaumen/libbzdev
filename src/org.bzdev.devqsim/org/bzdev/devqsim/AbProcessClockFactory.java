package org.bzdev.devqsim;

/**
 * Abstract factory for ProcessClock.
 * <P>
 * The parameters this factory provides are the same as the parameters
 * provided by its subclass {@link ProcessClockFactory}:
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/ProcessClockFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/ProcessClockFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * @see ProcessClockFactory
 */
public abstract class AbProcessClockFactory<OBJ extends ProcessClock>
    extends DefaultSimObjectFactory<OBJ>
{
    /**
     * Constructor.
     * @param sim the simulation used to name objects.
     */
    protected AbProcessClockFactory(Simulation sim) {
	super(sim);
    }
}

//  LocalWords:  ProcessClock ProcessClockFactory IFRAME SRC px HREF
//  LocalWords:  steelblue
