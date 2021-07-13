package org.bzdev.io;

import java.io.InputStream;
import java.io.IOException;

//@exbundle org.bzdev.io.lpack.IO

/**
 * An input stream with a byte count.
 * This class provides an implementation of InputStream with
 * a byte counter giving the current position in the input stream.
 * One use for this class is for software instrumentation during
 * development (e.g., to empirically determine buffer sizes).
 */
public class CounterInputStream extends InputStream {
    InputStream is;
    long count = 0;
    long mark = 0;
    int readlimit = -1;
    boolean markvalid = false;
    boolean markSupported;

    static String errorMsg(String key, Object... args) {
	return IoErrorMsg.errorMsg(key, args);
    }

    /**
     * Constructor.
     * @param is the input stream to count.
     */
    public CounterInputStream(InputStream is) {
	this.is = is;
	markSupported = is.markSupported();
    }
    
    public int available() throws IOException {
	return is.available();
    }
    public void close()  throws IOException {
	is.close();
    }

    public void mark(int readlimit) {
	is.mark(readlimit);
	if (markSupported()) {
	    if (readlimit > 0) {
		this.readlimit = readlimit;
		markvalid = true;
		markSupported = true;
		mark = count;
	    } else {
		readlimit = -1;
		markvalid = false;
	    }
	} else {
	    markSupported = false;
	}
    }

    public boolean markSupported() {
	markSupported = is.markSupported();
	return markSupported;
    }
    
    public int read() throws IOException {
	int val = is.read();
	if (val >= 0) {
	    count++;
	}
	return val;
    }

    public int read(byte[] b) throws IOException {
	int val = is.read(b);
	if (val >= 0) {
	    count += val;
	}
	return val;
    }

    public int read (byte[] b, int off, int len) throws IOException {
	int val = is.read(b, off, len);
	if (val >= 0) {
	    count += val;
	}
	return val;
    }

    /**
     * Repositions this stream to the position at the time the
     * <code>mark</code> method was last called on this input stream.
     * <p> The general contract of <code>reset</code> is:
     * <p>
     * <ul>
     * <li> If the method <code>markSupported</code> returns
     * <code>true</code>, then:
     *     <ul>
     *     <li> If the method <code>mark</code> has not been called since
     *     the stream was created, or the number of bytes read from the stream
     *     since <code>mark</code> was last called is larger than the argument
     *     to <code>mark</code> at that last call, then an
     *     <code>IOException</code> will be thrown.
     *
     *     <li> If such an <code>IOException</code> is not thrown, then the
     *     stream is reset to a state such that all the bytes read since the
     *     most recent call to <code>mark</code> (or since the start of the
     *     file, if <code>mark</code> has not been called) will be resupplied
     *     to subsequent callers of the <code>read</code> method, followed by
     *     any bytes that otherwise would have been the next input data as of
     *     the time of the call to <code>reset</code>.
     *     </ul>
     * <li> If the method <code>markSupported</code> returns
     * <code>false</code>, then:
     *
     *     <ul>
     *     <li> The call to <code>reset</code> will throw an
     *     <code>IOException</code>.
     *     <li> If an <code>IOException</code> is not thrown, then the stream
     *     is reset to a fixed state that depends on the particular type of the
     *     input stream and how it was created. The bytes that will be supplied
     *     to subsequent callers of the <code>read</code> method depend on the
     *     particular type of the input stream.
     *     </ul>
     * </ul>
     *
     * @exception IOException the read limit has been exceeded or if the mark 
     *            is not supported or set.
     */
    public void reset() throws IOException {
	markSupported = is.markSupported();
	if (markSupported) {
	    if ((count - mark) > readlimit) 
		throw new IOException(errorMsg("passedReadLimit"));
	    is.reset();
	    count = mark;
	} else {
	    throw new IOException(errorMsg("markNotSupported"));
	}
    }

    public long skip(long n) throws IOException {
	long val = is.skip(n);
	if (val >= 0) {
	    count += val;
	}
	return val;
    }

    /**
     * Get the current byte count.
     * This returns the number of bytes that have been read and skipped.
     * @return the number of bytes read so far.
     */
    public long getCount() {
	return count;
    }
}

//  LocalWords:  exbundle InputStream Repositions ul li markSupported
//  LocalWords:  IOException passedReadLimit markNotSupported
