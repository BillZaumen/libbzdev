import java.io.FileOutputStream;
import org.bzdev.geom.*;
import org.bzdev.math.VectorOps;
import org.bzdev.math.stats.BasicStats;
import org.bzdev.math.stats.BasicStats.Population;
import org.bzdev.p3d.Model3D;


public class ExtensionTest2 {
    public static void main(String argv[]) throws Exception {

	boolean makeImages = (argv.length > 0 && argv[0].equals("--isq"));
	boolean split  = (argv.length > 0 && argv[0].equals("--split"));

	int N = 4;
	int M = 2;

	Point3D[][] array = {
	    { new Point3D.Double(10.0, 0.0, 10.0),
	      new Point3D.Double(5.0, 0.0, 10.0)},
	    { new Point3D.Double(0.0, 10.0, 10.0),
	      new Point3D.Double(0.0, 5.0, 10.0)},
	    { new Point3D.Double(-10.0, 0.0, 10.0),
	      new Point3D.Double(-5.0, 0.0, 10.0)},
	    { new Point3D.Double(0.0, -10.0, 10.0),
	      new Point3D.Double(0.0, -5.0, 10.0)}
	};


	BezierGrid grid1 = new BezierGrid(array, true, false);

	Surface3D surface = new Surface3D.Double();
	grid1.reverseOrientation(true);
	surface.append(grid1);
	BezierCap topCap = new BezierCap(grid1.getBoundary(0,0),
					 3.0, true);
	
	surface.append(topCap);

	System.out.println("creating extension ...");

	int nbase = 4;
	double rbase = 1.0;
	double zbase = 0.0;

	BezierGrid extension =
	    grid1.createExtensionGrid((index, point, type, ends) -> {
		    double tt = (1.0*index)/(nbase-1);
		    double t = tt*tt;
		    double z = point.getZ()*(1-t) - zbase*tt;
		    double startR = Math.sqrt(point.getX()*point.getX()
					      + point.getY()*point.getY());
		    double rfactor = startR*(1-t) + rbase*t;
		    return new Point3D.Double(rfactor*point.getX()/startR,
					      rfactor*point.getY()/startR,
					      z);
		}, null, nbase, 0, 1);


	System.out.println("... extension created");
	System.out.format("extension V axis: %d, closed = %b\n",
			  extension.getVArrayLength(), extension.isVClosed());
	System.out.format("extension U axis: %d, closed = %b\n",
			  extension.getUArrayLength(), extension.isUClosed());
	extension.print();
	
	if (extension.badSplines(System.out)) {
	    System.out.println("splines were bad");
	}
	    
	if (!extension.isWellFormed(System.out)) {
	    System.out.println("extension not well formed");
	    System.exit(1);
	}

	surface.append(extension);
	if (!surface.isWellFormed(System.out)) {
	    System.out.println("surface not well formed");
	    System.exit(1);
	}


	Path3D boundary = surface.getBoundary();
	Path3DInfo.printSegments(boundary);

	/*
	Point3D center =  BezierCap.findCenter(boundary);
	double[] vcoords = BezierCap.findVector(boundary, center);
	System.out.format("center: (%g, %g, %g), vector: (%g, %g, %g)\n",
			  center.getX(), center.getY(), center.getZ(),
			  vcoords[0], vcoords[1], vcoords[2]);
	Rectangle3D sbox = surface.getBounds();
	System.out.println("sbox.getMaxX() = " + sbox.getMaxX());
	*/

	BezierCap cap = new BezierCap(boundary, 0.0, true);

	surface.append(cap);

	boundary = surface.getBoundary();

	if (boundary == null) {
	    System.out.println("no boundary because not well formed");
	    // System.exit(1);
	} else if (boundary.isEmpty()) {
	    System.out.println("boundary is empty as expected");
	} else {
	    System.out.println("boundary:");
	    Path3DInfo.printSegments(boundary);
	    // System.exit(1);
	}

	if (surface.numberOfComponents() != 1) {
	    System.out.println("number of components for surface  = "
			       + surface.numberOfComponents());
	    // System.exit(1);
	}

	Model3D m3d = new Model3D();
	m3d.append(surface);
	m3d.setTessellationLevel(5);
	if (m3d.notPrintable()) {
	    System.out.println("m3d is not printable");
	    // System.exit(1);
	}

	if (makeImages) {
	    m3d.createImageSequence(new FileOutputStream("extension2.isq"),
				    "png", 8, 6, 0.0, 0.0, 0.0, false);
	}
	System.exit(0);
   }
}
