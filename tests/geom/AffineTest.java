import org.bzdev.geom.*;
import org.bzdev.math.VectorOps;

public class AffineTest {
    public static void main(String argv[]) throws Exception {
	AffineTransform3D af = new AffineTransform3D();

	if (!af.isIdentity()) {
	    System.out.println("new AffineTransform3d() does not create an"
			       + " identity transform");
	    System.out.println("... test failed");
	    System.exit(1);
	}

	double trX = 10.0;
	double trY = 20.0;
	double trZ = 30.0;

	double shx = 10.0;
	double shy = 20.0;
	double shz = 30.0;


	double phi = Math.toRadians(90.0);
	double theta = Math.toRadians(-90.0);
	double psi = Math.toRadians(90.0);

	AffineTransform3D aft =
	    AffineTransform3D.getTranslateInstance(trX, trY, trZ);

	af.setToTranslation(trX, trY, trZ);
	if (!af.equals(aft)) {
	    System.out.println("... af.setToTranslation failed");
	    System.exit(1);
	}

	Point3D pt0 = new Point3D.Double();
	Point3D pt1 = new Point3D.Double(trX, trY, trZ);

	Point3D ptt = aft.transform(pt0, null);
	if (!ptt.equals(pt1)) {
	    System.out.println("getTranslatIntance translation failed");
	    System.exit(1);
	}
	System.out.println("rotate test");
	AffineTransform3D afr =
	    AffineTransform3D.getRotateInstance(phi, theta, psi);

	af.setToRotation(phi, theta, psi);
	if (!af.equals(afr)) {
	    System.out.println("afr = " + afr);
	    System.out.println("af  = " + af);
	    System.out.println("... af.setToRotation [3 arguments] failed");
	    System.exit(1);
	}


	Point3D ptr = afr.transform(pt1, null);
	if (ptr.getX() != -30.0 && ptr.getY() != -20.0 && ptr.getZ() != -10.0) {
	    System.out.println("ptr X = " + ptr.getX()
			       + ", expected -30.0");
	    System.out.println("ptr Y = " + ptr.getY()
			       + ", expected -20.0");
	    System.out.println("ptr Z = " + ptr.getZ()
			       + ", expected -10.0");
	    System.out.println("... test failed");
	    System.exit(1);
	}
	if (afr.getTranslateX() != 0.0 || afr.getTranslateY() != 0.0
	    || afr.getTranslateZ() != 0.0) {
	    System.out.format("afr includes unexpected translation"
			      +" (%g, %g, %g)\n",
			      afr.getTranslateX(),
			      afr.getTranslateY(),
			      afr.getTranslateZ());
	    System.out.println("... test failed");
	    System.exit(1);
	}

	int type = afr.getType();
	if (type != AffineTransform3D.TYPE_QUADRANT_ROTATION) {
	    System.out.println("wrong type for afr: " + type);
	    System.out.println("afr = " + afr);
	    System.out.println("... test failed");
	    System.exit(1);
	}

	System.out.println("rotate with anchor test");
	Point3D pt2 = aft.transform(pt1, null);
	AffineTransform3D aftr =
	    AffineTransform3D.getRotateInstance(phi, theta, psi, trX, trY, trZ);
	if (aftr.getType() != (AffineTransform3D.TYPE_QUADRANT_ROTATION
			       | AffineTransform3D.TYPE_TRANSLATION)) {
	    System.out.println("wrong type for aftr: " + type);
	    System.out.println("... test failed");
	    System.exit(1);
	}
	af.setToRotation(phi, theta, psi, trX, trY, trZ);
	if (!af.equals(aftr)) {
	    System.out.println("... af.setToRotation [6 arguments] failed");
	    System.exit(1);
	}

	Point3D ptrt = aftr.transform(pt2, null);
	ptr = aft.inverseTransform(ptrt, null);
	if (ptr.getX() != -30.0 && ptr.getY() != -20.0 && ptr.getZ() != -10.0) {
	    System.out.println("ptr X = " + ptr.getX()
			       + ", expected -30.0");
	    System.out.println("ptr Y = " + ptr.getY()
			       + ", expected -20.0");
	    System.out.println("ptr Z = " + ptr.getZ()
			       + ", expected -10.0");
	    System.out.println("... test failed");
	    System.exit(1);
	}
	
	System.out.println("test concatenate");
	AffineTransform3D aft2 = 
	    AffineTransform3D.getTranslateInstance(aftr.getTranslateX(),
						   aftr.getTranslateY(),
						   aftr.getTranslateZ());
	if (aft2.getType() != (AffineTransform3D.TYPE_TRANSLATION)) {
	    System.out.println("wrong type for aftr: " + type);
	    System.out.println("... test failed");
	    System.exit(1);
	}
	AffineTransform3D oldAftr = aftr;
	aftr = new AffineTransform3D(aft2);
	if (!aftr.equals(aft2)) {
	    System.out.println("new transform based on aft failed");
	    System.exit(1);
	}

	AffineTransform3D aftone = new AffineTransform3D(0.9, 0.7, 0.3,
						       2.1, 0.8, 0.9,
						       0.1, 3.0, 0.7,
						       1.0, 2.0, 3.0);

	AffineTransform3D afttwo = new AffineTransform3D(0.8, 0.2, 1.3,
						     0.9, 0.5, 1.4,
						     3.0, 0.8, 0.6,
						     5.0, 8.0, 11.0);
	if (aftone.getDeterminant() == 0.0) {
	    System.out.println("aft1 is singular");
	    System.exit(1);
	}
	if (afttwo.getDeterminant() == 0.0) {
	    System.out.println("aft2 is singular");
	    System.exit(1);
	}


	Point3D p = new Point3D.Double(20.0, 15.0, 45.0);
	Point3D p1 = afttwo.transform(p, null);
	Point3D p2 = aftone.transform(p1, null);
	AffineTransform3D aft3 = new AffineTransform3D();
	aft3.concatenate(aftone);
	aft3.concatenate(afttwo);
	Point3D p3 = aft3.transform(p, null);
	if (Math.abs(p3.getX() - p2.getX()) > 1.e-10
	    || Math.abs(p3.getY() - p2.getY()) > 1.e-10
	    || Math.abs(p3.getZ() - p2.getZ()) > 1.e-10) {
	    System.out.println(" ... test with arbitrary matrices failed");
	}

	aftr.concatenate(afr);
	ptrt = aftr.transform(pt2, null);
	ptr = aft.inverseTransform(ptrt, null);
	if (ptr.getX() != -30.0 && ptr.getY() != -20.0 && ptr.getZ() != -10.0) {
	    System.out.println("ptr X = " + ptr.getX()
			       + ", expected -30.0");
	    System.out.println("ptr Y = " + ptr.getY()
			       + ", expected -20.0");
	    System.out.println("ptr Z = " + ptr.getZ()
			       + ", expected -10.0");
	    System.out.println("aftr = " + aftr.toString());
	    System.out.println("oldAftr = " + oldAftr.toString());

	    System.out.println("... test failed");
	    System.exit(1);
	}

	System.out.println("test preconcatenate");
	aftr = new AffineTransform3D(afr);
	aftr.preConcatenate(aft2);
	ptrt = aftr.transform(pt2, null);
	ptr = aft.inverseTransform(ptrt, null);
	if (ptr.getX() != -30.0 && ptr.getY() != -20.0 && ptr.getZ() != -10.0) {
	    System.out.println("ptr X = " + ptr.getX()
			       + ", expected -30.0");
	    System.out.println("ptr Y = " + ptr.getY()
			       + ", expected -20.0");
	    System.out.println("ptr Z = " + ptr.getZ()
			       + ", expected -10.0");
	    System.out.println("aftr = " + aftr.toString());
	    System.out.println("oldAftr = " + oldAftr.toString());
	    System.out.println("... test failed");
	    System.exit(1);
	}

	AffineTransform3D afs =
	    AffineTransform3D.getScaleInstance(shx, shy, shz);
	if (afs.getType() != (AffineTransform3D.TYPE_SCALE)) {
	    System.out.println("wrong type for afs: " + type);
	    System.out.println("... test failed");
	    System.exit(1);
	}
	af.setToScale(shx, shy, shz);
	if (!af.equals(afs)) {
	    System.out.println("... af.setToScale failed");
	    System.exit(1);
	}

	aft3 = new AffineTransform3D();
	aft3.preConcatenate(afttwo);
	aft3.preConcatenate(aftone);
	p3 = aft3.transform(p, null);
	if (Math.abs(p3.getX() - p2.getX()) > 1.e-10
	    || Math.abs(p3.getY() - p2.getY()) > 1.e-10
	    || Math.abs(p3.getZ() - p2.getZ()) > 1.e-10) {
	    System.out.println(" ... test with arbitrary matrices failed");
	}

	Point3D pts0 = new Point3D.Double(1.0, 1.0, 1.0);
	Point3D pts1 = afs.transform(pts0, null);
	System.out.println("scale test");
	if (!pts1.equals(pt1)) {
	    System.out.println("scaling AffineTransform failed");
	    System.out.println("ptrs1.x = " + pts1.getX());
	    System.out.println("ptrs1.y = " + pts1.getY());
	    System.out.println("ptrs1.z = " + pts1.getZ());
	    System.out.println("... test failed");
	    System.exit(1);
	}
	afs = AffineTransform3D.getScaleInstance(1.0, 1.0, -1.0);
	if (afs.getType() != (AffineTransform3D.TYPE_SCALE
			      | AffineTransform3D.TYPE_FLIP_CHIRALITY)) {
	    System.out.println("wrong type for afs: " + type);
	    System.out.println("... test failed");
	    System.exit(1);
	}

	System.out.println("shear test");

	double shxy = 2.0; // m01
	double shxz = 3.0; // m02
	double shyx = 4.0; // m10
	double shyz = 5.0; // m12
	double shzx = 6.0; // m20
	double shzy = 7.0; // m21

	AffineTransform3D afsh = AffineTransform3D.getShearInstance
	    (shxy, shxz, shyx, shyz, shzx, shzy);

	if (afsh.getType() != (AffineTransform3D.TYPE_GENERAL)) {
	    System.out.println("wrong type for afsh: " + type);
	    System.out.println("... test failed");
	    System.exit(1);
	}

	af.setToShear(shxy, shxz, shyx, shyz, shzx, shzy);
	if (!af.equals(afsh)) {
	    System.out.println("... af.setToScale failed");
	    System.exit(1);
	}

	if (shxy != afsh.getShearXY()
	    || shxz != afsh.getShearXZ()
	    || shyx != afsh.getShearYX()
	    || shyz != afsh.getShearYZ()
	    || shzx != afsh.getShearZX()
	    || shzy != afsh.getShearZY()) {
	    System.out.format("afsh.getShear%s() = %g, expected %g\n",
			      "XY", afsh.getShearXY(), shxy);
	    System.out.format("afsh.getShear%s() = %g, expected %g\n",
			      "XZ", afsh.getShearXZ(), shxz);
	    System.out.format("afsh.getShear%s() = %g, expected %g\n",
			      "YX", afsh.getShearYX(), shyx);
	    System.out.format("afsh.getShear%s() = %g, expected %g\n",
			      "YZ", afsh.getShearYZ(), shyz);
	    System.out.format("afsh.getShear%s() = %g, expected %g\n",
			      "ZX", afsh.getShearZX(), shzx);
	    System.out.format("afsh.getShear%s() = %g, expected %g\n",
			      "ZY", afsh.getShearZY(), shzy);
	    System.out.println("... getting shear values failed");
	    System.exit(1);
	}

	Point3D shptxy1 = new Point3D.Double(1.0, 1.0, 0.0);
	Point3D shptyz1 = new Point3D.Double(0.0, 1.0, 1.0);
	Point3D shptxz1 = new Point3D.Double(1.0, 0.0, 1.0);

	Point3D shptxy2 = afsh.transform(shptxy1, null);
	Point3D shptyz2 = afsh.transform(shptyz1, null);
	Point3D shptxz2 = afsh.transform(shptxz1, null);

	if ((Math.abs(shptxy2.getX() - 3.0) > 1.e-10)
	    || (Math.abs(shptxy2.getY() - 5.0) > 1.e-10)
	    || (Math.abs(shptxy2.getZ() -13.0) > 1.e-10)) {
	    System.out.println("shptxy2 = " + shptxy2);
	    System.out.println("... shearing failed");
	    System.exit(1);
	}
	
	if ((Math.abs(shptyz2.getX() - 5.0) > 1.e-10)
	    || (Math.abs(shptyz2.getY() - 6.0) > 1.e-10)
	    || (Math.abs(shptyz2.getZ() - 8.0) > 1.e-10)) {
	    System.out.println("shptyz2 = " +shptyz2);
	    System.out.println("... shearing failed");
	    System.exit(1);
	}

	if ((Math.abs(shptxz2.getX() - 4.0) > 1.e-10)
	    || (Math.abs(shptxz2.getY() - 9.0) > 1.e-10)
	    || (Math.abs(shptxz2.getZ() - 7.0) > 1.e-10)) {
	    System.out.println("shptxz2 = " + shptxz2);
	    System.out.println("... shearing failed");
	    System.exit(1);
	}
	AffineTransform3D copy = aftr.affineTransform(0.0, 0.0, 0.0);
	if (!(copy.equals(aftr))) {
	    System.out.println("... the method affineTransform() failed");
	    System.exit(1);
	}
	if (copy.getType() != aftr.getType()) {
	    System.out.println("... aftr and a copy of it have "
			       + "different types");
	    System.exit(1);
	}


	double[] dsrc = {1.0, 2.0, 3.0};
	double[] ddst = new double[3];

	af = AffineTransform3D.getTranslateInstance(100.0, 200.0, 300.0);
	af.shear(1.1, 1.2, 1.3, 2.1, 2.2, 2.3);
	af.scale(2.0, 3.0, 4.0);
	af.rotate(1.0, 1.3, 1.8);

	af.transform(dsrc, 0, ddst, 0, 1);
	af.transform(dsrc, 0, dsrc, 0, 1);
	if (dsrc[0] != ddst[0] || dsrc[1] != ddst[1] || dsrc[2] != ddst[2]) {
	    System.out.println("bad transform");
	    System.exit(1);
	}

	float[] fsrc = {1.0F, 2.0F, 3.0F};
	float[] fdst = new float[3];

	af.transform(fsrc, 0, fdst, 0, 1);
	af.transform(fsrc, 0, fsrc, 0, 1);
	if (fsrc[0] != fdst[0] || fsrc[1] != fdst[1] || fsrc[2] != fdst[2]) {
	    System.out.println("bad transform");
	    System.exit(1);
	}


	af = AffineTransform3D
	    .getRotateInstance(Math.PI/6, Math.PI/45, Math.PI/7,
			       35.0, 45.0, 55.0);
	double xvec1[] = {1.0, 0.0, 0.0};
	double yvec1[] = {0.0, 1.0, 0.0};
	double xvec2[] = new double[3];
	double yvec2[] = new double[3];
	af.deltaTransform(xvec1, 0, xvec2, 0, 1);
	af.deltaTransform(yvec1, 0, yvec2, 0, 1);
	double zvec2[] = VectorOps.crossProduct(xvec2, yvec2);

	System.out.format("xvec2 = (%g, %g, %g)\n",
			  xvec2[0], xvec2[1], xvec2[2]);
	System.out.format("yvec2 = (%g, %g, %g)\n",
			  yvec2[0], yvec2[1], yvec2[2]);

	Point3D fixedPoint = new Point3D.Double(35.0, 45.0, 55.0);
	AffineTransform3D af2 = AffineTransform3D
	    .getMapInstance(fixedPoint, xvec1, yvec1,
			    fixedPoint, xvec2, yvec2, 0.0);

	double [] matrix1 = new double[12];
	double [] matrix2 = new double[12];

	af.getMatrix(matrix1);
	af2.getMatrix(matrix2);
	System.out.println();
	for (int i = 0; i < 3; i++) {
	    for (int j = 0; j < 4; j++) {
		System.out.format("  %g", matrix1[i+3*j]);;
	    }
	    System.out.println();
	}
	System.out.println();
	for (int i = 0; i < 3; i++) {
	    for (int j = 0; j < 4; j++) {
		System.out.format("  %g", matrix2[i+3*j]);;
	    }
	    System.out.println();
	}
	System.out.println();
	for (int i = 0; i < 12; i++) {
	    if (Math.abs(matrix1[i] - matrix2[i]) > 1.e-10) {
		System.out.format("matrix1[%d] = %g, matrix2[%d] = %g\n",
				  i, matrix1[i], i, matrix2[i]);
		System.exit(1);
	    }
	}

	double v[] = {fixedPoint.getX(),
		       fixedPoint.getY(),
		       fixedPoint.getZ()};
	double va[] = new double[3];
	double vb[] = new double[3];
	System.arraycopy(v, 0, va, 0, 3);
	System.arraycopy(v, 0, vb, 0, 3);
	va[0] += 1.0;
	af2.transform(va, 0, va, 0, 1);
	for (int i = 0; i < 3; i++) {
	    vb[i] += xvec2[i];
	}
	for (int i = 0; i < 3; i++) {
	    if (Math.abs(va[i] - vb[i]) > 1.e-10) {
		throw new Exception();
	    }
	}
	System.arraycopy(v, 0, va, 0, 3);
	System.arraycopy(v, 0, vb, 0, 3);
	va[1] += 1.0;
	af2.transform(va, 0, va, 0, 1);
	for (int i = 0; i < 3; i++) {
	    vb[i] += yvec2[i];
	}
	for (int i = 0; i < 3; i++) {
	    if (Math.abs(va[i] - vb[i]) > 1.e-10) {
		throw new Exception();
	    }
	}

	System.arraycopy(v, 0, va, 0, 3);
	System.arraycopy(v, 0, vb, 0, 3);
	va[2] += 1.0;
	af2.transform(va, 0, va, 0, 1);
	for (int i = 0; i < 3; i++) {
	    vb[i] += zvec2[i];
	}
	for (int i = 0; i < 3; i++) {
	    if (Math.abs(va[i] - vb[i]) > 1.e-10) {
		throw new Exception();
	    }
	}

	double thetas[] = {0.0,
			   Math.PI/6,
			   Math.PI/4,
			   Math.PI/3,
			   Math.PI/2,
			   Math.PI/2 + Math.PI/6,
			   Math.PI/2 + Math.PI/4,
			   Math.PI/2 + Math.PI/3,
			   Math.PI,
			   -Math.PI,
			   -Math.PI/2,
			   3*(Math.PI/2),
			   (3*Math.PI)/2
	};
	double[] vc = new double[3];
	for (int k = 0; k < thetas.length; k++) {
	    AffineTransform3D af3 = AffineTransform3D
		.getMapInstance(fixedPoint, xvec1, yvec1,
				fixedPoint, xvec2, yvec2, thetas[k]);
	    System.arraycopy(v, 0, va, 0, 3);
	    System.arraycopy(v, 0, vb, 0, 3);
	    System.arraycopy(v, 0, vc, 0, 3);
	    va[0] += 1.0;
	    vb[1] += 1.0;
	    vc[2] += 1.0;
	    af3.transform(va, 0, va, 0, 1);
	    af3.transform(vb, 0, vb, 0, 1);
	    af3.transform(vc, 0, vc, 0, 1);
	    VectorOps.sub(va, va, v);
	    VectorOps.sub(vb, vb, v);
	    VectorOps.sub(vc, vc, v);
	    if (Math.abs(VectorOps.norm(va) - 1.0) > 1.e-10) {
		throw new Exception();
	    }
	    if (Math.abs(VectorOps.norm(vb) - 1.0) > 1.e-10) {
		throw new Exception();
	    }
	    if (Math.abs(VectorOps.norm(vc) - 1.0) > 1.e-10) {
		throw new Exception();
	    }
	    if (Math.abs(VectorOps.norm(VectorOps.crossProduct(vc, zvec2)))
		> 1.e-10) {
		throw new Exception();
	    }
	    if (Math.abs(VectorOps.dotProduct(va,vb)) > 1.e-10) {
		System.out.println("VectorOps.dotProduct(va,vb) = "
				   + VectorOps.dotProduct(va,vb)
				   + ", theta = " + thetas[k]);
		throw new Exception();
	    }
	    if (Math.abs(VectorOps.dotProduct(vb,vc)) > 1.e-10) {
		throw new Exception();
	    }
	    if (Math.abs(VectorOps.dotProduct(vc,va)) > 1.e-10) {
		throw new Exception();
	    }
	    double dx = VectorOps.dotProduct(va, xvec2);
	    double dy = VectorOps.dotProduct(va, yvec2);
	    if (Math.abs(dx*dx + dy*dy - 1) > 1.e-10) {
		throw new Exception();
	    }
	    if (Math.abs(thetas[k] + Math.PI) < 1.e-10) {
		if (Math.abs(Math.abs(Math.atan2(dy, dx))
			     - Math.abs(thetas[k])) > 1.e-10) {
		    System.out.format("dy = %g, dx = %g\n", dy, dx);
		    System.out.format("%s != %s\n",
				      Math.atan2(dy, dx),
				      thetas[k]);
		    throw new Exception();
		}
	    } else if (thetas[k] == Math.PI/2) {
		if (Math.abs(VectorOps.dotProduct(va, yvec2) - 1)  > 1.e-10) {
		    System.out.format("va = (%g, %g, %g), theta = %g\n",
				      va[0], va[1], va[2], thetas[k]);
		    throw new Exception();
		}
	    } else if (thetas[k] == -Math.PI/2 || thetas[k] == (3*Math.PI)/2
		       || thetas[k] == -3*(Math.PI/2)) {
		if (Math.abs(VectorOps.dotProduct(va, yvec2) + 1)  > 1.e-10) {
		    throw new Exception();
		}
	    } else if ((Math.atan2(dy, dx) - thetas[k]) > 1.e-10) {
		System.out.format("dy = %g, dx = %g\n", dy, dx);
		System.out.format("%g != %g\n",
				   Math.atan2(dy, dx),
				   thetas[k]);
		throw new Exception();
	    }
	}
	System.exit(0);
   }
}
