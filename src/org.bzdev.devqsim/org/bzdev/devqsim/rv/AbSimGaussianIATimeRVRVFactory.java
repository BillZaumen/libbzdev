package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.GaussianIATimeRV;
import org.bzdev.math.rv.GaussianIATimeRVRV;
import org.bzdev.math.rv.DoubleRandomVariable;
import org.bzdev.math.rv.FixedDoubleRV;
import org.bzdev.obnaming.annotations.FactoryParmManager;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;
import org.bzdev.obnaming.annotations.PrimitiveParm;

/**
 * Abstract factory for {@link SimGaussianIATimeRVRV}.
 * <P>
 * The parameters this factory provides are the same as the parameters
 * provided by its subclass {@link SimGaussianIATimeRVRVFactory}:
 * <P>
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/rv/SimGaussianIATimeRVRVFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/SimGaussianIATimeRVRVFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * @see SimGaussianIATimeRVRVFactory
 */
@FactoryParmManager(value="GaussianIATimeRVRVParmManager",
		    tipResourceBundle="*.lpack.GaussianRVRVTips",
		    labelResourceBundle = "*.lpack.GaussianRVRVLabels",
		    stdFactory = "SimGaussianIATimeRVRVFactory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation")
public abstract class AbSimGaussianIATimeRVRVFactory<NRVRV extends SimGaussianIATimeRVRV>
    extends SimIATimeRVRVFactory<GaussianIATimeRV, GaussianIATimeRVRV, NRVRV>
{
    
    @PrimitiveParm("meanRV")
    DoubleRandomVariable meanRV = new FixedDoubleRV(0.0);

    @PrimitiveParm(value="sdevRV", lowerBound="0.0", lowerBoundClosed=true)
    DoubleRandomVariable sdevRV = new FixedDoubleRV(1.0);

    GaussianIATimeRVRVParmManager<NRVRV> pm = null;

    /**
     * Constructor.
     * @param sim the simulation used to name objects.
     */
    protected AbSimGaussianIATimeRVRVFactory(Simulation sim) {
	super(sim);
	pm = new GaussianIATimeRVRVParmManager<NRVRV>(this);
	initParms(pm, AbSimGaussianIATimeRVRVFactory.class);
    }

    @Override
    public void initObject(NRVRV object) {
	super.initObject(object);
	GaussianIATimeRVRV rv = new GaussianIATimeRVRV(meanRV, sdevRV);
	setRV(object, rv);

    }
}

//  LocalWords:  GaussianIATimeRVRV SimGaussianIATimeRVRVFactory IFRAME SRC px
//  LocalWords:  steelblue HREF GaussianIATimeRVRVParmManager meanRV sdevRV
