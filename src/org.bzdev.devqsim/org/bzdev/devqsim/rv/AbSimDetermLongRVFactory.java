package org.bzdev.devqsim.rv;
import java.util.TreeMap;
import java.util.Map;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.DetermLongRV;
import org.bzdev.obnaming.annotations.FactoryParmManager;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;
import org.bzdev.obnaming.annotations.PrimitiveParm;
import org.bzdev.obnaming.annotations.KeyedPrimitiveParm;

/**
 * Abstract factory for {@link SimDetermLongRV}.
 * <P>
 * The parameters this factory provides are the same as the parameters
 * provided by its subclass {@link SimDetermLongRVFactory}:
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/rv/SimDetermLongRVFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/SimDetermLongRVFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * @see SimDetermLongRVFactory
 */
@FactoryParmManager(value="DetermLongRVParmManager",
		    tipResourceBundle="*.lpack.DetermRVTips",
		    labelResourceBundle = "*.lpack.DetermRVLabels",
		    stdFactory = "SimDetermLongRVFactory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation")
public abstract class AbSimDetermLongRVFactory
    <NRV extends SimDetermLongRV>
    extends SimLongRVFactory<DetermLongRV,NRV>
{

    @KeyedPrimitiveParm("values")
    TreeMap<Integer,Long>map = new TreeMap<>();

    @PrimitiveParm("finalValue")
    Long finalValue = null;


    DetermLongRVParmManager<NRV> pm;
    /**
     * Constructor.
     * @param sim the simulation
     */
    protected AbSimDetermLongRVFactory(Simulation sim) {
	super(sim);
	pm = new DetermLongRVParmManager<NRV>(this);
	initParms(pm, AbSimDetermLongRVFactory.class);
    }

    @Override
    public void initObject(NRV object) {
	super.initObject(object);
	long values[] = new long[map.size()];
	int i = 0;
	for (long value: map.values()) {
	    values[i++] = value;
	}
	DetermLongRV rv = (finalValue == null)? new DetermLongRV(values):
	    new DetermLongRV(values, finalValue);
	setRV(object, rv);
    }
}

//  LocalWords:  DetermLongRV SimDetermLongRVFactory IFRAME SRC px HREF
//  LocalWords:  steelblue DetermLongRVParmManager NRV SimDetermLongRV
//  LocalWords:  sdev
