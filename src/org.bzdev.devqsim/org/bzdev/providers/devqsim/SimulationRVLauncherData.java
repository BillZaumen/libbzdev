package org.bzdev.providers.devqsim;
import java.io.InputStream;
import java.util.ResourceBundle;
import org.bzdev.lang.spi.ONLauncherData;

public class SimulationRVLauncherData implements ONLauncherData {

    public SimulationRVLauncherData() {}

    public String getName() {
	return "rvfactories";
    }

    public InputStream getInputStream() {
	return getClass().getResourceAsStream("SimulationRVLauncherData.yaml");
    }

    static final String BUNDLE =
	"org.bzdev.providers.devqsim.lpack.SimulationRVLauncherData";
    @Override
    public String description() {
	return ResourceBundle
	    .getBundle(BUNDLE)
	    .getString("description");
    }
}

//  LocalWords:  rvfactories SimulationRVLauncherData yaml
