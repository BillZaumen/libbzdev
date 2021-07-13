import org.bzdev.graphs.Graph;
import org.bzdev.gio.OutputStreamGraphics;
import org.bzdev.geom.*;
import java.awt.geom.Path2D;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.BasicStroke;
import java.io.*;

public class PathLength {
    static final int M = 4;
    static final int N = 36 * M;
    public static void main(String argv[]) {
	double x[] = new double[N];
	double y[] = new double[N];


	// set the coordinates of the points along a curve
	for (int i = 0; i < N; i++) {
	    double theta = Math.toRadians(i * 10.0 / M);
	    double u = Math.sin(M * theta);
	    double r = 10.0 + 5.0 * u * u;
	    x[i] = r * Math.cos(theta);
	    y[i] = r * Math.sin(theta);
	}
	// create a smooth curve, making it closed
	Path2D path = new SplinePath2D(x, y, N, true);

	// draw the curve on a graph.
	try {
	    String type = "png";
	    if (argv.length > 0) type = argv[0];
	    OutputStream os = new FileOutputStream("path." + type);
	    OutputStreamGraphics osg =
		OutputStreamGraphics.newInstance(os, 600, 600, type);
	    Graph.setDefaultBackgroundColor(Color.WHITE);
	    Graph graph = new Graph(osg);
	    graph.setOffsets(20, 20);
	    graph.setRanges(-15.0, 15.0, -15.0, 15.0);
	    Graphics2D g2d = graph.createGraphics();
	    g2d.setColor(Color.BLACK);
	    g2d.setStroke(new BasicStroke(2.0F));
	    graph.draw(g2d, path);
	    graph.write();
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	
	System.out.println("path length = " + Path2DInfo.pathLength(path));
	System.exit(0);
    }
}
