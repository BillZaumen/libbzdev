import org.bzdev.devqsim.*;

/*
 * Test various factories - just make sure they can create
 * the expected objects.
 */


public class FactoryTest {

    static class QS extends DefaultSimObject implements QueueServer {
	public long getInterval() {return 10;}

	public QS(Simulation sim, String name, boolean intern) {
	    super(sim, name, intern);
	}
    }

    public static void main(String argv[]) throws Exception {
	Simulation sim = new Simulation();

	FifoTaskQueueFactory ftqf = new FifoTaskQueueFactory(sim);
	LifoTaskQueueFactory ltqf = new LifoTaskQueueFactory(sim);
	PriorityTQFactory  ptqf = new PriorityTQFactory(sim);

	ftqf.set("canRelease", true);
	ltqf.set("canRelease", true);
	ptqf.set("canRelease", true);

	ftqf.set("releasePolicy", "CANCELS_AS_RELEASED");
	ftqf.set("deletePolicy", "MUST_BE_EMPTY");
	ltqf.set("releasePolicy", "CANCELS_AS_RELEASED");
	ltqf.set("deletePolicy", "MUST_BE_EMPTY");
	ptqf.set("releasePolicy", "CANCELS_AS_RELEASED");
	ptqf.set("deletePolicy", "MUST_BE_EMPTY");

	FifoTaskQueue ftq = ftqf.createObject("ftq");
	LifoTaskQueue ltq = ltqf.createObject("ltq");
	PriorityTaskQueue ptq = ptqf.createObject("ptq");

	System.out.println("release policy for ftq is " +
			   ftq.getReleasePolicy());
	System.out.println("deletion policy for ftq is " +
			   ftq.getDeletePolicy());

	System.out.println("release policy for ltq is " +
			   ltq.getReleasePolicy());
	System.out.println("deletion policy for ltq is " +
			   ltq.getDeletePolicy());

	System.out.println("release policy for ptq is " +
			   ptq.getReleasePolicy());
	System.out.println("deletion policy for ptq is " +
			   ptq.getDeletePolicy());


	FifoServerQueueFactory<FactoryTest.QS> fsqf
	    = new FifoServerQueueFactory<FactoryTest.QS>(sim) {
	    public Class<FactoryTest.QS> getQueueServerClass() {
		return FactoryTest.QS.class;
	    }
	};
	
	LifoServerQueueFactory<FactoryTest.QS> lsqf
	    = new LifoServerQueueFactory<FactoryTest.QS>(sim) {
	    public Class<FactoryTest.QS> getQueueServerClass() {
		return FactoryTest.QS.class;
	    }
	};

	PrioritySQFactory<FactoryTest.QS> psqf
	    = new PrioritySQFactory<FactoryTest.QS>(sim) {
	    public Class<FactoryTest.QS> getQueueServerClass() {
		return FactoryTest.QS.class;
	    }
	};

	FactoryTest.QS qs1 = new FactoryTest.QS(sim, "qs1", true);
	FactoryTest.QS qs2 = new FactoryTest.QS(sim, "qs2", true);
	FactoryTest.QS qs3 = new FactoryTest.QS(sim, "qs3", true);

	fsqf.set("deletePolicy", "MUST_BE_EMPTY");
	lsqf.set("deletePolicy", "MUST_BE_EMPTY");
	psqf.set("deletePolicy", "MUST_BE_EMPTY");

	fsqf.add("queueServer", qs1);
	fsqf.add("queueServer", qs2);
	fsqf.add("queueServer", qs3);

	lsqf.add("queueServer", qs1);
	lsqf.add("queueServer", qs2);
	lsqf.add("queueServer", qs3);

	psqf.add("queueServer", qs1);
	psqf.add("queueServer", qs2);
	psqf.add("queueServer", qs3);

	FifoServerQueue<FactoryTest.QS> fsq = fsqf.createObject("fsq");
	LifoServerQueue<FactoryTest.QS> lsq = lsqf.createObject("lsq");
	PriorityServerQueue<FactoryTest.QS> psq = psqf.createObject("psq");

	System.out.println("deletion policy for fsq is " +
			   fsq.getDeletePolicy());

	System.out.println("deletion policy for lsq is " +
			   lsq.getDeletePolicy());

	System.out.println("deletion policy for psq is " +
			   psq.getDeletePolicy());
	System.exit(0);
    }
}
