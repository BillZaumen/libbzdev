#!/usr/bin/java --source 11

import org.bzdev.math.stats.*;
import org.bzdev.math.rv.*;

public class Test4G {
    public static void main(String argv[]) throws Exception {
	DoubleRandomVariable rv = new GaussianRV(0.0, 1.0);
	BasicStats stats = new BasicStats.Population();

	rv.stream(50000000).forEach(stats::add);
	System.out.format("mean = %g, sdev = %g\n",
			  stats.getMean(), stats.getSDev());
    }
}
