package org.bzdev.anim2d;
import org.bzdev.graphs.Graph;
import java.awt.*;
import java.awt.geom.*;
import java.util.function.DoubleSupplier;

/**
 * Create a line whose end points may vary with time.
 * A functional interface, {@link java.util.function.DoubleSupplier},
 * makes it possible to use lambda expressions or method references
 * to determine the line's end points.  Two path parameters can
 * be set so that only a portion of this line is visible.
 * <P>
 * One use of this class is to be able to visually determine if
 * some feature lies between two designated points whose position
 * changes with time.
 */
public class ConnectingLine2D extends AnimationObject2D  {

    private static String errorMsg(String key, Object... args) {
	return Animation2D.errorMsg(key, args);
    }
    /**
     * Constructor.
     * @param animation the animation
     * @param name the name of the object; null for an automatically generated
     *        name
     * @param intern true if the object can be looked up by using the methods
     * in {@link org.bzdev.devqsim.Simulation Simulation}; false otherwise.
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     * @see org.bzdev.devqsim.Simulation#getObject(String,Class)
     */
    public ConnectingLine2D(Animation2D animation,
			    String name,
			    boolean intern)
    {
	super(animation, name, intern);
	setZorder(0, false);
    }

    DoubleSupplier fx1;
    DoubleSupplier fy1;
    DoubleSupplier fx2;
    DoubleSupplier fy2;

    double u1 = 0.0;
    double u2 = 1.0;

    /**
     * Configure the line's end points.
     * @param getX1 the supplier that provides the X coordinate of
     *              the first end point
     * @param getY1 the supplier that provides the Y coordinate of
     *              the first end point
     * @param getX2 the supplier that provides the X coordinate of
     *              the second end point
     * @param getY2 the supplier that provides the Y coordinate of
     *              the second end point
     * @return this object
     */
    public ConnectingLine2D configure(DoubleSupplier getX1,
				      DoubleSupplier getY1,
				      DoubleSupplier getX2,
				      DoubleSupplier getY2)
    {
	fx1 = getX1;
	fy1 = getY1;
	fx2 = getX2;
	fy2 = getY2;
	return this;
    }

    /**
     * Configure the line's end points with the first endpoint fixed.
     * @param x the X coordinate of the first end point
     * @param y the Y coordinate of the first end point
     * @param getX2 the supplier that provides the X coordinate of
     *              the second end point
     * @param getY2 the supplier that provides the Y coordinate of
     *              the second end point
     * @return this object
     */
    public ConnectingLine2D configure(double x,
				      double y,
				      DoubleSupplier getX2,
				      DoubleSupplier getY2)
    {
	DoubleSupplier getX1 = new DoubleSupplier() {
		public double getAsDouble() {return x;}
	    };
	DoubleSupplier getY1 = new DoubleSupplier() {
		public double getAsDouble() {return y;}
	    };
	return configure(getX1, getY1, getX2, getY2);
    }

    /**
     * Configure the line's end points with the second endpoint fixed.
     * @param getX1 the supplier that provides the X coordinate of
     *              the first end point
     * @param getY1 the supplier that provides the Y coordinate of
     *              the first end point
     * @param x the X coordinate of the second end point
     * @param y the Y coordinate of the second end point
     * @return this object
     */
    public ConnectingLine2D configure(DoubleSupplier getX1,
				      DoubleSupplier getY1,
				      double x,
				      double y)
    {
	DoubleSupplier getX2 = new DoubleSupplier() {
		public double getAsDouble() {return x;}
	    };
	DoubleSupplier getY2 = new DoubleSupplier() {
		public double getAsDouble() {return y;}
	    };
	return configure(getX1, getY1, getX2, getY2);
    }

    /**
     * Configure the line's end points with the both endpoints fixed.
     * @param x1 the X coordinate of the first end point
     * @param y1 the Y coordinate of the first end point
     * @param x2 the X coordinate of the second end point
     * @param y2 the Y coordinate of the second end point
     * @return this object
     */
    public ConnectingLine2D configure(double x1, double y1,
				      double x2, double y2)
    {
	DoubleSupplier getX1 = new DoubleSupplier() {
		public double getAsDouble() {return x1;}
	    };
	DoubleSupplier getY1 = new DoubleSupplier() {
		public double getAsDouble() {return y1;}
	    };
	DoubleSupplier getX2 = new DoubleSupplier() {
		public double getAsDouble() {return x2;}
	    };
	DoubleSupplier getY2 = new DoubleSupplier() {
		public double getAsDouble() {return y2;}
	    };
	return configure(getX1, getY1, getX2, getY2);
    }


    /**
     * Configure path parameters for a line's end points.
     * If getX1, getY1, getX2, and getY2 are the suppliers passed to
     * {@link #configure(DoubleSupplier,DoubleSupplier,DoubleSupplier,DoubleSupplier)},
     * if P<sub>1</sub> is the point whose coordinates are provided by
     * getX1 and getY1, and if P<sub>2</sub> is the point whose
     * coordinates are provided by getX2 and getY2, then
     * a point P(u) on the line is given by
     * P(u) = P<sub>1</sub>(1-u) + P<sub>2</sub>u for u &isin; [0, 1].
     * @param u1 is the parameter u &isin; [0, 1] for the first end of
     *        the line
     * @param u2 is the parameter u &isin; [0, 1] for the second end
     *        of the line
     * @return this object
     */
    public ConnectingLine2D configure(double u1, double u2)
    {
	this.u1 = u1;
	this.u2 = u2;
	return this;
    }

    private Color color = new Color(168, 168, 255);

    /**
     * Set the color for a line drawing this path when addTo is called.
     * @param color the color
     */
    public void setColor(Color color) {
	this.color = color;
    }

    /**
     * Get the color for a line drawing this path when addTo is called.
     * @return the color
     */
    public Color getColor() {return color;}


    private Stroke stroke = new BasicStroke(1.0F);

    /**
     * Set the stroke for a line drawing this path when addTo is called.
     * @param stroke the stroke
     */
    public void setStroke(Stroke stroke) {
	this.stroke = stroke;
    }

    /**
     * Get the stroke for a line drawing this path when addTo is called.
     * @return the stroke
     */
    public Stroke getStroke() {return stroke;}

    private boolean gcsMode = false;

    /**
     * Set GCS mode.
     * When GCS mode is set the stroke's width and dash array are interpreted
     * in graph coordinate space units; otherwise in user-space units (the
     * default).
     * @param gcsMode true if the stroke's units are in graph coordinate space
     *        units; false if they are in user space units.
     */
    public void setGcsMode(boolean gcsMode) {
	this.gcsMode = gcsMode;
    }

    /**
     * Get GCS mode.
     * When GCS mode is set the stroke's width and dash array are interpreted
     * in graph coordinate space units; otherwise in user-space units.
     * @return true if in graph-coordinate-space mode; false if in
     *          user-space mode
     */
    public boolean getGcsMode() {return gcsMode;}


    @Override
    public void addTo(Graph graph, Graphics2D g2d, Graphics2D g2dgcs) {
	double x1 = fx1.getAsDouble();
	double y1 = fy1.getAsDouble();
	double x2 = fx2.getAsDouble();
	double y2 = fy2.getAsDouble();
	double omu1 = 1.0 - u1;
	double omu2 = 1.0 - u2;

	double xa = x1 * omu1 + x2 * u1;
	double ya = y1 * omu1 + y2 * u1;
	double xb = x1 * omu2 + x2 * u2;
	double yb = y1 * omu2 + y2 * u2;

	Line2D line = new Line2D.Double(xa, ya, xb, yb);

	if (gcsMode) {
	    Color oldColor = g2dgcs.getColor();
	    Stroke oldStroke = g2dgcs.getStroke();
	    try {
		g2dgcs.setColor(color);
		g2dgcs.setStroke(stroke);
		g2dgcs.draw(line);
	    } finally {
		g2dgcs.setColor(oldColor);
		g2dgcs.setStroke(oldStroke);
	    }
	} else {
	    Color oldColor = g2d.getColor();
	    Stroke oldStroke = g2d.getStroke();
	    try {
		g2d.setColor(color);
		g2d.setStroke(stroke);
		graph.draw(g2d, line);
	    } finally {
		g2d.setColor(oldColor);
		g2d.setStroke(oldStroke);
	    }
	}
    }

    
}

//  LocalWords:  IllegalArgumentException getObject getX getY isin
//  LocalWords:  DoubleSupplier addTo GCS gcsMode
