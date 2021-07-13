package org.bzdev.providers.anim2d;
import java.io.InputStream;
import java.util.ResourceBundle;
import org.bzdev.obnaming.spi.ONLauncherProvider;
import org.bzdev.anim2d.Animation2DLauncher;

//@exbundle org.bzdev.providers.anim2d.lpack.Animation2DLauncher

public class Animation2DLauncherProvider implements ONLauncherProvider {

    @Override
    public String getName() {
	return "anim2d";
    }

    @Override
    public Class<Animation2DLauncher> onlClass() {
	return Animation2DLauncher.class;
    }

    @Override
    public InputStream getInputStream() {
	return Animation2DLauncher.getResourceStream();
    }

    @Override
    public String description() {
	return ResourceBundle
	    .getBundle("org.bzdev.providers.anim2d.lpack.Animation2DLauncher")
	    .getString("description");
    }

}
