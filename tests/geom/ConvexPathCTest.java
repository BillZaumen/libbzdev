import org.bzdev.geom.*;
import org.bzdev.p3d.Model3D;
import java.awt.geom.*;
import java.io.FileOutputStream;

public class ConvexPathCTest {

    public static void main(String argv[]) throws Exception {
	Path2D circle = Paths2D.createArc(0.0, 0.0, 0.0, -50.0,
					  2*Math.PI, Math.PI/4);
	circle.closePath();
	
	Path2D box = new Path2D.Double();
	box.moveTo(-75.0, -75.0);
	box.lineTo(75.0, -75.0);
	box.lineTo(75.0, 75.0);
	box.lineTo(-75.0, 75.0);
	box.closePath();

	ConvexPathConnector cpc = new ConvexPathConnector(circle, box);

	Model3D m3d = new Model3D();

	m3d.append(cpc);

	// m3d.setTessellationLevel(4);
	m3d.setTessellationLevel(2);
	m3d.createImageSequence(new FileOutputStream("cpc.isq"), "png",
				8, 6,
				0.0, 0.0, 0.0, true);


	Path3D circle3 = new Path3D.Double(circle,
					   (n, p, type, bounds) -> {
					       return new Point3D.Double
						   (p.getX(),
						    p.getY(),
						    10.0);
				 	   }, 0);
	Path3D box3 = new Path3D.Double(box,
					(n, p, type, bounds) -> {
					     return p;
					}, 0);
	ConvexPathConnector cpc3 = new ConvexPathConnector(circle3, box3, true);

	m3d = new Model3D();
	m3d.append(cpc3);

	m3d.setTessellationLevel(4);

	m3d.createImageSequence(new FileOutputStream("cpc2.isq"), "png",
				8, 6,
				0.0, 0.0, 0.0, false);

	Path2D box2i = new Path2D.Double();
	box2i.moveTo(-75.0, -75.0);
	box2i.lineTo(0.0, -75.0);
	box2i.lineTo(75.0, -75.0);
	box2i.lineTo(75.0, 0.0);
	box2i.lineTo(75.0, 75.0);
	box2i.lineTo(0.0, 75.0);
	box2i.lineTo(-75.0, 75.0);
	box2i.lineTo(-75.0, 0.0);
	box2i.closePath();
	Path2D box2o = new Path2D.Double();
	box2o.moveTo(-80.0, -80.0);
	box2o.lineTo(80.0, -80.0);
	box2o.lineTo(80.0, 80.0);
	box2o.lineTo(-80.0, 80.0);
	box2o.closePath();
	
	ConvexPathConnector cpc2a = new ConvexPathConnector(box, box2o);
	m3d = new Model3D();
	m3d.append(cpc2a);

	m3d.createImageSequence(new FileOutputStream("cpc4.isq"), "png",
				8, 6,
				0.0, 0.0, 0.0, true);

	ConvexPathConnector cpc2b = new ConvexPathConnector(box2i, box2o);
	m3d = new Model3D();
	m3d.append(cpc2b);

	m3d.createImageSequence(new FileOutputStream("cpc5.isq"), "png",
				8, 6,
				0.0, 0.0, 0.0, true);

	Path3D box3i = new Path3D.Double(box2i,
					 (n, p, type, bounds) -> {
					     return new Point3D.Double
						 (p.getX(),
						  p.getY(),
						  10.0);
				 	 }, 0);

	Path3D box3o = new Path3D.Double(box2o,
					 (n, p, type, bounds) -> {
					     return p;
				 	 }, 0);


	ConvexPathConnector cpc3b = new ConvexPathConnector(box3i, box3o, true);
	m3d = new Model3D();
	m3d.append(cpc3b);

	m3d.createImageSequence(new FileOutputStream("cpc6.isq"), "png",
				8, 6,
				0.0, 0.0, 0.0, true);

	ConvexPathConnector cpc3br = new ConvexPathConnector(box3i, box3o);
	m3d = new Model3D();
	m3d.append(cpc3br);

	m3d.createImageSequence(new FileOutputStream("cpc6r.isq"), "png",
				8, 6,
				0.0, 0.0, 0.0, true);


	Path3D box4 = new Path3D.Double(box,
					 (n, p, type, bounds) -> {
					     return new Point3D.Double
						 (p.getX(),
						  p.getY(),
						  10.0);
				 	 }, 0);


	ConvexPathConnector cpc3c = new ConvexPathConnector(box4, box3o, true);
	m3d = new Model3D();
	m3d.append(cpc3c);

	m3d.createImageSequence(new FileOutputStream("cpc7.isq"), "png",
				8, 6,
				0.0, 0.0, 0.0, true);
	System.exit(0);
    }
}
