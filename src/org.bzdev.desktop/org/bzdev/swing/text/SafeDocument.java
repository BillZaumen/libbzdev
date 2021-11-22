package org.bzdev.swing.text;

import java.lang.reflect.UndeclaredThrowableException;
import java.lang.reflect.InvocationTargetException;

import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.AttributeSet;
import javax.swing.text.Segment;
import javax.swing.text.BadLocationException;
import javax.swing.event.UndoableEditListener;
import javax.swing.event.DocumentListener;
import javax.swing.SwingUtilities;

// import javax.swing.text.PlainDocument;

/**
 * A Document class that is thread safe.
 * Operations are performed on the event-dispatch thread.
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
public class SafeDocument implements Document {
    Document doc;
    /**
     * Constructor.
     * @param doc a document to encapsulate
     */
    public SafeDocument(Document doc) {
	this.doc = doc;
    }

    /**
     * Get the encapsulated document.
     * @return the encapsulated document
     */
    public Document getEncapsulatedDocument() {
	return doc;
    }

    /**
     * SafeDocument runtime exception class for interrupted exceptions.
     */
    public class RIException extends java.lang.RuntimeException {
	RIException(InterruptedException ie) {
	    super((Throwable)ie);
	}
    }

    /**
     * SafeDocument runtime-exception class.
     * For an instance of this class to be constructed, a call
     * to SwingUtilities.invokeAndWait would have to be interrupted.
     */
    public class RTException extends java.lang.RuntimeException {
	RTException (Throwable cause) {
	    super(cause);
	}
    }

    /**
     * Runnable that tracks runtime exceptions.
     * This class is used internally by SafeDocument and SafeStyledDocument.
     */
    abstract class RunnableWithRE implements Runnable {
	RuntimeException rexception;
	/**
	 * The operation to perform.
	 */
	abstract protected void doit();
	public void run(){
	    try {
		doit();
	    }  catch (RuntimeException re) {
		rexception = re;
	    }
	}
    }

    abstract class RunnableWithInt extends RunnableWithRE {
	int intval;
    }

    /**
     * Perform an operation on the event dispatch thread and return an
     * int.
     * @param r an object providing the code to execute in a method named
     *        doit()
     * @return an integer value
     */
    protected int doitInt(RunnableWithInt r) {
	try {
	    SwingUtilities.invokeAndWait(r);
	    if (r.rexception != null) throw r.rexception;
	    return r.intval;
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
     * Returns number of characters of content currently
     * in the document.
     *
     * @return number of characters &gt;= 0
     */
    @Override
    public int getLength() {
	if (SwingUtilities.isEventDispatchThread()) {
	    return doc.getLength();
	} else {
	    return doitInt(new RunnableWithInt() {
		    protected void doit() {
			intval = doc.getLength();
		    }
		});
	}
    }

    /**
     * Perform an operation on the event dispatch thread with no
     * return value.
     * @param r an object providing the code to execute in a method named
     *        doit()
     */
    protected void doitVoid(RunnableWithRE r) {
	try {
	    SwingUtilities.invokeAndWait(r);
	    if (r.rexception != null) throw r.rexception;
	    return;
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
     * Registers the given observer to begin receiving notifications
     * when changes are made to the document.
     * @param listener the observer to register
     * @see Document#removeDocumentListener
     */
    @Override
    public void addDocumentListener(DocumentListener listener) {
	if (SwingUtilities.isEventDispatchThread()) {
	    doc.addDocumentListener(listener);
	    return;
	} else {
	    final DocumentListener lis = listener;
	    doitVoid(new RunnableWithRE() {
		    protected void doit() {
			    doc.addDocumentListener(lis);
		    }
		});
	}
    }
    
    /**
     * Unregisters the given observer from the notification list
     * so it will no longer receive change updates.
     *
     * @param listener the observer to register
     * @see Document#addDocumentListener
     */
    @Override
    public void removeDocumentListener(DocumentListener listener) {
	if (SwingUtilities.isEventDispatchThread()) {
	    doc.removeDocumentListener(listener);
	    return;
	} else {
	    final DocumentListener lis = listener;
	    doitVoid(new RunnableWithRE() {
		    protected void doit() {
			doc.removeDocumentListener(lis);
		    }
		});
	}
    }

    /**
     * Registers the given observer to begin receiving notifications
     * when undoable edits are made to the document.
     * @param listener the observer to register
     * @see javax.swing.event.UndoableEditEvent
     */
    @Override
    public void addUndoableEditListener(UndoableEditListener listener) {
	if (SwingUtilities.isEventDispatchThread()) {
	    doc.addUndoableEditListener(listener);
	    return;
	} else {
	    final UndoableEditListener lis = listener;
	    doitVoid(new RunnableWithRE() {
		    protected void doit() {
			doc.addUndoableEditListener(lis);
		    }
		});
	}
    }

    /**
     * Unregisters the given observer from the notification list
     * so it will no longer receive updates.
     *
     * @param listener the observer to register
     * @see javax.swing.event.UndoableEditEvent
     */
    @Override
    public void removeUndoableEditListener(UndoableEditListener listener) {
	if (SwingUtilities.isEventDispatchThread()) {
	    doc.removeUndoableEditListener(listener);
	} else {
	    final UndoableEditListener lis = listener;
	    doitVoid(new RunnableWithRE() {
		    protected void doit() {
			    doc.removeUndoableEditListener(lis);
		    }
		});
	}
    }

    abstract class RunnableWithObj extends RunnableWithRE {
	Object obj;
    }

    /**
     * Perform an operation on the event dispatch thread and return an
     * object.
     * @param r an object providing the code to execute in a method named
     *        doit() that will store its return value in r.obj
     */
    protected Object doitObject(RunnableWithObj r) {
	try {
	    SwingUtilities.invokeAndWait(r);
	    if (r.rexception != null) throw r.rexception;
	    return r.obj;
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
     * Gets the properties associated with the document.
     *
     * @param key a non-<code>null</code> property key
     * @return the properties
     * @see #putProperty(Object, Object)
     */
    @Override
    public Object getProperty(Object key) {
	if (SwingUtilities.isEventDispatchThread()) {
	    return doc.getProperty(key);
	} else {
	    final Object k = key;
	    return doitObject(new RunnableWithObj() {
		    protected void doit() {
			    obj = doc.getProperty(k);
		    }
		});
	}
    }

    /**
     * Associates a property with the document.  Two standard
     * property keys provided are: <a href="#StreamDescriptionProperty">
     * <code>StreamDescriptionProperty</code></a> and
     * <a href="#TitleProperty"><code>TitleProperty</code></a>.
     * Other properties, such as author, may also be defined.
     *
     * @param key the non-<code>null</code> property key
     * @param value the property value
     * @see #getProperty(Object)
     */
    @Override
    public void putProperty(Object key,  Object value) {
	if (SwingUtilities.isEventDispatchThread()) {
	    doc.putProperty(key, value);
	    return;
	} else {
	    final Object val = value;
	    final Object k = key;
	    doitVoid(new RunnableWithRE() {
		    protected void doit() {
			    doc.putProperty(k,val);
		    }
		});
	}
    }
    /**
     * Class to run a thread while logging any BadLocationException that occurs.
     */
    abstract public class RunnableWithBadLocException extends RunnableWithRE {
	BadLocationException exception;
	protected void doit() {throw new UnsupportedOperationException();}
	/**
	 * The code the runnable will execute.
	 * @exception BadLocationException a BadLocationException was thrown
	 */
	abstract protected void doitBLE() throws BadLocationException;
	public void run() {
	    try {
		doitBLE();
	    } catch (BadLocationException  e) {
		exception = e; 
	    } catch (RuntimeException re) {
		rexception = re;
	    }
	}
    }

    /**
     * Perform an operation on the event dispatch thread, possibly
     * throwing a BadLocationException.
     * @param r an object whose doitBLE() method provides the code to execute
     * @exception BadLocationException a BadLocationException occurred
     */
    protected void doitVoidBLE(RunnableWithBadLocException r) 
	throws BadLocationException {
	try {
	    SwingUtilities.invokeAndWait(r);
	    if (r.exception != null) throw r.exception;
	    if (r.rexception != null) throw r.rexception;
	    return;
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
     * Removes a portion of the content of the document.
     * This will cause a DocumentEvent of type
     * DocumentEvent.EventType.REMOVE to be sent to the
     * registered DocumentListeners, unless an exception
     * is thrown.  The notification will be sent to the
     * listeners by calling the removeUpdate method on the
     * DocumentListeners.
     * <p>
     * To ensure reasonable behavior in the face
     * of concurrency, the event is dispatched after the
     * mutation has occurred. This means that by the time a
     * notification of removal is dispatched, the document
     * has already been updated and any marks created by
     * <code>createPosition</code> have already changed.
     * For a removal, the end of the removal range is collapsed
     * down to the start of the range, and any marks in the removal
     * range are collapsed down to the start of the range.
     * <p style="text-align:center"><img src="doc-files/Document-remove.gif"
     *  alt="Diagram shows removal of 'quick' from 'The quick brown fox.'"
     *  class="imgBackground">
     * <p>
     * If the Document structure changed as result of the removal,
     * the details of what Elements were inserted and removed in
     * response to the change will also be contained in the generated
     * DocumentEvent. It is up to the implementation of a Document
     * to decide how the structure should change in response to a
     * remove.
     * <p>
     * If the Document supports undo/redo, an UndoableEditEvent will
     * also be generated.
     *
     * @param offs  the offset from the beginning &gt;= 0
     * @param len   the number of characters to remove &gt;= 0
     * @exception BadLocationException  some portion of the removal range
     *   was not a valid part of the document.  The location in the exception
     *   is the first bad position encountered.
     * @see javax.swing.event.DocumentEvent
     * @see javax.swing.event.DocumentListener
     * @see javax.swing.event.UndoableEditEvent
     * @see javax.swing.event.UndoableEditListener
     */
    @Override
    public void remove(int offs, int len) throws BadLocationException {
	if (SwingUtilities.isEventDispatchThread()) {
	    doc.remove(offs, len);
	} else {
	    final int xoffs = offs;
	    final int xlen = len;
	    doitVoidBLE(new RunnableWithBadLocException() {
		    protected void doitBLE() throws BadLocationException {
			doc.remove(xoffs, xlen);
		    }
		});
	}
    }

    /**
     * Inserts a string of content.  This will cause a DocumentEvent
     * of type DocumentEvent.EventType.INSERT to be sent to the
     * registered DocumentListeners, unless an exception is thrown.
     * The DocumentEvent will be delivered by calling the
     * insertUpdate method on the DocumentListener.
     * The offset and length of the generated DocumentEvent
     * will indicate what change was actually made to the Document.
     * <p style="text-align:center"><img src="doc-files/Document-insert.gif"
     *  alt="Diagram shows insertion of 'quick' in 'The quick brown fox'"
     *  class="imgBackground">
     * <p>
     * If the Document structure changed as result of the insertion,
     * the details of what Elements were inserted and removed in
     * response to the change will also be contained in the generated
     * DocumentEvent.  It is up to the implementation of a Document
     * to decide how the structure should change in response to an
     * insertion.
     * <p>
     * If the Document supports undo/redo, an UndoableEditEvent will
     * also be generated.
     *
     * @param offset  the offset into the document to insert the content &gt;= 0
.
     *    All positions that track change at or after the given location
     *    will move.
     * @param str    the string to insert
     * @param a      the attributes to associate with the inserted
     *   content.  This may be null if there are no attributes.
     * @exception BadLocationException  the given insert position is not a valid
     * position within the document
     * @see javax.swing.event.DocumentEvent
     * @see javax.swing.event.DocumentListener
     * @see javax.swing.event.UndoableEditEvent
     * @see javax.swing.event.UndoableEditListener
     */
    @Override
    public void insertString(int offset, String str, AttributeSet a)
	throws BadLocationException {
	if (SwingUtilities.isEventDispatchThread()) {
	    doc.insertString(offset, str, a);
	} else {
	    final int xoffset = offset;
	    final String xstr = str;
	    final AttributeSet xa = a;
	    doitVoidBLE(new RunnableWithBadLocException() {
		    protected void doitBLE() throws BadLocationException {
			doc.insertString(xoffset, xstr, xa);
		    }
		});
	}
    }

    abstract class RunnableWithString extends RunnableWithBadLocException {
	String string;
    }

    /**
     * Perform an operation on the event dispatch thread returning a
     * String, possibly throwing a BadLocationException.
     * @param r an object whose doitBLE() method provides the code to execute
     *    with the return value stored in r.string
     * @return a string
     * @exception BadLocationException a BadLocationException occurred
     */
    protected String doitStringBLE(RunnableWithString r) 
	throws BadLocationException {
	try {
	    SwingUtilities.invokeAndWait(r);
	    if (r.exception != null) throw r.exception;
	    if (r.rexception != null) throw r.rexception;
	    return r.string;
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
     * Fetches the text contained within the given portion
     * of the document.
     *
     * @param offset  the offset into the document representing the desired
     *   start of the text &gt;= 0
     * @param length  the length of the desired string &gt;= 0
     * @return the text, in a String of length &gt;= 0
     * @exception BadLocationException  some portion of the given range
     *   was not a valid part of the document.  The location in the exception
     *   is the first bad position encountered.
     */
    @Override
    public String getText(int offset, int length) throws BadLocationException {
	if (SwingUtilities.isEventDispatchThread()) {
	    return doc.getText(offset, length);
	} else {
	    final int xoffset = offset;
	    final int xlen = length;
	    return doitStringBLE(new RunnableWithString() {
		    protected void doitBLE() throws BadLocationException {
			string = doc.getText(xoffset, xlen);
		    }
		});
	}
    }
    
    /**
     * Fetches the text contained within the given portion
     * of the document.
     * <p>
     * If the partialReturn property on the txt parameter is false, the
     * data returned in the Segment will be the entire length requested and
     * may or may not be a copy depending upon how the data was stored.
     * If the partialReturn property is true, only the amount of text that
     * can be returned without creating a copy is returned.  Using partial
     * returns will give better performance for situations where large
     * parts of the document are being scanned.  The following is an example
     * of using the partial return to access the entire document:
     *
     * <pre><code>
     *
     * &nbsp; int nleft = doc.getDocumentLength();
     * &nbsp; Segment text = new Segment();
     * &nbsp; int offs = 0;
     * &nbsp; text.setPartialReturn(true);
     * &nbsp; while (nleft &gt; 0) {
     * &nbsp;     doc.getText(offs, nleft, text);
     * &nbsp;     // do someting with text
     * &nbsp;     nleft -= text.count;
     * &nbsp;     offs += text.count;
     * &nbsp; }
     *
     * </code></pre>
     *
     * @param offset  the offset into the document representing the desired
     *   start of the text &gt;= 0
     * @param length  the length of the desired string &gt;= 0
     * @param txt the Segment object to return the text in
     *
     * @exception BadLocationException  Some portion of the given range
     *   was not a valid part of the document.  The location in the exception
     *   is the first bad position encountered.
     */
    @Override
    public void getText(int offset, int length, Segment txt)
	throws BadLocationException {
	if (SwingUtilities.isEventDispatchThread()) {
	    doc.getText(offset, length, txt);
	} else {
	    final int xoffset = offset;
	    final int xlength = length;
	    final Segment xtxt = txt;
	    doitVoidBLE(new RunnableWithBadLocException() {
		    protected void doitBLE() throws BadLocationException {
			doc.getText(xoffset, xlength, xtxt);
		    }
		});
	}
    }

    abstract class RunnableWithPosition extends RunnableWithRE {
	Position position;
    }

    abstract class RunnableWithPositionBLE
	extends RunnableWithBadLocException {
	Position position;
    }

    /**
     * Execute an operation on the event dispatch thread and return
     * a Position.
     * @param r an object whose doitBLE() method provides the code to execute
     *        and that will store a return value in r.position
     * @return the position
     */
    protected Position doitPosition(RunnableWithPosition r) {
	try {
	    SwingUtilities.invokeAndWait(r);
	    if (r.rexception != null) throw r.rexception;
	    return r.position;
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
     * Returns a position that represents the start of the document.  The
     * position returned can be counted on to track change and stay
     * located at the beginning of the document.
     *
     * @return the position
     */
    @Override
    public Position getStartPosition() {
	if (SwingUtilities.isEventDispatchThread()) {
	    return doc.getStartPosition();
	} else {
	    return doitPosition(new RunnableWithPosition() {
		    protected void doit() {
			position = doc.getStartPosition();
		    }
		});
	}
    }

    /**
     * Returns a position that represents the end of the document.  The
     * position returned can be counted on to track change and stay
     * located at the end of the document.
     *
     * @return the position
     */
    @Override
    public Position getEndPosition() {
	if (SwingUtilities.isEventDispatchThread()) {
	    return doc.getEndPosition();
	} else {
	    return doitPosition(new RunnableWithPosition() {
		    protected void doit() {
			position = doc.getEndPosition();
		    }
		});
	}
    }

    /**
     * Execute a task on the event dispatch thread, returning a position,
     * throwing any BadLocationException that occurs.
     * @param r an object whose doitBLE() method provides the code to execute
     *        and that will store its return value in r.position
     * @return the position
     * @exception BadLocationException a BadLocationException occurred
     */
    protected Position doitPositionBLE(RunnableWithPositionBLE r) 
	throws BadLocationException {
	try {
	    SwingUtilities.invokeAndWait(r);
	    if (r.exception != null) throw r.exception;
	    if (r.rexception != null) throw r.rexception;
	    return r.position;
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
     * This method allows an application to mark a place in
     * a sequence of character content. This mark can then be
     * used to tracks change as insertions and removals are made
     * in the content. The policy is that insertions always
     * occur prior to the current position (the most common case)
     * unless the insertion location is zero, in which case the
     * insertion is forced to a position that follows the
     * original position.
     *
     * @param offs  the offset from the start of the document &gt;= 0
     * @return the position
     * @exception BadLocationException  if the given position does not
     *   represent a valid location in the associated document
     */
    @Override
    public Position createPosition(int offs) throws BadLocationException {
	if (SwingUtilities.isEventDispatchThread()) {
	    return doc.createPosition(offs);
	} else {
	    final int xoffs = offs;
	    return doitPositionBLE(new RunnableWithPositionBLE() {
		    protected void doitBLE() throws
			BadLocationException {
			position = doc.createPosition(xoffs);
		    }
		});
	}
    }
    
    abstract class RunnableWithElements extends RunnableWithRE {
	Element[] elements;
    }

    /**
     * Execute a task on the event dispatch  thread, returning an array
     * of Element.
     * @param r an object whose doit() method provides the code to execute
     *        and that will store the element array it computes in r.elements
     * @return the elements computed
     */
    protected Element[] doitElements(RunnableWithElements r) {
	try {
	    SwingUtilities.invokeAndWait(r);
	    if (r.rexception != null) throw r.rexception;
	    return r.elements;
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
     * Returns all of the root elements that are defined.
     * <p>
     * Typically there will be only one document structure, but the interface
     * supports building an arbitrary number of structural projections over the
     * text data. The document can have multiple root elements to support
     * multiple document structures.  Some examples might be:
     * </p>
     * <ul>
     * <li>Text direction.
     * <li>Lexical token streams.
     * <li>Parse trees.
     * <li>Conversions to formats other than the native format.
     * <li>Modification specifications.
     * <li>Annotations.
     * </ul>
     *
     * @return the root element
     */
    @Override
    public Element[] getRootElements() {
	if (SwingUtilities.isEventDispatchThread()) {
	    return doc.getRootElements();
	} else {
	    return doitElements(new RunnableWithElements() {
		    protected void doit() {
			    elements = doc.getRootElements();
		    }
		});
	}
    }

    abstract class RunnableWithElement extends RunnableWithRE {
	Element element;
    }

    /**
     * Execute a task on the event dispatch  thread, returning a
     * Element.
     * @param r an object whose doit() method provides the code to execute
     *        and that will store the element it computes in r.element
     * @return the element computed
     */
    protected Element doitElement(RunnableWithElement r) {
	try {
	    SwingUtilities.invokeAndWait(r);
	    if (r.rexception != null) throw r.rexception;
	    return r.element;
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
     * Returns the root element that views should be based upon,
     * unless some other mechanism for assigning views to element
     * structures is provided.
     *
     * @return the root element
     */
    @Override
    public Element getDefaultRootElement() {
	if (SwingUtilities.isEventDispatchThread()) {
	    return doc.getDefaultRootElement();
	} else {
	    return doitElement(new RunnableWithElement() {
		    public void doit() {
			element = doc.getDefaultRootElement();
		    }
		});
	}
    }

    /**
     * Allows the model to be safely rendered in the presence
     * of concurrency, if the model supports being updated asynchronously.
     * The given runnable will be executed in a way that allows it
     * to safely read the model with no changes while the runnable
     * is being executed.  The runnable itself may <em>not</em>
     * make any mutations.
     *
     * @param runnable a <code>Runnable</code> used to render the model
     */
    @Override
    public void render(Runnable runnable) {
	if (SwingUtilities.isEventDispatchThread()) {
	    doc.render(runnable);
	    return;
	} else {
	    final Runnable x = runnable;
	    doitVoid(new RunnableWithRE() {
		    protected void doit() {
			doc.render(x);
		    }
		});
	}
    }

    /*
    static public void main(String argv[]) {
	Document doc = new PlainDocument();
	Document sdoc = new SafeDocument(doc);

	System.out.println(doc.getLength());
	try {
	    doc.insertString(0, "hello", null);
	    System.out.println("length of " +doc.getText(0, doc.getLength()) 
			       +" = "  +doc.getLength());
	} catch (BadLocationException e) {
	    e.printStackTrace();
	}
	try {
	    doc.insertString(100,"hello", null);
	    System.out.println("Exception not raised");
	} catch (BadLocationException e) {
	    System.out.println("caught expected BadLocationException");
	}
    }
    */
}

//  LocalWords:  SwingUtilities invokeLater Runnable AWT SafeDocument
//  LocalWords:  runtime invokeAndWait SafeStyledDocument doit href
//  LocalWords:  removeDocumentListener Unregisters undoable runnable
//  LocalWords:  addDocumentListener putProperty TitleProperty img ul
//  LocalWords:  StreamDescriptionProperty getProperty doitBLE src li
//  LocalWords:  BadLocationException DocumentEvent DocumentListeners
//  LocalWords:  removeUpdate createPosition UndoableEditEvent len
//  LocalWords:  insertUpdate DocumentListener str partialReturn txt
//  LocalWords:  pre nbsp nleft getDocumentLength setPartialReturn
//  LocalWords:  getText someting argv PlainDocument sdoc getLength
//  LocalWords:  insertString printStackTrace
