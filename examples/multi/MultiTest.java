

public class MultiTest {
    public static void main(String argv[]) {

	TestClass1 tc1 = new TestClass1();
	TestClass2 tc2 = new TestClass2();

	Object arg1 = "hello";
	Object arg2 = "goodbye";

	tc1.test(arg1, arg2);
	tc2.test(arg1, arg2);
    }
}
