import java.awt.*;
import java.util.Vector;
import javax.swing.*;
import java.text.*;
import java.sql.*;
import javax.swing.text.*;
import javax.swing.table.*;

import org.bzdev.swing.*;

public class InputTablePaneTest3 {


    public static void setup() {

	InputTablePane.ColSpec[] spec = {
	    new InputTablePane.ColSpec("User", "mmmmmmmmmmmmmmm",
				       String.class, null, null),
	    new InputTablePane.ColSpec("Date", "mmmmmmmmmmmmmmm",
				       String.class, null, null)
	};

	Vector<Vector<Object>> data = new Vector<Vector<Object>>(2);
	Vector<Object> row = new Vector<Object>(2);
	row.add("user 1"); row.add("2021-05-20");
	data.add(row);
	row = new Vector<Object>(2);
	row.add("user 2"); row.add("2021-05-30");
	data.add(row);
	row = new Vector<Object>(2);
	row.add("user 3"); row.add("2021-05-30");
	data.add(row);
	row = new Vector<Object>(2);
	row.add("user 5"); row.add("2021-05-30");
	data.add(row);
	row = new Vector<Object>(2);
	row.add(""); row.add("");
	data.add(row);
	row = new Vector<Object>(2);
	row.add("user 6"); row.add("2021-05-30");
	data.add(row);

	InputTablePane ipane =
	    InputTablePane.showDialog(null, "Test", spec, data,
				      false, false, false);
	if (ipane == null) {
	    System.out.println("... was canceled");
	} else {
	    System.out.println("was closed");
	}
    }

    public static void main(String argv[]) throws Exception {

	if (argv.length == 0) {
	    // just check this if we are running without arguments
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
