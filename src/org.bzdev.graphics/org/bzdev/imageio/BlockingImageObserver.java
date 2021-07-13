package org.bzdev.imageio;
import java.awt.Image;
import java.awt.image.*;
import java.io.IOException;

//@exbundle org.bzdev.imageio.lpack.ImageIO


/**
 * Class providing an image observer with a method that blocks until
 * an image, its height and/or width, or some properties of the image
 * are available.
 * <P>
 * Users should construct an instance of this object, pass it
 * to a method that expects an image observer and that retrieves an
 * image, and then call the method {@link #waitUntilDone()}, which
 * will block until the image is available, an error occurs, or
 * the thread is interrupted. Instances of BlockingImageObserver
 * must be used with only a single image.
 * <P>
 * An alternative is to use a media tracker. This class provides a
 * simpler API: the Java classes that take an image obsever as an
 * argument may return before the image is completely loaded.
 * An example of usage is the following:
 * <BLOCKQUOTE><CODE><PRE>
 *    Image image = ...;
 *    BlockingImageObserver bio =
 *         new BlockingImageObserver(true, true, true, true);
 *    int width = image.getWidth(bio);
 *    bio.waitUntilDone();
 *    // repeat in case the image was not there on the first
 *    // call to getWidth()
 *    width = image.getWidth(bio);
 *    int height = image.getHeight(bio);
 * </PRE></CODE></BLOCKQUOTE>
 * <P>
 * This class should not be used when blocking would noticeably impact
 * performance. That could be an issue if an image is downloaded over
 * a computer network, but much less so if the image is stored locally.
 * Blocking is also less likely to be an issue when a computation
 * is compute bound and the additional time due to blocking is
 * small compared to the CPU time.
 */
public class BlockingImageObserver implements ImageObserver {

    static String errorMsg(String key, Object... args) {
	return ImageScaler.errorMsg(key, args);
    }

    boolean couldNotLoad = false;
    boolean imageDone = false;
    boolean needHeight, needWidth, needProperties, needImage;
    /**
     * Constructor.
     * @param needWidth true if waitUntilDone() should return
     *        when the width of an image is available; false if the
     *        width is not needed
     * @param needHeight true if waitUntilDone() should return
     *        when the height of an image is available; false if the
     *        height is not needed
     * @param needProperties true if waitUntilDone() should return
     *        when the properties of an image are available; false if the
     *        properties are not needed
     * @param needImage  true if waitUntilDone() should return
     *        when the complete image is available; false if the
     *        image itself is not needed
     */
    public BlockingImageObserver(boolean needWidth, boolean needHeight,
				 boolean needProperties,
				 boolean needImage)
    {
	this.needWidth = needWidth;
	this.needHeight = needHeight;
	this.needProperties = needProperties;
	this.needImage = needImage;
    }


    /**
     * This method is called when information about an image which was
     * previously requested using an asynchronous interface becomes
     * available.  Asynchronous interfaces are method calls such as
     * getWidth(ImageObserver) and drawImage(img, x, y, ImageObserver)
     * which take an ImageObserver object as an argument.  Those methods
     * register the caller as interested either in information about
     * the overall image itself (in the case of getWidth(ImageObserver))
     * or about an output version of an image (in the case of the
     * drawImage(img, x, y, [w, h,] ImageObserver) call).
     *
     * <p>This method
     * should return true if further updates are needed or false if the
     * required information has been acquired.  The image which was being
     * tracked is passed in using the img argument.  Various constants
     * are combined to form the infoflags argument which indicates what
     * information about the image is now available.  The interpretation
     * of the x, y, width, and height arguments depends on the contents
     * of the infoflags argument.
     * <p>
     * The {@code infoflags} argument should be the bitwise inclusive
     * <b>OR</b> of the following flags: {@code WIDTH},
     * {@code HEIGHT}, {@code PROPERTIES}, {@code SOMEBITS},
     * {@code FRAMEBITS}, {@code ALLBITS}, {@code ERROR},
     * {@code ABORT}.
     *
     * @param     img   the image being observed.
     * @param     infoflags   the bitwise inclusive OR of the following
     *               flags:  {@code WIDTH}, {@code HEIGHT},
     *               {@code PROPERTIES}, {@code SOMEBITS},
     *               {@code FRAMEBITS}, {@code ALLBITS},
     *               {@code ERROR}, {@code ABORT}.
     * @param     x   the <i>x</i> coordinate.
     * @param     y   the <i>y</i> coordinate.
     * @param     width    the width.
     * @param     height   the height.
     * @return    {@code false} if the infoflags indicate that the
     *            image is completely loaded; {@code true} otherwise.
     * @see #WIDTH
     * @see #HEIGHT
     * @see #PROPERTIES
     * @see #SOMEBITS
     * @see #FRAMEBITS
     * @see #ALLBITS
     * @see #ERROR
     * @see #ABORT
     * @see Image#getWidth
     * @see Image#getHeight
     * @see java.awt.Graphics#drawImage
     */
    @Override
    public synchronized boolean imageUpdate(Image img, int infoflags,
					    int x, int y, int width, int height)
    {
	int mask = 0;
	if (needWidth) mask |= ImageObserver.WIDTH;
	if (needHeight) mask |= ImageObserver.HEIGHT;
	if (needProperties) mask |= ImageObserver.PROPERTIES;
	if (needImage) mask |= ImageObserver.ALLBITS;

	int emask = ImageObserver.ABORT | ImageObserver.ERROR;
	couldNotLoad = ((infoflags & emask) != 0);
	imageDone =  ((infoflags & mask) == mask);
	if (imageDone && !couldNotLoad) {
	    notify();
	    return false;
	} else if (couldNotLoad) {
	    imageDone = true;
	    notify();
	    return false;
	} else {
	    return true;
	}
	/*
	boolean result = (!imageDone) || couldNotLoad;
	if (!result) notify();
	return result;
	*/
    }

    /**
     * Wait until this image observer has been notified that all
     * the requested quantities are available.
     * @exception IOException - if an IO error has occurred
     * @exception InterruptedException - if some thread has interrupted
     *            the current thread before or while this method was
     *            waiting for a notification (the interrupted status
     *            of the current thread will be cleared when this
     *            exception is thrown)
     */
    public synchronized void waitUntilDone()
	throws IOException, InterruptedException
    {
	while (!imageDone) {
	    // System.out.println("waiting");
	    wait();
	}
	if (couldNotLoad) {
	    throw new IOException(errorMsg("cannotLoadImage"));
	}
	return;
    }
}

//  LocalWords:  waitUntilDone BlockingImageObserver ImageObserver
//  LocalWords:  exbundle needWidth needHeight needProperties
//  LocalWords:  needImage IOException InterruptedException
//  LocalWords:  cannotLoadImage
