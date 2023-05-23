package org.bzdev.net;

import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;
import java.nio.charset.Charset;

//@exbundle org.bzdev.net.lpack.Net

/**
 * PEM Encoder class.
 * This class will encode data using the PEM format described in
 * RFC 7468 and RFC 1421.  The PEM-encoded data may be preceded
 * or followed by headers that consist of a key, followed by a
 * colon, followed by a value.
 */
public class PemEncoder {

    private String linesep = "\r\n";
    private static final Charset utf8 = Charset.forName("UTF-8");
    private byte[] linesepBytes = linesep.getBytes(utf8);

    /**
     * End of line sequences.
     */
    public enum EOL {
	/**
	 * A line ends with a carriage return followed by a line feed.
	 * This is the default
	 */
	CRLF,
	/**
	 * A line ends with a carriage return.
	 */
	CR,
	/**
	 * A line ends with a a line feed.
	 */
	LF
    }

    private Appendable out;

    /**
     * Constructor.
     * When called directly, the EOL sequence will be CRLF.
     * @param out the {@link Appendable} used for output
     */
    public PemEncoder(Appendable out) {
	String msg = NetErrorMsg.errorMsg("nullArg");
	if (out == null) throw new NullPointerException(msg);
	this.out = out;
    }

    /**
     * Constructor specifying the end-of-line sequence.
     * @param out the {@link Appendable} used for output
     * @param eol {@link EOL#CRLF} if lines are terminated with a
     *        carriage return followed by a line feed;
     *        {@link EOL#CR} if lines are terminated with a
     *        carriage return;
     *        {@link EOL#LF}if lines are terminated with a
     *        line feed
     */
    public PemEncoder(Appendable out, EOL eol) {
	this(out);
	if (eol == null) return;
	switch(eol) {
	case CRLF:
	    break;
	case CR:
	    linesep = "\r";
	    break;
	case LF:
	    linesep = "\n";
	    break;
	}
	linesepBytes = linesep.getBytes(utf8);
    }

    /**
     * Add a header.
     * Names must consist of only letters, digits, underscores, or
     * a minus. Values must contain only a single line of text without
     * any new-line characters or any character denoting a vertical change
     * in position in the text.  In both cases, the characters should
     * be those in the US ASCII character set.
     * <P>
     * Note that RFC 7468 allows data to be inserted before the
     * encapsulated encoding.
     * @param key the name of the header
     * @param value the value of the header
     */
    public void addHeader(String key, String value)
	throws IOException
    {
	if (key.split("[^a-zA-Z0-9_-]", 2).length != 1) {
	    throw new IOException();
	}
	if (value.split("\\v", 2).length != 1) {
	    throw new IOException();
	}
	out.append(key);
	out.append(": ");
	out.append(value);
	out.append(linesep);
	if (out instanceof Flushable) {
	    ((Flushable) out).flush();
	}
    }

    /**
     * Encode data using PEM format.
     * @param type the type of the data
     * @param data the data to be encoded
     * @throws IOException if an IO error occurs
     * @throws NullPointerException if an argument is null
     * @throws IllegalArgumentException if the first argument contains
     *         only whitespace or if it contains a minus
     */
    public void encode(String type, byte[] data)
	throws IOException, IllegalArgumentException, NullPointerException
    {
	if (type == null || data == null) {
	    String msg = NetErrorMsg.errorMsg("nullArgs");
	    throw new NullPointerException(msg);
	}
	type = type.trim();
	if (type.length() == 0 || type.indexOf("-") != -1) {
	    String msg = NetErrorMsg.errorMsg("illegalType");
	    throw new IllegalArgumentException(msg);
	}
	out.append("-----BEGIN " + type + "-----" + linesep);
	Base64.Encoder encoder = Base64
	    .getMimeEncoder(64, linesepBytes);
	String encoded = new String(encoder.encode(data), utf8);
	out.append(encoded);
	if (!encoded.endsWith(linesep)) {
	    out.append(linesep);
	}
	out.append("-----END " + type + "-----" + linesep);
	if (out instanceof Flushable) {
	    ((Flushable) out).flush();
	}
    }
}

//  LocalWords:  PEM UTF EOL CRLF Appendable eol LF zA IOException
//  LocalWords:  NullPointerException IllegalArgumentException
//  LocalWords:  whitespace exbundle nullArg nullArgs illegalType
