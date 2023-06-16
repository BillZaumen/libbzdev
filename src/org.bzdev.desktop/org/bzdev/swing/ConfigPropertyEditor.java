package org.bzdev.swing;

import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.Format;
import java.util.ArrayList;
import java.util.*;
import java.util.regex.*;
import java.util.function.Function;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.border.Border;
import javax.swing.filechooser.*;
import javax.swing.table.*;

import org.bzdev.io.AppendableWriter;
import org.bzdev.lang.Callable;
import org.bzdev.lang.UnexpectedExceptionError;
import org.bzdev.util.CopyUtilities;
import org.bzdev.swing.TextCellEditor;

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
 * color than the rest of the table.  To insert a new row above a row, or to
 * move or delete the row, the entire row must be selected. A table
 * can also be configured to suppress some of the buttons located just
 * above the table. Some keys in the table may consist of one or more
 * dashes (minus signs). There are treated as spacers and are ignored
 * when the properties are written or used outside of the editor.
 * <P>
 * Property values may be encrypted using GPG. The keys for these
 * properties start with the substring "ebase64." and the values are
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
 * methods (most are optional):
 * <UL>
 *   <LI><STRONG>{@link addIcon(Image)}</STRONG> or
 *      <STRONG>{@link addIcon(Class,String)}</STRONG>. These methods
 *      provide an icon to display when a configuration editor's window
 *      is iconified.  Normally there are multiple icons, corresponding to
 *      different sizes required by a window manager.
 *   <LI><STRONG>{@link ConfigPropertyEditor#addReservedKeys(String...)}</STRONG>.
 *      There may be some number of distinguished properties that are nominally
 *      expected to be present. This method will define a group of such
 *      keys. When called multiple times, each group will be separated 
 *      from the others by a series of dashes in a table's first column.
 *   <LI><STRONG>{@link ConfigPropertyEditor#addAltReservedKeys(String,String...)}</STRONG>.
 *      This method adds a set of reserved properties that will appear
 *      in a single row. The property names consist of a prefix,
 *      followed by a period, followed by a suffix.  A combo box will allow
 *      one to choose the appropriate property. The values can use
 *      different renderers and editors.
 *   <LI><STRONG>{@link ConfigPropertyEditor#setupCompleted()}</STRONG>.
 *      This method must be called by the constructor, and indicates that
 *      all the reserved keys, and any default values associated with
 *      these, have been provided.
 *   <LI><STRONG>{@link ConfigPropertyEditor#setDefaultProperty(String,String)}</STRONG>.
 *      Some properties have default values, typically in cases where
 *      the defaults are likely to be the ones the user needs. This method
 *      should be used to define what these defaults are.
 *   <LI><STRONG>{@link ConfigPropertyEditor#addRE(String,TableCellRenderer,TableCellEditor)}</STRONG>.
 *      This method associates a table-cell renderer and editor with the
 *      final components of a key (components are separated by periods).
 *      One use is for configuring specialized renderers and editors for
 *      properties that provide colors.
 *   <LI><STRONG>{@link changedPropertyClears(String,String...)}</STRONG>
 *      This method indicates that when one property's value is changed,
 *      or the property is removed, other properties should have their values
 *      set to null.  It is useful in cases where the change of one property
 *      indicates that the value of some other properties is almost certainly
 *      wrong. Generally these will reserved properties and should be listed
 *      in close proximity to each other.
 * </UL>
 * The constructor may optionally call the following methods:
 * <UL>
 * <LI><STRONG>{@link ConfigPropertyEditor#freezeRows()}</STRONG>.
 * This method prevents the user from adding, moving, or deleting rows. The
 * user may, however, change a row's value or key (unless the row is a
 * reserved row).
 * <LI><STRONG>{@link ConfigPropertyEditor#setInitialExtraRows(int)}</STRONG>.
 * This method sets the number of blank table entries that appear after
 * any predefined keys.
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
 * Finally, {@link ConfigPropertyEditor} is not a Swing or AWT
 * component, although it uses such components, and its public methods
 * do not have to be called on the AWT event dispatch thread.  The
 * rationale is that one use case is for providing a dialog box for
 * configuring a program that otherwise runs as a command-line program
 * and exits when done.
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

    private static class  StringBuilderHolder {
	StringBuilder sb = new StringBuilder();
    }

    private String decrypt(String value) {
	if (value == null || password == null) return EMPTY_STRING;
	byte[] data = Base64.getDecoder().decode(value);
	ByteArrayInputStream is = new ByteArrayInputStream(data);

	try {
	    /*
	    File tmpf = File.createTempFile("configPropEditor", "gpg");
	    tmpf.deleteOnExit();
	    FileOutputStream fos = new FileOutputStream(tmpf);
	    is.transferTo(fos);
	    is.close();
	    fos.close();
	    */

	    // Need to use --batch, etc. because when this runs in
	    // a dialog box, we don't have access to a terminal and
	    // GPG agent won't ask for a passphrase.
	    ProcessBuilder pb = new ProcessBuilder("gpg",
						   "--pinentry-mode",
						   "loopback",
						   "--passphrase-fd", "0",
						   "--batch", "-d"/*,
						    tmpf.getCanonicalPath()*/);
	    // pb.redirectError(ProcessBuilder.Redirect.DISCARD);
	    StringBuilderHolder sbh = new StringBuilderHolder();
	    Process p = pb.start();
	    Thread thread1 = new Thread(()->{
		    try {
			OutputStream os = p.getOutputStream();
			OutputStreamWriter w = new OutputStreamWriter(os);
			w.write(password, 0, password.length);
			w.write(System.getProperty("line.separator"));
			w.flush();
			is.transferTo(os);
			w.close();
			os.close();
		    } catch(Exception e) {
			System.err.println(e.getMessage());
		    }
	    });
	    Thread thread2 = new Thread(()->{
		    try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			InputStream is1 = p.getInputStream();
			is1.transferTo(os);
			sbh.sb.append(os.toString(UTF8));
		    } catch(Exception e) {
			System.err.println(e.getMessage());
		    } /*finally {
			tmpf.delete();
			}*/
	    });
	    thread2.start();
	    thread1.start();
	    thread1.join();
	    thread2.join();
	    StringBuilder sb = sbh.sb;
	    // thread.join();
	    p.waitFor();
	    if (p.exitValue() != 0) {
		System.err.println(errorMsg("gpgFailed", p.exitValue()));
		int status = JOptionPane
		    .showConfirmDialog(pwowner,
				       localeString("pwTryAgain"),
				       localeString("gpgFailedTitle"),
				       JOptionPane.YES_NO_OPTION);
		if (status == JOptionPane.OK_OPTION) {
		    password = null;
		    requestPassphrase(pwowner);
		    return decrypt(value);
		} else {
		    return EMPTY_STRING;
		}
	    }
	    return sb.toString();
	} catch (Exception e) {
	    System.err.println(errorMsg("decryption", e.getMessage()));
	    return null;
	}
    }

    private static final char[] EMPTY_CHAR_ARRAY = new char[0];


    private char[] decryptToCharArray(String value) {
	if (value == null || password == null) return EMPTY_CHAR_ARRAY;
	byte[] data = Base64.getDecoder().decode(value);
	ByteArrayInputStream is = new ByteArrayInputStream(data);

	try {
	    /*
	    File tmpf = File.createTempFile("configPropEditor", "gpg");
	    tmpf.deleteOnExit();
	    FileOutputStream fos = new FileOutputStream(tmpf);
	    is.transferTo(fos);
	    is.close();
	    fos.close();
	    */
	    // Need to use --batch, etc. because when this runs in
	    // a dialog box, we don't have access to a terminal and
	    // GPG agent won't ask for a passphrase.
	    ProcessBuilder pb = new ProcessBuilder("gpg",
						   "--pinentry-mode",
						   "loopback",
						   "--passphrase-fd", "0",
						   "--batch", "-d"/*,
						   tmpf.getCanonicalPath()*/);
	    // pb.redirectError(ProcessBuilder.Redirect.DISCARD);
	    ByteArrayOutputStream baos = new
		ByteArrayOutputStream(data.length);
	    Process p = pb.start();
	    Thread thread1 = new Thread(()->{
		    try {
			OutputStream os = p.getOutputStream();
			OutputStreamWriter w = new OutputStreamWriter(os);
			w.write(password, 0, password.length);
			w.write(System.getProperty("line.separator"));
			w.flush();
			is.transferTo(os);
			w.close();
			os.close();
		    } catch(Exception e) {
			System.err.println(e.getMessage());
		    }
	    });
	    Thread thread2 = new Thread(()->{
		    try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			InputStream is1 = p.getInputStream();
			is1.transferTo(baos);
		    } catch(Exception e) {
			System.err.println(e.getMessage());
		    } /*finally {
			tmpf.delete();
			}*/
	    });
	    thread2.start();
	    thread1.start();
	    thread1.join();
	    thread2.join();
	    // StringBuilder sb = sbh.sb;
	    // thread.join();
	    p.waitFor();
	    if (p.exitValue() != 0) {
		System.err.println(errorMsg("gpgFailed", p.exitValue()));
		return EMPTY_CHAR_ARRAY;
	    }
	    return (UTF8.decode(ByteBuffer.wrap(baos.toByteArray())))
		.array();
	} catch (Exception e) {
	    System.err.println(errorMsg("decryption", e.getMessage()));
	    return null;
	}
    }

    private static String encrypt(String value, String[] recipients) {
	if (value == null || value.length() == 0) return EMPTY_STRING;
	if (recipients.length == 0) return EMPTY_STRING;
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
			try {
			    p.waitFor();
			} catch (Exception ee) {
			    e.printStackTrace();
			}
		    }
	    });
	    thread.start();
	    try {
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
		thread.join();
		if (p.exitValue() != 0) {
		    System.err.println(errorMsg("gpgFailed", p.exitValue()));
		} else {
		    e.printStackTrace();
		}
		return null;
	    }
	} catch (Exception e) {
	    return null;
	}
    }

    // List of recipients used when we always want the same ones.
    String[] recipientsList = null;

    /**
     * Set the list of recipients to use when values are encrypted.
     * The list will be built interactively.
     * A recipient is a string acceptable to the '-r' argument for GPG.
     * @param c the component on which to center dialog boxes
     */
    public void setRecipients(Component c) {
	recipientsList = getRecipients(c);
    }

    /**
     * Set the list of recipients to use when values are encrypted, given
     * an explicit list.
     * A recipient is a string acceptable to the '-r' argument for GPG.
     * @param list the recipient list; null to remove the recipients list
     */
    public void setRecipients(String[] list) {
	recipientsList =  list.clone();
    }

    /**
     * Clear the recipients list.
     * After this is called, and until either
     * {@link #setRecipients(Component)} or {@link #setRecipients(String[])}
     * is called, the user will be asked for recipients each time a value
     * is encrypted.
     */
    public void clearRecipients() {
	recipientsList = null;
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
    boolean useNeedSave = true;


    /**
     * Set whether or not a warning message about needing to save the
     * state of the table should be shown.
     * @param mode true if the user should  be warned to save a
     *        modified table; false otherwise
     */
    public void setSaveQuestion(boolean mode) {
	useNeedSave = mode;
    }

    private static boolean endsWithIgnoreCase(String string, String tail) {
	int index = string.length() - tail.length();
	String stail = string.substring(index);
	return stail.equalsIgnoreCase(tail);
    }

    boolean doSave(Component frame, File file, boolean mode,
		   InputTablePane table) {
	File origFile = file;
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
	    file = origFile;
	    return false;
	}
	this.file = file;
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

    /**
     * Save the configuration in a file.
     * @param f the file
     * @throws IOException if an IO error occurred
     */
    public void save(File f) throws IOException {
	FileOutputStream os = new FileOutputStream(f);
	Writer w = new OutputStreamWriter(os, UTF8);
	w = new CRLFWriter(w);
	if (properties == null) {
	    properties = new Properties();
	}
	properties.store(w, "(!M.T " + mediaType() + ")");
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
     * @param keys the keys
     */
    protected void addReservedKeys(String... keys) {
	if (ind < 0) {
	    throw new IllegalStateException(errorMsg("indexWrap"));
	}
	Collections.addAll(reserved, keys);
	sl.add(reserved.size() + (ind++));
    }

    HashMap<Integer,TableCellEditor> editorMap = new HashMap<>();
    HashMap<String,Integer> keyMap = new HashMap<>();
    HashMap<String,String[]> otherKeys = new HashMap<>();
    HashMap<String,String> reverse = new HashMap<>();

    HashSet<String> altKeys = new HashSet<>(32);

    /**
     * Add a reserved-key entry where the key can be changed to use
     * various suffixes.
     * This will cause a spacer (a string of '-' characters where a
     * key is expected) to be added after the entry, unless the prefix
     * was already entered as a key by a call to
     * {@link #addReservedKeys(String...)} with that key given as
     * the concatination of the prefix, a period, and the first suffix.
     * In this case, the call to {@link #addReservedKeys(String...)}
     * must immediately preceed the call to this method or a sequence
     * of calls to this method.
     * @param prefix the start of a key
     * @param suffixes the possibile suffixes following the prefix
     *        and separated from the prefix by a period.
     * @exception IllegalArgumentException a key is already in use
     */
    protected void addAltReservedKeys(String prefix, String... suffixes)
	throws IllegalArgumentException
    {
	if (ind < 0) {
	    // should never happen - just catches a ridicularly large
	    // table.
	    throw new IllegalStateException(errorMsg("indexWrap"));
	}
	String ourkey = prefix + ((suffixes.length == 0)? "":
				  "." + suffixes[0]);
	boolean isNew = !reserved.contains(ourkey);

	int ourindx;
	if (isNew) {
	    ourindx = reserved.size() + ind;
	    reserved.add(ourkey);
	} else {
	    int index = 0;
	    for (String k: reserved) {
		if (k.equals(ourkey)) {
		    break;
		}
		index++;
	    }
	    ourindx = index + ind - 1;
	}
	final int ourind = ourindx;
	if (suffixes.length > 1) {
	    String[] strings = new String[suffixes.length-1];
	    System.arraycopy(suffixes, 1, strings, 0, strings.length);
	    for (int i = 0; i < strings.length; i++) {
		strings[i] = prefix + "." + strings[i];
	    }
	    otherKeys.put(ourkey, strings);
	    for (int i = 0; i < strings.length; i++) {
		reverse.put(strings[i], ourkey);
	    }
	    Vector<String> vector = new Vector<>(suffixes.length);
	    for (String s: suffixes) {
		String key = prefix + "." + s;
		String shortkey = key;
		if (key.startsWith(B64KEY_START)) {
		    shortkey = key.substring(B64KEY_START_LEN);
		} else if (key.startsWith(EB64KEY_START)) {
		    shortkey = key.substring(EB64KEY_START_LEN);
		}
		if (altKeys.contains(shortkey)) {
		    throw new IllegalArgumentException
			(errorMsg("altKeys", key));
		}
		altKeys.add(shortkey);
		vector.add(key);
		keyMap.put(key, ourind);
	    }
	    SwingUtilities.invokeLater(() -> {
		    editorMap.put(ourind,
				  new DefaultCellEditor
				  (new JComboBox<String>(vector)));
		});
	}
	if (isNew) sl.add(reserved.size() + (ind++));
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

    private void doShowLoadDialog(Component owner) {
	selectedIndex = -1;
	File f = null;
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
	    if (f != null) {
		loadFile(f);
	    }
	} catch (IOException e) {}
    }

    /**
     * Load a file, chosen using a dialog, to set up this editor.
     * This will be called before the editor's top-level window is
     * created.
     * @param owner the component on which any file-chooser dialog should
     *        be centered; null if there is none
     * @exception IOException an IO error occurred
     */
    public void showLoadDialog(Component owner)
	throws IOException
    {
	if (SwingUtilities.isEventDispatchThread()) {
	    doShowLoadDialog(owner);
	} else {
	    try  {
		SwingUtilities.invokeAndWait(() -> {
			doShowLoadDialog(owner);
		    });
	    } catch (InterruptedException e) {
	    } catch (InvocationTargetException et) {
		et.printStackTrace();
	    } catch (Exception ee) {
		ee.printStackTrace();
	    }

	}
    }



    /**
     * Load a file to set up this editor.
     * This will be called before the editor's top-level window is
     * created.
     * <P>
     * Calling this method directly will not result in the user being
     * prompted to save changes if values are edited.
     * @param f the file to open; null if no file should be loaded.
     * @exception IOException an IO exception occurred
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

    private static final Color DEFAULT_NOEDIT_COLOR_DM =
	new Color (64, 64, 0);

    private static Color noEditColor() {
	Color bg = (Color)UIManager.get("Table.background");
	int r = bg.getRed();
	int g = bg.getGreen();
	int b = bg.getBlue();
	// return new Color((2*r +128)/3, (2*g+128)/3, b);
	return new Color(r, g, (3*b)/4);
    }

    private static Color reservedEditColor() {
	Color bg1 = noEditColor();
	Color bg2 = (Color)UIManager.get("Table.background");
	int r1 = bg1.getRed();
	int g1 = bg1.getGreen();
	int b1 = bg1.getBlue();
	int r2 = bg2.getRed();
	int g2 = bg2.getGreen();
	int b2 = bg2.getBlue();
	return new Color((r1+r2)/2, (g1+g2)/2, (b1+b2)/2);
    }

    private static class OurCellRenderer1 extends DefaultTableCellRenderer {
	HashMap<Integer,TableCellEditor> editorMap;
	public OurCellRenderer1(HashMap<Integer,TableCellEditor> editorMap) {
	    super();
	    this.editorMap = editorMap;
	}

	@Override
	public Component getTableCellRendererComponent
	    (JTable table, Object value, boolean isSelected, boolean hasFocus,
	     int row, int column)
	{
	    boolean darkmode = DarkmodeMonitor.getDarkmode();
	    Component result = super.getTableCellRendererComponent
		(table, value, isSelected, hasFocus, row, column);
	    if (isSelected == false && hasFocus == false) {
		if (!table.isCellEditable(row,column)) {
		    result.setBackground(noEditColor());
		} else {
		    if (editorMap.get(row) != null) {
			result.setBackground(reservedEditColor());
		    } else {
			result.setBackground(darkmode?(Color)
					     UIManager.get("Table.background"):
					     Color.WHITE);
		    }
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
	    boolean darkmode = DarkmodeMonitor.getDarkmode();
	    Component result = super.getTableCellRendererComponent
		(table, value, isSelected, hasFocus, row, column);
	    if (column == 1) {
		String key = (String)table.getValueAt(row, 0);
		if (key != null && key.startsWith("ebase64.")) {
		    result.setForeground(darkmode? Color.GREEN.brighter()
					 .brighter():
					 Color.GREEN.darker().darker());
		} else {
		    result.setForeground(darkmode? Color.WHITE: Color.BLACK);
		}
	    } else {
		result.setForeground(darkmode? Color.WHITE: Color.BLACK);
	    }
	    return result;
	}
    }

    private static class TaggedTextField extends JTextField {
	JTable table;
	int tag = 0;
	String oldvalue;
	String oldShortValue;
	public TaggedTextField() {super();}
    }

    private static final Pattern keyPattern =
	Pattern.compile("[a-zA-Z][a-zA-Z0-9_]*([.][a-zA-Z][a-zA-Z0-9_]*)*");

    private static boolean badKey(String key) {
	Matcher matcher = keyPattern.matcher(key);
	return matcher.matches() == false;
    }

    String lastKey = null;

    private class OurCellEditor1 extends TextCellEditor<String> {
	HashSet<String> keys = null;

 	public OurCellEditor1(HashSet<String> keys) {
	    super(String.class, new TaggedTextField());
	    this.keys = keys;

	}
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
						     boolean isSelected,
						     int row, int column)
	{
	    String oldvalue = (String)value;
	    lastKey = oldvalue == null? null: oldvalue.trim();
	    TaggedTextField tf = (TaggedTextField)
		super.getTableCellEditorComponent(table, value, isSelected,
						  row, column);
	    tf.table = table;
	    tf.oldvalue = oldvalue;
	    if (oldvalue == null || oldvalue.equals("")) {
		tf.oldShortValue = null;
	    } else {
		if (oldvalue.startsWith(B64KEY_START)) {
		    oldvalue = oldvalue.substring(B64KEY_START_LEN);
		} else if (oldvalue.startsWith(EB64KEY_START)) {
		    oldvalue = oldvalue.substring(EB64KEY_START_LEN);
		}
		tf.oldShortValue = oldvalue;
	    }

	    /*
	    System.out.format("tf: bg %s, fg %s, caret %s, text = %s\n",
			      tf.getBackground(), tf.getForeground(),
			      tf.getCaretColor(), tf.getText());
	    */

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
	    // System.out.println("stopCellEditing called");
	    TaggedTextField tf = (TaggedTextField) getComponent();
	    String key = tf.getText();
	    if (key == null) {
		if (tf.oldShortValue != null) {
		    keys.remove(tf.oldShortValue);
		}
		return super.stopCellEditing();
	    }
	    key = key.trim();
	    if (key.length() == 0) {
		tf.setText(key);
		if (tf.oldShortValue != null) {
		    keys.remove(tf.oldShortValue);
		}
		return super.stopCellEditing();
	    }
	    if (key.equals(tf.oldvalue)) {
		tf.setText(key);
		return super.stopCellEditing();
	    }
	    if (key.startsWith("-")) {
		tf.setText(key);
		if (tf.oldShortValue != null) {
		    keys.remove(tf.oldShortValue);
		}
		return super.stopCellEditing();
	    }
	    String shortkey = key;
	    if (key.startsWith(B64KEY_START)) {
		shortkey = key.substring(B64KEY_START_LEN);
	    } else if (key.startsWith(EB64KEY_START)) {
		shortkey = key.substring(EB64KEY_START_LEN);
	    }

	    if (!shortkey.equals(tf.oldShortValue)) {
		while ((!shortkey.equals(tf.oldShortValue)
			&& (keys.contains(shortkey)
			    || altKeys.contains(shortkey)))
		       || badKey(key)) {
		    key = JOptionPane.showInputDialog
			(tf.table,"Key for this row (null to revert):",
			 "Key Request", JOptionPane.PLAIN_MESSAGE);
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
		if (tf.oldShortValue != null) {
		    keys.remove(tf.oldShortValue);
		}
		keys.add(shortkey);
	    }
	    tf.setText(key);
	    return super.stopCellEditing();
	}
    }

    private char[] password = null;

    /**
     * Request a GPG passphrase.
     * This method will open a dialog box to request a GPG pasphrase
     * for decryption. However, if the argument is null, this method
     * is not called from the event dispatch thread, and a system
     * console exists, the system console will be used to obtain the
     * password (unless the password exists).
     * <P>
     * To use a dialog box when 'owner' is null and a console exists,
     * use
     * <BLOCKQUOTE><PRE><CODE>
     * ConfigPropertyEditor cpe = ...;
     * ...
     * SwingUtilities.invokeAndWait(() -&gt; {
     *     cpe.requestPassphrase(null);
     * });
     * </BLOCKQUOTE></CODE></PRE>
     * <P>
     * NOTE: tests indicate that in Java, a system console exists only when
     * both standard input and standard output are connected to a terminal.
     * @param owner a component over which a dialog box should be displayed
     */
    public void requestPassphrase(Component owner) {
	if (password == null) {
	    if (!SwingUtilities.isEventDispatchThread()) {
		Console console = System.console();
		if (console != null) {
		    password = console
			.readPassword(localeString("enterPW2") + ":");
		    if (password == null || password.length == 0) {
			password = null;
		    }
		    return;
		} else {
		    try {
			SwingUtilities.invokeAndWait(() -> {
				requestPassphrase(owner);
			    });
		    } catch (InterruptedException e) {
		    } catch (InvocationTargetException e) {
		    }
		}
		return;
	    }
	    JPasswordField pwf = new JPasswordField(16);
	    pwf.addFocusListener(new FocusAdapter() {
		    boolean retry = true;
		    public void focusLost(FocusEvent e) {
			Component other = e.getOppositeComponent();
			Window w1 = SwingUtilities.getWindowAncestor
			    (pwf);
			Window w2 = (other == null)? null:
			    SwingUtilities.getWindowAncestor(other);
			if (retry && e.getCause()
			    == FocusEvent.Cause.UNKNOWN
			    && w1 == w2) {
			    SwingUtilities.invokeLater(() -> {
				    pwf.requestFocusInWindow();
				});
			} else {
			    retry = false;
			}
		    }
		});
	    pwf.addAncestorListener(new AncestorListener() {
		    public void ancestorAdded(AncestorEvent ev) {
			SwingUtilities.invokeLater(() -> {
				pwf.requestFocusInWindow();
			    });
		    }
		    public void ancestorRemoved(AncestorEvent ev) {
		    }
		    public void ancestorMoved(AncestorEvent ev) {
		    }
		});

	    for (;;) {
		int status = JOptionPane
		    .showConfirmDialog(owner, pwf, localeString("enterPW"),
				       JOptionPane.OK_CANCEL_OPTION);
		if (status == JOptionPane.OK_OPTION) {
		    char[] pw = pwf.getPassword();
		    if (pw == null) continue;
		    boolean ok = true;
		    for (int i = 0; i < pw.length; i++) {
			if (pw[i] == '\n') {
			    ok = false;
			    break;
			}
			if (pw[i] == '\r') {
			    ok = false;
			    break;
			}
		    }
		    if (!ok) {
			pwf.setText("");
			continue;
		    }
		    password = pw;
		    break;
		} else {
		    break;
		}
	    }
	}
    }

    /**
     * Remove the current GPG passphrase.
     * As a general rule, this method should be called as soon as
     * a passphrase is no longer needed, or will not be needed for
     * some time.
     */
    public void clearPassphrase() {
	if (password != null) {
	    for (int i = 0; i < password.length; i++) {
		password[i] = (char)0;
	    }
	}
	password = null;
    }

    private class OurCellEditor2 extends TextCellEditor<String> {
	HashSet<String> keys = null;
	public OurCellEditor2(HashSet<String>keys) {
	    super(String.class, new TaggedTextField());
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
		requestPassphrase(pwowner);
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
		String[] recipients = (recipientsList != null)? recipientsList:
		    getRecipients(tf.table);
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

    boolean tableExtensible = true;

    /**
     * configure the table so that new rows cannot be added and
     * rows cannot be moved or deleted.
     */
    protected void freezeRows() {
	tableExtensible = false;
    }

    int extraInitialRows = 10;
    /**
     * Set the number of blank rows at the end of the table.
     * These rows are added after any rows containing reserved keys.
     * The default is 10.  Set to zero if only the reserved rows should
     * be in the table.
     * @param count the number of rows
     */
    protected void setInitialExtraRows(int count) {
	extraInitialRows = count;
    }

    static class Pair {
	TableCellRenderer renderer;
	TableCellEditor editor;
	public Pair(TableCellRenderer r, TableCellEditor e) {
	    renderer = r; editor = e;
	}
	public TableCellRenderer getR() {return renderer;}
	public TableCellEditor getE() {return editor;}
    }

    HashMap<String,Pair> map = new HashMap<>();

    /**
     * Provide a custom table-cell renderer and/or editor for the
     * value in the second column of some row.
     * The key provided in column 1 of a row will be tested against
     * the tail argument provided by this method.  If there is an
     * exact match for a key, the renderer or editor is used.
     * Otherwise the key is replaced by the remainder of the key
     * after its first period and the test is repeated until there
     * is a match or the key can no longer by shorted.
     * If there are multiple possible matches, the longest match is
     * used. A match is based on the first argument, not the second
     * or third.
     * @param tail the last components of a key.
     * @param r the table cell renderer to use to display the value
     *        corresponding to a key; null if the normal choice is not
     *        overridden.
     * @param e the table cell editor to use to modify or create a value
     *        corresponding to a key; null if the normal choice is not
     *        overridden.
     */
    public void addRE(String tail, TableCellRenderer r, TableCellEditor e) {
	map.put(tail, new Pair(r, e));
    }

    private boolean hasRE(String tail) {
	return map.containsKey(tail);
    }

    private TableCellRenderer getR(String tail) {
	Pair p = map.get(tail);
	return (p == null)? null: p.getR();
    }

    private TableCellEditor getE(String tail) {
	Pair p = map.get(tail);
	return (p == null)? null: p.getE();
    }

    private JMenuItem helpMenuItem = null;

    /**
     * Provide a menu item for displaying 'help' documentation.
     * One should be cautious about using an instance of {@link HelpMenuItem}
     * as the argument to this method due to this menu item's action opening
     * a new window, which can be problematic with modal dialogs.
     * <P>
     * If not called with a  non-null argument, a Help menu will not be
     * included.
     * @param helpMenuItem the menuItem; null if there is not such a menu item
     */
    public void setHelpMenuItem(JMenuItem helpMenuItem) {
	this.helpMenuItem = helpMenuItem;
    }

    private static class Ebase64TableCellRenderer extends JLabel
                           implements TableCellRenderer
    {

	Border unselectedBorder = null;
	Border selectedBorder = null;
	boolean isBordered = true;

	/**
	 * Constructor.
	 * @param isBordered true if this cell renderer should use a border;
	 *                   false otherwise
	 */
	public Ebase64TableCellRenderer(boolean isBordered) {
	    super();
	    this.isBordered = isBordered;
	    setOpaque(true); //MUST do this for background to show up.
	}

	@Override
	public Component
	    getTableCellRendererComponent(JTable table, Object value,
					  boolean isSelected, boolean hasFocus,
					  int row, int column) {
	    String s = (value == null)? "": (String) value;
	    boolean darkmode = DarkmodeMonitor.getDarkmode();
	    setBackground(null);

	    if (s.trim().equals("")) {
		setText("");
	    } else {
		setText(localeString("encryptedTCR"));
	    }
	    if (darkmode) {
		setForeground(Color.YELLOW.darker());
	    } else {
		setForeground(Color.YELLOW.brighter());
	    }
	    if (isBordered) {
		if (isSelected) {
		    if (selectedBorder == null) {
			selectedBorder = BorderFactory
			    .createMatteBorder(2,5,2,5,
					       table.getSelectionBackground());
		    }
		    setBorder(selectedBorder);
		} else {
		    if (unselectedBorder == null) {
			unselectedBorder = BorderFactory
			    .createMatteBorder(2,5,2,5,
					       table.getBackground());
		    }
		    setBorder(unselectedBorder);
		}
	    }
	    return this;
	}
    }

    /**
     * Table-Cell renderer that describes its contents rather than
     * displays its contents.
     * An instance of this renderer can be used with
     * {@link #addRE(String,TableCellRenderer,TableCellEditor)} for
     * cases where the contents of a cell should not be displayed and
     * one wants adescription of its contents instead.
     * <P>
     * A function maps the value to the string describing it.  If the
     * value is a string, the string is trimmed and if the resulting
     * length is value, the valuse is handled as if it were null. Any
     * text that is displayed by this renderer will be bracked by square
     * brackets.
     * <P>
     * This renderer will use colors that are the same as those used for
     * indicating encrypted text.
     */
    public static class DescribingRenderer extends JLabel
                           implements TableCellRenderer
    {

	Border unselectedBorder = null;
	Border selectedBorder = null;
	boolean isBordered = true;
	Function<Object,String> textFunction = null;

	/**
	 * Constructor.
	 * @param textFunction a function that maps the value of a cell to
	 *        a string that should be displayed when the contents are
	 *        not empty
	 * @param isBordered true if this cell renderer should use a border;
	 *                   false otherwise
	 * @throws IllegalArgumentException if the first argument is null
	 */
	public DescribingRenderer(Function<Object,String> textFunction,
				  boolean isBordered) {
	    super();
	    if (textFunction == null) {
		String msg = errorMsg("textFunctionNeeded");
		throw new IllegalArgumentException(msg);
	    }
	    this.isBordered = isBordered;
	    setOpaque(true); //MUST do this for background to show up.
	    this.textFunction = textFunction;
	}

	@Override
	public Component
	    getTableCellRendererComponent(JTable table, Object value,
					  boolean isSelected, boolean hasFocus,
					  int row, int column) {
	    if (value instanceof String) {
		String s = (String) value;
		if (s.trim().length() == 0) value = null;
	    }
	    boolean darkmode = DarkmodeMonitor.getDarkmode();
	    setBackground(null);

	    if (value == null) {
		setText("");
	    } else {
		setText("[" + textFunction.apply(value) + "]");
	    }
	    if (darkmode) {
		setForeground(Color.YELLOW.darker());
	    } else {
		setForeground(Color.YELLOW.brighter());
	    }
	    if (isBordered) {
		if (isSelected) {
		    if (selectedBorder == null) {
			selectedBorder = BorderFactory
			    .createMatteBorder(2,5,2,5,
					       table.getSelectionBackground());
		    }
		    setBorder(selectedBorder);
		} else {
		    if (unselectedBorder == null) {
			unselectedBorder = BorderFactory
			    .createMatteBorder(2,5,2,5,
					       table.getBackground());
		    }
		    setBorder(unselectedBorder);
		}
	    }
	    return this;
	}
    }


    private static Ebase64TableCellRenderer encryptedTCR = null;

    static {
	if (SwingUtilities.isEventDispatchThread()) {
	    encryptedTCR = new Ebase64TableCellRenderer(false);
	} else {
	    try {
		SwingUtilities.invokeLater(() -> {
			encryptedTCR = new Ebase64TableCellRenderer(false);
		    });
		// using invokeAndWait causes the process to hang.
		Toolkit.getDefaultToolkit().sync();
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
    }

    /**
     * Mode to determine options for closing a property-editor window
     * or dialog.
     */
    public enum CloseMode {
	/**
	 * The program will exit when the property-editor window is
	 * closed.
	 */
	QUIT,
	/**
	 * The program will not exit when the property-editor window
	 * is closed.
	 */
	CLOSE,
	/**
	 * Menu items will determine if the program exits or continues when
	 * the property-editor window is closed
	 */
	BOTH
    }

    private JMenuItem closeMenuItem = null;
    private JMenuItem quitMenuItem = null;

    TableModelListener tml = (tme) -> {needSave = true;};

    private static class TmlAddedContainer {
	public boolean tmlAdded = false;
    }

    /**
     * Event for changes to monitored properties belonging to a
     * {@link ConfigPropertyEditor}.
     */
    public static class ConfigPropertyEvent extends java.util.EventObject {
	String property;
	String value;
	ConfigPropertyEvent (ConfigPropertyEditor source,
			     String property,
			     String value)
	{
	    super(source);
	    this.property = property;
	    this.value = value;
	}

	/**
	 * Get the property name for this event.
	 * @return the name of the property
	 */
	public String getProperty() {return property;}

	/**
	 * Get the property value for this event.
	 * @return the value for the property; null if either the
	 *         value is null or if the property is not currently
	 *         in the editor's tables
	 */
	public String getValue() {return value;}
    }

    /**
     * Listener for changes in ConfigPropertyEditor properties.
     */
    @FunctionalInterface
    public static interface ConfigPropertyListener
	extends java.util.EventListener
    {
	/**
	 * Indicate that a property change
	 * @param event the event that was triggered by a property change
	 */
	void propertyChanged(ConfigPropertyEvent event);
    }
    private Set<ConfigPropertyListener> cplSet = new LinkedHashSet<>();

    private Set<String> monitoredKeys = new HashSet<String>();
    private Map<String,String> monitorMap = new HashMap<>() {
	    public String put(String key, String value)
		throws UnsupportedOperationException,
		       ClassCastException,
		       NullPointerException,
		       IllegalArgumentException
	    {
		String result = super.put(key, value);
		ConfigPropertyEvent event = new	ConfigPropertyEvent
		    (ConfigPropertyEditor.this, key, value);
		for (ConfigPropertyListener l: cplSet) {
		    l.propertyChanged(event);
		}
		return result;
	    }

	    public String remove(Object key)
	    {
		String result = super.remove(key);
		String k = (String)key;
		if (result != null && monitoredKeys.contains(k)) {
		    ConfigPropertyEvent event = new ConfigPropertyEvent
			(ConfigPropertyEditor.this, k, null);
		    for (ConfigPropertyListener l: cplSet) {
			l.propertyChanged(event);
		    }
		}
		return result;
	    }
	};

    /**
     * Add a {@link ConfigPropertyListener} to this
     * {@link ConfigPropertyEditor}.  The listeners will be called
     * when a monitored property's value has changed.
     * @param l the listener to add
     * @see #monitorProperty(String)
     */
    public void addConfigPropertyListener(ConfigPropertyListener l)
    {
	cplSet.add(l);
    }

    /**
     * Remove a {@link ConfigPropertyListener} from this
     * {@link ConfigPropertyEditor}.
     * @param l the listener to remove
     */
    public void removeConfigPropertyListener(ConfigPropertyListener l)
    {
	cplSet.remove(l);
    }

    /**
     * Monitor a property.
     * Registered listeners will be called when the property changes value.
     * @param property the property to monitor
     * @exception IllegalArgumentException the property's name starts
     *            with "ebase64.", indicating an encrypted value
     * @see #addConfigPropertyListener(ConfigPropertyListener)
     */
    protected void monitorProperty(String property)
	throws IllegalArgumentException
    {
	if (property.startsWith(EB64KEY_START)) {
	    throw new IllegalArgumentException(errorMsg("monitorEB64"));
	}
	monitoredKeys.add(property);
    }

    /**
     * Get the value for a monitored property.
     * @param property a propertyy that is being monitored
     * @return the value for the specified property
     * @exception IllegalArgumentException the key was not monitored
     */
    protected String getMonitoredPropertyValue(String property)
	throws IllegalArgumentException
    {
	if (property == null || !monitoredKeys.contains(property)) {
	    throw new IllegalArgumentException
		(errorMsg("notMonitoredKey", property));
	}
	return monitorMap.get(property);
    }

    Map<String,Set<String>> cpcMap = new HashMap<>();

    // Need a comparator that ignores an initial "base64." or "ebase64."
    private static final Comparator<String> keyComparator =
	new Comparator<>()
	{
	    public int compare(String s1, String s2) {
		String s1a =
		s1.startsWith(B64KEY_START)? s1.substring(B64KEY_START_LEN):
		s1.startsWith(EB64KEY_START)? s1.substring(EB64KEY_START_LEN):
		s1;
		String s2a =
		s2.startsWith(B64KEY_START)? s2.substring(B64KEY_START_LEN):
		s2.startsWith(EB64KEY_START)? s2.substring(EB64KEY_START_LEN):
		s2;
		if (s1a.equals(s2a)) {
		    return s1.compareTo(s2);
		} else {
		    return s1a.compareTo(s2a);
		}
	    }
	};


    /**
     * Indicate that if one property's value changes, various other properties
     * should have their values set to null.
     * <P>
     * THis is useful in cases such as one property providing a file format
     * and a second providing a file name that is required to have a particular
     * extension.
     * @param p a property
     * @param others a list of properties whose values should be set to
     *        null if property p changes
     */
    public void changedPropertyClears(String p, String... others) {
	int rcount = 0;
	for (String s: others) {
	    if (s.equals(p) || s == null) {
		rcount++;
	    }
	}
	String[] properties = new String[others.length - rcount];
	int i = 0;
	for (String s: others) {
	    if (s.equals(p) || s == null) continue;
	    properties[i++] = s;
	}
	Arrays.sort(properties, keyComparator);
	rcount = 0;
	String last = null;
	for (String s: properties) {
	    if (s.equals(last)) {
		rcount++;
	    }
	    last = s;
	}
	if (rcount > 0) {
	    String[] tmp = properties;
	    properties = new String[properties.length - rcount];
	    last = null;
	    i = 0;
	    for (String s: tmp) {
		if (s.equals(last)) continue;
		properties[i++] = s;
		last = s;
	    }
	}
	cpcMap.put(p, Set.of(properties));
    }

    /**
     * Test if a key exists.
     * @param key the key
     * @return true if the key exists; false otherwise
     */
    public boolean hasKey(String key) {
	if (properties == null) {
	    return false;
	}
	return (properties.get(key) != null);
    }

    /**
     * Set a key.
     * The first argument is used when a key starts with "ebase64." as
     * that indicates that GPG encryption will be used, in which case
     * dialog boxes will be used to get the names of recipients.
     * If a key already exists, its value will be overwritten.
     * @param owner a compoent over which a dialog box may appear; null
     *        if a dialog box's location is not constrained
     * @param key the key
     * @param value the value for the key
     * @return true if successful; false otherwise (e.g., GPG failed
     *         to encrypt, the key was missing, or the value was null)
     */
    public boolean set(Component owner, String key, String value) {
	if (key == null) return false;
	key = key.trim();
	if (key.length() == 0) return false;
	if (value == null) return false;
	if (properties == null) {
	    properties = new Properties();
	}
	if (key.startsWith(EB64KEY_START)) {
	    String[] recipients = (recipientsList != null)? recipientsList:
		getRecipients(owner);

	    if (recipients == null || recipients.length == 0) return false;
	    String evalue = encrypt(value, recipients);
	    if (evalue == null) return false;
	    properties.setProperty(key, evalue);
	} else if (key.startsWith(B64KEY_START)) {
	    properties.setProperty(key, encode(value));
	} else {
	    properties.setProperty(key, value);
	}
	return true;
    }

    Component pwowner = null;

    /**
     * Prevent editing of specific rows and columns.
     * The default implementation returns <CODE>false</CODE> for any
     * pair of arguments. If the value returned is dependent on a
     * cell's row and column instead of its contents, the behavior
     * of this class may be erratic unless those cells cannot be moved.
     * <P>
     * This method can not override the prohibition on editing reserved
     * keys or spacers.
     * @param row the table row
     * @param col the table column
     * @return true if editing is explicitly prohibited for the given
     *         row and column; false otherwise
     */
    protected boolean prohibitEditing(int row, int col) {
	return false;
    }

    private void doEdit(final Component owner,  final Mode mode,
			final Callable continuation,
			final CloseMode quitCloseMode)
     {
	 selectedIndex = -1;
	 needSave = false;
	 final CallableContainer continuationContainer =
	    new CallableContainer(continuation);
	//System.out.println("mode = " + mode);
	 Window window;
	 switch (mode) {
	 case JFRAME:
	     window = new JFrame(configTitle());
	     break;
	 default:
	     if (owner == null) {
		 window = new JDialog(null, configTitle(),
				      ((mode == Mode.MODAL)?
				       Dialog.ModalityType.APPLICATION_MODAL:
				       Dialog.ModalityType.MODELESS));
	     } else if (owner instanceof Dialog) {
		 window = new JDialog((Dialog)owner, configTitle(),
				      mode == Mode.MODAL);
	     } else if (owner instanceof Frame) {
		 window = new JDialog((Frame)owner, configTitle(),
				      mode == Mode.MODAL);
	     } else if (owner instanceof Window) {
		 window = new JDialog((Window)owner, configTitle(),
				      ((mode == Mode.MODAL)?
				       Dialog.ModalityType.APPLICATION_MODAL:
				       Dialog.ModalityType.MODELESS));
	     } else {
		 window = new JDialog(SwingUtilities.getWindowAncestor(owner),
				      configTitle(),
				      ((mode == Mode.MODAL)?
				       Dialog.ModalityType.APPLICATION_MODAL:
				       Dialog.ModalityType.MODELESS));
	     }
	     break;
	 }
	 /*
	 Window window = (mode == Mode.JFRAME)?
	    new JFrame(configTitle()):
	    new JDialog(((owner == null)? null:
			 (owner instanceof Window)? (Window)owner:
			 SwingUtilities.getWindowAncestor(owner)),
			configTitle(),
			((mode == Mode.MODAL)?
			 Dialog.ModalityType.APPLICATION_MODAL:
			 Dialog.ModalityType.MODELESS));
	 */

	 pwowner = window;

	Set<String> names = new HashSet<>(64);
	if (properties == null) {
	    properties = new Properties();
	}
	names.addAll(properties.stringPropertyNames());
	for (String nm: reserved) {
	    if (!names.contains(nm)) {
		String[] others = otherKeys.get(nm);
		String kfound = null;
		if (others != null) {
		    for (String k: others) {
			if (names.contains(k)) {
			    kfound = k;
			    break;
			}
		    }
		}
		if (kfound == null) names.add(nm);
		else names.add(kfound);
	    }
	}
	// names.addAll(reserved);

	String[] keys1 = new String[names.size()];
	keys1 = names.toArray(keys1);
	/*
	for (String n: names) {
	    System.out.println("(names) n = " + n);
	}
	*/
	for (int i = 0; i < keys1.length; i++) {
	    String key = keys1[i];
	    if (reserved.contains(key)) {
		keys1[i] = "";
	    }
	}

	/*
	System.out.println("extraInitialRows = " + extraInitialRows);
	System.out.println("tableExtensible = " + tableExtensible);
	*/

	int adjustedSpacersLength = spacers.length
	    - ((extraInitialRows == 0 && !tableExtensible)? 1: 0);

	/*
	System.out.println("spacers.length = " + spacers.length
			   + ", adjustedSpacersLength = "
			   + adjustedSpacersLength);
	*/
	String[] keys = new String[keys1.length + adjustedSpacersLength];

	System.arraycopy(keys1, 0, keys, 0, keys1.length);
	for (int i = 0; i < adjustedSpacersLength; i++) {
	    keys[keys1.length+i] = "";
	}
	Arrays.sort(keys, keyComparator);
	int kind = 0;
	int sind = 0;
	int mkind = 0;
	/*
	for (int i = 0; i < adjustedSpacersLength; i++) {
	    System.out.print(" " + spacers[i]);
	}
	System.out.println();
	*/
	for (String key: reserved) {
	    if (properties.getProperty(key) == null) {
		String[] others = otherKeys.get(key);
		if (others != null) {
		    for (String k: others) {
			if (properties.getProperty(k) != null) {
			    key = k;
			    break;
			}
		    }
		}
	    }
	    keys[kind++] = key;
	    if (sind < adjustedSpacersLength && kind == spacers[sind]) {
		keys[kind++] = "-------------";
		sind++;
	    }
	}
	/*
	for (int i = 0; i < keys.length; i++) {
	    System.out.println(keys[i]);
	}
	*/

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
	    if (monitoredKeys.contains(key) && value != null) {
		monitorMap.put(key, value);
	    }
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
				       new OurCellRenderer1(editorMap),
				       new OurCellEditor1(editorKeys)),
	    new InputTablePane.ColSpec(localeString("Value"),
				       "mmmmmmmmmmmmmmm",
				       String.class,
				       new OurCellRenderer2(),
				       new OurCellEditor2(editorKeys))
	};

	InputTablePane ipane = new InputTablePane(colspec,
						  len + extraInitialRows,
						  data,
						  tableExtensible,
						  tableExtensible,
						  tableExtensible) {

		@Override
		protected boolean prohibitEditing(int row, int col) {
		    if (row < KLEN) {
			if (col == 0) {
			    return editorMap.get(row)== null;
			} else if (col == 1) {
			    String key = (String)getValueAt(row, 0);
			    if (key.trim().charAt(0) == '-') return true;
			}
		    }
		    return ConfigPropertyEditor.this
			.prohibitEditing(row, col);
		}
		@Override
		protected int minimumSelectableRow(int col, boolean all) {
		    if (all) {
			return KLEN;
		    } else {
			return 0;
		    }
		}
		@Override
		protected TableCellRenderer
		    getCustomRenderer(JTable tbl, int row, int col)
		{
		    if (col != 1) return null;
		    String key = (String)tbl.getValueAt(row, 0);
		    if (key == null) return null;
		    if (key.startsWith("ebase64.")) return encryptedTCR;
		    if (hasRE(key)) {
			return getR(key);
		    } else {
			int ind;
			while ((ind = key.indexOf('.')) != -1) {
			    key = key.substring(ind+1);
			    if (hasRE(key)) {
				return getR(key);
			    }
			}
			return null;
		    }
		}

		@Override
		protected TableCellEditor
		    getCustomEditor(JTable tbl, int row, int col)
		{
		    if (col == 0) {
			return editorMap.get(row);
		    }
		    if (col != 1) return null;
		    String key = (String)tbl.getValueAt(row, 0);
		    if (key == null) return null;
		    if (key.startsWith("ebase64.")) return null;
		    if (hasRE(key)) {
			return getE(key);
		    } else {
			int ind;
			while ((ind = key.indexOf('.')) != -1) {
			    key = key.substring(ind+1);
			    if (hasRE(key)) {
				return getE(key);
			    }
			}
			return null;
		    }
		}

		@Override
		protected void beforeRowDeletion(int row) {
		    String key = (String)getValueAt(row, 0);
		    String shortkey = key;
		    if (key.startsWith(B64KEY_START)) {
			shortkey = key.substring(B64KEY_START_LEN);
		    } else if (key.startsWith(EB64KEY_START)) {
			shortkey = key.substring(EB64KEY_START_LEN);
		    }
		    if (editorKeys.contains(shortkey)) {
			editorKeys.remove(shortkey);
		    }
		    if (monitoredKeys.contains(key)) {
			monitorMap.remove(key);
		    }
		    Set<String> clearSet = cpcMap.get(key);
		    if (clearSet != null) {
			int n = getRowCount();
			for (int i = 0; i < n; i++) {
			    String ckey = (String)getValueAt(i, 0);
			    if (ckey != null && clearSet.contains(ckey)) {
				setValueAt(null, i, 1);
				monitorMap.remove(ckey);
			    }
			}
		    }
		}
	    };

	TableModelListener tml2 = (tme2) -> {
	    if (tme2.getColumn() == 0) {
		int row = tme2.getFirstRow();
		if (row == tme2.getLastRow()) {
		    switch(tme2.getType()) {
		    case TableModelEvent.UPDATE:
			String key = (String)ipane.getValueAt(row, 0);
			if (otherKeys.containsKey(key)) {
			    for (String k: otherKeys.get(key)) {
				if (monitorMap.containsKey(k)) {
				    monitorMap.remove(k);
				}
			    }
			} else if (reverse.containsKey(key)) {
			    String k = reverse.get(key);
			    monitorMap.remove(k);
			    for (String kk: otherKeys.get(k)) {
				if (kk == key) continue;
				monitorMap.remove(kk);
			    }
			}
			if (key == null && lastKey != null) {
			    String lk = lastKey;
			    // lastKey = null; // to prevent a stack overflow
			    ipane.setValueAt(null, row, 1);
			    monitorMap.remove(lk);
			} else if (key != null
				   && !key.equals(lastKey))  {
			    monitorMap.remove(lastKey);
			    String val = defaults.getProperty(key);
			    if (val == null) {
				ipane.setValueAt(null, row, 1);
				monitorMap.remove(key);
			    } else {
				ipane.setValueAt(val, row, 1);
				if (monitoredKeys.contains(key)) {
				    monitorMap.put(key, val);
				}
			    }
			}
			break;
		    default:
			break;
		    }
		}
	    } else if (tme2.getColumn() == 1) {
		int start = tme2.getFirstRow();
		int last = tme2.getLastRow();
		for (int row = start; row <= last; row++) {
		    String key = (String)ipane.getValueAt(row, 0);
		    if (monitoredKeys.contains(key)) {
			monitorMap.put(key,
				       (String)ipane.getValueAt(row, 1));
		    }
		    switch(tme2.getType()) {
		    case TableModelEvent.UPDATE:
			Set<String> clearSet1 = cpcMap.get(lastKey);
			Set<String> clearSet2 = cpcMap.get(key);
			if (clearSet1 != null || clearSet2 != null) {
			    lastKey = null; // to prevent stack overflow
			    int n = ipane.getRowCount();
			    for (int i = 0; i < n; i++) {
				String ckey = (String)ipane.getValueAt(i, 0);
				if (ckey != null) {
				    if (clearSet1 != null
					&& clearSet1.contains(ckey)) {
					ipane.setValueAt(null, i, 1);
					monitorMap.remove(ckey);
				    }
				    if (clearSet2 != null
					&& clearSet2.contains(ckey)) {
					ipane.setValueAt(null, i, 1);
					monitorMap.remove(ckey);
				    }
				}
			    }
			}
			// clear for next selection
			lastKey = null;
			break;
		    default:
			break;
		    }
		}
	    }
	};

	ipane.addTableModelListener(tml2);

	// Use a container so we can change the value of tmlAdded
	// in a listener.
	TmlAddedContainer tmlAddedContainer = new TmlAddedContainer();

	if (useNeedSave) {
	    ipane.addTableModelListener(tml);
	    tmlAddedContainer.tmlAdded = true;
	}
	JPanel panel = new JPanel();
	panel.setLayout(new BorderLayout());
	panel.add(ipane, BorderLayout.CENTER);
	JMenuBar menubar = new JMenuBar();
	JMenu fileMenu = new JMenu(localeString("File"));
	fileMenu.setMnemonic(KeyEvent.VK_F);
	menubar.add(fileMenu);
	if (helpMenuItem != null) {
	    JMenu helpMenu = new JMenu(localeString("Help"));
	    helpMenu.add(helpMenuItem);
	    menubar.add(helpMenu);
	}

	ActionListener qcl = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    ipane.stopCellEditing();
		    if (tmlAddedContainer.tmlAdded) {
			ipane.removeTableModelListener(tml);
			tmlAddedContainer.tmlAdded = false;
		    }
		    if (needSave) {
			String emsg;
			if (e.getSource() == quitMenuItem) {
			    emsg = errorMsg("saveQuestionQuit");
			} else {
			    emsg = errorMsg("saveQuestionClose");
			}
			if (JOptionPane.showConfirmDialog
				(window, emsg, configTitle(),
				 JOptionPane.OK_CANCEL_OPTION,
				 JOptionPane.QUESTION_MESSAGE)
			    == JOptionPane.OK_OPTION) {
			    if (!doSave(window, file, false, /*table*/ipane)) {
				// We couldn't save. Let the user try
				// again and maybe explicitly cancel or
				// use save-as to pick a new file.
				return;
			    }
			}
		    }
		    window.setVisible(false);
		    window.dispose();
		    pwowner = null;
		    CloseMode qcm = quitCloseMode;
		    if (qcm == CloseMode.BOTH) {
			qcm = (e.getSource() == quitMenuItem)?
			    CloseMode.QUIT: CloseMode.CLOSE;
		    }
		    switch (qcm) {
		    case QUIT:
			System.exit(0);
		    case CLOSE:
			return;
		    }
		}
	    };

	/*
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
	menuItem.addActionListener(qcl);
	fileMenu.add(menuItem);
	*/
	boolean doBreak = true;
	JMenuItem menuItem = null;
	switch (quitCloseMode) {
	case BOTH:
	    doBreak = false;
	case QUIT:
	    menuItem = new JMenuItem(localeString("Quit"), KeyEvent.VK_Q);
	    menuItem.setAccelerator(KeyStroke.getKeyStroke
				    (KeyEvent.VK_Q,
				     InputEvent.CTRL_DOWN_MASK));
	    menuItem.addActionListener(qcl);
	    fileMenu.add(menuItem);
	    quitMenuItem = menuItem;
	    if (doBreak) break;
	case CLOSE:
	    menuItem = new JMenuItem(localeString("Close"), KeyEvent.VK_W);
	    menuItem.setAccelerator(KeyStroke.getKeyStroke
				    (KeyEvent.VK_W,
				     InputEvent.CTRL_DOWN_MASK));
	    menuItem.addActionListener(qcl);
	    fileMenu.add(menuItem);
	    closeMenuItem = menuItem;
	    break;
	}

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
			if (tmlAddedContainer.tmlAdded) {
			    ipane.removeTableModelListener(tml);
			    tmlAddedContainer.tmlAdded = false;
			}
			save(properties, /*table*/ipane);
			Set<String> loop = checkLoops(ipane);
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
			    JOptionPane.showMessageDialog(owner,
							  sb.toString(),
							  errorTitle(),
							  JOptionPane
							  .ERROR_MESSAGE);
			    // Restart the editor because there were
			    // loops and these have to be fixed. to have
			    // a consistent state.
			    window.dispose();
			    SwingUtilities.invokeLater(() -> {
				    doEdit(owner, mode, continuation,
					   quitCloseMode);
				});
			    return;
			}
			if (continuationContainer.callable!= null) {
			    Callable callable = continuationContainer.callable;
			    continuationContainer.callable = null;
			    callable.call();
			}
			window.dispose();
		    }
		    public void windowClosed() {
			if (tmlAddedContainer.tmlAdded) {
			    ipane.removeTableModelListener(tml);
			    tmlAddedContainer.tmlAdded = false;
			}
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
			    if (tmlAddedContainer.tmlAdded) {
				ipane.removeTableModelListener(tml);
				tmlAddedContainer.tmlAdded = false;
			    }
			    save(properties, /*table*/ipane);
			    Set<String> loop = checkLoops(ipane);
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
				JOptionPane.showMessageDialog(owner,
							      sb.toString(),
							      errorTitle(),
							      JOptionPane
							      .ERROR_MESSAGE);
				// Restart the editor because there were
				// loops and these have to be fixed. to have
				// a consistent state.
				window.dispose();
				SwingUtilities.invokeLater(() -> {
					doEdit(owner, mode, continuation,
					       quitCloseMode);
				    });
				return;
			    }
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
	    } else {
		if (continuationContainer.callable != null) {
		    Callable callable =
			continuationContainer.callable;
		    continuationContainer.callable = null;
		    callable.call();
		}
	    }
	}
	
	window.add(panel, BorderLayout.CENTER);
	window.pack();
	// It seems that we have to position the window explicitly,
	// at least in some cases.
	if (window.getOwner() != null) {
	    Rectangle r1 = window.getBounds();
	    Rectangle r2 = window.getOwner().getBounds();
	    Rectangle r3 = window.getOwner().getGraphicsConfiguration()
		.getBounds();
	    int x = r2.x  + r2.width/2 - r1.width/2;
	    int y = r2.y + r2.height/2 - r1.height/2;
	    int x3L = r3.x;
	    int y3L = r3.y;
	    int x3U = x3L + r3.width;
	    int y3U = x3L + r3.height;
	    if (x3L > x) {
		x = x3L;
	    } else if (x3U < x + r1.width) {
		x = x3U - r1.width;
	    }
	    if (y3L > y) {
		y = y3L;
	    } else if (y3U < y + r1.height) {
		y = y3U - r1.height;
	    }
	    Point p = new Point(x, y);
	    window.setLocation(p);
	}
	window.setVisible(true);
	if (mode == Mode.MODAL) {
	    ipane.stopCellEditing();
	    if (tmlAddedContainer.tmlAdded) {
		ipane.removeTableModelListener(tml);
		tmlAddedContainer.tmlAdded = false;
	    }
	    save(properties, /*table*/ipane);
	    Set<String> loop = checkLoops(ipane);
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
		JOptionPane.showMessageDialog(owner,
					      sb.toString(),
					      errorTitle(),
					      JOptionPane
					      .ERROR_MESSAGE);
		// Restart the editor because there were
		// loops and these have to be fixed. to have
		// a consistent state.
		window.dispose();
		doEdit(owner, mode, continuation, quitCloseMode);
	    } else if (continuationContainer.callable != null) {
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
     * @param quitCloseMode {@link CloseMode#QUIT} if the process should
     *        exit when the editor closes; {@link CloseMode#CLOSE} if
     *        the process should continue after the editor clsoes, or
     *        {@link CloseMode#BOTH} if both a Quit and a Close menu
     *        item should appear in the File menu.
     */
    public void edit(Component owner,  Mode mode, Callable continuation,
			CloseMode quitCloseMode)
    {
	// selectedIndex = -1;  (moved to doEdit)
	if (mode == null) throw new
			      IllegalArgumentException(errorMsg("noMode"));
	if (mode == Mode.MODAL) {
	    if (SwingUtilities.isEventDispatchThread()) {
		doEdit(owner, mode, continuation, quitCloseMode);
	    } else {
		try {
		    // System.out.println("invokeAndWait");
		    SwingUtilities.invokeAndWait(() -> {
			    doEdit(owner, mode, continuation, quitCloseMode);
			    // System.out.println("doEdit exited");
			});
		} catch (InterruptedException e) {
		} catch (InvocationTargetException et) {
		    et.printStackTrace();
		} catch (Exception ee) {
		    ee.printStackTrace();
		}
	    }
	} else {
	    if (SwingUtilities.isEventDispatchThread()) {
		doEdit(owner, mode, continuation, quitCloseMode);
	    } else {
		SwingUtilities.invokeLater(() -> {
			doEdit(owner, mode, continuation, quitCloseMode);
		    });
	    }
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
     * @return the properties, encoded if necessary
     */
    public Properties getEncodedProperties() {
	Properties results = new Properties();
	results.putAll(properties);
	return results;
    }

    /**
     * Get the decoded properties.
     * Base-64 encoding will be removed for unencrypted properties
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
     * {@link Hashtable#get(Object) get(Object)} will return
     * an {@link Object}
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
     * <P>
     * NOTE:  For encrypted values the method
     * {@link #requestPassphrase(Component)}
     * should be called before a call to {@link Properties#get(Object)}
     * if the dialog box is to be placed over a specific component.
     * @return the properties
     * @see #requestPassphrase(Component)
     * @see #clearPassphrase()
     */
    public Properties getDecodedProperties() {
	Properties results = new Properties() {
		public Object get(Object k) {
		    if (k instanceof String) {
			String key = (String) k;
			if (key.startsWith("ebase64.")) {
			    String encrypted = getProperty(key);
			    requestPassphrase(null);
			    return decryptToCharArray(encrypted);
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
