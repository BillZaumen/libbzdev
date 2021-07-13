package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.PoissonIATimeRV;
import org.bzdev.obnaming.annotations.FactoryParmManager;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;
import org.bzdev.obnaming.annotations.PrimitiveParm;

/**
 * Abstract factory for {@link SimPoissonIATimeRV}.
 * <P>
 * The parameters this factory provides are the same as the parameters
 * provided by its subclass {@link SimPoissonIATimeRVFactory}:
 * <P>
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/rv/SimPoissonIATimeRVFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/SimPoissonIATimeRVFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * @see SimPoissonIATimeRVFactory
 */
@FactoryParmManager(value="PoissonIATimeRVParmManager",
		    tipResourceBundle="*.lpack.PoissonRVTips",
		    labelResourceBundle = "*.lpack.PoissonRVLabels",
		    stdFactory = "SimPoissonIATimeRVFactory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation")
public abstract class AbSimPoissonIATimeRVFactory
    <NRV extends SimPoissonIATimeRV>
    extends SimIATimeRVFactory<PoissonIATimeRV,NRV>
{

    @PrimitiveParm("mean")
    double mean = 0.0;

    PoissonIATimeRVParmManager<NRV> pm;
    /**
     * Constructor.
     * @param sim the simulation
     */
    protected AbSimPoissonIATimeRVFactory(Simulation sim) {
	super(sim);
	pm = new PoissonIATimeRVParmManager<NRV>(this);
	initParms(pm, AbSimPoissonIATimeRVFactory.class);
    }

    @Override
    public void initObject(NRV object) {
	super.initObject(object);
	PoissonIATimeRV rv = new PoissonIATimeRV(mean);
	setRV(object, rv);
    }
}

//  LocalWords:  PoissonIATimeRV SimPoissonIATimeRVFactory IFRAME SRC px HREF
//  LocalWords:  steelblue PoissonIATimeRVParmManager NRV SimPoissonIATimeRV
//  LocalWords:  sdev
