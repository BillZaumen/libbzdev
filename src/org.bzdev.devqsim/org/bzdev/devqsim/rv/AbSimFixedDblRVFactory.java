package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.FixedDoubleRV;
import org.bzdev.obnaming.annotations.FactoryParmManager;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;
import org.bzdev.obnaming.annotations.PrimitiveParm;

/**
 * Abstract factory for {@link SimFixedDoubleRV}.
 * <P>
 * The parameters this factory provides are the same as the parameters
 * provided by its subclass {@link SimFixedDblRVFactory}:
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/rv/SimFixedDblRVFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/SimFixedDblRVFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * @see SimFixedDblRVFactory
 */
@FactoryParmManager(value="FixedDoubleRVParmManager",
		    tipResourceBundle="*.lpack.FixedRVTips",
		    labelResourceBundle = "*.lpack.FixedRVLabels",
		    stdFactory = "SimFixedDblRVFactory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation")
public abstract class AbSimFixedDblRVFactory
    <NRV extends SimFixedDoubleRV>
    extends SimDoubleRVFactory<FixedDoubleRV,NRV>
{

    @PrimitiveParm("value")
    double value = 0.0;


    FixedDoubleRVParmManager<NRV> pm;
    /**
     * Constructor.
     * @param sim the simulation
     */
    protected AbSimFixedDblRVFactory(Simulation sim) {
	super(sim);
	pm = new FixedDoubleRVParmManager<NRV>(this);
	initParms(pm, AbSimFixedDblRVFactory.class);
    }

    @Override
    public void initObject(NRV object) {
	super.initObject(object);
	FixedDoubleRV rv = new FixedDoubleRV(value);
	setRV(object, rv);
    }
}

//  LocalWords:  FixedDoubleRV SimFixedDblRVFactory IFRAME SRC px HREF
//  LocalWords:  steelblue FixedDblRVParmManager NRV SimFixedDoubleRV
//  LocalWords:  sdev
