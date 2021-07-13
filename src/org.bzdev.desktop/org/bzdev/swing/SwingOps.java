package org.bzdev.swing;

import java.awt.Toolkit;
import java.awt.Component;
import javax.swing.SwingUtilities;

/**
 * Additional Swing utilities.
 */
public class SwingOps {

    private static final int RFIW_LIMIT = 256;
    private static int lastLimcount = -1;

    /**
     * Try {@link Component#requestFocusInWindow()} on a target
     * component repeatedly until that method succeeds or until an
     * iteration count is exceeded.
     * Between each try, the event dispatch thread is emptied by
     * calling Tookit.getDefaultToolkit.sync().
     * <P>
     * This method is useful in cases where
     * {@link Component#requestFocusInWindow()} would fail due to being
     * called too early.
     * @param target the component
     * @param limit the iteration limit; 0 for a default.
     */
    public static void tryRequestFocusInWindow(final Component target,
					       int limit)
    {
	final int lim = (limit == 0)? RFIW_LIMIT: limit;

	Runnable r = new Runnable() {
		Runnable repeat = null;
		int limcount = 0;
		public void run() {
		    if (repeat == null) {
			repeat = () -> {
			    if (target.requestFocusInWindow()) {
				lastLimcount = limcount;
				return;
			    } else {
				limcount++;
				if (limcount > lim) {
				    lastLimcount = limcount;
				    return;
				}
				SwingUtilities.invokeLater(()-> {
					Toolkit.getDefaultToolkit().sync();
					SwingUtilities.invokeLater(repeat);
				    });
			    }
			};
		    }
		    SwingUtilities.invokeLater(()-> {
			    Toolkit.getDefaultToolkit().sync();
			    SwingUtilities.invokeLater(repeat);
			});
		}
	    };
	if (target.requestFocusInWindow() == false) {
	    r.run();
	}
    }

    /**
     * Return the number of iterations for SwingOps methods that have
     * an explicit or implied iteration limit.
     * The methods with this property are
     * <UL>
     * <LI> {@link #tryRequestFocusInWindow(Component,int)}.
     * </UL>
     * This method is provided for debugging or testing.
     * @return the number of iterations for the last call to one
     *         of these methods; -1 before the first call is made.
     */
    public static int lastLimitCount() {
	return lastLimcount;
    }
}

//  LocalWords:  requestFocusInWindow SwingOps
//  LocalWords:  tryRequestFocusInWindow
