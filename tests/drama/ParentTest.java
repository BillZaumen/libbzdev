import org.bzdev.devqsim.*;
import org.bzdev.drama.*;

public class ParentTest {
    public static void main(String argv[]) {
	DramaSimulation dsim = new DramaSimulation(1000.0);
	/*
	 * Verify that we can add a subclass of 
	 * DefaultSimObject to a drama simulation.
	 */
	DelayTaskQueue tq = new FifoTaskQueue(dsim, "tq", true);
	System.out.println("tq name is " + tq.getName());
    }
}
