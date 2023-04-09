package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.IntegerRandomVariable;
import org.bzdev.math.rv.FixedIntegerRV;
import org.bzdev.math.rv.UniformIntegerRV;
import org.bzdev.math.rv.UniformIntegerRVRV;
import org.bzdev.obnaming.annotations.FactoryParmManager;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;
import org.bzdev.obnaming.annotations.PrimitiveParm;

/**
 * Abstract factory for {@link SimUniformIntegerRVRV}.
 * <P>
 * The parameters this factory provides are the same as the parameters
 * provided by its subclass {@link SimUniformIntRVRVFactory}:
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/rv/SimUniformIntRVRVFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/SimUniformIntRVRVFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * @see SimUniformIntRVRVFactory
 */
@FactoryParmManager(value="UniformIntegerRVRVParmManager",
		    tipResourceBundle="*.lpack.UniformRVRVTips",
		    labelResourceBundle = "*.lpack.UniformRVRVLabels",
		    stdFactory = "SimUniformIntRVRVFactory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation")
public abstract class AbSimUniformIntRVRVFactory
    <NRVRV extends SimUniformIntegerRVRV>
    extends SimIntegerRVRVFactory<UniformIntegerRV,UniformIntegerRVRV,NRVRV>
{

    @PrimitiveParm("lowerLimit")
    IntegerRandomVariable lowerLimit = new FixedIntegerRV(0);

    @PrimitiveParm("lowerLimitClosed")
    boolean lowerLimitClosed = true;

    @PrimitiveParm("upperLimit")
    IntegerRandomVariable upperLimit = new FixedIntegerRV(1);

    @PrimitiveParm("upperLimitClosed")
    boolean upperLimitClosed = false;
        


    UniformIntegerRVRVParmManager<NRVRV> pm;
    /**
     * Constructor.
     * @param sim the simulation
     */
    protected AbSimUniformIntRVRVFactory(Simulation sim) {
	super(sim);
	pm = new UniformIntegerRVRVParmManager<NRVRV>(this);
	initParms(pm, AbSimUniformIntRVRVFactory.class);
    }

    @Override
    public void initObject(NRVRV object) {
	super.initObject(object);
	UniformIntegerRVRV rv =
	    new UniformIntegerRVRV(lowerLimit, lowerLimitClosed,
				  upperLimit, upperLimitClosed);
	setRV(object, rv);
    }
}

//  LocalWords:  UniformIntegerRVRV SimUniformIntRVRVFactory IFRAME SRC px HREF
//  LocalWords:  steelblue UniformIntRVRVParmManager NRVRV SimUniformIntegerRVRV
//  LocalWords:  sdev
