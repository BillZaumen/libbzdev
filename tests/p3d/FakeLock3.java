import org.bzdev.geom.*;
import org.bzdev.p3d.*;
import java.awt.geom.Path2D;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class FakeLock3 {

    private static double fix (double x) {
	return Math.round(x*10)/10.0;
    }

    static String bytesToHex(byte[] bytes) {
	final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9',
				 'A','B','C','D','E','F'};
	char[] hexChars = new char[bytes.length * 2];
	int v;
	for ( int j = 0; j < bytes.length; j++ ) {
	    v = bytes[j] & 0xFF;
	    hexChars[j * 2] = hexArray[v >>> 4];
	    hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	}
	return new String(hexChars);
    }

    private static String tdigest(Model3D m3d)
	throws UnsupportedEncodingException, NoSuchAlgorithmException
    {
	MessageDigest md = MessageDigest.getInstance("SHA-1");
	Iterator<Model3D.Triangle> it = m3d.tessellate();
	while(it.hasNext()) {
	    Model3D.Triangle triangle = it.next();
	    md.update(Double.toHexString(triangle.getX1()).getBytes("UTF-8"));
	    md.update(Double.toHexString(triangle.getY1()).getBytes("UTF-8"));
	    md.update(Double.toHexString(triangle.getZ1()).getBytes("UTF-8"));
	    md.update(Double.toHexString(triangle.getX2()).getBytes("UTF-8"));
	    md.update(Double.toHexString(triangle.getY2()).getBytes("UTF-8"));
	    md.update(Double.toHexString(triangle.getZ2()).getBytes("UTF-8"));
	    md.update(Double.toHexString(triangle.getX3()).getBytes("UTF-8"));
	    md.update(Double.toHexString(triangle.getY3()).getBytes("UTF-8"));
	    md.update(Double.toHexString(triangle.getZ3()).getBytes("UTF-8"));
	}
	return bytesToHex(md.digest());
    }

    private static String thdigest(Model3D m3d)
	throws UnsupportedEncodingException, NoSuchAlgorithmException
    {
	MessageDigest md = MessageDigest.getInstance("SHA-1");
	Iterator<Model3D.Triangle> it = m3d.tessellate();
	while(it.hasNext()) {
	    Model3D.Triangle triangle = it.next();
	    md.update(Integer.toHexString(triangle.hashCode())
		      .getBytes("UTF-8"));
	}
	return bytesToHex(md.digest());
    }


    public static void main(String argv[]) throws Exception {

	String digest1 = null;
	String digest2 = null;
	String digest1h = null;
	String digest2h = null;
	boolean tessellate1 = false;
	boolean tessellate2 = false;
	boolean hello  = false;
	boolean close = false;
	for (String arg: argv) {
	    if (arg.equals("hello")) hello  = true;
	    if (arg.equals("close")) close = true;
	    if (arg.equals("tessellate1")) tessellate1 = true;
	    if (arg.equals("tessellate2")) tessellate2 = true;
	}

	double r1 = 1.5;
	double h1 = 8;
	double h2 = 27 + h1;
	double r2 = 10.0;
	double delta = 0.5;

	double yoff = 2*r1;
	double xoff = 2*r1;

	var circle = Paths2D.createArc(0.0, 0.0, r1, 0.0,
					 2*Math.PI);
	circle.closePath();

	var lcircle = Paths2D.createArc(0.0, 0.0, fix(r1 + delta), 0.0,
					 2*Math.PI);
	lcircle.closePath();

	var loop2d = new Path2D.Double();
	/*
	loop2d.moveTo(0.0, fix(h1/2));
	loop2d.lineTo(0.0, h1);
	*/
	double delta1 = (h2 - h1)/5;
	/*
	for (int i = 1; i < 5; i++) {
	    double y = h1 + delta1*i;
	    loop2d.lineTo(0.0, y);
	}
	*/
	// loop2d.lineTo(0.0, h2);
	loop2d.moveTo(0.0, h2-delta1);
	loop2d.lineTo(0.0, h2);
	Path2D arc = Paths2D.createArc(loop2d, r2, false, Math.PI,
				       Math.PI/32);
	loop2d.append(arc, true);
	for (int i = 1; i < 2; i++) {
	    double y = h2 - i*delta1;
	    loop2d.lineTo(2*r2, y);
	}
	// loop2d.lineTo(2*r2, h1);

	Path3D loop = new Path3D.Double(loop2d, (i, p, type, bounds) -> {
		return new Point3D.Double(p.getX(), 0.0, p.getY());
	}, 0);

	
	double inormal[] = {1.0, 0.0, 0.0};
	BezierGrid loopGrid = new BezierGrid(circle,
					     BezierGrid.getMapper(loop,
								  inormal));
	
	Path3D circle1 = loopGrid.getBoundary(0, 0);

	int n = loopGrid.getUArrayLength();
	Path3D circle2 = loopGrid.getBoundary(n-1, 0);

	int m = loopGrid.getVArrayLength();

	System.out.format ("n = %d, m = %d\n", n, m);

	int ii = 32;
	int jj = 5;
	// create a smaller test case.
	BezierGrid subgrid = loopGrid.subgrid(ii, jj, 35-ii-1, 18-jj);

	Model3D m3d = new Model3D(false);
	// m3d.append(loopGrid);
	m3d.append(subgrid);
	// do not see an issue for tessellation level 0, 1, or 2.
	m3d.setTessellationLevel(3);

	if (tessellate1) {
	    digest1 = tdigest(m3d);
	    digest1h = thdigest(m3d);
	    if (digest1 == null) System.out.println("digest1 = null");
	}

	// SurfaceIterator  sit = m3d.getSurfaceIterator(null);

	if (hello) {
	    PrintWriter pw = new PrintWriter("patches.txt", "UTF-8");
	    pw.print("hello");
	    if (close) pw.close();
	}

	List<Model3D.Triangle> tlist = m3d.verifyEmbedded2DManifold();

	if (tessellate2) {
	    digest2 = tdigest(m3d);
	    digest2h = thdigest(m3d);
	}

	double[] coords = new double[48];
	int sgn = subgrid.getUArrayLength();
	int sgm = subgrid.getVArrayLength();
	System.out.format ("sgn = %d, sgm = %d\n", sgn, sgm);
	if (!hello) subgrid.print();

	if (tessellate1) {
	    System.out.println("digest1: " + digest1);
	    System.out.println("digest1h: " + digest1h);
	}
	if (tessellate2) {
	    System.out.println("digest2: " + digest2);
	    System.out.println("digest2h: " + digest2h);
	}

	if (tlist != null && !tlist.isEmpty()) {
	    System.out.println("m3d not an embedded manifold");
	    for (Model3D.Triangle triangle: tlist) {
		System.out
		    .format("(%s, %s, %s)---(%s, %s, %s)---(%s, %s, %s)\n",
			    triangle.getX1(),
			    triangle.getY1(),
			    triangle.getZ1(),
			    triangle.getX2(),
			    triangle.getY2(),
			    triangle.getZ2(),
			    triangle.getX3(),
			    triangle.getY3(),
			    triangle.getZ3());
		System.out.format("    normal = (%s, %s, %s)\n",
				  triangle.getNormX(),
				  triangle.getNormY(),
				  triangle.getNormZ());
	    }
	    System.out.println("try to find a single patch causing the error");
	    double[] tcoords = new double[48];
	    SurfaceIterator sit = m3d.getSurfaceIterator(null);
	    while (!sit.isDone()) {
		switch(sit.currentSegment(tcoords)) {
		case SurfaceIterator.CUBIC_PATCH:
		    Model3D tm3d = new Model3D(false);
		    Surface3D surface = new Surface3D.Double();
		    surface.addCubicPatch(tcoords);
		    tm3d.append(surface);
		    tm3d.setTessellationLevel(3);
		    List<Model3D.Triangle> tlist2 =
			tm3d.verifyEmbedded2DManifold();
		    if (tlist2 != null) {
			DataOutputStream dout = new DataOutputStream
			    (new FileOutputStream("patch.dat"));
			for (int i = 0; i < 48; i++) {
			    dout.writeDouble(tcoords[i]);
			}
			dout.flush();
			dout.close();
			System.out.println("created patch.dat:");
			for (int i = 36; i >= 0; i -= 12) {
			    for (int j = 0; j < 12; j += 3) {
				System.out.format(" (%g, %g, %g)",
						  tcoords[i+j],
						  tcoords[i+j+1],
						  tcoords[i+j+2]);
			    }
			    System.out.println();
			}
			System.exit(1);
		    }
		}
		sit.next();
	    }
	}

	m3d.createImageSequence(new FileOutputStream("fakelock3.isq"),
				 "png",
				 8, 6, 0.0, 0.5, 0.0, false);
	System.exit(0);
    }
}
