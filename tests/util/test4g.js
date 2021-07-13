scripting.importClasses("org.bzdev.math.stats", ["BasicStats",
						 "BasicStats.Population"]);
scripting.importClass ("org.bzdev.math.rv.GaussianRV");

var rv = new GaussianRV(0.0, 1.0);
var stats = new BasicStats.Population();

rv.stream(50000000).forEach(function(x) {stats.add(x)});
scripting.getWriter().format("mean = %g, sdev = %g\n",
			     stats.getMean(), stats.getSDev());
