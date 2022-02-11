package org.bzdev.swing;

import javax.swing.JTextField;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.text.Document;
import javax.swing.text.AbstractDocument;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import java.text.DecimalFormat;

import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import java.awt.BorderLayout;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import org.bzdev.swing.text.CharDocFilter;

//@exbundle org.bzdev.swing.lpack.Swing

/**
 * Text field constrained to allow time intervals.
 * The intervals can be SECONDS (including decimal values to
 * millisecond precision), MINUTES:SECONDS and HOURS:MINUTES:SECONDS.
 * In addition, the text field can be configures to accept a
 * single-character input containing '?' to indicate that the
 * application will interactively determine the time interval, and '*'
 * to indicate that the time interval is indefinite (i.e., not
 * specified by the user).
 *
 * @author Bill Zaumen
 */
public class TimeTextField extends VTextField {
    
    static String errorMsg(String key, Object... args) {
	return SwingErrorMsg.errorMsg(key, args);
    }

    /**
     * Indicate an indefinite interval.
     * This value for the field indicates that the time
     * interval is indefinite - until the application quits
     * or stops the operation for which the time interval is used.
     */
    public static final int INDEFINITE = -1;
    /**
     * Indicate a queried time interval.
     * This value for the field indicates that the caller
     * is expected to determine the time interval by some
     * interactive means.
     */
    public static final int QUERY = -2;
    private int minimumValue = 0; // 0, INDEFINITE, or QUERY

    private long defaultValue = 0;
    private long value;

    static public long parseTime(String value)
	throws Exception 
    {
	if (value == null) throw new NullPointerException(errorMsg("nullArg"));
	int i;
	value = value.trim();
	if (!value.matches("([0-9]+:){0,2}[0-9]+((\\.|,)[0-9]+)?"))
	    throw new Exception(errorMsg("timeExpected"));
	String values[] = value.split(":");
	// String val = values[0];
	long time = 0;
	for (i = 0; i < values.length-1; i++) {
	    time *=60;
	    time += Long.parseLong(values[i]);
	}
	values = values[i].split("\\.|,");
	switch (values.length) {
	case 1:
	    time *= 60;
	    time += Long.parseLong(values[0]);
	    time *= 1000;
	    break;
	case 2:
	    time *= 60;
	    time += Long.parseLong(values[0]);
	    time *= 1000;
	    value = values[1];
	    long ms;
	    switch (value.length()) {
	    case 0:
		ms = 0;
		break;
	    case 1:
		ms = Long.parseLong(value) * 100;
		break;
	    case 2:
		ms = Long.parseLong(value) * 10;
		break;
	    case 3:
		ms = Long.parseLong(value);
		break;
	    default:
		ms = Long.parseLong(value.substring(0,3));
		if (value.charAt(3) > '4') ms++;
		    
	    }
	    time += ms;
	    break;
	default:
	    throw new Exception(errorMsg("timeExpected"));
	}
	return time;
    }

    static String formatTime(long time) {
	long seconds = time /1000;
	long ms = time % 1000;
	long minutes = seconds / 60;
	long hours = minutes / 60;
	if (hours > 0) {
	    minutes = minutes % 60;
	}
	String msString;
	char decimalSeparator = (new DecimalFormat()).getDecimalFormatSymbols()
	    .getDecimalSeparator();

	if (decimalSeparator != '.' && decimalSeparator != ',') {
	    // just in case some locale uses a completely different format.
	    decimalSeparator = '.'; 
	    
	}

	if (ms >= 100) {
	    msString = "" + decimalSeparator + ms;
	} else if (ms >= 10) {
	    msString = decimalSeparator + "0" + ms;
	} else {
	    msString = decimalSeparator + "00" + ms;
	}
	String hoursString = (hours >= 10)? ("" + hours): "0" + hours;
	String minutesString = (minutes >- 10)? ("" + minutes): "0" + minutes;
	return ((hours > 0)? (hoursString + ":"): "")
	    +((minutes > 0 || hours > 0)? (minutesString + ":"): "")
	    + seconds + msString;
    }


    private CharDocFilter cdf = new
	CharDocFilter() {
	    protected boolean allowedChar(char ch) {
		return Character.isDigit(ch) || ch == '.' || ch == ','
		    || ch == ':';
	    }
	};

    /**
     * TimeTextField Modes.
     * These specify whether additional characters are allowed in
     * the input field of a TimeTextField.
     */
    public static enum Mode {
	/**
	 * Plain mode.
	 * No special characters are allowed.
	 */
	PLAIN,
	/**
	 * Query Mode.
	 * If the Text field contains the value, '?', that indicates
	 * that an application will interactively obtain the value from
	 * the user in some way (e.g., via a dialog box that is dismissed
	 * when the time interval expires
	 */
	QUERY,
	/**
	 * Indefinite Mode.
	 * If the TimeTextField contains the value, '*', that indicates
	 * that the time interval is indefinite and will be determined
	 * autonomously by an application.
	 */
	INDEFINITE,
	/**
	 * Combined Mode.
	 * A combination of both QUERY and INDEFINITE modes, as determined
	 * by a presence of either a '*' or a '?' in the text field.
	 */
	QUERY_AND_INDEFINITE
    }

    Mode mode;
    private void cdfInit(Mode mode) {
	this.mode = mode;
	switch (mode) {
	case PLAIN:
	    minimumValue = 0;
	    break;
	case QUERY:
	    cdf.setOptSingleChars("?");
	    minimumValue = QUERY;
	    break;
	case INDEFINITE:
	    cdf.setOptSingleChars("*");
	    minimumValue = INDEFINITE;
	    break;
	case QUERY_AND_INDEFINITE:
	    cdf.setOptSingleChars("*?");
	    minimumValue = QUERY;
	    break;
	}
    }

    protected void onAccepted() throws Exception {
	String text = getText();
	if (text.equals("*")) {
	    /* setValue(-1, text); */
	    value = INDEFINITE;
	} else if (text.equals("?")) {
	    /* setValue(-2, text); */
	    value = QUERY;
	} else {
	    value = parseTime(getText());
	}
    }

    /**
     * Set the value of the text field.
     * @param value the text-field's value (a non-negative integer,
     *        TimeTextField.QUERY, or TimeTextField.INDEFINITE)
     * @throws IllegalArgumentException the argument is out of range
     */
    public void setValue(long value) {
	if (value < QUERY)
	    throw new IllegalArgumentException
		(errorMsg("argIllegal", value));
	if (value == INDEFINITE) {
	    setValue(INDEFINITE, "*");
	} else if (value == QUERY) {
	    setValue(QUERY, "?");
	} else {
	    setText/*origSetText*/(formatTime(value));
	    this.value = value;
	}
    }

    /**
     * Set the value of the text field.
     * @param value the text-field's value (a non-negative integer,
     *        or possibly TimeTextField.QUERY or TimeTextField.INDEFINITE
     *        depending on the configuration)
     * @param text the text to display.
     * @throws IllegalArgumentException the argument is out of range.
     */
    private void setValue(long value, String text) {
	if (mode == Mode.QUERY) {
	    if (value < 0 && value != QUERY) {
	    throw new IllegalArgumentException
		(errorMsg("argIllegal", "QUERY (-2)"));
	    }
	}
	if (value < minimumValue) {
	    String s = (value == QUERY)? "QUERY (-2)":
		((value == INDEFINITE)? "INDEFINITE (-1)": "" + value);
	    throw new IllegalArgumentException(errorMsg("argIllegal", s));
	}
	setText(text);
	this.value = value;
    }

    /*
    private boolean allowEmptyTF = true;
    public void allowEmptyTextField(boolean value) {
	allowEmptyTF = value;
    }
    protected boolean getAllowEmptyTextField() {
	return allowEmptyTF;
    }
    */

    private boolean acceptText(String text) {
	if (text.equals("")) return true;
	switch(mode) {
	case QUERY:
	    if (text.equals("?")) return true;
	    break;
	case INDEFINITE:
	    if (text.equals("*")) return true;
	    break;
	case QUERY_AND_INDEFINITE:
	    if (text.equals("?")) return true;
	    if (text.equals("*")) return true;
	    break;
	default:
	    break;
	}
	try {
	    parseTime(text);
	    return true;
	} catch (Exception e) {
	    return false;
	}
    }

    private void init() {
	setInputVerifier(new InputVerifier() {
		public boolean verify(JComponent input) {
		    VTextField tf = (VTextField) input;
		    return acceptText(tf.getText());
		}
		public boolean shouldYieldFocus(JComponent input,
						JComponent target)
		{
		    return true;
		}
	    });
    }

    /**
     * Get the text field's value.
     * The value will be in units of milliseconds, with -1 indicting
     * that the value is indefinite and -2 indicating that the user is
     * asking for a value.  Values representing times are always
     * positive.
     * @return the value of the text field, or the default value
     *         when the text field is empty
     */
    synchronized public long getValue() {
	String str = /*mustValidate()? getValidatedText(): */getText();
	if (str.equals("")) {
	    if (getAllowEmptyTextField()) {
		return (defaultValue);
	    } else {
		getValidatedText();
	    }
	} else {
	    getValidatedText();
	}
	return value;
    }

    /**
     * Set the default value when the text-string is empty.  The value
     * may be any integer larger than -3, although values representing
     * a time will always be non-negative. This allows getValue to
     * return a negative value as an indication that the field is
     * indefinite (-1), or that the user is asking for a value (-2).
     * @param value the default value to use
     */
    public void setDefaultValue(long value) {defaultValue = value;}

    /**
     * Class constructor.
     */
    public TimeTextField() {
	super();

    }

    /**
     * Class constructor for a document model, initial string and field size.
     * @param doc the document model (must be an AbstractDocument)
     * @param text the initial text
     * @param ncols the number of columns in the text field.
     */
    public TimeTextField(Document doc, String text, int ncols) {
	this(doc, text, ncols, Mode.PLAIN);
    }

    /**
     * Class constructor specifying the field size..
     * @param ncols the number of columns in the text field.
     */
    public TimeTextField(int ncols) {
	this(ncols, Mode.PLAIN);
    }

    /**
     * Constructor giving an initial string.
     * @param text the initial text
     */
    public TimeTextField(String text) {
	this(text, Mode.PLAIN);
    }

    /**
     * Constructor giving an initial string and field size.
     * @param text the initial text
     * @param ncols the number of columns in the text field.
     */
    public TimeTextField(String text, int ncols) {
	this(text, ncols, Mode.PLAIN);
    }


    /**
     * Constructor for a document model, initial string, field size, and mode.
     * @param doc the document model (must be an AbstractDocument)
     * @param text the initial text
     * @param ncols the number of columns in the text field.
     * @param mode Mode.PLAIN, Mode.QUERY, Mode.INDEFINITE, or
     *        Mode.QUERY_AND_INDEFINITE to specify use of a single-character
     *        value
     * @see TimeTextField.Mode
     */
    public TimeTextField(Document doc, String text, int ncols, Mode mode) {
	super(doc, text, ncols);
	init();
	cdfInit(mode);
	AbstractDocument adoc = (AbstractDocument)doc;
	adoc.setDocumentFilter(cdf);
    }

    /**
     * Constructor specifying the field size and mode.
     * @param ncols the number of columns in the text field.
     * @param mode Mode.PLAIN, Mode.QUERY, Mode.INDEFINITE, or
     *        Mode.QUERY_AND_INDEFINITE to specify use of a single-character
     *        value
     * @see TimeTextField.Mode
     */
    public TimeTextField(int ncols, Mode mode) {
	super(ncols);
	init();
	cdfInit(mode);
	((AbstractDocument)getDocument()).setDocumentFilter(cdf);
    }

    /**
     * Constructor giving an initial string and mode.
     * @param text the initial text
     * @param mode Mode.PLAIN, Mode.QUERY, Mode.INDEFINITE, or
     *        Mode.QUERY_AND_INDEFINITE to specify use of a single-character
     *        value
     * @see TimeTextField.Mode
     */
    public TimeTextField(String text, Mode mode) {
	super(text);
	cdfInit(mode);
	((AbstractDocument)getDocument()).setDocumentFilter(cdf);
    }

    /**
     * Constructor giving an initial string, field size and mode.
     * @param text the initial text
     * @param ncols the number of columns in the text field
     * @param mode Mode.PLAIN, Mode.QUERY, Mode.INDEFINITE, or
     *        Mode.QUERY_AND_INDEFINITE to specify use of a single-character
     *        value
     * @see TimeTextField.Mode
     */
    public TimeTextField(String text, int ncols, Mode mode) {
	super(text, ncols);
	init();
	cdfInit(mode);
	((AbstractDocument)getDocument()).setDocumentFilter(cdf);
    }
}

//  LocalWords:  exbundle Zaumen nullArg timeExpected TimeTextField
//  LocalWords:  setValue IllegalArgumentException argIllegal boolean
//  LocalWords:  origSetText allowEmptyTF allowEmptyTextField ncols
//  LocalWords:  getAllowEmptyTextField mustValidate getValidatedText
//  LocalWords:  getValue AbstractDocument
