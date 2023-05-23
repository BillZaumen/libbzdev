import org.bzdev.ejws.*;
import org.bzdev.ejws.maps.*;
import org.bzdev.util.ErrorMessage;
import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ATest3 {
    public static void main(String argv[]) throws Exception {
	// ErrorMessage.setStackTrace(true);
	String realm = "realm";
	System.out.println(realm);
	EjwsSecureBasicAuth auth =
	    new EjwsSecureBasicAuth(realm, null);
	System.out.println("auth mode = " + auth.getMode());
	String publicKeyPem = Files.readString(Paths.get(argv[0]));
	System.out.println(publicKeyPem);

	auth.add("foo", publicKeyPem, "foo");
	EmbeddedWebServer ews = new EmbeddedWebServer(8080, 48, 2, null);
	ews.add("/", DirWebMap.class, new File("../../BUILD/api/"), auth,
		true, true, true);
	FileHandler handler = (FileHandler) ews.getHttpHandler("/");
	handler.setLoginAlias("login.html"/*, 10*/);
	handler.setLogoutAlias("logout.html",
			       new URI("https://www.google.com"));

	auth.setLoginFunction((p, t) -> {
		System.out.println("login: " + p.getUsername());
	    });

	auth.setLogoutFunction((p, t) -> {
		System.out.println("logout: " + p.getUsername());
	    });
	ews.setTracer("/", System.out);
	ews.start();
    }
}
