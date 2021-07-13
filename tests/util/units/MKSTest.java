import org.bzdev.util.units.*;

public class MKSTest {
    static void print (double x, String unit1, double value, String unit2) {
	System.out.println(x + " " + unit1 + " = " + value + " " + unit2);
    }

    public static void main(String argv[]) throws Exception {
	print(1.0, "inch", MKS.inches(1.0), "m");
	print(1.0, "foot", MKS.feet(1.0), "m");
	print(1.0, "yard", MKS.yards(1.0), "m");
	print(1.0, "mile", MKS.miles(1.0), "m");
	print(1.0, "cm", MKS.cm(1.0), "m");
	print(1.0, "mm", MKS.mm(1.0), "m");
	print(1.0, "um", MKS.um(1.0), "m");
	print(1.0, "nm", MKS.nm(1.0), "m");
	print(1.0, "km", MKS.km(1.0), "m");
	print(1.0, "mph", MKS.mph(1.0), "m/s");
	print(1.0, "kph", MKS.kph(1.0), "m/s");
	print(1.0, "ft/sec", MKS.feetPerSec(1.0), "m/s");
	print(1.0, "miles/sec", MKS.milesPerSec(1.0), "m/s");
	print(1.0, "lbm", MKS.lbm(1.0), "kg");
	print(1.0, "slug", MKS.slugs(1.0), "kg");
	print(1.0, "ton", MKS.tonm(1.0), "kg");
	print(1.0, "g", MKS.g(1.0), "kg");
	print(1.0, "mg", MKS.mg(1.0), "kg");
	print(1.0, "ug", MKS.ug(1.0), "kg");
	print(1.0, "ng", MKS.ng(1.0), "kg");
	print(1.0, "Mg", MKS.Mg(1.0), "kg");
	print(1.0, "Gg", MKS.Gg(1.0), "kg");
	print(1.0, "lbf", MKS.lbf(1.0), "N");
	print(1.0, "pdl", MKS.pdl(1.0), "N");
	print(1.0, "dyne", MKS.dyne(1.0), "N");
	print(1.0, "tonf", MKS.tonf(1.0), "N");
	print(1.0, "daN", MKS.daN(1.0), "N");
	print(1.0, "kN", MKS.kN(1.0), "N");
	print(1.0, "g", MKS.gFract(1.0), "m/s^2");
	print(1.0, "feet/sec^2", MKS.feetPerSecPerSec(1.0), "m/s^2");
	print(1.0, "yards/sec^2", MKS.yardsPerSecPerSec(1.0), "m/s^2");
	print(1.0, "miles/sec^2", MKS.milesPerSecPerSec(1.0), "m/s^2");
	print(1.0, "mph/sec", MKS.mphPerSec(1.0), "m/s^2");
	print(1.0, "kph/sec", MKS.kphPerSec(1.0), "m/s^2");
	print(0.0, "degrees C", MKS.degC(0.0), "K");
	print(32.0, "degrees F", MKS.degF(32.0), "K");
	print(68.0, "deg F", MKS.degF(68.0) - MKS.degC(0.0), "deg C");
	print(1.0, "minute", MKS.minutes(1.0), "s");
	print(1.0, "hour", MKS.hours(1.0), "s");
	print(1.0, "day", MKS.days(1.0), "s");
	System.exit(0);
   }
}
