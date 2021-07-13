package org.bzdev.util;
import java.util.Locale;

/**
 * Error message interface.
 * This interface provides a common interface for reporting errors.
 * Classes that implement this interface will determine where and
 * how the error message is displayed.
 */
public interface ErrorMsgOps {
    /**
     * Add separator if needed.
     */
    void addSeparatorIfNeeded();

    /**
     * Add a separator.
     */
    void addSeparator();

    /**
     * Set stack-trace recording.
     * Determines if a stack trace is shown when a Throwable is displayed.
     * @param value true if stack traces should be recorded; false otherwise
     * @see ErrorMessage#display(Throwable)
     */
    void setStackTrace(boolean value);

    /**
     * Check if stack traces are enabled.
     * @return true if stack traces are enabled; false otherwise
     * @see #setStackTrace(boolean)
     */
    boolean stackTraceEnabled();

    /**
     * Set the Appendable recording error messages.
     * The default is System.err.  The method
     * {@link #displayConsoleIfNeeded()} will display a console
     * if it is passed to this method and if it is an instance of
     * {@link org.bzdev.swing.SimpleConsole}.  To use the Java console
     * for output (the TTY associated with a process on Unix/Linux), try
     * setAppendable(System.console().writer()); however, normally
     * one would just use System.err.
     * @param out the Appendable for error messages; null for the default.
     */
    void setAppendable(Appendable out);

    /**
     * Display a message with a file name and line number.
     * @param fn the file name
     * @param lineno the line number
     * @param msg the message to display
     */
    void display(String fn, int lineno, String msg);

    /**
     * Display an error message, based on an exception,
     * with a line from some input.
     * The default format in English shows the filename in quotes
     * followed by the line number, the class for the exception, and
     * the corresponding message. The next line contains a portion of
     * the input, with a subsequent line including a caret indicating
     * the point where the error occurred, followed by some information
     * about the column number.
     * @param fn a file name or other name for the input
     * @param input the input being processed
     * @param index a location in the input where an error occurred
     * @param e the exception that generated an error
     * @param verbose true if a long format should be used
     * @param showLocation true if line number and column numbers should be
     *        shown; false otherwise
     */
    void display(String fn, String input, int index, Exception e,
		 boolean verbose, boolean showLocation);


    /**
     * Display a console if there is one, if it is an instance of
     * {@link org.bzdev.swing.SimpleConsole}, and if there is new text
     * to display. For a {@link org.bzdev.swing.SimpleConsole SimpleConsole}
     * to be available, it must have been passed to
     * {@link #setAppendable(Appendable)}.
     */
    void displayConsoleIfNeeded();

    /**
     * Display a Throwable with a prefix.
     * at a minimum, the Throwable's message will be displayed.
     * Optionally, a stack trace may be displayed as well.
     * @param prefix a prefix to show before a stack trace, typically
     *        consisting of spaces
     * @param t the Throwable
     * @see #setStackTrace
     */
     void display (String prefix, Throwable t);

    /**
     * Display a Throwable.
     * at a minimum, the Throwable's message will be displayed.
     * Optionally, a stack trace may be displayed as well.
     * @param t the Throwable
     * @see #setStackTrace
     */
    void display(Throwable t);

    /**
     * Display  a message.
     * @param msg the message to display
     */
    void display(String msg);

    /**
     * Display formatted text using the default locale.
     * See {@link java.util.Formatter} for a description of the
     * format string.
     * @param format a format string
     * @param args the arguments to format
     *
     */
    void format(String format, Object... args);    

    /**
     * Display formatted text for a specific locale.
     * See {@link java.util.Formatter} for a description of the
     * format string.
     * @param locale the Locale, null for no localization
     * @param format a format string
     * @param args the arguments to format
     */
    void format(Locale locale, String format, Object... args);

    /**
     * Display formatted text with a title using the default locale.
     * The title is used as the title of a dialog box when a
     * dialog box is used to display the message,  
     * If
     * {@link org.bzdev.swing.SwingErrorMessage#setComponent(Component) setComponent}
     * was not called, the title will be ignored and the message will
     * appear on the* {@link Appendable} used to log messages.
     * See {@link java.util.Formatter} for a description of the
     * format string.
     * @param title the title; null for the default title
     * @param format a format string
     * @param args the arguments to format
     *
     */
    void displayFormat(String title, String format, Object... args);

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
    void displayFormat(String title, Locale locale, String format,
		       Object... args);

    /**
     * Display  a message with a title.
     * The title is used as the title of a dialog box when a
     * dialog box is used to display the message.
     * If
     * {@link org.bzdev.swing.SwingErrorMessage#setComponent(Component) setComponent}
     * was not called,the title will be ignored and the message will
     *  appear on the {@link Appendable} used to log messages.
     * @param msg the message to display
     * @param title the title; null for the default title
     * @see ErrorMessage#setAppendable(Appendable)
     * @see org.bzdev.swing.SwingErrorMessage#setComponent(Component)
     */
    void display(String title, String msg);
}

//  LocalWords:  Throwable ErrorMessage setStackTrace boolean TTY fn
//  LocalWords:  Appendable displayConsoleIfNeeded lineno msg args
//  LocalWords:  SimpleConsole setAppendable Throwable's setComponent
