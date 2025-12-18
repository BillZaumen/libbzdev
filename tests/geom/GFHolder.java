import java.awt.geom.*;
import java.io.FileOutputStream;

import org.bzdev.geom.*;
import org.bzdev.p3d.*;

public class GFHolder {

    public static void main(String argv[]) throws Exception {

	// distances in mm.
	double r1 = 100;
	double r2 = 17;
	double r3 = r1;
	
	double r0 = r1 - r2;

	double vlen = 150 - r2 - r3;
	double r4 = r2;

	double radius = 2.0;


	Path2D path2D0 = Paths2D.createArc(r0, r1, -1.0, 0.0,
					   0.0, 0.0, 0.0, -1.0,
					   Math.PI/64);
	final Path3D path = new
	    Path3D.Double(path2D0,
			  (n, p, type, bounds) -> {
			      return new Point3D.Double(p.getX(), p.getY(),
							0.0);
			  });


	Path2D path2D1 = Paths2D.createArc(r1, 0.0, 0.0, 0.0,
					3*Math.PI/2, Math.PI/64);
	Path3D path3D1 = new
	    Path3D.Double(path2D1,
			  (n, p, type, bounds) -> {
			      return new Point3D.Double(p.getX(), p.getY(),
							0.0);
			  });
	path.append(path3D1, true);
	
	Path2D path2D2 = Paths2D.createArc(r2, 0.0, 0.0, 0.0,
					Math.PI/2, Math.PI/64);
	
	Path3D path3D2 =new
	    Path3D.Double(path2D2,
			  (n, p, type, bounds) -> {
			      Point3D end = path.getEnd();
			      return new Point3D.Double(end.getX() + p.getY(),
							end.getY(),
							end.getZ() + p.getX());
			  });
			  
	path.append(path3D2, true);

	path.lineTo(path.getEnd().getX(), path.getEnd().getY(),
		    path.getEnd().getZ() + vlen);

	Path2D path2D3 = Paths2D.createArc(r3, 0.0, 0.0, 0.0,
					Math.PI/2, Math.PI/64);
	Path3D path3D3 = new
	    Path3D.Double(path2D3,
			  (n, p, type, bounds) -> {
			      Point3D end = path.getEnd();
			      return new Point3D.Double(end.getX(),
							end.getY() - p.getX(),
							end.getZ() - p.getY());
			  });
	path.append(path3D3, true);

	Path2D path2D4 = Paths2D.createArc(r4, 0.0, 0.0, 0.0,
					   2*Math.PI - Math.PI/4 + Math.PI/32,
					   Math.PI/64);

	final double delta = 0.125/2;


	// double darray[] = {-2*delta};
	double darray[] = {-delta};

	Path3D path3D4 = new
	    Path3D.Double(path2D4,
			  (n, p, type, bounds) -> {
			      Point3D end = path.getEnd();
			      /*
			      double offset = darray[0];
			      darray[0] += (offset < 0)? delta;
			      if (offset < 0.0) offset = 0.0;
			      */
			      return new Point3D.Double(end.getX() + p.getX(),
							end.getY() + p.getY(),
							end.getZ());
			  });
	    
	path.append(path3D4, true);

        Path3DInfo.printSegments(path);
	/*
	int i = 0;
	for (Path3DInfo.Entry entry: Path3DInfo.getEntries(path)) {
	    double[] tangent = new double[9];
	    double[] normal = new double[9];
	    double[] binormal = new double[9];
	    int type = entry.getType();
	    if (type == PathIterator3D.SEG_MOVETO) {i++; continue;}
	    Point3D start = entry.getStart();
	    double[] coords = entry.getCoords();
	    if (Path3DInfo.getTangent(0.0, tangent, 0,
				      start.getX(), start.getY(), start.getZ(),
				      type, coords)) {
		if (i >= 129 && i <= 130) {
		    System.out.format("%d start, tangent = (%g, %g, %g)",
				      i, tangent[0], tangent[1], tangent[2]);
		}
		if (Path3DInfo.getNormal(0.0, normal, 0,
					 start.getX(), start.getY(),
					 start.getZ(),
					 type, coords)) {
		    if (i >= 129 && i <= 130) {
			System.out.format(", normal = (%g, %g, %g)",
					  normal[0], normal[1], normal[2]);
			Path3DInfo.getBinormal(0.0, binormal, 0,
					       start.getX(), start.getY(),
					       start.getZ(),
					       type, coords);
			System.out.format(", binormal = (%g, %g, %g)",
					  binormal[0], binormal[1],
					  binormal[2]);
		    }
		}
		if (i >= 129 && i <= 130) {
		    System.out.println();
		}
	    }
	    if (Path3DInfo.getTangent(1.0, tangent, 0,
				      start.getX(), start.getY(), start.getZ(),
				      type, coords)) {
		if (i >= 129 && i <= 130) {
		    System.out.format(" %d end, tangent = (%g, %g, %g)",
				      i, tangent[0], tangent[1], tangent[2]);
		}
		
		if (Path3DInfo.getNormal(1.0, normal, 0,
					 start.getX(), start.getY(),
					 start.getZ(),
					 type, coords)) {
		    if (i >= 129 && i <= 130) {
			System.out.format(", normal = (%g, %g, %g)",
					  normal[0], normal[1], normal[2]);
			Path3DInfo.getBinormal(0.0, binormal, 0,
					       start.getX(), start.getY(),
					       start.getZ(),
					       type, coords);
			System.out.format(", binormal = (%g, %g, %g)",
					  binormal[0], binormal[1],
					  binormal[2]);
		    }
		}
		if (i >= 129 && i <= 130) {
		    System.out.println();
		}
	    }
	    i++;
	}
	*/


	Path2D circle = Paths2D.createArc(0.0, 0.0, -radius, 0.0,
					  2*Math.PI, Math.PI/16);
	circle.closePath();

	double inormal[] = {0.0, -1.0, 0.0};

	BezierGrid grid = new BezierGrid(circle,
					 BezierGrid.getMapper(path, inormal));

	

	/*
	int un = grid.getUArrayLength();
	for (int i = 31; i < 36; i++) {
	    System.out.print(i + ":");
	    for (int j = 0; j < grid.getVArrayLength(); j++) {
		Point3D point = grid.getPoint(i, j);
		System.out.format(" (%g, %g, %g)",
				  point.getX(), point.getY(), point.getZ());
	    }
	    System.out.println();
	}
	*/

	Surface3D surface = new Surface3D.Double(grid);

	Path3D boundary1 = surface.getBoundary(path.getStart(), null, true);
	Path3D boundary2 = surface.getBoundary(path.getEnd(), null, true);

	BezierCap cap1 = new BezierCap(boundary1, 0.0, true);
	BezierCap cap2 = new BezierCap(boundary2, 0.0, true);

	surface.append(cap1);
	surface.append(cap2);

	Model3D m3d = new Model3D();
	m3d.append(surface);

	/*
	m3d.createImageSequence(new FileOutputStream("holder.isw"),
				"png", 8, 8,
				0.0, 0.0, 1.0, false );
	*/

	m3d.setTessellationLevel(1);

	if (m3d.notPrintable(System.out)) {
	    System.exit(1);
	}

	m3d.writeSTL("GFHolder: units in mm",
		     "holder.stl");

	System.exit(0);
    }
}
