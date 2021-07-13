import org.bzdev.graphs.*;
import org.bzdev.gio.*;
import java.io.*;

public class LogGraph {
    public static void main(String argv[]) {
	try {
	    File ofile = new File(argv[0]);
	    FileOutputStream fos = new FileOutputStream(ofile);
	    OutputStreamGraphics osg =
		OutputStreamGraphics.newInstance(fos, 600, 600, "ps");
	    Graph graph = new Graph(osg);
	    graph.setOffsets(75, 75);
	    graph.setRanges(0.0, 3.0, 0.0, 3.0);

	    Graph.Axis xAxis = new
		Graph.LogAxis(0.0, 0.0, Graph.Axis.Dir.HORIZONTAL_INCREASING,
			      3.0, 1.0, false);
	    xAxis.addTick(new Graph.TickSpec(4.0, 1.0, 9, "%#1.0g", 1.0));
	    xAxis.addTick(new Graph.TickSpec(3.0, 1.0, 9, 4));
	    xAxis.addTick(new Graph.TickSpec(2.0, 0.5, 1));
	    xAxis.addTick(new Graph.TickSpec(2.0, 1.0, 1, 0, 4));
	    xAxis.setWidth(2.0);
	    xAxis.setLabel("X Axis Label");

	    Graph.Axis yAxis = new
		Graph.LogAxis(0.0, 0.0, Graph.Axis.Dir.VERTICAL_INCREASING,
			      3.0, 1.0, true);
	    yAxis.addTick(new Graph.TickSpec(4.0, 1.0, 9, "%1.0f", 1.0));
	    yAxis.addTick(new Graph.TickSpec(3.0, 1.0, 9, 4));
	    yAxis.addTick(new Graph.TickSpec(2.0, 0.5, 1));
	    yAxis.addTick(new Graph.TickSpec(2.0, 1.0, 1, 0, 4));
	    yAxis.setWidth(2.0);
	    yAxis.setLabel("Y Axis Label");

	    graph.draw(xAxis);
	    graph.draw(yAxis);

	    graph.write();

	} catch (Exception e) {
	}
    }
}