import org.bzdev.geom.*;
import org.bzdev.math.*;

public class  MomentTest {
    public static void main(String argv[]) throws Exception {
	int N = 41;
	int NC = N/2;
	
	Point3D[][] array1 = new Point3D[N][N];
	Point3D[][] array2 = new Point3D[N][N];

	double r = 100.0;

	for (int i = 0; i < N; i++) {
	    for (int j = 0; j < N; j++) {
		int k = Math.max(Math.abs(i-NC),Math.abs(j-NC));
		double theta = k*(Math.PI/(N-1));
		double x, y, z, z1, z2;
		if (k == 0) {
		    x = 0.0; y = 0.0; z = r;
		    z1 = z;
		    z2 = z;
		} else {
		    int nanglesHalf = k*4;
		    double delta = Math.PI/(nanglesHalf);
		    double angle;
		    if (i == NC+k) {
			angle = -(NC-j)*delta;
		    } else if (j == NC-k) {
			angle = -(NC + 2*k - i)*delta;
		    } else if (i == NC-k) {
			angle = -((j-NC) + 4*k)*delta;
		    } else if (j == NC+k) {
			angle = (NC+2*k-i)*delta;
		    } else {
			throw new Error();
		    }
		    x = r * Math.cos(angle) * Math.sin(theta);
		    y = r * Math.sin(angle) * Math.sin(theta);
		    z = r * Math.cos(theta);
		    z1 = z;
		    z2 = z;
		    if (k < NC) {
			x += 0.1*y*Math.sin(2*theta);
			z1 += 0.1*y*z;
			z2 -= 0.1*y*z;
		    }
		}
		if (k == NC) {
		    z = 0.0;
		    z1 = 0.0;
		    z2 = 0.0;
		}
		array1[i][j] = new Point3D.Double(x, y, z1);
		array2[i][j] = new Point3D.Double(x, y, -z2);
	    }
	}

	BezierGrid grid1 = new BezierGrid(array1);
	BezierGrid grid2 = new BezierGrid(array2);
	grid2.reverseOrientation(true);

	Surface3D surface = new Surface3D.Double();
	surface.append(grid1);
	surface.append(grid2);

	double vol = surface.volume();
	Point3D cm = SurfaceOps.centerOfMassOf(surface, vol);
	double[][] moments = SurfaceOps.momentsOf(surface, cm, vol);
	System.out.format("cm = (%g, %g, %g)\n",
			  cm.getX(), cm.getY(), cm.getZ());
	System.out.println("moments:");
	for (int i = 0; i < 3; i++) {
	    System.out.print(" | ");
	    for (int j = 0; j < 3; j++) {
		System.out.format("%11g ", moments[i][j]);
	    }
	    System.out.println(" |");
	}

	double[][] I = SurfaceOps.toMomentsOfInertia(moments);
	System.out.println("I:");
	for (int i = 0; i < 3; i++) {
	    System.out.print(" | ");
	    for (int j = 0; j < 3; j++) {
		System.out.format("%11g ", I[i][j]);
	    }
	    System.out.println(" |");
	}

	double[] pI = SurfaceOps.principalMoments(I);
	double[][] pa = SurfaceOps.principalAxes(I);
	LUDecomp lud = new LUDecomp(pa);
	if (lud.det() < 0) {
	    System.out.println("bad det for pa");
	}

	System.out.print("pI =");
	for (int i = 0; i < 3; i++) {
	    System.out.format(" %g", pI[i]);
	}
	System.out.println();
	System.out.println("pa:");
	for (int i = 0; i < 3; i++) {
	    System.out.format("Axis %d: (%g, %g, %g)\n", i,
			      pa[i][0], pa[i][1], pa[i][2]);
	}

	AffineTransform3D at = SurfaceOps.principalAxesTransform(pa, cm,
								 false);

	Surface3D surface2 = new Surface3D.Double(surface, at);

	double vol2 = surface.volume();
	System.out.println("vol = " + vol + ", vol2 = " + vol2);
	Point3D cm2 = SurfaceOps.centerOfMassOf(surface2, vol2);
	double[][] moments2 = SurfaceOps.momentsOf(surface2, cm2, vol2);
	System.out.format("cm2 = (%g, %g, %g)\n",
			  cm2.getX(), cm2.getY(), cm2.getZ());
	System.out.println("moments2:");
	for (int i = 0; i < 3; i++) {
	    System.out.print(" | ");
	    for (int j = 0; j < 3; j++) {
		System.out.format("%11g ", moments2[i][j]);
	    }
	    System.out.println(" |");
	}
	double[][] I2 = SurfaceOps.toMomentsOfInertia(moments2);
	System.out.println("I2:");
	for (int i = 0; i < 3; i++) {
	    System.out.print(" | ");
	    for (int j = 0; j < 3; j++) {
		System.out.format("%11g ", I2[i][j]);
	    }
	    System.out.println(" |");
	}

	double[] pI2 = SurfaceOps.principalMoments(I2);
	double[][] pa2 = SurfaceOps.principalAxes(I2);

	lud = new LUDecomp(pa2);
	if (lud.det() < 0) {
	    System.out.println("bad det for pa2");
	}

	System.out.print("pI2 =");
	for (int i = 0; i < 3; i++) {
	    System.out.format(" %g", pI2[i]);
	}
	System.out.println();
	System.out.println("pa2:");
	for (int i = 0; i < 3; i++) {
	    System.out.format("Axis %d: (%g, %g, %g)\n", i,
			      pa2[i][0], pa2[i][1], pa2[i][2]);
	}

    }
}
