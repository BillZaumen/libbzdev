package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.BooleanRandomVariable;
import org.bzdev.math.rv.FixedBooleanRV;
import org.bzdev.math.rv.FixedBooleanRVRV;
import org.bzdev.obnaming.annotations.FactoryParmManager;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;
import org.bzdev.obnaming.annotations.PrimitiveParm;

/**
 * Abstract factory for {@link SimFixedBooleanRV}.
 * <P>
 * The parameters this factory provides are the same as the parameters
 * provided by its subclass {@link SimFixedBoolRVFactory}:
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/rv/SimFixedBoolRVFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/SimFixedBoolRVFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * @see SimFixedBoolRVFactory
 */
@FactoryParmManager(value="FixedBooleanRVRVParmManager",
		    tipResourceBundle="*.lpack.FixedRVRVTips",
		    labelResourceBundle = "*.lpack.FixedRVRVLabels",
		    stdFactory = "SimFixedBoolRVRVFactory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation")
public abstract class AbSimFixedBoolRVRVFactory
    <NRVRV extends SimFixedBooleanRVRV>
    extends SimBooleanRVRVFactory<FixedBooleanRV,FixedBooleanRVRV,NRVRV>
{

    @PrimitiveParm("value")
    BooleanRandomVariable value = new FixedBooleanRV(false);

    FixedBooleanRVRVParmManager<NRVRV> pm;
    /**
     * Constructor.
     * @param sim the simulation
     */
    protected AbSimFixedBoolRVRVFactory(Simulation sim) {
	super(sim);
	pm = new FixedBooleanRVRVParmManager<NRVRV>(this);
	initParms(pm, AbSimFixedBoolRVRVFactory.class);
    }

    @Override
    public void initObject(NRVRV object) {
	super.initObject(object);
	FixedBooleanRVRV rv = new FixedBooleanRVRV(value);
	setRV(object, rv);
    }
}

//  LocalWords:  FixedBooleanRV SimFixedBoolRVFactory IFRAME SRC px HREF
//  LocalWords:  steelblue FixedBoolRVParmManager NRV SimFixedBooleanRV
//  LocalWords:  sdev
