import org.bzdev.p3d.*;
import org.bzdev.geom.*;
import java.awt.*;
import java.awt.image.*;
import java.io.FileOutputStream;
import java.util.Iterator;

public class TLTest8 {
    public static void main(String argv[]) throws Exception {
	Surface3D surface = new Surface3D.Double();

	double coords1[] = {
	    67.88007455329418, -73.43225094356856, 0.0,
	    68.84130045767918, -72.54370285721953, 0.0,
	    69.78507751876967, -71.63627871853981, 0.0,
	    70.71067811865476, -70.71067811865474, 0.0,
	    68.72714390180971, -72.45321946337145, 3.3160715688999067,
	    69.68836980619471, -71.56467137702242, 3.3160715688999067,
	    70.71067811865476, -70.71067811865474, 0.0,
	    71.63627871853984, -69.78507751876967, 0.0,
	    69.57421325032523, -71.47418798317436, 6.6321431377998135,
	    70.55570053698887, -70.55570053698887, 5.418376702815127,
	    71.56467137702242, -69.68836980619467, 3.3160715688999067,
	    72.54370285721951, -68.84130045767915, 0.0,
	    70.49270069651074, -70.49270069651072, 7.8459095727845,
	    71.47418798317437, -69.57421325032523, 6.6321431377998135,
	    72.45321946337147, -68.7271439018097, 3.3160715688999067,
	    73.43225094356856, -67.88007455329416, 0.0,

	};

	double coords2[] = {
	    67.88007455329418, -73.43225094356856, 0.0,
	    68.72714390180971, -72.45321946337145, -3.3160715688999067,
	    69.57421325032523, -71.47418798317436, -6.6321431377998135,
	    70.49270069651074, -70.49270069651072, -7.8459095727845,
	    68.84130045767918, -72.54370285721953, 0.0,
	    69.68836980619471, -71.56467137702242, -3.3160715688999067,
	    70.55570053698887, -70.55570053698887, -5.418376702815127,
	    71.47418798317437, -69.57421325032523, -6.6321431377998135,
	    69.78507751876967, -71.63627871853981, 0.0,
	    70.71067811865476, -70.71067811865474, 0.0,
	    71.56467137702242, -69.68836980619467, -3.3160715688999067,
	    72.45321946337147, -68.7271439018097, -3.3160715688999067,
	    70.71067811865476, -70.71067811865474, 0.0,
	    71.63627871853984, -69.78507751876967, 0.0,
	    72.54370285721951, -68.84130045767915, 0.0,
	    73.43225094356856, -67.88007455329416, 0.0,

	};

	surface.addCubicPatch(coords1);
	Model3D m3d = new Model3D();
	m3d.append(surface);
	m3d.setTessellationLevel(0);

	Iterator<Model3D.Triangle> it = m3d.tessellate();
	while (it.hasNext()) {
	    Model3D.Triangle t = it.next();
	    System.out.format("(%g, %g, %g)->(%g, %g, %g)->(%g, %g, %g)\n",
			      t.getX1(), t.getY1(), t.getZ1(),
			      t.getX2(), t.getY2(), t.getZ2(),
			      t.getX3(), t.getY3(), t.getZ3());
	}
	surface = new Surface3D.Double();
	surface.addCubicPatch(coords2);
	m3d = new Model3D();
	m3d.append(surface);
	m3d.setTessellationLevel(0);
	it = m3d.tessellate();
	while (it.hasNext()) {
	    Model3D.Triangle t = it.next();
	    System.out.format("(%g, %g, %g)->(%g, %g, %g)->(%g, %g, %g)\n",
			      t.getX1(), t.getY1(), t.getZ1(),
			      t.getX2(), t.getY2(), t.getZ2(),
			      t.getX3(), t.getY3(), t.getZ3());
	}


	surface = new Surface3D.Double();
	surface.addCubicPatch(coords1);
	surface.addCubicPatch(coords2);


	

	if (surface.isWellFormed(System.out) == false) {
	    System.out.println("surface is not well formed");
	}

	m3d = new Model3D();

	m3d.append(surface);
	m3d.setTessellationLevel(0);

	if (m3d.verifyNesting(System.out)) {
	    System.out.println("verifyNesting failed as expected");
	    System.exit(0);
	} else {
	    System.out.println("m3d.verifgyNesting should have returned false");
	    System.exit(1);
	}
    }
}
