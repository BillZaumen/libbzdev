import org.bzdev.geom.*;
import org.bzdev.p3d.*;
import java.awt.Color;
import java.awt.geom.Path2D;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

// Failed, presumably  due to tessellation with level 3.
public class FakeLockT {

    private static double fix (double x) {
	return Math.round(x*10)/10.0;
    }

    public static void main(String argv[]) throws Exception {

	double r1 = 1.5;
	double h1 = 8;
	double h2 = 27 + h1;
	double r2 = 10.0;
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
	loop2d.moveTo(0.0, fix(h1/2));
	loop2d.lineTo(0.0, h1);
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

	sgb1.addRectangles(fix(-1.25*r1), fix(-1.25*r1),
			   fix(2.5*r1), fix(2.5*r1),
			   0.0 , 0.0, true, false);


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
	
	var c1 = new ConvexPathConnector(circle1, sg1Boundary);

	Path3D sg2Boundary = sg2.getBoundary();
	if (sg2Boundary == null) {
	    System.out.println("sg2: no boundary could be computed");
	    System.exit(1);
	}
	var c2 = new ConvexPathConnector(circle2, sg2Boundary);

	m3d.append(c1);
	m3d.append(c2);
	m3d.append(loopGrid);

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

	sgb.addRectangles(fix(-4*r1-delta), fix(-4*r1-delta),
			  fix(8*r1 + 2*delta), fix(8*r1 + 2*delta),
			  0.0, fix(h1/2), true, true); 

	sgb.removeRectangles(fix(2*r2 - 2*r1 - delta), fix(-2*r1 - delta),
			     fix(4*r1 + 2*delta), fix(2*r1 + delta + 6*r1));

	sgb.removeRectangles(fix(2*r2 - 2*r1 - delta - xoff), fix(yoff-delta),
			     fix(xoff + 4*r1 + 2*delta),
			     fix(6*r1 + 3*delta - yoff));

	double tx = 2*r2 - 2*r1-delta;
	double ty = yoff - delta;
	System.out.println("clearance = " + (2*r2 - Math.sqrt(tx*tx + ty*ty)
					     - r1 - delta));
	    
	

	SteppedGrid sg = sgb.create();
	/*
	Path3D[] boundaries = PathSplitter.split(sg.getBoundary());
	double[] cpts = Path3DInfo
	    .getControlPoints(boundaries[0].getPathIterator(null), false);
	Path3D topBoundary = (cpts[2] == fix(h1/2 + delta))?
	    boundaries[1]: boundaries[0];
	Path3D bottomBoundary = (cpts[2] == fix(h1/2 + delta))?
	    boundaries[0]: boundaries[1];
	*/

	Path3D topBoundary = sg.getBoundary(new Point3D.Double(0.0, 0.0, h1),
					    null, true);
	Path3D bottomBoundary = sg
	    .getBoundary(new Point3D.Double(0.0, 0.0, fix(h1/2)), null, true);

	Path3D topCircle = cylinder.getBoundary(0, 0);
	int cn = cylinder.getUArrayLength();
	Path3D bottomCircle = cylinder.getBoundary(cn-1, 0);
	
	var c3 = new ConvexPathConnector(topCircle, topBoundary);
	var c4 = new ConvexPathConnector(bottomCircle, bottomBoundary);
	m3d.append(cylinder);
	m3d.append(c3);
	m3d.append(c4);

	Path3D boundary = m3d.getBoundary();
	if (boundary == null) {
	    System.out.println("m3d does not have a boundary");
	} else if (!boundary.isEmpty()) {
	    Path3DInfo.printSegments(boundary);
	} else {
	    System.out.println("m3d boundary is empty");
	}

	if (m3d.notPrintable(System.out)) {
	    System.out.println("m3d model not printable");
	    // System.exit(1);
	} else {
	    System.out.println("number of components = "
			       + m3d.numberOfComponents());
	}

	System.out.println("volume = " + m3d.volume());
	System.out.println("area = "  + m3d.area());

	// m3d.setTessellationLevel(3);
	m3d.setTessellationLevel(3);
	System.out.println("m3d bounds = " + m3d.getBounds());

	if (m3d.notPrintable(System.out)) {
	    System.out.println("m3d not printable after "
			       + "tessellation level set");
	}

	Model3D m3df = new Model3D(false);

	m3df.addModel(m3d, true);
	
	boolean paired = false;

	if (paired) {
	    m3df.setObjectRotation(Math.PI, 0.0, 0.0);
	    m3df.setObjectTranslation(0.0, 0.0, 0.0, 2*r2, 13*r1, 0.0);
	    m3df.addModel(m3d, true);
	}

	Rectangle3D bounds = m3df.getBounds();

	double offset = 1.0;
	double xoffset = 1.25;
	double xmin = bounds.getCenterX() - xoffset;
	double xmax = bounds.getCenterX() + xoffset;
	double ymin = bounds.getMinY() - offset;
	double zmin = bounds.getMinZ() - offset;
	double xwidth = 2*xoffset;
	double ywidth = bounds.getHeight() + 2*offset;
	double zwidth = bounds.getDepth() - 8;

	// To use the stepped grid class, we have to use
	// a different orientation to set it up.
	// This stepped grid's Z directon corresonds to the
	// X direction in m3df, the stepped grid's Y direction
	// corresonds to the Y direction in m3df, and the
	// stepped grid's X diretion corresponds to the Z
	// direction in m3df.
	
	boolean wrap = false;

	if (wrap) {
	    SteppedGrid.Builder sgfb = new SteppedGrid
		.Builder(m3df, xoffset, -xoffset);

	    sgfb.addRectangles(zmin-3, ymin-3, zwidth+6, ywidth+6,
			       0.0, 0.0, false, false);
	    sgfb.removeRectangles(zmin, ymin, zwidth, ywidth);

	    sgfb.addRectangles(zmin + h1 + 3*delta, ymin, 3, 4,
			       6.0, -6.0, false, false);
 
	    sgfb.addRectangles(zmin + h1 + 3*delta, ymin + ywidth-4, 3, 4,
			       6.0, -6.0, false, false);

	    m3df.setObjectRotation(-Math.PI/2, -Math.PI/2, Math.PI/2);

	    m3df.setObjectTranslation(zmin, ymin, 0.0,
				      r2, -6*r1 - xoffset + delta/2, -delta);

	    sgfb.create();
	}

	System.out.println("m3df number of components = "
			   + m3df.numberOfComponents());

	if (m3df.notPrintable(System.out)) {
	    System.out.println("m3df not printable");
	    // Saw this for a closed 2D manifold test.
	    // Try to recover the offending triangles to get a
	    // reasonably sized test case. We had added a tessellated
	    // version of m3d, which passed the tests, so there must have
	    // been a problem with tessellation (the manifold test does not
	    // tessellate the model first).
	    List<Model3D.Triangle> tlist = m3df.verifyEmbedded2DManifold();
	    if (tlist != null) {
		// we found intersection triangles, which might help
		// localize the error
		System.out.println("... intersecting triangles found");
	    }
	    List<Model3D.Edge> elist = m3df.verifyClosed2DManifold();
	    Model3D.Edge edge = elist.get(0);
	    double x1 = edge.getX1();
	    double y1 = edge.getY1();
	    double z1 = edge.getZ1();
	    double x2 = edge.getX2();
	    double y2 = edge.getY2();
	    double z2 = edge.getZ2();
	    System.out.format("edge = (%s, %s, %s)--(%s, %s, %s)\n",
			      x1, y1, z1, x2, y2, z2);
	    if ((x1 != (float)x1) || (y1 != (float) y1) || (z1 != (float)z1)
		|| (x2 != (float)x2) || (y2 != (float) y2) || (z2 != (float)z2))
		{
		    System.out.println("double values not equal to floats");
		}
	    Model3D m3dft = new Model3D(false);
	    m3dft.addLineSegment(x1, y1, z1, x2, y2, z2, Color.BLACK);
	    int i = 0;
	    for (Model3D.Triangle triangle: m3df.triangles()) {
		double tx1 = triangle.getX1();
		double ty1 = triangle.getY1();
		double tz1 = triangle.getZ1();
		double tx2 = triangle.getX2();
		double ty2 = triangle.getY2();
		double tz2 = triangle.getZ2();
		double tx3 = triangle.getX3();
		double ty3 = triangle.getY3();
		double tz3 = triangle.getZ3();
		double tlimit = 0.1;
		boolean skip1 =
		    ((Math.abs(tx1-x1)>tlimit) && (Math.abs(tx1-x2)>tlimit))
		    || ((Math.abs(ty1-y1)>tlimit) && (Math.abs(ty1-y2)>tlimit))
		    || ((Math.abs(tz1-z1)>tlimit) && (Math.abs(tz1-z2)>tlimit));
		boolean skip2 =
		    ((Math.abs(tx2-x1)>tlimit) && (Math.abs(tx2-x2)>tlimit))
		    || ((Math.abs(ty2-y1)>tlimit) && (Math.abs(ty2-y2)>tlimit))
		    || ((Math.abs(tz2-z1)>tlimit) && (Math.abs(tz2-z2)>tlimit));
		boolean skip3 = 
		    ((Math.abs(tx3-x1)>tlimit) && (Math.abs(tx3-x2)>tlimit))
		    || ((Math.abs(ty3-y1)>tlimit) && (Math.abs(ty3-y2)>tlimit))
		    || ((Math.abs(tz3-z1)>tlimit) && (Math.abs(tz3-z2)>tlimit));
		
		if (skip1 && skip2 && skip3) continue;
		System.out.println("triangle " + (++i) + ":");
		System.out.format("    (%s, %s, %s)-->(%s, %s, %s)\n",
				  tx1, ty1, tz1, tx2, ty2, tz2);
		System.out.format("    (%s, %s, %s)-->(%s, %s, %s)\n",
				  tx2, ty2, tz2, tx3, ty3, tz3);
		System.out.format("    (%s, %s, %s)-->(%s, %s, %s)\n",
				  tx3, ty3, tz3, tx1, ty1, tz1);
		if (i != 18  && i != 10) {
		    // try leaving triangle 18 & 10 out
		    m3dft.addTriangle(tx1, ty1, tz1,
				      tx2, ty2, tz2,
				      tx3, ty3, tz3);
		}
	    }
	    System.out.println("m3dft.size() = " + m3dft.size());
	    double val1 = 39.53090286254883;
	    double val2 = 39.530914306640625;
	    System.out.format("val2 - val1 = %g, ulp = %g, ratio = %g\n",
			      (val2-val1),  Math.ulp((float) val1),
			      (val2-val1)/Math.ulp((float) val1));
	    double range1[] = m3dft.zbracket(val1);
	    double range2[] = m3dft.zbracket(val2);
	    System.out.format("%s <= %s <= %s\n", range1[0], val1, range1[1]);
	    System.out.format("%s <= %s <= %s\n", range2[0], val2, range2[1]);
	    
	    Path3D boundary2 = m3dft.getBoundary();
	    List<Model3D.Edge> elist2 = m3dft.verifyClosed2DManifold();
	    System.out.println("elist2.size() = " + elist2.size());
	    edge = elist2.get(0);
	    x1 = edge.getX1(); y1 = edge.getY1(); z1 = edge.getZ1();
	    x2 = edge.getX2(); y2 = edge.getY2(); z2 = edge.getZ2();
	    System.out.format("edge(elist2) = (%s, %s, %s)--(%s, %s, %s)\n",
			      x1, y1, z1, x2, y2, z2);

	    System.out.println("creating m3dft images ...");
	    m3dft.createImageSequence(new FileOutputStream("fakelockT.isq"),
				      "png",
				      8, 6, 0.0, 0.5, 0.0, true);
	    System.exit(1);
	}


	m3df.writeSTL("fakelock (coordiantes in mm)", "fakelock.stl");

	System.out.println("final area = " + m3df.area());
	System.out.println("final volume = " + m3df.volume());
			   
	
	System.out.println("creating images ...");
	m3df.createImageSequence(new FileOutputStream("fakelockT.isq"),
				 "png",
				 8, 6, 0.0, 0.5, 0.0, false);
    }
}
