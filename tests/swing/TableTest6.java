import org.bzdev.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.LineBorder;
import java.sql.Date;


// for security checks
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;

import java.util.function.Function;


// Just use Java built-in classes for this test.

/*
import java.awt.Component;
import java.awt.Color;
import javax.swing.*;
import javax.swing.border.LineBorder;
*/

// for security checks
/*
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
*/

public class TableTest6 {

    /*
    static public class OurTextualCellEditor<T> extends DefaultCellEditor {

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


	public OurTextualCellEditor(Class<T> clasz) {
	    this(clasz, new JTextField());
	}

	public OurTextualCellEditor(Class<T> clasz, JTextField tf) {
	    super(tf);
	     this.clasz = clasz;
	    getComponent().setName("Table.editor");
	}

	public OurTextualCellEditor(Class<T> clasz,
				    Function<T,String> formatter,
				    Function<String,T> parser)
	{
	    this(clasz, new JTextField(), formatter, parser);
	}
	public OurTextualCellEditor(Class<T> clasz, JTextField tf,
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
		    
		    // if (constructor.getDeclaringClass() == String.class) {
		    //	value = s;
		    // }

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
		// value = constructor.newInstance(new Object[]{s});
		if (parser != null) {
		    value = parser.apply(s);
		} else if (constructor != null) {
		    checkAccess(constructor.getModifiers());
		    value = constructor.newInstance(new Object[]{s});
		}
		System.out.println("value = " + value);
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
		e.printStackTrace();
		return null;
	    }
	    return super.getTableCellEditorComponent(table, value,
						     isSelected, row, column);
	}

	@Override
	public Object getCellEditorValue() {
	    return value;
	}
    }
    */

    public static void setup() {

	Vector<String> colNames = new Vector<>(2);
	colNames.add("Test Data 1");
	colNames.add("TCP Port");

	Vector<Vector<Object>> data = new Vector<>(6);
	Vector<Object> row = new Vector<Object>(2);
	row.add("Line 1");
	row.add(null);
	data.add(row);
	row = new Vector<Object>(2);
	row.add("Line 2"); row.add(null);
	data.add(row);
	row = new Vector<Object>(2);
	row.add(""); row.add(null);
	data.add(row);
	row = new Vector<Object>(2);
	row.add(""); row.add(null);
	data.add(row);
	row = new Vector<Object>(2);
	row.add(""); row.add(null);
	data.add(row);
	row = new Vector<Object>(2);
	row.add(""); row.add(null);
	data.add(row);


	JTable table = new JTable(data, colNames) {
		public Class<?> getColumnClass(int col) {
		    if (col == 1) return Integer.class;
		    else return String.class;
		}
	    };


	/*
	OurTextualCellEditor<Date> cellEditor = new
	    OurTextualCellEditor<Date>(Date.class, (d) -> {
		    return (d == null)? "": d.toString();
	    }, (s) -> {
		    return (s == null || s.trim().equals(""))? null:
			Date.valueOf(s);
	    });
	*/

	TextCellEditor<Integer> cellEditor = new
	    TextCellEditor<>(Integer.class, new PortTextField(),
			     (i) ->{return i.toString();},
			     Integer::valueOf);

	table.getColumnModel()
	    .getColumn(1)
	    .setCellEditor(cellEditor);

	// JScrollPane scrollPane = new JScrollPane(table);
	// table.setFillsViewportHeight(true);

	JFrame frame = new JFrame ("Table Test");
        Container fpane = frame.getContentPane();
        frame.addWindowListener(new WindowAdapter () {
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });
        fpane.setLayout(new BorderLayout());

        frame.add("Center", table);
        frame.pack();
	frame.setSize(300,500);
        frame.setVisible(true);
    }


    public static void main(String argv[]) throws Exception {

	SwingUtilities.invokeAndWait(() -> {
		try {
		    UIManager.setLookAndFeel
			(UIManager.getSystemLookAndFeelClassName());
		    // without the following, on a Pop!_OS system, the
		    // caret is very hard to see. It's probably any
		    // Ubuntu system but only tested on Pop!_OS.
		    UIManager.put("TextField.caretForeground",
				  UIManager.get("TextField.foreground"));
		    UIManager.put("TextArea.caretForeground",
				  UIManager.get("TextArea.foreground"));
		    UIManager.put("EditorPane.caretForeground",
				  UIManager.get("EditorPane.foreground"));
		    UIManager.put("TextPane.caretForeground",
				  UIManager.get("TextPane.foreground"));
		} catch(Exception e) {
		    System.err.println("system look and feel failed");
		    System.exit(1);
		}
	    });

	SwingUtilities.invokeLater(() -> {setup();});

    }
}
