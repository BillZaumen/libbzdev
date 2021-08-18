package org.bzdev.swing;

import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.Format;
import java.util.ArrayList;
import java.util.*;
import java.util.regex.*;

import javax.swing.*;
import javax.swing.filechooser.*;
import javax.swing.table.*;

import org.bzdev.lang.Callable;
import org.bzdev.lang.UnexpectedExceptionError;
import org.bzdev.util.CopyUtilities;
import org.bzdev.swing.table.ITextCellEditor;

//@exbundle org.bzdev.swing.lpack.Swing

/**
 * Property-editor class.
 * This class supports configuration files that are represented
 * as Java property files using the syntax described in the
 * documentation for {@link java.util.Properties#load(Reader)}.
 * The editor displays a window (whether a frame or dialog) containing
 * a menubar, some buttons, and a table with two columns: one labeled
 * <STRONG>Property</STRONG> and one labeled <STRONG>Value</STRONG>.
 * The initial properties may have keys that cannot be edited,
 * although their values can be edited. When this is the case, the
 * background for this portion of the table will have a different
 * color than the rest of the table.  To insert a above a row, or to
 * move or delete the row, the entire row must be selected. A table
 * can also be configured to suppress some of the buttons located just
 * above the table. Some keys in the table may consist of one or ore
 * dashes (minus signs). There are treated as spacers and are ignored
 * when the properties are written or used outside of the editor.
 * <P>
 * Property values may be encrypted using GPG. The keys for these
 * properties start with the substring "ebase74." and the values are
 * stored in an encrypted form. GPG Agent should be configured so that
 * decryption can be used conveniently. To use GPG Agent on Debian
 * Linux systems, add the following lines to the .bashrc script:
 * <BLOCKQUOTE><PRE><CODE>
 *     GPG_TTY=$(tty)
 *     export GPG_TTY
 * </CODE></PRE></BLOCKQUOTE>
 * <P>
 * The file format is actually a stylized version of a standard Java
 * properties file. The first additional constraint is that the first
 * line, should be a comment (hence the initial '#') of the form
 * <BLOCKQUOTE><PRE><CODE>
 * #(M.T MEDIATYPE)
 * </CODE></PRE></BLOCkQUOTE>
 * where <CODE>MEDIATYPE</CODE> is the media type assigned to the
 * property file. This convention is used when {@link ConfigPropertyEditor}
 * reads or writes a configuration file.  In addition each line will
 * be terminated with a carriage return followed by a line feed (the
 * convention used in most RFCs for text-based formats). The first
 * line must appear exactly as shown, with no leading or trailing
 * white space, with a single space before the media type, and no
 * spaces between the media type and the terminating closing
 * parenthesis.
  * <P>
 * This format must use UTF-8 as its character set. While Java
 * historically used ISO 8859-1 for a property file's character set,
 * the methods {@link Properties#load(Reader)} and
 * {@link Properties#store(Writer,String)} are used by this class,
 * with the readers and writers configured to use the UTF-8 character
 * set, and those methods, with UTF-8, are used by this class.

 * <P>
 * Keys are restricted to ones that consist of a sequence of tokens
 * separated by periods, with each token starting with a letter 
 * followed by 0 or more letters, digits, and underscores. There are
 * two special initial tokens:
 * <UL>
 *   <LI><STRONG>base64</STRONG>. This token indicates that the value
 *      is base-64 encoded.
 *   <LI><STRONG>ebase64</STRONG>. This token indicates that the value
 *      is first base64 encoded and then encrypted using GPG or PGP.
 *      One use of this field is to store passwords (e.g., for a
 *      database).  To encrypt, one will provide a list of recipients:
 *      when configuring a database, for instance, one can use this
 *      feature so that an administrator and a user can have access to
 *      the same password.
 * </UL>
 * ConfigPropertyEditor will remove the encryption and encoding to allow
 * editing of such fields. To decrypt, the user will have to enter a
 * GPG passphrase. GPG Agent should be configured for this to work
 * properly
 * <P>
 * Values that are not encoded or encrypted allow variable substitutions.
 * the expression
 * <BLOCKQUOTE><PRE><CODE>
 *   $(<I>KEY</I>)
 * </CODE></PRE></BLOCkQUOTE>
 * will be replaced with the value of the key. The order of the keys
 * does not matter, but references must not be circular.  There is not
 * an escape syntax that would allow string "$(" to be part of a value.
 * Instead, one should use base-64 encoding, in which case variable
 * substitution will not occur. Leading and trailing whitespace is
 * preserved for keys whose first token is "base64" or "ebase64", but
 * not otherwise. With any "base64" or "ebase64" token and its
 * following delimited removed, each key must be unique.
 * <P>
 * To create an instance of this class for editing a configuration
 * file for a specific application, a subclass has to be defined The
 * constructor for such a subclass is expected to call the following
 * methods:
 * <UL>
 *   <LI><STRONG>{@link addIcon(Image)}</STRONG> or
 *      <LI><STRONG>{@link addIcon(Class,String)}</STRONG>. These methods
 *      provide an icon to display when a configuration editor's window
 *      is iconified.  Normally there are multiple icons, corresponding to
 *      different sizes required by a window manager.
 *   <LI><STRONG>{@link ConfigPropertyEditor#setDefaultProperty(String,String)}</STRONG>. Some properties have default values, typically in cases were
 *      the defaults are likely to be the ones the user needs. This method
 *      should be used to define what these defaults are.
 *   <LI><STRONG>{@link ConfigPropertyEditor#addReservedKeys(String...)}</STRONG>.
 *      There may be some number of distinguished keys that are nominally
 *      expected to be present. This method will define a group of such
 *      keys. When called multiple times, each group will be separated 
 *      from the others by a series of dashes in a table's first column.
 *   <LI><STRONG>{@link ConfigPropertyEditor#setupCompleted()}</STRONG>.
 *      This method must be called by the constructor, and indicates that
 *      all the reserved keys, and any default values associated with
 *      these, have been provided.
 * </UL>
 * In addition a subclass must define the following methods:
 * <UL>
 * <LI><STRONG>errorTitle()</STRONG>. This contains the title used
 *     in dialog boxes associated with error messages.
 * <LI><STRONG>configTitle()</STRONG>. This contains the title used
 *     in an ConfigPropertyEditor window (whether a frame or a dialog).
 * <LI><STRONG>mediaType()</STRONG>. This contains the application-specific
 *     media type for a properties file, and appears in the first line
 *     of files saved by this methods in this class.
 * <LI><STRONG>extensionFilterTitle()</STRONG>. This contains the title to
 *     use in a file-chooser dialog for the extension associated with an
 *     application's configuration file.
 * <LI><STRONG>extension</STRONG>. This contains the file-name extension
 *     for an application's configuration file.
 * </UL>
 * <P>
 * The following statements provide some examples of frequently used
 * operations, assuming an instance named <CODE>editor</CODE> has
 * been created.
 * <UL>
 *     <LI><STRONG>Loading from a file</STRONG>:
 *        <BLOCKQUOTE><PRE><CODE>
 *          editor.loadFile(file);
 *        </CODE></PRE></BLOCKQUOTE>
 *     <LI><STRONG>Loading using a dialog</STRONG>:
 *        <BLOCKQUOTE><PRE><CODE>
 *            editor.showLoadDialog(component)
 *        </CODE></PRE></BLOCKQUOTE>
 *     <LI><STRONG>Opening the editor</STRONG>:
 *        <BLOCKQUOTE><PRE><CODE>
 * 	      editor.edit(null, ConfigPropertyEditor.Mode.MODAL, null, true);
 *        </CODE></PRE></BLOCKQUOTE>
 *     <LI><STRONG>Getting decoded/decrypted properties</STRONG>:
 *        <BLOCKQUOTE><PRE><CODE>
 *            Properties config = editor.getDecodedProperties();
 *        </CODE></PRE></BLOCKQUOTE>
 * </UL>
 */
public abstract class ConfigPropertyEditor {

    static final Charset UTF8 = Charset.forName("utf-8");
 
    static String errorMsg(String key, Object... args) {
	return SwingErrorMsg.errorMsg(key, args);
    }

    static String localeString(String key) {
	return SwingErrorMsg.errorMsg(key);
    }

    File file = null;
    Properties properties = null;

    Properties defaults = new Properties();

    java.util.List<Image> iconList = new LinkedList<Image>();

    /**
     * Obtain a list of images that can be used by a GUI when
     * a window associated with this instance is closed.
     * @return the images
     */
    public java.util.List<Image> getIconList() {
	return Collections.unmodifiableList(iconList);
    }

    /**
     * Constructor.
     */
    protected ConfigPropertyEditor() {
    }

    /**
     * Get the title to use in modal dialog boxes reporting
     * errors
     * @return the title
     */
    protected abstract String errorTitle();

    /**
     * Get the title for the window containing this editor.
     * @return the title
     */
    protected abstract String configTitle();

    /**
     * Get the media type for the configuration file.
     * @return the media type
     */
    protected abstract String mediaType();

    /**
     * Set the default value for a property.
     * If the key starts with "base64.", this method will apply the
     * Base-64 encoding.  If the key starts with "ebase64.", the
     * value must be first encrypted with GPG and then Base-64 encoded.
     * @param key the property key
     * @param value the default value for the specified key
     */
    protected void setDefaultProperty(String key, String value) {
	if (key.startsWith("base64.")) value = encode(value);
	defaults.setProperty(key, value);
    }

    /**
     * Get the title for a file-chooser filter for the extension
     * supported by this configuration property editor.
     * This will appear as the title for a pull-down box that allows one
     * to choose specific file extensions in an 'open' or 'save' dialog
     * box
     * @return the title
     */
    protected abstract String extensionFilterTitle();

    /**
     * Get the filename extension for configuration files.
     * @return the filename extension
     */
    protected abstract String extension();


    /**
     * Add an icon for use by a window system when this object is
     * iconified.
     * @param icon the icon
     */
    protected void addIcon(Image icon) {
	iconList.add(icon);
    }

    /**
     * Add an icon for use by a window system when this object is
     * iconified, given a class and resource.
     * The name will be used to find a resource using the class loader
     * for the specified class.
     * <P>
     * To work well with Java modules, the resource should be in the same
     * package as the class.
     * @param clasz the class
     * @param name the name of a resource
     */
    protected void addIcon(Class<?> clasz, String name) {
	iconList.add(new ImageIcon(clasz.getResource(name)).getImage());
    }

    private static void addComponent(JPanel panel, JComponent component,
				     GridBagLayout gridbag,
				     GridBagConstraints c)
    {
	gridbag.setConstraints(component, c);
	panel.add(component);
    }

    private static String decode(String value) {
	if (value == null) return EMPTY_STRING;
	byte[] data = Base64.getDecoder().decode(value);
	ByteArrayInputStream is = new ByteArrayInputStream(data);
	StringBuilder sb = new StringBuilder();
	try {
	    CopyUtilities.copyStream(is, sb, UTF8);
	} catch (IOException eio) {}
	return sb.toString();
    }

    private static String encode(String value) {
	ByteArrayOutputStream os = new ByteArrayOutputStream(value.length());
	OutputStreamWriter w = new OutputStreamWriter(os, UTF8);
	try {
	    w.write(value, 0, value.length());
	    w.flush();
	    w.close();
	} catch (IOException eio) {
	    throw new UnexpectedExceptionError(eio);
	}
	byte[] data = os.toByteArray();
	data = Base64.getEncoder().encode(data);
	return new String(data, UTF8);
    }

    private static String decrypt(String value) {
	if (value == null) return EMPTY_STRING;
	byte[] data = Base64.getDecoder().decode(value);
	ProcessBuilder pb = new ProcessBuilder("gpg", "-d");
	pb.redirectError(ProcessBuilder.Redirect.DISCARD);
	try {
	    StringBuilder sb = new StringBuilder();
	    Process p = pb.start();
	    Thread thread = new Thread(()->{
		    try {
			CopyUtilities.copyStream(p.getInputStream(), sb, UTF8);
			p.waitFor();
		    } catch(Exception e) {
		    }
	    });
	    thread.start();
	    OutputStream os = p.getOutputStream();
	    os.write(data);
	    os.flush();
	    os.close();
	    thread.join();
	    if (p.exitValue() != 0) {
		System.err.println(errorMsg("gpgFailed", p.exitValue()));
		return EMPTY_STRING;
	    }
	    return sb.toString();
	} catch (Exception e) {
	    System.err.println(errorMsg("decryption", e.getMessage()));
	    return null;
	}
    }

    private static String encrypt(String value, String[] recipients) {
	if (value == null) return EMPTY_STRING;
	LinkedList<String> args = new LinkedList<>();
	args.add("gpg");
	args.add("-o");
	args.add("-");
	for (String recipient: recipients) {
	    args.add("-r");
	    args.add(recipient);
	}
	args.add("-e");
	ProcessBuilder pb = new ProcessBuilder(args);
	pb.redirectError(ProcessBuilder.Redirect.DISCARD);
	final StringBuilder sb = new StringBuilder(256);
	try {
	    Process p = pb.start();
	    Thread thread = new Thread(() -> {
		    try {
			InputStream is = p.getInputStream();
			byte data2[] = is.readAllBytes();
			p.waitFor();
			data2 = Base64.getEncoder().encode(data2);
			sb.append(new String(data2, UTF8));
		    } catch (Exception e) {
			e.printStackTrace();
		    }
	    });
	    thread.start();
	    OutputStream os = p.getOutputStream();
	    OutputStreamWriter w = new OutputStreamWriter(os, UTF8);
	    w.write(value);
	    w.flush();
	    w.close();
	    thread.join();
	    if (p.exitValue() != 0) {
		System.err.println(errorMsg("gpgFailed", p.exitValue()));
		return null;
	    } else {
		return sb.toString();
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }

    static String[] getRecipients(Component frame) {
	ArrayList<String> list = new ArrayList<>();
	JPanel panel = new JPanel(new BorderLayout());
	JCheckBox cb = new JCheckBox(localeString("addMoreKeys"));
	panel.add(cb, BorderLayout.NORTH);
	panel.add(new JLabel(localeString("nextGPG")), BorderLayout.SOUTH);
	do {
	    cb.setSelected(false);
	    String next =
		JOptionPane.showInputDialog(frame, panel);
	    if (next != null) {
		next = next.trim();
		if (next.length() > 0) {
		    list.add(next);
		}
	    }
	} while (cb.isSelected());
	return list.toArray(new String[list.size()]);
    }

    private static final int[] EMPTY_INT_ARRAY = new int[0];
    private static final String EMPTY_STRING = "";

    int selectedIndex = -1;
    boolean addingRow = false;
    boolean editingValTF = false;
    Set<String> keySet = new HashSet<String>(32);
    boolean needSave = false;

    private static boolean endsWithIgnoreCase(String string, String tail) {
	int index = string.length() - tail.length();
	String stail = string.substring(index);
	return stail.equalsIgnoreCase(tail);
    }

    boolean doSave(Component frame, File file, boolean mode,
		   InputTablePane table) {
	Set<String> loop = checkLoops(table);
	if (loop.size() > 0) {
	    StringBuffer sb = new StringBuffer();
	    String msg = errorMsg("propertiesLoop");
	    sb.append(msg + ": ");
	    boolean first = true;
	    for (String key: loop) {
		if (first == false) {
		    sb.append("\u27F6");
		} else {
		    first = false;
		}
		sb.append(key);
	    }
	    JOptionPane.showMessageDialog(frame, sb.toString(), errorTitle(),
					  JOptionPane.ERROR_MESSAGE);
	    return false;
	}

	try {
	    if (mode || file == null) {
		File cdir = new File(System.getProperty("user.dir"))
		    .getCanonicalFile();
		JFileChooser chooser = new JFileChooser(cdir);
		FileNameExtensionFilter filter =
		    new FileNameExtensionFilter
		    (extensionFilterTitle(), extension());
		chooser.setFileFilter(filter);
		chooser.setSelectedFile(file);
		int status = chooser.showSaveDialog(frame);
		if (status == JFileChooser.APPROVE_OPTION) {
		    file = chooser.getSelectedFile();
		    String name = file.getName();
		    if (!endsWithIgnoreCase(name, "." + extension())) {
			file = new File(file.getParentFile(),
					name + "." + extension());
		    }
		} else {
		    return false;
		}
	    }
	    if (!file.exists()) {
		save(file, table);
	    } else {
		File parent = file.getCanonicalFile().getParentFile();
		File tmp = File.createTempFile("cnfgtmp", "." + extension(),
					       parent);
		tmp.deleteOnExit();
		save(tmp, table);
		File backup = new File(file.getCanonicalPath() + "~");
		Path filePath = file.toPath();
		Files.move(filePath, backup.toPath(),
			   StandardCopyOption.REPLACE_EXISTING);
		Files.move(tmp.toPath(), filePath,
			   StandardCopyOption.ATOMIC_MOVE);
	    }
	    needSave = false;
	} catch (IOException eio) {
	    String msg =
		errorMsg("saveToMsg", file.toString(), eio.getMessage());
	    JOptionPane.showMessageDialog(frame, msg, errorTitle(),
					  JOptionPane.ERROR_MESSAGE);
	    return false;
	}
	return true;
    }

    private static String chooseKey(HashSet<String> set) {
	for (String key: set) {
	    return key;
	}
	return null;
    }

    static final Pattern pattern =
	Pattern.compile(Pattern.quote("$(")
			+ "([a-zA-Z][a-zA-Z0-9_]*([.][a-zA-Z][a-zA-Z0-9_]*)*)"
			+ Pattern.quote(")"));

    private static Set<String> checkLoops(InputTablePane ipane) {
	// do a topological sort using Kahn's algorithm
	int rowCount = ipane.getRowCount();
	
	HashMap<String,HashSet<String>> inmap =
	    new HashMap<String,HashSet<String>>(64);
	HashMap<String,HashSet<String>> outmap =
	    new HashMap<String,HashSet<String>>(64);
	Set<String> results = new LinkedHashSet<String>(64);
	LinkedList<String> terminals = new LinkedList<String>();
	Properties props = new Properties(64);

	for (int i = 0; i < rowCount; i++) {
	    String key = ((String)ipane.getValueAt(i, 0));
	    if (key == null) continue;
	    key = key.trim();
	    if (key.length() == 0) continue;
	    if (key.startsWith("-")) continue;
	    String value = (String)ipane.getValueAt(i, 1);
	    if (value == null) value = "";
	    props.setProperty(key,value);
	    inmap.put(key, new HashSet<String>());
	    outmap.put(key, new HashSet<String>());
	}
	for (int i = 0; i < rowCount; i++) {
	    String key = ((String)ipane.getValueAt(i, 0));
	    if (key == null) continue;
	    key = key.trim();
	    if (key.length() == 0) continue;
	    if (key.startsWith("-")) continue;
	    HashSet<String>inmapLinks = inmap.get(key);
	    String value = props.getProperty(key);
	    if (value == null) continue;
	    Matcher matcher = pattern.matcher(value);
	    int index = 0;
	    while (matcher.find(index)) {
		int start = matcher.start();
		int end = matcher.end();
		String pkey = value.substring(start+2, end-1);
		String pval = props.getProperty(pkey);
		if (pval != null) {
		    Set<String> outmapLinks = outmap.get(pkey);
		    inmapLinks.add(pkey);
		    outmapLinks.add(key);
		}
		index = end;
	    }
	}
	for (Map.Entry<String,HashSet<String>> entry: inmap.entrySet()) {
	    if (entry.getValue().size() == 0) {
		terminals.add(entry.getKey());
	    }
	}
	while (terminals.size() > 0) {
	    String n = terminals.poll();
	    HashSet<String> outset = outmap.get(n);
	    for (String key: new ArrayList<String>(outset)) {
		outset.remove(key);
		HashSet<String> inset = inmap.get(key);
		inset.remove(n);
		if (inset.isEmpty()) {
		    terminals.add(key);
		}
	    }
	}
	for (Map.Entry<String,HashSet<String>> entry: inmap.entrySet()) {
	    HashSet<String> upstream = entry.getValue();
	    if (upstream.size() > 0) {
		String end = entry.getKey();
		results.add(end);
		String next = chooseKey(upstream);
		while (next != null) {
		    results.add(next);
		    if (next.equals(end)) break;
		    upstream = inmap.get(next);
		    next = chooseKey(upstream);
		}
	    }
	}
	return results;
    }

    private static Set<String> checkLoops(JTable table) {
	// do a topological sort using Kahn's algorithm
	TableModel model = table.getModel();
	int rowCount = model.getRowCount();
	HashMap<String,HashSet<String>> inmap =
	    new HashMap<String,HashSet<String>>(64);
	HashMap<String,HashSet<String>> outmap =
	    new HashMap<String,HashSet<String>>(64);
	Set<String> results = new LinkedHashSet<String>(64);
	LinkedList<String> terminals = new LinkedList<String>();
	Properties props = new Properties(64);

	for (int i = 0; i < rowCount; i++) {
	    String key = ((String)model.getValueAt(i, 0)).trim();
	    if (key.length() == 0) continue;
	    if (key.startsWith("-")) continue;
	    props.setProperty(key, (String)model.getValueAt(i, 1));
	    inmap.put(key, new HashSet<String>());
	    outmap.put(key, new HashSet<String>());
	}
	for (int i = 0; i < rowCount; i++) {
	    String key = ((String)model.getValueAt(i, 0)).trim();
	    if (key.length() == 0) continue;
	    if (key.startsWith("-")) continue;
	    HashSet<String>inmapLinks = inmap.get(key);
	    String value = props.getProperty(key);
	    if (value == null) continue;
	    Matcher matcher = pattern.matcher(value);
	    int index = 0;
	    while (matcher.find(index)) {
		int start = matcher.start();
		int end = matcher.end();
		String pkey = value.substring(start+2, end-1);
		String pval = props.getProperty(pkey);
		if (pval != null) {
		    Set<String> outmapLinks = outmap.get(pkey);
		    inmapLinks.add(pkey);
		    outmapLinks.add(key);
		}
		index = end;
	    }
	}
	for (Map.Entry<String,HashSet<String>> entry: inmap.entrySet()) {
	    if (entry.getValue().size() == 0) {
		terminals.add(entry.getKey());
	    }
	}
	while (terminals.size() > 0) {
	    String n = terminals.poll();
	    HashSet<String> outset = outmap.get(n);
	    for (String key: new ArrayList<String>(outset)) {
		outset.remove(key);
		HashSet<String> inset = inmap.get(key);
		inset.remove(n);
		if (inset.isEmpty()) {
		    terminals.add(key);
		}
	    }
	}
	for (Map.Entry<String,HashSet<String>> entry: inmap.entrySet()) {
	    HashSet<String> upstream = entry.getValue();
	    if (upstream.size() > 0) {
		String end = entry.getKey();
		results.add(end);
		String next = chooseKey(upstream);
		while (next != null) {
		    results.add(next);
		    if (next.equals(end)) break;
		    upstream = inmap.get(next);
		    next = chooseKey(upstream);
		}
	    }
	}
	return results;
    }

    private static class CRLFWriter extends FilterWriter {
	boolean prepend = false;
	boolean append = true;
	public CRLFWriter(Writer w) {
	    super(w);
	    String eol = System.lineSeparator();
	    if (eol.equals("\n")) {
		prepend = true;
	    } else if (eol.equals("\r")) {
		append = true;
	    }
	}
	@Override
	public void write(int c) throws IOException {
	    if (c == '\n' && prepend) {
		out.write('\r');
		out.write(c);
	    } else if (c == '\r' && append) {
		out.write(c);
		out.write('\r');
	    } else {
		out.write(c);
	    }
	}
	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
	    int limit = off + len;
	    for (int i = off; i < limit; i++) {
		write(cbuf[i]);
	    }
	}
	public void write(String str, int off, int len) throws IOException {
	    int limit = off + len;
	    for (int i = off; i < limit; i++) {
		write(str.charAt(i));
	    }
	}
    }

    private void save(File f, InputTablePane table) throws IOException {
	FileOutputStream os = new FileOutputStream(f);
	Writer w = new OutputStreamWriter(os, UTF8);
	w = new CRLFWriter(w);
	Properties props = new Properties();
	// PrintWriter out = new PrintWriter(w);
	// out.print("#(!M.T " + mediaType() + ")\r\n");
	// TableModel model = table.getModel();
	// int len = model.getRowCount();
	int len = table.getRowCount();
	for (int i = 0; i < len; i++) {
	    String key = (String)table.getValueAt(i, 0);
	    if (key == null) continue;
	    key = key.trim();
	    if (key.length() == 0) continue;
	    if (key.startsWith("-")) continue;
	    String value = (String)table.getValueAt(i, 1);
	    if (value == null) continue;
	    if (key.startsWith("base64.")) {
		if (value == null || value.length() == 0) continue;
		value = encode(value);
	    }
	    value = value.trim();
	    if (value.length() == 0) continue;
	    props.setProperty(key, value);
	}
	props.store(w, "(!M.T " + mediaType() + ")");
	w.flush();
	w.close();
    }

    private void save(Properties props, InputTablePane table) {
	// System.out.println("saving properties");
	props.clear();
	// TableModel model = table.getModel();
	// int len = model.getRowCount();
	int len = table.getRowCount();
	for (int i = 0; i < len; i++) {
	    String key = (String)table.getValueAt(i, 0);
	    if (key == null) continue;
	    //System.out.println("found key " + key);
	    key = key.trim();
	    if (key.length() == 0) continue;
	    if (key.startsWith("-")) continue;
	    String value = (String)table.getValueAt(i, 1);
	    if (value == null) continue;
	    if (key.startsWith("base64.")) {
		if (value == null || value.length() == 0) continue;
		value = encode(value);
	    }
	    value = value.trim();
	    if (value.length() == 0) continue;
	    props.setProperty(key, value);
	    // System.out.println("saving " + key + " as " + value);
	}
	// System.out.println(".... properties saved");
    }


    final Set<String> reserved = new LinkedHashSet<String>(32);
    int[] spacers  = null;

    int ind = 0;
    ArrayList<Integer> sl = new ArrayList<>(4);

    /**
     * Add a set of reserved keys.
     */
    protected void addReservedKeys(String... keys) {
	if (ind < 0) {
	    throw new IllegalStateException(errorMsg("indexWrap"));
	}
	Collections.addAll(reserved, keys);
	sl.add(reserved.size() + (ind++));
    }

    private int KLEN = 0;

    /**
     * Indicate that all reserved keywords have been added.
     * If called multiple times, only the first call will have
     * any effect.
     * <P>
     * This should be called in a constructor.
     */
    protected void setupCompleted() {
	if (ind == -1) return;
	ind = -1;
	int i = 0;
	spacers = new int[sl.size()];
	for (Integer val: sl) {
	    spacers[i++] = val;
	}
	KLEN = reserved.size() + spacers.length;
    }

    /**
     * The window-type mode for this editor. This determines if
     * the editor's top-level frame is a JFrame or a JDialog, and
     * for dialogs, whether it is modeless or not.
     */
    public static enum Mode {
	/**
	 * The top-level window is a JFRAME.
	 */
	JFRAME,
	/**
	 * The top-level window is a JDialog and the dialog is
	 * a modal dialog.
	 */
	MODAL,
	/**
	 * The top-level window is a JDialog and the dialog is
	 * a modeless dialog.
	 */
	MODELESS
    }

    /**
     * Load a file, chosen using a dialog, to set up this editor.
     * This will be called before the editor's top-level window is
     * created.
     * @param owner the component on which any file-chooser dialog should
     *        be centered; null if there is none
     */
    public void showLoadDialog(Component owner)
	throws IOException
    {
	selectedIndex = -1;
	File f = null;
	try {
	    SwingUtilities.invokeAndWait(() -> {
		    try {
			File cdir = new File(System.getProperty("user.dir"))
			    .getCanonicalFile();
			JFileChooser chooser = new JFileChooser(cdir);
			FileNameExtensionFilter filter =
			    new FileNameExtensionFilter
			    (extensionFilterTitle(), extension());
			chooser.setFileFilter(filter);
			chooser.setSelectedFile(file);
			int status = chooser.showOpenDialog(owner);
			if (status == JFileChooser.APPROVE_OPTION) {
			    file = chooser.getSelectedFile();
			}
		    } catch (IOException e) {
		    }
		});
	} catch (InterruptedException e) {
	} catch (InvocationTargetException e) {
	}
	if (f != null) {
	    loadFile(f);
	}
    }
    /**
     * Load a file to set up this editor.
     * This will be called before the editor's top-level window is
     * created.
     * @param f the file to open.
     * @exception IOException an IO exception occurred or f was null
     */
    public void loadFile(File f)
	throws IOException
    {
	if (f == null) {
	    throw new IOException("noFileSpecified");
	}
	file = f;
	Reader r = new InputStreamReader(new FileInputStream(file),
					 UTF8);
	String comment = "#(!M.T " + mediaType() + ")\r\n";
	char[] cbuf1 = comment.toCharArray();
	char[] cbuf2 = new char[cbuf1.length];
	int n = r.read(cbuf2);
	if (n != cbuf2.length) {
	    throw new IOException(errorMsg("wrongMediaType", f.toString()));
	}
	for  (int i = 0; i < n; i++) {
	    if (cbuf1[i] != cbuf2[i]) {
		throw new IOException(errorMsg("wrongMediaType", f.toString()));
	    }
	}
	if (properties == null) {
	    properties = new Properties();
	} else {
	    properties.clear();
	}
	properties.load(r);
	r.close();
	for (String key: defaults.stringPropertyNames()) {
	    if (properties.getProperty(key) == null) {
		properties.setProperty(key, defaults.getProperty(key));
	    }
	}
    }

    private static final Color DEFAULT_NOEDIT_COLOR =
	new Color (255, 255, 225);

    private static class OurCellRenderer1 extends DefaultTableCellRenderer {
	public OurCellRenderer1() {super();}

	@Override
	public Component getTableCellRendererComponent
	    (JTable table, Object value, boolean isSelected, boolean hasFocus,
	     int row, int column)
	{
	    Component result = super.getTableCellRendererComponent
		(table, value, isSelected, hasFocus, row, column);
	    if (isSelected == false && hasFocus == false) {
		if (!table.isCellEditable(row,column)) {
		    result.setBackground(DEFAULT_NOEDIT_COLOR);
		} else {
		    result.setBackground(Color.WHITE);
		}
	    }
	    return result;
	}
    }
    private static class OurCellRenderer2 extends DefaultTableCellRenderer {
	public OurCellRenderer2() {super();}
	@Override
	public Component getTableCellRendererComponent
	    (JTable table, Object value, boolean isSelected, boolean hasFocus,
	     int row, int column)
	{
	    Component result = super.getTableCellRendererComponent
		(table, value, isSelected, hasFocus, row, column);
	    if (column == 1) {
		String key = (String)table.getValueAt(row, 0);
		if (key != null && key.startsWith("ebase64.")) {
		    result.setForeground(Color.GREEN.darker().darker());
		} else {
		    result.setForeground(Color.BLACK);
		}
	    } else {
		result.setForeground(Color.BLACK);
	    }
	    return result;
	}
    }

    private static class TaggedTextField extends JTextField {
	JTable table;
	int tag = 0;
	String oldvalue;
	public TaggedTextField() {super();}
    }

    private static final Pattern keyPattern =
	Pattern.compile("[a-zA-Z][a-zA-Z0-9_]*([.][a-zA-Z][a-zA-Z0-9_]*)*");

    private static boolean badKey(String key) {
	Matcher matcher = keyPattern.matcher(key);
	return matcher.matches() == false;
    }


    private class OurCellEditor1 extends DefaultCellEditor {
	HashSet<String> keys = null;

 	public OurCellEditor1(HashSet<String> keys) {
	    super(new TaggedTextField());
	    this.keys = keys;

	}
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
						     boolean isSelected,
						     int row, int column)
	{
	    String oldvalue = (String)value;
	    TaggedTextField tf = (TaggedTextField)
		super.getTableCellEditorComponent(table, value, isSelected,
						  row, column);
	    tf.table = table;
	    tf.oldvalue = oldvalue;
	    return tf;
	}

	@Override
	public Object getCellEditorValue() {
	    TaggedTextField tf = (TaggedTextField) getComponent();
	    String key = tf.getText();
	    if (key == null) return null;
	    key = key.trim();
	    return key;
	}
	@Override
	public boolean stopCellEditing() {
	    TaggedTextField tf = (TaggedTextField) getComponent();
	    String key = tf.getText();
	    if (key == null) {
		return super.stopCellEditing();
	    }
	    key = key.trim();
	    if (key.length() == 0) {
		tf.setText(key);
		return super.stopCellEditing();
	    }
	    if (key.equals(tf.oldvalue)) {
		tf.setText(key);
		return super.stopCellEditing();
	    }
	    if (key.startsWith("-")) {
		tf.setText(key);
		return super.stopCellEditing();
	    }
	    String shortkey = key;
	    if (key.startsWith(B64KEY_START)) {
		shortkey = key.substring(B64KEY_START_LEN);
	    } else if (key.startsWith(EB64KEY_START)) {
		shortkey = key.substring(EB64KEY_START_LEN);
	    }

	    while (keys.contains(shortkey) || badKey(key)) {
		key = JOptionPane.showInputDialog(tf.table,
						  "Key for this row "
						  + "(null to revert):",
						  "Key Request",
						  JOptionPane.PLAIN_MESSAGE);
		if (key != null) key = key.trim();
		if (key == null || key.length() == 0) {
		    key = tf.oldvalue;
		    tf.setText(key);
		    return super.stopCellEditing();
		}
		shortkey = key;
		if (key.startsWith(B64KEY_START)) {
		    shortkey = key.substring(B64KEY_START_LEN);
		} else if (key.startsWith(EB64KEY_START)) {
		    shortkey = key.substring(EB64KEY_START_LEN);
		}
	    }
	    keys.add(shortkey);
	    tf.setText(key);
	    return super.stopCellEditing();
	}
    }

    private class OurCellEditor2 extends DefaultCellEditor {
	HashSet<String> keys = null;
	public OurCellEditor2(HashSet<String>keys) {
	    super(new TaggedTextField());
	    this.keys = keys;

	}
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
						     boolean isSelected,
						     int row, int column)
	{
	    int tag = 0;
	    String oldvalue = (String)value;
	    // column is always 1
	    String key = (String) table.getValueAt(row, 0);
	    while (key == null) {
		key = JOptionPane.showInputDialog(table,
						  "Key for this row:",
						  "Key Request",
						  JOptionPane.PLAIN_MESSAGE);
		if (key != null) {
		    if (badKey(key)) {
			key = null;
			continue;
		    }
		    String shortkey = key;
		    if (key.startsWith(B64KEY_START)) {
			shortkey = key.substring(B64KEY_START_LEN);
		    } else if (key.startsWith(EB64KEY_START)) {
			shortkey = key.substring(EB64KEY_START_LEN);
		    }
		    if (keys.contains(shortkey)) {
			String msg = errorMsg("keyInUse", key); 
			JOptionPane
			    .showMessageDialog(table, msg, errorTitle(),
					       JOptionPane.ERROR_MESSAGE);
			key = null;
			continue;
		    }
		    keys.add(shortkey);
		    table.setValueAt(key, row, 0);
		} else {
		    cancelCellEditing();
		    return null;
		}
	    }
	    if (key.startsWith("base64.")) {
		tag = 1;
		// value = decode(oldvalue);
	    } else if (key.startsWith("ebase64.")) {
		tag = 2;
		value = decrypt(oldvalue);
	    }

	    TaggedTextField tf = (TaggedTextField)
		super.getTableCellEditorComponent(table, value, isSelected,
						  row, column);
	    tf.tag = tag;
	    tf.table = table;
	    tf.oldvalue = oldvalue;
	    return tf;
	}
	@Override
	public Object getCellEditorValue() {
	    TaggedTextField tf = (TaggedTextField) getComponent();
	    String value = tf.getText();
	    switch (tf.tag) {
	    default:
		return value;
	    case 1:
		return value;
	    case 2:
		String[] recipients = getRecipients(tf.table);
		if (recipients == null  || recipients.length == 0) {
		    return tf.oldvalue;
		}
		return encrypt(value, recipients);
	    }
	}
    }

    private static class CallableContainer {
	public CallableContainer(Callable callable) {
	    this.callable = callable;
	}
	Callable callable;
    }

    private void doEdit(Component owner,  Mode mode, Callable continuation,
			boolean quitCloseMode)
    {
	final CallableContainer continuationContainer =
	    new CallableContainer(continuation);
	//System.out.println("mode = " + mode);
	Window window = (mode == Mode.JFRAME)?
	    new JFrame(configTitle()):
	    new JDialog(((owner == null)? null:
			 SwingUtilities.getWindowAncestor(owner)),
			configTitle(),
			((mode == Mode.MODAL)?
			 Dialog.ModalityType.APPLICATION_MODAL:
			 Dialog.ModalityType.MODELESS));

	Set<String> names = new HashSet<>(64);
	if (properties == null) {
	    properties = new Properties();
	}
	names.addAll(properties.stringPropertyNames());
	names.addAll(reserved);

	String[] keys1 = new String[names.size()];
	keys1 = names.toArray(keys1);
	for (int i = 0; i < keys1.length; i++) {
	    String key = keys1[i];
	    if (reserved.contains(key)) {
		keys1[i] = "";
	    }
	}
	String[] keys = new String[keys1.length + spacers.length]; 
	System.arraycopy(keys1, 0, keys, 0, keys1.length);
	for (int i = 0; i < spacers.length; i++) {
	    keys[keys1.length+i] = "";
	}
	Arrays.sort(keys);
	int kind = 0;
	int sind = 0;
	for (int i = 0; i < spacers.length; i++) {
	    System.out.print(" " + spacers[i]);
	}
	System.out.println();
	for (String key: reserved) {
	    keys[kind++] = key;
	    if (kind == spacers[sind]) {
		keys[kind++] = "-------------";
		sind++;
	    }
	}
	for (int i = 0; i < keys.length; i++) {
	    System.out.println(keys[i]);
	}



	Vector<Vector<Object>> data = new Vector<Vector<Object>>(keys.length);
	for (int i = 0; i < keys.length; i++) {
	    String key = keys[i];
	    String value = "";
	    if (!key.startsWith("-")) {
		value = properties.getProperty(key);
		if (value == null || value.length() == 0) {
		    String defaultValue = defaults.getProperty(key);
		    if (defaultValue != null) {
			value = defaultValue;
		    }
		}
		if (key.startsWith("base64.")) {
		    value = decode(value);
		}
	    }
	    Vector<Object>row = new Vector<>(2);
	    /*
	      System.out.println("adding row with key=" + key
	      + ", value = " + value);
	    */
	    row.add(key);
	    row.add(value);
	    // tm.addRow(row);
	    data.add(row);
	}
	int len = data.size();
	HashSet<String> editorKeys = new HashSet<>(2*len + 64);
	for (int i = 0; i < len; i++) {
	    String key = (String)(data.get(i).get(0));
	    if (key == null) continue;
	    key = key.trim();
	    if (key.length() == 0) continue;
	    if (key.startsWith("-")) continue;
	    if (key.startsWith(B64KEY_START)) {
		key = key.substring(B64KEY_START_LEN);
	    } else if (key.startsWith(EB64KEY_START)) {
		key = key.substring(EB64KEY_START_LEN);
	    }
	    editorKeys.add(key);
	}

	InputTablePane.ColSpec[] colspec = {
	    new InputTablePane.ColSpec(localeString("Property"),
				       "mmmmmmmmmmmmmmm",
				       String.class,
				       new OurCellRenderer1(),
				       new OurCellEditor1(editorKeys)),
	    new InputTablePane.ColSpec(localeString("Value"),
				       "mmmmmmmmmmmmmmm",
				       String.class,
				       new OurCellRenderer2(),
				       new OurCellEditor2(editorKeys))
	};

	InputTablePane ipane = new InputTablePane(colspec, data.size() + 10,
						  data, true, true, true) {
		@Override
		protected boolean prohibitEditing(int row, int col) {
		    return col == 0 && row < KLEN;
		}
		@Override
		protected int minimumSelectableRow(int col, boolean all) {
		    if (all) {
			return KLEN;
		    } else {
			return 0;
		    }
		}
	    };

       
	JPanel panel = new JPanel();
	panel.setLayout(new BorderLayout());
	panel.add(ipane, BorderLayout.CENTER);
	JMenuBar menubar = new JMenuBar();
	JMenu fileMenu = new JMenu(localeString("File"));
	fileMenu.setMnemonic(KeyEvent.VK_F);
	menubar.add(fileMenu);
	String qctitle = quitCloseMode?
	    localeString("Quit"): localeString("Close");
	JMenuItem menuItem =
	    new JMenuItem(localeString(qctitle),
			  quitCloseMode? KeyEvent.VK_Q: KeyEvent.VK_W);
	if (quitCloseMode) {
	    menuItem.setAccelerator(KeyStroke.getKeyStroke
				    (KeyEvent.VK_Q,
				     InputEvent.CTRL_DOWN_MASK));
	} else {
	    menuItem.setAccelerator(KeyStroke.getKeyStroke
				    (KeyEvent.VK_W,
				     InputEvent.CTRL_DOWN_MASK));
	}
	menuItem.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    ipane.stopCellEditing();
		    if (needSave) {
			String emsg = errorMsg("saveQuestion");
			switch (JOptionPane.showConfirmDialog
				(window, emsg, configTitle(),
				 JOptionPane.OK_CANCEL_OPTION,
				 JOptionPane.QUESTION_MESSAGE)) {
			case JOptionPane.OK_OPTION:
				window.setVisible(false);
				window.dispose();
				if (quitCloseMode) {
				    System.exit(0);
				}
			default:
			    return;
			}
		    } else {
			window.setVisible(false);
			window.dispose();
			if (quitCloseMode) {
			    System.exit(0);
			}
		    }
		}
	    });
	fileMenu.add(menuItem);
	menuItem = new JMenuItem(localeString("Save"), KeyEvent.VK_S);
	menuItem.setAccelerator(KeyStroke.getKeyStroke
				(KeyEvent.VK_S,
				 InputEvent.CTRL_DOWN_MASK));
	menuItem.addActionListener(new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
		    ipane.stopCellEditing();
		    if (doSave(window, file, false, /*table*/ipane)) {
			needSave = false;
		    }
		}
	    });
	fileMenu.add(menuItem);
	menuItem = new JMenuItem(localeString("SaveAs"), KeyEvent.VK_A);
	menuItem.setAccelerator(KeyStroke.getKeyStroke
				(KeyEvent.VK_S,
				 (InputEvent.CTRL_DOWN_MASK
				  | InputEvent.SHIFT_DOWN_MASK)));
	menuItem.addActionListener(new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
		    ipane.stopCellEditing();
		    if (doSave(window, file, true, /*table*/ipane)) {
			needSave = false;
		    }
		}
	    });
	fileMenu.add(menuItem);

	window.setIconImages(iconList);
	window.setLayout(new BorderLayout());
	window.setPreferredSize(new Dimension(800, 600));
	if (window instanceof JFrame) {
	    JFrame frame = (JFrame) window;
	    frame.setJMenuBar(menubar);
	    frame.addWindowListener(new WindowAdapter() {
		    public void windowClosing(WindowEvent e) {
			ipane.stopCellEditing();
			save(properties, /*table*/ipane);
			if (continuationContainer.callable!= null) {
			    Callable callable = continuationContainer.callable;
			    continuationContainer.callable = null;
			    callable.call();
			}
			window.dispose();
		    }
		    public void windowClosed() {
			if (continuationContainer.callable != null) {
			    continuationContainer.callable.call();
			}
		    }
		});
	} else {
	    JDialog dialog = (JDialog)window;
	    dialog.setJMenuBar(menubar);
	    if (mode != Mode.MODAL) {
		dialog.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
			    ipane.stopCellEditing();
			    save(properties, /*table*/ipane);
			    if (continuationContainer.callable != null) {
				Callable callable =
				    continuationContainer.callable;
				continuationContainer.callable = null;
				callable.call();
			    }
			}
			public void windowClosed() {
			    if (continuationContainer.callable != null) {
				continuationContainer.callable.call();
			    }
			}
		    });
	    }
	}
	
	window.add(panel, BorderLayout.CENTER);
	window.pack();
	window.setVisible(true);
	if (mode == Mode.MODAL) {
	    ipane.stopCellEditing();
	    save(properties, /*table*/ipane);
	    if (continuationContainer.callable != null) {
		continuationContainer.callable.call();
	    }
	}
    }

    /**
     * Start the editor.

     * This will cause a window to appear that will allow configuration
     * parameters to be edited. Depending on the mode argument, the
     * window may be a JFrame or a JDialog, and for a dialog, modal or
     * modeless. One should call {@link #loadFile( File)} if
     * the parameters should be loaded from a known file or
     * {@link #showLoadDialog(Component)} if a dialog box should be
     * used to select a file. Otherwise default values may be provided
     * for some of the parameters.
     * @param owner the component on which the editor should be centered;
     *        null if there is none
     * @param mode the window mode ({@link Mode#JFRAME}, {@link Mode#MODAL},
     *        or {@link Mode#MODELESS})
     * @param continuation a {@link Callable} that provides some code to
     *        run while or just after this editor's top-level window is
     *        closing.
     * @param quitCloseMode true if, when this editor's window is
     *        closing and is not a modal dialog, the application
     *        should quit (exit with an exit code of 0); false if,
     *        when this editor's window is closing, the application
     *        will continue running.
     */
    public void edit(Component owner,  Mode mode, Callable continuation,
			boolean quitCloseMode)
    {
	selectedIndex = -1;
	if (mode == null) throw new
			      IllegalArgumentException(errorMsg("noMode"));
	if (mode == Mode.MODAL) {
	    try {
		// System.out.println("invokeAndWait");
		SwingUtilities.invokeAndWait(() -> {
			doEdit(owner, mode, continuation, false);
			// System.out.println("doEdit exited");
		    });
	    } catch (InterruptedException e) {
		e.printStackTrace();
	    } catch (InvocationTargetException et) {
		et.printStackTrace();
	    } catch (Exception ee) {
		ee.printStackTrace();
	    }
	} else {
	    SwingUtilities.invokeLater(() -> {
		    doEdit(owner, mode, continuation, quitCloseMode);
		});
	}
    }

    private static Set<String> getKeySet(Properties dbProperties)
	throws IllegalStateException
    {
	// do a topological sort using Kahn's algorithm
	HashMap<String,HashSet<String>> inmap =
	    new HashMap<String,HashSet<String>>(64);
	HashMap<String,HashSet<String>> outmap =
	    new HashMap<String,HashSet<String>>(64);
	Set<String> results = new LinkedHashSet<String>(64);
	LinkedList<String> terminals = new LinkedList<String>();
	for (String key: dbProperties.stringPropertyNames()) {
	    inmap.put(key, new HashSet<String>());
	    outmap.put(key, new HashSet<String>());
	}
	for (String key: dbProperties.stringPropertyNames()) {
	    HashSet<String>inmapLinks = inmap.get(key);
	    String value = dbProperties.getProperty(key);
	    if (value == null) continue;
	    Matcher matcher = pattern.matcher(value);
	    int index = 0;
	    while (matcher.find(index)) {
		int start = matcher.start();
		int end = matcher.end();
		String pkey = value.substring(start+2, end-1);
		String pval = dbProperties.getProperty(pkey);
		if (pval != null) {
		    Set<String> outmapLinks = outmap.get(pkey);
		    inmapLinks.add(pkey);
		    outmapLinks.add(key);
		}
		index = end;
	    }
	}
	for (Map.Entry<String,HashSet<String>> entry: inmap.entrySet()) {
	    if (entry.getValue().size() == 0) {
		terminals.add(entry.getKey());
	    }
	}
	while (terminals.size() > 0) {
	    String n = terminals.poll();
	    results.add(n);
	    HashSet<String> outset = outmap.get(n);
	    for (String key: new ArrayList<String>(outset)) {
		outset.remove(key);
		HashSet<String> inset = inmap.get(key);
		inset.remove(n);
		if (inset.isEmpty()) {
		    terminals.add(key);
		}
	    }
	}
	for (Map.Entry<String,HashSet<String>> entry: inmap.entrySet()) {
	    if (entry.getValue().size() > 0) {
		throw new IllegalStateException(localeString("circular"));
	    }
	}
	return results;
    }

    private static final String B64KEY_START = "base64.";
    private static final int B64KEY_START_LEN = B64KEY_START.length();

    private static final String EB64KEY_START = "ebase64.";
    private static final int EB64KEY_START_LEN = EB64KEY_START.length();

    /**
     * Get the encoded properties.
     * Keys starting with 'base64.' will have their value Base-64 encoded and
     * keys starting with 'ebase64.' will have their valued encrypted with GPG
     * and then Base-64 encoded.
     */
    public Properties getEncodedProperties() {
	Properties results = new Properties();
	results.putAll(properties);
	return results;
    }

    /**
     * Get the decoded properties.

     * Base-64 encryption will be removed for unencrypted properties
     * and all string substitution will be performed before the
     * results are returned. For keys starting with the component
     * "base64", the first token and its following delimiter
     * ("base64.") will be stripped from the key. Encrypted data is
     * handled so as to allow the unencrypted data to be kept for as
     * short a time as possible. For other keys, the value for a key
     * will have parameter references replaced with the corresponding
     * values.
     * <P>
     * The {@link java.util.Properties} object returned by this method
     * has been customized: for keys starting with "ebase64.", the
     * method {@link Properties#getProperty(String)} will return the
     * encrypted value, whereas the method
     * {@link Properties#get(Object)} will return an {@link Object}
     * that is actually an array of characters and that contains the
     * decrypted data. For these keys, a new array will be returned
     * each time {@link Properties#get(Object)} is called. This is
     * somewhat atypical &emdash; normally
     * {@link Properties#getProperty(String)} and
     * {@link Properties#get(Object)} return the same object but with
     * a different type. The rationale is that encrypted data is
     * typically sensitive and should be removed as soon as it is no
     * longer needed.  You can overwrite a character array, but you
     * cannot overwrite a string, which will persist until reclaimed
     * by the garbage collector.
     * @return the properties
     */
    public Properties getDecodedProperties() {
	Properties results = new Properties() {
		public Object get(Object k) {
		    if (k instanceof String) {
			String key = (String) k;
			if (key.startsWith("ebase64.")) {
			    String encrypted = getProperty(key);
			    byte[] data = Base64.getDecoder().decode(encrypted);
			    ByteArrayInputStream is =
				new ByteArrayInputStream(data);
			    ProcessBuilder pb = new ProcessBuilder("gpg", "-d");
			    try {
				Process process = pb.start();
				Thread ot = new Thread(() -> {
					try {
					    OutputStream os =
						process.getOutputStream();
					    is.transferTo(os);
					    // don't have to close a
					    // ByteArrayInputStream
					    os.close();
					} catch (IOException eio) {
					    System.err
						.println(eio.getMessage());
					}
				});
				ot.start();
				CharArrayWriter w = new
				    CharArrayWriter(encrypted.length()) {
					public char[] toCharArray() {
					    char[] result = super.toCharArray();
					    if (result != this.buf) {
						// want to keep sensitive info
						// for as little as possible
						Arrays.fill(buf, '\0');
					    }
					    return result;
					}
				    };
				Reader r = new InputStreamReader
				    (process.getInputStream(), UTF8);
				r.transferTo(w);
				return w.toCharArray();
			    } catch (IOException e) {
				throw new IllegalStateException
				    (e.getMessage(), e);
			    }
			} else {
			    return super.get(k);
			}
		    } else {
			return super.get(k);
		    }
		}
	    };
	results.putAll(properties);
	/*
	System.out.println("properties.size() = " + properties.size());
	System.out.println("results.size() = " + results.size());
	*/
	for (String key: getKeySet(results)) {
	    // System.out.println("checking key " + key);
	    String value = results.getProperty(key).trim();
	    if (value == null || value.length() == 0) continue;
	    if (key.startsWith(B64KEY_START)) {
		byte[] data = Base64.getDecoder().decode(value);
		ByteArrayInputStream is = new ByteArrayInputStream(data);
		StringBuilder sb = new StringBuilder();
		try {
		    CopyUtilities.copyStream(is, sb, UTF8);
		} catch (IOException eio) {}
		results.remove(key);
		key = key.substring(B64KEY_START_LEN);
		results.setProperty(key, sb.toString());
	    }  else if (key.startsWith(EB64KEY_START)) {
		// nothing to do for this case as we decrypt when
		// the user calls 'get(key)'
	    } else {
		Matcher matcher = pattern.matcher(value);
		int index = 0;
		StringBuilder sb = new StringBuilder();
		while (matcher.find(index)) {
		    int start = matcher.start();
		    int end = matcher.end();
		    String pkey =value.substring(start+2, end-1);
		    sb.append(value.substring(index, start));
		    String pval = results.getProperty(pkey);
		    if (pval == null) pval = System.getProperty(pkey);
		    if (pval != null) {
			sb.append(pval);
		    }
		    index = end;
		}
		if (index > 0) {
		    sb.append(value.substring(index));
		    value = sb.toString();
		    results.setProperty(key, value);
		}
	    }
	    // System.out.println("... processed");
	}
	return results;
    }
}

//  LocalWords:  exbundle BLOCKQUOTE PRE MEDIATYPE BLOCkQUOTE ebase
//  LocalWords:  ConfigPropertyEditor GPG decrypt addIcon iconified
//  LocalWords:  setDefaultProperty setupCompleted errorTitle utf gpg
//  LocalWords:  configTitle mediaType extensionFileTitle clasz dir
//  LocalWords:  gpgFailed nextGPG propertiesLoop cnfgtmp saveToMsg
//  LocalWords:  zA Kahn's PrintWriter ArrayList sl addAll dbName len
//  LocalWords:  dbPath configAuth dbOwner configRoles KLEN menubar
//  LocalWords:  AddRow EditSelRow DelSelRow keyInUse NewKey NewValue
//  LocalWords:  AcceptNewRow AcceptValue mmmmmmmmmmmmmmmmmm SaveAs
//  LocalWords:  mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm saveQuestion tm
//  LocalWords:  ByteArrayInputStream whitespace extensionFilterTitle
//  LocalWords:  TableModel getModel getRowCount JFrame JDialog TTY
//  LocalWords:  dialogs modeless oldvalue addRow mmmmmmmmmmmmmmm tty
//  LocalWords:  loadFile MODELSS quitCloseMode noMode invokeAndWait
//  LocalWords:  doEdit showLoadDialog decrypted substring bashrc EB
//  LocalWords:  config getDecodedProperties addMoreKeys IOException
//  LocalWords:  noFileSpecified wrongMediaType unencrypted emdash pb
//  LocalWords:  getProperty rawkey ProcessBuilder ot OutputStream os
//  LocalWords:  getOutputStream transferTo eio getMessage buf sb
//  LocalWords:  CharArrayWriter toCharArray InputStreamReader
//  LocalWords:  getInputStream StringBuilder setProperty toString
//  LocalWords:  println errorMsg
