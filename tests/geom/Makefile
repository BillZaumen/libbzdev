JAVA = java  -classpath ../../BUILD/libbzdev.jar:classes

HAS_JS = java -p ../../BUILD -m org.bzdev.scrunner --supportsLanguage ECMAScript


all:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo -------- Path2DInfoTest ------------
	@$(JAVA) Path2DInfoTest
	@echo -------- Location2DTest -------------
	@$(JAVA) Location2DTest
	@echo -------- Location3DTest -------------
	@$(JAVA) Location3DTest
	@echo -------- BezierSplineTest ------------
	@$(JAVA) BezierSplineTest
	@echo -------- BasicPathTest -------------
	@$(JAVA) BasicPathTest
	@echo --------- CPointTest --------------
	@$(JAVA) CPointTest
	@echo --------- BuilderTest --------------
	@$(JAVA) BuilderTest
	@$(JAVA) BuilderTestOps
	@echo --------- BuilderTest2 -------------
	@$(JAVA) BuilderTest2
	@echo --------- BuilderTest3 -------------
	@$(JAVA) BuilderTest3
	@echo ------- BuilderTest4 ----------
	@$(JAVA) BuilderTest4
	@echo ------- BuilderTest5 ----------
	@$(JAVA) BuilderTest5 false
	@$(JAVA) BuilderTest5 true
	@echo ------- BuilderTest6 ----------
	@$(JAVA) BuilderTest6
	@echo ------- BuilderTest7 ----------
	@$(JAVA) BuilderTest7
	@$(JAVA) BuilderTest8
	@echo --------- scripting path-builder tests
	@$(HAS_JS) && java -classpath \
		../../BUILD/libbzdev.jar:../../BUILD/scrunner.jar \
		org.bzdev.bin.scrunner.SCRunnerCmd pbtest.js \
		|| echo ... skipping pbtest.js
	@java -classpath \
		../../BUILD/libbzdev.jar:../../BUILD/scrunner.jar \
		org.bzdev.bin.scrunner.SCRunnerCmd pbtest.esp
	@java -classpath \
		../../BUILD/libbzdev.jar:../../BUILD/scrunner.jar \
		org.bzdev.bin.scrunner.SCRunnerCmd cwrtest.esp
	@echo --------- AreaTest -----------------
	@$(JAVA) AreaTest
	@echo --------- Point3DTest --------------
	@$(JAVA) Point3DTest
	@echo ------- AffineTest ----------
	$(JAVA) AffineTest
	@echo ------- Test3D ----------
	$(JAVA) Test3D
	@echo ------- BasicPathTest3D ----------
	$(JAVA) BasicPath3DTest
	@echo ------- Path3DInfoTest ----------
	$(JAVA) Path3DInfoTest
	@echo ------- Surface3DTest ----------
	$(JAVA) Surface3DTest
	@echo ------- Surface3DTest2 ----------
	$(JAVA) Surface3DTest2
	@echo ------- Surface3DTest3 ----------
	$(JAVA) Surface3DTest3
	@echo ------- Surface3DTest4 ----------
	$(JAVA) Surface3DTest4
	@echo ------- Surface3DTest5 ----------
	$(JAVA) Surface3DTest5
	@echo ------- Surface3DTest6 ----------
	$(JAVA) Surface3DTest6
	@echo ------- Surface3DTest7 ----------
	$(JAVA) Surface3DTest7
	@echo -------- SplineTest ---------
	$(JAVA) SplineTest
	@echo -------- FlatTest ----------
	$(JAVA) FlatTest2D
	@echo -------- FlatTest2D ----------
	$(JAVA) FlatTest2D
	@echo -------- CondTest2D ----------
	$(JAVA) CondTest2D
	@echo -------- CondTest3D ----------
	$(JAVA) CondTest3D
	@echo -------- SubdivTest ----------
	$(JAVA) SubdivTest
	@echo -------- SubdivTest2 ----------
	$(JAVA) SubdivTest2
	@echo -------- PathSplitTest2D ----------
	$(JAVA) PathSplitTest2D
	@echo -------- PathSplitTest3D ----------
	$(JAVA) PathSplitTest3D
	@echo -------- Sphere ---------
	$(JAVA) Sphere
	@echo -------- Sphere2 ---------
	$(JAVA) Sphere2
	@echo -------- Sphere3 ---------
	$(JAVA) Sphere3
	@echo -------- BGSphere ---------
	$(JAVA) BGSphere
	@echo -------- BGSphere2 ---------
	$(JAVA) BGSphere2
	@echo -------- BGSphere3 ---------
	$(JAVA) BGSphere3
	@echo -------- BGCycloidTest ---------
	$(JAVA) BGCycloidTest
	@echo -------- CycliodTest ------------
	$(JAVA) CycloidTest
	@echo -------- CycliodTestOps ------------
	$(JAVA) CycloidTestOps
	@echo -------- Torus ---------
	$(JAVA) Torus
	@echo -------- ModifiedTorus ---------
	$(JAVA) MTorus
	@echo -------- SurfaceOps --------
	$(JAVA) SurfaceOpsTest
	@echo -------- MomentTest -------
	$(JAVA) MomentTest
	@echo ------ RoundedCubeTest -------
	$(JAVA) RoundedCubeTest
	@echo ------ Paths2DTest -------
	$(JAVA) Paths2DTest
	@echo ------ Paths3DTest -------
	$(JAVA) Paths2DTest
	@echo -------- ConnectTest1 ---------
	$(JAVA) ConnectTest1
	@echo -------- ConnectTest2 ---------
	$(JAVA) ConnectTest2
	$(JAVA) ConnectTest2 --split
	@echo -------- ConnectTest3 ---------
	$(JAVA) ConnectTest3
	$(JAVA) ConnectTest3 --split
	@echo -------- ConnectTest4 ---------
	$(JAVA) ConnectTest4
	$(JAVA) ConnectTest4 --split
	@echo -------- ConnectTest5 ---------
	$(JAVA) ConnectTest5
	$(JAVA) ConnectTest5 --split
	@echo -------- ConnectTest6 ---------
	$(JAVA) ConnectTest6
	$(JAVA) ConnectTest6 --split
	@echo -------- ConnectTest7 ---------
	$(JAVA) ConnectTest7
	@echo -------- ConnectTest8 ---------
	$(JAVA) ConnectTest8
	@echo -------- MobiusStrip ---------
	$(JAVA) MobiusStrip
	@echo -------- MobiusStrip2 ---------
	$(JAVA) MobiusStrip2
	@echo -------- MobiusStrip3 ---------
	$(JAVA) MobiusStrip3
	@echo -------- MobiusStrip4 ---------
	$(JAVA) MobiusStrip4
	@echo -------- BGTest --------
	$(JAVA) BGTest
	@echo -------- BGTest2 --------
	$(JAVA) BGTest2
	@echo -------- BGTest3 --------
	$(JAVA) BGTest3
	@echo -------- BGPathTest -------
	$(JAVA) BGPathTest
	$(JAVA) BGPathTest2
	$(JAVA) BGPathTest3
	@echo ------- BezierMapperTest ----------
	$(JAVA) BezierMapperTest
	@echo -------- PathIntegralTest --------
	@$(JAVA) PathIntegralTest
	@echo -------- SurfaceIntTest --------
	@$(JAVA) SurfaceIntTest
	@echo ------- BezierVertexTest ----------
	$(JAVA) BezierVertexTest
	@echo ------- ConvexPathCTest -----------
	@$(JAVA) ConvexPathCTest
	@echo ------- TPITest -----------
	@$(JAVA) TPITest
	@echo ------- TPITest -----------
	@$(JAVA) CondTest
	@echo ------- TwoCylinderTest -----------
	@$(JAVA) TwoCylinderTest
	@echo ------- CylinderBoxTest -----------
	@$(JAVA) CylinderBoxTest
	@echo ------- BezierBox -----------
	@$(JAVA) BezierBox
	@$(JAVA) BezierBox2
	@echo ---- Cylinder ------
	@$(JAVA) CylinderTest
	@echo ---- Cylinder2 ------
	@$(JAVA) CylinderTest2
	@echo ---- Carafe ----
	@$(JAVA) Carafe

affine:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo ------- AffineTest ----------
	$(JAVA) AffineTest


btest4:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo ------- BuilderTest4 ----------
	$(JAVA) BuilderTest4

btest5a:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo ------- BuilderTest5 ----------
	$(JAVA) BuilderTest5 false

btest5b:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo ------- BuilderTest5 ----------
	$(JAVA) BuilderTest5  true

# set CTL to true or false, and I and J to 0 ... 5
# to reduce the output for debugging.
btest6:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo ------- BuilderTest6 $(CTL) $(I) $(J) ----------
	$(JAVA) BuilderTest6 $(CTL) $(I) $(J)

# set CTL to true or false, END to true or false, and I and J to 0 ... 5
# to reduce the output for debugging.
btest7:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo ------- BuilderTest7 $(CTL) $(I) $(J) ----------
	$(JAVA) BuilderTest7 $(CTL) $(END) $(I) $(J)

# set CTL to true or false, and I and J to 0 ... 5
# to reduce the output for debugging.
btest8:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo ------- BuilderTest8 $(CTL) $(I) $(J) ----------
	$(JAVA) BuilderTest8 $(CTL) $(I) $(J)

btest9:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo ------- BuilderTest9  ----------
	$(JAVA) BuilderTest9


btest3d:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	$(JAVA) BasicPath3DTest

bvtest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo ------- BezierVertexTest ----------
	$(JAVA) BezierVertexTest

paths2d:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo ------ Paths2DTest -------
	$(JAVA) Paths2DTest

paths3d:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo ------ Paths3DTest -------
	$(JAVA) Paths3DTest

rcube:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo ------ RoundedCubeTest -------
	$(JAVA) RoundedCubeTest


surfaceops:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo -------- SurfaceOps --------
	$(JAVA) SurfaceOpsTest

sphere:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo ------- Sphere ----------
	$(JAVA) Sphere

bgsphere1:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo ------- BGSphere1 ----------
	$(JAVA) BGSphere1

bgmapper:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo ------- BezierMapperTest ----------
	$(JAVA) BezierMapperTest

builder:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo --------- BuilderTest --------------
	@$(JAVA) BuilderTest

cpoint:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo --------- CPointTest --------------
	@$(JAVA) CPointTest

loc2d:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo -------- Location2DTest -------------
	@$(JAVA) Location2DTest

loc3d:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo -------- Location3DTest -------------
	@$(JAVA) Location3DTest


bgtest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo -------- BGTest --------
	$(JAVA) BGTest

bgtest2:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo -------- BGTest2 --------
	$(JAVA) BGTest2

bgtest3:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo -------- BGTest3 --------
	$(JAVA) BGTest3

bgtest4:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo -------- BGTest2 --------
	$(JAVA) OpenFitting

bgsphere:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo ------- BGSphere ----------
	$(JAVA) BGSphere

moments:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	$(JAVA) MomentTest

bgcycloid0:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	$(JAVA) BGCycloidTest

bgpath:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	$(JAVA) BGPathTest

bgpath2:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	$(JAVA) BGPathTest2

bgpath3:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	$(JAVA) BGPathTest3


bgcycloid:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo -------- BGCycloidTest ---------
	$(JAVA) BGCycloidTest
	@echo -------- ConnectTest1 ---------
	$(JAVA) ConnectTest1
	@echo -------- ConnectTest2 ---------
	$(JAVA) ConnectTest2
	@echo -------- ConnectTest3 ---------
	$(JAVA) ConnectTest3
	@echo -------- ConnectTest4 ---------
	$(JAVA) ConnectTest4
	@echo -------- ConnectTest5 ---------
	$(JAVA) ConnectTest5
	@echo -------- ConnectTest6 ---------
	$(JAVA) ConnectTest6
	@echo -------- ConnectTest7 ---------
	$(JAVA) ConnectTest7
	@echo -------- ConnectTest8 ---------
	$(JAVA) ConnectTest8

sphere2:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo ------- Sphere2 ----------
	$(JAVA) Sphere2

# All tests involving BezierGrid instances.
bgrid:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo -------- BGSphere ---------
	$(JAVA) BGSphere
	@echo ------- Sphere2 ----------
	$(JAVA) BGSphere2
	@echo ------- Sphere3 ----------
	$(JAVA) BGSphere3
	@echo ------- Sphere4 ----------
	$(JAVA) BGSphere4
	@echo -------- BGCycloidTest ---------
	$(JAVA) BGCycloidTest
	@echo -------- BGTest --------
	$(JAVA) BGTest
	@echo ------- Torus ----------
	$(JAVA) Torus
	@echo -------- ModifiedTorus ---------
	$(JAVA) MTorus
	@echo -------- ConnectTest1 ---------
	$(JAVA) ConnectTest1


# This runs the same tests as 'make all' but also creates sets of images
# showing the surfaces that were created.
ctest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo -------- ConnectTest1 ---------
	$(JAVA) ConnectTest1 --isq
	@echo -------- ConnectTest2 ---------
	$(JAVA) ConnectTest2 --isq
	$(JAVA) ConnectTest2 --split
	@echo -------- ConnectTest3 ---------
	$(JAVA) ConnectTest3 --isq
	$(JAVA) ConnectTest3 --split
	@echo -------- ConnectTest4 ---------
	$(JAVA) ConnectTest4 --isq
	$(JAVA) ConnectTest4 --split
	@echo -------- ConnectTest5 ---------
	$(JAVA) ConnectTest5 --isq
	$(JAVA) ConnectTest5 --split
	@echo -------- ConnectTest6 ---------
	$(JAVA) ConnectTest6 --isq
	$(JAVA) ConnectTest6 --split
	@echo -------- ConnectTest7 ---------
	$(JAVA) ConnectTest7 --isq
	@echo -------- ConnectTest8 ---------
	$(JAVA) ConnectTest8 --isq

ctest1:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo -------- ConnectTest1 ---------
	$(JAVA) ConnectTest1 --isq

ctest2:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo -------- ConnectTest2 ---------
	$(JAVA) ConnectTest2 --isq
	$(JAVA) ConnectTest2 --split

ctest3:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo -------- ConnectTest3 ---------
	$(JAVA) ConnectTest3 --isq
	$(JAVA) ConnectTest3 --split

ctest4:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo -------- ConnectTest4 ---------
	$(JAVA) ConnectTest4 --isq
	$(JAVA) ConnectTest4 --split

ctest5:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo -------- ConnectTest5 ---------
	$(JAVA) ConnectTest5 --isq
	$(JAVA) ConnectTest5 --split

ctest6:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo -------- ConnectTest6 ---------
	$(JAVA) ConnectTest6 --isq

ctest7:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo -------- ConnectTest7 ---------
	$(JAVA) ConnectTest7 --isq

ctest8:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo -------- ConnectTest8 ---------
	$(JAVA) ConnectTest8 --isq

# like ctest but uses createExtensionGrid
etest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo -------- ExtensionTest1 ---------
	$(JAVA) ExtensionTest1 --isq
	@echo -------- ExtensionTest2 ---------
	$(JAVA) ExtensionTest2 --isq

etest2:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo -------- ExtensionTest2 ---------
	$(JAVA) ExtensionTest2 --isq

mobius:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo -------- MobiusStrip ---------
	$(JAVA) MobiusStrip --isq
	@echo -------- MobiusStrip2 ---------
	$(JAVA) MobiusStrip2 --isq
	@echo -------- MobiusStrip3 ---------
	$(JAVA) MobiusStrip3 --isq
	@echo -------- MobiusStrip4 ---------
	$(JAVA) MobiusStrip4 --isq

mobius1:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo -------- MobiusStrip ---------
	$(JAVA) MobiusStrip --isq

mobius2:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo -------- MobiusStrip2 ---------
	$(JAVA) MobiusStrip2 --isq


mobius3:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo -------- MobiusStrip3 ---------
	$(JAVA) MobiusStrip3 --isq

mobius4:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo -------- MobiusStrip4 ---------
	$(JAVA) MobiusStrip4 --isq

mobius5:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo -------- MobiusStrip5 ---------
	$(JAVA) MobiusStrip5 --isq --supports


bgsphere2:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo ------- Sphere2 ----------
	$(JAVA) BGSphere2

bgsphere3:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo ------- Sphere3 ----------
	$(JAVA) BGSphere3

bgsphere4:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo ------- Sphere4 ----------
	$(JAVA) BGSphere4

bgsphere5:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo ------- Sphere5 ----------
	$(JAVA) BGSphere5 --isq


sphere3:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo ------- Sphere3 ----------
	$(JAVA) Sphere3

torus:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo ------- Torus ----------
	$(JAVA) Torus
	@echo -------- ModifiedTorus ---------
	$(JAVA) MTorus

mtorus:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo -------- ModifiedTorus ---------
	$(JAVA) MTorus

# used to estimate size limit for switching from sequential to parallel
ntorusA:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo -------- ModifiedTorus ---------
	$(JAVA) NTorus area

ntorusV:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo -------- ModifiedTorus ---------
	$(JAVA) NTorus vol

ntorusCM:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo -------- ModifiedTorus ---------
	$(JAVA) NTorus cm

ntorusM:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo -------- ModifiedTorus ---------
	$(JAVA) NTorus moments

test3d:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo ------- Test3D ----------
	$(JAVA) Test3D

info2d:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo -------- Path2DInfoTest ------------
	@$(JAVA) Path2DInfoTest

info2d-timing:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo -------- Path2DInfoTest ------------
	@$(JAVA) Path2DInfoTest timing

info2d-timing2:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo -------- Path2DInfoTest ------------
	@$(JAVA) Path2DInfoTest timing2

info3d:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo -------- Path3DInfoTest ------------
	@$(JAVA) Path3DInfoTest

pstest2d:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo -------- PathSplitTest2D ----------
	$(JAVA) PathSplitTest2D

basic2d:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo -------- BasicPathTest -------------
	@$(JAVA) BasicPathTest

basic3d:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo ------- BasicPathTest3D ----------
	$(JAVA) BasicPath3DTest


pstest3d:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo -------- PathSplitTest3D ----------
	$(JAVA) PathSplitTest3D

flat:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo -------- FlatTest ----------
	$(JAVA) FlatTest

flat2d:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo -------- FlatTest2D ----------
	$(JAVA) FlatTest2D

cond2d:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo -------- CondTest2D ----------
	$(JAVA) CondTest2D

cond3d:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo -------- CondTest3D ----------
	$(JAVA) CondTest3D


spline:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	$(JAVA) SplineTest


subdiv:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	$(JAVA) SubdivTest

subdiv2:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	$(JAVA) SubdivTest2

surface:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	$(JAVA) Surface3DTest

surface2:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	$(JAVA) -Xmx1G -Xmn500M Surface3DTest2

surface3:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	$(JAVA) Surface3DTest3

surface4:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	$(JAVA) Surface3DTest4

surface5:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo ------- Surface3DTest5 ----------
	$(JAVA) Surface3DTest5

surface6:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo ------- Surface3DTest6 ----------
	$(JAVA) Surface3DTest6

surface7:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo ------- Surface3DTest7 ----------
	$(JAVA) Surface3DTest7


bezier:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@$(JAVA) BezierSplineTest

info:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo -------- Path2DInfoTest ------------
	@$(JAVA) Path2DInfoTest
	@echo ------- Path3DInfoTest ----------
	$(JAVA) Path3DInfoTest

cycloid:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo -------- CycliodTest ------------
	$(JAVA) CycloidTest
	@echo -------- CycliodTestOps ------------
	$(JAVA) CycloidTestOps

cpc:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	$(JAVA) ConvexPathCTest
	@echo ----- CubicVertexRing ------
	@$(JAVA) CubicVertexRing

cpc2:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	$(JAVA) ConvexPathCTest2


cvring:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo ----- CubicVertexRing ------
	@$(JAVA) CubicVertexRing

pbtest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@(HAS_JS) && java -classpath \
		../../BUILD/libbzdev.jar:../../BUILD/scrunner.jar \
		org.bzdev.bin.scrunner.SCRunnerCmd pbtest.js \
		|| echo ... skipping pbtest.js

pbtest-esp:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@java -classpath \
		../../BUILD/libbzdev.jar:../../BUILD/scrunner.jar \
		org.bzdev.bin.scrunner.SCRunnerCmd --stackTrace pbtest.esp



cycloid-esp:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@java -classpath \
		../../BUILD/libbzdev.jar:../../BUILD/scrunner.jar \
		org.bzdev.bin.scrunner.SCRunnerCmd --stackTrace cycloid.esp

cycloid2-esp:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@java -classpath \
		../../BUILD/libbzdev.jar:../../BUILD/scrunner.jar \
		org.bzdev.bin.scrunner.SCRunnerCmd --stackTrace cycloid2.esp


cwrtest-esp:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@java -classpath \
		../../BUILD/libbzdev.jar:../../BUILD/scrunner.jar \
		org.bzdev.bin.scrunner.SCRunnerCmd --stackTrace cwrtest.esp

tptest-esp:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@java -classpath \
		../../BUILD/libbzdev.jar:../../BUILD/scrunner.jar \
		org.bzdev.bin.scrunner.SCRunnerCmd --stackTrace tptest.esp
captest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo -------- CapTest ------------
	$(JAVA) CapTest


area:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@echo --------- AreaTest -----------------
	@$(JAVA) AreaTest

pint:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@$(JAVA) PathIntegralTest

sint:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@$(JAVA) SurfaceIntTest

tpitest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@$(JAVA) TPITest

condtest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@$(JAVA) CondTest

carafe:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@$(JAVA) Carafe

cylinder:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@$(JAVA) CylinderTest

cylinder2:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@$(JAVA) CylinderTest2

twocylinders:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@$(JAVA) TwoCylinderTest

cylinderbox:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@$(JAVA) CylinderBoxTest

bezierbox:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@$(JAVA) BezierBox
	@$(JAVA) BezierBox2

hextest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	@$(JAVA) Path2DHexTest.java

clean:
	rm -rf classes
	rm -f *.isq *.png *.stl condtest?.ps condtest?.eps circle.ps
