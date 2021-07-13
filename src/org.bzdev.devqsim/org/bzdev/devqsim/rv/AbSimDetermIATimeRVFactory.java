package org.bzdev.devqsim.rv;
import java.util.TreeMap;
import java.util.Map;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.DetermIATimeRV;
import org.bzdev.obnaming.annotations.FactoryParmManager;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;
import org.bzdev.obnaming.annotations.PrimitiveParm;
import org.bzdev.obnaming.annotations.KeyedPrimitiveParm;

/**
 * Abstract factory for {@link SimDetermIATimeRV}.
 * <P>
 * The parameters this factory provides are the same as the parameters
 * provided by its subclass {@link SimDetermIATimeRVFactory}:
 * <P>
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/rv/SimDetermIATimeRVFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/SimDetermIATimeRVFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * @see SimDetermIATimeRVFactory
 */
@FactoryParmManager(value="DetermIATimeRVParmManager",
		    tipResourceBundle="*.lpack.DetermRVTips",
		    labelResourceBundle = "*.lpack.DetermRVLabels",
		    stdFactory = "SimDetermIATimeRVFactory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation")
public abstract class AbSimDetermIATimeRVFactory
    <NRV extends SimDetermIATimeRV>
    extends SimIATimeRVFactory<DetermIATimeRV,NRV>
{

    @KeyedPrimitiveParm("values")
    TreeMap<Integer,Long>map = new TreeMap<>();

    @PrimitiveParm("finalValue")
    Long finalValue = null;


    DetermIATimeRVParmManager<NRV> pm;
    /**
     * Constructor.
     * @param sim the simulation
     */
    protected AbSimDetermIATimeRVFactory(Simulation sim) {
	super(sim);
	pm = new DetermIATimeRVParmManager<NRV>(this);
	initParms(pm, AbSimDetermIATimeRVFactory.class);
    }

    @Override
    public void initObject(NRV object) {
	super.initObject(object);
	long values[] = new long[map.size()];
	int i = 0;
	for (Long value: map.values()) {
	    values[i++] = value;
	}
	DetermIATimeRV rv = (finalValue == null)? new DetermIATimeRV(values):
	    new DetermIATimeRV(values, finalValue);
	setRV(object, rv);
    }
}

//  LocalWords:  DetermIATimeRV SimDetermIATimeRVFactory IFRAME SRC px HREF
//  LocalWords:  steelblue DetermIATimeRVParmManager NRV SimDetermIATimeRV
//  LocalWords:  sdev
