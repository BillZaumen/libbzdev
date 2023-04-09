package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.LogNormalRV;
import org.bzdev.math.rv.LogNormalRVRV;
import org.bzdev.math.rv.DoubleRandomVariable;
import org.bzdev.math.rv.FixedDoubleRV;
import org.bzdev.obnaming.annotations.FactoryParmManager;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;
import org.bzdev.obnaming.annotations.PrimitiveParm;

/**
 * Abstract factory for {@link SimLogNormalRVRV}.
 * <P>
 * The parameters this factory provides are the same as the parameters
 * provided by its subclass {@link SimLogNormalRVRVFactory}:
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/rv/SimLogNormalRVRVFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/SimLogNormalRVRVFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * @see SimLogNormalRVRVFactory
 */
@FactoryParmManager(value="LogNormalRVRVParmManager",
		    tipResourceBundle="*.lpack.LogNormalRVRVTips",
		    labelResourceBundle = "*.lpack.LogNormalRVRVLabels",
		    stdFactory = "SimLogNormalRVRVFactory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation")
public abstract class AbSimLogNormalRVRVFactory<NRVRV extends SimLogNormalRVRV>
    extends SimDoubleRVRVFactory<LogNormalRV, LogNormalRVRV, NRVRV>
{

    @PrimitiveParm("muRV")
    DoubleRandomVariable muRV = new FixedDoubleRV(0.0);

    @PrimitiveParm(value="sigmaRV", lowerBound = "0.0", lowerBoundClosed=true)
    DoubleRandomVariable sigmaRV = new FixedDoubleRV(1.0);

    LogNormalRVRVParmManager<NRVRV> pm = null;

    /**
     * Constructor.
     * @param sim the simulation used to name objects.
     */
    protected AbSimLogNormalRVRVFactory(Simulation sim) {
	super(sim);
	pm = new LogNormalRVRVParmManager<NRVRV>(this);
	initParms(pm, AbSimLogNormalRVRVFactory.class);
    }

    @Override
    public void initObject(NRVRV object) {
	super.initObject(object);
	LogNormalRVRV rv = new LogNormalRVRV(muRV, sigmaRV);
	setRV(object, rv);

    }
}

//  LocalWords:  LogNormalRVRV SimLogNormalRVRVFactory IFRAME SRC px
//  LocalWords:  steelblue HREF LogNormalRVRVParmManager meanRV sdevRV
