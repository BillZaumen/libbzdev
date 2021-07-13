import org.bzdev.swing.*;
import java.lang.reflect.UndeclaredThrowableException;
import java.lang.reflect.InvocationTargetException;

import javax.swing.*;
import javax.swing.text.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

public class SimpleConsoleTest2 {
    // static SimpleConsole tc;
    static JTextPane tc;
    static MutableAttributeSet aset = StyleContext.getDefaultStyleContext().
	getStyle(StyleContext.DEFAULT_STYLE);
    static Document doc;
    static int pos;
    static BadLocationException ble = null;

    abstract static class RunnableWithBadLocException implements Runnable {
	RuntimeException rexception;
	BadLocationException exception;
	/**
	 * The code the runnable will execute.
	 */
	abstract protected void doitBLE() throws BadLocationException;
	public void run() {
	    try {
		doitBLE();
	    } catch (BadLocationException  e) {
		exception = e; 
	    } catch (RuntimeException re) {
		rexception = re;
	    }
	}
    }

    static void doitVoidBLE(RunnableWithBadLocException r) 
	throws BadLocationException
    {
	try {
	    SwingUtilities.invokeAndWait(r);
	    if (r.exception != null) throw r.exception;
	    if (r.rexception != null) throw r.rexception;
	    return;
	} catch (InterruptedException ie) {
	    throw new RuntimeException(ie);
	} catch (InvocationTargetException ite) {
	    Throwable thr = ite.getCause();
	    if (thr instanceof RuntimeException)
		throw (RuntimeException) thr;
	    else
		throw new RuntimeException(thr);
	}
    }


    public static void insertString(int offset, String str, AttributeSet a)
	throws BadLocationException {
	if (SwingUtilities.isEventDispatchThread()) {
	    doc.insertString(offset, str, a);
	} else {
	    final int xoffset = offset;
	    final String xstr = str;
	    final AttributeSet xa = a;
	    doitVoidBLE(new RunnableWithBadLocException() {
		    protected void doitBLE() throws BadLocationException {
			doc.insertString(xoffset, xstr, xa);
		    }
		});
	}
    }

    abstract static class RunnableWithInt implements Runnable {
	RuntimeException rexception;
	int intval;
	abstract protected void doit();
	public void run(){
	    try {
		doit();
	    }  catch (RuntimeException re) {
		rexception = re;
	    }
	}
    }

    static int doitInt(RunnableWithInt r) {
	try {
	    SwingUtilities.invokeAndWait(r);
	    if (r.rexception != null) throw r.rexception;
	    return r.intval;
	} catch (InterruptedException ie) {
	    throw new RuntimeException(ie);
	} catch (InvocationTargetException ite) {
	    Throwable thr = ite.getCause();
	    if (thr instanceof RuntimeException)
		throw (RuntimeException) thr;
	    else
		throw new RuntimeException(thr);
	}
    }

    public static int getLength() {
	if (SwingUtilities.isEventDispatchThread()) {
	    return doc.getLength();
	} else {
	    return doitInt(new RunnableWithInt() {
		    protected void doit() {
			intval = doc.getLength();
		    }
		});
	}
    }

    public static synchronized void append(CharSequence csq) {

	String str = csq.toString();
	try {
	    doc.insertString(doc.getLength(), str, aset);
	} catch (BadLocationException e) {
	    System.err.println("bad location");
	    System.exit(1);
	}
    }

    public static void main(String argv[]) throws Exception {
	try {
	    SwingUtilities.invokeAndWait(() -> {
		    // tc = new SimpleConsole();
		    tc = new JTextPane();
		    StyleConstants.setBackground(aset, tc.getBackground());
		    StyleConstants.setForeground(aset, tc.getForeground());
		    doc = tc.getDocument();
		    JFrame frame = new JFrame("TextPane Test");
		    Container fpane = frame.getContentPane();
		    frame.addWindowListener(new WindowAdapter () {
			    public void windowClosing(WindowEvent e) {
				System.exit(0);
			    }
			});
		    
		    frame.setSize(700,400);
		    JScrollPane scrollpane = new JScrollPane(tc);
		    fpane.setLayout(new BorderLayout());

		    fpane.add("Center", scrollpane);
		    // fpane.setVisible(true);
		    frame.setVisible(true);
		});

	    System.setSecurityManager(new SecurityManager());
	    System.out.println("security manager installed");

	    /*
	    SwingUtilities.invokeAndWait(() -> {
		    // System.out.println("running on event dispatch thread");
		});
		System.out.println("ran a print stmt on the event dispatch thread");
	    */

	    // tc.append("start\n");

	    pos = getLength();


	    doc.insertString(pos, "start\n", aset);

	    /*
	    if (SwingUtilities.isEventDispatchThread()) {
		try {
		    doc.insertString(pos, "start\n", aset);
		} catch (BadLocationException e) {
		    ble = e;
		}
	    } else {
		ble = null;
		SwingUtilities.invokeAndWait(() -> {
			try {
			    doc.insertString(pos, "start\n", aset);
			} catch (BadLocationException e) {
			    ble = e;
			}
		    });
		if (ble != null) throw ble;
	    }
	    */

	    System.out.println("start printed");

	    Thread.currentThread().sleep(10000);
	    System.exit(0);

	} catch (Exception e) {
            e.printStackTrace();
	    System.exit(1);
        }
    }
}
