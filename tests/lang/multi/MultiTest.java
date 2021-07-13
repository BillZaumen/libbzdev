

public class MultiTest {
    public static void main(String argv[]) throws Exception {

	TestClass1 tc1 = new TestClass1();
	TestClass2 tc2 = new TestClass2();
	TestClass3 tc3 = new TestClass3();

	Object arg1 = "hello";
	Object arg2 = "goodbye";

	tc1.test(arg1, arg2);
	tc2.test(arg1, arg2);
	tc3.test(arg1, arg2);
	tc3.test(Double.valueOf(10.0), Double.valueOf(20.0));
	System.exit(0);
    }
}
