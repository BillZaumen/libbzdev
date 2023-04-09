package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.LogNormalRV;
import org.bzdev.obnaming.annotations.FactoryParmManager;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;
import org.bzdev.obnaming.annotations.PrimitiveParm;

/**
 * Abstract factory for {@link SimLogNormalRV}.
 * <P>
 * The parameters this factory provides are the same as the parameters
 * provided by its subclass {@link SimLogNormalRVFactory}:
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/rv/SimLogNormalRVFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/SimLogNormalRVFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * @see SimLogNormalRVFactory
 */
@FactoryParmManager(value="LogNormalRVParmManager",
		    tipResourceBundle="*.lpack.LogNormalRVTips",
		    labelResourceBundle = "*.lpack.LogNormalRVLabels",
		    stdFactory = "SimLogNormalRVFactory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation")
public abstract class AbSimLogNormalRVFactory
    <NRV extends SimLogNormalRV>
    /*<RV extends LogNormalRV, NRV extends SimLogNormalRV<RV>>*/
    extends SimDoubleRVFactory<LogNormalRV,NRV>/*<RV,NRV>*/
{

    @PrimitiveParm("mu")
    double mu = 0.0;

    @PrimitiveParm(value="sigma", lowerBound = "0.0", lowerBoundClosed=true)
    double sigma = 1.0;

    LogNormalRVParmManager<NRV> pm;
    /**
     * Constructor.
     * @param sim the simulation
     */
    protected AbSimLogNormalRVFactory(Simulation sim) {
	super(sim);
	pm = new LogNormalRVParmManager<NRV>(this);
	initParms(pm, AbSimLogNormalRVFactory.class);
    }

    @Override
    public void initObject(NRV object) {
	super.initObject(object);
	LogNormalRV rv = new LogNormalRV(mu, sigma);
	setRV(object, rv);
    }
}
    

//  LocalWords:  LogNormalRV SimLogNormalRVFactory IFRAME SRC px HREF
//  LocalWords:  steelblue LogNormalRVParmManager NRV SimLogNormalRV
//  LocalWords:  sdev
