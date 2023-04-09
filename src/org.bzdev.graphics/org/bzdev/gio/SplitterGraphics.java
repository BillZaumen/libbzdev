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
 * Graphics subclass that mirrors requests to two instances of
 * {@link java.awt.Graphics}.
 */
public class SplitterGraphics extends Graphics  {

    Graphics g1;
    Graphics g2;

    /**
     * Constructor.
     * @param primary the primary {@link Graphics}
     * @param secondary the secondary {@link Graphics}
     */
    public SplitterGraphics(Graphics primary, Graphics secondary) {
	super();
	g1 = primary;
	g2 = secondary;
    }

    @Override
    public void drawString(String str, int x, int y) {
	g1.drawString(str, x, y);
	g2.drawString(str, x, y);
    }

    @Override
    public void drawString(AttributedCharacterIterator iterator,
			   int x, int y)
    {
	AttributedCharacterIterator iterator2 =
	    (AttributedCharacterIterator)(iterator.clone());
	g1.drawString(iterator, x, y);
	g2.drawString(iterator2, x, y);
	return;
    }

    @Override
    public void translate(int x, int y) {
	g1.translate(x, y);
	g2.translate(x, y);
    }

    // following from Graphics

    @Override
    public void clearRect(int x, int y,
			  int width, int height)
    {
	g1.clearRect(x, y, width, height);
	g2.clearRect(x, y, width, height);
    }

    @Override
    public void clipRect(int x, int y,
			 int width, int height)
    {
	g1.clipRect(x, y, width, height);
	g2.clipRect(x, y, width, height);
    }

    @Override
    public void copyArea(int x, int y,
			 int width, int height,
			 int dx, int dy)
    {
	g1.copyArea(x, y, width, height, dx, dy);
	g2.copyArea(x, y, width, height, dx, dy);
    }

    @Override
    public Graphics create() {
	return new SplitterGraphics(g1.create(), g2.create());
    }

    @Override
    public Graphics create(int x, int y, 
			   int width, int height)
    {
	Graphics gg1 = g1.create(x, y, width, height);
	Graphics gg2 = g2.create(x, y, width, height);
	return new SplitterGraphics(gg1, gg2);
    }

    @Override
    public void dispose() {
	g1.dispose();
	g2.dispose();
    }

    @Override
    public void drawArc(int x, int y,
			int width, int height,
			int startAngle, int arcAngle)
    {
	g1.drawArc(x, y, width, height, startAngle, arcAngle);
	g2.drawArc(x, y, width, height, startAngle, arcAngle);
    }

    @Override
    public boolean drawImage(Image img, int x, int y,
			     Color bgcolor,
			     ImageObserver observer)
    {
	return g1.drawImage(img, x, y, bgcolor, observer)
	    && g2.drawImage(img, x, y, bgcolor, observer);
    }

    @Override
    public boolean drawImage(Image img, int x, int y,
			     ImageObserver observer)
    {
	return g1.drawImage(img, x, y, observer)
	    && g2.drawImage(img, x, y, observer);
    }

    @Override
    public boolean drawImage(Image img, int x, int y,
			     int width, int height,
			     Color bgcolor,
			     ImageObserver observer)
    {
	return g1.drawImage(img, x, y, width, height, bgcolor, observer)
	    && g2.drawImage(img, x, y, width, height, bgcolor, observer);
    }

    @Override
    public boolean drawImage(Image img, int x, int y,
			     int width, int height,
			     ImageObserver observer)
    {
	return g1.drawImage(img, x, y, width, height, observer)
	    && g2.drawImage(img, x, y, width, height, observer);
    }

    @Override
    public boolean drawImage(Image img, int dx1, int dy1,
			     int dx2, int dy2,
			     int sx1, int sy1,
			     int sx2, int sy2,
			     Color bgcolor,
			     ImageObserver observer)
    {
	return g1.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2,
			     bgcolor, observer)
	    && g1.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2,
			      bgcolor, observer);
    }

    @Override
    public boolean drawImage(Image img, int dx1, int dy1,
			     int dx2, int dy2,
			     int sx1, int sy1,
			     int sx2, int sy2,
			     ImageObserver observer)
    {
	return g1.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2,
			     observer)
	    && g1.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2,
			      observer);
    }

    @Override
    public void drawLine(int x1, int y1,
			 int x2, int y2)
    {
	g1.drawLine(x1, y1, x2, y2);
	g2.drawLine(x1, y1, x2, y2);
    }

    @Override
    public void drawOval(int x, int y,
			 int width, int height)
    {
	g1.drawOval(x, y, width, height);
	g2.drawOval(x, y, width, height);
    }

    @Override
    public void drawPolygon(int[] xPoints, int[] yPoints,
			    int nPoints)
    {
	g1.drawPolygon(xPoints, yPoints, nPoints);
	g2.drawPolygon(xPoints, yPoints, nPoints);
    }

    public void drawPolyline(int[] xPoints, int[] yPoints,
			     int nPoints)
    {
	g1.drawPolyline(xPoints, yPoints, nPoints);
	g2.drawPolyline(xPoints, yPoints, nPoints);
    }
	
    @Override
    public void drawRoundRect(int x, int y,
			      int width, int height,
			      int arcWidth, int arcHeight)
    {
	g1.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
	g2.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
    }

    @Override
    public void fillArc(int x, int y,
			int width, int height,
			int startAngle, int arcAngle)
    {
	g1.fillArc(x, y, width, height, startAngle, arcAngle);
	g2.fillArc(x, y, width, height, startAngle, arcAngle);
    }

    @Override
    public void fillOval(int x, int y,
			 int width, int height)
    {
	g1.fillOval(x, y, width, height);
	g2.fillOval(x, y, width, height);
    }

    @Override
    public void fillPolygon(int[] xPoints, int[] yPoints,
			    int nPoints)
    {
	g1.fillPolygon(xPoints, yPoints, nPoints);
	g2.fillPolygon(xPoints, yPoints, nPoints);
    }

    @Override
    public void fillRect(int x, int y,
			 int width, int height)
    {
	g1.fillRect(x, y, width, height);
	g2.fillRect(x, y, width, height);
    }

    @Override
    public void fillRoundRect(int x, int y,
			      int width, int height,
			      int arcWidth, int arcHeight)
    {
	g1.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
	g2.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
    }

    @Override
    public Shape getClip() {
	return g1.getClip();
    }

    @Override
    public Rectangle getClipBounds() {
	return g1.getClipBounds();
    }

    @Override
    public Color getColor() {
	return g1.getColor();
    }

    @Override
    public Font getFont() {
	return g1.getFont();
    }

    @Override
    public FontMetrics getFontMetrics() {
	return g1.getFontMetrics();
    }

    @Override
    public FontMetrics getFontMetrics(Font f) {
	return g1.getFontMetrics(f);
    }

    @Override
    public boolean hitClip(int x, int y, int width, int height) {
	return g1.hitClip(x, y, width, height);
    }

    @Override
    public void setClip(int x, int y,
			int width, int height)
    {
	g1.setClip(x, y, width, height);
	g2.setClip(x, y, width, height);
    }

    @Override
    public void setClip(Shape clip) {
	g1.setClip(clip);
	g2.setClip(clip);
    }

    @Override
    public void setColor(Color c) {
	g1.setColor(c);
	g2.setColor(c);
    }

    @Override
    public void setFont(Font font) {
	g1.setFont(font);
	g2.setFont(font);
    }

    @Override
    public void setPaintMode() {
	g1.setPaintMode();
	g2.setPaintMode();
    }

    @Override
    public void setXORMode(Color c1) {
	g1.setXORMode(c1);
	g2.setXORMode(c1);
    }
}

//  LocalWords:  DRecorder paintComponent preferAlpha
//  LocalWords:  SplitterGraphics
