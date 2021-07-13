package org.bzdev.util.units;

/**
 * Class to convert various units to MKS units.
 * The resulting units will measure distances in meters, mass in kilograms,
 * time in seconds, and temperature in Kelvin.
 */
public class MKS {

    // to prevent a Constructor entry in the API documentation
    private MKS() {}

    /**
     * Convert distances in inches to meters.
     * @param x the distance in inches
     * @return the distance in meters
     */
    public static double inches(double x) {
	return 0.0254 * x;
    }
    /**
     * Convert distances in feet to meters.
     * @param x the distance in feet
     * @return the distance in meters
     */
    public static double feet(double x) {
	return 0.3048 * x;
    }

    /**
     * Convert distances in yards to meters.
     * @param x the distance in yards
     * @return the distance in meters
     */
    public static double yards(double x) {
	return 0.9144 * x;
    }

    /**
     * Convert distances in miles to meters.
     * @param x the distance in miles
     * @return the distance in meters
     */
    public static double miles(double x) {
	return x * 1609.344;
    }

    /**
     * Convert distances in centimeters to meters.
     * @param x the distance in centimeters
     * @return the distance in meters
     */
    public static double cm(double x) {
	return x * 0.01;
    }

    /**
     * Convert distances in millimeters to meters.
     * @param x the distance in millimeters
     * @return the distance in meters
     */
    public static double mm(double x) {
	return x * 0.001;
    }

    /**
     * Convert distances in micrometers to meters.
     * @param x the distance in micrometers (microns)
     * @return the distance in meters
     */
    public static double um(double x) {
	return x * 0.000001;
    }

    /**
     * Convert distances in nanometers to meters.
     * @param x the distance in nanometers
     * @return the distance in meters
     */
    public static double nm(double x) {
	return x * 0.000000001;
    }


    /**
     * Convert distances in kilometers to meters.
     * @param x the distance in kilometers
     * @return the distance in meters
     */
    public static double km(double x) {
	return x * 1000.0;
    }

    /**
     * Convert time in milliseconds to seconds.
     * @param t the time in milliseconds
     * @return the time in seconds
     */
    public static double ms(double t) {
	return t * .001;
    }

    /**
     * Convert time in microseconds to seconds.
     * @param t the time in microseconds
     * @return the time in seconds
     */
    public static double us(double t) {
	return t * .000001;
    }

    /**
     * Convert time in nanoseconds to seconds.
     * @param t the time in nanoseconds
     * @return the time in seconds
     */
    public static double ns(double t) {
	return t * .000000001;
    }

    /**
     * Convert time in minutes to seconds.
     * @param t the time in minutes
     * @return the time in seconds
     */
    public static double minutes(double t) {
	return t * 60.0;
    }

    /**
     * Convert time in hours to seconds.
     * @param t the time in hours
     * @return the time in seconds
     */
    public static double hours(double t) {
	return t * 3600.0;
    }

    /**
     * Convert time in days to seconds.
     * @param t the time in days
     * @return the time in seconds
     */
    public static double days(double t) {
	return t * 86400.0;
    }

    /**
     * Convert speeds in mph to m/s
     * @param x the speed in mph
     * @return the speed in m/s
     */
    public static double mph(double x) {
	return x * ((0.3048 * 88.0)/60.0);
    }

    /**
     * Convert speeds in kph to m/s
     * @param x the speed in kph
     * @return the speed in m/s
     */
    public static double kph(double x) {
	return x * (25.0/90.0);
    }

    /**
     * Convert speeds in feet per second to m/s
     * @param x the speed in feet per second
     * @return the speed in m/s
     */
    public static double feetPerSec(double x) {
	return feet(x);
    }

    /**
     * Convert speeds in yards per second to m/s
     * @param x the speed in yards per second
     * @return the speed in m/s
     */
    public static double yardsPerSec(double x) {
	return yards(x);
    }

    /**
     * Convert speeds in miles per second to m/s
     * @param x the speed in miles per second
     * @return the speed in m/s
     */
    public static double milesPerSec(double x) {
	return miles(x);
    }

    /**
     * Convert pound-mass to kilograms.
     * @param x the mass in pound-mass units
     * @return the mass in kg
     */
    public static double lbm(double x) {
	// exact conversion
	return x * 0.45359237;
    }


    /**
     * Convert slugs to kilograms.
     * @param x the mass in units of slugs
     * @return the mass in kg
     */
    public static double slugs(double x) {
	return x * 14.5939029372;
    }

    /**
     * Convert poundals to newtons.
     * @param x the force in units of poundals
     * @return the force in newtons
     */
    public static double pdl(double x) {
	return x * 0.138254954376;
    }

    /**
     * Convert grams to kilograms.
     * @param x the mass in units of grams
     * @return the mass in kg
     */
    public static double g(double x) {
	return x / 1000.0;
    }

    /**
     * Convert milligrams to kilograms.
     * @param x the mass in units of mg
     * @return the mass in kg
     */
    public static double mg(double x) {
	return x/1.e6;
    }

    /**
     * Convert micrograms to kilograms.
     * @param x the mass in units of micrograms
     * @return the mass in kg
     */
    public static double ug(double x) {
	return x/1.e9;
    }

    /**
     * Convert nanograms to kilograms.
     * @param x the mass in units of micrograms
     * @return the mass in kg
     */
    public static double ng(double x) {
	return x/1.e12;
    }

    /**
     * Convert Megagrams (metric tons) to kilograms.
     * @param x the mass in units of Mg
     * @return the mass in kg
     */
    public static double Mg(double x) {
	return x * 1.e3;
    }

    /**
     * Convert Gigagrams  to kilograms.
     * @param x the mass in units of Mg
     * @return the mass in kg
     */
    public static double Gg(double x) {
	return x * 1.e6;
    }

    /**
     * Convert acceleration in fractions of g to m s<sup>-2</sup>.
     * The constant g is set to the standard value of 9.80665 m s<sup>-2</sup>.
     * @param x the acceleration as a fraction of g
     * @return the acceleration in m s<sup>-2</sup>
     */
    public static double gFract(double x) {
	return x * 9.80665;
    }
    /**
     * Convert acceleration in feet per second per second to m s<sup>-2</sup>.
     * @param x the acceleration in feet per second per second
     * @return the acceleration in m s<sup>-2</sup>
     */
    public static double feetPerSecPerSec(double x) {
	return feet(x);
    }

    /**
     * Convert acceleration in yards per second per second to m s<sup>-2</sup>.
     * @param x the acceleration in yards per second per second
     * @return the acceleration in m s<sup>-2</sup>
     */
    public static double yardsPerSecPerSec(double x) {
	return yards(x);
    }

    /**
     * Convert acceleration in miles per second per second to m s<sup>-2</sup>.
     * @param x the acceleration in miles per second per second
     * @return the acceleration in m s<sup>-2</sup>
     */
    public static double milesPerSecPerSec(double x) {
	return miles(x);
    }

    /**
     * Convert acceleration in mph per second to m s<sup>-2</sup>.
     * @param x the acceleration in mph per second
     * @return the acceleration in m s<sup>-2</sup>
     */
    public static double mphPerSec(double x) {
	return mph(x);
    }

    /**
     * Convert acceleration in kph per second to m s<sup>-2</sup>.
     * @param x the acceleration in kph per second
     * @return the acceleration in m s<sup>-2</sup>
     */
    public static double kphPerSec(double x) {
	return kph(x);
    }

    /**
     * Convert temperature in degrees Celsius to Kelvin.
     * @param x the Celsius temperature
     * @return the thermodynamic temperature (Kelvin)
     */
    public static double degC(double x) {
	return x + 273.15;
    }

    /**
     * Convert temperature in degrees Fahrenheit to Kelvin.
     * @param x the Fahrenheit temperature
     * @return the thermodynamic temperature (Kelvin)
     */
    public static double degF(double x) {
	return 273.15 + (x - 32.0) / 1.8;
    }

    /**
     * Convert lb force to Newtons.
     * @param x the force in units of pounds
     * @return the force in units of newtons
     */
    public static double lbf(double x) {
	return x * 4.4482216152605;
    }

    /**
     * Convert dynes to newtons.
     * @param x the force in units of dynes
     * @return the force in units of newtons
     */
    public static double dyne(double x) {
	return x * 1.e-5;
    }

    /**
     * Convert decanewtons to newtons.
     * Because the acceleration due to gravity on the earth's surface
     * is close to 10, the weight of a 1 kg mass is approximately
     * 1 daN. While the "da" prefix is rarely used in practice, it is
     * used in some product specifications when the mass something
     * can support is of interest to the reader.
     * @param x the force in units of decanewtons
     * @return the force in units of newtons
     */
    public static double daN(double x) {
	return x * 10.0;
    }

    /**
     * Convert kilonewtons to newtons.
     * @param x the force in units of decanewtons
     * @return the force in units of newtons
     */
    public static double kN(double x) {
	return x * 1000.0;
    }


    private static final double TON_FACTOR = 2000.0 * 4.4482216152605;

    /**
     * Convert tons to newtons.
     * @param x the force in units of tons
     * @return the force in units of newtons
     */
    public static double tonf(double x) {
	return TON_FACTOR * x;
    }

    private static final double TON_KGFACTOR = 2000.0 * 0.45359237;

    /**
     * Convert tons to kg.
     * @param x the force in units of tons
     * @return the force in units of newtons
     */
    public static double tonm(double x) {
	return TON_KGFACTOR * x;
    }

}

//  LocalWords:  nanometers poundals micrograms nanograms Megagrams
//  LocalWords:  Gigagrams dynes decanewtons daN da kilonewtons
