import org.bzdev.drama.*;

public class TestCondition extends BooleanCondition
{
    public TestCondition(DramaSimulation sim,
			 String name,
			 boolean intern)
    {
	super(sim, name, intern);
    }

    public TestCondition(DramaSimulation sim,
			 String name,
			 boolean intern,
			 boolean value)
    {
	super(sim, name, intern, value);
    }

    public void setValue(boolean value) {
	super.setValue(value);
    }
}
