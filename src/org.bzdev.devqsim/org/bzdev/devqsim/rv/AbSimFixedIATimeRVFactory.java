package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.FixedIATimeRV;
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
@FactoryParmManager(value="FixedIATimeRVParmManager",
		    tipResourceBundle="*.lpack.FixedRVTips",
		    labelResourceBundle = "*.lpack.FixedRVLabels",
		    stdFactory = "SimFixedIATimeRVFactory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation")
public abstract class AbSimFixedIATimeRVFactory
    <NRV extends SimFixedIATimeRV>
    extends SimIATimeRVFactory<FixedIATimeRV,NRV>
{

    @PrimitiveParm(value = "value", lowerBound = "0", lowerBoundClosed = true)
    long value = 0;


    FixedIATimeRVParmManager<NRV> pm;
    /**
     * Constructor.
     * @param sim the simulation
     */
    protected AbSimFixedIATimeRVFactory(Simulation sim) {
	super(sim);
	pm = new FixedIATimeRVParmManager<NRV>(this);
	initParms(pm, AbSimFixedIATimeRVFactory.class);
    }

    @Override
    public void initObject(NRV object) {
	super.initObject(object);
	FixedIATimeRV rv = new FixedIATimeRV(value);
	setRV(object, rv);
    }
}

//  LocalWords:  FixedIATimeRV SimFixedIATimeRVFactory IFRAME SRC
//  LocalWords:  px HREF steelblue FixedIATimeRVParmManager NRV
//  LocalWords:  SimFixedIATimeRV sdev
