package org.bzdev.net;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.nio.charset.Charset;

//@exbundle org.bzdev.net.lpack.Net

/**
 * PEM Decoder.
 * This decoder will decode PEM-encoded data and additionally will
 * provide the type of the data and any headers that may precede
 * the PEM-encoded data.
 * <P>
 * PEM encoding is defined in RFC 7468 and related RFCs.
 */
public class PemDecoder {

    private static final Charset utf8 = Charset.forName("UTF-8");

    /**
     * Result class for PEM decoding.
     */
    public static class Result {
	String type;
	byte[] data;
	HeaderOps headers = HeaderOps.newInstance();

	/**
	 * Get the type of the data.
	 * @return the type (e.g. PUBLIC KEY, CERTIFICATE, etc.)
	 */
	public String getType() {return type;}

	/**
	 * Get the object in binary form.
	 * @return the byte sequence representing an encoded object
	 */
	public byte[] getBytes(){return data;}

	/**
	 * Get any headers that appeared before the start of a PEM-encoded
	 * object.
	 * @return the headers
	 */
	public HeaderOps getHeaders() {return headers;}
    }

    private static byte[] bytearray = new byte[256];
    private static ByteBuffer bbuf = ByteBuffer.wrap(bytearray);

    // Skip to first line starting with "-----BEGIN " and position
    // the input stream so the final ' ' has been read but not the
    // following character
    private static void skipToLabel(InputStream is, Result result)
	throws IOException
    {
	int ch = is.read();
	boolean startedLine = true;
	boolean store = true;
	
	for (;;) {
	    if (ch == '\n' || ch == '\r') {
		int blen = bbuf.position();
		if (blen != 0) {
		    bbuf.flip();
		    String line = new String(bytearray, 0, blen, "UTF-8");
		    String[] fields = line.split(":", 2);
		    if (fields.length == 2) {
			result.headers.set(fields[0].trim(), fields[1].trim());
		    }
		}
		startedLine = true;
		ch = is.read();
		bbuf.clear();
		continue;
	    }
	    if (startedLine && ch == '-') {
		startedLine = false;
		store = false;
		ch = is.read();
		if (ch == '-') {
		    ch = is.read();
		} else {
		    ch = is.read();
		    continue;
		}
		if (ch == '-') {
		    ch = is.read();
		} else {
		    ch = is.read();
		    continue;
		}
		if (ch == '-') {
		    ch = is.read();
		} else {
		    ch = is.read();
		    continue;
		}
		if (ch == '-') {
		    ch = is.read();
		} else {
		    ch = is.read();
		    continue;
		}
		if (ch == 'B') {
		    ch = is.read();
		} else {
		    ch = is.read();
		    continue;
		}
		if (ch == 'E') {
		    ch = is.read();
		} else {
		    ch = is.read();
		    continue;
		}
		if (ch == 'G') {
		    ch = is.read();
		} else {
		    ch = is.read();
		    continue;
		}
		if (ch == 'I') {
		    ch = is.read();
		} else {
		    ch = is.read();
		    continue;
		}
		if (ch == 'N') {
		    ch = is.read();
		} else {
		    ch = is.read();
		    continue;
		}
		if (ch == ' ') {
		    return;
		} else {
		    ch = is.read();
		    continue;
		}
	    } else {
		if (ch == -1) return;
		bbuf.put((byte)(ch & 0xFF));
		ch = is.read();
		startedLine = false;
		store = true;
	    }
	}
    }

    /**
     * Decode headers and PEM-encoded data from a string.
     * @param string a string containing optional headers and
     *        PEM-encoded data 
     * @return a {@link Result} object storing the decoded data
     * @throws IOException if an IO error occurred
     */
    public static Result decode(String string) throws IOException {
	InputStream is = new ByteArrayInputStream(string.getBytes(utf8));
	return decode(is);
    }

    /**
     * Decode headers and PEM-encoded data from an input stream.
     * @param is an  input stream containing optional headers and
     *        PEM-encoded data
     * @return a {@link Result} object storing the decoded data
     * @throws IOException if an IO error occurred
     */
    public static Result decode(InputStream is) throws IOException {
	Base64.Decoder decoder = Base64.getMimeDecoder();
	
	Result result = new Result();

	byte[] buffer = new byte[64];
	skipToLabel(is, result);
	int ch = is.read();
	if (ch == -1) return null;
	StringBuilder sb = new StringBuilder();
	sb.append((char)ch);
	while ((ch = is.read()) != -1 && ch != '-' && ch != '\n'
	       && ch != '\r') {
	    sb.append((char)ch);
	}
	if (ch == '-') {
	    result.type = sb.toString().trim().replaceAll("[ \t][ \t]+", " ");
	} else {
	    String msg = NetErrorMsg.errorMsg("typeTermination");
	    throw new IOException(msg);
	}
	ch = is.read();
	for (int i = 0; i < 4; i++) {
	    if (ch == '-') {
		ch = is.read();
	    } else {
		String msg = NetErrorMsg.errorMsg("typeTermination5");
		throw new IOException(msg);
	    }
	}
	if (ch == '-') {
	    String msg = NetErrorMsg.errorMsg("typeTermination5");
	    throw new IOException(msg);
	}
	while (ch == ' ' || ch == '\t') ch = is.read();
	while (ch == '\n' || ch == '\r') ch = is.read();
	// now at start of the base64 encoded lines
	sb = new StringBuilder();
	while (ch != '-' && ch != -1) {
	    sb.append((char) ch);
	    ch = is.read();
	}
	result.data = decoder.decode(sb.toString().trim());

	for(int i = 0; i < 5; i++) {
	    if (ch != '-') {
		String msg = NetErrorMsg.errorMsg("PemTermination5");
		throw new IOException("ch = " + ch + ", i = " + i);
	    }
	    ch = is.read();
	}
	if (ch != 'E') {
	    String msg = NetErrorMsg.errorMsg("PemTermination5");
	    throw new IOException(msg);
	}
	if ((ch = is.read()) != 'N') {
	    String msg = NetErrorMsg.errorMsg("PemTermination5");
	    throw new IOException(msg);
	}
	if ((ch = is.read()) != 'D') {
	    String msg = NetErrorMsg.errorMsg("PemTermination5");
	    throw new IOException(msg);
	}
	if (ch == -1) throw new IOException();
	if ((ch = is.read()) != ' ' && ch != '\t') {
	    String msg = NetErrorMsg.errorMsg("PemTermination5");
	    throw new IOException(msg);
	}
	while (ch == ' ' || ch == '\t') {
	    ch = is.read();
	}
	if (ch == -1) {
	    String msg = NetErrorMsg.errorMsg("unterminatedEnd");
	    throw new IOException();
	}
	if (ch == '\n') {
	    throw new IOException();
	}
	if (ch == '\r') {
	    throw new IOException();
	}
        sb = new StringBuilder();
	sb.append((char)ch);
	while ((ch = is.read()) != -1 && ch != '-' && ch != '\n'
	       && ch != '\r') {
	    sb.append((char)ch);
	}
	if (ch == '-') {
	    String type = sb.toString().trim().replaceAll("[ \t][ \t]+", " ");
	    if (!result.type.equals(type)) {
		String msg = NetErrorMsg.errorMsg("typeMismatch");
		throw new IOException(msg);
	    }
	} else {
	    String msg = NetErrorMsg.errorMsg("unterminatedEnd");
	    throw new IOException(msg);
	}
	for(int i = 0; i < 5; i++) {
	    if (ch != '-') {
		String msg = NetErrorMsg.errorMsg("unterminatedEnd");
		throw new IOException(msg);
	    }
	    ch = is.read();
	}
	while (ch == ' ' || ch == '\t') ch = is.read();
	if (ch != '\r' && ch != '\n') {
	    String msg = NetErrorMsg.errorMsg("missingEOL");
	    throw new IOException(msg);
	}
	return result;
    }
}

//  LocalWords:  PEM UTF IOException exbundle typeTermination
//  LocalWords:  PemTermination unterminatedEnd typeMismatch
//  LocalWords:  missingEOL
