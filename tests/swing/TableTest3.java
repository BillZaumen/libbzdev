import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.LineBorder;

// for security checks
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;


// Just use Java built-in classes for this test.
public class TableTest3 {


    static class GenericEditor extends DefaultCellEditor {

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
	Class<?>clasz;

        public GenericEditor(Class<?> clasz, JTextField tf) {
            super(tf);
	    this.clasz = clasz;
            getComponent().setName("Table.editor");
        }

        public boolean stopCellEditing() {
            String s = (String)super.getCellEditorValue();
            // Here we are dealing with the case where a user
            // has deleted the string value in a cell, possibly
            // after a failed validation. Return null, so that
            // they have the option to replace the value with
            // null or use escape to restore the original.
            // For Strings, return "" for backward compatibility.
            try {
                if ("".equals(s)) {
                    if (constructor.getDeclaringClass() == String.class) {
                        value = s;
                    }
                    return super.stopCellEditing();
                }

                // SwingUtilities2.checkAccess(constructor.getModifiers());
                value = constructor.newInstance(new Object[]{s});
            }
            catch (Exception e) {
                ((JComponent)getComponent())
		    .setBorder(new LineBorder(Color.red));
                return false;
            }
            return super.stopCellEditing();
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected,
                                                 int row, int column) {
            this.value = null;
            ((JComponent)getComponent()).setBorder(new LineBorder(Color.black));
            try {
                Class<?> type = clasz;
                // Since our obligation is to produce a value which is
                // assignable for the required type it is OK to use the
                // String constructor for columns which are declared
                // to contain Objects. A String is an Object.
                if (type == Object.class) {
                    type = String.class;
                }
                // ReflectUtil.checkPackageAccess(type);
                // SwingUtilities2.checkAccess(type.getModifiers());
                constructor = type.getConstructor(argTypes);
            }
            catch (Exception e) {
                return null;
            }
            return super.getTableCellEditorComponent(table, value,
						     isSelected, row, column);
        }

        public Object getCellEditorValue() {
            return value;
        }
    }


    public static void setup() {

	String[] colNames = {"Test Data 1", "Test Data 2"};
	String data[][] = {
	    {"Line 1", ""},
	    {"Line 2", ""},
	    {"", ""},
	    {"", ""},
	    {"", ""},
	    {"", ""}
	};


	JTable table = new JTable(data, colNames);

	final JTextField tf = new JTextField();

	// Uncomment to instrument part of what the text field is doing.
	tf.addComponentListener(new ComponentListener() {
		public void componentShown(ComponentEvent e) {
		    System.out.println("shown");
		}
		public void componentMoved(ComponentEvent e) {
		    System.out.format("moved to (%d, %d)\n",
				      tf.getX(), tf.getY());
		}
		public void componentResized(ComponentEvent e) {
		    System.out.format("resized to w = %d, h= %d\n",
				      tf.getWidth(), tf.getHeight());;
		}
		public void componentHidden(ComponentEvent e) {
		    System.out.println("hidden");
		}
	    });
	tf.addCaretListener((ce) -> {
		System.out.println("caret position = " + ce.getDot()
				   + ", text = " +  tf.getText());
		Object src = ce.getSource();
		System.out.println("... tf bg = " + tf.getBackground());
		System.out.println("... tf fg = " + tf.getForeground());
		System.out.println("... tf caret = " + tf.getCaretColor());
		System.out.println("... tf font = " +tf.getFont());
		System.out.println("... tf showing = " + tf.isShowing());
	    });

	table.getColumnModel()
	    .getColumn(0)
	    .setCellEditor(new GenericEditor(String.class,tf));

	System.out.println("our cell editor: " + table.getCellEditor(0, 0));
	System.out.println("tbl cell editor: " + table.getCellEditor(0, 1));


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
