package org.bzdev.swing;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.bzdev.util.EvntListenerList;

/**
 * Class to track background color changes due to a pluggable look and
 * feel that toggles between normal mode and dark mode.
 * If the red, green, and blue components of a color have values less
 * than 128 (with the maximum being 255), then dark mode is set to true
 * otherwise it is false.
 <P>
 * If the mode changes, the {@link java.beans.PropertyChangeEvent}
 * will have a source equal to {@link DarkmodeMonitor}.class, a
 * property name equal to "darkmode", and Boolean values for the
 * old value and new value fields.
 * <P>
 * To get the mode initially, call {@link DarkmodeMonitor#getDarkmode()}.
 */
public class DarkmodeMonitor {
    private static JFrame frame;
    private static boolean darkmode = false;
    private static EvntListenerList list = new EvntListenerList();

    /**
     * Get the current dark-mode state.
     * This may be called after the look and feel is installed.
     * @return true if dark mode is being used; false otherwise
     */
    static public boolean getDarkmode() {
	if (!initialized) init();
	return darkmode;
    }

    private static boolean initialized = false;

    /**
     * Initialization.
     * This may be called after the look and feel is installed.
     * Until it is called, events will not be sent to listeners.
     * {@link #getDarkmode()} will call this method automatically.
     */
    public static synchronized void init() {
	if (initialized) return;
	try {
	    SwingUtilities.invokeLater(() -> {
		    try {
			frame = new JFrame();
			Toolkit.getDefaultToolkit().sync();
		    } catch (Exception e) {}
		});
	    SwingUtilities.invokeLater(() -> {
		    try {
			Toolkit.getDefaultToolkit().sync();
		    } catch (Exception e) {}
		    frame.getContentPane().addPropertyChangeListener(evnt -> {
			    if (modeChanged()) {
				Boolean oldmode = Boolean.valueOf(!darkmode);
				Boolean newmode = Boolean.valueOf(darkmode);
				PropertyChangeEvent evt = new
				    PropertyChangeEvent(DarkmodeMonitor.class,
							"darkmode",
							oldmode, newmode);
				SwingUtilities.invokeLater(() -> {
				    for (Object o: list.getListeners
					     (PropertyChangeListener.class)) {
					if (o instanceof
					    PropertyChangeListener) {
					    PropertyChangeListener l =
						(PropertyChangeListener) o;
					    l.propertyChange(evt);
					}
				    }
				});
			    }
			});
		    modeChanged();
		});
	} catch (Exception e) {
	    // e.printStackTrace();
	}
	initialized = true;
    }

    private static boolean modeChanged() {
	// Color c = frame.getContentPane().getBackground();
	Color c = (Color) UIManager.get("Panel.background");
	boolean dm = 
	    ((c.getRed() < 128 && c.getGreen() < 128 && c.getBlue() < 128));
	try {
	    return (dm != darkmode);
	} finally {
	    darkmode = dm;
	}
    }
    
    /**
     * Add a property change listener.
     * @param listener the listener
     */
    public static synchronized void
	addPropertyChangeListener( PropertyChangeListener listener)
    {
	list.add(PropertyChangeListener.class, listener);
    }

    /**
     * Remove a property change listener.
     * @param listener the listener
     */
    public static synchronized void
	removePropertyChangeListener(PropertyChangeListener listener)
    {
	list.remove(PropertyChangeListener.class, listener);
    }

    /**
     * Set the look and feel to a modified system look and feel.
     * Some system look and feels have a dark caret on a dark background
     * making the caret (cursor) hard to see. For JTextField, JTextArea,
     * and JEditorPane, the look and feel is modified so that the
     * caret color always matches the foreground color (that is,
     * the text color) when using the settings provided by the look and
     * feel.
     */
    public static void setSystemPLAF() {
	try {
	    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	    /*
	     * Some look and feels do not set this to something that
	     * is easy to see.
	     */
	    UIManager.put("TextField.caretForeground",
			  UIManager.get("TextField.foreground"));
	    UIManager.put("TextArea.caretForeground",
			  UIManager.get("TextArea.foreground"));
	    UIManager.put("EditorPane.caretForeground",
			  UIManager.get("EditorPane.foreground"));
	    DarkmodeMonitor.addPropertyChangeListener(evnt -> {
		    UIManager.put("TextField.caretForeground",
				  UIManager.get("TextField.foreground"));
		    UIManager.put("TextArea.caretForeground",
				  UIManager.get("TextArea.foreground"));
		    UIManager.put("EditorPane.caretForeground",
				  UIManager.get("EditorPane.foreground"));
		});
	} catch (Exception e) {
	}
    }

}
