package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.BinomialBooleanRV;
import org.bzdev.math.rv.BinomialBooleanRVRV;
import org.bzdev.math.rv.DoubleRandomVariable;
import org.bzdev.math.rv.FixedDoubleRV;
import org.bzdev.obnaming.annotations.FactoryParmManager;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;
import org.bzdev.obnaming.annotations.PrimitiveParm;

/**
 * Abstract factory for {@link SimBinomialBooleanRV}.
 * <P>
 * The parameters this factory provides are the same as the parameters
 * provided by its subclass {@link SimBinomialBoolRVRVFactory}:
 * <P>
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/rv/SimBinomialBoolRVRVFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/SimBinomialBoolRVRVFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * @see SimBinomialBoolRVRVFactory
 */
@FactoryParmManager(value="BinomialBooleanRVRVParmManager",
		    tipResourceBundle="*.lpack.BinomialBooleanRVRVTips",
		    labelResourceBundle = "*.lpack.BinomialBooleanRVRVLabels",
		    stdFactory = "SimBinomialBoolRVRVFactory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation")
public abstract class AbSimBinomialBoolRVRVFactory
    <NRVRV extends SimBinomialBooleanRVRV>
    extends SimBooleanRVRVFactory<BinomialBooleanRV,BinomialBooleanRVRV,NRVRV>
{

    @PrimitiveParm(value = "prob", lowerBound = "0.0", lowerBoundClosed = true,
		   upperBound = "1.0", upperBoundClosed = true)
    DoubleRandomVariable prob = new FixedDoubleRV(0.5);


    BinomialBooleanRVRVParmManager<NRVRV> pm;
    /**
     * Constructor.
     * @param sim the simulation
     */
    protected AbSimBinomialBoolRVRVFactory(Simulation sim) {
	super(sim);
	pm = new BinomialBooleanRVRVParmManager<NRVRV>(this);
	initParms(pm, AbSimBinomialBoolRVRVFactory.class);
    }

    @Override
    public void initObject(NRVRV object) {
	super.initObject(object);
	BinomialBooleanRVRV rv = new BinomialBooleanRVRV(prob);
	setRV(object, rv);
    }
}

//  LocalWords:  BinomialBooleanRV SimBinomialBoolRVRVFactory IFRAME SRC px HREF
//  LocalWords:  steelblue BinomialBoolRVParmManager NRV SimBinomialBooleanRV
//  LocalWords:  sdev
