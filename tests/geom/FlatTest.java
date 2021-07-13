import org.bzdev.geom.*;
import org.bzdev.math.RealValuedFunctionThree;
import org.bzdev.graphs.Graph;
import java.awt.*;
import java.awt.geom.*;

public class FlatTest {

    public static void main(String argv[]) throws Exception {

	Path3D.Double path1 = new Path3D.Double();

	path1.moveTo (10.0, 20.0, 30.0);
	path1.quadTo (50.0, 50.0, 50.0, 20.0, 40.0, 60.0);

	PathIterator3D pit = new 
	    FlatteningPathIterator3D(path1.getPathIterator(null), 1.0, 1);
	Path3D.Double path2 = new Path3D.Double();
	System.out.println("creating path2");
	path2.append(pit, false);
	System.out.println("printing path2");
	Path3DInfo.printSegments(System.out, path2);

	System.out.println("path1 length: " + Path3DInfo.pathLength(path1));
	System.out.println("path2 length: " + Path3DInfo.pathLength(path2));

	Path3D.Double path3 = new Path3D.Double();

	path3.moveTo(10.0, 20.0, 30.0);
	path3.curveTo(40.0, 40.0, 40.0, 50.0, 50.0, 50.0, 20.0, 40.0, 60.0);

	pit = new FlatteningPathIterator3D(path3.getPathIterator(null),
					   1.0, 10);
	Path3D.Double path4 = new Path3D.Double();
	System.out.println("creating path4");
	path4.append(pit, false);
	System.out.println("printing path4");
	Path3DInfo.printSegments(System.out, path4);

	System.out.println("path3 length: " + Path3DInfo.pathLength(path3));
	System.out.println("path4 length: " + Path3DInfo.pathLength(path4));


	Path3D.Double path5 = new Path3D.Double();

	path5.moveTo (10.0, 20.0, 30.0);
	
	path5.lineTo(30.0, 40.0, 50.0);
	path5.quadTo( 50.0, 60.0, 70.0, 80.0, 90.0, 100.0);
	path5.curveTo(120.0, 130.0, 140.0, 150.0, 126.0, 160.0,
		      200.0, 210.0, 120.0);
	path5.lineTo(0.0, 0.0, 0.0);
	path5.closePath();
	path5.moveTo(300.0, 310.0, 330.0);
	path5.lineTo(20.0, 30.0, 4.0);
	path5.lineTo(100.0, 100.0, 100.0);
	path5.lineTo(300.0, 310.0, 330.0);
	path5.closePath();
	path5.quadTo(350.0, 350.0, 350.0, 400.0, 410.0, 420.0);

	System.out.println("Checking iteration for flatness 0.0:");
	pit = new 
	    FlatteningPathIterator3D(path5.getPathIterator(null), 0.0, 3);	
	double[] coords = new double[12];
	int cnt = 0;
	int mode = -1;
	while (!pit.isDone()) {
	    int newmode = pit.currentSegment(coords);
	    if (mode == -1) {
		mode = newmode;
	    } else if (newmode != mode) {
		System.out.format("mode = %d, count = %d\n", mode, cnt);
		cnt = 1;
		mode = newmode;
	    } else {
		cnt++;
	    }
	    pit.next();
	}
	
	pit = new FlatteningPathIterator3D(path5.getPathIterator(null),
					   0.0, 3);
	Path3D.Double path5a = new Path3D.Double();
	path5a.append(pit, false);
	
	System.out.println("path5 length: " + Path3DInfo.pathLength(path5));
	System.out.println("path5a length: " + Path3DInfo.pathLength(path5a));


	System.out.println("creating path6");
	path4.append(pit, false);
	Path3D.Double path6 = new Path3D.Double();
	path6.append(new FlatteningPathIterator3D(path5.getPathIterator(null),
						  1.0), false);
	System.out.println("printing path6");
	Path3DInfo.printSegments(System.out, path6);

	System.out.println("path5 length: " + Path3DInfo.pathLength(path5));
	System.out.println("path6 length: " + Path3DInfo.pathLength(path6));

	RealValuedFunctionThree xfunct = new RealValuedFunctionThree() {
		public double valueAt(double x, double y, double z) {
		    return x * Math.tanh(y/100.0) * Math.tanh(z/100.0);
		}
		public double deriv1At(double x, double y, double z) {
		    return Math.tanh(y/100.0) * Math.tanh(z/100.0);
		}
		public double deriv2At(double x, double y, double z) {
		    double thy = Math.tanh(y/100.0);
		    double dy = (1 -thy*thy)/100.0;
		    return x * dy * Math.tanh(z/100.0);
		}
		public double deriv3At(double x, double y, double z) {
		    double thz = Math.tanh(z/100.0);
		    double dz = (1 -thz*thz)/100.0;
		    return x * dz * Math.tanh(y/100.0);
		}
	    };

	RealValuedFunctionThree yfunct = new RealValuedFunctionThree() {
		public double valueAt(double x, double y, double z) {
		    return y* Math.tanh(x/100.0) * Math.tanh(z/100.0);
		}  
		public double deriv1At(double x, double y, double z) {
		    double thx = Math.tanh(x/100.0);
		    double dx = (1 -thx*thx)/100.0;
		    return y * dx * Math.tanh(z/100.0);
		}
		public double deriv2At(double x, double y, double z) {
		    return Math.tanh(x/100.0) * Math.tanh(z/100.0);
		}
		public double deriv3At(double x, double y, double z) {
		    double thz = Math.tanh(z/100.0);
		    double dz = (1 -thz*thz)/100.0;
		    return y * Math.tanh(x/100.0) * dz;
		}
	    };

	RealValuedFunctionThree zfunct = new RealValuedFunctionThree() {
		public double valueAt(double x, double y, double z) {
		    return z* Math.tanh(x/100.0) * Math.tanh(y/100.0);
		}  
		public double deriv1At(double x, double y, double z) {
		    double thx = Math.tanh(x/100.0);
		    double dx = (1 -thx*thx)/100.0;
		    return z * dx * Math.tanh(y/100.0);
		}
		public double deriv2At(double x, double y, double z) {
		    double thy = Math.tanh(y/100.0);
		    double dy = (1 -thy*thy)/100.0;
		    return z * dy * Math.tanh(x/100.0);
		}
		public double deriv3At(double x, double y, double z) {
		    return Math.tanh(x/100.0) * Math.tanh(y/100.0);
		}
	    };

	Transform3D tf = new RVFTransform3D(xfunct, yfunct, zfunct);


	AffineTransform3D af = tf.affineTransform(40.0, 50.0, 60.0);
	Point3D pt1 = new Point3D.Double(40.0, 50.0, 60.0);
	Point3D pt2 = new Point3D.Double(40.0, 50.0, 60.0);
	tf.transform(pt1, pt2);
	System.out.println("point = " + pt1 + ", transformed = " + pt2);
	af.transform(pt1, pt2);
	System.out.println("point = " + pt1 + ", affine transformed = " + pt2);
	System.out.println();
	
	pt1.setLocation(41.0, 51.0, 61.0);
	tf.transform(pt1, pt2);
	System.out.println("point = " + pt1 + ", transformed = " + pt2);
	af.transform(pt1, pt2);
	System.out.println("point = " + pt1 + ", affine transformed = " + pt2);
	System.out.println();

	PathIterator3D opit = path5.getPathIterator(null);
	pit = new FlatteningPathIterator3D(path5.getPathIterator(null),
					   tf, 0.0, 0);
	Path3D path7 = new Path3D.Double();
	path7.append(pit, false);
	System.out.println();
	pit = path7.getPathIterator(null);

	double[] coords1 = new double[12];
	double[] coords2 = new double[12];
	double[] coords3 = new double[12];
	while (!(pit.isDone() || opit.isDone())) {
	    int mode1 = opit.currentSegment(coords1);
	    int mode2 = pit.currentSegment(coords2);
	    if (mode1 != mode2) {
		throw new Exception("modes don't match");
	    }
	    int len = 0;
	    switch(mode1) {
	    case PathIterator3D.SEG_CLOSE:
		System.out.println("mode = SEG_CLOSE");
		break;
	    case PathIterator3D.SEG_MOVETO:
		System.out.println("mode = SEG_MOVETO");
		len = 1;
		break;
	    case PathIterator3D.SEG_LINETO:
		System.out.println("mode = SEG_LINETO");
		len = 1;
		break;
	    case PathIterator3D.SEG_QUADTO:
		System.out.println("mode = SEG_QUADTO");
		len = 2;
		break;
	    case PathIterator3D.SEG_CUBICTO:
		System.out.println("mode = SEG_CUBICTO");
		len = 3;
		break;
	    }
	    tf.transform(coords1, 0, coords3, 0, len);
	    for (int i = 0; i < len*3; i ++) {
		System.out.format("%g %g %g\n",
				  coords1[i], coords2[i], coords3[i]);
	    }
	    opit.next();
	    pit.next();
	}
 
	Path3D path8 = new Path3D.Double();

	path8.moveTo (10.0, 20.0, 30.0);
	
	path8.lineTo(30.0, 40.0, 50.0);
	path8.quadTo( 50.0, 60.0, 70.0, 80.0, 90.0, 100.0);
	path8.curveTo(120.0, 130.0, 140.0, 150.0, 126.0, 160.0,
		      200.0, 210.0, 120.0);
	path8.lineTo(0.0, 0.0, 0.0);
	

	BasicSplinePath3D path9 = new BasicSplinePath3D(path8);
	Path3D path10a = new Path3D.Double();
	path10a.append(new
		       FlatteningPathIterator3D(path8.getPathIterator(null),
						tf, 0.1), false);
	BasicSplinePath3D path10 = new BasicSplinePath3D(path10a);

	// create 2D paths by ignoring the Z coordinates. Then we
	// can easily plot curves to compare the two paths
	// visually.
	double maxu = path9.getMaxParameter();

	double[] xs = new double[101];
	double[] ys = new double[101];
	for (int i = 0; i <= 100; i++) {
	    double u = (maxu*i)/100.0;
	    double[] ppt1 = {path9.getX(u), path9.getY(u), path9.getZ(u)};
	    double[] ppt2 = new double[3];
	    tf.transform(ppt1, 0, ppt2, 0, 1);
	    xs[i] = ppt2[0];
	    ys[i] = ppt2[1];
	}
	Path2D path11 = new BasicSplinePath2D(xs, ys, false);

	maxu = path10.getMaxParameter();
	for (int i = 0; i <= 100; i++) {
	    double u = (maxu*i)/100.0;
	    xs[i] = path10.getX(u);
	    ys[i] = path10.getY(u);
	}
	
	Path2D path12 = new BasicSplinePath2D(xs, ys, false);

	Graph graph = new Graph(700, 700);
	graph.setOffsets(25,25);
	graph.setRanges(0.0, 200.0 , 0.0, 200.0);
	
	Graphics2D g2d = graph.createGraphics();

	g2d.setStroke(new BasicStroke(4.0F));
	g2d.setColor(Color.RED);
	graph.draw(g2d, path11);

	g2d.setStroke(new BasicStroke(3.0F));
	g2d.setColor(Color.BLACK);
	graph.draw(g2d, path12);

	graph.write("png", "ftest.png");
	
	Path3D path13 = new Path3D.Double();
	path13.moveTo(0.0, 0.0, 0.0);
	path13.curveTo(10.0, 20.0, 25.0,
		       80.0, -20.0, -25.0,
		       100.0, 5.0, 7.0);
	Path3D path14 = new Path3D.Double();
	pit = new
	    FlatteningPathIterator3D(path13.getPathIterator(null), 0.0, 2);
	System.out.println("appending to path14");
	path14.append(pit, false);

	System.out.println("path13 length: " + Path3DInfo.pathLength(path13));
	System.out.println("path14 length: " + Path3DInfo.pathLength(path14));

	coords = new double[9];
	coords[0] = 10.0;
	coords[1] = 20.0;
	coords[2] = 25.0;
	coords[3] = 80.0;
	coords[4] = -20.0;
	coords[5] = -25.0;
	coords[6] = 100.0;
	coords[7] = 5.0;
	coords[8] = 7.0;

	System.out.println("full segment length: "
			   + Path3DInfo.segmentLength(PathIterator.SEG_CUBICTO,
						      0.0, 0.0, 0.0, coords));

	pit = new FlatteningPathIterator3D(PathIterator.SEG_CUBICTO,
					   0.0, 0.0, 0.0,
					   coords, 100.0, 0);

	if (pit.isDone()) {
	    throw new Exception("iteration should not have ended");
	}
	if (pit.currentSegment(coords) != PathIterator.SEG_MOVETO) {
	    throw new Exception("not SEG_MOVETO");
	}
	if (coords[0] != 0.0 || coords[1] != 0.0 || coords[2] != 0.0)
	    throw new Exception("initial value bad");
	pit.next();
	if (pit.isDone()) {
	    throw new Exception("iteration should not have ended");
	}
	if (pit.currentSegment(coords) != PathIterator.SEG_CUBICTO) {
	    throw new Exception("not SEG_CUBICTO");
	}
	System.out.println("full segment length (limit = 0): "
			   + Path3DInfo.segmentLength(PathIterator.SEG_CUBICTO,
						      0.0, 0.0, 0.0, coords));

	if (coords[0] != 10.0 || coords[1] != 20.0 || coords[2] != 25.0
	    || coords[3] != 80.0 || coords[4] != -20.0 || coords[5] != -25.0
	    || coords[6] != 100.0 || coords[7] != 5.0 || coords[8] != 7.0) {
	    throw new Exception("bad coords");
	}

	pit.next();
	if (!pit.isDone()) {
	    throw new Exception("iteration should have ended");
	}

	coords[0] = 10.0;
	coords[1] = 20.0;
	coords[2] = 25.0;
	coords[3] = 80.0;
	coords[4] = -20.0;
	coords[5] = -25.0;
	coords[6] = 100.0;
	coords[7] = 5.0;
	coords[8] = 7.0;

	pit = new FlatteningPathIterator3D(PathIterator3D.SEG_CUBICTO,
					   0.0, 0.0, 0.0,
					   coords, 0.0, 2);
	Path3D fpath = new Path3D.Double();
	fpath.append(pit, false);
	// Path3DInfo.printSegments(System.out, fpath);
	System.out.println("fpath length = " + Path3DInfo.pathLength(fpath));


	pit = new FlatteningPathIterator3D(PathIterator3D.SEG_CUBICTO,
					   0.0, 0.0, 0.0,
					   coords, 0.0, 2);

	pit.next();

	double x0 = 0.0, y0 = 0.0, z0 = 0.0;
	double tlen = 0.0;
	while (!pit.isDone()) {
	    int st = pit.currentSegment(coords);
	    if (st != PathIterator.SEG_CUBICTO)
		throw new Exception("st not SEG_CUBICTO");
	    double len = Path3DInfo.segmentLength(st,x0, y0, z0, coords);
	    tlen += len;
	    System.out.format("len = %s\n", len);
	    x0 = coords[6];
	    y0 = coords[7];
	    z0 = coords[8];
	    pit.next();
	}
	System.out.format("tlen = %s\n", tlen);


	System.exit(0);


  }
}