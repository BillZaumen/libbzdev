package org.bzdev.graphs.symbols;
import org.bzdev.graphs.Graph;

import java.awt.geom.Path2D;
import java.awt.Graphics2D;

/**
 * Graph symbol for a 'bow tie' with an empty interior
 *
 */
public class EmptyBowtie extends Graph.Symbol {

    static final Path2D triangle1 = new Path2D.Double();

    static final Path2D triangle2 = new Path2D.Double();

    static {
	triangle1.moveTo(0.0, 0.0);
	triangle1.lineTo(5.0, -5.0);
	triangle1.lineTo(5.0, 5.0);
	triangle1.closePath();

	triangle2.moveTo(0.0, 0.0);
	triangle2.lineTo(-5.0, 5.0);
	triangle2.lineTo(-5.0, -5.0);
	triangle2.closePath();
    }

    @Override
    protected Graph.UserGraphic getUserGraphic(boolean xAxisPointsRight,
					       boolean yAxisPointsDown)
    {
	return new Graph.UserGraphic() {
	    public void addTo(Graph graph, Graphics2D g2d) {
		g2d.draw(triangle1);
		g2d.draw(triangle2);
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
	return 0.0;
    }

    @Override
    protected double getEBarStartBottom(boolean xAxisPointsRight,
				 boolean yAxisPointsDown)
    {
	return 0.0;
    }
 }
