package org.bzdev.swing;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.plaf.SplitPaneUI;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.net.MalformedURLException;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.InputStream;
import java.io.IOException;


import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.FactoryConfigurationError;
import org.xml.sax.SAXException;

import java.awt.*;
import java.awt.event.*;

import java.awt.ComponentOrientation;
import java.util.Locale;
import java.util.ResourceBundle;
import java.awt.Component;

//@exbundle org.bzdev.swing.lpack.Swing

/**
 * Class providing an HTML Pane combined with a side panel showing a
 * table of contents.  One may initialize the class or set the table
 * of contents by providing an XML document with the following DTD:
 * <blockquote><pre>
 * &lt;!ELEMENT toc (node) &gt;
 * &lt;!ELEMENT node (node)*&gt;
 * &lt;!ATTLIST node 
 *              title CDATA #IMPLIED
 *              uri   CDATA #IMPLIED
 *              href  CDATA #IMPLIED
 * &gt;
 * </pre></blockquote>
 * A DOCTYPE directive is not necessary - the DTD above will be
 * assumed. If a DOCTYPE directive is desired (e.g, for documentation
 * reasons), one should use "sreource:/org/bzdev/swing/toc.dtd".
 * For each <code>node</code> element, the <code>title</code>attribute
 * will be the title that appears in a table-of-contents panel and
 * the <code>uri</code> attribute provides the URI for the contents
 * for that title.  The contents should be an HTML document using HTML 3.2
 * the HTML version that Java supports (Java is slowly migrating to full
 * HTML 4.0 support but this class uses whatever the standard Java runtime
 * environment supports). The <code>href</code> attribute is ignored by
 * HtmlWithTocPane and is provided in case the XML file is also  used by
 * a web server, in which case the URL it needs to  use may be
 * different than the one provided by the <code>uri</code> attribute.
 *<P>
 * After the document is loaded (e.g., by using one of the
 * constructors for this class), one should call
 * <code>setSelectionWithAction(row)</code> to select the initial row
 * and the corresponding URL. This will almost always be row 0. If
 * <code>setSelectionWithAction</code> is not called, no initial
 * contents will appear in the HTML portion of the pane.
 * <p>
 * To initialize the class manually, use the same procedure as for
 * an instance of the class {@link org.bzdev.swing.UrlTocPane}.
 * A number of method signatures are borrowed from the JSplitPane class
 * (with some of the Javadoc comments) and simply call the
 * corresponding JSplitPane methods (but with 'left' and 'right' replaced by
 * the edges closest to the TOC pane and HTML pane respectively.
 * <P>
 * When an instance of this class is no longer needed, one should call
 * {@link HtmlWithTocPane#clearToc()} to release some resources.
 *
 * @author Bill Zaumen
 */
public class HtmlWithTocPane extends JComponent implements UrlTocTree {
    private JSplitPane splitPane;
    private UrlTocPane tocPane;
    private JScrollPane tocScrollPane;
    private HtmlPane htmlPane;

    static String errorMsg(String key, Object... args) {
	return SwingErrorMsg.errorMsg(key, args);
    }


    /**
     *  Class Constructor.
     */
    public HtmlWithTocPane () {
	tocPane = new UrlTocPane();
	finishInit(false);
    }

    /**
     * Class Constructor specifying an input stream for initialization.
     * The input stream contains an XML document.
     * @param is The input stream
     * @throws FactoryConfigurationError the XML parser cannot be configured
     * @throws SAXException the XML data in the input stream in not well formed
     *         or is not valid.
     * @throws IOException an IO error was seen
     * @throws ParserConfigurationException the XML parser cannot be configured
     */
    public HtmlWithTocPane(InputStream is)
	throws FactoryConfigurationError, SAXException, IOException,
	       ParserConfigurationException
    {
	this(is, false, false);
    }


    /**
     * Class Constructor specifying an input stream for initialization and
     * a flag indicating if the nodes should be expanded or not.
     * The input stream contains an XML document.
     * @param is The input stream
     * @param expand True if the nodes should be expanded; false otherwise
     * @throws FactoryConfigurationError the XML parser cannot be configured
     * @throws SAXException the XML data in the input stream in not well formed
     *         or is not valid.
     * @throws IOException an IO error was seen
     * @throws ParserConfigurationException the XML parser cannot be configured
     */
    public HtmlWithTocPane(InputStream is, boolean expand)
	throws FactoryConfigurationError, SAXException, IOException,
	       ParserConfigurationException
    {
	this(is, expand, false);
    }

     /**
     * Class constructor specifying and input stream for initialization, a flag
     * indicating if the nodes should be expanded or not, and a flag 
     * indicating if a validating parser should be used.
     * The input stream contains an XML document.
     * @param is The input stream
     * @param expand True if the nodes should be expanded; false otherwise
     * @param validating True if the parser is validating; false otherwise
     * @throws FactoryConfigurationError the XML parser cannot be configured
     * @throws SAXException the XML data in the input stream in not well formed
     *         or is not valid.
     * @throws IOException an IO error was seen
     * @throws ParserConfigurationException the XML parser cannot be configured
     */
    public HtmlWithTocPane(InputStream is, boolean expand, boolean validating)
	throws FactoryConfigurationError, SAXException, IOException,
	       ParserConfigurationException
    {
	tocPane = new UrlTocPane();
	tocPane.setToc(is, expand, validating);
	finishInit(true);
    }

    /**
     * Class constructor specifying an URL for initialization.
     * The URL contains an XML document.
     * @param url The URL
     * @throws FactoryConfigurationError the XML parser cannot be configured
     * @throws SAXException the XML data in the URL in not well formed
     *         or is not valid.
     * @throws IOException an IO error was seen
     * @throws ParserConfigurationException the XML parser cannot be configured
     */
    public HtmlWithTocPane(URL url)
	throws FactoryConfigurationError, SAXException, IOException,
	       ParserConfigurationException
    {
	this(url, false, false);
    }


    /**
     * Class Constructor specifying an URL for initialization and
     * a flag indicating if the nodes should be expanded or not.
     * The URL contains an XML document.
     * @param url The URL
     * @param expand True if the nodes should be expanded; false otherwise
     * @throws FactoryConfigurationError the XML parser cannot be configured
     * @throws SAXException the XML data in the URL in not well formed
     *         or is not valid.
     * @throws IOException an IO error was seen
     * @throws ParserConfigurationException the XML parser cannot be configured
     */
    public HtmlWithTocPane(URL url, boolean expand)
	throws FactoryConfigurationError, SAXException, IOException,
	       ParserConfigurationException
    {
	this(url, expand, false);
    }

     /**
     * Class Constructor specifying and URL for initialization, a flag
     * indicating if the nodes should be expanded or not, and a flag 
     * indicating if a validating parser should be used.
     * The URL contains an XML document.
     * @param url The URL
     * @param expand True if the nodes should be expanded; false otherwise
     * @param validating True if the parser is validating; false otherwise
     * @throws FactoryConfigurationError the XML parser cannot be configured
     * @throws SAXException the XML data in the URL in not well formed
     *         or is not valid.
     * @throws IOException an IO error was seen
     * @throws ParserConfigurationException the XML parser cannot be configured
     */
    public HtmlWithTocPane(URL url, boolean expand, boolean validating)
	throws FactoryConfigurationError, SAXException, IOException,
	       ParserConfigurationException
    {
	this(url.openStream(), false, false);
    }


    /**
     * Class constructor specifying an URL represented as a String for
     * initialization.
     * The URL contains an XML document.
     * @param url The URL
     * @throws FactoryConfigurationError the XML parser cannot be configured
     * @throws SAXException the XML data in the URL in not well formed
     *         or is not valid.
     * @throws IOException an IO error was seen
     * @throws ParserConfigurationException the XML parser cannot be configured
     * @throws MalformedURLException if the url is malformed
     */
    public HtmlWithTocPane(String url)
	throws FactoryConfigurationError, SAXException, IOException,
	       ParserConfigurationException, MalformedURLException
    {
	this(url, false, false);
    }


    /**
     * Class constructor specifying an URL represented as a String for
     * initialization and a flag indicating if the nodes should be
     * expanded or not.
     * The URL contains an XML document.
     * @param url The URL
     * @param expand True if the nodes should be expanded; false otherwise
     * @throws FactoryConfigurationError the XML parser cannot be configured
     * @throws SAXException the XML data in the URL in not well formed
     *         or is not valid.
     * @throws IOException an IO error was seen
     * @throws ParserConfigurationException the XML parser cannot be configured
     * @throws MalformedURLException if the url is malformed
    */
    public HtmlWithTocPane(String url, boolean expand)
	throws FactoryConfigurationError, SAXException, IOException,
	       ParserConfigurationException, MalformedURLException
    {
	this(url, expand, false);
    }

     /**
     * Class Constructor specifying and URL represented as a String for
     * initialization, a flag indicating if the nodes should be
     * expanded or not, and a flag indicating if a validating parser
     * should be used.
     * The URL contains an XML document.
     * @param url The URL
     * @param expand True if the nodes should be expanded; false otherwise
     * @param validating True if the parser is validating; false otherwise
     * @throws FactoryConfigurationError the XML parser cannot be configured
     * @throws SAXException the XML data in the URL in not well formed
     *         or is not valid.
     * @throws IOException an IO error was seen
     * @throws ParserConfigurationException the XML parser cannot be configured
     * @throws MalformedURLException if the url is malformed
     */
    public HtmlWithTocPane(String url, boolean expand, boolean validating)
	throws FactoryConfigurationError, SAXException, IOException,
	       ParserConfigurationException, MalformedURLException
    {
	this((new URL(url)).openStream(), expand, validating);
    }

    // The JSplitPane methods seem to use left/right top/down explicitly,
    // but I can't seem to get it to work properly, so we'll just ignore
    // orientation for now (setting it for components, but not trying
    // to switch right and left explicitly.
    

    public void setComponentOrientation(ComponentOrientation o) {
	super.setComponentOrientation(o);

	splitPane.setComponentOrientation(o);
	tocScrollPane.setComponentOrientation(o);
	tocPane.setComponentOrientation(o);
	htmlPane.setComponentOrientation(o);
    }

    public void setLocale(Locale locale) {
	super.setLocale(locale);
	splitPane.setLocale(locale);
	tocScrollPane.setLocale(locale);
	tocPane.setLocale(locale);
	htmlPane.setLocale(locale);
    }

    /**
     * Set the local of the content pane.
     * This method allows the locale of the content panes (the table
     * of contents and its corresponding HTML pane) to be set without
     * changing the layout of navigation controls.
     *
     *@param locale the locale.
     */
    public void setContentLocale(Locale locale) {
	tocPane.setLocale(locale);
	htmlPane.setLocale(locale);
    }

    /**
     * Get the border for this component's split pane.
     * @return the border
     */
    public Border getSplitPaneBorder() {
	return splitPane.getBorder();
    }

    /**
     * Set the border for this component's split pane.
     * @param border the border
     */
    public void setSplitPaneBorder(Border border) {
	 splitPane.setBorder(border);
    }

    /**
     * Get the border for this component's table of contents.
     * @return the border
     */
    public Border getTocPaneBorder() {
	return tocPane.getBorder();
    }

    /**
     * Set the border for this component's table of contents.
     * @param border the border
     */
    public void setTocPaneBorder(Border border) {
	tocPane.setBorder(border);
    }

    /**
     * Get the border for this component's HTML pane.
     * @return the border
     */
    public Border getHtmlPaneBorder() {
	return htmlPane.getBorder();
    }

    /**
     * Set the border for this component's HTML pane.
     * @param border the border
     */
    public void setHtmlPaneBorder(Border border) {
	htmlPane.setBorder(border);
    }

    /**
     * Get the border for this component's contents.
     * @return the border
     */
    public Border getContentPaneBorder() {
	return htmlPane.getContentPaneBorder();
    }

    /**
     * Set the border for this component's contents.
     * @param border the border
     */
    public void setContentPaneBorder(Border border) {
	htmlPane.setContentPaneBorder(border);
    }


    /**
     * Get the preferred size for the TOC pane.
     * @return the preferred size
     */
    public Dimension getTocPreferredSize() {
	return tocScrollPane.getPreferredSize();
    }

    /**
     * Get the preferred size for the HTML pane.
     * @return the preferred size
     */
    public Dimension getHtmlPanePreferredSize() {
	return htmlPane.getPreferredSize();
    }

    /**
     * Set the preferred size for the Table of Contents pane.
     * @param preferredSize the preferred size
     */
    public void setTocPreferredSize(Dimension preferredSize) {
	tocScrollPane.setPreferredSize(preferredSize);
    }

    /**
     * Set the preferred size for the HTML pane.
     * @param preferredSize the preferred size
     */
    public void setHtmlPanePreferredSize(Dimension preferredSize) {
	htmlPane.setPreferredSize(preferredSize);
    }

    private boolean backgroundSet = false;
    private boolean tocScrollbarColorSet = false;


    /**
     * Set the background color.
     * This method will also set the background color of scroll bars
     * unless the last call to {@link #setTocScrollbarBackground(Color)}
     * or {@link #setHtmlPaneScrollbarBackground(Color)}
     * if any were made, had a non-null argument, which explicitly
     * sets the background color for the corresponding scrollbars.
     * <P>
     * Note:  with some pluggable look and feels, this method may
     * be fully or partially ignored.  It works as expected with the
     * default look and feel.
     * @param color the color
     */
    @Override
    public void setBackground(Color color) {
	super.setBackground(color);
	htmlPane.setBackground(color);
	tocPane.setBackground(color);
	tocScrollPane.setBackground(color);
	splitPane.setBackground(color);
	if (!tocScrollbarColorSet) {
	    tocScrollPane.getVerticalScrollBar().setBackground(color);
	    tocScrollPane.getHorizontalScrollBar().setBackground(color);
	}
	backgroundSet = (color != null);
    }

    /**
     * Set the background color for the table of contents scroll bars.
     * If color is null and the last call to {@link #setBackground(Color)},
     * if one was made, had a non-null argument, that background color will
     * be used.  If the last call to {@link #setBackground(Color)} had a
     * null argument, the color passed as an argument will be used.
     * <P>
     * Note:  with some pluggable look and feels, this method may
     * be fully or partially ignored.  It works as expected with the
     * default look and feel.
     * @param color the color; null for the default color
     */
    public void setTocScrollbarBackground(Color color) {
	if (backgroundSet && color == null) {
	    Color c = getBackground();
	    tocScrollPane.getVerticalScrollBar().setBackground(c);
	    tocScrollPane.getHorizontalScrollBar().setBackground(c);
	} else {
	    tocScrollPane.getVerticalScrollBar().setBackground(color);
	    tocScrollPane.getHorizontalScrollBar().setBackground(color);
	}
	tocScrollbarColorSet = (color != null);
    }

    /**
     * Set the background color for the HTML pane scroll bars.
     * If color is null and the last call to {@link #setBackground(Color)},
     * if one was made, had a non-null argument, that background color will
     * be used.  If the last call to {@link #setBackground(Color)} had a
     * null argument, the color passed as an argument will be used.
     * <P>
     * Note:  with some pluggable look and feels, this method may
     * be fully or partially ignored.  It works as expected with the
     * default look and feel.
     * @param color the color; null for the default color as modified by
     *        the last call (if any) to {@link #setBackground(Color)}
     */
    public void setHtmlPaneScrollbarBackground(Color color) {
	htmlPane.setScrollbarBackground(color);
    }

    /**
     * Set the background color for the 'start', 'back', 'reload',
     * 'forward', and 'end' controls.
     * When rvmode is true, the icons will be white when enabled
     * and grey when disabled, the reverse from the normal behavior.
     * <P>
     * Note:  with some pluggable look and feels, this method may
     * be fully or partially ignored.  It works as expected with the
     * default look and feel.
     * @param color the color
     * @param rvmode true for reverse video; false otherwise
     */
    public void setHtmlButtonBackground(Color color, boolean rvmode) {
	htmlPane.setButtonBackground(color, rvmode);
    }


    BasicSplitPaneUI origspui = null;
    BasicSplitPaneUI newspui = null;
    Color spcolor = null;

    /**
     * Set the background color of the splitter (or divider).
     * <P>
     * Note: with some pluggable look and feels, this method may
     * be fully or partially ignored.  It works as expected with the
     * default look and feel.
     * @param color the color
     */
    public void setSplitterBackground(Color color) {
	if (spcolor == null && color == null) return;
	SplitPaneUI spui = splitPane.getUI();
	if (spui instanceof BasicSplitPaneUI) {
	    if (origspui == null) {
		BasicSplitPaneUI bspui = (BasicSplitPaneUI) spui;
		origspui = bspui;
		newspui = new BasicSplitPaneUI() {
			public BasicSplitPaneDivider createDefaultDivider() {
			    return new BasicSplitPaneDivider(this) {
				@Override
				public void paint(Graphics g) {
				    Color c = g.getColor();
				    try {
					g.setColor(spcolor);
					Dimension size = getSize();
					g.fillRect(0, 0,
						   size.width, size.height);
					super.paint(g);
				    } finally {
					g.setColor(c);
				    }
				}
			    };
			}
		    };
	    }
	}
	boolean nosetUI = false;
	if (color == null && spui == newspui) {
	    BasicSplitPaneDivider divider =  newspui.getDivider();
	    BasicSplitPaneDivider divider2 =  origspui.getDivider();
	    Border b = divider.getBorder();
	    if (divider2 != null) {
		divider2.setBorder(b);
	    }
	    splitPane.setUI(origspui);
	} else if (color != null && spui == origspui) {
	    BasicSplitPaneDivider divider =  origspui.getDivider();
	    BasicSplitPaneDivider divider2 =  newspui.getDivider();
	    Border b = divider.getBorder();
	    if (divider2 != null) {
		divider2.setBorder(b);
	    }
	    splitPane.setUI(newspui);
	} else {
	    nosetUI = true;
	}
	spcolor = color;
	if (nosetUI && splitPane.isVisible()) splitPane.repaint();
    }


    /**
     * Set the background color for the HTML pane.
     * <P>
     * Note: sometimes the background color for the HTML pane
     * will be visible such as when the HTML code is being loaded
     * and this method can produce a suitable color for this case.
     * <P>
     * Note:  with some pluggable look and feels, this method may
     * be fully or partially ignored.  It works as expected with the
     * default look and feel.
     * @param color the color
     */
    public void setHtmlPaneBackground(Color color) {
	htmlPane.setBackground(color);
    }

    /**
     * Reset the split pane to the preferred sizes.
     * This must be called after individual preferred sizes are set
     * for those to have any effect. The divider may move as a result
     * of using this method.
     */
    public void resetToPreferredSizes() {
	splitPane.resetToPreferredSizes();
    }

    /**
     * Sets the divider location as a percentage of the HtmlWithTocPane's size.
     * <p>
     * This method is implemented in terms of {@link #setDividerLocation(int)}.
     * This method immediately changes the size of the split pane
     * based on its current size. If the split pane is not correctly
     * realized and on screen, this method will have no effect (new
     * divider location will become (current size * proportionalLocation) 
     * which is 0).
     * @param proportionalLocation a double-precision floating point 
     * value that specifies a percentage, from zero (The TOC side of the
     * component) to 1.0 (the HTML Pane side of the component)
     */
    public void setDividerLocation(double proportionalLocation) {
	splitPane.setDividerLocation(proportionalLocation);
    }

    /**
     * Returns the last value passed to setDividerLocation.  The
     * value returned from this method may differ from the actual
     * divider location (if setDividerLocation was passed a value
     * bigger than the current size).
     * @return an integer specifying the location of the divider
     */
    public int getDividerLocation() {
	return splitPane.getDividerLocation();
    }

    /**
     * Sets the location of the divider. 
     * This is passed off to the look and feel implementation, and
     * then listeners are notified. A value less than 0 implies the
     * divider should be reset to a value that attempts to honor the
     * preferred size of the TOC component.
     * @param location  an integer specifying the location of the divider
     * measured from the right or left hand edge adjacent to the TOC pane.
     */
    public void setDividerLocation(int location) {
	splitPane.setDividerLocation(location);
    }


    /**
     * Specifies how to distribute extra space when the size of 
     * the split pane changes. 
     * A value of 0, the default, indicates the right/bottom component
     * gets all the extra space (the left/top component acts fixed),
     * where as a value of 1 specifies the left/top component gets all
     * the extra space (the right/bottom component acts
     * fixed). Specifically, the left/top component gets (weight *
     * diff) extra space and the right/bottom component gets (1 -
     * weight) * diff extra space.
     * @param weight as described above
     */
    public void  setResizeWeight(double weight) {
	splitPane.setResizeWeight(weight);
    }

    /**
     * Returns the number that determines how extra space is distributed.
     * @return how extra space is to be distributed on a resize of the 
     * split pane contained in this component.
     */
    public double getResizeWeight() {
	return splitPane.getResizeWeight();
    }

    private void finishInit(boolean hasPage) {
	tocScrollPane = new JScrollPane(tocPane);
	htmlPane = new HtmlPane();
	splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				   tocScrollPane, htmlPane);
	splitPane.setOneTouchExpandable(true);
	if (hasPage) {
	    int tocwidth = tocScrollPane.getPreferredSize().width;
	    splitPane.setDividerLocation(tocwidth + 5);
	}
	splitPane.setResizeWeight((0.0));
	tocPane.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    ObjTocPane.Entry entry = (ObjTocPane.Entry)
			e.getSource();
		    if (entry == null || entry.getValue() == null) return;
		    try {
			htmlPane.setPage((URL)entry.getValue());
			int tocwidth = tocScrollPane.getPreferredSize().width;
			splitPane.setDividerLocation(tocwidth+5);
		    } catch (java.io.IOException ee) {
			JOptionPane.
			    showMessageDialog(splitPane,
					      ee.getMessage(),
					      "Loading Error",
					      JOptionPane.ERROR_MESSAGE);
		    }
		}
	    });
	setLayout(new BorderLayout());
	add(splitPane, "Center");
    }


    public void addEntry(String title, Object obj) {
	tocPane.addEntry(title, obj);
    }

    public void addEntry (String title, String url) 
	throws MalformedURLException
    {
	tocPane.addEntry(title, url);
    }
    
    public void addEntry(String title, URL url)
	throws MalformedURLException  
    {
	tocPane.addEntry(title, url);
    }

    @Override
    public void nextLevel() {tocPane.nextLevel();}

    @Override
    public void prevLevel() {tocPane.prevLevel();}

    boolean needDLM = true;
    private PropertyChangeListener dml = evnt -> {
	splitPane.setDividerLocation(tocPane.getPreferredSize().width);
    };


    @Override
    public void entriesCompleted() {
	tocPane.entriesCompleted();
	if (needDLM) {
	    DarkmodeMonitor.addPropertyChangeListener(dml);
	    needDLM = false;
	}
    }

    @Override
    public void entriesCompleted(boolean expand) {
	tocPane.entriesCompleted(expand);
	if (needDLM) {
	    DarkmodeMonitor.addPropertyChangeListener(dml);
	    needDLM = false;
	}
    }

    @Override
    public void setSelectionWithAction(int row) {
	tocPane.setSelectionWithAction(row);
    }

    @Override
    public void clearSelection() {
	tocPane.clearSelection();
    }

    @Override
    public void addActionListener(ActionListener l) {
	tocPane.addActionListener(l);
    }

    @Override
    public void removeActionListener(ActionListener l) {
	tocPane.removeActionListener(l);
    }

    @Override
    public void collapseRow(int row) {
	tocPane.collapseRow(row);
    }

    @Override
    public void expandRow(int row) {
	tocPane.expandRow(row);
    }

    @Override
    public boolean isCollapsed(int row) {
	return tocPane.isCollapsed(row);
    }

    @Override
    public boolean isExpanded(int row) {
	return tocPane.isExpanded(row);
    }

    @Override
    public void clearToc() {
	tocPane.clearToc();
	if (needDLM == false) {
	    DarkmodeMonitor.removePropertyChangeListener(dml);
	    needDLM = true;
	}
    }

    @Override
    public void setToc(URL url, boolean expand, boolean validating)
	throws FactoryConfigurationError, SAXException, IOException,
	       ParserConfigurationException, MalformedURLException
    {
	tocPane.setToc(url, expand, validating);
	setDividerLocation(tocPane.getPreferredSize().width);
	if (needDLM) {
	    DarkmodeMonitor.addPropertyChangeListener(dml);
	    needDLM = false;
	}
    }

    @Override
    public void setToc(String url, boolean expand, boolean validating)
	throws FactoryConfigurationError, SAXException, IOException,
	       ParserConfigurationException
    {
	tocPane.setToc(url, expand, validating);
	setDividerLocation(tocPane.getPreferredSize().width);
	if (needDLM) {
	    DarkmodeMonitor.addPropertyChangeListener(dml);
	    needDLM = false;
	}
    }

    @Override
    public void setToc(InputStream is, boolean expand, boolean validating)
	throws FactoryConfigurationError, SAXException, IOException,
	       ParserConfigurationException
    {
	tocPane.setToc(is, expand, validating);
	setDividerLocation(tocPane.getPreferredSize().width);
	if (needDLM) {
	    DarkmodeMonitor.addPropertyChangeListener(dml);
	    needDLM = false;
	}
    }

    /**
     * Set the title to use in error message dialog boxes associated
     * with the HTML pane.
     * This title will be used as a title for various dialog boxes.
     * @param title the title to display.
     */


    /**
     * Get the title used in error message dialog boxes  associated
     * with the HTML pane.
     * @return the current title.
     */
    public final String getHtmlErrorTitle() {
	return htmlPane.getErrorTitle();
    }

    /**
     * Get the tre cell renderer for the table of contents.
     * @return the cell renderer
     */
    public DefaultTreeCellRenderer getTocCellRenderer() {
	return (DefaultTreeCellRenderer) tocPane.getCellRenderer();
    }

    /**
     * Get the background color for the table of contents.
     * @return the background color for the table of contents (if none
     *         has been explicitly set, the background color of the
     *         TOC component's parent is returned)
     *
     */
    public final Color getTocBackgound() {
	return tocPane.getBackground();
    }

    /**
     * Set the background color for the table of contents.
     * <P>
     * Note:  with some pluggable look and feels, this method may
     * be fully or partially ignored.  It works as expected with the
     * default look and feel.
     * @param c the background color; null if the parent component's color
     *        should be used.
     */
    public final void setTocBackground(Color c) {
	tocPane.setBackground(c);
    }

    /**
     * Get the foreground color for the table of contents.
     * @return the foreground color for the table of contents (if none
     *         has been explicitly set, the foreground color of the
     *         TOC component's parent is returned)
     *
     */
    public final Color getTocForeground() {
	return tocPane.getForeground();
    }

    /**
     * Set the foreground color for the table of contents.
     * <P>
     * Note:  with some pluggable look and feels, this method may
     * be fully or partially ignored.  It works as expected with the
     * default look and feel.
     * @param c the foreground color; null if the parent component's color
     *        should be used.
     */
    public final void setTocForeground(Color c) {
	tocPane.setForeground(c);
    }

    /**
     * Set the background color for items that are selected in the
     * table of contents.
     * <P>
     * Note:  with some pluggable look and feels, this method may
     * be fully or partially ignored.  It works as expected with the
     * default look and feel.
     * @param c the color
     */
    public void setTocBackgroundSelectionColor(Color c) {
	tocPane.setTCRBackgroundSelectionColor(c);
    }

    /**
     * Set the background color for items that are not selected in the
     * table of contents.
     * <P>
     * Note:  with some pluggable look and feels, this method may
     * be fully or partially ignored.  It works as expected with the
     * default look and feel.
     * @param c the color
     */
    public void setTocBackgroundNonSelectionColor(Color c) {
	tocPane.setTCRBackgroundNonSelectionColor(c);
    }

    /**
     * Set the color of text describing items that are selected in
     * the table of contents.
     * <P>
     * Note:  with some pluggable look and feels, this method may
     * be fully or partially ignored.  It works as expected with the
     * default look and feel.
     * @param c the color
     */
    public void setTocTextSelectionColor(Color c) {
	tocPane.setTCRTextSelectionColor(c);
    }

    /**
     * Set the color of text describing items that are not selected in
     * the table of contents.
     * <P>
     * Note:  with some pluggable look and feels, this method may
     * be fully or partially ignored.  It works as expected with the
     * default look and feel.
     * @param c the color
     */
    public void setTocTextNonSelectionColor(Color c) {
	tocPane.setTCRTextNonSelectionColor(c);
    }

    /**
     * Set the font for the entries in table of contents.
     * <P>
     * Note:  with some pluggable look and feels, this method may
     * be fully or partially ignored.  It works as expected with the
     * default look and feel.
     * @param font the font
     */
    public final void setTocTCRFont(Font font) {
	tocPane.setTCRFont(font);
    }
}

//  LocalWords:  exbundle DTD blockquote pre lt toc ATTLIST CDATA uri
//  LocalWords:  href DOCTYPE runtime HtmlWithTocPane JSplitPane url
//  LocalWords:  setSelectionWithAction Javadoc Zaumen SAXException
//  LocalWords:  FactoryConfigurationError IOException preferredSize
//  LocalWords:  ParserConfigurationException HtmlWithTocPane's
//  LocalWords:  setDividerLocation proportionalLocation
