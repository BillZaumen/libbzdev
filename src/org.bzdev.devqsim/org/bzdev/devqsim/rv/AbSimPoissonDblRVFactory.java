package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.PoissonDoubleRV;
import org.bzdev.obnaming.annotations.FactoryParmManager;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;
import org.bzdev.obnaming.annotations.PrimitiveParm;

/**
 * Abstract factory for {@link SimPoissonDoubleRV}.
 * <P>
 * The parameters this factory provides are the same as the parameters
 * provided by its subclass {@link SimPoissonDblRVFactory}:
 * <P>
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/rv/SimPoissonDblRVFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/SimPoissonDblRVFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * @see SimPoissonDblRVFactory
 */
@FactoryParmManager(value="PoissonDoubleRVParmManager",
		    tipResourceBundle="*.lpack.PoissonRVTips",
		    labelResourceBundle = "*.lpack.PoissonRVLabels",
		    stdFactory = "SimPoissonDblRVFactory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation")
public abstract class AbSimPoissonDblRVFactory
    <NRV extends SimPoissonDoubleRV>
    extends SimDoubleRVFactory<PoissonDoubleRV,NRV>
{

    @PrimitiveParm("mean")
    double mean = 0.0;

    @PrimitiveParm("tableMode")
    boolean mode = false;

    PoissonDoubleRVParmManager<NRV> pm;
    /**
     * Constructor.
     * @param sim the simulation
     */
    protected AbSimPoissonDblRVFactory(Simulation sim) {
	super(sim);
	pm = new PoissonDoubleRVParmManager<NRV>(this);
	initParms(pm, AbSimPoissonDblRVFactory.class);
    }

    @Override
    public void initObject(NRV object) {
	super.initObject(object);
	PoissonDoubleRV rv = mode? new PoissonDoubleRV(mean, mode):
			   new PoissonDoubleRV(mean);
	setRV(object, rv);
    }
}

//  LocalWords:  PoissonDoubleRV SimPoissonDblRVFactory IFRAME SRC px HREF
//  LocalWords:  steelblue PoissonDblRVParmManager NRV SimPoissonDoubleRV
//  LocalWords:  sdev
