import org.bzdev.ejws.*;

public class AuthCodeTest {

    public static void main(String argv[]) throws Exception {
	var authCode1 = new AuthCode("this is a test");
	var authCode2 = new AuthCode("");

	// make sure we always get the same code for the same
	// input and that we get a different code for a different
	// secret (to test that cloning worked as expected).

	System.out.println("foo: " + authCode1.getCode("foo"));
	System.out.println("foo: " + authCode1.getCode("foo"));
	System.out.println("----");
	System.out.println("foo: " + authCode2.getCode("foo"));
	System.out.println("foo: " + authCode2.getCode("foo"));
    }
}
