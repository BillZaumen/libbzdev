import org.bzdev.graphs.*;

import java.awt.*;
import java.awt.geom.*;
import java.io.*;

import org.bzdev.gio.OutputStreamGraphics;
import org.bzdev.geom.SplinePath2D;
import org.bzdev.math.rv.GaussianRV;
import org.bzdev.util.StaticRandom;

public class TestExample {

    public static void main(String argv[]) {
	try {
	    // maximize the quality of the random number generator
	    // by using an implementation suitable for cryptography
	    StaticRandom.maximizeQuality();
	    // create an output stream that will write to a file
	    FileOutputStream os = new FileOutputStream("exampleGraph.ps");

	    // Set up the output stream so that the file will be a
	    // Postscript file containing a graph
	    OutputStreamGraphics osg = OutputStreamGraphics.newInstance
		(os, Graph.DEFAULT_WIDTH, Graph.DEFAULT_HEIGHT, "ps");
	    // Create a graph whose output will be to the Postscript
	    // output stream just created.
	    Graph graph = new Graph(osg);
	    //Configure the graph - add axes, points, etc.
	    configureGraph(graph);
	    // complete writing the graph to the output stream
	    graph.write();
	    // close the output stream.
	    os.close();

	    // repeat the above but creating a PNG file instead of a
	    // Postscript file.
	    os = new FileOutputStream("exampleGraph.png");
	    osg = OutputStreamGraphics.newInstance
		(os, Graph.DEFAULT_WIDTH, Graph.DEFAULT_HEIGHT, "png");
	    graph = new Graph(osg);
	    configureGraph(graph);

	    graph.write();
	    os.close();

	    // terminate the program with an exit code of 0 indicating success
	    System.exit(0);
	} catch (Exception e) {
	    // handle an error by showing where it occurred and terminating
	    // the program with an exit code of 1
	    e.printStackTrace();
	    System.exit(1);
	}
	System.exit(0);
    }

    static void configureGraph(Graph graph) {
	// Set offsets to allow room for tick marks, axes, and labels
	graph.setOffsets(75,75);
	// set range of values to be displayed
	graph.setRanges(0.0, 360.0, -1.0, 1.0);
	// horizontal axis at y = 0 with no labels or tick marks
	Graph.Axis oAxis = new Graph.Axis (0.0, 0.0,
	      Graph.Axis.Dir.HORIZONTAL_INCREASING, 360.0, 0.0, 1.0, false);
	oAxis.setWidth(4.0);
	graph.draw(oAxis);

	// horizontal axis at bottom with tick marks
	Graph.Axis xAxis = new Graph.Axis(0.0, -1.0, 
	      Graph.Axis.Dir.HORIZONTAL_INCREASING, 360.0, 0.0, 1.0, false);
	xAxis.setWidth(4.0);
	xAxis.addTick(new Graph.TickSpec(4.0, 1.0, 30, "%1.0f", 1.0));
	xAxis.addTick(new Graph.TickSpec(4.0, 1.0, 10));
	graph.draw(xAxis);
	// vertical axis on left with tick marks
	Graph.Axis yAxis = new Graph.Axis(0.0, -1.0,
	      Graph.Axis.Dir.VERTICAL_INCREASING, 2.0, -1.0, 0.01, true);
	yAxis.addTick(new Graph.TickSpec(4.0, 1.0, 50, "%2.1f", 1.0));
	yAxis.addTick(new Graph.TickSpec(2.0, 1.0, 10));
	yAxis.addTick(new Graph.TickSpec(1.5, 0.5, 2));
	yAxis.setWidth(4.0);
	graph.draw(yAxis);
	// create a curve giving y = sin(x) by calculating values
	// at 37 points and connecting them with a smooth curve
	Point2D  points[] = new Point2D[37];
	for (int i = 0; i < points.length; i++) {
	    double x = i * 10.0;
	    double y = Math.sin(Math.toRadians(x));
	    points[i] = new Point2D.Double(x, y);
	}
	Path2D path = new SplinePath2D(points, points.length, false);
	// add the path to the graph, setting its color and the
	// line thickness
	Graphics2D g2d = graph.createGraphics();
	g2d.setColor(Color.BLACK);
	g2d.setStroke(new BasicStroke(2.0F));
	graph.draw(g2d, path);
	// Add some symbols, using a random valiable
	// to construct typical points
	Graph.SymbolFactory sf = new Graph.SymbolFactory();
	sf.setColor(Color.BLACK);
	sf.setLineThickness(2.0);
	Graph.Symbol symbol = sf.newSymbol("SolidCircle");
	GaussianRV rv = new GaussianRV(0.0, 0.1);
	for (int i = 1; i < 12; i++) {
	    double x = i * 30.0;
	    double y = Math.sin(Math.toRadians(x)) + rv.next();
	    if (y <= 0.9 && y >= -0.9) graph.drawEY(symbol, x, y, 0.1);
	}
    }
}
