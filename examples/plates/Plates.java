import org.bzdev.math.*;
import org.bzdev.gio.*;
import org.bzdev.graphs.*;
import org.bzdev.geom.*;
import java.awt.*;
import java.awt.geom.*;
import java.io.*;

public class Plates {
    public static void main(String argv[]) {
	try {
	    File ofile = new File(argv[0]);
	    FileOutputStream fos = new FileOutputStream(ofile);
	    OutputStreamGraphics osg =
		OutputStreamGraphics.newInstance(fos, 1024, 768, "ps");

	    Graph graph = new Graph(osg);
	    graph.setOffsets(75, 75);
	    graph.setRanges(0.0, 10.0, -0.5, 1.0);

	    Graph.Axis xAxis =
		new Graph.Axis(0.0, -0.5,
			       Graph.Axis.Dir.HORIZONTAL_INCREASING,
			       10.0,
			       -1.0, 0.1, false);
	    xAxis.addTick(new Graph.TickSpec(10.0, 2.0, 10,
					     "%4.1f", 5.0));

	    xAxis.setLabel("Distance from center");
	    xAxis.setLabelOffset(10.0);
	    Graph.Axis yAxis =
		new Graph.Axis(0.0, -0.5,
			       Graph.Axis.Dir.VERTICAL_INCREASING,
			       1.5,
			       -1.0, 0.1, true);
	    yAxis.addTick(new Graph.TickSpec(5.0, 2.0, 1));
	    yAxis.addTick(new Graph.TickSpec(10.0, 2.0, 10,
					     "%4.1f", 5.0));

	    yAxis.setLabel("Electric Field");
	    yAxis.setLabelOffset(10.0);

	    Graphics2D g2d = graph.createGraphics();
	    g2d.setColor(Color.BLUE);
	    g2d.setStroke(new BasicStroke(1.0F));

	    graph.draw(g2d, new Line2D.Double(0.0, 0.0, 10.0, 0.0));

	    graph.draw(xAxis);
	    graph.draw(yAxis);

	    double xs[] = new double[101];
	    double ys[] = new double[101];
	    int j = 0;
	    for (int i = 0; i <= 100; i++) {
		double x = i / 10.0;
		double y = Functions.J(0, x);
		xs[j] = x;
		ys[j++] = y;
	    }
	    SplinePath2D path = new SplinePath2D(xs, ys, false);
	    g2d.setColor(Color.BLACK);
	    g2d.setStroke(new BasicStroke(2.0F));
	    graph.draw(g2d, path);
	    graph.write();
	    //  Second case
	    ofile = new File(argv[1]);
	    fos = new FileOutputStream(ofile);
	    osg = OutputStreamGraphics.newInstance(fos, 1024, 640, "ps");

	    graph = new Graph(osg);
	    graph.setOffsets(75, 75);
	    graph.setRanges(0.0, 10.0, -1.0, 1.0);

	    xAxis = new Graph.Axis(0.0, -0.2,
				   Graph.Axis.Dir.HORIZONTAL_INCREASING,
				   10.0, 0.0, 0.1, false);
	    xAxis.setLabel("Frequency");
	    xAxis.setLabelOffset(10.0);

	    // xAxis.addTick(new Graph.TickSpec(5.0, 2.0, 1));
	    xAxis.addTick(new Graph.TickSpec(10.0, 2.0, 10, "%4.1f", 5.0));

	    yAxis = new Graph.Axis(0.0, -0.2,
				   Graph.Axis.Dir.VERTICAL_INCREASING,
				   1.8, -1.0, 0.1, true);
	    yAxis.setLabel("Total Charge");
	    yAxis.setLabelOffset(10.0);

	    yAxis.addTick(new Graph.TickSpec(5.0, 2.0, 1));
	    yAxis.addTick(new Graph.TickSpec(10.0, 2.0, 10, "%4.1f", 5.0));

	    g2d = graph.createGraphics();
	    g2d.setColor(Color.BLUE);
	    g2d.setStroke(new BasicStroke(1.0F));

	    graph.draw(g2d, new Line2D.Double(0.0, 0.0, 10.0, 0.0));

	    graph.draw(xAxis);
	    graph.draw(yAxis);

	    xs = new double[101];
	    ys = new double[101];
	    j = 0;
	    for (int i = 0; i <= 100.0; i++) {
		double x = i / 10.0;
		double y;
		if (i == 0) {
		    y = 1.0;
		} else {
		    y = 2.0*Functions.J(1,x)/x;
		}
		xs[j] = x;
		ys[j++] = y;
	    }
	    path = new SplinePath2D(xs, ys, false);
	    g2d.setColor(Color.BLACK);
	    g2d.setStroke(new BasicStroke(2.0F));
	    graph.draw(g2d, path);
	    graph.write();

	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}

