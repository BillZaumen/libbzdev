package org.bzdev.swing;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Vector;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.colorchooser.ColorSelectionModel;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.*;

import org.bzdev.graphs.Colors;

/**
 * Color-chooser panel for CSS named colors.
 * An instance of this class can be installed into a color chooser
 * to provide an option to select CSS named colors. The panel will
 * allow the color to be selected by  using an array of swatches or
 * to be selected by name.  A separate control sets the Alpha value.
 * The typical usage of this class is as follows:
 * <BLOCKQUOTE><CODE><PRE>
 *    Component c ... ;
 *    Color color = ... ;
 *
 *    void example() {
 *        boolean modal = true;
 *        JColorChooser cc = new JColorChooser(color);
 *        cc.addChooserPanel(new CSSColorChooserPanel());
 *        JDialog dialog = JColorChooser.createDialog
 *             (c, "Color Chooser", modal, cc,
 *              (e) -&gt; {
 *                  // Color accepted
 *                  color = cc.getColor();
 *                },
 *              (e) -&gt; {
 *                  // Dialog was canceled and
 *                  // previous color kept.
 *              });
 *        dialog.setVisible(true);
 *   }
 * </PRE></CODE></BLOCKQUOTE>
 * 
 */
public class CSSColorChooserPanel extends AbstractColorChooserPanel {

    final private String bundleName =
	"org.bzdev.swing.lpack.CSSColorChooserPanel";


    class ColorRenderer extends JLabel implements TableCellRenderer {
	Border unselectedBorder;
	Border selectedBorder;

	ColorRenderer() {
	    setOpaque(true);
	    unselectedBorder =
		BorderFactory.createMatteBorder(2, 2, 2, 2,
						table.getBackground());
	    selectedBorder =
		BorderFactory.createMatteBorder(2, 2, 2, 2,
						Color.BLACK);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table,
						       Object color,
						       boolean isSelected,
						       boolean hasFocus,
						       int row, int column)
	{
	    Color theColor = (Color)color;
	    setBackground(theColor);
	    if (isSelected) {
		setBorder(selectedBorder);
		// JTable, etc. seems to only fire events when the selected row
		// changes, so we'll make the table cell renderer fix up
		// the combo box too.
		int cbIndex = Colors.namedCSSColorIndex(theColor, true);
		if (!popupVisible) {
		    namedColorCB.setSelectedIndex(cbIndex);
		}
	    } else {
		setBorder(unselectedBorder);
	    }
	    setToolTipText(Colors.getCSSName(theColor));
	    return this;
	}
    }

    static Vector<String> names = new Vector<String>(148);
    static {
	for (String name: Colors.namedCSSColors()) {
	    names.add(name);
	}
    }

    JTable table;
    JComboBox<String> namedColorCB;

    JLabel alphaLabel;
    JSlider alphaSlider;
    JSpinner alphaSpinner;

    private void setAlphaEnabled(boolean enabled) {
	alphaLabel.setEnabled(enabled);
	alphaSlider.setEnabled(enabled);
	alphaSpinner.setEnabled(enabled);
    }


    boolean popupVisible = false;

    @Override
    public void buildChooser() {
	ResourceBundle bundle =
	    ResourceBundle.getBundle(bundleName, getLocale());

	setLayout(new BorderLayout());
	JLabel cbLabel = new JLabel(bundle.getString("CSSColor"));
	namedColorCB = new JComboBox<>(names);
	JPanel topPanel = new JPanel();
	topPanel.setLayout(new FlowLayout());
	topPanel.add(cbLabel);
	topPanel.add(namedColorCB);
	add(topPanel, "North");
	JPanel bottomPanel = new JPanel();
	bottomPanel.setLayout(new FlowLayout());
	alphaLabel = new JLabel(bundle.getString("Alpha"));
	alphaSlider = new JSlider(0, 255, 255);
	SpinnerNumberModel alphaModel = new SpinnerNumberModel(255, 0, 255, 1);
	alphaSpinner = new JSpinner(alphaModel);
	bottomPanel.add(alphaLabel);
	bottomPanel.add(alphaSlider);
	bottomPanel.add(alphaSpinner);

	alphaSlider.addChangeListener((e) -> {
		int value = alphaSlider.getValue();
		Integer ival = Integer.valueOf(value);
		if (!alphaSpinner.getValue().equals(ival)) {
		    alphaSpinner.setValue(ival);
		}
		if (namedColorCB.getSelectedIndex() != -1) {
		    Color c = Colors.getColorByCSS
			((String) namedColorCB.getSelectedItem());
		    c = new Color(c.getRed(), c.getGreen(), c.getBlue(),
				  value);
		    getColorSelectionModel().setSelectedColor(c);
		}
	    });

	alphaSpinner.addChangeListener((e) -> {
		int value = alphaModel.getNumber().intValue();
		if (alphaSlider.getValue() != value) {
		    alphaSlider.setValue(value);
		}
	    });

	namedColorCB.addFocusListener(new FocusAdapter() {
		public void focusGained(FocusEvent e) {
		    if (!popupVisible) {
			table.requestFocusInWindow();
		    }
		}
	    });


	namedColorCB.addPopupMenuListener(new PopupMenuListener() {
		public void popupMenuCanceled(PopupMenuEvent e) {
		    popupVisible = false;
		    table.requestFocusInWindow();
		}
		public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
		    popupVisible = false;
		    table.requestFocusInWindow();
		}
		public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
		    popupVisible = true;
		}
	    });


	table = new JTable();
	DefaultTableModel tm = new DefaultTableModel(6,23) {
		public Class<?> getColumnClass(int col) {
		    return Color.class;
		}
	    };
	table.setPreferredSize(new Dimension(23*(20 + 4) + 3, 6*(20 + 4) + 3));
	table.setDefaultRenderer(Color.class, new ColorRenderer(/*table,
								  namedColorCB*/));
	table.setModel(tm);
	int i = 0;
	int j = 0;
	for (String name: Colors.namedCSSColors(false)) {
	    Color c = Colors.getColorByCSS(name);
	    table.setValueAt(c, i, j);
	    j++;
	    if ((j % 23) == 0) {
		i++;
		j = 0;
	    }
	}
	table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	// table.setColumnSelectionAllowed(true);
	// table.setRowSelectionAllowed(true);
	table.setCellSelectionEnabled(true);
	for (int k = 0; k < 23; k++) {
	    TableColumn col = table.getColumnModel().getColumn(k);
	    col.setPreferredWidth(20+4);
	}
	table.setRowHeight(20+4);
	table.setTableHeader(null);
	add(table, "Center");
	add(bottomPanel, "South");

	namedColorCB.addActionListener((e)->{
		int index = namedColorCB.getSelectedIndex();
		if (index == -1) {
		    table.clearSelection();
		    setAlphaEnabled(false);
		    return;
		}
		String name = (String)namedColorCB.getSelectedItem();
		if (name != null) {
		    Color c = Colors.getColorByCSS(name);
		    int ind = Colors.namedCSSColorIndex(c, false);
		    int ii = ind / 23;
		    int jj = ind % 23;
		    table.changeSelection(ii, jj, false, false);
		    setAlphaEnabled(true);
		    c = new Color(c.getRed(), c.getGreen(), c.getBlue(),
				  alphaSlider.getValue());
		    getColorSelectionModel().setSelectedColor(c);
		}
	    });
    }

    private void updateColor(Color c) {
	int ind = Colors.namedCSSColorIndex(c, false);
	if (ind == -1) {
	    namedColorCB.setSelectedIndex(-1);
	    table.clearSelection();
	    setAlphaEnabled(false);
	} else {
	    int ii = ind / 23;
	    int jj = ind % 23;
	    table.changeSelection(ii, jj, false, false);
	    setAlphaEnabled(true);
	    alphaSlider.setValue(c.getAlpha());
	}
    }
    
    private ChangeListener cl = (e) -> {
	updateColor(getColorFromModel());
    };

    @Override
    public void updateChooser() {
	updateColor(getColorFromModel());
    }


    @Override
    public void installChooserPanel(JColorChooser enclosingChooser) {
	super.installChooserPanel(enclosingChooser);
	ColorSelectionModel csm = enclosingChooser.getSelectionModel();
	csm.addChangeListener(cl);
	
    }

    @Override
    public void uninstallChooserPanel(JColorChooser enclosingChooser) {
	ColorSelectionModel csm = enclosingChooser.getSelectionModel();
	csm.removeChangeListener(cl);
	super.uninstallChooserPanel(enclosingChooser);
    }
    @Override
    public String getDisplayName() {
	ResourceBundle bundle =
	    ResourceBundle.getBundle(bundleName, getLocale());
	return bundle.getString("CSS");
    }
    

    @Override
    public Icon  getSmallDisplayIcon() {
	return null;
    }

    public Icon getLargeDisplayIcon() {
	return null;
    }

}

//  LocalWords:  CSS BLOCKQUOTE PRE boolean JColorChooser JDialog
//  LocalWords:  addChooserPanel CSSColorChooserPanel createDialog
//  LocalWords:  getColor setVisible JTable renderer CSSColor
//  LocalWords:  namedColorCB setColumnSelectionAllowed
//  LocalWords:  setRowSelectionAllowed
