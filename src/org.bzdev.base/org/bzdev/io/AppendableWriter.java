package org.bzdev.io;
import java.io.Writer;
import java.io.IOException;
import java.nio.CharBuffer;

//@exbundle org.bzdev.io.lpack.IO

/**
 * Writer that writes to an Appendable.
 * This class allows a Writer to be used to add text to an Appendable.
 * For example <code>new PrintWriter(new AppendableWriter(appendable)</code>
 * creates a PrintWriter that can write formatted text to
 * <CODE>appendable</CODE> (whose class is, of course,
 * <CODE>Appendable</CODE>).
 * The implementation is thread-safe.
 */
public class AppendableWriter extends Writer {
    Appendable appendable;

    static String errorMsg(String key, Object... args) {
	return IoErrorMsg.errorMsg(key, args);
    }

    public AppendableWriter(Appendable appendable) {
	if (appendable == null)
	    throw new NullPointerException(errorMsg("nullArg"));
	this.appendable = appendable;
    }

    public Writer append(char c) throws IOException {
	synchronized(lock) {
	    if (appendable == null)
		throw new IOException(errorMsg("writerClosed"));
	    appendable.append(c);
	    return this;
	}
    }

    public Writer append(CharSequence csq) throws IOException {
	synchronized(lock) {
	    if (appendable == null) throw new IOException(errorMsg("writerClosed"));
	    appendable.append(csq);
	    return this;
	}
    }

    public Writer append(CharSequence csq, int start, int end) 
	throws IOException 
    {
	synchronized(lock) {
	    if (appendable == null) throw new IOException(errorMsg("writerClosed"));
	    appendable.append(csq, start, end);
	    return this;
	}
    }

    public void write (char[] cbuf, int off, int len) throws IOException {
	synchronized(lock) {
	    if (appendable == null) throw new IOException(errorMsg("writerClosed"));
	    CharBuffer cb = CharBuffer.wrap(cbuf, off, len);
	    appendable.append(cb);
	}
    }

    public void flush() throws IOException {
	synchronized(lock) {
	    if (appendable == null) throw new IOException(errorMsg("writerClosed"));
	}
    }

    public void close() throws IOException {
	synchronized(lock) {
	    if (appendable == null) throw new IOException(errorMsg("writerClosed"));
	    appendable = null;
	}
    }
}

//  LocalWords:  exbundle Appendable PrintWriter AppendableWriter
//  LocalWords:  appendable nullArg writerClosed
