package org.bzdev.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.GraphicsConfiguration;
import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
// import java.awt.geom.Point2D;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.*;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Stack;

//@exbundle org.bzdev.swing.lpack.Swing

/**
 * A widget for displaying HTML documents.
 * This class wraps a {@link JEditorPane} in a {@link JScrollPane} and
 * provides a set of buttons for navigation.  Links that appear in the
 * HTML may be clicked to visit a URL.  The buttons provide the
 * following functions.
 * <ul>
 *   <li>The "start" button takes you to the URL provided by the
 *       <code>setPage</code> method.  It is equivalent to the maximum
 *       possible number of repetitions of the "back" button.
 *   <li> The "back" button returns to the page that was last visited
 *        by clicking on a link.
 *   <li> The "forw" button moves forward, undoing the effects of pushing
 *        the "back" button.
 *   <li> The "end" button is equivalent to the repeated use of the
 *        "forw" button.
 *   <li> The "reload" button reloads the link currently visited.
 * </ul> 
 * Note: the use of these buttons does not restore any scrolling that
 * the user may have performed.  The buttons contain icons instead of
 * text, but have text-based tooltips. The icons are in resources named
 * org/bzdev/swing/icons/fleft.gif, org/bzdev/swing/icons/left.gif,
 * org/bzdev/swing/icons/redo.gif, org/bzdev/swing/icons/right.gif,
 * org/bzdev/swing/icons/fright.gif, and org/bzdev/swing/icons/rlredo.gif.
 * <p>
 * The MIME types supported are those that a {@link JEditorPane}
 * supports.
 * <p>
 * Internationalization is handled by the resource
 * <code>org/bzdev/swing/HtmlPaneBundle.properties</code>.  This defines
 * the strings startText, backText,  reloadText, frwdText, endText. as
 * values to use in buttons if icons are not provided.  The strings
 * startTip, backTip, reloadTip, frwdTip, and endTip provide corresponding
 * tool tips.  Finally, the string direction indicates if the components
 * should be laid out LR (left to right) or RL (right to left.) This
 * also has an effect on the how icons are assigned to buttons.  The
 * icon rlredo.gif is the mirror image of redo.gif and is provided for
 * right-to-left layouts.
 * <P>
 * One can set the HTML page displayed by calling one of the setPage
 * methods or by using a constructor that takes a URL or a String
 * specifying a URL as an argument.
 * <P>
 * This component allows HTML pages to be displayed but does not allow
 * them to be edited.
 * <P>
 * A few methods allow one to set colors to implement a "dark mode" for
 * viewing HTML pages in reverse video.  This does change an HTML page's
 * appearance: to change it (e.g., to make the text white and the background
 * dark), a style sheet can be used.
 * <P>
 * The behavior specified for JEditorPane in Java 11 is such that the
 * constuctor {@link HtmlPane#HtmlPane()} configure a JEditorPane so
 * that {@link JEditorPane#getPage()} will return null even if
 * {@link HtmlPane#setPage(URL)} or {@link HtmlPane#setPage(String)} was
 * called. This may affect address resolution.  The method
 * {@link HtmlPane#getPage()} always returns the page that was last set.
 * @author Bill Zaumen
 */
public class HtmlPane extends JComponent {

    static String errorMsg(String key, Object... args) {
	return SwingErrorMsg.errorMsg(key, args);
    }

    private class StackElement {
	URL url;
	HTMLFrameHyperlinkEvent event;
	Point position = null;
	StackElement(URL url, HTMLFrameHyperlinkEvent env) {
	    this.url = url;
	    event = env;
	}
	StackElement(URL url, Point p) {
	    this.url = url;
	    event = null;
	    position = p;
	}
    }
    private HtmlPane me = null;
    private JEditorPane editorPane = null /*new JEditorPane() */;
    private JScrollPane editorScrollPane = null /*new JScrollPane(editorPane)*/;
    JPanel top = new JPanel();

    public void setLocale(Locale locale) {
	super.setLocale(locale);
	editorScrollPane.setLocale(locale);
	editorPane.setLocale(locale);
    }

    /** Set the local of the content pane.
     * This method allows the locale of the content panes (the HTML
     * pane) to be set without changing the layout of navigation
     * controls.
     * @param locale the locale.
     */
    public void setContentLocale(Locale locale) {
	editorPane.setLocale(locale);
    }

    private KeyAdapter editorKeyAdapter = new KeyAdapter() {
	    private Rectangle toRect(Rectangle2D r) {
		double x = r.getX();
		double y = r.getY();
		double w = r.getWidth();
		double h = r.getHeight();
		int ix = (int)Math.round(x);
		int iy = (int)Math.round(y);
		int iw =(int)Math.ceil(w);
		int ih =(int)Math.ceil(h);
		return new Rectangle(ix, iy, iw, ih);
	    }

	    private Point2D toPoint2D(Point p) {
		return new Point2D.Double(p.getX(), p.getY());
	    }


	    public void keyPressed(KeyEvent e) {
		int keycode = e.getKeyCode();
		int mod = e.getModifiersEx();
		if (mod != 0) return;
		switch (keycode) {
		case KeyEvent.VK_HOME:
		    editorPane.setCaretPosition(0);
		    try {
			Rectangle r = new Rectangle(0, 0, 0, 0);
			editorPane.scrollRectToVisible(r);
		    } catch (Exception badloc) {}
		    break;
		case KeyEvent.VK_END:
		    int len = editorPane.getDocument().getLength();
		    editorPane.setCaretPosition(len);
		    try {
			Rectangle r = toRect(editorPane.modelToView2D(len));
			editorPane.scrollRectToVisible(r);
		    } catch (Exception badloc) {}
		    break;
		case KeyEvent.VK_DOWN:
		    try {
			Rectangle r = editorScrollPane.getViewport()
			    .getViewRect();
			Point2D p = new Point2D
			    .Double(r.getX(),r.getY() + r.getHeight());
			int pos = editorPane.viewToModel2D(p);
			Rectangle nr = toRect(editorPane.modelToView2D(pos));
			int dist = nr.height;
			if (dist == 0) {
			    dist = 10;
			} else if (dist > 20) {
			    dist = 20;
			}
			r.translate(0, dist);
			editorPane.scrollRectToVisible(r);
		    } catch (Exception badloc) {}
		    break;
		case KeyEvent.VK_UP:
		    try {
			Rectangle r = editorScrollPane.getViewport()
			    .getViewRect();
			Point2D p = toPoint2D(editorScrollPane.getViewport()
					      .getViewPosition());
			int pos = editorPane.viewToModel2D(p);
			Rectangle nr = toRect(editorPane.modelToView2D(pos));
			int dist = nr.height;
			if (dist == 0) {
			    dist = 10;
			} else if (dist > 20) {
			    dist = 20;
			}
			int y1 = r.y;
			int y2 = y1 - dist;
			if (y2 < 0) y2 = 0;
			r.translate(0, y2 - y1);
			editorPane.scrollRectToVisible(r);
		    } catch (Exception badloc) {}
		    break;
		case KeyEvent.VK_PAGE_DOWN:
		    try {
			Rectangle r = editorScrollPane.getViewport()
			    .getViewRect();
			Rectangle r1 = new Rectangle(r.x, r.y, r.width, 0);
			r1.translate(0, r.height);
			editorPane.scrollRectToVisible(r1);
			r1 = editorScrollPane.getViewport()
			    .getViewRect();
			if (r1.y == r.y) {
			    // did not move: have to fix it up
			    int max = editorPane.getDocument().getLength();
			    Rectangle lr =
				toRect(editorPane.modelToView2D(max));
			    int ymax = lr.y + lr.height;
			    Point2D p = new Point2D
				.Double(r.getX(),r.getY() + r.getHeight());
			    int pos = editorPane.viewToModel2D(p) ;
			    Rectangle r2 = r1;
			    do {
				r2.translate(0, 10);
				editorPane.scrollRectToVisible(r2);
				/*
				pos++;
				r1 = toRect(editorPane.modelToView2D(pos));
				editorPane.scrollRectToVisible(r1);
				*/
				r1 = editorScrollPane.getViewport()
				    .getViewRect();
			    } while (r1.y == r.y && r2.y < ymax);
			}
		    } catch (Exception badloc) {}
		    break;
		case KeyEvent.VK_PAGE_UP:
		    try {
			Rectangle r = editorScrollPane.getViewport()
			    .getViewRect();
			if (r.y == 0) return;
			Rectangle r1  = new Rectangle(r.x, r.y, r.width, 0);
			r1.translate(0, -r.height);
			editorPane.scrollRectToVisible(r1);
			r1 = editorScrollPane.getViewport()
			    .getViewRect();
			if (r1.y == r.y) {
			    // did not move: have to fix it up
			    Point2D p = new Point2D
				.Double(r.getX(),r.getY() - r.getHeight());
			    // int pos = editorPane.viewToModel2D(p) - 1;
			    // r1 = toRect(editorPane.modelToView2D(pos));
			    // editorPane.scrollRectToVisible(r1);
			    // int pos = editorPane.viewToModel2D(p);
			    Rectangle r2 = r1;
			    do {
				r2.translate(0, -10);
				editorPane.scrollRectToVisible(r1);
				/*
				pos--;
				r1 = toRect(editorPane.modelToView2D(pos));
				editorPane.scrollRectToVisible(r1);
				*/
				r1 = editorScrollPane.getViewport()
				    .getViewRect();
			    } while (r1.y == r.y && r2.y > 0);
			}
		    } catch (Exception badloc) {}
		    break;
		}
	    }
	};
    // private Stack urlstack = new Stack();
    // private Stack revUrlstack = new Stack();
    private Stack<StackElement> urlstack = new Stack<StackElement>();
    private Stack<StackElement> revUrlstack = new Stack<StackElement>();
    private StackElement currentElement = null;

    // use for internationalization - appears in tooltips.
    private static String startTip = "startTip";
    private static String backTip = "backTip";
    private static String reloadTip = "reloadTip";
    private static String frwdTip = "frwdTip";
    private static String endTip = "endTip";

    // internationalization:  button strings;
    final private static String startText = "startText";
    final private static String backText = "backText";
    final private static String reloadText = "reloadText";
    final private static String frwdText = "frwdText";
    final private static String endText = "endText";

    // internationalization: direction
    final private String direction = "direction";
    final private String bundleName = "org.bzdev.swing.lpack.HtmlPaneBundle";

    ImageIcon frightIcon = null;
    ImageIcon rightIcon = null;
    ImageIcon reloadIcon = null;
    ImageIcon rlReloadIcon = null;
    ImageIcon leftIcon = null;
    ImageIcon fleftIcon = null;

    ImageIcon frightRVIcon = null;
    ImageIcon rightRVIcon = null;
    ImageIcon reloadRVIcon = null;
    ImageIcon rlReloadRVIcon = null;
    ImageIcon leftRVIcon = null;
    ImageIcon fleftRVIcon = null;

    ImageIcon frightRVDIcon = null;
    ImageIcon rightRVDIcon = null;
    ImageIcon reloadRVDIcon = null;
    ImageIcon rlReloadRVDIcon = null;
    ImageIcon leftRVDIcon = null;
    ImageIcon fleftRVDIcon = null;

    private boolean iconsInitialized = false;

    private void initIcons() {
	if (iconsInitialized) return;
	AccessController.doPrivileged
	    (new PrivilegedAction<Void>() {
		    public Void run() {
			URL frightUrl = HtmlPane.class
			    .getResource("/org/bzdev/swing/icons/fright.gif");
			URL rightUrl = HtmlPane.class
			    .getResource("/org/bzdev/swing/icons/right.gif");
			URL reloadUrl = HtmlPane.class
			    .getResource("/org/bzdev/swing/icons/redo.gif");
			URL rlReloadUrl = HtmlPane.class
			    .getResource("/org/bzdev/swing/icons/rlredo.gif");
			URL leftUrl = HtmlPane.class
			    .getResource("/org/bzdev/swing/icons/left.gif");
			URL fleftUrl = HtmlPane.class
			    .getResource("/org/bzdev/swing/icons/fleft.gif");

			URL frightRVUrl = HtmlPane.class
			    .getResource("/org/bzdev/swing/icons/frightRV.gif");
			URL rightRVUrl = HtmlPane.class
			    .getResource("/org/bzdev/swing/icons/rightRV.gif");
			URL reloadRVUrl = HtmlPane.class
			    .getResource("/org/bzdev/swing/icons/redoRV.gif");
			URL rlReloadRVUrl = HtmlPane.class
			    .getResource("/org/bzdev/swing/icons/rlredoRV.gif");
			URL leftRVUrl = HtmlPane.class
			    .getResource("/org/bzdev/swing/icons/leftRV.gif");
			URL fleftRVUrl = HtmlPane.class
			    .getResource("/org/bzdev/swing/icons/fleftRV.gif");


			URL frightRVDUrl = HtmlPane.class.getResource
			    ("/org/bzdev/swing/icons/frightRVD.gif");
			URL rightRVDUrl = HtmlPane.class
			    .getResource("/org/bzdev/swing/icons/rightRVD.gif");
			URL reloadRVDUrl = HtmlPane.class
			    .getResource("/org/bzdev/swing/icons/redoRVD.gif");
			URL rlReloadRVDUrl = HtmlPane.class.getResource
			    ("/org/bzdev/swing/icons/rlredoRVD.gif");
			URL leftRVDUrl = HtmlPane.class
			    .getResource("/org/bzdev/swing/icons/leftRVD.gif");
			URL fleftRVDUrl = HtmlPane.class
			    .getResource("/org/bzdev/swing/icons/fleftRVD.gif");

			/*
			URL frightUrl = ClassLoader.getSystemClassLoader()
			    .getResource("org/bzdev/swing/icons/fright.gif");
			URL rightUrl = ClassLoader.getSystemClassLoader()
			    .getResource("org/bzdev/swing/icons/right.gif");
			URL reloadUrl = ClassLoader.getSystemClassLoader()
			    .getResource("org/bzdev/swing/icons/redo.gif");
			URL rlReloadUrl = ClassLoader.getSystemClassLoader()
			    .getResource("org/bzdev/swing/icons/rlredo.gif");
			URL leftUrl = ClassLoader.getSystemClassLoader()
			    .getResource("org/bzdev/swing/icons/left.gif");
			URL fleftUrl = ClassLoader.getSystemClassLoader()
			    .getResource("org/bzdev/swing/icons/fleft.gif");
			*/
			frightIcon = (frightUrl == null)? null:
			    new ImageIcon(frightUrl);
			rightIcon = (frightUrl == null)? null:
			    new ImageIcon(rightUrl);
			reloadIcon = (reloadUrl == null)? null:
			    new ImageIcon(reloadUrl);
			rlReloadIcon = (rlReloadUrl == null)? null:
			    new ImageIcon(rlReloadUrl);
			leftIcon = (frightUrl == null)? null:
			    new ImageIcon(leftUrl);
			fleftIcon = (fleftUrl == null)? null:
			    new ImageIcon(fleftUrl);

			frightRVIcon = (frightRVUrl == null)? null:
			    new ImageIcon(frightRVUrl);
			rightRVIcon = (frightRVUrl == null)? null:
			    new ImageIcon(rightRVUrl);
			reloadRVIcon = (reloadRVUrl == null)? null:
			    new ImageIcon(reloadRVUrl);
			rlReloadRVIcon = (rlReloadRVUrl == null)? null:
			    new ImageIcon(rlReloadRVUrl);
			leftRVIcon = (frightRVUrl == null)? null:
			    new ImageIcon(leftRVUrl);
			fleftRVIcon = (fleftRVUrl == null)? null:
			    new ImageIcon(fleftRVUrl);

			frightRVDIcon = (frightRVDUrl == null)? null:
			    new ImageIcon(frightRVDUrl);
			rightRVDIcon = (frightRVDUrl == null)? null:
			    new ImageIcon(rightRVDUrl);
			reloadRVDIcon = (reloadRVDUrl == null)? null:
			    new ImageIcon(reloadRVDUrl);
			rlReloadRVDIcon = (rlReloadRVDUrl == null)? null:
			    new ImageIcon(rlReloadRVDUrl);
			leftRVDIcon = (frightRVDUrl == null)? null:
			    new ImageIcon(leftRVDUrl);
			fleftRVDIcon = (fleftRVDUrl == null)? null:
			    new ImageIcon(fleftRVDUrl);

			return (Void)null;
		    }
		});
	iconsInitialized = true;
    }

    private JButton startButton;
    private JButton backButton;
    private JButton reloadButton;
    private JButton frwdButton;
    private JButton endButton;

    HTMLFrameHyperlinkEvent event = null;
    private HyperlinkListener hl = new HyperlinkListener () {
	    public void hyperlinkUpdate(HyperlinkEvent e) {
		try {
		    JEditorPane pane = (JEditorPane) e.getSource();
		    URL current = /* pane. */getPage();
		    if (e.getEventType() == 
			HyperlinkEvent.EventType.ACTIVATED) {
			if (e instanceof HTMLFrameHyperlinkEvent) {
			    HTMLFrameHyperlinkEvent  evt = 
				(HTMLFrameHyperlinkEvent)e;
			    HTMLDocument doc =
				(HTMLDocument)pane.getDocument();
			    doc.processHTMLFrameHyperlinkEvent(evt);
			    urlstack.push(new StackElement(current, event));
			    revUrlstack.clear();
			    startButton.setEnabled(true);
			    backButton.setEnabled(true);
			    frwdButton.setEnabled(false);
			    endButton.setEnabled(false);
			    event = evt;
			} else {
			    URL newpage = e.getURL();
			    if (newpage.equals(page)) {
				pane.setPage(newpage);
				event = null;
				return;
			    }
			    Point p = editorScrollPane.getViewport()
				.getViewPosition();
			    // so we have the current position when we
			    // clicked the link
			    currentElement = new StackElement(page, p);
			    urlstack.push(currentElement);
			    pane.setPage(e.getURL());
			    page = e.getURL();
			    position = editorScrollPane.getViewport()
				.getViewPosition();
			    // urlstack.push(new StackElement(current, p));
			    // The position field in the stack element is
			    // null because the actual value is not
			    // available immediately.  Instead, it is
			    // determined using a property chagne listener.
			    currentElement = new StackElement(page,
							      (Point) null);
			    revUrlstack.clear();
			    startButton.setEnabled(true);
			    backButton.setEnabled(true);
			    frwdButton.setEnabled(false);
			    endButton.setEnabled(false);
			    event = null;
			}
		    }
		} catch (Throwable t) {
		    String msg = errorMsg("cannotOpenURL", e.getURL());
		    JOptionPane.showMessageDialog(me,
						  msg,
						  errorTitle,
						  JOptionPane.ERROR_MESSAGE);
		}
	    }
	};

    private void setPageAux() {
	urlstack.clear();
	revUrlstack.clear();
	reloadButton.setEnabled(true);
	startButton.setEnabled(false);
	frwdButton.setEnabled(false);
	backButton.setEnabled(false);
	endButton.setEnabled(false);
    }

    /**
     * Set the page to display and display it.
     * @param page the URL of the page to display.
     * @throws IOException an error occurred reading the page.
     */
    public void setPage(String page) throws IOException {
	setPage (new URL(page));
    }

    // Java 1.4.1 does not keep track of the reference or fragment
    // field when setPage is called, so we keep track of it just in
    // case.
    private URL page = null;
    private Point position = null;

    private void finishSetPage(URL page) {
	this.page = page;
	this.position = editorScrollPane.getViewport().getViewPosition();
	currentElement = new StackElement(this.page, this.position);
	setPageAux();
    }


    /**
     * Set the page to display and display it.
     * @param page the URL of the page to display.
     * @throws IOException an error occurred reading the page.
     */
    public void setPage(URL page) throws IOException {
	editorPane.setPage(page);
	finishSetPage(page);
    }

    private boolean backgroundSet = false;
    private boolean scrollbarColorSet = false;

    /**
     * Set the background color.
     * This method will also set the background color of scroll bars
     * unless the last call to {@link #setScrollBarBackground(Color)},
     * if one was made, had a non-null argument.
     * @param color the color
     */
    @Override
    public void setBackground(Color color) {
	super.setBackground(color);
	top.setBackground(color);
	editorPane.setBackground(color);
	editorScrollPane.setBackground(color);
	if (!scrollbarColorSet) {
	    editorScrollPane.getVerticalScrollBar().setBackground(color);
	    editorScrollPane.getHorizontalScrollBar().setBackground(color);
	}
	backgroundSet = (color != null);
    }

    /**
     * Set the background color for scroll bars.
     * If color is null and the last call to {@link #setBackground(Color)},
     * if one was made, had a non-null argument, that background color will
     * be used.
     * @param color the color; null for the default color
     */
    public void setScrollBarBackground(Color color) {
	if (backgroundSet && color == null) {
	    Color c = getBackground();
	    editorScrollPane.getVerticalScrollBar().setBackground(c);
	    editorScrollPane.getHorizontalScrollBar().setBackground(c);
	} else {
	    editorScrollPane.getVerticalScrollBar().setBackground(color);
	    editorScrollPane.getHorizontalScrollBar().setBackground(color);
	}
	scrollbarColorSet = (color != null);
    }

    /**
     * get the URL for the page being displayed.
     * @return the URL for the page being displayed;  null if there is none.
     */
    public URL getPage() {return page /*editorPane.getPage()*/;}

    /**
     * The string that will be displayed as a frame title when a dialog
     * box reporting an error is displayed.  The default is obtained
     * from the resource bundle org.bzdev.swing.lpack.HtmlPaneBundle
     */
    protected String errorTitle;

    /**
     * Set the title to use in error message dialog boxes.
     * This title will be used as a title for various dialog boxes.
     * @param title the title to display; null to use the default
     *        (which is locale specific)
     */
    public final void setErrorTitle (String title) {
	if (title == null) {
	    ResourceBundle bundle =
		ResourceBundle.getBundle(bundleName, getLocale());
	    errorTitle = bundle.getString("errorTitle");
	} else {
	    errorTitle = title;
	}
    }

    /**
     * Get the title used in error message dialog boxes.
     * @return the current title.
     */
    public final String getErrorTitle() {return errorTitle;}

    boolean lrmode = true;
    public void setComponentOrientation(ComponentOrientation o) {
	initIcons();
	boolean oldlrmode = lrmode;
	super.setComponentOrientation(o);

	// editorScrollPane.applyComponentOrientation(o);
	// top.applyComponentOrientation(o);
	lrmode = o.isLeftToRight();


	if (lrmode != oldlrmode) {
	    /*
	     * change icons.
	     */
	    ResourceBundle bundle = 
		ResourceBundle.getBundle(bundleName, getLocale());
	    top.remove(startButton);
	    top.remove(backButton);
	    top.remove(reloadButton);
	    top.remove(frwdButton);
	    top.remove(endButton);
	    startButton.setIcon(null);
	    backButton.setIcon(null);
	    reloadButton.setIcon(null);
	    frwdButton.setIcon(null);
	    endButton.setIcon(null);
	    startButton.setDisabledIcon(null);
	    backButton.setDisabledIcon(null);
	    reloadButton.setDisabledIcon(null);
	    frwdButton.setDisabledIcon(null);
	    endButton.setDisabledIcon(null);
	    if (lrmode) {
		if (rvmode) {
		    startButton.setIcon(fleftRVIcon);
		    backButton.setIcon(leftRVIcon);
		    reloadButton.setIcon(reloadRVIcon);
		    frwdButton.setIcon(rightRVIcon);
		    endButton.setIcon(frightRVIcon);

		    startButton.setDisabledIcon(fleftRVDIcon);
		    backButton.setDisabledIcon(leftRVDIcon);
		    reloadButton.setDisabledIcon(reloadRVDIcon);
		    frwdButton.setDisabledIcon(rightRVDIcon);
		    endButton.setDisabledIcon(frightRVDIcon);
		} else {
		    startButton.setIcon(fleftIcon);
		    backButton.setIcon(leftIcon);
		    reloadButton.setIcon(reloadIcon);
		    frwdButton.setIcon(rightIcon);
		    endButton.setIcon(frightIcon);

		    startButton.setDisabledIcon(null);
		    backButton.setDisabledIcon(null);
		    reloadButton.setDisabledIcon(null);
		    frwdButton.setDisabledIcon(null);
		    endButton.setDisabledIcon(null);
		}
	    } else {
		if (rvmode) {
		    startButton.setIcon(frightRVIcon);
		    backButton.setIcon(rightRVIcon);
		    reloadButton.setIcon(rlReloadRVIcon);
		    frwdButton.setIcon(leftRVIcon);
		    endButton.setIcon(fleftRVIcon);

		    startButton.setDisabledIcon(frightRVDIcon);
		    backButton.setDisabledIcon(rightRVDIcon);
		    reloadButton.setDisabledIcon(rlReloadRVDIcon);
		    frwdButton.setDisabledIcon(leftRVDIcon);
		    endButton.setDisabledIcon(fleftRVDIcon);
		} else {
		    startButton.setIcon(frightIcon);
		    backButton.setIcon(rightIcon);
		    reloadButton.setIcon(rlReloadIcon);
		    frwdButton.setIcon(leftIcon);
		    endButton.setIcon(fleftIcon);

		    startButton.setDisabledIcon(null);
		    backButton.setDisabledIcon(null);
		    reloadButton.setDisabledIcon(null);
		    frwdButton.setDisabledIcon(null);
		    endButton.setDisabledIcon(null);
		}
	    }
	    startButton.setToolTipText(bundle.getString(startTip));
	    backButton.setToolTipText(bundle.getString(backTip));
	    reloadButton.setToolTipText(bundle.getString(reloadTip));
	    frwdButton.setToolTipText(bundle.getString(frwdTip));
	    endButton.setToolTipText(bundle.getString(endTip));

	    top.add(startButton);
	    top.add(backButton);
	    top.add(reloadButton);
	    top.add(frwdButton);
	    top.add(endButton);
	}
    }

    boolean rvmode = false;

    /**
     * Set the background color for the 'start', 'back', 'reload',
     * 'forward', and 'end' controls.
     * When rvmode is true, the icons will be white when enabled
     * and grey when disabled, the reverse from the normal behavior.
     * @param color the color
     * @param rvmode true for reverse video; false otherwise
     */
    public void setButtonBackground(Color color, boolean rvmode) {
	if (this.rvmode != rvmode) {
	    if (rvmode) {
		if (lrmode) {
		    startButton.setIcon(fleftRVIcon);
		    backButton.setIcon(leftRVIcon);
		    reloadButton.setIcon(reloadRVIcon);
		    frwdButton.setIcon(rightRVIcon);
		    endButton.setIcon(frightRVIcon);

		    startButton.setDisabledIcon(fleftRVDIcon);
		    backButton.setDisabledIcon(leftRVDIcon);
		    reloadButton.setDisabledIcon(reloadRVDIcon);
		    frwdButton.setDisabledIcon(rightRVDIcon);
		    endButton.setDisabledIcon(frightRVDIcon);
		} else {
		    startButton.setIcon(frightRVIcon);
		    backButton.setIcon(rightRVIcon);
		    reloadButton.setIcon(rlReloadRVIcon);
		    frwdButton.setIcon(leftRVIcon);
		    endButton.setIcon(fleftRVIcon);

		    startButton.setDisabledIcon(frightRVDIcon);
		    backButton.setDisabledIcon(rightRVDIcon);
		    reloadButton.setDisabledIcon(rlReloadRVDIcon);
		    frwdButton.setDisabledIcon(leftRVDIcon);
		    endButton.setDisabledIcon(fleftRVDIcon);
		}
	    } else {
		if (lrmode) {
		    startButton.setIcon(fleftIcon);
		    backButton.setIcon(leftIcon);
		    reloadButton.setIcon(reloadIcon);
		    frwdButton.setIcon(rightIcon);
		    endButton.setIcon(frightIcon);
		} else {
		    startButton.setIcon(frightIcon);
		    backButton.setIcon(rightIcon);
		    reloadButton.setIcon(rlReloadIcon);
		    frwdButton.setIcon(leftIcon);
		    endButton.setIcon(fleftIcon);
		}

		startButton.setDisabledIcon(null);
		backButton.setDisabledIcon(null);
		reloadButton.setDisabledIcon(null);
		frwdButton.setDisabledIcon(null);
		endButton.setDisabledIcon(null);
	    }
	}
	startButton.setBackground(color);
	backButton.setBackground(color);
	reloadButton.setBackground(color);
	frwdButton.setBackground(color);
	endButton.setBackground(color);
    }

    /**
     * Constructor.
     */
    private HtmlPane(JEditorPane ep)  {
	super();
	editorPane = ep;
	editorPane.addPropertyChangeListener((evt) -> {
		if(evt.getPropertyName().equals("page")
		   && currentElement != null
		   && currentElement.event == null
		   && currentElement.position == null) {
		    currentElement.position =
			editorScrollPane.getViewport().getViewPosition();
		}
	    });
	editorScrollPane = new JScrollPane(editorPane);
	me = this;
	initIcons();
	Locale locale = getLocale();
	ResourceBundle bundle = 
	    ResourceBundle.getBundle(bundleName, locale);

	errorTitle = bundle.getString("errorTitle");

	if (frightIcon != null && rightIcon != null && reloadIcon != null &&
	    leftIcon != null && fleftIcon != null) {
	    startButton = new JButton(fleftIcon);
	    backButton = new JButton(leftIcon);
	    reloadButton = new JButton(reloadIcon);
	    frwdButton = new JButton(rightIcon);
	    endButton = new JButton(frightIcon);
	} else {
	    // Some icons not included - use text instead.
	    startButton = new JButton(bundle.getString(startText));
	    backButton = new JButton(bundle.getString(backText));
	    reloadButton = new JButton(bundle.getString(reloadText));
	    frwdButton = new JButton(bundle.getString(frwdText));
	    endButton = new JButton(bundle.getString(endText));
	}
	startButton.setToolTipText(bundle.getString(startTip));
	backButton.setToolTipText(bundle.getString(backTip));
	reloadButton.setToolTipText(bundle.getString(reloadTip));
	frwdButton.setToolTipText(bundle.getString(frwdTip));
	endButton.setToolTipText(bundle.getString(endTip));

	startButton.addActionListener(new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
		    if (urlstack.empty()) {
			if (currentElement != null) {
			    position = currentElement.position;
			    editorScrollPane.getViewport()
				.setViewPosition(position);
			}
			backButton.setEnabled(false);
			startButton.setEnabled(false);
			return;
		    }
		    if (currentElement != null) {
			revUrlstack.push(currentElement);
		    }
		    currentElement = urlstack.pop();
		    while (!urlstack.empty()) {
			revUrlstack.push(currentElement);
			currentElement = urlstack.pop();
		    }
		    
		    URL current = /*editorPane.*/getPage();
		    // StackElement element=(StackElement)urlstack.elementAt(0);
		    HTMLDocument doc =
			(HTMLDocument)editorPane.getDocument();
		    try {
			if (page == null ||
			    !page.sameFile(currentElement.url)) {
			    editorPane.setPage(currentElement.url);
			}
			// editorPane.setPage(element.url);
			page = currentElement.url;
			position = currentElement.position;
			if (position != null) {
			    editorScrollPane.getViewport()
				.setViewPosition(position);
			}
			if (currentElement.event != null) {
			    doc.processHTMLFrameHyperlinkEvent(currentElement.
							       event);
			}
			StackElement el = new StackElement(current, event);
			event = currentElement.event;
			frwdButton.setEnabled(true);
			endButton.setEnabled(true);
			/*
			if (urlstack.empty()) {
			    backButton.setEnabled(false);
			    startButton.setEnabled(false);
			}
			*/
		    } catch (Exception ee) {
			boolean fixed = true;
			try {
			    editorPane.setPage(current);
			    page = current;
			    position = editorScrollPane.getViewport()
				.getViewPosition();
			    if (event != null) {
				doc.processHTMLFrameHyperlinkEvent(event);
			    }
			    urlstack.push(currentElement);
			} catch (Exception eee){fixed = false;}
			if (fixed) {
			    JOptionPane.
				showMessageDialog(me,
						  "Cannot go back to " 
						  +currentElement.url,
						  errorTitle,
						  JOptionPane.ERROR_MESSAGE);
			} else {
			    JOptionPane.
				showMessageDialog(me,
						  "Navigation fault: "
						  +"cannot recover",
						  errorTitle,
						  JOptionPane.ERROR_MESSAGE);
			}
		    }
		}
	    });

	reloadButton.addActionListener(new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
		    URL current = /*editorPane.*/getPage();
		    try {
			editorPane.setPage(current);
			page = current;
			position = editorScrollPane.getViewport()
			    .getViewPosition();
			if (event != null) {
			    HTMLDocument doc =
				(HTMLDocument)editorPane.getDocument();
			    doc.processHTMLFrameHyperlinkEvent(event);
			}
		    } catch (Exception ee) {
			    JOptionPane.
				showMessageDialog(me,
						  "Cannot reload page",
						  errorTitle,
						  JOptionPane.ERROR_MESSAGE);
			
		    }
		}
	    });
	backButton.addActionListener(new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
		    
		    if (urlstack.empty()) {
			return;
		    }
		    if (currentElement != null) {
			revUrlstack.push(currentElement);
		    }
		    URL current = /*editorPane.*/getPage();
		    currentElement = urlstack.pop();
		    HTMLDocument doc =
			(HTMLDocument)editorPane.getDocument();
		    try {
			if (page == null ||
			    !page.sameFile(currentElement.url)) {
			    editorPane.setPage(currentElement.url);
			}
			page = currentElement.url;
			position = currentElement.position;
			if (position != null) {
			    JViewport vp = editorScrollPane.getViewport();
			    vp.setViewPosition(position);
			}
			if (currentElement.event != null) {
			    doc.processHTMLFrameHyperlinkEvent(currentElement.
							       event);
			}
			// revUrlstack.push(new StackElement(current, event));
			event = currentElement.event;
			frwdButton.setEnabled(true);
			endButton.setEnabled(true);
			if (urlstack.empty()) {
			    backButton.setEnabled(false);
			    startButton.setEnabled(false);
			}
		    } catch (Exception ee) {
			boolean fixed = true;
			try {
			    editorPane.setPage(current);
			    page = current;
			    position = editorScrollPane.getViewport()
				.getViewPosition();
			    if (event != null) {
				doc.processHTMLFrameHyperlinkEvent(event);
			    }
			    urlstack.push(currentElement);
			} catch (Exception eee){fixed = false;}
			if (fixed) {
			    JOptionPane.
				showMessageDialog(me,
						  "Cannot go back to " 
						  +currentElement.url,
						  errorTitle,
						  JOptionPane.ERROR_MESSAGE);
			} else {
			    JOptionPane.
				showMessageDialog(me,
						  "Navigation fault: "
						  +"cannot recover",
						  errorTitle,
						  JOptionPane.ERROR_MESSAGE);
			}
		    }
		}
	    });
	frwdButton.addActionListener(new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
		    if (revUrlstack.empty()) {
			return;
		    }
		    if (currentElement != null) {
			urlstack.push(currentElement);
		    }
		    URL current = /*editorPane.*/getPage();
		    currentElement = revUrlstack.pop();
		    HTMLDocument doc =
			(HTMLDocument)editorPane.getDocument();
		    try {
			editorPane.setPage(currentElement.url);
			page = currentElement.url;
			position = currentElement.position;
			if (position != null) {
			    editorScrollPane.getViewport()
				.setViewPosition(position);
			}
			if (currentElement.event != null) {
			    doc.processHTMLFrameHyperlinkEvent(currentElement.
							       event);
			}
			/*
			urlstack.push(new 
				      StackElement(current, event));
			*/
			event = currentElement.event;
			backButton.setEnabled(true);
			startButton.setEnabled(true);
			if (revUrlstack.empty()) {
			    frwdButton.setEnabled(false);
			    endButton.setEnabled(false);
			}
		    } catch (Exception eee) {
			boolean fixed = true;
			try {
			    editorPane.setPage(current);
			    page = current;
			    position = editorScrollPane.getViewport()
				.getViewPosition();
			    if (event != null) {
				doc.processHTMLFrameHyperlinkEvent(event);
			    }
			    revUrlstack.push(currentElement);
			} catch (Exception eeee) {fixed = false;}
			if (fixed) {
			    JOptionPane.
				showMessageDialog(me,
						  "Cannot go to " 
						  +currentElement.url,
						  errorTitle,
						  JOptionPane.ERROR_MESSAGE);
			} else {
			    JOptionPane.
				showMessageDialog(me,
						  "Navigation fault: "
						  +"cannot recover",
						  errorTitle,
						  JOptionPane.ERROR_MESSAGE);
			}
		    }
		}
	    });
	endButton.addActionListener(new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
		    if (revUrlstack.empty()) {
			if (currentElement != null) {
			    position = currentElement.position;
			    editorScrollPane.getViewport()
				.setViewPosition(position);
			}
			frwdButton.setEnabled(false);
			endButton.setEnabled(false);
			return;
		    }
		    if (currentElement != null) {
			urlstack.push(currentElement);
		    }
		    currentElement = revUrlstack.pop();
		    while (!revUrlstack.empty()) {
			urlstack.push(currentElement);
			currentElement = revUrlstack.pop();
		    }
		    URL current = /*editorPane.*/getPage();
		    /*
		    StackElement element = 
			(StackElement)revUrlstack.elementAt(0);
		    */
		    HTMLDocument doc =
			(HTMLDocument)editorPane.getDocument();
		    try {
			if (page == null
			    || !page.sameFile(currentElement.url)) {
			    editorPane.setPage(currentElement.url);
			}
			page = currentElement.url;
			position = currentElement.position;
			if (position != null) {
			    editorScrollPane.getViewport()
				.setViewPosition(position);
			}
			if (currentElement.event != null) {
			    doc.processHTMLFrameHyperlinkEvent(currentElement.
							       event);
			}
			StackElement el = new StackElement(current, event);
			event = currentElement.event;
			backButton.setEnabled(true);
			startButton.setEnabled(true);
			/*
			if (revUrlstack.empty()) {
			    frwdButton.setEnabled(false);
			    endButton.setEnabled(false);
			}
			*/
		    } catch (Exception eee) {
			boolean fixed = true;
			try {
			    editorPane.setPage(current);
			    page = current;
			    position = editorScrollPane.getViewport()
				.getViewPosition();
			    if (event != null) {
				doc.processHTMLFrameHyperlinkEvent(event);
			    }
			    revUrlstack.push(currentElement);
			} catch (Exception eeee) {fixed = false;}
			if (fixed) {
			    JOptionPane.
				showMessageDialog(me,
						  "Cannot go to " 
						  +currentElement.url,
						  errorTitle,
						  JOptionPane.ERROR_MESSAGE);
			} else {
			    JOptionPane.
				showMessageDialog(me,
						  "Navigation fault: "
						  +"cannot recover",
						  errorTitle,
						  JOptionPane.ERROR_MESSAGE);
			}
		    }
		}
	    });
	editorPane.setContentType("text/html");
	editorPane.setEditable(false);
	editorPane.addHyperlinkListener(hl);
	setLayout(new BorderLayout());
	// top.setComponentOrientation(co);

	top.add(startButton);
	top.add(backButton);
	top.add(reloadButton);
	top.add(frwdButton);
	top.add(endButton);

	startButton.setEnabled(false);
	backButton.setEnabled(false);
	reloadButton.setEnabled(false);
	frwdButton.setEnabled(false);
	endButton.setEnabled(false);
	add(top, "North");
	add(editorScrollPane, "Center");
	editorPane.addKeyListener(editorKeyAdapter);
    }

    /**
     * Constructor.
     */
    public HtmlPane() {
	this(new JEditorPane());
    }

    /**
     * Constructor based on a url represented by a string.
     * @param url the URL that the pane should initially display
     * @throws IOException an error setting the page occured.
     */
    public HtmlPane(String url) throws IOException {
	this(new JEditorPane(url));
	finishSetPage(editorPane.getPage());
    }

    /**
     * Constructor based on a url.
     * @param url the URL that the pane should initially display
     * @throws IOException an error setting the page occurred.
     */
    public HtmlPane(URL url) throws IOException {
	this(new JEditorPane(url));
	finishSetPage(url);
    }
}

//  LocalWords:  exbundle JEditorPane JScrollPane ul li setPage forw
//  LocalWords:  tooltips startText backText reloadText frwdText RL
//  LocalWords:  endText startTip backTip reloadTip frwdTip endTip
//  LocalWords:  rlredo gif Zaumen urlstack revUrlstack frightUrl url
//  LocalWords:  ClassLoader getSystemClassLoader getResource leftUrl
//  LocalWords:  rightUrl reloadUrl rlReloadUrl fleftUrl IOException
//  LocalWords:  cannotOpenURL editorPane getPage errorTitle
//  LocalWords:  editorScrollPane applyComponentOrientation
//  LocalWords:  setComponentOrientation
