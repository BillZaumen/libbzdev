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
 * Graphics2D subclass that mirrors requests to two instances of
 * {@link java.awt.Graphics2D}.
 */
public class SplitterGraphics2D extends Graphics2D  {

    Graphics2D g2d1;
    Graphics2D g2d2;

    /**
     * Constructor.
     * @param primary the first Graphics2D
     * @param secondary the second Graphics2D
     */
    public SplitterGraphics2D(Graphics2D primary, Graphics2D secondary) {
	g2d1 = primary;
	g2d2 = secondary;
    }

    @Override
    public void addRenderingHints(Map<?,?> hints) {
	g2d1.addRenderingHints(hints);
	g2d2.addRenderingHints(hints);
    }

    @Override
    public void clip(Shape s) {
	g2d1.clip(s);
	g2d1.clip(s);
    }
	
    @Override
    public void draw(Shape s) {
	g2d1.draw(s);
	g2d2.draw(s);
	return;
    }

    @Override
    public void drawGlyphVector(GlyphVector g,
				float x, float y)
    {
	g2d1.drawGlyphVector(g, x, y);
	g2d2.drawGlyphVector(g, x, y);
	return;
    }

    @Override
    public FontRenderContext getFontRenderContext() {
	return g2d1.getFontRenderContext();
    }
	
    @Override
    public void drawImage(BufferedImage img, 
			  BufferedImageOp op,
			  int x, int y)
    {
	g2d1.drawImage(img, op, x, y);
	g2d2.drawImage(img, op, x, y);
	return;
    }

    @Override
    public boolean drawImage(Image img, AffineTransform xform,
			     ImageObserver obs)
    {
	return g2d1.drawImage(img, xform, obs)
	    && g2d2.drawImage(img, xform, obs);
    }

    @Override
    public void drawRenderableImage(RenderableImage img,
				    AffineTransform xform)
    {
	g2d1.drawRenderableImage(img, xform);
	g2d2.drawRenderableImage(img, xform);
	return;
    }

    @Override
    public void drawRenderedImage(RenderedImage img,
				  AffineTransform xform)
    {
	g2d1.drawRenderedImage(img, xform);
	g2d2.drawRenderedImage(img, xform);
	return;
    }

    @Override
    public void drawString(AttributedCharacterIterator iterator,
			   float x, float y)
    {
	AttributedCharacterIterator iterator2 =
	    (AttributedCharacterIterator)(iterator.clone());
	g2d1.drawString(iterator, x, y);
	g2d2.drawString(iterator2, x, y);
	return;
    }

    @Override
    public void drawString(AttributedCharacterIterator iterator,
			   int x, int y)
    {
	AttributedCharacterIterator iterator2 =
	    (AttributedCharacterIterator)(iterator.clone());
	g2d1.drawString(iterator, x, y);
	g2d2.drawString(iterator2, x, y);
	return;
    }

    @Override
    public void drawString(String str, float x, float y) {
	g2d1.drawString(str, x, y);
	g2d2.drawString(str, x, y);
	return;
    }

    @Override
    public void drawString(String str, int x, int y) {
	g2d1.drawString(str, x, y);
	g2d2.drawString(str, x, y);
	return;
    }

    @Override
    public void fill(Shape s) {
	g2d1.fill(s);
	g2d2.fill(s);
	return;
    }

    @Override
    public Color getBackground() {
	return g2d1.getBackground();
    }

    @Override
    public Composite getComposite() {
	return g2d1.getComposite();
    }

    @Override
    public GraphicsConfiguration getDeviceConfiguration() {
	return g2d1.getDeviceConfiguration();
    }

    @Override
    public Paint getPaint() {
	return g2d1.getPaint();
    }

    @Override
    public Object getRenderingHint(RenderingHints.Key hintKey) {
	return g2d1.getRenderingHint(hintKey);
    }

    @Override
    public RenderingHints getRenderingHints() {
	return g2d1.getRenderingHints();
    }

    @Override
    public Stroke getStroke() {
	return g2d1.getStroke();
    }

    @Override
    public AffineTransform getTransform() {
	return g2d1.getTransform();
    }

    @Override
    public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
	return g2d1.hit(rect, s, onStroke);
    }

    @Override
    public void rotate(double theta) {
	g2d1.rotate(theta);
	g2d2.rotate(theta);
    }

    @Override
    public void rotate(double theta, double x, double y) {
	g2d1.rotate(theta, x, y);
	g2d2.rotate(theta, x, y);
    }

    @Override
    public void scale(double sx, double sy) {
	g2d1.scale(sx, sy);
	g2d2.scale(sx, sy);
    }

    @Override
    public void setBackground(Color color) {
	g2d1.setBackground(color);
	g2d2.setBackground(color);
    }

    @Override
    public void setComposite(Composite comp) {
	g2d1.setComposite(comp);
	g2d2.setComposite(comp);
    }

    @Override
    public void setPaint(Paint paint) {
	g2d1.setPaint(paint);
	g2d2.setPaint(paint);
    }

    @Override
    public void setRenderingHint(RenderingHints.Key hintKey,
				 Object hintValue)
    {
	g2d1.setRenderingHint(hintKey, hintValue);
	g2d2.setRenderingHint(hintKey, hintValue);
    }

    @Override
    public void setRenderingHints(Map<?,?> hints) {
	g2d1.setRenderingHints(hints);
	g2d2.setRenderingHints(hints);
    }

    @Override
    public void setStroke(Stroke s) {
	g2d1.setStroke(s);
	g2d2.setStroke(s);
    }

    @Override
    public void setTransform(AffineTransform Tx) {
	g2d1.setTransform(Tx);
	g2d2.setTransform(Tx);
    }

    @Override
    public void shear(double shx, double shy) {
	g2d1.shear(shx, shy);
	g2d2.shear(shx, shy);
    }

    @Override
    public void transform(AffineTransform Tx) {
	g2d1.transform(Tx);
	g2d2.transform(Tx);
    }

    @Override
    public void translate(double tx, double ty) {
	g2d1.translate(tx, ty);
	g2d2.translate(tx, ty);
    }

    @Override
    public void translate(int x, int y) {
	g2d1.translate(x, y);
	g2d2.translate(x, y);
    }

    // following from Graphics

    @Override
    public void clearRect(int x, int y,
			  int width, int height)
    {
	g2d1.clearRect(x, y, width, height);
	g2d2.clearRect(x, y, width, height);
    }

    @Override
    public void clipRect(int x, int y,
			 int width, int height)
    {
	g2d1.clipRect(x, y, width, height);
	g2d2.clipRect(x, y, width, height);
    }

    @Override
    public void copyArea(int x, int y,
			 int width, int height,
			 int dx, int dy)
    {
	g2d1.copyArea(x, y, width, height, dx, dy);
	g2d2.copyArea(x, y, width, height, dx, dy);
    }

    @Override
    public Graphics create() {
	Graphics g1 = g2d1.create();
	Graphics g2 = g2d2.create();
	if (g1 instanceof Graphics2D && g1 instanceof Graphics2D) {
	    return new SplitterGraphics2D((Graphics2D) g1, (Graphics2D) g2);
	} else {
	    return new SplitterGraphics(g1, g2);
	}
    }

    @Override
    public Graphics create(int x, int y, 
			   int width, int height)
    {
	Graphics g1 = g2d1.create(x, y, width, height);
	Graphics g2 = g2d2.create(x, y, width, height);
	if (g1 instanceof Graphics2D && g1 instanceof Graphics2D) {
	    return new SplitterGraphics2D((Graphics2D) g1, (Graphics2D) g2);
	} else {
	    return new SplitterGraphics(g1, g2);
	}
    }

    @Override
    public void dispose() {
	g2d1.dispose();
	g2d2.dispose();
    }

    @Override
    public void drawArc(int x, int y,
			int width, int height,
			int startAngle, int arcAngle)
    {
	g2d1.drawArc(x, y, width, height, startAngle, arcAngle);
	g2d2.drawArc(x, y, width, height, startAngle, arcAngle);
    }

    @Override
    public boolean drawImage(Image img, int x, int y,
			     Color bgcolor,
			     ImageObserver observer)
    {
	return g2d1.drawImage(img, x, y, bgcolor, observer)
	    && g2d2.drawImage(img, x, y, bgcolor, observer);
    }

    @Override
    public boolean drawImage(Image img, int x, int y,
			     ImageObserver observer)
    {
	return g2d1.drawImage(img, x, y, observer)
	    && g2d2.drawImage(img, x, y, observer);
    }

    @Override
    public boolean drawImage(Image img, int x, int y,
			     int width, int height,
			     Color bgcolor,
			     ImageObserver observer)
    {
	return g2d1.drawImage(img, x, y, width, height, bgcolor, observer)
	    && g2d2.drawImage(img, x, y, width, height, bgcolor, observer);
    }

    @Override
    public boolean drawImage(Image img, int x, int y,
			     int width, int height,
			     ImageObserver observer)
    {
	return g2d1.drawImage(img, x, y, width, height, observer)
	    && g2d2.drawImage(img, x, y, width, height, observer);
    }

    @Override
    public boolean drawImage(Image img, int dx1, int dy1,
			     int dx2, int dy2,
			     int sx1, int sy1,
			     int sx2, int sy2,
			     Color bgcolor,
			     ImageObserver observer)
    {
	return g2d1.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2,
			     bgcolor, observer)
	    && g2d1.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2,
			      bgcolor, observer);
    }

    @Override
    public boolean drawImage(Image img, int dx1, int dy1,
			     int dx2, int dy2,
			     int sx1, int sy1,
			     int sx2, int sy2,
			     ImageObserver observer)
    {
	return g2d1.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2,
			     observer)
	    && g2d1.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2,
			      observer);
    }

    @Override
    public void drawLine(int x1, int y1,
			 int x2, int y2)
    {
	g2d1.drawLine(x1, y1, x2, y2);
	g2d2.drawLine(x1, y1, x2, y2);
    }

    @Override
    public void drawOval(int x, int y,
			 int width, int height)
    {
	g2d1.drawOval(x, y, width, height);
	g2d2.drawOval(x, y, width, height);
    }

    @Override
    public void drawPolygon(int[] xPoints, int[] yPoints,
			    int nPoints)
    {
	g2d1.drawPolygon(xPoints, yPoints, nPoints);
	g2d2.drawPolygon(xPoints, yPoints, nPoints);
    }

    public void drawPolyline(int[] xPoints, int[] yPoints,
			     int nPoints)
    {
	g2d1.drawPolyline(xPoints, yPoints, nPoints);
	g2d2.drawPolyline(xPoints, yPoints, nPoints);
    }
	
    @Override
    public void drawRoundRect(int x, int y,
			      int width, int height,
			      int arcWidth, int arcHeight)
    {
	g2d1.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
	g2d2.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
    }

    @Override
    public void fillArc(int x, int y,
			int width, int height,
			int startAngle, int arcAngle)
    {
	g2d1.fillArc(x, y, width, height, startAngle, arcAngle);
	g2d2.fillArc(x, y, width, height, startAngle, arcAngle);
    }

    @Override
    public void fillOval(int x, int y,
			 int width, int height)
    {
	g2d1.fillOval(x, y, width, height);
	g2d2.fillOval(x, y, width, height);
    }

    @Override
    public void fillPolygon(int[] xPoints, int[] yPoints,
			    int nPoints)
    {
	g2d1.fillPolygon(xPoints, yPoints, nPoints);
	g2d2.fillPolygon(xPoints, yPoints, nPoints);
    }

    @Override
    public void fillRect(int x, int y,
			 int width, int height)
    {
	g2d1.fillRect(x, y, width, height);
	g2d2.fillRect(x, y, width, height);
    }

    @Override
    public void fillRoundRect(int x, int y,
			      int width, int height,
			      int arcWidth, int arcHeight)
    {
	g2d1.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
	g2d2.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
    }

    @Override
    public Shape getClip() {
	return g2d1.getClip();
    }

    @Override
    public Rectangle getClipBounds() {
	return g2d1.getClipBounds();
    }

    @Override
    public Color getColor() {
	return g2d1.getColor();
    }

    @Override
    public Font getFont() {
	return g2d1.getFont();
    }

    @Override
    public FontMetrics getFontMetrics() {
	return g2d1.getFontMetrics();
    }

    @Override
    public FontMetrics getFontMetrics(Font f) {
	return g2d1.getFontMetrics(f);
    }

    @Override
    public boolean hitClip(int x, int y, int width, int height) {
	return g2d1.hitClip(x, y, width, height);
    }

    @Override
    public void setClip(int x, int y,
			int width, int height)
    {
	g2d1.setClip(x, y, width, height);
	g2d2.setClip(x, y, width, height);
    }

    @Override
    public void setClip(Shape clip) {
	g2d1.setClip(clip);
	g2d2.setClip(clip);
    }

    @Override
    public void setColor(Color c) {
	g2d1.setColor(c);
	g2d2.setColor(c);
    }

    @Override
    public void setFont(Font font) {
	g2d1.setFont(font);
	g2d2.setFont(font);
    }

    @Override
    public void setPaintMode() {
	g2d1.setPaintMode();
	g2d2.setPaintMode();
    }

    @Override
    public void setXORMode(Color c1) {
	g2d1.setXORMode(c1);
	g2d2.setXORMode(c1);
    }
}

//  LocalWords:  DRecorder paintComponent preferAlpha
//  LocalWords:  SplitterGraphics
