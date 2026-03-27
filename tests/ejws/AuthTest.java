import java.io.*;
import java.net.*;
import org.bzdev.ejws.*;
import org.bzdev.ejws.maps.*;


public class AuthTest {

    public static void main(String argv[]) throws Exception {

	File gpgdir = new File(argv[0]);
	File f = new File(argv[1]);
	InputStream is = new FileInputStream(f);
	Reader isr = new InputStreamReader(is);
	BufferedReader r = new BufferedReader(isr);
	StringBuilder sb = new StringBuilder();
	r.lines().forEach((line) -> {
		sb.append(line); sb.append("\r\n");
	    });
	String key = sb.toString();

	String email = EjwsAuthenticator.storeGPGKey(gpgdir, key)
	    .getEmailAddress();
	System.out.println("stored key for " + email);
	
	InetSocketAddress saddr = new InetSocketAddress("0.0.0.0", 8080);

	EmbeddedWebServer ews = new
	    EmbeddedWebServer(saddr.getAddress(),
			      8080, 48, 10,
			      (new EmbeddedWebServer.SSLSetup("TLS"))
			      .keystore(new FileInputStream("thelio-ks.jks"))
			      .truststore(new FileInputStream
					  ("thelio-ts.jks")));

	File dir = new File(argv[2]);
	EjwsSecureBasicAuth auth = new EjwsSecureBasicAuth(ews, "realm");
	auth.setGPGHome(gpgdir);
	ews.add("/", DirWebMap.class, dir, auth, true, true, true);
	auth.createUser(email, "Example", null)
	    .setURI("login.html")
	    .setTruststore(System.getProperty("user.dir") + "/our-ca.jks")
	    .setTruststorePW("changeit".toCharArray())
	    .addUser(false);

	byte[] sblBytes = auth.getSBL(email);
	System.out.println("sblBytes.length = " + sblBytes.length);
	FileOutputStream os = new FileOutputStream("authtest.sbl");
	os.write(sblBytes);
	os.flush();
	os.close();

	System.exit(0);
    }
}
