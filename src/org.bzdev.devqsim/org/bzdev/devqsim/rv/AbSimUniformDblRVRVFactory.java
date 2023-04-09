package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.DoubleRandomVariable;
import org.bzdev.math.rv.FixedDoubleRV;
import org.bzdev.math.rv.UniformDoubleRV;
import org.bzdev.math.rv.UniformDoubleRVRV;
import org.bzdev.obnaming.annotations.FactoryParmManager;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;
import org.bzdev.obnaming.annotations.PrimitiveParm;

/**
 * Abstract factory for {@link SimUniformDoubleRVRV}.
 * <P>
 * The parameters this factory provides are the same as the parameters
 * provided by its subclass {@link SimUniformDblRVRVFactory}:
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/rv/SimUniformDblRVRVFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/SimUniformDblRVRVFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * @see SimUniformDblRVRVFactory
 */
@FactoryParmManager(value="UniformDoubleRVRVParmManager",
		    tipResourceBundle="*.lpack.UniformRVRVTips",
		    labelResourceBundle = "*.lpack.UniformRVRVLabels",
		    stdFactory = "SimUniformDblRVRVFactory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation")
public abstract class AbSimUniformDblRVRVFactory
    <NRVRV extends SimUniformDoubleRVRV>
    extends SimDoubleRVRVFactory<UniformDoubleRV,UniformDoubleRVRV,NRVRV>
{

    @PrimitiveParm("lowerLimit")
    DoubleRandomVariable lowerLimit = new FixedDoubleRV(0.0);

    @PrimitiveParm("lowerLimitClosed")
    boolean lowerLimitClosed = true;

    @PrimitiveParm("upperLimit")
    DoubleRandomVariable upperLimit = new FixedDoubleRV(1.0);

    @PrimitiveParm("upperLimitClosed")
    boolean upperLimitClosed = false;
        


    UniformDoubleRVRVParmManager<NRVRV> pm;
    /**
     * Constructor.
     * @param sim the simulation
     */
    protected AbSimUniformDblRVRVFactory(Simulation sim) {
	super(sim);
	pm = new UniformDoubleRVRVParmManager<NRVRV>(this);
	initParms(pm, AbSimUniformDblRVRVFactory.class);
    }

    @Override
    public void initObject(NRVRV object) {
	super.initObject(object);
	UniformDoubleRVRV rv =
	    new UniformDoubleRVRV(lowerLimit, lowerLimitClosed,
				  upperLimit, upperLimitClosed);
	setRV(object, rv);
    }
}

//  LocalWords:  UniformDoubleRVRV SimUniformDblRVRVFactory IFRAME SRC px HREF
//  LocalWords:  steelblue UniformDblRVRVParmManager NRVRV SimUniformDoubleRVRV
//  LocalWords:  sdev
