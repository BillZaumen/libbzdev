package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.GaussianRV;
import org.bzdev.obnaming.annotations.FactoryParmManager;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;
import org.bzdev.obnaming.annotations.PrimitiveParm;

/**
 * Abstract factory for {@link SimGaussianRV}.
 * <P>
 * The parameters this factory provides are the same as the parameters
 * provided by its subclass {@link SimGaussianRVFactory}:
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/rv/SimGaussianRVFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/SimGaussianRVFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * @see SimGaussianRVFactory
 */
@FactoryParmManager(value="GaussianRVParmManager",
		    tipResourceBundle="*.lpack.GaussianRVTips",
		    labelResourceBundle = "*.lpack.GaussianRVLabels",
		    stdFactory = "SimGaussianRVFactory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation")
public abstract class AbSimGaussianRVFactory
    <NRV extends SimGaussianRV>
    /*<RV extends GaussianRV, NRV extends SimGaussianRV<RV>>*/
    extends SimDoubleRVFactory<GaussianRV,NRV>/*<RV,NRV>*/
{

    @PrimitiveParm("mean")
    double mean = 0.0;

    @PrimitiveParm(value="sdev", lowerBound = "0.0", lowerBoundClosed=true)
    double sdev = 1.0;

    GaussianRVParmManager<NRV> pm;
    /**
     * Constructor.
     * @param sim the simulation
     */
    protected AbSimGaussianRVFactory(Simulation sim) {
	super(sim);
	pm = new GaussianRVParmManager<NRV>(this);
	initParms(pm, AbSimGaussianRVFactory.class);
    }

    @Override
    public void initObject(NRV object) {
	super.initObject(object);
	GaussianRV rv = new GaussianRV(mean, sdev);
	setRV(object, rv);
    }
}
    

//  LocalWords:  GaussianRV SimGaussianRVFactory IFRAME SRC px HREF
//  LocalWords:  steelblue GaussianRVParmManager NRV SimGaussianRV
//  LocalWords:  sdev
