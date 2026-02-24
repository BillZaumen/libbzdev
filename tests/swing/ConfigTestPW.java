import java.util.function.Supplier;
import javax.swing.SwingUtilities;
import org.bzdev.swing.*;

public class ConfigTestPW {

    static class ConfigEditor extends ConfigPropertyEditor {
	/**
	 * Constructor.
	 */
	public ConfigEditor() {
	    super();
	    addReservedKeys("key1", "key2", "config.url", "input.url");
	    addAltReservedKeys("config", "url", "file");
	    addAltReservedKeys("input", "url", "file");
	    addReservedKeys("key3", "base64.key4", "base64.key5");
	    addReservedKeys("ebase64.password");
	    addReservedKeys("output.file");

	    setupCompleted();
	    setDefaultProperty("key1", "foo");
	    setDefaultProperty("key2", "$(key1), $(key3)");
	    setDefaultProperty("key3", "bar");

	    monitorProperty("input.file");
	    monitorProperty("output.file");
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

	System.out.println("... requestPassphrase(null)");
	editor.requestPassphrase(null);
	editor.clearPassphrase();
	System.out.println("... requestPassphrase(null) "
			   + "on event dispatch thread");
	SwingUtilities.invokeAndWait(() -> {
	    editor.requestPassphrase(null);
	    });
	editor.clearPassphrase();
	System.out.println("... requestPassphrase(null, true)");
	editor.requestPassphrase(null, true);
	editor.clearPassphrase();
	System.out.println("... requestPassphrase (null, true, false)");
	char[] pw = ConfigPropertyEditor.requestPassphrase(null, true, false);
	System.out.println(new String(pw));
	System.out.println("... requestPassphrase (null, true, true)");
	pw = ConfigPropertyEditor.requestPassphrase(null, true, true);
	System.out.println(new String(pw));
	System.out.println("(test) printing to stdout");
	Supplier<char[]> supplier = ConfigPropertyEditor
	    .passphraseSupplier(null, true, false);
	System.out.println(supplier.get());
	supplier = ConfigPropertyEditor
	    .passphraseSupplier(null, true, true);
	System.out.println(supplier.get());
	supplier = ConfigPropertyEditor
	    .passphraseSupplier(null, false, false);
	System.out.println(supplier.get());
	supplier = ConfigPropertyEditor
	    .passphraseSupplier(null, false, true);
	System.out.println(supplier.get());

	System.exit(0);
    }
}
