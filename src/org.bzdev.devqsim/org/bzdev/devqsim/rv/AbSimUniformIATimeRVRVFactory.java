package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.InterarrivalTimeRV;
import org.bzdev.math.rv.LongRandomVariable;
import org.bzdev.math.rv.FixedIATimeRV;
import org.bzdev.math.rv.UniformIATimeRV;
import org.bzdev.math.rv.UniformIATimeRVRV;
import org.bzdev.obnaming.annotations.FactoryParmManager;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;
import org.bzdev.obnaming.annotations.PrimitiveParm;

/**
 * Abstract factory for {@link SimUniformIATimeRVRV}.
 * <P>
 * The parameters this factory provides are the same as the parameters
 * provided by its subclass {@link SimUniformIATimeRVRVFactory}:
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/rv/SimUniformIATimeRVRVFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/SimUniformIATimeRVRVFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * @see SimUniformIATimeRVRVFactory
 */
@FactoryParmManager(value="UniformIATimeRVRVParmManager",
		    tipResourceBundle="*.lpack.UniformRVRVTips",
		    labelResourceBundle = "*.lpack.UniformRVRVLabels",
		    stdFactory = "SimUniformIATimeRVRVFactory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation")
public abstract class AbSimUniformIATimeRVRVFactory
    <NRVRV extends SimUniformIATimeRVRV>
    extends SimIATimeRVRVFactory<UniformIATimeRV,UniformIATimeRVRV,NRVRV>
{

    @PrimitiveParm("lowerLimit")
    LongRandomVariable lowerLimit = new FixedIATimeRV(0);

    @PrimitiveParm("lowerLimitClosed")
    boolean lowerLimitClosed = true;

    @PrimitiveParm("upperLimit")
    LongRandomVariable upperLimit = new FixedIATimeRV(1);

    @PrimitiveParm("upperLimitClosed")
    boolean upperLimitClosed = false;
        


    UniformIATimeRVRVParmManager<NRVRV> pm;
    /**
     * Constructor.
     * @param sim the simulation
     */
    protected AbSimUniformIATimeRVRVFactory(Simulation sim) {
	super(sim);
	pm = new UniformIATimeRVRVParmManager<NRVRV>(this);
	initParms(pm, AbSimUniformIATimeRVRVFactory.class);
    }

    @Override
    public void initObject(NRVRV object) {
	super.initObject(object);
	UniformIATimeRVRV rv =
	    new UniformIATimeRVRV(lowerLimit, lowerLimitClosed,
				  upperLimit, upperLimitClosed);
	setRV(object, rv);
    }
}

//  LocalWords:  UniformIATimeRVRV SimUniformIATimeRVRVFactory IFRAME SRC px HREF
//  LocalWords:  steelblue UniformIATimeRVRVParmManager NRVRV SimUniformIATimeRVRV
//  LocalWords:  sdev
