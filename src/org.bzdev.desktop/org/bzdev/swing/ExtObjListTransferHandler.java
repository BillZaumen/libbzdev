package org.bzdev.swing;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import javax.swing.*;
import org.bzdev.util.ErrorMessage;


/**
 * Transfer handler for JLists of objects reference by URLs or file names.
 * This transfer handler supports objects represented externally by
 * A file name or URL.  Subclasses must implement the method
 * <code>insertByURL</code>.  This is used when objects are loaded
 * from an external source.  If the list is being reordered, the
 * objects in the list are not modified and <code>insertByURL</code> is not
 * called.
 * <P>
 * In one use case, the objects will be subclasses of ImageIcon, which
 * a JList can display graphically, with each object containing additional
 * methods that the application needs.  For example, the ImageIcon might
 * contain a scaled "thumbnail" image of a larger image file, and store the
 * original file's URL for later processing.
 * <P>
 * The following code is an example of how to set up a JList that
 * supports reordering and displays icon images obtained from a URL or
 * file name (the class OurImageIcon is a subclass of ImageIcon that
 * provides an icon and some additional data):
 * <blockquote><pre><code>
 *  DefaultListModel&lt;OurImageIcon&gt; listModel =
 *      new DefaultListModel&lt;OurImageIcon&gt;();
 *  JList&lt;OurImageIcon&gt; list = new JList&lt;OurImageIcon&gt;(listModel);
 *  JScrollPane scrollPane = new JScrollPane(list);
 *  File dir = ...;
 *  TransferHandler th = new ExtObjListTransferHandler(list, dir) {
 *          public void insertByURL(URL url,
 *                                  DefaultListModel model,
 *                                  int index)
 *          {
 *            // Create OurIconImage
 *                OurIconImage image = ... ;
 *            // Make sure the image is fully loaded.
 *            ...
 *            // Add the image to the model at the specified index, or at
 *            // the end if no index was specified.
 *             if (index == -1) {
 *                model.addElement(image);
 *             } else {
 *               model.addElement(index, image);
 *             }
 *	    }
 *       };
 *  list.setTranferHandler(th);
 *  list.setDragEnabled(true);
 *  list.setDropMode(DropMode.INSERT);
 *  DefaultListCellRenderer lcr = (DefaultListCellRenderer)
 *       list.getCellRenderer();
 *  lcr.setHorizontalAlignment(SwingConstants.CENTER);
 *  Dimension d = scrollPane.getHorizontalScrollBar().getPreferredSize();
 *  int h = d.height;
 *  d = scrollPane.getVerticalScrollBar().getPreferredSize();
 *  int w = d.width;
 *  int iconWidth = ...;
 *  int iconHeight = ...;
 *  list.setFixedCellHeight(iconHeight + h);
 *  rlist.setFixedCellWidth(iconWidth + w);
 *  rlist.setLayoutOrientation(JList.VERTICAL);
 *  rlist.setVisibleRowCount(5);
 * </CODE></PRE></blockquote>
 */
abstract public class ExtObjListTransferHandler extends TransferHandler {
    private int[] indices  = null;
    private int count = 0;

    // InputPane.SelectionMode smode;
    private boolean multiEntryMode = true;
    private boolean cmode = false;

    private File icurrentDir;

    private JList jlist;

    /**
     * Constructor.
     * @param jlist the JList that will be the drop target.
     * @param currentDir the current working directory to use for
     *        relative URLs or file names
     */
    public ExtObjListTransferHandler(JList jlist, File currentDir) {
	this.jlist = jlist;
	this.icurrentDir = currentDir;
    }

    /**
     * Insert an object given its URL.
     * The implementation is responsible for creating the object given its
     * URL, and the object must be one that the JList can display. The
     * default JList implementation can handle objects of type
     * String and ImageIcon.
     * After an object OBJ is created, the implementation must call 
     * model.addElement(OBJ) when index is -1; otherwise it must call 
     * model.add(index, OBJ). Some implementations will create an object
     * that must later be modified to be displayed correctly (e.g., a 'blank'
     * image until the actual image has been loaded), with the object modified
     * after it was added to the list model.  The modification would typically
     * be done in a separate thread.
     * In this case, as a final step, the implementation may call
     * model.indexOf(OBJ) to get the actual index INDEX and then call
     * model.setElementAt(INDEX, OBJ), which will automatically force the
     * JList to be repainted so that the modified object is displayed
     * correctly.
     * @param url the image's URL
     * @param model the list model for the JList in which the image will appear
     * @param index the index in the list model before which the image will be
     *        inserted; -1 if the entry is to be added to the end of the list
     * @exception Exception an error occurred
     */
    abstract protected void insertByURL(URL url,
					DefaultListModel model,
					int index)
	throws Exception;


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
     * Set multi-selection mode and replacement option.
     * The behavior depends on the combination of arguments provided:
     * <TABLE BORDER="1">
     * <caption> &nbsp;</caption>
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
	    DefaultListModel listModel = (DefaultListModel)jlist.getModel();
	    for (int i = listModel.size()-1; i > 0; i--) {
		listModel.remove(i);
	    }
	}
	multiEntryMode = mode;
	this.cmode = cmode;
    }

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

    static DataFlavor localObjectDataFlavor;
    static {
        try {
            localObjectDataFlavor =
                new DataFlavor (DataFlavor.javaJVMLocalObjectMimeType);
        } catch (ClassNotFoundException cnfe) { cnfe.printStackTrace(); } 
    } 
    static DataFlavor[] supportedFlavors = { localObjectDataFlavor }; 

    static class ObjTransferable implements Transferable {
	Object object; 
	public ObjTransferable (Object[] oarray) {
	    object = oarray;
	}
	public Object getTransferData(DataFlavor df)
	    throws UnsupportedFlavorException, IOException {
	    if (isDataFlavorSupported (df))
		return object;
	    else
		throw new UnsupportedFlavorException(df);
	}
	public boolean isDataFlavorSupported (DataFlavor df) {
	    return (df.equals(localObjectDataFlavor));
	}
	public DataFlavor[] getTransferDataFlavors () {
	    return supportedFlavors; } 
    }

    // java 1.7 incompatibility with 1.6 (now jlist.getSelectedValuesList())
    @SuppressWarnings("deprecation")
    @Override
    protected Transferable createTransferable(JComponent c) {
	// System.out.println("createTransferable");
	if (c == jlist) {
	    // JList list = (JList)c;
	    indices = jlist.getSelectedIndices();
	    Object[] values = jlist.getSelectedValues();
	    return new ObjTransferable(values);
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
		transferFlavors[i].equals(plainTextStringDataFlavor) ||
		transferFlavors[i].equals(localObjectDataFlavor)) {
		return true;
	    } 
	}
	return false;
    }	    

    private int adjustIndexAndRemoveEntries(int index, 
					    DefaultListModel model) 
    {
	if (indices == null) return index; // nothing to do.

	for (int i = indices.length - 1; i >= 0; i--) {
	    model.remove(indices[i]);
	    if (indices[i] < index) index--;
	}
	indices = null;	// so we can't accidentally try this twice
	return index;
    }


    // java 1.7 incompabibility with 1.6 (listModel in 1.7 uses type parameters)
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
	int lower = -1;
	int upper = -1;
	if (listmode) {
	    xjlist = (JList)comp;
	    listModel = (DefaultListModel)xjlist.getModel();
	    if (support.isDrop()) {
		// droploc is always configure to insert
		JList.DropLocation droploc = 
		    (JList.DropLocation)(support.getDropLocation());
		index = droploc.getIndex();
	    } else {
		index = -1;
	    }
	} else {
	    xjlist = jlist /*parent.getImageList()*/;
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
	} else if (t.isDataFlavorSupported(localObjectDataFlavor)) {
	    mode = 4;
	} else {
	    return false;
	}
	// System.out.println("mode = " + mode);
	try {
	    if (mode == 0) {
		java.util.List flist = (java.util.List)
		    t.getTransferData(DataFlavor.javaFileListFlavor);
		if (listmode) {
		    count = flist.size();
		    index = adjustIndexAndRemoveEntries(index,
							listModel);

		}
		if (multiEntryMode == false) {
		    /*
		    listModel.clear();
		    index = -1;
		    */
		    lower = (index == -1)? listModel.getSize() : index;
		    upper = lower + (cmode? 1: flist.size());
		}
		for (Object obj: flist) {
		    File f = (File)obj;
		    URL url;
		    try {
			url = f.toURI().toURL();
		    } catch (Exception e){
			url = null;
		    }
		    if (url != null) {
			insertByURL(url, listModel, index++);
		    }
		    if ((!multiEntryMode) && cmode) break;
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
		if (multiEntryMode == false) {
		    lower = (index == -1)? listModel.getSize() : index;
		    upper = lower + (cmode? 1: strings.length);
		}
		for (String s: strings) {
		    URL url;
		    try {
			url = new URL(s);
		    } catch (Exception e) {
			url = null;
		    }
		    if (url != null) {
			insertByURL(url, listModel, index++);
		    }
		    if ((!multiEntryMode) && cmode) break;
		}
	    } else if (mode == 2 || mode == 3) {
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
		if (multiEntryMode == false) {
		    /*
		    listModel.clear();
		    index = -1;
		    */
		    lower = (index == -1)? listModel.getSize() : index;
		    upper = lower + (cmode? 1: strings.length);
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
			    insertByURL(url, listModel, index++);
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
			    insertByURL(u, listModel, index++);
			}
		    }
		    if ((!multiEntryMode) && cmode) break;
		}
	    } else /* if (mode == 4) */ {
		Object[] array = (Object [])
		    t.getTransferData(localObjectDataFlavor);
		if (listmode) {
		    count = array.length;
		    index = adjustIndexAndRemoveEntries(index, listModel);
		}
		for (Object obj: array) {
		    if (listmode) {
			listModel.add(index++, obj);
		    } else {
			listModel.addElement(obj);
		    }
		}
	    }
	    // delay cleaning up to avoid throwing null pointer exceptions
	    // from d&d code.
	    if (multiEntryMode == false && lower >= 0 && upper >= 0 ) {
		final int xlower = lower;
		final int xupper = upper;
		final DefaultListModel model = listModel;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
			    int i;
			    int len = model.getSize();
			    int upper = xupper;
			    int lower = xlower;
			    for (i = upper; i < len; len--) {
				model.remove(upper);
			    }
			    while (lower > 0) {
				model.remove(0);
				lower--;
			    }
			}
		    });
	    }
	    return true;
	} catch (Exception e) {
	    ErrorMessage.display(e);
	    // e.printStackTrace();
	}
	return false;
    }

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

//  LocalWords:  JLists Subclasses insertByURL subclasses ImageIcon
//  LocalWords:  JList OurImageIcon blockquote pre DefaultListModel
//  LocalWords:  lt listModel JScrollPane scrollPane dir th url lcr
//  LocalWords:  TransferHandler ExtObjListTransferHandler addElement
//  LocalWords:  OurIconImage setTranferHandler setDragEnabled rlist
//  LocalWords:  DefaultListCellRenderer getCellRenderer iconWidth
//  LocalWords:  iconHeight setFixedCellHeight setFixedCellWidth uri
//  LocalWords:  setVisibleRowCount InputPane SelectionMode smode
//  LocalWords:  jlist currentDir indexOf setElementAt boolean cmode
//  LocalWords:  setMultiEntryMode getSelectedValuesList toString
//  LocalWords:  createTransferable transferFlavors getPrimaryType
//  LocalWords:  getSubType incompabibility importData droploc http
//  LocalWords:  getImageList https printStackTrace
