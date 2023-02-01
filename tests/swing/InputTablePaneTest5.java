import java.awt.*;
import java.util.Vector;
import javax.swing.*;
import java.text.*;
import java.sql.*;
import javax.swing.text.*;
import javax.swing.table.*;

import org.bzdev.swing.*;
import org.bzdev.swing.table.*;

public class InputTablePaneTest5 {

    public static void setup() {

	InputTablePane.ColSpec[] spec = {
	    new InputTablePane.ColSpec("User", "mmmmmmmmmmmmmmm",
				       String.class, null, null),
	    new InputTablePane.ColSpec("Date", "mmmmmmmmmmmmmmm",
				       String.class,
				       new CSSTableCellRenderer(false),
				       new CSSCellEditor())
	};

	Vector<Vector<Object>> data = new Vector<Vector<Object>>(2);
	Vector<Object> row = new Vector<Object>(2);
	row.add("color 1"); row.add("green");
	data.add(row);
	row = new Vector<Object>(2);
	row.add("color 2"); row.add("blue");
	data.add(row);

	InputTablePane ipane = new InputTablePane(spec, data.size(), data,
						  false, false, false);

	int status = InputTablePane.showDialog(null, "Test", ipane);
	for (int i = 0; i < ipane.getRowCount(); i++) {
	    System.out.println(ipane.getValueAt(i, 0) + " = "
			       + ipane.getValueAt(i, 1));
	}
	System.out.println("status = " + status
			   + " (expected " + status + ")");
    }

    public static void main(String argv[]) throws Exception {

	boolean systemUI = argv.length > 0 && argv[0].equals("--systemUI");
	if (systemUI) {
	    /*
	    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	    */
	    DarkmodeMonitor.setSystemPLAF();
	    DarkmodeMonitor.init();
	}


	SwingUtilities.invokeLater(() -> {
		setup();
	    });
	if (argv.length > 1) {
	    Thread.currentThread().currentThread().sleep(30000);
	    System.exit(0);
	}
    }
}
