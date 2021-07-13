import org.bzdev.anim2d.*;
import org.bzdev.gio.ImageSequenceWriter;
import org.bzdev.graphs.Graph;

import java.io.File;
import java.awt.Color;

public class GridTest {

    public static void main(String argv[]) {
	try {
	    Animation2D animation = new Animation2D(1.0, 1);

	    CartesianGrid2DFactory gridf =
		new CartesianGrid2DFactory(animation);

	    double udist = (animation.getWidth() > animation.getHeight())?
		animation.getHeight(): animation.getWidth();
	    double scalef = udist/100.0;

	    animation.setRanges(0.0, 0.0, 0.5, 0.5, scalef, scalef);
	    animation.setBackgroundColor(Color.WHITE);

	    gridf.set("spacing", 10);
	    gridf.set("subspacing", 5);

	    gridf.createObject("grid");

	    int maxFrames = 5;
	    animation.initFrames(maxFrames, "gtmp/col-", "png");
	    animation.scheduleFrames(0, maxFrames);
	    File dir = new File("gtmp");
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
