package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.UniformDoubleRV;
import org.bzdev.obnaming.annotations.FactoryParmManager;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;
import org.bzdev.obnaming.annotations.PrimitiveParm;

/**
 * Abstract factory for {@link SimUniformDoubleRV}.
 * <P>
 * The parameters this factory provides are the same as the parameters
 * provided by its subclass {@link SimUniformDblRVFactory}:
 * <P>
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/rv/SimUniformDblRVFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/SimUniformDblRVFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * @see SimUniformDblRVFactory
 */
@FactoryParmManager(value="UniformDoubleRVParmManager",
		    tipResourceBundle="*.lpack.UniformRVTips",
		    labelResourceBundle = "*.lpack.UniformRVLabels",
		    stdFactory = "SimUniformDblRVFactory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation")
public abstract class AbSimUniformDblRVFactory
    <NRV extends SimUniformDoubleRV>
    extends SimDoubleRVFactory<UniformDoubleRV,NRV>
{

    @PrimitiveParm("lowerLimit")
    double lowerLimit = 0.0;

    @PrimitiveParm("lowerLimitClosed")
    boolean lowerLimitClosed = true;

    @PrimitiveParm("upperLimit")
    double upperLimit = 1.0;

    @PrimitiveParm("upperLimitClosed")
    boolean upperLimitClosed = false;
        


    UniformDoubleRVParmManager<NRV> pm;
    /**
     * Constructor.
     * @param sim the simulation
     */
    protected AbSimUniformDblRVFactory(Simulation sim) {
	super(sim);
	pm = new UniformDoubleRVParmManager<NRV>(this);
	initParms(pm, AbSimUniformDblRVFactory.class);
    }

    @Override
    public void initObject(NRV object) {
	super.initObject(object);
	UniformDoubleRV rv = new UniformDoubleRV(lowerLimit, lowerLimitClosed,
						 upperLimit, upperLimitClosed);
	setRV(object, rv);
    }
}

//  LocalWords:  UniformDoubleRV SimUniformDblRVFactory IFRAME SRC px HREF
//  LocalWords:  steelblue UniformDblRVParmManager NRV SimUniformDoubleRV
//  LocalWords:  sdev
