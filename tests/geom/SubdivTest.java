import org.bzdev.geom.*;
import java.awt.geom.*;
import org.bzdev.lang.MathOps;
import org.bzdev.math.LUDecomp;
import org.bzdev.math.MatrixOps;
import org.bzdev.math.rv.DoubleRandomVariable;
import org.bzdev.math.rv.UniformDoubleRV;
import org.bzdev.p3d.Model3D;
import java.io.*;
import java.util.TreeSet;

public class SubdivTest {
    // See https://en.wikipedia.org/wiki/B%C3%A9zier_triangle for the
    // matrix.
    //   P000  P012  P021  P030  P102  P111  P120  P201  P210  P300
    //    0     3     6     9     12    15    18    21    24    27 (offset)
    //   a3    a2b   ab2   b3    a2g   abg   b2g   ag2   bg2   g3
    // | 1     0     0     0     0     0     0     0     0     0    |
    // | 0.5   0.5   0     0     0     0     0     0     0     0    |
    // | 0.25  0.5   0.25  0     0     0     0     0     0     0    |
    // | 0.125 0.375 0.375 0.125 0     0     0     0     0     0    |
    // | 0     0     0     0     1     0     0     0     0     0    |
    // | 0     0     0     0     0.5   0.5   0     0     0     0    |*  P111'
    // | 0     0     0     0     0.25  0.5   0.25  0     0     0    |*  P120'
    // | 0     0     0     0     0     0     0     1     0     0    |
    // | 0     0     0     0     0     0     0     0.5  0.5    0    |*  P210'
    // | 0     0     0     0     0     0     0     0     0     1    |

    //  b2g' = 0.25 (a2g) + 0.5(abg) + 0.25(b2g)
    //  bg2' = 0.5 (ag2) + 0.5 (bg2)
    //  abg' = 0.5 (a2g) + 0.5 (abg)

    // P120' = 0.25 P102 + 0.5 P111 + 0.25 P120
    // P210' = 0.5 P201 + 0.5 P210
    // P111' = 0.5 P102 + 0.5 P111

    // By symmetry, for the upper half (not shown for the matrix,
    // abg' should be 0.5(b2g) + 0.5 (abg) = 0.5 P120 + 0.5 P111
    // We can directly partition the Bezier curves on the edges.
    // The symmetry argument is that we can change (u, v, w)
    // to (w, u, v) and similarly reorder the control points
    // so that Pijk is mapped to Pkij.  With this new ordering,
    // the partioning produces the upper half of the partition.
    // Also, this new ordering does not change the control point
    // P111, which is mapped to itself: the (u, v, w) ordering is
    // P000, P012, P021, P030, P102, P111, P120, P201, P210, P300
    // and the (w, u, v) ordering is
    // P030, P021, P012, P003, P120, P111, P102, P210, P201, P300.

    /*
    static double[][] splitCubicTriangle(double[] coords) {
	// orient the results so that splitting the split triangles
	// will split the edges of the original that had not yet
	// been split.
	double[][] results = new double[2][];
	results[0] = new double[30];
	results[1] = new double[30];

	double[] cp120 = new double[3];
	double[] cp210 = new double[3];
	double[] cp111L = new double[3];
	double[] cp111U = new double[3];

	double[][] splitEdges = SubdivisionIterator
	    .splitCubicBezierCurve(coords);
	for (int i = 0; i < 3; i++) {
	    cp120[i] = 0.25*coords[12+i] + 0.5*coords[15+i] + 0.25*coords[18+i];
	    cp210[i] = 0.5*coords[21+i] + 0.5*coords[24+i];
	    cp111L[i] = 0.5*coords[12+i] + 0.5*coords[15+i];
	    cp111U[i] = 0.5*coords[18+i] + 0.5*coords[15+i];
	}

	System.arraycopy(coords, 27, results[0], 0, 3);
	System.arraycopy(coords, 21, results[0], 3, 3);
	System.arraycopy(coords, 12, results[0], 6, 3);
	System.arraycopy(coords, 0, results[0], 9, 3);
	System.arraycopy(cp210, 0, results[0], 12, 3);
	System.arraycopy(cp111L, 0, results[0], 15, 3);
	System.arraycopy(splitEdges[0], 3, results[0], 18, 3);
	System.arraycopy(cp120, 0, results[0], 21, 3);
	System.arraycopy(splitEdges[0], 6, results[0], 24, 3);
	System.arraycopy(splitEdges[0], 9, results[0], 27, 3);

	System.arraycopy(coords, 9, results[1], 0, 3);
	System.arraycopy(coords, 18, results[1], 3, 3);
	System.arraycopy(coords, 24, results[1], 6, 3);
	System.arraycopy(coords, 27, results[1], 9, 3);
	System.arraycopy(splitEdges[1], 6, results[1], 12, 3);
	System.arraycopy(cp111U, 0, results[1], 15, 3);
	System.arraycopy(cp210, 0, results[1], 18, 3);
	System.arraycopy(splitEdges[1], 3, results[1], 21, 3);
	System.arraycopy(cp120, 0, results[1], 24, 3);
	System.arraycopy(splitEdges[1], 0, results[1], 27, 3);

	return results;
    }

    public static void splitCubicTriangle(double[] coords, int offset,
					  double[] coords1, int offset1,
					  double[] coords2, int offset2)
    {
	// orient the results so that splitting the split triangles
	// will split the edges of the original that had not yet
	// been split.
	double[][] results = new double[2][];
	boolean copy1 = (coords != coords1)
	    || (Math.abs(offset1 - offset) < 30);
	boolean copy2 = (coords != coords2)
	    || (Math.abs(offset2 - offset) < 30);

	results[0] = copy1? new double[30]: coords1;
	results[1] = copy2? new double[30]: coords2;
	int off1 = copy1? 0: offset1;
	int off2 = copy2? 0: offset2;
	double[] cp120 = new double[3];
	double[] cp210 = new double[3];
	double[] cp111L = new double[3];
	double[] cp111U = new double[3];

	double[][] splitEdges = SubdivisionIterator
	    .splitCubicBezierCurve(coords);
	for (int i = 0; i < 3; i++) {
	    cp120[i] = 0.25*coords[offset+12+i]
		+ 0.5*coords[offset+15+i] + 0.25*coords[offset+18+i];
	    cp210[i] = 0.5*coords[offset+21+i] + 0.5*coords[offset+24+i];
	    cp111L[i] = 0.5*coords[offset+12+i] + 0.5*coords[offset+15+i];
	    cp111U[i] = 0.5*coords[offset+18+i] + 0.5*coords[offset+15+i];
	}

	System.arraycopy(coords, offset+27, results[0], off1+0, 3);
	System.arraycopy(coords, offset+21, results[0], off1+3, 3);
	System.arraycopy(coords, offset+12, results[0], off1+6, 3);
	System.arraycopy(coords, offset+0, results[0], off1+9, 3);
	System.arraycopy(cp210, 0, results[0], off1+12, 3);
	System.arraycopy(cp111L, 0, results[0], off1+15, 3);
	System.arraycopy(splitEdges[0], 3, results[0], off1+18, 3);
	System.arraycopy(cp120, 0, results[0], off1+21, 3);
	System.arraycopy(splitEdges[0], 6, results[0], off1+24, 3);
	System.arraycopy(splitEdges[0], 9, results[0], off1+27, 3);

	System.arraycopy(coords, offset+9, results[1], off2+0, 3);
	System.arraycopy(coords, offset+18, results[1], off2+3, 3);
	System.arraycopy(coords, offset+24, results[1], off2+6, 3);
	System.arraycopy(coords, offset+27, results[1], off2+9, 3);
	System.arraycopy(splitEdges[1], 6, results[1], off2+12, 3);
	System.arraycopy(cp111U, 0, results[1], off2+15, 3);
	System.arraycopy(cp210, 0, results[1], off2+18, 3);
	System.arraycopy(splitEdges[1], 3, results[1], off2+21, 3);
	System.arraycopy(cp120, 0, results[1], 24, 3);
	System.arraycopy(splitEdges[1], 0, results[1], off2+27, 3);

	if (copy1) {
	    System.arraycopy(results[0], off1, coords1, offset1, 30);
	}
	if (copy2) {
	    System.arraycopy(results[1], off2, coords2, offset2, 30);
	}
    }
    */

    static void splitTest() {
	double[] tcoords = new double[30];
	double[] pcoords;

	pcoords = Path3D.setupCubic(10.0, 10.0, 0.0,
				     40.0, 10.0, 0.0);
	Surface3D.setupV0ForTriangle(pcoords, tcoords, false);

	pcoords = Path3D.setupCubic(10.0, 10.0, 0.0,
				     10.0, 40.0, 0.0);

	Surface3D.setupU0ForTriangle(pcoords, tcoords, false);

	pcoords = Path3D.setupCubic(40.0, 10.0, 0.0,
				   10.0, 40.0, 0.0);

	Surface3D.setupW0ForTriangle(pcoords, tcoords, false);
	Surface3D.setupCP111ForTriangle(tcoords);

	double[] scoords  = new double[30];
	System.arraycopy(tcoords, 0, scoords, 0, 30);
	for (int i = 0; i < 30; i += 3) {
	    scoords[i] += 50.0;
	}
	double[] workspace = new double[30*4];
	System.arraycopy(tcoords, 0, workspace, 0, 30);
	SubdivisionIterator.splitCubicTriangle
	    (workspace, 0, workspace, 0, workspace, 30);
	SubdivisionIterator.splitCubicTriangle
	    (workspace, 0, workspace, 60, workspace, 90);
	SubdivisionIterator.splitCubicTriangle
	    (workspace, 30, workspace, 0, workspace, 30);

	Surface3D surface = new Surface3D.Double();

	System.arraycopy(workspace, 0, tcoords, 0, 30);
	surface.addCubicTriangle(tcoords);
	System.arraycopy(workspace, 30, tcoords, 0, 30);
	surface.addCubicTriangle(tcoords);
	System.arraycopy(workspace, 60, tcoords, 0, 30);
	surface.addCubicTriangle(tcoords);
	System.arraycopy(workspace, 90, tcoords, 0, 30);
	surface.addCubicTriangle(tcoords);
	surface.addCubicTriangle(scoords);

	pcoords = Path3D.setupCubic(0.0,  50.0, 0.0, 50.0, 50.0, 0.0);
	Surface3D.setupV0ForTriangle(pcoords, tcoords, false);
	pcoords = Path3D.setupCubic(0.0, 50.0, 0.0, 25.0, 50 + 25.0, 0.0);
	Surface3D.setupU0ForTriangle(pcoords, tcoords, false);
	pcoords = Path3D.setupCubic(50.0, 50.0, 0.0, 25.0, 50 + 25.0, 0.0);
	Surface3D.setupW0ForTriangle(pcoords, tcoords, false);
	Surface3D.setupCP111ForTriangle(tcoords);

	SubdivisionIterator.permuteCubicTriangle(tcoords, 0);
	System.arraycopy(tcoords, 0, workspace, 0, 30);
	SubdivisionIterator.splitCubicTriangle
	    (workspace, 0, workspace, 0, workspace, 30);
	SubdivisionIterator.splitCubicTriangle
	    (workspace, 0, workspace, 60, workspace, 90);
	SubdivisionIterator.splitCubicTriangle
	    (workspace, 30, workspace, 0, workspace, 30);
	System.arraycopy(workspace, 0, tcoords, 0, 30);
	surface.addCubicTriangle(tcoords);
	System.arraycopy(workspace, 30, tcoords, 0, 30);
	surface.addCubicTriangle(tcoords);
	System.arraycopy(workspace, 60, tcoords, 0, 30);
	surface.addCubicTriangle(tcoords);
	System.arraycopy(workspace, 90, tcoords, 0, 30);
	surface.addCubicTriangle(tcoords);

	pcoords = Path3D.setupCubic(0.0, 100.0, 0.0, 50.0, 100.0, 0.0);
	Surface3D.setupV0ForTriangle(pcoords, tcoords, false);
	pcoords = Path3D.setupCubic(0.0, 100.0, 0.0, 0.0, 125.0, 0.0);
	Surface3D.setupU0ForTriangle(pcoords, tcoords, false);
	pcoords = Path3D.setupCubic(50.0, 100.0, 0.0, 0.0, 125.0, 0.0);
	Surface3D.setupW0ForTriangle(pcoords, tcoords, false);
	Surface3D.setupCP111ForTriangle(tcoords);

	SubdivisionIterator.permuteCubicTriangle(tcoords, 0);
	System.arraycopy(tcoords, 0, workspace, 0, 30);
	SubdivisionIterator.splitCubicTriangle
	    (workspace, 0, workspace, 0, workspace, 30);
	SubdivisionIterator.splitCubicTriangle
	    (workspace, 0, workspace, 60, workspace, 90);
	SubdivisionIterator.splitCubicTriangle
	    (workspace, 30, workspace, 0, workspace, 30);
	System.arraycopy(workspace, 0, tcoords, 0, 30);
	surface.addCubicTriangle(tcoords);
	System.arraycopy(workspace, 30, tcoords, 0, 30);
	surface.addCubicTriangle(tcoords);
	System.arraycopy(workspace, 60, tcoords, 0, 30);
	surface.addCubicTriangle(tcoords);
	System.arraycopy(workspace, 90, tcoords, 0, 30);
	surface.addCubicTriangle(tcoords);

	/*
	double[][] split = splitCubicTriangle(tcoords);
	double[][] split2 = splitCubicTriangle(split[0]);
	surface.addCubicTriangle(split2[0]);
	surface.addCubicTriangle(split2[1]);
	split2 = splitCubicTriangle(split[1]);
	surface.addCubicTriangle(split2[0]);
	surface.addCubicTriangle(split2[1]);
	*/

	Model3D m3d = new Model3D();
	m3d.append(surface);
	System.out.println("m3d.size() = " + m3d.size());
	try {
	    m3d.createImageSequence(new FileOutputStream("ctsplit.isq"),
				    "png",
				    8, 6, 0.0, 0.0, 0.0, true);
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
    }


    static void midcoordTest() throws Exception {

	/*
	DoubleRandomVariable rv = new UniformDoubleRV(-100.0, 100.0);
	for (int i = 0; i < (1<<20); i++) {
	    double x1 = rv.next();
	    double x2 = rv.next();
	    double mid1 = SubdivisionIterator.midcoord(x1, x2);
	    double mid2 = SubdivisionIterator.midcoord(x2, x1);
	    if (mid1 != mid2) {
		System.out.println("mid1 = " + mid1 + ", mid2 = " + mid2
				   + ", diff = " + (mid2 - mid1));
	    }
	}
	*/

	double[] edgeV0 = Path3D.setupCubic(1.0, 1.0, 0.0, 18.0, 0.0, 0.0);
	double[] edgeU0 = Path3D.setupCubic(1.0, 1.0, 0.0, 1.0, 18.0, 0.0);
	double[] edgeV1 = Path3D.setupCubic(1.0, 18.0, 0.0, 18.0, 18.0, 0.0);
	double[] edgeU1 = Path3D.setupCubic(18.0, 1.0, 0.0, 18.0, 18.0, 0.0);

	double[] cpcoords = new double[48];

	Surface3D.setupV0ForPatch(edgeV0, cpcoords, false);
	Surface3D.setupV1ForPatch(edgeV1, cpcoords, false);
	Surface3D.setupU0ForPatch(edgeU0, cpcoords, false);
	Surface3D.setupU1ForPatch(edgeU1, cpcoords, false);
	Surface3D.setupRestForPatch(cpcoords);

	Surface3D surface = new Surface3D.Double();
	surface.addCubicPatch(cpcoords);
	SurfaceIterator si = surface.getSurfaceIterator(null, 1);
	double[] coords = new double[48];
	double mid = SubdivisionIterator.midcoord((float)1.0, (float)18.0);
	int indices[] = {0, 9, 36, 45};
	while (!si.isDone()) {
	    si.currentSegment(coords);
	    for (int i: indices) {
		double value = coords[i];
		if (value == 1.0) continue;
		if (value == 18.0) continue;
		if (Math.abs(value - mid) <= Math.ulp((float)mid)) continue;
		String msg = value + " != 1.0, 18.0, or "
		    + mid;
		throw new Exception(msg);
	    }
	    si.next();
	}
    }


    static void setupRcoords(double[] rcoords, double[] tmp) {
	double[] tmp1 = new double[48];
	double[] tmp2 = new double[48];
	    System.arraycopy(tmp, 0, rcoords, 0, 3);
	    System.arraycopy(tmp, 3, rcoords, 12, 3);
	    System.arraycopy(tmp, 6, rcoords, 24, 3);
	    System.arraycopy(tmp, 9, rcoords, 36, 3);
	    System.arraycopy(tmp, 12, rcoords, 3, 3);
	    System.arraycopy(tmp, 21, rcoords, 6, 3);
	    System.arraycopy(tmp, 27, rcoords, 9, 3);
	    System.arraycopy(tmp, 24, rcoords, 42, 3);
	    System.arraycopy(tmp, 18, rcoords, 39, 3);

	    System.arraycopy(tmp, 27, rcoords,  21, 3);
	    System.arraycopy(tmp, 27, rcoords,  33, 3);
	    System.arraycopy(tmp, 27, rcoords,  45, 3);

	    System.arraycopy(tmp, 21, tmp1, 0, 3);
	    System.arraycopy(tmp, 24, tmp1, 3, 3);
	    Path3DInfo.elevateDegree(1, tmp2, 0, tmp1, 0);
	    Path3DInfo.elevateDegree(2, tmp1, 0, tmp2, 0);
	    System.arraycopy(tmp1, 3, rcoords, 18, 3);
	    System.arraycopy(tmp1, 6, rcoords, 30, 3);
	    System.arraycopy(tmp, 12, tmp1, 0, 3);
	    System.arraycopy(tmp, 15, tmp1, 3, 3);
	    System.arraycopy(tmp, 18, tmp1, 3, 3);
	    Path3DInfo.elevateDegree(2, tmp2, 0, tmp1, 0);
	    System.arraycopy(tmp2, 3, rcoords, 15, 3);
	    System.arraycopy(tmp2, 6, rcoords, 27, 3);
    }

    public static void main(String argv[]) throws Exception {

	Surface3D surface1 = new Surface3D.Double();

	surface1.addPlanarTriangle(0.0, 0.0, 0.0,
				   2.0, 0.0, 0.0,
				   1.0, 2.0, 0.0,
				   "surface1");

	double[] ptcoords = new double[9];
	SurfaceIterator si = surface1.getSurfaceIterator(null);
	System.out.println(si.currentTag() + ":");
	si.currentSegment(ptcoords);
	System.out.println("planar triangle control points:");

	for (int i = 0; i < 9; i += 3) {
	    System.out.format("    %g %g %g\n",
			      ptcoords[i], ptcoords[i+1], ptcoords[i+2]);
	}
	si = surface1.getSurfaceIterator(null);

	SubdivisionIterator sdi = new SubdivisionIterator(si, 1);
	double[] ptc = new double[9];
	int index = 0;
	System.out.println(sdi.currentTag() + ", subtriangle 0:");
	sdi.currentSegment(ptc);
	for (int i = 0; i < 9; i += 3) {
	    System.out.format("    %g %g %g\n", ptc[i], ptc[i+1], ptc[i+2]);
	}
	System.out.println(sdi.currentTag() + ", subtriangle 1:");
	index++;
	sdi.next();
	sdi.currentSegment(ptc);
	for (int i = 0; i < 9; i += 3) {
	    System.out.format("    %g %g %g\n", ptc[i], ptc[i+1], ptc[i+2]);
	}
	System.out.println(sdi.currentTag() + ", subtriangle 2:");
	index++;
	sdi.next();
	sdi.currentSegment(ptc);
	for (int i = 0; i < 9; i += 3) {
	    System.out.format("    %g %g %g\n", ptc[i], ptc[i+1], ptc[i+2]);
	}
	System.out.println(sdi.currentTag() + ", subtriangle 3:");
	index++;
	sdi.next();
	sdi.currentSegment(ptc);
	for (int i = 0; i < 9; i += 3) {
	    System.out.format("    %g %g %g\n", ptc[i], ptc[i+1], ptc[i+2]);
	}

	index++;
	sdi.next();
	ptc = new double[48];
	if (!sdi.isDone()) {
	    System.out.println("sdi should have terminated its iteration");
	    while (!sdi.isDone()) {
		for (int i = 0; i < 9; i += 3) {
		    ptc[i] = Double.NaN;
		}
		int type = sdi.currentSegment(ptc);
		System.out.println("Entry " + index
				   + " ( type = " + type + ")");
		for (int i = 0; i < 9; i += 3) {
		    System.out.format("    %g %g %g\n",
				      ptc[i], ptc[i+1], ptc[i+2]);
		}
		index++;
		sdi.next();
	    }
	    System.exit(1);
	}

	System.out.println("------------------");
	
	double[] pathcoords = new double[12];
 
	double[] edge1 = Path3D.setupCubic(0.0, 0.0, 0.0, 6.0, 0.0, 0.0);
	double[] edge2 = Path3D.setupCubic(6.0, 0.0, 0.0, 3.0, 6.0, 0.0);
	double[] edge3 = Path3D.setupCubic(0.0, 0.0, 0.0, 3.0, 6.0, 0.0);

	double[] tcoords = new double[48];

	Surface3D.setupV0ForTriangle(edge1, tcoords, false);
	Surface3D.setupW0ForTriangle(edge2, tcoords,false);
	Surface3D.setupU0ForTriangle(edge3, tcoords,false);
	Surface3D.setupPlanarCP111ForTriangle(tcoords);

	Surface3D surface2 = new Surface3D.Double();

	surface2.addCubicTriangle(tcoords, "surface2");

	si = surface2.getSurfaceIterator(null);
	System.out.println(si.currentTag() + ", cubic triangle:");

	double[] tcoords1 = new double[30];
	double[] tcoords2 = new double[30];
	double[] tcoordsA = new double[30];
	double[] tcoordsB = new double[30];
	double[] tcoordsC = new double[30];
	double[] tcoordsD = new double[30];
	SubdivisionIterator.splitCubicTriangle(tcoords, 0, tcoords1, 0,
					       tcoords2, 0);

	SubdivisionIterator.splitCubicTriangle(tcoords1, 0, tcoordsA, 0,
					       tcoordsB, 0);

	SubdivisionIterator.splitCubicTriangle(tcoords2, 0, tcoordsC, 0,
					       tcoordsD, 0);

	double[] tworkspace = new double[60];
	System.arraycopy(tcoords2, 0, tworkspace, 30, 30);
	SubdivisionIterator.splitCubicTriangle(tworkspace, 30,
					       tworkspace, 0,
					       tworkspace, 30);

	for (int i = 0; i < 30; i++) {
	    if (tcoordsD[i] != tworkspace[30+i]) {
		System.out.format("disagree at i = %d, %g != %g\n", i,
				  tcoordsD[i], tworkspace[30+i]);
		System.exit(1);
	    }
	}


	double meanx = 0.0, meany = 0.0, meanz = 0.0;
	for (int i = 0; i < 30; i += 3) {
	    meanx += tcoords[i];
	    meany += tcoords[i+1];
	    meanz += tcoords[i+2];
	}
	meanx /= 10;
	meany /= 10;
	meanz /= 10;
	System.out.format("tcoords P111: (%g, %g, %g) <--> (%g, %g, %g)\n",
			  meanx, meany, meanz,
			  tcoords[15], tcoords[16], tcoords[17]);

	meanx = 0.0; meany = 0.0; meanz = 0.0;
	for (int i = 0; i < 30; i += 3) {
	    meanx += tcoordsA[i];
	    meany += tcoordsA[i+1];
	    meanz += tcoordsA[i+2];
	}
	meanx /= 10;
	meany /= 10;
	meanz /= 10;
	System.out.format("tcoordsA P111: (%g, %g, %g) <--> (%g, %g, %g)\n",
			  meanx, meany, meanz,
			  tcoordsA[15], tcoordsA[16], tcoordsA[17]);

	meanx = 0.0; meany = 0.0; meanz = 0.0;
	for (int i = 0; i < 30; i += 3) {
	    meanx += tcoordsB[i];
	    meany += tcoordsB[i+1];
	    meanz += tcoordsB[i+2];
	}
	meanx /= 10;
	meany /= 10;
	meanz /= 10;
	System.out.format("tcoordsB P111: (%g, %g, %g) <--> (%g, %g, %g)\n",
			  meanx, meany, meanz,
			  tcoordsB[15], tcoordsB[16], tcoordsB[17]);

	meanx = 0.0; meany = 0.0; meanz = 0.0;
	for (int i = 0; i < 30; i += 3) {
	    meanx += tcoordsC[i];
	    meany += tcoordsC[i+1];
	    meanz += tcoordsC[i+2];
	}
	meanx /= 10;
	meany /= 10;
	meanz /= 10;
	System.out.format("tcoordsC P111: (%g, %g, %g) <--> (%g, %g, %g)\n",
			  meanx, meany, meanz,
			  tcoordsC[15], tcoordsC[16], tcoordsC[17]);

	meanx = 0.0; meany = 0.0; meanz = 0.0;
	for (int i = 0; i < 30; i += 3) {
	    meanx += tcoordsD[i];
	    meany += tcoordsD[i+1];
	    meanz += tcoordsD[i+2];
	}
	meanx /= 10;
	meany /= 10;
	meanz /= 10;
	System.out.format("tcoordsD P111: (%g, %g, %g) <--> (%g, %g, %g)\n",
			  meanx, meany, meanz,
			  tcoordsD[15], tcoordsD[16], tcoordsD[17]);

	System.out.println("--------");
	int e1inds[] = {0, 3, 6, 9};
	int e2inds[] = {0, 12, 21, 27};
	int e3inds[] = {9, 18, 24, 27};
	int einds[][] = {e1inds, e2inds, e3inds};

	System.out.println("tcoords:");
	for (int i = 0; i < 30; i += 3) {
	    System.out.format("    %g %g %g\n",
			      tcoords[i], tcoords[i+1], tcoords[i+2]);
	}
	System.out.println("tcoords1:");
	for (int i = 0; i < 30; i += 3) {
	    System.out.format("    %g %g %g\n",
			      tcoords1[i], tcoords1[i+1], tcoords1[i+2]);
	}
	System.out.println("tcoordsA:");
	for (int i = 0; i < 30; i += 3) {
	    System.out.format("    %g %g %g\n",
			      tcoordsA[i], tcoordsA[i+1], tcoordsA[i+2]);
	}
	System.out.println("tcoordsB:");
	for (int i = 0; i < 30; i += 3) {
	    System.out.format("    %g %g %g\n",
			      tcoordsB[i], tcoordsB[i+1], tcoordsB[i+2]);
	}

	System.out.println("tcoords2:");
	for (int i = 0; i < 30; i += 3) {
	    System.out.format("    %g %g %g\n",
			      tcoords2[i], tcoords2[i+1], tcoords2[i+2]);
	}
	System.out.println("tcoordsC:");
	for (int i = 0; i < 30; i += 3) {
	    System.out.format("    %g %g %g\n",
			      tcoordsC[i], tcoordsC[i+1], tcoordsC[i+2]);
	}
	System.out.println("tcoordsD:");
	for (int i = 0; i < 30; i += 3) {
	    System.out.format("    %g %g %g\n",
			      tcoordsD[i], tcoordsD[i+1], tcoordsD[i+2]);
	}

	double[][] arrays = {tcoordsA, tcoordsB, tcoordsC, tcoordsD};

	for (double[] array: arrays) {
	    System.out.println("... checking array");
	    for (int[] eind: einds) {
		int count = 0;
		double lastx = 0.0, lasty = 0.0, lastz = 0.0;
		double diffx = 0.0, diffy = 0.0, diffz = 0.0;
		for (int i: eind) {
		    switch(count++) {
		    case 0:
			break;
		    case 1:
			diffx = array[i] - lastx;
			diffy = array[i+1] - lasty;
			diffz = array[i+2] - lastz;
			break;
		    default:
			if (Math.abs((array[i] - lastx) - diffx) > 1.e-10
			    || Math.abs((array[i+1] - lasty) - diffy) > 1.e-10
			    || Math.abs((array[i+2] - lastz) - diffz) > 1.e-10)
			    {
				System.out.format("ind %d, (%g, %g, %g): "
						  + "last = (%g, %g, %g), "
						  + "expecting diffs "
						  + "(%g, %g, %g)\n",
						  i, array[i], array[i+1],
						  array[i+1], lastx, lasty,
						  lastz, diffx, diffy, diffz);
				throw new Exception();
			    }
			break;
		    }
		    lastx = array[i];
		    lasty = array[i+1];
		    lastz = array[i+2];
		}
	    }
	}
	System.out.println("--------");

	double[] ctcoords = new double[30];
	double[] ctc = new double[30];

	si.currentSegment(ctcoords);
	System.out.println("ctcoords:");
	for (int i = 0; i < 30; i += 3) {
	    System.out.format("    %g %g %g\n",
			      ctcoords[i], ctcoords[i+1], ctcoords[i+2]);
	}

	// sdi.checkP(ctcoords);
	// sdi.setupP(ctcoords);
	// sdi.printP();

	sdi = new SubdivisionIterator(si, 1);

	System.out.println(sdi.currentTag() + ", (cubic) subtriangle 0:");
	sdi.currentSegment(ctc);
	for (int i = 0; i < 30; i += 3) {
	    System.out.format("    %g %g %g\n",
			      ctc[i], ctc[i+1], ctc[i+2]);
	}
	for (int i = 0; i < 30; i++) {
	    if (Math.abs(ctc[i] - tcoordsB[i]) > 1.e-5) {
		throw new Exception("bad subtriangle 0");
	    }
	}
	sdi.next();

	System.out.println(sdi.currentTag() + ", (cubic) subtriangle 1:");
	sdi.currentSegment(ctc);
	for (int i = 0; i < 30; i += 3) {
	    System.out.format("    %g %g %g\n",
			      ctc[i], ctc[i+1], ctc[i+2]);
	}
	for (int i = 0; i < 30; i++) {
	    if (Math.abs(ctc[i] - tcoordsA[i]) > 1.e-5) {
		throw new Exception("bad subtriangle 1");
	    }
	}
	sdi.next();

	System.out.println(sdi.currentTag() + ", (cubic) subtriangle 2:");
	sdi.currentSegment(ctc);
	for (int i = 0; i < 30; i += 3) {
	    System.out.format("    %g %g %g\n",
			      ctc[i], ctc[i+1], ctc[i+2]);
	}
	for (int i = 0; i < 30; i++) {
	    if (Math.abs(ctc[i] - tcoordsD[i]) > 1.e-5) {
		throw new Exception("bad subtriangle 2 (i = " + i + ")");
	    }
	}
	sdi.next();

	System.out.println(sdi.currentTag() + ", (cubic) subtriangle 3:");
	sdi.currentSegment(ctc);
	for (int i = 0; i < 30; i += 3) {
	    System.out.format("    %g %g %g\n",
			      ctc[i], ctc[i+1], ctc[i+2]);
	}
	for (int i = 0; i < 30; i++) {
	    if (Math.abs(ctc[i] - tcoordsC[i]) > 1.e-5) {
		throw new Exception("bad subtriangle 3 (i = " + i + ")");
	    }
	}
	sdi.next();


	if (!sdi.isDone()) {
	    System.out.println("sdi should have terminated its iteration");
	}
	

	System.out.println("Bezier patch test");

	Surface3D surface3 = new Surface3D.Double();

	double[] edgeV0 = Path3D.setupCubic(0.0, 0.0, 0.0, 18.0, 0.0, 0.0);
	double[] edgeU0 = Path3D.setupCubic(0.0, 0.0, 0.0, 0.0, 18.0, 0.0);
	double[] edgeV1 = Path3D.setupCubic(0.0, 18.0, 0.0, 18.0, 18.0, 0.0);
	double[] edgeU1 = Path3D.setupCubic(18.0, 0.0, 0.0, 18.0, 18.0, 0.0);
	
	double[] cpcoords = new double[48];

	Surface3D.setupV0ForPatch(edgeV0, cpcoords, false);
	Surface3D.setupV1ForPatch(edgeV1, cpcoords, false);
	Surface3D.setupU0ForPatch(edgeU0, cpcoords, false);
	Surface3D.setupU1ForPatch(edgeU1, cpcoords, false);
	Surface3D.setupRestForPatch(cpcoords);

	Surface3D surface0 = new Surface3D.Double();
	surface0.addCubicPatch(cpcoords);

	System.out.println("... simple iterator test with one object");
	SurfaceIterator sit0 = surface0.getSurfaceIterator(null);
	System.out.println("....... no partitioning");
	double[] coords = new double[48];
	while (!sit0.isDone()) {
	    switch (sit0.currentSegment(coords)) {
	    case SurfaceIterator.CUBIC_PATCH:
		System.out.println("cubic patch:");
		for (int i = 0; i < 4; i++) {
		    for (int j = 0; j < 12; j += 3) {
			int offset = i*12 + j;
			System.out.format(" (%g,%g,%g)",
					  coords[offset], coords[offset+1],
					  coords[offset+2]);
		    }
		    System.out.println();
		}
		break;
	    default:
		System.out.println("unexpected surface-sgement type");
		System.exit(1);
	    }
	    sit0.next();
	}
	System.out.println("....... level 1  partitioning");
	sit0 = surface0.getSurfaceIterator(null, 1);
	while (!sit0.isDone()) {
	    switch (sit0.currentSegment(coords)) {
	    case SurfaceIterator.CUBIC_PATCH:
		System.out.println("cubic patch:");
		for (int i = 0; i < 4; i++) {
		    for (int j = 0; j < 12; j += 3) {
			int offset = i*12 + j;
			System.out.format(" (%g,%g,%g)",
					  coords[offset], coords[offset+1],
					  coords[offset+2]);
		    }
		    System.out.println();
		}
		break;
	    default:
		System.out.println("unexpected surface-sgement type");
		System.exit(1);
	    }
	    sit0.next();
	}
	System.out.println("....... level 2  partitioning");
	sit0 = surface0.getSurfaceIterator(null, 2);
	while (!sit0.isDone()) {
	    switch (sit0.currentSegment(coords)) {
	    case SurfaceIterator.CUBIC_PATCH:
		System.out.println("cubic patch:");
		for (int i = 0; i < 4; i++) {
		    for (int j = 0; j < 12; j += 3) {
			int offset = i*12 + j;
			System.out.format(" (%g,%g,%g)",
					  coords[offset], coords[offset+1],
					  coords[offset+2]);
		    }
		    System.out.println();
		}
		break;
	    default:
		System.out.println("unexpected surface-sgement type");
		System.exit(1);
	    }
	    sit0.next();
	}
	System.out.println("..............");

	double[] patchL = new double[48];
	double[] patchR = new double[48];
	SubdivisionIterator.getLeftPatch(patchL, 0, cpcoords, 0);
	SubdivisionIterator.getRightPatch(patchR, 0, cpcoords, 0);

	double[] patchB = new double[48];
	double[] patchT = new double[48];
	SubdivisionIterator.getBottomPatch(patchB, 0, cpcoords, 0);
	SubdivisionIterator.getTopPatch(patchT, 0, cpcoords, 0);
	surface3.addCubicPatch(cpcoords, "surface3");
	System.out.println("area of surface3 = " + surface3.area());

	Surface3D surface3lr = new Surface3D.Double();
	surface3lr.addCubicPatch(patchL);
	surface3lr.addCubicPatch(patchR);
	System.out.println("area of surface3lr = " + surface3lr.area());

	if (Math.abs(surface3.area() - surface3lr.area()) > 1.e-10) {
	    throw new Exception("areas differ");
	}

	Surface3D surface3tb = new Surface3D.Double();
	surface3tb.addCubicPatch(patchB);
	surface3tb.addCubicPatch(patchT);
	System.out.println("area of surface3tb = " + surface3tb.area());
	if (Math.abs(surface3.area() - surface3tb.area()) > 1.e-10) {
	    throw new Exception("areas differ");
	}

	si = surface3.getSurfaceIterator(null);

	/*
	Surface3D  split3 = new Surface3D.Double();

	split3.append(surface3.getSurfaceIterator(null, 1));

	if (!split3.isWellFormed(System.out)) {
	    System.out.println("... not well formed");
	    System.exit(1);
	}
	*/

	sdi = new SubdivisionIterator(si, 1);

	double[] cpc = new double[48];
	System.out.println(si.currentTag() + ", patch:");
	for (int i = 0; i < 48; i += 3)  {
	    System.out.format("    %g, %g, %g\n",
			      cpcoords[i], cpcoords[i+1], cpcoords[i+2]);
	}

	sdi.currentSegment(cpc);
	System.out.println(sdi.currentTag() + ", (LL) patch 0:");
	for (int i = 0; i < 48; i += 3) {
	    System.out.format("    %g, %g, %g\n", cpc[i], cpc[i+1], cpc[i+2]);
	}

	sdi.next();
	sdi.currentSegment(cpc);

	System.out.println(sdi.currentTag() + ", (RL) patch 1:");
	for (int i = 0; i < 48; i += 3) {
	    System.out.format("    %g, %g, %g\n", cpc[i], cpc[i+1], cpc[i+2]);
	}

	sdi.next();
	sdi.currentSegment(cpc);
	System.out.println(sdi.currentTag() + ", (LR) patch 2:");
	for (int i = 0; i < 48; i += 3) {
	    System.out.format("    %g, %g, %g\n", cpc[i], cpc[i+1], cpc[i+2]);
	}

	sdi.next();
	sdi.currentSegment(cpc);
	System.out.println(sdi.currentTag() + ", (RR) patch 3:");
	for (int i = 0; i < 48; i += 3) {
	    System.out.format("    %g, %g, %g\n", cpc[i], cpc[i+1], cpc[i+2]);
	}

	sdi.next();
	if (!sdi.isDone()) {
	    System.out.println("sdi should have terminated its iteration");
	}

	System.out.println("Matrix test");
	double[][] SL = {{1.0, 0.0, 0.0, 0.0},
			 {0.5, 0.5, 0.0, 0.0},
			 {0.25, 0.5, 0.25, 0.0},
			 {0.125, 0.375, 0.375, 0.125}};
	double[][] CP = {{0.0, 0.0, 0.0, 0.0},
			 {6.0, 6.0, 6.0, 6.0},
			 {12.0, 12.0, 12.0, 12.0},
			 {18.0, 18.0, 18.0, 18.0}};
	double[][] SLT = MatrixOps.transpose(SL);
	double[][] T1 = new double[4][4];
	double[][] T2 = new double[4][4];
	MatrixOps.multiply(T1, CP, SLT);
	MatrixOps.multiply(T2, SL, T1);
	for (int i = 0; i < 4; i++) {
	    System.out.format("%g %g %g %g\n",
			      T2[i][0], T2[i][1], T2[i][2], T2[i][3]);
	}


	Surface3D.setupV0ForTriangle(edge1, tcoords, false);
	Surface3D.setupW0ForTriangle(edge2, tcoords,false);
	Surface3D.setupU0ForTriangle(edge3, tcoords,false);
	Surface3D.setupPlanarCP111ForTriangle(tcoords);
	setupRcoords(cpcoords, tcoords);
	for (int i = 0; i < 48; i += 3) {
	    System.out.format("(%g, %g, %g) ", cpcoords[i],
		cpcoords[i+1], cpcoords[i+2]);
	    if (i%12 == 9) System.out.println();
	}

	midcoordTest();

	splitTest();

	double lower = Math.sqrt(2);
	double upper = Math.sqrt(5);
	edgeV0 = Path3D.setupCubic(lower, lower, 0.0, upper, 0.0, 0.0);
	edgeU0 = Path3D.setupCubic(lower, lower, 0.0, lower, upper, 0.0);
	edgeV1 = Path3D.setupCubic(lower, upper, 0.0, upper, upper, 0.0);
	edgeU1 = Path3D.setupCubic(upper, lower, 0.0, upper, upper, 0.0);

	cpcoords = new double[48];

	Surface3D.setupV0ForPatch(edgeV0, cpcoords, false);
	Surface3D.setupV1ForPatch(edgeV1, cpcoords, false);
	Surface3D.setupU0ForPatch(edgeU0, cpcoords, false);
	Surface3D.setupU1ForPatch(edgeU1, cpcoords, false);
	Surface3D.setupRestForPatch(cpcoords);

	Surface3D surface = new Surface3D.Double();
	surface.addCubicPatch(cpcoords);

	double[] cpcoordsR = new double[48];
	for (int i = 0; i < 3; i++) {
	    MatrixOps.reflect(cpcoordsR, 0, cpcoords, 0, 4, 4,
			      true, i, 3, i, 3);
	}
	Surface3D surfaceR = new Surface3D.Double();
	surfaceR.addCubicPatch(cpcoordsR);

	// shift so we don't overlap with the patch
	upper += 10.0;
	lower += 10.0;
	double middle = (upper+lower)/2.0 + Math.sqrt(7)/100;

	edge1 = Path3D.setupCubic(lower, lower, lower, upper, lower, lower);
	edge2 = Path3D.setupCubic(upper, lower, lower, middle, upper, lower);
	edge3 = Path3D.setupCubic(lower, lower, lower, middle, upper, lower);

	tcoords = new double[48];

	Surface3D.setupV0ForTriangle(edge1, tcoords, false);
	Surface3D.setupW0ForTriangle(edge2, tcoords,false);
	Surface3D.setupU0ForTriangle(edge3, tcoords,false);
	Surface3D.setupPlanarCP111ForTriangle(tcoords);

	surface.addCubicTriangle(tcoords, "surface2");

	edge1 = Path3D.setupCubic(upper, lower, lower, lower, lower, lower);
	edge2 = Path3D.setupCubic(lower, lower, lower, middle, upper, lower);
	edge3 = Path3D.setupCubic(upper, lower, lower, middle, upper, lower);
	Surface3D.setupV0ForTriangle(edge1, tcoords, false);
	Surface3D.setupW0ForTriangle(edge2, tcoords,false);
	Surface3D.setupU0ForTriangle(edge3, tcoords,false);
	Surface3D.setupPlanarCP111ForTriangle(tcoords);

	surfaceR.addCubicTriangle(tcoords, "surface2");

	upper += 10.0;
	lower += 10.0;
	middle += 10.0;

	surface.addPlanarTriangle(lower, lower, lower,
				  upper, upper, upper,
				  middle, middle, middle);

	surfaceR.addPlanarTriangle(lower, lower, lower,
				   middle, middle, middle,
				   upper,upper,upper);

	upper += 10.0;
	lower += 10.0;
	middle += 10.0;

	double[] pcoords =
	    Path3D.setupCubic(lower, lower, lower, upper, upper, upper);
	System.arraycopy(pcoords, 0, tcoords, 0, 12);
	tcoords[12] = middle;
	tcoords[13] = middle;
	tcoords[14] = middle;
	surface.addCubicVertex(tcoords);
	pcoords = Path3D.setupCubic(upper, upper, upper, lower, lower, lower);
	System.arraycopy(pcoords, 0, tcoords, 0, 12);
	surfaceR.addCubicVertex(tcoords);

	TreeSet<Point3D> points = new TreeSet<Point3D>((e1, e2) -> {
		Point3D p1 = (e1 instanceof Point3D)? (Point3D) e1: null;
		Point3D p2 = (e2 instanceof Point3D)? (Point3D) e2: null;
		if (p1.getX() < p2.getX()) return -1;
		else if (p1.getX() > p2.getX()) return 1;
		if (p1.getY() < p2.getY()) return -1;
		else if (p1.getY() > p2.getY()) return 1;
		if (p1.getZ() < p2.getZ()) return -1;
		else if (p1.getZ() > p2.getZ()) return 1;
		return 0;
	});

	int level = 10; // try a high level in an attempt to break things.
	System.out.println("subdivide to level 10 (increases the number of "
			   + "segments by 2^20)");
	si = surface.getSurfaceIterator(null, level);
	while (!si.isDone()) {
	    int n = 0;
	    switch(si.currentSegment(cpcoords)) {
	    case SurfaceIterator.CUBIC_PATCH:
		n = 48;
		break;
	    case SurfaceIterator.CUBIC_TRIANGLE:
		n = 30;
		break;
	    case SurfaceIterator.CUBIC_VERTEX:
		n = 15;
		break;
	    case SurfaceIterator.PLANAR_TRIANGLE:
		n = 9;
		break;
	    }
	    for (int i = 0; i < n; i += 3) {
		Point3D p = new Point3D.Double(cpcoords[i],
					       cpcoords[i+1],
					       cpcoords[i+2]);
		points.add(p);
	    }
	    si.next();
	}

	si = surface.getSurfaceIterator(null, level);
	long maxid = -1;
	while (!si.isDone()) {
	    maxid = ((SubdivisionIterator)si).currentSourceID();
	    int n = 0;
	    switch (si.currentSegment(cpcoordsR)) {
	    case SurfaceIterator.CUBIC_PATCH:
		n = 48;
		break;
	    case SurfaceIterator.CUBIC_TRIANGLE:
		n = 30;
		break;
	    case SurfaceIterator.CUBIC_VERTEX:
		n = 15;
		break;
	    case SurfaceIterator.PLANAR_TRIANGLE:
		n = 9;
		break;
	    }
	    for (int i = 0; i < n; i += 3) {
		Point3D p = new Point3D.Double(cpcoordsR[i],
					       cpcoordsR[i+1],
					       cpcoordsR[i+2]);
		points.add(p);
	    }
	    si.next();
	}
	if (maxid != surface.size() - 1) {
	    throw new
		Exception("maxid = " + maxid + ", size = "
			   + surface.size());
	}
	double limit = 0.1/MathOps.pow(2.0, level);
	Point3D last = null;
	for (Point3D p: points) {
	    if (last == null) {
		last = p;
	    } else {
		if (last.distance(p) < limit) {
		    throw new Exception("last.distance(p) = "
					+  last.distance(p));
		}
	    }
	}

	System.exit(0);
   }
}
