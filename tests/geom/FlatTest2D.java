import org.bzdev.geom.*;
import org.bzdev.math.RealValuedFunctionTwo;
import org.bzdev.graphs.Graph;
import java.awt.*;
import java.awt.geom.*;

public class FlatTest2D {

    public static void main(String argv[]) throws Exception {

	Path2D.Double path1 = new Path2D.Double();

	path1.moveTo (10.0, 20.0);
	path1.quadTo (50.0, 50.0, 20.0, 40.0);

	PathIterator pit = new 
	    FlatteningPathIterator2D(path1.getPathIterator(null), 1.0, 1);
	Path2D.Double path2 = new Path2D.Double();
	System.out.println("creating path2");
	path2.append(pit, false);
	System.out.println("printing path2");
	Path2DInfo.printSegments(System.out, path2);

	System.out.println("path1 length: " + Path2DInfo.pathLength(path1));
	System.out.println("path2 length: " + Path2DInfo.pathLength(path2));

	Path2D path2a = new Path2D.Double();
	pit = new 
	    FlatteningPathIterator2D(path1.getPathIterator(null), 0.0, 2);
	path2a.append(pit, false);
	System.out.println("path2a length: " + Path2DInfo.pathLength(path2a));
	
	System.out.println("printing path2a:");
	Path2DInfo.printSegments(System.out, path2a);


	Path2D.Double path3 = new Path2D.Double();

	path3.moveTo(10.0, 20.0);
	path3.curveTo(40.0, 40.0, 50.0, 50.0, 20.0, 40.0);

	pit = new FlatteningPathIterator2D(path3.getPathIterator(null),
					   1.0, 10);
	Path2D.Double path4 = new Path2D.Double();
	System.out.println("creating path4");
	path4.append(pit, false);
	System.out.println("printing path4");
	Path2DInfo.printSegments(System.out, path4);

	System.out.println("path3 length: " + Path2DInfo.pathLength(path3));
	System.out.println("path4 length: " + Path2DInfo.pathLength(path4));
	
	Path2D path4a = new Path2D.Double();
	pit = new FlatteningPathIterator2D(path3.getPathIterator(null),
					   0.0, 2);
	path4a.append(pit, false);
	System.out.println("path4a length: " + Path2DInfo.pathLength(path4a));
	System.out.println("printing path4a");
	Path2DInfo.printSegments(System.out, path4a);


	Path2D.Double path5 = new Path2D.Double();

	path5.moveTo (10.0, 20.0);
	
	path5.lineTo(30.0, 40.0);
	path5.quadTo( 50.0, 60.0, 80.0, 90.0);
	path5.curveTo(120.0, 130.0, 150.0, 126.0, 200.0, 210.0);
	path5.lineTo(0.0, 0.0);
	path5.closePath();
	path5.moveTo(300.0, 310.0);
	path5.lineTo(20.0, 30.0);
	path5.lineTo(100.0, 100.0);
	path5.lineTo(300.0, 310.0);
	path5.closePath();
	path5.quadTo(350.0, 350.0, 400.0, 410.0);
	
	AffineTransform aff = AffineTransform.getRotateInstance(Math.PI/4.0);
	PathIterator pit1 = path5.getPathIterator(aff);
	PathIterator pit3 = new FlatteningPathIterator2D
	    (path5.getPathIterator(null), aff, 0.0, 0);

	int cnt = 0;
	while (!pit1.isDone()) {
	    cnt++;
	    double[] c1 = new double[6];
	    double[] c3 = new double[6];
	    int mode1 = pit1.currentSegment(c1);
	    int mode3 = pit3.currentSegment(c3);
	    if (mode1 != mode3) {
		throw new Exception(String.format("cnt = %d: %d %d",
						  cnt,
						  mode1, mode3));
	    }
	    int len = 0;
	    switch(mode1) {
	    case PathIterator.SEG_CLOSE:
		break;
	    case PathIterator.SEG_MOVETO:
		len = 2;
		break;
	    case PathIterator.SEG_LINETO:
		len = 2;
		break;
	    case PathIterator.SEG_QUADTO:
		len = 4;
		break;
	    case PathIterator.SEG_CUBICTO:
		len = 6;
		break;
	    }
	    for (int i = 0; i < len; i++) {
		if (c1[i] != c3[i]) {
		    throw new Exception(String.format("cnt = %d: %g %g",
						      cnt, c1[i], c3[i]));
		}
	    }

	    pit1.next(); pit3.next();
	}


	System.out.println("Checking iteration for flatness 0.0:");
	pit = new 
	    FlatteningPathIterator2D(path5.getPathIterator(null), 0.0, 3);	
	double[] coords = new double[12];
	cnt = 0;
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
	
	pit = new FlatteningPathIterator2D(path5.getPathIterator(null),
					   0.0, 3);
	Path2D.Double path5a = new Path2D.Double();
	path5a.append(pit, false);
	
	System.out.println("path5 length: " + Path2DInfo.pathLength(path5));
	System.out.println("path5a length: " + Path2DInfo.pathLength(path5a));

	System.out.println("creating path6");
	path4.append(pit, false);
	Path2D.Double path6 = new Path2D.Double();
	path6.append(new FlatteningPathIterator2D(path5.getPathIterator(null),
						  1.0), false);
	System.out.println("printing path6");
	Path2DInfo.printSegments(System.out, path6);

	System.out.println("path5 length: " + Path2DInfo.pathLength(path5));
	System.out.println("path6 length: " + Path2DInfo.pathLength(path6));

	RealValuedFunctionTwo xfunct = new RealValuedFunctionTwo() {
		public double valueAt(double x, double y) {
		    return x * Math.tanh(y/100.0);
		}
		public double deriv1At(double x, double y) {
		    return Math.tanh(y/100.0);
		}
		public double deriv2At(double x, double y) {
		    double thy = Math.tanh(y/100.0);
		    double dy = (1 -thy*thy)/100.0;
		    return x * dy;
		}
	    };

	RealValuedFunctionTwo yfunct = new RealValuedFunctionTwo() {
		public double valueAt(double x, double y) {
		    return y* Math.tanh(x/100.0);
		}  
		public double deriv1At(double x, double y) {
		    double thx = Math.tanh(x/100.0);
		    double dx = (1 -thx*thx)/100.0;
		    return y * dx;
		}
		public double deriv2At(double x, double y) {
		    return Math.tanh(x/100.0);
		}
	    };


	Transform2D tf = new RVFTransform2D(xfunct, yfunct);


	AffineTransform af = tf.affineTransform(40.0, 50.0);
	Point2D pt1 = new Point2D.Double(40.0, 50.0);
	Point2D pt2 = new Point2D.Double(40.0, 50.0);
	tf.transform(pt1, pt2);
	System.out.println("point = " + pt1 + ", transformed = " + pt2);
	af.transform(pt1, pt2);
	System.out.println("point = " + pt1 + ", affine transformed = " + pt2);
	System.out.println();
	
	pt1.setLocation(41.0, 51.0);
	tf.transform(pt1, pt2);
	System.out.println("point = " + pt1 + ", transformed = " + pt2);
	af.transform(pt1, pt2);
	System.out.println("point = " + pt1 + ", affine transformed = " + pt2);
	System.out.println();

	PathIterator opit = path5.getPathIterator(null);
	pit = new FlatteningPathIterator2D(path5.getPathIterator(null),
					   tf, 0.0, 0);
	Path2D path7 = new Path2D.Double();
	path7.append(pit, false);
	System.out.println();
	pit = path7.getPathIterator(null);

	double[] coords1 = new double[8];
	double[] coords2 = new double[8];
	double[] coords3 = new double[8];
	while (!(pit.isDone() || opit.isDone())) {
	    int mode1 = opit.currentSegment(coords1);
	    int mode2 = pit.currentSegment(coords2);
	    if (mode1 != mode2) {
		throw new Exception("modes don't match");
	    }
	    int len = 0;
	    switch(mode1) {
	    case PathIterator.SEG_CLOSE:
		System.out.println("mode = SEG_CLOSE");
		break;
	    case PathIterator.SEG_MOVETO:
		System.out.println("mode = SEG_MOVETO");
		len = 1;
		break;
	    case PathIterator.SEG_LINETO:
		System.out.println("mode = SEG_LINETO");
		len = 1;
		break;
	    case PathIterator.SEG_QUADTO:
		System.out.println("mode = SEG_QUADTO");
		len = 2;
		break;
	    case PathIterator.SEG_CUBICTO:
		System.out.println("mode = SEG_CUBICTO");
		len = 3;
		break;
	    }
	    tf.transform(coords1, 0, coords3, 0, len);
	    for (int i = 0; i < len*2; i ++) {
		System.out.format("%g %g %g\n",
				  coords1[i], coords2[i], coords3[i]);
	    }
	    opit.next();
	    pit.next();
	}
 
	System.out.println("creating path 8");
	Path2D path8 = new Path2D.Double();

	path8.moveTo (10.0, 20.0);
	
	path8.lineTo(30.0, 40.0);
	path8.quadTo( 50.0, 60.0, 80.0, 90.0);
	path8.curveTo(120.0, 130.0, 150.0, 126.0,
		      200.0, 210.0);
	path8.lineTo(0.0, 0.0);
	
	pit = new FlatteningPathIterator2D(path8.getPathIterator(null),
					   tf, 0.1);
	System.out.println("checking path 8 with flattening iterator");
	while (!pit.isDone()) {
	    int mode2 = pit.currentSegment(coords2);
	    int len = 0;
	    switch(mode2) {
	    case PathIterator.SEG_CLOSE:
		System.out.println("mode = SEG_CLOSE");
		break;
	    case PathIterator.SEG_MOVETO:
		System.out.println("mode = SEG_MOVETO");
		len = 1;
		break;
	    case PathIterator.SEG_LINETO:
		System.out.println("mode = SEG_LINETO");
		len = 1;
		break;
	    case PathIterator.SEG_QUADTO:
		System.out.println("mode = SEG_QUADTO");
		len = 2;
		break;
	    case PathIterator.SEG_CUBICTO:
		System.out.println("mode = SEG_CUBICTO");
		len = 3;
		break;
	    }
	    tf.transform(coords1, 0, coords3, 0, len);
	    for (int i = 0; i < len*2; i ++) {
		System.out.format("%g\n", coords2[i]);
	    }
	    pit.next();
	}
	

	System.out.println("creating path 9");

	BasicSplinePath2D path9 = new BasicSplinePath2D(path8);
	Path2D path10a = new Path2D.Double();
	path10a.append(new
		       FlatteningPathIterator2D(path8.getPathIterator(null),
						tf, 0.1), false);
	BasicSplinePath2D path10 = new BasicSplinePath2D(path10a);

	double maxu = path9.getMaxParameter();

	double[] xs = new double[101];
	double[] ys = new double[101];
	for (int i = 0; i <= 100; i++) {
	    double u = (maxu*i)/100.0;
	    double[] ppt1 = {path9.getX(u), path9.getY(u)};
	    double[] ppt2 = new double[2];
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

	graph.write("png", "ftest2.png");
	
	Path2D path13 = new Path2D.Double();
	path13.moveTo(0.0, 0.0);
	path13.curveTo(10.0, 20.0,
		       80.0, -20.0,
		       100.0, 5.0);
	Path2D path14 = new Path2D.Double();
	pit = new 
	    FlatteningPathIterator2D(path13.getPathIterator(null), 0.0, 2);
	System.out.println("appending to path14");
	path14.append(pit, false);

	System.out.println("path13 length: " + Path2DInfo.pathLength(path13));
	System.out.println("path14 length: " + Path2DInfo.pathLength(path14));
	
	


	coords = new double[6];
	coords[0] = 10.0;
	coords[1] = 20.0;
	coords[2] = 80.0;
	coords[3] = -20.0;
	coords[4] = 100.0;
	coords[5] = 5.0;

	


	System.out.println("full segment length: "
			   + Path2DInfo.segmentLength(PathIterator.SEG_CUBICTO,
						      0.0, 0.0, coords));
	
	pit = new FlatteningPathIterator2D(PathIterator.SEG_CUBICTO,
					   0.0, 0.0,
					   coords, 100.0, 0);
	

	if (pit.isDone()) {
	    throw new Exception("iteration should not have ended");
	}
	if (pit.currentSegment(coords) != PathIterator.SEG_MOVETO) {
	    throw new Exception("not SEG_MOVETO");
	}
	if (coords[0] != 0.0 || coords[1] != 0.0)
	    throw new Exception("initial value bad");
	pit.next();
	if (pit.isDone()) {
	    throw new Exception("iteration should not have ended");
	}
	if (pit.currentSegment(coords) != PathIterator.SEG_CUBICTO) {
	    throw new Exception("not SEG_CUBICTO");
	}
	System.out.println("full segment length (limit = 0): "
			   + Path2DInfo.segmentLength(PathIterator.SEG_CUBICTO,
						      0.0, 0.0, coords));

	if (coords[0] != 10.0 || coords[1] != 20.0
	    || coords[3] != -20.0 || coords[2] != 80.0
	    || coords[4] != 100.0 || coords[5] != 5.0) {
	    throw new Exception("bad coords");
	}

	pit.next();
	if (!pit.isDone()) {
	    throw new Exception("iteration should have ended");
	}

	coords[0] = 10.0;
	coords[1] = 20.0;
	coords[2] = 80.0;
	coords[3] = -20.0;
	coords[4] = 100.0;
	coords[5] = 5.0;
	
	pit = new FlatteningPathIterator2D(PathIterator.SEG_CUBICTO,
					   0.0, 0.0,
					   coords, 0.0, 2);
	Path2D fpath = new Path2D.Double();
	fpath.append(pit, false);
	// Path2DInfo.printSegments(System.out, fpath);
	System.out.println("fpath length = " + Path2DInfo.pathLength(fpath));


	pit = new FlatteningPathIterator2D(PathIterator.SEG_CUBICTO,
					   0.0, 0.0,
					   coords, 0.0, 2);



	pit.next();

	double x0 = 0.0, y0 = 0.0;
	double tlen = 0.0;
	while (!pit.isDone()) {
	    int st = pit.currentSegment(coords);
	    if (st != PathIterator.SEG_CUBICTO)
		throw new Exception("st not SEG_CUBICTO");
	    double len = Path2DInfo.segmentLength(st,x0, y0, coords);
	    tlen += len;
	    System.out.format("len = %s\n", len);
	    x0 = coords[4];
	    y0 = coords[5];
	    pit.next();
	}
	System.out.format("tlen = %s\n", tlen);
	

	System.exit(0);


  }
}