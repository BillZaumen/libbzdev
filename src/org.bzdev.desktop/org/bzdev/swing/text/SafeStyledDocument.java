package org.bzdev.swing.text;

import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.AttributeSet;
import javax.swing.text.Segment;
import javax.swing.text.BadLocationException;
import javax.swing.event.UndoableEditListener;
import javax.swing.event.DocumentListener;
import javax.swing.SwingUtilities;

import javax.swing.text.Style;
import java.awt.Color;
import java.awt.Font;

import java.lang.reflect.UndeclaredThrowableException;
import java.lang.reflect.InvocationTargetException;

/**
 * A StyledDocument class that is thread safe.
 * Operations are performed on the Swing event-dispatching thread.
 * An existing document is encapsulated, with access to it controlled
 * so as to be thread safe.
 * <P>
 * This class makes use of the static method
 * {@link SwingUtilities#invokeLater(Runnable)}. One should avoid the
 * use of synchronized methods that call methods in this class when
 * those synchronized methods might be called from tasks waiting on
 * the AWT event dispatch queue, as there is a possibility of
 * deadlock: If for some class methods m1 and m2 are synchronized and
 * call one of the methods in this class, and m1 is called, a call to
 * {@link SwingUtilities#invokeLater(Runnable)} may process other
 * entries on its event queue first, causing m2 to be called, but m2
 * will wait until m1 returns, which cannot occur until m2 returns.
 * An experiment indicated that the behavior of the event queue can
 * change if, for example, a security manager is installed, so initial
 * testing can easily miss cases that could lead to deadlocks.
 */
public class SafeStyledDocument extends SafeDocument implements StyledDocument {
    StyledDocument sdoc;

    /**
     * Constructor.
     * @param document the document to encapsulate
     */
    public SafeStyledDocument(StyledDocument document) {
	super(document);
	sdoc = document;
    }

    abstract class RunnableWithStyle extends RunnableWithRE {
	Style style;
    }

    /**
     * Execute code on the event dispatch thread and return a Style.
     */
    protected Style doitStyle(RunnableWithStyle r) {
	try {
	    SwingUtilities.invokeAndWait(r);
	    if (r.rexception != null) throw r.rexception;
	    return r.style;
	} catch (InterruptedException ie) {
	    throw new SafeDocument.RIException(ie);
	} catch (InvocationTargetException ite) {
	    Throwable thr = ite.getCause();
	    if (thr instanceof RuntimeException)
		throw (RuntimeException) thr;
	    else
		throw new SafeDocument.RTException(thr);
	}
    }
    
    /**
     * Adds a new style into the logical style hierarchy.  Style attributes
     * resolve from bottom up so an attribute specified in a child
     * will override an attribute specified in the parent.
     *
     * @param nm   the name of the style (must be unique within the
     *   collection of named styles).  The name may be null if the style
     *   is unnamed, but the caller is responsible
     *   for managing the reference returned as an unnamed style can't
     *   be fetched by name.  An unnamed style may be useful for things
     *   like character attribute overrides such as found in a style
     *   run.
     * @param parent the parent style.  This may be null if unspecified
     *   attributes need not be resolved in some other style.
     * @return the style
     */
    @Override
    public Style addStyle(String nm, Style parent) {
	if (SwingUtilities.isEventDispatchThread()) {
	    return sdoc.addStyle(nm, parent);
	} else {
	    final String xnm = nm;
	    final Style xparent = parent;
	    return doitStyle(new RunnableWithStyle () {
		    protected void doit() {
			sdoc.addStyle(xnm, xparent);
		    }
		});
	}
    }

    /**
     * Removes a named style previously added to the document.
     *
     * @param nm  the name of the style to remove
     */
    @Override
    public void removeStyle(String nm) {
	if (SwingUtilities.isEventDispatchThread()) {
	    sdoc.removeStyle(nm);
	    return;
	} else {
	    final String xnm = nm;
	    doitVoid(new RunnableWithRE() {
		    protected void doit() {
			sdoc.removeStyle(xnm);
		    }
		});
	}
    }

    /**
     * Fetches a named style previously added.
     *
     * @param nm  the name of the style
     * @return the style
     */
    @Override
    public Style getStyle(String nm) {
	if (SwingUtilities.isEventDispatchThread()) {
	    return sdoc.getStyle(nm);
	} else {
	    final String xnm = nm;
	    return doitStyle(new RunnableWithStyle () {
		    protected void doit() {
			sdoc.getStyle(xnm);
		    }
		});
	}
    }
    

    /**
     * Changes the content element attributes used for the given range of
     * existing content in the document.  All of the attributes
     * defined in the given Attributes argument are applied to the
     * given range.  This method can be used to completely remove
     * all content level attributes for the given range by
     * giving an Attributes argument that has no attributes defined
     * and setting replace to true.
     *
     * @param offset the start of the change &gt;= 0
     * @param length the length of the change &gt;= 0
     * @param attr the non-null attributes to change to.  Any attributes
     *  defined will be applied to the text for the given range.
     * @param replace indicates whether or not the previous
     *  attributes should be cleared before the new attributes
     *  as set.  If true, the operation will replace the
     *  previous attributes entirely.  If false, the new
     *  attributes will be merged with the previous attributes.
     */
    @Override
    public void setCharacterAttributes(int offset, int length,
				       AttributeSet attr,
				       boolean replace) {
	if (SwingUtilities.isEventDispatchThread()) {
	    sdoc.setCharacterAttributes(offset, length, attr, replace);
	    return;
	} else {
	    final int xoffset = offset;
	    final int xlength = length;
	    final AttributeSet xattr = attr;
	    final boolean xreplace = replace;
	    doitVoid(new RunnableWithRE() {
		    protected void doit() {
			sdoc.setCharacterAttributes(xoffset, xlength,
						    xattr, xreplace);
		    }
		});
	}
    }
				       
    /**
     * Sets paragraph attributes.
     *
     * @param offset the start of the change &gt;= 0
     * @param length the length of the change &gt;= 0
     * @param attr the non-null attributes to change to.  Any attributes
     *  defined will be applied to the text for the given range.
     * @param replace indicates whether or not the previous
     *  attributes should be cleared before the new attributes
     *  are set.  If true, the operation will replace the
     *  previous attributes entirely.  If false, the new
     *  attributes will be merged with the previous attributes.
     */
    @Override
    public void setParagraphAttributes(int offset, int length,
				       AttributeSet attr,
				       boolean replace) {
	if (SwingUtilities.isEventDispatchThread()) {
	    sdoc.setParagraphAttributes(offset, length, attr, replace);
	    return;
	} else {
	    final int xoffset = offset;
	    final int xlength = length;
	    final AttributeSet xattr = attr;
	    final boolean xreplace = replace;
	    doitVoid(new RunnableWithRE() {
		    protected void doit() {
			sdoc.setParagraphAttributes(xoffset, xlength,
						    xattr, xreplace);
		    }
		});
	}
    }

    /**
     * Sets the logical style to use for the paragraph at the
     * given position.  If attributes aren't explicitly set
     * for character and paragraph attributes they will resolve
     * through the logical style assigned to the paragraph, which
     * in turn may resolve through some hierarchy completely
     * independent of the element hierarchy in the document.
     *
     * @param pos the starting position &gt;= 0
     * @param s the style to set
     */
    @Override
    public void setLogicalStyle(int pos, Style s) {
	if (SwingUtilities.isEventDispatchThread()) {
	    sdoc.setLogicalStyle(pos, s);
	} else {
	    final int xpos = pos;
	    final Style xs = s;
	    doitVoid(new RunnableWithRE() {
		    protected void doit() {
			sdoc.setLogicalStyle(xpos, xs);
		    }
		});
	}
    }

    /**
     * Gets a logical style for a given position in a paragraph.
     *
     * @param p the position &gt;= 0
     * @return the style
     */
    @Override
    public Style getLogicalStyle(int p) {
	if (SwingUtilities.isEventDispatchThread()) {
	    return sdoc.getLogicalStyle(p);
	} else {
	    final int xpos = p;
	    return doitStyle(new RunnableWithStyle () {
		    protected void doit() {
			sdoc.getLogicalStyle(xpos);
		    }
		});
	}
    }

    /**
     * Gets the element that represents the paragraph that
     * encloses the given offset within the document.
     *
     * @param pos the offset &gt;= 0
     * @return the element
     */
    @Override
    public Element getParagraphElement(int pos) {
	if (SwingUtilities.isEventDispatchThread()) {
	    return sdoc.getParagraphElement(pos);
	} else {
	    final int xpos = pos;
	    return doitElement(new RunnableWithElement() {
		    public void doit() {
			element = sdoc.getParagraphElement(xpos);
		    }
		});
	}
    }

    /**
     * Gets the element that represents the character that
     * is at the given offset within the document.
     *
     * @param pos the offset &gt;= 0
     * @return the element
     */
    @Override
    public Element getCharacterElement(int pos) {
	if (SwingUtilities.isEventDispatchThread()) {
	    return sdoc.getCharacterElement(pos);
	} else {
	    final int xpos = pos;
	    return doitElement(new RunnableWithElement() {
		    public void doit() {
			element = sdoc.getCharacterElement(xpos);
		    }
		});
	}
    }

    /*
     * Assume following are just convenience methods so
     * that we don't have to put these on the event dispatch
     * thread.
     */

    /**
     * Takes a set of attributes and turn it into a foreground color
     * specification.  This might be used to specify things
     * like brighter, more hue, etc.
     *
     * @param attr the set of attributes
     * @return the color
     */
    @Override
    public Color getForeground(AttributeSet attr) {
	return sdoc.getForeground(attr);
    }

    /**
     * Takes a set of attributes and turn it into a background color
     * specification.  This might be used to specify things
     * like brighter, more hue, etc.
     *
     * @param attr the set of attributes
     * @return the color
     */
    @Override
    public Color getBackground(AttributeSet attr) {
	return sdoc.getBackground(attr);
    }

    /**
     * Takes a set of attributes and turn it into a font
     * specification.  This can be used to turn things like
     * family, style, size, etc into a font that is available
     * on the system the document is currently being used on.
     *
     * @param attr the set of attributes
     * @return the font
     */
    @Override
    public Font getFont(AttributeSet attr) {
	return sdoc.getFont(attr);
    }
}

//  LocalWords:  StyledDocument SwingUtilities invokeLater Runnable
//  LocalWords:  AWT nm attr pos
