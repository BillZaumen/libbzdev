// scrunner should define an image type named imgtype
// and an output stream named os for the output graph.

if (typeof(err) == "undefined") {
    err = scripting.getErrorWriter();
}
if (typeof(out) == "undefined") {
    out = scripting.getWriter();
}

if (typeof(os) == "undefined") {
    throw "os undefined";
}

if (typeof(imgtype) == "undefined") {
    throw "imgtype undefined";
}

scripting.importClass("org.bzdev.drama.DramaSimulation");
scripting.importClass("org.bzdev.util.units.MKS");
scripting.importClass("org.bzdev.gio.OutputStreamGraphics");
scripting.importClass("org.bzdev.math.rv.GaussianRV");

scripting.importClass("org.bzdev.graphs.Graph");
scripting.importClass("TimeAxis")

sim = scripting.create(DramaSimulation.class, scripting, 1000.0);

hf = sim.createFactory("HomeFactory");
uf = sim.createFactory("UtilityFactory");
tf = sim.createFactory("TemperatureCondFactory")
df = sim.createFactory("org.bzdev.drama.DomainFactory");

maxtime = MKS.hours(16.0);
risetime = MKS.hours(3.0);

osg = OutputStreamGraphics.newInstance(os, 800, 600, imgtype);
graph = new Graph(osg);
graph.setOffsets(75, 75);
graph.setRanges(0.0, maxtime, 0.0, 4.0e7);
xaxis = new TimeAxis(0.0, 0.0, Graph.Axis.Dir.HORIZONTAL_INCREASING,
		     maxtime, 0.0, MKS.minutes(1.0), false);
xaxis.setLabel("Time in hours");
xaxis.setLabelOffset(10.0);
xaxis.setWidth(2.0);
xaxis.addTick(new Graph.TickSpec(3.0, 1.0, 10));
xaxis.addTick(new Graph.TickSpec(5.0, 1.5, 60, "%2.0f", 5.0));
yaxis = new Graph.Axis(0.0, 0.0,  Graph.Axis.Dir.VERTICAL_INCREASING,
		       4.0E7, 0.0, 1.0E6, true);
yaxis.setLabel("Power in Watts");
yaxis.setLabelOffset(10.0);
yaxis.setWidth(2.0);
yaxis.addTick(new Graph.TickSpec(3.0, 1.0, 1));
yaxis.addTick(new Graph.TickSpec(5.0, 1.6, 10, "%#2.0g", 5.0));
graph.draw(xaxis);
graph.draw(yaxis);
g2d = graph.createGraphics();

outsideTempCond = tf.createObject("outsideTemperature", {
    maxtime: maxtime, risetime: risetime,
    minTemperature: MKS.degC(18.0), maxTemperature: MKS.degC(30.0)});

area = df.createObject("area", {condition: [outsideTempCond]});

utility = uf.createObject("utility");
utility.setupGraphics(graph, g2d);
utility.recordAveragePower(1.0);

hf.configure([
    {withKey: area, config: {domain: true}},
    {utility: utility,
     desiredLowTemperature: new GaussianRV(MKS.degC(20.0), 0.5),
     desiredHighTemperature: new GaussianRV(MKS.degC(22.0), 0.5),
     referenceTemperature: MKS.degC(30.0),
     warmingTime: new GaussianRV(MKS.minutes(30.0), MKS.minutes(10.0)),
     coolingTime: new GaussianRV(MKS.minutes(5.0), 180.0),
     fanCoolingTime: new GaussianRV(MKS.minutes(5.0), 60.0),
     initialTemperature: new GaussianRV(MKS.degC(18.0), 1.0),
     acPower: new GaussianRV(3500.0, 100.0)}]);

n = 10000;
array = scripting.createArray("Home", n)

homes = hf.createObjects(array, "home", n);

tput = sim.getTicksPerUnitTime();
endtime = Math.round(tput * maxtime);

time1 = Math.round(tput * maxtime * 0.4);
time2 = Math.round(tput * maxtime * 0.6);

sim.scheduleCallObject({call: function() {
    out.println("reduction fraction set to 0.7 at " + sim.currentTime());
    utility.setReductionFraction(0.7);
}}, time1);

sim.scheduleCallObject({call: function() {
    out.println("reduction fraction set to 1.0 at " + sim.currentTime());
    utility.setReductionFraction(1.0);
}}, time2);

sim.run(endtime);
graph.write();
