import org.bzdev.obnaming.*;
import org.bzdev.obnaming.annotations.*;
import org.bzdev.math.rv.*;
import java.util.*;

@FactoryParmManager(value = "TestFactoryParmManager",
		    tipResourceBundle = "Tip",
		    labelResourceBundle = "Label")
public class TestFactory extends AbstractFactory<ATestObject2> {

    @CompoundParmType(tipResourceBundle = "ParmSetTips",
		      labelResourceBundle = "ParmSetLabels")
    public static class Data {
	@PrimitiveParm("u") double u = 3.0;
	@PrimitiveParm("v") double v = 4.0;
    }
    
    @KeyedCompoundParm("object") Map<Integer,Data> dataMap =
	new TreeMap<Integer,Data>();

    public TestFactory(ATestNamer namer) {
	super(namer);
	TestFactoryParmManager pm = new TestFactoryParmManager(this);
	initParms(pm, TestFactory.class);
    }

    public void clear() {
	dataMap.clear();
	super.clear();
    }

    protected ATestObject2 newObject(String name) {
	return new ATestObject2(getObjectNamer(), name, willIntern());
    }

    public void initObject(ATestObject2 object) {
	super.initObject(object);
	for (Map.Entry<Integer,Data> entry: dataMap.entrySet()) {
	    int key = entry.getKey();
	    Data value = entry.getValue();
	    System.out.format("For key = %d, u = %f, v = %f\n",
			      key, value.u, value.v);
	}
    }
}
