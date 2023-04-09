package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.BinomialIntegerRV;
import org.bzdev.obnaming.annotations.FactoryParmManager;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;
import org.bzdev.obnaming.annotations.PrimitiveParm;

/**
 * Abstract factory for {@link BinomialIntegerRV}.
 * <P>
 * The parameters this factory provides are the same as the parameters
 * provided by its subclass {@link SimBinomialIntRVFactory}:
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/rv/SimBinomialIntRVFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/SimBinomialIntRVFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * @see SimBinomialIntRVFactory
 */
@FactoryParmManager(value="BinomialIntRVParmManager",
		    tipResourceBundle="*.lpack.BinomialNumbRVTips",
		    labelResourceBundle = "*.lpack.BinomialNumbRVLabels",
		    stdFactory = "SimBinomialIntRVFactory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation")
public abstract class AbSimBinomialIntRVFactory
    <NRV extends SimBinomialIntegerRV>
    extends SimIntegerRVFactory<BinomialIntegerRV,NRV>
{

    @PrimitiveParm(value = "prob", lowerBound = "0.0", lowerBoundClosed = true,
		   upperBound = "1.0", upperBoundClosed = true)
    double prob = 0.5;

    @PrimitiveParm(value = "n", lowerBound = "1", lowerBoundClosed = true)
    int n = 2;

    BinomialIntRVParmManager<NRV> pm;
    /**
     * Constructor.
     * @param sim the simulation
     */
    protected AbSimBinomialIntRVFactory(Simulation sim) {
	super(sim);
	pm = new BinomialIntRVParmManager<NRV>(this);
	initParms(pm, AbSimBinomialIntRVFactory.class);
    }

    @Override
    public void initObject(NRV object) {
	super.initObject(object);
	BinomialIntegerRV rv = new BinomialIntegerRV(prob, n);
	setRV(object, rv);
    }
}

//  LocalWords:  BinomialIntegerRV SimBinomialIntRVFactory IFRAME SRC px HREF
//  LocalWords:  steelblue BinomialIntRVParmManager NRV SimBinomialIntegerRV
//  LocalWords:  sdev
