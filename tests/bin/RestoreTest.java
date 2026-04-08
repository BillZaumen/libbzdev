import java.util.*;
import org.bzdev.util.*;


public class RestoreTest {
    static String data = "H4sIAAAAAAAA/0VSS3OiQBC+W+V/YCt7SA6Lw/ioxCoOS8wYEXAZGBDKy8BMhDAwLKBRf/2q67qH7ur+ur+ufj08frNVX6F1LfKUdrmsBvuKqcmJ8b3aJoLuqjTjzVO/94CaXLFpo2hDBWrT0WgKRsqvma9AACf93q7ljVpKxnV4cy5K73jb/bhYNzChLdezrqvbzXQwEDKlIpNtt5k+g2cw+Mdscv374z3/SchtXqlZV4p+7wJMRmrBjzXNG7XeJefGl/yop1BUSYkAW5vCClEbw5cjXWOZ+HJLQGbYnwQyKAB9C2YYOcbMlZ3lgc4iJsJArBZnfIGMwC0Opns0LBwUf+MuWPokbhgIhiHJrESrTV7KLxcgHBJx9/EbwhHAOAYIERGNMVp8RSdG7fn4nRA2tH3cxTCecBTPSJH9TrdgaUFz5JL6XOewPouVzAIQfqJj6jPDK5xVMHfsKESug7I9K5HjFOaeDQ1INXxwT45IhkEXhVoew2BMfPDyf6ZghV8NdJ7DDYhpe8TeeiCIrzEPOK/bjb7R78u8brqmbfslG6aHpXmy3guJi5isNaeK5j/L6Ea4ZjLepk1eX55F98/3VeSHQivFMyzlIxe83/sDM0R5DVUCAAA=";

    public static void main(String argv[]) throws Exception {

	System.out.println("base64.keypair.publicKey: signature-algorithm: SHA256withECDSA\n"
+ "-----BEGIN EC PUBLIC KEY-----\n"
+ "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAERV9DR0c7Z0nGQGw14mgfzxVCRHjr\n"
+ "/bxAJQYLWYLKl5tZ1rq7AJCNTcLaaP4XovaM6BowpvkTqC3eoumambgeyQ==\n"
+ "-----END EC PUBLIC KEY-----");
	System.out.println("user.description: Test of an SBL file");
	System.out.println("ebase64.user.password: Zbs,y(FFT_Sg``&b");
	System.out.println("user.base: https://localhost:8080/");
	System.out.println("user.uri: $(user.base)login.html");
	System.out.println("user.user: test-user");
	System.out.println("user.mode: 2");

	ConfigProperties cprops = new
	    ConfigProperties(data, "application/vnd.bzdev.sblauncher");

	String keys[] = {
	    "base64.keypair.publicKey",
	    "user.description",
	    "base64.user.password",
	    "user.base",
	    "user.uri",
	    "user.user",
	    "user.mode"
	};
	System.out.println("DECODED:");

	for (String key: keys) {
	    System.out.println(key + ": " + cprops.getProperty(key));
	}
    }
}
