import java.awt.*;
import java.awt.geom.*;
import java.io.FileOutputStream;

import org.bzdev.geom.*;
import org.bzdev.graphs.*;
import org.bzdev.gio.*;
import org.bzdev.math.VectorOps;

public class CondTest {

    public static void main(String argv[]) throws Exception {

	Path2D path1 = new Path2D.Double();
	path1.moveTo(0.0, 0.0);
	path1.curveTo(90.0, 20.0, 10.0, -20.0, 100.0, 0.0);

	PathIterator pi = new ConditionalPathIterator2D
	    (path1.getPathIterator(null), (coords) -> {
		    if (coords.length == 8) {
			double[] v1 = VectorOps.sub(null,0,coords,6,coords,0,2);
			double[] v2 = VectorOps.sub(null,0,coords,4,coords,2,2);
			return VectorOps.dotProduct(v1,v2) < 0.0;
		    } else {
			return false;
		    }
		});
	Path2D path2 = (new Path2D.Double());
	path2.append(pi, false);

	Path2D path1cp = Path2DInfo.controlPointPolygon(path1, true, null);
	Path2D path2cp = Path2DInfo.controlPointPolygon(path2, true, null);

	OutputStreamGraphics osg1 = OutputStreamGraphics
	    .newInstance(new FileOutputStream("condtest1.ps"), 300, 200, "ps");

	OutputStreamGraphics osg2 = OutputStreamGraphics
	    .newInstance(new FileOutputStream("condtest2.ps"), 300, 200, "ps");

	Graph g1 = new Graph(osg1);
	Graph g2 = new Graph(osg2);

	g1.setRanges(-100.0, 200.0, -100.0, 100.0);
	g2.setRanges(-100.0, 200.0, -100.0, 100.0);
	
	Graphics2D g2d = g1.createGraphics();
	g2d.setColor(Color.BLUE);
	g2d.setStroke(new BasicStroke(2.0F));
	g1.draw(g2d, path1cp);
	g2d.setColor(Color.BLACK);
	g2d.setStroke(new BasicStroke(1.0F));
	g1.draw(g2d, path1);
	g1.write();

	g2d = g2.createGraphics();
	g2d.setColor(Color.BLUE);
	g2d.setStroke(new BasicStroke(2.0F));
	g2.draw(g2d, path2cp);
	g2d.setColor(Color.BLACK);
	g2d.setStroke(new BasicStroke(1.0F));
	g2.draw(g2d, path2);
	g2.write();
    }
}
