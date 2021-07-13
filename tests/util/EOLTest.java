import org.bzdev.util.ErrorMessage;

public class EOLTest {
    public static void main(String argv[]) throws Exception {

	String test = "abcdefg\n123456\r\nABCDEFG\r\nxyz";
	Exception e;

	for (int i = 0; i < test.length(); i++) {
	    if (test.charAt(i)== '\r' || test.charAt(i) ==  '\n') continue;
	    e = new Exception("this is a test..." + test.charAt(i));
	    String msg = ErrorMessage
		.getMultilineString("*** ", "foo.txt", test, i, e, true, true);
	    System.out.println(msg);
	}
	System.out.println("----------------------------");

	ErrorMessage.setAppendable(System.out);
	ErrorMessage.display("foo.txt", "abcdefghijk", 5,
			     new Exception("test"), true, true);

	System.out.println("-----");
	ErrorMessage.display("foo.txt", "abcdefghijk", 5,
			     new Exception("test"), false, false);

    }
}
