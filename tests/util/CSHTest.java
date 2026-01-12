import java.util.*;
import org.bzdev.util.*;
import org.bzdev.math.StaticRandom;
import org.bzdev.math.rv.*;

public class CSHTest {

    static class TestEvent extends CachedSkewHeap.Entry<TestEvent> {
	double dist;
	public TestEvent(double d) {
	    dist = d;
	}
    }

    public static void main(String argv[]) throws Exception {
	StaticRandom.maximizeQuality();
	DoubleRandomVariable rv = new UniformDoubleRV(0.0, true, 100.0, true);
	BooleanRandomVariable brv = new BinomialBooleanRV(0.01);

	CachedSkewHeap<TestEvent> heap = new CachedSkewHeap<>() {
		protected int
		    compareEntries(CachedSkewHeap.Entry<TestEvent> e1,
				   CachedSkewHeap.Entry<TestEvent> e2) {
		    TestEvent te1 = (TestEvent)e1;
		    TestEvent te2 = (TestEvent)e2;
		    return ((te1.dist < te2.dist)? -1:
			    (te1.dist == te2.dist)? 0:
			    1);
		}
	    };

	PriorityQueue<TestEvent> pq = new PriorityQueue<>
	    ((te1, te2) -> {
		return ((te1.dist < te2.dist)? -1:
			(te1.dist == te2.dist)? 0:
			1);
	    });

	TestEvent te1 = new TestEvent(1.0);
	TestEvent te2 = new TestEvent(2.0);
	heap.add(te1);
	heap.add(te2);
	System.out.println(heap.size());
	System.out.println(heap.poll().dist);
	System.out.println(heap.poll().dist);
	System.out.println(heap.size());
	heap.add(te2);
	heap.add(te1);
	System.out.println(heap.size());
	System.out.println(heap.poll().dist);
	System.out.println(heap.poll().dist);
	System.out.println(heap.size());
	ArrayList<TestEvent> list = new ArrayList<>(2000);
	int n = 1000000;
	for (int i = 0; i < n; i++) {
	    TestEvent te = new TestEvent(rv.next());
	    heap.add(te);
	    pq.add(te);
	    if (brv.next()) list.add(te);
	}
	
	if (heap.size() != n) throw new Exception(heap.size() + "!= " + n);

	for (TestEvent te: list) {
	    heap.remove(te);
	    pq.remove(te);
	}
	if (heap.size() != (n - list.size())) throw new Exception();

	for (TestEvent te: list) {
	    heap.add(te);
	    pq.add(te);
	}
	if (heap.size() != n) throw new Exception();

	double last = heap.poll().dist;
	for (int i = 1; i < n; i++) {
	    if (heap.isEmpty()) throw new Exception();
	    double val = heap.peek().dist;
	    pq.peek();
	    double current = heap.poll().dist;
	    pq.poll();
	    if (val != current) throw new Exception();
	    if (last > current) throw new Exception();
	}
	if (!heap.isEmpty()) throw new Exception();

	System.out.println(".... timing test ...");

	ArrayList<TestEvent> alist = new ArrayList<>(n);
	for (int i = 0; i < n; i++) {
	    TestEvent te = new TestEvent(rv.next());
	    alist.add(te);
	}

	long time1 = System.nanoTime();
	for (TestEvent te: alist) {
	    pq.add(te);
	    pq.remove(te);
	    pq.add(te);
	}
	for (int i = 0; i < n; i++) {
	    pq.poll();
	}
	long time2 = System.nanoTime();
	for (TestEvent te: alist) {
	    heap.add(te);
	    heap.remove(te);
	    heap.add(te);
	}
	for (int i = 0; i < n; i++) {
	    heap.poll();
	}
	long time3 = System.nanoTime();
	System.out.println("time2 - time1: " + (time2 - time1));
	System.out.println("time3 - time2: " + (time3 - time2));
	System.out.println("ratio: " +
			   ((double)(time2 - time1) / (double)(time3 - time2)));

	System.out.println("add sequentional timing");
	alist.clear();
	for (int i = 0; i < n; i++) {
	    TestEvent te = new TestEvent((double)i);
	    alist.add(te);
	}

	time1 = System.nanoTime();
	for (TestEvent te: alist) {
	    pq.add(te);
	}
	for (int i = 0; i < n; i++) {
	    pq.poll();
	}
	time2 = System.nanoTime();
	heap.merge();
	for (TestEvent te: alist) {
	    heap.add(te);
	}
	heap.merge();
	for (int i = 0; i < n; i++) {
	    heap.poll();
	}
	heap.merge();
	time3 = System.nanoTime();
	System.out.println("time2 - time1: " + (time2 - time1));
	System.out.println("time3 - time2: " + (time3 - time2));
	System.out.println("ratio: " +
			   ((double)(time2 - time1) / (double)(time3 - time2)));
	

    }
}
