package org.bzdev.bin.sbl;
import java.awt.*;
import java.awt.image.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSession;
import java.security.GeneralSecurityException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.filechooser.*;
import org.bzdev.net.SSLUtilities;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Properties;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import org.bzdev.net.SecureBasicUtilities;
import org.bzdev.swing.ConfigPropertyEditor;
import org.bzdev.swing.DarkmodeMonitor;
import org.bzdev.swing.FileNameCellEditor;
import org.bzdev.swing.SimpleConsole;
import org.bzdev.swing.SwingErrorMessage;
import org.bzdev.util.SafeFormatter;

//@exbundle org.bzdev.bin.sbl.lpack.SBL

public class SBL {
    // resource bundle for messages used by exceptions and errors
    private static ResourceBundle exbundle = ResourceBundle.getBundle
	("org.bzdev.bin.sbl.lpack.SBL");

    private static String errorMsg(String key, Object... args) {
	return (new SafeFormatter()).format(exbundle.getString(key), args)
	    .toString();
    }

    private static String localeString(String key) {
	return errorMsg(key);
    }

    private static int vk(String key) {
	return org.bzdev.swing.keys.VirtualKeys.lookup(localeString(key));
    }

    private static Charset utf8 = Charset.forName("UTF-8");


    private static SecureBasicUtilities.Mode[] modes =
	SecureBasicUtilities.Mode.class.getEnumConstants();
    private static String[] modeNames = new String[modes.length];
    static {
	for (int i = 0; i < modes.length; i++) {
	    modeNames[i] = localeString(modes[i].name());
	}
    }

    private static List<Image> iconList = new LinkedList<Image>();

    public static List<Image> getIconList() {return iconList;}

    private static String[] iconNames = {
        "sblauncher16.png",
        "sblauncher20.png",
        "sblauncher22.png",
        "sblauncher24.png",
        "sblauncher32.png",
        "sblauncher36.png",
        "sblauncher48.png",
        "sblauncher64.png",
        "sblauncher72.png",
        "sblauncher96.png",
        "sblauncher128.png",
        "sblauncher192.png",
        "sblauncher256.png",
        "sblauncher512.png"
    };

    static {
        try {
            for (String iconName: iconNames) {
                iconList.add(new
                             ImageIcon((SBL.class.getResource(iconName)))
                             .getImage());
            }
        } catch (Exception e) {
            System.err.println("could not initilize icons");
        }
    }



    private static class BooleanCBTableCellRenderer
	implements TableCellRenderer
    {
	private JCheckBox cb = new JCheckBox();
	public Component getTableCellRendererComponent
	    (JTable table, Object value, boolean isSelected,
	     boolean hasFocus, int row, int col) {
	    if (value == null) {
		cb.setSelected(false);
	    } else if (value instanceof String) {
		String val = (String)value;
		val = val.trim();
		cb.setSelected(val.equalsIgnoreCase("true"));
	    }
	    return cb;
	}
    }

    private static class BooleanCBCellEditor extends DefaultCellEditor {
	public BooleanCBCellEditor() {
	    super(new JCheckBox());
	}
	public Object getCellEditorValue() {
	    Object object = super.getCellEditorValue();
	    if (object.equals(Boolean.TRUE)) {
		return "true";
	    } else {
		return "false";
	    }
	}
    }


    static class ConfigEditor extends ConfigPropertyEditor {
	public ConfigEditor() {
	    super();
	    addReservedKeys("title",
			    "base64.keypair.publicKey",
			    "ebase64.keypair.privateKey",
			    "trustStore.file",
			    "ebase64.trustStore.password",
			    "trust.selfsigned",
			    "trust.allow.loopback");
	    setDefaultProperty("trust.selfsigned", "false");
	    setDefaultProperty("trust.allow.loopback", "false");
	    
	    addRE("publicKey", new ConfigPropertyEditor
		  .DescribingRenderer((object) -> {
			  return localeString("aPublicKey");
		  }, false),
		  null);
		  
	    FileNameCellEditor fnce = new FileNameCellEditor(false);
	    FileNameExtensionFilter ff = new FileNameExtensionFilter
		(localeString("ffdescr"), "jks", "pfx", "p12");
	    fnce.setFileFilter(ff);
	    addRE("file", null, fnce);
	    addRE("selfsigned",
		  new BooleanCBTableCellRenderer(),
		  new BooleanCBCellEditor());
	    addRE("loopback",
		  new BooleanCBTableCellRenderer(),
		  new BooleanCBCellEditor());

	    addRE("mode", new DefaultTableCellRenderer() {
		    protected void setValue(Object value) {
			String s = (String) value;
			if (s == null) s = "0";
			s = s.trim();
			if (s.length() == 0) s = "0";
			int index = Integer.valueOf(s);
			super.setValue(modeNames[index]);
		    } 
		}, new DefaultCellEditor(new JComboBox<String>(modeNames)) {
			public Object getCellEditorValue() {
			    Component c = getComponent();
			    JComboBox cb = (c instanceof JComboBox)?
				(JComboBox)c: null;
			    int ind = cb.getSelectedIndex();
			    if (ind < 0) ind = 0;
			    return (String)("" + ind);
			}
		    });

	    setupCompleted();
	}
	@Override
	protected String errorTitle() {return localeString("errorTitle");}
	@Override
	protected String configTitle() {return localeString("configTitle");}
	@Override
	protected String mediaType() {
	    return "application/vnd.bzdev.sblauncher";
	}
	@Override
	protected String extensionFilterTitle() {
	    return localeString("extensionFilterTitle");
	}
	@Override
	protected String extension() {return "sbl";}

	@Override
	protected boolean prohibitEditing(int row, int col) {
	    // Users should not be able to edit the public or private
	    // keys.
	    return col == 1 && (row == 1 || row == 2);
	}

    }

    static ConfigEditor cpe;

    static String configDirName = System.getProperty("sbl.config.dir");

    static File configDir = (configDirName == null)?
	new File(System.getProperty("user.home"), ".config/sbl"):
	configDirName.startsWith(System.getProperty("file.separator"))?
	    new File(configDirName):
	    new File(System.getProperty("user.home"), configDirName);

    private static File getFile(Component component) {
	JFileChooser fc = new JFileChooser(configDir);
	FileNameExtensionFilter ef = new FileNameExtensionFilter
	    ("SBL Configurations", "sbl");
	fc.setFileFilter(ef);
	int status = fc.showOpenDialog(component);
	if (status == JFileChooser.APPROVE_OPTION) {
	    File f = fc.getSelectedFile();
	    if (f.getName().endsWith(".sbl")) return f;
	    File p = f.getParentFile();
	    return new File(p, f.getName() + ".sbl");
	} else {
	    return null;
	}
    }

    private static class Entry extends JPanel {
	JTextField description = new JTextField(48);
	JTextField  uri = new JTextField(48);
	JTextField user = new JTextField(32);
	JTextField password = new JTextField(32);
	JComboBox<String> modeCB = new JComboBox<>(modeNames);
	public Entry() {
	    super();
	    GridBagLayout gb = new GridBagLayout();
	    GridBagConstraints c = new GridBagConstraints();
	    setLayout(gb);
	    JLabel label = new JLabel(localeString("description") + ": ");
	    c.gridwidth = 1;
	    c.anchor = GridBagConstraints.LINE_END;
	    gb.setConstraints(label, c);
	    add(label);
	    c.gridwidth = GridBagConstraints.REMAINDER;
	    c.anchor = GridBagConstraints.LINE_START;
	    gb.setConstraints(description, c);
	    add(description);
	    label = new JLabel(localeString("uri") + ":");
	    c.gridwidth = 1;
	    c.anchor = GridBagConstraints.LINE_END;
	    gb.setConstraints(label, c);
	    add(label);
	    c.gridwidth = GridBagConstraints.REMAINDER;	   
	    c.anchor = GridBagConstraints.LINE_START;
	    gb.setConstraints(uri, c);
	    add(uri);
	    label = new JLabel(localeString("user") + ":");
	    c.gridwidth = 1;
	    c.anchor = GridBagConstraints.LINE_END;
	    gb.setConstraints(label, c);
	    add(label);
	    c.gridwidth = GridBagConstraints.REMAINDER;	   
	    c.anchor = GridBagConstraints.LINE_START;
	    gb.setConstraints(user, c);
	    add(user);
	    label = new JLabel(localeString("password") + ":");
	    c.gridwidth = 1;
	    c.anchor = GridBagConstraints.LINE_END;
	    gb.setConstraints(label, c);
	    add(label);
	    c.gridwidth = GridBagConstraints.REMAINDER;	   
	    c.anchor = GridBagConstraints.LINE_START;
	    gb.setConstraints(password, c);
	    add(password);
	    label = new JLabel(localeString("mode") + ":");
	    c.gridwidth = 1;
	    c.anchor = GridBagConstraints.LINE_END;
	    gb.setConstraints(label, c);
	    add(label);
	    c.gridwidth = GridBagConstraints.REMAINDER;	   
	    c.anchor = GridBagConstraints.LINE_START;
	    gb.setConstraints(modeCB, c);
	    add(modeCB);
	    // grap the keyboard focus
	    description.addFocusListener(new FocusAdapter() {
		boolean retry = true;
		public void focusLost(FocusEvent e) {
		    Component other = e.getOppositeComponent();
		    Window w1 = SwingUtilities.getWindowAncestor
			(Entry.this);
		    Window w2 = (other == null)? null:
			SwingUtilities.getWindowAncestor(other);
		    if (retry && e.getCause() == FocusEvent.Cause.UNKNOWN
			&& w1 == w2
			&& !SwingUtilities.isDescendingFrom
			(other, Entry.this)) {
			SwingUtilities.invokeLater(() -> {
				description.requestFocusInWindow();
			    });
		    } else {
			retry = false;
		    }
		}
	    });
	    description.addAncestorListener(new AncestorListener() {
		    public void ancestorAdded(AncestorEvent ev) {
			SwingUtilities.invokeLater(() -> {
				description.requestFocusInWindow();
			    });
		    }
		    public void ancestorRemoved(AncestorEvent ev) {
		    }
		    public void ancestorMoved(AncestorEvent ev) {
		    }
		});

	}
    }

    static JFrame frame;
    static File configFile;
    static HashSet<String> entries = new HashSet<>();
    
    static void fixupEntries(JComboBox<String>cb) {
	int len = cb.getItemCount();
	for (int i = len-1; i > 0; i--) {
	    cb.removeItemAt(i);
	}
	entries.clear();
	cb.setSelectedIndex(0);
	for (String s:
		 new TreeSet<String>
		 (cpe.getEncodedProperties().stringPropertyNames())) {
	    if (s.endsWith(".description")) {
		String name = s.substring(0, s.lastIndexOf('.'));
		cb.addItem(name);
		entries.add(name);
	    }
	}
    }

    private static boolean init(List<String> rlist) {
	try {
	    String[] keypair =
		SecureBasicUtilities.createPEMPair(null,null);
	    for (;;) {
		if (rlist.size() == 0) {
		    cpe.setRecipients(frame);
		} else {
		    cpe.setRecipients(rlist);
		    // If we fail, ask explicitly.
		    rlist.clear();
		}

		if (cpe.set(frame, "ebase64.keypair.privateKey", keypair[0])
		    && cpe.set(frame, "base64.keypair.publicKey",
			       keypair[1])) {
		    break;
		}
		String s1 = errorMsg("encryptFailed");
		String s2 = errorMsg("encryptFailedTitle");

		int status = JOptionPane
		    .showConfirmDialog(frame, s1, s2,
				       JOptionPane.YES_NO_OPTION);
		if (status != JOptionPane.OK_OPTION) {
		    return false;
		}
	    }
	    cpe.set(frame, "title", configFile.getName());
	    cpe.save(configFile);
	    return true;
	} catch (Exception ex) {
	    return false;
	} finally {
	    cpe.clearRecipients();
	}
    }

    static String getName(JComboBox<String> cb) {
	int index = cb.getSelectedIndex();
	return (index > 0)? cb.getItemAt(index): null;
    }

    static SecureBasicUtilities dops =  new SecureBasicUtilities();
    static SecureBasicUtilities ops = null;

    static char[] getSecurePW(Component component, String name) {
	if (ops == null) {
	    cpe.requestPassphrase(component);
	    char[] carray = (char[])cpe.getDecodedProperties()
		.get("ebase64.keypair.privateKey");
	    CharBuffer cbuf = CharBuffer.wrap(carray);
	    ByteBuffer bbuf = utf8.encode(cbuf);
	    byte[] barray;
	    if (bbuf.hasArray() && bbuf.arrayOffset() == 0) {
		barray = bbuf.array();
	    } else {
		barray = new byte[bbuf.limit()];
		bbuf.get(barray);
	    }
	    try {
		InputStream is = new ByteArrayInputStream(barray);
		ops = new SecureBasicUtilities(is);
	    } catch (IOException eio) {
		// System.err.println(eio.getMessage());
		SwingErrorMessage.format("%s",eio.getMessage());
	    } catch (GeneralSecurityException egs) {
		// System.err.println(egs.getMessage());
		SwingErrorMessage.format("%s",egs.getMessage());
	    }
	}
	Properties props = cpe.getEncodedProperties();
	char[]  password = props.getProperty(name + ".password")
	    .toCharArray();
	String uriString = props.getProperty(name + ".uri");
	SecureBasicUtilities.Mode mode;
	try {
	    mode = modes[Integer.valueOf(props.getProperty(name + ".mode"))];
	} catch (Exception em) {
	    // System.err.println(em.getMessage());
	    SwingErrorMessage.format("%s", em.getMessage());
	    return null;
	}
	URI uri = null;
	try {
	    uri = new URI(uriString);
	} catch (Exception e) {
	    // System.err.println(e.getMessage());
	    SwingErrorMessage.format("%s", e.getMessage());
	    return null;
	}
	String scheme = uri.getScheme().toLowerCase();
	String host = uri.getHost();
	int port = uri.getPort();
	if (port < 0) {
	    if (scheme.equals("http")) port = 80;
	    else if (scheme.equals("https")) port = 443;
	    else return null;
	}
	Certificate cert = null;
	if (scheme.equals("https")) {
	    SSLSocketFactory factory = (SSLSocketFactory)
		SSLSocketFactory.getDefault();
	    try (SSLSocket socket = (SSLSocket)
		 factory.createSocket(host, port)) {
		SSLSession session = socket.getSession();
	        Certificate[] chain = session.getPeerCertificates();
	        cert =  (chain == null || chain.length == 0)? null:
		    chain[0];
	    } catch (Exception e) {
		// System.err.println(e.getMessage());
		SwingErrorMessage.format("%s", e.getMessage());
		Throwable ee = e.getCause();
		if (ee != null) {
		    // System.err.println(ee.getMessage());
		    SwingErrorMessage.format("... %s", ee.getMessage());
		}
		cert = null;
	    }
	}
	try {
	    // System.out.println("mode = " + mode);
	    switch(mode) {
	    case DIGEST:
		password = dops.createPassword(null, password);
		break;
	    case SIGNATURE_WITHOUT_CERT:
		password = ops.createPassword(null, password);
		break;
	    case SIGNATURE_WITH_CERT:
		if (cert == null) {
		    // System.err.println("no certificate");
		    SwingErrorMessage.format("%s", errorMsg("noCertificate"));
		    SwingErrorMessage.displayConsoleIfNeeded();
		    return null;
		}
		password = ops.createPassword(cert, password);
		break;
	    case PASSWORD:
		return password;
	    }
	    return password;
	} catch (Exception e) {
	    // System.err.println(e.getMessage());
	    SwingErrorMessage.format("%s", e.getMessage());
	    SwingErrorMessage.displayConsoleIfNeeded();
	    return null;
	}
    }

    // Will be set to true if loading from a file provided on the
    // command line as sbl may have been started by a browser.
    static boolean checkConfig = true;

    public static HashSet<X509Certificate> certSet = new HashSet<>();

    public static void configTrust(Component owner) {
	Properties p = cpe.getDecodedProperties();
	String tname = p.getProperty("trustStore.file");
	if (tname != null) tname = tname.trim();
	final boolean selfsigned =
	    Boolean.valueOf(p.getProperty("trust.selfsigned"));
	boolean loopback =
	    Boolean.valueOf(p.getProperty("trust.allow.loopback"));
	if (checkConfig && ((tname != null  && tname.length() > 0)
			    || selfsigned || loopback)) {
	    String msg = errorMsg("REQ", tname, selfsigned, loopback);
	    JLabel label = new JLabel(msg);
	    String title = errorMsg("REQTITLE");
	    switch (JOptionPane
		    .showConfirmDialog(owner, msg, title,
				       JOptionPane.OK_CANCEL_OPTION,
				       JOptionPane.QUESTION_MESSAGE)) {
	    case JOptionPane.OK_OPTION:
		break;
	    case JOptionPane.CANCEL_OPTION:
	    default:
		return;
	    }
	}
	try {
	    Predicate<X509Certificate> selfSignedTest = (cert) -> {
		if (selfsigned) {
		    return true;
		} else {
		    if (certSet.contains(cert)) {
			return true;
		    } else {
			String msg = errorMsg("acceptSelfSigned");
			String title = errorMsg("acceptSelfSignedTitle");
			switch(JOptionPane.showConfirmDialog
			       (frame, msg, title,
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE)) {
			case JOptionPane.OK_OPTION:
			    certSet.add(cert);
			    return true;
			default:
			    return false;
			}
		    }
		}
	    };
	    if (tname != null && tname.length() > 0) {
		cpe.requestPassphrase(owner);
		Object obj = p.get("ebase64.trustStore.password");
		char[] pw = null;
		File trustStore = new File(tname);
		if (obj instanceof char[]) {
		    pw = (char []) obj;
		}
		SSLUtilities.installTrustManager("TLS",
						 trustStore, pw,
						 /*
						 (cert) -> {
						     return selfsigned;
						 }
						 */
						 selfSignedTest);
	    } else {
		SSLUtilities.installTrustManager("TLS",
						 null, null,
						 /*
						 (cert) -> {
						     return selfsigned;
						 }
						 */
						 selfSignedTest);
	    }
	    if (loopback) {
		SSLUtilities.allowLoopbackHostname();
	    } else {
		SSLUtilities.disallowLoopbackHostname();
	    }
	} catch (Exception e) {
	    // System.err.println(e.getMessage());
	    SwingErrorMessage.format("%s", e.getMessage());
	    SwingErrorMessage.displayConsoleIfNeeded();
	    return;
	}
    }

    // This is a bit of a hack.  We want to be able to paste two
    // fields, a user name and a passphrase.  We'd expect a browser
    // to give the user textfield the keyboard focus before the password
    // textfield, so we alternate between the two for each successive
    // paste, starting with the user name.  This saves the user from
    // having to go back and forth between two windows or having to paste
    // in an unnatural order.
    private static class StringSelection2
	implements Transferable, ClipboardOwner
    {

	long startTime = System.currentTimeMillis();
	boolean start = false;

	int i = 0;
	StringSelection[] selections =  new StringSelection[2];
	public StringSelection2(StringSelection ss1, StringSelection ss2) {
	    selections[0] = ss1;
	    selections[1] = ss2;
	}

	public Object getTransferData(DataFlavor flavor)
	    throws UnsupportedFlavorException, IOException
	{
	    try {
		return selections[i].getTransferData(flavor);
	    } finally {
		// getTransferData can be called before the user
		// pastes the results.  When this occurs, the call
		// should be almost immediately after this instance
		// was initialized.  A 200 ms window should be more
		// than long enough, and users cannot push a button
		// and start the place operation within this short
		// time interval.
		if (!start && (System.currentTimeMillis() - startTime) > 200) {
		    start = true;
		}
		if (start) {
		    i++;
		    i = i % 2;
		}
	    }
	}
	public DataFlavor[] getTransferDataFlavors() {
	    return selections[i].getTransferDataFlavors();
	}
	public boolean isDataFlavorSupported(DataFlavor flavor) {
	    return selections[i].isDataFlavorSupported(flavor);
	}

	public void lostOwnership(Clipboard clipboard,
				 Transferable contents)
	{
	    // A check of the openjdk source code indicates that
	    // for string selections, this method doesn't do anything.
	    // The call is made just in case something changes later.
	    selections[i].lostOwnership(clipboard, contents);
	}
    }



    public static void main(String argv[]) throws Exception {
	configDir.mkdirs();
	System.setProperty("user.dir", configDir.getCanonicalPath());

	DarkmodeMonitor.setSystemPLAF();
	DarkmodeMonitor.init();
	// Have to construct this after dark mode is set.
	cpe  = new ConfigEditor();
	int indx = 0;
	boolean allowLoopback = false;
	boolean allowSelfSigned = false;
	boolean addTrustStore = false;
	File trustStore = null;
	char[] trustStorePW = null;

	boolean print = false;
	boolean printPassword = false;
	boolean printUser = false;
	boolean printURI = false;
	boolean printDescription = false;
	boolean printMode = false;
	boolean printPublicKey = false;
	boolean printList = false;
	String nm = null;
	List<String> rlist = new LinkedList<String>();

	while (indx < argv.length) {
	    if (argv[indx].equals("--")) {
		indx++;
		break;
	    } else if (argv[indx].equals("-r")) {
		indx++;
		rlist.add(argv[indx]);
	    } else if (argv[indx].equals("-f")) {
		checkConfig = false;
	    } else if (argv[indx].equals("-n")) {
		indx++;
		nm = argv[indx];
	    } else if (argv[indx].equals("--print")) {
		if (print == true) {
		    System.err.println("sbl: " + errorMsg("multiplePrints"));
		    System.exit(1);
		}
		print = true;
		indx++;
		if (argv[indx].equals("user")) {
		    printUser = true;
		} else if (argv[indx].equals("uri")) {
		    printURI = true;
		} else if (argv[indx].equals("description")) {
		    printDescription = true;
		} else if (argv[indx].equals("password")) {
		    printPassword = true;
		} else if (argv[indx].equals("mode")) {
		    printMode = true;
		} else if (argv[indx].equals("publicKey")) {
		    printPublicKey = true;
		} else if (argv[indx].equals("list")) {
		    printList = true;
		} else {
		    String arg = argv[indx];
		    System.err.println("sbl: " + errorMsg("printerr", arg));
		    System.exit(1);
		}
	    } else if (argv[indx].startsWith("-")) {
		System.err.println("sbl: " + errorMsg("badOption", argv[indx]));
		System.exit(1);
	    } else {
		break;
	    }
	    indx++;
	}
	/*
	final boolean selfSigned = allowSelfSigned;

	if (addTrustStore) {
	    SSLUtilities.installTrustManager("TLS",
					     trustStore, trustStorePW,
					     (cert) -> {return selfSigned;});
	}
	if (allowLoopback) {
	    SSLUtilities.allowLoopbackHostname();
	}
	*/
	configFile = (argv.length-indx == 1)? new File(argv[indx]): null;

	if (print) {
	    if (configFile == null) {
		System.err.println("sbl: " + errorMsg("noConfigFile"));
	    }
	    try {
		cpe.loadFile(configFile);
		Properties props = cpe.getDecodedProperties();
		if (printPassword) {
		    if (nm == null) {
			System.err.println("sbl: " + errorMsg("noName"));
		    }
		    System.out.println(props.getProperty(nm + ".password"));
		}
		if (printUser) {
		    if (nm == null) {
			System.err.println("sbl: " + errorMsg("noName"));
		    }
		    System.out.println(props.getProperty(nm + ".user"));
		}
		if (printURI) {
		    if (nm == null) {
			System.err.println("sbl: " + errorMsg("noName"));
		    }
		    System.out.println(props.getProperty(nm + ".uri"));
		}
		if (printDescription) {
		    if (nm == null) {
			System.err.println("sbl: " + errorMsg("noName"));
		    }
		    System.out.println(props
				       .getProperty(nm + ".description"));
		}
		if (printMode) {
		    if (nm == null) {
			System.err.println("sbl: " + errorMsg("noName"));
		    }
		    int imd = Integer.valueOf(props.getProperty(nm + ".mode"));
		    System.out.println(modes[imd] + " ("
				       + modeNames[imd] + ")");
		}
		if (printPublicKey) {
		    String pk = props.getProperty("keypair.publicKey");
		    System.out.println(pk);
		}
		if (printList) {
		    for (String key: props.stringPropertyNames()) {
			if (key.endsWith(".description")) {
			    int last = key.lastIndexOf(".");
			    key = key.substring(0, last);
			    System.out.println(key);
			}
		    }
		}
		System.exit(0);
	    } catch (Exception e) {
		System.err.println("sbl: " + e.getMessage());
		System.exit(1);
	    }
	}

	if (configFile != null && configFile.exists() && rlist.size() > 0) {
	    // add a new keypair with a new list of recipients
	    cpe.loadFile(configFile);
	    String[] keypair =
		SecureBasicUtilities.createPEMPair(null,null);
	    cpe.setRecipients(rlist);
	    if (cpe.set(null, "ebase64.keypair.privateKey", keypair[0])
		&& cpe.set(null, "base64.keypair.publicKey",
			   keypair[1])) {
		cpe.save(configFile);
		System.exit(0);
	    } else {
		System.err.println("sbl: " + errorMsg("encryptFailed"));
		System.exit(1);
	    }
	}


	SwingUtilities.invokeLater(() -> {
		boolean loadedAtStart = false;
		JPanel panel = new JPanel(new GridLayout(3, 3));
		JLabel titleLabel = new JLabel(" ");
		JLabel descrLabel = new JLabel(" ");

		JButton loadButton = new JButton(localeString("loadButton"));
		JComboBox<String> selectSiteCB =
		    new JComboBox<>(new Vector<String>());
		selectSiteCB.addItem(localeString("selectSiteCB"));
		selectSiteCB.setSelectedIndex(0);
		JMenuItem findMenuItem = new JMenuItem(localeString("Find"));
		JMenuItem browserMenuItem =
		    new JMenuItem(localeString("Browser"));
		SimpleConsole console = new SimpleConsole();
		console.addSeparator();
		String clbl = errorMsg("openConsole");
		JMenuItem consoleMenuItem = console
		    .createMenuItem(clbl, "console", 800, 600);
		SwingErrorMessage.setAppendable(console);
		console.addCloseAccelerator(KeyEvent.VK_W,
					    InputEvent.CTRL_DOWN_MASK,
					    InputEvent.CTRL_DOWN_MASK
					    | InputEvent.ALT_DOWN_MASK);

		JButton visitSiteButton = new
		    JButton(localeString("visitSiteButton"));
		JButton generateButton =
		    new JButton(localeString("generateButton"));
		JButton addEntryButton = new
		    JButton(localeString("addEntryButton"));
		JButton editEntriesButton = new
		    JButton(localeString("editEntriesButton"));
		JButton copyUserButton = new
		    JButton(localeString("copyUserButton"));
		JButton copyPubKeyButton = new
		    JButton(localeString("copyPubKeyButton"));
		JButton copyPWButton = new
		    JButton(localeString("copyPWButton"));


		if (configFile == null) {
		    addEntryButton.setEnabled(false);
		    editEntriesButton.setEnabled(false);
		    selectSiteCB.setEnabled(false);
		    visitSiteButton.setEnabled(false);
		    findMenuItem.setEnabled(false);
		    browserMenuItem.setEnabled(false);
		    generateButton.setEnabled(false);
		    copyUserButton.setEnabled(false);
		    copyPubKeyButton.setEnabled(false);
		    copyPWButton.setEnabled(false);
		} else {
		    if (!configFile.exists()) {
			if (init(rlist)) {
			    addEntryButton.setEnabled(true);
			    editEntriesButton.setEnabled(true);
			    selectSiteCB.setEnabled(false);
			    visitSiteButton.setEnabled(false);
			    findMenuItem.setEnabled(false);
			    browserMenuItem.setEnabled(false);
			    generateButton.setEnabled(false);
			    copyUserButton.setEnabled(false);
			    copyPubKeyButton.setEnabled(true);
			    copyPWButton.setEnabled(false);
			} else {
			    addEntryButton.setEnabled(false);
			    editEntriesButton.setEnabled(false);
			    selectSiteCB.setEnabled(false);
			    visitSiteButton.setEnabled(false);
			    findMenuItem.setEnabled(false);
			    browserMenuItem.setEnabled(false);
			    generateButton.setEnabled(false);
			    copyUserButton.setEnabled(false);
			    copyPubKeyButton.setEnabled(false);
			    copyPWButton.setEnabled(false);
			}
		    } else {
			try {
			    cpe.loadFile(configFile);
			    configTrust(frame);
			    fixupEntries(selectSiteCB);
			    String txt =
				cpe.getEncodedProperties().getProperty("title");
			    titleLabel.setText(localeString("loaded") + ": "
					       + txt);
			    descrLabel.setText(" ");
			    loadedAtStart = true;
			} catch (IOException eio) {
			    // System.err.println(eio.getMessage());
			    SwingErrorMessage.format("%s",  eio.getMessage());
			    SwingErrorMessage.displayConsoleIfNeeded();
			    System.exit(1);
			}
			addEntryButton.setEnabled(true);
			editEntriesButton.setEnabled(true);
			selectSiteCB.setEnabled(true);
			visitSiteButton.setEnabled(false);
			findMenuItem.setEnabled(true);
			browserMenuItem.setEnabled(false);
			generateButton.setEnabled(false);
			addEntryButton.setEnabled(false);
			copyUserButton.setEnabled(false);
			copyPubKeyButton.setEnabled(true);
			copyPWButton.setEnabled(false);
		    }
		}

		ActionListener loadAL = (e) -> {
		    File f = getFile(frame);
		    if (f == null) {
			// Keep the current configuration
			return;
		    }
		    configFile = f;
		    if (!configFile.exists()) {
			if (init(rlist)) {
			    addEntryButton.setEnabled(true);
			    editEntriesButton.setEnabled(true);
			    selectSiteCB.setEnabled(false);
			    visitSiteButton.setEnabled(false);
			    findMenuItem.setEnabled(false);
			    browserMenuItem.setEnabled(false);
			    generateButton.setEnabled(false);
			    copyUserButton.setEnabled(false);
			    copyPubKeyButton.setEnabled(true);
			    copyPWButton.setEnabled(false);
			} else {
			    addEntryButton.setEnabled(false);
			    editEntriesButton.setEnabled(false);
			    selectSiteCB.setEnabled(false);
			    visitSiteButton.setEnabled(false);
			    findMenuItem.setEnabled(false);
			    browserMenuItem.setEnabled(false);
			    generateButton.setEnabled(false);
			    copyUserButton.setEnabled(false);
			    copyPubKeyButton.setEnabled(false);
			    copyPWButton.setEnabled(false);
			}
		    }
		    try {
			cpe.loadFile(configFile);
			checkConfig = false;
			configTrust(frame);
			fixupEntries(selectSiteCB);
			addEntryButton.setEnabled(true);
			editEntriesButton.setEnabled(true);
			selectSiteCB.setEnabled(entries.size() > 0);
			visitSiteButton.setEnabled(false);
			findMenuItem.setEnabled(entries.size() > 0);
			browserMenuItem.setEnabled(false);
			generateButton.setEnabled(false);
			copyUserButton.setEnabled(false);
			copyPubKeyButton.setEnabled(true);
			copyPWButton.setEnabled(false);
			String txt =
			    cpe.getEncodedProperties().getProperty("title");
			titleLabel.setText(localeString("loaded") + ": "
					   + txt);
			descrLabel.setText(" ");

		    } catch (IOException eio) {
			// System.err.println(eio.getMessage());
			SwingErrorMessage.format("%s", eio.getMessage());
			SwingErrorMessage.displayConsoleIfNeeded();
		    }
		};

		loadButton.addActionListener(loadAL);

		addEntryButton.addActionListener((e) -> {
			String name;
			do {
			    name = JOptionPane
				.showInputDialog(frame,
						 localeString("name"),
						 localeString("nameTitle"),
						 JOptionPane.QUESTION_MESSAGE);
			    if (name == null) return;
			    int i = 0;
			    boolean loop = false;
			    for (char ch: name.toCharArray()) {
				if (!((i++ == 0)?
				      Character.isJavaIdentifierStart(ch):
				      Character.isJavaIdentifierPart(ch))) {
				    loop = true;
				    break;
				}
			    }
			    if (loop) continue;
			} while (entries.contains(name));

			Entry entry = new Entry();
			String et = errorMsg("entryTitle", name);
			int status = JOptionPane
			    .showConfirmDialog(frame, entry, et,
					       JOptionPane.OK_CANCEL_OPTION);
			if (status == JOptionPane.OK_OPTION) {
			    entries.add(name);
			    cpe.set(frame, name + ".description",
				    entry.description.getText());
			    cpe.set(frame, name + ".uri",
				    entry.uri.getText());
			    cpe.set(frame, name + ".user",
				    entry.user.getText());
			    cpe.set(frame, name + ".password",
				    entry.password.getText());
			    int ind = entry.modeCB.getSelectedIndex();
			    if (ind < 0) ind = 0;
			    cpe.set(frame, name + ".mode", "" + ind);
			    try {
				cpe.save(configFile);
				fixupEntries(selectSiteCB);
				selectSiteCB.setEnabled(entries.size() > 0);
				visitSiteButton.setEnabled(false);
				findMenuItem.setEnabled(entries.size() > 0);
				browserMenuItem.setEnabled(false);
				generateButton.setEnabled(false);
				copyUserButton.setEnabled(false);
				copyPubKeyButton.setEnabled(false);
				copyPWButton.setEnabled(false);
			    } catch (IOException eio) {
				// System.err.println(eio.getMessage());
				SwingErrorMessage.format("%s",
							 eio.getMessage());
				SwingErrorMessage.displayConsoleIfNeeded();
			    }
			}
		    });

		editEntriesButton.addActionListener((e) -> {
			cpe.edit(frame, ConfigPropertyEditor.Mode.MODAL,
				 null, ConfigPropertyEditor.CloseMode.CLOSE);
			try {
			    cpe.save(configFile);
			    fixupEntries(selectSiteCB);
			    selectSiteCB.setEnabled(entries.size() > 0);
			    visitSiteButton.setEnabled(false);
			    findMenuItem.setEnabled(entries.size() > 0);
			    browserMenuItem.setEnabled(false);
			    generateButton.setEnabled(false);
			    copyUserButton.setEnabled(false);
			    copyPubKeyButton.setEnabled(false);
			    copyPWButton.setEnabled(false);
			    String txt =
				cpe.getEncodedProperties().getProperty("title");
			    titleLabel.setText(localeString("loaded") + ": "
					       + txt);
			    descrLabel.setText(" ");
			} catch (IOException eio) {
			    // System.err.println(eio.getMessage());
			    SwingErrorMessage.format("%s", eio.getMessage());
			    SwingErrorMessage.displayConsoleIfNeeded();
			}
		    });

		selectSiteCB.addActionListener((e) -> {
			int index = selectSiteCB.getSelectedIndex();
			if (index < 1) {
			    visitSiteButton.setEnabled(false);
			    browserMenuItem.setEnabled(false);
			    generateButton.setEnabled(false);
			    copyUserButton.setEnabled(false);
			    copyPubKeyButton.setEnabled(true);
			    copyPWButton.setEnabled(false);
			    descrLabel.setText(" ");
			} else {
			    visitSiteButton.setEnabled(true);
			    browserMenuItem.setEnabled(true);
			    generateButton.setEnabled(true);
			    copyUserButton.setEnabled(true);
			    copyPubKeyButton.setEnabled(true);
			    copyPWButton.setEnabled(true);
			    String key = (String)selectSiteCB.getSelectedItem();
			    String txt = cpe.getEncodedProperties()
				.getProperty(key + ".description");
			    descrLabel.setText(localeString("site")
					       + ": " + key + "\u2014" + txt);
			}
		    });

		if (loadedAtStart && entries.size() == 1) {
		    // If the configuration file was loaded from a file
		    // specified on the command line and there is only
		    // a single item to select, select that one automatically.
		    // The use case is a user who opens a file provided by
		    // a browser and there is only a single entry.
		    selectSiteCB.setSelectedIndex(1);
		}

		copyUserButton.addActionListener((e) -> {
			String name = getName(selectSiteCB);
			if (name == null) return;
			String value = cpe.getEncodedProperties()
			    .getProperty(name + ".user");
			Clipboard cb = panel.getToolkit()
			    .getSystemClipboard();
			StringSelection selection = new StringSelection(value);
			cb.setContents(selection, selection);
		    });

		copyPubKeyButton.addActionListener((e) -> {
			String value = cpe.getDecodedProperties()
			    .getProperty("keypair.publicKey");
			Clipboard cb = panel.getToolkit()
			    .getSystemClipboard();
			StringSelection selection = new StringSelection(value);
			cb.setContents(selection, selection);
		    });

		copyPWButton.addActionListener((e) -> {
			String name = getName(selectSiteCB);
			if (name == null) return;
			String value = cpe.getEncodedProperties()
			    .getProperty(name + ".password");
			Clipboard cb = panel.getToolkit()
			    .getSystemClipboard();
			StringSelection selection = new StringSelection(value);
			cb.setContents(selection, selection);
		    });

		ActionListener visitAL = (e) -> {
		    console.addSeparatorIfNeeded();
		    String name = getName(selectSiteCB);
		    if (name == null) return;
		    Properties props = cpe.getEncodedProperties();
		    String user = props.getProperty(name + ".user");
		    String uriString = props.getProperty(name + ".uri");
		    URI uri = null;
			
		    Clipboard cb = panel.getToolkit().getSystemClipboard();
		    char[] passwd = getSecurePW(panel, name);
		    if (passwd == null) return;
		    String password = new String(passwd);
		    StringSelection selection1 =
		    new StringSelection(user);
		    StringSelection selection2 = new StringSelection(password);
		    StringSelection2 selection = new
		    StringSelection2(selection1, selection2);
		    cb.setContents(selection, selection);
		    try {
			uri = new URI(uriString);
			if (Desktop.isDesktopSupported()) {
			    Desktop desktop = Desktop.getDesktop();
			    if (desktop.isSupported(Desktop.Action
						    .BROWSE)) {
				desktop.browse(uri);
			    }
			}
		    } catch (Exception ex) {
			// System.err.println("sbl: " + ex.getMessage());
			SwingErrorMessage.format("%s", ex.getMessage());
			SwingErrorMessage.displayConsoleIfNeeded();
			StringSelection s = new StringSelection("");
			cb.setContents(s, s);
		    }
		};

		visitSiteButton.addActionListener(visitAL);

		generateButton.addActionListener((e) -> {
			console.addSeparatorIfNeeded();
			String name = getName(selectSiteCB);
			String user = cpe.getEncodedProperties()
			    .getProperty(name + ".user");
			if (name == null) return;
			char[] passwd = getSecurePW(panel, name);
			if (passwd == null) {
			    return;
			}
			String value = new String(passwd);
			Clipboard cb = panel.getToolkit()
			    .getSystemClipboard();
			StringSelection selection =
			    new StringSelection(value);
			cb.setContents(selection, selection);
		    });

		if (configFile == null) {
		    frame = new JFrame(errorMsg("title0"));
		} else {
		    frame = new JFrame(errorMsg("title", configFile.getName()));
		}
		SwingErrorMessage.setComponent(frame);

		JMenuBar menubar = new JMenuBar();
		JMenu fileMenu = new JMenu(localeString("File"));
		JMenuItem menuItem = new JMenuItem(localeString("Close"));
		fileMenu.setMnemonic(vk("VK_FILE"));
		menuItem.setAccelerator(KeyStroke.getKeyStroke
					(KeyEvent.VK_W,
					 InputEvent.CTRL_DOWN_MASK));
		menuItem.addActionListener((ae) -> {
			System.exit(0);
		    });
		fileMenu.add(menuItem);
		menuItem = new JMenuItem(localeString("Open"));
		menuItem.setAccelerator(KeyStroke.getKeyStroke
					(KeyEvent.VK_O,
					 InputEvent.CTRL_DOWN_MASK));
		menuItem.addActionListener(loadAL);
		fileMenu.add(menuItem);
		menuItem = findMenuItem;
		menuItem.setAccelerator(KeyStroke.getKeyStroke
					(KeyEvent.VK_F,
					 InputEvent.CTRL_DOWN_MASK));
		menuItem.addActionListener((ae) -> {
			selectSiteCB.requestFocusInWindow();
			selectSiteCB.showPopup();
		    });
		fileMenu.add(menuItem);
		menuItem = browserMenuItem;
		menuItem.setAccelerator(KeyStroke.getKeyStroke
					(KeyEvent.VK_B,
					InputEvent.CTRL_DOWN_MASK));
		menuItem.addActionListener(visitAL);
		fileMenu.add(menuItem);

		consoleMenuItem.setAccelerator(KeyStroke.getKeyStroke
					(KeyEvent.VK_J,
					 InputEvent.SHIFT_DOWN_MASK
					 | InputEvent.CTRL_DOWN_MASK));
		fileMenu.add(consoleMenuItem);


		menubar.add(fileMenu);
		frame.setJMenuBar(menubar);

		frame.setIconImages(getIconList());
		panel.add(loadButton);
		panel.add(selectSiteCB);
		panel.add(visitSiteButton);
		panel.add(generateButton);
		panel.add(addEntryButton);
		panel.add(editEntriesButton);
		panel.add(copyUserButton);
		panel.add(copyPubKeyButton);
		panel.add(copyPWButton);

		frame.setLayout(new BorderLayout());
		frame.add(titleLabel, BorderLayout.NORTH);
		frame.add(panel, BorderLayout.CENTER);
		frame.add(descrLabel, BorderLayout.SOUTH);
		frame.pack();
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
			    System.exit(0);
			}
		    });
		frame.setVisible(true);
	    });
    }
}
