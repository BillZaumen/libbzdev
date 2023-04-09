package org.bzdev.swing;
import org.bzdev.gio.*;
import org.bzdev.lang.UnexpectedExceptionError;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import java.util.LinkedList;
import java.util.ResourceBundle;

//@exbundle org.bzdev.swing.lpack.Swing

/**
 * Class implementing output-stream graphics for a JPanel.
 * This class implements the interface {@link org.bzdev.gio.OSGraphicsOps}.
 * Instead of writing to an output stream, the graphics operations
 * will be performed on a JPanel, allowing the graphics to be viewed
 * on a display without first creating an image file.
 * <P>
 * When an instance of this class is created, one will specify a width
 * and height.  This height and width will represent the width and
 * height of a rectangle, called the drawing area, in user space. The
 * origin of this rectangle is (0, 0). The drawing area may be scaled to 
 * fit the current size of the associated {@link javax.swing.JPanel}
 * with the scaling depending on the {@link PanelGraphics.Mode mode}
 * configured by calling {@link PanelGraphics#setMode(PanelGraphics.Mode)}.
 * The default mode is {@link PanelGraphics.Mode#FIT}.
 * <P>
 * The use-case that led to the creation of this class is that
 * of a {@link org.bzdev.graphs.Graph} that should be displayed on
 * a screen instead of being written to a file.
 * <P>
 * Users of this class will typically write the following code:
 * <blockquote><pre><code>
 *  import org.bzdev.swing.PanelGraphics;
 *  import javax.swing.JPanel;
 *  ...
 *      PanelGraphics pg = new PanelGraphics(...);
 *      JPanel panel = pg.getPanel();
 *      // perform 'swing' operations on the panel.
 *      ...
 *      // create the graphics to be displayed
 *      Graphics2D g2d = pg.createGraphics();
 *      ...
 *      // display the results
 *      pg.imageComplete();
 * </CODE></PRE></blockquote>
 * Alternatively, one may use the newFramedInstance method:
 * <blockquote><pre><code>
 *  import org.bzdev.swing.PanelGraphics;
 *  import javax.swing.JPanel;
 *  ...
 *      PanelGraphics pg = PanelGraphics.newFramedInstance(...);
 *      ...
 *      // create the graphics to be displayed
 *      Graphics2D g2d = pg.createGraphics();
 *      ...
 *      // display the results
 *      pg.imageComplete();
 * </CODE></PRE></blockquote>
 * <P>
 * To create a graph, one can use the following statements:
 * <blockquote><pre><code>
 *  import org.bzdev.swing.PanelGraphics;
 *  import org.bzdev.graph.*;
 *  import javax.swing.JPanel;
 *      ...
 *      PanelGraphics pg = PanelGraphics.newFramedInstance(...);
 *      Graph graph = new Graph(pg);
 *      // perform normal graph operations to create the graph
 *      ...
 *     graph.write();
 * </CODE></PRE></blockquote>
 * The graph will be displayed in the panel.  If the frame was created
 * so that it is not visible, one can call
 * <blockquote><pre><code>
 *      pg.setVisible(true);
 * </CODE></PRE></blockquote>
 * or
 * <blockquote><pre><code>
 *      pg.getPanelWindow().setVisible(true);
 * </CODE></PRE></blockquote>
 * to make it visible.
 * <P>
 * While the PanelGraphics class has Java 'swing' components
 * associated with it, the constructors and the newFramedInstance methods
 * are thread safe - their use is not restricted to the event dispatch
 * thread. Consequently, it is easy to create a command-line program that
 * will open a window to display its output and exit when that window is
 * closed.
 */
public class PanelGraphics implements OSGraphicsOps {

    static String errorMsg(String key, Object... args) {
	return SwingErrorMsg.errorMsg(key, args);
    }

    static private final String resourceBundleName =
	"org.bzdev.swing.lpack.PanelGraphics";
    static ResourceBundle bundle =
	ResourceBundle.getBundle(resourceBundleName);

    static String localeString(String name) {
	try {
	    return bundle.getString(name);
	} catch (Exception e) {
	    return name;
	}
    }

    boolean flushImmediately = false;
    /**
     * Determine if {@link #flush()} method immediately updates the
     * display or if the update occurs with normal processing delay.
     * <P>
     *  When true the JComponent method
     * {@link JComponent#paintImmediately(java.awt.Rectangle)}
     * @param mode true if the flush operation should occur immediately;
     *        false if normal processing delays are acceptable.
     */
    public void setFlushImmediately(boolean mode) {
	try {
	    SwingUtilities.invokeAndWait(new Runnable() {
		    public void run() {
			flushImmediately = mode;
		    }
		});
	} catch (Exception e) {}
    }

    /**
     * PanelGraphics modes.
     * The term "drawing area" is described in the documentation
     * for {@link PanelGraphics}.
     */
    public static enum Mode {
	/**
	 * Scale the drawing area equally in the horizontal and vertical
	 * directions so that it just fits inside a JPanel.
	 */
	FIT,
	/**
	 * Scale the drawing area equally in the horizontal and vertical
	 * directions so that it's horizontal dimensions
	 * just fits inside a JPanel.
	 */
	FIT_HORIZONTAL,
	/**
	 * Scale the drawing area equally in the horizontal and vertical
	 * directions so that it's vertical dimensions
	 * just fits inside a JPanel.
	 */
	FIT_VERTICAL,
	/**
	 * Do not perform any scaling. The drawing area will have an
	 * origin at (0, 0).
	 */
	AS_IS,
	/**
	 * Scale the drawing area in the horizontal and vertical
	 * directions so that it just fits inside a JPanel. If the
	 * dimensions of the area and JPanel are not consistent, the
	 * scaling may be unequal.
	 */
	FILL,
    }

    JPanel panel;

    /**
     * Class to determine how an application can exit,even when a
     * security manager is installed, given a window created with
     * {@link PanelGraphics#newFramedInstance(int,int,String,boolean,ExitAccessor)}
     * is closed.  When an instance of this class is the last argument
     * for
     * {@link PanelGraphics#newFramedInstance(int,int,String,boolean,ExitAccessor)},
     * the application will exit if the console's frame is closed.
     */
    public static class ExitAccessor {
	private boolean allow;

	/**
	 * Constructor.
	 */
	public ExitAccessor() {
	    allow = true;
	    /*
	    SecurityManager sm = System.getSecurityManager();
	    if (sm == null) {
		allow = true;
	    } else {
		try {
		    sm.checkPermission(new ExitPermission
				       ("org.bzdev.swing.PanelGraphics"));
		    allow = true;
		} catch (SecurityException se) {
		    allow = false;
		}
	    }
	    */
	}
	/**
	 * Return true if closing a frame created with
	 * {@link PanelGraphics#newFramedInstance(int,int,String,boolean,ExitAccessor)}
	 * or
	 * {@link PanelGraphics#newFramedInstance(int,int,String,boolean,ExitAccessor,boolean)}
}	 * can cause an application to exit; false if the user must confirm
	 * that the application will exit.
	 */
	boolean allow() {return allow;}
    }

    boolean createdByNewFramedInstance = false;

    /**
     * Dispose this PanelGraphic's window if that was created
     * by a call to newFramedInstance.
     */
    public void disposeFrame() {
	if (createdByNewFramedInstance) {
	    getPanelWindow().dispose();
	}
    }

    /**
     * Create an instance of PanelGraphics with the associated panel in
     * a frame.
     * The dimensions provided by the first two arguments are the
     * dimensions of the panel that displays the graphics that will be
     * created through the use of this class. The frame will be larger
     * to allow room for some buttons, combo boxes, labels, and text
     * fields.
     * <P>
     * This method is particularly useful for simple applications in
     * which one wants a window to display something, perhaps a graph,
     * but where a full-fledged GUI is not needed.
     * @param width the width of the panel
     * @param height the height of the panel
     * @param title the frame title
     * @param visible true if the frame is visible; false otherwise
     * @param exitOnClose true if the application should exit when the
     *        frame closes; false if the frame should be hidden.
     * @return the new instance of PanelGraphics
     */
    public static PanelGraphics newFramedInstance(int width,
						  int height,
						  String title,
						  boolean visible,
						  boolean exitOnClose)
    {
	return PanelGraphics.newFramedInstance(width, height, title, visible,
					       exitOnClose, true);
    }

    /**
     * Create an instance of PanelGraphics with the associated panel in
     * a frame with the exit mode determined by an exit accessor.
     * The dimensions provided by the first two arguments are the
     * dimensions of the panel that displays the graphics that will be
     * created through the use of this class. The frame will be larger
     * to allow room for some buttons, combo boxes, labels, and text
     * fields.
     * <P>
     * This method is particularly useful for simple applications in
     * which one wants a window to display something, perhaps a graph,
     * but where a full-fledged GUI is not needed.
     * @param width the width of the panel
     * @param height the height of the panel
     * @param title the frame title
     * @param visible true if the frame is visible; false otherwise
     * @param accessor an exit accessor that determines if the frame will
     *        ask the user if the application should exit before exiting
     *        the application; null implies that the application will exit
     *        immediately.
     * @return the new instance of PanelGraphics
     */
    public static PanelGraphics newFramedInstance(int width,
						  int height,
						  String title,
						  boolean visible,
						  ExitAccessor accessor)
    {
	return PanelGraphics.newFramedInstance(width, height, title, visible,
					       true, accessor, true);
    }


    /**
     * Create an instance of PanelGraphics with the associated panel in
     * a frame, optionally adding Print and Save-As buttons.
     * The dimensions provided by the first two arguments are the
     * dimensions of the panel that displays the graphics that will be
     * created through the use of this class. The frame will be larger
     * to allow room for some buttons, combo boxes, labels, and text
     * fields when the addButtons argument is <code>true</code>.
     * <P>
     * This method is particularly useful for simple applications in
     * which one wants a window to display something, perhaps a graph,
     * but where a full-fledged GUI is not needed.
     * @param width the width of the panel and frame
     * @param height the height of the panel and frame
     * @param title the frame title
     * @param visible true if the frame is visible; false otherwise
     * @param exitOnClose true if the application should exit when the
     *        frame closes; false if the frame should be hidden.
     * @param addButtons true if the frame should include buttons for
     *        printing and writing to files; false otherwise
     * @return the new instance of PanelGraphics
     */
    public static PanelGraphics newFramedInstance(final int width,
						  final int height,
						  final String title,
						  final boolean visible,
						  final boolean exitOnClose,
						  final boolean addButtons)
    {
	return newFramedInstance(width, height, title, visible, exitOnClose,
				 null, addButtons);
    }

    /**
     * Create an instance of PanelGraphics with the associated panel
     * in a frame with the exit mode determined by an ExitAccessor,
     * optionally adding Print and Save-As buttons.
     * The dimensions provided by the first two arguments are the
     * dimensions of the panel that displays the graphics that will be
     * created through the use of this class. The frame will be larger
     * to allow room for some buttons, combo boxes, labels, and text
     * fields when the addButtons argument is <code>true</code>.
     * <P>
     * This method is particularly useful for simple applications in
     * which one wants a window to display something, perhaps a graph,
     * but where a full-fledged GUI is not needed.
     * @param width the width of the panel and frame
     * @param height the height of the panel and frame
     * @param title the frame title
     * @param visible true if the frame is visible; false otherwise
     * @param accessor an exit accessor that determines if the frame will
     *        ask the user if the application should exit before exiting
     *        the application; null implies that the application will exit
     *        immediately.
     * @param addButtons true if the frame should include buttons for
     *        printing and writing to files; false otherwise
     * @return the new instance of PanelGraphics
     */
    public static PanelGraphics newFramedInstance(final int width,
						  final int height,
						  final String title,
						  final boolean visible,
						  ExitAccessor accessor,
						  final boolean addButtons)
    {
	return newFramedInstance(width, height, title, visible, true,
				 accessor, addButtons);
    }

    private static PanelGraphics newFramedInstance(final int width,
						  final int height,
						  final String title,
						  final boolean visible,
						  final boolean exitOnClose,
						  final ExitAccessor accessor,
						  final boolean addButtons)
    {
	boolean ask = false;
	if (exitOnClose) {
	    if (accessor == null) {
		ask = false;
		/*
		SecurityManager sm = System.getSecurityManager();
		if (sm != null) {
		    try {
			sm.checkPermission(new ExitPermission
					   ("org.bzdev.swing.PanelGraphics"));
		    } catch (SecurityException se) {
			ask = true;
		    }
		} else {
		    ask = false;
		}
		*/
	    } else {
		ask = !accessor.allow();
	    }
	}
	final boolean askOnClose = ask;
	final PanelGraphics pg = new PanelGraphics(width, height, true);
	final JPanel panel = pg.getPanel();
	final Runnable r = new Runnable() {
		JFrame frame;
		ImageOrientation omode = ImageOrientation.NORMAL;
		private void doAddButtons(PanelGraphics pg, JPanel panel,
					  final JFrame frame,
					  int width, int height)
		{
		    JButton printButton =
			new JButton(localeString("print"));
		    printButton.addActionListener(new ActionListener() {
			    public void actionPerformed(ActionEvent e) {
				try {
				    AccessController.doPrivileged
					(new PrivilegedExceptionAction<Void>() {
						public Void run()
						    throws PrinterException
						{
						    pg.print(true);
						    return (Void) null;
						}
					    });
				    // pg.print(true);
				} catch (PrivilegedActionException ee) {
				    SwingErrorMessage.display
					(panel, null,
					 localeString("pfailed"));
				    SwingErrorMessage.display(ee.getCause());
				}
			    }
			});
		    String options[] = {
			localeString("normal"),
			localeString("ccw"),
			localeString("cw")
		    };
		    final JLabel widthLabel =
			new JLabel(localeString("widthLabel"));
		    final JLabel heightLabel =
			new JLabel(localeString("heightLabel"));
		    final WholeNumbTextField widthTF =
			new WholeNumbTextField(4);
		    widthTF.setValue(width);
		    final WholeNumbTextField heightTF =
			new WholeNumbTextField( 4);
		    heightTF.setValue(height);

		    final JComboBox<String> orientation =
			new JComboBox<>(options);
		    orientation.addActionListener(new ActionListener() {
			    public void actionPerformed(ActionEvent e) {
				switch(orientation.getSelectedIndex()) {
				case 0:
				    omode = ImageOrientation.NORMAL;
				    break;
				case 1:
				    omode =
					ImageOrientation.CLOCKWISE90;
				    break;
				case 2:
				    omode =
					ImageOrientation.COUNTERCLOCKWISE90;
				    break;
				default:
				    throw new UnexpectedExceptionError();
				}
			    }
			});
		    JButton fileButton =
			new JButton(localeString("saveAs"));
		    final File currentDir = AccessController.doPrivileged
			(new PrivilegedAction<File>() {
				public File run() {
				    return new File(System.getProperty
						    ("user.dir"));
				}
			    });
		    fileButton.addActionListener(new ActionListener() {
			    private void runAction() {
				JFileChooser fc =
				    new JFileChooser(currentDir);
				for (FileFilter f:
					 fc.getChoosableFileFilters()) {
				    fc.removeChoosableFileFilter(f);
				}
				// add our filters
				for (String mtype: OutputStreamGraphics
					 .getMediaTypes()) {
				    FileNameExtensionFilter fnef =
					new FileNameExtensionFilter
					(localeString(mtype),
					 OutputStreamGraphics.
					 getSuffixesForMediaType(mtype));
				    fc.addChoosableFileFilter(fnef);
				    if (mtype.equals("image/png")) {
					fc.setFileFilter(fnef);
				    }
				}
				int status = fc.showSaveDialog(frame);
				if (status == JFileChooser.APPROVE_OPTION) {
				    try {
					File of = fc.getSelectedFile();
					String saveFileName =
					    of.getCanonicalPath();
					int ind = saveFileName.lastIndexOf
					    ('.');
					if (ind == -1) {
					    SwingErrorMessage.display
						(panel, null,
						 localeString("noExt"));
					}
					String suffix = saveFileName
					    .substring(ind+1);
					String imageType =
					    OutputStreamGraphics
					    .getImageTypeForSuffix(suffix);
					OutputStream os =
					    new FileOutputStream(of);
					int w = widthTF.getValue();
					int h = heightTF.getValue();
					OutputStreamGraphics osg =
					    OutputStreamGraphics
					    .newInstance(os, w, h, omode,
							 imageType);
					pg.write(osg);
					osg.flush();
					osg.close();
					os.close();
				    } catch(IOException eio) {
					String msg =
					    localeString("fileError");
					SwingErrorMessage.display(panel, null,
							     msg);
					SwingErrorMessage.display(eio);
				    }
				}
			    }
			    @Override
			    public void actionPerformed(ActionEvent e) {
				AccessController.doPrivileged
				    (new PrivilegedAction<Void>() {
					    public Void run() {
						runAction();
						return (Void)null;
					    }
					});
			    }
			});
		    GridBagLayout gridbag = new GridBagLayout();
		    JPanel subpanel = new JPanel(gridbag);
		    GridBagConstraints c = new GridBagConstraints();
		    c.ipadx = 10;
		    c.ipady = 10;
		    c.anchor = GridBagConstraints.BASELINE;
		    gridbag.setConstraints(fileButton, c);
		    subpanel.add(fileButton);
		    c.gridwidth = GridBagConstraints.REMAINDER;
		    gridbag.setConstraints(printButton, c);
		    subpanel.add(printButton);

		    c.gridwidth = GridBagConstraints.REMAINDER;
		    c.anchor = GridBagConstraints.BASELINE;
		    JLabel separator =
			new JLabel("<html><b>________</b></html>");
		    gridbag.setConstraints(separator, c);
		    subpanel.add(separator);
		    JLabel spacer1 = new JLabel(" ");
		    gridbag.setConstraints(spacer1, c);
		    subpanel.add(spacer1);
		    JLabel optionTitle =
			(new JLabel(localeString("saveAsOptions")));
		    gridbag.setConstraints(optionTitle, c);
		    subpanel.add(optionTitle);
		    c.gridwidth = 1;
		    c.anchor = GridBagConstraints.BASELINE_TRAILING;
		    gridbag.setConstraints(widthLabel, c);
		    subpanel.add(widthLabel);
		    c.anchor = GridBagConstraints.BASELINE_LEADING;
		    c.gridwidth = GridBagConstraints.REMAINDER;
		    gridbag.setConstraints(widthTF, c);
		    subpanel.add(widthTF);
		    c.gridwidth = 1;
		    c.anchor = GridBagConstraints. BASELINE_TRAILING;
		    gridbag.setConstraints(heightLabel, c);
		    subpanel.add(heightLabel);
		    c.anchor = GridBagConstraints.BASELINE_LEADING;
		    c.gridwidth = GridBagConstraints.REMAINDER;
		    gridbag.setConstraints(heightTF, c);
		    subpanel.add(heightTF);
		    c.ipadx = 0;
		    c.ipady = 0;
		    JLabel spacer2 = new JLabel(" ");
		    gridbag.setConstraints(spacer2, c);
		    subpanel.add(spacer2);
		    c.anchor = GridBagConstraints.BASELINE;
		    gridbag.setConstraints(orientation, c);
		    subpanel.add(orientation);
		    JPanel controls = new JPanel(new BorderLayout());
		    controls.add(BorderLayout.PAGE_START, subpanel);
		    frame.add(BorderLayout.LINE_START, controls);

		}
		private void doExit() {
		    AccessController.doPrivileged(new PrivilegedAction<Void>() {
			    public Void run() {
				System.exit(0);
				return (Void)null;
			    }
			});
		}
		private void doSetVisible() {
		    AccessController.doPrivileged(new PrivilegedAction<Void>() {
			    public Void run() {
				SwingUtilities.invokeLater(new Runnable()
				    {
					public void run() {
					    frame.setVisible(true);
					}
				    });
				return (Void)null;
			    }
			});
		}
		public void run() {
		    frame = (title == null)? new JFrame():
			new JFrame(title);

		    int closeOperation = exitOnClose? JFrame.EXIT_ON_CLOSE:
			WindowConstants.HIDE_ON_CLOSE;

		    if (exitOnClose == false) {
			frame.setDefaultCloseOperation
			    (WindowConstants.HIDE_ON_CLOSE);
		    } else {
			/*
			  boolean ask = askOnClose;
			  boolean ask = false;
			  if (accessor == null) {
			  SecurityManager sm = System.getSecurityManager();
			  if (sm != null) {
			  try {
			  sm.checkPermission
			  (new ExitPermission
			  ("org.bzdev.swing.PanelGraphics"));
			  } catch (SecurityException se) {
			  ask = true;
			  }
			  } else {
			  ask = false;
			  }
			  } else {
			  ask = !accessor.allow();
			  }
			*/
			// final boolean askMode = ask;
			frame.addWindowListener (new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
				    Window w = e.getWindow();
				    if (!(w instanceof JFrame)) return;
				    JFrame frame = (JFrame) e.getWindow();
				    if (askOnClose) {
					int status =
					    JOptionPane.showConfirmDialog
					    (frame,
					     localeString("exit"),
					     localeString("exitTitle"),
					     JOptionPane.OK_CANCEL_OPTION);
					if (status == JOptionPane.OK_OPTION) {
					    doExit();
					} else {
					    // Delay until all the
					    // current events have
					    // finished: otherwise the
					    // code run after this
					    // method was called can
					    // make the window
					    // invisible.
					    doSetVisible();
					}
				    } else {
					doExit();
				    }
				}
			    });
		    }
		    panel.setOpaque(false);
		    Dimension psize = new Dimension(width,height);
		    panel.setPreferredSize(psize);
		    Container fpane = frame.getContentPane();
		    fpane.setLayout(new BorderLayout());
		    if (addButtons) {
			doAddButtons(pg, panel, frame, width, height);
		    }
		    frame.add(BorderLayout.CENTER, panel);
		    frame.pack();
		    // frame.setSize(width, height);
		    frame.setVisible(visible);
		}
	    };
	if (SwingUtilities.isEventDispatchThread()) {
	    r.run();
	} else {
	    try {
		SwingUtilities.invokeAndWait(r);
	    } catch (InterruptedException e) {
	    } catch (InvocationTargetException e) {
	    }
	}
	pg.createdByNewFramedInstance = true;
	return pg;
    }

    static class WindowContainer {
	Window window;
    }

    static class BooleanContainer {
	boolean value;
    }

    /**
     * Find the top-level container for the panel associated with
     * this object and return non-null
     * if the container is a Window.
     * @return the window; null if there is none
     */
    public Window getPanelWindow() {
	if (SwingUtilities.isEventDispatchThread()) {
	    if (panel == null) return null;
	    Container c = panel;
	    Container cc = c;
	    while (cc != null) {
		c = cc;
		cc = c.getParent();
	    }
	    if (c instanceof Window) {
		return (Window) c;
	    } else {
		return null;
	    }
	} else {
	    final WindowContainer wc = new WindowContainer();
	    try {
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
			    wc.window = getPanelWindow();
			}
		    });
	    } catch (Exception e) {}
	    return wc.window;
	}
    }

    /**
     * Set the visibility of the panel associated with this PanelGraphics.
     * This method has no effect if the panel is not contained in a window.
     * @param visible true if the panel should be visible; false otherwise
     */
    public void setVisible(final boolean visible) {
	if (SwingUtilities.isEventDispatchThread()) {
	    Window window = getPanelWindow();
	    if (window != null) {
		window.setVisible(visible);
	    }
	} else {
	    try {
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
			    Window window = getPanelWindow();
			    if (window != null) {
				window.setVisible(visible);
			    }
			}
		    });
	    } catch (Exception e) {}
	}
    }

    /**
     * Determine if the window containing the panel associated with
     * this instance of PanelGraphics is visible.
     * @return true if it is visible; false if it is not visible or
     *         if the panel is not contained within a window
     */
    public boolean isVisible() {
	if (SwingUtilities.isEventDispatchThread()) {
	    Window window = getPanelWindow();
	    if (window != null) {
		return window.isVisible();
	    } else {
		return false;
	    }
	} else {
	    try {
		final BooleanContainer bc = new BooleanContainer();
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
			    bc.value = isVisible();
			}
		    });
		return bc.value;
	    } catch (Exception e){
		return false;
	    }
	}
    }


    boolean requestAlpha;
    int targetWidth;
    int targetHeight;

    @Override
    public boolean requestsAlpha() {
	return requestAlpha;
    }
    
    @Override
    public int getWidth() {return targetWidth;}

    @Override
    public int getHeight() {return targetHeight;}

    /**
     * Set the background color for the panel associated with this object.
     * @param c the color; null if the color of the panel's parent
     *        should be used
     */
    public void setBackground(final Color c) {
	if (SwingUtilities.isEventDispatchThread()) {
	    panel.setBackground(c);
	} else {
	    try {
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
			    setBackground(c);
			}
		    });
	    } catch (Exception e) {}
	}
    }

    /**
     * Get the background color for the panel associated with this object.
     * @return the background color
     */
    public Color getBackground() {
	return panel.getBackground();
    }

    /**
     * Get the JPanel associated with this instance.
     * @return the panel
     */
    public JPanel getPanel() {return panel;}

    /**
     * Off-screen GraphicsCreator.
     * This class is used to store graphics operations
     * that will be applied by an instance of PanelGraphics, replacing
     * any graphics previously displayed. To create an instance of this
     * object, call {@link PanelGraphics#newPanelGraphicsCreator(boolean)}.
     * One will then call the method {@link Creator#createGraphics()}
     * to get a graphics context and, after using it to create the
     * graphics, one will call {@link Creator#apply()} to replace the
     * graphics of the instance of {@link PanelGraphics} that created
     * this object. When this object is no longer needed, one should
     * call {@link Creator#dispose()} to free resources.
     * <P>
     * The class {@link AnimatedPanelGraphics} uses this method, created
     * by calling {@link PanelGraphics#newPanelGraphicsCreator(boolean)}
     * with an argument set to <code>true</code>, to create individual
     * animation frames that can be replayed.
     */
    public class Creator implements GraphicsCreator {
	// LinkedList<Graphics2DRecorder> recorders = new LinkedList<>();
	SurrogateGraphics2D sg2d = new SurrogateGraphics2D(requestAlpha);
	Graphics2DRecorder recorder = new Graphics2DRecorder(sg2d);
	/**
	 * Create a new graphics context.
	 * @return the new graphics context
	 */
	public Graphics2D createGraphics() {
	    return recorder.createGraphics();
	}
	/***
	 * Free any resources used by this object, provided this object
	 * was created with a mode whose value is true.
	 * If this object was created with its mode set to false,
	 * the method {@link #apply()} must be called and that method
	 * will free the resources instead.  In that case, this method
	 * will have no effect.
	 */
	public void dispose() {
	    if (keep) {
		sg2d.dispose();
	    }
	}

	/*
	public void playback(Graphics2D g2d) {
	    recorder.playback(g2d);
	}
	*/

	int count = 1;

	/**
	 * Get the  repetition count
	 * @return the repetition count
	 */
	public final int getRepetitionCount() {
	    return count;
	}

	boolean keep = false;

	/**
	 * Replace graphics contexts and graphics-context tables
	 * in the PanelGraphics associated with this object with
	 * the graphics contexts and tables encapsulated in this
	 * object.
	 */
	public void apply() {
	    PanelGraphics.this.setPanelGraphicsCreator(this,keep);
	}

	/*
	@Override
	public void finalize() {
	    sg2d.dispose();
	}
	*/

	Creator(boolean mode) {
	    keep = mode;
	}

	Creator(boolean mode, int count) {
	    keep = mode;
	    this.count = count;
	}

    }

    /**
     * Create a new set of graphics contexts.
     * @param mode true if the object created will be used after
     *        its apply method is called; false otherwise
     * @return a graphics creator
     */
    public Creator newPanelGraphicsCreator(boolean mode) {
	return new Creator(mode);
    }

    /**
     * Create a new set of graphics contexts with a repetition count.
     * The value of the repetition count can be used in animations to
     * indicate the number of times a frame should be repeated.
     * @param mode true if the object created will be used after
     *        its app method is called; false otherwise
     * @param count a positive integer giving the repetition count
     * @return a graphics creator
     * @see Creator#getRepetitionCount()
     */
    public Creator newPanelGraphicsCreator(boolean mode, int count) {
	if (count < 0) {
	    String msg = errorMsg("negativeRepCount");
	    throw new IllegalArgumentException(msg);
	}
	return new Creator(mode, count);
    }

    boolean delete = true;

    // called by the Creator method named apply()
    void setPanelGraphicsCreator(Creator graphicsCreator,
				 boolean keep)
    {
	if (SwingUtilities.isEventDispatchThread()) {
	    synchronized (this) {
		done = false;
	    }
	    recorders = new LinkedList<Graphics2DRecorder>
		(/*graphicsCreator.recorders*/);
	    recorder = graphicsCreator.recorder;
	    Graphics2D old = sg2d;
	    sg2d = graphicsCreator.sg2d;
	    if (delete) old.dispose();
	    delete = !keep;
	    try {
		// Will create a new recorder and store the
		// one from graphicsCreator in the recorders list.
		flush();
	    } catch (IOException e) {
		SwingErrorMessage.display("flush error");
	    }
	} else {
	    try {
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
			    synchronized (this) {
				done = false;
			    }
			    recorders = new LinkedList<Graphics2DRecorder>
				(/*graphicsCreator.recorders*/);
			    recorder = graphicsCreator.recorder;
			    Graphics2D old = sg2d;
			    sg2d = graphicsCreator.sg2d;
			    if (delete) old.dispose();
			    delete = !keep;
			    try {
				// Will create a new recorder and store the
				// one from graphicsCreator in the recorders
				// list.
				flush();
			    } catch(IOException e){
				SwingErrorMessage.display("flush error");
			    }
			}
		    });
	    } catch (Exception e) {}
	}
    }

    SurrogateGraphics2D sg2d;
    LinkedList<Graphics2DRecorder> recorders = new LinkedList<>();

    Graphics2DRecorder recorder;

    private boolean done = false;

    @Override
    public boolean canReset() {return true;}

    /**
     * Reset the panel so that its contents are empty and clear
     * any existing graphics operations.
     * It is the caller's responsibility to make sure that all
     * graphics contexts previously created by this instance of
     * PanelGraphics are not used subsequently.
.     */
    public void reset() {
	synchronized (this) {
	    done = false;
	}
	if (SwingUtilities.isEventDispatchThread()) {
	    recorders.clear();
	    recorder.reset();
	    sg2d.setClip(null);
	} else {
	    try {
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
			    recorders.clear();
			    recorder.reset();
			    sg2d.setClip(null);
			}
		    });
	    } catch (Exception e) {}
	}
	panel.repaint();
    }

    // Point2D origin = new Point2D.Double(0.0, 0.0);

    Mode mode = Mode.FIT;

    /**
     * Set the mode
     * @param mode the mode, as described by {@link PanelGraphics.Mode}
     */
    public void setMode(Mode mode) {
	this.mode = mode;

    }

    /**
     * Get the current mode
     * @return the mode
     */
    public Mode getMode() {return mode;}

    /*
     * Deprecated and removed in Java 11.
     * Note: rethink how we want to handle freeing resources in this
     * case.
    @Override
    public void finalize() throws Throwable {
	super.finalize();
	if (sg2d != null) {
	    sg2d.dispose();
	}
    }
    */

    private void init(int targetWidth, int targetHeight, boolean requestAlpha) {
	this.targetWidth = targetWidth;
	this.targetHeight = targetHeight;
	this.requestAlpha = requestAlpha;
	sg2d = new SurrogateGraphics2D(requestAlpha);
	recorder = new Graphics2DRecorder(sg2d);
    }

    /**
     * Write the image that this object generates to a stream.
     * The orientation of the image is determined by the
     * OutputStreamGraphics argument. The mode determines how this
     * object is scaled. If the full image is to appear in the output
     * stream, the mode should be {@link Mode#FIT} (the default mode)
     * or {@link Mode#FILL}: otherwise part of the image might be
     * lost.
     * @param osg the object managing the output stream
     * @exception IOException an IO error occurred
     * @see #setMode(Mode)
     * @see Mode
     */
    public void write(OSGraphicsOps osg) throws IOException {
	if (!recorders.isEmpty()) {
	    Graphics2D g2d = (Graphics2D) (osg.createGraphics());
	    double pw = osg.getWidth();
	    double pgw = (double)getWidth();
	    double ph = osg.getHeight();
	    double pgh = (double)getHeight();
	    double scale, scalex, scaley;
	    double cx, cy, tx, ty;

	    switch(mode) {
	    case FIT:
		scalex = pw / pgw;
		scaley = ph / pgh;
		scale = (scalex < scaley)? scalex: scaley;
		cx = (scale * pgw);
		cy = (scale *pgh);
		tx = (pw - cx) / 2.0;
		ty = (ph - cy) / 2.0;
		if (scale > 0.0) {
		    if (tx != 0.0 || ty != 0.0) {
			g2d.translate(tx, ty);
		    }
		    if (scale != 1.0) {
			g2d.scale(scale,scale);
		    }
		}
		break;
	    case FIT_HORIZONTAL:
		scalex = pw / pgw;
		cy = (scalex * pgh);
		ty = (ph - cy) / 2.0;
		if (scalex > 0.0) {
		    if (ty != 0.0) {
			g2d.translate(0.0, ty);
		    }
		    if (scalex != 1.0) {
			g2d.scale(scalex,scalex);
		    }
		}
		break;
	    case FIT_VERTICAL:
		scaley = ph / pgh;
		cx = (scaley * pgw);
		tx = (pw - cx) / 2.0;
		if (scaley > 0.0) {
		    if (tx != 0.0) {
			g2d.translate(tx, 0.0);
		    }
		    if (scaley != 1.0) {
			g2d.scale(scaley,scaley);
		    }
		}
		break;
	    case AS_IS:
		break;
	    case FILL:
		scalex = pw / pgw;
		scaley = ph / pgh;
		if (scalex > 0.0 && scaley > 0.0) {
		    if (scalex != 0.0 && scaley != 1.0) {
			g2d.scale(scalex, scaley);
		    }
		}
		break;
	    }
	    try {
		for (Graphics2DRecorder r: recorders) {
		    r.playback(g2d);
		}
	    } catch (Exception e) {
		if (e instanceof IOException) {
		    throw e;
		}
		String msg = errorMsg("writeFailed");
		throw new IOException(msg, e);
	    } finally {
		g2d.dispose();
	    }
	}
	osg.imageComplete();
    }

    /**
     * Print this object using the default printer.
     * If error messages (e.g., due to a printer not being available)
     * should be shown in a dialog box, the method
     * {@link SwingErrorMessage#setComponent(java.awt.Component)} must be
     * called with a non-null argument.
     * <P>
     * Note: this method will print the object on a single page,
     * scaling or truncating the image as specified by the mode
     * set by this instance of PanelGraphics.
     * @exception PrinterException an error occurred while printing.
     * @see #setMode(Mode)
     * @see Mode
     */
    public void print() throws PrinterException {
	print(false);
    }

    /**
     * Print this object, optionally displaying a dialog box that
     * allows a user to choose a printer.
     * A dialog will be created to allow the user to choose a
     * printer.
     * <P>
     * If showDialog is false, and error messages (e.g., due to a
     * printer not being available) should be shown in a dialog box,
     * the method {@link SwingErrorMessage#setComponent(java.awt.Component)}
     * must be called with a non-null argument.
     * <P>
     * Note: this method will print the object on a single page,
     * scaling or truncating the image as specified by the mode set by
     * this instance of PanelGraphics.  The orientation used for
     * printing (portrait or landscape) will be set so that the
     * printed image is as large as possible, but when a printing dialog
     * is displayed, the user can override this choice.  For more
     * fine-grained control over the printing process when a printing
     * dialog box is not displayed, use the method
     * {@link #print(PrinterJob,PrintRequestAttributeSet)}.
     * @param showDialog true if a dialog box allowing the user
     *        to choose a printer should be displayed; false
     *        to use the default.
     * @exception PrinterException an error occurred while printing.
     * @see #setMode(Mode)
     * @see Mode
     */
    public void print(boolean showDialog) throws PrinterException {
	PrinterJob job = PrinterJob.getPrinterJob();
	if (job == null) {
	    if (showDialog) {
		SwingErrorMessage.display(getPanel(), null,
				     localeString("noPrinter"));
	    } else {
		SwingErrorMessage.display(null, localeString("noPrinter"));
	    }
	    return;
	}
	PrintRequestAttributeSet aset =
	    new HashPrintRequestAttributeSet((targetWidth <= targetHeight)?
					     OrientationRequested.PORTRAIT:
					     OrientationRequested.LANDSCAPE);
	boolean status = (showDialog == false) || job.printDialog(aset);
	if (status) {
	    print(job, aset);
	}
     }

    /**
     * Print this object given a printer job and attribute set.
     * This method is provided for the case where the caller
     * determines the printer and the attributes used for printing.
     * <P>
     * Note: this method will print the object on a single page,
     * scaling or truncating the image as specified by the mode
     * set by this instance of PanelGraphics. If the caller has
     * not requested an orientation (e.g., portrait or landscape),
     * the  orientation will be set so that the printed image is
     * as large as possible.
     * @param job a printer job
     * @param aset a print-request attribute set
     * @exception PrinterException an error occurred while printing.
     * @see #setMode(Mode)
     * @see Mode
     * @see javax.print.attribute.standard.OrientationRequested
     */
    public void print(PrinterJob job, PrintRequestAttributeSet aset)
	throws PrinterException
    {
	if  (aset == null) {
	    aset = new HashPrintRequestAttributeSet();
	}
	if (!aset.containsKey(OrientationRequested.class)) {
	    aset.add((targetWidth <= targetHeight)?
		     OrientationRequested.PORTRAIT:
		     OrientationRequested.LANDSCAPE);
	}
	final double pgw = (double)getWidth();
	final double pgh = (double)getHeight();
	job.setPrintable(new Printable() {
		public int print(Graphics graphics,
				 PageFormat pf,
				 int page)
		    throws PrinterException
		{
		    double ix = pf.getImageableX();
		    double iy = pf.getImageableY();
		    // A test showed that pf.getWidth() and
		    // pf.getHeight() return the actual page width and
		    // height, not the imageable area.
		    double pw = pf.getWidth() - 2.0 * ix;
		    double ph = pf.getHeight() - 2.0 * iy;
		    if (page > 0) {
			return Printable.NO_SUCH_PAGE;
		    }
		    Graphics2D g2d = (Graphics2D) graphics;
		    g2d.translate(ix, iy);
		    paintComponentAux(g2d, pw, ph, pgw, pgh);
		    return Printable.PAGE_EXISTS;
		}
	    });
	job.print(aset);
    }


    private void paintComponentAux(Graphics g,
				   double pw, double ph,
				   double pgw, double pgh)
    {
	if (recorders.isEmpty()) return;
	Graphics2D g2d = (Graphics2D) (g.create());
	double scale, scalex, scaley;
	double cx, cy, tx, ty;
	switch(mode) {
	case FIT:
	    scalex = pw / pgw;
	    scaley = ph / pgh;
	    scale = (scalex < scaley)? scalex: scaley;
	    cx = (scale * pgw);
	    cy = (scale *pgh);
	    tx = (pw - cx) / 2.0;
	    ty = (ph - cy) / 2.0;
	    if (scale > 0.0) {
		if (tx != 0.0 || ty != 0.0) {
		    g2d.translate(tx, ty);
		}
		if (scale != 1.0) {
		    g2d.scale(scale,scale);
		}
	    }
	    break;
	case FIT_HORIZONTAL:
	    scalex = pw / pgw;
	    cy = (scalex * pgh);
	    ty = (ph - cy) / 2.0;
	    if (scalex > 0.0) {
		if (ty != 0.0) {
		    g2d.translate(0.0, ty);
		}
		if (scalex != 1.0) {
		    g2d.scale(scalex,scalex);
		}
	    }
	    break;
	case FIT_VERTICAL:
	    scaley = ph / pgh;
	    cx = (scaley * pgw);
	    tx = (pw - cx) / 2.0;
	    if (scaley > 0.0) {
		if (tx != 0.0) {
		    g2d.translate(tx, 0.0);
		}
		if (scaley != 1.0) {
		    g2d.scale(scaley,scaley);
		}
	    }
	    break;
	case AS_IS:
	    break;
	case FILL:
	    scalex = pw / pgw;
	    scaley = ph / pgh;
	    if (scalex > 0.0 && scaley > 0.0) {
		if (scalex != 0.0 && scaley != 1.0) {
		    g2d.scale(scalex, scaley);
		}
	    }
	    break;
	}
	Rectangle2D drawingArea = new Rectangle2D.Double(0.0, 0.0, pgw, pgh);
	g2d.clip(drawingArea);
	try {
	    for (Graphics2DRecorder r: recorders) {
		r.playback(g2d);
	    }
	} finally {
	    g2d.dispose();
	}
    }

    private void createPanel(LayoutManager layout, boolean isDoubleBuffered) {
	panel = new JPanel(layout, isDoubleBuffered) {
		@Override
		public void paintComponent(Graphics g) {
		    super.paintComponent(g);
		    int ipw = getWidth();
		    int iph  = getHeight();
		    // g.clearRect doesn't fill the background with
		    // the background color for some reason, but
		    // filling a rectangle works.
		    // g.clearRect(0, 0, ipw, iph);
		    Color c  = g.getColor();
		    g.setColor(getBackground());
		    g.fillRect(0, 0, ipw, iph);
		    g.setColor(c);
		    double pw = (double)ipw;
		    double pgw = (double)PanelGraphics.this.getWidth();
		    double ph = (double)iph;
		    double pgh = (double)PanelGraphics.this.getHeight();
		    paintComponentAux(g, pw, ph, pgw, pgh);
		}

	    };
    }

    private void callCreatePanel(final LayoutManager layout,
				 final boolean isDoubleBuffered)
    {
	if (SwingUtilities.isEventDispatchThread()) {
	    createPanel(layout, isDoubleBuffered);
	} else {
	    try {
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
			    createPanel(layout, isDoubleBuffered);
			}
		    });
	    } catch (InterruptedException e) {
		String msg = errorMsg("createJPanelInterrupted");
		throw new RuntimeException(msg, e);
	    } catch (InvocationTargetException e) {
		String msg = errorMsg("createJPanelError");
		throw new RuntimeException(msg, e);
	    }
	}
    }

    /**
     * Constructor with default JPanel options.
     * The drawing area is specified by the parameters
     * targetWidth, targetHeight, and preferAlpha.
     * @param targetWidth the width of the drawing area
     * @param targetHeight the height of the drawing area
     * @param requestAlpha true if the drawing area should be
     *        configured with an alpha channel; false otherwise
     */
    public PanelGraphics(int targetWidth, int targetHeight,
			 boolean requestAlpha)
    {
	init(targetWidth, targetHeight, requestAlpha);
	AccessController.doPrivileged
	    (new PrivilegedAction<Void>() {
		    public Void run() {
			callCreatePanel(new FlowLayout(), true);
			return (Void)null;
		    }
		});
    }

    /**
     * Constructor with double-buffering option.
     * The drawing area is specified by the parameters
     * targetWidth, targetHeight, and preferAlpha.
     * The isDoubleBuffered argument is used to construct the panel.
     * @param targetWidth the width of the drawing area
     * @param targetHeight the height of the drawing area
     * @param requestAlpha true if the drawing area should be
     *        configured with an alpha channel; false otherwise
     * @param isDoubleBuffered true if the panel is double buffered;
     *        false otherwise.
     */
    public PanelGraphics(int targetWidth, int targetHeight,
			 boolean requestAlpha,
			 final boolean isDoubleBuffered)
    {
	init(targetWidth, targetHeight, requestAlpha);
	AccessController.doPrivileged
	    (new PrivilegedAction<Void>() {
		    public Void run() {
			callCreatePanel(new FlowLayout(), isDoubleBuffered);
			return (Void) null;
		    }
		});
    }

    /**
     * Constructor specifying a layout manager.
     * The drawing area is specified by the parameters
     * targetWidth, targetHeight, and preferAlpha.
     * The layout argument is used to construct the panel.
     * @param targetWidth the width of the drawing area
     * @param targetHeight the height of the drawing area
     * @param requestAlpha true if the drawing area should be
     *        configured with an alpha channel; false otherwise
     * @param layout the layout manager to use for the panel
     */
    public PanelGraphics(int targetWidth, int targetHeight,
			 boolean requestAlpha,
			 LayoutManager layout)
    {
	init(targetWidth, targetHeight, requestAlpha);
	AccessController.doPrivileged
	    (new PrivilegedAction<Void>() {
		    public Void run() {
			callCreatePanel(layout, true);
			return (Void) null;
		    }
		});
	
    }

    /**
     * Constructor specifying a layout manager and a double-buffer option.
     * The drawing area is specified by the parameters
     * targetWidth, targetHeight, and preferAlpha.
     * The layout and isDoubleBuffered arguments are used to construct
     * the panel.
     * @param targetWidth the width of the drawing area
     * @param targetHeight the height of the drawing area
     * @param requestAlpha true if the drawing area should be
     *        configured with an alpha channel; false otherwise
     * @param layout the layout manager to use for the panel
     * @param isDoubleBuffered true if the panel is double buffered;
     *        false otherwise.
     */
    public PanelGraphics(int targetWidth, int targetHeight,
			 boolean requestAlpha,
			 LayoutManager layout, boolean isDoubleBuffered)
    {
	init(targetWidth, targetHeight, requestAlpha);
	AccessController.doPrivileged
	    (new PrivilegedAction<Void>() {
		    public Void run() {
			callCreatePanel(layout, isDoubleBuffered);
			return (Void) null;
		    }
		});
    }

    @Override
    public Graphics2D createGraphics() {
	return recorder.createGraphics();
    }

    /**
     * Close resources.
     * For this implementation of {@link OSGraphicsOps}, there is nothing
     * to do.
     */
    @Override
    public void close() {
    }



    @Override
    public ColorModel getColorModel() {
	ColorModel cm = panel.getColorModel();
	if (cm == null) {
	    GraphicsConfiguration gconfig = sg2d.getDeviceConfiguration();
	    cm = (gconfig == null)? null: gconfig.getColorModel();
	}
	return cm;
    }

    boolean flushing = false;

    @Override
    public void flush() throws IOException{
	synchronized (this) {
	    if (done) {
		throw new IOException(errorMsg("imageAlreadyComplete"));
	    }
	    if (flushing) {
		throw new IOException(errorMsg("flushing"));
	    }
	    flushing = true;
	}
	if (SwingUtilities.isEventDispatchThread()) {
	    Graphics2DRecorder tmp = recorder;
	    recorder = new Graphics2DRecorder(sg2d);
	    recorders.add(tmp);
	    if (flushImmediately) {
		panel.paintImmediately(panel.getBounds());
		Toolkit.getDefaultToolkit().sync();
	    }
	} else {
	    try {
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
			    Graphics2DRecorder tmp = recorder;
			    recorder = new Graphics2DRecorder(sg2d);
			    recorders.add(tmp);
			    if (flushImmediately) {
				panel.paintImmediately(panel.getBounds());
				Toolkit.getDefaultToolkit().sync();
			    }
			}
		    });
	    } catch (Exception e) {}
	}
	flushing = false;
	if (!flushImmediately) {
	    panel.repaint();
	}
    }

    @Override
    public void imageComplete() throws IOException {
	flush();
	synchronized (this) {
	    done = true;
	}
    }
}

//  LocalWords:  exbundle JPanel PanelGraphics setMode blockquote pre
//  LocalWords:  getPanel createGraphics imageComplete setVisible ccw
//  LocalWords:  newFramedInstance exitOnClose addButtons pfailed dir
//  LocalWords:  widthLabel heightLabel saveAs png noExt fileError sm
//  LocalWords:  html saveAsOptions OutputStreamGraphics osg aset ipw
//  LocalWords:  IOException SwingErrorMessage setComponent showDialog iph
//  LocalWords:  noPrinter PrinterException getWidth getHeight se
//  LocalWords:  imageable createJPanelInterrupted createJPanelError
//  LocalWords:  targetWidth targetHeight preferAlpha requestAlpha
//  LocalWords:  isDoubleBuffered OSGraphicsOps imageAlreadyComplete
//  LocalWords:  PrinterJob PrintRequestAttributeSet setSize boolean
//  LocalWords:  JComponent paintImmediately GraphicsCreator accessor
//  LocalWords:  newPanelGraphicsCreator AnimatedPanelGraphics
//  LocalWords:  LinkedList DRecorder getRepetitionCount writeFailed
//  LocalWords:  graphicsCreator clearRect ExitAccessor askOnClose
//  LocalWords:  ExitPermission SecurityManager getSecurityManager
//  LocalWords:  checkPermission SecurityException askMode exitTitle
