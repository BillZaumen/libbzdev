import org.bzdev.math.*;
import org.bzdev.gio.*;
import org.bzdev.graphs.*;
import org.bzdev.geom.*;
import org.bzdev.math.StaticRandom;
import org.bzdev.math.rv.GaussianRV;
import java.awt.*;
import java.awt.geom.*;
import java.io.*;

public class FitTest2 {
    public static void main(String argv[]) {
	try {
	    // Create an SVG-graphics output stream.
	    File ofile = new File(argv[0]);
	    FileOutputStream fos = new FileOutputStream(ofile);
	    OutputStreamGraphics osg =
		OutputStreamGraphics.newInstance(fos, 1024, 768, "svg");

	    // Create a Graph and configure it.
	    Graph graph = new Graph(osg);
	    graph.setOffsets(75, 75);
	    graph.setRanges(0.0, 10.0, -0.5, 1.0);

	    // create an X axis
	    AxisBuilder.Linear xab = new
		AxisBuilder.Linear(graph, 0.0, -0.5, 10.0, true, null);
	    xab.setMaximumExponent(0);
	    xab.addTickSpec(0, 0, false, "%4.1f");

	    // create a Y axis
	    AxisBuilder.Linear yab = new
		AxisBuilder.Linear(graph, 0.0, -0.5, 1.5, false, null);
	    xab.setMaximumExponent(0);
	    yab.addTickSpec(0, 0, false, "%4.1f");
	    yab.addTickSpec(1, 1, false, null);

	    Graphics2D g2d = graph.createGraphics();
	    g2d.setColor(Color.BLUE);
	    g2d.setStroke(new BasicStroke(1.0F));

	    graph.draw(g2d, new Line2D.Double(0.0, 0.0, 10.0, 0.0));

	    // draw the X axis and the Y axis
	    graph.draw(xab.createAxis());
	    graph.draw(yab.createAxis());

	    // Get a symbol for data points.
	    Graph.SymbolFactory sf = new Graph.SymbolFactory();
	    sf.setColor(Color.BLACK);
	    sf.setLineThickness(1.5);
	    Graph.Symbol symbol = sf.newSymbol("SolidCircle");

	    // Simulate data with errors
	    StaticRandom.maximizeQuality();

	    double xs[] = new double[101];
	    double ys[] = new double[101];
	    double[] ydata = new double[101];
	    double sigma = 0.1;
	    GaussianRV rv = new GaussianRV(0.0, sigma);

	    int j = 0;
	    for (int i = 0; i <= 100; i++) {
		double x = i / 10.0;
		double y = Functions.J(0, x);
		xs[j] = x;
		double yd =  y + rv.next();
		ydata[j] = yd;
		if (i % 5 == 0) {
		    // plot every 5th data point, with error bars
		    graph.drawEY(symbol, x, yd, sigma);
		}
		ys[j++] = y;
		
	    }

	    // Draw the exact graph.
	    SplinePath2D path = new SplinePath2D(xs, ys, false);
	    g2d.setColor(Color.GREEN);
	    g2d.setStroke(new BasicStroke(2.0F));
	    graph.draw(g2d, path);

	    // Do a least squares fit to the data that has
	    // errors added to it.
	    LeastSquaresFit fit =
		new LeastSquaresFit.BSpline(3, 10, xs, ydata, sigma);

	    // Create a path for the least squares fit and draw it.
	    path = new SplinePath2D(RealValuedFunction.xFunction,
				    fit, 0.0, 10.0, 60, false);

	    g2d.setColor(Color.BLACK);
	    graph.draw(g2d, path);

	    graph.write();
	    System.exit(0);
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
    }
}
