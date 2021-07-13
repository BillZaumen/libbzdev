package ourpkg;
import org.bzdev.drama.*;
import org.bzdev.devqsim.rv.*;

public class Test {
    public static void main(String argv[]) throws Exception {
	DramaSimulation sim = new DramaSimulation();

	SimGaussianRVFactory rvf = new SimGaussianRVFactory(sim);
	rvf.set("mean", 0.0);
	rvf.set("sdev", 1.0);
	SimGaussianRV nrv = rvf.createObject("grv");

	Factory f = new Factory(sim);

	f.set("value", 20);
	f.set("rv", "grv");

	TestActor actor = f.createObject("test");
	f.set("rv", nrv);
	TestActor actor2 = f.createObject("test2");

	System.out.println("actor.getValue() = " + actor.getValue());

	for (int i = 0; i < 10; i++) {
	    System.out.println(actor.next());
	}

	System.out.println("actor2.getValue() = " + actor2.getValue());
	for (int i = 0; i < 10; i++) {
	    System.out.println(actor2.next());
	}


    }
}
