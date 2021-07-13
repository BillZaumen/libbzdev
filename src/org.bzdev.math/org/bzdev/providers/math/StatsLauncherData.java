package org.bzdev.providers.math;
import java.io.InputStream;
import java.util.ResourceBundle;
import org.bzdev.lang.spi.ONLauncherData;

public class StatsLauncherData implements ONLauncherData {

    public StatsLauncherData() {}

    public String getName() {
	return "stats";
    }

    public InputStream getInputStream() {
	return getClass().getResourceAsStream("StatsLauncherData.yaml");
    }

    @Override
    public String description() {
	return ResourceBundle
	    .getBundle("org.bzdev.providers.math.lpack.StatsLauncherData")
	    .getString("description");
    }


}
