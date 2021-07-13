/**
 * Mathematics module
 * <P>
 * This module provides numerical algorithms, mathematical functions,
 * random variables, and statistics
 */
module org.bzdev.math {
    exports org.bzdev.math;
    // exports org.bzdev.providers.math;
    exports org.bzdev.math.rv;
    exports org.bzdev.math.spi;
    exports org.bzdev.math.stats;
    opens org.bzdev.math.lpack;
    opens org.bzdev.math.rv.lpack;
    opens org.bzdev.math.stats.lpack;
    opens org.bzdev.providers.math.lpack;
    requires java.base;
    requires java.scripting;
    requires org.bzdev.base;
    uses org.bzdev.math.spi.FFTProvider;
    provides org.bzdev.lang.spi.ONLauncherData with
	org.bzdev.providers.math.MathLauncherData,
	org.bzdev.providers.math.StatsLauncherData,
	org.bzdev.providers.math.RVLauncherData;
    provides org.bzdev.math.spi.FFTProvider with
	org.bzdev.providers.math.fft.DefaultFFTProvider;
}
