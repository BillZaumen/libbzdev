package org.bzdev.math;
import java.util.Arrays;

//@exbundle org.bzdev.math.lpack.Math

/**
 * Class representing polynomials using a monomial basis.
 * Methods include ones for computing a polynomial's value,
 * obtaining or modifying a polynomial's coefficients,
 * computing integrals and derivatives of a polynomial, and
 * adding, multiplying or dividing polynomials.
 * <P>
 * The class {@link Polynomials} has a series of static methods
 * that can be used to add, multiply, and divide polynomials.
 * It also has methods that perform these operations on arrays
 * representing a polynomial's coefficients. Whether decides to
 * use the {@link Polynomials} class or the corresponding methods
 * in this class is dependent more on coding style than anything
 * else (for addition, multiplication and division, the methods
   in this class use the methods in the {@link Polynomials} class).
 */
public class Polynomial extends RealValuedFunction {

    static String errorMsg(String key, Object... args) {
	return MathErrorMsg.errorMsg(key, args);
    }

    int degree;
    double[] coefficients;

    static final double[] EMPTY_COEFFICIENTS = new double[0];

    /**
     * Constructor.
     */
    public Polynomial() {
	super();
	degree = -1;
	coefficients = EMPTY_COEFFICIENTS;
    }

    /**
     * Constructor based on an existing polynomial.
     * @param p the existing polynomial
     */
    public Polynomial(Polynomial p) {
	this(p.coefficients, p.degree);
    }


    /**
    * Constructor specifying a degree.
    * The degree is  used to set an internal table size.
    * @param degree the degree of a polynomial that this
    *        instance should support.
    */
    public Polynomial (int degree) {
	super();
	this.degree = -1;
	coefficients = new double[degree+1];
    }

    /**
    * Constructor specifying coefficients and a degree.
     * Coefficients are order so that, for a polynomial
     * p(x) = a<sub>0</sub> + a<sub>1</sub>x + ... + a<sub>n</sub>x<sup>n</sup>,
     * the coefficient for the x<sup>i</sup> term is stored in an
     * array at index i, thus making a<sub>0</sub> the first index in p's
     * coefficient array.
    * The degree is  used to set an internal table size.
    * @param coefficients the coefficients for this polynomial.
    * @param degree the degree of a polynomial that this
    *        instance should support.
    */
    public Polynomial(double[] coefficients, int degree) {
	super();
	this.coefficients = new double[degree+1];
	System.arraycopy(coefficients, 0, this.coefficients, 0, degree+1);
	while (degree >= 0 && coefficients[degree] == 0.0) degree--;
	this.degree = degree;

    }

    /**
    * Constructor specifying coefficients.
     * Coefficients are order so that, for a polynomial
     * p(x) = a<sub>0</sub> + a<sub>1</sub>x + ... + a<sub>n</sub>x<sup>n</sup>,
     * the coefficient for the x<sup>i</sup> term is stored in an
     * array at index i, thus making a<sub>0</sub> the first index in p's
     * coefficient array.
    * @param coefficients the coefficients for this polynomial.
    */
    public Polynomial(double... coefficients) {
	super();
	int degree = coefficients.length - 1;
	this.coefficients = new double[degree+1];
	System.arraycopy(coefficients, 0, this.coefficients, 0, degree+1);
	while (degree >= 0 && coefficients[degree] == 0.0) degree--;
	this.degree = degree;
    }

    /**
     * Set the coefficients for this polynomial.
     * Coefficients are order so that, for a polynomial
     * p(x) = a<sub>0</sub> + a<sub>1</sub>x + ... + a<sub>n</sub>x<sup>n</sup>,
     * the coefficient for the x<sup>i</sup> term is stored in an
     * array at index i, thus making a<sub>0</sub> the first index in p's
     * coefficient array.
     * @param coefficients the coefficients
     */
    public void setCoefficients(double[] coefficients) {
	this.degree = coefficients.length-1;
	if (coefficients.length > this.coefficients.length) 
	    this.coefficients = new double[coefficients.length];
	System.arraycopy(coefficients, 0, this.coefficients, 0,
			 coefficients.length);
	while (degree >= 0 && coefficients[degree] == 0.0) degree--;
    }
    
    /**
     * Reset this polynomial so that it is empty.
     * If necessary, enough space will be allocated to store the
     * coefficients of a polynomial whose degree is equal to or
     * less than the argument, but the internal array will not be modified
     * unless a new array has to be allocated, and if the array is
     * extended, the old values will be preserved.
     * <P>
     * This method is appropriate when one will set all of the coefficients
     * explicitly or when the old values need to be temporarily preserved
     * (this can happen in cases where the same polynomial is used as
     * two arguments, one of which will be modified).
     * @param degree the degree
     */
    public void softReset(int degree) {
	this.degree = -1;
	if (coefficients.length < degree+1) {
	    double[] tmp = new double[degree+1];
	    System.arraycopy(coefficients, 0, tmp, 0, coefficients.length);
	    coefficients = tmp;
	}
    }

    /**
     * Reset this polynomial so that it is empty but with space
     * allocated for polynomials whose degree is no larger than
     * the one specified. The internal array's elements for indices
     * in the range [0,degree] will be set to zero.
     * @param degree the degree
     */
    public void reset(int degree) {
	this.degree = -1;
	if (coefficients.length < degree+1) {
	    coefficients = new double[degree+1];
	} else {
	    Arrays.fill(coefficients, 0, degree+1, 0.0);
	}
    }

    /**
     * Set the degree and coefficients of this polynomial to match
     * those of a specified polynomial.
     * @param p the specified polynomial
     */
    public void setTo(Polynomial p) {
	degree = p.degree;
	if (coefficients.length < degree+1) {
	    coefficients = (p.degree == -1)? EMPTY_COEFFICIENTS:
		new double[p.degree+1];
	}
	if (degree > -1) {
	    System.arraycopy(p.coefficients, 0, coefficients, 0, degree+1);
	}
    }


    /**
     * Set the coefficients of this polynomial, specifying a degree.
     * Coefficients are order so that, for a polynomial
     * p(x) = a<sub>0</sub> + a<sub>1</sub>x + ... + a<sub>n</sub>x<sup>n</sup>,
     * the coefficient for the x<sup>i</sup> term is stored in an
     * array at index i, thus making a<sub>0</sub> the first index in p's
     * coefficient array.
     * @param coefficients the coefficients (only those indices in the range
     *        [0, degree] will be used)
     * @param degree the degree of this polynomial
     */
    public void setCoefficients(double[] coefficients, int degree)
	throws IllegalArgumentException
    {
	int dp1 = degree+1;
	if (dp1 > coefficients.length) {
	    throw new IllegalArgumentException(errorMsg("argArrayTooShort"));
	}
	if (this.coefficients == coefficients) {
	    return;
	}
	if (coefficients.length < dp1) {
	    this.coefficients = new double[dp1];
	}
	System.arraycopy(coefficients, 0, this.coefficients, 0, dp1);
	while (degree >= 0 && coefficients[degree] == 0.0) degree--;
	this.degree = degree;
    }

    /**
     * Get the degree of this polynomial.
     * @return the degree; -1 if the polynomial is empty (i.e.,
     *         no coefficients have been provided)
     */
    public int getDegree() {
	return degree;
    }

    /**
     * Get the array used to store coefficients for this polynomial.
     * The size of the array is guaranteed to be at least 1 larger
     * then the degree of this polynomial. Modifying this array may
     * change the polynomial.
     * Coefficients are ordered so that, for a polynomial
     * p(x) = a<sub>0</sub> + a<sub>1</sub>x + ... + a<sub>n</sub>x<sup>n</sup>,
     * the coefficient for the x<sup>i</sup> term is stored in an
     * array at index i, thus making a<sub>0</sub> the first index in p's
     * coefficient array.
     * @return the array
     */
    public double[] getCoefficientsArray() {return coefficients;}

    /**
     * Get the coefficients for this polynomial
     * Coefficients are order so that, for a polynomial
     * p(x) = a<sub>0</sub> + a<sub>1</sub>x + ... + a<sub>n</sub>x<sup>n</sup>,
     * the coefficient for the x<sup>i</sup> term is stored in an
     * array at index i, thus making a<sub>0</sub> the first index in p's
     * coefficient array.
     * The size of the array will be the next integer larger than the
     * this polynomial's degree. Modifying the array returned by this
     * method will not change the polynomial.
     * @return the coefficients
     */
    public double[] getCoefficients() {
	if (degree == -1) return EMPTY_COEFFICIENTS;
	double[] result = new double[degree+1];
	System.arraycopy(coefficients, 0, result, 0, degree+1);
	return result;
    }

    @Override
    public double valueAt(double x) {
	if (degree < 0) return 0.0;
	// Use Kahan's addition algorithm for accuracy
	double c = 0;
	double sum = coefficients[0];
	double prod = x;
	for (int i = 1; i <= degree; i++) {
	    double y = coefficients[i]*prod - c;
	    double t = sum + y;
	    c = (t - sum) - y;
	    sum = t;
	    prod *= x;
	}
	return sum;
    }

    /**
     * Integrate a polynomial.
     * For a polynomial P(x) The integral I(x) = &int;P(x) is
     * implemented so that I(0) = 0.  If this polynomial's degree
     * as returned by {@link #getDegree()} is -1, which indicates that
     * the polynomial is not defined, a
     * new polynomial with a degree of -1 will be returned.
     * @return the integral of this polynomial
     */
    public Polynomial integral() {
	if (degree < 0) return new Polynomial();
	int rdegree = (degree == 0 && coefficients[0] == 0.0) ? degree:
	    degree + 1;
	Polynomial result = new Polynomial(rdegree);
	result.coefficients[0] = 0.0;
	result.degree = rdegree;
	if (degree == 0) {
	    if (coefficients[0] != 0.0) {
		result.coefficients[1] = coefficients[0];
	    }
	} else {
	    result.coefficients[1] = coefficients[0];
	    for (int i = 2; i <= rdegree; i++) {
		result.coefficients[i] = coefficients[i-1]/i;
	    }
	}
	return result;
    }

    /**
     * Evaluated the integral of a polynomial.
     * For a polynomial P(x) The integral I(x) = &int;P(x) is
     * implemented so that I(0) = 0.
     * @param x the polynomial's argument
     * @return the integral of the polynomial, evaluated at x
     */
    public double integralAt(double x) {
	if (degree < 1) return 0.0;
	double prod = x;
	// use Kahan's addition algorithm to reduce
	// floating-point errors: at the roots of a
	// polynomial, the sum is zero.
	double c = 0;
	double sum = coefficients[0]*x;
	int i = 1;
	while (i <= degree) {
	    prod *= x;
	    int ind = (i++);
	    double y = coefficients[ind]*prod/(i) - c;
	    double t = sum + y;
	    c = (t  - sum) - y;
	    sum = t;
	}
	return sum;
    }

    @Override
    public Polynomial deriv() {
	if (degree < 0) return new Polynomial();
	else if (degree == 0) {
	    Polynomial r = new Polynomial(0);
	    r.degree = 0;
	    return r;
	}
	Polynomial result = new Polynomial(degree - 1);
	for (int i = 1; i <= degree; i++) {
	    result.coefficients[i-1] = i*coefficients[i];
	}
	result.degree = degree - 1;
	while (result.degree > 0
	       && result.coefficients[result.degree] == 0.0) {
	    result.degree--;
	}
	return result;
    }

    @Override
    public double derivAt(double x) {
	if (degree < 1) return 0.0;
	// Use Kahan's addition algorithm for accuracy
	double c = 0;
	double sum = coefficients[1];
	double prod = x;
	for (int i = 2; i <= degree; i++) {
	    double y = i*coefficients[i]*prod - c;
	    double t = sum + y;
	    c = (t - sum) - y;
	    sum = t;
	    prod *= x;
	}
	return sum;
    }

    @Override
    public Polynomial secondDeriv() {
	if (degree <= 2) return new Polynomial();
	Polynomial result = new Polynomial(degree - 2);
	int last = 1;
	for (int i = 2; i <= degree; i++) {
	    result.coefficients[i-2] = i*last*coefficients[i];
	    last = i;
	}
	result.degree = degree - 2;
	while (result.degree >= 0
	       && result.coefficients[result.degree] == 0.0) {
	    result.degree--;
	}
	return result;
    }


    @Override
    public double secondDerivAt(double x) {
	if (degree < 2) return 0.0;
	// Use Kahan's addition algorithm for accuracy
	double c = 0;
	double sum = 2*coefficients[2];
	double prod = x;
	int last = 2;
	for (int i = 3; i <= degree; i++) {
	    double y = i*last*coefficients[i]*prod - c;
	    double t = sum + y;
	    c = (t - sum) - y;
	    sum = t;
	    prod *= x;
	    last = i;
	}
	return sum;
    }

    /**
     * Multiply this polynomial by adding another polynomial to this
     * polynomial.
     * This polynomial is modified.
     * @param p the polynomial by which this polynomial is incremented
     */
    public void incrBy(Polynomial p) {
	Polynomials.add(this, this, p);
    }

    /**
     * Modify this polynomial by multiplying it by a scalar.
     * This polynomial is modified.
     * @param s the scalar
     */
    public void multiplyBy(double s) {
	Polynomials.multiply(this, s, this);
    }

    /**
     * Modify this polynomial by multiplying it by another polynomial.
     * This polynomial is modified.
     * @param p the polynomial by which this polynomial is multiplied
     */
    public void multiplyBy(Polynomial p) {
	Polynomials.multiply(this, this, p);
    }

    /**
     * Compute the sum of this polynomial and another polynomial.
     * This polynomial is not modified.
     * @param p the polynomial to add to this polynomial
     * @return a new polynomial that is equal to the sum of this
     *         polynomial and the polynomial p
     */
    public Polynomial add(Polynomial p) {
	return Polynomials.add(this, p);
    }

    /**
     * Compute the product of this polynomial and another polynomial.
     * This polynomial is not modified.
     * @param p the polynomial by which this polynomial is multiplied
     * @return a new polynomial that is equal to the product of this
     *         polynomial and the polynomial p
     *
     */
    public Polynomial multiply(Polynomial p) {
	return Polynomials.multiply(this, p);
    }

    /**
     * Compute the product of this polynomial and a scalar.
     * This polynomial is not modified.
     * @param s the scalar by which this polynomial is multiplied
     * @return a new polynomial that is equal to the product of this
     *         polynomial and the scalar s
     */
    public Polynomial multiply(double s) {
	return Polynomials.multiply(s, this);
    }

    /**
     * Divide this polynomial by another polynomial and return either
     * the quotient or the remainder.
     * This polynomial is not modified.
     * <P>
     * Please see
     * {@link Polynomials#divide(double[],double[],double[],int,double[],int)}
     * for documentation regarding numerical accuracy.
     * @param p the polynomial by which this polynomial is divided
     * @param isQuotient true if the quotient polynomial is returned;
     *        false if the remainder polynomial is returned
     * @return a new polynomial whose value is either the quotient or
     *         the remainder when this polynomial is divided by the
     *         polynomial p
     */
    public Polynomial divide(Polynomial p, boolean isQuotient) {
	return Polynomials.divide(this, p, isQuotient);
    }
}

//  LocalWords:  p's Kahan's getDegree isQuotient
