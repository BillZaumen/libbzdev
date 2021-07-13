package org.bzdev.providers.p3d;
import java.io.InputStream;
import java.util.ResourceBundle;
import org.bzdev.lang.spi.ONLauncherData;

public class P3DLauncherData implements ONLauncherData {

    public P3DLauncherData() {}

    public String getName() {
	return "p3d";
    }

    public InputStream getInputStream() {
	return getClass().getResourceAsStream("P3DLauncherData.yaml");
    }

    @Override
    public String description() {
	return ResourceBundle
	    .getBundle("org.bzdev.providers.p3d.lpack.P3DLauncherData")
	    .getString("description");
    }


}

//  LocalWords:  DLauncherData yaml
