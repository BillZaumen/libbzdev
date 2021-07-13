import org.bzdev.geom.*;
import org.bzdev.p3d.*;
import java.awt.geom.*;
import java.io.*;

public class CapTest {

    static void ourCP111(double[] tcoords, double r) {
	double[] cp = new double[3];
	for (int i = 0; i < 3; i++) {
	    cp[i] = (tcoords[12+i] + tcoords[18+i])/2.0;
	}
	double len = Math.sqrt(cp[0]*cp[0] + cp[1]*cp[1]);
	double factor = (len < r)? r/len: 1.0;
	for (int i = 0; i < 3; i++) {
	    tcoords[15+i] = cp[i]*factor;
	}

    }

    public static void main(String argv[]) throws Exception {
	int r = 50;
	double height = 5.0;

	Point3D[] points = new Point3D[36];
	for (int i = 0; i < 36; i++) {
	    double theta = Math.toRadians(i*10.0 - 5.0);

	    points[i] = new Point3D.Double(r*Math.cos(theta),
					   r*Math.sin(theta),
					   0.0);
	}

	Path3D boundary = new SplinePath3D(points, true);
	double[] scoords = Path3DInfo.getEntries(boundary).get(0).getCoords();
	double[] bcoords = Path3DInfo.getEntries(boundary).get(1).getCoords();

	for (int i = 0; i < scoords.length; i++) {
	    System.out.format("%g, ", bcoords[i]);
	    if (i %3 == 2) System.out.println();
	}
	for (int i = 0; i < bcoords.length; i++) {
	    System.out.format("%g, ", bcoords[i]);
	    if (i %3 == 2) System.out.println();
	}

	double[] tmp = new double[2];

	Path2DInfo.getLineIntersectionXY( 0.0, height, 1.0, height,
					  r, 0.0, r, 1.0, tmp, 0);

	Path2D arc = new Path2D.Double();
	arc.moveTo(0.0, height);
	arc.curveTo(tmp[0], tmp[1], tmp[0], tmp[1], r, 0.0);
	PathIterator pit = arc.getPathIterator(null);

	double[] acoords = new double[6];
	pit.next();
	pit.currentSegment(acoords);

	double[] tcoords = new double[30];
	double[] pcoords = new double[12];

	// coordinate test.
	Surface3D.setupU0ForTriangle(Path3D.setupCubic(scoords[0], scoords[1],
						       0.0,
						       bcoords[6], bcoords[7],
						       0.0),
				     tcoords, false);
	Surface3D.setupV0ForTriangle(Path3D.setupCubic(0.0, 0.0, 0.0,
						       scoords[0], scoords[1],
						       0.0),
				     tcoords, true);
			   
	Surface3D.setupW0ForTriangle(Path3D.setupCubic(0.0, 0.0, 0.0,
						       bcoords[6], bcoords[7],
						       0.0),
				     tcoords, false);

	Surface3D.setupPlanarCP111ForTriangle(tcoords);

	Point3D baseA =
	    Surface3D.segmentValue(SurfaceIterator.CUBIC_TRIANGLE, tcoords,
				   1.0/3, 0.0, -1.0);
	System.out.format("baseA = (%g, %g, %g)\n",
			   baseA.getX(), baseA.getY(), baseA.getZ());
	Point3D baseB =
	    Surface3D.segmentValue(SurfaceIterator.CUBIC_TRIANGLE, tcoords,
				   -1.0, 2.0/3, 0.0);
	System.out.format("baseB = (%g, %g, %g)\n",
			   baseB.getX(), baseB.getY(), baseB.getZ());

	Point3D base111 =
	    Surface3D.segmentValue(SurfaceIterator.CUBIC_TRIANGLE, tcoords,
				   1.0/3, 1.0/3, 1.0/3);
	System.out.format("base111 = (%g, %g, %g)\n",
			   base111.getX(), base111.getY(), base111.getZ());
				   


	pcoords[0] = scoords[0];
	pcoords[1] = scoords[1];
	pcoords[2] = 0.0;
	System.arraycopy(bcoords, 0, pcoords, 3, 9);
	Surface3D.setupU0ForTriangle(pcoords, tcoords, false);

	pcoords[0] = 0.0;
	pcoords[1] = 0.0;
	pcoords[2] = height;
	pcoords[3] = acoords[0];
	pcoords[4] = 0.0;
	pcoords[5] = acoords[1];
	pcoords[6] = acoords[2];
	pcoords[7] = 0.0;
	pcoords[8] = acoords[3];
	pcoords[9] = scoords[0];
	pcoords[10] = scoords[1];
	pcoords[11] = 0.0;
	Surface3D.setupV0ForTriangle(pcoords, tcoords, true);

	pcoords[4] = -pcoords[4];
	pcoords[7] = -pcoords[7];
	pcoords[10] = -pcoords[10];
	Surface3D.setupW0ForTriangle(pcoords, tcoords, false);
	// Surface3D.setupCP111ForTriangle(tcoords);
	ourCP111(tcoords, r);
	System.out.format("CP111=(%g, %g, %g)\n",
			  tcoords[15], tcoords[16], tcoords[17]);

	Surface3D surface = new Surface3D.Double();
	surface.addFlippedCubicTriangle(tcoords);
	Model3D m3d = new Model3D();
	m3d.append(surface);
	m3d.setTessellationLevel(6);
	m3d.createImageSequence(new FileOutputStream("captest.isq"),
				    "png", 8, 6, 0.0, 0.0, 0.0, false);

    }

}
