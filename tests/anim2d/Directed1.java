import org.bzdev.anim2d.*;
import org.bzdev.geom.Path2DInfo;
import org.bzdev.geom.BasicSplinePath2D;
import org.bzdev.graphs.Graph;
import org.bzdev.util.units.MKS;
import java.awt.*;
import java.awt.geom.*;
import java.io.File;

public class Directed1 {

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
	    Animation2D a2d = new Animation2D
		(1474, 820, 10000.0, 200);

	    double scaleFactor = 820.0/MKS.feet(50.0*30.0);
	    a2d.setRanges(0.0, 0.0, 0.0, 0.0, scaleFactor, scaleFactor);

	    OurDirectedObject2D ko =
		new OurDirectedObject2D(a2d, "ko", true) {
		};
	    ko.setZorder(10, true);
	    BasicSplinePath2D path = new BasicSplinePath2D();
	    path.moveTo(MKS.feet(200.0), MKS.feet(123.0));
	    path.lineTo(MKS.feet(120.0), MKS.feet(93.0));
	    path.lineTo(MKS.feet(110.0), MKS.feet(90.0));
	    double xs[] = {MKS.feet(100.0), MKS.feet(90.0),
			   MKS.feet(80.0), MKS.feet(70.0)};
	    double ys[] = {MKS.feet(87.0), MKS.feet(84.0),
			   MKS.feet(90.0), MKS.feet(75.0)};
	    path.splineTo(xs, ys);
	    ko.setPath(path, 0.0, 0.0, true, 0.0);
	    ko.setPathVelocity(MKS.mph(15.0));
	    int maxFrames = a2d.estimateFrameCount(8.6);
	    a2d.initFrames(maxFrames, "dtmp1/col-", "png");
	    a2d.scheduleFrames(0L, maxFrames);
	    File dir = new File("dtmp1");
	    dir.mkdirs();
	    for (File file: dir.listFiles()) {
		file.delete();
	    }
	    a2d.run();
	    System.exit(0);
	} catch(Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
    }
}
