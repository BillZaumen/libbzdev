package org.bzdev.swing;
import java.awt.*;
import java.awt.event.*;
import java.text.Format;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;
import javax.swing.table.*;

import org.bzdev.lang.UnexpectedExceptionError;

//@exbundle org.bzdev.swing.lpack.Swing

/**
 * Table pane for inputing data.
 * This class creates a JPanel containing a JTable plus some controls.
 * The class {@link InputTablePane.ColSpec} specifies each column in
 * the table, indicating a title, how the cells in the column are rendered,
 * and how the cells in the column are edited (a null editor indicates that
 * the column is a read-only column).
 * <P>
 * There are methods for determining the number of rows and columns in the
 * table and for obtaining the values at any given cell, and to create
 * dialog boxes containing this panel or a new instance of this panel.
 * <P>
 * The table model for this panel's table is initialized by providing a
 * {@link java.util.Vector} whose type is
 * Vector&lt;Vector&lt;Object&gt;&gt;, with the Vector&lt;Object&gt;
 * representing the rows.
 */
public class InputTablePane extends JPanel {

    static String errorMsg(String key, Object... args) {
	return SwingErrorMsg.errorMsg(key, args);
    }

    private  static DefaultCellEditor default_cell_editor;

    static {
	Runnable r = () -> {
	    default_cell_editor = new DefaultCellEditor(new JTextField());
	};
	if (SwingUtilities.isEventDispatchThread()) {
	    r.run();
	} else {
	    try {
		SwingUtilities.invokeAndWait(r);
	    } catch (Exception e) {
		throw new UnexpectedExceptionError(e);
	    }
	}
    }

    /**
     * Get a custom table-cell renderer for a specific row and column.
     * This method should be overridden if a custom table-cell renderer
     * is desired.
     * @param tbl the internal table used by this object.
     * @param i the row number
     * @param j the column number
     * @return the custom cell editor; null for a default
     */
    protected TableCellRenderer getCustomRenderer(JTable tbl, int i, int j) {
	return null;
    }

    /**
     * Get a custom cell editor for a specific row and column.
     * This method should be overridden if a custom table-cell renderer
     * is desired.
     * @param tbl the internal table used by this object.
     * @param i the row number
     * @param j the column number
     * @return the custom table-cell renderer; null for a default
     */
    protected  TableCellEditor getCustomEditor(JTable tbl, int i, int j) {
	return null;
    }


    /**
     * Prevent editing of specific rows and columns.
     * The default implementation returns <CODE>false</CODE> for any
     * pair of arguments. If the value returned is dependent on a
     * cell's row and column instead of its contents, the behavior
     * of this class may be erratic unless one ensures that those
     * cells cannot be moved (e.g., by using
     * {@link #minimumSelectableRow(int,boolean)}).
     *
     * @param row the table row
     * @param col the table column
     * @return true if editing is explicitly prohibited for the given
     *         row and column; false otherwise
     */
    protected boolean prohibitEditing(int row, int col) {
	return false;
    }

    /**
     * Limit the range of rows that are selectable for a given column.
     * A table selection may not include a cell for which selection
     * is prohibited.  Because the table used in this object's
     * implementation allows only a contiguous selection, rectangular
     * in shape, if any cell within a rectangle is prohibited, the
     * selection cannot be made.
     * <P>
     * The allColumnSelection argument is provided because rows can
     * be inserted, moved or deleted only when a selection includes every
     * column, and one might want to have stricter criteria for that case.
     * The default implementation returns 0 in call cases, so this method
     * will have no effect unless it is overridden.
     * @param col the column number
     * @param allColumnSelection true if all the columns were selected;
     *        false otherwise
     * @return the first row in the column that may be selected.
     */
    protected  int minimumSelectableRow(int col, boolean allColumnSelection) {
	return 0;
    }


    /**
     * Indicate that a row is about to be deleted
     * @param row the row number.
     */
    protected void beforeRowDeletion(int row) {
	return;
    }



    /**
     * Table-cell editor that indicates that a table's default should
     * be used.  This should be used only with an instance of
     * {@link InputTablePane}.
     */
    public static final DefaultCellEditor DEFAULT_CELL_EDITOR
	= default_cell_editor;

    private static int configColumn(JTable table, int col,
				    String heading, String example)
    {
	TableCellRenderer tcr = table.getDefaultRenderer(String.class);
	int w;
	if (tcr instanceof DefaultTableCellRenderer) {
	    DefaultTableCellRenderer dtcr = (DefaultTableCellRenderer)tcr;
	    FontMetrics fm = dtcr.getFontMetrics(dtcr.getFont());
	    int cw = Math.max(fm.stringWidth(heading), fm.stringWidth(example));
	    w = 10 + cw;
	} else {
	    int cw = Math.max(heading.length(), example.length());
	    w = 10 + 12 * cw;
	}
	TableColumnModel cmodel = table.getColumnModel();
	TableColumn column = cmodel.getColumn(col);
	int ipw = column.getPreferredWidth();
	if (ipw > w) {
	    w = ipw; 
	}
	column.setPreferredWidth(w);
	return w;
    }

    /**
     * Specification for a table column.
     */
    public static class ColSpec {
	String heading;
	String example;
	Class<?> clasz;
	TableCellRenderer tcr;
	TableCellEditor tce;

	/**
	 * Constructor.
	 * <P>
	 * Note: if one explicitly sets the trc argument to
	 * {@link DefaultTableCellRenderer}, {@link javax.swing.JTable}
	 * implementation will render boolean values as strings not as
	 * checkboxes. As a general rule, one should set the tcr argument
	 * to specify a specific table cell renderer to use when the default
	 * behavior is not wanted.
	 * If the tce argument is set to
	 * {@link InputTablePane#DEFAULT_CELL_EDITOR}, the table will
	 * use the system default editor: the value
	 * {@link InputTablePane#DEFAULT_CELL_EDITOR} merely indicates
	 * that the cells in the column are in fact editable.
	 * @param heading the table heading
	 * @param example, sample text to compute the column width
	 * @param clasz the class for the data in this column
	 * @param tcr the table-cell renderer used to render a cell in the
	 *        column; null for the {@link javax.swing.JTable} default
	 *        redenderer
	 * @param tce the table-cell editor used to edit a cell in the table;
	 *        null if the column is not editable
	 */
	public ColSpec(String heading, String example, Class<?> clasz,
		       TableCellRenderer tcr, TableCellEditor tce)
	{
	    this.heading = heading;
	    this.example = example;
	    this.clasz = clasz;
	    this.tcr = tcr;
	    this.tce = tce;
	}
    }
    private DefaultTableModel tm;

    /**
     * Constructor.
     * @param colspec an array (one entry per column in column  order)
     *        specifying a column
     * @param nrows the number of rows
     * @param canAdd true if rows can be added to the table; false otherwise
     * @param canDel true if rows can be deleted from the table;
     *        false otherwise
     * @param canMove true if rows can be moved up or down in the table;
     *        false otherwise
     */
    public InputTablePane(ColSpec[] colspec, int nrows,
			  boolean canAdd, boolean canDel, boolean canMove)
    {
	this(colspec, nrows, null, canAdd, canDel, canMove);
    }

    /**
     * Constructor based on explicitly provided rows.
     * @param colspec an array (one entry per column in column  order)
     *        specifying a column
     * @param rows the table's rows as a Vector  of rows, where each row
     *        contains a vector whose elements fit the specification provided
     *        by the first argument (colspec)
     * @param canAdd true if rows can be added to the table; false otherwise
     * @param canDel true if rows can be deleted from the table;
     *        false otherwise
     * @param canMove true if rows can be moved up or down in the table;
     *        false otherwise
     */
    public InputTablePane(ColSpec[] colspec, Vector<Vector<Object>>rows,
			  boolean canAdd, boolean canDel, boolean canMove)
    {
	this(colspec, Math.max(rows.size(), 1), rows, canAdd, canDel, canMove);
    }

    /*private*/ JTable table;

    /**
     * Adds a listener to the list that is notified each time a
     * change to the underlying table's  data model occurs.
     * @param l the TableModelListener
     */
    public void addTableModelListener(TableModelListener l) {
	table.getModel().addTableModelListener(l);
    }

    /**
     * Removes a listener from the list that is notified each time a
     * change to the underlying table's data model occurs.
     * @param l the table model listener to remove
     */
    public void removeTableModelListener(TableModelListener l) {
	table.getModel().removeTableModelListener(l);
    }


    private static final String appendRowStr = errorMsg("appendRow");
    private static final String insertRowStr = errorMsg("insertRow");
    private static final String deleteRowStr = errorMsg("deleteRow");
    private static final String moveUpStr = errorMsg("moveUp");
    private static final String moveDownStr = errorMsg("moveDown");
    private static final String clearSelectionStr = errorMsg("clearSelection");

    // record these so we can determine if the table can be edited.
    boolean canAdd;
    boolean canDel;
    boolean canMove;
    boolean canEdit;

    /**
     * Constructor with explicitly provided rows, possibly followed by
     * blank rows.
     * @param colspec an array (one entry per column in column  order)
     *        specifying a column
     * @param initialRows the table's initial rows as a Vector of rows,
     *         where each row contains a vector whose elements fit the
     *        specification provided by the first argument (colspec)
     * @param nrows the number of rows
     * @param canAdd true if rows can be added to the table; false otherwise
     * @param canDel true if rows can be deleted from the table;
     *        false otherwise
     * @param canMove true if rows can be moved up or down in the table;
     *        false otherwise
     */
    public InputTablePane(ColSpec[] colspec, int nrows,
			  Vector<Vector<Object>> initialRows,
			  boolean canAdd, boolean canDel, boolean canMove)
    {
	super();
	this.canAdd = canAdd;
	this.canDel = canDel;
	this.canMove = canMove;
	this.canEdit = false;
	for (ColSpec spec: colspec) {
	    if (spec.tce != null) this.canEdit = true;
	}

	JButton addRowButton = canAdd? new JButton(appendRowStr): null;
	JButton insertRowButton =
	    (canAdd && canMove)? new JButton(insertRowStr): null;
	JButton deleteRowButton = canDel? new JButton(deleteRowStr): null;
	JButton moveUpButton = canMove? new JButton(moveUpStr): null;
	JButton moveDownButton = canMove? new JButton(moveDownStr): null;
	JButton clearSelectionButton = new JButton(clearSelectionStr);

	if (insertRowButton != null) {
	    insertRowButton.setToolTipText(errorMsg("inputPaneButtonTip"));
	}
	if (deleteRowButton != null) {
	    deleteRowButton.setToolTipText(errorMsg("inputPaneButtonTip"));
	}
	if (moveUpButton != null) {
	    moveUpButton.setToolTipText(errorMsg("inputPaneButtonTip"));
	}
	if (moveDownButton != null) {
	    moveDownButton.setToolTipText(errorMsg("inputPaneButtonTip"));
	}


	table = new JTable() {
		public Class<?> getColumnClass(int columnIndex) {
		    return colspec[columnIndex].clasz;
		}
		public boolean isCellEditable(int row, int col) {
		    if (prohibitEditing(row, col)) return false;
		    return colspec[col].tce != null;
		}
		public TableCellRenderer getCellRenderer(int row, int col) {
		    TableCellRenderer r = getCustomRenderer(table, row, col);
		    return (r != null)? r: super.getCellRenderer(row, col);
		}
		public TableCellEditor getCellEditor(int row, int col) {
		    TableCellEditor e = getCustomEditor(table, row, col);
		    return (e != null)? e: super.getCellEditor(row, col);
		}

	    };
	Vector<String> colHeadings = new Vector<>(colspec.length);
	for (int i = 0; i < colspec.length; i++) {
	    colHeadings.add(colspec[i].heading);
	}
	Vector<Vector<Object>>rows;
	if (initialRows != null) {
	    int isize = initialRows.size();
	    int max = Math.max(isize, nrows);
	    rows = new Vector<Vector<Object>>(max);
	    for (int i = 0; i < isize; i++) {
		Vector<Object>row = new Vector<>(colspec.length);
		for (int j = 0; j < colspec.length; j++) {
		    row.add(initialRows.get(i).get(j));
		}
		rows.add(row);
	    }
	    for (int i = isize; i < max; i++) {
		Vector<Object>row = new Vector<>(colspec.length);
		for (int j = 0; j < colspec.length; j++) {
		    row.add(null);
		}
		rows.add(row);
	    }
	} else {
	    rows = new Vector<Vector<Object>>(nrows);
	    for (int i = 0; i < nrows; i++) {
		Vector<Object>row = new Vector<>(colspec.length);
		for (int j = 0; j < colspec.length; j++) {
		    row.add(null);
		}
		rows.add(row);
	    }
	}
	int twidth = 0;
	tm = new DefaultTableModel(rows, colHeadings);
	table.setModel(tm);
	TableColumnModel tcm = table.getColumnModel();
	for (int i = 0; i < colspec.length; i++) {
	    TableColumn tc = tcm.getColumn(i);
	    if (colspec[i].tcr != null) {
		tc.setCellRenderer(colspec[i].tcr);
	    }
	    TableCellEditor tce = colspec[i].tce;
	    if (tce != DEFAULT_CELL_EDITOR) {
		// for DEFAULT_CELL_EDITOR, we use the default
		// behavior so this field is not set. When colspec[i].tce
		// is DEFAULT_CELL_EDITOR, that merely indicates that the
		// cell is in fact editable.
		tc.setCellEditor(colspec[i].tce);
	    }
	    twidth += configColumn(table, i, colspec[i].heading,
				   colspec[i].example);
	}
	ListSelectionModel lsm = new DefaultListSelectionModel();
	table.setSelectionModel(lsm);
	table.setColumnSelectionAllowed(true);
	table.setRowSelectionAllowed(true);
	lsm.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
	ListSelectionListener lse = (se) -> {
	    int rowcount = table.getSelectedRowCount();
	    int selcolcount = table.getSelectedColumnCount();
	    int colcount = table.getColumnCount();
	    boolean test1 = (selcolcount == colcount);

	    int firstrow = table.getSelectedRow();
	    if (firstrow >= 0) {
		int firstcol = table.getSelectedColumn();
		int minUsableRow = 0;
		for (int j = 0; j < colcount; j++) {
		    int r = minimumSelectableRow(firstcol+j, test1);
		    if (r > minUsableRow) minUsableRow = r;
		}
		if (firstrow < minUsableRow) {
		    /*
		    System.out.format("[%d,%d] in [%d,%d]\n",
				      firstrow, firstrow+rowcount-1,
				      0, table.getRowCount());
		    */
		    table.removeRowSelectionInterval(firstrow,
						     firstrow+rowcount-1);
		    table.removeColumnSelectionInterval(firstcol,
							firstcol+selcolcount-1);
		    return;
		}
	    }

	    boolean test2 = (rowcount > 0);
	    
	    if (test1 && test2) {
		if (insertRowButton != null) {
		    insertRowButton.setEnabled(true);
		}
		if (deleteRowButton != null) {
		    deleteRowButton.setEnabled(true);
		}
		if (canMove) {
		    moveUpButton.setEnabled(true);
		    moveDownButton.setEnabled(true);
		}
	    } else {
		if (insertRowButton != null) {
		    insertRowButton.setEnabled(false);
		}
		if (deleteRowButton != null) {
		    deleteRowButton.setEnabled(false);
		}
		if (canMove) {
		    moveUpButton.setEnabled(false);
		    moveDownButton.setEnabled(false);
		}
	    }
	    clearSelectionButton.setEnabled(test2);
	};
	lsm.addListSelectionListener(lse);
	table.getColumnModel().getSelectionModel()
	    .addListSelectionListener(lse);
	
	JScrollPane sp = new JScrollPane(table);
	table.setFillsViewportHeight(true);
	Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
	twidth += 10;
	int maxwidth = (screensize.width*8)/10;
	int maxheight = (screensize.height*8)/10;
	if (twidth > maxwidth) twidth = maxwidth;
	// int theight = 14*(nrows+2);
	int rowHeight = table.getRowHeight();
	
	int theight = rowHeight * (nrows + 1) + rowHeight/3 + 1
	    + 2*table.getRowMargin();
	if (theight > maxheight) theight = maxheight;
	sp.setPreferredSize(new Dimension(twidth, theight));
	setLayout(new BorderLayout());
	add(sp, BorderLayout.CENTER);

	JPanel topPanel = new JPanel();
	topPanel.setLayout(new FlowLayout());

	if (addRowButton != null) {
	    addRowButton.setEnabled(true);
	    addRowButton.addActionListener((ae) -> {
		    Object[] nullrow = null;
		    tm.addRow(nullrow);
		});
	    topPanel.add(addRowButton);
	}
	if (insertRowButton != null) {
	    insertRowButton.setEnabled(false);
	    insertRowButton.addActionListener((ae) -> {
		    int ind = table.getSelectedRow();
		    Object[] nullrow = null;
		    tm.insertRow(ind, nullrow);
		});
	    topPanel.add(insertRowButton);
	}
	if (deleteRowButton != null) {
	    deleteRowButton.setEnabled(false);
	    deleteRowButton.addActionListener((ae) -> {
		    int ind = table.getSelectedRow();
		    int n = table.getSelectedRowCount();
		    while (n-- > 0) {
			beforeRowDeletion(ind);
			tm.removeRow(ind);
		    }
		    table.clearSelection();
		});
	    topPanel.add(deleteRowButton);
	}
	if (canMove) {
	    moveUpButton.setEnabled(false);
	    moveUpButton.addActionListener((ae) -> {
		    int ind = table.getSelectedRow();
		    int n = table.getSelectedRowCount();
		    int end = ind + n - 1;
		    if (ind > 0) {
			tm.moveRow(ind, end, ind-1);
			table.setRowSelectionInterval(ind-1, end-1);
		    }
		});
	    topPanel.add(moveUpButton);
	}
	if (canMove) {
	    moveDownButton.setEnabled(false);
	    moveDownButton.addActionListener((ae) -> {
		    int ind = table.getSelectedRow();
		    int n = table.getSelectedRowCount();
		    int end = ind + n - 1;
		    int lastRow = table.getRowCount() - 1;
		    if (end < lastRow) {
			tm.moveRow(ind, end, ind+1);
			table.setRowSelectionInterval(ind+1, end+1);
		    }
		});
	    topPanel.add(moveDownButton);
	}
	clearSelectionButton.setEnabled(false);
	clearSelectionButton.addActionListener((ae) -> {
		table.clearSelection();
	    });
	topPanel.add(clearSelectionButton);
	add(topPanel, BorderLayout.NORTH);
    }

    /**
     * Get the number of rows in the table.
     * @return the number of rows
     */
    public int getRowCount() {return tm.getRowCount();}

    /**
     * Get the number of columns in the table.
     * @return the number of tables.
     */
    public int getColumnCount() {return tm.getColumnCount();}

    /**
     * Get the value of a table cell for this table.
     * @param rowIndex the row index (starting at 0)
     * @param columnIndex the column index (starting at 0)
     * @return the value for the specified row and column
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
	return tm.getValueAt(rowIndex, columnIndex);
    }

    /**
     * Set the valoue of a table cell for this table.
     * As usual, indicies start at 0.
     * @param val the new value
     * @param row the row index into the table
     * @param col the column index into the table
     */
    public void setValueAt(Object val, int row, int col) {
	tm.setValueAt(val, row, col);
    }
    
    /**
     * Stop editing a cell.
     */
    public void stopCellEditing() {
	CellEditor ce = table.getCellEditor();
	if (ce != null) {
	    ce.stopCellEditing();
	}
    }

    /**
     * Create a new InputTablePane inside a dialog.
     * @param parent the component on which to center the dialog
     * @param title the title of the dialog
     * @param colspec the column specification for the table
     * @param nrows the number of rows in the table
     * @param canAdd true if rows can be added to the table; false otherwise
     * @param canDel true if rows can be deleted from the table;
     *        false otherwise
     * @param canMove true if rows can be moved up or down in the table;
     *        false otherwise
     * @return an input-table pane if successful; null if canceled or closed
     */
    public static InputTablePane
	showDialog(Component parent, String title,
		   ColSpec[] colspec, int nrows,
		   boolean canAdd, boolean canDel, boolean canMove)
    {
	return showDialog(parent, title, colspec, nrows, null,
			  canAdd, canDel, canMove);
    }

    /**
     * Create a new InputTablePane, initializing some rows, placing
     * the table inside a dialog.
     * @param parent the component on which to center the dialog
     * @param title the title of the dialog
     * @param colspec the column specification for the table
     * @param initialRows the rows to initially add to the table,
     *        which determines the initial number of rows in the table.
     * @param canAdd true if rows can be added to the table; false otherwise
     * @param canDel true if rows can be deleted from the table;
     *        false otherwise
     * @param canMove true if rows can be moved up or down in the table;
     *        false otherwise
     * @return an input-table pane if successful; null if canceled or closed
     */
    public static InputTablePane
	showDialog(Component parent, String title,
		   ColSpec[] colspec, Vector<Vector<Object>> initialRows,
		   boolean canAdd, boolean canDel, boolean canMove)
    {
	return showDialog(parent, title, colspec,
			  Math.max(initialRows.size(), 1), initialRows,
			  canAdd, canDel, canMove);
    }

    private static final String okStr = errorMsg("OK");
    private static final String cancelStr = errorMsg("Cancel");

    private static final Object[] options = {okStr, cancelStr};

    /**
     * Determine if this input table pane can be modified.
     * A table can be modified if its cells can be changed, if
     * new rows can be added, if rows can be moved, or if rows
     * can be deleted.
     * @return true if it can be modified; false otherwise
     */
    boolean isModifiable() {
	return canAdd || canDel || canMove || canEdit;
    }

    /**
     * Create a new InputTablePane, initializing some rows, placing
     * the table inside a dialog.
     * @param parent the component on which to center the dialog
     * @param title the title of the dialog
     * @param colspec the column specification for the table
     * @param initialRows the rows to initially add to the table,
     *        placed as the first rows in the table
     * @param nrows the number of rows in the table
     * @param canAdd true if rows can be added to the table; false otherwise
     * @param canDel true if rows can be deleted from the table;
     *        false otherwise
     * @param canMove true if rows can be moved up or down in the table;
     *        false otherwise
     * @return an input-table pane if successful or closed; null if canceled
     */
    public static InputTablePane
	showDialog(Component parent, String title,
		   ColSpec[] colspec, int nrows,
		   Vector<Vector<Object>> initialRows,
		   boolean canAdd, boolean canDel, boolean canMove)
    {
	InputTablePane pane = new InputTablePane(colspec, nrows, initialRows,
						 canAdd, canDel, canMove);

	if (pane.isModifiable()) {
	    int status = JOptionPane.showOptionDialog(parent, pane, title,
						      JOptionPane.YES_NO_OPTION,
						      JOptionPane.PLAIN_MESSAGE,
						      null,
						      options, options[0]);
	    pane.stopCellEditing();
	    if (status == 0 || status == JOptionPane.CLOSED_OPTION) {
		return pane;
	    } else {
		return null;
	    }
	} else {
	    JOptionPane.showMessageDialog(parent, pane, title,
					  JOptionPane.PLAIN_MESSAGE, null);
	    return pane;
	}
    }

    /**
     * The value returned by
     * {@link #showDialog(Component,String,InputTablePane)} when
     * the dialog's OK button is pressed.
     */
    public static final int OK = 0;

    /**
     * The value returned by
     * {@link #showDialog(Component,String,InputTablePane)} when
     * the dialog's CANCEL button is pressed.
     */
    public static final int CANCEL = 1;

    /**
     * Show a dialog containing an InputTablePane
     * @param parent the component on which to center the dialog
     * @param title the title of the dialog
     * @param ipane the InputTablePane to display
     * @return {@link InputTablePane#OK} if the input is accepted;
     *        {@link InputTablePane#CANCEL} if the input is cancelled
     */
    public static int showDialog(Component parent, String title,
				       InputTablePane ipane)
    {
	if (ipane.isModifiable()) {
	    int status = JOptionPane.showOptionDialog(parent, ipane, title,
						      JOptionPane.YES_NO_OPTION,
						      JOptionPane.PLAIN_MESSAGE,
						      null,
						      options, options[0]);
	    ipane.stopCellEditing();
	    return (status == JOptionPane.CLOSED_OPTION)? 0: status;
	} else {
	    JOptionPane.showMessageDialog(parent, ipane, title,
					 JOptionPane.PLAIN_MESSAGE,
					 null);
	    return OK;
	}
    }
}

//  LocalWords:  inputing JPanel JTable InputTablePane ColSpec lt
//  LocalWords:  minimSelectedRow boolean selectable
//  LocalWords:  allColumnSelection
