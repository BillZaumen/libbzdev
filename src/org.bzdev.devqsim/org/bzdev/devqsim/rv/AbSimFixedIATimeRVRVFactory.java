package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.InterarrivalTimeRV;
import org.bzdev.math.rv.FixedIATimeRV;
import org.bzdev.math.rv.FixedIATimeRVRV;
import org.bzdev.math.rv.LongRandomVariable;
import org.bzdev.math.rv.FixedLongRV;
import org.bzdev.obnaming.annotations.FactoryParmManager;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;
import org.bzdev.obnaming.annotations.PrimitiveParm;

/**
 * Abstract factory for {@link SimFixedIATimeRV}.
 * <P>
 * The parameters this factory provides are the same as the parameters
 * provided by its subclass {@link SimFixedIATimeRVFactory}:
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/rv/SimFixedIATimeRVFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/SimFixedIATimeRVFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * @see SimFixedIATimeRVFactory
 */
@FactoryParmManager(value="FixedIATimeRVRVParmManager",
		    tipResourceBundle="*.lpack.FixedRVRVTips",
		    labelResourceBundle = "*.lpack.FixedRVRVLabels",
		    stdFactory = "SimFixedIATimeRVRVFactory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation")
public abstract class AbSimFixedIATimeRVRVFactory
    <NRVRV extends SimFixedIATimeRVRV>
    extends SimIATimeRVRVFactory<FixedIATimeRV,FixedIATimeRVRV,NRVRV>
{

    @PrimitiveParm("value")
    LongRandomVariable value = new FixedLongRV(0);

    FixedIATimeRVRVParmManager<NRVRV> pm;
    /**
     * Constructor.
     * @param sim the simulation
     */
    protected AbSimFixedIATimeRVRVFactory(Simulation sim) {
	super(sim);
	pm = new FixedIATimeRVRVParmManager<NRVRV>(this);
	initParms(pm, AbSimFixedIntRVRVFactory.class);
    }

    @Override
    public void initObject(NRVRV object) {
	super.initObject(object);
	FixedIATimeRVRV rv = new FixedIATimeRVRV(value);
	setRV(object, rv);
    }
}

//  LocalWords:  FixedIATimeRV SimFixedIntRVFactory IFRAME SRC px HREF
//  LocalWords:  steelblue FixedIntRVParmManager NRV SimFixedIATimeRV
//  LocalWords:  sdev
