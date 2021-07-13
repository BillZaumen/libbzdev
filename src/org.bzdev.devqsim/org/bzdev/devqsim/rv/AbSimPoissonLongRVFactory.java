package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.PoissonLongRV;
import org.bzdev.obnaming.annotations.FactoryParmManager;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;
import org.bzdev.obnaming.annotations.PrimitiveParm;

/**
 * Abstract factory for {@link SimPoissonLongRV}.
 * <P>
 * The parameters this factory provides are the same as the parameters
 * provided by its subclass {@link SimPoissonLongRVFactory}:
 * <P>
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/rv/SimPoissonLongRVFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/SimPoissonLongRVFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * @see SimPoissonLongRVFactory
 */
@FactoryParmManager(value="PoissonLongRVParmManager",
		    tipResourceBundle="*.lpack.PoissonRVTips",
		    labelResourceBundle = "*.lpack.PoissonRVLabels",
		    stdFactory = "SimPoissonLongRVFactory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation")
public abstract class AbSimPoissonLongRVFactory
    <NRV extends SimPoissonLongRV>
    extends SimLongRVFactory<PoissonLongRV,NRV>
{

    @PrimitiveParm("mean")
    double mean = 0.0;

    @PrimitiveParm("tableMode")
    boolean mode = false;

    PoissonLongRVParmManager<NRV> pm;
    /**
     * Constructor.
     * @param sim the simulation
     */
    protected AbSimPoissonLongRVFactory(Simulation sim) {
	super(sim);
	pm = new PoissonLongRVParmManager<NRV>(this);
	initParms(pm, AbSimPoissonLongRVFactory.class);
    }

    @Override
    public void initObject(NRV object) {
	super.initObject(object);
	PoissonLongRV rv = mode? new PoissonLongRV(mean, mode):
			   new PoissonLongRV(mean);
	setRV(object, rv);
    }
}

//  LocalWords:  PoissonLongRV SimPoissonLongRVFactory IFRAME SRC px HREF
//  LocalWords:  steelblue PoissonLongRVParmManager NRV SimPoissonLongRV
//  LocalWords:  sdev
