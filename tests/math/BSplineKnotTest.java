import org.bzdev.math.*;


public class BSplineKnotTest {
    public static void main(String argv[]) throws Exception {

	double[] xs = new double[21];
	for (int i = 0; i < 21; i++) {
	    xs[i] = i;
	}

	System.out.println("CLAMPED");
	double[] knots = BSpline.createKnots(5, 10, BSpline.Mode.CLAMPED, xs);
	for (double x: knots) {
	    System.out.format("%g ", x);
	}
	System.out.println("\n");

	System.out.println("UNCLAMPED");
	knots = BSpline.createKnots(5, 10, BSpline.Mode.UNCLAMPED, xs);
	for (double x: knots) {
	    System.out.format("%g ", x);
	}
	System.out.println("\n");	


	System.out.println("CLAMPED_LEFT");
	knots = BSpline.createKnots(5, 10, BSpline.Mode.CLAMPED_LEFT, xs);
	for (double x: knots) {
	    System.out.format("%g ", x);
	}
	System.out.println("\n");	


	System.out.println("CLAMPED_RIGHT");
	knots = BSpline.createKnots(5, 10, BSpline.Mode.CLAMPED_RIGHT, xs);
	for (double x: knots) {
	    System.out.format("%g ", x);
	}
	System.out.println("\n");	

	System.out.println("PERIODIC");
	knots = BSpline.createKnots(5, 10, BSpline.Mode.PERIODIC, xs);
	for (double x: knots) {
	    System.out.format("%g ", x);
	}
	System.out.println("\n");	

    }
}