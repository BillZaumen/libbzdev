package org.bzdev.devqsim.rv;
import java.util.TreeMap;
import java.util.Map;
import org.bzdev.devqsim.Simulation;
import org.bzdev.math.rv.DetermBooleanRV;
import org.bzdev.obnaming.annotations.FactoryParmManager;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;
import org.bzdev.obnaming.annotations.PrimitiveParm;
import org.bzdev.obnaming.annotations.KeyedPrimitiveParm;

/**
 * Abstract factory for {@link SimDetermBooleanRV}.
 * <P>
 * The parameters this factory provides are the same as the parameters
 * provided by its subclass {@link SimDetermBoolRVFactory}:
 * <P>
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/devqsim/rv/SimDetermBoolRVFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/devqsim/SimDetermBoolRVFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * @see SimDetermBoolRVFactory
 */
@FactoryParmManager(value="DetermBooleanRVParmManager",
		    tipResourceBundle="*.lpack.DetermRVTips",
		    labelResourceBundle = "*.lpack.DetermRVLabels",
		    stdFactory = "SimDetermBoolRVFactory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation")
public abstract class AbSimDetermBoolRVFactory
    <NRV extends SimDetermBooleanRV>
    extends SimBooleanRVFactory<DetermBooleanRV,NRV>
{

    @KeyedPrimitiveParm("values")
    TreeMap<Integer,Boolean>map = new TreeMap<>();

    @PrimitiveParm("finalValue")
    Boolean finalValue = null;


    DetermBooleanRVParmManager<NRV> pm;
    /**
     * Constructor.
     * @param sim the simulation
     */
    protected AbSimDetermBoolRVFactory(Simulation sim) {
	super(sim);
	pm = new DetermBooleanRVParmManager<NRV>(this);
	initParms(pm, AbSimDetermBoolRVFactory.class);
    }

    @Override
    public void initObject(NRV object) {
	super.initObject(object);
	boolean values[] = new boolean[map.size()];
	int i = 0;
	for (Boolean value: map.values()) {
	    values[i++] = value;
	}
	DetermBooleanRV rv = (finalValue == null)? new DetermBooleanRV(values):
	    new DetermBooleanRV(values, finalValue);
	setRV(object, rv);
    }
}

//  LocalWords:  DetermBooleanRV SimDetermBoolRVFactory IFRAME SRC px HREF
//  LocalWords:  steelblue DetermBoolRVParmManager NRV SimDetermBooleanRV
//  LocalWords:  sdev
