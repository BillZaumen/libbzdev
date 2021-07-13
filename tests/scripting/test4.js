listener = new Packages.ScriptingTest4$Adapter
            (scripting, {method1: function(str, val) {
				      print("hi\n");
			  }});

out = Packages.java.lang.System.out;

tl = new Packages.ScriptingTest4$TripDataAdapter(scripting, {
    tripStarted: function(tripID, time, ticks, hub) {
	out.println("tripStarted at " + hub.getName() +": time = " + time);

    },
    tripEnded: function (tripID, time, ticks, hub) {
	out.println("tripEnded at " + hub.getName() +": time = " + time);
    },
    tripFailedAtStart: function(tripID, time, ticks, hub) {
	out.println("tripFailedAtStart at " + hub.getName() +": time = "
		    + time);
    }
});
