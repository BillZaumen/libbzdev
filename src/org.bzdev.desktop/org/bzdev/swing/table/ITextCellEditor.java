package org.bzdev.swing.table;

/*
import java.sql.Time;
import java.time.LocalTime;
import java.time.format.FormatStyle;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
*/

import java.text.Format;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.DefaultCellEditor;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.Component;
import java.awt.Toolkit;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.text.Format;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.InternationalFormatter;


/**
 * Base class for custom text-based table-cell editors in which
 * a cell's value can be created using an {@link InternationalFormatter}.
 * An {@link InternationalFormatter} can create dates, times, and numbers.
 * To use this class, the methods {@link ITextCellEditor#getFormat()},
 * {@link ITextCellEditor#getFormatter()} and 
 * {@link ITextCellEditor#convertObject(Object)} must be implement:
 * <UL>
 *   <LI>{@link ITextCellEditor#getFormatter()} provides a subclass
 *     of {@link InternationalFormatter} that will format text strings
 *     to produce a desired object. This method is called once in
 *     the constructor.
 *   <LI>{@link ITextCellEditor#getFormat()} will provide a format that
 *     programs the formatter returned by
 *     {@link ITextCellEditor#getFormatter()}. This method is called once in
 *     the constructor.
 *   <LI>{@link ITextCellEditor#convertObject(Object)} converts the
 *       value of the text field used by a cell into an object of the
 *       desired type.
 * </UL>
 * <P>
 * This class is based on the example provided in a
 * <A HREF="https://docs.oracle.com/javase/tutorial/uiswing/examples/components/TableFTFEditDemoProject/src/components/IntegerEditor.java">
 * Java tutorial</A>.  That link includes a copyright and permission
 * to use/modify that example. This class generalizes that example.
 */
public abstract class ITextCellEditor extends DefaultCellEditor {

    static private final String resourceBundleName =
	"org.bzdev.swing.table.lpack.ITextCellEditor";
    static ResourceBundle bundle =
	ResourceBundle.getBundle(resourceBundleName);

    static String localeString(String name) {
	try {
	    return bundle.getString(name);
	} catch (Exception e) {
	    return name;
	}
    }

    
    JFormattedTextField ftf;
    Format tf;
    // SimpleDateFormat tf;

    /**
     * Indicates that object conversion failed, so
     * fall back to using the parser. This is thrown by
     * {@link ITextCellEditor#convertObject(Object)} if it
     * cannot convert its argument to an equivalent one with
     * the desired class.
     */
    public static class NoConvertException extends Exception {

	/**
	 * Constructor.
	 */
	public NoConvertException() {super();}
    }

    /**
     * Get the format used to parse the string representation for the object
     * in the cells handled by this cell editor.
     * @return the format
     */
    protected abstract Format getFormat();

    /**
     * Return the alignment for text.
     * Valid values are {@link JTextField#LEFT}, {@link JTextField#CENTER}
     * {@link JTextField#RIGHT}, {@link JTextField#LEADING} (the default),
     * or {@link JTextField#TRAILING}.
     * @return the alignment constant
     */
    protected int alignment() {
	return JTextField.LEADING;
    }

    /**
     * Get the formatter for this object.
     * The formatter will be configured by this object's constructor
     * to use the formatter returned by {@link #getFormat()}.
     * @return the formatter
     */
    protected abstract InternationalFormatter getFormatter();

    /**
     * Convert an object to the correct type for a cell.
     * An instance of {@link org.bzdev.swing.InputTablePane} assigns
     * an object type to each column. In case editing a cell creates
     * a different type, this class can be used to convert that type to
     * the desired one.  For example, a database application may want
     * table entries to be instances of java.sql.Date. If the
     * object o ends up as an instance of {@link java.util.Date}, it
     * can be easily converted to java.sql.Date by the
     * expression
     *  <CODE>new java.sql.Date(((java.util.Date)o).getTime())</CODE>.
     * @param o the object
     * @exception NoConvertException if the argument cannot be converted
     *       to the desired type
     */
    protected abstract Object convertObject(Object o)
	throws NoConvertException;

    /**
     * Message to use if the new value for a cell is valid.
     * For example, if the value must be a date using a
     * specific date format, this message might be
     * "The value must be a date specified by &lt;br&gt;
     * the format MM/DD/YYYY"
     * <P>
     * Each line of the message should be about 40 characters
     * long except for the last line. The message is treated as
     * an HTML fragment.
     * @return the message string
     */
    protected abstract String revertMessage();
    
    
    /**
     * Constructor.
     */
    public ITextCellEditor() {
	super(new JFormattedTextField());
	ftf = (JFormattedTextField) getComponent();
	// tf = new SimpleDateFormat("h:mm a");
	tf = getFormat();
	InternationalFormatter formatter = getFormatter();
	// DateFormatter formatter = new DateFormatter(tf);
	formatter.setFormat(tf);
	ftf.setFormatterFactory(new DefaultFormatterFactory(formatter));
	ftf.setHorizontalAlignment(alignment());
	ftf.setFocusLostBehavior(JFormattedTextField.PERSIST);
        //React when the user presses Enter while the editor is
        //active.  (Tab is handled as specified by
        //JFormattedTextField's focusLostBehavior property.)
        ftf.getInputMap().put(KeyStroke.getKeyStroke(
						     KeyEvent.VK_ENTER, 0),
			      "check");
        ftf.getActionMap().put("check", new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
		    if (!ftf.isEditValid()) { //The text is invalid.
			if (userSaysRevert()) { //reverted
			    ftf.postActionEvent(); //inform the editor
			}
		    } else try {              //The text is valid,
			    ftf.commitEdit();     //so use it.
			    ftf.postActionEvent(); //stop editing
			} catch (java.text.ParseException exc) { }
		}
	    });
    }

    //Override to invoke setValue on the formatted text field.
    /**
     * Sets an initial value for the editor.
     * Sets an initial value for the editor. This will cause the
     * editor to stopEditing and lose any partially edited value if
     * the editor is editing when this method is called.
     * <P>
     * Returns the component that should be added to the client's
     * Component hierarchy. Once installed in the client's hierarchy
     * this component will then be able to draw and receive user
     * input.
     * <P>
     * Note: this description was copied from the documentation for
     * {@link javax.swing.table.TableCellEditor}.
     * @param table the JTable that is asking the editor to edit; can
     *        be null
     * @param value the value of the cell to be edited; it is up to
     *        the specific editor to interpret and draw the value. For
     *        example, if value is the string "true", it could be
     *        rendered as a string or it could be rendered as a check
     *        box that is checked. null is a valid value
     * @param isSelected true if the cell is to be rendered with
     *        highlighting
     * @param row  the row of the cell being edited
     * @param column  the column of the cell being edited
     * @return the component for editing
     */
    @Override
    public Component getTableCellEditorComponent(JTable table,
						 Object value,
						 boolean isSelected,
						 int row, int column)
    {
	JFormattedTextField ftf =
	    (JFormattedTextField)super
	    .getTableCellEditorComponent(table, value, isSelected,
					 row, column);
	if (ftf != null) {
	    // added a null-value check just in case: not used
	    // in the example.
	    ftf.setValue(value);
	}
	return ftf;
    }

    //Override to ensure that the value remains a java.sql.Time.
    /**
     * Returns the value contained in this editor.
     * @return the value contained in this editor
     */
    @Override
    public Object getCellEditorValue() {
	JFormattedTextField ftf = (JFormattedTextField)getComponent();
	Object o = ftf.getValue();

	try {
	    if (o == null) {
		return null;
	    } else {
		return convertObject(o);
	    }
	} catch (NoConvertException e) {
	    try {
		String s = o.toString();
		if (s.trim().length() == 0) return null;
		return tf.parseObject(s);
	    } catch (ParseException exc) {
		System.err.println("getCellEditorValue: can't parse: "
				   + o);
		return null;
	    }
	}
    }

    //Override to check whether the edit is valid,
    //setting the value if it is and complaining if
    //it isn't.  If it's OK for the editor to go
    //away, we need to invoke the superclass's version 
    //of this method so that everything gets cleaned up.
    @Override
    /**
     * Tells the editor to stop editing and accept any partially
     * edited value as the value of the editor.
     * The editor returns false if editing was not stopped; this is
     * useful for editors that validate and can not accept invalid
     * entries.
     * @return true if editing was stopped; false otherwise
     */
    public boolean stopCellEditing() {
	JFormattedTextField ftf = (JFormattedTextField)getComponent();
	if (ftf.isEditValid()) {
	    try {
		ftf.commitEdit();
	    } catch (java.text.ParseException exc) { }
	    
	} else { //text is invalid
	    if (!userSaysRevert()) { //user wants to edit
		return false; //don't let the editor go away
	    } 
	}
	return super.stopCellEditing();
    }

    /**
     * Lets the user know that the text they entered is 
     * bad. Returns true if the user elects to revert to
     * the last good value.  Otherwise, returns false, 
     * indicating that the user wants to continue editing.
     */
    private boolean userSaysRevert() {
	Toolkit.getDefaultToolkit().beep();
	ftf.selectAll();
	Object[] options = {"Edit",
			    "Revert"};
	int answer = JOptionPane
	    .showOptionDialog(SwingUtilities.getWindowAncestor(ftf),
			      "<html>" +
			      revertMessage() + "<br><br>" +
			      localeString("revertMsg") + "</html>",
			      localeString("revertTitle"),
			      JOptionPane.YES_NO_OPTION,
			      JOptionPane.ERROR_MESSAGE,
			      null,
			      options,
			      options[1]);
	    
	if (answer == 1) { //Revert!
	    ftf.setValue(ftf.getValue());
	    return true;
	}
	return false;
    }
}

//  LocalWords:  InternationalFormatter ITextCellEditor getFormat tf
//  LocalWords:  getFormatter convertObject formatter HREF JTextField
//  LocalWords:  SimpleDateFormat NoConvertException lt br setValue
//  LocalWords:  DateFormatter JFormattedTextField's stopEditing html
//  LocalWords:  focusLostBehavior JTable isSelected superclass's
//  LocalWords:  getCellEditorValue revertMsg revertTitle
