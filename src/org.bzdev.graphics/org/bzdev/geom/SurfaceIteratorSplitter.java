package org.bzdev.geom;
import java.awt.Color;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

//@exbundle org.bzdev.geom.lpack.Geom

/**
 * Split a surface iterator into multiple surface iterators,
 * each covering a portion of a surface.
 * Some algorithms (e.g., the computation of moments, centers of
 * mass, volumes, etc.) perform essentially the same computation
 * on a surface's segments, and the results of those computations
 * can be summed.  If each is summed on a different thread,
 * running time can be improved on a multiprocessor as long as
 * the processing time per segment is large compared to the time
 * used in managing multiple processes.
 * <P>
 * Segments are passed to subiterators when needed, so depending
 * on usage, the number of segments each subiterator processes
 * is not predictable. Similarly the segments each subiterator obtains
 * is not predictable: there is no attempt to keep nearby segments
 * together, for example.
 */
public class SurfaceIteratorSplitter {

    static String errorMsg(String key, Object... args) {
	return GeomErrorMsg.errorMsg(key, args);
    }

    static class Data {
	boolean isDone = false;
	int type;
	double[] coords;
	Color color;
	Object tag;
	static final double[] COORDS = new double[48];

	public Data(boolean done) {
	    isDone = done;
	    coords = isDone? COORDS: (new double[48]);
	}
    }

    private static int N=128;

    SurfaceIterator baseSit;
    boolean isOriented;

    Thread sitThread;

    /**
     * Interrupt the thread generating surface iterator values.
     */
    public void interrupt() {
	sitThread.interrupt();
    }

    BlockingQueue<Data> queue;

    SurfaceIterator[] childIterators;

    private Data getData() {
	try {
	    return queue.take();
	} catch (InterruptedException ei) {
	    return new Data(true);
	}
    }

    /**
     * Constructor.
     * Generally, n should be no more than 1 less than the
     * number of processors (this class creates one thread
     * for internal use) if the computation is to be sped
     * up.
     * @param n the number of subiterators to create.
     * @param sit the original surface iterator.
     */
    public SurfaceIteratorSplitter(int n, SurfaceIterator sit) {
	if (n < 1) throw new IllegalArgumentException(errorMsg("firstArgLT1"));
	if (sit == null) {
	    throw new IllegalArgumentException(errorMsg("nullArg", 2));
	}
	queue = new ArrayBlockingQueue<Data>(N*n);
	baseSit = sit;
	childIterators = new SurfaceIterator[n];
	isOriented = baseSit.isOriented();
	for (int i = 0; i < n; i++) {
	    childIterators[i] = new SurfaceIterator() {
		    Data data = null;
		    @Override
		    public Color currentColor() {
			if (data == null) data = getData();
			return data.color;
		    }

		    @Override
		    public int currentSegment(double[] coords) {
			int nc = 0;
			if (data == null) data = getData();
			switch(data.type) {
			case SurfaceIterator.PLANAR_TRIANGLE:
			    nc = 9;
			    break;
			case SurfaceIterator.CUBIC_TRIANGLE:
			    nc = 30;
			    break;
			case SurfaceIterator.CUBIC_PATCH:
			    nc = 48;
			    break;
			case SurfaceIterator.CUBIC_VERTEX:
			    nc = 15;
			    break;
			}
			System.arraycopy(data.coords, 0, coords, 0, nc);
			return data.type;
		    }

		    @Override
		    public int currentSegment(float[] coords) {
			int nc = 0;
			if (data == null) data = getData();
			switch(data.type) {
			case SurfaceIterator.PLANAR_TRIANGLE:
			    nc = 9;
			    break;
			case SurfaceIterator.CUBIC_TRIANGLE:
			    nc = 30;
			    break;
			case SurfaceIterator.CUBIC_PATCH:
			    nc = 48;
			    break;
			case SurfaceIterator.CUBIC_VERTEX:
			    nc = 15;
			    break;
			}
			for (int i = 0; i < nc; i++) {
			    coords[i] = (float)data.coords[i];
			}
			return data.type;
		    }

		    @Override
		    public Object currentTag() {
			if (data == null) data = getData();
			return data.tag;
		    }

		    @Override
		    public boolean isDone() {
			if (data == null) data = getData();
			return data.isDone;
		    }

		    @Override
		    public boolean isOriented() {return isOriented;}

		    @Override
		    public void next() {
			if (data == null) data = getData();
			if (data.isDone == false) {
			    try {
				data = queue.take();
			    } catch (InterruptedException ei) {
				data.isDone = true;
			    }
			}
		    }
		};
	}
	sitThread = new Thread(() -> {
		try {
		    while(!baseSit.isDone()) {
			Data data = new Data(false);
			data.type = baseSit.currentSegment(data.coords);
			data.color = baseSit.currentColor();
			data.tag = baseSit.currentTag();
			queue.put(data);
			baseSit.next();
		    }
		} catch (InterruptedException ei) {
		} finally {
		    try {
			for (int i = 0; i < n; i++) {
			    queue.put(new Data(true));
			}
		    }  catch (InterruptedException ei2) {}
		}
	});
	sitThread.start();
    }

    /**
     * Get the number of subiterators.
     */
    public int size() {
	return childIterators.length;
    }

    /**
     * Get the i<sup>th</sup> subiterator.

     * @param index the index for the subiterator (an integer in the
     *        range [0, n) where n is the number of subiterators)
     */
    public SurfaceIterator getSurfaceIterator(int index) {
	if (index < 0 || index >= childIterators.length) {
	    throw new IllegalArgumentException
		(errorMsg("subiterIndex", index, childIterators.length));
	}
	return childIterators[index];
    }
}

//  LocalWords:  th
