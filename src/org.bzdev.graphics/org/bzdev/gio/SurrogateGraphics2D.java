package org.bzdev.gio;

import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.font.*;
import java.awt.image.*;
import java.awt.image.renderable.*;
import java.text.AttributedCharacterIterator;

import org.bzdev.graphs.Graph;

/**
 * Graphics2D class providing a minimal implementation.
 * When this class is used, it will typically be used to create a
 * minimal implementation of {@link Graphics2D} that will be passed
 * to the constructor
 * {@link org.bzdev.gio.Graphics2DRecorder#Graphics2DRecorder(Graphics2D)}.
 * A typical use is to record a series of graphics operation that will
 * be "replayed" inside a method such as
 * {@link javax.swing.JComponent#paintComponent(Graphics)}.
 * <P>
 * The implementation uses a buffered image for some operations
 * to ensure that the behavior is the same as that for the Java class
 * library, but the methods that draw objects, with a few exceptions
 * involving the explicit use of image observers, do nothing.
 */
public class SurrogateGraphics2D extends Graphics2D  {

    BufferedImage bi;
    Graphics2D g2d;

    /**
     * Constructor.
     */
    public SurrogateGraphics2D() {
	this(10, 10, false);
    }

    /**
     * Constructor indicating if an internal buffered image should use
     * an alpha channel.
     * @param preferAlpha true if the an alpha channel should be provided;
     *        false otherwise
     */
    public SurrogateGraphics2D(boolean preferAlpha) {
	this(10, 10, preferAlpha);
    }

    private static int getBIType(boolean preferAlpha) {
	return preferAlpha? BufferedImage.TYPE_INT_ARGB_PRE:
	    BufferedImage.TYPE_INT_RGB;
    }

    /**
     * Constructor specifying a width and height.
     * @param width the width for a buffered image
     * @param height the height for a buffered image
     * @param preferAlpha true if an alpha channel should be provided;
     *        false otherwise
     */
    public SurrogateGraphics2D(int width, int height, boolean preferAlpha) {
	bi = new BufferedImage(width, height, getBIType(preferAlpha));
	g2d = bi.createGraphics();
    }

    /**
     * Get the image type used by a buffered image associated with an
     * instance of SurrogateGraphics2D.
     * @param preferAlpha true if the buffered image should have an alpha
     *        channel; false otherwise
     * @return the image type as an enumeration constant
     */
    public static Graph.ImageType getGraphImageType(boolean preferAlpha) {
	return Graph.ImageType.getImageType(getBIType(preferAlpha));
    }

    @Override
    public void addRenderingHints(Map<?,?> hints) {
	g2d.addRenderingHints(hints);
    }

    @Override
    public void clip(Shape s) {
	g2d.clip(s);
    }
	
    @Override
    public void draw(Shape s) {
	return;
    }

    @Override
    public void drawGlyphVector(GlyphVector g,
				float x, float y)
    {
	return;
    }

    @Override
    public FontRenderContext getFontRenderContext() {
	return g2d.getFontRenderContext();
    }
	
    @Override
    public void drawImage(BufferedImage img, 
			  BufferedImageOp op,
			  int x, int y)
    {
	return;
    }

    @Override
    public boolean drawImage(Image img, AffineTransform xform,
			     ImageObserver obs)
    {
	return g2d.drawImage(img, xform, obs);
    }

    @Override
    public void drawRenderableImage(RenderableImage img,
				    AffineTransform xform)
    {
	return;
    }

    @Override
    public void drawRenderedImage(RenderedImage img,
				  AffineTransform xform)
    {
	return;
    }

    @Override
    public void drawString(AttributedCharacterIterator iterator,
			   float x, float y)
    {
	return;
    }

    @Override
    public void drawString(AttributedCharacterIterator iterator,
			   int x, int y)
    {
	return;
    }

    @Override
    public void drawString(String str, float x, float y) {
	return;
    }

    @Override
    public void drawString(String str, int x, int y) {
	return;
    }

    @Override
    public void fill(Shape s) {
	return;
    }

    @Override
    public Color getBackground() {
	return g2d.getBackground();
    }

    @Override
    public Composite getComposite() {
	return g2d.getComposite();
    }

    @Override
    public GraphicsConfiguration getDeviceConfiguration() {
	return g2d.getDeviceConfiguration();
    }

    @Override
    public Paint getPaint() {
	return g2d.getPaint();
    }

    @Override
    public Object getRenderingHint(RenderingHints.Key hintKey) {
	return g2d.getRenderingHint(hintKey);
    }

    @Override
    public RenderingHints getRenderingHints() {
	return g2d.getRenderingHints();
    }

    @Override
    public Stroke getStroke() {
	return g2d.getStroke();
    }

    @Override
    public AffineTransform getTransform() {
	return g2d.getTransform();
    }

    @Override
    public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
	return g2d.hit(rect, s, onStroke);
    }

    @Override
    public void rotate(double theta) {
	g2d.rotate(theta);
    }

    @Override
    public void rotate(double theta, double x, double y) {
	g2d.rotate(theta, x, y);
    }

    @Override
    public void scale(double sx, double sy) {
	g2d.scale(sx, sy);
    }

    @Override
    public void setBackground(Color color) {
	g2d.setBackground(color);
    }

    @Override
    public void setComposite(Composite comp) {
	g2d.setComposite(comp);
    }

    @Override
    public void setPaint(Paint paint) {
	g2d.setPaint(paint);
    }

    @Override
    public void setRenderingHint(RenderingHints.Key hintKey,
				 Object hintValue)
    {
	g2d.setRenderingHint(hintKey, hintValue);
    }

    @Override
    public void setRenderingHints(Map<?,?> hints) {
	g2d.setRenderingHints(hints);
    }

    @Override
    public void setStroke(Stroke s) {
	g2d.setStroke(s);
    }

    @Override
    public void setTransform(AffineTransform Tx) {

	g2d.setTransform(Tx);
    }

    @Override
    public void shear(double shx, double shy) {
	g2d.shear(shx, shy);
    }

    @Override
    public void transform(AffineTransform Tx) {
	g2d.transform(Tx);
    }

    @Override
    public void translate(double tx, double ty) {
	g2d.translate(tx, ty);
    }

    @Override
    public void translate(int x, int y) {
	g2d.translate(x, y);
    }

    // following from Graphics

    @Override
    public void clearRect(int x, int y,
			  int width, int height)
    {
    }

    @Override
    public void clipRect(int x, int y,
			 int width, int height)
    {
	g2d.clipRect(x, y, width, height);
    }

    @Override
    public void copyArea(int x, int y,
			 int width, int height,
			 int dx, int dy)
    {
    }

    @Override
    public Graphics create() {
	return g2d.create();
    }

    @Override
    public Graphics create(int x, int y, 
			   int width, int height)
    {
	return g2d.create(x, y, width, height);
    }

    @Override
    public void dispose() {
	g2d.dispose();
    }

    @Override
    public void drawArc(int x, int y,
			int width, int height,
			int startAngle, int arcAngle)
    {
    }

    @Override
    public boolean drawImage(Image img, int x, int y,
			     Color bgcolor,
			     ImageObserver observer)
    {
	return g2d.drawImage(img, x, y, bgcolor, observer);
    }

    @Override
    public boolean drawImage(Image img, int x, int y,
			     ImageObserver observer)
    {
	return g2d.drawImage(img, x, y, observer);
    }

    @Override
    public boolean drawImage(Image img, int x, int y,
			     int width, int height,
			     Color bgcolor,
			     ImageObserver observer)
    {
	return g2d.drawImage(img, x, y, width, height, bgcolor, observer);
    }

    @Override
    public boolean drawImage(Image img, int x, int y,
			     int width, int height,
			     ImageObserver observer)
    {
	return g2d.drawImage(img, x, y, width, height, observer);
    }

    @Override
    public boolean drawImage(Image img, int dx1, int dy1,
			     int dx2, int dy2,
			     int sx1, int sy1,
			     int sx2, int sy2,
			     Color bgcolor,
			     ImageObserver observer)
    {
	return g2d.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2,
			     bgcolor, observer);
    }

    @Override
    public boolean drawImage(Image img, int dx1, int dy1,
			     int dx2, int dy2,
			     int sx1, int sy1,
			     int sx2, int sy2,
			     ImageObserver observer)
    {
	return g2d.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2,
			     observer);
    }

    @Override
    public void drawLine(int x1, int y1,
			 int x2, int y2)
    {
    }

    @Override
    public void drawOval(int x, int y,
			 int width, int height)
    {
    }

    @Override
    public void drawPolygon(int[] xPoints, int[] yPoints,
			    int nPoints)
    {
    }

    public void drawPolyline(int[] xPoints, int[] yPoints,
			     int nPoints)
    {
    }
	
    @Override
    public void drawRoundRect(int x, int y,
			      int width, int height,
			      int arcWidth, int arcHeight)
    {
    }

    @Override
    public void fillArc(int x, int y,
			int width, int height,
			int startAngle, int arcAngle)
    {
    }

    @Override
    public void fillOval(int x, int y,
			 int width, int height)
    {
	g2d.fillOval(x, y, width, height);
    }

    @Override
    public void fillPolygon(int[] xPoints, int[] yPoints,
			    int nPoints)
    {
    }

    @Override
    public void fillRect(int x, int y,
			 int width, int height)
    {
    }

    @Override
    public void fillRoundRect(int x, int y,
			      int width, int height,
			      int arcWidth, int arcHeight)
    {
    }

    @Override
    public Shape getClip() {
	return g2d.getClip();
    }

    @Override
    public Rectangle getClipBounds() {
	return g2d.getClipBounds();
    }

    @Override
    public Color getColor() {
	return g2d.getColor();
    }

    @Override
    public Font getFont() {
	return g2d.getFont();
    }

    @Override
    public FontMetrics getFontMetrics() {
	return g2d.getFontMetrics();
    }

    @Override
    public FontMetrics getFontMetrics(Font f) {
	return g2d.getFontMetrics(f);
    }

    @Override
    public boolean hitClip(int x, int y, int width, int height) {
	return g2d.hitClip(x, y, width, height);
    }

    @Override
    public void setClip(int x, int y,
			int width, int height)
    {
	g2d.setClip(x, y, width, height);
    }

    @Override
    public void setClip(Shape clip) {
	g2d.setClip(clip);
    }

    @Override
    public void setColor(Color c) {
	g2d.setColor(c);
    }

    @Override
    public void setFont(Font font) {
	g2d.setFont(font);
    }

    @Override
    public void setPaintMode() {
    }

    @Override
    public void setXORMode(Color c1) {
    }
}

//  LocalWords:  DRecorder paintComponent preferAlpha
//  LocalWords:  SurrogateGraphics
