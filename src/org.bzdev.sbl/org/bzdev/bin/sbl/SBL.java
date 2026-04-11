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
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
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
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Vector;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Properties;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.zip.GZIPInputStream;

import org.bzdev.io.AppendableWriter;
import org.bzdev.net.SecureBasicUtilities;
import org.bzdev.net.SSLUtilities;
import org.bzdev.swing.ConfigPropertyEditor;
import org.bzdev.swing.DarkmodeMonitor;
import org.bzdev.swing.FileNameCellEditor;
import org.bzdev.swing.SimpleConsole;
import org.bzdev.swing.SwingErrorMessage;
import org.bzdev.util.ConfigProperties;
import org.bzdev.util.ConfigPropUtilities;
import org.bzdev.util.SafeFormatter;

//@exbundle org.bzdev.bin.sbl.lpack.SBL

public class SBL {

    private static final Charset UTF8 = Charset.forName("UTF-8");

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

    // private static Charset utf8 = Charset.forName("UTF-8");


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

    static SecureRandom random = new SecureRandom();
    static String genpw() {
	char[] pw = new char[16];
	for (int i = 0; i < 16; i++) {
	    char ch = (char)(random.nextInt(127 - 33) + 33);
	    pw[i] = ch;
	}
	return new String(pw);
    }

    static class ConfigEditor extends ConfigPropertyEditor {

	// So we have direct access.
	protected Properties getProperties() {
	    return super.getProperties();
	}

	public ConfigEditor() {
	    super();
	    addReservedKeys("title",
			    "recipients",
			    "base64.keypair.publicKey",
			    "ebase64.keypair.privateKey",
			    "trustStore.file",
			    "ebase64.trustStore.password",
			    "trust.selfsigned",
			    "trust.allow.loopback");
	    setUseGPGOnLoad("ebase64.keypair.privateKey");
	    setDefaultProperty("trust.selfsigned", "false");
	    setDefaultProperty("trust.allow.loopback", "false");
	    setRecipientsKey("recipients");

	    addRE("recipients", new ConfigPropertyEditor
		  .DescribingRenderer((object) -> {
			  return localeString("recipients");
		  }, false),
		  null);

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
	    return col == 1 && (row == 2 || row == 3);
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
	JTextField base = new JTextField(48);
	JTextField  uri = new JTextField(48);
	JTextField user = new JTextField(32);
	// JTextField password = new JTextField(32);
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

	    label = new JLabel(localeString("base") + ":");
	    c.gridwidth = 1;
	    c.anchor = GridBagConstraints.LINE_END;
	    gb.setConstraints(label, c);
	    add(label);
	    c.gridwidth = GridBagConstraints.REMAINDER;
	    c.anchor = GridBagConstraints.LINE_START;
	    gb.setConstraints(base, c);
	    add(base);

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
	    /*
	    label = new JLabel(localeString("password") + ":");
	    c.gridwidth = 1;
	    c.anchor = GridBagConstraints.LINE_END;
	    gb.setConstraints(label, c);
	    add(label);
	    c.gridwidth = GridBagConstraints.REMAINDER;	   
	    c.anchor = GridBagConstraints.LINE_START;
	    gb.setConstraints(password, c);
	    add(password);
	    */
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
	fixupEntries(cb, cpe);
    }
    static void fixupEntries(JComboBox<String>cb, ConfigEditor ce) {
	if (cb != null) {
	    int len = cb.getItemCount();
	    for (int i = len-1; i > 0; i--) {
		cb.removeItemAt(i);
	    }
	    cb.setSelectedIndex(0);
	}
	entries.clear();
	for (String s:
		 new TreeSet<String>
		 (ce.getEncodedProperties().stringPropertyNames())) {
	    if (s.endsWith(".description")) {
		String name = s.substring(0, s.lastIndexOf('.'));
		if (cb != null) cb.addItem(name);
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
		    cpe.set(frame, "recipients",
			    ConfigPropUtilities.encodeRecipients(rlist));
		    // If we fail somewhere below, ask explicitly.
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
	boolean nullResult = false;
	Properties props = cpe.getDecodedProperties();
	Properties rawProps = cpe.getProperties();
	String pwstring = rawProps.getProperty("ebase64." + name + ".password");
	if (pwstring != null) {
	    cpe.useGPG(!pwstring.startsWith("==="));
	}
	SecureBasicUtilities.Mode mode = null;
	try {
	    mode = modes[Integer.valueOf(props.getProperty(name + ".mode"))];
	} catch (Exception em) {
	    // System.err.println(em.getMessage());
	    SwingErrorMessage.format("%s: %s", em.getMessage(),
				     name + ".mode");
	    nullResult = true;
	    // SwingErrorMessage.displayConsoleIfNeeded();
	    // return null;
	}
	Component saved = cpe.getPWOwner();
	if (ops == null && !(mode == SecureBasicUtilities.Mode.PASSWORD
			     || mode == SecureBasicUtilities.Mode.DIGEST)) {
	    try {
		cpe.setPWOwner(component);
		cpe.requestPassphrase(component);
		char[] carray = (char[])cpe.getDecodedProperties()
		    .get("ebase64.keypair.privateKey");
		if (carray == null) {
		    SwingErrorMessage.format("%s\n", errorMsg("noPrivateKey"));
		    SwingErrorMessage.displayConsoleIfNeeded();
		    return null;
		}
		CharBuffer cbuf = CharBuffer.wrap(carray);
		ByteBuffer bbuf = UTF8.encode(cbuf);
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
		    SwingErrorMessage.displayConsoleIfNeeded();
		    return null;
		} catch (GeneralSecurityException egs) {
		    // System.err.println(egs.getMessage());
		    SwingErrorMessage.format("%s",egs.getMessage());
		    SwingErrorMessage.displayConsoleIfNeeded();
		    return null;
		}
	    } finally {
		cpe.setPWOwner(saved);
	    }
	}
	char[]  password = null;
	try {
	    cpe.setPWOwner(frame);
	    Object val = props.get("ebase64." + name + ".password");
	    if (val == null) {
		val = props.get(name + ".password");
	    }
	    password = (val instanceof String)?
		((String) val).toCharArray():
		(char[]) val;
	    if (password == null) {
		SwingErrorMessage
		    .format(errorMsg("noPassword", name + ".password"));
		nullResult = true;
	    }
	} catch (Exception epw) {
	    SwingErrorMessage.format("%s: %s", epw.getMessage(),
				     name + ".password");
	    nullResult = true;
	    // SwingErrorMessage.displayConsoleIfNeeded();
	    // return null;
	} finally {
	    cpe.setPWOwner(saved);
	}
	String uriString = null;
	try {
	    uriString = props.getProperty(name + ".uri");
	} catch (Exception euri) {
	    SwingErrorMessage.format("%s: %s", euri.getMessage(),
				     name + ".uri");
	    nullResult = true;
	    // SwingErrorMessage.displayConsoleIfNeeded();
	    // return null;
	}
	URI uri = null;
	boolean uriException = false;
	try {
	    uri = new URI(uriString);
	} catch (Exception e) {
	    // System.err.println(e.getMessage());
	    SwingErrorMessage.format("%s: %s", e.getMessage(),
				     uriString);
	    uriException = true;
	    nullResult = true;
	    // SwingErrorMessage.displayConsoleIfNeeded();
	    // return null;
	}
	String scheme = uriException? null: uri.getScheme().toLowerCase();
	String host = uriException? null: uri.getHost();
	if (scheme == null || scheme.trim().length() == 0) {
	    if (!uriException) {
		SwingErrorMessage.format(errorMsg("noScheme"));
		nullResult = true;
	    }
	}
	if (host == null || host.trim().length() == 0) {
	    if (!uriException) {
		SwingErrorMessage.format(errorMsg("noHost"));
		nullResult = true;
	    }
	}
	int port = uri.getPort();
	if (port < 0) {
	    if (scheme.equalsIgnoreCase("http")) port = 80;
	    else if (scheme.equalsIgnoreCase("https")) port = 443;
	    else {
		SwingErrorMessage.format(errorMsg("unsupportedScheme", scheme));
		nullResult = true;
	    }
	} else if (!(scheme.equalsIgnoreCase("http")
		     || scheme.equalsIgnoreCase("https"))) {
		SwingErrorMessage.format(errorMsg("unsupportedScheme", scheme));
		nullResult = true;
	}
	Certificate cert = null;
	Certificate[] chain = null;
	if (scheme.equals("https")) {
	    SSLSocketFactory factory = (SSLSocketFactory)
		SSLSocketFactory.getDefault();
	    try (SSLSocket socket = (SSLSocket)
		 factory.createSocket(host, port)) {
		SSLSession session = socket.getSession();
	        chain = session.getPeerCertificates();
	        cert =  (chain == null || chain.length == 0)? null:
		    chain[0];
		if (cert == null)  {
		    nullResult = true; // SSL: must have a certificate chain
		    SwingErrorMessage.format(errorMsg("nocert", host, port));
		}
	    } catch (Exception e) {
		// System.err.println(e.getMessage());
		SwingErrorMessage.format("%s", e.getMessage());
		Throwable ee = e.getCause();
		if (ee != null) {
		    // System.err.println(ee.getMessage());
		    SwingErrorMessage.format("... %s", ee.getMessage());
		}
		nullResult = true; // if SSL, there must be a certificate chain
		cert = null;
	    }
	}
	if (nullResult) {
	    SwingErrorMessage.displayConsoleIfNeeded();
	    return null;
	}
	try {
	    // System.out.println("mode = " + mode);
	    switch(mode) {
	    case DIGEST:
		password = dops.createPassword((Certificate[])null, password);
		break;
	    case SIGNATURE_WITHOUT_CERT:
		password = ops.createPassword((Certificate[])null, password);
		break;
	    case SIGNATURE_WITH_CERT:
		if (cert == null) {
		    // System.err.println("no certificate");
		    SwingErrorMessage.format("%s", errorMsg("noCertificate"));
		    SwingErrorMessage.displayConsoleIfNeeded();
		    return null;
		}
		password = ops.createPassword(chain, password);
		break;
	    case PASSWORD:
		return password;
	    }
	    return password;
	} catch (Exception e) {
	    // System.err.println(e.getMessage());
	    SwingErrorMessage.format("%s", e.getMessage());
	    SwingErrorMessage.displayConsoleIfNeeded();
	    if (stacktrace) {
		e.printStackTrace();
	    }
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
		char[] pw = null;
		if (cpe.hasKey("ebase64.trustStore.password")) {
		    Component saved = cpe.getPWOwner();
		    try {
			cpe.setPWOwner(owner);
			cpe.requestPassphrase(owner);
			Object obj = p.get("ebase64.trustStore.password");
			if (obj instanceof char[]) {
			    pw = (char []) obj;
			}
		    } finally {
			cpe.setPWOwner(saved);
		    }
		} else {
		    pw = "changeit".toCharArray();
		}
		File trustStore = new File(tname);
		SSLUtilities.installTrustManager("TLS",
						 trustStore, pw,
						 /*
						 (cert) -> {
						     return selfsigned;
						 }
						 */
						 selfSignedTest);
	    } else {
		File truststore = null;
		char[] pw = null;
		SSLUtilities.installTrustManager("TLS",
						 truststore, pw,
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
	    if (stacktrace) {
		e.printStackTrace();
	    }
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

    private static  boolean stacktrace = false;

    private static File fnexpand(String fname) {
	String sep = System.getProperty("file.separator");
	if (fname == null) {
	    return null;
	} else if (fname.startsWith("~" + sep) ){
	    String home = System.getProperty("user.home");
	    if (home.endsWith(sep)) {
		home = home.substring(0, home.length() - sep.length());
	    }
	    return new File(home + fname.substring(1));
	} else if (fname.startsWith("..." + sep)) {
	    fname = fname.substring(sep.length() + 3);
	    return new File(configDir, fname);
	} else {
	    return new File(fname);
	}
    }

    private static class KeyInfoPair {
	String[] keyids;
	String[] fprs;
	KeyInfoPair(String[]keyids, String[] fprs) {
	    this.keyids = keyids;
	    this.fprs = fprs;
	}
    }

    private static KeyInfoPair getGPGPrivateKeyInfo() {
	return getGPGPrivateKeyInfo(null);
    }

    private static KeyInfoPair getGPGPrivateKeyInfo(String keyid) {
	return getGPGPrivateKeyInfo(keyid, true);
    }
    private static KeyInfoPair getGPGPrivateKeyInfo(String keyid,
						    boolean quiet)
    {
	ProcessBuilder pb = (keyid == null)?
	    new ProcessBuilder("gpg", "--with-colons", "-K"):
	    new ProcessBuilder("gpg", "--with-colons", "-K", keyid);
	pb.redirectError(ProcessBuilder.Redirect.DISCARD);
	try {
	    Process p = pb.start();
	    LineNumberReader r = new LineNumberReader
		(new InputStreamReader(p.getInputStream()));
	    int cnt = -1;
	    String line;
	    int status = -1;
	    ArrayList<String>keyids = new ArrayList<>();
	    ArrayList<String>fprs = new ArrayList<>();

	    while ((line = r.readLine()) != null) {
		if (line.startsWith("sec:")) {
		    status = 0;
		    cnt++;
		} else if (line.startsWith("fpr:") && status == 0) {
		    status = 1;
		    String[] entries = line.split(":");
		    fprs.add(entries[9]);
		} else if (line.startsWith("uid") && status == 1) {
		    status = 2;
		    String[] entries = line.split(":");
		    keyids.add(entries[9]);
		}
	    }
	    r.close();
	    status = p.waitFor();
	    if (status == 0) {
		// found the keys.
		String keyidsS[] = new String[keyids.size()];
		String fprsS[] = new String[fprs.size()];
		keyids.toArray(keyidsS);
		fprs.toArray(fprsS);
		return new KeyInfoPair(keyidsS, fprsS);
	    } else {
		if (!quiet) {
		    SwingErrorMessage.format("%s", errorMsg("gpgFailed1"));
		    SwingErrorMessage.displayConsoleIfNeeded();
		}
		return null;
	    }
	} catch (Exception e) {
	    if (!quiet) {
		SwingErrorMessage.format("%s", e.getMessage());
		SwingErrorMessage.displayConsoleIfNeeded();
	    }
	}
	return null;
    }

    public static String getGPGPublicKey(String fpr) {
	ProcessBuilder pb = new ProcessBuilder("gpg", "--armor", "--export",
					       fpr);
	pb.redirectError(ProcessBuilder.Redirect.DISCARD);
	try {
	    Process p = pb.start();
	    StringBuilder sb = new StringBuilder();
	    AppendableWriter w = new AppendableWriter(sb);
	    LineNumberReader r = new LineNumberReader
		(new InputStreamReader(p.getInputStream()));
	    r.transferTo(w);
	    r.close();
	    w.flush();
	    w.close();
	    int status = p.waitFor();
	    if (status == 0) {
		return sb.toString();
	    } else {
		return null;
	    }
	} catch (Exception e) {
	    return null;
	}
    }

    private static boolean sendSBLDirectly = false;
    private static String sendSBLDirectlyKey = null;

    private static void dumpResponse(HttpURLConnection c) {
	try {
	    int len = c.getContentLength();
	    if (len > 0) {
		InputStream is = c.getInputStream();
		OutputStream os = OutputStream.nullOutputStream();
		is.transferTo(os);
		while (is.read() != -1);
		is.close();
		os.flush();
		os.close();
	    }
	} catch (Exception e) {}
    }

    private static void fetchSBL(URI location) throws IOException {
	URL url = location.toURL();
	URLConnection urlc = url.openConnection();
	if (urlc instanceof HttpURLConnection) {
	    HttpURLConnection c = (HttpURLConnection) urlc;
	    c.setRequestMethod("GET");
	    int rcode = c.getResponseCode();
	    if (rcode == 200) {
		String ct = c.getContentType();
		String ce = c.getContentEncoding();
		if (ct != null
		    && ct.equals("application/vnd.bzdev.sblauncher")) {
		    int len = c.getContentLength();
		    InputStream is = c.getInputStream();
		    if (ce != null && ce.equalsIgnoreCase("gzip")) {
			is = new GZIPInputStream(is);
		    }
		    ConfigEditor old = cpe;
		    cpe = new ConfigEditor();
		    InputStreamReader r = new InputStreamReader(is, UTF8);
		    cpe.loadReader(r);
		    // part of a negotiation with the server so keep the
		    // previous passphrase so the user is not asked for a
		    // new one.
		    cpe.requestPassphrase(old);
		    configFile = null;
		}
	    }
	} else {
	    System.err.println("slb: not HttpURLConnection");
	}
    }

    private static int
	processHttpURLC(JFrame frame, String user, HttpURLConnection c,
			String ekey409)
	throws IOException
    {
	int rcode = c.getResponseCode();
	String emsg;
	String title = errorMsg("errorTitle");
	switch(rcode) {
	case 200:
	    {
		String contentType = c.getContentType();
		int len = c.getContentLength();
	    }
	case 205:
	    dumpResponse(c);
	case 204:
	    return rcode;
	case 201:
	    {
		String location  = c.getHeaderField("Location");
		// System.out.println("Location = " + location);
		dumpResponse(c);
		if (Desktop.isDesktopSupported()) {
		    try {
			URI uri = new URI(location);
			fetchSBL(uri);
			String key = null;
			for (String k: cpe.getProperties()
				 .stringPropertyNames()) {
			    if (k.endsWith(".description")) {
				key = k;
				break;
			    }
			}
			if (key != null) {
			    int ind = key.lastIndexOf('.');
			    key = (ind == -1)? null: key.substring(0, ind);
			}
			Properties dprops = cpe.getDecodedProperties();
			String user1 = dprops.getProperty(key + ".user");
			if (!user.equals(user1)) {
			    String msg = errorMsg("userConflict", user1, user);
			    throw new IOException(msg);
			}
			String uriString = dprops.getProperty(key + ".uri");
			uri = new URI(uriString);
			Clipboard cb = frame.getToolkit().getSystemClipboard();
			boolean hadPassphrase = cpe.hasPassphrase();
			char[] passwd = getSecurePW(frame, key);
			if (!hadPassphrase) cpe.clearPassphrase();
			if (passwd == null) {
			    throw new IOException(errorMsg("pwGenFailed"));
			}
			String password = new String(passwd);
			StringSelection selection1 = new StringSelection(user);
			StringSelection selection2 =
			    new StringSelection(password);
			StringSelection2 selection = new
			    StringSelection2(selection1, selection2);
			cb.setContents(selection, selection);
			Desktop desktop = Desktop.getDesktop();
			if (desktop.isSupported(Desktop.Action.BROWSE)) {
			    desktop.browse(uri);
			}
		    } catch (URISyntaxException urie) {
			String msg = errorMsg("badURI", location);
		        title = errorMsg("errorTitle");
			JOptionPane
			    .showMessageDialog(frame, msg, title,
					       JOptionPane.ERROR_MESSAGE);
			return -1;
		    }
		} else {
		    Clipboard cb = frame.getToolkit().getSystemClipboard();
		    StringSelection selection = new StringSelection(location)/* {
			    @Override
			    public Object getTransferData(DataFlavor flavor)
				throws UnsupportedFlavorException, IOException
			    {
				try {
				    return super.getTransferData(flavor);
				} finally {
				    // Delayed so the transfer will have
				    // completed.
				    SwingUtilities.invokeLater(() -> {
					    System.exit(0);
					});
				}
			    }
			    }*/;
		    cb.setContents(selection,selection);

		    String msg = errorMsg("pleaseVisit", location);
		    title = errorMsg("titleText3");
		    JOptionPane.showMessageDialog(frame, msg, title,
						  JOptionPane.PLAIN_MESSAGE);
		}
	    }
	    break;
	case 202:
	    if(c.getContentType()
	       .toLowerCase()
	       .equals("text/plain; charset=utf-8")) {
		int len =  c.getContentLength();
		InputStream is = c.getInputStream();
		InputStreamReader r = new InputStreamReader
		    (is, "UTF-8");
		char data[] =new char[len];
		r.read(data, 0, len);
		String msg =
		    new String(data);
		JOptionPane.showMessageDialog(frame, msg, title,
					      JOptionPane.PLAIN_MESSAGE);
	    }
	    break;
	case 409:
	    dumpResponse(c);
	    emsg = errorMsg(ekey409, user);
	    JOptionPane.showMessageDialog(frame, emsg, title,
					  JOptionPane.ERROR_MESSAGE);
	    break;
	default:
	    dumpResponse(c);
	    emsg = errorMsg("status", rcode);
	    JOptionPane.showMessageDialog (frame, emsg, title,
					   JOptionPane.ERROR_MESSAGE);
	}
	return rcode;
    }

    private static String asBlank(String s) {
	char[] array = new char[s.length()];
	for (int i = 0; i < array.length; i++) {
	    array[i] = ' ';
	}
	return new String(array);
    }

    private static class ServerResponse {
	int rcode;
	String msg;
	ServerResponse(int rcode, String msg) {
	    this.rcode = rcode;
	    this.msg = msg;
	}

	public int getResponseCode() {return rcode;}
	public String getMessage() {return msg;}
    }

    private static ServerResponse sendSBLToServer(URI serverURI, String key)
	throws Exception
    {
	Properties cpeProps = cpe.getEncodedProperties();
	Properties decoded = cpe.getDecodedProperties();
	ConfigProperties props = new ConfigProperties
	    ("application/vnd.bzdev.sblauncher");
	/*
	String key = (sendSBLDirectly)? sendSBLDirectlyKey:
	    getName(selectSiteCB);
	*/
	String ourkeys[] = {
	    "base64.keypair.publicKey",
	    key + ".description",
	    "ebase64." + key + ".password",
	    key + ".base",
	    key + ".uri",
	    key + ".user",
	    key + ".mode"
	};
	for (String k: ourkeys) {
	    String value;
	    String kk = null;
	    if (k.startsWith("ebase64.")) {
		value = new String((char[])decoded.get(k));
		kk = k.substring(1);
	    } else if (k.startsWith("base64.")) {
		String kk2 = k.substring(7);
		value = (String)decoded.get(kk2);
	    } else {
		value = cpeProps.getProperty(k);
	    }
	    // System.out.println(k + ": " + value);
	    if (value != null) {
		if (kk != null) {
		    props.setProperty(kk, value);
		} else {
		    props.setProperty(k, value);
		}
	    }
	}
	String string = props.store();
	byte[] arr = string.getBytes("UTF-8");
	URL url = serverURI.toURL();
	// System.out.println("server URL = " +url.toString());
	URLConnection urlc = url.openConnection();
	if (urlc instanceof HttpURLConnection) {
	    HttpURLConnection c =
		(HttpURLConnection) urlc;
	    c.setRequestMethod("POST");
	    String mediaType =
		"application/vnd.bzdev.sblogindata";
	    c.setRequestProperty("content-type",
				 mediaType);
	    c.setRequestProperty("content-length",
				 "" + arr.length);
	    c.setDoOutput(true);
	    OutputStream os =
		c.getOutputStream();
	    os.write(arr);
	    os.flush();
	    os.close();
	    String user = props
		.getProperty(key + ".user");
	    int rcode = c.getResponseCode();
	    String contentType = c.getContentType();
	    // System.out.println("contentType = " + contentType);
	    int len = c.getContentLength();
	    // System.out.println("content length = " + len);
	    String msg = null;
	    if (contentType == null) {
		return new ServerResponse(rcode, null);
	    }
	    contentType = contentType.toLowerCase();
	    if (contentType.equals("text/plain; charset=utf-8")) {
		InputStream is = c.getInputStream();
		InputStreamReader r = new InputStreamReader(is, UTF8);
		char[] data = new char[len];
		r.read(data, 0, len);
		msg = new String(data);
		return new ServerResponse(rcode, msg);
	    } else if (contentType.equals("text/html; charset=utf-8")) {
		InputStream is = c.getInputStream();
		InputStreamReader r = new InputStreamReader(is, UTF8);
		char[] data = new char[len];
		int rdlen = r.read(data, 0, len);
		r.close();
		if (rdlen == len) {
		    msg = new String(data);
		    String lmsg = msg.toLowerCase();
		    int ind1 = lmsg.indexOf("<body>");
		    int ind2 = lmsg.lastIndexOf("</body>");
		    if (ind1 < 0 || ind2 < 0 || ind2 < ind1) {
			msg = null;
		    } else {
			msg = "<html>"
			    + msg.substring(0, ind2).substring(ind1+6)
			    + "</html>";
		    }
		}
		return new ServerResponse(rcode, msg);
	    } else {
		throw new IOException(errorMsg("contentType", contentType));
	    }
	} else {
	    throw new IOException(errorMsg("notHTTP"));
	}
    }

    private static JButton button1 = null;
    private static JButton button2 = null;
    private static JButton button3 = null;
    private static String button1User = null;
    private static String button1Path = null;
    private static URI button2URI = null;
    private static byte[] button1Arr = null;

    // used by "sbl" account-setup case.
    private static URI button1URI = null;

    private static String getRespErrorMsg(ServerResponse response) {
	int rc = response.getResponseCode();
	String msg = response.getMessage();
	if (msg == null) {
	    return errorMsg("sblFailed", rc);
	} else {
	    return errorMsg("sblFailed2", rc, msg);
	}
    }

    private static void setupBrowser(JFrame frame, URI uri) {
	Clipboard cb = frame.getToolkit().getSystemClipboard();
	String user = cpe.getDecodedProperties().getProperty("user.user");
	char[] passwd =  getSecurePW(frame, "user");
	if (passwd != null) {
	    String password = new String(passwd);
	    StringSelection selection1 = new StringSelection(user);
	    StringSelection selection2 = new StringSelection(password);
	    StringSelection2 selection = new StringSelection2(selection1,
							      selection2);
	    cb.setContents(selection, selection);
	    Desktop desktop = Desktop.getDesktop();
	    if (uri != null && desktop.isSupported(Desktop.Action.BROWSE)) {
		try {
		    desktop.browse(uri);
		} catch (Exception ee) {
		    SwingErrorMessage.format("%s\n", ee.getMessage());
		    SwingErrorMessage.displayConsoleIfNeeded();
		}
	    }
	} else {
	    // don't pop up the console because the user may have canceled.
	    SwingErrorMessage.format("%s\n", errorMsg("noPasswordAvailable"));
	}
    }

    public static void main(String argv[]) throws Exception {

	configDir.mkdirs();
	// System.setProperty("user.dir", configDir.getCanonicalPath());
	// System.setProperty("user.home", configDir.getCanonicalPath());

	DarkmodeMonitor.setSystemPLAF();
	DarkmodeMonitor.init();
	// Have to construct this after dark mode is set.
	cpe = new ConfigEditor();
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
	boolean create = false;
	String usr = null;
	String uriS = null;
	String baseS = null;
	List<String> rlist = new LinkedList<String>();

	// When non-null, this will be used to fill in
	Consumer<String> init3Consumer = null;

	while (indx < argv.length) {
	    if (argv[indx].equals("--")) {
		indx++;
		break;
	    } else if (argv[indx].equals("-c")
		       || argv[indx].equals("--create")) {
		create = true;
	    } else if(argv[indx].equals("--user")) {
		indx++;
		if (indx == argv.length) {
		    String msg = errorMsg("missingArg", "--user");
		    System.err.println("sbl: " + msg);
		    System.exit(1);
		}
		usr = argv[indx];
	    } else if (argv[indx].equals("--uri")) {
		indx++;
		if (indx == argv.length) {
		    String msg = errorMsg("missingArg", "--uri");
			System.err.println("sbl: " + msg);;
			System.exit(1);
		}
		uriS = argv[indx];
	    } else if (argv[indx].equals("--base")) {
		indx++;
		if (indx == argv.length) {
		    String msg = errorMsg("missingArg", "--base");
			System.err.println("sbl: " + msg);;
			System.exit(1);
		}
		baseS = argv[indx];
	    } else if (argv[indx].equals("-r")) {
		indx++;
		if (indx == argv.length) {
		    System.err.println("sbl: " + errorMsg("missingArg", "-r"));
		    System.exit(1);
		}
		rlist.add(argv[indx]);
	    } else if (argv[indx].equals("-f")) {
		checkConfig = false;
	    } else if (argv[indx].equals("--loopback")) {
		allowLoopback = true;
	    } else if (argv[indx].equals("-n")) {
		indx++;
		if (indx == argv.length) {
		    System.err.println("sbl: " + errorMsg("missingArg", "-n"));
		    System.exit(1);
		}
		nm = argv[indx];
	    } else if (argv[indx].equals("--gpgdir")) {
		indx++;
		if (indx == argv.length) {
		    String msg = errorMsg("missingArg", "--gpgdir");
		    System.err.println("sbl: " + msg);
		    System.exit(1);
		}
		cpe.setGPGDir(argv[indx]);
	    } else if (argv[indx].equals("--print")) {
		if (print == true) {
		    System.err.println("sbl: " + errorMsg("multiplePrints"));
		    System.exit(1);
		}
		print = true;
		indx++;
		if (indx == argv.length) {
		    String msg = errorMsg("missingArg", "--print");
		    System.err.println("sbl: " + msg);
		    System.exit(1);
		}
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
	    } else if (argv[indx].equals("--selfSigned")) {
		allowSelfSigned = true;
	    } else if (argv[indx].equals("--stacktrace")) {
		stacktrace = true;
	    } else if (argv[indx].equals("--truststore")) {
		indx++;
		if (indx == argv.length) {
		    String msg = errorMsg("missingArg", "--truststore");
		    System.err.println("sbl: " + msg);
		    System.exit(1);
		}
		trustStore = new File(argv[indx]);
	    } else if (argv[indx].equals("--truststorePW")) {
		indx++;
		if (indx == argv.length) {
		    String msg = errorMsg("missingArg", "--truststorePW");
		    System.err.println("sbl: " + msg);
		    System.exit(1);
		}
		trustStorePW = argv[indx].toCharArray();
	    } else if (argv[indx].startsWith("-")) {
		System.err.println("sbl: " + errorMsg("badOption", argv[indx]));
		System.exit(1);
	    } else {
		break;
	    }
	    indx++;
	}
	String fname = (argv.length-indx == 1)? argv[indx]: null;
	configFile = fnexpand(fname);

	if (create) {
	    File stdoutFile = new File("-");
	    File configFile1 = (argv.length-indx == 2)?
		(argv[indx].equals("-"))? stdoutFile:
		new File(argv[indx]): null;
	    File configFile2 = (argv.length-indx == 2)?
		(argv[indx].equals("-"))? stdoutFile:
		new File(argv[indx+1]): null;
	    boolean json = false;

	    if (configFile1 == null && configFile2 == null) {
		json = true;
	    } else {
		if (configFile1 == null || configFile2 == null) {
		    System.err.println("sbl: " + errorMsg("noConfigFile"));
		    System.exit(1);
		}
		if (configFile1 != stdoutFile && configFile1.exists()) {
		    String msg =  errorMsg("existingConfigFile", argv[indx]);
		    System.err.println("sbl: " + msg);
		    System.exit(1);
		}
		if (configFile2 != stdoutFile && configFile2.exists()) {
		    String msg =  errorMsg("existingConfigFile", argv[indx+1]);
		    System.err.println("sbl: " + msg);
		    System.exit(1);
		}
		if (configFile1 == stdoutFile && configFile2 == stdoutFile) {
		    String msg = errorMsg("duplicateStdout");
		    System.err.println("sbl: " + msg);
		    System.exit(1);
		}
	    }
	    boolean failed = false;
	    if (rlist.size() == 0) {
		System.err.println("sbl: " + errorMsg("rlistEmpty"));
		failed = true;
	    }
	    if (usr == null) {
		System.err.println("sbl: " + errorMsg("noUser"));
		failed = true;
	    }
	    if (uriS == null) {
		System.err.println("sbl: " + errorMsg("noURI"));
		failed = true;
	    }
	    if (baseS == null) {
		System.err.println("sbl: " + errorMsg("noBASE"));
		failed = true;
	    }
	    String name = (nm == null)? "user": nm;
	    int i = 0;
	    for (char ch: name.toCharArray()) {
		if (!((i++ == 0)?
		      Character.isJavaIdentifierStart(ch):
		      Character.isJavaIdentifierPart(ch))) {
		    System.err.println("sbl: " + errorMsg("badName", name));
		    failed = true;
		    break;
		}
	    }
	    if (failed) {
		System.exit(1);
	    }

	    ConfigEditor cpe2  = new ConfigEditor();

	    String[] keypair =
		SecureBasicUtilities.createPEMPair(null,null);
	    cpe2.setRecipients(rlist);
	    cpe.set(null, "title", json? name: configFile1.getName());
	    cpe2.set(null, "title", json? name: configFile2.getName());
	    cpe.set(null, "base64.keypair.publicKey", keypair[1]);
	    cpe2.set(null, "ebase64.keypair.privateKey", keypair[0]);
	    if (trustStore != null) {
		cpe2.set(null, "trustStore.file",
			 trustStore.getCanonicalPath());
		if (trustStorePW == null) {
		    cpe2.set(null, "ebase64.trustStore.password",
			     "changeit");
		} else {
		    cpe2.set(null, "ebase64.trustStore.password",
			     new String(trustStorePW));
		}
	    }
	    if (allowLoopback) {
		cpe2.set(null, "trust.allow.loopback", "true");
	    }
	    if (allowSelfSigned) {
		cpe2.set(null, "trust.selfsigned", "true");
	    }
	    cpe.set(null, name +".user", usr);
	    cpe.set(null, name +".mode", "2");
	    String pw = genpw();
	    cpe.set(null, "base64." + name +".password", pw);
	    cpe.set(null, name +".base", baseS);
	    // for cross checking
	    cpe.set(null, name +".base", baseS);
	    cpe.set(null, name +".uri", uriS);
	    // cpe.set(null, name +".uri", uriS);
	    cpe.set(null, name +".description", "login data");
	    cpe2.set(null, name +".user", usr);
	    cpe2.set(null, name +".mode", "2");
	    cpe2.set(null, "ebase64." + name +".password", pw);
	    cpe2.set(null, name +".base", baseS);
	    cpe2.set(null, name +".uri", uriS);
	    cpe2.set(null, name +".description", "login data");

	    if (json) {
		System.out
		    .format("{\"serverInfo\": %s, \"clientInfo\": \"%s\"}\n",
			    cpe.toJSON(), cpe2.toBase64());
	    } else {
		cpe.save(configFile1);
		cpe2.save(configFile2);
	    }
	    System.exit(0);
	}

	if (print) {
	    if (configFile == null) {
		System.err.println("sbl: " + errorMsg("noConfigFile"));
		System.exit(1);
	    }
	    try {
		cpe.loadFile(configFile);
		Properties props = cpe.getDecodedProperties();
		if (printPassword) {
		    if (nm == null) {
			System.err.println("sbl: " + errorMsg("noName"));
			System.exit(1);
		    }
		    Object val = props.getProperty(nm + ".password");
		    val = (val instanceof String)? val:
			new String((char[]) val);
		    System.out.println(val);
		}
		if (printUser) {
		    if (nm == null) {
			System.err.println("sbl: " + errorMsg("noName"));
			System.exit(1);
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
			System.exit(1);
		    }
		    System.out.println(props
				       .getProperty(nm + ".description"));
		}
		if (printMode) {
		    if (nm == null) {
			System.err.println("sbl: " + errorMsg("noName"));
			System.exit(1);
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
		if (stacktrace) {
		    e.printStackTrace();
		}
		System.exit(1);
	    }
	}

	if (configFile != null && configFile.exists()) {
	    if (rlist.size() > 0) {
		// add a new keypair with a new list of recipients
		cpe.loadFile(configFile);
		String[] keypair =
		    SecureBasicUtilities.createPEMPair(null,null);
		cpe.setRecipients(rlist);
		if (cpe.set(null, "ebase64.keypair.privateKey", keypair[0])
		    && cpe.set(null, "base64.keypair.publicKey", keypair[1])) {
		    cpe.save(configFile);
		    System.exit(0);
		} else {
		    System.err.println("sbl: " + errorMsg("encryptFailed"));
		    System.exit(1);
		}
	    } else {
		ConfigEditor cpe3 = new ConfigEditor();
		cpe3.loadFile(configFile);
		Properties decoded = cpe3.getDecodedProperties();
		String tmp = decoded.getProperty("recipients");
		// System.out.println("tmp = " + tmp);
		String[] recipients = (tmp == null)? new String[0]:
		    ConfigPropUtilities.decodeRecipients(tmp);
		String cemode = decoded.getProperty("need");
		String dmode = decoded.getProperty("sbl.downloaded");
		if (dmode == null) dmode = "falset";
		if (dmode.equals("true")) {
		    // if the "need" property is not null and
		    // the "sbl.downloaded" property has the value "true",
		    // then delete the config file when SBL exits.
		    configFile.deleteOnExit();
		    if (cemode == null) {
			// signal that we should use the downloaded
			// SBL file with a simplified UI as this is
			// a standard case with only one login per file.
			cemode = "usesbl";
		    }
		}
		if (cemode == null) {
		    // nothing to do - we just go on to the
		    // normal case.
		} else if (cemode.equals("pgpkey") || cemode.equals("usesbl")) {
		    String uriStr = decoded.getProperty("base");
		    if (uriStr == null) {
			String key = null;
			for (String k: decoded.stringPropertyNames()) {
			    if (k.endsWith(".description")) {
				int  ind = k.lastIndexOf('.');
				key = k.substring(0, ind);
				break;
			    }
			}
			uriStr = decoded.getProperty(key +".base");
		    }
		    final String uriS2 = uriStr;
		    String titleText = errorMsg("titleText", uriS2);
		    boolean noUpload = cemode.equals("usesbl");
		    ActionListener button1AL = new ActionListener() {
			    boolean justCopy = noUpload;
			    boolean visitBrowser = noUpload; // for justCopy
			    public void actionPerformed(ActionEvent e) {
				if (justCopy) {
				    String key = null;
				    for (String k: cpe.getProperties()
					     .stringPropertyNames()) {
					if (k.endsWith(".description")) {
					    int ind = k.lastIndexOf('.');
					    key = k.substring(0, ind);
					    break;
					}
				    }
				    Properties dprops =
					cpe.getDecodedProperties();
				    String user =
					dprops.getProperty(key + ".user");
				    String uriString =
					dprops.getProperty(key + ".uri");
				    if (noUpload) {
					try {
					    button2URI = new URI(uriString);
					} catch (URISyntaxException euri) {}
				    }
				    Clipboard cb = frame.getToolkit()
					.getSystemClipboard();
				    boolean hadPassphrase = cpe.hasPassphrase();
				    char[] passwd = getSecurePW(frame, key);
				    if (!hadPassphrase) cpe.clearPassphrase();
				    if (passwd == null) return;
				    String password = new String(passwd);
				    StringSelection selection1 =
					new StringSelection(user);
				    StringSelection selection2 =
					new StringSelection(password);
				    StringSelection2 selection = new
					StringSelection2(selection1,
							 selection2);
				    cb.setContents(selection, selection);
				    if (visitBrowser) {
					// just do this once.
					Desktop desktop = Desktop.getDesktop();
					if (desktop.isSupported
					    (Desktop.Action.BROWSE)) {
					    try {
						URI uri = new URI(uriString);
						desktop.browse(uri);
					    } catch (Exception ee) {
						SwingErrorMessage
						    .format("%s",
							    ee.getMessage());
						SwingErrorMessage
						    .displayConsoleIfNeeded();
					    }
					}
					button1.setText(errorMsg("COPY"));
					if (noUpload) {
					    button2.setText(errorMsg("LOGIN"));
					    button2.setEnabled(true);
					}
					button3.setText(errorMsg("EXIT"));
					visitBrowser = false;
				    }
				} else {
				    try {
					String encodedUser =URLEncoder
					    .encode(button1User, UTF8);
					URL url = new URL(uriS2 + button1Path);
					button2URI = new
					    URI(uriS2 + button1Path);
					URLConnection urlc =
					    url.openConnection();
					if (urlc instanceof
					    HttpURLConnection) {
					    HttpURLConnection c =
						(HttpURLConnection) urlc;
					    c.setRequestMethod("POST");
					    c.setRequestProperty
						("content-type",
						 "application/pgp-keys");
					    c.setRequestProperty
						("content-length",
						 "" + button1Arr.length);
					    c.setDoOutput(true);
					    OutputStream os =
						c.getOutputStream();
					    os.write(button1Arr);
					    os.flush();
					    os.close();
					    // processHttpURCL will set up
					    // cpe if the status code is 201
					    System.out.println("frame = "
							       + frame);
					    switch(processHttpURLC
						   (frame, button1User, c,
						    "pgpCnflct")) {
					    case -1:
						//Internal error;
						// the rest are
						// HTTP status codes
						System.exit(1);
						break;
					    case 200:
					    case 204:
					    case 205:
						System.exit(0);
						break;
					    case 201:
						// either new URI copied to
						// clipboard or browser told
						// which URI to visit and
						// user name + password copied
						// to clipboard.
						justCopy = true;
						button1
						    .setText(errorMsg("COPY"));
						button2.setEnabled(true);
						button3
						    .setText(errorMsg("EXIT"));
						return;
					    case 202:
						System.exit(0);
						break;
					    case 409:
						System.exit(1);
						break;
					    default:
						System.exit(1);
						break;
					    }
					} else {
					    String msg =
						errorMsg("notHTTP");
					    String title =
						errorMsg("errorTitle");
					    JOptionPane.showMessageDialog
						(frame, msg, title,
						 JOptionPane.ERROR_MESSAGE);
					    System.exit(1);
					}
				    } catch (Exception ee) {
					System.err.println(ee.getMessage());
					System.exit(1);
				    }
				}
			    }
			};
		    ActionListener button2AL = (event) -> {
			String key = null;
			for (String k: cpe.getProperties()
				 .stringPropertyNames()) {
			    if (k.endsWith(".description")) {
				int ind = k.lastIndexOf('.');
				key = k.substring(0, ind);
				break;
			    }
			}
			Properties dprops = cpe.getDecodedProperties();
			String user = dprops.getProperty(key + ".user");
			Clipboard cb = frame.getToolkit().getSystemClipboard();
			boolean hadPassphrase = cpe.hasPassphrase();
			char[] passwd = getSecurePW(frame, key);
			if (!hadPassphrase) cpe.clearPassphrase();
			if (passwd == null) return;
			String password = new String(passwd);
			StringSelection selection1 =
			new StringSelection(user);
			StringSelection selection2 =
			new StringSelection(password);
			StringSelection2 selection = new
			StringSelection2(selection1,
					 selection2);
			cb.setContents(selection, selection);

			Desktop desktop = Desktop.getDesktop();
			if (desktop.isSupported
			    (Desktop.Action.BROWSE)) {
			    try {
				desktop.browse(button2URI);
			    } catch (Exception ee) {
				SwingErrorMessage
				    .format("%s",
					    ee.getMessage());
				SwingErrorMessage
				    .displayConsoleIfNeeded();
			    }
			}
		    };
		    SwingUtilities.invokeLater(() -> {
			    frame = new JFrame(errorMsg("title", titleText));
			    SwingErrorMessage.setComponent(frame);
			    JPanel panel = new JPanel(new GridLayout(2, 1));
			    JPanel panel2 = null;
			    String key = null;
			    if (noUpload) {
				fixupEntries(null, cpe3);
				if (entries.size() == 1) {
				    for (String k: entries) {
					key = k;
				    }
				}
			    }
			    String path = (key != null)?
				decoded.getProperty(key + ".uri"):
				decoded.getProperty("loginAlias");
			    String user = (key != null)?
				decoded.getProperty(key + ".user"):
				decoded.getProperty("user");
			    button1User = user;
			    button1Path = path;
			    KeyInfoPair kip = getGPGPrivateKeyInfo(user);
			    String fpr = null;
			    String gpgPublicKey = null;
			    byte[] array = null;
			    String errorMessage = null;
			    String message = null;
			    JLabel label = null;
			    button1 = new JButton(errorMsg("OK"));
			    button2 = new JButton(errorMsg("LOGIN"));
			    button2.setEnabled(false);
			    button2.addActionListener(button2AL);
			    button3 = new JButton(errorMsg("CANCEL"));
			    button3.addActionListener((e) -> {
				    System.exit(0);
				});
			    if (kip.fprs.length == 1) {
				fpr = kip.fprs[0];
				gpgPublicKey = getGPGPublicKey(fpr);
				try {
				    array = gpgPublicKey.getBytes("UTF-8");
				} catch (UnsupportedEncodingException ec) {}
				message = errorMsg("foundKey", user, fpr);
				label = new JLabel(message);
				panel2 = new JPanel(new GridLayout(1, 3));
				panel2.add(button1);
				panel2.add(button2);
				panel2.add(button3);
			    } else {
				int len = kip.fprs.length;
				errorMessage = errorMsg("foundKeys", len);
				label = new JLabel(errorMessage);
				panel2 = new JPanel(new GridLayout(1, 1));
				panel2.add(button3);
				// System.exit(1);
			    }
			    panel.add(label);
			    panel.add(panel2);
			    // byte[] arr = array;
			    button1Arr = array;

			    button1.addActionListener(button1AL);
			    frame.setLayout(new BorderLayout());
			    frame.add(panel, BorderLayout.CENTER);
			    frame.pack();
			    frame.addWindowListener(new WindowAdapter() {
				    public void windowClosing(WindowEvent e) {
					System.exit(0);
				    }
				});
			    frame.setVisible(true);
			    if (errorMessage == null) {
				cpe = cpe3;
				cpe.setPWOwner(frame);
				if (cpe.hasKey("ebase64.trustStore.password")) {
				    cpe.requestPassphrase(frame);
				}
				configTrust(frame);
			    }
			});
		    return;
		} else if (cemode.equals("sbl")) {
		    configFile = null;
		    final Properties ourProps = cpe3.getProperties();
		    init3Consumer = (key) -> {
			Properties cpeProps = cpe.getProperties();
			String keys[] = {
			    "recipients",
			    "trust.allow.loopback",
			    "trust.selfsigned",
			    "trustStore.file",
			    "ebase64.trustStore.password"
			};

			String tpw = cpeProps.getProperty
			    ("ebase64.trustStore.password");
			if (tpw != null) {
			    // if the server has the user's GPG
			    // public key, then we can use GPG.
			    // This test works in the rare case
			    // where a trust store file name and password is
			    // provided by the server, which should
			    // occur only during testing or some local use.
			    cpe.useGPG(!tpw.startsWith("==="));
			} else {
			    String u = ourProps.getProperty("user");
			    KeyInfoPair kpi = getGPGPrivateKeyInfo(u);
			    if (kpi == null) {
				cpe.useGPG(false);
			    } else {
				// To use GPG, the user name should
				// be suitable for use as a key ID,
				// and only one GPG key should match.
				if (kpi.keyids.length == 1) {
				    cpe.useGPG(true);
				} else {
				    cpe.useGPG(false);
				}
			    }
			}
			cpe.setPWOwner(frame);

			cpe.set(frame, key + ".description",
				"login for " + ourProps.getProperty("base"));

			for (String k: keys) {
			    String s = ourProps.getProperty(k);
			    if (s != null) {
				cpeProps.setProperty(k, s);
			    }
			}
			String keys2[] = {
			    "user",
			    "base",
			    "uri",
			    "mode"
			};
			for (String k: keys2) {
			    String s = ourProps.getProperty(k);
			    if (s != null) {
				cpeProps.setProperty(key +"." + k, s);
			    }
			}
			cpe.set(frame, "ebase64." +  key + ".password",
				new String(genpw()));
			String base = ourProps.getProperty("base");
			if (!base.endsWith("/")) base = base + "/";
			cpe.set(frame, key + ".base", base);
			String loginAlias = ourProps.getProperty("loginAlias");
			cpe.set(frame, key +".uri",
				"$(" + key + ".base)" + loginAlias);
			SecureBasicUtilities.Mode mode = null;
			try {
			    mode = modes[Integer.valueOf
					 (ourProps.getProperty("mode"))];
			} catch (Exception e){
			    mode = null;
			}
			try {
			    if (mode != SecureBasicUtilities.Mode.PASSWORD
				&& mode != SecureBasicUtilities.Mode.DIGEST) {
				String[] keypair = SecureBasicUtilities
				    .createPEMPair(null,null);
				cpe.set(frame, "ebase64.keypair.privateKey",
					keypair[0]);
				cpe.set(frame, "base64.keypair.publicKey",
					keypair[1]);
			    }
			    cpe.set(frame, "ebase64." + key + ".password",
				    genpw());
			} catch (GeneralSecurityException se) {
			    System.err.println(se.getMessage());
			}
		    };
		    SwingUtilities.invokeLater(() -> {
			    // System.out.println("got here 2");
			    String ourbase = decoded.getProperty("base");
			    String ourlogin = decoded.getProperty("loginAlias");
			    String titleText = errorMsg("titleText", ourbase);
			    String user = decoded.getProperty("user");
			    frame = new JFrame(errorMsg("title", titleText));
			    SwingErrorMessage.setComponent(frame);
			    JPanel panel = new JPanel(new GridLayout(2, 1));
			    String message1 =
				errorMsg("loginFor", user, ourbase);
			    JLabel label = new JLabel(message1);
			    panel.add(label);
			    button1 = new JButton(errorMsg("OK"));
			    configFile = null;
			    button1.addActionListener((ae)-> {
					if (configFile == null) {
					    configFile = getFile(frame);
					    if (configFile == null) {
						System.exit(0);
					    }
					    try {
						URI serverURI =
						    new URI(ourbase + ourlogin);
						button1URI = serverURI;
						cpe.setPWOwner(frame);
						ServerResponse resp =
						    sendSBLToServer(serverURI,
								    "user");
						String title =
						    errorMsg("rcodeTitle");
						int rc = resp.getResponseCode();
						String rmsg = resp.getMessage();
						String msg = (rmsg != null)?
						    rmsg:
						    errorMsg("sblSuccess", rc);
						switch(resp.rcode) {
						case 200: // OK
						case 201: // Created.
						case 204: // No Content
						    JOptionPane
							.showMessageDialog
							(frame, msg, title,
							 JOptionPane
							 .PLAIN_MESSAGE);
						    cpe.save(configFile);
						    String b1t =
							errorMsg("COPY");
						    String b3t =
							errorMsg("EXIT");
						    button1.setText(b1t);
						    button2.setEnabled(true);
						    button3.setText(b3t);
						    setupBrowser(frame,
								 button1URI);
						    button2URI = button1URI;
						    button1URI = null;
						    break;
						case 202: // Accepted.
						    JOptionPane
							.showMessageDialog
							(frame, msg, title,
							 JOptionPane
							 .PLAIN_MESSAGE);
						    cpe.save(configFile);
						    System.exit(0);
						    break;
						default:
						    msg = getRespErrorMsg
							(resp);
						    JOptionPane
							.showMessageDialog
							(frame, msg, title,
							 JOptionPane
							 .PLAIN_MESSAGE);
						    System.exit(1);
						}
					    } catch (Exception e) {
						System.err
						    .println
						    (e.getMessage());
					    }
					} else {
					    // config file set so we just
					    // visit the URL and set up
					    // copy and paste.
					    setupBrowser(frame, button1URI);
					}
			    });
			    button2 = new JButton(errorMsg("LOGIN"));
			    button2.setEnabled(false);
			    button2.addActionListener((ae) -> {
				    setupBrowser(frame, button2URI);
				});
			    button3 = new JButton(errorMsg("CANCEL"));
			    button3.addActionListener((ae) -> {
				    System.exit(0);
				});
			    JPanel panel2 = new JPanel(new GridLayout(1, 3));
			    panel2.add(button1);
			    panel2.add(button2);
			    panel2.add(button3);
			    panel.add(panel2);
			    frame.add(panel);
			    frame.pack();
			});

		    // String uriS2 = decoded.getProperty("base");
		    // String titleText = errorMsg("titleText2", uriS2);
		    // System.out.println("*** setting recipients");
		    cpe.setRecipients(recipients);
		    SwingUtilities.invokeAndWait(() -> {
			    frame.setVisible(true);
			});
		    init3Consumer.accept("user");
		    SwingUtilities.invokeLater(() -> {
			    cpe.setPWOwner(frame);
			    configTrust(frame);
			});
		    // See what we got for testing:
		    // cpe.save(new File("output.sbl"));
		    // System.exit(0);
		    return;
		} else if (cemode != null && !cemode.equals("usesbl")) {
		    System.exit(1);
		}
	    }
	}

	// Need to make this final for use in a lambda expression
	final Consumer<String> init3 = init3Consumer;

	if (GraphicsEnvironment.isHeadless()) {
	    System.err.println("sbl: " + errorMsg("headless"));
	    System.exit(1);
	}

	final String ournm = nm;
	SwingUtilities.invokeLater(() -> {
		boolean loadedAtStart = false;
		JPanel panel = new JPanel(new GridLayout(3, 3));
		JLabel titleLabel = new JLabel(" ");
		JLabel descrLabel = new JLabel(" ");
		JMenuItem serverSBLMI = new
		    JMenuItem(localeString("serverSBL"));

		JButton loadButton = new JButton(localeString("loadButton"));
		JComboBox<String> selectSiteCB =
		    new JComboBox<>(new Vector<String>());
		selectSiteCB.addItem(localeString("selectSiteCB"));
		selectSiteCB.setSelectedIndex(0);
		serverSBLMI.setEnabled(false);
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
			    cpe.setPWOwner(frame);
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
			    if (stacktrace) {
				eio.printStackTrace();
				System.exit(1);
			    }
			    SwingErrorMessage.displayConsoleIfNeeded();
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
			ops = null; // so we get a new private key
			checkConfig = false;
			cpe.setPWOwner(frame);
			configTrust(frame);
			fixupEntries(selectSiteCB);
			addEntryButton.setEnabled(true);
			editEntriesButton.setEnabled(true);
			selectSiteCB.setEnabled(entries.size() > 0);
			serverSBLMI.setEnabled(false);
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
			    String base = entry.base.getText().trim();
			    if (!base.endsWith("/")) base = base + "/";
			    cpe.set(frame, name + ".base", base);
			    String uri = entry.uri.getText().trim();
			    if (uri.startsWith("/")) uri = uri.substring(1);
			    cpe.set(frame, name + ".uri",
				    "$(" + name + ".base)" + uri);
			    cpe.set(frame, name + ".user",
				    entry.user.getText());
			    cpe.set(frame, "ebase64." +  name + ".password",
				    new String(genpw()));
			    int ind = entry.modeCB.getSelectedIndex();
			    if (ind < 0) ind = 0;
			    cpe.set(frame, name + ".mode", "" + ind);
			    try {
				cpe.save(configFile);
				fixupEntries(selectSiteCB);
				selectSiteCB.setEnabled(entries.size() > 0);
				serverSBLMI.setEnabled(false);
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
			cpe.clearPassphrase();
			try {
			    cpe.save(configFile);
			    fixupEntries(selectSiteCB);
			    selectSiteCB.setEnabled(entries.size() > 0);
			    serverSBLMI.setEnabled(sendSBLDirectly);
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
			    serverSBLMI.setEnabled(false);
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
			    serverSBLMI.setEnabled(true);
			}
		    });

		if (loadedAtStart) {
		    if (ournm != null) {
			// allow the command line to specify the name
			int len = selectSiteCB.getItemCount();
			for (int i = 1; i < len; i++) {
			    if (selectSiteCB.getItemAt(i).equals(ournm)) {
				selectSiteCB.setSelectedIndex(i);
				serverSBLMI.setEnabled(true);
				break;
			    }
			}
		    } else if (entries.size() == 1) {
			// If the configuration file was loaded from a file
			// specified on the command line and there is only
			// a single item to select, select that one
			// automatically. The use case is a user who opens
			// a file provided by a browser and there is only
			// a single entry.
			selectSiteCB.setSelectedIndex(1);
			serverSBLMI.setEnabled(true);

		    }
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
			Properties dprops = cpe.getDecodedProperties();
			Component pwowner = cpe.getPWOwner();
			// Not known if this is ebase64 encoded so we
			// have to explicitly set the CPE owner component
			cpe.setPWOwner(frame);
			Object val = dprops.getProperty(name + ".password");
			if (val == null) {
			    val = dprops.get("ebase64." + name + ".password");
			}
			cpe.setPWOwner(pwowner);
			String value = (val instanceof String)? (String) val:
			    new String((char[])val);
			Clipboard cb = panel.getToolkit()
			    .getSystemClipboard();
			StringSelection selection = new StringSelection(value);
			cb.setContents(selection, selection);
		    });

		ActionListener visitAL = (e) -> {
		    console.addSeparatorIfNeeded();
		    String name = getName(selectSiteCB);
		    if (name == null) return;
		    Component pwowner = cpe.getPWOwner();
		    cpe.setPWOwner(frame);
		    Properties props = cpe.getEncodedProperties();
		    Properties dprops = cpe.getDecodedProperties();
		    String user = dprops.getProperty(name + ".user");
		    String uriString = dprops.getProperty(name + ".uri");
		    URI uri = null;
		    Clipboard cb = panel.getToolkit().getSystemClipboard();
		    boolean hadPassphrase = cpe.hasPassphrase();
		    char[] passwd = getSecurePW(panel, name);
		    if (!hadPassphrase) cpe.clearPassphrase();
		    cpe.setPWOwner(pwowner);
		    if (passwd == null) return;
		    String password = new String(passwd);
		    StringSelection selection1 = new StringSelection(user);
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
			if (stacktrace) {
			    ex.printStackTrace();
			}
		    }
		};

		visitSiteButton.addActionListener(visitAL);

		ActionListener serverSBLAL = (e) -> {
		    Component pwowner = cpe.getPWOwner();
		    cpe.setPWOwner(frame);
		    int ind = selectSiteCB.getSelectedIndex();
		    if (sendSBLDirectly || ind > 0) {
			Properties cpeProps = cpe.getEncodedProperties();
			Properties decoded = cpe.getDecodedProperties();
			ConfigProperties props = new ConfigProperties
			    ("application/vnd.bzdev.sblauncher");
			String key = (sendSBLDirectly)? sendSBLDirectlyKey:
			    getName(selectSiteCB);
			String ourkeys[] = {
			    "base64.keypair.publicKey",
			    key + ".description",
			    "ebase64." + key + ".password",
			    key + ".base",
			    key + ".uri",
			    key + ".user",
			    key + ".mode"
			};
			for (String k: ourkeys) {
			    String value;
			    String kk = null;
			    if (k.startsWith("ebase64.")) {
				value = new String((char[])decoded.get(k));
				kk = k.substring(1);
			    } else if (k.startsWith("base64.")) {
				String kk2 = k.substring(7);
				value = (String)decoded.get(kk2);
			    } else {
				value = cpeProps.getProperty(k);
			    }
			    // System.out.println(k + ": " + value);
			    if (value != null) {
				if (kk != null) {
				    props.setProperty(kk, value);
				} else {
				    props.setProperty(k, value);
				}
			    }
			}
			cpe.setPWOwner(pwowner);
			String string = props.store();
			if (sendSBLDirectly) {
			    String uriS3 = props.getProperty(key + ".uri");
			    try {
				byte[] arr = string.getBytes("UTF-8");
				URL url = new URL(uriS3);
				URLConnection urlc = url.openConnection();
				if (urlc instanceof HttpURLConnection) {
				    HttpURLConnection c =
					(HttpURLConnection) urlc;
				    c.setRequestMethod("POST");
				    String mediaType =
					"application/vnd.bzdev.sblogindata";
				    c.setRequestProperty("content-type",
							 mediaType);
				    c.setRequestProperty("content-length",
							 "" + arr.length);
				    c.setDoOutput(true);
				    OutputStream os =
					c.getOutputStream();
				    os.write(arr);
				    os.flush();
				    os.close();
				    String user = props
					.getProperty(key + ".user");
				    switch(processHttpURLC(frame, user, c,
							   "sblCnflct")) {
				    case -1:
					//Internal error; the rest are
					// HTTP status codes
					System.exit(1);
					break;
				    case 200:
				    case 204:
				    case 205:
					System.exit(0);
					break;
				    case 201:
					// either new URI copied to
					// clipboard or browser told
					// which URI to visit.
					System.exit(0);
					break;
				    case 202:
					System.exit(0);
					break;
				    case 409:
					System.exit(1);
					break;
				    default:
					System.exit(1);
					break;
				    }
				} else {
				    String msg = errorMsg("notHTTP");
				    String title = errorMsg("errorTitle");
				    JOptionPane.showMessageDialog
					(frame, msg, title,
					 JOptionPane.ERROR_MESSAGE);
				    System.exit(1);
				}
			    } catch (Exception ee) {
				System.err.println(ee.getMessage());
				System.exit(1);
			    }
			} else {
			    Clipboard cb = panel.getToolkit()
				.getSystemClipboard();
			    StringSelection selection = new
				StringSelection(string);
			    cb.setContents(selection, selection);
			}
		    }
		};

		ActionListener gpgPublicKeyAL = (e) -> {
		    console.addSeparatorIfNeeded();
		    KeyInfoPair keysInfo = getGPGPrivateKeyInfo();
		    if (keysInfo.keyids.length == 0) return;
		    int keyind = -1;
		    if (keysInfo.keyids.length == 1) {
			keyind = 0;
		    } else {
			JComboBox<String> cb = new JComboBox<>(keysInfo.keyids);
			int status = JOptionPane
			    .showConfirmDialog(frame, cb,
					       localeString("GPGKeyIDTitle"),
					       JOptionPane.OK_CANCEL_OPTION);
			if (status == 0) {
			    keyind = cb.getSelectedIndex();
			} else {
			    return;
			}
		    }
		    if (keyind >= 0) {
			String fpr = keysInfo.fprs[keyind];
			String gpgkey = getGPGPublicKey(fpr);
			Clipboard cb = panel.getToolkit()
			    .getSystemClipboard();
			StringSelection selection = new StringSelection(gpgkey);
			cb.setContents(selection, selection);
		    }
		};

		generateButton.addActionListener((e) -> {
			console.addSeparatorIfNeeded();
			Component pwowner = cpe.getPWOwner();
			cpe.setPWOwner(frame);
			String name = getName(selectSiteCB);
			String user = cpe.getEncodedProperties()
			    .getProperty(name + ".user");
			if (name == null) return;
			boolean hadPassphrase = cpe.hasPassphrase();
			char[] passwd = getSecurePW(panel, name);
			cpe.setPWOwner(pwowner);
			if (!hadPassphrase) cpe.clearPassphrase();
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
		JMenuItem openMenuItem  = new JMenuItem(localeString("Open"));
		JMenuItem gpgMenuItem =
		    new JMenuItem(localeString("gpgPublicKey"));
		fileMenu.setMnemonic(vk("VK_FILE"));
		menuItem.setAccelerator(KeyStroke.getKeyStroke
					(KeyEvent.VK_W,
					 InputEvent.CTRL_DOWN_MASK));
		menuItem.addActionListener((ae) -> {
			System.exit(0);
		    });
		fileMenu.add(menuItem);
		openMenuItem.setAccelerator(KeyStroke.getKeyStroke
					    (KeyEvent.VK_O,
					     InputEvent.CTRL_DOWN_MASK));
		openMenuItem.addActionListener(loadAL);
		fileMenu.add(openMenuItem);
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

		menuItem =
		menuItem = gpgMenuItem;
		menuItem.addActionListener(gpgPublicKeyAL);
		fileMenu.add(menuItem);

		serverSBLMI.setEnabled(false);
		serverSBLMI.addActionListener(serverSBLAL);

		fileMenu.add(serverSBLMI);

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

		if (init3 != null) {
		    while (true) {
			File f = getFile(frame);
			if (f == null) {
			    System.exit(0);
			}
			if (f.exists()) {
			    String fn = f.getName();
			    String msg = errorMsg("fileExists", fn);
			    String title = errorMsg("errorTitle");
			    JOptionPane.showMessageDialog
				(frame, msg, title,
				 JOptionPane.ERROR_MESSAGE);
			} else {
			    configFile = f;
			    cpe.set(frame, "title", configFile.getName());
			    try {
				cpe.save(configFile);
			    } catch (IOException eio) {
			    }
			    break;
			}
		    }
		    String key;
		    do {
			key = JOptionPane
			    .showInputDialog(frame,
					     localeString("name"),
					     localeString("nameTitle"),
					     JOptionPane.QUESTION_MESSAGE);
			if (key == null) return;
			int i = 0;
			boolean loop = false;
			for (char ch: key.toCharArray()) {
			    if (!((i++ == 0)?
				  Character.isJavaIdentifierStart(ch):
				  Character.isJavaIdentifierPart(ch))) {
				loop = true;
				break;
			    }
			}
			if (loop) continue;
		    } while (entries.contains(key));
		    init3.accept(key);
		    try {
			cpe.save(configFile);
			cpe.loadFile(configFile);
			cpe.setPWOwner(frame);
			configTrust(frame);
			loadButton.setEnabled(false);
			openMenuItem.setEnabled(false);
			editEntriesButton.setEnabled(true);
			selectSiteCB.setEnabled(false);
			findMenuItem.setEnabled(false);
			serverSBLMI.setText(errorMsg("serverSBL2"));
			serverSBLMI.setEnabled(true);
			gpgMenuItem.setEnabled(false);
			sendSBLDirectly = true;
			sendSBLDirectlyKey = key;
			String msg = errorMsg("init3Hint");
			String title = errorMsg("hintTitle");
			JOptionPane.showMessageDialog
			    (frame, msg,title,
			     JOptionPane.PLAIN_MESSAGE);
		    } catch (IOException eio) {
			String fn = configFile.getName();
			String msg = errorMsg("canNotSave", fn);
			String title = errorMsg("errorTitle");
			JOptionPane.showMessageDialog
			    (frame, msg, title,
			     JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		    }
		}
	    });
    }
}
