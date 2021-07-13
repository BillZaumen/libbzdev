import org.bzdev.p3d.*;
import org.bzdev.geom.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.io.*;

public class OpenFitting {
    public static void main(String argv[]) throws Exception {
	

	double h1 = 4.0;
	double h2 = 20.0;
	double h3 = 30;
	double r1 = 15.0;
	double r2 = 20.0;
	double r3 = 30.0;

	Path2D path = new Path2D.Double();

	path.moveTo(r1, 0.0);
	path.lineTo(r2, 0.0);
	path.lineTo(r2,h3);
	path.lineTo(r3, h3);
	Path2D arc = Paths2D.createArc(r3-h1, h3, r3, h3,
				       Math.PI/2, Math.PI/2);
	path.append(arc, true);
	path.lineTo(r1, h3+h1);
	path.lineTo(r1, 0.0);
	path.closePath();

	int n = 36;

	BezierGrid bg = new BezierGrid(path, (i, p, t, bounds) -> {
		double x = p.getX();
		double y;
		double z = p.getY();
		double phi = i* Math.PI/n;
		y = (i == 18)? x:
		    (i == 0)? 0.0: (i == 36)? 0.0 :x * Math.sin(phi);
		x = (i == 18)? 0.0:
		    (i == 0)? x: (i == 36)? -x: x * Math.cos(phi);
		return new Point3D.Double(x, y, z);
	}, n+1, false);

	bg.print();

	Path3D b1 = bg.getBoundary(0,0);
	if (b1 == null) {
	    System.out.println("b1 was null");
	    System.exit(1);
	} else if (Path3DInfo.getEntries(b1).size() == 0) {
	    System.out.println("b1 was empty");
	    System.exit(1);
	}

	Path3D b2 = bg.getBoundary(n,0);
	if (b2 == null) {
	    System.out.println("b2 was null");
	    System.exit(1);
	} else if (Path3DInfo.getEntries(b2).size() == 0) {
	    System.out.println("b2 was empty");
	    System.exit(1);
	}

	double[] vector = {0.0, -1.0, 0.0};
	BezierCap bc1 = new BezierCap(b1,
				      new Point3D.Double(17.5, 0.0, 29.0),
				      vector, 0.0, true);
	BezierCap bc2 = new BezierCap(b2,
				      new Point3D.Double(-17.5, 0.0, 29.0),
				      vector, 0.0, true);



	Surface3D surface = new Surface3D.Double();
	surface.append(bg);
	surface.append(bc1);
	surface.append(bc2);

	Model3D m3d = new Model3D();
	m3d.append(surface);
	
	m3d.setTessellationLevel(4);

	m3d.createImageSequence(new FileOutputStream("fitting.isq"), "png",
				8, 6,
				0.0, 0.0, 0.0, false);
    }
}
