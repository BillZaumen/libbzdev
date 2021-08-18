package org.bzdev.io;
import java.io.Writer;
import java.io.IOException;

//@exbundle org.bzdev.io.lpack.IO

/**
 * Writer for CSV (Comma Separated Values) output streams.
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
 *<P>
 * The constructors provide the number of columns (i.e., the
 * number of fields per row). One can then create a CSV file
 * by calling {@link CSVWriter#writeField(String)} repeatedly,
 * adding fields for columns 0 to (n-1) before proceeding to the
 * next row..  If a row is not complete,
 * one can call {@link CSVWriter#nextRow()} to pad the row with
 * empty fields. The method {@link CSVWriter#nextRowIfNeeded()}
 * can be used to finish a row, but will not add a row that
 * has only empty fields.
 * <P>
 * Alternatively, one can use the {@link Writer} methods
 * {@link Writer#write(char[])}, {@link Writer#write(char[],int,int)},
 * {@link Writer#write(int)}, {@link Writer#write(String)} or
 * {@link Writer#write(String,int,int)} to store the character sequence
 * making up a field, followed by {@link CSVWriter#nextField()} to
 * create the field, and clear the input for the next field.
 * <P> For example,
 * <BLOCKQUOTE><CODE><PRE>
 *   int ncols = 5;
 *   Writer out = new PrintWriter("output.csv", "US-ASCII");
 *   CSVWriter w = new CSVWriter(out, ncols);
 *   w.writeRow("col1", "col2", "col3", "col4", "col5");
 *   w.writeRow(...)
 *   ...
 *   w.close();
 * </PRE></CODE></BLOCKQUOTE>
 *  Alternatively, one can write the rows as follows:
 * <BLOCKQUOTE><CODE><PRE>
 *   int nrows = 20;
 *   int ncols = 5;
 *   String data[][] = {
 *     {"col1", "col2", "col3", "col4", "col5"},
 *     ... // 20 rows of data
 *   }
 *   Writer out = new PrintWriter("output.csv", "US-ASCII");
 *   CSVWriter w = new CSVWriter(out, ncols);
 *   for (int i = 0, i &lt; nrows; i++) {
 *      for (int j = 0; j &lt; ncols; j++) {
 *          w.writeField(data[i][j]);
 *      }
 *   }
 * </PRE></CODE></BLOCKQUOTE>
 * The call to {@link CSVWriter#writeField(String)} can be replaced
 * with a series of calls to the <code>write</code> methods defined by
 * the {@link java.io.Writer} class, followed by a call to
 * {@link CSVWriter#nextField()}.
 * <P>
 * To specify a character encoding, the {@link java.io.Writer} passed
 * as the first argument of a constructor should be an instance of
 * {@link java.io.OutputStreamWriter} or a {@link java.io.Writer} that
 * contains an instance of {@link java.io.OutputStreamWriter}, possibly
 * with several intermediate writers.
 */
public class CSVWriter extends Writer {

    static String errorMsg(String key, Object... args) {
	return IoErrorMsg.errorMsg(key, args);
    }

    Writer out;
    int n;
    boolean alwaysQuote = false;
    StringBuffer field = new StringBuffer();
    
    int index = 0;

    /**
     * Get the current index.
     * The current index is a number in the range [0, n), where n is
     * the number of columns.
     * @return the current index
     */
    public int currentIndex() {
	return index;
    }

    private LineReader.Delimiter delimiter = LineReader.Delimiter.CRLF;

    // the end-of-line sequence defined for this system.
    static final String DEFAULT_DELIMITER = System.lineSeparator();
    
    /**
     * Get the delimiter.
     * Fields will be quoted only if quoting is necessary (i.e., if
     * the field contains a double quote, a comma, or a character that
     * could be part of a line separator).
     * The delimiter is used to terminate rows or lines
     * @return the delimiter
     */
    public LineReader.Delimiter getDelimiter() {return delimiter;}
    
    private static final char[] CRLF = {'\r', '\n'};
    private static final char[] CR = {'\r'};
    private static final char[] LF = {'\n'};

    private char[] DELIMITER = CRLF;
    private String DELIMITER_STRING = "\r\n";

    /**
     * Constructor.
     * @param out the output for this writer
     * @param n the number of columns
     */
    public CSVWriter(Writer out, int n)
    {
	this.out = out;
	this.n = n;
    }

    /**
     * Construct specifying if all fields are quoted.
     * Fields must be quoted if a field contains a comma,
     * a double quotation mark, a carriage return, or a new line
     * character.
     * @param out the output for this writer
     * @param n the number of columns
     * @param alwaysQuote true if each field should be quoted;
     *        false if fields should be quoted only when necessary
     */
    public CSVWriter(Writer out, int n, boolean alwaysQuote)
    {
	this(out, n);
	this.alwaysQuote = alwaysQuote;
    }


    /**
     * Construct specifying if all fields are quoted and specifying an
     * end-of-line (EOL) delimiter for terminating rows.
     * Fields will be quoted if a field contains a comma, a double
     * quotation mark, a carriage return, a new line character, or the
     * system-defined EOL sequence. If alwaysQuote is true, all fields
     * will be quoted.  The delimiter indicates the end-of-line
     * sequence that terminates each row. If a field contains the
     * system end-of-line sequence, that sequence will be replaced
     * with the delimiter.
     * @param out the output for this writer
     * @param n the number of columns
     * @param alwaysQuote true if each field should be quoted;
     *        false if fields should be quoted only when necessary
     * @param delimiter the end of line sequence used to terminate a row
     *        ({@link LineReader.Delimiter#LF}, {@link LineReader.Delimiter#CR},
     *        or {@link LineReader.Delimiter#CRLF}); null for the
     *        system default (or {@link LineReader.Delimiter#CRLF} if the
     *        the system default is not {@link LineReader.Delimiter#LF},
     *        {@link LineReader.Delimiter#CR},
     *        or {@link LineReader.Delimiter#CRLF})
     */
    public CSVWriter(Writer out, int n,
		     boolean alwaysQuote,
		     LineReader.Delimiter delimiter)
    {
	this(out, n);
	this.alwaysQuote = alwaysQuote;
	this.delimiter = delimiter;
	if (delimiter != null) {
	    switch(delimiter) {
	    case CR:
		DELIMITER = CR;
		DELIMITER_STRING = "\r";
		break;
	    case LF:
		DELIMITER = LF;
		DELIMITER_STRING = "\n";
		break;
	    default:
		DELIMITER = CRLF;
		DELIMITER_STRING = "\r\n";
		break;
	    }
	    if (DEFAULT_DELIMITER.equals(DELIMITER_STRING)) {
		// so we can do an '==' comparison for this case
		DELIMITER_STRING = DEFAULT_DELIMITER;
	    }
	} else {
	    if (DEFAULT_DELIMITER.equals("\r")) {
		DELIMITER = CR;
		DELIMITER_STRING = DEFAULT_DELIMITER;
	    } else if (DEFAULT_DELIMITER.equals("\n")) {
		DELIMITER = LF;
		DELIMITER_STRING = DEFAULT_DELIMITER;
	    } else if (DEFAULT_DELIMITER.equals("\r\n")) {
		DELIMITER = CRLF;
		DELIMITER_STRING = DEFAULT_DELIMITER;
	    }
	}
    }

    /**
     * {@inheritDoc}
     * <P>
     * If a field was partially created, {@link #nextField()} will be called
     * to create that field, and if a new row was not started, the
     * current row will be padded with empty fields and terminated with
     * an end-of-line sequence as specified by the delimiter.
     */
    @Override
    public void close() throws IOException {
	synchronized(lock) {
	    int len = field.length();
	    if (index == 0 && len == 0) {
		out.close();
	    } else {
		if (len != 0) {
		    nextField();
		}
		if (index != 0) {
		    nextRow( );
		}
		out.close();
	    }
	}
    }

    @Override
    public void flush() throws IOException {
	synchronized(lock) {
	    out.flush();
	}
    }

    @Override
    public void write(char[] cbuf, int off, int len) {
	synchronized(lock) {
	    field.append(cbuf, off, len);
	}
    }

    /**
     * Write a single field.
     * A call to {@link #nextField()} is implicit.
     * @param string the field
     */
    public void writeField(String string) throws IOException {
	synchronized(lock) {
	    if (field.length() > 0) {
		nextField();
	    }
	    field.append(string);
	    nextField();
	}
    }


    /**
     * Write a row.
     * If IO operations created the text for a field, but
     * {@link #nextField()} was not yet called, a call to
     * {@link #nextField()} will be inserted. If the number of fields
     * is less than the number of columns specified in a constructor,
     * the the line will be padded with empty fields.  A new line
     * sequence, as specified by the delimiter, will be added at the
     * end.
     * @param fields the fields to write
     * @exception IllegalArgumentException the number of fields
     *            is larger than the number of columns specified
     *            in the constructor
     * @exception IOException an IO error occurred
     */
    public void writeRow(String... fields)
	throws IOException, IllegalArgumentException
    {
	if (fields.length > n) {
	    throw new IllegalArgumentException(errorMsg("tooManyFields"));
	}
	synchronized(lock) {
	    if (field.length() > 0) {
		nextField();
	    }
	    nextRowIfNeeded();
	    for (int i = 0; i < fields.length; i++) {
		field.append(fields[i]);
		nextField();
	    }
	    nextRowIfNeeded();
	}
    }

    private String encode(String string) {
	if (string.contains("\r") || string.contains("\n")
	    || string.contains(DEFAULT_DELIMITER)
	    || string.contains("\"") || string.contains(",")) {
	    string = string.replaceAll("\"", "\"\"");
	    if (DEFAULT_DELIMITER != DELIMITER_STRING) {
		string = string.replaceAll(DEFAULT_DELIMITER, DELIMITER_STRING);
	    }
	    return "\"" + string + "\"";
	} else if (alwaysQuote) {
	    return "\"" + string + "\"";
	} else {
	    return string;
	}
    }


    /**
     * Start a new field.
     * If the field is the last field in a row, a new
     * row will be started automatically. 
     */
    public void nextField() throws IOException {
	synchronized(lock) {
	    if (index == n) {
		out.write(DELIMITER, 0, DELIMITER.length);
		index = 0;
	    }
	    if (index > 0) out.write(',');
	    out.write(encode(field.toString()));
	    field.setLength(0);
	    index++;
	    if (index == n) {
		out.write(DELIMITER, 0, DELIMITER.length);
		index = 0;
	    }
	}
    }

    /**
     * End the current row.
     * This method will pad the current row with empty fields
     * if necessary. If a field was partially written but
     * {@link #nextField()} was not called, an implicit call to
     * {@link #nextField()} will be added. If called at the
     * start of a row, a row with empty fields will be written.
     */
    public void nextRow() throws IOException {
	synchronized(lock) {
	    if (field.length() > 0) {
		nextField();
		if (index == 0) return;
	    }
	    while(index < n) {
		if (index > 0) out.write(',');
		index++;
	    }
	    if (index == n) {
		out.write(DELIMITER, 0, DELIMITER.length);
		index = 0;
	    }
	}
    }

    /**
     * Terminate the current row unless the current row is empty.
     * The row will be padded with empty fields if it is too short.
     */
    public void nextRowIfNeeded() throws IOException {
	if (field.length() > 0 || index != 0) nextRow();
    }

    /**
     * Get the media type for the output created by this CSV writer.
     * The character-set names are those supported by the IETF. A
     * list of
     * <A href="https://www.iana.org/assignments/character-sets/character-sets.xhtml">valid names</A>
     * is provided by IANA. Examples include "US-ASCII" and "UTF-8".
     * <P>
     * When hasHeader is true, the media type will contain a parameter
     * named "header" that indicates that the first row of the contents
     * represents a header - a set of fields with text describing or
     * labeling each column.
     * @param hasHeader true if the first line of the contents contains
     *        a header; false if it does not
     * @param charsetName the name of the character set used to encode
     *        an output stream; null if a charset parameter is not
     *        included in the media type
     * @return the media type
     */
    public static String getMediaType(boolean hasHeader, String charsetName) {
	if (hasHeader) {
	    if (charsetName == null) {
		return "text/csv; header";
	    } else {
		return "text/csv; header; charset=\"" + charsetName + "\"";
	    }
	} else {
	    if (charsetName == null) {
		return "text/csv";
	    } else {
		return "text/csv; charset=\"" + charsetName + "\"";
		
	    }
	}
    }
}

//  LocalWords:  CSV HREF CSVWriter CSVReader CRLF writeField nextRow
//  LocalWords:  nextRowIfNeeded nextField BLOCKQUOTE PRE ncols csv
//  LocalWords:  PrintWriter writeRow nrows lt alwaysQuote EOL LF UTF
//  LocalWords:  LineReader IllegalArgumentException IOException IETF
//  LocalWords:  href IANA hasHeader charsetName charset
