package org.bzdev.devqsim.rv;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.BinomialIntegerRV;
import org.bzdev.math.rv.BinomialIntegerRVRV;
import org.bzdev.math.rv.DoubleRandomVariable;
import org.bzdev.math.rv.FixedDoubleRV;
import org.bzdev.obnaming.annotations.FactoryParmManager;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;
import org.bzdev.obnaming.annotations.PrimitiveParm;

/**
 * Abstract factory for {@link SimBinomialIntegerRV}.
 * <P>
 * The parameters this factory provides are the same as the parameters
 * provided by its subclass {@link SimBinomialIntRVRVFactory}:
 * <P>
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/rv/SimBinomialIntRVRVFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/SimBinomialIntRVRVFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * @see SimBinomialIntRVRVFactory
 */
@FactoryParmManager(value="BinomialIntegerRVRVParmManager",
		    tipResourceBundle="*.lpack.BinomialNumbRVRVTips",
		    labelResourceBundle = "*.lpack.BinomialNumbRVRVLabels",
		    stdFactory = "SimBinomialIntRVRVFactory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation")
public abstract class AbSimBinomialIntRVRVFactory
    <NRVRV extends SimBinomialIntegerRVRV>
    extends SimIntegerRVRVFactory<BinomialIntegerRV,BinomialIntegerRVRV,NRVRV>
{

    @PrimitiveParm(value = "prob", lowerBound = "0.0", lowerBoundClosed = true,
		   upperBound = "1.0", upperBoundClosed = true)
    DoubleRandomVariable prob = new FixedDoubleRV(0.5);

    @PrimitiveParm(value = "n", lowerBound = "1", lowerBoundClosed = true)
    int n = 2;


    BinomialIntegerRVRVParmManager<NRVRV> pm;
    /**
     * Constructor.
     * @param sim the simulation
     */
    protected AbSimBinomialIntRVRVFactory(Simulation sim) {
	super(sim);
	pm = new BinomialIntegerRVRVParmManager<NRVRV>(this);
	initParms(pm, AbSimBinomialIntRVRVFactory.class);
    }

    @Override
    public void initObject(NRVRV object) {
	super.initObject(object);
	BinomialIntegerRVRV rv = new BinomialIntegerRVRV(prob, n);
	setRV(object, rv);
    }
}

//  LocalWords:  BinomialIntegerRV SimBinomialIntRVRVFactory IFRAME SRC px HREF
//  LocalWords:  steelblue BinomialIntRVParmManager NRV SimBinomialIntegerRV
//  LocalWords:  sdev
