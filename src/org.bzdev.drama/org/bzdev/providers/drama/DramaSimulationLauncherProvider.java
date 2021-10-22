package org.bzdev.providers.drama;
import java.io.InputStream;
import java.util.ResourceBundle;
import org.bzdev.obnaming.spi.ONLauncherProvider;
import org.bzdev.drama.DramaSimulationLauncher;

public class DramaSimulationLauncherProvider implements ONLauncherProvider {
    @Override
    public String getName() {
	return "drama";
    }

    @Override
    public Class<DramaSimulationLauncher> onlClass() {
	return DramaSimulationLauncher.class;
    }

    @Override
    public InputStream getInputStream() {
	return DramaSimulationLauncher.getResourceStream();
    }

    @Override
    public String description() {
	return ResourceBundle
	    .getBundle("org.bzdev.providers.drama.lpack."
		       + "DramaSimulationLauncher")
	    .getString("description");
    }
}
