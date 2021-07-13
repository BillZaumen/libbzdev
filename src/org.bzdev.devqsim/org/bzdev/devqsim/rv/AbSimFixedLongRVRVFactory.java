package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.LongRandomVariable;
import org.bzdev.math.rv.FixedLongRV;
import org.bzdev.math.rv.FixedLongRVRV;
import org.bzdev.obnaming.annotations.FactoryParmManager;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;
import org.bzdev.obnaming.annotations.PrimitiveParm;

/**
 * Abstract factory for {@link SimFixedLongRV}.
 * <P>
 * The parameters this factory provides are the same as the parameters
 * provided by its subclass {@link SimFixedLongRVFactory}:
 * <P>
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/rv/SimFixedLongRVFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/SimFixedLongRVFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * @see SimFixedLongRVFactory
 */
@FactoryParmManager(value="FixedLongRVRVParmManager",
		    tipResourceBundle="*.lpack.FixedRVRVTips",
		    labelResourceBundle = "*.lpack.FixedRVRVLabels",
		    stdFactory = "SimFixedLongRVRVFactory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation")
public abstract class AbSimFixedLongRVRVFactory
    <NRVRV extends SimFixedLongRVRV>
    extends SimLongRVRVFactory<FixedLongRV,FixedLongRVRV,NRVRV>
{

    @PrimitiveParm("value")
    LongRandomVariable value = new FixedLongRV(0);

    FixedLongRVRVParmManager<NRVRV> pm;
    /**
     * Constructor.
     * @param sim the simulation
     */
    protected AbSimFixedLongRVRVFactory(Simulation sim) {
	super(sim);
	pm = new FixedLongRVRVParmManager<NRVRV>(this);
	initParms(pm, AbSimFixedLongRVRVFactory.class);
    }

    @Override
    public void initObject(NRVRV object) {
	super.initObject(object);
	FixedLongRVRV rv = new FixedLongRVRV(value);
	setRV(object, rv);
    }
}

//  LocalWords:  FixedLongRV SimFixedLongRVFactory IFRAME SRC px HREF
//  LocalWords:  steelblue FixedLongRVParmManager NRV SimFixedLongRV
//  LocalWords:  sdev
