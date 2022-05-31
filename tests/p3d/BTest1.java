import org.bzdev.p3d.*;
import org.bzdev.gio.OutputStreamGraphics;
import java.io.*;
import java.nio.*;
import java.nio.channels.FileChannel;
import java.awt.*;
import java.awt.image.*;
import javax.imageio.*;
import java.io.*;

public class BTest1 {

    final static int WIDTH = 1024;
    final static int HEIGHT = 900;

    static Model3D createFile() {
	Model3D m3d = new Model3D();

	m3d.addTriangle(0.0, 0.0, 0.0,
			100.0, 100.0, 0.0,
			100.0,  0.0,  0.0, Color.BLACK);
	m3d.addTriangle(0.0, 0.0, 0.0,
			0.0, 100.0, 0.0,
			100.0, 100.0, 0.0, Color.BLACK);

	m3d.addTriangle(0.0, 0.0, 0.0,
			100.0, 0.0, 0.0,
			50.0, 50.0, 100.0, Color.RED.darker());
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
	return m3d;
    }



    public static void main(String argv[]) {
	try {
	    Model3D m3d = createFile();

	    System.out.println("Surface area = " + m3d.area());
	    System.out.println("Volume = " + m3d.volume());

	    OutputStream os = new FileOutputStream(new File("btest1.ps"));
	    OutputStreamGraphics osg =
		OutputStreamGraphics.newInstance(os, WIDTH, HEIGHT, "ps");

	    try {
		System.setSecurityManager(new SecurityManager());
	    } catch (UnsupportedOperationException eu) {}

	    Model3D.Image image = new Model3D.Image (osg);

	    Graphics2D g2d = image.createGraphics();
	    g2d.setColor(Color.BLUE);
	    g2d.fillRect(0,0, WIDTH, HEIGHT);
	    g2d.setColor(Color.black);
	    g2d.setRenderingHint
		(RenderingHints.KEY_INTERPOLATION,
		 RenderingHints.VALUE_INTERPOLATION_BILINEAR);

	    image.setCoordRotation(-Math.PI/6.0, Math.PI/4.0, 0.0);
	    image.setColorFactor(0.0);
	    m3d.setImageParameters(image);

	    m3d.render(image);
	    // ImageIO.write(image, "png", new File("BTest.png"));
	    image.write();
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	System.exit(0);
   }
}
