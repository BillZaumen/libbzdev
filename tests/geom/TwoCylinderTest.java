import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import org.bzdev.geom.*;
import org.bzdev.p3d.*;

public class TwoCylinderTest {

    public static void main(String argv[]) throws Exception {

	Path2D circle1 = Paths2D.createArc(0.0, 0.0, 0.0, -50.0,
					  2*Math.PI, Math.PI/4);
	circle1.closePath();
	
	Path2D circle2 = Paths2D.createArc(0.0, 0.0, 0.0, -100.0,
					  2*Math.PI, Math.PI/4);
	circle2.closePath();
	
	BezierGrid grid1 = new
	    BezierGrid(circle1, (ind, p, type, bounds) -> {
		    return new Point3D.Double(p.getX(),
					      p.getY(),
					      100 - 100*ind);  
	    }, 2, false);


	BezierGrid grid2 = new
	    BezierGrid(circle2, (ind, p, type, bounds) -> {
		    return new Point3D.Double(p.getX(),
					      p.getY(),
					      - 100*ind);  
	    }, 2, false);


	Surface3D surface = new Surface3D.Double();
	surface.append(grid1);
	surface.append(grid2);

	Path3D boundary1 = grid1.getBoundary(0, 0);
	Path3D boundary2 = grid1.getBoundary(1, 0);
	Path3D boundary3 = grid2.getBoundary(0, 0);
	Path3D boundary4 = grid2.getBoundary(1, 0);

	BezierVertex v1 = new BezierVertex(boundary1, 0.0);
	v1.reverseOrientation(true);
	BezierVertex v2 = new BezierVertex(boundary4, 0.0);
	v2.reverseOrientation(true);

	ConvexPathConnector c1 = new
	    ConvexPathConnector(boundary2, boundary3);

	surface.append(v1);
	surface.append(c1);
	surface.append(v2);

	Model3D m3d = new Model3D();
	m3d.append(surface);
	m3d.setTessellationLevel(4);

	Model3D.Image image = new
	    Model3D.Image(400, 400, BufferedImage.TYPE_INT_ARGB);

	Graphics2D g2d = image.createGraphics();
	g2d.setBackground(Color.BLUE.darker().darker());
	g2d.clearRect(0, 0, 400, 400);
	g2d.dispose();
	image.setCoordRotation(0.0, Math.toRadians(60.0), 0.0);
	image.setColorFactor(0.5);
	image.setNormalFactor(0.5);
	m3d.setImageParameters(image, 50.0);
	m3d.render(image);
	image.write("png", "twocylinders.png");

	System.exit(0);

    }
}
