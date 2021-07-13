package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.FixedBooleanRV;
import org.bzdev.obnaming.annotations.FactoryParmManager;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;
import org.bzdev.obnaming.annotations.PrimitiveParm;

/**
 * Abstract factory for {@link SimFixedBooleanRV}.
 * <P>
 * The parameters this factory provides are the same as the parameters
 * provided by its subclass {@link SimFixedBoolRVFactory}:
 * <P>
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/rv/SimFixedBoolRVFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/SimFixedBoolRVFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * @see SimFixedBoolRVFactory
 */
@FactoryParmManager(value="FixedBooleanRVParmManager",
		    tipResourceBundle="*.lpack.FixedRVTips",
		    labelResourceBundle = "*.lpack.FixedRVLabels",
		    stdFactory = "SimFixedBoolRVFactory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation")
public abstract class AbSimFixedBoolRVFactory
    <NRV extends SimFixedBooleanRV>
    extends SimBooleanRVFactory<FixedBooleanRV,NRV>
{

    @PrimitiveParm("value")
    boolean value = false;


    FixedBooleanRVParmManager<NRV> pm;
    /**
     * Constructor.
     * @param sim the simulation
     */
    protected AbSimFixedBoolRVFactory(Simulation sim) {
	super(sim);
	pm = new FixedBooleanRVParmManager<NRV>(this);
	initParms(pm, AbSimFixedBoolRVFactory.class);
    }

    @Override
    public void initObject(NRV object) {
	super.initObject(object);
	FixedBooleanRV rv = new FixedBooleanRV(value);
	setRV(object, rv);
    }
}

//  LocalWords:  FixedBooleanRV SimFixedBoolRVFactory IFRAME SRC px HREF
//  LocalWords:  steelblue FixedBoolRVParmManager NRV SimFixedBooleanRV
//  LocalWords:  sdev
