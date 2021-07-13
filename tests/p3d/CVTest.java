import org.bzdev.geom.*;
import org.bzdev.graphs.Graph;
import org.bzdev.p3d.*;
import java.awt.geom.Path2D;
import java.io.*;
import java.util.List;


// Test cubic vertex attached to a cubic patch. The surface
// segments are stored in a binary file cvtest.dat and were
// obtained from a much larger test case that had failed.
// After some effort, that test case was reduced to two
// segments.  The segment data is read from a binary file
// because the bug was hard to reproduce: it involved
// floating-point round-off.
//
public class CVTest {

    public static void main(String argv[]) throws Exception {
	
	DataInputStream din = new DataInputStream
	    (new FileInputStream("cvtest.dat"));
	Surface3D surface = new Surface3D.Double();
	Model3D m3d = new Model3D(false);
	double[] coords = new double[48];
	double[] edge1 = new double[12];
	double[] edge2 = new double[12];
	try {
	    for (;;) {
		int type = din.readInt();
		int m = 0;
		switch(type) {
		case SurfaceIterator.CUBIC_PATCH:
		    m = 48;
		    break;
		case SurfaceIterator.CUBIC_TRIANGLE:
		    m = 30;
		    break;
		case SurfaceIterator.CUBIC_VERTEX:
		    m = 15;
		    break;
		case SurfaceIterator.PLANAR_TRIANGLE:
		    m = 9;
		    break;
		}
		for (int i = 0; i < m; i++) {
		    coords[i] = din.readDouble();
		}
		int ii = 0;
		switch(type) {
		case SurfaceIterator.CUBIC_PATCH:
		    surface.addCubicPatch(coords, false);
		    System.out.println("Cubic Patch V0:");
		    /*
		    for (int i = 0; i < 12; i += 3) {
			System.out.format("    (%s, %s, %s)\n",
					  coords[i], coords[i+1], coords[i+2]);
		    }
		    System.out.println("Cubic Patch V1:");
		    for (int i = 36; i < 48; i += 3) {
			System.out.format("    (%s, %s, %s)\n",
					  coords[i], coords[i+1], coords[i+2]);
		    }
		    */
		    System.out.println("Cubic Patch U1:");
		    for (int i = 9; i < 48; i += 12) {
			System.out.format("    (%s, %s, %s)\n",
					  coords[i], coords[i+1], coords[i+2]);
			edge1[ii++] = coords[i];
			edge1[ii++] = coords[i+1];
			edge1[ii++] = coords[i+2];
		    }
		    /*
		    System.out.println("Cubic Patch U0:");
		    for (int i = 0; i < 48; i += 12) {
			System.out.format("    (%s, %s, %s)\n",
					  coords[i], coords[i+1], coords[i+2]);
		    }
		    */
		    break;
		case SurfaceIterator.CUBIC_TRIANGLE:
		    surface.addCubicTriangle(coords, false);
		    break;
		case SurfaceIterator.CUBIC_VERTEX:
		    surface.addCubicVertex(coords, false);
		    System.out.println("CubicVertex Cubic Curve):");
		    System.arraycopy(coords, 0, edge2, 0, 12);
		    for (int i = 0; i < 12; i += 3) {
			System.out.format("    (%s, %s, %s)\n",
					  coords[i], coords[i+1], coords[i+2]);
		    }
		    break;
		case SurfaceIterator.PLANAR_TRIANGLE:
		    m3d.addTriangle(coords[0], coords[1], coords[2],
				    coords[6], coords[7], coords[8],
				    coords[3], coords[4], coords[6]);
		    break;
		}
	    }
	} catch (EOFException edone) {
	    din.close();
	}
	
	m3d.append(surface);

	List<Model3D.Triangle> tlist = m3d.verifyEmbedded2DManifold();
	if (tlist != null && !tlist.isEmpty()) {
	    System.out.println("m3d not embedded");
	}

	double[][] curves1 = SubdivisionIterator.splitCubicBezierCurve(edge1);
	double[][] curves2 = SubdivisionIterator.splitCubicBezierCurve(edge2);
	System.out.println("edge1a:");
	double[] ecoords = curves1[0];
	for (int i = 0; i < 12; i += 3) {
	    System.out.format("    (%s, %s, %s)\n",
			      ecoords[i], ecoords[i+1], ecoords[i+2]);
	}
	System.out.println("edge1b:");
	ecoords = curves1[1];
	for (int i = 0; i < 12; i += 3) {
	    System.out.format("    (%s, %s, %s)\n",
			      ecoords[i], ecoords[i+1], ecoords[i+2]);
	}
	System.out.println("edge2a:");
	ecoords = curves2[0];
	for (int i = 0; i < 12; i += 3) {
	    System.out.format("    (%s, %s, %s)\n",
			      ecoords[i], ecoords[i+1], ecoords[i+2]);
	}
	System.out.println("edge2b:");
	ecoords = curves2[1];
	for (int i = 0; i < 12; i += 3) {
	    System.out.format("    (%s, %s, %s)\n",
			      ecoords[i], ecoords[i+1], ecoords[i+2]);
	}



	m3d.setTessellationLevel(2);
	tlist = m3d.verifyEmbedded2DManifold();
	if (tlist != null && !tlist.isEmpty()) {
	    System.out.println("tessellated m3d not embedded");
	}

    }

}
