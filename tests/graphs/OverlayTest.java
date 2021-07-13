import org.bzdev.graphs.*;
import org.bzdev.gio.*;
import java.awt.*;
import java.awt.geom.*;
import java.io.*;

public class OverlayTest {
    public static void main(String argv[]) {
	try {
	    FileOutputStream os = new FileOutputStream(argv[0]);
	    OutputStreamGraphics osg = OutputStreamGraphics.newInstance
		(os, Graph.DEFAULT_WIDTH, Graph.DEFAULT_HEIGHT, "ps");
	    Graph graph1 = new Graph(osg);
	    graph1.setOffsets(75, 75);
	    graph1.setRanges(0.0, 100.0, 0.0, 100.0);

	    Graph.Axis xAxis = new 
		Graph.Axis(0.0, 0.0, Graph.Axis.Dir.HORIZONTAL_INCREASING,
			   100.0, 0.0, 1.0, false);
	    xAxis.setWidth(2.0);
	    xAxis.addTick(new Graph.TickSpec(4.0, 1.0, 20, "%1.0f", 1.0));
	    
	    Graph.Axis yAxis = new 
		Graph.Axis(0.0, 0.0, Graph.Axis.Dir.VERTICAL_INCREASING,
			   100.0, 0.0, 1.0, true);
	    yAxis.setWidth(2.0);
	    yAxis.addTick(new Graph.TickSpec(4.0, 1.0, 20, "%1.0f", 1.0));

	    Graph graph2 = new Graph(graph1, true);
	    graph2.setRanges(0.0, 100.0, 0.0, 10.0);
	    
	    Graph.Axis yAxis2 = new 
		Graph.Axis(100.0, 0.0, Graph.Axis.Dir.VERTICAL_INCREASING,
			   10.0, 0.0, 0.1, false);
	    yAxis2.setWidth(2.0);
	    yAxis2.addTick(new Graph.TickSpec(4.0, 1.0, 20, "%1.0f", 1.0));
	    
	    graph1.draw(xAxis);
	    graph1.draw(yAxis);
	    graph2.draw(yAxis2);

	    Graphics2D g2d1 = graph1.createGraphics();
	    Graphics2D g2d2 = graph2.createGraphics();
	    g2d1.setColor(Color.BLACK);
	    g2d2.setColor(Color.BLUE);
	    g2d1.setStroke(new BasicStroke(2.0F));
	    g2d2.setStroke(new BasicStroke(2.0F));

	    graph1.draw(g2d1, new Line2D.Double(0.0, 0.0, 100.0, 75.0));
	    graph2.draw(g2d2, new Line2D.Double(0.0, 2.0, 100.0, 5.0));

	    graph1.write();

	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	System.exit(0);
    }
}
