import java.awt.*;
import java.awt.geom.*;
import java.io.FileOutputStream;
import org.bzdev.p3d.*;
import org.bzdev.geom.*;
import org.bzdev.graphs.*;
import org.bzdev.gio.OutputStreamGraphics;
import org.bzdev.gio.ImageSequenceWriter;
import org.bzdev.gio.ISWriterOps;
import org.bzdev.gio.ImageOrientation;

public class ConvexPathCTest2 {
    public static void main(String argv[]) throws Exception {

	Model3D m3d = new Model3D();
	double baseThickness = 4.5;
	SteppedGrid.Builder sgb = new SteppedGrid
	    .Builder(m3d, baseThickness, 0.0);

	double height0 = 44.45;

	double height2 = 10.0;
	double height1 = height0 - height2;
	double width = 6.0;
	double r = 1.0;
	
	sgb.addRectangles(0.0, 0.0, width, height0, baseThickness, 0.0);
	sgb.addRectangles(0.0, height1, width, height2,
			  baseThickness, baseThickness);

	double y1 = (height1 - width)/2.0;
	sgb.addRectangles(0.0, y1, width, width, 0.0, 0.0, true, true);

	SteppedGrid sg = sgb.create();

	Path2D tpath = Paths2D.createArc(0.0, 0.0, r, 0.0, 2*Math.PI,
					 Math.PI/180);

	tpath.closePath();

	FileOutputStream os = new FileOutputStream("circle.ps");
	OutputStreamGraphics osg = OutputStreamGraphics.newInstance
	    (os, Graph.DEFAULT_WIDTH, Graph.DEFAULT_HEIGHT, "ps");
	Graph graph = new Graph(osg);
	graph.setRanges(-1.5, 1.5, -1.5, 1.5);
	Graphics2D g2d = graph.createGraphics();
	g2d.setColor(Color.BLACK);
	g2d.setStroke(new BasicStroke(1.0F));
	graph.draw(g2d, tpath);
	g2d.dispose();
	graph.write();

	Path3D line = new Path3D.Double();
	line.moveTo(width/2.0, height1/2, baseThickness);
	line.lineTo(width/2.0, height1/2, 0.0);
	double inormal[] = {1.0, 0.0, 0.0};
	BezierGrid bg = new BezierGrid(tpath,
				       BezierGrid.getMapper(line, inormal));
	
	bg.flip();

	Path3D topCircle = bg.getBoundary(new Point3D.Double(r, 0.0,
							     baseThickness),
				    null, true);
	Path3DInfo.printSegments(topCircle);

	Path3D bottomCircle = bg.getBoundary(new Point3D.Double(r, 0.0, 0.0),
					     null, true);

	Path3D topSquare = sg.getBoundary(new Point3D.Double(0.0, y1,
							     baseThickness),
					  null, true);
	Path3DInfo.printSegments(topCircle);

	Path3D bottomSquare = sg.getBoundary(new Point3D.Double(0.0, y1, 0.0),
					     null, true);

	ConvexPathConnector c1 = new ConvexPathConnector(topCircle, topSquare);
	ConvexPathConnector c2 = new ConvexPathConnector(bottomCircle,
							 bottomSquare);

	m3d.append(bg);
	m3d.append(c1);
	m3d.append(c2);

	// if (m3d.notPrintable(System.out)) System.exit(1);

	m3d.createImageSequence(new FileOutputStream("fpTab.isq"),
						     "png", 8,8);
	System.out.println("bounding box = " + m3d.getBounds());


    }
}
