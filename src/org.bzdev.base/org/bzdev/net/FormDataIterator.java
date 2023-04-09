package org.bzdev.net;
import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

//@exbundle org.bzdev.net.lpack.Net

/**
 * Iterator to read input streams that provide data using the
 * multipart/form-data media type.
 * <P>
 * The embedded HTTP server handles POST requests by providing an
 * input stream containing some data.  HTML forms in particular will
 * generate such a stream using the multipart/form-data media type.
 * When implementing a subclass of {@link org.bzdev.ejws.WebMap},
 * given an instance of {@link org.bzdev.ejws.WebMap.RequestInfo},
 * <CODE>requestInfo</CODE>, one will typically use the following
 * design pattern:
 * <BLOCKQUOTE><PRE><CODE>
 *     InputStream is = requestInfo.getDecodedInputStream();
 *     String mediatype = requestInfo.getFromHeader("content-type", null);
 *     if (mediatype.equalsIgnoreCase("multipart/form-data")) {
 *         String boundary =
 *              requestHeader.getFromHeader("content-type", "boundary");
 *         FormDataInterator it = new FormDataIterator(is, boundary);
 *         while (it.hasNext()) {
 *            InputStream cis = it.next();
 *            ...
 *            cis.close();
 *         }
 *     }
 * </CODE></PRE></BLOCKQUOTE>
 * If the content-type header's value <CODE>CTYPE</CODE>, and the input stream
 * <CODE>is</CODE> containing the form's content, are available, then
 * the following design pattern can be used:
 * <BLOCKQUOTE><PRE><CODE>
 *     HeaderOps headerOps = HeaderOps.newInstance();
 *     headerOps.set("content-type", CTYPE);
 *     String boundary = headerOps.getFirst("content-type", false)
 *            .get("boundary");
 *     FormDataIterator it = new FormDataIterator(is, boundary);
 *     while (it.hasNext()) {
 *       InputStream cis = it.next();
 *       ...
 *       it.close();
 *     }
 * </CODE></PRE></BLOCKQUOTE>
 * <P>
 * Notes:
 * <UL>
 *   <LI> There is an HTML convention that forms with an element whose
 *     name is "_charset_" contains the default charset for files whose
 *     media type is text/plain.  To support this convention, if a
 *     the content type is text/plain and a charset parameter is not
 *     provided, the default charset is returned by
 *     {@link FormDataIterator#getCharset()}.  The default charset can
 *     be set by calling {@link FormDataIterator#setDefaultCharset(Charset)}.
 *     to follow this convention, an entry whose name is "_charset_"
 *     should have its stream read and then used to set the default
 *     charset.
 *   <LI> If multiple files are being transferred for a single form field,
 *     the latest standard,
 *     <A href="https://tools.ietf.org/html/rfc7578#section-4.3">(RFC 7578)</A>,
 *     requires that all the files use entities with the same name,
 *     and the 'filename' parameter can be used to distinguish them.
 *     Older software may provide a nested multipart-mixed part.  To
 *     handle this deprecated usage, the user can simply process the
 *     input stream for this form field as an object whose type is
 *     multipart/mixed.
 *   <LI> While this class implements the {@link java.util.Iterator}
 *     interface, there is a restriction on its use: after the method
 *     {@link FormDataIterator next()} has been called, the stream returned
 *     must either be read to its end or closed before
 *     {@link FormDataIterator hasNext()} is called. Otherwise an
 *     {@link java.lang.IllegalStateException IllegalStateException}
 *     will be thrown.
 * </UL>
 */
public class FormDataIterator implements Iterator<InputStream> {

    static String errorMsg(String key, Object... args) {
	return NetErrorMsg.errorMsg(key, args);
    }

    static final Charset UTF8 = Charset.forName("UTF-8");

    Charset defaultCharset = UTF8;
    
    /**
     * Set the default character set for this object.
     * @param charset the character set; null for the UTF-8 default
     */
    public void setDefaultCharset(Charset charset) {
	if (charset == null) defaultCharset = UTF8;
	defaultCharset = charset;
    }
    
    static final byte[] CONTENT_DISPOSITION =
	"content-disposition".getBytes(UTF8);
    static final byte[] FORM_DATA = "form-data".getBytes(UTF8);
    static final byte[] NAME = "name".getBytes(UTF8);
    static final byte[] FILENAME = "filename".getBytes(UTF8);
    static final byte[] CONTENT_TYPE = "content-type".getBytes(UTF8);

    InputStream is;
    byte[] initialBoundary;
    byte[] boundary;
    static byte[] internalEnd = {'\r', '\n'};
    static byte[] finalEnd = {'-', '-', '\r', '\n'};
    static final int DEFAULT_BSIZE = 512;
    static int BSIZE = 1024;	     // never changed after constructor
    byte[] buffer = new byte[BSIZE]; // circular buffer
    long rposition = 0;		     // next to read when rposition < wposition
    long wposition = 0;
    long end = -1;		// -1 is undetermined
    long testPositionStart = -1;
    long nextHeaderPosition = -1;
    boolean atHeaders = false;
    boolean scannedHeaders = false;
    HeaderOps headers = null;
    boolean more = true;

    private void fillBuffer() throws IOException {
	if (end != -1) return;
	int incr = BSIZE - (int)(wposition - rposition);
	if (incr == 0) return;
	int tailLen = BSIZE - ((int)(wposition % BSIZE));
	if (incr >= tailLen) {
	    int n = 0;
	    while (n < tailLen && end == -1) {
		int start = (int)(wposition % BSIZE);
		int nn = is.read(buffer, start, tailLen-n);
		if (nn == -1) {
		    end = wposition;
		    return;
		} else {
		    wposition += nn;
		    n += nn;
		}
	    }
	    incr -= tailLen;
	}
	int m = 0;
	while (m < incr && end == -1) {
	    int start = (int)(wposition % BSIZE);
	    int mm = is.read(buffer, start, incr-m);
	    if (mm == -1) {
		end = wposition;
		return;
	    } else {
		wposition += mm;
		m += mm;
	    }
	}
    }
    Charset USASCII = Charset.forName("US-ASCII");
    byte[] tmpbuf = new byte[BSIZE];

    // Will not read the terminating 'CRLF' for an empty line.
    // Returns null for an empty line.
    private String readLine() throws IOException {
	long pos = rposition;
	fillBuffer();
	String value = null;
	int count = 0;
	while (pos < wposition) {
	    if (buffer[(int)(pos%BSIZE)] == '\r') {
		pos++;
		if (buffer[(int)(pos%BSIZE)] != '\n') {
		    throw new IOException(errorMsg("lineTermination"));
		}
		pos++;
		if (count == 0) return null;
		value = new String(tmpbuf, 0, count, USASCII);
		rposition = pos;
		return value;
	    }
	    tmpbuf[count++] = buffer[(int)(pos%BSIZE)];
	    pos++;
	    if ((wposition - pos) > BSIZE/2) {
		fillBuffer();
	    }
	}
	throw new IOException(errorMsg("readLineFailed"));
    }

    private void augmentHeaders(String header) {
	header = header.trim();
	int n = 0;
	int hlen = header.length();
	int locs[] = new int[hlen+1];
	int p = 0;
	boolean quoting = false;
	boolean prevNotBackslash = true;
	for (int i = 0; i < hlen; i++) {
	    char ch = header.charAt(i);
	    switch(ch) {
	    case ':':
		if (n == 0) {
		    locs[n++] = p;
		}
		prevNotBackslash = true;
		break;
	    case ',':
		if (!quoting) {
		    locs[n++] = p;
		}
		prevNotBackslash = true;
		break;
	    case '\"':
		if (prevNotBackslash) {
		    quoting = !quoting;
		}
		prevNotBackslash = true;
		break;
	    case '\\':
		if (quoting && prevNotBackslash) {
		    // We won't process a quote after this one
		    prevNotBackslash = false;
		} else {
		    prevNotBackslash = true;
		}
		break;
	    default:
		prevNotBackslash = true;
		break;
	    }
	    p++;
	}
	locs[n++] = header.length();
	String key = header.substring(0, locs[0]).trim().toLowerCase();
	if (n == 2) {
	    int start = locs[0]+1;
	    int end = locs[1];
	    String value = header.substring(start, end).trim();
	    headers.set(key, value);
	} else {
	    for (int i = 1; i < n-1; i++) {
		int start = locs[i]+1;
		int end = locs[i+1];
		if (start < end) {
		    String value = header.substring(start, end).trim();
		    headers.add(key, value);
		}
	    }
	}
    }

    private boolean scanForBoundary() throws IOException {
	long pos = Math.max(rposition, testPositionStart);
	while (pos+1 < wposition) {
	    if (buffer[(int)(pos%BSIZE)] == '\r'
		&& buffer[(int)((pos+1)%BSIZE)] == '\n') {
		break;
	    }
	    pos++;
	}
	if (pos < wposition) {
	    testPositionStart = pos;
	}
	if (wposition - pos >= boundary.length + 4) {
	    // there are enough remaining bytes in the buffer
	    // to find a boundary.
	    for (int i = 0; i < boundary.length; i++) {
		long bpos = i + testPositionStart;
		if (buffer[(int)(bpos%BSIZE)] != boundary[i]) {
		    testPositionStart = bpos;
		    pos = testPositionStart;
		    while (pos+1 < wposition) {
			if (buffer[(int)(pos%BSIZE)] == '\r'
			    && buffer[(int)((pos+1)%BSIZE)] == '\n') {
			    break;
			}
			pos++;
		    }
		    if (pos < wposition) {
			testPositionStart = pos;
		    }
		    if (wposition - bpos > boundary.length + 4) {
			i = -1;
			continue;
		    } else {
			return false;
		    }
		}
	    }

	    endStream = testPositionStart;
	    endBoundary = testPositionStart + boundary.length;
	    if (buffer[(int)(endBoundary%BSIZE)] == '-'
		&& buffer[(int)((endBoundary + 1) % BSIZE)] == '-'
		&& buffer[(int)((endBoundary + 2) % BSIZE)] == '\r'
		&& buffer[(int)((endBoundary + 3) % BSIZE)] == '\n') {
		// found the end of the file
		testPositionStart = endBoundary + 4;
		end = testPositionStart;
		nextHeaderPosition = end;
		more = false;
	    } else if (buffer[(int)(endBoundary%BSIZE)] == '\r'
		       && buffer[(int)((endBoundary + 1) % BSIZE)] == '\n') {
		nextHeaderPosition = endBoundary + 2;
		testPositionStart = nextHeaderPosition;
	    }
	    return true;
	}
	if (end != -1 && (end - testPositionStart) < boundary.length+4) {
	    // cannot reach end so the input stream must be too short.
	    throw new IOException(errorMsg("badTermination"));
	}
	return false;
    }

    private void readHeaders() throws IOException {
	// headers = new Headers();
	headers = HeaderOps.newInstance();
	StringBuilder sb = new StringBuilder();
	String line;
	while ((line = readLine()) != null) {
	    char start = line.charAt(0);
	    if (start == ' ' || start == '\t') {
		sb.append(line);
	    } else {
		if (sb.length() > 0) {
		    augmentHeaders(sb.toString());
		}
		sb.setLength(0);
		sb.append(line);
	    }
	}
	if (sb.length() > 0) {
	    augmentHeaders(sb.toString());
	}
	fillBuffer();
	if (buffer[(int)(rposition % BSIZE)] == '\r' &&
	    buffer[(int)((rposition+1) % BSIZE)] == '\n') {
	    rposition += 2;
	}
	scanForBoundary();
    }

    private void skipToHeaders() throws IOException {
	if (endBoundary < rposition) {
	    while (scanForBoundary() == false) {
		rposition = testPositionStart;
		fillBuffer();
	    }
	}
	rposition = endBoundary;
	if (end >= 0 && endBoundary == end) {
	    more = false;
	    nextHeaderPosition = end;
	} else {
	    if (buffer[(int)((rposition)%BSIZE)] == '\r'
		&& buffer[(int)((rposition+1)%BSIZE)] == '\n') {
		rposition += 2;
		nextHeaderPosition = rposition;
	    } else if (buffer[(int)((rposition)%BSIZE)] == '-'
		       && buffer[(int)((rposition+1)%BSIZE)] == '-'
		       && buffer[(int)((rposition+2)%BSIZE)] == '\r'
		       && buffer[(int)((rposition+3)%BSIZE)] == '\n') {
		// occurs if we did not read the data first.
		more = false;
		rposition += 4;
		end = rposition;
		nextHeaderPosition = end;
	    }
	}	       
    }

    /**
     * Determine if their are more entries to process.
     * <P>
     * Note: if the {@link java.io.InputStream InputStream} returned
     * by {@link #next()} has not been read fully and if such a stream
     * has not been explicitly closed, an exception will be thrown
     * @return true if there are more entries; false otherwise
     * @exception IllegalStateException this method cannot be called
     *            when the input stream returned by next() has not
     *            been read completely or explicitly closed.
     */
    @Override
    public boolean hasNext() throws IllegalStateException {
	if (streamOpened) {
	    throw new IllegalStateException(errorMsg("hasNext"));
	}
	return more;
    }

    /**
     * Get the next entry.
     * Note: if the {@link java.io.InputStream InputStream} returned
     * by this method has not been read fully and if such a stream
     * has not been explicitly closed, an exception will be thrown when
     * {@link #hasNext()} is called.
     * @return an input stream containing the data for the next
     *         entry
     */
    @Override
    public InputStream next() {
	try {
	    fillBuffer();
	    if (more) {
		if (nextHeaderPosition != rposition) {
		    skipToHeaders();
		}
		if (nextHeaderPosition != -1 &&
		    nextHeaderPosition != end) {
		    readHeaders();
		    return new FDInputStream();
		} else {
		    headers = null;
		    return null;
		}
	    } else {
		headers = null;
		return null;
	    }
	} catch (IOException e) {
	    NoSuchElementException nse =
		new NoSuchElementException(errorMsg("formData"));
	    nse.initCause(e);
	    throw nse;
	}
    }

    /**
     * Get the headers provided for the current entry.
     * These will typically be a "content-disposition" header and an
     * optional content-type header.
     * @return the headers
     */
    public HeaderOps getHeaders() {
	return headers;
    }

    /**
     * Get the name for the current entry.
     * This will be the same as the name of a control in an HTML form
     * when HTML forms are used.  The name is provided in a
     * "content-disposition" header.
     * @return the name; null if there is no header or the name is missing
     */
    public String getName() {
	Map<String,String> map =
	    headers.parseFirst("content-disposition", false);
	if (map == null) return null;
	return map.get("name");
    }

    /**
     * Get the file name for the current entry.
     * When provided, this will typically be the name of a file on a
     * file system accessed by whatever process initiated a POST method.
     * The file name is provided in a "content-disposition" header.
     * @return the name; null if there is no header or the name is missing
     */
    public String getFileName() {
	Map<String,String> map = headers.parseFirst("content-disposition",
						    false);
	if (map == null) return null;
	return map.get("filename");
    }

    /**
     * Get the value for the content-type header for the current entry.
     * @return the full content-type header; null if there is not one
     */
    public String getContentType() {
	if (headers == null) return null;
	return headers.getFirst("content-type");
    }

    /**
     * Get the media type, excluding its parameters, for the current entry
     * @return the media type; null if a media type was not provided
     */
    public String getMediaType() {
	Map<String,String> map = headers.parseFirst("content-type", false);
	if (map == null) return null;
	return map.get("content-type");
    }


    /**
     * Get the charset for the current entry
     * @return the charset provided by a "content-type" header; a
     *     default charset if there is no content-type header
     */
    public Charset getCharset() {
	Map<String,String> map = headers.parseFirst("content-type", false);
	if (map == null) return UTF8;
	String charset =  map.get("charset");
	if (charset == null) {
	    String mt = map.get("content-type");
	    if (mt != null && mt.equalsIgnoreCase("text/plain")) {
		return defaultCharset;
	    } else {
		return UTF8;
	    }
	}
	return Charset.forName(charset);
    }

    /**
     * Constructor.
     * @param is an input stream containing the multipart/form-data object
     * @param boundary the boundary, typically obtained by reading
     *        the 'boundary' parameter of a multipart/form-data media type
     * @exception IOException an IO error occurred
     */
    public FormDataIterator(InputStream is, String boundary)
	throws IOException
    {
	int boundaryLength = boundary.length();
	int boundaryLength2 = boundaryLength*2;
	BSIZE = DEFAULT_BSIZE;
	while (BSIZE < boundaryLength2) {
	    BSIZE *= 2;
	}
	this.is = is;
	this.boundary = new byte[4 + boundaryLength];
	initialBoundary = new byte[4 + boundaryLength];
	int i = 0;
	int ii = 0;
	int j = 0;
	this.initialBoundary[ii++] = '-';
	this.initialBoundary[ii++] = '-';
	this.boundary[i++] = '\r';
	this.boundary[i++] = '\n';
	this.boundary[i++] = '-';
	this.boundary[i++] = '-';
	while (j < boundaryLength) {
	    byte b = (byte)(boundary.charAt(j++));
	    this.boundary[i++] = b;
	    initialBoundary[ii++] = b;
	}
	initialBoundary[ii++] = '\r';
	initialBoundary[ii++] = '\n';

	fillBuffer();
	i = 0;
	while (rposition < wposition && i < initialBoundary.length) {
	    if (initialBoundary[i++] != buffer[((int)(rposition++) % BSIZE)]) {
		throw new IOException(errorMsg("wrongBoundary"));
	    }
	}
	if (i != initialBoundary.length) {
	    throw new IOException(errorMsg("wrongBoundaryLength"));
	}
	fillBuffer();
	boolean atHeaders =
	    (rposition < wposition)
	    && (buffer[(int)(rposition % BSIZE)] != '\r');
	if (!atHeaders) {
	    // if there are no headers, we should have an empty form
	    i = 0;
	    while (rposition < wposition && i < boundaryLength) {
		if (this.boundary[i] != buffer[(int)((rposition++) % BSIZE)]) {
		    // We were expecting an empty form
		    throw new IOException(errorMsg("noHeaders"));
		}
	    }
	    if (buffer[(int)((rposition++) % BSIZE)] != '-'
		|| buffer[(int)((rposition++) % BSIZE)] != '-'
		|| buffer[(int)((rposition++) % BSIZE)] != '\r'
		|| buffer[(int)((rposition++) % BSIZE)] != '\n') {
		throw new IOException(errorMsg("noHeadersTerm"));
	    }
	    more = false;
	} else {
	    endBoundary = rposition;
	    nextHeaderPosition = rposition;
	}
    }

    long endStream = -1;
    long endBoundary = -1;
    boolean streamOpened = false;


    private class FDInputStream extends InputStream {
	boolean needBoundary;
	protected FDInputStream() throws IOException  {
	    fillBuffer();
	    needBoundary = (nextHeaderPosition < rposition);
	    streamOpened = true;
	}

       @Override
	public void close() throws IOException {
	   if (streamOpened) {
	       // This will read to the end of the current stram
	       // and set streamOpened to false.  By reading to
	       // the end at this point, the caller does not have
	       // to explicitly read the full stream, although reading
	       // it anyway is harmless.
	       while (read() != -1);
	   }

	    if (endBoundary < rposition) {
		if (needBoundary) {
		    while (scanForBoundary() == false) {
			rposition = testPositionStart;
			fillBuffer();
		    }
		    needBoundary = false;
		}
	    }
	    if (rposition < endStream) {
		rposition = endStream;
	    }
	    // streamOpened = false;
	}

       @Override
	public int read() throws IOException {
	   if (streamOpened == false) return -1;
	    if(needBoundary && scanForBoundary()) {
		needBoundary = false;
	    }
	    if (rposition == endStream) {
		streamOpened = false;
		return -1;
	    }
	    byte b = buffer[(int)(rposition % BSIZE)];
	    try {
		return ((int) b) & 0xFF;
	    } finally {
		rposition++;
		if (rposition >= wposition - boundary.length - 4) {
		    // need to fill boundary if we are close to the point
		    // where scanForBoundary would not detect the end.
		    fillBuffer();
		}
	    }
	}
    }
}

//  LocalWords:  exbundle WebMap RequestInfo requestInfo BLOCKQUOTE
//  LocalWords:  PRE InputStream getDecodedInputStream mediatype cis
//  LocalWords:  getFromHeader equalsIgnoreCase requestHeader hasNext
//  LocalWords:  FormDataInterator FormDataIterator charset href UTF
//  LocalWords:  getCharset setDefaultCharset IllegalStateException
//  LocalWords:  rposition wposition CRLF lineTermination formData
//  LocalWords:  readLineFailed badTermination wrongboundary
//  LocalWords:  wrongboundaryLength noHeaders noHeadersTerm
//  LocalWords:  scanForBoundary
