import org.bzdev.devqsim.*;
import org.bzdev.devqsim.rv.*;
import org.bzdev.math.rv.*;

public class RVTest {

    public static void main(String argv[]) throws Exception {
	
	Simulation sim = new Simulation();
	/*
	SimDoubleRV<DoubleRandomVariable> nrv =
	    new SimDoubleRV<>(sim, "rv", true,
			    new GaussianRV(0.0, 1.0));
	*/
	SimGaussianRV nrv = new SimGaussianRV(sim, "rv", true,
					     new GaussianRV(0.0, 1.0));
	System.out.println(nrv.getName());
	for (int i = 0; i < 10; i++) {
	    System.out.println("rv.next() = " + nrv.next());
	}
	System.out.println("using stream(10):");
	nrv.stream(10)
	    .forEach((v) -> {
		System.out.println(v);
	    });

	System.out.println("using stream().limit(10):");
	nrv.stream().limit(10)
	    .forEach((v) -> {
		System.out.println(v);
	    });

	SimGaussianRVFactory gf = new SimGaussianRVFactory(sim);
	gf.set("mean", 10.0);
	gf.set("sdev", 2.0);
	SimGaussianRV nrv2 = gf.createObject("rv2");
	for (int i = 0; i < 10; i++) {
	    System.out.println("rv2.next() = " + nrv2.next());
	}

	GaussianRV grv = nrv2.getRandomVariable();
	System.out.println(grv.getClass());
	if (nrv2 instanceof org.bzdev.obnaming.NamedRandomVariableOps) {
	    System.out.println("nvr2 instance of NamedRandomVariableOps");
	}
	org.bzdev.obnaming.NamedObjectOps no = nrv2;
	if (no instanceof org.bzdev.obnaming.NamedRandomVariableOps) {
	    System.out.println("'no' instance of NamedRandomVariableOps");
	    RandomVariable<?> rv =
		((org.bzdev.obnaming.NamedRandomVariableOps)no)
		.getRandomVariable();
	}
	System.out.println("GaussianRVRV");
	GaussianRVRV grvrv = new GaussianRVRV(new FixedDoubleRV(0.0),
					      new FixedDoubleRV(1.0));
	SimGaussianRVRV nrvrv = new SimGaussianRVRV(sim, "grvrv", true, grvrv);
	grv = nrvrv.next();
	for (int i = 0; i < 10; i++) {
	    System.out.println("grv.next() = " + grv.next());
	}
	SimGaussianRVRVFactory gff = new SimGaussianRVRVFactory(sim);
	System.out.println("SimGaussianRVRVFactory");
	nrvrv = gff.createObject("nrvrv2");
	grv = nrvrv.next();
	for (int i = 0; i < 10; i++) {
	    System.out.println("grv.next() = " + grv.next());
	}
    }
}
