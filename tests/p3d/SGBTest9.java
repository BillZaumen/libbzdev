import org.bzdev.p3d.*;
import org.bzdev.math.rv.*;
import java.awt.*;
import java.awt.image.*;


public class SGBTest9 {
    static IntegerRandomVariable iz = new UniformIntegerRV( 0, true, 5, true);
    static double zIncr() {return (double)iz.next();}

    static BooleanRandomVariable brv = new BinomialBooleanRV(0.5);
    static boolean allowed() {return brv.next();}

    public static void main(String argv[]) throws Exception {
	

	for (int i = 0; i < 1000; i++) {
	    if (i % 50 == 0) {
		System.out.println("i = " + i);
	    }

	    Model3D m3d = new Model3D(false);

	    SteppedGrid.Builder sgb = new SteppedGrid.Builder(m3d, 10.0, -10.0);

	    sgb.addRectangles(0.0, 0.0, 100.0, 20.0, zIncr(), zIncr());
	    sgb.addRectangles(0.0, 80.0, 100.0, 20.0, zIncr(), zIncr());
	    sgb.addRectangles(0.0, 0.0, 20.0, 100.0, zIncr(), zIncr());
	    sgb.addRectangles(80.0, 0.0, 20.0, 100.0, zIncr(), zIncr());

	    sgb.addX(30.0);
	    sgb.addX(70.0);
	    sgb.addY(30.0);
	    sgb.addY(70.0);

	    sgb.addX(10.0);
	    sgb.addX(90.0);
	    sgb.addY(10.0);
	    sgb.addY(90.0);

	    sgb.addX(5.0);
	    sgb.addX(95.0);
	    sgb.addY(5.0);
	    sgb.addY(95.0);

	    sgb.addX(25.0);
	    sgb.addX(75.0);
	    sgb.addY(25.0);
	    sgb.addY(75.0);


	    if (allowed()) {
		sgb.addHalfRectangles(0.0, 0.0, 2,
				      SteppedGrid.Builder.Corner.UPPER_RIGHT,
				      zIncr(), zIncr());
	    }
	    if (allowed()) {
		sgb.addHalfRectangles(90.0, 0.0, 2,
				      SteppedGrid.Builder.Corner.UPPER_LEFT,
				      zIncr(), zIncr());
	    }

	    if (allowed()) {
		sgb.addHalfRectangles(90.0, 90.0, 2,
				      SteppedGrid.Builder.Corner.LOWER_LEFT,
				      zIncr(), zIncr());
	    }

	    if (allowed()) {
		sgb.addHalfRectangles(0.0, 90.0, 2,
				      SteppedGrid.Builder.Corner.LOWER_RIGHT,
				      zIncr(), zIncr());
	    }

	    if (allowed()) {
		sgb.addHalfRectangles(20.0, 20.0, 2,
				      SteppedGrid.Builder.Corner.LOWER_LEFT,
				      zIncr(), zIncr());
	    }
	    if (allowed()) {
		sgb.addHalfRectangles(70.0, 20.0, 2,
				      SteppedGrid.Builder.Corner.LOWER_RIGHT,
				      zIncr(), zIncr());
	    }
	    if (allowed()) {
		sgb.addHalfRectangles(70.0, 70.0, 2,
				      SteppedGrid.Builder.Corner.UPPER_RIGHT,
				      zIncr(), zIncr());
	    }
	    if (allowed()) {
		sgb.addHalfRectangles(20.0, 70.0, 2,
				      SteppedGrid.Builder.Corner.UPPER_LEFT,
				      zIncr(), zIncr());
	    }

	    sgb.create(m3d);

	    if (m3d.notPrintable(System.out)) {
		System.out.println("not printable");
		System.exit(1);
	    }
	}
	System.exit(0);
    }
}
