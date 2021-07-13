import org.bzdev.util.Cloner;
import org.bzdev.geom.*;
import java.awt.*;
import java.awt.geom.*;

public class CloneTest {
    public static void main(String argv[]) throws Exception {
	Path2D path = new Path2D.Double();
	path.moveTo(10.0, 20.0);
	path.lineTo(20.0, 30.0);

	Shape shape = Cloner.makeClone(path);
	System.out.println(shape.getClass());

	SplinePath2D spath =new SplinePath2D();
	spath.moveTo(10.0, 20.0);
	spath.lineTo(20.0, 30.0);
	try {
	    shape = Cloner.makeClone(spath);
	    System.out.println(shape.getClass());
	    System.exit(1);
	} catch (Exception e) {
	    System.out.println("exception expected: " + e.getMessage());
	}
	shape = Cloner.makeCastedClone(Shape.class, spath);
	System.out.println(shape.getClass());


	BasicSplinePath2D bpath =new BasicSplinePath2D();
	bpath.moveTo(10.0, 20.0);
	bpath.lineTo(20.0, 30.0);
	shape = Cloner.makeCastedClone(Shape.class, bpath);
	System.out.println(shape.getClass());
	if (shape instanceof Path2D) {
	    path = (Path2D) shape;
	    System.out.println("For partially cloned BasicSplinePath:");
	    for (Path2DInfo.Entry entry: Path2DInfo.getEntries(path)) {
		System.out.format("    entry: type %s, end = %s\n",
				  entry.getTypeString(), entry.getEnd());
	    }
	}

	byte[] barray = {0, 1, 2};
	byte[] bcopy = Cloner.makeClone(barray);

	byte[] sarray = {0, 1, 2};
	byte[] scopy = Cloner.makeClone(sarray);

	int[] iarray = {0, 1, 2};
	int[] icopy = Cloner.makeClone(iarray);

	long[] larray = {0L, 1L, 2L};
	long[] lcopy = Cloner.makeClone(larray);

	float[] farray = {0.0F, 1.0F, 2.0F};
	float[] fcopy = Cloner.makeClone(farray);

	double[] darray = {0.0, 1.0, 2.0};
	double[] dcopy = Cloner.makeClone(darray);

	boolean[] blarray = {true, false};
	boolean[] blcopy = Cloner.makeClone(blarray);

	String[] starray = {"hello", "goodbye"};
	String[] stcopy = Cloner.makeClone(starray);

	System.exit(0);
    }
}
