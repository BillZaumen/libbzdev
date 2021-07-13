package org.bzdev.graphs.symbols;
import org.bzdev.graphs.Graph;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.Graphics2D;

/**
 * Graph-symbol class for a filled circle.
 */
public class SolidCircle extends Graph.Symbol {

    static final Shape circle =
	new Ellipse2D.Double(-5.0, -5.0, 10.0, 10.0);

    protected Graph.UserGraphic getUserGraphic(boolean xAxisPointsRight,
					       boolean yAxisPointsDown)
    {
	return new Graph.UserGraphic() {
	    public void addTo(Graph graph, Graphics2D g2d) {
		g2d.draw(circle);
		g2d.fill(circle);
	    }
	};
    }

    @Override
    protected double getEBarStartRight(boolean xAxisPointsRight,
				       boolean yAxisPointsDown)
    {
	return 5.0;
    }

    @Override
    protected double getEBarStartLeft(boolean xAxisPointsRight,
				      boolean yAxisPointsDown)
    {
	return -5.0;
    }

    @Override
    protected double getEBarStartTop(boolean xAxisPointsRight,
				 boolean yAxisPointsDown)
    {
	return -5.0;
    }

    @Override
    protected double getEBarStartBottom(boolean xAxisPointsRight,
				 boolean yAxisPointsDown)
    {
	return 5.0;
    }
 }
