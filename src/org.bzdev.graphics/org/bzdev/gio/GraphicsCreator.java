package org.bzdev.gio;
import java.awt.Graphics2D;

/**
 * Interface for objects that create graphics contexts of type Graphic2D.
 * Examples of uses include the org.bzdev.gio package.
 */
public interface GraphicsCreator {
    /**
     * Create a graphics context.
     * @return the graphics context
     */
    Graphics2D createGraphics() throws UnsupportedOperationException;
}
