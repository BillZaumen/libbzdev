package org.bzdev.math.stats;
import org.bzdev.math.Functions;

/**
 * Class providing methods for Gaussian/Normal distributions.
 * The use of A, P and Q follows the convention in Abramowitz and
 * Stegun, "Handbook of Mathematical Functions" (10th printing [1972],
 * 9th Dover printing), chapter 26. Some of the methods have names
 * that start with an upper-case letter, contrary to the usual Java
 * convention, in order to conform to this text.
 */
public class GaussianDistr extends ProbDistribution {
    
    private double mu;
    private double sigma;

    @Override
    public boolean isSymmetric(double x) {return x == mu;}

    /**
     * Constructor.
     * The mean is 0.0 and the standard deviation is 1.0.
     */
    public GaussianDistr() {mu = 0.0; sigma = 1.0;}


    /**
     * Constructor given a mean and standard deviation.
     * @param mu the mean value for the distribution (&mu;)
     * @param sigma the standard deviation for the distribution (&sigma;)
     */
    public GaussianDistr(double mu, double sigma) {
	this.mu = mu;
	this.sigma = sigma;
    }

    @Override
    public double pd(double x) {
	return pd(x, mu, sigma);
    }

    @Override
    public double P(double x) {
	return P(x, mu, sigma);
    }

    @Override
    public double Q(double x) {
	return Q(x, mu, sigma);
    }


    @Override
    public double A(double x) {
	return A(x, mu, sigma);
    }

    static final double ROOT_2PI = Math.sqrt(2*Math.PI);
    static final double ROOT_2 = Math.sqrt(2.0);

    /**
     * Compute the probability density for a Gaussian distribution
     * with a variance of &sigma;<sup>2</sup> and a mean of &mu;.
     * @param x a value in the range [-&infin;,&infin;]
     * @param mu the mean &mu;
     * @param sigma the standard deviation &sigma;
     * @return the probability density
     */
    public static double pd(double x, double mu, double sigma) {
	double xx = (x - mu)/sigma;
	return Math.exp(-xx*xx/2.0)/(ROOT_2PI*sigma);
    }


    /**
     * Compute the cumulative probability function for a Gaussian distribution
     * with a variance of &sigma;<sup>2</sup> and a mean of &mu;.
     * @param x a value in the range [-&infin;,&infin;]
     * @param mu the mean &mu;
     * @param sigma the standard deviation &sigma;
     * @return the cumulative probability P(x,&mu;,&sigma;)
     */
    public static double P(double x, double mu, double sigma) {
	return Functions.erfc(-(x-mu)/(sigma*ROOT_2))/2.0;
    }

    /**
     * Compute the complement of the cumulative probability function
     * for a Gaussian distribution with a variance of &sigma;<sup>2</sup>
     * and a mean of &mu;.
     * This is equal to 1 - P(x,&mu; &sigma;) when the computation is exact.
     * @param x a value in the range [-&infin;,&infin;]
     * @param mu the mean &mu;
     * @param sigma the standard deviation &sigma;
     * @return the complement, 1 - P(x,&mu; &sigma;), of the cumulative
     *         probability
     */
    public static double Q(double x, double mu, double sigma) {
	return Functions.erfc((x-mu)/(sigma*ROOT_2))/2.0;
    }

    /**
     * Compute the probability that a value deviates from 0 by no more than
     * a specified amount given a Gaussian distribution with a variance
     * of &sigma;<sup>2</sup> and a mean of &mu;.
     * of 1.0.
     * The probability is equal to the integral of the probability density
     * from -x to x.
     * @param x the maximum value for an interval.
     * @param mu the mean &mu;
     * @param sigma the standard deviation &sigma;
     * @return the probability that a value is in the range [-x,x]
     */
    public static double A(double x, double mu, double sigma) {
	return Functions.erf((x-mu)/(sigma*ROOT_2));
    }
}

//  LocalWords:  Abramowitz Stegun th infin
