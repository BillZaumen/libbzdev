package org.bzdev.providers.devqsim;
import java.io.InputStream;
import java.util.ResourceBundle;
import org.bzdev.obnaming.spi.ONLauncherProvider;
import org.bzdev.devqsim.SimulationLauncher;

public class SimulationLauncherProvider implements ONLauncherProvider {
    @Override
    public String getName() {
	return "devqsim";
    }

    @Override
    public Class<SimulationLauncher> onlClass() {
	return SimulationLauncher.class;
    }

    @Override
    public InputStream getInputStream() {
	return SimulationLauncher.getResourceStream();
    }

    @Override
    public String description() {
	return ResourceBundle
	    .getBundle("org.bzdev.providers.devqsim.lpack.SimulationLauncher")
	    .getString("description");
    }

}

//  LocalWords:  devqsim
