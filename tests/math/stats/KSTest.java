import org.bzdev.math.stats.*;
import org.bzdev.math.*;
import org.bzdev.math.StaticRandom;
import org.bzdev.math.rv.*;

public class KSTest {
    public static void main(String argv[]) throws Exception {

	StaticRandom.maximizeQuality();

	GaussianRV rv = new GaussianRV(0.0, 1.0);

	RealValuedFunction f = new RealValuedFunction() {
		public double valueAt(double x) {
		    return GaussianDistr.P(x, 0.0, 1.0);
		}
	    };

	for (int k = 0;  k < 10; k++) {
	    KSStat ks = new KSStat(f);

	    for (int i = 0; i < 1000; i++) ks.add(rv.next());

	    int n = ks.size();
	    double Dn = ks.getValue();
	    System.out.println("Dn = " + Dn);
	    System.out.println("p = " + ks.getPValue(null));
	    System.out.println("KDistr.P(0.04301, n) = "
			       + KDistr.P(0.04301, n));
	    System.out.println("expected Dn around 0.024 and P around 0.95");
	    if (k < 9) {
		System.out.println("---");
	    }
	}
    } 
}
