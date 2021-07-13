import org.bzdev.devqsim.*;
import org.bzdev.drama.*;

public class FactoryTest {
    public static void main(String argv[]) throws Exception {

	DramaSimulation sim = new DramaSimulation();

	DoubleConditionFactory dcf = new DoubleConditionFactory(sim);
	dcf.set("initialValue", 1.0);
	
	IntegerConditionFactory icf = new IntegerConditionFactory(sim);
	icf.set("initialValue", 2);

	LongConditionFactory lcf = new LongConditionFactory(sim);
	lcf.set("initialValue", 3);
	
	BooleanConditionFactory bcf = new BooleanConditionFactory(sim);
	bcf.set("initialValue", true);

	DoubleCondition dc = dcf.createObject("doubleCondition");
	IntegerCondition ic = icf.createObject("integerCondition");
	LongCondition lc = lcf.createObject("longCondition");
	BooleanCondition bc = bcf.createObject("booleanCondition");

	System.out.println("dc value = " + dc.getValue());
	System.out.println("ic value = " + ic.getValue());
	System.out.println("lc value = " + lc.getValue());
	System.out.println("bc value = " + bc.getValue());

	DomainFactory df = new DomainFactory(sim);

	df.set("priority", 1);
	df.add("condition", dc);
	df.add("condition", ic);
	df.add("condition", lc);
	df.add("condition", bc);

	Domain d = df.createObject("domain");

	System.out.println("Conditions for domain:");
	for (Condition condition: d.conditionSet()) {
	    System.out.println("    " + condition.getName());
	}

	TestActorFactory taf = new TestActorFactory(sim);
	taf.set("domain", d, true);
	TestActor ta = taf.createObject("testActor");

	DomainMemberFactory dmf = new DomainMemberFactory(sim);

	dmf.set("domain", d, true);
	DomainMember dm = dmf.createObject("dm");

	taf.unset("domain", d);

	taf.set("domainMember", dm);

	TestActor ta2 = taf.createObject("ta2");
	System.exit(0);
    }
}
