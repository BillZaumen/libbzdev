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

	cm.setTimeZone("UTC");
	System.out.println("timezone = " + cm.getTimeZone());
	System.out.println("getInitialWaitMillis() = "
			   + cm.getInitialWaitMillis());

	cm.setTimeZone("");

	System.out.println("timezone = " + cm.getTimeZone());

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
	    .setHelper(ewsHelper)
	    .setMode(CertManager.Mode.TEST);


	// Used only for a test of CertManager methods, so it uses
	// HTTP instead of HTTPS.
	EmbeddedWebServer ews = new EmbeddedWebServer(8080);
	
	if (ews.usesHTTPS()) {
	    System.exit(0);
	}

	cm.getSetup();
	cm.startMonitoring(ews);
	Thread.currentThread().sleep(150000);
	cm.alwaysCreate(true);
	Thread.currentThread().sleep(150000);
	cm.stopMonitoring();
	ews.start();
	ews.shutdown(0);

	System.out.println("try 'external' cert manager");
	cm = CertManager.newInstance("external")
	    .setCertName("test")
	    .setDomain("localhost")
	    .setKeystoreFile(new File("cmkeystore.jks"))
	    .setInterval(0)
	    .setStopDelay(2)
	    .setTracer(System.out)
	    .setCertTrace(true)
	    .setProtocol("TLS")
	    .setMode(CertManager.Mode.TEST);

	try {
	    ews = new EmbeddedWebServer(8080);
	} catch (Exception e) {
	    Thread.currentThread().sleep(60000);
	}

	cm.getSetup();
	cm.startMonitoring(ews);
	Thread.currentThread().sleep(150000);
	cm.alwaysCreate(true);
	Thread.currentThread().sleep(150000);
	cm.stopMonitoring();

    }
}
