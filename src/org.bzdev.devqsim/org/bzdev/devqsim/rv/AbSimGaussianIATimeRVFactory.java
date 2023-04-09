package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.GaussianIATimeRV;
import org.bzdev.obnaming.annotations.FactoryParmManager;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;
import org.bzdev.obnaming.annotations.PrimitiveParm;

/**
 * Abstract factory for {@link SimGaussianIATimeRV}.
 * <P>
 * The parameters this factory provides are the same as the parameters
 * provided by its subclass {@link SimGaussianIATimeRVFactory}:
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/rv/SimGaussianIATimeRVFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/SimGaussianIATimeRVFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * @see SimGaussianIATimeRVFactory
 */
@FactoryParmManager(value="GaussianIATimeRVParmManager",
		    tipResourceBundle="*.lpack.GaussianRVTips",
		    labelResourceBundle = "*.lpack.GaussianRVLabels",
		    stdFactory = "SimGaussianIATimeRVFactory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation")
public abstract class AbSimGaussianIATimeRVFactory
    <NRV extends SimGaussianIATimeRV>
    /*<RV extends GaussianIATimeRV, NRV extends SimGaussianIATimeRV<RV>>*/
    extends SimIATimeRVFactory<GaussianIATimeRV,NRV>/*<RV,NRV>*/
{

    @PrimitiveParm("mean")
    double mean = 0.0;

    @PrimitiveParm(value="sdev", lowerBound = "0.0", lowerBoundClosed=true)
    double sdev = 1.0;

    GaussianIATimeRVParmManager<NRV> pm;
    /**
     * Constructor.
     * @param sim the simulation
     */
    protected AbSimGaussianIATimeRVFactory(Simulation sim) {
	super(sim);
	pm = new GaussianIATimeRVParmManager<NRV>(this);
	initParms(pm, AbSimGaussianIATimeRVFactory.class);
    }

    @Override
    public void initObject(NRV object) {
	super.initObject(object);
	GaussianIATimeRV rv = new GaussianIATimeRV(mean, sdev);
	setRV(object, rv);
    }
}
    

//  LocalWords:  GaussianIATimeRV SimGaussianIATimeRVFactory IFRAME SRC px HREF
//  LocalWords:  steelblue GaussianIATimeRVParmManager NRV SimGaussianIATimeRV
//  LocalWords:  sdev
