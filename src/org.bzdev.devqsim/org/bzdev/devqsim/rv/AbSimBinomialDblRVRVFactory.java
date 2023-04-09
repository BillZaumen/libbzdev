package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.BinomialDoubleRV;
import org.bzdev.math.rv.BinomialDoubleRVRV;
import org.bzdev.math.rv.DoubleRandomVariable;
import org.bzdev.math.rv.FixedDoubleRV;
import org.bzdev.obnaming.annotations.FactoryParmManager;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;
import org.bzdev.obnaming.annotations.PrimitiveParm;

/**
 * Abstract factory for {@link SimBinomialDoubleRV}.
 * <P>
 * The parameters this factory provides are the same as the parameters
 * provided by its subclass {@link SimBinomialDblRVRVFactory}:
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/rv/SimBinomialDblRVRVFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/SimBinomialDblRVRVFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * @see SimBinomialDblRVRVFactory
 */
@FactoryParmManager(value="BinomialDoubleRVRVParmManager",
		    tipResourceBundle="*.lpack.BinomialNumbRVRVTips",
		    labelResourceBundle = "*.lpack.BinomialNumbRVRVLabels",
		    stdFactory = "SimBinomialDblRVRVFactory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation")
public abstract class AbSimBinomialDblRVRVFactory
    <NRVRV extends SimBinomialDoubleRVRV>
    extends SimDoubleRVRVFactory<BinomialDoubleRV,BinomialDoubleRVRV,NRVRV>
{

    @PrimitiveParm(value = "prob", lowerBound = "0.0", lowerBoundClosed = true,
		   upperBound = "1.0", upperBoundClosed = true)
    DoubleRandomVariable prob = new FixedDoubleRV(0.5);

    @PrimitiveParm(value = "n", lowerBound = "1", lowerBoundClosed = true)
    long n = 2;


    BinomialDoubleRVRVParmManager<NRVRV> pm;
    /**
     * Constructor.
     * @param sim the simulation
     */
    protected AbSimBinomialDblRVRVFactory(Simulation sim) {
	super(sim);
	pm = new BinomialDoubleRVRVParmManager<NRVRV>(this);
	initParms(pm, AbSimBinomialDblRVRVFactory.class);
    }

    @Override
    public void initObject(NRVRV object) {
	super.initObject(object);
	BinomialDoubleRVRV rv = new BinomialDoubleRVRV(prob, n);
	setRV(object, rv);
    }
}

//  LocalWords:  BinomialDoubleRV SimBinomialDblRVRVFactory IFRAME SRC px HREF
//  LocalWords:  steelblue BinomialDblRVParmManager NRV SimBinomialDoubleRV
//  LocalWords:  sdev
