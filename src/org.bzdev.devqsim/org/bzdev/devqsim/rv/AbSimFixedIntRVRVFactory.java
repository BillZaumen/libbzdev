package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.IntegerRandomVariable;
import org.bzdev.math.rv.FixedIntegerRV;
import org.bzdev.math.rv.FixedIntegerRVRV;
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
@FactoryParmManager(value="FixedIntegerRVRVParmManager",
		    tipResourceBundle="*.lpack.FixedRVRVTips",
		    labelResourceBundle = "*.lpack.FixedRVRVLabels",
		    stdFactory = "SimFixedIntRVRVFactory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation")
public abstract class AbSimFixedIntRVRVFactory
    <NRVRV extends SimFixedIntegerRVRV>
    extends SimIntegerRVRVFactory<FixedIntegerRV,FixedIntegerRVRV,NRVRV>
{

    @PrimitiveParm("value")
    IntegerRandomVariable value = new FixedIntegerRV(0);

    FixedIntegerRVRVParmManager<NRVRV> pm;
    /**
     * Constructor.
     * @param sim the simulation
     */
    protected AbSimFixedIntRVRVFactory(Simulation sim) {
	super(sim);
	pm = new FixedIntegerRVRVParmManager<NRVRV>(this);
	initParms(pm, AbSimFixedIntRVRVFactory.class);
    }

    @Override
    public void initObject(NRVRV object) {
	super.initObject(object);
	FixedIntegerRVRV rv = new FixedIntegerRVRV(value);
	setRV(object, rv);
    }
}

//  LocalWords:  FixedIntegerRV SimFixedIntRVFactory IFRAME SRC px HREF
//  LocalWords:  steelblue FixedIntRVParmManager NRV SimFixedIntegerRV
//  LocalWords:  sdev
