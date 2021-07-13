package org.bzdev.graphs.symbols;
import org.bzdev.graphs.Graph;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;

/**
 * Graph-symbol class for an unfilled square symbol.
 */
public class EmptySquare extends Graph.Symbol {

    static final Shape square =
	new Rectangle2D.Double(-5.0, -5.0, 10.0, 10.0);

    @Override
    protected Graph.UserGraphic getUserGraphic(boolean xAxisPointsRight,
					       boolean yAxisPointsDown)
    {
	return new Graph.UserGraphic() {
	    public void addTo(Graph graph, Graphics2D g2d) {
		g2d.draw(square);
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
