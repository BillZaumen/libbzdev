package org.bzdev.gio;

import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.font.*;
import java.awt.image.*;
import java.awt.image.renderable.*;
import java.text.AttributedCharacterIterator;

import org.bzdev.lang.Callable;
import org.bzdev.util.Cloner;

//@exbundle org.bzdev.gio.lpack.Gio

/**
 * Class for creating Graphics2D instances that can be replayed.
 * <P>
 * While in most cases, instances of this class will just "do the
 * right thing", one should note that the Graphics2D methods
 * <UL>
 *  <LI> getFontRenderContext()
 *  <LI> getDeviceConfiguration()
 *  <LI> toString()
 * </UL>
 * may return values that are are different from the return values for
 * the initial graphics content and one passed to
 * {@link Graphics2DRecorder#playback(Graphics2D)}.  One should avoid
 * using these methods (except for the toString() method for diagnostics
 * or debugging). By contrast, the methods
 * <UL>
 *  <LI> getBackground()
 *  <LI> getComposite()
 *  <LI> getPaint()
 *  <LI> getRenderingHint(RenderingHints.Key)
 *  <LI> getRenderingHints()
 *  <LI> getStroke()
 *  <LI> getTransform()
 *  <LI> hit()
 *  <LI> getClip()
 *  <LI> getClipBounds()
 *  <LI> getFont()
 *  <LI> getFontMetrics()
 *  <LI> hitClip()
 * </UL>
 * may return values that are are different from the return values for
 * the initial graphics content and one passed to
 * {@link Graphics2DRecorder#playback(Graphics2D)}, but only if the
 * corresponding 'set' methods have not been called. In other words,
 * to get a desired background color, one should explicitly call
 * {@link Graphics2D#setBackground(Color)} and not depend on a default
 * value. Similarly, before calling {@link Graphics2D#getFontMetrics()},
 * one should call {@link Graphics2D#setFont(Font)}.
 */
public class Graphics2DRecorder implements GraphicsCreator, Cloneable {
    Graphics2D g2dBase;
    AffineTransform af;
    AffineTransform invaf;

    static String errorMsg(String key, Object... args) {
	return OutputStreamGraphics.errorMsg(key, args);
    }

    /**
     * Constructor.
     * @param g2d the Graphics2D used by createGraphics to
     *        create multiple Graphics 2D graphics contexts
     */
    public Graphics2DRecorder(Graphics2D g2d) {
	g2dBase = g2d;
	af = makeClone(g2d.getTransform());
	try {
	    invaf = af.isIdentity()? af: af.createInverse();
	} catch (NoninvertibleTransformException e) {
	    String msg = errorMsg("cannotCreateNI");
	    throw new IllegalStateException(msg, e);
	}
    }

    private <T> T makeClone(T obj) {
	try {
	    return Cloner.makeClone(obj);
	} catch (Exception e) {
	    String msg = errorMsg("cannotClone");
	    throw new UnsupportedOperationException(msg, e);
	}
    }

    java.util.List<Callable> methodCallList = new LinkedList<Callable>();
    HashMap<Integer, RecordingGraphics2D> map = new HashMap<>();
    int lastIndex = -1;

    /*
    @Override
    public Object clone() throws CloneNotSupportedException {
	Graphics2DRecorder theClone = (Graphics2DRecorder) super.clone();
	theClone.methodCallList = new LinkedList<Callable>(methodCallList);
	theClone.map = new HashMap<Integer,RecordingGraphics2D>(map);
	return theClone;
    }
    */

    /**
     * Reset the recorder.
     * It is the caller's responsibility to make sure that all
     * graphics contexts previously created by the recorder are
     * not used subsequently.
     */
    public void reset() {
	methodCallList.clear();
	map.clear();
	lastIndex = -1;
    }

    class RecordingGraphics2D extends Graphics2D  {
	Graphics2D g2d;
	int index;
	RecordingGraphics2D(Graphics2D g2d) {
	    this.g2d = g2d;
	    index = lastIndex;
	    map.put(index, this);
	}

	public void addRenderingHints(Map<?,?> hints) {
	    final HashMap<?,?> ourhints = new HashMap<>(hints);
	    synchronized (Graphics2DRecorder.this) {
		methodCallList.add(new Callable() {
			public void call() {
			    g2d.addRenderingHints(ourhints);
			}
		    });
	    }
	    g2d.addRenderingHints(hints);
	}

	private <T> T makeClone(T obj) {
	    try {
		return Cloner.makeClone(obj);
	    } catch (Exception e) {
		String msg = errorMsg("cannotClone");
		throw new UnsupportedOperationException(msg, e);
	    }
	}

	class ClonedShape implements Shape {
	    Path2D.Double path;
	    Rectangle rect;
	    Rectangle2D rect2d;
	    ClonedShape(Shape shape) {
		rect = shape.getBounds();
		rect2d = shape.getBounds2D();
		path = new Path2D.Double(shape);
	    }
	    @Override
	    public boolean contains(double x, double y) {
		return path.contains(x, y);
	    }
	    @Override
	    public boolean contains(double x, double y, double w, double h) {
		return path.contains(x, y, w, h);
	    }
	    @Override
	    public boolean contains(Point2D p) {return path.contains(p);}
	    @Override
	    public boolean contains(Rectangle2D r) {
		return path.contains(r);
	    }

	    @Override
	    public Rectangle getBounds() {
		try {
		return Cloner.makeClone(rect);
		} catch (Exception e) {
		    return rect;
		}
	    }
	    @Override
	    public Rectangle2D getBounds2D() {
		try {
		    return Cloner.makeClone(rect2d);
		} catch (Exception e) {
		    return rect2d;
		}
	    }
	    @Override
	    public PathIterator getPathIterator(AffineTransform at) {
		return path.getPathIterator(at);
	    }
	    @Override
	    public PathIterator getPathIterator(AffineTransform at,
						double flatness)
	    {
		return path.getPathIterator(at, flatness);
	    }
	    @Override
	    public boolean intersects(double x, double y, double w, double h) {
		return path.intersects(x, y, w, h);
	    }
	    @Override
	    public boolean intersects(Rectangle2D r) {
		return rect2d.intersects(r);
	    }
	}

	private Shape makeShapeClone(Shape object) {
	    if (object instanceof ClonedShape) {
		// We can just copy it - there are no methods to change
		// a ClonedShape after it is constructed.
		return (Shape) object;
	    }
	    try {
		return Cloner.makeCastedClone(Shape.class, object);
	    } catch (Exception e) {
		// After Java 8, they added restrictions on the use
		// of reflection to clone objects. This is fall-back
		// where we construct an object that tries to reproduce
		// a shape as accurately as possible. We don't want to
		// just use the original object because the caller may
		// change it before we replay.
		return new ClonedShape(object);
	    }
	}

	/*
	private <C,T extends C> C makeCastedClone(Class<C> clazz, T object) {
	    try {
		return Cloner.makeCastedClone(clazz, object);
	    }  catch (Exception e) {
		String msg = errorMsg("cannotClone");
		throw new UnsupportedOperationException(msg, e);
	    }
	}
	*/

	public void clip(Shape s) {
	    final Shape fs = makeShapeClone(s);
	    synchronized (Graphics2DRecorder.this) {
		methodCallList.add(new Callable() {
			public void call() {
			    g2d.clip(fs);
			}
		    });
	    }
	    g2d.clip(s);
	}
	
	public void draw(Shape s) {
	    final Shape fs = makeShapeClone(s);
	    synchronized (Graphics2DRecorder.this) {
		methodCallList.add(new Callable() {
			public void call() {
			    g2d.draw(fs);
			}
		    });
	    }
	    g2d.draw(s);
	}

	public void drawGlyphVector(GlyphVector g,
				    final float x, final float y)
	{
	    final GlyphVector fg = makeClone(g);
	    synchronized (Graphics2DRecorder.this) {
		methodCallList.add(new Callable() {
			public void call() {
			    g2d.drawGlyphVector(fg, x, y);
			}
		    });
	    }
	    g2d.drawGlyphVector(g, x, y);
	}

	public FontRenderContext getFontRenderContext() {
	    return g2d.getFontRenderContext();
	}
	
	public void drawImage(final BufferedImage img, 
			      final BufferedImageOp op,
			      final int x, final int y)
	{
	    synchronized (Graphics2DRecorder.this) {
		methodCallList.add(new Callable() {
			public void call() {
			    g2d.drawImage(img, op, x, y);
			}
		    });
	    }
	    g2d.drawImage(img, op, x, y);
	}

	public boolean drawImage(final Image img, AffineTransform xform,
				 final ImageObserver obs)
	{
	    final AffineTransform fxform = makeClone(xform);
	    synchronized (Graphics2DRecorder.this) {
		methodCallList.add(new Callable() {
			public void call() {
			    g2d.drawImage(img, fxform, obs);
			}
		    });
	    }
	    return g2d.drawImage(img, xform, obs);
	}

	public void drawRenderableImage(final RenderableImage img,
					AffineTransform xform)
	{
	    final AffineTransform fxform = makeClone(xform);
	    synchronized (Graphics2DRecorder.this) {
		methodCallList.add(new Callable() {
			public void call() {
			    g2d.drawRenderableImage(img, fxform);
			}
		    });
	    }
	    g2d.drawRenderableImage(img, xform);
	}

	public void drawRenderedImage(final RenderedImage img,
				      AffineTransform xform)
	{
	    final AffineTransform fxform = makeClone(xform);
	    synchronized (Graphics2DRecorder.this) {
		methodCallList.add(new Callable() {
			public void call() {
			    g2d.drawRenderedImage(img, fxform);
			}
		    });
	    }
	    g2d.drawRenderedImage(img, xform);
	}

	public void drawString(AttributedCharacterIterator iterator,
			       final float x, final float y)
	{
	    final AttributedCharacterIterator fit = makeClone(iterator);
	    synchronized (Graphics2DRecorder.this) {
		methodCallList.add(new Callable() {
			AttributedCharacterIterator fiterator = fit;
			public void call() {
			    AttributedCharacterIterator newit =
				makeClone(fiterator);
			    g2d.drawString(fiterator, x, y);
			    fiterator = newit;
			}
		    });
	    }
	    g2d.drawString(iterator, x, y);
	}

	public void drawString(AttributedCharacterIterator iterator,
			       final int x, final int y)
	{
	    final AttributedCharacterIterator fit = makeClone(iterator);
	    synchronized (Graphics2DRecorder.this) {
		methodCallList.add(new Callable() {
			AttributedCharacterIterator fiterator = fit;
			public void call() {
			    AttributedCharacterIterator newit =
				makeClone(fiterator);
			    g2d.drawString(fiterator, x, y);
			    fiterator = newit;
			}
		    });
	    }
	    g2d.drawString(iterator, x, y);
	}

	public void drawString(final String str, final float x, final float y) {
	    synchronized (Graphics2DRecorder.this) {
		methodCallList.add(new Callable() {
			public void call() {
			    g2d.drawString(str, x, y);
			}
		    });
	    }
	    g2d.drawString(str, x, y);
	}

	public void drawString(final String str, final int x, final int y) {
	    synchronized (Graphics2DRecorder.this) {
		methodCallList.add(new Callable() {
			public void call() {
			    g2d.drawString(str, x, y);
			}
		    });
	    }
	    g2d.drawString(str, x, y);
	}

	public void fill(Shape s) {
	    final Shape fs = makeShapeClone(s);
	    synchronized (Graphics2DRecorder.this) {
		methodCallList.add(new Callable() {
			public void call() {
			    g2d.fill(fs);
			}
		    });
	    }
	    g2d.fill(s);
	}

	public Color getBackground() {
	    return g2d.getBackground();
	}

	public Composite getComposite() {
	    return g2d.getComposite();
	}

	public GraphicsConfiguration getDeviceConfiguration() {
	    return g2d.getDeviceConfiguration();
	}


	public Paint getPaint() {
	    return g2d.getPaint();
	}

	public Object getRenderingHint(RenderingHints.Key hintKey) {
	    return g2d.getRenderingHint(hintKey);
	}

	public RenderingHints getRenderingHints() {
	    return g2d.getRenderingHints();
	}

	public Stroke getStroke() {
	    return g2d.getStroke();
	}

	public AffineTransform 	getTransform() {
	    AffineTransform g2daf = makeClone(g2d.getTransform());
	    g2daf.preConcatenate(Graphics2DRecorder.this.invaf);
	    return g2daf;
	    // return g2d.getTransform();
	}

	public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
	    return g2d.hit(rect, s, onStroke);
	}

	public void rotate(final double theta) {
	    synchronized (Graphics2DRecorder.this) {
		methodCallList.add(new Callable() {
			public void call() {
			    g2d.rotate(theta);
			}
		    });
	    }
	    g2d.rotate(theta);
	}

	public void rotate(final double theta, final double x, final double y) {
	    synchronized (Graphics2DRecorder.this) {
		methodCallList.add(new Callable() {
			public void call() {
			    g2d.rotate(theta, x, y);
			}
		    });
	    }
	    g2d.rotate(theta, x, y);
	}

	public void scale(final double sx, final double sy) {
	    synchronized (Graphics2DRecorder.this) {
		methodCallList.add(new Callable() {
			public void call() {
			    g2d.scale(sx, sy);
			}
		    });
	    }
	    g2d.scale(sx, sy);
	}

	public void setBackground(final Color color) {
	    synchronized (Graphics2DRecorder.this) {
		methodCallList.add(new Callable() {
			public void call() {
			    g2d.setBackground(color);
			}
		    });
	    }
	    g2d.setBackground(color);
	}

	public void setComposite(final Composite comp) {
	    synchronized (Graphics2DRecorder.this) {
		methodCallList.add(new Callable() {
			public void call() {
			    g2d.setComposite(comp);
			}
		    });
	    }
	    g2d.setComposite(comp);
	}

	public void setPaint(final Paint paint) {
	    synchronized (Graphics2DRecorder.this) {
		methodCallList.add(new Callable() {
			public void call() {
			    g2d.setPaint(paint);
			}
		    });
	    }
	    g2d.setPaint(paint);
	}

	public void setRenderingHint(final RenderingHints.Key hintKey,
				     final Object hintValue)
	{
	    synchronized (Graphics2DRecorder.this) {
		methodCallList.add(new Callable() {
			public void call() {
			    g2d.setRenderingHint(hintKey, hintValue);
			}
		    });
	    }
	    g2d.setRenderingHint(hintKey, hintValue);
	}

	public void setRenderingHints(Map<?,?> hints) {
	    final HashMap<?,?> ourhints = new HashMap<>(hints);
	    synchronized (Graphics2DRecorder.this) {
		methodCallList.add(new Callable() {
			public void call() {
			    g2d.setRenderingHints(ourhints);
			}
		    });
	    }
	    g2d.setRenderingHints(hints);
	}

	public void setStroke(final Stroke s) {
	    synchronized (Graphics2DRecorder.this) {
		methodCallList.add(new Callable() {
			public void call() {
			    g2d.setStroke(s);
			}
		    });
	    }
	    g2d.setStroke(s);
	}

	public void setTransform(AffineTransform Tx) {
	    final AffineTransform fTx = makeClone(Tx);
	    AffineTransform ourTx = makeClone(Tx);
	    ourTx.preConcatenate(Graphics2DRecorder.this.af);
	    synchronized (Graphics2DRecorder.this) {
		methodCallList.add(new Callable() {
			public void call() {
			    AffineTransform ffTx = makeClone(fTx);
			    ffTx.preConcatenate(Graphics2DRecorder.this.af);
			    g2d.setTransform(ffTx);
			}
		    });
	    }
	    g2d.setTransform(ourTx);
	}

	public void shear(final double shx, final double shy) {
	    synchronized (Graphics2DRecorder.this) {
		methodCallList.add(new Callable() {
			public void call() {
			    g2d.shear(shx, shy);
			}
		    });
	    }
	    g2d.shear(shx, shy);
	}

	public void transform(AffineTransform Tx) {
	    final AffineTransform fTx = makeClone(Tx);
	    synchronized (Graphics2DRecorder.this) {
		methodCallList.add(new Callable() {
			public void call() {
			    g2d.transform(fTx);
			}
		    });
	    }
	    g2d.transform(Tx);
	}

	public void translate(final double tx, final double ty) {
	    synchronized (Graphics2DRecorder.this) {
		methodCallList.add(new Callable() {
			public void call() {
			    g2d.translate(tx, ty);
			}
		    });
	    }
	    g2d.translate(tx, ty);
	}

	public void translate(final int x, final int y) {
	    synchronized (Graphics2DRecorder.this) {
		methodCallList.add(new Callable() {
			public void call() {
			    g2d.translate(x, y);
			}
		    });
	    }
	    g2d.translate(x, y);
	}

	// following from Graphics

	public void clearRect(final int x, final int y,
			      final int width, final int height)
	{
	    synchronized (Graphics2DRecorder.this) {
		methodCallList.add(new Callable() {
			public void call() {
			    g2d.clearRect(x, y, width, height);
			}
		    });
	    }
	    g2d.clearRect(x, y, width, height);
	}

	public void clipRect(final int x, final int y,
			     final int width, final int height)
	{
	    synchronized (Graphics2DRecorder.this) {
		methodCallList.add(new Callable() {
			public void call() {
			    g2d.clipRect(x, y, width, height);
			}
		    });
	    }
	    g2d.clipRect(x, y, width, height);
	}

	public void copyArea(final int x, final int y,
			     final int width, final int height,
			     final int dx, final int dy)
	{
	    synchronized (Graphics2DRecorder.this) {
		methodCallList.add(new Callable() {
			public void call() {
			    g2d.copyArea(x,y, width, height, dx, dy);
			}
		    });
	    }
	    g2d.copyArea(x, y, width, height, dx, dy);
	}

	public Graphics create() {
	    Graphics g = g2d.create();
	    if (g instanceof Graphics2D) {
		Graphics2D newg2d = (Graphics2D)g;
		synchronized (Graphics2DRecorder.this) {
		    ++lastIndex;
		    final int newindex = lastIndex;
		    methodCallList.add(new Callable() {
			    public void call() {
				Graphics g = map.get(index).g2d.create();
				if (g instanceof Graphics2D) {
				    Graphics2D newg2d = (Graphics2D)g;
				    map.get(newindex).g2d = newg2d;
				}
			    }
			});
		    return new RecordingGraphics2D(newg2d);
		}
	    }
	    return null;
	}

	public Graphics create(final int x, final int y, 
			       final int width, final int height)
	{
	    Graphics g = g2d.create(x, y, width, height);
	    if (g instanceof Graphics2D) {
		Graphics2D newg2d = (Graphics2D)g;
		synchronized (Graphics2DRecorder.this) {
		    ++lastIndex;
		    int newindex = lastIndex;
		    methodCallList.add(new Callable() {
			    public void call() {
				Graphics g = map.get(index).g2d.create(x, y,
								       width,
								       height);
				if (g instanceof Graphics2D) {
				    Graphics2D newg2d = (Graphics2D)g;
				    map.get(newindex).g2d = newg2d;
				}
			    }
			});
		    return new RecordingGraphics2D(newg2d);
		}
	    }
	    return null;
	}

	public synchronized void dispose() {
	    // The one we are disposing is always one we created,
	    // and we repeat the creating a context when replayed.
	    methodCallList.add(new Callable() {
		    public void call() {
			map.get(index).g2d.dispose();
		    }
		});
	    g2d.dispose();
	}

	public void drawArc(final int x, final int y,
			    final int width, final int height,
			    final int startAngle, final int arcAngle)
	{
	    synchronized (Graphics2DRecorder.this) {
		methodCallList.add(new Callable() {
			public void call() {
			    g2d.drawArc(x, y, width, height, startAngle, arcAngle);
			}
		    });
	    }
	    g2d.drawArc(x, y, width, height, startAngle, arcAngle);
	}

	public boolean drawImage(final Image img, final int x, final int y,
				 final Color bgcolor,
				 final ImageObserver observer)
	{
	    synchronized (Graphics2DRecorder.this) {
		methodCallList.add(new Callable() {
			public void call() {
			    g2d.drawImage(img, x, y, bgcolor, observer);
			}
		    });
	    }
	    return g2d.drawImage(img, x, y, bgcolor, observer);
	}

	public boolean drawImage(final Image img, final int x, final int y,
				 final ImageObserver observer)
	{
	    synchronized (Graphics2DRecorder.this) {
		methodCallList.add(new Callable() {
			public void call() {
			    g2d.drawImage(img, x, y, observer);
			}
		    });
	    }
	    return g2d.drawImage(img, x, y, observer);
	}

	public boolean drawImage(final Image img, final int x, final int y,
				 final int width, final int height,
				 final Color bgcolor,
				 final ImageObserver observer)
	{
	    synchronized (Graphics2DRecorder.this) {
		methodCallList.add(new Callable() {
			public void call() {
			    g2d.drawImage(img, x, y, width, height, bgcolor,
					  observer);
			}
		    });
	    }
	    return g2d.drawImage(img, x, y, width, height, bgcolor, observer);
	}

	public boolean drawImage(final Image img, final int x, final int y,
				 final int width, final int height,
				 final ImageObserver observer)
	{
	    synchronized (Graphics2DRecorder.this) {
		methodCallList.add(new Callable() {
			public void call() {
			    g2d.drawImage(img, x, y, width, height, observer);
			}
		    });
	    }
	    return g2d.drawImage(img, x, y, width, height, observer);
	}

	public boolean drawImage(final Image img, final int dx1, final int dy1,
				 final int dx2, final int dy2,
				 final int sx1, final int sy1,
				 final int sx2, final int sy2,
				 final Color bgcolor,
				 final ImageObserver observer)
	{
	    synchronized (Graphics2DRecorder.this) {
		methodCallList.add(new Callable() {
			public void call() {
			    g2d.drawImage(img, dx1, dy1, dx2, dy2,
					  sx1, sy1, sx2, sy2,
					  bgcolor, observer);
			}
		    });
	    }
	    return g2d.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2,
				 bgcolor, observer);
	}

	public boolean drawImage(final Image img, final int dx1, final int dy1,
				 final int dx2, final int dy2,
				 final int sx1, final int sy1,
				 final int sx2, final int sy2,
				 final ImageObserver observer)
	{
	    synchronized (Graphics2DRecorder.this) {
		methodCallList.add(new Callable() {
			public void call() {
			    g2d.drawImage(img, dx1, dy1, dx2, dy2,
					  sx1, sy1, sx2, sy2,
					  observer);
			}
		    });
	    }
	    return g2d.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2,
				 observer);
	}

	public void drawLine(final int x1, final int y1,
			     final int x2, final int y2)
	{
	    synchronized (Graphics2DRecorder.this) {
		methodCallList.add(new Callable() {
			public void call() {
			    g2d.drawLine(x1, y1, x2, y2);
			}
		    });
	    }
	    g2d.drawLine(x1, y1, x2, y2);
	}

	public void drawOval(final int x, final int y,
			     final int width, final int height)
	{
	    synchronized (Graphics2DRecorder.this) {
		methodCallList.add(new Callable() {
			public void call() {
			    g2d.drawOval(x, y, width, height);
			}
		    });
	    }
	    g2d.drawOval(x, y, width, height);
	}

	public void drawPolygon(int[] xPoints, int[] yPoints,
				final int nPoints)
	{
	    final int[] fxPoints = makeClone(xPoints);
	    final int[] fyPoints = makeClone(yPoints);
	    synchronized (Graphics2DRecorder.this) {
		methodCallList.add(new Callable() {
			public void call() {
			    g2d.drawPolygon(fxPoints, fyPoints, nPoints);
			}
		    });
	    }
	    g2d.drawPolygon(xPoints, yPoints, nPoints);
	}

	public void drawPolyline(int[] xPoints, int[] yPoints,
				 final int nPoints)
	{
	    final int[] fxPoints = makeClone(xPoints);
	    final int[] fyPoints = makeClone(yPoints);
	    synchronized (Graphics2DRecorder.this) {
		methodCallList.add(new Callable() {
			public void call() {
			    g2d.drawPolyline(fxPoints, fyPoints, nPoints);
			}
		    });
	    }
	    g2d.drawPolyline(xPoints, yPoints, nPoints);
	}
	
	public void drawRoundRect(final int x, final int y,
				  final int width, final int height,
				  final int arcWidth, final int arcHeight)
	{
	    synchronized (Graphics2DRecorder.this) {
		methodCallList.add(new Callable() {
			public void call() {
			    g2d.drawRoundRect(x, y, width, height,
					      arcWidth, arcHeight);
			}
		    });
	    }
	    g2d.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
	}

	public void fillArc(final int x, final int y,
			    final int width, final int height,
			    final int startAngle, final int arcAngle)
	{
	    synchronized (Graphics2DRecorder.this) {
		methodCallList.add(new Callable() {
			public void call() {
			    g2d.fillArc(x, y, width, height, startAngle, arcAngle);
			}
		    });
	    }
	    g2d.fillArc(x, y, width, height, startAngle, arcAngle);
	}

	public void fillOval(final int x, final int y,
			     final int width, final int height)
	{
	    synchronized (Graphics2DRecorder.this) {
		methodCallList.add(new Callable() {
			public void call() {
			    g2d.fillOval(x, y, width, height);
			}
		    });
	    }
	    g2d.fillOval(x, y, width, height);
	}

	public void fillPolygon(int[] xPoints, int[] yPoints,
				final int nPoints)
	{
	    final int[] fxPoints = makeClone(xPoints);
	    final int[] fyPoints = makeClone(yPoints);
	    synchronized (Graphics2DRecorder.this) {
		methodCallList.add(new Callable() {
			public void call() {
			    g2d.fillPolygon(fxPoints, fyPoints, nPoints);
			}
		    });
	    }
	    g2d.fillPolygon(xPoints, yPoints, nPoints);
	}

	public void fillRect(final int x, final int y,
			     final int width, final int height)
	{
	    synchronized (Graphics2DRecorder.this) {
		methodCallList.add(new Callable() {
			public void call() {
			    g2d.fillRect(x, y, width, height);
			}
		    });
	    }
	    g2d.fillRect(x, y, width, height);
	}

	public void fillRoundRect(final int x, final int y,
				  final int width, final int height,
				  final int arcWidth, final int arcHeight)
	{
	    synchronized (Graphics2DRecorder.this) {
		methodCallList.add(new Callable() {
			public void call() {
			    g2d.fillRoundRect(x, y, width, height,
					      arcWidth, arcHeight);
			}
		    });
	    }
	    g2d.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
	}

	public Shape getClip() {
	    return g2d.getClip();
	}

	public Rectangle getClipBounds() {
	    return g2d.getClipBounds();
	}

	public Color getColor() {
	    return g2d.getColor();
	}

	public Font getFont() {
	    return g2d.getFont();
	}

	public FontMetrics getFontMetrics() {
	    return g2d.getFontMetrics();
	}

	public FontMetrics getFontMetrics(Font f) {
	    return g2d.getFontMetrics(f);
	}

	public boolean hitClip(int x, int y, int width, int height) {
	    return g2d.hitClip(x, y, width, height);
	}

	public void setClip(final int x, final int y,
			    final int width, final int height)
	{
	    synchronized (Graphics2DRecorder.this) {
		methodCallList.add(new Callable() {
			public void call() {
			    g2d.setClip(x, y, width, height);
			}
		    });
	    }
	    g2d.setClip(x, y, width, height);
	}

	public void setClip(Shape clip) {
	    final Shape fclip = makeShapeClone(clip);
	    synchronized (Graphics2DRecorder.this) {
		methodCallList.add(new Callable() {
			public void call() {
			    g2d.setClip(fclip);
			}
		    });
	    }
	    g2d.setClip(clip);
	}

	public void setColor(final Color c) {
	    synchronized (Graphics2DRecorder.this) {
		methodCallList.add(new Callable() {
			public void call() {
			    g2d.setColor(c);
			}
		    });
	    }
	    g2d.setColor(c);
	}

	public void setFont(final Font font) {
	    synchronized (Graphics2DRecorder.this) {
		methodCallList.add(new Callable() {
			public void call() {
			    g2d.setFont(font);
			}
		    });
	    }
	    g2d.setFont(font);
	}

	public void setPaintMode() {
	    synchronized (Graphics2DRecorder.this) {
		methodCallList.add(new Callable() {
			public void call() {
			    g2d.setPaintMode();
			}
		    });
	    }
	    g2d.setPaintMode();
	}

	public void setXORMode(final Color c1) {
	    synchronized (Graphics2DRecorder.this) {
		methodCallList.add(new Callable() {
			public void call() {
			    g2d.setXORMode(c1);
			}
		    });
	    }
	    g2d.setXORMode(c1);
	}

	public String toString() {
	    return g2d.toString();
	}

    }

    /**
     * Create a graphics context.
     * @return the new graphics context
     */
    public synchronized Graphics2D createGraphics() {
	Graphics g = g2dBase.create();
	if (g instanceof Graphics2D) {
	    Graphics2D newg2d = (Graphics2D)g;
	    final int ind = ++lastIndex;
	    methodCallList.add(new Callable() {
		    public void call() {
			Graphics g = g2dBase.create();
			if (g instanceof Graphics2D) {
			    Graphics2D newg2d = (Graphics2D)g;
				map.get(ind).g2d = newg2d;
			}
		    }
		});
	    return new RecordingGraphics2D(newg2d);
	} else {
	    throw new UnsupportedOperationException
		(errorMsg("cannotCreateG2D"));
		/*("could not create a " + "Graphics2D");*/
	}
    }

    /**
     * Play back all the operations perforemed on the graphics contexts
     * created, except for a dispose as that will be handled separately
     * for the target graphics context.
     * @param tg2d the target graphics context for the playback
     */
    public synchronized void playback(Graphics2D tg2d) {
	Graphics2D saved = g2dBase;
	try {
	    g2dBase = tg2d;
	    af = tg2d.getTransform();
	    invaf = af.isIdentity()? af: af.createInverse();
	    for (Callable c: methodCallList) {
		c.call();
	    }
	} catch (NoninvertibleTransformException e) {
	    String msg = errorMsg("cannotCreateNI");
	    throw new IllegalStateException(msg, e);
	} finally {
	    g2dBase = saved;
	}
    }
}

//  LocalWords:  exbundle getFontRenderContext getDeviceConfiguration
//  LocalWords:  toString DRecorder getBackground getComposite clazz
//  LocalWords:  getPaint getRenderingHint RenderingHints getStroke
//  LocalWords:  getRenderingHints getTransform getClip getClipBounds
//  LocalWords:  getFont getFontMetrics hitClip setBackground setFont
//  LocalWords:  createGraphics cannotCreateNI cannotClone theClone
//  LocalWords:  CloneNotSupportedException methodCallList LinkedList
//  LocalWords:  HashMap RecordingGraphics ClonedShape Cloner msg tg
//  LocalWords:  makeCastedClone errorMsg cannotCreateG perforemed
//  LocalWords:  UnsupportedOperationException
