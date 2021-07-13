import org.bzdev.devqsim.*;
import org.bzdev.lang.*;

public class TickTest {
    static Simulation sim = new Simulation(1000.0);

    public static void main(String argv[]) throws Exception {
	long ticks = sim.getTicks(0.0001);
	long ticksC =sim.getTicksCeil(0.0001);
	long ticksF = sim.getTicksFloor(0.0001);
	System.out.println("ticks = " + ticks
			   + ", ticksC = " + ticksC
			   +", ticksF =  " + ticksF);
	System.out.println("sim.getTicks(10.0) = " + sim.getTicks(10.0));

	System.out.println("sim.getTime(1000) = " + sim.getTime(1000));
	System.exit(0);
    }
}
