import org.bzdev.math.rv.IntegerRandomVariable;
import java.util.*;

public class ATestObject2 extends ATestObject1 {
    private int value1;
    public void setValue1(int x) {
	value1 = x;
    }
    public int getValue1() {return value1;}

    private int value2;
    public void setValue2(int x) {
	value2 = x;
    }
    public int getValue2() {return value2;}

    private IntegerRandomVariable value3;

    public void setValue3(IntegerRandomVariable rv) {
	value3 = rv;
    }
    public int getValue3() {
	return value3.next();
    }

    private double value4 = 0.0;
    private double value5 = 0.0;
    public void setValue4(double value) {value4 = value;}
    public void setValue5(double value) {value5 = value;}
    public double getValue4() {return value4;}
    public double getValue5() {return value5;}

    Map<ATestObject2,Integer> keyedValue1Map = new HashMap<>();
    Map<ATestObject2,Integer> keyedValue2Map = new HashMap<>();
    Map<ATestObject2,Set<ATestObject2>> keyedValue3Map = new HashMap<>();

    public void setKeyedValue1(ATestObject2 key, int value) {
	keyedValue1Map.put(key, value);
    }
    public void setKeyedValue2(ATestObject2 key, int value) {
	keyedValue2Map.put(key, value);
    }

    public void addKeyedValue3(ATestObject2 key, ATestObject2 value) {
	Set<ATestObject2> set = keyedValue3Map.get(key);
	if (set == null) {
	    set = new HashSet<ATestObject2>();
	    keyedValue3Map.put(key, set);
	}
	set.add(value);
    }

    public Map<ATestObject2,Integer> keyedValue1s() {
	return Collections.unmodifiableMap(keyedValue1Map);
    }
    public Map<ATestObject2,Integer>keyedValue2s() {
	return Collections.unmodifiableMap(keyedValue2Map);
    }
    public Map<ATestObject2,Set<ATestObject2>> keyedValue3s() {
	return Collections.unmodifiableMap(keyedValue3Map);
    }


    private ATestObject2 peer = null;
    public void setPeer(ATestObject2 peer) {
	this.peer = peer;
    }
    public ATestObject2 getPeer()  {return peer;}


    private String label = null;
    public void setLabel(String label) {
	this.label = label;
    }
    public String getLabel() {return label;}

    private Set<ATestObject2> othersSet = new HashSet<>();
    public void addOther(ATestObject2 object) {
	othersSet.add(object);
    }

    public Set<ATestObject2> others() {
	return Collections.unmodifiableSet(othersSet);
    }


    public ATestObject2(ATestNamer namer, String name, boolean intern) {
	super(namer, name, intern);
    }

    private ATestObject2Factory.Option defaultOption = null;
    public void setDefaultOption(ATestObject2Factory.Option option) {
	defaultOption = option;
    }
    public ATestObject2Factory.Option getDefaultOption() {
	return defaultOption;
    }

    private Set<ATestObject2Factory.Option> options = new HashSet<>();
    public void setOptions(Set<ATestObject2Factory.Option> options) {
	this.options.clear();
	this.options.addAll(options);
    }
    public Set<ATestObject2Factory.Option> getOptions() {
	return Collections.unmodifiableSet(options);
    }
}
