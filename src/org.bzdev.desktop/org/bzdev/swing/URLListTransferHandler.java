package org.bzdev.swing;

import java.awt.Component;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import javax.swing.JList;
import javax.swing.JComponent;
import javax.swing.DefaultListModel;
import javax.swing.TransferHandler;
import java.net.URL;
import java.io.File;
import java.io.IOException;

/**
 * TransferHandler for list of URLs.
 * Allows URLs and file names to be dragged into a JList, and
 * allows the JList to be reordered.
 * <P>
 * A typical use of this class is shown in the following example:
 * <blockquote><code><pre>
 *  JList jlist = ...;
 *  File currentDirectory = ...;
 *  TransferHandler th =
 *      new URLListTransferHandler(jlist, currentDirectory) {
 *          public void processURL(url) {
 *            // add the URL to some tables, but check for
 *            // an existing entry.  The drag and drop code
 *            // will add the URL to its JList, so processURL does
 *            // not have to modify the JList.
 *            ...
 *          }
 *          public void assertModified() {
 *            ...
 *          }
 *      };
 *  jlist.setTransferHandler(th);
 *  jlist.setDragEnabled(true);
 *  jlist.setDropMode(DropMode.INSERT);
 * </pre></code></blockquote>
 */
public class URLListTransferHandler extends TransferHandler {

    File icurrentDir;
    JList jlist;

    /**
     * Constructor.
     * @param jlist the JList for this transfer handler
     * @param currentDir the current working directory for resolving
     *        relative file names.
     */
    public URLListTransferHandler(JList jlist, File currentDir) {
	this.jlist = jlist;
	this.icurrentDir = currentDir;
    }

    /**
     * Assert that the JList's model has been modified.
     * Called if an element is added or removed from the JList's model.
     * By default, does nothing, but subclasses can override it.
     * Called once per drag and drop operation at the end of the data
     * import operation if an insertion or deletion was detected (including
     * in a reorder that managed to restore the previous list ordering).
     * <P>
     * This method is meant to provide an indication that a JList's
     * model has been modified programatically in cases where listeners are
     * overkill or merely inconvenient.
     */
    protected void assertModified() {}

    /**
     * Process a newly added URL.  
     * The default method does nothing, but can be overridden to respond
     * as an entry is inserted or moved.  Note that, if the list is reordered,
     * the same URL could be processed multiple times.
     * The associated JList should not be modified by this method as the
     * drag and drop implementation will add or move the entry to the JList
     * automatically.
     * @param url the URL
     */
    protected void processURL(URL url){}


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
		 "\"text/plain; class=java.lang.String\"");
	    System.exit(1);
	}
    }

    private int[] indices  = null;
    private int count = 0;

    // java 1.7 incompatibility with 1.6 (now jlist.getSelectedValuesList())
    @SuppressWarnings("deprecation")
    @Override
    protected Transferable createTransferable(JComponent c) {
	// System.out.println("createTransferable");
	if (c == jlist) {
	    // JList list = (JList)c;
	    indices = jlist.getSelectedIndices();
	    Object[] values = jlist.getSelectedValues();
        
	    StringBuffer buff = new StringBuffer();

	    for (int i = 0; i < values.length; i++) {
		Object val = values[i];
		buff.append(val == null ? "" : val.toString());
		if (i != values.length - 1) {
		    buff.append("\n");
		}
	    }
	    return new StringSelection(buff.toString());
	} else {
	    return null;
	}
    }

    @Override
    public int getSourceActions(JComponent c) {
	return  TransferHandler.COPY;
    }

    @Override
    public boolean importData(JComponent comp, Transferable t) {
	return importData(new TransferHandler.TransferSupport(comp, t));
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {
	DataFlavor[] transferFlavors = support.getDataFlavors();
	for (int i = 0; i < transferFlavors.length; i++) {
	    /*
	      System.out.println(i + " " + transferFlavors[i].toString());
	      System.out.println("   ..." +
	      transferFlavors[i].getPrimaryType()
	      +" " +
	      transferFlavors[i].getSubType());
	    */
	    if (transferFlavors[i].
		equals(DataFlavor.javaFileListFlavor) ||
		transferFlavors[i].equals(uriListDataFlavor) ||
		transferFlavors[i].equals(DataFlavor.stringFlavor) ||
		transferFlavors[i].equals(plainTextStringDataFlavor)) {
		return true;
	    } 
	}
	return false;
    }	    

    boolean modified = false;

    private int adjustIndexAndRemoveEntries(int index, 
					    DefaultListModel model) 
    {
	if (indices == null) return index; // nothing to do.

	for (int i = indices.length - 1; i >= 0; i--) {
	    modified = true;
	    model.remove(indices[i]);
	    if (indices[i] < index) index--;
	}
	indices = null;	// so we can't accidentally try this twice
	return index;
    }

    // java 1.7 incompatibility with 1.6 (listModel in 1.7 uses type parameters)
    @SuppressWarnings("unchecked")
    @Override
    public boolean importData(TransferHandler.TransferSupport support) {
	// System.out.println("importData");
	Component comp = support.getComponent();
	JList xjlist;
	DefaultListModel listModel;
	int index;
	boolean insertFlag;
	boolean listmode = (comp instanceof JList);
	modified = false;
	if (listmode) {
	    xjlist = (JList)comp;
	    listModel = (DefaultListModel)jlist.getModel();
	    if (support.isDrop()) {
		JList.DropLocation droploc;
		droploc = (JList.DropLocation)(support.getDropLocation());
		index = droploc.getIndex();
	    } else {
		index = -1;
	    }
	    // droploc is always configure to insert
	} else {
	    xjlist = jlist;
	    listModel = (DefaultListModel)xjlist.getModel();
	    index = -1;
	}
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
	try {
	    if (mode == 0) {
		java.util.List flist = (java.util.List)
		    t.getTransferData(DataFlavor.javaFileListFlavor);
		if (listmode) {
		    count = flist.size();
		    index = adjustIndexAndRemoveEntries(index,
							listModel);

		}
		for (Object obj: flist) {
		    File f = (File)obj;
		    URL url;
		    try {
			url = f.toURI().toURL();
		    } catch (Exception e){
			url = null;
		    }
		    modified = true;
		    if (listmode) {
			listModel.add(index++, url.toString());
		    } else {
			listModel.addElement(url.toString());
		    }
		    processURL(url);
		}
	    } else if (mode == 1) {
		String[] strings = 
		    ((String) 
		     t.getTransferData(uriListDataFlavor)).trim().split("\\s+");
		if (listmode) {
		    count = strings.length;
		    index = adjustIndexAndRemoveEntries(index,
							listModel);
		}
		for (String s: strings) {
		    URL url;
		    try {
			url = new URL(s);
		    } catch (Exception e) {
			url = null;
		    }
		    if (url != null) {
			modified = true;
			if (listmode) {
			    listModel.add(index++, url.toString());
			} else {
			    listModel.addElement(url.toString());
			}
			processURL(url);
		    }
		}
	    } else /* if (mode == 2 || mode == 3) */ {
		String[] strings = 
		    ((String) 
		     t.getTransferData((mode == 2)?
				       plainTextStringDataFlavor:
				       DataFlavor.stringFlavor))
		    .split("(\\r\\n|\\n)+");
		if (listmode) {
		    count = strings.length;
		    index = adjustIndexAndRemoveEntries(index,
							listModel);
		}
		for (String s: strings) {
		    if (s.length() == 0) continue;
		    boolean isURL = false;
		    if (s.startsWith("file:") || s.startsWith("ftp:")
			|| s.startsWith("http:")
			|| s.startsWith("https:")) {
			isURL = true;
		    }
		    if (isURL) {
			URL url;
			try {
			    url = new URL(s.trim());
			} catch (Exception e) {
			    url = null;
			}
			if (url != null) {
			    modified = true;
			    if (listmode) {
				listModel.add(index++, url.toString());
			    } else {
				listModel.addElement(url.toString());
			    }
			    processURL(url);
			}
		    } else {
			File f = new File(s);
			if (!f.isAbsolute()) {
			    f = new File(icurrentDir, s);
			}
			URL u;
			String url;
			try {
			    u = f.toURI().toURL();
			    url = u.toString();
			} catch (Exception e) {
			    url = null;
			    u = null;
			}
			if (url != null) {
			    modified = true;
			    if (listmode) {
				listModel.add(index++, u.toString());
			    } else {
				listModel.addElement(u.toString());
			    }
			}
			processURL(u);
		    }
		}
	    }
	    // if (modified) assertModified();
	    return true;
	} catch (UnsupportedFlavorException ufe) {
	    SwingErrorMessage.display(ufe);
	    // ufe.printStackTrace();
	} catch (IOException ioe) {
	    SwingErrorMessage.display(ioe);
	    // ioe.printStackTrace();
	} finally {
	    if (modified) assertModified();
	}
	return false;
    }

    @Override
    protected void exportDone(JComponent src,
			      Transferable data, 
			      int action)
    {
	/*
	 * We do not support a MOVE operation but will
	 * allow the list to be reordered (see above).
	 */
	indices = null;
	count = 0;
    }
}

//  LocalWords:  TransferHandler JList blockquote pre jlist th url
//  LocalWords:  currentDirectory URLListTransferHandler processURL
//  LocalWords:  assertModified setTransferHandler setDragEnabled uri
//  LocalWords:  currentDir JList's subclasses programatically http
//  LocalWords:  getSelectedValuesList createTransferable toString
//  LocalWords:  transferFlavors getPrimaryType getSubType listModel
//  LocalWords:  importData droploc https ufe printStackTrace ioe
