import org.bzdev.anim2d.*;
import org.bzdev.gio.ImageSequenceWriter;
import org.bzdev.graphs.Graph;
import org.bzdev.lang.Callable;
import org.bzdev.geom.Path2DInfo;
import org.bzdev.geom.BasicSplinePathBuilder;
import org.bzdev.geom.BasicSplinePath2D;
import org.bzdev.geom.SplinePathBuilder;
import org.bzdev.geom.SplinePathBuilder.CPointType;
import org.bzdev.geom.SplinePathBuilder.CPoint;

import java.awt.*;
import java.awt.geom.*;
import java.io.File;
import org.bzdev.io.DirectoryAccessor;

class PolyObject extends DirectedObject2D {
    Path2D shape;
    PolyObject(Animation2D animation, String name, boolean intern) {
	super(animation, name, intern);
	shape = new Path2D.Double();
	shape.moveTo(-0.5, -0.5);
	shape.lineTo(0.0, -0.5);
	shape.lineTo(0.5, 0.0);
	shape.lineTo(0.0, 0.5);
	shape.lineTo(-0.5, 0.5);
	shape.closePath();
    }

    public void addTo(Graph graph, Graphics2D g2d, Graphics2D g2dGCS) {
	Color csave = g2d.getColor();
	Stroke ssave = g2d.getStroke();
	try {
	    AffineTransform af = getAddToTransform();
	    Shape s = new Path2D.Double(shape, af);
	    g2d.setColor(Color.RED);
	    graph.draw(g2d, s);
	    graph.fill(g2d, s);
	} finally {
	    g2d.setColor(csave);
	    g2d.setStroke(ssave);
	}
    }
}

class CircObject extends DirectedObject2D {
    CircObject(Animation2D animation, String name, boolean intern) {
	super(animation, name, intern);
    }

    Ellipse2D circ = new Ellipse2D.Double(-0.5, -0.5, 1.0, 1.0);

    public void addTo(Graph graph, Graphics2D g2d, Graphics2D g2dGCS) {

	double x = getX();
	double y = getY();
	Ellipse2D circ = new Ellipse2D.Double(x - 0.5, y - 0.5, 1.0, 1.0);

	Color csave = g2d.getColor();
	Stroke ssave = g2d.getStroke();

	g2d.setColor(Color.RED);
	g2d.setStroke(new BasicStroke(1.0F));

	graph.draw(g2d, circ);
	graph.fill(g2d, circ);
	g2d.setColor(csave);
	g2d.setStroke(ssave);
    }
}


public class CircPathTest {

    public static void main(String argv[]) {
	try {

	    final Animation2D animation = new Animation2D();
	    //Graph graph = new Graph(1600, 900, BufferedImage.TYPE_INT_RGB);
	    animation.setRanges(0.0, 0.0, 0.5, 0.5, 25.0, 25.0);
	    CircObject obj = new CircObject(animation, "obj", true);
	    obj.setZorder(0, true);
	    PolyObject pobj = new PolyObject(animation, "pobj", true);
	    pobj.setZorder(1, true);

	    // test a few methods.

	    pobj.setPosition(10.0, 20.0, Math.PI/2);
	    if (pobj.getX() != 10.0 || pobj.getY() != 20.0
		|| pobj.getAngle() != Math.PI/2) {
		throw new Exception("setPosition failed");
	    }
	    pobj.setPosition(5.0, 10.0);
	    if (pobj.getX() != 5.0 || pobj.getY() != 10.0
		|| pobj.getAngle() != Math.PI/2) {
		throw new Exception("setPosition failed");
	    }
	    pobj.setAngle(0.0);
	    if (pobj.getX() != 5.0 || pobj.getY() != 10.0
		|| pobj.getAngle() != 0.0) {
		throw new Exception("setAngle failed");
	    }
	    pobj.setPosition(0.0, 0.0, 0.0);

	    BasicSplinePathBuilder spb = new BasicSplinePathBuilder();

	    double r = 8.0;
	    BasicSplinePath2D path =
		new BasicSplinePath2D(new Ellipse2D.Double(-r,-r,2*r,2*r));

	    double length = 0;
	    for (Path2DInfo.Entry entry: Path2DInfo.getEntries(path)) {
		double seglen = entry.getSegmentLength();
		length += seglen;
		System.out.println("segment type = " + entry.getTypeString()
				   +", seglen = " + seglen);
		Point2D sp = entry.getStart();
		Point2D ep = entry.getEnd();
		if (sp != null && ep != null) {
		    System.out.println(" ... (" + sp.getX() + ", " + sp.getY()
				       + ") --> ("
				       + ep.getX() + ", " + ep.getY() +")");
		}
	    }
	    System.out.println("length = " + length);

	    AnimationPath2D apath =
		new AnimationPath2D(animation, "path", true);
	    SplinePathBuilder.CPoint[] cpoints= new SplinePathBuilder.CPoint[37];
	    cpoints[0] = new SplinePathBuilder.CPoint
		(SplinePathBuilder.CPointType.MOVE_TO, r, 0.0);
	    for (int i = 1; i < 36; i++) {
		double theta = Math.toRadians(i * 10.0);
		cpoints[i] =
		    new SplinePathBuilder.CPoint
		    (SplinePathBuilder.CPointType.SPLINE,
		     r * Math.cos(theta), r * Math.sin(theta));
	    }
	    cpoints[36] =
		new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.CLOSE);
	    // apath.append(cpoints);
	    spb.initPath(cpoints);
	    apath.setPath(spb.getPath());
	    apath.printConfiguration();
	    apath.printState();
	    if (argv.length > 0 && argv[0].equals("print")) {
		System.out.println("... now try printing with indentation");
		apath.printConfiguration("   ");
		apath.printState("    ");
		System.exit(0);
	    }
	    System.out.println("now try a cyclic spline");
	    length = 0.0;
	    for (Path2DInfo.Entry entry:
		     Path2DInfo.getEntries(apath.getPath())) {
		double seglen = entry.getSegmentLength();
		length += seglen;
		/*
		System.out.println("segment type = " + entry.getTypeString()
				   +", seglen = " + seglen);
		Point2D sp = entry.getStart();
		Point2D ep = entry.getEnd();
		if (sp != null && ep != null) {
		    System.out.println(" ... (" + sp.getX() + ", " + sp.getY()
				       + ") --> ("
				       + ep.getX() + ", " + ep.getY() +")");
		}
		*/
	    }
	    System.out.println("length = " + length
			       +" (exact value: " + (r * 2 * Math.PI) + ")");
	    // next case
	    apath = new AnimationPath2D(animation, "nextpath", true);
	    cpoints= new SplinePathBuilder.CPoint[19];
	    cpoints[0] = new SplinePathBuilder.CPoint
		(SplinePathBuilder.CPointType.MOVE_TO, r, 0.0);
	    for (int i = 1; i < 18; i++) {
		double theta = Math.toRadians(i * 10.0);
		cpoints[i] =
		    new SplinePathBuilder.CPoint
		    (SplinePathBuilder.CPointType.SPLINE,
		     r * Math.cos(theta), r * Math.sin(theta));
	    }
	    cpoints[18] = new SplinePathBuilder.CPoint
		(SplinePathBuilder.CPointType.SEG_END, -r, 0.0);
	    spb.initPath(cpoints);
	    // apath.append(cpoints);
	    apath.setPath(spb.getPath());
	    System.out.println("now try a non-cyclic spline:");
	    length = 0.0;
	    for (Path2DInfo.Entry entry:
		     Path2DInfo.getEntries(apath.getPath())) {
		double seglen = entry.getSegmentLength();
		length += seglen;
	    }

	    BasicSplinePath2D sp = apath.getPath();
	    if (apath.getMaxParameter() != sp.getMaxParameter()) {
		throw new Exception("apath - path test failed");
	    }

	    if (apath.getX(0.8) != sp.getX(0.8)) {
		throw new Exception("apath - path test failed");
	    }
	    if (apath.getY(0.8) != sp.getY(0.8)) {
		throw new Exception("apath - path test failed");
	    }
	    if (!apath.getPoint(0.8).equals(sp.getPoint(0.8))) {
		throw new Exception("apath - path test failed");
	    }
	    if (apath.dxDu(0.8) != sp.dxDu(0.8)) {
		throw new Exception("apath - path test failed");
	    }
	    if (apath.dyDu(0.8) != sp.dyDu(0.8)) {
		throw new Exception("apath - path test failed");
	    }
	    if (apath.d2xDu2(0.8) != sp.d2xDu2(0.8)) {
		throw new Exception("apath - path test failed");
	    }
	    if (apath.d2yDu2(0.8) != sp.d2yDu2(0.8)) {
		throw new Exception("apath - path test failed");
	    }
	    if (apath.curvature(0.8) != sp.curvature(0.8)) {
		throw new Exception("apath - path test failed");
	    }
	    if (apath.dsDu(0.8) != sp.dsDu(0.8)) {
		throw new Exception("apath - path test failed");
	    }
	    if (apath.getInversionLimit() != sp.getInversionLimit()) {
		throw new Exception("apath - path test failed");
	    }
	    if (apath.getPathLength() != sp.getPathLength()) {
		throw new Exception("apath - path test failed");
	    }
	    if (apath.getPathLength(0.4, 0.8) != sp.getPathLength(0.4, 0.8)) {
		throw new Exception("apath - path test failed");
	    }
	    if (apath.s(0.8) != sp.s(0.8)) {
		throw new Exception("apath - path test failed");
	    }
	    if (apath.u(1.0) != sp.u(1.0)) {
		throw new Exception("apath - path test failed");
	    }
	    double ilim = 1.e-8;
	    double ilim2 = ilim/2.0;
	    apath.setInversionLimit(ilim2);
	    if (sp.getInversionLimit() != ilim2) {
		throw new Exception("apath - path test failed: "
				    + sp.getInversionLimit()
				    + " != " + ilim2);
	    }
	    if (apath.getInversionLimit() != ilim2) {
		throw new Exception("apath - path test failed");
	    }
	    apath.setInversionLimit(ilim);
	    if (apath.getInversionLimit() != ilim) {
		throw new Exception("apath - path test failed");
	    }
	    apath.setInversionLimit(-1.0);
	    if (apath.getInversionLimit() != -1.0) {
		throw new Exception("apath - path test failed");
	    }

	    System.out.println("length = " + length
			       +" (exact value: " + (r * Math.PI) + ")");
	    // create a path using the factory.
	    AnimationPath2DFactory factory =
		new AnimationPath2DFactory(animation);
	    factory.set("color.red", 255);
	    factory.set("color.green", 255);
	    factory.set("color.blue", 255);
	    factory.set("color.alpha", 255);
	    factory.set("stroke.width", 2.0);
	    factory.set("stroke.dashIncrement", 20.0);
	    factory.set("stroke.dashPattern", "- ");
	    factory.set("zorder", 10);
	    factory.set("visible", true);
	    factory.set("cpoint.type", 0, "MOVE_TO");
	    factory.set("cpoint.x", 0, 7.0);
	    factory.set("cpoint.y", 0, 0.0);
	    double rc = 7.0;
	    for (int i = 1; i < 36; i++) {
		double theta = Math.toRadians(i * 10.0);
		factory.set("cpoint.type", i, "SPLINE");
		factory.set("cpoint.x", i, rc* Math.cos(theta));
		factory.set("cpoint.y", i, rc * Math.sin(theta));
	    }
	    factory.set("cpoint.type", 36, "CLOSE");
	    AnimationPath2D innerCircle = factory.createObject("innerCircle");


	    // Set length to the value we will use for a full circle as
	    // that is what we use in the animation
	    length = r * 2 * Math.PI;

	    double speed = length / 7.5;
	    int secs = 15;
	    int maxFrames = 25 * secs;
	    if (maxFrames != animation.estimateFrameCount(15.0)) {
		System.out.println("estimateFrameCount failed");
		System.exit(1);
	    }

	    int maxSimtime = 15000;

	    animation.initFrames(maxFrames, "ctmp/col-", "png");
	    animation.scheduleFrames(0, maxFrames);
	    File dir = new File("ctmp");
	    dir.mkdirs();
	    for (File file: dir.listFiles()) {
		file.delete();
	    }
	    obj.setPath(path, 0.0);
	    obj.setPathVelocity(speed);
	    obj.setPathAcceleration(0.0);

	    pobj.setPosition(r, 0.0, Math.PI/2.0);
	    pobj.setPath(path, 15.0/4.0);
	    pobj.setPathVelocity(speed);
	    pobj.setPathAcceleration(-2.0*speed/15.0);

	    animation.scheduleCall(new Callable() {
		    public void call() {
			animation.setBackgroundColor(new Color(0, 0, 0, 32));
			animation.setClearByFillMode(true);
		    }
		}, maxSimtime/2);
	    animation.run(maxSimtime);

	    // try again & write to a single file

	    final Animation2D a2d = new Animation2D();
	    a2d.setRanges(0.0, 0.0, 0.5, 0.5, 25.0, 25.0);
	    obj = new CircObject(a2d, "obj", true);
	    obj.setZorder(0, true);
	    ImageSequenceWriter isw = new ImageSequenceWriter("test.isq");
	    a2d.initFrames(maxFrames, "png", isw);
	    a2d.scheduleFrames(0, maxFrames);
	    obj.setPath(path, 0.0);
	    obj.setPathVelocity(speed);
	    obj.setPathAcceleration(0.0);
	    a2d.scheduleCall(new Callable() {
		    public void call() {
			a2d.setBackgroundColor(new Color(0, 0, 0, 32));
			a2d.setClearByFillMode(true);
		    }
		}, maxSimtime/2);

	    obj.printConfiguration();
	    obj.printState();

	    a2d.run(maxSimtime);
	    isw.close();

	    // write graphs to a directory but use a directory
	    // acessor.

	    System.out.println("trying to create datmp files");
	    final Animation2D a2d2 = new Animation2D();
	    dir = new File("datmp");
	    dir.mkdirs();
	    DirectoryAccessor da = new DirectoryAccessor("datmp");
	    /*
	    try {
		System.setSecurityManager(new SecurityManager());
	    } catch (UnsupportedOperationException eu) {}
	    */
	    a2d2.setRanges(0.0, 0.0, 0.5, 0.5, 25.0, 25.0);
	    obj = new CircObject(a2d2, "obj", true);
	    obj.setZorder(0, true);
	    a2d2.initFrames(maxFrames, "img-", "png", da);
	    a2d2.scheduleFrames(0, maxFrames);
	    obj.setPath(path, 0.0);
	    obj.setPathVelocity(speed);
	    obj.setPathAcceleration(0.0);
	    a2d2.scheduleCall(new Callable() {
		    public void call() {
			System.out.println("scheduledCall for background ran");
			a2d2.setBackgroundColor(new Color(0, 0, 0, 32));
			a2d2.setClearByFillMode(true);
		    }
		}, maxSimtime/2);
	    System.out.println("maxSimtime = " + maxSimtime);
	    a2d2.run(maxSimtime);
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	System.exit(0);
    }
}
