import java.util.*;
import org.bzdev.util.*;

public class EventListTest {

    static class OurList1 implements EventListener {}
    static class OurList2 implements EventListener {}

    public static void main(String argv[]) throws Exception {
	EvntListenerList list = new EvntListenerList();

	OurList1 l1a = new OurList1() {
		public String toString() {return "l1a";}
	    };
	OurList1 l1b = new OurList1() {
		public String toString() {return "l1b";}
	    };

	OurList2 l2a = new OurList2() {
		public String toString() {return "l2a";}
	    };
	OurList2 l2b = new OurList2() {
		public String toString() {return "l2b";}
	    };

	list.add(OurList1.class, l1a);
	list.add(OurList2.class, l2a);
	list.add(OurList1.class, l1b);
	list.add(OurList2.class, l2b);
	list.add(OurList1.class, l1a);

	for (Object el: list.getListenerList()) {
	    System.out.print(" " + el);
	}
	System.out.println();
	System.out.println();
	for (EventListener el: list.getListeners(OurList1.class)) {
	    System.out.print(" " + el);
	}
	System.out.println();
	System.out.println();

	for (EventListener el: list.getListeners(OurList2.class)) {
	    System.out.print(" " + el);
	}
	System.out.println();

	System.out.format("OurList1: %d, OurList2, %d, total: %d\n",
			  list.getListenerCount(OurList1.class),
			  list.getListenerCount(OurList2.class),
			  list.getListenerCount());
	list.remove(OurList2.class, l2b);
	System.out.format("OurList1: %d, OurList2, %d, total: %d\n",
			  list.getListenerCount(OurList1.class),
			  list.getListenerCount(OurList2.class),
			  list.getListenerCount());
	list.remove(OurList1.class, l1a);
	System.out.format("OurList1: %d, OurList2, %d, total: %d\n",
			  list.getListenerCount(OurList1.class),
			  list.getListenerCount(OurList2.class),
			  list.getListenerCount());
	list.remove(OurList1.class, l1b);
	System.out.format("OurList1: %d, OurList2, %d, total: %d\n",
			  list.getListenerCount(OurList1.class),
			  list.getListenerCount(OurList2.class),
			  list.getListenerCount());

	list.remove(OurList1.class, l1a);
	System.out.format("OurList1: %d, OurList2, %d, total: %d\n",
			  list.getListenerCount(OurList1.class),
			  list.getListenerCount(OurList2.class),
			  list.getListenerCount());


	list.remove(OurList2.class, l2a);
	System.out.format("OurList1: %d, OurList2, %d, total: %d\n",
			  list.getListenerCount(OurList1.class),
			  list.getListenerCount(OurList2.class),
			  list.getListenerCount());


    }
}
