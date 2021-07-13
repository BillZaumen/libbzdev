package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.DoubleRandomVariable;
import org.bzdev.math.rv.FixedDoubleRV;
import org.bzdev.math.rv.PoissonDoubleRV;
import org.bzdev.math.rv.PoissonDoubleRVRV;
import org.bzdev.obnaming.annotations.FactoryParmManager;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;
import org.bzdev.obnaming.annotations.PrimitiveParm;

/**
 * Abstract factory for {@link SimPoissonDoubleRVRV}.
 * <P>
 * The parameters this factory provides are the same as the parameters
 * provided by its subclass {@link SimPoissonDblRVRVFactory}:
 * <P>
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/rv/SimPoissonDblRVRVFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/SimPoissonDblRVRVFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * @see SimPoissonDblRVRVFactory
 */
@FactoryParmManager(value="PoissonDoubleRVRVParmManager",
		    tipResourceBundle="*.lpack.PoissonRVRVTips",
		    labelResourceBundle = "*.lpack.PoissonRVRVLabels",
		    stdFactory = "SimPoissonDblRVRVFactory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation")
public abstract class AbSimPoissonDblRVRVFactory
    <NRV extends SimPoissonDoubleRVRV>
    extends SimDoubleRVRVFactory<PoissonDoubleRV,PoissonDoubleRVRV,NRV>
{

    @PrimitiveParm("meanRV")
    DoubleRandomVariable meanRV = new FixedDoubleRV(0.0);

    @PrimitiveParm("tableMode")
    boolean mode = false;

    PoissonDoubleRVRVParmManager<NRV> pm;
    /**
     * Constructor.
     * @param sim the simulation
     */
    protected AbSimPoissonDblRVRVFactory(Simulation sim) {
	super(sim);
	pm = new PoissonDoubleRVRVParmManager<NRV>(this);
	initParms(pm, AbSimPoissonDblRVRVFactory.class);
    }

    @Override
    public void initObject(NRV object) {
	super.initObject(object);
	PoissonDoubleRVRV rv = mode? new PoissonDoubleRVRV(meanRV, mode):
			   new PoissonDoubleRVRV(meanRV);
	setRV(object, rv);
    }
}

//  LocalWords:  PoissonDoubleRVRV SimPoissonDblRVRVFactory IFRAME SRC px HREF
//  LocalWords:  steelblue PoissonDblRVRVParmManager NRV SimPoissonDoubleRVRV
//  LocalWords:  sdev
