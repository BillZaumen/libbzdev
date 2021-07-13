import org.bzdev.anim2d.*;
import org.bzdev.gio.ImageSequenceWriter;
import org.bzdev.graphs.Graph;

import java.io.File;
import java.awt.Color;

/*
 * We found a bug where a horizontal line created as a path
 * could not be
 * printed in an Animation2DLayer constructed using a
 * factory. This is a test for this specific case.
 */

public class BackgroundTest2 {

    public static void main(String argv[]) {
	try {
	    Animation2D animation = new Animation2D(1.0, 1);

	    AnimationLayer2DFactory bof =
		new AnimationLayer2DFactory(animation);

	    animation.setRanges(0.0, 0.0, 0.5, 0.5, 25.0, 25.0);
	    animation.setBackgroundColor(Color.WHITE);

	    bof.set("zorder", 1);
	    bof.set("visible", true);

	    int key = 0;
	    
	    bof.set("object.type", key, "LINE");
	    bof.set("object.x", key, 0.0);
	    bof.set("object.y", key, -1.5);
	    bof.set("object.xend", key, 0.0);
	    bof.set("object.yend", key, 1.5);
	    bof.set("object.draw", key, true);
	    bof.set("object.drawColor.css", key, "black");
	    bof.set("object.stroke.width", key, "3.0");

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
