package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.BinomialIATimeRV;
import org.bzdev.math.rv.BinomialIATimeRVRV;
import org.bzdev.math.rv.DoubleRandomVariable;
import org.bzdev.math.rv.FixedDoubleRV;
import org.bzdev.obnaming.annotations.FactoryParmManager;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;
import org.bzdev.obnaming.annotations.PrimitiveParm;

/**
 * Abstract factory for {@link SimBinomialIATimeRV}.
 * <P>
 * The parameters this factory provides are the same as the parameters
 * provided by its subclass {@link SimBinomialIATimeRVRVFactory}:
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/rv/SimBinomialIATimeRVRVFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/SimBinomialIATimeRVRVFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * @see SimBinomialIATimeRVRVFactory
 */
@FactoryParmManager(value="BinomialIATimeRVRVParmManager",
		    tipResourceBundle="*.lpack.BinomialNumbRVRVTips",
		    labelResourceBundle = "*.lpack.BinomialNumbRVRVLabels",
		    stdFactory = "SimBinomialIATimeRVRVFactory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation")
public abstract class AbSimBinomialIATimeRVRVFactory
    <NRVRV extends SimBinomialIATimeRVRV>
    extends SimIATimeRVRVFactory<BinomialIATimeRV,BinomialIATimeRVRV,NRVRV>
{

    @PrimitiveParm(value = "prob", lowerBound = "0.0", lowerBoundClosed = true,
		   upperBound = "1.0", upperBoundClosed = true)
    DoubleRandomVariable prob = new FixedDoubleRV(0.5);

    @PrimitiveParm(value = "n", lowerBound = "1", lowerBoundClosed = true)
    long n = 2;


    BinomialIATimeRVRVParmManager<NRVRV> pm;
    /**
     * Constructor.
     * @param sim the simulation
     */
    protected AbSimBinomialIATimeRVRVFactory(Simulation sim) {
	super(sim);
	pm = new BinomialIATimeRVRVParmManager<NRVRV>(this);
	initParms(pm, AbSimBinomialIATimeRVRVFactory.class);
    }

    @Override
    public void initObject(NRVRV object) {
	super.initObject(object);
	BinomialIATimeRVRV rv = new BinomialIATimeRVRV(prob, n);
	setRV(object, rv);
    }
}

//  LocalWords:  BinomialIATimeRV SimBinomialIATimeRVRVFactory IFRAME SRC px HREF
//  LocalWords:  steelblue BinomialIATimeRVParmManager NRV SimBinomialIATimeRV
//  LocalWords:  sdev
