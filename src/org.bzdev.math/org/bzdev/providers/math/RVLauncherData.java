package org.bzdev.providers.math;
import java.io.InputStream;
import java.util.ResourceBundle;
import org.bzdev.lang.spi.ONLauncherData;

public class RVLauncherData implements ONLauncherData {

    public RVLauncherData() {}

    public String getName() {
	return "rv";
    }

    public InputStream getInputStream() {
	return getClass().getResourceAsStream("RVLauncherData.yaml");
    }

    @Override
    public String description() {
	return ResourceBundle
	    .getBundle("org.bzdev.providers.math.lpack.RVLauncherData")
	    .getString("description");
    }
}
