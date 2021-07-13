import java.awt.Color;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.PathIterator;
import java.io.FileOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.bzdev.geom.*;
import org.bzdev.math.VectorOps;
import org.bzdev.math.stats.BasicStats;
import org.bzdev.math.stats.BasicStats.Population;
import org.bzdev.p3d.Model3D;
import org.bzdev.p3d.Model3DView;
import org.bzdev.anim2d.Animation2D;
import org.bzdev.util.units.MKS;


// This was taken from an application.


public class BGTest2 {

    private static final double SQR2 = Math.sqrt(2.0);

    static BezierGrid makeTop(double h, double h2, double r1, int n,
			      double ri, double z, int m,
			      ArrayList<Integer>linearIndices)
	throws IllegalArgumentException
    {
	Path2D path = new Path2D.Double();
	if (h2 < 0.0 || h - h2 <= 0.0) {
	    throw new IllegalArgumentException("bad arguments");
	}
	double r2 = (r1 - SQR2*h)/(SQR2-1);
	if (r2 < 0.0) throw new IllegalArgumentException("bad arguments");
	double r0 = Math.sqrt((r1+r2)*(r1+r2) - (h+r2)*(h+r2));
	double maxDelta = Math.PI/(2*n);
	double hdelta = Math.sin(maxDelta)*r0;
	double hh = hdelta;
	path.moveTo(-h, -r0);
	while (hh < -h2) {
	    path.lineTo(hh, -r0);
	    hh += hdelta;
	}
	path.lineTo(-h2, -r0);
	hh = -h2 + hdelta;
	while (hh < 0.0) {
	    path.lineTo(hh, -r0);
	    hh += hdelta;
	}
	path.lineTo(0.0, -r0);
	while (hh < h2) {
	    path.lineTo(hh, -r0);
	    hh += hdelta;
	}
	path.lineTo(h2, -r0);
	hh = h2+hdelta;
	while (hh < h) {
	    path.lineTo(hh, -r0);
	    hh += hdelta;
	}
	path.lineTo(h, -r0);
	double[] t1 = {0.0, 1.0};
	double[] t2 = {1.0, 0.0};
	Path2D arc = Paths2D.createArc(h, -r0, t1, 0, r0, -h, maxDelta);
	path.append(arc, true);
	hh = -h + hdelta;
	while (hh < -h2) {
	    path.lineTo(r0, hh);
	    hh += hdelta;
	}
	path.lineTo(r0, -h2);
	hh = -h2 + hdelta;
	while (hh < 0.0) {
	    path.lineTo(r0, hh);
	    hh += hdelta;
	}
	path.lineTo(r0, 0.0);
	hh = hdelta;
	while (hh < h2) {
	    path.lineTo(r0, hh);
	    hh += hdelta;
	}
	path.lineTo(r0, h2);
	hh = h2 + hdelta;
	while (hh < h) {
	    path.lineTo(r0, hh);
	    hh += hdelta;
	}
	path.lineTo(r0, h);
	t2[0] = -1.0;
	arc = Paths2D.createArc(r0, h, t2, 0, h, r0, maxDelta);
	path.append(arc, true);
	hh = h - hdelta;
	while (hh > h2) {
	    path.lineTo(hh, r0);
	    hh -= hdelta;
	}
	path.lineTo(h2, r0);
	hh = h2 - hdelta;
	while (hh > 0.0) {
	    path.lineTo(hh, r0);
	    hh -= hdelta;
	}
	path.lineTo(0.0, r0);
	hh = -hdelta;
	while (hh > 0.0) {
	    path.lineTo(hh, r0);
	    hh -= hdelta;
	}
	path.lineTo(-h2, r0);
	hh = -h2 - hdelta;
	while (hh > -h) {
	    path.lineTo(hh, r0);
	    hh -= hdelta;
	}
	path.lineTo(-h, r0);
	t1[1] = -1.0;
	arc = Paths2D.createArc(-h, r0, t1, 0, -r0, h, maxDelta);
	path.append(arc, true);
	hh = h - hdelta;
	while (hh > h2) {
	    path.lineTo(-r0, hh);
	    hh -= hdelta;
	}
	path.lineTo(-r0, h2);
	hh = h2 - hdelta;
	while (hh > 0.0) {
	    path.lineTo(-r0, hh);
	    hh -= hdelta;
	}
	path.lineTo(-r0, 0.0);
	hh =  -hdelta;
	while (hh > -h2) {
	    path.lineTo(-r0, hh);
	    hh -= hdelta;
	}
	path.lineTo(-r0, -h2);
	hh = -h2 - hdelta;
	while (hh > -h) {
	    path.lineTo(-r0, hh);
	    hh -= hdelta;
	}
	path.lineTo(-r0, -h);
	t2[0] = 1.0;
	arc = Paths2D.createArc(-r0, -h, t2, 0, -h, -r0, maxDelta);
	path.append(arc, true);
	path.closePath();
	List<Path2DInfo.Entry>list = Path2DInfo.getEntries(path);
	BezierGrid grid = new BezierGrid(list.size()-2, true, 2 + m, false);
	int nu = grid.getUArrayLength();
	int i = 0;
	int lastCubicIndex = -2;
	for (Path2DInfo.Entry entry: list) {
	    Point2D p2 = entry.getEnd();
	    double scale = (p2 == null)? 0.0: ri/p2.distance(0.0, 0.0);
	    Point2D p1 = (p2 == null)? null:
		new Point2D.Double(p2.getX()*scale, p2.getY()*scale);
	    switch(entry.getType()) {
	    case PathIterator.SEG_LINETO:
		grid.setRegion(i-1, m, 0);
		// fall through
		if (lastCubicIndex == (i-1)) {
		    linearIndices.add(lastCubicIndex);
		}
	    case PathIterator.SEG_MOVETO:
		linearIndices.add(i);
		grid.setPoint(i, m,
			      new Point3D.Double(p2.getX(), p2.getY(), z));
		grid.setPoint(i, m+1,
			      new Point3D.Double(p1.getX(), p1.getY(), z));
		grid.setRegion(i, m+1, 3);
		break;
	    case PathIterator.SEG_CUBICTO:
		if (i < nu) {
		    grid.setPoint(i, m,
				  new Point3D.Double(p2.getX(), p2.getY(), z));
		    grid.setPoint(i, m+1,
				  new Point3D.Double(p1.getX(), p1.getY(), z));
		    grid.setRegion(i, m+1, 3);
		    lastCubicIndex = i;
		}
		grid.setRegion(i-1, m, 1);
		break;
	    case PathIterator.SEG_CLOSE:
		break;
	    }
	    i++;
	}
	return grid;
    }

    static  Path2D makeRampTemplate(double divider, double z2, int m,
				    double extension,
				    double grade, double radius)
    {
	Path2D path = new Path2D.Double();
	double x = 0.0;
	double z = z2;
	path.moveTo(x, z);
	x += divider;
	path.lineTo(x, z);
	x += divider;
	path.lineTo(x, z);
	x += extension;
	path.lineTo(x, z);
	double theta = Math.atan(grade);
	path.append(Paths2D.createArc(path, radius, false,
				      theta, Math.PI/(2*m)),
		    true);
	x = path.getCurrentPoint().getX();
	z = path.getCurrentPoint().getY();
	double newz = z2 - z;
	double deltaz = z - newz;
	double deltax = deltaz/grade;
	x += deltax;
	z = newz;
	path.lineTo(x, z);
	z = 0.0;
	path.append(Paths2D.createArc(path, radius, true,
				      theta, Math.PI/(2*m)),
		    true);
	return Paths2D.reverse(path);
    }

    public static void main(String argv[]) throws Exception {

	boolean makeImages = true;
	boolean makeAnimation = false;

	double scale = 10.0;
	double h = MKS.feet(14.0)*scale;
	double h2 = MKS.feet(2.0)*scale;
	double r1 = MKS.feet(30.0)*scale;
	double ri = r1 - MKS.feet(14)*scale;
	double z1 = MKS.feet(15)*scale;
	double z2 = z1 + 1.0*scale;
	double divider = MKS.feet(1.5)*scale;
	double dheight = MKS.feet(2.0)*scale;
	double capHeight = MKS.feet(2.5)*scale;
	double extension = MKS.feet(6.0)*scale;
	double extension3 = MKS.feet(50.0)*scale;
	double grade = 0.25;
	double radius = MKS.feet(50.0)*scale;

	ArrayList<Integer> linearIndices = new ArrayList<>(64);
	int n = 10;
	int mp = 10;
	Path2D epath = makeRampTemplate(divider, z2, mp, extension,
					grade, radius);
	Path2DInfo.Entry[] list =
	    Path2DInfo.getEntries(epath).toArray(new Path2DInfo.Entry[0]);

	int m = list.length-1;
	System.out.println("m = " + m);
	// Path2DInfo.printSegments(epath);

	Path2D epath3 = makeRampTemplate(divider, z2, m, extension3,
					grade, radius);

	Path2DInfo.Entry[] list3 =
	    Path2DInfo.getEntries(epath3).toArray(new Path2DInfo.Entry[0]);
	System.out.println("m3 = " + (list3.length-1));

	BezierGrid top = makeTop(h, h2, r1, n, ri, z2, m,
				 linearIndices);

	int lastInd = -1;
	int indCase = 0;
	for (Integer ind: linearIndices) {
	    if (ind != lastInd+1) {
		indCase++;
	    }
	    Point3D p1 = top.getPoint(ind, m);
	    switch (indCase) {
	    case 0:
		for (int k = 0; k < m; k++) {
		    Point2D pt = list[k+1].getStart();
		    top.setPoint(ind, k, p1.getX(),
				 p1.getY() - pt.getX(),
				 pt.getY());
		}
		break;
	    case 1:
		for (int k = 0; k < m; k++) {
		    Point2D pt = list[k+1].getStart();
		    top.setPoint(ind, k,
				 p1.getX() + pt.getX(),
				 p1.getY(),
				 pt.getY());
		}
		/*
		top.setPoint(ind,m-1, new Point3D.Double(p1.getX() + scale,
							 p1.getY(),
							 p1.getZ()));
		top.setPoint(ind,m-2, new Point3D.Double(p1.getX() + 2*scale,
							 p1.getY(),
							 p1.getZ()));
		*/
		break;
	    case 2:
		for (int k = 0; k < m; k++) {
		    Point2D pt = list[k+1].getStart();
		    top.setPoint(ind, k, p1.getX(),
				 p1.getY() + pt.getX(),
				 pt.getY());
		}
		break;
	    case 3:
		for (int k = 0; k < m; k++) {
		    Point2D pt = list3[k+1].getStart();
		    top.setPoint(ind, k,
				 p1.getX() - pt.getX(),
				 p1.getY(),
				 pt.getY());
		}
		break;
	    default:
		break;
	    }
	    lastInd = ind;
	}

	lastInd = -1;
	indCase = 0;
	double[] scoords;
	double[] tcoords;
	for (Integer ind: linearIndices) {
	    if (ind != lastInd+1) {
		indCase++;
	    }
	    Point3D p1 = top.getPoint(ind, m);
	    switch (indCase) {
	    case 0:
		System.out.println("ind = " + ind);
		for (int k = 0; k < m; k++) {
		    if (list[k+1].getType() == PathIterator.SEG_LINETO) {
			tcoords = Path3D.setupCubic(top.getPoint(ind, k),
						    top.getPoint(ind, k+1));
			top.setSplineV(ind, k, tcoords);
		    } else {
			Point3D pt = top.getPoint(ind, k);
			tcoords = list[k+1].getCoords();
			scoords = Path3D.setupCubic(top.getPoint(ind, k),
						    top.getPoint(ind, k+1));
			scoords[1] = p1.getY() - tcoords[0];
			scoords[2] = tcoords[1];
			scoords[4] = p1.getY() - tcoords[2];
			scoords[5] = tcoords[3];
			top.setSplineV(ind, k, scoords);
		    }
		}
		break;
	    case 1:
		for (int k = 0; k < m; k++) {
		    if (list[k+1].getType() == PathIterator.SEG_LINETO) {
			tcoords = Path3D.setupCubic(top.getPoint(ind, k),
						    top.getPoint(ind, k+1));
			top.setSplineV(ind, k, tcoords);
		    } else {
			Point3D pt = top.getPoint(ind, k);
			tcoords = list[k+1].getCoords();
			scoords = Path3D.setupCubic(top.getPoint(ind, k),
						    top.getPoint(ind, k+1));
			scoords[0] = p1.getX() + tcoords[0];
			scoords[2] = tcoords[1];
			scoords[3] = p1.getX() + tcoords[2];
			scoords[5] = tcoords[3];
			top.setSplineV(ind, k, scoords);
		    }
		}
		break;
	    case 2:
		for (int k = 0; k < m; k++) {
		    if (list[k+1].getType() == PathIterator.SEG_LINETO) {
			tcoords = Path3D.setupCubic(top.getPoint(ind, k),
						    top.getPoint(ind, k+1));
			top.setSplineV(ind, k, tcoords);
		    } else {
			Point3D pt = top.getPoint(ind, k);
			tcoords = list[k+1].getCoords();
			scoords = Path3D.setupCubic(top.getPoint(ind, k),
						    top.getPoint(ind, k+1));
			scoords[1] = p1.getY() + tcoords[0];
			scoords[2] = tcoords[1];
			scoords[4] = p1.getY() + tcoords[2];
			scoords[5] = tcoords[3];
			top.setSplineV(ind, k, scoords);
		    }
		}
		break;
	    case 3:
		for (int k = 0; k < m; k++) {
		    if (list3[k+1].getType() == PathIterator.SEG_LINETO) {
			tcoords = Path3D.setupCubic(top.getPoint(ind, k),
						    top.getPoint(ind, k+1));
			top.setSplineV(ind, k, tcoords);
		    } else {
			Point3D pt = top.getPoint(ind, k);
			tcoords = list3[k+1].getCoords();
			scoords = Path3D.setupCubic(top.getPoint(ind, k),
						    top.getPoint(ind, k+1));
			scoords[0] = p1.getX() - tcoords[0];
			scoords[2] = tcoords[1];
			scoords[3] = p1.getX() -  tcoords[2];
			scoords[5] = tcoords[3];
			top.setSplineV(ind, k, scoords);
		    }
		}
		break;
	    default:
		break;
	    }
	    lastInd = ind;
	}


	Surface3D surface = new Surface3D.Double();

	for (int i = 0; i < top.getUArrayLength(); i++) {
	    Point3D p = top.getPoint(i, m-2);
	    if (p != null && (p.getX() == 0.0 || p.getY() == 0.0)) {
		p = top.getPoint(i-1, m-2);
		System.out.println(" divider corner: " + p);
		if (i-1 == 18) {
		    System.out.println(top.getPoint(i-2, m-2));
		}
		top.remove(i-1,m-2,2,2);
		System.out.println("i-1 = " + (i-1) + ", m-2 = " + (m-2));
		Path3DInfo.printSegments(top.getBoundary(i-1, m-2));

		BezierCap dcap = new BezierCap(top.getBoundary(i-1,m-2),
					       dheight, false);
 		surface.append(dcap);
	    }
	}

	surface.append(top);
	// Path3DInfo.printSegments(surface.getBoundary());

	if (!surface.isWellFormed(System.out)) {
	    System.out.println("surface is not well formed");
	    System.exit(1);
	}
	System.exit(0);
    }
}
