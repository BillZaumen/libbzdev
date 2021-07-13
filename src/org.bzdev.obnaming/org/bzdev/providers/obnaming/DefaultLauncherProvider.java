package org.bzdev.providers.obnaming;
import java.io.InputStream;
import java.util.ResourceBundle;
import org.bzdev.obnaming.spi.ONLauncherProvider;
import org.bzdev.obnaming.DefaultLauncher;

public class DefaultLauncherProvider implements ONLauncherProvider {
    @Override
    public String getName() {
	return "default";
    }

    @Override
    public Class<DefaultLauncher> onlClass() {
	return DefaultLauncher.class;
    }

    @Override
    public InputStream getInputStream() {
	return DefaultLauncher.getResourceAsStream();
    }

    @Override
    public String description() {
	return ResourceBundle
	    .getBundle("org.bzdev.providers.obnaming.lpack.DefaultLauncher")
	    .getString("description");
    }

}
