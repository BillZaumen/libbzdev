package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.GaussianRV;
import org.bzdev.math.rv.GaussianRVRV;
import org.bzdev.math.rv.DoubleRandomVariable;
import org.bzdev.math.rv.FixedDoubleRV;
import org.bzdev.obnaming.annotations.FactoryParmManager;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;
import org.bzdev.obnaming.annotations.PrimitiveParm;

/**
 * Abstract factory for {@link SimGaussianRVRV}.
 * <P>
 * The parameters this factory provides are the same as the parameters
 * provided by its subclass {@link SimGaussianRVRVFactory}:
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/rv/SimGaussianRVRVFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/SimGaussianRVRVFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * @see SimGaussianRVRVFactory
 */
@FactoryParmManager(value="GaussianRVRVParmManager",
		    tipResourceBundle="*.lpack.GaussianRVRVTips",
		    labelResourceBundle = "*.lpack.GaussianRVRVLabels",
		    stdFactory = "SimGaussianRVRVFactory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation")
public abstract class AbSimGaussianRVRVFactory<NRVRV extends SimGaussianRVRV>
    extends SimDoubleRVRVFactory<GaussianRV, GaussianRVRV, NRVRV>
{
    
    @PrimitiveParm("meanRV")
    DoubleRandomVariable meanRV = new FixedDoubleRV(0.0);

    @PrimitiveParm(value="sdevRV", lowerBound="0.0", lowerBoundClosed=true)
    DoubleRandomVariable sdevRV = new FixedDoubleRV(1.0);

    GaussianRVRVParmManager<NRVRV> pm = null;

    /**
     * Constructor.
     * @param sim the simulation used to name objects.
     */
    protected AbSimGaussianRVRVFactory(Simulation sim) {
	super(sim);
	pm = new GaussianRVRVParmManager<NRVRV>(this);
	initParms(pm, AbSimGaussianRVRVFactory.class);
    }

    @Override
    public void initObject(NRVRV object) {
	super.initObject(object);
	GaussianRVRV rv = new GaussianRVRV(meanRV, sdevRV);
	setRV(object, rv);

    }
}

//  LocalWords:  GaussianRVRV SimGaussianRVRVFactory IFRAME SRC px
//  LocalWords:  steelblue HREF GaussianRVRVParmManager meanRV sdevRV
