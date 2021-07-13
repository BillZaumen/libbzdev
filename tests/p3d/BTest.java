import org.bzdev.p3d.*;
import org.bzdev.geom.SurfaceIterator;
// import org.bzdev.io.FISOutputStream;
import java.io.*;
import java.nio.*;
import java.nio.channels.FileChannel;
import java.awt.*;
import java.awt.image.*;
import javax.imageio.*;
import java.io.*;

public class BTest {

    final static int WIDTH = 1024;
    final static int HEIGHT = 900;

    static void createFile() {
	File f = new File("BTest.stl");
	Model3D m3d = new Model3D();

	m3d.addTriangle(0.0, 0.0, 0.0,
			100.0, 100.0, 0.0,
			100.0,  0.0,  0.0, Color.BLACK);
	m3d.addTriangle(0.0, 0.0, 0.0,
			0.0, 100.0, 0.0,
			100.0, 100.0, 0.0, Color.BLACK);

	m3d.addTriangle(0.0, 0.0, 0.0,
			100.0, 0.0, 0.0,
			50.0, 50.0, 100.0, Color.RED);
	m3d.addTriangle(100.0, 0.0, 0.0,
			100.0, 100.0, 0.0,
			50.0, 50.0, 100.0, Color.BLUE);
			
	m3d.addTriangle(100.0, 100.0, 0.0,
			0.0, 100.0, 0.0,
			50.0, 50.0, 100.0, Color.GREEN);

	m3d.addTriangle(0.0, 100.0, 0.0,
			0.0, 0.0, 0.0,
			50.0, 50.0, 100.0, Color.WHITE);

	java.util.List<Model3D.Edge> elist =  m3d.verifyClosed2DManifold();
	if (elist != null) {
	    System.out.println("bad edges");
	    for (Model3D.Edge edge: elist) {
		System.out.println("(" +edge.getX1()
					   + ", " +edge.getY1()
					   + ", " + edge.getZ1()
					   + ") <---> (" + edge.getX2()
					   + ", " +edge.getY2()
					   + ", " + edge.getZ2()
					   + ((edge.getTag() == null)? ")":
					      ") " + edge.getTag().
					      toString()));
	    }
	    System.exit(1);
	}
	java.util.List<Model3D.Triangle> tlist =
	    m3d.verifyEmbedded2DManifold();
	if (tlist != null) {
	    java.util.Iterator<Model3D.Triangle> it = tlist.iterator();
	    while (it.hasNext()) {
		Model3D.Triangle t1 = it.next();
		Model3D.Triangle t2 = it.next();
		System.out.println(t1.toString() + " intersects "
				   + t2.toString());
	    }
	    System.exit(1);
	}

	System.out.println("Surface area = " + m3d.area());
	System.out.println("Volume = " + m3d.volume());

	System.out.println("size = " + m3d.size());
	SurfaceIterator si = m3d.getSurfaceIterator(null);
	int sicount = 0;
	double[] coords = new double[9];
	System.out.println("triangles (a coord can be 0, 100, 50):");
	while (!si.isDone()) {
	    sicount++;
	    si.currentSegment(coords);
	    System.out.format("(%g,%g,%g)->(%g,%g,%g)->(%g,%g,%g)\n",
			      coords[0], coords[1], coords[2],
			      coords[6], coords[7], coords[8],
			      coords[3], coords[4], coords[5]);
	    si.next();
	}
	System.out.println("surface iterator suggests size is " + sicount);

	try {
	    m3d.writeSTL("Test STL File", "BTest.stl");
	    m3d.writeX3D("Test X3D File", "a pentahedron", null,
			 new FileOutputStream("BTest.x3d"), false);
	    /*
	    if (FISOutputStream.isSupported()) {
		m3d.writeX3D("Test X3D File", "a pentahedron", null,
			     "BTest.x3db");
	    }
	    */
	} catch (IOException e) {
	    e.printStackTrace();
	    System.exit(1);
	}
    }



    public static void main(String argv[]) {
	try {
	    createFile();

	    InputStream is = new FileInputStream("BTest.stl");

	    BinarySTLParser parser = new BinarySTLParser(is);

	    Model3D m3d = new Model3D();
	    Model3D.Image image = new 
		Model3D.Image (WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
	    Graphics2D g2d = image.createGraphics();
	    g2d.setColor(Color.BLUE);
	    g2d.fillRect(0,0, WIDTH, HEIGHT);
	    g2d.setColor(Color.black);
	    g2d.setRenderingHint
		(RenderingHints.KEY_INTERPOLATION,
		 RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	    parser.addToModel(m3d);
	    /*
	    double scalex = (WIDTH - 1) / m3d.getMaxX();
	    double scaley = (HEIGHT - 1) / m3d.getMaxY();
	    double scale = (scalex > scaley)? scaley: scalex;
	    image.setScaleFactor(scale);
	    image.setOrigin(0);
	    */

	    image.setCoordRotation(-Math.PI/6.0, Math.PI/4.0, 0.0);
	    image.setColorFactor(0.0);

	    if (argv.length > 1 && argv[1].equals("norotate")) {
		// nothing to do.
		image.setColorFactor(8.0);
	    }
	    if (argv.length > 1 && argv[1].equals("rotate")) {
		image.setCoordRotation(-Math.PI/4.0, Math.PI/4.0, 0.0);
		image.setColorFactor(0.3);
	    }
	    if (argv.length > 1 && argv[1].equals("rotate90")) {
		image.setCoordRotation(0.0, Math.PI/2.0, 0.0);
		image.setColorFactor(1.2);
	    }
	    if (argv.length > 1 && argv[1].equals("rotate45,90")) {
		image.setCoordRotation(Math.PI/4.0, Math.PI/2.0, 0.0);
		image.setDelta(4.0);
	        image.setColorFactor(0.5);
	    }
	    if (argv.length > 1 && argv[1].equals("rotate90,90")) {
		image.setCoordRotation(Math.PI/2.0, Math.PI/2.0, 0.0);
		image.setColorFactor(0.8);
	    }
	    if (argv.length > 1 && argv[1].equals("rotate135,90")) {
		image.setCoordRotation(Math.PI*3.0/4.0, Math.PI/2.0, 0.0);
		image.setDelta(4.0);
	        image.setColorFactor(0.8);
	    }
	    if (argv.length > 1 && argv[1].equals("rotate160,90")) {
		image.setCoordRotation(Math.PI*160.0/180.0, Math.PI/2.0, 0.0);
		image.setDelta(4.0);
	        image.setColorFactor(0.4);
	    }
	    if (argv.length > 1 && argv[1].equals("rotate180,90")) {
		image.setCoordRotation(Math.PI, Math.PI/2.0, 0.0);
		image.setColorFactor(0.4);
	    }
	    if (argv.length > 3) {
		double magnification = Double.parseDouble(argv[3]);
		double xfract = 0.0;
		double yfract = 0.0;
		if (argv.length > 4) {
		    xfract = Double.parseDouble(argv[4]);
		} if (argv.length > 5) {
		    yfract = Double.parseDouble(argv[argv.length-1]);
		}
		m3d.setImageParameters(image, magnification, xfract, yfract);
		image.setDelta(4.0);
	    } else {
		m3d.setImageParameters(image);
	    }
	    // m3d.setImageOrigin(200, HEIGHT - 200);
	    // m3d.setScaleFactor(3.0);
	    // m3d.setImageOrigin(-800, HEIGHT + 200);
	    // m3d.setScaleFactor(50.0);
	    // m3d.setScaleFactor(100.0);
	    // m3d.setRotationOrigin(20.0, 20.0, 10.0);
	    // m3d.setCoordRotation(0.0, Math.PI/2 - Math.PI/7.5, 0.0);
	    // m3d.setCoordRotation(0.0, -Math.PI/6, -Math.PI/8.0);
	    // m3d.setTranslation(-1.0, -1.5);
	    // m3d.setEdgeColor(Color.green);
	    // m3d.setLightSource(Math.PI/2.0, Math.PI/7.0);

	    m3d.render(image);
	    image.write("png",  new File("btest.png"));

	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	System.exit(0);
   }
}
