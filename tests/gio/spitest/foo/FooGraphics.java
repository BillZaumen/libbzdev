package foo;

import org.bzdev.gio.*;
import java.io.*;
import java.awt.Graphics2D;
import java.awt.image.ColorModel;

public class FooGraphics extends OutputStreamGraphics {

    OutputStreamGraphics osg;

    public FooGraphics(OutputStream os, int width, int height,
		       ImageOrientation orientation, String type,
		       boolean requestAlpha) {
	super(os, width, height, orientation, type, false);
	osg = OutputStreamGraphics.newInstance(os, width, height, orientation,
					       "jpeg",
					       requestAlpha);
    }

    public static Integer getDefaultWidth(ImageOrientation orientation) {
	return 800;
    }

    public static Integer getDefaultHeight(ImageOrientation orientation) {
	return 600;
    }

    public ColorModel getColorModel() {
	return osg.getColorModel();
    }
    
    public Graphics2D createGraphics() throws UnsupportedOperationException {
	return osg.createGraphics();
    }
    
    public void imageComplete() throws IOException {osg.imageComplete();}
}
