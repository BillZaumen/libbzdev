package org.bzdev.geom;
import org.bzdev.lang.UnexpectedExceptionError;
import org.bzdev.util.Cloner;
import java.awt.Color;

//@exbundle org.bzdev.geom.lpack.Geom

class SurfaceComponent implements Shape3D, SurfaceOps {

    static String errorMsg(String key, Object... args) {
	return GeomErrorMsg.errorMsg(key, args);
    }

    Surface3D surface;
    int tindex[];
    int cindex[];

    @Override
    public int size() {
	return tindex.length;
    }

    @Override
    public int getSegment(int i, double[] coords)
	throws IllegalArgumentException
    {
	return surface.getSegment(tindex[i], coords);
    }

    @Override
    public Color getSegmentColor(int i) {
	return surface.getSegmentColor(tindex[i]);
    }

    @Override
    public Object getSegmentTag(int i) {
	return surface.getSegmentTag(tindex[i]);
    }

    static int numbCoords(int type) {
	switch (type) {
	case SurfaceIterator.CUBIC_PATCH:
	    return 48;
	case SurfaceIterator.CUBIC_TRIANGLE:
	    return 30;
	case SurfaceIterator.CUBIC_VERTEX:
	    return 15;
	case SurfaceIterator.PLANAR_TRIANGLE:
	    return 9;
	default:
	    return 0;
	}
    }

    static int numbPoints(int type) {
	switch(type) {
	case SurfaceIterator.CUBIC_PATCH:
	    return 16;
	case SurfaceIterator.CUBIC_TRIANGLE:
	    return 10;
	case SurfaceIterator.CUBIC_VERTEX:
	    return 5;
	case SurfaceIterator.PLANAR_TRIANGLE:
	    return 3;
	default:
	    return 0;
	}
    }

    SurfaceComponent(Surface3D surface, int[] tindex, int[] cindex) {
	this.surface = surface;
	this.tindex = tindex;
	this.cindex = cindex;

	int length = tindex.length;
	double[] dcoords = (surface instanceof Surface3D.Double)?
	    ((Surface3D.Double)(surface)).coords: null;
	float[] fcoords = (surface instanceof Surface3D.Float)?
	    ((Surface3D.Float)(surface)).coords: null;

	if (dcoords != null) {
	    int offset = cindex[0];
	    bounds = new Rectangle3D.Double(dcoords[offset++],
					    dcoords[offset++],
					    dcoords[offset++],
					    0.0, 0.0, 0.0);
	    for (int i = 0; i < length; i++) {
		int n = numbPoints(surface.types[tindex[i]]);
		offset = cindex[i];
		for (int j = 0; j < n; j++) {
		    double x = dcoords[offset++];
		    double y = dcoords[offset++];
		    double z = dcoords[offset++];
		    bounds.add(x, y, z);
		}
	    }
	} else {
	    int offset = cindex[0];
	    bounds = new Rectangle3D.Double((double)fcoords[offset++],
					    (double)fcoords[offset++],
					    (double)fcoords[offset++],
					    0.0, 0.0, 0.0);
	    for (int i = 0; i < length; i++) {
		int n = numbPoints(surface.types[tindex[i]]);
		offset = cindex[i];
		for (int j = 0; j < n; j++) {
		    double x = fcoords[offset++];
		    double y = fcoords[offset++];
		    double z = fcoords[offset++];
		    bounds.add(x, y, z);
		}
	    }
	}
    }

    class Iterator1 implements SurfaceIterator {
	int index = 0;
	@Override
	public int currentSegment(double[] coords) {
	    int type = surface.types[tindex[index]];
	    double[] dcoords = (surface instanceof Surface3D.Double)?
		((Surface3D.Double)(surface)).coords: null;
	    float[] fcoords = (surface instanceof Surface3D.Float)?
		((Surface3D.Float)(surface)).coords: null;
	    int cind = cindex[index];
	    if (dcoords != null) {
		int limit = numbCoords(type);
		System.arraycopy(dcoords, cind, coords, 0, limit);
	    } else {
		int limit = numbCoords(type);
		for (int i = 0; i < limit; i++) {
		    coords[i] = (double) fcoords[cind+i];
		}
	    }
	    return type;
	}

    
	@Override
	public int currentSegment(float[] coords) {
	    int type = surface.types[tindex[index]];
	    double[] dcoords = (surface instanceof Surface3D.Double)?
		((Surface3D.Double)(surface)).coords: null;
	    float[] fcoords = (surface instanceof Surface3D.Float)?
		((Surface3D.Float)(surface)).coords: null;
	    int cind = cindex[index];
	    if (fcoords != null) {
		int limit = numbCoords(type);
		System.arraycopy(fcoords, cind, coords, 0, limit);
	    } else {
		int limit = numbCoords(type);
		for (int i = 0; i < limit; i++) {
		    coords[i] = (float) dcoords[cind+i];
		}
	    }
	    return type;
	}

	@Override
	public Object currentTag() {
	    return surface.tags[tindex[index]];
	}
	
	@Override
	public Color currentColor() {
	    return surface.colors[tindex[index]];
	}

	@Override
	public boolean isDone() {
	    return index >= tindex.length;
	}

	@Override
	public boolean isOriented() {
	    return surface.isOriented();
	}

	@Override
	public void next() {
	    if (index < tindex.length) index++;
	}
    }

    class Iterator2 implements SurfaceIterator {
	Transform3D tform = null;
	int index = 0;

	Iterator2(Transform3D tform) {this.tform = tform;}

	@Override
	public int currentSegment(double[] coords) {
	    int type = surface.types[tindex[index]];
	    double[] dcoords = (surface instanceof Surface3D.Double)?
		((Surface3D.Double)(surface)).coords: null;
	    float[] fcoords = (surface instanceof Surface3D.Float)?
		((Surface3D.Float)(surface)).coords: null;
	    int cind = cindex[index];
	    if (dcoords != null) {
		tform.transform(dcoords, cind, coords, 0, numbPoints(type));
	    } else {
		tform.transform(fcoords, cind, coords, 0, numbPoints(type));
	    }
	    return type;
	}

    
	@Override
	public int currentSegment(float[] coords) {
	    int type = surface.types[tindex[index]];
	    double[] dcoords = (surface instanceof Surface3D.Double)?
		((Surface3D.Double)(surface)).coords: null;
	    float[] fcoords = (surface instanceof Surface3D.Float)?
		((Surface3D.Float)(surface)).coords: null;
	    int cind = cindex[index];
	    if (fcoords != null) {
		tform.transform(fcoords, cind, coords, 0, numbPoints(type));
	    } else {
		tform.transform(dcoords, cind, coords, 0, numbPoints(type));
	    }
	    return type;
	}

	@Override
	public Object currentTag() {
	    return surface.tags[tindex[index]];
	}
	
	@Override
	public Color currentColor() {
	    return surface.colors[tindex[index]];
	}

	@Override
	public boolean isDone() {
	    return index >= tindex.length;
	}

	@Override
	public boolean isOriented() {
	    return surface.isOriented();
	}

	@Override
	public void next() {
	    if (index < tindex.length) index++;
	}
    }

    @Override
    public boolean isOriented() {return surface.isOriented();}

    Rectangle3D bounds;

    @Override
    public Rectangle3D getBounds() {
	try {
	    return Cloner.makeClone(bounds);
	} catch (CloneNotSupportedException e) {
	    throw new UnexpectedExceptionError(e);
	}
    }

    boolean isClosed = false;
    boolean boundaryComputed = false;
    Path3D boundaryPath = null;
    int[] boundarySegments = null;
    int[] boundaryEdgeNumbers = null;

    @Override
    public SurfaceIterator getSurfaceIterator(Transform3D tform) {
	    if (tform == null) {
		return new Iterator1();
	    } else {
		return new Iterator2(tform);
	    }
    }

    @Override
    public SurfaceIterator getSurfaceIterator(Transform3D tform, int level) {
	if (tform == null) {
	    if (level == 0) {
		return new Iterator1();
	    } else {
		return new SubdivisionIterator(new Iterator1(), level);
	    }
	} else if (tform instanceof AffineTransform3D) {
	    if (level == 0) {
		return new Iterator2(tform);
	    } else {
		return new SubdivisionIterator(new Iterator2(tform),
					       level);
	    }
	} else {
	    if (level == 0) {
		return new Iterator2(tform);
	    } else {
		return new SubdivisionIterator(new Iterator1(), tform,
					       level);
	    }
	}
    }

    @Override
    public Path3D getBoundary() {
	if (!boundaryComputed) {
	    Surface3D.Boundary boundary
		= new Surface3D.Boundary(getSurfaceIterator(null),
					 isOriented());
	    boundaryPath = boundary.getPath();
	    boundarySegments = boundary.getSegmentIndices();
	    boundaryEdgeNumbers = boundary.getEdgeNumbers();
	    boundaryComputed = true;
	}
	if (boundaryPath == null) return null;
	try {
	    return Cloner.makeClone(boundaryPath);
	} catch (CloneNotSupportedException e) {
	    throw new UnexpectedExceptionError(e);
	}
    }

    @Override
    public int[] getBoundarySegmentIndices() {
	if (!boundaryComputed) {
	    Surface3D.Boundary boundary
		= new Surface3D.Boundary(getSurfaceIterator(null),
					 isOriented());
	    boundaryPath = boundary.getPath();
	    boundarySegments = boundary.getSegmentIndices();
	    boundaryEdgeNumbers = boundary.getEdgeNumbers();
	    boundaryComputed = true;
	}
	if (boundarySegments == null) return null;
	return boundarySegments.clone();
    }

    @Override
    public int[] getBoundaryEdgeNumbers() {
	if (!boundaryComputed) {
	    Surface3D.Boundary boundary
		= new Surface3D.Boundary(getSurfaceIterator(null),
					 isOriented());
	    boundaryPath = boundary.getPath();
	    boundarySegments = boundary.getSegmentIndices();
	    boundaryEdgeNumbers = boundary.getEdgeNumbers();
	    boundaryComputed = true;
	}
	if (boundaryEdgeNumbers == null) return null;
	return boundaryEdgeNumbers.clone();
    }

    @Override
    public boolean isClosedManifold() {
	if (!boundaryComputed) {
	    Surface3D.Boundary boundary
		= new Surface3D.Boundary(getSurfaceIterator(null),
					 isOriented());
	    boundaryPath = boundary.getPath();
	    boundaryComputed = true;
	}
	if (boundaryPath == null) return false;
	return boundaryPath.isEmpty();
    }

    @Override
    public int numberOfComponents() {return 1;}

    @Override
    public Shape3D getComponent(int i) {
	if (i != 0) {
	    throw new IllegalArgumentException(errorMsg("segmentIndex", i));
	}
	return this;
    }

}

//  LocalWords:  exbundle
