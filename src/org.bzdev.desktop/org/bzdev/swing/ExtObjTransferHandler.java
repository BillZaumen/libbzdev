package org.bzdev.swing;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.awt.*;
import javax.swing.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import org.bzdev.util.ErrorMessage;


/**
 * TransferHandler for external objects referenced by file name or URL.
 * This class supports drag and drop operations in which the objects
 * transferred are identified by file names or URLs.
 * <p>
 * At a minimum, users of this class must implement the methods
 * clear(boolean) and addURL(URL).  It may be desirable to also
 * implement setFile() addFile(FILE), and setURL(URL) for efficiency.
 * These methods affect the target of a D&amp;D operation and may do
 * more than simply copy a URL and store it.
 * <P>
 * As an example, suppose a class named InputPane is used to provide
 * input and can be used as a D&amp;D target when the boolean field dndInput
 * has the value <code>true</code>.  The class definition of InputPane
 * could include the following inner class:
 * <blockquote><pre><code>
 *    class FilenameTransferHandler extends ExtObjTransferHandler {
 *      {@literal @}Override
 *      protected void clear(boolean all) {
 *          InputPane.this.clear(all);
 *      }
 *
 *      {@literal @}Override
 *      protected void addFile(File f) {
 *          InputPane.this.addFile(f);
 *      }
 *
 *      {@literal @}Override
 *      protected void addURL(URL url) {
 *          InputPane.this.addURL(url);
 *      }
 *
 *      FilenameTransfer(File cdir) {
 *          super(cdir);
 *      }
 *
 *      {@literal @}Override
 *      public boolean importData(TransferHandler.TransferSupport support) {
 *          Component comp = support.getComponent();
 *          if (!(comp instanceof InputPane) &amp;&amp;
 *              !(comp.getParent() instanceof InputPane)) {
 *              return false;
 *          }
 *          return super.importData(support);
 *      }
 *
 *      {@literal @}Override
 *      public boolean canImport(TransferHandler.TransferSupport support) {
 *          Component comp = support.getComponent();
 *          if ((comp instanceof InputPane ||
 *               comp.getParent() instanceof InputPane)) {
 *              if (!dndInput) return false;
 *              return super.canImport(support);
 *          }
 *          return false;
 *      }
 *  }
 * </CODE></PRE></blockquote>
 * This class uses methods of InputPane to store the objects transferred
 * via a D&amp;D operation and additionally overrides importData and
 * canImport to test the the target is the desired one: either an
 * InputPane or a component (e.g., an Icon) of an InputPane. The
 * constructor for InputPane includes the following statements:
 * <blockquote><pre><code>
 *      fnt = new FilenameTransferHandler(currentDirectory);
 *      setTransferHandler(fnt);
 * </CODE></PRE></blockquote>
 * <P>
 * The behavior of this class can be modified by calling
 * {@link ExtObjTransferHandler#setMultiEntryMode(boolean)} or
 * {@link ExtObjTransferHandler#setMultiEntryMode(boolean,boolean)}.
 * If not called, the default behavior is to respond to a D&amp;D
 * request by keeping all existing entries and accepting all the new entries
 * the D&amp;D operation provides.  For some specialized cases, other
 * options may be desirable, but the default is what one would use in
 * nearly all cases.
 */

abstract public class ExtObjTransferHandler extends TransferHandler {

    /**
     * Transfer an object and clear existing objects.
     * The default implementation converts the argument to a URL
     * passes that URL to setURL.
     * @param file a file representing the object to be transferred.
     */
    protected void setFile(File file) {
	try {
	    setURL(file.toURI().toURL());
	} catch (Exception e) {}
    }

    /**
     * Transfer an object and add it to a collection of existing objects.
     * The default implementation converts the argument to a URL
     * passes that URL to addURL.
     * @param file a file representing the object to be transferred.
     */
    protected void addFile(File file) {
	try {
	    addURL(file.toURI().toURL());
	} catch (Exception e) {}
    }

    /**
     * Remove  existing objects.
     * @param all true to clear all entries; false to keep the first entry
     */
    abstract protected void clear(boolean all);

    /**
     * Transfer an object referenced by a URL and clear existing objects.
     * The default implementation calls clear() and then addURL(url).
     * @param url the URL representing the object to be transferred.
     */
    protected void setURL (URL url) {
	clear(true);
	addURL(url);
    }

    /**
     * Transfer an object referenced by a URL and add it to the collection of
     * existing objects.
     * @param url the URL representing the object to be transferred.
     */
    abstract protected void addURL(URL url);


    // drag and drop from a Gnome Nautilus file-browser window provides
    // the following flavor instead of DataFlavor.javaFileListFlavor, so
    // we'll try to support both.

    static DataFlavor uriListDataFlavor;
    static {
	try {
	    uriListDataFlavor = 
		new DataFlavor("text/uri-list; class=java.lang.String");
	} catch (Exception e) {
	    System.err.println
		("cannot create MIME type " +
		 "\"text/uri-list; class=java.lang.String\"");
	    System.exit(1);
	}
    }
    static DataFlavor plainTextStringDataFlavor;
    static {
	try {
	    plainTextStringDataFlavor =
		new DataFlavor("text/plain; class=java.lang.String");
	} catch (Exception e) {
	    System.err.println
		("cannot create MIME type " +
		 "\"text/uri-list; class=java.lang.String\"");
	    System.exit(1);
	}
    }

    private boolean multiEntryMode = true; // multi-entry mode
    private boolean cmode = false;
    private File icurrentDir;

    /**
     * Constructor.
     * @param currentDir the current working directory to use for resolving 
     *        a relative file name referencing an object being transferred.
     */
    public ExtObjTransferHandler (File currentDir) {
	this.icurrentDir = currentDir;
    }

	
    /**
     * Set multi-entry mode.
     * Equivalent to setMultiEntryMode(mode, false).
     * When this class is initialized, its state is equivalent to calling
     * <code>setMultiEntryMode(true)</code>.
     * @param mode true if multiple entries are allowed; false otherwise
     * @see #setMultiEntryMode(boolean,boolean)
     */
    public void setMultiEntryMode(boolean mode) {
	setMultiEntryMode(mode, false);
    }
    /**
     * Set multi-entry mode with the replacement option.
     * The behavior depends on the combination of arguments provided:
     * <TABLE BORDER="1">
     * <caption>&nbsp;</caption>
     * <TR><TH>mode</TH><TH>cmode</TH><TH>Description</TH></TR>
     * <TR><TD>true</TD><TD>(ignored)</TD><TD>A drag &amp; drop operation may
     *  insert multiple values, with new entries added to the target</TD></TR>
     * <TR><TD>false</TD><TD>true</TD><TD>A drag &amp; drop operation may
     * insert a single value, even if multiple values were selected, and
     * previous values will be cleared from the JList.</TD></TR>
     * <TR><TD>false</TD><TD>false</TD><TD>A drag &amp; drop operation may
     * insert multiple values and previous values will be cleared from
     * the JList.</TD></TR>
     * </TABLE>
     * @param mode true if images from  multiple D&amp;D entries are kept;
     *        false if they are replaced
     * @param cmode true if only one value is to be inserted in a
     *        D&amp;D operation when mode has the value false; false
     *        if multiple values can be inserted in a D&amp;D
     *        operation when mode has the value false; ignored if mode has the
     *        value true
     */
    public void setMultiEntryMode(boolean mode, boolean cmode) {
	if (mode == false && cmode) {
	    clear(false);
	}
	multiEntryMode = mode;
	this.cmode = cmode;
    }

    @Override
    public boolean importData(JComponent comp, Transferable t) {
	return importData(new TransferHandler.TransferSupport(comp, t));
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport support) {
	Component comp = support.getComponent();
	Transferable t = support.getTransferable();
	int mode = -1;
	if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
	    mode = 0;
	} else if (t.isDataFlavorSupported(uriListDataFlavor)) {
	    mode = 1;
	} else if (t.isDataFlavorSupported(plainTextStringDataFlavor)) {
	    mode = 2;
	} else if (t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
	    mode = 3;
	} else {
	    return false;
	}
	// System.out.println("mode = " + mode);
	try {
	    if (mode == 0) {
		java.util.List flist = (java.util.List)
		    t.getTransferData(DataFlavor.javaFileListFlavor);
		boolean firsttime = !multiEntryMode;
		for (Object obj: flist) {
		    File f = (File)obj;
		    if (firsttime) {
			setFile(f);
			firsttime = false;
		    } else {
			addFile(f);
		    }
		    if ((!multiEntryMode) && cmode) break;
		}
	    } else if (mode == 1) {
		String[] strings = 
		    ((String) 
		     t.getTransferData(uriListDataFlavor)).trim().split("\\s+");
		boolean firsttime = !multiEntryMode;
		for (String s: strings) {
		    if (firsttime) {
			setURL(new URL(strings[0]));
			firsttime = false;
		    } else {
			addURL(new URL(s));
		    }
		    if ((!multiEntryMode) && cmode) break;
		}
	    } else /* if (mode == 2 || mode == 3) */ {
		String[] strings = 
		    ((String) 
		     t.getTransferData((mode == 2)?
				       plainTextStringDataFlavor:
				       DataFlavor.stringFlavor))
		    .split("(\\r\\n|\\n)+");
		boolean firsttime = !multiEntryMode;
		for (String s: strings) {
		    if (s.length() == 0) continue;
		    boolean isURL = false;
		    if (s.startsWith("file:") || s.startsWith("ftp:")
			|| s.startsWith("http:")
			|| s.startsWith("https:")) {
			isURL = true;
		    }
		    if (isURL) {
			if (firsttime) {
			    setURL(new URL(s.trim()));
			    firsttime = false;
			} else {
			    addURL(new URL(s.trim()));
			}
		    } else {
			if (firsttime) {
			    File f = new File(s);
			    if (f.isAbsolute()) {
				setFile(f);
			    } else {
				setFile(new File(icurrentDir, s));
			    }
			    firsttime = false;
			} else {
			    File f = new File(s);
			    if (f.isAbsolute()) {
				addFile(f);
			    } else {
				addFile(new File(icurrentDir, s));
			    }
			}
		    }
		    if ((!multiEntryMode) && cmode) break;
		}
	    }
	    return true;
	} catch (UnsupportedFlavorException ufe) {
	    ErrorMessage.display(ufe);
	    // ufe.printStackTrace();
	} catch (IOException ioe) {
	    ErrorMessage.display(ioe);
	    // ioe.printStackTrace();
	}
	return false;
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {
	DataFlavor[] transferFlavors = support.getDataFlavors();
	for (int i = 0; i < transferFlavors.length; i++) {
	    if (transferFlavors[i].
		equals(DataFlavor.javaFileListFlavor) ||
		transferFlavors[i].equals(uriListDataFlavor) ||
		transferFlavors[i].equals(plainTextStringDataFlavor) ||
		transferFlavors[i].equals(DataFlavor.stringFlavor)) {
		return true;
	    } 
	}
	return false;
    }
}

//  LocalWords:  TransferHandler boolean addURL setFile addFile pre
//  LocalWords:  setURL InputPane dndInput blockquote url cdir fnt
//  LocalWords:  FilenameTransferHandler ExtObjTransferHandler uri
//  LocalWords:  FilenameTransfer importData TransferSupport cmode
//  LocalWords:  getComponent instanceof getParent canImport http ufe
//  LocalWords:  currentDirectory setTransferHandler DataFlavor https
//  LocalWords:  setMultiEntryMode javaFileListFlavor currentDir ioe
//  LocalWords:  printStackTrace
