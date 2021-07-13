package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.BinomialBooleanRV;
import org.bzdev.obnaming.annotations.FactoryParmManager;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;
import org.bzdev.obnaming.annotations.PrimitiveParm;

/**
 * Abstract factory for {@link SimBinomialBooleanRV}.
 * <P>
 * The parameters this factory provides are the same as the parameters
 * provided by its subclass {@link SimBinomialBoolRVFactory}:
 * <P>
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/rv/SimBinomialBoolRVFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/SimBinomialBoolRVFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * @see SimBinomialBoolRVFactory
 */
@FactoryParmManager(value="BinomialBooleanRVParmManager",
		    tipResourceBundle="*.lpack.BinomialBooleanRVTips",
		    labelResourceBundle = "*.lpack.BinomialBooleanRVLabels",
		    stdFactory = "SimBinomialBoolRVFactory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation")
public abstract class AbSimBinomialBoolRVFactory
    <NRV extends SimBinomialBooleanRV>
    extends SimBooleanRVFactory<BinomialBooleanRV,NRV>
{

    @PrimitiveParm(value = "prob", lowerBound = "0.0", lowerBoundClosed = true,
		   upperBound = "1.0", upperBoundClosed = true)
    double prob = 0.5;


    BinomialBooleanRVParmManager<NRV> pm;
    /**
     * Constructor.
     * @param sim the simulation
     */
    protected AbSimBinomialBoolRVFactory(Simulation sim) {
	super(sim);
	pm = new BinomialBooleanRVParmManager<NRV>(this);
	initParms(pm, AbSimBinomialBoolRVFactory.class);
    }

    @Override
    public void initObject(NRV object) {
	super.initObject(object);
	BinomialBooleanRV rv = new BinomialBooleanRV(prob);
	setRV(object, rv);
    }
}

//  LocalWords:  BinomialBooleanRV SimBinomialBoolRVFactory IFRAME SRC px HREF
//  LocalWords:  steelblue BinomialBoolRVParmManager NRV SimBinomialBooleanRV
//  LocalWords:  sdev
