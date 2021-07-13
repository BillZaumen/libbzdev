import org.bzdev.geom.*;
import org.bzdev.graphs.*;
import java.awt.geom.*;
import java.awt.*;

public class SubdivTest2 {
    public static void main(String argv[]) throws Exception {

	Surface3D surface = new Surface3D.Double();

	double[] pathcoords = new double[12];
 
	double[] edge1 = Path3D.setupCubic(0.0, 0.0, 0.0, 600.0, 0.0, 0.0);
	double[] edge2 = Path3D.setupCubic(600.0, 0.0, 0.0, 300.0, 600.0, 0.0);
	double[] edge3 = Path3D.setupCubic(0.0, 0.0, 0.0, 300.0, 600.0, 0.0);

	double[] tcoords = new double[48];

	Surface3D.setupV0ForTriangle(edge1, tcoords, false);
	Surface3D.setupW0ForTriangle(edge2, tcoords,false);
	Surface3D.setupU0ForTriangle(edge3, tcoords,false);
	Surface3D.setupPlanarCP111ForTriangle(tcoords);
	surface.addCubicTriangle(tcoords);

	/*
	surface.addPlanarTriangle(0.0, 0.0, 0.0,
				  600.0, 0.0, 0.0,
				  300.0, 600.0, 0.0);
	*/

	SurfaceIterator si = surface.getSurfaceIterator(null, 2);

	Path2D path = new Path2D.Double();

	double[] coords = new double[48];
	int entry = -1;
	while (!si.isDone()) {
	    int type = si.currentSegment(coords);
	    if (type == SurfaceIterator.CUBIC_TRIANGLE) {
		path.moveTo(coords[0], coords[1]);
		path.curveTo(coords[12], coords[13],
			     coords[21], coords[22],
			     coords[27], coords[28]);
		path.curveTo(coords[24], coords[25],
			     coords[18], coords[19],
			     coords[9], coords[10]);
		path.curveTo(coords[6], coords[7],
			     coords[3], coords[4],
			     coords[0], coords[1]);
		System.out.println("Entry " + (++entry));
		System.out.format("    (%g, %g)-(%g, %g)-(%g, %g)-(%g, %g)\n",
				  coords[0], coords[1],
				  coords[12], coords[13],
				  coords[21], coords[22],
				  coords[27], coords[28]);
		System.out.format("    (%g, %g)-(%g, %g)-(%g, %g)-(%g, %g)\n",
				  coords[27], coords[28],
				  coords[24], coords[25],
				  coords[18], coords[19],
				  coords[9], coords[10]);
		System.out.format("    (%g, %g)-(%g, %g)-(%g, %g)-(%g, %g)\n",
				  coords[9], coords[10],
				  coords[6], coords[7],
				  coords[3], coords[4],
				  coords[0], coords[1]);
	    } else if (type == SurfaceIterator.PLANAR_TRIANGLE) {
		path.moveTo(coords[0], coords[1]);
		path.lineTo(coords[6], coords[7]);
		path.lineTo(coords[3], coords[4]);
		path.lineTo(coords[0], coords[1]);
		System.out.println("Entry " + (++entry));
		System.out.format("    (%g, %g)-(%g, %g74)\n",
				  coords[0], coords[1],
				  coords[6], coords[7]);
		System.out.format("    (%g, %g)-(%g, %g)\n",
				  coords[6], coords[7],
				  coords[3], coords[4]);
		System.out.format("    (%g, %g)-(%g, %g)\n",
				  coords[3], coords[4],
				  coords[0], coords[1]);

	    }
	    si.next();
	}
	
	Graph graph = new Graph(800,800);
	graph.setOffsets(50, 50);
	graph.setRanges(0.0, 600.0, 0.0, 600.0);
	Graphics2D g2d = graph.createGraphics();

	g2d.setStroke(new BasicStroke(1.5F));
	g2d.setColor(Color.BLACK);

	graph.draw(g2d, path);

	graph.write("png", "subdiv2.png");

	System.exit(0);
   }
}
