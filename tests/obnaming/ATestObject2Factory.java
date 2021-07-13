import org.bzdev.obnaming.*;
import org.bzdev.obnaming.annotations.*;
import org.bzdev.math.rv.*;
import java.util.*;

@FactoryParmManager(value = "ATestObject2ParmManager",
		    tipResourceBundle = "Tip",
		    labelResourceBundle = "Label",
		    docResourceBundle = "Doc")
public class ATestObject2Factory
    extends NamedObjectFactory
	    <ATestObject2Factory, ATestNamer, ATestObject, ATestObject2>
{
    @PrimitiveParm("value1")
	int value1 = 0;

    public int getValue1() {return value1;}


    @PrimitiveParm(value="value2", rvmode=true,
		   lowerBound="1", upperBound="25")
	IntegerRandomVariable value2 = new UniformIntegerRV(0, 15);

    public int getNextValue2() {return value2.next();}

    @PrimitiveParm(value="value2a", rvmode=false,
		   lowerBound="1", upperBound="25")
	IntegerRandomVariable value2a = new UniformIntegerRV(0, 15);

    @PrimitiveParm(value="value2b", rvmode=false,
		   lowerBound="1.0", upperBound="25.0")
	DoubleRandomVariable value2b = new FixedDoubleRV(10.0);


    @PrimitiveParm(value="value2c", rvmode=false,
		   lowerBound="1.0", upperBound="25.0")
	DoubleRandomVariable value2c = null;

    public void print2c() {
	System.out.println("value2c = "
			   + ((value2c == null)? "null": value2c.toString()));
    }

    @PrimitiveParm(value="value3", rvmode = true,
		   lowerBound="0", upperBound="101")
	IntegerRandomVariableRV<? extends IntegerRandomVariable> value3 =
	new FixedIntegerRVRV(new UniformIntegerRV(0, 30));

    @PrimitiveParm("value4")
	double value4 = 0.0;

    public double getValue4() {
	return value4;
    }

    @PrimitiveParm(value="value5", rvmode = true)
	DoubleRandomVariable value5 = new UniformDoubleRV(0.0, 5.0);

    @PrimitiveParm(value = "value5a")
	DoubleRandomVariable value5a = new UniformDoubleRV(0.0, 5.0);

    public double getNextValue5() {
	return value5.next();
    }
    
    public double getNextValue5a() {
	return value5a.next();
    }


    public static enum Option {OPT1, OPT2, OPT3}

    @PrimitiveParm("value6")
	Option defaultOption = Option.OPT1;

    public Option getValue6() {return defaultOption;}

    @PrimitiveParm("value7")
	EnumSet<Option> options = EnumSet.noneOf(Option.class);

    public void printValue7() {
	System.out.print("value7:");
	for (Option opt: options) {
	    System.out.print(" " + opt);
	}
	System.out.println();
    }

    @PrimitiveParm("value8")
	HashSet<Integer> intset = new HashSet<>();

    @PrimitiveParm("value9")
	Set<String> strset = new HashSet<>();

    @PrimitiveParm("peer")
	ATestObject2 peer = null;

    public String getPeerName() {
	return (peer == null)? "<null>": peer.getName();
    }

    @PrimitiveParm("label")
	String label = "<no label>";

    @PrimitiveParm("others")
	Set<ATestObject2> others = new HashSet<>();

    public void printOthers() {
	System.out.print("others: ");
	for (ATestObject2 other: others) {
	    System.out.print(" " + other.getName());
	}
	System.out.println();
    }

    @CompoundParmType(tipResourceBundle = "ParmSetTips",
		      labelResourceBundle = "ParmSetLabels",
		      docResourceBundle = "ParmSetDocs")
    public static class ParmSet {
	@PrimitiveParm("Value1")
	    int psvalue1 = 0;
	@PrimitiveParm(value="Value2", rvmode=true,
		       lowerBound="0", upperBound="103")
	    IntegerRandomVariable psvalue2 = new UniformIntegerRV(0, 10);

	@PrimitiveParm(value="Value2a", rvmode=false,
		       lowerBound="1", upperBound="25")
	    IntegerRandomVariable psvalue2a = new UniformIntegerRV(0, 15);

	@PrimitiveParm(value="Value2b", rvmode=false,
		       lowerBound="1.0", upperBound="25.0")
	    DoubleRandomVariable psvalue2b = new FixedDoubleRV(10.0);

	@PrimitiveParm(value="Value2c", rvmode=false,
		       lowerBound="1.0", upperBound="25.0")
	    DoubleRandomVariable psvalue2c = null;


	@PrimitiveParm(value="Value3", rvmode = true,
		       lowerBound="0", upperBound="101")
	    IntegerRandomVariableRV<? extends IntegerRandomVariable> psvalue3 =
	    new FixedIntegerRVRV(new UniformIntegerRV(0, 30));

	@PrimitiveParm("Value4")
	    double psvalue4 = 0.0;

	@PrimitiveParm(value="Value5", rvmode = true)
	    DoubleRandomVariable psvalue5 = new UniformDoubleRV(0.0, 5.0);

	@PrimitiveParm(value="Value5a")
	    DoubleRandomVariable psvalue5a = new UniformDoubleRV(0.0, 5.0);

	@PrimitiveParm("Value6")
	    Option defaultOption = Option.OPT1;

	@PrimitiveParm("Value7")
	    EnumSet<Option> options = EnumSet.noneOf(Option.class);

	@PrimitiveParm(value="Peer")
	    ATestObject2 peer = null;

	@PrimitiveParm("Label")
	    String label = "<no label>";

	@PrimitiveParm("Others")
	    Set<ATestObject2>neighbors = new HashSet<>();

	public void print() {
	    System.out.println("    Value1 = " + psvalue1);
	    System.out.println("    Value2 = " + psvalue2.next());
	    System.out.println("    Value2c = "
			       + ((psvalue2c == null)? "null":
				  psvalue2c.toString()));
	    System.out.println("    Value3 = " + psvalue3.next().next());
	    System.out.println("    Value4 = " + psvalue4);
	    System.out.println("    Value5 = " + psvalue5.next());
	    System.out.println("    Value5a = " + psvalue5a.next());
	    System.out.println("    Value6 = " + defaultOption.toString());
	    System.out.println("    Value7:");
	    for (Option option: options) {
		System.out.println("\t" + option.toString());
	    }
	    System.out.println("    Peer = "
			       + ((peer == null)? "null":
				  peer.getName().toString()));
	    System.out.println("    Label = " + label);
	    System.out.println("    Others:");
	    for (ATestObject2 nbr: neighbors) {
		System.out.println("\t" + nbr.getName());
	    }
	}
    }

    @KeyedCompoundParm("keyed")
	Map<ATestObject2,ParmSet> map = new HashMap<>();

    public void printKeyedValue7(ATestObject2 key) {
	System.out.println("Options (Value 7) for " + key.getName() + ":");
	ParmSet parmset = map.get(key);
	if (parmset == null || parmset.options == null) {
	    System.out.println("    [no Value 7 options]");
	} else {
	    for (Option opt: parmset.options) {
		System.out.println("    " + opt);
	    }
	}
    }

    @KeyedCompoundParm("optkeyed")
	Map<Option,ParmSet> optmap = new HashMap<>();

    @KeyedCompoundParm("strkeyed")
	Map<String,ParmSet> strmap = new HashMap<>();

    @KeyedCompoundParm("intkeyed")
	Map<Integer,ParmSet> intmap = new HashMap<>();

    public void printIntKeyed() {
	for(Map.Entry<Integer,ParmSet> entry: intmap.entrySet()) {
	    Integer index = entry.getKey();
	    ParmSet value = entry.getValue();
	    System.out.format("intkey[%d].Value1 = %d, ...\n",
			      index, value.psvalue1);
	}
    }

    @CompoundParm("unkeyed")
	ParmSet parmset = new ParmSet();

    @KeyedPrimitiveParm(value="kprim1")
	Map<ATestObject2, Integer> kprimMap1 = new HashMap<>();

    @KeyedPrimitiveParm(value="kprim2", rvmode=true,
			lowerBound="0", upperBound="103")
	Map<ATestObject2,IntegerRandomVariable> kprimMap2 = new HashMap<>();

    @KeyedPrimitiveParm(value="kprim3", rvmode=true,
			lowerBound="0", upperBound="101")
	Map<ATestObject2, IntegerRandomVariableRV<IntegerRandomVariable>>
	kprimMap3 = new HashMap<>();

    @KeyedPrimitiveParm(value="kprim4")
	Map<ATestObject2,Double> kprimMap4 = new HashMap<>();

    @KeyedPrimitiveParm(value="kprim5", rvmode = true)
	Map<ATestObject2, DoubleRandomVariable> kprimMap5 = new HashMap<>();

    @KeyedPrimitiveParm(value="kprim5a")
	Map<ATestObject2, DoubleRandomVariable> kprimMap5a = new HashMap<>();


    @KeyedPrimitiveParm(value="kprim6")
	Map<ATestObject2,Option> kprimMap6 = new HashMap<>();

    @KeyedPrimitiveParm(value="kprim7")
	Map<ATestObject2,ATestObject2> kprimMap7 = new HashMap<>();

    @KeyedPrimitiveParm(value="kprim8")
	Map<ATestObject2,String> kprimMap8 = new HashMap<>();

    @KeyedPrimitiveParm(value="kprim9")
	Map<ATestObject2,HashSet<ATestObject2>> kprimMap9 = new HashMap<>();

    Map<ATestObject2,HashSet<ATestObject2>> getKprimMap9() {
	return kprimMap9;
    }


    @KeyedPrimitiveParm(value="kprimInt1")
	Map<Integer, Integer> kprimIntMap1 = new HashMap<>();

    @KeyedPrimitiveParm(value="kprimInt2", rvmode=true,
			lowerBound="0", upperBound="103")
	Map<Integer,IntegerRandomVariable> kprimIntMap2 = new HashMap<>();

    @KeyedPrimitiveParm(value="kprimInt3", rvmode=true,
			lowerBound="0", upperBound="101")
	Map<Integer, IntegerRandomVariableRV<IntegerRandomVariable>>
	kprimIntMap3 = new HashMap<>();

    @KeyedPrimitiveParm(value="kprimInt4")
	Map<Integer,Double> kprimIntMap4 = new HashMap<>();

    @KeyedPrimitiveParm(value="kprimInt5", rvmode = true)
	Map<Integer, DoubleRandomVariable> kprimIntMap5 = new HashMap<>();

    @KeyedPrimitiveParm(value="kprimInt5a")
	Map<Integer, DoubleRandomVariable> kprimIntMap5a = new HashMap<>();

    @KeyedPrimitiveParm(value="kprimInt6")
	Map<Integer,Option> kprimIntMap6 = new HashMap<>();

    @KeyedPrimitiveParm(value="kprimInt7")
	Map<Integer,ATestObject2> kprimIntMap7 = new HashMap<>();

    @KeyedPrimitiveParm(value="kprimInt8")
	Map<Integer,String> kprimIntMap8 = new HashMap<>();

    @KeyedPrimitiveParm(value="kprimInt9")
	Map<Integer,HashSet<ATestObject2>> kprimIntMap9 = new HashMap<>();

    @KeyedPrimitiveParm(value="kprimStr1")
	Map<String, Integer> kprimStrMap1 = new HashMap<>();

    @KeyedPrimitiveParm(value="kprimStr2", rvmode=true,
			lowerBound="0", upperBound="103")
	Map<String,IntegerRandomVariable> kprimStrMap2 = new HashMap<>();

    @KeyedPrimitiveParm(value="kprimStr3", rvmode=true,
			lowerBound="0", upperBound="101")
	Map<String, IntegerRandomVariableRV<IntegerRandomVariable>>
	kprimStrMap3 = new HashMap<>();

    @KeyedPrimitiveParm(value="kprimStr4")
	Map<String,Double> kprimStrMap4 = new HashMap<>();

    @KeyedPrimitiveParm(value="kprimStr5", rvmode = true)
	Map<String, DoubleRandomVariable> kprimStrMap5 = new HashMap<>();

    @KeyedPrimitiveParm(value="kprimStr5a")
	Map<String, DoubleRandomVariable> kprimStrMap5a = new HashMap<>();

    @KeyedPrimitiveParm(value="kprimStr6")
	Map<String,Option> kprimStrMap6 = new HashMap<>();

    @KeyedPrimitiveParm(value="kprimStr7")
	Map<String,ATestObject2> kprimStrMap7 = new HashMap<>();

    @KeyedPrimitiveParm(value="kprimStr8")
	Map<String,String> kprimStrMap8 = new HashMap<>();

    @KeyedPrimitiveParm(value="kprimStr9")
	Map<String,HashSet<ATestObject2>> kprimStrMap9 = new HashMap<>();


    @KeyedPrimitiveParm(value="kprimOpt1")
	Map<Option, Integer> kprimOptMap1 = new HashMap<>();

    @KeyedPrimitiveParm(value="kprimOpt2", rvmode=true,
			lowerBound="0", upperBound="103")
	Map<Option,IntegerRandomVariable> kprimOptMap2 = new HashMap<>();

    @KeyedPrimitiveParm(value="kprimOpt3", rvmode=true,
			lowerBound="0", upperBound="101")
	Map<Option, IntegerRandomVariableRV<IntegerRandomVariable>>
	kprimOptMap3 = new HashMap<>();

    @KeyedPrimitiveParm(value="kprimOpt4")
	Map<Option,Double> kprimOptMap4 = new HashMap<>();

    @KeyedPrimitiveParm(value="kprimOpt5", rvmode = true)
	Map<Option, DoubleRandomVariable> kprimOptMap5 = new HashMap<>();

    @KeyedPrimitiveParm(value="kprimOpt5a")
	Map<Option, DoubleRandomVariable> kprimOptMap5a = new HashMap<>();

    @KeyedPrimitiveParm(value="kprimOpt6")
	Map<Option,Option> kprimOptMap6 = new HashMap<>();

    @KeyedPrimitiveParm(value="kprimOpt7")
	Map<Option,ATestObject2> kprimOptMap7 = new HashMap<>();

    @KeyedPrimitiveParm(value="kprimOpt8")
	Map<Option,String> kprimOptMap8 = new HashMap<>();

    @KeyedPrimitiveParm(value="kprimOpt9")
	Map<Option,HashSet<ATestObject2>> kprimOptMap9 = new HashMap<>();

    @KeyedPrimitiveParm(value="kprimOptSet")
	Map<Option, EnumSet<Option>> kprimOptSet = new HashMap<>();

    public void printKprims() {

	System.out.println("kprim1:");
	for (Map.Entry<ATestObject2,Integer> entry: kprimMap1.entrySet()) {
	    System.out.println("    " + entry.getKey().getName()
			       + " " + entry.getValue());
	}
	System.out.println("kprim2:");
	for (Map.Entry<ATestObject2,IntegerRandomVariable> entry:
		 kprimMap2.entrySet()) {
	    int count = 0; int sum = 0;
	    IntegerRandomVariable rv = entry.getValue();
	    for (int i = 0; i < 10000; i++) {
		sum += rv.next();
		count++;
	    }
	    System.out.println("    " + entry.getKey().getName()
			       + " " + (sum/count));
	}
	System.out.println("kprim3:");
	for (Map.Entry<ATestObject2,
		 IntegerRandomVariableRV<IntegerRandomVariable>> entry:
		 kprimMap3.entrySet()) {
	    IntegerRandomVariableRV<IntegerRandomVariable> rvrv =
		entry.getValue();
	    IntegerRandomVariable rv = rvrv.next();
	    int count1 = 0; int sum1 = 0;
	    for (int i = 0; i < 10000; i++) {
		sum1 += rv.next();
		count1++;
	    }
	    rv = rvrv.next();
	    int count2 = 0; int sum2 = 0;
	    for (int i = 0; i < 10000; i++) {
		sum2 += rv.next();
		count2++;
	    }
	    System.out.println("    " + entry.getKey().getName()
			       + " " + (sum1/count1) + " " + (sum2 / count2));
	}
	System.out.println("kprim4:");
	for (Map.Entry<ATestObject2,Double> entry: kprimMap4.entrySet()) {
	    System.out.println("    " + entry.getKey().getName()
			       + " " + entry.getValue());
	}

	System.out.println("kprim5:");
	for (Map.Entry<ATestObject2,DoubleRandomVariable> entry:
		 kprimMap5.entrySet()) {
	    int count = 0; double sum = 0.0;
	    DoubleRandomVariable rv = entry.getValue();
	    for (int i = 0; i < 10000; i++) {
		sum += rv.next();
		count++;
	    }
	    System.out.println("    " + entry.getKey().getName()
			       + " " + (sum/count));
	}

	System.out.println("kprim5a:");
	for (Map.Entry<ATestObject2,DoubleRandomVariable> entry:
		 kprimMap5a.entrySet()) {
	    int count = 0; double sum = 0.0;
	    DoubleRandomVariable rv = entry.getValue();
	    for (int i = 0; i < 10000; i++) {
		sum += rv.next();
		count++;
	    }
	    System.out.println("    " + entry.getKey().getName()
			       + " " + (sum/count));
	}


	System.out.println("kprim6:");
	for (Map.Entry<ATestObject2,Option> entry: kprimMap6.entrySet()) {
	    System.out.println("    " + entry.getKey().getName()
			       + " " + entry.getValue());
	}

	System.out.println("kprim7:");
	for (Map.Entry<ATestObject2,ATestObject2> entry: kprimMap7.entrySet()) {
	    System.out.println("    " + entry.getKey().getName()
			       + " " + entry.getValue().getName());
	}

	System.out.println("kprim8:");
	for (Map.Entry<ATestObject2,String> entry: kprimMap8.entrySet()) {
	    System.out.println("    " + entry.getKey().getName()
			       + " " + entry.getValue());
	}

	System.out.println("kprim9:");
	for (Map.Entry<ATestObject2,HashSet<ATestObject2>>
		 entry: kprimMap9.entrySet()) {
	    System.out.println("    " + entry.getKey().getName());
	    for (ATestObject2 value: entry.getValue()) {
		System.out.println("        " + value.getName());
	    }
	}

	// int case
	System.out.println("kprimInt1:");
	for (Map.Entry<Integer,Integer> entry: kprimIntMap1.entrySet()) {
	    System.out.println("    " + entry.getKey()
			       + " " + entry.getValue());
	}
	System.out.println("kprimInt2:");
	for (Map.Entry<Integer,IntegerRandomVariable> entry:
		 kprimIntMap2.entrySet()) {
	    int count = 0; int sum = 0;
	    IntegerRandomVariable rv = entry.getValue();
	    for (int i = 0; i < 10000; i++) {
		sum += rv.next();
		count++;
	    }
	    System.out.println("    " + entry.getKey()
			       + " " + (sum/count));
	}
	System.out.println("kprimInt3:");
	for (Map.Entry<Integer,
		 IntegerRandomVariableRV<IntegerRandomVariable>> entry:
		 kprimIntMap3.entrySet()) {
	    IntegerRandomVariableRV<IntegerRandomVariable> rvrv =
		entry.getValue();
	    IntegerRandomVariable rv = rvrv.next();
	    int count1 = 0; int sum1 = 0;
	    for (int i = 0; i < 10000; i++) {
		sum1 += rv.next();
		count1++;
	    }
	    rv = rvrv.next();
	    int count2 = 0; int sum2 = 0;
	    for (int i = 0; i < 10000; i++) {
		sum2 += rv.next();
		count2++;
	    }
	    System.out.println("    " + entry.getKey()
			       + " " + (sum1/count1) + " " + (sum2 / count2));
	}
	System.out.println("kprimInt4:");
	for (Map.Entry<Integer,Double> entry: kprimIntMap4.entrySet()) {
	    System.out.println("    " + entry.getKey()
			       + " " + entry.getValue());
	}

	System.out.println("kprimInt5:");
	for (Map.Entry<Integer,DoubleRandomVariable> entry:
		 kprimIntMap5.entrySet()) {
	    int count = 0; double sum = 0.0;
	    DoubleRandomVariable rv = entry.getValue();
	    for (int i = 0; i < 10000; i++) {
		sum += rv.next();
		count++;
	    }
	    System.out.println("    " + entry.getKey()
			       + " " + (sum/count));
	}

	System.out.println("kprimInt5a:");
	for (Map.Entry<Integer,DoubleRandomVariable> entry:
		 kprimIntMap5a.entrySet()) {
	    int count = 0; double sum = 0;
	    DoubleRandomVariable rv = entry.getValue();
	    for (int i = 0; i < 10000; i++) {
		sum += rv.next();
		count++;
	    }
	    System.out.println("    " + entry.getKey()
			       + " " + (sum/count));
	}

	System.out.println("kprimInt6:");
	for (Map.Entry<Integer,Option> entry: kprimIntMap6.entrySet()) {
	    System.out.println("    " + entry.getKey()
			       + " " + entry.getValue());
	}

	System.out.println("kprimInt7:");
	for (Map.Entry<Integer,ATestObject2> entry:
		 kprimIntMap7.entrySet()) {
	    System.out.println("    " + entry.getKey()
			       + " " + entry.getValue().getName());
	}

	System.out.println("kprimInt8:");
	for (Map.Entry<Integer,String> entry: kprimIntMap8.entrySet()) {
	    System.out.println("    " + entry.getKey()
			       + " " + entry.getValue());
	}

	System.out.println("kprimInt9:");
	for (Map.Entry<Integer,HashSet<ATestObject2>> entry:
		 kprimIntMap9.entrySet()) {
	    System.out.println("    " + entry.getKey());
	    for (ATestObject2 value: entry.getValue()) {
		System.out.println("        " + value.getName());
	    }
	}

	// string case
	System.out.println("kprimStr1:");
	for (Map.Entry<String,Integer> entry: kprimStrMap1.entrySet()) {
	    System.out.println("    " + entry.getKey()
			       + " " + entry.getValue());
	}
	System.out.println("kprimStr2:");
	for (Map.Entry<String,IntegerRandomVariable> entry:
		 kprimStrMap2.entrySet()) {
	    int count = 0; int sum = 0;
	    IntegerRandomVariable rv = entry.getValue();
	    for (int i = 0; i < 10000; i++) {
		sum += rv.next();
		count++;
	    }
	    System.out.println("    " + entry.getKey()
			       + " " + (sum/count));
	}
	System.out.println("kprimStr3:");
	for (Map.Entry<String,
		 IntegerRandomVariableRV<IntegerRandomVariable>> entry:
		 kprimStrMap3.entrySet()) {
	    IntegerRandomVariableRV<IntegerRandomVariable> rvrv =
		entry.getValue();
	    IntegerRandomVariable rv = rvrv.next();
	    int count1 = 0; int sum1 = 0;
	    for (int i = 0; i < 10000; i++) {
		sum1 += rv.next();
		count1++;
	    }
	    rv = rvrv.next();
	    int count2 = 0; int sum2 = 0;
	    for (int i = 0; i < 10000; i++) {
		sum2 += rv.next();
		count2++;
	    }
	    System.out.println("    " + entry.getKey()
			       + " " + (sum1/count1) + " " + (sum2 / count2));
	}
	System.out.println("kprimStr4:");
	for (Map.Entry<String,Double> entry: kprimStrMap4.entrySet()) {
	    System.out.println("    " + entry.getKey()
			       + " " + entry.getValue());
	}

	System.out.println("kprimStr5:");
	for (Map.Entry<String,DoubleRandomVariable> entry:
		 kprimStrMap5.entrySet()) {
	    int count = 0; double sum = 0;
	    DoubleRandomVariable rv = entry.getValue();
	    for (int i = 0; i < 10000; i++) {
		sum += rv.next();
		count++;
	    }
	    System.out.println("    " + entry.getKey()
			       + " " + (sum/count));
	}

	System.out.println("kprimStr5a:");
	for (Map.Entry<String,DoubleRandomVariable> entry:
		 kprimStrMap5a.entrySet()) {
	    int count = 0; double sum = 0;
	    DoubleRandomVariable rv = entry.getValue();
	    for (int i = 0; i < 10000; i++) {
		sum += rv.next();
		count++;
	    }
	    System.out.println("    " + entry.getKey()
			       + " " + (sum/count));
	}

	System.out.println("kprimStr6:");
	for (Map.Entry<String,Option> entry: kprimStrMap6.entrySet()) {
	    System.out.println("    " + entry.getKey()
			       + " " + entry.getValue());
	}

	System.out.println("kprimStr7:");
	for (Map.Entry<String,ATestObject2> entry:
		 kprimStrMap7.entrySet()) {
	    System.out.println("    " + entry.getKey()
			       + " " + entry.getValue().getName());
	}

	System.out.println("kprimStr8:");
	for (Map.Entry<String,String> entry: kprimStrMap8.entrySet()) {
	    System.out.println("    " + entry.getKey()
			       + " " + entry.getValue());
	}

	System.out.println("kprimStr9:");
	for (Map.Entry<String,HashSet<ATestObject2>> entry:
		 kprimStrMap9.entrySet()) {
	    System.out.println("    " + entry.getKey());
	    for (ATestObject2 value: entry.getValue()) {
		System.out.println("        " + value.getName());
	    }
	}

	// opt case
	System.out.println("kprimOpt1:");
	for (Map.Entry<Option,Integer> entry: kprimOptMap1.entrySet()) {
	    System.out.println("    " + entry.getKey()
			       + " " + entry.getValue());
	}
	System.out.println("kprimOpt2:");
	for (Map.Entry<Option,IntegerRandomVariable> entry:
		 kprimOptMap2.entrySet()) {
	    int count = 0; int sum = 0;
	    IntegerRandomVariable rv = entry.getValue();
	    for (int i = 0; i < 10000; i++) {
		sum += rv.next();
		count++;
	    }
	    System.out.println("    " + entry.getKey()
			       + " " + (sum/count));
	}
	System.out.println("kprimOpt3:");
	for (Map.Entry<Option,
		 IntegerRandomVariableRV<IntegerRandomVariable>> entry:
		 kprimOptMap3.entrySet()) {
	    IntegerRandomVariableRV<IntegerRandomVariable> rvrv =
		entry.getValue();
	    IntegerRandomVariable rv = rvrv.next();
	    int count1 = 0; int sum1 = 0;
	    for (int i = 0; i < 10000; i++) {
		sum1 += rv.next();
		count1++;
	    }
	    rv = rvrv.next();
	    int count2 = 0; int sum2 = 0;
	    for (int i = 0; i < 10000; i++) {
		sum2 += rv.next();
		count2++;
	    }
	    System.out.println("    " + entry.getKey()
			       + " " + (sum1/count1) + " " + (sum2 / count2));
	}
	System.out.println("kprimOpt4:");
	for (Map.Entry<Option,Double> entry: kprimOptMap4.entrySet()) {
	    System.out.println("    " + entry.getKey()
			       + " " + entry.getValue());
	}

	System.out.println("kprimOpt5:");
	for (Map.Entry<Option,DoubleRandomVariable> entry:
		 kprimOptMap5.entrySet()) {
	    int count = 0; double sum = 0;
	    DoubleRandomVariable rv = entry.getValue();
	    for (int i = 0; i < 10000; i++) {
		sum += rv.next();
		count++;
	    }
	    System.out.println("    " + entry.getKey()
			       + " " + (sum/count));
	}

	System.out.println("kprimOpt5a:");
	for (Map.Entry<Option,DoubleRandomVariable> entry:
		 kprimOptMap5a.entrySet()) {
	    int count = 0; double sum = 0;
	    DoubleRandomVariable rv = entry.getValue();
	    for (int i = 0; i < 10000; i++) {
		sum += rv.next();
		count++;
	    }
	    System.out.println("    " + entry.getKey()
			       + " " + (sum/count));
	}

	System.out.println("kprimOpt6:");
	for (Map.Entry<Option,Option> entry: kprimOptMap6.entrySet()) {
	    System.out.println("    " + entry.getKey()
			       + " " + entry.getValue());
	}

	System.out.println("kprimOpt7:");
	for (Map.Entry<Option,ATestObject2> entry:
		 kprimOptMap7.entrySet()) {
	    System.out.println("    " + entry.getKey()
			       + " " + entry.getValue().getName());
	}

	System.out.println("kprimOpt8:");
	for (Map.Entry<Option,String> entry: kprimOptMap8.entrySet()) {
	    System.out.println("    " + entry.getKey()
			       + " " + entry.getValue());
	}

	System.out.println("kprimOpt9:");
	for (Map.Entry<Option,HashSet<ATestObject2>> entry:
		 kprimOptMap9.entrySet()) {
	    System.out.println("    " + entry.getKey());
	    for (ATestObject2 value: entry.getValue()) {
		System.out.println("        " + value.getName());
	    }
	}
	System.out.println("kprimOptSet:");
	for (Map.Entry<Option,EnumSet<Option>> entry:
		 kprimOptSet.entrySet()) {
	    System.out.println("    " + entry.getKey());
	    for (Option value: entry.getValue()) {
		System.out.println("        " + value);
	    }
	}
    }



    public void printUnkeyed() {
	System.out.println("unkeyed:");
	parmset.print();
    }

    public void printKeyed() {
	System.out.println("keyed:");
	for (Map.Entry<ATestObject2,ParmSet> entry: map.entrySet()) {
	    System.out.println("  " + entry.getKey().getName());
	    entry.getValue().print();
	}
	System.out.println("optkeyed:");
	for (Map.Entry<Option,ParmSet> entry: optmap.entrySet()) {
	    System.out.println("  " + entry.getKey());
	    entry.getValue().print();
	}
	System.out.println("strkeyed:");
	for (Map.Entry<String,ParmSet> entry: strmap.entrySet()) {
	    System.out.println("  " + entry.getKey());
	    entry.getValue().print();
	}
	System.out.println("intkeyed:");
	for (Map.Entry<Integer,ParmSet> entry: intmap.entrySet()) {
	    System.out.println("  " + entry.getKey());
	    entry.getValue().print();
	}
    }

    public void printIntkeyedValue4() {
	System.out.println("intkeyed.Value4:");
	for (Map.Entry<Integer,ParmSet> entry: intmap.entrySet()) {
	    System.out.println("  " + entry.getKey()
			       + ": " + entry.getValue().psvalue4);
	}
    }

    public void printIntkeyedOthers() {
	System.out.println("intkeyed.Others:");
	for (Map.Entry<Integer,ParmSet> entry: intmap.entrySet()) {
	    System.out.print("  " + entry.getKey()
			     + ": ");
	    boolean first = true;
	    for (ATestObject2 nbr: entry.getValue().neighbors) {
		System.out.print(((first)? "": ",") + nbr.getName());
		first = false;
	    }
	    System.out.println();
	}
    }

    public void testValue2() throws Exception {
	for (int i = 0; i < 1000; i++) {
	    int ix = value2.next();
	    if (ix < 1 || ix > 30) {
		throw new Exception("value2().next out of range: " + ix);
	    }
	}
    }

    public void printValue8() {
	System.out.println("intset:");
	for (int i: intset) {
	    System.out.println("    " + i);
	}
    }

    public void printValue9() {
	System.out.println("strset:");
	for (String s: strset) {
	    System.out.println("    " + s);
	}
    }

    ATestNamer namer;
    ATestObject2ParmManager pm;

    public ATestObject2Factory(ATestNamer namer) {
	super(namer);
	this.namer = namer;
	// addDefault(ATestObject2Factory.class, "defaults");
	pm = new ATestObject2ParmManager(this);
	initParms(pm, ATestObject2Factory.class);
	// pm.setDefaults(this);
    }

    public void clear() {
	pm.setDefaults(this);
	super.clear();
    }

    protected ATestObject2 newObject(String name) {
	return new ATestObject2(getObjectNamer(), name, willIntern());
    }

    protected void initObject(ATestObject2 object) {
	object.setValue1(value1);
	object.setValue2(value2.next());
	object.setValue3(value3.next());
	object.setValue4(value4);
	object.setValue5(value5.next());
	object.setDefaultOption(defaultOption);
	object.setOptions(options);
	object.setPeer(peer);
	object.setLabel(label);
	for (ATestObject2 other: others) {
	    object.addOther(other);
	}
	for (Map.Entry<ATestObject2,ParmSet>entry: map.entrySet()) {
	    ATestObject2 key = entry.getKey();
	    ParmSet value = entry.getValue();
	    object.setKeyedValue1(key, value.psvalue1);
	    object.setKeyedValue2(key, value.psvalue2.next());
	    for (ATestObject2 v: value.neighbors) {
		object.addKeyedValue3(key, v);
	    }
	}
    }
}
