package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.DoubleRandomVariable;
import org.bzdev.math.rv.FixedDoubleRV;
import org.bzdev.math.rv.PoissonLongRV;
import org.bzdev.math.rv.PoissonLongRVRV;
import org.bzdev.obnaming.annotations.FactoryParmManager;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;
import org.bzdev.obnaming.annotations.PrimitiveParm;

/**
 * Abstract factory for {@link SimPoissonLongRVRV}.
 * <P>
 * The parameters this factory provides are the same as the parameters
 * provided by its subclass {@link SimPoissonLongRVRVFactory}:
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/rv/SimPoissonLongRVRVFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/SimPoissonLongRVRVFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * @see SimPoissonLongRVRVFactory
 */
@FactoryParmManager(value="PoissonLongRVRVParmManager",
		    tipResourceBundle="*.lpack.PoissonRVRVTips",
		    labelResourceBundle = "*.lpack.PoissonRVRVLabels",
		    stdFactory = "SimPoissonLongRVRVFactory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation")
public abstract class AbSimPoissonLongRVRVFactory
    <NRV extends SimPoissonLongRVRV>
    extends SimLongRVRVFactory<PoissonLongRV,PoissonLongRVRV,NRV>
{

    @PrimitiveParm("meanRV")
    DoubleRandomVariable meanRV = new FixedDoubleRV(0.0);

    @PrimitiveParm("tableMode")
    boolean mode = false;

    PoissonLongRVRVParmManager<NRV> pm;
    /**
     * Constructor.
     * @param sim the simulation
     */
    protected AbSimPoissonLongRVRVFactory(Simulation sim) {
	super(sim);
	pm = new PoissonLongRVRVParmManager<NRV>(this);
	initParms(pm, AbSimPoissonLongRVRVFactory.class);
    }

    @Override
    public void initObject(NRV object) {
	super.initObject(object);
	PoissonLongRVRV rv = mode? new PoissonLongRVRV(meanRV, mode):
			   new PoissonLongRVRV(meanRV);
	setRV(object, rv);
    }
}

//  LocalWords:  PoissonLongRVRV SimPoissonLongRVRVFactory IFRAME SRC px HREF
//  LocalWords:  steelblue PoissonLongRVRVParmManager NRV SimPoissonLongRVRV
//  LocalWords:  sdev
