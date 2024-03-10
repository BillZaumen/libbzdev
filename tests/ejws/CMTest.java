import java.io.File;
import org.bzdev.ejws.*;

public class CMTest {
    public static void main(String argv[]) throws Exception {
	CertManager cm = CertManager.newInstance("default");

	System.out.println("Provider names: ");
	for (String s: CertManager.providerNames()) {
	    System.out.println("... " + s);
	}

	cm.setCertName("test")
	    .setDomain("localhost")
	    .setEmail("nobody@nowhere.com")
	    .setTimeOffset(3600)
	    .setInterval(5)
	    .setStopDelay(2);

	System.out.println("getInitialWaitMillis() = "
			   + cm.getInitialWaitMillis());

	System.out.println("getIntervalMillis() = "
			    + cm.getIntervalMillis());

	System.out.println(cm.getCertName());
	System.out.println(cm.getDomain());
	System.out.println(cm.getEmail());
	System.out.println(cm.getTimeOffset());
	System.out.println(cm.getInterval());
	System.out.println(cm.getStopDelay());

	EmbeddedWebServer ewsHelper = new EmbeddedWebServer(8081);

	cm = CertManager.newInstance()
	    .setCertName("test")
	    .setDomain("localhost")
	    .setKeystoreFile(new File("cmkeystore.jks"))
	    .setInterval(0)
	    .setStopDelay(2)
	    .setTracer(System.out)
	    .setCertTrace(true)
	    .setProtocol("TLS")
	    .setHelper(ewsHelper);


	// Used only for a test of CertManager methods, so it uses
	// HTTP instead of HTTPS.
	EmbeddedWebServer ews = new EmbeddedWebServer(8080);
	
	if (ews.usesHTTPS()) {
	    System.exit(0);
	}

	cm.getSetup();
	cm.startMonitoring(ews);
	Thread.currentThread().sleep(300000);
	cm.stopMonitoring();

    }
}
