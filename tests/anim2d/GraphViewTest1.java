import org.bzdev.anim2d.*;
import org.bzdev.graphs.Graph;
import org.bzdev.lang.Callable;
import org.bzdev.geom.BasicSplinePath2D;
import org.bzdev.util.units.MKS;

import java.io.File;

// This test was created based on a bug in which, when the graph view
// is created after a background image, the background image shifts
// between the first frame and the second frame relative to a path,
// which seems to stay in the same place on the screen.  The position
// of the graph view at t=0 is probably different from what the call
// to setRanges would imply, but the graph view is scheduled first due
// to its zorder, and calls setRanges, so tha graph view should configure
// the graph for a frame before any other objects are drawn.
// 
// The bug was due to an error in a comparator method.  Originally it
// used (x-y) to determine if x < y or x > y, but that test doesn't work 
// for a graph view because its zorder is the smallest allowable negative
// long integer. A second cause was due to setting the zorder in a
// GraphView constructor, but providing a different value when a factory
// configures the graph view.

// Another bug that was fixed involved a requirement for some factory
// parameters that, if one provides either x, y, or an angle, one has
// to provide all three.  For most objects, that is fine, but graph views
// do not have angles (at least, not at present). A method was added
// to supress the angle test for cases where an angle is not wanted.
// The result was that providing some parameters for factories didn't work.
// The code below includes a test for this case.

public class GraphViewTest1 {
    public static void main(String argv[]) throws Exception {

	System.setProperty("org.bzdev.protocols.resource.path",
			   "images");
	org.bzdev.protocols.Handlers.enable();

	Animation2D a2d = new Animation2D(1474, 820, 10000.0, 200);
	// mimic hook2.js script as far as possible
	GraphViewFactory gvf = (GraphViewFactory)
	    a2d.createFactory(GraphViewFactory.class);
	AnimationPath2DFactory pathf = (AnimationPath2DFactory)
	    a2d.createFactory(AnimationPath2DFactory.class);
	AnimationLayer2DFactory alf = (AnimationLayer2DFactory)
	    a2d.createFactory(AnimationLayer2DFactory.class);
	
	double scaleFactor = 820.0 / MKS.feet(5.0*30);
	System.out.println("set ranges called: scalefactor = " + scaleFactor);
	a2d.setRanges(0.0, 0.0, 0.0, 0.0, scaleFactor, scaleFactor);

	Graph graph = a2d.getGraph();

	double iscale = 0.077;

	pathf.set("zorder", 10);
	pathf.set("visible", true);
	pathf.set("color.blue", 255);
	pathf.set("color.green", 0);
	pathf.set("color.red", 0);
	pathf.set("stroke.width", 2.0);
	pathf.set("stroke.gcsMode", false);
	pathf.set("cpoint.type", 0, "MOVE_TO");
	pathf.set("cpoint.x", 0, MKS.feet(200.0));
	pathf.set("cpoint.y", 0, MKS.feet(115.0));
	pathf.set("cpoint.type", 1, "SEG_END");
	pathf.set("cpoint.x", 1, MKS.feet(200.0));
	pathf.set("cpoint.y", 1, MKS.feet(115.0));
	pathf.set("cpoint.type", 2, "SEG_END");
	pathf.set("cpoint.x", 2, MKS.feet(115.0));
	pathf.set("cpoint.y", 2, MKS.feet(61.0));
	pathf.set("cpoint.type", 3, "CONTROL");
	pathf.set("cpoint.x", 3, MKS.feet(102.0));
	pathf.set("cpoint.y", 3, MKS.feet(52.0));
	pathf.set("cpoint.type", 4, "SEG_END");
	pathf.set("cpoint.x", 4, MKS.feet(93.0));
	pathf.set("cpoint.y", 4, MKS.feet(61.0));
	pathf.set("cpoint.type", 5, "SEG_END");
	pathf.set("cpoint.x", 5, MKS.feet(20.0));
	pathf.set("cpoint.y", 5, MKS.feet(120.0));
	pathf.set("showSegments", true);

	AnimationPath2D path = pathf.createObject("path");
	pathf.clear();

	alf.set("visible", true);
	alf.set("zorder", 0);
	alf.set("object.type", 0, "IMAGE");
	alf.set("object.imageURL", 0, "resource:background.png");
	alf.set("object.refPoint", 0, "LOWER_LEFT");
	alf.set("object.x", 0, 0.0);
	alf.set("object.y", 0, -0.1);
	alf.set("object.imageScaleX", 0, iscale);
	alf.set("object.imageScaleY", 0, iscale);

	AnimationLayer2D  layer = alf.createObject("background");
	System.out.println("layer.isVisible() = " + layer.isVisible());

	pathf.set("zorder", 10);
	pathf.set("visible", false);
	pathf.set("color.blue", 255);
	pathf.set("color.green", 0);
	pathf.set("color.red", 0);
	pathf.set("stroke.width", 2.0);
	pathf.set("stroke.gcsMode", false);
	pathf.set("cpoint.type", 0, "MOVE_TO");
	pathf.set("cpoint.x", 0, MKS.feet(90.0));
	pathf.set("cpoint.y", 0, MKS.feet(60.0));
	pathf.set("cpoint.type", 1, "SEG_END");
	pathf.set("cpoint.x", 1, MKS.feet(90.0));
	pathf.set("cpoint.y", 1, MKS.feet(60.0));

	AnimationPath2D vpath = pathf.createObject("fixed");

	gvf.set("xFrameFraction", 0.25);
	gvf.set("yFrameFraction", 0.25);
	gvf.set("scaleX", scaleFactor);
	gvf.set("scaleY", scaleFactor);
	gvf.set("zoom", 1.0);
	gvf.set("initialX", MKS.feet(90.0));
	gvf.set("initialY", MKS.feet(60.0));
	gvf.set("timeline.time", 0, 0.25);
	gvf.set("timeline.x", 0, MKS.feet(95.0));
	gvf.set("timeline.y", 0, MKS.feet(65.0));

	GraphView gv = gvf.createObject("view");

	File dir = new File("gvt1");
	dir.mkdirs();
	for (File file: dir.listFiles()) {
	    file.delete();
	}

	int maxFrames = a2d.estimateFrameCount(0.3);
	a2d.initFrames(maxFrames, "gvt1/col-", "png");
	a2d.scheduleFrames(0L, maxFrames);
	System.out.println("running animation ...");
	a2d.run();
	System.out.println("... done");
	System.exit(0);
    }
}
