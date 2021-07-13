import org.bzdev.geom.*;
import org.bzdev.p3d.*;
import java.io.FileOutputStream;

public class DielectricSphere {
    public static void main(String argv[]) throws Exception {

	int N = 41;
	int M = 61;

	int NC = N/2;
	int MC = M/2;

	double deltaX = 10.0;
	double deltaY = 10.0;
	double radius = 100.0;

	double E0 = -1.0;
	double epsilon = 2.0;

	double radius3 = radius*radius*radius;

	Point3D[][] array1 = new Point3D[N][M];
	Point3D[][] array2 = new Point3D[N][M];

	double offset = 300.0;

	for (int i = 0; i < N; i++) {
	    double x = (i-NC)*deltaX;
	    for (int j = 0; j < M; j++) {
		double y = (j - MC)*deltaY;
		double r2 = x*x + y*y;
		double r = Math.sqrt(r2);
		double z;
		if (Math.abs(r) < 1.e-10) {
		    z = 0.0;
		} else {
		    double cosTheta = x/Math.sqrt(x*x+y*y);
		    if (cosTheta > 1.0) cosTheta = 1.0;
		    if (cosTheta < -1.0) cosTheta = -1.0;
		    if (r <= radius) {
			z = -r*((3.0)/(epsilon+2))*E0*cosTheta;
		    } else {
			z = -E0*r*cosTheta
			    + ((epsilon-1)/(epsilon+2))
			    * E0*(radius3/r2)*cosTheta;
		    }
		}
		z += offset;
		array1[i][j] = new Point3D.Double(x, y, z);
		array2[i][j] = new Point3D.Double(x, y, z-5.0);
	    }
	}

	BezierGrid grid1 = new BezierGrid(array1);
	BezierGrid grid2 = new BezierGrid(array2);

	for (int j = 0; j < M; j++) {
	    double y = (j - MC)*deltaY;
	    int si = 0;
	    int si2 = 0;
	    boolean case1Seen = false;
	    boolean case2Seen = false;
	    for (int i = 0; i < N; i++) {
		double x = (i-NC)*deltaX;
		double r2 = x*x + y*y;
		double r = Math.sqrt(r2);
		if (r < radius) {
		    if (case1Seen == false) {
			if (i >= 2) {
			    grid1.startSpline(0,j);
			    grid1.moveU(i);
			    grid2.startSpline(0,j);
			    grid2.moveU(i);
			}
			si2 = i;
			case1Seen = true;
		    }
		} else if (case1Seen == true && case2Seen == false) {
		    // System.out.println("started case2");
		    if (i - si2 > 1) {
			grid1.startSpline(si2, j);
			grid1.moveU(i-si2);
			grid1.endSpline(false);
			grid2.startSpline(si2, j);
			grid2.moveU(i-si2);
			grid2.endSpline(false);
			grid1.startSpline(i,j);
			if (N-i-1 > 1) {
			    grid1.moveU(N-i-1);
			    grid1.endSpline(false);
			    grid2.startSpline(i,j);
			    grid2.moveU(N-i-1);
			    grid2.endSpline(false);
			}
			// System.out.println("ended case2");
		    }
		    case2Seen = true;
		}
	    }
	}

	if(grid1.badSplines(System.out)) {
	    System.exit(1);
	}

	if(grid2.badSplines(System.out)) {
	    System.exit(1);
	}

	grid2.reverseOrientation(true);

	Model3D m3d = new Model3D();
	m3d.setTessellationLevel(2);
	
	System.out.println("creating images");
	m3d.append(grid1);
	m3d.createImageSequence(new FileOutputStream("dsphere.isq"), "png",
				8, 6,
				0.0, 0.5, 0.5, false);
    }
}
