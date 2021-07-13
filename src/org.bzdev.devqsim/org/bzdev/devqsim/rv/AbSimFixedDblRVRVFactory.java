package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.DoubleRandomVariable;
import org.bzdev.math.rv.FixedDoubleRV;
import org.bzdev.math.rv.FixedDoubleRVRV;
import org.bzdev.math.rv.UniformDoubleRV;
import org.bzdev.obnaming.annotations.FactoryParmManager;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;
import org.bzdev.obnaming.annotations.PrimitiveParm;

/**
 * Abstract factory for {@link SimFixedDoubleRV}.
 * <P>
 * The parameters this factory provides are the same as the parameters
 * provided by its subclass {@link SimFixedDblRVFactory}:
 * <P>
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/rv/SimFixedDblRVFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/SimFixedDblRVFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * @see SimFixedDblRVFactory
 */
@FactoryParmManager(value="FixedDoubleRVRVParmManager",
		    tipResourceBundle="*.lpack.FixedRVRVTips",
		    labelResourceBundle = "*.lpack.FixedRVRVLabels",
		    stdFactory = "SimFixedDblRVRVFactory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation")
public abstract class AbSimFixedDblRVRVFactory
    <NRVRV extends SimFixedDoubleRVRV>
    extends SimDoubleRVRVFactory<FixedDoubleRV,FixedDoubleRVRV,NRVRV>
{

    @PrimitiveParm("value")
    DoubleRandomVariable value = new FixedDoubleRV(0.0);

    FixedDoubleRVRVParmManager<NRVRV> pm;
    /**
     * Constructor.
     * @param sim the simulation
     */
    protected AbSimFixedDblRVRVFactory(Simulation sim) {
	super(sim);
	pm = new FixedDoubleRVRVParmManager<NRVRV>(this);
	initParms(pm, AbSimFixedDblRVRVFactory.class);
    }

    @Override
    public void initObject(NRVRV object) {
	super.initObject(object);
	FixedDoubleRVRV rv = new FixedDoubleRVRV(value);
	setRV(object, rv);
    }
}

//  LocalWords:  FixedDoubleRV SimFixedDblRVFactory IFRAME SRC px HREF
//  LocalWords:  steelblue FixedDblRVParmManager NRV SimFixedDoubleRV
//  LocalWords:  sdev
