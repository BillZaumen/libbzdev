import java.util.Spliterator;
import org.bzdev.math.stats.BasicStatsMV;
import org.bzdev.math.stats.CovarianceMatrix;
import org.bzdev.math.stats.CovarianceMatrix.Population;
import org.bzdev.math.StaticRandom;
import org.bzdev.math.rv.DoubleRandomVariable;
import org.bzdev.math.rv.GaussianRV;
import org.bzdev.math.rv.GaussianRVs;

public class GaussianRVsTest {

    static double nx = 0.0;
    static double ny = 0.0;
    static double nz = 0.0;

    public static void main(String argv[]) throws Exception {

	StaticRandom.maximizeQuality();

	double means[] = {1.0, 2.0, 3.0};

	GaussianRV x = new GaussianRV(means[0], 2.0);
	GaussianRV y = new GaussianRV(means[1], 3.0);
	GaussianRV z = new GaussianRV(means[2], 2.5);

        DoubleRandomVariable xc = new DoubleRandomVariable() {
		public Double next() {
		    return nx + 0.5*ny + 0.1*nz;
		}
	    };

        DoubleRandomVariable yc = new DoubleRandomVariable() {
		public Double next() {
		    return 1.5*nx + 0.4*ny + 0.3*nz;
		}
	    };

        DoubleRandomVariable zc = new DoubleRandomVariable() {
		public Double next() {
		    return 1.8*nx + 0.3*ny + 0.4*nz;
		}
	    };

	CovarianceMatrix cm = new CovarianceMatrix.Population(3);
	double[] values = new double[3];

	for (int i = 0; i < 100000; i++) {
	    nx = x.next();
	    ny = y.next();
	    nz = z.next();
	    values[0] = xc.next();
	    values[1] = yc.next();
	    values[2] = zc.next();
	    cm.add(values);
	}
	cm.addsComplete();

	x = new GaussianRV(0.0, 1.0);
	y = new GaussianRV(0.0, 1.0);
	z = new GaussianRV(0.0, 1.0);
	
	GaussianRVs grvs = new GaussianRVs(cm);

	CovarianceMatrix cm2 = new CovarianceMatrix.Population(3);
	for (int i = 0; i < 100000; i++) {
	    grvs.next(values);
	    cm2.add(values);
	}
	cm2.addsComplete();

	System.out.println();
	System.out.println("First Covariance Matrix:");
	double[][] m = cm.getMatrix();
	for (int i = 0; i < 3; i++) {
	    System.out.format(" %g %g %g\n", m[i][0], m[i][1], m[i][2]);
	}
	double[] v = cm.getMeans();
	System.out.format("... means: %g, %g, %g\n", v[0], v[1], v[2]);

	System.out.println();
	System.out.println("Second Covariance Matrix:");
	m = cm2.getMatrix();
	for (int i = 0; i < 3; i++) {
	    System.out.format(" %g %g %g\n", m[i][0], m[i][1], m[i][2]);
	}
	v = cm2.getMeans();
	System.out.format("... means: %g, %g, %g\n", v[0], v[1], v[2]);

	BasicStatsMV stats = new BasicStatsMV.Population(3);
	Spliterator<double[]> spliterator = grvs.spliterator(1000000);
	Spliterator<double[]> spliterator2;
	int scount = 0;
	BasicStatsMV ourstats1 = stats;
	while ((spliterator2 = spliterator.trySplit()) != null) {
	    scount++;
	    System.out.println("... estimated size = "
			       + spliterator2.estimateSize());
	    spliterator2.forEachRemaining((val) -> {
		    ourstats1.add(val);
		});
	}
	spliterator.forEachRemaining((val) -> {
		ourstats1.add(val);
	    });
	if (stats.size() != 1000000) {
	    throw new Exception("stats.size() = " + stats.size());
	}

	BasicStatsMV stats2 = new BasicStatsMV.Population(3);
	spliterator = grvs.spliterator();
	spliterator2 = spliterator.trySplit();
	while (spliterator2.tryAdvance((val)->{stats2.add(val);})
	       && stats2.size() < 500000);
	while (spliterator.tryAdvance((val)->{stats2.add(val);})
	       && stats2.size() < 1000000);
	if (stats2.size() != 1000000) {
	    throw new Exception("stats2.size() = " + stats2.size());
	}
	System.out.println("stats2.size() = " + stats2.size());

	double array[] = new double[3];
	stats.getMeans(array);
	System.out.print("means:");
	for (int i = 0; i < 3; i++) {
	    System.out.print(" " + array[i]);
	}
	System.out.println();
	stats.getVariances(array);
	System.out.print("variances:");
	for (int i = 0; i < 3; i++) {
	    System.out.print(" " + array[i]);
	}
	System.out.println();

	System.out.println("test stream(1000000):");
	stats = new BasicStatsMV.Population(3);
	grvs.stream(1000000).forEach(stats::add);
	stats.getMeans(array);
	System.out.print("means:");
	for (int i = 0; i < 3; i++) {
	    System.out.print(" " + array[i]);
	}
	System.out.println();
	stats.getVariances(array);
	System.out.print("variances:");
	for (int i = 0; i < 3; i++) {
	    System.out.print(" " + array[i]);
	}
	System.out.println();

	System.out.println("test stream().limit(1000000):");
	grvs.stream().limit(1000000).forEach(stats::add);
	stats.getMeans(array);
	System.out.print("means:");
	for (int i = 0; i < 3; i++) {
	    System.out.print(" " + array[i]);
	}
	System.out.println();
	stats.getVariances(array);
	System.out.print("variances:");
	for (int i = 0; i < 3; i++) {
	    System.out.print(" " + array[i]);
	}
	System.out.println();

	System.out.println("test parallelStream(1000000):");
	stats = new BasicStatsMV.Population(3);
	grvs.parallelStream(1000000).forEach(stats::add);
	stats.getMeans(array);
	System.out.print("means:");
	for (int i = 0; i < 3; i++) {
	    System.out.print(" " + array[i]);
	}
	System.out.println();
	stats.getVariances(array);
	System.out.print("variances:");
	for (int i = 0; i < 3; i++) {
	    System.out.print(" " + array[i]);
	}
	System.out.println();

	System.out.println("test parallelStream().limit(1000000):");
	grvs.parallelStream().limit(1000000).forEach(stats::add);
	stats.getMeans(array);
	System.out.print("means:");
	for (int i = 0; i < 3; i++) {
	    System.out.print(" " + array[i]);
	}
	System.out.println();
	stats.getVariances(array);
	System.out.print("variances:");
	for (int i = 0; i < 3; i++) {
	    System.out.print(" " + array[i]);
	}
	System.out.println();
    }
}
