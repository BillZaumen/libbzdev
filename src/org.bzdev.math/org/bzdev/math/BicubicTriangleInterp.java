package org.bzdev.math;

/**
 * Interpolation based on cubic triangular Bernstein-B&eacute;zier patches.
 * <P>
 * <script>
 * MathJax = {
 *	  tex: {
 *	      inlineMath: [['$', '$'], ['\\(', '\\)']],
 *	      displayMath: [['$$', '$$'], ['\\[', '\\]']]}
 * };
 * </script>
 * <script id="MathJax-script" async
 *	    src="https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-chtml.js">
 * </script>
 * This interpolator computes values by using the following
 * expression:
 * $$ \sum_{|\lambda| = 3} \beta_\lambda B^3_\lambda(u,v,w) $$
 * <NOSCRIPT><blockquote>
 * <pre><span style="font-size: 125%;">
 *      <span style="font-size: 200%">&sum;</span><sub>|&lambda;|=3</sub> &beta;<sub>&lambda;</sub>B<sup>3</sup><sub>&lambda;</sub>(u,v,w)
 * </span></pre>
 * </blockquote></NOSCRIPT>
 * where u + v + w = 1, &lambda;= (&lambda;<sub>1</sub>,&lambda;<sub>2</sub>,&lambda;<sub>3</sub>)
 * represent three indices, each in the range [0,3], and
 * |&lambda;| is defined as
 * &lambda;<sub>1</sub>+&lambda;<sub>2</sub>+&lambda;<sub>3</sub>.
 * $B^3_\lambda (u,v,w)$
 * <!-- B<sup>3</sup><sub>&lambda;</sub>(u,v,w) -->
 * is a Bernstein polynomial of degree 3 over a triangle specified by
 * barycentric coordinates (u, v, w), and is defined by the equation
 * $$ B^3_\lambda (u,v,w) = \frac{3!}{\lambda_1!\lambda_2!\lambda_3!}
 *    u^{\lambda_1}v^{\lambda_2}w^{\lambda_3} $$
 * <NOSCRIPT><blockquote>
 * <pre><span style="font-size:125%;">
 *                  3!
 * B<sup>3</sup><sub>&lambda;</sub>(u,v,w) = -------- u<sup>&lambda;<sub>1</sub></sup>v<sup>&lambda;<sub>2</sub></sup>w<sup>&lambda;<sub>3</sub></sup>
 *              &lambda;<sub>1</sub>!&lambda;<sub>2</sub>!&lambda;<sub>3</sub>!
 * </span></pre>
 * </blockquote></NOSCRIPT>
 * <P>
 * By convention, we will use (u,v) as independent variables and set
 * w = 1 - (u + v).  The control points are located as follows:
 * <blockquote>
 * <pre>
 *                  (0,1)
 *                   030
 *                    *
 *                   / \
 *                  /   \
 *                 /     \
 *                /       \
 *           021 *---------* 120
 *              / \       / \
 *    [u = 0]  /   \     /   \  [w = 0]
 *   (v axis) /     \   /     \
 *           /    111\ /       \
 *      012 *---------*---------* 210
 *         / \       / \       / \
 *        /   \     /   \     /   \
 *       /     \   /     \   /     \
 *      /       \ /       \ /       \
 *     *---------*---------*---------*
 *    003       102       201       300
 *  (0,0)                          (1,0)
 *                 [v = 0]
 *                 (u axis)
 * </pre>
 * </blockquote>
 * The ordered pairs (0,0), (0,1), and (1,0) give the values of
 * (u,v) at the vertices of the triangular region, and the sequences
 * of three numbers denote the &lambda; indices labeling each
 * control point. On the boundaries, either u, v, or w is zero.
 *
 * In many of the methods described below, we use the notation (x, y)
 * instead of (u, w).  Some of the constructors allow a range for x
 * and a range for y to be specified. in this case
 * <blockquote>
 *     u = (x - x<sub>min</sub>) / (x<sub>max</sub> - x<sub>min</sub>)
 * <br>
 *     v = (y - y<sub>min</sub>) / (y<sub>max</sub> - y<sub>min</sub>)
 * </blockquote>
 * The default values are x<sub>min</sub> = 0, x<sub>max</sub> = 1,
 * y<sub>min</sub> = 0, and y<sub>max</sub> = 1.  For the defaults,
 *  u = x and v = y.
 */
public class BicubicTriangleInterp extends RealValuedFunctionTwo {

    static String errorMsg(String key, Object... args) {
	return MathErrorMsg.errorMsg(key, args);
    }

    private double[] cpoints;

    boolean altargs = false;
    double xmin = 0.0;
    double xmax = 1.0;
    double ymin = 0.0;
    double ymax = 1.0;

    double scalex = 1.0;
    double scaley = 1.0;
    double scalex2 = 1.0;
    double scaley2 = 1.0;
    double scalexy = 1.0;

    /**
     * Constructor using an array.
     * The array contains the control points whose indices are
     * 003, 012, 021, 030, 102, 111, 120, 201, 210, 300, listed in
     * that order.
     * @param inits the control points
     */
    public BicubicTriangleInterp(double[] inits) {
	cpoints =  new double[10];
	System.arraycopy(inits, 0, cpoints, 0, 10);
    }

    /**
     * Constructor using an array and specifying the min/max values
     * this function's domain.
     * The array contains the control points whose indices are
     * 003, 012, 021, 030, 102, 111, 120, 201, 210, 300, listed in
     * that order.
     * @param xmin the bound on the interpolation region, corresponding
     *        to u = 0, for  the first argument of f
     * @param xmax the bound on the interpolation region, corresponding
     *        to u = 1, for the first argument of f
     * @param ymin the bound on the interpolation region, corresponding
     *        to v = 0, for the second argument of f
     * @param ymax the bound on the interpolation region, corresponding
     *        to v = 1, for the second argument of f
     * @param inits the control points
     */
    public BicubicTriangleInterp(double xmin, double xmax,
				 double ymin, double ymax,
				 double[] inits)
    {

	cpoints =  new double[10];
	System.arraycopy(inits, 0, cpoints, 0, 10);
	if (xmin != 0.0 || ymin != 0.0 || xmax != 1.0 || ymax != 1.0) {
	    altargs = true;
	}
	this.xmin = xmin;
	this.ymin = ymin;
	this.xmax = xmax;
	this.ymax = ymax;

	scalex = xmax - xmin;
	scaley = ymax - ymin;
	scalex2 = scalex*scalex;
	scaley2 = scaley*scaley;
	scalexy = scalex*scaley;
    }

    /**
     * Constructor using explicit arguments.
     * The arguments provide the control points.
     * @param p003 the control point indexed by &lambda;=(0,0,3)
     * @param p012 the control point indexed by &lambda;=(0,1,2)
     * @param p021 the control point indexed by &lambda;=(0,2,1)
     * @param p030 the control point indexed by &lambda;=(0,3,0)
     * @param p102 the control point indexed by &lambda;=(1,0,2)
     * @param p111 the control point indexed by &lambda;=(1,1,1)
     * @param p120 the control point indexed by &lambda;=(1,2,0)
     * @param p201 the control point indexed by &lambda;=(2,0,1)
     * @param p210 the control point indexed by &lambda;=(2,1,0)
     * @param p300 the control point indexed by &lambda;=(3,0,0)
     */
    public BicubicTriangleInterp
	(double p003, double p012, double p021, double p030,
	 double p102, double p111, double p120,
	 double p201, double p210,
	 double p300)
    {
	double[] cpoints = {p003, p012, p021, p030, p102, p111, p120,
			    p201, p210, p300};
	this.cpoints = cpoints;
    }

    /**
     * Constructor using explicit arguments and the minimum and
     * maximum values of this functions domain.
     * The arguments provide the control points.
     * @param xmin the lower bound of the interpolation region for
     *        the first argument of f
     * @param xmax the upper bound of the interpolation region for
     *        the first argument of f
     * @param ymin the lower bound of the interpolation region for
     *        the second argument of f
     * @param ymax the upper bound of the interpolation region for
     *        the second argument of f
     * @param p003 the control point indexed by &lambda;=(0,0,3)
     * @param p012 the control point indexed by &lambda;=(0,1,2)
     * @param p021 the control point indexed by &lambda;=(0,2,1)
     * @param p030 the control point indexed by &lambda;=(0,3,0)
     * @param p102 the control point indexed by &lambda;=(1,0,2)
     * @param p111 the control point indexed by &lambda;=(1,1,1)
     * @param p120 the control point indexed by &lambda;=(1,2,0)
     * @param p201 the control point indexed by &lambda;=(2,0,1)
     * @param p210 the control point indexed by &lambda;=(2,1,0)
     * @param p300 the control point indexed by &lambda;=(3,0,0)
     */
    public BicubicTriangleInterp
	(double xmin, double xmax, double ymin, double ymax,
	 double p003, double p012, double p021, double p030,
	 double p102, double p111, double p120,
	 double p201, double p210,
	 double p300)
    {
	double[] cpoints = {p003, p012, p021, p030, p102, p111, p120,
			    p201, p210, p300};
	this.cpoints = cpoints;
	if (xmin != 0.0 || ymin != 0.0 || xmax != 1.0 || ymax != 1.0) {
	    altargs = true;
	}
	this.xmin = xmin;
	this.ymin = ymin;
	this.xmax = xmax;
	this.ymax = ymax;

	scalex = xmax - xmin;
	scaley = ymax - ymin;
	scalex2 = scalex*scalex;
	scaley2 = scaley*scaley;
	scalexy = scalex*scaley;
    }


    @Override
    public double getDomainMin1() {return (xmin < xmax)? xmin: xmax;}

    @Override
    public double getDomainMax1() {return (xmin < xmax)? xmax: xmin;}

    @Override
    public double getDomainMin2() {return (ymin < ymax)? ymin: ymax;}

    @Override
    public double getDomainMax2() {return (ymin < ymax)? ymax: ymin;}

    @Override
    public boolean isInDomain(double x, double y)
	throws UnsupportedOperationException
    {
	boolean result = super.isInDomain(x, y);
	if (altargs) {
	    x = (x - xmin)/scalex;
	    y = (y - ymin)/scaley;
	}
	return result && (x + y <= 1.0);
    }

    /**
     * Get the normalized barycentric coordinate u given the coordinates
     * (x, y).
     * The barycentric coordinates are denoted as (u, v, w) with
     * the constraint that u + v + w = 1 and with each coordinate
     * in the range [0, 1].
     * @param x the first argument for this interpolator's function
     * @param y the second argument for this interpolator's function
     * @return the value of u
     */
    public double uFromXY(double x, double y) {
	if (altargs) {
	    return (x - xmin) /scalex;
	} else {
	    return x;
	}

    }

    /**
     * Get the normalized barycentric coordinate v given the coordinates
     * (x, y).
     * The barycentric coordinates are denoted as (u, v, w) with
     * the constraint that u + v + w = 1 and with each coordinate
     * in the range [0, 1].
     * @param x the first argument for this interpolator's function
     * @param y the second argument for this interpolator's function
     * @return the value of v
     */
    public double vFromXY(double x, double y) {
	if (altargs) {
	    return (y - ymin) /scaley;
	} else {
	    return y;
	}
    }

    /**
     * Get the normalized barycentric coordinate w given the coordinates
     * (x, y).
     * The barycentric coordinates are denoted as (u, v, w) with
     * the constraint that u + v + w = 1 and with each coordinate
     * in the range [0, 1].
     * @param x the first argument for this interpolator's function
     * @param y the second argument for this interpolator's function
     * @return the value of w
     */
    public double wFromXY(double x, double y) {
	if (altargs) {
	    x = (x - xmin)/scalex;
	    y = (y - ymin) /scaley;
	}
	return 1.0 - (x + y);
    }

    /**
     * Get the X coordinate given the barycentric coordinates u and v.
     * The barycentric coordinates are denoted as (u, v, w) with
     * the constraint that u + v + w = 1 and with each coordinate
     * in the range [0, 1].
     * @param u the barycentric coordinate u
     * @param v the barycentric coordinate v
     * @return the corresponding X coordinate
     */
    public double xFromUV(double u, double v) {
	if (altargs) {
	    u = u*scalex + xmin;
	}
	return u;
    }

    /**
     * Get the X coordinate given the barycentric coordinates w and u.
     * The barycentric coordinates are denoted as (u, v, w) with
     * the constraint that u + v + w = 1 and with each coordinate
     * in the range [0, 1].
     * @param w the barycentric coordinate w
     * @param u the barycentric coordinate u
     * @return the corresponding X coordinate
     */
    public double xFromWU(double w, double u) {
	if (altargs) {
	    u = u*scalex + xmin;
	}
	return u;
    }

    /**
     * Get the X coordinate given the barycentric coordinates v and w.
     * The barycentric coordinates are denoted as (u, v, w) with
     * the constraint that u + v + w = 1 and with each coordinate
     * in the range [0, 1].
     * @param v the barycentric coordinate v
     * @param w the barycentric coordinate w
     * @return the corresponding X coordinate
     */
    public double xFromVW(double v, double w) {
	double u = 1.0 - (v + w);
	if (altargs) {
	    u = u*scalex + xmin;
	}
	return u;
    }

    /**
     * Get the Y coordinate given the barycentric coordinates u and v.
     * The barycentric coordinates are denoted as (u, v, w) with
     * the constraint that u + v + w = 1 and with each coordinate
     * in the range [0, 1].
     * @param u the barycentric coordinate u
     * @param v the barycentric coordinate v
     * @return the corresponding Y coordinate
     */
    public double yFromUV(double u, double v) {
	if (altargs) {
	    v = v*scaley + ymin;
	}
	return v;
    }

    /**
     * Get the Y coordinate given the barycentric coordinates w and u.
     * The barycentric coordinates are denoted as (u, v, w) with
     * the constraint that u + v + w = 1 and with each coordinate
     * in the range [0, 1].
     * @param w the barycentric coordinate w
     * @param u the barycentric coordinate u
     * @return the corresponding Y coordinate
     */
    public double yFromWU(double w, double u) {
	double v = 1.0 - (w + u);
	if (altargs) {
	    v = v*scaley + ymin;
	}
	return v;
    }

    /**
     * Get the Y coordinate given the barycentric coordinates v and w.
     * The barycentric coordinates are denoted as (u, v, w) with
     * the constraint that u + v + w = 1 and with each coordinate
     * in the range [0, 1].
     * @param v the barycentric coordinate v
     * @param w the barycentric coordinate w
     * @return the corresponding Y coordinate
     */
    public double yFromVW(double v, double w) {
	if (altargs) {
	    v = v*scaley + ymin;
	}
	return v;
    }


    /**
     * Get the value of second argument to the function represented by
     * this interpolator  corresponding to a particular value of the
     * first argument when w is zero.
     * @param x the value of the first argument
     * @return the value of the second argument
     */
    public double yForZeroW(double x) {
	if (altargs) {
	    return (1.0 - (x-xmin)/scalex)*scaley + ymin;
	} else {
	    return 1.0 - x;
	}
    }

    /**
     * Get the value of first argument to the function represented by
     * this interpolator corresponding to a particular value of the
     * second argument when w is zero.
     * @param y the value of the second argument
     * @return the value of the first argument
     */
    public double xForZeroW(double y) {
	if (altargs) {
	    return (1.0 - (y-ymin)/scaley)*scalex + xmin;
	} else {
	    return 1.0 - y;
	}
    }

    @Override
    public double valueAt(double x, double y) {
	if (altargs) {
	    double u = (x - xmin)/scalex;
	    double v = (y - ymin)/scaley;
	    return Functions.Bernstein.sumB(cpoints, 3, u, v, 1.0 - (u + v));
	} else {
	    return Functions.Bernstein.sumB(cpoints, 3, x, y, 1.0 - (x + y));
	}
    }

    @Override
    public double deriv1At(double x, double y) {
	if (altargs) {
	    double u = (x - xmin)/scalex;
	    double v = (y - ymin)/scaley;
	    double w = 1.0 - (u + v);
	    return (Functions.Bernstein.dsumBdx(0, cpoints, 3, u, v, w)
		    - Functions.Bernstein.dsumBdx(2, cpoints, 3, u, v, w))
		/ scalex;
	} else {
	    double w = 1.0 - (x + y);
	    return Functions.Bernstein.dsumBdx(0, cpoints, 3, x, y, w)
		- Functions.Bernstein.dsumBdx(2, cpoints, 3, x, y, w);
	}
    }

    @Override
    public double deriv2At(double x, double y) {
	if (altargs) {
	    double u = (x - xmin)/scalex;
	    double v = (y - ymin)/scaley;
	    double w = 1.0 - (u + v);
	    return (Functions.Bernstein.dsumBdx(1, cpoints, 3, u, v, w)
		    - Functions.Bernstein.dsumBdx(2, cpoints, 3, u, v, w))
		/ scaley;
	} else {
	    double w = 1.0 - (x + y);
	    return Functions.Bernstein.dsumBdx(1, cpoints, 3, x, y, w)
		- Functions.Bernstein.dsumBdx(2, cpoints, 3, x, y, w);
	}
    }


    @Override
    public double deriv11At(double x, double y) {
	if (altargs) {
	    double u = (x - xmin)/scalex;
	    double v = (y - ymin)/scaley;
	    double w = 1.0 - (u + v);
	    return (Functions.Bernstein.d2sumBdxdy(0, 0, cpoints, 3, u, v, w)
		    - 2.0 * Functions.Bernstein.d2sumBdxdy(0, 2, cpoints, 3,
							   u, v, w)
		    + Functions.Bernstein.d2sumBdxdy(2, 2, cpoints, 3, u, v, w))
		/ scalex2;
	} else {
	    double w = 1.0 - (x + y);
	    return Functions.Bernstein.d2sumBdxdy(0, 0, cpoints, 3, x, y, w)
		- 2.0 * Functions.Bernstein.d2sumBdxdy(0, 2, cpoints, 3,
						       x, y, w)
		+ Functions.Bernstein.d2sumBdxdy(2, 2, cpoints, 3, x, y, w);
	}
    }

    @Override
    public double deriv12At(double x, double y) {
	if (altargs) {
	    double u = (x - xmin)/scalex;
	    double v = (y - ymin)/scaley;
	    double w = 1.0 - (u + v);
	    return (Functions.Bernstein.d2sumBdxdy(0, 1, cpoints, 3, u, v, w)
		    - Functions.Bernstein.d2sumBdxdy(0, 2, cpoints, 3, u, v, w)
		    - Functions.Bernstein.d2sumBdxdy(2, 1, cpoints, 3, u, v, w)
		    + Functions.Bernstein.d2sumBdxdy(2, 2, cpoints, 3, u, v, w))
		/ scalexy;
	} else {
	    double w = 1.0 - (x + y);
	    return Functions.Bernstein.d2sumBdxdy(0, 1,
						  cpoints, 3, x, y, w)
		- Functions.Bernstein.d2sumBdxdy(0, 2, cpoints, 3, x, y, w)
		- Functions.Bernstein.d2sumBdxdy(2, 1, cpoints, 3, x, y, w)
		+ Functions.Bernstein.d2sumBdxdy(2, 2, cpoints, 3, x, y, w);
	}
    }

    @Override
    public double deriv21At(double x, double y) {
	if (altargs) {
	    double u = (x - xmin)/scalex;
	    double v = (y - ymin)/scaley;
	    double w = 1.0 - (u + v);
	    return (Functions.Bernstein.d2sumBdxdy(1, 0, cpoints, 3, u, v, w)
		    - Functions.Bernstein.d2sumBdxdy(1, 2, cpoints, 3, u, v, w)
		    - Functions.Bernstein.d2sumBdxdy(2, 0, cpoints, 3, u, v, w)
		    + Functions.Bernstein.d2sumBdxdy(2, 2, cpoints, 3, u, v, w))
		/ scalexy;
	} else {
	    double w = 1.0 - (x + y);
	    return Functions.Bernstein.d2sumBdxdy(1, 0, cpoints, 3, x, y, w)
		- Functions.Bernstein.d2sumBdxdy(1, 2, cpoints, 3, x, y, w)
		- Functions.Bernstein.d2sumBdxdy(2, 0, cpoints, 3, x, y, w)
		+ Functions.Bernstein.d2sumBdxdy(2, 2, cpoints, 3, x, y, w);
	}
    }

    @Override
    public double deriv22At(double x, double y) {
	if (altargs) {
	    double u = (x - xmin)/scalex;
	    double v = (y - ymin)/scaley;
	    double w = 1.0 - (u + v);
	    return (Functions.Bernstein.d2sumBdxdy(1, 1, cpoints, 3, u, v, w)
		    - 2.0 * Functions.Bernstein.d2sumBdxdy(1, 2, cpoints, 3,
							   u, v, w)
		    + Functions.Bernstein.d2sumBdxdy(2, 2, cpoints, 3, u, v, w))
		/ scaley2;
	} else {
	    double w = 1.0 - (x + y);
	    return Functions.Bernstein.d2sumBdxdy(1, 1, cpoints, 3, x, y, w)
		- 2.0 * Functions.Bernstein.d2sumBdxdy(1, 2, cpoints, 3,
						       x, y, w)
		+ Functions.Bernstein.d2sumBdxdy(2, 2, cpoints, 3, x, y, w);
	}
    }
}

//  LocalWords:  eacute zier interpolator blockquote pre barycentric
//  LocalWords:  inits xmin xmax ymin ymax interpolator's
