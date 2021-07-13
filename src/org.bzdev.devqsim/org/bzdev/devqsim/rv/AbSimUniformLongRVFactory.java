package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.UniformLongRV;
import org.bzdev.obnaming.annotations.FactoryParmManager;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;
import org.bzdev.obnaming.annotations.PrimitiveParm;

/**
 * Abstract factory for {@link SimUniformLongRV}.
 * <P>
 * The parameters this factory provides are the same as the parameters
 * provided by its subclass {@link SimUniformLongRVFactory}:
 * <P>
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/rv/SimUniformLongRVFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/SimUniformLongRVFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * @see SimUniformLongRVFactory
 */
@FactoryParmManager(value="UniformLongRVParmManager",
		    tipResourceBundle="*.lpack.UniformRVTips",
		    labelResourceBundle = "*.lpack.UniformRVLabels",
		    stdFactory = "SimUniformLongRVFactory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation")
public abstract class AbSimUniformLongRVFactory
    <NRV extends SimUniformLongRV>
    extends SimLongRVFactory<UniformLongRV,NRV>
{

    @PrimitiveParm("lowerLimit")
    long lowerLimit = 0;

    @PrimitiveParm("lowerLimitClosed")
    boolean lowerLimitClosed = true;

    @PrimitiveParm("upperLimit")
    long upperLimit = 1;

    @PrimitiveParm("upperLimitClosed")
    boolean upperLimitClosed = false;

    UniformLongRVParmManager<NRV> pm;
    /**
     * Constructor.
     * @param sim the simulation
     */
    protected AbSimUniformLongRVFactory(Simulation sim) {
	super(sim);
	pm = new UniformLongRVParmManager<NRV>(this);
	initParms(pm, AbSimUniformLongRVFactory.class);
    }

    @Override
    public void initObject(NRV object) {
	super.initObject(object);
	UniformLongRV rv = new UniformLongRV(lowerLimit, lowerLimitClosed,
						 upperLimit, upperLimitClosed);
	setRV(object, rv);
    }
}

//  LocalWords:  UniformLongRV SimUniformLongRVFactory IFRAME SRC px HREF
//  LocalWords:  steelblue UniformLongRVParmManager NRV SimUniformLongRV
//  LocalWords:  sdev
