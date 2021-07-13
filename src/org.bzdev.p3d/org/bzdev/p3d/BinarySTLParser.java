package org.bzdev.p3d;

import java.io.*;
import java.nio.*;
import java.nio.channels.FileChannel;
import java.awt.*;
import java.awt.image.*;
import javax.imageio.*;


/**
 * Parser for Binary STL files.
 */
public class BinarySTLParser {
    byte[] barray = new byte[50];
    ByteBuffer buffer;
    boolean ignoreAttributeByteCounts = false;
    InputStream is;

    /**
     * Determine whether the attribute byte count field should be ignored.
     * @param value true if the attribute byte count field should be ignored;
     *        false otherwise
     */
    public void setIgnoreAttributeByteCounts(boolean value) {
	ignoreAttributeByteCounts = value;
    }

    /**
     * Constructor.
     * @param is the input stream used to read an STL file
     */

    public BinarySTLParser(InputStream is) {
	if (! (is instanceof BufferedInputStream)) {
	    is = new BufferedInputStream(is);
	}
	buffer = ByteBuffer.wrap(barray);
	buffer.order(ByteOrder.LITTLE_ENDIAN);
	this.is = is;
    }
    
    /**
     * Add the contents of an STL file to a model.
     * If the model is configures to use object transformations,
     * (rotations and translation), those will be applied.
     * @param m3d the model
     */
    public void addToModel(Model3D m3d) throws IOException {
	is.skip(80);
	buffer.clear();
	is.read(barray, 0, 4);
	buffer.put(barray, 0, 4);
	buffer.flip();
	long cnt = buffer.getInt();
	if (cnt < 0) cnt = (1L << 32) - cnt;
	for (int i = 0; i < cnt; i++) {
	    buffer.clear();
	    is.read(barray);
	    buffer.put(barray);
	    buffer.flip();
	    float nx = buffer.getFloat();
	    float ny = buffer.getFloat();
	    float nz = buffer.getFloat();
	    float x1 = buffer.getFloat();
	    float y1 = buffer.getFloat();
	    float z1 = buffer.getFloat();
	    float x2 = buffer.getFloat();
	    float y2 = buffer.getFloat();
	    float z2 = buffer.getFloat();
	    float x3 = buffer.getFloat();
	    float y3 = buffer.getFloat();
	    float z3 = buffer.getFloat();
	    int acnt = buffer.getShort();
	    if (acnt < 0) acnt = (1 << 16) - acnt;
	    if (!ignoreAttributeByteCounts && acnt != 0) {
		is.skip(acnt);
	    }
	    m3d.addTriangle((double)x1, (double) y1, (double) z1,
			    (double)x2, (double) y2, (double) z2,
			    (double)x3, (double) y3, (double) z3);
	}
    }
}

//  LocalWords:  STL
