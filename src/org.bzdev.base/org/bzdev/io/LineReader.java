package org.bzdev.io;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Buffered reader with a choice of line terminations.
 * <P>
 * When a constructor specifies a delimiter, and that delimiter is not
 * null, the method {@link LineReader#readLine()} will assume that an
 * end of line is represented by either a line feed, a carriage
 * return, or a carriage return followed by a line feed, with the
 * delimiter specifying which of these combinations represents the end
 * of a line.  If the delimiter is null (the default), the behavior is
 * identical to that for {@link java.io.BufferedReader} and any of
 * these will indicate an end of line. When a line feed or carriage
 * return does not denote an end of line, that line feed or carriage
 * return will be kept as part of the line.
 * <P>
 * The behavior for {@link java.io.BufferedReader} is appropriate when
 * one is reading text files that may have been generated on systems
 * using differing, but common, end-of-line conventions.  {@link
 * LineReader} with an explicit delimiter choice is useful when
 * processing files that conform to standards that specify specific
 * line-termination sequences, and where characters that are not part
 * of that sequence should be kept as is.
 * <P>
 * To specify a character encoding, the {@link java.io.Reader} passed
 * as the first argument of a constructor should be an instance of
 * {@link java.io.InputStreamReader} or a {@link java.io.Reader} that
 * contains an instance of {@link java.io.InputStreamReader}, possibly
 * with several intermediate readers.
 */
public class LineReader extends  BufferedReader {

    /**
     * End-of-line Delimiter.
     * If the delimiter is set to null, all of these values are
     * recognized as indicating an end-of-line, which follows the
     * convention used by {@link java.io.BufferedReader}.
     */
    public enum Delimiter {
	/**
	 * A line is terminated by a carriage-return ("\r") followed by a 
	 * line feed ("\n").
	 */
	CRLF,
	/**
	 * A line is terminated by a carriage-return character ("\r").
	 * The ASCII (and UTF-8) code is 015 (Octal), 13 (Decimal), or
	 *  0x0D (Hexidecimal).
	 */
	CR,
	/**
	 * A line is terminated by a new-line character ("\n"). 
	 * The ASCII (and UTF-8) code is 012 (Octal), 10 (Decimal), or
	 *  0x0A (Hexidecimal).
	 */
	LF
    }

    Delimiter delimiter = null;

    char LF[] = {'\n'};
    char CR[] = {'\r'};
    char CRLF[] = {'\r', '\n'};

    char[] eol = null;


    /**
     * Constructor.
     * The default buffer size will be used.
     * @param in a Reader
     */
    public LineReader(Reader in) {
	super(in);
    }

    /**
     * Constructor providing a buffer size.
     * @param in a Reader
     * @param sz the input-buffer size
     */
    public LineReader(Reader in, int sz) {
	super(in, sz);
    }

    /**
     * Constructor specifying the end-of-line character or sequence of
     * characters.
     * The default buffer size will be used.
     * @param in a Reader
     * @param delimiter the choice of delimiter
     *        ({@link LineReader.Delimiter#LF},
     *        {@link LineReader.Delimiter#CR},
     *        {@link LineReader.Delimiter#CRLF}, or null for all of the
     *        these)
     */
    public LineReader(Reader in, Delimiter delimiter) {
	super(in);
	this.delimiter = delimiter;
	if (delimiter != null) {
	    switch(delimiter) {
	    case LF:
		eol = LF;
		break;
	    case CR:
		eol = CR;
		break;
	    case CRLF:
		eol = CRLF;
		break;
	    }
	}
    }

    /**
     * Constructor providing a buffer size and specifying the
     * end-of-line character or sequence of characters.
     * @param in a Reader
     * @param sz the input-buffer size
     * @param delimiter the choice of delimiter
     *        ({@link LineReader.Delimiter#LF},
     *        {@link LineReader.Delimiter#CR},
     *        {@link LineReader.Delimiter#CRLF}, or null for all of the
     *        these)
     */
    public LineReader(Reader in, int sz, Delimiter delimiter) {
	super(in, sz);
	this.delimiter = delimiter;
	if (delimiter != null) {
	    switch(delimiter) {
	    case LF:
		eol = LF;
		break;
	    case CR:
		eol = CR;
		break;
	    case CRLF:
		eol = CRLF;
		break;
	    }
	}
    }

    StringBuilder sb = new StringBuilder(128);

    /**
     * Read a line of text.
     * When the constructor provides a delimiter, that delimiter
     * is the one that will be used, and the other end-of-line characters
     * are treated as characters within the line.  If the delimiter is null (the
     * default), the behavior is identical to that for
     * {@link java.io.BufferedReader}.
     * @return a line of text; null if the end of the file has been reached
     * @exception IOException an IO error occurred
     */
    @Override
    public String readLine() throws IOException {
	if (delimiter == null) {
	    return super.readLine();
	}
	synchronized(lock) {
	    try {
		for(;;) {
		    int ch = read();
		    if (ch == -1) {
			if (sb.length() == 0) {
			    return null;
			} else {
			    return sb.toString();
			}
		    }
		    switch(delimiter) {
		    case LF:
			if (ch != '\n') {
			    sb.append((char) ch);
			} else {
			    return sb.toString();
			}
			break;
		    case CR:
			if (ch != '\r') {
			    sb.append((char) ch);
			} else {
			    return sb.toString();
			}
			break;
		    case CRLF:
			if (ch != '\r') {
			    sb.append((char) ch);
			} else {
			    int ch1 = ch;
			    while (ch1 == '\r') {
				ch1 = read();
				if (ch1 == '\n') {
				    return sb.toString();
				} else {
				    sb.append('\r');
				}
			    }
			    if (ch1 == -1) {
				return sb.toString();
			    } else {
				sb.append((char) ch1);
			    }
			}
		    }
		}
	    } finally {
		sb.setLength(0);
	    }
	}
    }
}

//  LocalWords:  LineReader readLine UTF Hexidecimal sz LF CRLF
