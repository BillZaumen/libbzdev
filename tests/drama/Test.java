import org.bzdev.devqsim.*;
import org.bzdev.drama.*;
import org.bzdev.drama.common.ConditionMode;

class OurBooleanCondition extends BooleanCondition {
    public OurBooleanCondition(DramaSimulation sim, String name,
			       boolean intern) {
	super(sim, name, intern);
    }
    public OurBooleanCondition(DramaSimulation sim, String name,
			       boolean intern, boolean value) {
	super(sim, name, intern, value);
    }
    public void setValue(boolean value) {
	super.setValue(value);
    }
}

public class Test {
    static DramaSimulation sim = new DramaSimulation();

    public static void main(String argv[]) throws Exception {
	sim.run();

        TestActor e1 = new TestActor(sim, "e1") {
                protected void onConditionChange(Condition condition,
						 ConditionMode mode,
						 SimObject source) {
                    System.out.println ("Actor e1 reacted to Condition "
                                        +condition.getName()
					+ ": mode = " + mode
					+ ", source = " + source.getName());
                }
            };
        TestActor e2 = new TestActor(sim, "e2") {
                protected void onConditionChange(Condition condition,
						 ConditionMode mode,
						 SimObject source) {
                    System.out.println ("Actor e2 reacted to Condition "
                                        +condition.getName());
		}
	    };

	System.out.println("list actors (e1, e2 defined)");
	for (TestActor actor: sim.getObjects(TestActor.class)) {
	    System.out.println("    " + actor.getName());
	}
	System.out.println("list actors using Actor.class");
	for (Actor actor: sim.getObjects(Actor.class)) {
	    System.out.println("    " + actor.getName());
	}

        TestActor e3 = new TestActor(sim, "e3");

        TestActor e4 = new TestActor(sim, "e4");

        Group g = new TestGroup(sim, "g");

	Domain d = new TestDomain(sim, "general", 0);

	OurBooleanCondition bc =
	    new OurBooleanCondition(sim, "bc", true, false);

	System.out.println("list actors (e1, e2, e3, e4 defined)");
	for (TestActor actor: sim.getObjects(TestActor.class)) {
	    System.out.println("    " + actor.getName());
	}
	System.out.println("list actors using Actors.class");
	for (Actor actor: sim.getObjects(Actor.class)) {
	    System.out.println("    " + actor.getName());
	}

	e1.joinDomain(d, true);

        if (e1.inDomain(d) == false || d.containsActor(e1) == false) {
            System.out.println("e1.joinDomain failed");
        }

        e1.leaveDomain(d);
        if (e1.inDomain(d) == true || d.containsActor(e1) == true) {
            System.out.println("e1.leaveDomain failed");
        }
        e1.joinDomain(d, true);
        if (e1.inDomain(d) == false || d.containsActor(e1) == false) {
            System.out.println("e1.joinDomain failed");
        }

        d.addCondition(bc);
        if (d.hasCondition(bc) == false || bc.impactsDomain(d) == false) {
            System.out.println("d.addConidtion(bc) failed");
        }

	System.out.println(".... check d.print... & bc.print...");
	d.printConfiguration();
	d.printState();
	bc.printConfiguration();
	bc.printState();

	System.out.println("........ repeat d.print & bc.print "
			   + "with more indentation");
	d.printConfiguration("    ");
	d.printState("    ");
	bc.printConfiguration("    ");
	bc.printState("    ");
        
        System.out.println("Domain d has conditions:");
        for (Condition c: d.conditionSet()) {
            System.out.println("    " +c.getName());
        }
	System.out.println("condition bc impacts domains:");
        for (Domain dd: bc.domainSet()) {
            System.out.println("    " + dd.getName());
        }

        e2.joinDomain(d, true);
        e3.joinDomain(d);

	e4.addCondition(bc);


	bc.setValue(true);
	bc.notifyObservers();
	bc.completeNotification();

	e4.removeCondition(bc);

        e1.joinGroup(g);
        e2.joinGroup(g);
        e3.joinGroup(g);


	System.out.println(".... check e1.print... & g.print... ");
	e1.printConfiguration();
	e1.printState();
	g.printConfiguration();
	g.printState();
	
	System.out.println(".... more indentation for  e1.print... & "
			   + "g.print... ");
	e1.printConfiguration("    ");
	e1.printState("    ");
	g.printConfiguration("    ");
	g.printState("    ");

      if (g.getActorMembersSize() != 3) {
            System.out.println("g.getActorMembersSize() != 3");
        }
        
        if (!e1.inGroup(g) || !g.isMember(e1)) {
            System.out.println("e1.joinGroup(g, 1.0) failed");
        }
        boolean result = e1.leaveGroup(g);
        if (!result) System.out.println("e1.leaveGroup() returned false");
        if (e1.inGroup(g) || g.isMember(e1)) {
            System.out.println("e1.leaveGroup(g) failed");
        }
        if (g.getActorMembersSize() != 2) {
            System.out.println("g.getActorMembersSize() != 2");
        }

        e1.joinGroup(g);

        System.out.println("members of Group g:");
        for (Actor mr: g.getActorMembers()) {
            System.out.println("    " +mr.getName());
        }
        for (Group mr: g.getGroupMembers()) {
            System.out.println("    " +mr.getName());
        }
	if (sim.findCommDomain(e1,  null,e2) == null) {
            System.out.println("e1 cannot talk to e2");
        }

        if (sim.findCommDomain(e2, null, e1) == null) {
            System.out.println("e2 cannot talk to e1");
        }

        if (sim.findCommDomain(e1,  null,e3) == null) {
            System.out.println("e1 cannot talk to e3");
        }

        if (sim.findCommDomain(e3, null, e1) == null) {
            System.out.println("e3 cannot talk to e1");
        }

        if (!e1.inDomain(d)) System.out.println("e1.inDomain(d) failed");

        System.out.println("Domains for e1:");

	for (Domain gid: e1.domainSet()) {
            System.out.println("   " +gid.getName());
        }

        System.out.println ("Actors for Domain d:");
        for (Actor actor: d.actorSet()) {
            System.out.println("    " + actor.getName());
        }

        g.joinDomain(d);
        System.out.println("Domains associated with Group g:");
        for (Domain dom: g.domainSet()) {
            System.out.println("    " +dom.getName());
        }

	System.out.println("Groups for Domain g:");
	for (Group xg: d.groupSet()) {
	    System.out.println("    " + xg.getName());
	}
        if (!g.inDomain(d) || !d.containsGroup(g)) 
            System.out.println("g.isAssoicatedWith(d) failed");

        g.leaveDomain(d);
        for (Domain dom: g.domainSet()) {
            System.out.println("failed (g still associated with" 
                               +dom.getName());
        }
        if (g.inDomain(d) || d.containsGroup(g)) 
            System.out.println("g.disassoicatedfrom(d) failed");

	/*
        g.addCondition(bc);
        if (g.addressesCondition(bc) == false ||
            bc.addressedByGroup(g) == false) {
            System.out.println("g.addCondition(bc) failed");
        }
        System.out.println("Conditions group g addresses:");
        for (Condition c: g.conditionSet()) {
            System.out.println("   " +c.getName());
        }
        System.out.println("Groups that address condition bc:");
        for (Group group: bc.groupSet()) {
            System.out.println("   " +group.getName());
        }
        g.removeCondition(bc);
        if (g.addressesCondition(bc) || bc.addressedByGroup(g)) {
            System.out.println("g.removeCondition(bc) failed");
        }
	*/

        d.addCondition(bc);
        if (d.hasCondition(bc) == false || bc.impactsDomain(d) == false) {
            System.out.println("d.addCondition(bc) failed");
        }
        System.out.println("Conditions for Domain d:");
        for (Condition c: d.conditionSet()) {
            System.out.println("    " +c.getName());
        }
        System.out.println("Domain for Condition bc:");
        for (Domain dom: bc.domainSet()) {
            System.out.println("    " +dom.getName());
        }

        d.removeCondition(bc);
        if (d.hasCondition(bc) || bc.impactsDomain(d)) {
            System.out.println("d.removeCondition(bc) failed");
        }
        e1.scheduleDefaultCall(100);
        e2.scheduleDefaultCall(200);
        e3.scheduleDefaultCall(300);
        
        System.out.println("simulation contains objects:");
        for (String name: sim.getObjectNames()) {
            System.out.println("   " +name);
        }
        System.out.println("Actors only:");
        for (String name: sim.getObjectNames(TestActor.class)) {
            System.out.println("   " +name);
        }


        System.out.println("Starting simulation runs");
        sim.run();

        System.out.println("run 1:current time = " +sim.currentTicks());

        e3.scheduleDefaultCall(300);
        e2.scheduleDefaultCall(200);
        e2.cancelDefaultCall();
        e1.scheduleDefaultCall(100);

        e1.sendNewMsg(e2, 400);

        sim.run();

        System.out.println("run 2: current time = " +sim.currentTicks());
        e1.sendNewMsg(g, 100);
        sim.run();
        System.out.println("run 3: current time = " +sim.currentTicks());

        e2.leaveGroup(g);
        e1.sendNewMsg(g, 100);

        sim.run();

        System.out.println("run 4: current time = " +sim.currentTicks());
	System.exit(0);
    }
}
