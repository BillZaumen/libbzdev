 package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.BinomialDoubleRV;
import org.bzdev.obnaming.annotations.FactoryParmManager;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;
import org.bzdev.obnaming.annotations.PrimitiveParm;

/**
 * Abstract factory for {@link SimBinomialDoubleRV}.
 * <P>
 * The parameters this factory provides are the same as the parameters
 * provided by its subclass {@link SimBinomialDblRVFactory}:
 * <P>
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/rv/SimBinomialDblRVFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/SimBinomialDblRVFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * @see SimBinomialDblRVFactory
 */
@FactoryParmManager(value="BinomialDblRVParmManager",
		    tipResourceBundle="*.lpack.BinomialNumbRVTips",
		    labelResourceBundle = "*.lpack.BinomialNumbRVLabels",
		    stdFactory = "SimBinomialDblRVFactory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation")
public abstract class AbSimBinomialDblRVFactory
    <NRV extends SimBinomialDoubleRV>
    extends SimDoubleRVFactory<BinomialDoubleRV,NRV>
{

    @PrimitiveParm(value = "prob", lowerBound = "0.0", lowerBoundClosed = true,
		   upperBound = "1.0", upperBoundClosed = true)
    double prob = 0.5;

    @PrimitiveParm(value = "n", lowerBound = "1", lowerBoundClosed = true)
    long n = 2;

    BinomialDblRVParmManager<NRV> pm;
    /**
     * Constructor.
     * @param sim the simulation
     */
    protected AbSimBinomialDblRVFactory(Simulation sim) {
	super(sim);
	pm = new BinomialDblRVParmManager<NRV>(this);
	initParms(pm, AbSimBinomialDblRVFactory.class);
    }

    @Override
    public void initObject(NRV object) {
	super.initObject(object);
	BinomialDoubleRV rv = new BinomialDoubleRV(prob, (double)n);
	setRV(object, rv);
    }
}

//  LocalWords:  BinomialDoubleRV SimBinomialDblRVFactory IFRAME SRC px HREF
//  LocalWords:  steelblue BinomialDblRVParmManager NRV SimBinomialDoubleRV
//  LocalWords:  sdev
