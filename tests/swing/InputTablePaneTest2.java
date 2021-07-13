import java.awt.*;
import javax.swing.*;
import java.text.*;
import javax.swing.text.*;
import javax.swing.table.*;

import org.bzdev.swing.*;
import org.bzdev.swing.table.*;

public class InputTablePaneTest2 {

    static class OurCellRenderer extends DefaultTableCellRenderer {
	public OurCellRenderer() {super();}

	public Component getTableCellRendererComponent
	    (JTable table, Object value, boolean isSelected, boolean hasFocus,
	     int row, int column)
	{
	    Component result = super.getTableCellRendererComponent
		(table, value, isSelected, hasFocus, row, column);
	    if (column == 1) {
		String key = (String)table.getValueAt(row, 0);
		if (key != null && key.startsWith("base64.")) {
		    result.setForeground(Color.GREEN.darker().darker());
		} else {
		    result.setForeground(Color.BLACK);
		}
	    } else {
		result.setForeground(Color.BLACK);
	    }
	    return result;
	}
    }
    
    static class TaggedTextField extends JTextField {
	boolean tag = false;
	public TaggedTextField() {super();}
    }

    static class OurCellEditor extends DefaultCellEditor {
	public OurCellEditor() {
	    super(new TaggedTextField());

	}
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
						     boolean isSelected,
						     int row, int column)
	{
	    boolean tag = false;
	    if (column == 1) {
		String key = (String) table.getValueAt(row, 0);
		tag = (key != null) && key.startsWith("base64.");
		if (tag) {
		    if (value != null) {
			// use a leading "* " as a surrogate for base64
			value = "* " + (String)value;
			tag = true;
		    } else {
			value = "* ";
		    }
		}
	    }
	    TaggedTextField tf = (TaggedTextField)
		super.getTableCellEditorComponent(table, value, isSelected,
						  row, column);
	    tf.tag = tag;
	    return tf;
	}
	@Override
	public Object getCellEditorValue() {
	    TaggedTextField tf = (TaggedTextField) getComponent();
	    String val = tf.getText();
	    if (tf.tag) {
		return val.substring(2);
	    } else {
		return val;
	    }
	}

    }

    public static void setup() {

	InputTablePane.ColSpec[] spec = {
	    new InputTablePane.ColSpec("User", "mmmmmmmmmmmmmmm",
				       String.class, null,
				       InputTablePane.DEFAULT_CELL_EDITOR),
	    new InputTablePane.ColSpec("Date", "mmmmmmmmmmmmmmm",
				       String.class,
				       new OurCellRenderer(),
				       new OurCellEditor())
	};

	InputTablePane ipane =
	    InputTablePane.showDialog(null, "Test", spec, 10,
				      true, true, true);
	if (ipane == null) {
	    System.out.println("... canceled");
	}
	for (int i = 0; i < ipane.getRowCount(); i++) {
	    String user = (String)ipane.getValueAt(i,0);
	    String value = (String)ipane.getValueAt(i, 1);
	    if (user != null && value != null) {
		System.out.format("%s: %s\n", user, value);
	    }
	}
    }

    public static void main(String argv[]) throws Exception {
	SwingUtilities.invokeLater(() -> {
		setup();
	    });
	if (argv.length > 1) {
	    Thread.currentThread().currentThread().sleep(30000);
	    System.exit(0);
	}
    }
}
