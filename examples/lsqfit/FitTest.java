import org.bzdev.math.*;
import org.bzdev.gio.*;
import org.bzdev.graphs.*;
import org.bzdev.geom.*;
import org.bzdev.math.StaticRandom;
import org.bzdev.math.rv.GaussianRV;
import java.awt.*;
import java.awt.geom.*;
import java.io.*;

public class FitTest {
    public static void main(String argv[]) {
	try {
	    // Create a postscript-graphics output stream.
	    File ofile = new File(argv[0]);
	    FileOutputStream fos = new FileOutputStream(ofile);
	    OutputStreamGraphics osg =
		OutputStreamGraphics.newInstance(fos, 1024, 768, "ps");

	    // Create a Graph and configure it.
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

	    xAxis.setLabelOffset(10.0);
	    Graph.Axis yAxis =
		new Graph.Axis(0.0, -0.5,
			       Graph.Axis.Dir.VERTICAL_INCREASING,
			       1.5,
			       -1.0, 0.1, true);
	    yAxis.addTick(new Graph.TickSpec(5.0, 2.0, 1));
	    yAxis.addTick(new Graph.TickSpec(10.0, 2.0, 10,
					     "%4.1f", 5.0));

	    Graphics2D g2d = graph.createGraphics();
	    g2d.setColor(Color.BLUE);
	    g2d.setStroke(new BasicStroke(1.0F));

	    graph.draw(g2d, new Line2D.Double(0.0, 0.0, 10.0, 0.0));

	    graph.draw(xAxis);
	    graph.draw(yAxis);

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
		    // plot every 5th data point
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
