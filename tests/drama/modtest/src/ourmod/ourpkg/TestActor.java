package ourpkg;
import org.bzdev.drama.*;
import org.bzdev.math.rv.DoubleRandomVariable;

public class TestActor extends Actor {

    private int val = -1;

    private DoubleRandomVariable rv = null;

    public TestActor(DramaSimulation sim, String name, boolean intern) {
	super(sim, name, intern);
    }

    public void setValue(int value) {val = value;}

    public int getValue() {return val;}


    public void setRV(DoubleRandomVariable rv) {
	this.rv = rv;
    }

    public double next() {
	if (rv != null) return rv.next();
	else return 0.0;
    }

}
