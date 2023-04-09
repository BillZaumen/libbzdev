package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.UniformIATimeRV;
import org.bzdev.obnaming.annotations.FactoryParmManager;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;
import org.bzdev.obnaming.annotations.PrimitiveParm;

/**
 * Abstract factory for {@link SimUniformIATimeRV}.
 * <P>
 * The parameters this factory provides are the same as the parameters
 * provided by its subclass {@link SimUniformIATimeRVFactory}:
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/rv/SimUniformIATimeRVFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/SimUniformIATimeRVFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * @see SimUniformIATimeRVFactory
 */
@FactoryParmManager(value="UniformIATimeRVParmManager",
		    tipResourceBundle="*.lpack.UniformRVTips",
		    labelResourceBundle = "*.lpack.UniformRVLabels",
		    stdFactory = "SimUniformIATimeRVFactory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation")
public abstract class AbSimUniformIATimeRVFactory
    <NRV extends SimUniformIATimeRV>
    extends SimIATimeRVFactory<UniformIATimeRV,NRV>
{

    @PrimitiveParm("lowerLimit")
    long lowerLimit = 0;

    @PrimitiveParm("lowerLimitClosed")
    boolean lowerLimitClosed = true;

    @PrimitiveParm("upperLimit")
    long upperLimit = 1;

    @PrimitiveParm("upperLimitClosed")
    boolean upperLimitClosed = false;

    UniformIATimeRVParmManager<NRV> pm;
    /**
     * Constructor.
     * @param sim the simulation
     */
    protected AbSimUniformIATimeRVFactory(Simulation sim) {
	super(sim);
	pm = new UniformIATimeRVParmManager<NRV>(this);
	initParms(pm, AbSimUniformIATimeRVFactory.class);
    }

    @Override
    public void initObject(NRV object) {
	super.initObject(object);
	UniformIATimeRV rv = new UniformIATimeRV(lowerLimit, lowerLimitClosed,
						 upperLimit, upperLimitClosed);
	setRV(object, rv);
    }
}

//  LocalWords:  UniformIATimeRV SimUniformIATimeRVFactory IFRAME SRC px HREF
//  LocalWords:  steelblue UniformIATimeRVParmManager NRV SimPoissonIATimeRV
//  LocalWords:  sdev
