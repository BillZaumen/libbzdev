import org.bzdev.graphs.*;
import org.bzdev.gio.OutputStreamGraphics;
import java.io.*;

public class AxisCases {
    public static void main(String argv[]) throws Exception {

	int mode = Integer.parseInt(argv[0]);
	int height;
	switch (mode) {
	case 1:
	    height = 200;
	    break;
	case 2:
	    height = 200;
	    break;
	case 3:
	    height = 400;
	    break;
	case 4:
	    height = 400;
	    break;
	case 5:
	    height = 500;
	    break;
	case 6:
	    height = 600;
	    break;
	default:
	    height = 600;
	}

	String imageType = OutputStreamGraphics.getImageTypeForFile(argv[1]);
	OutputStream os = new FileOutputStream(argv[1]);

	OutputStreamGraphics gio =
	    OutputStreamGraphics.newInstance(os, 600, height, imageType);
	Graph graph = new Graph(gio);
	graph.setOffsets(25, 25, 25, 10);
	if (mode < 6) {
	    graph.setRanges(0.0, 10.0, 0.0, 40.0);
	} else {
	    graph.setRanges(0.0, 1.0, 0.0, 60.0);
	}

	System.out.println("mode = " + mode);
	switch (mode) {
	case 1:
	    {
		AxisBuilder.Linear ab = new AxisBuilder.Linear
		    (graph, 0.0, 20.0, 10.0, true,
		     "ab.addTickSpec(level=0, depth=0, false, \"%1.0f\")");
		ab.setMaximumExponent(0);
		ab.addTickSpec(0, 0, false, "%1.0f");
		graph.draw(ab.createAxis());
	    }
	    break;
	case 2:
	    {
		AxisBuilder.Linear ab = new AxisBuilder.Linear
		    (graph, 0.0, 20.0, 10.0, true,
		     "ab.addTickSpec(level0, 0, true, \"%1.0f\")");
		ab.setMaximumExponent(0);
		ab.addTickSpec(0, 0, true, "%1.0f");
		graph.draw(ab.createAxis());
	    }
	    break;
	case 3:
	    {
		AxisBuilder.Linear ab = new AxisBuilder.Linear
		    (graph, 0.0, 30.0, 10.0, true,
		     "ab.addTickSpec(level=0, depth=0, false, \"%1.0f\")");
		ab.setMaximumExponent(0);
		ab.addTickSpec(0, 0, false, "%1.0f");
		graph.draw(ab.createAxis());
		ab = new AxisBuilder.Linear
		    (graph, 0.0, 10.0, 10.0, true,
		     "previous and "
		     + "ab.addTickSpec(level=1, depth=1, false, null)");
		ab.setMaximumExponent(0);
		ab.addTickSpec(0, 0, false, "%1.0f");
		ab.addTickSpec(1, 1, false, null);
		graph.draw(ab.createAxis());
	    }
	    break;
	case 4:
	    {
		AxisBuilder.Linear ab = new AxisBuilder.Linear
		    (graph, 0.0, 30.0, 10.0, true,
		     "ab.addTickSpec(level=0, depth=0, true, \"%1.0f\")");
		ab.setMaximumExponent(0);
		ab.addTickSpec(0, 0, true, "%1.0f");
		graph.draw(ab.createAxis());
		ab = new AxisBuilder.Linear
		    (graph, 0.0, 10.0, 10.0, true,
		     "previous and "
		     + "ab.addTickSpec(level=2, depth=1, false, null)");
		ab.setMaximumExponent(0);
		ab.addTickSpec(0, 0, true, "%1.0f");
		ab.addTickSpec(2, 1, false, null);
		graph.draw(ab.createAxis());
	    }
	    break;
	case 5:
	    {
		AxisBuilder.Linear ab = new AxisBuilder.Linear
		    (graph, 0.0, 40.0, 10.0, true,
		     "ab.addTickSpec(level=0, depth=0, false, \"%1.0f\")");
		ab.setMaximumExponent(0);
		ab.setNumberOfSteps(4);
		ab.addTickSpec(0, 0, false, "%1.0f");
		graph.draw(ab.createAxis());
		ab = new AxisBuilder.Linear
		    (graph, 0.0, 25.0, 10.0, true,
		     "previous and ab.addTickSpec(level=1, divisor=2, null)");
		ab.setMaximumExponent(0);
		ab.setNumberOfSteps(4);
		ab.addTickSpec(0, 0, false, "%1.0f");
		ab.addTickSpec(1, 2, null);
		graph.draw(ab.createAxis());
		ab = new AxisBuilder.Linear
		    (graph, 0.0, 10.0, 10.0, true,
		     "previous and ab.addTickSpec(level=2, divisor=4, null)");
		ab.setMaximumExponent(0);
		ab.setNumberOfSteps(4);
		ab.addTickSpec(0, 0, false, "%1.0f");
		ab.addTickSpec(1, 2, null);
		ab.addTickSpec(2, 4, null);
		graph.draw(ab.createAxis());
	    }
	    break;
	case 6:
	    {
		AxisBuilder.Log ab = new AxisBuilder.Log
		    (graph, 0.0, 60.0, 1.0, true,
		     "ab.addTickSpec(0, true, \"%1.0f\")");
		ab.addTickSpec(0, true, "%1.0f");
		graph.draw(ab.createAxis());
		ab = new AxisBuilder.Log
		    (graph, 0.0, 50.0, 1.0, true,
		     "previous and ab.addTickSpec(2, 1)");
		ab.addTickSpec(0, true, "%1.0f");
		ab.addTickSpec(2, 1);
		graph.draw(ab.createAxis());
		ab = new AxisBuilder.Log
		    (graph, 0.0, 40.0, 1.0, true,
		     "previous and ab.addTickSpec(3, 1, 2, 3)");
		ab.addTickSpec(0, true, "%1.0f");
		ab.addTickSpec(2, 1);
		ab.addTickSpec(3, 1, 2, 3);
		graph.draw(ab.createAxis());
		ab = new AxisBuilder.Log
		    (graph, 0.0, 30.0, 1.0, true,
		     "previous and ab.addTickSpec(4, 1, 10, 3)");
		ab.addTickSpec(0, true, "%1.0f");
		ab.addTickSpec(2, 1);
		ab.addTickSpec(3, 1, 2, 3);
		ab.addTickSpec(4, 1, 10, 3);
		graph.draw(ab.createAxis());
		ab = new AxisBuilder.Log
		    (graph, 0.0, 20.0, 1.0, true,
		     "previous and ab.addTickSpec(4, 1, 5, 7)");
		ab.addTickSpec(0, true, "%1.0f");
		ab.addTickSpec(2, 1);
		ab.addTickSpec(3, 1, 2, 3);
		ab.addTickSpec(4, 1, 10, 3);
		ab.addTickSpec(4, 1, 5, 7);
		graph.draw(ab.createAxis());
		ab = new AxisBuilder.Log
		    (graph, 0.0, 10.0, 1.0, true,
		     "previous and ab.addOneTick(3, 7.5)");
		ab.addTickSpec(0, true, "%1.0f");
		ab.addTickSpec(2, 1);
		ab.addTickSpec(3, 1, 2, 3);
		ab.addTickSpec(4, 1, 10, 3);
		ab.addTickSpec(4, 1, 5, 7);
		ab.addOneTick(3, 7.5);
		graph.draw(ab.createAxis());
		
	    }
	    break;
	default:
	    throw new Exception("unknown mode");
	}
	graph.write();
	System.exit(0);
    }
}

