import org.bzdev.graphs.*;

import java.awt.*;
import java.awt.geom.*;
import java.io.*;

import org.bzdev.gio.OutputStreamGraphics;
import org.bzdev.geom.SplinePath2D;
import org.bzdev.math.rv.GaussianRV;
import org.bzdev.math.StaticRandom;

public class DataPlot {
    public static void main(String argv[]) {
	StaticRandom.maximizeQuality();
	try {
	    FileOutputStream os = new FileOutputStream(argv[0]);
	    OutputStreamGraphics osg = OutputStreamGraphics.newInstance
		(os, Graph.DEFAULT_WIDTH, Graph.DEFAULT_HEIGHT, "ps");
	    Graph graph = new Graph(osg);
	    configureGraph(graph);
	    graph.write();
	    os.close();
	    System.exit(0);
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
    }

    static void configureGraph(Graph graph) {
	graph.setOffsets(75,75);
	graph.setRanges(0.0, 360.0, -1.0, 1.0);
	Graph.Axis oAxis = new Graph.Axis (0.0, 0.0,
	      Graph.Axis.Dir.HORIZONTAL_INCREASING, 360.0, 0.0, 1.0, false);
	oAxis.setWidth(1.0);
	graph.draw(oAxis);

	Graph.Axis xAxis = new Graph.Axis(0.0, -1.0, 
	      Graph.Axis.Dir.HORIZONTAL_INCREASING, 360.0, 0.0, 1.0, false);
	xAxis.setWidth(2.0);
	xAxis.addTick(new Graph.TickSpec(4.0, 1.0, 30, "%1.0f", 1.0));
	xAxis.addTick(new Graph.TickSpec(4.0, 1.0, 10));
	graph.draw(xAxis);

	Graph.Axis yAxis = new Graph.Axis(0.0, -1.0,
	      Graph.Axis.Dir.VERTICAL_INCREASING, 2.0, -1.0, 0.01, true);
	yAxis.addTick(new Graph.TickSpec(4.0, 1.0, 50, "%2.1f", 1.0));
	yAxis.addTick(new Graph.TickSpec(2.0, 1.0, 10));
	yAxis.addTick(new Graph.TickSpec(1.5, 0.5, 2));
	yAxis.setWidth(2.0);
	graph.draw(yAxis);

	Point2D  points[] = new Point2D[37];
	for (int i = 0; i < points.length; i++) {
	    double x = i * 10.0;
	    double y = Math.sin(Math.toRadians(x));
	    points[i] = new Point2D.Double(x, y);
	}
	Path2D path = new SplinePath2D(points, points.length, false);
	Graphics2D g2d = graph.createGraphics();
	g2d.setColor(Color.BLACK);
	g2d.setStroke(new BasicStroke(2.0F));
	graph.draw(g2d, path);

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
