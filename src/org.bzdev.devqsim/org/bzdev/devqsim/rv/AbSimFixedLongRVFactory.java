package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.FixedLongRV;
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
@FactoryParmManager(value="FixedLongRVParmManager",
		    tipResourceBundle="*.lpack.FixedRVTips",
		    labelResourceBundle = "*.lpack.FixedRVLabels",
		    stdFactory = "SimFixedLongRVFactory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation")
public abstract class AbSimFixedLongRVFactory
    <NRV extends SimFixedLongRV>
    extends SimLongRVFactory<FixedLongRV,NRV>
{

    @PrimitiveParm("value")
    long value = 0;


    FixedLongRVParmManager<NRV> pm;
    /**
     * Constructor.
     * @param sim the simulation
     */
    protected AbSimFixedLongRVFactory(Simulation sim) {
	super(sim);
	pm = new FixedLongRVParmManager<NRV>(this);
	initParms(pm, AbSimFixedLongRVFactory.class);
    }

    @Override
    public void initObject(NRV object) {
	super.initObject(object);
	FixedLongRV rv = new FixedLongRV(value);
	setRV(object, rv);
    }
}

//  LocalWords:  FixedLongRV SimFixedLongRVFactory IFRAME SRC px HREF
//  LocalWords:  steelblue FixedLongRVParmManager NRV SimFixedLongRV
//  LocalWords:  sdev
