package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.LongRandomVariable;
import org.bzdev.math.rv.FixedLongRV;
import org.bzdev.math.rv.UniformLongRV;
import org.bzdev.math.rv.UniformLongRVRV;
import org.bzdev.obnaming.annotations.FactoryParmManager;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;
import org.bzdev.obnaming.annotations.PrimitiveParm;

/**
 * Abstract factory for {@link SimUniformLongRVRV}.
 * <P>
 * The parameters this factory provides are the same as the parameters
 * provided by its subclass {@link SimUniformLongRVRVFactory}:
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/rv/SimUniformLongRVRVFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/SimUniformLongRVRVFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * @see SimUniformLongRVRVFactory
 */
@FactoryParmManager(value="UniformLongRVRVParmManager",
		    tipResourceBundle="*.lpack.UniformRVRVTips",
		    labelResourceBundle = "*.lpack.UniformRVRVLabels",
		    stdFactory = "SimUniformLongRVRVFactory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation")
public abstract class AbSimUniformLongRVRVFactory
    <NRVRV extends SimUniformLongRVRV>
    extends SimLongRVRVFactory<UniformLongRV,UniformLongRVRV,NRVRV>
{

    @PrimitiveParm("lowerLimit")
    LongRandomVariable lowerLimit = new FixedLongRV(0);

    @PrimitiveParm("lowerLimitClosed")
    boolean lowerLimitClosed = true;

    @PrimitiveParm("upperLimit")
    LongRandomVariable upperLimit = new FixedLongRV(1);

    @PrimitiveParm("upperLimitClosed")
    boolean upperLimitClosed = false;
        


    UniformLongRVRVParmManager<NRVRV> pm;
    /**
     * Constructor.
     * @param sim the simulation
     */
    protected AbSimUniformLongRVRVFactory(Simulation sim) {
	super(sim);
	pm = new UniformLongRVRVParmManager<NRVRV>(this);
	initParms(pm, AbSimUniformLongRVRVFactory.class);
    }

    @Override
    public void initObject(NRVRV object) {
	super.initObject(object);
	UniformLongRVRV rv =
	    new UniformLongRVRV(lowerLimit, lowerLimitClosed,
				  upperLimit, upperLimitClosed);
	setRV(object, rv);
    }
}

//  LocalWords:  UniformLongRVRV SimUniformLongRVRVFactory IFRAME SRC px HREF
//  LocalWords:  steelblue UniformLongRVRVParmManager NRVRV SimUniformLongRVRV
//  LocalWords:  sdev
