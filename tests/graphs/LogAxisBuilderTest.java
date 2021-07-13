import org.bzdev.graphs.*;
import java.awt.Color;

public class LogAxisBuilderTest {
    public static void main(String argv[]) throws Exception {
	Graph graph = new Graph(800, 600);
	graph.setRanges(-1.0, 2.0, 0.0, 40.0);
	graph.setOffsets(75, 75);
	graph.setBackgroundColor(Color.WHITE);
	graph.clear();

	AxisBuilder.Log ab = 
	    new AxisBuilder.Log(graph, -1.0, 0.0, 3.0, true, "Axis Label");
	ab.addTickSpec(0, false, "%#.0g");
	graph.draw(ab.createAxis());

	System.out.println("case 2");
	ab = new AxisBuilder.Log(graph, -1.0, 10.0, 3.0, true, null);
	ab.addTickSpec(0, true, null);
	ab.addTickSpec(2, 1);
	graph.draw(ab.createAxis());
	
	System.out.println("case 3");
	ab = new AxisBuilder.Log(graph, -1.0, 20.0, 3.0, true, "cutoff test");
	ab.addTickSpec(0, false, null);
	ab.addTickSpec(1, 1, 5);
	ab.addTickSpec(2, 1);
	ab.addTickSpec(3, 2, 5);
	graph.draw(ab.createAxis());

	System.out.println("case 4");
	ab = new AxisBuilder.Log(graph, -1.0, 30.0, 3.0, true, "div test");
	ab.addTickSpec(0, true, null);
	ab.addTickSpec(2, 1);
	ab.addTickSpec(3, 1, 2, 3);
	ab.addTickSpec(4, 1, 10, 3);
	ab.addTickSpec(4, 1, 5, 7);
	ab.addOneTick(3, 7.5);
	ab.addOneTick(3, 8.5);
	ab.addOneTick(3, 9.5);
	graph.draw(ab.createAxis());


	graph.write("png", "logabtest1.png");



    }
}