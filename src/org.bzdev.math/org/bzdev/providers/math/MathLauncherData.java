package org.bzdev.providers.math;
import java.io.InputStream;
import java.util.ResourceBundle;
import org.bzdev.lang.spi.ONLauncherData;

public class MathLauncherData implements ONLauncherData {

    public MathLauncherData() {}

    public String getName() {
	return "math";
    }

    public InputStream getInputStream() {
	return getClass().getResourceAsStream("MathLauncherData.yaml");
    }

    @Override
    public String description() {
	return ResourceBundle
	    .getBundle("org.bzdev.providers.math.lpack.MathLauncherData")
	    .getString("description");
    }


}
