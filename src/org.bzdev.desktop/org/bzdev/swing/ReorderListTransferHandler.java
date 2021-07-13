package org.bzdev.swing;
import javax.swing.*;
import java.awt.Component;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import org.bzdev.util.ErrorMessage;

/**
 * JList TransferHandler supporting reordering.
 * This transfer handler allows a JList to be reordered using
 * drag and drop operations. It does not allow D&amp;D to be used
 * to add or remove elements from a list.  One use case this transfer
 * handler supports is a component that contains a JList, a button
 * to add new entries to the JList and name them, and some additional
 * parameters.  The use of this transfer handler then allows the JList
 * to be reordered conveniently.
 * <P>
 * Typically, a list using this transfer handler has a scroll pane
 * as a parent.  In the following, we assume that the list contains
 * elements whose type is <code>OurType</code> and that the class
 * <code>OurType</code> has a <code>toString()</code> method that will
 * produce a string whose length is the largest string length that should
 * appear in a JList for <code>OurType</code>.  The code is straightforward:
 * <blockquote><code><pre>
 *  DefaultListModel&lt;OurType&gt; listModel = new DefaultListModel&lt;OurType&gt;();
 *  JList&lt;OurType&gt; list = new JList&lt;OurType&gt;(listModel);
 *  JScrollPane scrollPane = new JScrollPane(list);
 *  TransferHandler th = new ReorderListTransferHandler(list);
 *  list.setPrototypeCellValue(new OurType(...));
 *  list.setTranferHandler(th);
 *  list.setDragEnabled(true);
 *  list.setDropMode(DropMode.INSERT);
 * </pre></code></blockquote>
 * It is basically the default sequence of operations for enabling drag and
 * drop for a JList.
 */
public class ReorderListTransferHandler extends TransferHandler {

    static DataFlavor localObjectFlavor;
    static {
        try {
            localObjectFlavor =
                new DataFlavor (DataFlavor.javaJVMLocalObjectMimeType);
        } catch (ClassNotFoundException cnfe) { cnfe.printStackTrace(); } 
    } 
    static final DataFlavor[] ourFlavors = {localObjectFlavor};

    private int[] indices  = null;
    private int count = 0;
    
    private JList jlist;

    /**
     * Constructor.
     * @param jlist a jlist using a DefaultListModel
     */
    public ReorderListTransferHandler(JList jlist) {
	this.jlist = jlist;
    }

    // java 1.7 incompabibility with 1.6  (now jlist.getSelectedValuesList())
    @SuppressWarnings("deprecation")
    @Override
    protected Transferable createTransferable(JComponent c) {
	// System.out.println("createTransferable");
	if (c == jlist) {
	    indices = jlist.getSelectedIndices();
	    final Object[] values = jlist.getSelectedValues();
        
	    StringBuffer buff = new StringBuffer();

	    for (int i = 0; i < values.length; i++) {
		Object val = values[i];
		buff.append(val == null ? "" : val.toString());
		if (i != values.length - 1) {
		    buff.append("\n");
		}
	    }
	    return new Transferable() {
		Object data = values;
		public Object getTransferData(DataFlavor flavor) {
		    if (flavor != localObjectFlavor) return null;
		    return data;
		}
		public DataFlavor[] getTransferDataFlavors() {
		    return ourFlavors;
		}
		public boolean isDataFlavorSupported(DataFlavor flavor)
		{
		    return flavor == localObjectFlavor;
		}
	    };
	    // return new StringSelection(buff.toString());
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
	    if (transferFlavors[i] == localObjectFlavor) return true;
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
	JList.DropLocation droploc;
	int index;
	boolean insertFlag;
	boolean listmode = (comp instanceof JList);
	if (listmode) {
	    xjlist = (JList)comp;
	    listModel = (DefaultListModel)xjlist.getModel();
	    if (support.isDrop()) {
		droploc = (JList.DropLocation)(support.getDropLocation());
		index = droploc.getIndex();
		// droploc is always configure to insert
	    } else {
		index = -1;
	    }
	} else {
	    xjlist = jlist;
	    listModel = (DefaultListModel)xjlist.getModel();
	    index = -1;
	}
	Transferable t = support.getTransferable();
	if (!t.isDataFlavorSupported(localObjectFlavor)) {
	    return false;
	}
	// System.out.println("mode = " + mode);
	try {
	    Object[] objects = (Object[])
		t.getTransferData(localObjectFlavor);
	    if (listmode) {
		count = objects.length;
		index = adjustIndexAndRemoveEntries(index,
						    listModel);
	    }
	    for (Object obj: objects) {
		if (listmode) {
		    listModel.add(index++, obj);
		} else {
		    listModel.addElement(obj);
		}
	    }
	    return true;
	} catch (UnsupportedFlavorException ufe) {
	    ErrorMessage.display(ufe);
	    // ufe.printStackTrace();
	} catch (java.io.IOException ioe) {
	    ErrorMessage.display(ioe);
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

//  LocalWords:  JList TransferHandler OurType toString blockquote lt
//  LocalWords:  pre DefaultListModel listModel JScrollPane th jlist
//  LocalWords:  scrollPane ReorderListTransferHandler setDragEnabled
//  LocalWords:  setPrototypeCellValue setTranferHandler getSubType
//  LocalWords:  incompabibility getSelectedValuesList getPrimaryType
//  LocalWords:  createTransferable StringSelection transferFlavors
//  LocalWords:  importData droploc ufe printStackTrace
