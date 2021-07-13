import org.bzdev.anim2d.*;
import org.bzdev.graphs.Graph;
import org.bzdev.lang.Callable;
import org.bzdev.geom.BasicSplinePath2D;
import org.bzdev.geom.Path2DInfo;
import org.bzdev.devqsim.SimFunction;

import java.awt.image.BufferedImage;
import java.awt.*;
import java.awt.geom.*;
import java.io.*;
import java.util.Formatter;


public class FactoryTest {

    public static void main(String argv[]) {
	try {
	    Animation2D animation = new Animation2D();
	    animation.setBackgroundColor
		(Color.GRAY.darker().darker());
	
	    AnimationLayer2DFactory alf =
		new AnimationLayer2DFactory(animation);

	    alf.set("zorder", 1);
	    alf.set("visible", true);
	    for (int j = 0; j < 400; j++) {
		int jj = j + 1;
		double x = j * 30.0;
		double y = 50.0;
		double width = 15.0;
		double height = 5.0;
		if (j%10 == 0) {
		    y = 45.0;
		}
		alf.set("object.type", jj, "RECTANGLE");
		alf.set("object.draw", jj, true);
		alf.set("object.fill", jj, true);
		alf.set("object.x", jj, x);
		alf.set("object.y", jj, y);
		alf.set("object.width", jj, width);
		alf.set("object.height", jj, height);
		if ((jj % 2) == 0) {
		    alf.set("object.drawColor.css", jj, "white");
		    alf.set("object.fillColor.css", jj, "white");
		} else {
		    alf.set("object.drawColor.red", jj, 255);
		    alf.set("object.drawColor.blue", jj, 255);
		    alf.set("object.drawColor.green", jj, 255);
		    alf.set("object.fillColor.red", jj, 255);
		    alf.set("object.fillColor.blue", jj, 255);
		    alf.set("object.fillColor.green", jj, 255);
		}
	    }

	    AnimationShape2DFactory shapef =
		new AnimationShape2DFactory(animation);

	    shapef.set("windingRule", "WIND_EVEN_ODD");
	    shapef.set("fillColor.css", "yellow");
	    shapef.set("drawColor.css", "black");
	    shapef.set("shape", 0,
		       new Rectangle2D.Double(10.0, 55.0, 20.0, 20.0));
	    shapef.set("shape", 1,
		       new Rectangle2D.Double(15.0, 60.0, 10.0, 10.0));

	    AnimationShape2D shape = shapef.createObject("shape");

	    alf.set("object.type", 402, "SHAPE");
	    alf.set("object.shape", 402, shape);

	    alf.createObject("background");

	    shapef.clear();
	    shapef.set("windingRule", "WIND_EVEN_ODD");
	    shapef.set("fillColor.css", "green");
	    shapef.set("drawColor.css", "black");
	    shapef.set("stroke.width", 4.0);
	    shapef.set("shape", 0,
		       new Rectangle2D.Double(40.0, 55.0, 20.0, 20.0));
	    shapef.set("shape", 1,
		       new Rectangle2D.Double(45.0, 60.0, 10.0, 10.0));
	    shapef.set("visible", true);
	    shapef.set("zorder", 10);
	    AnimationShape2D shape2 = shapef.createObject("shape2");

	    AnimationPath2DFactory pf = new AnimationPath2DFactory(animation);

	    pf.set("visible", true);

	    pf.set("cpoint.type", 0, "MOVE_TO");
	    pf.set("cpoint.x", 0, 0.0);
	    pf.set("cpoint.y", 0, 0.0);
	    pf.set("cpoint.type", 1, "SEG_END");
	    pf.set("cpoint.x", 1, 1200.0);
	    pf.set("cpoint.y", 1, 0.0);
	
	    AnimationPath2D path = pf.createObject("path");
	
	    pf.clear();

	    pf.set("cpoint.type", 0, "MOVE_TO");
	    pf.set("cpoint.x", 0, 70.0);
	    pf.set("cpoint.y", 0, 60.0);
	    pf.set("cpoint.type", 1, "SEG_END");
	    pf.set("cpoint.x", 1, 100.0);
	    pf.set("cpoint.y", 1, 60.0);
	    pf.set("stroke.width", 10.0);
	    pf.set("visible", true);
	    AnimationPath2D path2 = pf.createObject("path2");

	    pf.clear();
	    pf.set("cpoint.type", 0, "MOVE_TO");
	    pf.set("cpoint.x", 0, 120.0);
	    pf.set("cpoint.y", 0, 60.0);
	    pf.set("cpoint.type", 1, "SEG_END");
	    pf.set("cpoint.x", 1, 140.0);
	    pf.set("cpoint.y", 1, 60.0);
	    pf.set("cpoint.type", 2, "SEG_END");
	    pf.set("cpoint.x", 2, 130.0);
	    pf.set("cpoint.y", 2, 80.0);
	    pf.set("cpoint.type", 3, "CLOSE");
	    pf.set("visible", false);
	    AnimationPath2D path3a = pf.createObject("path3a");
	    pf.clear();
	    pf.set("cpoint.type", 0, "MOVE_TO");
	    pf.set("cpoint.x", 0, 127.0);
	    pf.set("cpoint.y", 0, 65.0);
	    pf.set("cpoint.type", 1, "SEG_END");
	    pf.set("cpoint.x", 1, 133.0);
	    pf.set("cpoint.y", 1, 65.0);
	    pf.set("cpoint.type", 2, "SEG_END");
	    pf.set("cpoint.x", 2, 130.0);
	    pf.set("cpoint.y", 2, 72.0);
	    pf.set("cpoint.type", 3, "CLOSE");
	    pf.set("visible", false);
	    AnimationPath2D path3b = pf.createObject("path3b");
	    shapef.clear();
	    shapef.set("windingRule", "WIND_EVEN_ODD");
	    shapef.set("fillColor.css", "blue");
	    shapef.set("drawColor.css", "black");
	    shapef.set("stroke.width", 5.0);
	    shapef.set("shape", 0, path3a);
	    shapef.set("shape", 1, path3b);
	    shapef.set("visible", true);
	    AnimationShape2D shape3 = shapef.createObject("shape3");

	    pf.clear();
	    pf.set("cpoint.type", 0, "MOVE_TO");
	    pf.set("cpoint.x", 0, 150.0);
	    pf.set("cpoint.y", 0, 60.0);
	    pf.set("cpoint.type", 1, "SEG_END");
	    pf.set("cpoint.x", 1, 170.0);
	    pf.set("cpoint.y", 1, 60.0);
	    pf.set("cpoint.type", 2, "SEG_END");
	    pf.set("cpoint.x", 2, 160.0);
	    pf.set("cpoint.y", 2, 80.0);
	    pf.set("cpoint.type", 3, "CLOSE");
	    pf.set("visible", false);
	    AnimationPath2D path4 = pf.createObject("path4");
	    shapef.clear();
	    shapef.set("windingRule", "WIND_EVEN_ODD");
	    shapef.set("fillColor.css", "white");
	    shapef.set("drawColor.css", "black");
	    shapef.set("stroke.width", 5.0);
	    shapef.set("shape", 0, path4.getPath().getPathIterator(null));
	    shapef.set("visible", true);
	    AnimationShape2D shape4 = shapef.createObject("shape4");

	    pf.clear();
	    pf.set("cpoint.type", 0, "MOVE_TO");
	    pf.set("cpoint.x", 0, 180.0);
	    pf.set("cpoint.y", 0, 60.0);
	    pf.set("cpoint.type", 1, "SEG_END");
	    pf.set("cpoint.x", 1, 200.0);
	    pf.set("cpoint.y", 1, 60.0);
	    pf.set("cpoint.type", 2, "SEG_END");
	    pf.set("cpoint.x", 2, 190.0);
	    pf.set("cpoint.y", 2, 80.0);
	    pf.set("cpoint.type", 3, "CLOSE");
	    pf.set("visible", false);
	    AnimationPath2D path5a = pf.createObject("path5a");
	    pf.clear();
	    pf.set("cpoint.type", 0, "MOVE_TO");
	    pf.set("cpoint.x", 0, 187.0);
	    pf.set("cpoint.y", 0, 65.0);
	    pf.set("cpoint.type", 1, "SEG_END");
	    pf.set("cpoint.x", 1, 193.0);
	    pf.set("cpoint.y", 1, 65.0);
	    pf.set("cpoint.type", 2, "SEG_END");
	    pf.set("cpoint.x", 2, 190.0);
	    pf.set("cpoint.y", 2, 72.0);
	    pf.set("cpoint.type", 3, "CLOSE");
	    pf.set("visible", false);
	    AnimationPath2D path5b = pf.createObject("path5b");
	    shapef.clear();
	    shapef.set("windingRule", "WIND_EVEN_ODD");
	    shapef.set("fillColor.css", "mediumorchid");
	    shapef.set("drawColor.css", "black");
	    shapef.set("stroke.width", 5.0);
	    shapef.set("shape", 0, path5a.getPath().getPathIterator(null));
	    shapef.set("shape", 1, path5b.getPath().getPathIterator(null));
	    shapef.set("visible", true);
	    AnimationShape2D shape5 = shapef.createObject("shape5");

	    GraphViewFactory gvf = new GraphViewFactory(animation);
	    gvf.set("initialX", 0.0);
	    gvf.set("initialY", 0.0);
	    gvf.set("xFrameFraction", 0.0);
	    gvf.set("yFrameFraction", 0.0);
	    gvf.set("scaleX", 1600.0/200.0);
	    gvf.set("scaleY", 1600.0/200.0);
	    gvf.set("zoom", 1.0);
	    gvf.set("timeline.time", 0, 0.0);
	    gvf.set("timeline.path", 0, path);
	    gvf.set("timeline.velocity", 0, 1200.0/30.0);

	    gvf.set("timeline.time", 1, 15.0);
	    gvf.set("timeline.zoomMode", 1, "SET_RATE");
	    gvf.set("timeline.zoomRate", 1, Math.log(1/2.0)/15.0);


	    gvf.set("timeline.time", 2, 31.0);
	    gvf.set("timeline.path", 2, animation.nullPath());

	    gvf.createObject("view");

	    int secs = 30;

	    animation.initFrames(25*secs, "ftmp/col-", "png");
	    animation.scheduleFrames(0, 25*secs);
	    File dir = new File("ftmp");
	    dir.mkdirs();
	    for (File file: dir.listFiles()) {
		file.delete();
	    }
	    animation.run();

	    SimFunction sf = animation.nullFunction();
	    System.out.println("sf.getFunction() = " + sf.getFunction()
			       +" (should be null)");


	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	System.exit(0);
    }
}
