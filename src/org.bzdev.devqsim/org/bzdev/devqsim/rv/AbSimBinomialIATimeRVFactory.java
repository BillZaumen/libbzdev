package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.BinomialIATimeRV;
import org.bzdev.obnaming.annotations.FactoryParmManager;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;
import org.bzdev.obnaming.annotations.PrimitiveParm;

/**
 * Abstract factory for {@link SimBinomialIATimeRV}.
 * <P>
 * The parameters this factory provides are the same as the parameters
 * provided by its subclass {@link SimBinomialIATimeRVFactory}:
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/rv/SimBinomialIATimeRVFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/SimBinomialIATimeRVFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * @see SimBinomialIATimeRVFactory
 */
@FactoryParmManager(value="BinomialIATimeRVParmManager",
		    tipResourceBundle="*.lpack.BinomialNumbRVTips",
		    labelResourceBundle = "*.lpack.BinomialNumbRVLabels",
		    stdFactory = "SimBinomialIATimeRVFactory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation")
public abstract class AbSimBinomialIATimeRVFactory
    <NRV extends SimBinomialIATimeRV>
    extends SimIATimeRVFactory<BinomialIATimeRV,NRV>
{

    @PrimitiveParm(value = "prob", lowerBound = "0.0", lowerBoundClosed = true,
		   upperBound = "1.0", upperBoundClosed = true)
    double prob = 0.5;

    @PrimitiveParm(value = "n", lowerBound = "1", lowerBoundClosed = true)
    long n = 2;

    BinomialIATimeRVParmManager<NRV> pm;
    /**
     * Constructor.
     * @param sim the simulation
     */
    protected AbSimBinomialIATimeRVFactory(Simulation sim) {
	super(sim);
	pm = new BinomialIATimeRVParmManager<NRV>(this);
	initParms(pm, AbSimBinomialIATimeRVFactory.class);
    }

    @Override
    public void initObject(NRV object) {
	super.initObject(object);
	BinomialIATimeRV rv = new BinomialIATimeRV(prob, n);
	setRV(object, rv);
    }
}

//  LocalWords:  BinomialIATimeRV SimBinomialIATimeRVFactory IFRAME SRC px HREF
//  LocalWords:  steelblue BinomialIATimeRVParmManager NRV SimBinomialIATimeRV
//  LocalWords:  sdev
