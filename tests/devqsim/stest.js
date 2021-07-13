
scripting.importClass("org.bzdev.devqsim.Simulation");
scripting.importClass("org.bzdev.devqsim.TaskThread");

var out = scripting.getWriter();

sim = new Simulation(scripting, 1000.0);

sim.scheduleCall(function() {
    out.println("time = " + sim.currentTime());
}, sim.getTicks(4.0));

sim.scheduleCall(function() {
    out.println("time = " + sim.currentTime());
}, sim.getTicks(2.0))


sim.scheduleTask(function() {
    for (var i = 0; i < 5; i++) {
	out.println("time = " + sim.currentTime());
	TaskThread.pause(sim.getTicks(0.1));
    }
}, sim.getTicks(3.0));

sim.run();
