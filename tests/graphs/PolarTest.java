import org.bzdev.graphs.*;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

public class PolarTest {
    public static void main(String argv[]) throws Exception {

	Graph graph = new Graph(1600, 1200);
	Graphs.PolarGrid polar = new Graphs.PolarGrid();

	double scalef = 1200/100.0;

	graph.setRanges(0.0, 0.0, 0.5, 0.5, scalef, scalef);
	graph.setBackgroundColor(Color.WHITE);
	graph.clear();

	graph.add(polar);

	graph.write("png", "testpolar1.png");

	graph = new Graph(1600, 1200);
	graph.setRanges(0.0, 0.0, 0.5, 0.5, scalef, scalef);
	graph.setBackgroundColor(Color.WHITE);
	graph.clear();

	polar.setRadialSpacing(10.0);
	polar.setAngularSpacing(5);
	polar.setStrokeWidth(2.0);
	polar.setColor(Colors.getColorByCSS("black"));

	graph.add(polar);
	graph.write("png", "testpolar2.png");

	graph = new Graph(1600, 1200);
	graph.setRanges(0.0, 0.0, 0.5, 0.5, scalef, scalef);
	graph.setBackgroundColor(Color.WHITE);
	graph.clear();
	polar = new Graphs.PolarGrid(10.0, 10);
	polar.setOrigin(-100.0, 0.0, false);
	graph.add(polar);
	graph.write("png", "testpolar3.png");
	graph.clear();
	polar.setOrigin(-100.0, 100.0, false);
	graph.add(polar);
	graph.write("png", "testpolar4.png");
	graph.clear();
	polar.setOrigin(-100.0, -100.0, false);
	graph.add(polar);
	graph.write("png", "testpolar5.png");
	graph.clear();
	polar.setOrigin(0.0, 100.0, false);
	graph.add(polar);
	graph.write("png", "testpolar6.png");
	polar.setOrigin(0.0, -100.0, false);
	graph.clear();
	graph.add(polar);
	graph.write("png", "testpolar7.png");
	graph.clear();
	polar.setOrigin(100.0, 100.0, false);
	graph.add(polar);
	graph.write("png", "testpolar8.png");
	graph.clear();
	polar.setOrigin(100.0, 0.0, false);
	graph.add(polar);
	graph.write("png", "testpolar9.png");
	graph.clear();
	polar.setOrigin(100.0, -100.0, false);
	graph.add(polar);
	graph.write("png", "testpolar10.png");
	
	graph = new Graph(1600+30, 1200+70);
	scalef = 1200/100.0;

	graph.setOffsets(10, 20, 30, 40);
	graph.setRanges(0.0, 0.0, 0.5, 0.5, scalef, scalef);
	graph.setBackgroundColor(Color.WHITE);
	graph.clear();

	Graphics2D g2d = graph.createGraphics();
	g2d.setColor(new Color(220, 220, 220));
	g2d.setStroke(new BasicStroke(2.0F));
	double xL = graph.getXLower();
	double yL = graph.getYLower();
	double w = graph.getXUpper() - graph.getXLower();
	double h = graph.getYUpper() - graph.getYLower();
	Rectangle2D r = new Rectangle2D.Double(xL, yL, w, h);

	graph.draw(g2d, r);
	graph.fill(g2d, r);
	polar  = new Graphs.PolarGrid();
	graph.add(polar);
	graph.write("png", "testpolar11.png");


	System.exit(0);
    }
}
