if (typeof(err) == "undefined") {
    err = scripting.getErrorWriter();
}
if (typeof(out) == "undefined") {
    out = scripting.getWriter();
}

out.println("started script");
/*
importPackage(org.bzdev.drama);
// import classes in the unnamed package
importPackage(Packages)
*/

scripting.importClasses("org.bzdev.drama",
			["DramaSimulation",
			 "IntegerConditionFactory",
			 "BooleanConditionFactory"]);
scripting.importClass(null, "TestActorFactory");


out.println("finished imports");

sim = new DramaSimulation(scripting);

dcf = sim.createFactory("org.bzdev.drama.DoubleConditionFactory");
icf = sim.createFactory(IntegerConditionFactory.class);
lfc = sim.createFactory("org.bzdev.drama.LongConditionFactory");
bcf = sim.createFactory(BooleanConditionFactory.class);
df = sim.createFactory( "org.bzdev.drama.DomainFactory");
taf = sim.createFactory(TestActorFactory.class);

out.println("created factories");

icf.configure({initialValue: 2});
ic = icf.createObject("integerCondition");

lc = lfc.createObject("longCondition", {initialValue: 3});
dc = dcf.createObject("doubleCondition", {initialValue: 1.0} );
bc = bcf.createObject("booleanCondition", true, {initialValue: true});

out.println(dc.getName() + ": value = " + dc.getValue());
out.println(ic.getName() + ": value = " + ic.getValue());
out.println(lc.getName() + ": value = " + lc.getValue());
out.println(bc.getName() + ": value = " + bc.getValue());

a1 = taf.createObject("a1");
a2 = taf.createObject("a2");

a1.sendNewMsg(a2, 10);

adapter = sim.createAdapter({
    simulationStart: function(s) {out.println("simulation started");},
    simulationStop: function(s) {out.println("simulation stopped");},
    messageReceiveStart: function(s,f,t,msg) {
	out.println("starting msg " + msg);
    },
    messageReceiveEnd: function(s,f,t,msg) {out.println("ending msg " + msg);}
});



sim.addSimulationListener(adapter);
a2.addSimulationListener(adapter);

sim.run();

it = sim.getObjects("TestActor").iterator();
while (it.hasNext()) {
    out.println("it.next() = " + it.next().getName());
}
out.println("-----");
it = sim.getObjects("org.bzdev.drama.Actor").iterator();
while (it.hasNext()) {
    out.println("it.next() = " + it.next().getName());
}
