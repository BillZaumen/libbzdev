import org.bzdev.obnaming.*;
import org.bzdev.obnaming.annotations.*;
import org.bzdev.math.rv.*;
import java.util.*;


@FactoryParmManager(value = "AbstractParmManager",
		    tipResourceBundle = "Tip",
		    labelResourceBundle = "Label")
abstract public class AbstractFactory<Obj extends ATestObject2>
    extends NamedObjectFactory
	    <AbstractFactory<Obj>, ATestNamer, ATestObject, Obj>
{

    @CompoundParmType(tipResourceBundle = "ParmSetTips",
		      labelResourceBundle = "ParmSetLabels")
    public static class Data {
	@PrimitiveParm("x") double x = 1.0;
	@PrimitiveParm("y") double y = 2.0;
    }

    @KeyedCompoundParm("object") Map<Integer,Data> dataMap =
	new TreeMap<Integer,Data>();

    public AbstractFactory(ATestNamer namer) {
	super(namer);
	AbstractParmManager<Obj> pm = new AbstractParmManager<Obj>(this);
	initParms(pm, AbstractFactory.class);
    }

    public void clear() {
	dataMap.clear();
	super.clear();
    }

    public void initObject(Obj object) {
	super.initObject(object);
	for (Map.Entry<Integer,Data> entry: dataMap.entrySet()) {
	    int key = entry.getKey();
	    Data value = entry.getValue();
	    System.out.format("For key = %d, x = %f, y = %f\n",
			      key, value.x, value.y);
	}
    }
}
