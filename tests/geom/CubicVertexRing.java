import org.bzdev.geom.*;
import org.bzdev.p3d.Model3D;
import java.awt.geom.*;
import java.io.FileOutputStream;


public class CubicVertexRing {

    public static void main(String argv[]) throws Exception
    {
	Surface3D ring = new Surface3D.Double();

	// Series of cubic vertices that 'touch' at a single
	// point, to see if we can compute a boundary for this
	// corner case.  Normally there is no boundary but we
	// can force one by passing 'true' to compute boundary.
	//
	// This test allows us to verify a use of computeBoundary
	// in Model3D.
	double ringdata[][] = {
	    {0.0, 10.0, 0.0,
	     13.0, 15.0, 0.0,
	     17.0, 15.0, 0.0,
	     20.0, 10.0, 0.0,
	     15.0, 0.0, 0.0},

	    {20.0, 10.0, 0.0,
	     23.0, 15.0, 0.0,
	     27.0, 15.0, 0.0,
	     30.0, 10.0, 0.0,
	     25.0, 0.0, 0.0},

	    {30.0, 10.0, 0.0,
	     33.0, 15.0, 0.0,
	     37.0, 15.0, 0.0,
	     40.0, 10.0, 0.0,
	     35.0, 0.0, 0.0},

	    {40.0, 10.0, 0.0,
	     43.0, 15.0, 0.0,
	     47.0, 15.0, 0.0,
	     60.0, 10.0, 0.0,
	     45.0, 0.0, 0.0}
	};
	
	for (double[] coords: ringdata) {
	    ring.addCubicVertex(coords);
	}
    
	ring.computeBoundary(System.out, true);
	Path3D boundary = ring.getBoundary();
	if (boundary == null) {
	    System.out.println("no boundary");
	} else {
	    Path3DInfo.printSegments(boundary);
	}
    }
}
