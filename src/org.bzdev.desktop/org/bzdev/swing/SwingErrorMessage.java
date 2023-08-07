package org.bzdev.swing;

import java.util.*;
import java.io.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.text.MessageFormat;
import org.bzdev.util.ErrorMsgOps;
import org.bzdev.util.ErrorMessage;

/**
 * Error-Message Handling with GUI support.
 * This class handles the display of error messages. It is intended
 * for applications that may generate multiple errors or warnings
 * and that might run with or without a GUI.
 * <P>
 * When a GUI is available, methods support cases where one would
 * use a dialog box, which requires user-interaction after each
 * messages, or a console window to display messages, in which case
 * messages just accumulate in the window. Dialog boxes need a component
 * on which to center themselves, and a default can be specified.
 * <P>
 * If nothing is configured, the messages by default are printed on
 * System.err. Where the messages are sent can be modified by calling
 * {@link ErrorMessage#setAppendable(Appendable)}.  For methods that have
 * a title as an argument and that do have an argument representing a
 * Swing component, the behavior depends on whether setComponent was
 * called.  If {@link SwingErrorMessage#setComponent(Component)} was not
 * called, the message appears on the designated Appendable (which by
 * default is System.err). If {@link SwingErrorMessage#setComponent(Component)}
 * has been called, including with a null argument, the message will
 * be shown in a dialog box.
 * <P>
 * For methods that cause a message to be displayed the order of arguments
 * are:
 * <OL>
 *   <LI> a {@link Component} providing a component indicating
 *        where a dialog box should be centered. If null, the dialog
 *        box will appear in the center of the screen.
 *   <LI> a {@link String} giving a title for a dialog box. A dialog box
 *        will be provided only if
 *        {@link SwingErrorMessage#setComponent(Component)} has been called,
 *        including with a null argument.
 *   <LI> a {@link Locale} giving the locale to use (null implies the
 *        default locale.
 *   <LI> a {@link String} containing format directives or the message itself
 *   <LI> a variable number of {@link Object}s providing arguments specified by
 *        a format string.
 * </OL> 
 * Depending on the method, some of these may be missing.  The order is
 * different from that used by {@link JOptionPane}. The rationale for a
 * different order is to make any format string used appear directly before
 * its arguments and to keep the order of arguments consistent among methods
 * defined by this class.
 * <P>
 * While the method {@link ErrorMessage#setStackTrace(boolean)} can be used
 * to programatically turn the display of stack traces on and off for
 * Those "display" methods that take a Throwable as an argument, one may
 * wish to control this behavior from a GUI.  The class
1 * {@link StackTraceMenuItem} will an appropriate menu item.
 * Error messages will often be directed to a console. The documentation
 * for {@link SimpleConsole} explains how do do this
 * @see SimpleConsole
 * @see StackTraceMenuItem
 */

public class SwingErrorMessage extends ErrorMessage {

    private static final ErrorMsgOps ops = new ErrorMsgOps() {
	    @Override
	    public void addSeparatorIfNeeded() {
		SwingErrorMessage.addSeparatorIfNeededAux();
	    }

	    @Override
	    public void addSeparator() {
		SwingErrorMessage.addSeparatorAux();
	    }

	    @Override
	    public void setStackTrace(boolean value) {
		ErrorMessage.setStackTraceAux(value);
	    }

	    @Override
	    public boolean stackTraceEnabled() {
		return ErrorMessage.stackTraceEnabledAux();
	    }

	    @Override
	    public void setAppendable(Appendable out) {
		ErrorMessage.setAppendableAux(out);
	    }
	    

	    @Override
	    public void display(String fn, int lineno, String msg) {
		ErrorMessage.displayAux(fn, lineno, msg);
	    }

	    @Override
	    public void display(String fn,
				String input, int index,
				Exception e,
				boolean verbose,
				boolean showLocation)
	    {
		ErrorMessage.displayAux(fn, input, index, e, verbose,
					showLocation);
	    }

	    @Override
	    public void displayConsoleIfNeeded() {
		SwingErrorMessage.displayConsoleIfNeededAux();
	    }

	    @Override
	    public void display(String prefix, Throwable t) {
		ErrorMessage.displayAux(prefix, t);
	    }

	    @Override
	    public void display(Throwable t) {
		ErrorMessage.displayAux(t);
	    }

	    @Override
	    public void display(String msg) {
		ErrorMessage.displayAux(msg);
	    }

	    @Override
	    public void format(String format, Object... args) {
		ErrorMessage.formatAux(format, args);
	    }

	    @Override
	    public void format(Locale locale, String format, Object... args) {
		ErrorMessage.formatAux(locale, format, args);
	    }

	    @Override
	    public void displayFormat(String title, String format,
				      Object... args)
	    {
		if (useGUI) {
		    SwingErrorMessage.displayFormat(comp, title, format, args);
		} else {
		ErrorMessage.displayFormatAux(title, format, args);
		}
	    }

	    @Override
	    public void displayFormat(String title, Locale locale,
				      String format,
				      Object... args)
	    {
		if (useGUI) {
		    SwingErrorMessage.displayFormat(comp, title, locale,
						    format, args);
		} else {
		    ErrorMessage.displayFormatAux(title, locale, format, args);
		}
	    }

	    @Override
	    public void display(String title, String msg) {
		if (useGUI) {
		    SwingErrorMessage.display(comp, title, msg);
		} else {
		    ErrorMessage.displayAux(title, msg);
		}
	    }
	};

    static {
	ErrorMessage.setOps(ops);
    }

    /**
     * Get an instance of ErrorMsgOps.
     * @return an instance of ErrorMsgOps that can be used for reporting
     *         error messages or warnings.
     */
    public static ErrorMsgOps getErrorMsgOps() {
	return ops;
    }

    static final boolean headless = GraphicsEnvironment.isHeadless();

    private static SwingErrorMessage me = headless? null:
	new SwingErrorMessage();

    /**
     * Get the instance of ErrorMessage used as a tag.
     * This object is merely a tag - it has no ErrorMessage-specific
     * methods. It is useful for the case where the source of an event
     * is an object responsible for error messages.
     * @return the instance; null if the graphics environment is headless.
     */
    public static SwingErrorMessage getTaggingInstance() {return me;}

    // This is a constructor for a singleton object used as the source of
    // change events.
    private SwingErrorMessage() {}

    

    private static final String resourceBundleName = 
	"org.bzdev.swing.lpack.ErrorMessage";
    static ResourceBundle bundle = 
	ResourceBundle.getBundle(resourceBundleName);
    static String localeString(String name) {
	return bundle.getString(name);
    }

    private static boolean useGUI = false;

    private static boolean needSeparator = false;
    /**
     * Add separator if needed.
     */
    public static void addSeparatorIfNeeded() {
	if (ErrorMessage.err instanceof SimpleConsole) {
	    ((SimpleConsole)ErrorMessage.err).addSeparatorIfNeeded();
	    ErrorMessage.needSeparator = false;
	} else {
	    ErrorMessage.addSeparatorAux();
	    /*
	    if (needSeparator) {
		addSeparator();
		}*/
	}
	// needSeparator = false;
    }

    private static EventListenerList stlist = headless? null:
	new EventListenerList();
    static ChangeEvent changeEvent = headless? null: new ChangeEvent(me);

    /**
     * Add a state-change listener.
     * State-change listeners will be notified if stack-trace mode has
     * been changed.
     * @param listener the listener to add.
     */
    public static void addChangeListener(ChangeListener listener) {
	if (headless) return;
	stlist.add(ChangeListener.class, listener);
    }

    /**
     * Remove a state-change listener
     * @param listener the listener to remove
     */
    public static void removeChangeListener(ChangeListener listener) {
	if (headless) return;
	stlist.remove(ChangeListener.class, listener);
    }

    /**
     * Notify listeners that the state of this class has changed.
     */
    static void fireStateChanged() {
	if (headless) return;
	Object[] listeners = stlist.getListenerList();
	for (int i = listeners.length-2; i >= 0; i-=2) {
	    ((ChangeListener)listeners[i+1]).stateChanged(changeEvent);
	}
    }

    /**
     * Add a separator.
     */
    public static void addSeparator() {
	if (err instanceof SimpleConsole) {
	    ((SimpleConsole)err).addSeparator();
	    ErrorMessage.needSeparator = false;
	} else {
	    ErrorMessage.addSeparatorAux();
	    /*
	    try {
		err.append
		    ("________________________________________________\n\n");
	    } catch (IOException e) {}
	    */
	}
	// needSeparator = false;
    }


    static boolean recordStackTrace = false;
    /**
     * Set stack-trace recording.
     * Determines if a stack trace is shown when a Throwable is displayed.
     * @param value true if stack traces should be recorded; false otherwise
     * @see ErrorMessage#display(Throwable)
     */
    public static void setStackTrace(boolean value) {
	boolean old = recordStackTrace;
	recordStackTrace = value;
	if (headless) return;
	if (old != value) {
	    if (SwingUtilities.isEventDispatchThread()) {
		fireStateChanged();
	    } else {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
			    fireStateChanged();
			}
		    });
	    }
	}
    }

    /**
     * Check if stack traces are enabled.
     * @return true if stack traces are enabled; false otherwise
     * @see #setStackTrace(boolean)
     */
    public static boolean stackTraceEnabled() {return recordStackTrace;}

    static Component comp = null;
    /**
     * Set the component on which to center dialog boxes; null for
     * a system default.
     * If this method is not called this class will assume that no GUI
     * is available so all messages will be passed to an Appendable
     * (by default, System.err).
     * @param comp the component; null if no particular component is
     *        specified.
     */
    public static void setComponent(Component comp) {
	boolean oldUseGUI = useGUI;
	Component oldComp = SwingErrorMessage.comp;

	SwingErrorMessage.comp = comp;
	useGUI = true;
	if (headless) return;
	if ((oldUseGUI != useGUI) || (oldComp != comp)) {
	    if (SwingUtilities.isEventDispatchThread()) {
		fireStateChanged();
	    } else {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
			    fireStateChanged();
			}
		    });
	    }
	}
    }

    // static Appendable err = System.err;

    /*
     * Set the Appendable recording error messages.
     * The default is System.err.  The method
     * {@link #displayConsoleIfNeeded()} will display a console
     * if it is passed to this method and if it is an instance of
     * {@link SimpleConsole}.  To use the Java console for output
     * (the TTY associated with a process on Unix/Linux), try
     * setAppendable(System.console().writer()); however, normally
     * one would just use System.err.
     * @param out the Appendable for error messages; null for the default.
    public static void setAppendable(Appendable out) {
	Appendable old = err;
	if (out == null) out = System.err;
	err = out;
	if (headless) return;
	if (err != old) {
	    if (SwingUtilities.isEventDispatchThread()) {
		fireStateChanged();
	    } else {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
			    fireStateChanged();
			}
		    });
	    }
	}
    }
     */

    /*
     * Display a message with a file name and line number.
     * @param fn the file name
     * @param lineno the line number
     * @param msg the message to display
    public static void display(String fn, int lineno, String msg) {
	if (msg == null) return;
	String newmsg = (fn == null)? 
	    MessageFormat.format(localeString("lineNoMsg"), lineno, msg):
	    MessageFormat.format(localeString("fnLineNoMsg"), 
				 fn, lineno, msg);
	try {
	    err.append(newmsg);
	    err.append('\n');
	} catch (IOException e) {
	}
    }
     */

    /**
     * Display a console if there is one, if it is an instance of
     * {@link SimpleConsole}, and if there is new text to display.
     * For a {@link SimpleConsole} to be available, it must have
     * been passed to {@link #setAppendable(Appendable)}.
     */
    public static void displayConsoleIfNeeded() {
	ops.displayConsoleIfNeeded();
    }
    private static void displayConsoleIfNeededAux() {
	if (headless) return;
	if (err instanceof SimpleConsole) {
	    SimpleConsole console = (SimpleConsole) err;
	    if (console.hasNewTextToDisplay()) {
		Container c = console.getParent();
		while (!(c == null || c instanceof JFrame)) {
		    c = c.getParent();
		}
		if (c != null) {
		    JFrame frame = (JFrame) c;
		    frame.setVisible(true);
		}
	    }
	}
    }

    /*
     * Display a Throwable with a prefix.
     * at a minimum, the Throwable's message will be displayed.
     * Optionally, a stack trace may be displayed as well.
     * @param prefix a prefix to show before a stack trace, typically
     *        consisting of spaces
     * @param t the Throwable
     * @see #setStackTrace
    private static void display (String prefix, Throwable t) 
    {
	try {
	    err.append(prefix);
	    String tmsg = t.getMessage();
	    if (tmsg == null) {
		err.append("caused by " + t.getClass().toString());
	    } else {
		err.append("caused by " + t.getClass().toString() +": " + tmsg);
	    }
	    err.append('\n');
	    if (recordStackTrace && t instanceof Exception) {
		for (StackTraceElement ste: t.getStackTrace()) {
		    err.append(prefix);
		    err.append("    ");
		    err.append(ste.toString());
		    err.append('\n');
		}
	    }
	} catch (IOException eio) {
	}
	needSeparator = true;
    }
    */
    /*
     * Display a Throwable.
     * at a minimum, the Throwable's message will be displayed.
     * Optionally, a stack trace may be displayed as well.
     * @param t the Throwable
     * @see #setStackTrace
    public static void display(Throwable t) {
	if (t == null) return;
	String msg = t.getMessage();
	if (msg == null) {
	    msg = t.getClass().toString();   
	} else if (recordStackTrace && t instanceof Exception) {
	    msg = t.getClass().toString() + ": " + msg;
	}
	try {
	    err.append(msg);
	    err.append('\n');
	    if (recordStackTrace && t instanceof Exception) {
		for (StackTraceElement ste: t.getStackTrace()) {
		    err.append("    ");
		    err.append(ste.toString());
		    err.append('\n');
		}
		Throwable tt = t.getCause();
		if (tt != null) {
		    display("  ", tt);
		}
	    }
	} catch (IOException ioe) {
	}
	needSeparator = true;
    }
     */


    /*
     * Display  a message.
     * @param msg the message to display
    public static void display(String msg) {
	if (msg == null) return;
	try {
	    err.append(msg);
	    err.append('\n');
	} catch (IOException e) {
	}
	needSeparator = true;
    }
    */
    /*
     * Display formatted text using the default locale.
     * See {@link java.util.Formatter} for a description of the
     * format string.
     * @param format a format string
     * @param args the arguments to format
     *
    public static void format(String format, Object... args) {
	display(String.format(format, args));
    }
    */
    /*
     * Display formatted text for a specific locale.
     * See {@link java.util.Formatter} for a description of the
     * format string.
     * @param locale the Locale, null for no localization
     * @param format a format string
     * @param args the arguments to format
    public static void format(Locale locale, String format, Object... args) {
	display(String.format(locale, format, args));
    }
    */
    /*
     * Display formatted text with a title using the default locale.
     * The title is used as the title of a dialog box when a
     * dialog box is used to display the message,  
     * If {@link #setComponent(Component) setComponent} was not called,
     * the title will be ignored and the message will appear on the
     * {@link Appendable} used to log messages.
     * See {@link java.util.Formatter} for a description of the
     * format string.
     * @param title the title; null for the default title
     * @param format a format string
     * @param args the arguments to format
    public static void displayFormat(String title, String format,
					 Object... args)
    {
	display(title, String.format(format, args));
    }
     */

    /**
     * Display formatted text with a title using the default locale and
     * a specified component.
     * The title is used as the title of a dialog box.
     * See {@link java.util.Formatter} for a description of the
     * format string.
     * @param comp the component; null for the default.
     * @param title the title; null for the default title
     * @param format a format string
     * @param args the arguments to format
     */
    public static void displayFormat(Component comp,
				     String title, String format,
				     Object... args)
    {
	display(comp, title, String.format(format, args));
    }

    /**
     * Display formatted text with a title for a specific locale.
     * The title is used as the title of a dialog box when a
     * dialog box is used to display the message,  If no component
     * was specified for centering a dialog box,  the
     * title is ignored and the message is printed on an
     * {@link java.lang.Appendable Appendable}.
     * See {@link java.util.Formatter} for a description of the
     * format string.
     * @param title the title; null for the default title
     * @param locale the Locale, null for no localization
     * @param format a format string
     * @param args the arguments to format
     */
    public static void displayFormat(String title, Locale locale,
				     String format,
				     Object... args)
    {
	display(title, String.format(locale, format, args));
    }

    /**
     * Display formatted text with a title for a specific locale.
     * The title is used as the title of a dialog box when a
     * dialog box is used to display the message,  If the Component
     * argument is null, a default location for the dialog box will
     * be used.
     * See {@link java.util.Formatter} for a description of the
     * format string.
     * @param comp the Component on which to center the dialog box.
     * @param title the title; null for the default title
     * @param locale the Locale, null for no localization
     * @param format a format string
     * @param args the arguments to format
     */
    public static void displayFormat(Component comp, String title,
				     Locale locale, String format,
				     Object... args)
    {
	display(comp, title, String.format(locale, format, args));
    }

    /**
     * Display  a message with a title.
     * The title is used as the title of a dialog box when a
     * dialog box is used to display the message.
     * If {@link #setComponent(Component) setComponent} was not called,
     * the title will be ignored and the message will appear on the
     * {@link Appendable} used to log messages.
     * @param msg the message to display
     * @param title the title; null for the default title
     * @see #setAppendable(Appendable)
     * @see #setComponent(Component)
     */
    public static void display(String title, String msg) {
	if (!useGUI) {
	    try {
		err.append(msg);
		err.append('\n');
	    } catch (IOException e) {}
	} else {
	    display(comp, title, msg);
	}
    }

    /** Display a message with a title, centering any dialog box on a component.
     * The title is used as the title of a dialog box.
     * @param comp the component; null for a default.
     * @param title the title; null for the default title
     * @param msg the message to display
     * @see SwingErrorMessage#setComponent(Component)
     */
    public static void display(Component comp, String title, String msg) {
	if (msg == null) return;
	if (title == null) {
	    title = localeString("ErrorMessage");
	}
	JOptionPane.showMessageDialog(comp, msg, title,
				      JOptionPane.ERROR_MESSAGE);
    }
}

//  LocalWords:  ErrorMessage setAppendable Appendable setComponent
//  LocalWords:  SwingErrorMessage OL JOptionPane setStackTrace TTY
//  LocalWords:  boolean programatically Throwable StackTraceMenuItem
//  LocalWords:  SimpleConsole ErrorMsgOps needSeparator addSeparator
//  LocalWords:  IOException displayConsoleIfNeeded SwingUtilities fn
//  LocalWords:  isEventDispatchThread fireStateChanged invokeLater
//  LocalWords:  Runnable lineno msg newmsg MessageFormat lineNoMsg
//  LocalWords:  localeString fnLineNoMsg Throwable's tmsg getMessage
//  LocalWords:  recordStackTrace instanceof StackTraceElement ste tt
//  LocalWords:  getStackTrace eio getCause ioe args displayFormat
