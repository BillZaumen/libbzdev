package org.bzdev.swing;
import java.awt.Component;
import java.awt.Color;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.util.function.Function;


// for security checks
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;


/**
 * Cell editor for objects that can be represented textually.
 * The type parameter generally should be the type of a JTable
 * column, but may be a super type of the column's type if the
 * class provided in a constructor is null.
 * <P>
 * Typically the text that is entered must follow some syntactic
 * rules.  If not, a red square will appear around a cell and the
 * user can either re-edit the text or hit the escape key to
 * revert to the previous value.  The class
 * {@link ITextCellEditor}, by contrast, brings up a dialog box
 * when it detects an input error.
 * <P>
 * Note:  for Ubuntu or Pop!_OS openjdk packages such as
 * openjdk-11-... version 11.0.17+8-1ubuntu2~22.04 or
 * openjdk-17-... version 17.0.5+8-2ubuntu1~22.04 there is a
 * bug in which, when editing text in a JTable, neither the
 * text nor a caret is visible.  This occurs when a
 * {@link DefaultCellEditor} is created with the constructor
 * {@link DefaultCellEditor#DefaultCellEditor(JTextField)}
 * explicitly used, and the look and feel set to the system
 * look and feel.  The reference implementations, however, work as
 * expected.  To see if this bug has been fixed, compile
 * TableTest.java from the source code's tests/swing directory and run
 * java -classpath=... TableTest --systemUI (the argument sets the
 * look and feel to the system look and feel).
 * <P>
 * The initial motivation for {#link TextCellEditor} was to
 * work around this bug, but the constructors providing functions
 * for formatting and parsing provide extra capabilities. Because
 * JTable alone did not exhibit this bug and includes a (non public)
 * cell editor names GenericEditor, the code for that class was used
 * as a model for {@link TextCellEditor}.
 */
public class TextCellEditor<T> extends DefaultCellEditor {

    // Following tests copied from SwingUtilities2 and
    // ReflectUtil so that this class does not depend on
    // those.

    static boolean isNonPublicProxyClass(Class<?> cls) {
	if (!Proxy.isProxyClass(cls)) {
	    return false;
	}
	return !Modifier.isPublic(cls.getModifiers());
    }

    private static void privateCheckProxyPackageAccess
	(@SuppressWarnings("removal") SecurityManager s, Class<?> clazz) {
	// check proxy interfaces if the given class is a proxy class
	if (Proxy.isProxyClass(clazz)) {
	    for (Class<?> intf : clazz.getInterfaces()) {
		privateCheckPackageAccess(s, intf);
	    }
	}
    }

    private static void privateCheckPackageAccess
	(@SuppressWarnings("removal") SecurityManager s, Class<?> clazz)
    {
	String pkg = clazz.getPackageName();
	if (!pkg.isEmpty()) {
	    s.checkPackageAccess(pkg);
	}
	if (isNonPublicProxyClass(clazz)) {
	    privateCheckProxyPackageAccess(s, clazz);
	}
    }

    static void checkPackageAccess(Class<?> clazz) {
	@SuppressWarnings("removal")
	    SecurityManager s = System.getSecurityManager();
	if (s != null) {
	    privateCheckPackageAccess(s, clazz);
	}
    }
	
    @SuppressWarnings("removal")
    static void checkAccess(int modifiers) {
	if (System.getSecurityManager() != null
	    && !Modifier.isPublic(modifiers)) {
	    throw new SecurityException("Resource is not accessible");
	}
    }
    Class<?>[] argTypes = new Class<?>[]{String.class};
    java.lang.reflect.Constructor<?> constructor;
    Object value;
    Class<T> clasz;
    Function<T,String> formatter = null;
    Function<String,T> parser = null;


    /**
     * Constructor.
     * <P>
     * With this constructor, the implentation assumes that clasz
     * has a single-argument public constructor that takes a string
     * as its argument, and that constructor will be used to create
     * an instance of the class provided by the type parameter T.
     * @param clasz the class of the object being edited; null if
     *              the class for a table's column should be used.
     */
    public TextCellEditor(Class<T> clasz) {
	this(clasz, new JTextField());
    }

    /**
     * Constructor given a text field..
     * <P>
     * With this constructor, the implentation assumes that clasz
     * has a single-argument public constructor that takes a string
     * as its argument, and that constructor will be used to create
     * an instance of the class provided by the type parameter T.
     * @param clasz the class of the object being edited; null if
     *              the class for a table's column should be used.
     * @param tf the text field used to store values that are being
     *        edited
     */
    public TextCellEditor(Class<T> clasz, JTextField tf) {
	super(tf);
	this.clasz = clasz;
	getComponent().setName("Table.editor");
    }

    /**
     * Constructor providing formatters and parsers.
     * The parameter clasz should be the class object for
     * the type parameter T.
     * @param clasz the class of the object being edited
     * @param formatter a function that maps objects of type T
     *        to strings
     * @param parser a function that maps strings to objects of
     *        type T
     */
    public TextCellEditor(Class<T> clasz,
			     Function<T,String> formatter,
			     Function<String,T> parser)
    {
	this(clasz, new JTextField(), formatter, parser);
    }


    /**
     * Constructor providing a text field,  formatters and parsers.
     * The parameter clasz should be the class object for
     * the type parameter T.
     * @param clasz the class of the object being edited
     * @param tf the text field used to store values that are being
     *        edited
     * @param formatter a function that maps objects of type T
     *        to strings
     * @param parser a function that maps strings to objects of
     *        type T
     */
    public TextCellEditor(Class<T> clasz, JTextField tf,
			     Function<T,String> formatter,
			     Function<String,T> parser)
    {
	this(clasz, tf);
	this.formatter = formatter;
	this.parser = parser;
    }


    @Override
    public boolean stopCellEditing() {
	Object v = super.getCellEditorValue();
	String s = (String)v;
	// Here we are dealing with the case where a user
	// has deleted the string value in a cell, possibly
	// after a failed validation. Return null, so that
	// they have the option to replace the value with
	// null or use escape to restore the original.
	// For Strings, return "" for backward compatibility.
	try {
	    if ("".equals(s)) {
		/*
		  if (constructor.getDeclaringClass() == String.class) {
		  value = s;
		  }
		*/
		if (parser == null && constructor == null) {
		    value = s;
		} else if (parser != null) {
		    value = (clasz.equals(String.class)
			     || clasz.equals(Object.class))?
			"": null;
		} else if (constructor.getDeclaringClass()
			   == String.class) {
		    value = s;
		}
		return super.stopCellEditing();
	    }

	    // SwingUtilities2.checkAccess(constructor.getModifiers());
	    /*
	      value = constructor.newInstance(new Object[]{s});
	    */
	    if (parser != null) {
		value = parser.apply(s);
	    } else if (constructor != null) {
		checkAccess(constructor.getModifiers());
		value = constructor.newInstance(new Object[]{s});
	    }
	    // System.out.println("value = " + value);
	} catch (Exception e) {
	    ((JComponent)getComponent())
		.setBorder(new LineBorder(Color.red));
	    return false;
	}
	return super.stopCellEditing();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
						 boolean isSelected,
						 int row, int column) {
	this.value = null;
	((JComponent)getComponent()).setBorder(new LineBorder(Color.black));
	try {
	    Class<?> type = clasz;
	    if (type == null) type = table.getColumnClass(column);
	    // Since our obligation is to produce a value which is
	    // assignable for the required type it is OK to use the
	    // String constructor for columns which are declared
	    // to contain Objects. A String is an Object.
	    if (type.equals(Object.class)) {
		type = String.class;
	    }
	    if (formatter == null) {
		checkPackageAccess(type);
		checkAccess(type.getModifiers());
		constructor = type.getConstructor(argTypes);
	    } else {
		constructor = null;
		value = (value == null)? "":
		    formatter.apply(clasz.cast(value));
	    }
	}  catch (Exception e) {
	    return null;
	}
	return super.getTableCellEditorComponent(table, value,
						 isSelected, row, column);
    }

    @Override
    public Component getTreeCellEditorComponent(JTree tree, Object value,
						boolean isSelected,
						boolean expanded,
						boolean leaf,
						int row)
    {
	// This is a guess: mimic what we did for the Table case.
	this.value = null;
	((JComponent)getComponent()).setBorder(new LineBorder(Color.black));
	try {
	    Class<?> type = clasz;
	    if (type == null) type = Object.class;
	    if (type.equals(Object.class)) {
		type = String.class;
	    }
	    if (formatter == null) {
		checkPackageAccess(type);
		checkAccess(type.getModifiers());
		constructor = type.getConstructor(argTypes);
	    } else {
		constructor = null;
		value = (value == null)? "":
		    formatter.apply(clasz.cast(value));
	    }
	}  catch (Exception e) {
	    return null;
	}
	return super.getTreeCellEditorComponent(tree, value,
						isSelected, expanded, leaf,
						row);
    }

    @Override
    public Object getCellEditorValue() {
	return value;
    }

}
