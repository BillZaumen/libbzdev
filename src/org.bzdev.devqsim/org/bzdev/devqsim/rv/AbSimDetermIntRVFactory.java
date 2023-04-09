package org.bzdev.devqsim.rv;
import java.util.TreeMap;
import java.util.Map;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.DetermIntegerRV;
import org.bzdev.obnaming.annotations.FactoryParmManager;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;
import org.bzdev.obnaming.annotations.PrimitiveParm;
import org.bzdev.obnaming.annotations.KeyedPrimitiveParm;

/**
 * Abstract factory for {@link SimDetermIntegerRV}.
 * <P>
 * The parameters this factory provides are the same as the parameters
 * provided by its subclass {@link SimDetermIntRVFactory}:
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/rv/SimDetermIntRVFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/SimDetermIntRVFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * @see SimDetermIntRVFactory
 */
@FactoryParmManager(value="DetermIntRVParmManager",
		    tipResourceBundle="*.lpack.DetermRVTips",
		    labelResourceBundle = "*.lpack.DetermRVLabels",
		    stdFactory = "SimDetermIntRVFactory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation")
public abstract class AbSimDetermIntRVFactory
    <NRV extends SimDetermIntegerRV>
    extends SimIntegerRVFactory<DetermIntegerRV,NRV>
{

    @KeyedPrimitiveParm("values")
    TreeMap<Integer,Integer>map = new TreeMap<>();

    @PrimitiveParm("finalValue")
    Integer finalValue = null;


    DetermIntRVParmManager<NRV> pm;
    /**
     * Constructor.
     * @param sim the simulation
     */
    protected AbSimDetermIntRVFactory(Simulation sim) {
	super(sim);
	pm = new DetermIntRVParmManager<NRV>(this);
	initParms(pm, AbSimDetermIntRVFactory.class);
    }

    @Override
    public void initObject(NRV object) {
	super.initObject(object);
	int values[] = new int[map.size()];
	int i = 0;
	for (int value: map.values()) {
	    values[i++] = value;
	}
	DetermIntegerRV rv = (finalValue == null)? new DetermIntegerRV(values):
	    new DetermIntegerRV(values, finalValue);
	setRV(object, rv);
    }
}

//  LocalWords:  DetermIntegerRV SimDetermIntRVFactory IFRAME SRC px HREF
//  LocalWords:  steelblue DetermIntRVParmManager NRV SimDetermIntegerRV
//  LocalWords:  sdev
