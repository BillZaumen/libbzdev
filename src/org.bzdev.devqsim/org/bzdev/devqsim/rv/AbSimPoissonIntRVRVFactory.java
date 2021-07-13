package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.DoubleRandomVariable;
import org.bzdev.math.rv.FixedDoubleRV;
import org.bzdev.math.rv.PoissonIntegerRV;
import org.bzdev.math.rv.PoissonIntegerRVRV;
import org.bzdev.obnaming.annotations.FactoryParmManager;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;
import org.bzdev.obnaming.annotations.PrimitiveParm;

/**
 * Abstract factory for {@link SimPoissonIntegerRVRV}.
 * <P>
 * The parameters this factory provides are the same as the parameters
 * provided by its subclass {@link SimPoissonIntRVRVFactory}:
 * <P>
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/rv/SimPoissonIntRVRVFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/SimPoissonIntRVRVFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * @see SimPoissonIntRVRVFactory
 */
@FactoryParmManager(value="PoissonIntegerRVRVParmManager",
		    tipResourceBundle="*.lpack.PoissonRVRVTips",
		    labelResourceBundle = "*.lpack.PoissonRVRVLabels",
		    stdFactory = "SimPoissonIntRVRVFactory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation")
public abstract class AbSimPoissonIntRVRVFactory
    <NRV extends SimPoissonIntegerRVRV>
    extends SimIntegerRVRVFactory<PoissonIntegerRV,PoissonIntegerRVRV,NRV>
{

    @PrimitiveParm("meanRV")
    DoubleRandomVariable meanRV = new FixedDoubleRV(0.0);

    @PrimitiveParm("tableMode")
    boolean mode = false;

    PoissonIntegerRVRVParmManager<NRV> pm;
    /**
     * Constructor.
     * @param sim the simulation
     */
    protected AbSimPoissonIntRVRVFactory(Simulation sim) {
	super(sim);
	pm = new PoissonIntegerRVRVParmManager<NRV>(this);
	initParms(pm, AbSimPoissonIntRVRVFactory.class);
    }

    @Override
    public void initObject(NRV object) {
	super.initObject(object);
	PoissonIntegerRVRV rv = mode? new PoissonIntegerRVRV(meanRV, mode):
			   new PoissonIntegerRVRV(meanRV);
	setRV(object, rv);
    }
}

//  LocalWords:  PoissonIntegerRVRV SimPoissonIntRVRVFactory IFRAME SRC px HREF
//  LocalWords:  steelblue PoissonIntRVRVParmManager NRV SimPoissonIntegerRVRV
//  LocalWords:  sdev
