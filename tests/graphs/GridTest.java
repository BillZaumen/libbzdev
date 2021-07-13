import org.bzdev.graphs.*;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

public class GridTest {
    public static void main(String argv[]) throws Exception {

	Graph graph = new Graph(1600, 1200);
	Graphs.CartesianGrid grid = new Graphs.CartesianGrid();

	double scalef = 1200/100.0;

	graph.setRanges(0.0, 0.0, 0.5, 0.5, scalef, scalef);
	graph.setBackgroundColor(Color.WHITE);
	graph.clear();

	graph.add(grid);

	graph.write("png", "testgrid1.png");

	graph = new Graph(1600, 1200);
	graph.setRanges(0.0, 0.0, 0.5, 0.5, scalef, scalef);
	graph.setBackgroundColor(Color.WHITE);
	graph.clear();

	grid.setSpacing(20.0);
	grid.setSubspacing(5);
	grid.setStrokeWidth(4.0);
	grid.setColors(Colors.getColorByCSS("black"),
		       Colors.getColorByCSS("gray"),
		       Colors.getColorByCSS("lightgray"));

	graph.add(grid);
	graph.write("png", "testgrid2.png");

	graph = new Graph(1600+30, 1200+70);
	grid = new Graphs.CartesianGrid();

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
	graph.add(grid);
	graph.write("png", "testgrid3.png");
	System.exit(0);
    }
}
