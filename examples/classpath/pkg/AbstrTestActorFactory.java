package pkg;
import org.bzdev.drama.*;
import org.bzdev.obnaming.*;
import org.bzdev.obnaming.annotations.*;


@FactoryParmManager(value = "PM",
		    labelResourceBundle = "*.TestActorLabels",
		    tipResourceBundle = "*.TestActorTips")
public abstract class AbstrTestActorFactory<Obj extends TestActor>
    extends AbstractActorFactory<Obj>
{
    @PrimitiveParm(value="parameter")
    int parameter = 0;

    PM<Obj> pm;
    protected AbstrTestActorFactory(DramaSimulation sim) {
	super(sim);
	pm = new PM<Obj>(this);
	initParms(pm, AbstrTestActorFactory.class);
    }

    @Override
    public void clear() {
	super.clear();
	pm.setDefaults(this);
    }
}
