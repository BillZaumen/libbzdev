scripting.importClass("org.bzdev.devqsim.Simulation");
scripting.importClass("org.bzdev.devqsim.TaskThread");

var out = scripting.getWriter();

sim = new Simulation(scripting, 1000.0);

sim.createFactories("org.bzdev.devqsim",
		    {fifof: "FifoTaskQueueFactory"});

var fifo = fifof.createObject("fifo");

sim.scheduleTask(function() {
    out.println("time for task = " + sim.currentTime());
    fifo.addCurrentTask(sim.getTicks(1.0));
    out.println("time for task = " + sim.currentTime());
    fifo.addCurrentTask(sim.getTicks(1.0));
    out.println("time for task = " + sim.currentTime());
    fifo.addCurrentTask(sim.getTicks(1.0));
    out.println("time for task = " + sim.currentTime());
});

var count = 0;
var callable = {
    call: function() {
	out.println("time for call = " + sim.currentTime());
	if (count++ == 3) return;
	fifo.addCallObject(callable, sim.getTicks(1.0));
    }
};

sim.scheduleCallObject(callable, 0.0);

sim.run();
