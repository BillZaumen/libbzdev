import org.bzdev.geom.*;
import org.bzdev.graphs.Graph;
import org.bzdev.p3d.*;
import java.awt.geom.Path2D;
import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FakeLock {

    static Model3D makeTestCase(Model3D m3d, List<Model3D.Triangle> tlist) {
	Model3D nm3d = new Model3D(false);
	Surface3D surface = new Surface3D.Double();
	SurfaceIterator sit = m3d.getSurfaceIterator(null);
	double coords[] = new double[48];
	while (!sit.isDone()) {
	    int n = 0;
	    int type = sit.currentSegment(coords);
	    switch(type) {
	    case SurfaceIterator.CUBIC_PATCH:
		n = 48;
		break;
	    case SurfaceIterator.CUBIC_TRIANGLE:
		n = 30;
		break;
	    case SurfaceIterator.CUBIC_VERTEX:
		n = 15;
		break;
	    case SurfaceIterator.PLANAR_TRIANGLE:
		n = 9;
		break;
	    }
	    for (int i = 0; i < n; i +=3) {
		boolean done = false;
		if (coords[i] < 19.2 || coords[i] > 19.5) continue;
		if (coords[i+2] < 8.0 || coords[i+2]> 13.5) continue;
		if (coords[i+1] < -1.4 || coords[i+1] > -1.3) continue;
		for (Model3D.Triangle triangle: tlist) {
		    if (coords[i] == triangle.getX1()
			|| coords[i+1] == triangle.getY1()
			|| coords[i+2] == triangle.getZ1()
			|| coords[i] == triangle.getX3()
			|| coords[i+1] == triangle.getY3()
			|| coords[i+2] == triangle.getZ3()
			|| coords[i] == triangle.getX2()
			|| coords[i+1] == triangle.getY2()
			|| coords[i+2] == triangle.getZ2()) {
			// matched
			switch(type) {
			case SurfaceIterator.CUBIC_PATCH:
			    surface.addCubicPatch(coords);
			    break;
			case SurfaceIterator.CUBIC_TRIANGLE:
			    surface.addCubicTriangle(coords);
			    break;
			case SurfaceIterator.CUBIC_VERTEX:
			    surface.addCubicVertex(coords);
			    break;
			case SurfaceIterator.PLANAR_TRIANGLE:
			    nm3d.addTriangle(coords[0], coords[1], coords[2],
					     coords[6], coords[7], coords[8],
					     coords[3], coords[4], coords[5]);
			    break;
			}
			done = true;
			break;
		    }
		}
		if (done) break;
	    }
	    sit.next();
	}
	nm3d.append(surface);
	return nm3d;
    }



    private static double fix (double x) {
	return Math.round(x*10)/10.0;
    }

    public static void main(String argv[]) throws Exception {

	boolean generateError = (argv.length > 0)
	    && argv[0].equals("--error");


	double r1 = 1.5;
	double h1 = 8;
	double h2 = 27 + h1;
	double r2 = 10.0;
	// double delta = 0.7;
	double delta = 0.5;
	double yoff = 2*r1;
	double xoff = 2*r1;


	var circle = Paths2D.createArc(0.0, 0.0, r1, 0.0,
					 2*Math.PI);
	circle.closePath();

	var lcircle = Paths2D.createArc(0.0, 0.0, fix(r1 + delta), 0.0,
					 2*Math.PI);
	lcircle.closePath();


	var loop2d = new Path2D.Double();
	if (generateError) {
	    loop2d.moveTo(0.0, h1);
	} else {
	    loop2d.moveTo(0.0, fix(h1/2));
	    loop2d.lineTo(0.0, h1);
	}
	double delta1 = (h2 - h1)/5;
	for (int i = 1; i < 5; i++) {
	    double y = h1 + delta1*i;
	    loop2d.lineTo(0.0, y);
	}
	loop2d.lineTo(0.0, h2);
	Path2D arc = Paths2D.createArc(loop2d, r2, false, Math.PI,
				       Math.PI/32);
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
	
	Path3D circle1 = loopGrid.getBoundary(0, 0);

	int n = loopGrid.getUArrayLength();
	Path3D circle2 = loopGrid.getBoundary(n-1, 0);

	Model3D m3d = new Model3D(false);

	SteppedGrid.Builder sgb1 = new SteppedGrid.Builder(m3d, fix(h1/2), 0.0);

	sgb1.addRectangles(fix(-4*r1), fix(-4*r1), fix(8*r1), fix(8*r1),
			   0.0, 0.0, false, false);

	if (generateError) {
	    sgb1.addRectangles(fix(-2*r1), fix(-2*r1), fix(4*r1), fix(4*r1),
			       fix(h1/2), 0.0, true, false);
	} else {
	    sgb1.addRectangles(fix(-1.25*r1), fix(-1.25*r1),
			       fix(2.5*r1), fix(2.5*r1),
			       0.0 , 0.0, true, false);
	}

	SteppedGrid.Builder sgb2 = new SteppedGrid.Builder(m3d, h1, 0.0);

	sgb2.addRectangles(fix(2*r2 - 2*r1), fix(-2*r1), fix(4*r1), fix(4*r1),
			   0.0, 0.0, true, false);

	sgb2.addRectangles(fix(2*r2 - 2*r1 - xoff), fix(yoff),
			   fix(xoff + 4*r1), fix(6*r1 - yoff),
			   0.0, 0.0, false, false);

	SteppedGrid sg1 = sgb1.create();
	SteppedGrid sg2 = sgb2.create();

	Path3D sg1Boundary = sg1.getBoundary();
	if (sg1Boundary == null) {
	    System.out.println("sg1: no boundary could be computed");
	    System.exit(1);
	}
	
	/*
	System.out.println("circle1");
	Path3DInfo.printSegments(circle1);
	*/

	var c1 = new ConvexPathConnector(circle1, sg1Boundary);

	Path3D sg2Boundary = sg2.getBoundary();
	if (sg2Boundary == null) {
	    System.out.println("sg2: no boundary could be computed");
	    System.exit(1);
	}

	/*
	System.out.println("circle2");
	Path3DInfo.printSegments(circle2);
	*/

	var c2 = new ConvexPathConnector(circle2, sg2Boundary);

	m3d.append(c1);
	m3d.append(c2);
	m3d.append(loopGrid);

	Path3D boundary = m3d.getBoundary();
	if (boundary == null) {
	    System.out.println("m3d does not have a boundary (case 1)");
	} else if (!boundary.isEmpty()) {
	    System.out.println("m3d boundary should be empty (case 1)");
	    Path3DInfo.printSegments(boundary);
	    System.exit(1);
	}

	// m3d = new Model3D(false);

	BezierGrid cylinder = new
	    BezierGrid(lcircle, (i, p, type, bounds) -> {
		    double z = (i == 0)? fix(h1 + delta):
			fix(h1/2 + delta);
		    return new Point3D.Double((float)p.getX(),
					      (float)p.getY(),
					      (float)z);
	    }, 2, false);
	cylinder.flip();


	SteppedGrid.Builder sgb =
	    new SteppedGrid.Builder(m3d, h1 + delta, delta);


	sgb.addRectangles(fix(-6*r1), fix(-6*r1), fix(12*r1+2*r2), fix(12*r1),
			  0.0, 0.0);

	/*
	sgb.addRectangles(fix(-4*r1-delta), fix(-4*r1-delta),
			  fix(8*r1 + 2*delta), fix(8*r1 + 2*delta),
			  0.0, fix(h1/2), false, false); 

	sgb.addRectangles(fix(-2*r1 - delta), fix(-2*r1 - delta),
			  fix(4*r1 + 2*delta), fix(4*r1 + 2*delta),
			  0.0, fix(h1/2), true, true);
	*/
	sgb.addRectangles(fix(-4*r1-delta), fix(-4*r1-delta),
			  fix(8*r1 + 2*delta), fix(8*r1 + 2*delta),
			  0.0, fix(h1/2), true, true); 

	sgb.removeRectangles(fix(2*r2 - 2*r1 - delta), fix(-2*r1 - delta),
			     fix(4*r1 + 2*delta), fix(2*r1 + delta + 6*r1));

	sgb.removeRectangles(fix(2*r2 - 2*r1 - delta - xoff), fix(yoff-delta),
			     fix(xoff + 4*r1 + 2*delta),
			     fix(6*r1 + 3*delta - yoff));



	SteppedGrid sg = sgb.create();
	Path3D[] boundaries = PathSplitter.split(sg.getBoundary());
	double[] cpts = Path3DInfo
	    .getControlPoints(boundaries[0].getPathIterator(null), false);
	Path3D topBoundary = (cpts[2] == fix(h1/2 + delta))?
	    boundaries[1]: boundaries[0];
	Path3D bottomBoundary = (cpts[2] == fix(h1/2 + delta))?
	    boundaries[0]: boundaries[1];

	Path3D topCircle = cylinder.getBoundary(0, 0);
	int cn = cylinder.getUArrayLength();
	Path3D bottomCircle = cylinder.getBoundary(cn-1, 0);
	
	var c3 = new ConvexPathConnector(topCircle, topBoundary);
	var c4 = new ConvexPathConnector(bottomCircle, bottomBoundary);
	m3d.append(cylinder);
	m3d.append(c3);
	m3d.append(c4);

	boundary = m3d.getBoundary();
	if (boundary == null) {
	    System.out.println("m3d does not have a boundary (case 2)");
	    System.exit(1);
	} else if (!boundary.isEmpty()) {
	    System.out.println("m3d boundary should be empty (case 2)");
	    Path3DInfo.printSegments(boundary);
	    System.exit(1);
	}

	System.out.println("number of components = "
			   + m3d.numberOfComponents());
	System.out.println("m3d.findMinULPRatio() = " +m3d.findMinULPRatio());
	m3d.setTessellationLevel(3);

	if (!generateError) {
	    List<Model3D.Triangle> tlist = m3d.verifyEmbedded2DManifold();
	    if (tlist != null && !tlist.isEmpty()) {
		for (Model3D.Triangle triangle: tlist) {
		    System.out
			.format("(%s, %s, %s)---(%s, %s, %s)---(%s, %s, %s)\n",
				triangle.getX1(),
				triangle.getY1(),
				triangle.getZ1(),
				triangle.getX2(),
				triangle.getY2(),
				triangle.getZ2(),
				triangle.getX3(),
				triangle.getY3(),
				triangle.getZ3());
		}
		System.out.println("... as (double)(float)");
		for (Model3D.Triangle triangle: tlist) {
		    System.out
			.format("(%s, %s, %s)---(%s, %s, %s)---(%s, %s, %s)\n",
				(double)(float)triangle.getX1(),
				(double)(float)triangle.getY1(),
				(double)(float)triangle.getZ1(),
				(double)(float)triangle.getX2(),
				(double)(float)triangle.getY2(),
				(double)(float)triangle.getZ2(),
				(double)(float)triangle.getX3(),
				(double)(float)triangle.getY3(),
				(double)(float)triangle.getZ3());
		}
		Model3D tm3d = new Model3D(false);
		tm3d.append(loopGrid);
		tm3d.append(c2);
		Model3D nm3d = makeTestCase(tm3d, tlist);
		System.out.println("nm3d.size() = " + nm3d.size());
		SurfaceIterator sit = nm3d.getSurfaceIterator(null);
		DataOutputStream dout = new DataOutputStream
		    (new FileOutputStream("cvtest.dat.new"));
		double ncoords[] = new double[48];
		while (!sit.isDone()) {
		    int type = sit.currentSegment(ncoords);
		    int m = 0;
		    switch(type) {
		    case SurfaceIterator.CUBIC_PATCH:
			m = 48;
			break;
		    case SurfaceIterator.CUBIC_TRIANGLE:
			m = 30;
			break;
		    case SurfaceIterator.CUBIC_VERTEX:
			m = 15;
			break;
		    case SurfaceIterator.PLANAR_TRIANGLE:
			m = 9;
			break;
		    }
		    System.out.println("... saw surface type " + type
				       + ", m = " + m);
		    dout.writeInt(type);
		    for (int i = 0; i < m; i++) {
			dout.writeDouble(ncoords[i]);
		    }
		    sit.next();
		}
		dout.flush();
		dout.close();
		nm3d.setTessellationLevel(2);
		tlist = nm3d.verifyEmbedded2DManifold();
		if (tlist != null && !tlist.isEmpty()) {
		    P3d.printTriangleErrors(System.out, tlist);
		    System.out.println("nm3d not embedded");
		} else {
		    System.out.println("nm3d embedded");
		}
	    }
	}

	if (m3d.notPrintable(System.out)) {
	    System.out.println("model not printable ("
			       + (generateError? "expected)": "not expected)"));
	    // if (!generateError)  System.exit(1);
	    System.out.println("... creating graphs");
	    Graph graph1 = new Graph(700, 700);
	    m3d.createCrossSection(graph1, 0.0, 3.0, 8.0,
				   new double[] {0.0, 0.0, 1.0});
	    graph1.write("png", "fakelock1.png");
	    Graph graph2 = new Graph(700, 700);
	    m3d.createCrossSection(graph2, 0.0, 0.0, 8.0,
				   new double[] {0.0, 1.0, 0.0});
	    graph2.write("png", "fakelock2.png");
	} else {
	    Graph graph3 = new Graph(400, 700);
	    m3d.createCrossSection(graph3, -3.0, 0.0, 4.7,
				   new double[] {0.0, 1.0, 0.0});
	    graph3.write("png", "fakelock3.png");
	    Graph graph4 = new Graph(700, 400);
	    m3d.createCrossSection(graph4, -3.0, 0.0, 4.7,
				   new double[] {0.0, 1.0, 0.0});
	    graph4.write("png", "fakelock4.png");
	}
    }
}
