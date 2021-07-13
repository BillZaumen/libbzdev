import org.bzdev.graphs.*;
import java.awt.Color;

public class AxisBuilderTest {

    public static void main(String argv[]) throws Exception {
	Graph graph = new Graph(800, 600);
	graph.setRanges(0.0, 30.0, 0.0, 40.0);
	graph.setOffsets(75, 75);
	graph.setBackgroundColor(Color.WHITE);
	graph.clear();

	int level0 = 0;
	int level1 = 1;
	int level2 = 2;

	System.out.println("Graph 1");

	// case 1
	AxisBuilder.Linear ab =
	    new AxisBuilder.Linear(graph, 0.0, 0.0, 30, true,
				   "Axis Label");
	ab.setMaximumExponent(1);
	ab.addTickSpec(level0, 0, false, "%3.1f");
	graph.draw(ab.createAxis());
	
	// case 2
	ab = new AxisBuilder.Linear(graph, 0.0, 10.0, 30.0, true,
				    "Axis Label");
	ab.setMaximumExponent(1);
	ab.addTickSpec(level0, 0, true, null);
	ab.addTickSpec(level2, 1, false, null);
	graph.draw(ab.createAxis());

	// case 3
	ab = new AxisBuilder.Linear(graph, 0.0, 20.0, 30.0, true, null);
	ab.setMaximumExponent(1);
	ab.addTickSpec(level0, 0, false, null);
	ab.addTickSpec(level1, 1, false, null);
	ab.addTickSpec(level2, 2, false, null);
	graph.draw(ab.createAxis());

	// case 4
	ab = new AxisBuilder.Linear(graph, 0.0, 30.0, 30.0, true, null);
	ab.setMaximumExponent(1);
	ab.setNumberOfSteps(10);
	ab.addTickSpec(level0, 0, false, null);
	ab.addTickSpec(level1, 1, false, null);
	ab.addTickSpec(level2, 5, null);
	graph.draw(ab.createAxis());

	// case 5
	ab = new AxisBuilder.Linear(graph, 0.0, 40.0, 30.0, true, null);
	ab.setMaximumExponent(1);
	ab.setNumberOfSteps(4);
	ab.addTickSpec(level0, 0, false, null);
	ab.addTickSpec(level1, 2, null);
	ab.addTickSpec(level2, 4, null);
	graph.draw(ab.createAxis());

	graph.write("png", "abtest1.png");

	System.out.println("GRAPH 2");

	graph = new Graph(800, 600);
	graph.setRanges(0.0, 30.0, 0.0, 40.0);
	graph.setOffsets(75, 75);
	graph.setBackgroundColor(Color.WHITE);
	graph.clear();

	ab = new AxisBuilder.Linear(graph, 0.0, 0.0, 30, true, true,
				    "Axis Label");
	ab.setMaximumExponent(1);
	ab.addTickSpec(level1, 0, false, "%3.1f");
	graph.draw(ab.createAxis());
	
	ab = new AxisBuilder.Linear(graph, 0.0, 10.0, 30.0, true, true,
				    "Axis Label");
	ab.setMaximumExponent(1);
	ab.addTickSpec(level0, 0, true, null);
	ab.addTickSpec(level2, 1, false, null);
	graph.draw(ab.createAxis());

	ab = new AxisBuilder.Linear(graph, 0.0, 20.0, 30.0, true, true, null);
	ab.setMaximumExponent(1);
	ab.addTickSpec(level0, 0, false, null);
	ab.addTickSpec(level1, 1, false, null);
	ab.addTickSpec(level2, 2, false, null);
	graph.draw(ab.createAxis());

	ab = new AxisBuilder.Linear(graph, 0.0, 30.0, 30.0, true, true, null);
	ab.setMaximumExponent(1);
	ab.setNumberOfSteps(10);
	
	ab.addTickSpec(level0, 0, false, null);
	ab.addTickSpec(level1, 1, false, null);
	ab.addTickSpec(level2, 5, null);
	graph.draw(ab.createAxis());

	ab = new AxisBuilder.Linear(graph, 0.0, 40.0, 30.0, true, true, null);
	ab.setMaximumExponent(1);
	ab.setNumberOfSteps(4);
	ab.addTickSpec(level0, 0, false, null);
	ab.addTickSpec(level1, 2, null);
	ab.addTickSpec(level2, 4, null);
	graph.draw(ab.createAxis());

	graph.write("png", "abtest2.png");

	System.out.println("GRAPH 3");
	graph = new Graph(800, 600);
	graph.setRanges(0.0, 40.0, 0.0, 30.0);
	graph.setOffsets(75, 75);
	graph.setBackgroundColor(Color.WHITE);
	graph.clear();

	// case 1
	ab =new AxisBuilder.Linear(graph, 0.0, 0.0, 30, false, "Axis Label");
	ab.setMaximumExponent(1);
	ab.addTickSpec(level1, 0, false, "%3.1f");
	graph.draw(ab.createAxis());

	// case 2
	ab = new AxisBuilder.Linear(graph, 10.0, 0.0, 30.0, false,
				    "Axis Label");
	ab.setMaximumExponent(1);
	ab.addTickSpec(level0, 0, true, null);
	ab.addTickSpec(level2, 1, false, null);
	graph.draw(ab.createAxis());

	// case 3
	ab = new AxisBuilder.Linear(graph, 20.0, 0.0, 30.0, false, null);
	ab.setMaximumExponent(1);
	ab.addTickSpec(level0, 0, false, null);
	ab.addTickSpec(level1, 1, false, null);
	ab.addTickSpec(level2, 2, false, null);
	graph.draw(ab.createAxis());

	// case 4
	ab = new AxisBuilder.Linear(graph, 30.0, 0.0, 30.0, false, null);
	ab.setMaximumExponent(1);
	ab.setNumberOfSteps(10);
	ab.addTickSpec(level0, 0, false, null);
	ab.addTickSpec(level1, 1, false, null);
	ab.addTickSpec(level2, 5, null);
	graph.draw(ab.createAxis());

	// case 5
	ab = new AxisBuilder.Linear(graph, 40.0, 0.0, 30.0, false, null);
	ab.setMaximumExponent(1);
	ab.setNumberOfSteps(4);
	ab.addTickSpec(level0, 0, false, null);
	ab.addTickSpec(level1, 2, null);
	ab.addTickSpec(level2, 4, null);
	graph.draw(ab.createAxis());

	graph.write("png", "abtest3.png");

	System.out.println("GRAPH 4");

	graph = new Graph(800, 600);
	graph.setRanges(0.0, 40.0, 0.0, 30.0);
	graph.setOffsets(75, 75);
	graph.setBackgroundColor(Color.WHITE);
	graph.clear();

	ab = new AxisBuilder.Linear(graph, 0.0, 0.0, 30.0, false, true,
				    "Axis Label");
	ab.setMaximumExponent(1);
	ab.addTickSpec(level0, 0, false, "%3.1f");
	graph.draw(ab.createAxis());
	
	ab = new AxisBuilder.Linear(graph, 10.0, 0.0, 30.0, false, true,
				    "Axis Label");
	ab.setMaximumExponent(1);
	ab.addTickSpec(level0, 0, true, null);
	ab.addTickSpec(level2, 1, false, null);
	graph.draw(ab.createAxis());

	ab = new AxisBuilder.Linear(graph, 20.0, 0.0, 30.0, false, true, null);
	ab.setMaximumExponent(1);
	ab.addTickSpec(level0, 0, false, null);
	ab.addTickSpec(level1, 1, false, null);
	ab.addTickSpec(level2, 2, false, null);
	graph.draw(ab.createAxis());

	ab = new AxisBuilder.Linear(graph, 30.0, 0.0, 30.0, false, true, null);
	ab.setMaximumExponent(1);
	ab.setNumberOfSteps(10);
	
	ab.addTickSpec(level0, 0, false, null);
	ab.addTickSpec(level1, 1, false, null);
	ab.addTickSpec(level2, 5, null);
	graph.draw(ab.createAxis());

	ab = new AxisBuilder.Linear(graph, 40.0, 0.0, 30.0, false, true, null);
	ab.setMaximumExponent(1);
	ab.setNumberOfSteps(4);
	ab.addTickSpec(level0, 0, false, null);
	ab.addTickSpec(level1, 2, null);
	ab.addTickSpec(level2, 4, null);
	graph.draw(ab.createAxis());

	graph.write("png", "abtest4.png");

	System.out.println("GRAPH 5");

	graph = new Graph(800, 600);
	graph.setRanges(30.0, 0.0, 0.0, 40.0);
	graph.setOffsets(75, 75);
	graph.setBackgroundColor(Color.WHITE);
	graph.clear();

	ab = new AxisBuilder.Linear(graph, 0.0, 0.0, 30, true,
				   "Axis Label");
	ab.setMaximumExponent(1);
	ab.addTickSpec(level1, 0, false, "%3.1f");
	graph.draw(ab.createAxis());
	
	ab = new AxisBuilder.Linear(graph, 0.0, 10.0, 30.0, true,
				    "Axis Label");
	ab.setMaximumExponent(1);
	ab.addTickSpec(level0, 0, true, null);
	ab.addTickSpec(level2, 1, false, null);
	graph.draw(ab.createAxis());

	ab = new AxisBuilder.Linear(graph, 0.0, 20.0, 30.0, true, null);
	ab.setMaximumExponent(1);
	ab.addTickSpec(level0, 0, false, null);
	ab.addTickSpec(level1, 1, false, null);
	ab.addTickSpec(level2, 2, false, null);
	graph.draw(ab.createAxis());

	ab = new AxisBuilder.Linear(graph, 0.0, 30.0, 30.0, true, null);
	ab.setMaximumExponent(1);
	ab.setNumberOfSteps(10);
	
	ab.addTickSpec(level0, 0, false, null);
	ab.addTickSpec(level1, 1, false, null);
	ab.addTickSpec(level2, 5, null);
	graph.draw(ab.createAxis());

	ab = new AxisBuilder.Linear(graph, 0.0, 40.0, 30.0, true, null);
	ab.setMaximumExponent(1);
	ab.setNumberOfSteps(4);
	ab.addTickSpec(level0, 0, false, null);
	ab.addTickSpec(level1, 2, null);
	ab.addTickSpec(level2, 4, null);
	graph.draw(ab.createAxis());

	graph.write("png", "abtest5.png");

	System.out.println("GRAPH 6");

	graph = new Graph(800, 600);
	graph.setRanges(30.0, 0.0, 0.0, 40.0);
	graph.setOffsets(75, 75);
	graph.setBackgroundColor(Color.WHITE);
	graph.clear();

	ab = new AxisBuilder.Linear(graph, 0.0, 0.0, 30, true, true,
				    "Axis Label");
	ab.setMaximumExponent(1);
	ab.addTickSpec(level0, 0, false, "%3.1f");
	graph.draw(ab.createAxis());
	
	ab = new AxisBuilder.Linear(graph, 0.0, 10.0, 30.0, true, true,
				    "Axis Label");
	ab.setMaximumExponent(1);
	ab.addTickSpec(level0, 0, true, null);
	ab.addTickSpec(level2, 1, false, null);
	graph.draw(ab.createAxis());

	ab = new AxisBuilder.Linear(graph, 0.0, 20.0, 30.0, true, true, null);
	ab.setMaximumExponent(1);
	ab.addTickSpec(level0, 0, false, null);
	ab.addTickSpec(level1, 1, false, null);
	ab.addTickSpec(level2, 2, false, null);
	graph.draw(ab.createAxis());

	ab = new AxisBuilder.Linear(graph, 0.0, 30.0, 30.0, true, true, null);
	ab.setMaximumExponent(1);
	ab.setNumberOfSteps(10);
	
	ab.addTickSpec(level0, 0, false, null);
	ab.addTickSpec(level1, 1, false, null);
	ab.addTickSpec(level2, 5, null);
	graph.draw(ab.createAxis());

	ab = new AxisBuilder.Linear(graph, 0.0, 40.0, 30.0, true, true, null);
	ab.setMaximumExponent(1);
	ab.setNumberOfSteps(4);
	ab.addTickSpec(level0, 0, false, null);
	ab.addTickSpec(level1, 2, null);
	ab.addTickSpec(level2, 4, null);
	graph.draw(ab.createAxis());

	graph.write("png", "abtest6.png");

	System.out.println("GRAPH 7");
	graph = new Graph(800, 600);
	graph.setRanges(0.0, 40.0, 30.0, 0.0);
	graph.setOffsets(75, 75);
	graph.setBackgroundColor(Color.WHITE);
	graph.clear();

	ab =new AxisBuilder.Linear(graph, 0.0, 0.0, 30, false, "Axis Label");
	ab.setMaximumExponent(1);
	ab.addTickSpec(level0, 0, false, "%3.1f");
	graph.draw(ab.createAxis());
	
	ab = new AxisBuilder.Linear(graph, 10.0, 0.0, 30.0, false,
				    "Axis Label");
	ab.setMaximumExponent(1);
	ab.addTickSpec(level0, 0, true, null);
	ab.addTickSpec(level2, 1, false, null);
	graph.draw(ab.createAxis());

	ab = new AxisBuilder.Linear(graph, 20.0, 0.0, 30.0, false, null);
	ab.setMaximumExponent(1);
	ab.addTickSpec(level0, 0, false, null);
	ab.addTickSpec(level1, 1, false, null);
	ab.addTickSpec(level2, 2, false, null);
	graph.draw(ab.createAxis());

	ab = new AxisBuilder.Linear(graph, 30.0, 0.0, 30.0, false, null);
	ab.setMaximumExponent(1);
	ab.setNumberOfSteps(10);
	
	ab.addTickSpec(level0, 0, false, null);
	ab.addTickSpec(level1, 1, false, null);
	ab.addTickSpec(level2, 5, null);
	graph.draw(ab.createAxis());

	ab = new AxisBuilder.Linear(graph, 40.0, 0.0, 30.0, false, null);
	ab.setMaximumExponent(1);
	ab.setNumberOfSteps(4);
	ab.addTickSpec(level0, 0, false, null);
	ab.addTickSpec(level1, 2, null);
	ab.addTickSpec(level2, 4, null);
	graph.draw(ab.createAxis());

	graph.write("png", "abtest7.png");

	System.out.println("GRAPH 8");

	graph = new Graph(800, 600);
	graph.setRanges(0.0, 40.0, 30.0, 0.0);
	graph.setOffsets(75, 75);
	graph.setBackgroundColor(Color.WHITE);
	graph.clear();

	ab = new AxisBuilder.Linear(graph, 0.0, 0.0, 30.0, false, true,
				    "Axis Label");
	ab.setMaximumExponent(1);
	ab.addTickSpec(level0, 0, false, "%3.1f");
	graph.draw(ab.createAxis());
	
	ab = new AxisBuilder.Linear(graph, 10.0, 0.0, 30.0, false, true,
				    "Axis Label");
	ab.setMaximumExponent(1);
	ab.addTickSpec(level0, 0, true, null);
	ab.addTickSpec(level2, 1, false, null);
	graph.draw(ab.createAxis());

	ab = new AxisBuilder.Linear(graph, 20.0, 0.0, 30.0, false, true, null);
	ab.setMaximumExponent(1);
	ab.addTickSpec(level0, 0, false, null);
	ab.addTickSpec(level1, 1, false, null);
	ab.addTickSpec(level2, 2, false, null);
	graph.draw(ab.createAxis());

	ab = new AxisBuilder.Linear(graph, 30.0, 0.0, 30.0, false, true, null);
	ab.setMaximumExponent(1);
	ab.setNumberOfSteps(10);
	
	ab.addTickSpec(level0, 0, false, null);
	ab.addTickSpec(level1, 1, false, null);
	ab.addTickSpec(level2, 5, null);
	graph.draw(ab.createAxis());

	ab  = new AxisBuilder.Linear(graph, 40.0, 0.0, 30.0, false, true, null);
	ab.setMaximumExponent(1);
	ab.setNumberOfSteps(4);
	ab.addTickSpec(level0, 0, false, null);
	ab.addTickSpec(level1, 2, null);
	ab.addTickSpec(level2, 4, null);
	graph.draw(ab.createAxis());

	graph.write("png", "abtest8.png");

    }
}