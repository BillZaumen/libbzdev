package org.bzdev.gio;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.IOException;
import java.util.Vector;

/**
 * Class to create a buffered image backed by an instance of 
 * {@link OutputStreamGraphics}. The method
 * {@link OSGBufferedImage#imageComplete()} must be called when
 * an image is complete: otherwise the image file may not be
 * completely written or may not be written at all.
 */
public class OSGBufferedImage extends BufferedImage implements OSGraphicsOps {

    OutputStreamGraphics osg;

    /**
     * Constructor.
     * @param osg the output-stream graphics used to output images
     */
    public OSGBufferedImage(OutputStreamGraphics osg) {
	super(osg.getWidth(), osg.getHeight(), osg.requestsAlpha()?
	      BufferedImage.TYPE_INT_ARGB: BufferedImage.TYPE_INT_RGB);
	this.osg = osg;

    }

    @Override
    public Graphics2D createGraphics() {
	return new
	    SplitterGraphics2D(super.createGraphics(), osg.createGraphics());
    }

    @Override
    public Graphics getGraphics() {
	return new
	    SplitterGraphics2D(super.createGraphics(), osg.createGraphics());
    }

    // Methods required by OSGraphicsOps

    @Override
    public boolean canReset() {
	return false;
    }

    @Override
    public void close() throws IOException {
	osg.close();
    }

    @Override
    public void flush() {
	super.flush();
	try {
	    osg.flush();
	} catch (IOException e) {
	    throw new RuntimeException(e);
	}
    }

    @Override
    public void imageComplete() throws IOException {
	osg.imageComplete();
    }

    @Override
    public boolean requestsAlpha() {
	return osg.requestsAlpha();
    }

    @Override
    public void reset() throws UnsupportedOperationException {
	throw new UnsupportedOperationException();
    }

}

