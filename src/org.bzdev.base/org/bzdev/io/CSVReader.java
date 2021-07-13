package org.bzdev.io;
import java.io.BufferedReader;
import java.io.Reader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.*;

/**
 * Reader for CSV (Comma Separated Values) input streams.
 * The CSV format is describe in
 * <A HREF="https://tools.ietf.org/html/rfc4180">RFC 4180</A>.
 * Essentially, it consists of a set a series of rows providing
 * a field for each of N columns, using a comma to separate
 * columns. A quoting convention allows a quoted field to contain
 * commas, new lines, or double quotes. All rows must have the same
 * number of columns, although the length of a field may be zero.
 * Unfortunately, there are a variety of implementations that are
 * not completely compatible with each other.  The classes
 * {@link CSVWriter} and {@link CSVReader} follows RFC 4180 but
 * provides options to aid in conversions between variants of this
 * format.  For example, RFC 4180, as written, assumes that each field
 * contains printable 7-bit ASCII characters (excluding, for example,
 * control characters). {@link CSVWriter} and {@link CSVReader} will
 * allow any character to appear in a field.  Options allow a choice
 * of line separators (the default is CRLF, the one used in RFC 4180).
 * <P>
 * The constructors' arguments indicate
 * <UL>
 *   <LI> the reader providing the input. This reader should be
 *        configured to use the character set appropriate for the
 *        input.
 *   <LI> whether or not the first line of the input is a header.
 *   <LI> optionally a delimiter used as a line separator when a
 *        row is split between multiple lines.  This delimiter is used
 *        to join adjacent lines to form a single row. If null the
 *        system's line separator is used.  If carriage returns and
 *        line feeds should be reproduced "as is" when lines are
 *        joined to construct rows, One should specify a delimiter
 *        that matches the end-of-line convention used in the input.
 * </UL>
 * <P>
 * For the simplest use of this class, one will first call a constructor.
 * If the constructor indicates that the first line is a header, the
 * method {@link CSVReader#getHeaders()} will return an array containing
 * the headers in column order.  Calling {@link CSVReader#nextRow()} will
 * similarly return the fields of the next row in the input, finally
 * returning null when the end of the input is reached. For example,
 * <BLOCKQUOTE><CODE><PRE>
 *     Reader in = new FileReader("input.csv");
 *     CSVReader r = new CSVReader(in, true);
 *     String[] headers = r.getHeaders();
 *     String[] fields;
 *     while ((fields = r.nextRow()) != null) {
 *         ...
 *     }
 *     r.close();
 * </PRE></CODE></BLOCKQUOTE>
 * One may also use the {@link java.io.Reader} <code>read</code> methods
 * to read a field, followed by a call to {@link CSVReader#nextField()}
 * to go to the next field.
 * <P>
 * To specify a character encoding, the {@link java.io.Reader} passed
 * as the first argument of a constructor should be an instance of
 * {@link java.io.InputStreamReader} or a {@link java.io.Reader} that
 * contains an instance of {@link java.io.InputStreamReader}, possibly
 * with several intermediate readers.
 */
public class CSVReader extends Reader {
    BufferedReader in;
    boolean hasHeader = false;
    String[] header;

    int index = 0;
    String[] fields;

    static final String pattern =
	"\"|,|[^\",]+";
    static final Pattern p = Pattern.compile(pattern);

    static final Pattern even = Pattern.compile("(([^\"])|(\"[^\"]*\"))*");

    static final String EMPTY_STRING = "";

    ArrayList<String> list = new ArrayList<>();

    static final String DEFAULT_DELIMITER = System.lineSeparator();

    LineReader.Delimiter delimiter = null;

    /**
     * Get the delimiter.
     * The specified delimiter is used to join lines when a row is split into
     * multiple lines.
     * @return the delimiter; null for the system-defined line separator
     *         ("\n" on Unix system, "\r\n" on Microsoft Windows, and
     *          "\r" on the original MacOS systems).
     */
    public LineReader.Delimiter getDelimiter() {return delimiter;}


    private String[] parseLine(String line) {
	list.clear();
	if (line == null) return null;
	int start = 0;
	int end = line.length();
	int last = 0;
	boolean skipping = false;
	Matcher m = p.matcher(line);
	String field = "";
	boolean qmode = false;
	for (;;) {
	    if (m.find()) {
		start = m.start();
		int tend = m.end();
		char ch = line.charAt(start);
		switch (ch) {
		case ',':
		    if (!qmode) {
			if (start > 0 && line.charAt(start-1) == '"') {
			    start = start-1;
			}
			if (line.charAt(last) == '"') last++;
			String token = line.substring(last,start);
			token = token.replaceAll("\"\"", "\"");
			list.add(token);
			last = tend;
		    }
		    break;
		case '"':
		    if (tend < end) {
			if (qmode) {
			    if (line.charAt(tend) == '"') {
				m.find();
				tend = m.end();
			    } else {
				qmode = false;
			    }
			} else {
			    qmode = true;
			}
		    } else {
			break;
		    }
		    break;
		}
	    } else {
		break;
	    }
	}
	if (end > 0) {
	    if (line.charAt(end-1) == '"') {
		end--;
	    }
	    int length = line.length();
	    String token;
	    if (last == length) {
		token = EMPTY_STRING;
	    } else if (line.charAt(last) == '"') {
		last++;
		if (last == length) {
		    token = EMPTY_STRING;
		} else {
		    token = line.substring(last,end);
		    token = token.replaceAll("\"\"", "\"");
		}
	    } else {
		token = line.substring(last,end);
	    }
	    list.add(token);
	} else {
	    String token = EMPTY_STRING;
	    list.add(token);
	}
	String[] result = new String[list.size()];
	result = list.toArray(result);
	list.clear();
	return result;
    }

    private String getLine() throws IOException {
	String line = in.readLine();
	if (line == null) return null;
	String tmp = "";
	for (;;) {
	    tmp = tmp + line;
	    if (tmp.contains("\"")) {
		Matcher m = even.matcher(tmp);
		int end = 0;
		if (m.find()) {
		    end = m.end();
		}
		if (tmp.substring(end).contains("\"")) {
		    line = in.readLine();
		    if (line == null) {
			throw new IOException();
		    }
		    if (delimiter == null) {
			tmp = tmp + DEFAULT_DELIMITER;
		    } else {
			switch(delimiter) {
			case LF:
			    tmp = tmp + "\n";
			    break;
			case CR:
			    tmp = tmp + "\r";
			    break;
			case CRLF:
			    tmp = tmp + "\r\n";
			    break;
			}
		    }
		} else {
		    return tmp;
		}
	    } else {
		return tmp;
	    }
	}
    }

    /**
     * Constructor.
     * When the first row is classified as a header, it is skipped but
     * can be retrieved by calling {@link #getHeaders()}.
     * @param in the input
     * @param hasHeader true if the first line (or row) is a header;
     *                  false otherwise
     * @exception IOException an IO Exception was thrown
     */
    public CSVReader(Reader in, boolean hasHeader)
	throws IOException
    {
	this.in = (in instanceof BufferedReader)?
	    (BufferedReader) in: new BufferedReader(in);
	this.hasHeader = hasHeader;
	if (hasHeader) {
	    header = parseLine(getLine());
	}
	fields = parseLine(getLine());
    }
	
    /**
     * Constructor specifying a delimiter.
     * When the row is classified as a header, it is skipped but can
     * be retrieved by calling {@link #getHeaders()}.  If a field
     * (which must be a quoted one in this case) is split between two
     * lines, the specified delimiter will be inserted when the lines
     * are joined to create a single row.
     * @param in the input
     * @param hasHeader true if the first line (or row) is a header;
     *                  false otherwise
     * @param delimiter the delimiter ({@link LineReader.Delimiter#LF}
     *        for a new line, {@link LineReader.Delimiter#CR} for a
     *        carriage return, or {@link LineReader.Delimiter#CRLF}
     *        for a carriage return followed by a new line; null for
     *        the system-defined line separator ("\n" on Unix system,
     *        "\r\n" on Microsoft Windows, and "\r" on the original
     *        MacOS systems)
     * @exception IOException an IO Exception was thrown
     */
    public CSVReader(Reader in, boolean hasHeader,
		     LineReader.Delimiter delimiter)
	throws IOException
    {
	this.in = (in instanceof BufferedReader)? (BufferedReader) in:
	    ((delimiter == null)? new BufferedReader(in):
	     new LineReader(in, delimiter));

	this.hasHeader = hasHeader;
	this.delimiter = delimiter;
	if (hasHeader) {
	    header = parseLine(getLine());
	}
	fields = parseLine(getLine());
    }

    /**
     * Get the headers.
     * @return the headers; null if the constructor indicates that
     *         the first row does not contain headers
     */
    public String[] getHeaders() {
	return (header == null)? null: header.clone();
    }

    /**
     * Get the fields that make up the next row.
     * @return an array containing the fields in column order
     * @exception IOException an IO Exception was thrown
     */
    public String[] nextRow() throws IOException {
	synchronized(lock) {
	    if (index > 0) {
		index = 0;
		fields = parseLine(getLine());
	    }
	    String[] result = fields;
	    fields = parseLine(getLine());
	    return result;
	}
    }

    @Override
    public void close() throws IOException {
	synchronized(lock) {
	    in.close();
	    in = null;
	}
    }

    int offset = 0;

    /**
     * {@inheritDoc}
     * <P>
     * This method always returns true because
     * {@link #read(char[],int,int)} returns -1 when the end of
     * a field is reached. To obtain more characters to read,
     * {@link #nextField()} should be called. That method may
     * block until the next row is read.
     */
    @Override
    public boolean ready() throws IOException {
	return true;
    }

    /**
     * {@inheritDoc}
     * <P>
     * This method returns -1 when the end of a field is reached.
     * To read more data, one should call {@link #nextField()},
     * which may block when the next row has to be read.
     */
    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
	synchronized(lock) {
	    if (fields == null) {
		return -1;
	    }
	    int flen = fields[index].length() - offset;
	    if (flen == 0) {
		return -1;
	    }
	    if (cbuf.length <= off) {
		return 0;
	    }
	    if (off + len > cbuf.length) {
		len = cbuf.length - off;
	    }
	    if (len > flen) len = flen;
	    int end = offset + len;
	    fields[index].getChars(offset, end, cbuf, off);
	    offset = end;
	    return len;
	}
    }

    /**
     * Get the next field.
     * @return true if the next field has been read; false
     *         if there are no more fields to read
     * @exception IOException an IO Exception was thrown
     */
    public boolean nextField() throws IOException {
	synchronized(lock) {
	    index++;
	    offset = 0;
	    if (index == fields.length) {
		index = 0;
		String line = getLine();
		if (line == null) {
		    fields = null;
		    return false;
		}
		fields = parseLine(line);
	    }
	    return true;
	}
    }
}


//  LocalWords:  CSV HREF CSVWriter CSVReader CRLF getHeaders nextRow
//  LocalWords:  BLOCKQUOTE PRE FileReader csv nextField MacOS LF
//  LocalWords:  hasHeader IOException LineReader
