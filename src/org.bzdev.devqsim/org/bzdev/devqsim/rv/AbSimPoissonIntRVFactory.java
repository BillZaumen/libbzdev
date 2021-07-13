package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.PoissonIntegerRV;
import org.bzdev.obnaming.annotations.FactoryParmManager;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;
import org.bzdev.obnaming.annotations.PrimitiveParm;

/**
 * Abstract factory for {@link SimPoissonIntegerRV}.
 * <P>
 * The parameters this factory provides are the same as the parameters
 * provided by its subclass {@link SimPoissonIntRVFactory}:
 * <P>
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/rv/SimPoissonIntRVFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/SimPoissonIntRVFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * @see SimPoissonIntRVFactory
 */
@FactoryParmManager(value="PoissonIntegerRVParmManager",
		    tipResourceBundle="*.lpack.PoissonRVTips",
		    labelResourceBundle = "*.lpack.PoissonRVLabels",
		    stdFactory = "SimPoissonIntRVFactory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation")
public abstract class AbSimPoissonIntRVFactory
    <NRV extends SimPoissonIntegerRV>
    extends SimIntegerRVFactory<PoissonIntegerRV,NRV>
{

    @PrimitiveParm("mean")
    double mean = 0.0;

    @PrimitiveParm("tableMode")
    boolean mode = false;

    PoissonIntegerRVParmManager<NRV> pm;
    /**
     * Constructor.
     * @param sim the simulation
     */
    protected AbSimPoissonIntRVFactory(Simulation sim) {
	super(sim);
	pm = new PoissonIntegerRVParmManager<NRV>(this);
	initParms(pm, AbSimPoissonIntRVFactory.class);
    }

    @Override
    public void initObject(NRV object) {
	super.initObject(object);
	PoissonIntegerRV rv = mode? new PoissonIntegerRV(mean, mode):
			   new PoissonIntegerRV(mean);
	setRV(object, rv);
    }
}

//  LocalWords:  PoissonIntegerRV SimPoissonIntRVFactory IFRAME SRC px HREF
//  LocalWords:  steelblue PoissonIntRVParmManager NRV SimPoissonIntegerRV
//  LocalWords:  sdev
