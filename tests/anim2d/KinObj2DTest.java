import org.bzdev.anim2d.*;
import org.bzdev.geom.Path2DInfo;
import org.bzdev.geom.BasicSplinePath2D;
import org.bzdev.graphs.Graph;
import java.awt.*;
import java.awt.geom.*;

public class KinObj2DTest {

    static class OurDirectedObject2D extends DirectedObject2D {
	public OurDirectedObject2D(Animation2D animation, String name,
				   boolean intern) {
	    super(animation, name, intern);
	}
	public void addTo(Graph graph, Graphics2D g2d,
			  Graphics2D g2dGCS) {}
	@Override
	    public void update(double time, long ticks) {
	    super.update(time, ticks);
	}

	// make public for testing.
	@Override
	public double getU() {return super.getU();}

    }

    public static void main(String argv[]) {
	try {
	    Animation2D animation = new Animation2D
		(Graph.DEFAULT_WIDTH, Graph.DEFAULT_HEIGHT);
	    //Graph graph = new Graph(1600, 900, BufferedImage.TYPE_INT_RGB);
	    animation.setRanges (-10.0, 10.0, -10.0, 10.0);
	    animation.initFrames(10, "kin/img", "png");
	    Graph graph = animation.getGraph();
	    boolean closeIt = false;
	    boolean endEarly = false;
	    boolean showCurve = false;
	    boolean extendIt = false;
	    boolean preMove = false;

	    for (int i = 0; i < argv.length; i++) {
		if (argv[i].equals("--preMove")) preMove = true;
		if (argv[i].equals("--closePath")) closeIt = true;
		if (argv[i].equals("--endEarly")) endEarly = true;
		if (argv[i].equals("--showCurve")) showCurve = true;
		if (argv[i].equals("--extendPath")) extendIt = true;
	    }

	    OurDirectedObject2D ko =
		new OurDirectedObject2D(animation, "ko", true) {
		};
	    ko.setZorder(0, false);
	    BasicSplinePath2D path = new BasicSplinePath2D();
	    if (preMove) {
		path.moveTo(0.0, 0.0);
		path.lineTo(0.0, 1.0);
	    }
	    path.moveTo(-8.0, 0.0);
	    path.lineTo(-8.0, -4.0);
	    path.lineTo(0.0, -4.0);
	    path.quadTo(8.0, -4.0, 8.0, 0.0);
	    path.curveTo(8.0, 2.0, 2.0, 4.0, 0.0, 4.0);
	    path.curveTo(-2.0, 4.0, -8.0, 4.0, -8.0, (endEarly? 2.0: 0.0));
	    if (closeIt) path.closePath();
	    if (extendIt) {
		path.lineTo(-7.0, 0.0);
		path.lineTo(-7.0, 1.0);
		path.closePath();
	    }
	    int j = 0;
	    PathIterator pit = path.getPathIterator(null);
	    double[] coords = new double[6];
	    while (!pit.isDone()) {
		int type = pit.currentSegment(coords);
		switch (type) {
		case PathIterator.SEG_MOVETO:
		    System.out.println("MOVETO (" + coords[0] 
				       + ", " + coords[1] + ")");
		    break;
		case PathIterator.SEG_LINETO:
		    System.out.println("LINETO (" + coords[0] 
				       + ", " + coords[1] + ")");
		    break;
		case PathIterator.SEG_QUADTO:
		    System.out.println("QUADTO:");
		    for (int i = 0; i < 4; i += 2)
			System.out.println("    ... control point ("
					   + coords[i] + ", " + coords[i+1]
					   + ")");
		    break;
		case PathIterator.SEG_CUBICTO:
		    System.out.println("CUBICTO:");
		    for (int i = 0; i < 6; i += 2)
			System.out.println("    ... control point ("
					   + coords[i] + ", " + coords[i+1]
					   + ")");
		    break;
		case PathIterator.SEG_CLOSE:
		    System.out.println("CLOSE PATH");
		    break;
		}
		System.out.println("     ... segment length["
				   + j
				   +"] = " +
				   Path2DInfo.segmentLength(path, j++));
		pit.next();
	    }
	    ko.setPath(path, (preMove? 0.0: 1.0), 0.0, true, 0.0);
	    ko.setPathVelocity(1.0);
	    Graphics2D g2d = graph.createGraphics();
	    g2d.setColor(Color.BLACK);
	    long i = 0;
	    double tt = 0.0;
	    animation.run(0L);
	    if (preMove) {
		// ko.setInitialTime(t);
		ko.update(tt, i++);
		tt = 1.0;
		double xc = ko.getX();
		double yc = ko.getY();
		double angle = ko.getAngle();
		double dx = Math.cos(angle);
		double dy = Math.sin(angle);
		System.out.println(tt + "(u=" + ko.getU()
				   + "): " + xc +" " + yc +" "
				   + Math.toDegrees(angle));
		graph.draw(g2d, new Line2D.Double(xc-dx, yc-dy, xc+dx, yc+dy));
	    }
	    for (double t = tt; t <= tt + 60.0; t += 1.0) {
		ko.update(t, i++);
		double xc = ko.getX();
		double yc = ko.getY();
		double angle = ko.getAngle();
		double dx = Math.cos(angle);
		double dy = Math.sin(angle);
		System.out.println(t + "(u=" + ko.getU() + "): "
				   + xc +" " + yc +" "
				   + Math.toDegrees(angle));
		graph.draw(g2d, new Line2D.Double(xc-dx, yc-dy, xc+dx, yc+dy));
	    }
	    if (showCurve) {
		g2d.setColor(Color.BLUE);
		graph.draw(g2d, path);
	    }
	    graph.write("png", "testGraph.png");
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	System.exit(0);
    }
}
