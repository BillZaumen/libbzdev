

public class STest {
    public static void main(String argv[]) {
	try {
	    STest2 testClass = new STest2();
	    Double x = Double.valueOf(20.0);
	    testClass.test(x);
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	System.exit(0);
    }
}
