import org.bzdev.util.*;

public class CipherTest {
    public static void main(String argv[]) throws Exception {
	byte[] data = "hello there".getBytes("UTF8");
	char[] key = "password".toCharArray();
	byte[] encrypted = SymmetricCipher.encrypt(key, data);
	for (byte b: encrypted) {
	    System.out.format("%02x", b);
	}
	System.out.println();
	byte[] decrypted = SymmetricCipher.decrypt(key, encrypted);
	String result = new String(decrypted, "UTF8");
	System.out.println(result);

	System.out.println("try an empty byte array");

	data = new byte[0];
	System.out.println("data.length = " + data.length);
	
	encrypted = SymmetricCipher.encrypt(key, data);
	for (byte b: encrypted) {
	    System.out.format("%02x", b);
	}
	System.out.println();
	decrypted = SymmetricCipher.decrypt(key, encrypted);
	System.out.println("decrypted.length = " + decrypted.length);
	result = new String(decrypted, "UTF8");
	System.out.println("result = \"" +result + "\"");


    }

}
