import org.bzdev.geom.*;
import org.bzdev.math.VectorOps;
import org.bzdev.math.RealValuedFunctThreeOps;
import org.bzdev.math.stats.BasicStats;
import org.bzdev.math.stats.BasicStats.Population;

import java.util.HashSet;
import java.util.Arrays;

public class CycloidTestOps {
    public static void main(String argv[]) throws Exception {

	int N = 21;

	Point3D[][] array = new Point3D[N][N];

	double r = 20.0;
	
	for (int i = 1; i < N-1; i++) {
	   for (int j = 1; j < N-1; j++) {

	       double thetaX = 2 * Math.PI * (i - 1) / (N-3);
	       double thetaY = 2 * Math.PI * (j - 1) / (N-3);

	       double x = r * (thetaX - Math.sin(thetaX));
	       double y = r * (thetaY - Math.sin(thetaY));
	       if (Math.abs(x) < 1.e-10) x = 0;
	       if (Math.abs(y) < 1.e-10) y = 0;
	       double z = r * (1 - Math.cos(thetaX)) * (1 - Math.cos(thetaY))
		   + 10.0;
	       array[i][j] = new Point3D.Double(x, y, z);
	   }
	}
	for (int i = 1 ; i < N-1 ; i++) {
	    array[0][i] = new Point3D.Double(array[1][i].getX(),
					     array[1][i].getY(),
					     0.0);
	    array[i][0] = new Point3D.Double(array[i][1].getX(),
					     array[i][1].getY(),
					     0.0);
	    array[N-1][i] = new Point3D.Double(array[N-2][i].getX(),
					     array[N-2][i].getY(),
					     0.0);
	    array[i][N-1] = new Point3D.Double(array[i][N-2].getX(),
					     array[i][N-2].getY(),
					     0.0);
	}
	array[0][0] = array[0][1];
	array[N-1][0] = array[N-1][1];
	array[0][N-1] = array[0][N-2];
	array[N-1][N-1] = array[N-1][N-2];

	BasicSplinePath3D[] upaths = new BasicSplinePath3D[N];
	BasicSplinePath3D[] vpaths = new BasicSplinePath3D[N];
	double[] c;
	for (int i = 1; i < N-1; i++) {
	    Point3D[] tmp = new Point3D[N-3];
	    System.arraycopy(array[i], 2, tmp, 0, N-3);
	    vpaths[i] = new BasicSplinePath3D();
	    c = Path3D.setupCubic(array[i][0], array[i][1]);
	    vpaths[i].moveTo(c[0], c[1], c[2]);
	    vpaths[i].curveTo(c[3], c[4], c[5],
			      c[6], c[7], c[8],
			      c[9], c[10], c[11]);
	    vpaths[i].splineTo(tmp);
	    c = Path3D.setupCubic(array[i][N-2], array[i][N-1]);
	    vpaths[i].curveTo(c[3], c[4], c[5],
			      c[6], c[7], c[8],
			      c[9], c[10], c[11]);
	}
	
	for (int j = 1; j < N-1; j++) {
	    Point3D[] tmp = new Point3D[N-3];
	    for (int i = 2; i < N-1; i++) {
		tmp[i-2] = array[i][j];
	    }
	    upaths[j] = new BasicSplinePath3D();
	    c = Path3D.setupCubic(array[0][j], array[1][j]);
	    upaths[j].moveTo(c[0], c[1], c[2]);
	    upaths[j].curveTo(c[3], c[4], c[5],
			      c[6], c[7], c[8],
			      c[9], c[10], c[11]);
	    upaths[j].splineTo(tmp);
	    c = Path3D.setupCubic(array[N-2][j], array[N-1][j]);
	    upaths[j].curveTo(c[3], c[4], c[5],
			      c[6], c[7], c[8],
			      c[9], c[10], c[11]);
	}

	Transform3D zzeroTransform = new RVFTransform3D((x,y,z) -> x,
							(x,y,z) -> y,
							(x,y,z) -> 0.0);

	double pitcoords[] = new double[9];
	PathIterator3D pit = upaths[1].getPathIterator(zzeroTransform);
	upaths[0] = new BasicSplinePath3D();
	upaths[0].append(pit, false);

	pit = upaths[N-2].getPathIterator(zzeroTransform);
	upaths[N-1] = new BasicSplinePath3D();
	upaths[N-1].append(pit, false);

	pit = vpaths[1].getPathIterator(zzeroTransform);
	vpaths[0] = new BasicSplinePath3D();
	vpaths[0].append(pit, false);

	pit = vpaths[N-2].getPathIterator(zzeroTransform);
	vpaths[N-1] = new BasicSplinePath3D();
	vpaths[N-1].append(pit, false);

	Surface3D surface = new Surface3D.Double();
	double[] coords = new double[48];
	double[] ucoords0 = new double[12];
	double[] ucoords1 = new double[12];
	double[] vcoords0 = new double[12];
	double[] vcoords1 = new double[12];
	for (int i = 1; i < N-2; i++) {
	    for (int j = 1; j < N-2; j++) {
		upaths[j].getSegment(i, ucoords0);
		upaths[j+1].getSegment(i, ucoords1);
		vpaths[i].getSegment(j, vcoords0);
		vpaths[i+1].getSegment(j, vcoords1);
		Surface3D.setupU0ForPatch(vcoords0, coords, false);
		Surface3D.setupU1ForPatch(vcoords1, coords, false);
		Surface3D.setupV0ForPatch(ucoords0, coords, false);
		Surface3D.setupV1ForPatch(ucoords1, coords, false);
		Surface3D.setupRestForPatch(coords);
		if (!Surface3D.checkPatchCorners(ucoords0, ucoords1,
						 vcoords0, vcoords1,
						 System.out)) {
		    System.exit(1);
		};
		surface.addCubicPatch(coords);

		Surface3D.setupU0ForPatch(vcoords0, coords, true);
		Surface3D.setupU1ForPatch(vcoords1, coords, true);
		Surface3D.setupV0ForPatch(ucoords1, coords, false);
		Surface3D.setupV1ForPatch(ucoords0, coords, false);
		Surface3D.setupRestForPatch(coords);
		if (!Surface3D.checkPatchCorners(ucoords0, ucoords1,
						 vcoords0, vcoords1,
						 System.out)) {
		    System.exit(1);
		};
		
		for (int k = 2; k < coords.length; k += 3) {
		    coords[k] = 0.0;
		}
		surface.addCubicPatch(coords);
	    }

	    upaths[0].getSegment(i, ucoords0);
	    upaths[1].getSegment(i, ucoords1);
	    vpaths[i].getSegment(0, vcoords0);
	    vpaths[i+1].getSegment(0,vcoords1);
	    Surface3D.setupU0ForPatch(vcoords0, coords, false);
	    Surface3D.setupU1ForPatch(vcoords1, coords, false);
	    Surface3D.setupV0ForPatch(ucoords0, coords, false);
	    Surface3D.setupV1ForPatch(ucoords1, coords, false);
	    Surface3D.setupRestForPatch(coords);
	    if (!Surface3D.checkPatchCorners(ucoords0, ucoords1,
					     vcoords0, vcoords1,
					     System.out)) {
		System.exit(1);
	    };
	    surface.addCubicPatch(coords);

	    upaths[N-2].getSegment(i, ucoords0);
	    upaths[N-1].getSegment(i, ucoords1);
	    vpaths[i].getSegment(N-2, vcoords0);
	    vpaths[i+1].getSegment(N-2,vcoords1);
	    Surface3D.setupU0ForPatch(vcoords0, coords, false);
	    Surface3D.setupU1ForPatch(vcoords1, coords, false);
	    Surface3D.setupV0ForPatch(ucoords0, coords, false);
	    Surface3D.setupV1ForPatch(ucoords1, coords, false);
	    Surface3D.setupRestForPatch(coords);
	    if (!Surface3D.checkPatchCorners(ucoords0, ucoords1,
					     vcoords0, vcoords1,
					     System.out)) {
		System.exit(1);
	    };
	    surface.addCubicPatch(coords);

	    upaths[i].getSegment(0, ucoords0);
	    upaths[i+1].getSegment(0, ucoords1);
	    vpaths[0].getSegment(i, vcoords0);
	    vpaths[1].getSegment(i, vcoords1);
	    Surface3D.setupU0ForPatch(vcoords0, coords, false);
	    Surface3D.setupU1ForPatch(vcoords1, coords, false);
	    Surface3D.setupV0ForPatch(ucoords0, coords, false);
	    Surface3D.setupV1ForPatch(ucoords1, coords, false);
	    Surface3D.setupRestForPatch(coords);
	    if (!Surface3D.checkPatchCorners(ucoords0, ucoords1,
					     vcoords0, vcoords1,
					     System.out)) {
		System.exit(1);
	    };
	    surface.addCubicPatch(coords);

	    upaths[i].getSegment(N-2, ucoords0);
	    upaths[i+1].getSegment(N-2, ucoords1);
	    vpaths[N-2].getSegment(i, vcoords0);
	    vpaths[N-1].getSegment(i, vcoords1);
	    Surface3D.setupU0ForPatch(vcoords0, coords, false);
	    Surface3D.setupU1ForPatch(vcoords1, coords, false);
	    Surface3D.setupV0ForPatch(ucoords0, coords, false);
	    Surface3D.setupV1ForPatch(ucoords1, coords, false);
	    Surface3D.setupRestForPatch(coords);
	    if (!Surface3D.checkPatchCorners(ucoords0, ucoords1,
					     vcoords0, vcoords1,
					     System.out)) {
		System.exit(1);
	    };
	    surface.addCubicPatch(coords);
	}

	if (!surface.isWellFormed(System.out)) {
	    System.out.println("surface not well formed");
	    System.exit(1);
	}

	Path3D boundary = surface.getBoundary();

	if (boundary.isEmpty()) {
	    System.out.println("boundary is empty as expected");
	} else {
	    System.out.println("boundary:");
	    Path3DInfo.printSegments(boundary);
	}

	System.out.println("now try splitting each patch into subpatches");

	Surface3D surface2 = new Surface3D.Double();

	surface2.append(surface.getSurfaceIterator(null, 2));

	if (!surface2.isWellFormed(System.out)) {
	    System.out.println("surface2 not well formed");
	    System.exit(1);
	}

	boundary = surface2.getBoundary();

	if (boundary.isEmpty()) {
	    System.out.println("boundary for surface2 is empty as expected");
	} else {
	    System.out.println("boundary for surface2:");
	    Path3DInfo.printSegments(boundary);
	    System.out.println("boundary for surface 2 not empty");
	    System.exit(1);
	}

	double v = surface.volume();
	double v2 = surface2.volume();

	System.out.println("number of components for surface  = "
			   + surface.numberOfComponents());
	System.out.println("number of components for surface2 = "
			   + surface2.numberOfComponents());

	System.out.println("Volume for surface is " + v);
	System.out.println("Volume for surface2 is " + v2);

	if (Math.abs((v-v2)/v) > 3.e-7) {
	    System.out.println("v != v2");
	    System.exit(1);
	}

	Rectangle3D bounds = surface.getBounds();
	Rectangle3D bounds2 = surface2.getBounds();
	System.out.println("bounding box for surface: " + bounds);
	System.out.println("bounding box for surface2: " + bounds2);

	if (bounds.equals(bounds2)) {
	    System.out.println("bounding rectangles for surface and surface2"
			       + " are equal");
	} else {
	    System.out.format("diff for min values: (%g, %g, %g)\n",
			      bounds2.getMinX() - bounds.getMinX(),
			      bounds2.getMinY() - bounds.getMinY(),
			      bounds2.getMinZ() - bounds.getMinZ());

	    System.out.format("diff for max values: (%g, %g, %g)\n",
			      bounds2.getMaxX() - bounds.getMaxX(),
			      bounds2.getMaxY() - bounds.getMaxY(),
			      bounds2.getMaxZ() - bounds.getMaxZ());
	    System.exit(1);
	}

	System.exit(0);
    }
}
