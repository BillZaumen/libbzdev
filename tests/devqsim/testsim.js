scripting.importClass("org.bzdev.devqsim.Simulation");
scripting.importClass("org.bzdev.devqsim.TaskThread");

var sim = new Simulation(scripting);
sim.scheduleTaskObject({run: function() {
        for (var i = 0; i < 5; i++) {
	    scripting.getWriter().println("task is at simulation time "
                                          + sim.currentTicks()
                                          + " [i= " + i +"]");
	    TaskThread.pause(10);
        }
    }
}, 10);
sim.run()
