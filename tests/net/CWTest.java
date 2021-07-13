import java.net.*;
import org.bzdev.net.*;

public class CWTest {

    public static void main(String argv[]) throws Exception {
	InetSocketAddress saddr = new InetSocketAddress(8080);

	CloseWaitService cws = new CloseWaitService(120, 30, saddr);

	cws.start();

	if (argv.length > 0) {
	    long tau = Long.parseLong(argv[0]);
	    Thread.currentThread().sleep(tau*1000);
	    cws.stop();
	    Thread.currentThread().sleep(tau*1000);
	    cws.start();
	    Thread.currentThread().sleep(tau*1000);
	    cws.stop();

	    saddr = new InetSocketAddress("localhost", 8080);
	    
	    cws = new CloseWaitService(120, 30, saddr);
	    Thread.currentThread().sleep(tau*1000);
	    cws.stop();
	    Thread.currentThread().sleep(tau*1000);
	    cws.start();
	    Thread.currentThread().sleep(tau*1000);
	    cws.stop();
	}
    }
}
