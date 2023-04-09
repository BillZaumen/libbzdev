package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.UniformIntegerRV;
import org.bzdev.obnaming.annotations.FactoryParmManager;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;
import org.bzdev.obnaming.annotations.PrimitiveParm;

/**
 * Abstract factory for {@link SimUniformIntegerRV}.
 * <P>
 * The parameters this factory provides are the same as the parameters
 * provided by its subclass {@link SimUniformIntRVFactory}:
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/rv/SimUniformIntRVFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/SimUniformIntRVFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * @see SimUniformIntRVFactory
 */
@FactoryParmManager(value="UniformIntegerRVParmManager",
		    tipResourceBundle="*.lpack.UniformRVTips",
		    labelResourceBundle = "*.lpack.UniformRVLabels",
		    stdFactory = "SimUniformIntRVFactory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation")
public abstract class AbSimUniformIntRVFactory
    <NRV extends SimUniformIntegerRV>
    extends SimIntegerRVFactory<UniformIntegerRV,NRV>
{

    @PrimitiveParm("lowerLimit")
    int lowerLimit = 0;

    @PrimitiveParm("lowerLimitClosed")
    boolean lowerLimitClosed = true;

    @PrimitiveParm("upperLimit")
    int upperLimit = 1;

    @PrimitiveParm("upperLimitClosed")
    boolean upperLimitClosed = true;
        


    UniformIntegerRVParmManager<NRV> pm;
    /**
     * Constructor.
     * @param sim the simulation
     */
    protected AbSimUniformIntRVFactory(Simulation sim) {
	super(sim);
	pm = new UniformIntegerRVParmManager<NRV>(this);
	initParms(pm, AbSimUniformIntRVFactory.class);
    }

    @Override
    public void initObject(NRV object) {
	super.initObject(object);
	UniformIntegerRV rv = new UniformIntegerRV(lowerLimit, lowerLimitClosed,
						 upperLimit, upperLimitClosed);
	setRV(object, rv);
    }
}

//  LocalWords:  UniformIntegerRV SimUniformIntRVFactory IFRAME SRC px HREF
//  LocalWords:  steelblue UniformIntRVParmManager NRV SimUniformIntegerRV
//  LocalWords:  sdev
