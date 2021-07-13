import org.bzdev.geom.*;
import org.bzdev.p3d.Model3D;
import java.awt.geom.Point2D;
import java.awt.Color;
import java.awt.geom.Path2D;
import java.io.FileOutputStream;

public class BGPathTest {
    public static void main(String argv[]) throws Exception {

	Path2D path1 = new Path2D.Double();
	Path2D path2 = new Path2D.Double();
	Path2D path3 = new Path2D.Double();

	path1.moveTo(10.0, 0.0);
	path2.moveTo(10.0, 0.0);
	path3.moveTo(10.0, 0.0);

	path1.lineTo(100.0, 0.0);
	path2.lineTo(100.0, 0.0);
	path3.lineTo(100.0, 0.0);

	path1.quadTo(150.0, 100.0, 100.0, 100.0);
	path2.quadTo(150.0, 100.0, 100.0, 100.0);
	path3.quadTo(150.0, 100.0, 100.0, 100.0);

	path1.curveTo(75.0, 120.0, 25.0, 120.0, 10.0, 100.0);
	path2.curveTo(75.0, 120.0, 25.0, 120.0, 10.0, 100.0);
	path3.curveTo(75.0, 120.0, 25.0, 120.0, 10.0, 100.0);
	path3.lineTo(10.0, 0.0);
	path2.closePath();
	path3.closePath();

	final int nu = 21;

	Point3DMapper<Point3D> mapper = (i, p, t, bounds) -> {
	    double x = p.getX();
	    double y;
	    double z = p.getY();
	    double angle = (Math.PI /(nu-1)) * i;
	    y = x * Math.sin(angle);
	    x = x * Math.cos(angle);
	    return new Point3D.Double(x, y, z);
	};

	BezierGrid g1 = new BezierGrid(path1, mapper, nu, false);
	System.out.println("created grid1");
	BezierGrid g2 = new BezierGrid(path2, mapper, nu, false);
	System.out.println("created grid2");
	BezierGrid g3 = new BezierGrid(path3, mapper, nu, false);
	System.out.println("created grid3");

	Surface3D surface1 = new Surface3D.Double();
	surface1.append(g1);
	System.out.println("bounding box for surface1: "
			   + surface1.getBounds());

	Surface3D surface2 = new Surface3D.Double();
	surface2.append(g2);
	System.out.println("bounding box for surface2: "
			   + surface2.getBounds());
	Surface3D surface3 = new Surface3D.Double();
	surface3.append(g3);
	System.out.println("bounding box for surface3: "
			   + surface3.getBounds());
	Model3D m3d = new Model3D();
	/*
	m3d.append(surface1);
	m3d.setTessellationLevel(4);
	m3d.createImageSequence(new FileOutputStream("bgpath1.isq"), "png",
				8, 6,
				0.0, 0.0, 0.0, false);

	m3d = new Model3D();
	m3d.append(surface2);
	m3d.setTessellationLevel(4);
	m3d.createImageSequence(new FileOutputStream("bgpath2.isq"), "png",
				8, 6,
				0.0, 0.0, 0.0, false);


	m3d = new Model3D();
	m3d.append(surface3);
	m3d.setTessellationLevel(4);
	m3d.createImageSequence(new FileOutputStream("bgpath3.isq"), "png",
				8, 6,
				0.0, 0.0, 0.0, false);
	*/

	final int nu2 = 40;
	mapper = (i, p, t, bounds) -> {
	    double x = p.getX();
	    double y;
	    double z = p.getY();
	    double angle = (2*Math.PI /(nu2)) * i;
	    y = x * Math.sin(angle);
	    x = x * Math.cos(angle);
	    return new Point3D.Double(x, y, z);
	};

	BezierGrid g4 = new BezierGrid(path1, mapper, nu2, true);
	System.out.println("created grid4");
	BezierGrid g5 = new BezierGrid(path2, mapper, nu2, true);
	System.out.println("created grid5");
	BezierGrid g6 = new BezierGrid(path3, mapper, nu2, true);
	System.out.println("created grid6");

	Path2D template = new SplinePath2D(new Point2D.Double[] {
		new Point2D.Double(25.0, 0.0),
		new Point2D.Double(-25.0, 25.0),
		new Point2D.Double(-25.0,-25.0)
	    }, true);

	Surface3D surface4 = new Surface3D.Double();
	surface4.append(g4);
	System.out.println("bounding box for surface4: "
			   + surface4.getBounds());
	Surface3D surface5 = new Surface3D.Double();
	surface5.append(g5);
	System.out.println("bounding box for surface5: "
			   + surface5.getBounds());
	Surface3D surface6 = new Surface3D.Double();
	surface6.append(g6);
	System.out.println("bounding box for surface6: "
			   + surface6.getBounds());

	SplinePath3D path = new SplinePath3D();
	double r = 200;
	path.moveTo(r, 0.0, 0.0);
	double angle = Math.PI/2;
	path.splineTo(new Point3D.Double
		      (r* Math.cos(angle*0.1), r* Math.sin(angle*0.1), 0),
		       new Point3D.Double
		       (r* Math.cos(angle*0.2), r* Math.sin(angle*0.2), 0),
		       new Point3D.Double
		       (r* Math.cos(angle*0.3), r* Math.sin(angle*0.3), 0),
		       new Point3D.Double
		       (r* Math.cos(angle*0.4), r* Math.sin(angle*0.4), 0),
		       new Point3D.Double
		       (r* Math.cos(angle*0.5), r* Math.sin(angle*0.5), 0),
		       new Point3D.Double
		       (r* Math.cos(angle*0.6), r* Math.sin(angle*0.6), 0),
		       new Point3D.Double
		       (r* Math.cos(angle*0.7), r* Math.sin(angle*0.7), 0),
		       new Point3D.Double
			(r* Math.cos(angle*0.8), r* Math.sin(angle*0.8), 0),
		       new Point3D.Double
			(r* Math.cos(angle*0.9), r* Math.sin(angle*0.9), 0),
		       new Point3D.Double(0.0, r, 0.0));
	Path3DInfo.printSegments(path);

	double[] inormal = {1.0, 0.0, 0.0};
	BezierGrid g7 = new BezierGrid(template,
				       BezierGrid.getMapper(path, inormal));
	System.out.println("created grid7");
	Surface3D surface7 = new Surface3D.Double();
	surface7.append(g7);
	System.out.println("bounding box for surface7: "
			   + surface7.getBounds());

	double r2 = 2*r;
	path = new SplinePath3D();
	path.moveTo(r, 0.0, 0.0);
	path.splineTo(new Point3D.Double
		      (r* Math.cos(angle*0.1), r* Math.sin(angle*0.1), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*0.2), r* Math.sin(angle*0.2), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*0.3), r* Math.sin(angle*0.3), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*0.4), r* Math.sin(angle*0.4), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*0.5), r* Math.sin(angle*0.5), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*0.6), r* Math.sin(angle*0.6), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*0.7), r* Math.sin(angle*0.7), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*0.8), r* Math.sin(angle*0.8), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*0.9), r* Math.sin(angle*0.9), 0),
		      new Point3D.Double
		      (r* Math.cos(angle), r* Math.sin(angle), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*1.1), r2-r* Math.sin(angle*1.1), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*1.2), r2-r* Math.sin(angle*1.2), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*1.3), r2-r* Math.sin(angle*1.3), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*1.4), r2-r* Math.sin(angle*1.4), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*1.5), r2-r* Math.sin(angle*1.5), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*1.6), r2-r* Math.sin(angle*1.6), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*1.7), r2-r* Math.sin(angle*1.7), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*1.8), r2-r* Math.sin(angle*1.8), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*1.9), r2-r* Math.sin(angle*1.9), 0),
		      new Point3D.Double(-r, r2, 0.0));

	BezierGrid g8 = new BezierGrid(template,
				       BezierGrid.getMapper(path, inormal));
	System.out.println("created grid8");
	Surface3D surface8 = new Surface3D.Double();
	surface8.append(g8);
	System.out.println("bounding box for surface8: "
			   + surface8.getBounds());

	path = new SplinePath3D();
	path.moveTo(r, 0.0, 0.0);
	path.splineTo(new Point3D.Double
		      (r* Math.cos(angle*0.1), r* Math.sin(angle*0.1), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*0.2), r* Math.sin(angle*0.2), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*0.3), r* Math.sin(angle*0.3), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*0.4), r* Math.sin(angle*0.4), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*0.5), r* Math.sin(angle*0.5), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*0.6), r* Math.sin(angle*0.6), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*0.7), r* Math.sin(angle*0.7), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*0.8), r* Math.sin(angle*0.8), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*0.9), r* Math.sin(angle*0.9), 0),
		      new Point3D.Double
		      (r* Math.cos(angle), r* Math.sin(angle), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*1.1), r, r-r* Math.sin(angle*1.1)),
		      new Point3D.Double
		      (r* Math.cos(angle*1.2), r, r-r* Math.sin(angle*1.2)),
		      new Point3D.Double
		      (r* Math.cos(angle*1.3), r, r-r* Math.sin(angle*1.3)),
		      new Point3D.Double
		      (r* Math.cos(angle*1.4), r, r-r* Math.sin(angle*1.4)),
		      new Point3D.Double
		      (r* Math.cos(angle*1.5), r, r-r* Math.sin(angle*1.5)),
		      new Point3D.Double
		      (r* Math.cos(angle*1.6), r, r-r* Math.sin(angle*1.6)),
		      new Point3D.Double
		      (r* Math.cos(angle*1.7), r, r-r* Math.sin(angle*1.7)),
		      new Point3D.Double
		      (r* Math.cos(angle*1.8), r, r-r* Math.sin(angle*1.8)),
		      new Point3D.Double
		      (r* Math.cos(angle*1.9), r, r-r* Math.sin(angle*1.9)),
		      new Point3D.Double(-r, r, r));

	BezierGrid g9 = new BezierGrid(template,
				       BezierGrid.getMapper(path, inormal));
	System.out.println("created grid9");
	Surface3D surface9 = new Surface3D.Double();
	surface9.append(g9);
	System.out.println("bounding box for surface9: "
			   + surface9.getBounds());

	path = new SplinePath3D();
	path.moveTo(r, 0.0, 0.0);
	path.cycleTo(new Point3D.Double
		      (r* Math.cos(angle*0.1), r* Math.sin(angle*0.1), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*0.2), r* Math.sin(angle*0.2), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*0.3), r* Math.sin(angle*0.3), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*0.4), r* Math.sin(angle*0.4), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*0.5), r* Math.sin(angle*0.5), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*0.6), r* Math.sin(angle*0.6), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*0.7), r* Math.sin(angle*0.7), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*0.8), r* Math.sin(angle*0.8), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*0.9), r* Math.sin(angle*0.9), 0),
		      new Point3D.Double
		      (r* Math.cos(angle), r* Math.sin(angle), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*1.1), r* Math.sin(angle*1.1), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*1.2), r* Math.sin(angle*1.2), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*1.3), r* Math.sin(angle*1.3), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*1.4), r* Math.sin(angle*1.4), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*1.5), r* Math.sin(angle*1.5), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*1.6), r* Math.sin(angle*1.6), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*1.7), r* Math.sin(angle*1.7), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*1.8), r* Math.sin(angle*1.8), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*1.9), r* Math.sin(angle*1.9), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*2.0), r* Math.sin(angle*2.0), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*2.1), r* Math.sin(angle*2.1), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*2.2), r* Math.sin(angle*2.2), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*2.3), r* Math.sin(angle*2.3), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*2.4), r* Math.sin(angle*2.4), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*2.5), r* Math.sin(angle*2.5), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*2.6), r* Math.sin(angle*2.6), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*2.7), r* Math.sin(angle*2.7), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*2.8), r* Math.sin(angle*2.8), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*2.9), r* Math.sin(angle*2.9), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*3.0), r* Math.sin(angle*3.0), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*3.1), r* Math.sin(angle*3.1), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*3.2), r* Math.sin(angle*3.2), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*3.3), r* Math.sin(angle*3.3), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*3.4), r* Math.sin(angle*3.4), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*3.5), r* Math.sin(angle*3.5), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*3.6), r* Math.sin(angle*3.6), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*3.7), r* Math.sin(angle*3.7), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*3.8), r* Math.sin(angle*3.8), 0),
		      new Point3D.Double
		      (r* Math.cos(angle*3.9), r* Math.sin(angle*3.9), 0));

	double[] tmp = new double[3];
	if (Path3DInfo.getStartingTangent(path, tmp)) {
	    System.out.format("path starting tangent: (%g, %g, %g)\n",
			      tmp[0], tmp[1], tmp[2]);
	    System.out.println("closed = " + Path3DInfo.isClosed(path));
	}
	System.out.println("starting to create g10");
	BezierGrid g10 = new BezierGrid(template,
				       BezierGrid.getMapper(path, inormal));
	g10.print();
	System.out.println("created grid10");
	Surface3D surface10 = new Surface3D.Double();
	surface10.append(g10);
	System.out.println("bounding box for surface10: "
			   + surface10.getBounds());
	m3d = new Model3D();
	m3d.append(surface4);
	m3d.setTessellationLevel(4);
	m3d.createImageSequence(new FileOutputStream("bgpath4.isq"), "png",
				8, 6,
				0.0, 0.0, 0.0, false);
	m3d = new Model3D();
	m3d.append(surface5);
	m3d.setTessellationLevel(4);
	m3d.createImageSequence(new FileOutputStream("bgpath5.isq"), "png",
				8, 6,
				0.0, 0.0, 0.0, false);
	m3d = new Model3D();
	m3d.append(surface6);
	m3d.setTessellationLevel(4);
	m3d.createImageSequence(new FileOutputStream("bgpath6.isq"), "png",
				8, 6,
				0.0, 0.0, 0.0, false);

	m3d = new Model3D();
	m3d.append(surface7);
	m3d.setTessellationLevel(4);
	m3d.createImageSequence(new FileOutputStream("bgpath7.isq"), "png",
				8, 6,
				0.0, 0.0, 0.0, false);

	m3d = new Model3D();
	m3d.append(surface8);
	m3d.setTessellationLevel(4);
	m3d.createImageSequence(new FileOutputStream("bgpath8.isq"), "png",
				8, 6,
				0.0, 0.0, 0.0, false);

	m3d = new Model3D();
	m3d.append(surface9);
	m3d.setTessellationLevel(4);
	m3d.createImageSequence(new FileOutputStream("bgpath9.isq"), "png",
				8, 6,
				0.0, 0.0, 0.0, false);

	m3d = new Model3D();
	m3d.append(surface10);
	m3d.setTessellationLevel(4);
	m3d.createImageSequence(new FileOutputStream("bgpath10.isq"), "png",
				8, 6,
				0.0, 0.0, 0.0, false);


    }
}
