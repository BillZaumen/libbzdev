package org.bzdev.providers.graphics;
import java.io.InputStream;
import java.util.ResourceBundle;
import org.bzdev.lang.spi.ONLauncherData;

public class GraphicsLauncherData implements ONLauncherData {

    public GraphicsLauncherData() {}

    public String getName() {
	return "graphics";
    }

    public InputStream getInputStream() {
	return getClass().getResourceAsStream("GraphicsLauncherData.yaml");
    }

    static final String BUNDLE
	= "org.bzdev.providers.graphics.lpack.GraphicsLauncherData";

    @Override
    public String description() {
	return ResourceBundle.getBundle(BUNDLE).getString("description");
    }
}
