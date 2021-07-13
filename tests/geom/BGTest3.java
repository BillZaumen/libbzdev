import org.bzdev.geom.*;
import java.awt.geom.Path2D;

public class BGTest3 {

    public static void main(String argv[]) throws Exception {

	double r1 = 1.5;
	double h1 = 8;
	double h2 = 27 + h1;
	double r2 = 10.0;

	var circle = Paths2D.createArc(0.0, 0.0, r1, 0.0, 2*Math.PI);
	circle.closePath();

	var loop2d = new Path2D.Double();
	loop2d.moveTo(0.0, h1);
	double delta1 = (h2 - h1)/5;
	for (int i = 1; i < 5; i++) {
	    double y = h1 + delta1*i;
	    loop2d.lineTo(0.0, y);
	}
	loop2d.lineTo(0.0, h2);
	Path2D arc = Paths2D.createArc(loop2d, r2, false, Math.PI,
				       Math.PI/8);
	loop2d.append(arc, true);
	for (int i = 1; i < 5; i++) {
	    double y = h2 - i*delta1;
	    loop2d.lineTo(2*r2, y);
	}
	loop2d.lineTo(2*r2, h1);

	Path3D loop = new Path3D.Double(loop2d, (i, p, type, bounds) -> {
		return new Point3D.Double(p.getX(), 0.0, p.getY());
	}, 0);

	// Path3DInfo.printSegments(loop);
 
	double inormal[] = {1.0, 0.0, 0.0};
	BezierGrid loopGrid = new BezierGrid(circle,
					     BezierGrid.getMapper(loop,
								  inormal));
	loopGrid.print();

   }

}

