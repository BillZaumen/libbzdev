import org.bzdev.graphs.*;
import java.awt.*;
import java.awt.image.*;
import javax.imageio.ImageIO;
import java.awt.geom.*;
import java.io.*;

import org.bzdev.gio.OutputStreamGraphics;
import org.bzdev.gio.ImageSequenceWriter;
import org.bzdev.gio.ISWriterOps;
import org.bzdev.gio.ImageOrientation;

public class RadialTest {




    public static void main(String argv[]) throws Exception {
	Graph graph = new Graph(800, 600);
	double sf = 800/100;
	graph.setRanges(0.0, 0.0, 0.5, 0.5, sf, sf);
	
	Graphics2D g2dGCS = graph.createGraphicsGCS();

	Ellipse2D circle = new Ellipse2D.Double(-25.0, -25.0, 50.0, 50.0);

	float fractions[] = {0.0F, 1.0F};
	int level = 64;
	Color colors[] = {
	    Color.WHITE,
	    new Color(level, level, level)
	};

	Graphics2D g2d = graph.createGraphics();
	g2d.setColor(new Color(level,level,level));
	Rectangle2D rect = new Rectangle2D.Double(-25.0, -25.0, 50.0, 50.0);
	graph.fill(g2d, rect);
	graph.draw(g2d, rect);


	RadialGradientPaint paint = new
	    RadialGradientPaint(new Point2D.Double(0.0, 0.0),
				25.0F, fractions, colors);
	
	g2dGCS.setPaint(paint);
	g2dGCS.fill(circle);

	graph.write("png", "radial.png");
    }
}
