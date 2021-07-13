package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.BinomialLongRV;
import org.bzdev.math.rv.BinomialLongRVRV;
import org.bzdev.math.rv.DoubleRandomVariable;
import org.bzdev.math.rv.FixedDoubleRV;
import org.bzdev.obnaming.annotations.FactoryParmManager;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;
import org.bzdev.obnaming.annotations.PrimitiveParm;

/**
 * Abstract factory for {@link SimBinomialLongRV}.
 * <P>
 * The parameters this factory provides are the same as the parameters
 * provided by its subclass {@link SimBinomialLongRVRVFactory}:
 * <P>
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/rv/SimBinomialLongRVRVFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/SimBinomialLongRVRVFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * @see SimBinomialLongRVRVFactory
 */
@FactoryParmManager(value="BinomialLongRVRVParmManager",
		    tipResourceBundle="*.lpack.BinomialNumbRVRVTips",
		    labelResourceBundle = "*.lpack.BinomialNumbRVRVLabels",
		    stdFactory = "SimBinomialLongRVRVFactory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation")
public abstract class AbSimBinomialLongRVRVFactory
    <NRVRV extends SimBinomialLongRVRV>
    extends SimLongRVRVFactory<BinomialLongRV,BinomialLongRVRV,NRVRV>
{

    @PrimitiveParm(value = "prob", lowerBound = "0.0", lowerBoundClosed = true,
		   upperBound = "1.0", upperBoundClosed = true)
    DoubleRandomVariable prob = new FixedDoubleRV(0.5);

    @PrimitiveParm(value = "n", lowerBound = "1", lowerBoundClosed = true)
    long n = 2;


    BinomialLongRVRVParmManager<NRVRV> pm;
    /**
     * Constructor.
     * @param sim the simulation
     */
    protected AbSimBinomialLongRVRVFactory(Simulation sim) {
	super(sim);
	pm = new BinomialLongRVRVParmManager<NRVRV>(this);
	initParms(pm, AbSimBinomialLongRVRVFactory.class);
    }

    @Override
    public void initObject(NRVRV object) {
	super.initObject(object);
	BinomialLongRVRV rv = new BinomialLongRVRV(prob, n);
	setRV(object, rv);
    }
}

//  LocalWords:  BinomialLongRV SimBinomialLongRVRVFactory IFRAME SRC px HREF
//  LocalWords:  steelblue BinomialLongRVParmManager NRV SimBinomialLongRV
//  LocalWords:  sdev
