package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.BinomialLongRV;
import org.bzdev.obnaming.annotations.FactoryParmManager;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;
import org.bzdev.obnaming.annotations.PrimitiveParm;

/**
 * Abstract factory for {@link SimBinomialLongRV}.
 * <P>
 * The parameters this factory provides are the same as the parameters
 * provided by its subclass {@link SimBinomialLongRVFactory}:
 * <P>
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/rv/SimBinomialLongRVFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/SimBinomialLongRVFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * @see SimBinomialLongRVFactory
 */
@FactoryParmManager(value="BinomialLongRVParmManager",
		    tipResourceBundle="*.lpack.BinomialNumbRVTips",
		    labelResourceBundle = "*.lpack.BinomialNumbRVLabels",
		    stdFactory = "SimBinomialLongRVFactory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation")
public abstract class AbSimBinomialLongRVFactory
    <NRV extends SimBinomialLongRV>
    extends SimLongRVFactory<BinomialLongRV,NRV>
{

    @PrimitiveParm(value = "prob", lowerBound = "0.0", lowerBoundClosed = true,
		   upperBound = "1.0", upperBoundClosed = true)
    double prob = 0.5;

    @PrimitiveParm(value = "n", lowerBound = "1", lowerBoundClosed = true)
    long n = 2;

    BinomialLongRVParmManager<NRV> pm;
    /**
     * Constructor.
     * @param sim the simulation
     */
    protected AbSimBinomialLongRVFactory(Simulation sim) {
	super(sim);
	pm = new BinomialLongRVParmManager<NRV>(this);
	initParms(pm, AbSimBinomialLongRVFactory.class);
    }

    @Override
    public void initObject(NRV object) {
	super.initObject(object);
	BinomialLongRV rv = new BinomialLongRV(prob, n);
	setRV(object, rv);
    }
}

//  LocalWords:  BinomialLongRV SimBinomialLongRVFactory IFRAME SRC px HREF
//  LocalWords:  steelblue BinomialLongRVParmManager NRV SimBinomialLongRV
//  LocalWords:  sdev
