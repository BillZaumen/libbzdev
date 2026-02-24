import java.io.*;
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
	    addReservedKeys("key1", "key2", "config.url", "input.url");
	    addAltReservedKeys("config", "url", "file");
	    addAltReservedKeys("input", "url", "file");
	    addReservedKeys("key3", "base64.key4", "base64.key5");
	    addReservedKeys("ebase64.password");
	    addReservedKeys("output.file");
	    addReservedKeys("key6", "key7");

	    setupCompleted();
	    setDefaultProperty("key1", "foo");
	    setDefaultProperty("key2", "$(key1), $(key3)");
	    setDefaultProperty("key3", "bar");
	    setDefaultProperty("key6", "$$(hello)");
	    setDefaultProperty("key7", "$$$$$(key1)");
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

    private static final int ONEKEYID = 0;
    private static final int NOKEYIDS = 1;
    private static final int MULTIPLEKEYIDS = 2;
    private static String gpgdir = null;

    private static int gpgHasKey(String keyid, boolean publicKey) {
	String arg = publicKey? "-k": "-K";
	ProcessBuilder pb = (gpgdir == null)?
	    new ProcessBuilder("gpg", "--with-colons", arg, keyid):
	    new ProcessBuilder("gpg", "--homedir", gpgdir, "--with-colons",
			       arg, keyid);
	pb.redirectError(ProcessBuilder.Redirect.DISCARD);
	String start = publicKey? "pub:": "sec:";
	try {
	    Process p = pb.start();
	    LineNumberReader r = new LineNumberReader
		(new InputStreamReader(p.getInputStream()));
	    String line;
	    int cnt = 0;
	    while ((line = r.readLine()) != null) {
		if (line.startsWith(start)) {
		    cnt++;
		}
	    }
	    int status = p.waitFor();
	    if (status == 0) {
		if (cnt == 0) {
		    return NOKEYIDS;
		} else if (cnt == 1) {
		    return ONEKEYID;
		} else {
		    return MULTIPLEKEYIDS;
		}
	    } else {
		return NOKEYIDS;
	    }
	} catch (IOException e) {
	    return NOKEYIDS;
	} catch (InterruptedException e) {
	    return NOKEYIDS;
	}
    }


    public static void main(String argv[]) throws Exception {

	System.out.println(gpgHasKey("wtz-email", false));
	System.out.println(gpgHasKey("wtz-email", true));
	System.out.println(gpgHasKey("wtz-backup", false));
	System.out.println(gpgHasKey("wtz-backup", true));


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

	editor.addConfigPropertyListener((cpe) -> {
		System.out.println(cpe.getProperty() + " --> "
				   + cpe.getValue());
	    });

	SwingUtilities.invokeAndWait(() -> {
		editor.addRE("color", new CSSTableCellRenderer(false),
			     new CSSCellEditor());

		editor.addRE("file", null,
			     new FileNameCellEditor("ConfigTest file", false));

		editor.addRE("output.file", null,
			     new FileNameCellEditor("ConfigTest output file",
						    false));

		// when foo changes, bar should be set to null
		editor.changedPropertyClears("foo", "bar");

	    });

	if (argv.length > (systemUI? 1: 0)) {
	    File f = new File(argv[systemUI? 1: 0]);
	    editor.loadFile(f);
	}

	if (editor.hasKey("zzz")) {
	    System.out.println("key zzz exists");
	}
	editor.set(null, "zzz", "programatically added property");
	editor.set(null, "base64.zzz1", "programatically added property");
	editor.set(null, "ebase64.zzz2", "programatically added property");

	System.out.println("opening dialog");
	editor.setSaveQuestion(true);
	JMenuItem helpMI = new JMenuItem("Show Help");
	helpMI.addActionListener((ae) -> {
		System.out.println("would open help menu");
	    });

	editor.setHelpMenuItem(helpMI);

	editor.edit(null, ConfigPropertyEditor.Mode.MODAL, null,
		    ConfigPropertyEditor.CloseMode.BOTH);

	System.out.println("-----------------");
	editor.clearPassphrase();
	System.out.println("JSON: " + editor.toJSON());
	System.out.println("-----------------");
	System.out.println("Base64: " + editor.toBase64());
	System.out.println("-----------------");


	System.out.println("getting config:");
	Properties config = editor.getDecodedProperties();
	if (config == null) {
	    System.out.println("no config");
	    System.exit(1);
	}
	for (String key: config.stringPropertyNames()) {
	    System.out.println(key + ": " + config.getProperty(key));
	    if (key.startsWith("ebase64")) {
		char[] decrypted = (char[])config.get(key);
		System.out.println("... decrypted: " + new String(decrypted));
	    }
	}
	System.out.println("-----------------");
	editor.clearPassphrase();
	System.exit(0);
    }
}
