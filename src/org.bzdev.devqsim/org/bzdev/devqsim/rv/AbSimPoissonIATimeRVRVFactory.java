package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.DoubleRandomVariable;
import org.bzdev.math.rv.FixedDoubleRV;
import org.bzdev.math.rv.PoissonIATimeRV;
import org.bzdev.math.rv.PoissonIATimeRVRV;
import org.bzdev.obnaming.annotations.FactoryParmManager;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;
import org.bzdev.obnaming.annotations.PrimitiveParm;

/**
 * Abstract factory for {@link SimPoissonIATimeRVRV}.
 * <P>
 * The parameters this factory provides are the same as the parameters
 * provided by its subclass {@link SimPoissonIATimeRVRVFactory}:
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/rv/SimPoissonIATimeRVRVFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/SimPoissonIATimeRVRVFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * @see SimPoissonIATimeRVRVFactory
 */
@FactoryParmManager(value="PoissonIATimeRVRVParmManager",
		    tipResourceBundle="*.lpack.PoissonRVRVTips",
		    labelResourceBundle = "*.lpack.PoissonRVRVLabels",
		    stdFactory = "SimPoissonIATimeRVRVFactory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation")
public abstract class AbSimPoissonIATimeRVRVFactory
    <NRV extends SimPoissonIATimeRVRV>
    extends SimIATimeRVRVFactory<PoissonIATimeRV,PoissonIATimeRVRV,NRV>
{

    @PrimitiveParm("meanRV")
    DoubleRandomVariable meanRV = new FixedDoubleRV(0.0);

    PoissonIATimeRVRVParmManager<NRV> pm;
    /**
     * Constructor.
     * @param sim the simulation
     */
    protected AbSimPoissonIATimeRVRVFactory(Simulation sim) {
	super(sim);
	pm = new PoissonIATimeRVRVParmManager<NRV>(this);
	initParms(pm, AbSimPoissonIATimeRVRVFactory.class);
    }

    @Override
    public void initObject(NRV object) {
	super.initObject(object);
	PoissonIATimeRVRV rv = new PoissonIATimeRVRV(meanRV);
	setRV(object, rv);
    }
}

//  LocalWords:  PoissonIATimeRVRV SimPoissonIATimeRVRVFactory IFRAME SRC px HREF
//  LocalWords:  steelblue PoissonIATimeRVRVParmManager NRV SimPoissonIATimeRVRV
//  LocalWords:  sdev
