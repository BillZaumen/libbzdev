import java.io.File;
import java.util.Properties;
import javax.swing.*;

import org.bzdev.swing.*;
import org.bzdev.swing.table.*;

public class ConfigTest {

    static class ConfigEditor extends ConfigPropertyEditor {
	/**
	 * Constructor.
	 */
	public ConfigEditor() {
	    super();
	    addReservedKeys("key1", "key2", "key3");
	    addAltReservedKeys("input", "url", "file");
	    addReservedKeys("base64.key4", "base64.key5");
	    addReservedKeys("ebase64.password");

	    setupCompleted();
	    setDefaultProperty("key1", "foo");
	    setDefaultProperty("key2", "$(key1), $(key3)");
	    setDefaultProperty("key3", "bar");
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

	boolean systemUI = argv.length > 0 && argv[0].equals("--systemUI");
	if (systemUI) {
	    /*
	    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	    */
	    SwingUtilities.invokeLater(() -> {
		    DarkmodeMonitor.setSystemPLAF();
		    DarkmodeMonitor.init();
		});
	}

	ConfigEditor editor = new ConfigEditor();

	SwingUtilities.invokeAndWait(() -> {
		editor.addRE("color", new CSSTableCellRenderer(false),
			     new CSSCellEditor());

		editor.addRE("file", null,
			     new FileNameCellEditor("ConfigTest file", false));
	    });


	if (argv.length > (systemUI? 1: 0)) {
	    File f = new File(argv[systemUI? 1: 0]);
	    editor.loadFile(f);
	}

	System.out.println("opening dialog");
	editor.setSaveQuestion(true);
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
