import java.awt.*;
import javax.swing.*;
import java.text.*;
import java.sql.*;
import javax.swing.text.*;
import javax.swing.table.*;

import org.bzdev.swing.*;

public class InputTablePaneTest {

    static class DateTableCellRenderer extends DefaultTableCellRenderer {
	public DateTableCellRenderer() {super();}

	public Component getTableCellRendererComponent
	    (JTable table, Object value, boolean isSelected, boolean hasFocus,
	     int row, int column)
	{
	    Component result = super.getTableCellRendererComponent
		(table, value, isSelected, hasFocus, row, column);
	    if (column == 1) {
		result.setForeground(Color.GREEN.darker().darker());
	    } else {
		result.setForeground(Color.BLACK);
	    }
	    return result;
	}
    }

    static class DateTableCellEditor extends ITextCellEditor {
	@Override
	protected Format getFormat() {
	    return new SimpleDateFormat("MM/dd/yyyy");
	}
	@Override
	protected InternationalFormatter getFormatter() {
	    return new DateFormatter();
	}
	@Override
	protected Object convertObject(Object o) throws
	    ITextCellEditor.NoConvertException
	{
	    if (o instanceof java.sql.Date) {
		return o;
	    } else if (o instanceof java.util.Date) {
		java.util.Date d = (java.util.Date) o;
		return new java.sql.Date(d.getTime());
	    }
	    throw new ITextCellEditor.NoConvertException();
	}
	@Override
	protected String revertMessage() {
	    return "The value must be a date specified by <br> "
		+ "the format MM/DD/YYYY";
	}
    }


    public static void setup() {

	InputTablePane.ColSpec[] spec = {
	    new InputTablePane.ColSpec("User", "mmmmmmmmmmmmmmm",
				       String.class, null,
				       InputTablePane.DEFAULT_CELL_EDITOR),
	    new InputTablePane.ColSpec("Date", "mmmmmmmmmmmmmmm",
				       java.sql.Date.class,
				       new DateTableCellRenderer(),
				       new DateTableCellEditor())
	};

	InputTablePane ipane =
	    InputTablePane.showDialog(null, "Test", spec, 10,
				      true, true, true);
	if (ipane == null) {
	    System.out.println("... was canceled");
	} else {
	    for (int i = 0; i < ipane.getRowCount(); i++) {
		String user = (String)ipane.getValueAt(i,0);
		java.sql.Date date = (java.sql.Date)ipane.getValueAt(i, 1);
		if (user != null && date != null) {
		    System.out.format("%s: %s\n", user, date);
		}
	    }
	}
    }

    public static void main(String argv[]) throws Exception {

	boolean systemUI = argv.length > 0 && argv[0].equals("--systemUI");
	if (systemUI) {
	    /*
	    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	    */
	    SwingUtilities.invokeLater(() -> {
		    DarkmodeMonitor.setSystemPLAF();
		    DarkmodeMonitor.init();
		});
	}


	SwingUtilities.invokeLater(() -> {
		setup();
	    });
	if (argv.length > (systemUI? 2: 1)) {
	    Thread.currentThread().currentThread().sleep(30000);
	    System.exit(0);
	}
    }
}
