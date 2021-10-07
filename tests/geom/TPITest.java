import java.awt.geom.*;
import java.util.Arrays;
import org.bzdev.geom.*;
import org.bzdev.math.*;

public class TPITest {
    public static void main(String argv[]) throws Exception {
	Path2D path2d = new Path2D.Double();
	path2d.moveTo(10.0, 20.0);
	path2d.lineTo(30.0, 40.0);
	path2d.quadTo(40.0, 55.0, 50.0, 60.0);
	path2d.curveTo(60.0, 75.0, 70.0, 85.0, 90.0, 100.0);
	path2d.closePath();

	AffineTransform af = AffineTransform.getTranslateInstance(7.0, 8.0);
	Transform2D tf = new RVFTransform2D
	    (new RealValuedFunctionTwo((x, y) -> {return x + 7.0;},
				       (x, y) -> {return 1.0;},
				       (x, y) -> {return 0.0;}),
	     new RealValuedFunctionTwo((x, y) -> {return y + 8.0;},
				       (x, y) -> {return 1.0;},
				       (x, y) -> {return 0.0;}));


	PathIterator pi2d = path2d.getPathIterator(af);

	PathIterator pi2d1 =
	    new TransformedPathIterator(path2d.getPathIterator(null), af);

	PathIterator pi2d2 =
	    new TransformedPathIterator(path2d.getPathIterator(null), tf);
	
	
	double[] coords = new double[9];
	double[] coords1 = new double[9];
	double[] coords2 = new double[9];

	while (!pi2d.isDone()) {
	    Arrays.fill(coords, 0.0);
	    Arrays.fill(coords1, 0.0);
	    Arrays.fill(coords2, 0.0);
	    int type = pi2d.currentSegment(coords);
	    int type1 = pi2d1.currentSegment(coords1);
	    int type2 = pi2d2.currentSegment(coords2);
	    if (type != type1 || type != type2) {
		throw new Exception();
	    }
	    for (int i = 0; i < 9; i++) {
		if (coords[i] != coords1[i]
		    || coords[i] != coords2[i]) {
		    throw new Exception();
		}
	    }
	    pi2d.next();
	    pi2d1.next();
	    pi2d2.next();
	}
	if (! pi2d1.isDone() || ! pi2d2.isDone()) throw new Exception();

	Path3D path3d = new Path3D.Double();
	path3d.moveTo(10.0, 20.0, 110.0);
	path3d.lineTo(30.0, 40.0, 130.0);
	path3d.quadTo(40.0, 55.0, 140.0, 50.0, 60.0, 150.0);
	path3d.curveTo(60.0, 75.0, 160.0, 70.0, 85.0, 170.0,  90.0, 100.0, 190.0);
	path3d.closePath();

	AffineTransform3D af3d = AffineTransform3D.getTranslateInstance(7.0, 8.0, 9.0);
	Transform3D tf3d = new RVFTransform3D
	    (new RealValuedFunctionThree((x, y, z) -> {return x + 7.0;},
					 (x, y, z) -> {return 1.0;},
					 (x, y, z) -> {return 0.0;},
					 (x, y, z) -> {return 0.0;}),
	     new RealValuedFunctionThree((x, y, z) -> {return y + 8.0;},
					 (x, y, z) -> {return 0.0;},
					 (x, y, z) -> {return 1.0;},
					 (x, y, z) -> {return 0.0;}),
	     new RealValuedFunctionThree((x, y, z) -> {return z + 9.0;},
					 (x, y, z) -> {return 0.0;},
					 (x, y, z) -> {return 0.0;},
					 (x, y, z) -> {return 1.0;}));


	PathIterator3D pi3d = path3d.getPathIterator(af3d);

	PathIterator3D pi3d1 =
	    new TransformedPathIterator3D(path3d.getPathIterator(null), af3d);

	PathIterator3D pi3d2 =
	    new TransformedPathIterator3D(path3d.getPathIterator(null), tf3d);
	
	
	while (!pi3d.isDone()) {
	    Arrays.fill(coords, 0.0);
	    Arrays.fill(coords1, 0.0);
	    Arrays.fill(coords2, 0.0);
	    int type = pi3d.currentSegment(coords);
	    int type1 = pi3d1.currentSegment(coords1);
	    int type2 = pi3d2.currentSegment(coords2);
	    if (type != type1 || type != type2) {
		throw new Exception();
	    }
	    for (int i = 0; i < 9; i++) {
		if (coords[i] != coords1[i]
		    || coords[i] != coords2[i]) {
		    throw new Exception();
		}
	    }
	    pi3d.next();
	    pi3d1.next();
	    pi3d2.next();
	}
	if (! pi3d1.isDone() || ! pi3d2.isDone()) throw new Exception();
    }
}
