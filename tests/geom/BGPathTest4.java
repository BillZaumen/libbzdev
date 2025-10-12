import org.bzdev.geom.*;
import org.bzdev.graphs.*;
import org.bzdev.p3d.*;
import java.awt.geom.Path2D;
import java.io.File;


public class BGPathTest4 {
    public static void main(String argv[]) throws Exception {
	double r1 = 10.0;
	double r2 = 200.0;

	var circle = Paths2D.createArc(0.0, 0.0, r1, 0.0,
					 2*Math.PI);
	circle.closePath();
	

	var loop2d = Paths2D.createArc(0.0, 0.0, r2, 0.0,
				     2*Math.PI, Math.PI/16);
	loop2d.closePath();

	Path3D loop = new Path3D.Double(loop2d, (i, p, type, bounds) -> {
		return new Point3D.Double(p.getX(), p.getY(), 0.0);
	}, 0);

	System.out.println(Path2DInfo.isClosed(circle));
	System.out.println(Path3DInfo.isClosed(loop));

	BezierGrid bg = new BezierGrid(circle, loop, null);

	System.out.format("%b %b\n", bg.isUClosed(), bg.isVClosed());

	Model3D m3d = new Model3D();
	m3d.append(bg);
	if (m3d.notPrintable(System.out)) {
	    System.exit(1);
	}

	m3d.writeSTL("BGPathTest4",
		     "bgpath4.stl");

	System.exit(0);


    }
}
