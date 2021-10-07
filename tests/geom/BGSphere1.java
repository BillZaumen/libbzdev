import java.io.FileOutputStream;
import org.bzdev.geom.*;
import org.bzdev.p3d.*;


public class BGSphere1 {


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
		double x, y, z;
		if (k == 0) {
		    x = 0.0; y = 0.0; z = r;
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
		}
		if (k == NC) z = 0.0;
		array1[i][j] = new Point3D.Double(x, y, z);
		array2[i][j] = new Point3D.Double(x, y, -z);
	    }
	}

	// Create the sphere
	BezierGrid grid1 = new BezierGrid(array1);
	BezierGrid grid2 = new BezierGrid(array2);

	for (int k = 3; k <= NC; k++) {
	    int start = NC-k;
	    int k2 = 2*k;
	    grid1.startSpline(start, start);
	    grid1.moveU(k2);
	    grid1.moveV(k2);
	    grid1.moveU(-k2);
	    grid1.moveV(-k2);
	    grid1.endSpline(true);

	    grid2.startSpline(start, start);
	    grid2.moveU(k2);
	    grid2.moveV(k2);
	    grid2.moveU(-k2);
	    grid2.moveV(-k2);
	    grid2.endSpline(true);
	}

	grid2.reverseOrientation(true);

	Surface3D surface = new Surface3D.Double();
	surface.append(grid1);
	surface.append(grid2);

	Model3D m3d = new Model3D();
	m3d.append(surface);
	m3d.setTessellationLevel(2);
	if (m3d.notPrintable()) {
	    System.out.println("m3d is not printable");
	    // System.exit(1);
	}

	m3d.createImageSequence(new FileOutputStream("bgsphere1.isq"),
				"png", 8, 6, 0.0, 0.0, 0.0, false);
	
    }
}
