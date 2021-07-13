package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.FixedIntegerRV;
import org.bzdev.obnaming.annotations.FactoryParmManager;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;
import org.bzdev.obnaming.annotations.PrimitiveParm;

/**
 * Abstract factory for {@link SimFixedIntegerRV}.
 * <P>
 * The parameters this factory provides are the same as the parameters
 * provided by its subclass {@link SimFixedIntRVFactory}:
 * <P>
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/rv/SimFixedIntRVFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/SimFixedIntRVFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * @see SimFixedIntRVFactory
 */
@FactoryParmManager(value="FixedIntegerRVParmManager",
		    tipResourceBundle="*.lpack.FixedRVTips",
		    labelResourceBundle = "*.lpack.FixedRVLabels",
		    stdFactory = "SimFixedIntRVFactory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation")
public abstract class AbSimFixedIntRVFactory
    <NRV extends SimFixedIntegerRV>
    extends SimIntegerRVFactory<FixedIntegerRV,NRV>
{

    @PrimitiveParm("value")
    int value = 0;


    FixedIntegerRVParmManager<NRV> pm;
    /**
     * Constructor.
     * @param sim the simulation
     */
    protected AbSimFixedIntRVFactory(Simulation sim) {
	super(sim);
	pm = new FixedIntegerRVParmManager<NRV>(this);
	initParms(pm, AbSimFixedIntRVFactory.class);
    }

    @Override
    public void initObject(NRV object) {
	super.initObject(object);
	FixedIntegerRV rv = new FixedIntegerRV(value);
	setRV(object, rv);
    }
}

//  LocalWords:  FixedIntegerRV SimFixedIntRVFactory IFRAME SRC px HREF
//  LocalWords:  steelblue FixedIntRVParmManager NRV SimFixedIntegerRV
//  LocalWords:  sdev
