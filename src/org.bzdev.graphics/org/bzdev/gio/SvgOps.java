package org.bzdev.gio;

/**
 * Auxiliary SVG operations.
 * When {@link org.bzdev.gio.OutputStreamGraphics} is used to generate
 * SVG, a provider will typically use the width and height passed to
 * the {@link org.bzdev.gio.OutputStreamGraphics}'s constructor the
 * svg element's width, height, and viewBox attributes, with the
 * viewBox typically set to "0 0 W H" where W is the width and
 * H is the height.  The width and height attributes should normally
 * use units of points (pt).  For scaling the image, other units
 * and other values may be appropriate.
 * <P>
 * Implementing this interface is optional for an SVG provider but is
 * encouraged.
 */
public interface SvgOps {

    /**
     * Set the SVG height and width attributes.
     * This option should not override the viewBox.
     * The units must be standard SVG units (px, pt, mm, etc.).
     * <P>
     * This method should be called before the output is
     * written, which occurs when
     * {@link OutputStreamGraphics#imageComplete()} is called.
     * @param width the width
     * @param widthUnit the units for the width
     * @param height the height
     * @param heightUnit the units for the height
     * @exception IllegalStateException this method was called at
     *            the wrong time.
     */
    void setDimensions(double width, String widthUnit,
		       double height, String heightUnit)
	throws IllegalStateException;

}
