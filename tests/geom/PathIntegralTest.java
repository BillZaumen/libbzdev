import java.awt.geom.*;
import org.bzdev.geom.*;

public class PathIntegralTest {

    public static void main(String argv[]) throws Exception {

	Path2D path1 = Paths2D.createArc(500.0, 200.0, 500.0, 100.0, 2*Math.PI);
	
	Path2D path2 =
	    Paths2D.createArc(500.0, 200.0, 500.0, 100.0, -2*Math.PI);

	


	PathIntegral pint1 = new PathIntegral(1,
					      (x,y) -> { return -y;},
					      (x,y) -> { return x;});

	System.out.println("half of line integral for path1: "
			   + (pint1.integrate(path1)/2));
	    
	System.out.println("half of line integral for path2:"
			   + (pint1.integrate(path2)/2));

	System.out.println("area enclosed by path1:"
			   + Path2DInfo.areaOf(path1));
	System.out.println("area enclosed by path2:"
			   + Path2DInfo.areaOf(path2));

	System.out.println("half of line integral for path1 using a PI: "
			   + (pint1.integrate(path1.getPathIterator(null))/2));

	System.out.println("half of line integral for path2 using a PI: "
			   + (pint1.integrate(path2.getPathIterator(null))/2));


	PathIntegral pint2 = new PathIntegral(2, (x, y) -> {
		double xp = x - 500.0;
		double yp = y - 200.0;
		return xp*xp + yp*yp;
	});

	System.out.println("pint2: integral for path1 divided by 10000 = "
			   + (pint2.integrate(path1)/10000));
	System.out.println("pint2: integral for path2 divided by 10000  = "
			   + (pint2.integrate(path2)/10000));
	System.out.println("circumference: "
			   + Path2DInfo.circumferenceOf(path1));
	System.out.println("ideal circumference: " + (2*Math.PI*100.0));

	// Use a vector field corresponding to a uniform magnetic
	// field along the Z axis plus the magnetic field one would
	// get with a current running along the Z axis.  In this case,
	// the path integral of the magnetic field around any closed path
	// such that a surface whose boundary is that path goes through
	// the Z axix will produce the same value for the path integral.

	PathIntegral pint3 =
	    new PathIntegral(5, (x,y,z) -> {
		    double r = Math.sqrt(x*x + y*y);
		    return -(10/r) * y/r;
	    }, (x,y,z) -> {
		    double r = Math.sqrt(x*x + y*y);
		    return (10/r) * x/r;
	    }, (x,y,z) -> {
		    return 20.0;    
	    });

	Path3D path3 = new SplinePath3D((u) -> {
		return 100*Math.cos(u);},
	    (u)  -> {
		return 100*Math.sin(u);},
	    (u) -> {return 0.0;},
	    0.0, 2*Math.PI, 36, true);

	Path3D path4 = new SplinePath3D((u) -> {
		return 10*Math.cos(u);},
	    (u)  -> {
		return 10*Math.sin(u);},
	    (u) -> {return 10.0*Math.sin(u)*Math.cos(u);},
	    0.0, 2*Math.PI, 36, true);

	System.out.println("pint3.integrate(path3) = "
			   + pint3.integrate(path3));
	System.out.println("pint3.integrate(path4) = "
			   + pint3.integrate(path4));
    }
}
