package org.bzdev.swing;

import java.io.File;
import java.io.IOException;
import java.io.Flushable;
import java.nio.CharBuffer;
import java.net.URL;
import java.net.MalformedURLException;
import java.awt.*;
import java.awt.event.InputEvent;
import javax.swing.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

//@exbundle org.bzdev.swing.lpack.Swing

/**
 * TransferHandler that lists data flavors and objects being transferred.
 * It outputs information about the transfer to an Appendable (a common
 * interface implemented by a number of classes, including PrintStream,
 * PrintWriter, CharBuffer, Writer, StringBuilder, and StringBuffer.  This 
 * class should be used only for debugging and not as part of a released
 * application.  It is intended for cases in which one would like to trace
 * the operation of a TransferHandler when the source code for the 
 * TransferHandler is not available or cannot be modified for some reason.
 * For example, given a JComponent <code>c</code>, one could call
 * <pre><code>
 * c.setTransferHandler(new DebugTransferHandler(c.getTransferHandler(),
 *                                               System.out));
 * </code></pre>
 * to trace a drag and drop operation, showing the data flavors that are
 * available and their values. In some cases, the data flavor used is
 * system dependent and may change from release to release, so being able
 * to add tracing of drag and drop operations quickly is advantageous,
 * and some of the data flavors used in practice are not mentioned in the
 * Java documentation (for example, some if not all versions of the Nautilus
 * file manager on Linux/Gnome systems will use lists of URLs when dragging
 * and dropping files).
 * <p>
 * The class is a wrapper for an existing TransferHandler, with a
 * default behavior if the wrapped TransferHandler is null.
 * <p>
 */
public class DebugTransferHandler extends TransferHandler {

    static String errorMsg(String key, Object... args) {
	return SwingErrorMsg.errorMsg(key, args);
    }

    private Appendable output;
    private TransferHandler handler = null;

    private void append(String string) {
	try {
	    output.append(string);
	} catch (IOException eio) {}
    }

    private void flush() {
	try {
	    if (output instanceof Flushable) {
		((Flushable)output).flush();
	    }
	} catch (IOException eio) {}
    }

    /**
     * Constructor.
     * @param out the output character stream or object
     */
    public DebugTransferHandler(Appendable out) {
	output = out;
    }

    /**
     * Constructor wrapping an existing TransferHandler.
     * @param handler the transfer handler to wrap
     * @param out the output character stream, writer, or object
     */
    public DebugTransferHandler(TransferHandler handler, Appendable out) {
	output = out;
	this.handler = handler;
    }

    /*
      // not needed because handler will call its own method without
      // our methods having to do anything.
    private Transferable ourCreateTransferable(final JComponent c) {
	Method method =  AccessController.doPrivileged
	    (new PrivilegedAction<Method>() {
		public Method run() {
		    Class<?> clazz = handler.getClass();
		    try {
			Method m = clazz.getDeclaredMethod
			    ("createTransferable", JComponent.class);
			m.setAccessible(true);
			return m;
		    } catch (Throwable t) {
			String msg = errorMsg("reflectionFailed");
			throw new Error(msg, t);
		    }
		}
	    });
	try {
	    Object result = method.invoke(handler, c);
	    return Transferable.class.cast(result);
	} catch (RuntimeException e) {
	    throw e;
	} catch (Exception ee) {
	    String msg = errorMsg("createTransferableFailed");
	    throw new Error(msg, ee);
	}
    }
    */

    @Override
    protected Transferable createTransferable(JComponent c) {
	return (handler == null)? super.createTransferable(c): null;
	    // ourCreateTransferable(c);
    }

    @Override
    public void exportAsDrag(JComponent source, InputEvent e, int action) {
	if (handler == null) {
	    super.exportAsDrag(source, e, action);
	} else {
	    handler.exportAsDrag(source, e, action);
	}
    }

    @Override
    public void exportToClipboard(JComponent comp,
				  Clipboard clip,
				  int action) {
	if (handler == null) {
	    super.exportToClipboard(comp, clip, action);
	} else {
	    handler.exportToClipboard(comp, clip, action);
	}
    }

    @Override
    public int getSourceActions(JComponent c) {
	return (handler == null)? super.getSourceActions(c):
	    handler.getSourceActions(c);
    }

    @Override
    public Icon getVisualRepresentation(Transferable t) {
	return (handler == null)? super.getVisualRepresentation(t):
	    handler.getVisualRepresentation(t);
    }

    @Override
    public boolean importData(JComponent comp, Transferable t) {
	return importData(new TransferHandler.TransferSupport(comp, t));
    }

    private void printAsNeeded(TransferHandler.TransferSupport support) {
	DataFlavor[] transferFlavors = support.getDataFlavors();
	Transferable t = support.getTransferable();
	append("-------------------\n");
	append("Data Flavors:\n");
	for (int i = 0; i < transferFlavors.length; i++) {
	    append("   " + transferFlavors[i].toString()
		   + "\n        type =  " 
		   + transferFlavors[i].getPrimaryType()
		   + "\n        subtype = " 
		   + transferFlavors[i].getSubType()
		   + "\n        representationClass = "
		   + transferFlavors[i].getRepresentationClass().toString()
		   + "\n        MIME type = "
		   + transferFlavors[i].getMimeType());
	    if (support.isDrop()) {
		try {
		    Object objects = t.getTransferData(transferFlavors[i]);
		    append("\n        transfer data class = "
			   + objects.getClass());
		    if (objects instanceof java.util.List) {
			append("\nList =");
			for (Object obj: (java.util.List)objects) {
			    append("\n    " +  obj.toString());
			}
		    } else if (objects instanceof String) {
			append("\nString = \"" + (String) objects
			       + "\"");
		    } else if (objects instanceof CharBuffer) {
			append("\nCharBuffer = \"" 
			       + ((CharBuffer)objects).toString()
			       +"\"");
		    }
		} catch (IOException ioe) {
		append("\n        getTranferData failure (IO Error) - "
			      + ioe.getMessage());

		} catch (UnsupportedFlavorException ufe) {
		    append("\n        getTranferData failure (unsupported flavor)"
			   + " - " + ufe.getMessage());
		}
	    }
	    append("\n\n");
	    flush();
	}
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport support) {
	printAsNeeded(support);
	return (handler == null)? false: handler.importData(support);
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {
	return (handler == null)? true: handler.canImport(support);
    }

    /*
      // not needed because handler will call its own method without
      // our methods having to do anything.
    private void ourExportDone(final JComponent source,
			       final Transferable data,
			       final int action) {
	Method method = AccessController.doPrivileged
	    (new PrivilegedAction<Method>() {
		public Method run() {
		    Class<?> clazz = handler.getClass();
		    try {
			Method m = clazz.getDeclaredMethod
			    ("exportDone", JComponent.class, Transferable.class,
			     int.class);
			m.setAccessible(true);
			return m;
		    } catch (Throwable t) {
			String msg = errorMsg("reflectionFailed");
			throw new Error(msg, t);
		    }
		}
	    });
	try {
	    method.invoke(handler, source, data, action);
	} catch (RuntimeException e) {
	    throw e;
	} catch (Exception ee) {
	    String msg = errorMsg("exportDoneFailed");
	    throw new Error (msg, ee);
	}
    }
    */

    @Override
    protected void exportDone(JComponent source, Transferable data, int action)
    {
	if (handler == null) {
	    super.exportDone(source, data, action);
	}/* else {
	    ourExportDone(source, data, action);
	} 
	*/
    } 
}

//  LocalWords:  exbundle TransferHandler Appendable PrintStream pre
//  LocalWords:  PrintWriter CharBuffer StringBuilder StringBuffer ee
//  LocalWords:  JComponent setTransferHandler DebugTransferHandler
//  LocalWords:  getTransferHandler ourCreateTransferable clazz msg
//  LocalWords:  AccessController doPrivileged PrivilegedAction nList
//  LocalWords:  getClass getDeclaredMethod createTransferable
//  LocalWords:  setAccessible Throwable errorMsg reflectionFailed
//  LocalWords:  RuntimeException createTransferableFailed subtype
//  LocalWords:  representationClass nString nCharBuffer exportDone
//  LocalWords:  getTranferData ourExportDone exportDoneFailed
