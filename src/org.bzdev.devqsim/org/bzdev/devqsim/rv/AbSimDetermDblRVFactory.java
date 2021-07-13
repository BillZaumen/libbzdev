package org.bzdev.devqsim.rv;
import java.util.TreeMap;
import java.util.Map;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.DetermDoubleRV;
import org.bzdev.obnaming.annotations.FactoryParmManager;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;
import org.bzdev.obnaming.annotations.PrimitiveParm;
import org.bzdev.obnaming.annotations.KeyedPrimitiveParm;

/**
 * Abstract factory for {@link SimDetermDoubleRV}.
 * <P>
 * The parameters this factory provides are the same as the parameters
 * provided by its subclass {@link SimDetermDblRVFactory}:
 * <P>
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/rv/SimDetermDblRVFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/SimDetermDblRVFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * @see SimDetermDblRVFactory
 */
@FactoryParmManager(value="DetermDoubleRVParmManager",
		    tipResourceBundle="*.lpack.DetermRVTips",
		    labelResourceBundle = "*.lpack.DetermRVLabels",
		    stdFactory = "SimDetermDblRVFactory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation")
public abstract class AbSimDetermDblRVFactory
    <NRV extends SimDetermDoubleRV>
    extends SimDoubleRVFactory<DetermDoubleRV,NRV>
{

    @KeyedPrimitiveParm("values")
    TreeMap<Integer,Double>map = new TreeMap<>();

    @PrimitiveParm("finalValue")
    Double finalValue = null;


    DetermDoubleRVParmManager<NRV> pm;
    /**
     * Constructor.
     * @param sim the simulation
     */
    protected AbSimDetermDblRVFactory(Simulation sim) {
	super(sim);
	pm = new DetermDoubleRVParmManager<NRV>(this);
	initParms(pm, AbSimDetermDblRVFactory.class);
    }

    @Override
    public void initObject(NRV object) {
	super.initObject(object);
	double values[] = new double[map.size()];
	int i = 0;
	for (Double value: map.values()) {
	    values[i++] = value;
	}
	DetermDoubleRV rv = (finalValue == null)? new DetermDoubleRV(values):
	    new DetermDoubleRV(values, finalValue);
	setRV(object, rv);
    }
}

//  LocalWords:  DetermDoubleRV SimDetermDblRVFactory IFRAME SRC px HREF
//  LocalWords:  steelblue DetermDblRVParmManager NRV SimDetermDoubleRV
//  LocalWords:  sdev
