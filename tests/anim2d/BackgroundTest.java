import org.bzdev.anim2d.*;
import org.bzdev.gio.ImageSequenceWriter;
import org.bzdev.graphs.Graph;

import java.io.File;
import java.awt.Color;

public class BackgroundTest {

    public static void main(String argv[]) {
	try {
	    Animation2D animation = new Animation2D(1.0, 1);

	    AnimationLayer2DFactory bof =
		new AnimationLayer2DFactory(animation);

	    animation.setRanges(0.0, 0.0, 0.5, 0.5, 25.0, 25.0);
	    animation.setBackgroundColor(Color.WHITE);

	    bof.set("zorder", 1);
	    bof.set("visible", true);

	    bof.set("fillColor.green", 255);

	    bof.set("object.type", 1, "RECTANGLE");
	    bof.set("object.x", 1, 0.0);
	    bof.set("object.y", 1, 0.0);
	    bof.set("object.height", 1, 5.0);
	    bof.set("object.width", 1, 10.0);
	    bof.set("object.refPoint", 1, "CENTER");
	    bof.set("object.draw", 1, true);
	    bof.set("object.fill", 1, true);

	    bof.set("object.type", 2, "ELLIPSE");
	    bof.set("object.x", 2, 0.0);
	    bof.set("object.y", 2, 0.0);
	    bof.set("object.height", 2, 5.0);
	    bof.set("object.width", 2, 10.0);
	    bof.set("object.refPoint", 2, "CENTER");
	    bof.set("object.fillColor.blue", 2, 255);
	    bof.set("object.draw", 2, true);
	    bof.set("object.fill", 2, true);

	    double f = 2.0;
	    bof.set("object.type", 3, "ELLIPSE");
	    bof.set("object.x", 3, 0.0);
	    bof.set("object.y", 3, 0.0);
	    bof.set("object.height", 3, 5.0 * f);
	    bof.set("object.width", 3, 10.0 * f);
	    bof.set("object.refPoint", 3, "CENTER");
	    bof.set("object.fillColor.blue", 3, 255);
	    bof.set("object.draw", 3, true);
	    bof.set("object.stroke.width", 3, "1.5");
	    bof.set("object.stroke.gcsMode", 3, true);

	    bof.set("object.type", 4, "LINE");
	    bof.set("object.x", 4, -1.5);
	    bof.set("object.y", 4, 0.0);
	    bof.set("object.xend", 4, 1.5);
	    bof.set("object.yend", 4, 0.0);
	    bof.set("object.draw", 4, true);
	    bof.set("object.stroke.width", 4, "1.0");
	    bof.set("object.stroke.gcsMode", 4, true);
	    
	    bof.set("object.type", 5, "LINE");
	    bof.set("object.x", 5, 0.0);
	    bof.set("object.y", 5, -1.5);
	    bof.set("object.xend", 5, 0.0);
	    bof.set("object.yend", 5, 1.5);
	    bof.set("object.draw", 5, true);
	    bof.set("object.stroke.width", 5, "3.0");


	    int key = 6;

	    bof.set("object.type", key, "RECTANGLE");
	    bof.set("object.x", key, -20.0);
	    bof.set("object.y", key, 10.0);
	    bof.set("object.height", key, 10.0);
	    bof.set("object.width", key, 10.0);
	    bof.set("object.refPoint", key, "CENTER");
	    bof.set("object.stroke.width", key, "3.0");
	    bof.set("object.draw", key, true);

	    key++;
	    bof.set("object.type", key, "ROUND_RECTANGLE");
	    bof.set("object.x", key, -20.0);
	    bof.set("object.y", key, 10.0);
	    bof.set("object.height", key, 5.0);
	    bof.set("object.width", key, 5.0);
	    bof.set("object.arcwidth", key, 3.0);
	    bof.set("object.archeight", key, 1.5);
	    bof.set("object.refPoint", key, "CENTER");
	    bof.set("object.stroke.width", key, "3.0");
	    bof.set("object.draw", key, true);


	    key++;
	    bof.set("object.type", key, "ARC_CHORD");
	    bof.set("object.x", key, -20.0);
	    bof.set("object.y", key, 10.0);
	    bof.set("object.height", key, 10.0);
	    bof.set("object.width", key, 10.0);
	    bof.set("object.extent", key, 60.0);
	    bof.set("object.start", key, 30.0);
	    bof.set("object.refPoint", key, "CENTER");
	    bof.set("object.fillColor.blue", key, 255);
	    bof.set("object.stroke.width", key, "3.0");
	    bof.set("object.draw", key, true);
	    bof.set("object.fill", key, true);

	    key++;
	    bof.set("object.type", key, "ARC_OPEN");
	    bof.set("object.x", key, -10.0);
	    bof.set("object.y", key, 10.0);
	    bof.set("object.height", key, 10.0);
	    bof.set("object.width", key, 10.0);
	    bof.set("object.extent", key, 90.0);
	    bof.set("object.start", key, 30.0);
	    bof.set("object.refPoint", key, "CENTER");
	    bof.set("object.fillColor.blue", key, 255);
	    bof.set("object.stroke.width", key, "3.0");
	    bof.set("object.draw", key, true);
	    bof.set("object.fill", key, true);

	    key++;
	    bof.set("object.type", key, "ARC_PIE");
	    bof.set("object.x", key, -0.0);
	    bof.set("object.y", key, 10.0);
	    bof.set("object.height", key, 10.0);
	    bof.set("object.width", key, 10.0);
	    bof.set("object.extent", key, 90.0);
	    bof.set("object.start", key, 30.0);
	    bof.set("object.refPoint", key, "CENTER");
	    bof.set("object.fillColor.blue", key, 255);
	    bof.set("object.stroke.width", key, "3.0");
	    bof.set("object.draw", key, true);
	    bof.set("object.fill", key, true);

	    key++;
	    bof.set("object.type", key, "QUAD_CURVE");
	    bof.set("object.x", key, -25.0);
	    bof.set("object.y", key, -10.0);
	    bof.set("object.xcontrol", key, -15.0);
	    bof.set("object.ycontrol", key, -10.0);
	    bof.set("object.xend", key, -15.0);
	    bof.set("object.yend", key,  0.0);
	    bof.set("object.draw", key, true);
	    bof.set("object.stroke.width", key, "3.0");


	    key++;
	    bof.set("object.type", key, "CUBIC_CURVE");
	    bof.set("object.x", key, -25.0);
	    bof.set("object.y", key, -10.0);
	    bof.set("object.xcontrol1", key, -25.0);
	    bof.set("object.ycontrol1", key, -5.0);
	    bof.set("object.xcontrol2", key, -20.0);
	    bof.set("object.ycontrol2", key, 0.0);
	    bof.set("object.xend", key, -15.0);
	    bof.set("object.yend", key,  0.0);
	    bof.set("object.draw", key, true);
	    bof.set("object.stroke.width", key, "3.0");
	    bof.set("object.drawColor.blue", key, 255);

	    key++;
	    bof.set("object.type", key, "PATH_START");
	    key++;
	    bof.set("object.type", key, "MOVE_TO");
	    bof.set("object.x", key, 10.0);
	    bof.set("object.y", key, -10.0);
	    key++;
	    bof.set("object.type", key, "SEG_END");
	    bof.set("object.x", key, 20.0);
	    bof.set("object.y", key, -10.0);
	    key++;
	    bof.set("object.type", key, "CONTROL_POINT");
	    bof.set("object.x", key, 20.0);
	    bof.set("object.y", key, -5.0);
	    key++;
	    bof.set("object.type", key, "SEG_END");
	    bof.set("object.x", key, 15.0);
	    bof.set("object.y", key, -5.0);
	    key++;
	    bof.set("object.type", key, "CONTROL_POINT");
	    bof.set("object.x", key, 15.0);
	    bof.set("object.y", key, 0.0);
	    key++;
	    bof.set("object.type", key, "CONTROL_POINT");
	    bof.set("object.x", key, 15.0);
	    bof.set("object.y", key, 0.0);
	    key++;
	    bof.set("object.type", key, "SEG_END");
	    bof.set("object.x", key, 20.0);
	    bof.set("object.y", key, 0.0);

	    double r = 5.0;
	    for(int i = 10; i < 90; i += 10) {
		double theta = Math.toRadians((double)i);
		double x = 15.0 + r * Math.cos(theta);
		double y = r * Math.sin(theta);
		key++;
		bof.set("object.type", key, "SPLINE_POINT");
		bof.set("object.x", key, x);
		bof.set("object.y", key, y);
	    }
	    
	    key++;
	    bof.set("object.type", key, "SEG_END");
	    bof.set("object.x", key, 15.0);
	    bof.set("object.y", key, 5.0);
	    
	    key++;
	    bof.set("object.type", key, "SEG_CLOSE");

	    key++;
	    bof.set("object.type", key, "PATH_END");
	    bof.set("object.draw", key, true);
	    bof.set("object.stroke.width", key, "4.0");
	    bof.set("object.fill", key, true);
	    bof.set("object.fillColor.green", key, 255);


	    key++;
	    bof.set("object.type", key, "PATH_START");
	    key++;
	    bof.set("object.type", key, "MOVE_TO");
	    bof.set("object.x", key, 20.0);
	    bof.set("object.y", key, 11.0);

	    for(int i = 10; i < 360; i += 10) {
		double theta = Math.toRadians((double)i);
		double x = 15.0 + r * Math.cos(theta);
		double y = 11.0 + r * Math.sin(theta);
		key++;
		bof.set("object.type", key, "SPLINE_POINT");
		bof.set("object.x", key, x);
		bof.set("object.y", key, y);
	    }
	    key++;
	    bof.set("object.type", key, "SEG_CLOSE");

	    key++;
	    bof.set("object.type", key, "PATH_END");
	    bof.set("object.draw", key, true);
	    bof.set("object.stroke.width", key, "4.0");
	    bof.set("object.fill", key, true);
	    bof.set("object.fillColor.blue", key, 255);
	    
	    key++;
	    bof.set("object.type", key, "PATH_START");
	    key++;
	    bof.set("object.type", key, "MOVE_TO");
	    bof.set("object.x", key, -20.0);
	    bof.set("object.y", key, -15.0);
	    for (int i = 1; i < 10; i++) {
		double x = -20.0 + 2.0 * i;
		double theta = Math.toRadians(18.0 * i);
		double y = -12.5 - 2.5 * Math.cos(theta);
		key++;
		bof.set("object.type", key, "SPLINE_POINT");
		bof.set("object.x", key, x);
		bof.set("object.y", key, y);
	    }
	    key++;
	    bof.set("object.type", key, "SEG_END");
	    bof.set("object.x", key, 0.0);
	    bof.set("object.y", key, -10.0);
	    key++;
	    bof.set("object.type", key, "PATH_END");
	    bof.set("object.draw", key, true);
	    bof.set("object.drawColor.css", key, "blue");
	    bof.set("object.stroke.width", key, "1.0");
	    bof.set("object.stroke.dashIncrement", key, 1.0);
	    bof.set("object.stroke.dashPattern", key, "-- ");
	    bof.set("object.stroke.gcsMode", key, true);
	    
	    key++;
	    bof.set("object.type", key, "PATH_START");
	    key++;
	    bof.set("object.type", key, "MOVE_TO");
	    bof.set("object.x", key, -20.0);
	    bof.set("object.y", key, -10.0);
	    key++;
	    bof.set("object.type", key, "SEG_END");
	    bof.set("object.x", key,  0.0);
	    bof.set("object.y", key, -10.0);
	    key++;
	    bof.set("object.type", key, "PATH_END");
	    bof.set("object.draw", key, true);
	    bof.set("object.drawColor.css", key, "green");
	    bof.set("object.stroke.width", key, "4.0");
	    bof.set("object.stroke.gcsMode", key, false);

	    // test the timeline
	    bof.set("timeline.time", 1, 4.0);
	    bof.set("timeline.visible", 1, false);


	    AnimationObject2D bg = bof.createObject("background");
	    bg.printConfiguration();
	    bg.printState();
	    if (argv.length > 0 && argv[0].equals("print")) {
		System.out.println("... now try printing with indentation");
		System.out.println("  ... configuration");
		bg.printConfiguration("    ");
		System.out.println("  ... state");
		bg.printState("    ");
		System.exit(0);
	    }

	    int maxFrames = 5;
	    animation.initFrames(maxFrames, "btmp/col-", "png");
	    animation.scheduleFrames(0, maxFrames);
	    File dir = new File("btmp");
	    dir.mkdirs();
	    for (File file: dir.listFiles()) {
		file.delete();
	    }
	    animation.run(10);

	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	System.exit(0);
    }
}
