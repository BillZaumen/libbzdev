package org.bzdev.providers.swing;
import java.io.InputStream;
import java.util.ResourceBundle;
import org.bzdev.lang.spi.ONLauncherData;

public class DesktopLauncherData implements ONLauncherData {

    public DesktopLauncherData() {}

    public String getName() {
	return "desktop";
    }

    public InputStream getInputStream() {
	return getClass().getResourceAsStream("DesktopLauncherData.yaml");
    }

    @Override
    public String description() {
	return ResourceBundle
	    .getBundle("org.bzdev.providers.swing.lpack.DesktopLauncherData")
	    .getString("description");
    }
}
