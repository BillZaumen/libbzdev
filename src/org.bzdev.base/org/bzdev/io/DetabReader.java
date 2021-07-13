package org.bzdev.io;
import java.io.IOException;
import java.io.Reader;

//@exbundle org.bzdev.io.lpack.IO


/**
 * Reader to remove tabs.
 * The tab spacing is assumed to be 8 unless explicitly set in
 * a constructor.
 * <P>
 * Formats such as YAML that use indentation to denote nesting do
 * not allow tabs because the tab spacing cannot be predicted. While
 * 8 is a common number, some editors can be configured to use a different
 * value.
 */
public class DetabReader extends Reader {

    static String errorMsg(String key, Object... args) {
	return IoErrorMsg.errorMsg(key, args);
    }


    Reader r;
    int tabspacing = 8;
    int tsm1 = 7;

    int spacecount = 0;
    long col = -1;

    /**
     * Get the tab spacing.
     * @return the tab spacing
     */
    public int getTabSpacing() {return tabspacing;}

    /**
     * Constructor.
     * @param r a reader.
     */
    public DetabReader(Reader r) {
	this.r = r;
    }

    /**
     * Constructor with an explicit tab spacing.
     * @param r a reader.
     * @param tabspacing the tab spacing
     * @exception IllegalArgumentException if the tab spacing is less than 1
     */
    public DetabReader(Reader r, int tabspacing)
	throws IllegalArgumentException
    {
	this(r);
	if (tabspacing < 1) {
	    throw new IllegalArgumentException(errorMsg("arg2LessThanOne"));
	}
	this.tabspacing = tabspacing;
	tsm1 = tabspacing - 1;
    }

    @Override
    public int read() throws IOException {
	synchronized(lock) {
	    if (spacecount > 0) {
		spacecount--;
		col++;
		return ' ';
	    } else {
		int b = r.read();
		if (b == '\n') {
		    col = -1;
		} else if (b == '\t') {
		    col++;
		    int loc = (int) (col % tabspacing);
		    spacecount = tsm1 - loc;
		    return ' ';
		} else {
		    col++;
		}
		return b;
	    }
	}
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
	synchronized (lock) {
	    int lim = off+len;
	    int count = 0;
	    for (int i = off; i < lim; i++) {
		int b = r.read();
		if (b == -1) return (count == 0)? -1: count;
		cbuf[i] = (char)b;
		count++;
	    }
	    return count;
	}
    }

    @Override
    public void close() throws IOException {
	r.close();
    }


}

//  LocalWords:  YAML tabspacing IllegalArgumentException
