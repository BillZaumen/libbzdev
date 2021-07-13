import org.bzdev.geom.*;
import org.bzdev.graphs.*;
import org.bzdev.p3d.*;
import java.awt.geom.Path2D;
import java.io.File;

// This example resulted in a 'vertex not visisble' error in Version 1.2.189

public class BGPathTest3 {

    private static double fix (double x) {
	return Math.round(x*10)/10.0;
    }

    public static void trivtest1() {
	Path3D inner = new Path3D.Double();
	Path3D outer = new Path3D.Double();
	outer.moveTo(0.0, 0.0, 0.0);
	outer.lineTo(100.0, 0.0, 0.0);
	outer.lineTo(100.0, 100.0, 0.0);
	outer.lineTo(0.0, 100.0, 0.0);
	outer.lineTo(0.0, 0.0, 0.0);
	outer.closePath();

	inner.moveTo(20.0, 20.0, 0.0);
	inner.lineTo(80.0, 20.0, 0.0);
	inner.lineTo(80.0, 80.0, 0.0);
	inner.lineTo(20.0, 80.0, 0.0);
	inner.lineTo(20.0, 20.0, 0.0);
	inner.closePath();

	var c = new ConvexPathConnector(inner, outer);

	Model3D m3d = new Model3D();
	m3d.append(c);
	Path3D boundary = m3d.getBoundary();
	if (boundary == null) {
	    System.out.println("no boundary");
	    System.exit(1);
	}
    }

    public static void trivtest2() {
	Path3D inner = new Path3D.Double();
	Path3D outer = new Path3D.Double();
	outer.moveTo(0.0, 0.0, 8.0);
	outer.lineTo(100.0, 0.0, 8.0);
	outer.lineTo(100.0, 100.0, 8.0);
	outer.lineTo(0.0, 100.0, 8.0);
	outer.lineTo(0.0, 0.0, 8.0);
	outer.closePath();

	inner = Paths3D.createArc(50.0, 20.0, 8.0,
				  new double[] {1.0, 0.0, 0.0},
				  new double[] {0.0, 1.0, 0.0},
				  30.17,
				  2*Math.PI);
	inner.closePath();

	var c = new ConvexPathConnector(inner, outer);

	Model3D m3d = new Model3D();
	m3d.append(c);
	Path3D boundary = m3d.getBoundary();
	if (boundary == null) {
	    System.out.println("no boundary");
	    System.exit(1);
	}
    }



    public static void main(String argv[]) throws Exception {

	trivtest1();
	trivtest2();

	double r1 = 1.5;
	double h1 = 8;
	double h2 = 27 + h1;
	double r2 = 10.0;
	double delta = 0.7;

	double yoff = 2*r1;
	double xoff = 2*r1;


	var circle = Paths2D.createArc(0.0, 0.0, r1, 0.0,
					 2*Math.PI);
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

	double inormal[] = {1.0, 0.0, 0.0};
	BezierGrid loopGrid = new BezierGrid(circle,
					     BezierGrid.getMapper(loop,
								  inormal));
	int n = loopGrid.getUArrayLength();
	Path3D circle2 = loopGrid.getBoundary(n-1, 0);

	Model3D m3d = new Model3D();
	
	SteppedGrid.Builder sgb2 = new SteppedGrid.Builder(m3d, h1, 0.0);

	sgb2.addRectangles(fix(2*r2 - 2*r1), fix(-2*r1), fix(4*r1), fix(4*r1),
			   0.0, 0.0, true, false);

	sgb2.addRectangles(fix(2*r2 - 2*r1 - xoff), fix(yoff),
			   fix(xoff + 4*r1), fix(6*r1 - yoff),
			   0.0, 0.0, false, false);

	SteppedGrid sg2 = sgb2.create();

	Path3D sg2Boundary = sg2.getBoundary();
	if (sg2Boundary == null) {
	    System.out.println("sg2: no boundary could be computed");
	    System.exit(1);
	}

	Graph graph = new Graph(700, 600);
	ConvexPathConnector.setupDebuggingGraph(graph, new File("bgpath3.png"));
	var c2 = new ConvexPathConnector(circle2, sg2Boundary);
    }
}
