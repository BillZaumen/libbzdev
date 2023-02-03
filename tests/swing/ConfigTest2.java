import java.io.File;
import java.util.Properties;
import javax.swing.*;

import org.bzdev.swing.*;


public class ConfigTest2 {

    static class ConfigEditor extends ConfigPropertyEditor {
	/**
	 * Constructor.
	 */
	public ConfigEditor() {
	    super();
	    addReservedKeys("width", "height",
			    "foreground.color",
			    "background.color",
			    "error.correction.level");
	    setupCompleted();

	    setDefaultProperty("height", "100");
	    setDefaultProperty("width", "100");
	    setDefaultProperty("foreground.color", "black");
	    setDefaultProperty("background.color", "white");
	    setDefaultProperty("error.correction.level", "L");
	    setInitialExtraRows(0);
	    freezeRows();
	}

	@Override
	protected String errorTitle() {return "Error";}
	@Override
	protected String configTitle() {return "Config";}
	@Override
	protected String mediaType() {return "application/foo";}
	@Override
	protected String extensionFilterTitle() {return "Foo files";}
	@Override
	protected String extension() {return "foo";}
    }

    public static void main(String argv[]) throws Exception {

	ConfigEditor editor = new ConfigEditor();

	if (argv.length > 0) {
	    File f = new File(argv[0]);
	    editor.loadFile(f);
	}

	editor.edit(null, ConfigPropertyEditor.Mode.MODAL, null,
		    ConfigPropertyEditor.CloseMode.CLOSE);

	System.out.println("getting config");
	Properties config = editor.getDecodedProperties();
	if (config == null) {
	    System.out.println("no config");
	    System.exit(1);
	}
	System.out.println("-----------------");
	for (String key: config.stringPropertyNames()) {
	    System.out.println(key + ": " + config.getProperty(key));
	    if (key.startsWith("ebase64")) {
		char[] decrypted = (char[])config.get(key);
		System.out.println("... decrypted: " + new String(decrypted));
	    }
	}
	System.out.println("-----------------");
	System.exit(0);
    }
}
