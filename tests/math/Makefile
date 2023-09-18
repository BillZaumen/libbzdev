JAVA = java -classpath ../../BUILD/libbzdev.jar:classes

# No longer using this rule for adder tests because as of Java 11,
# the AggressiveOpts argument is being deprecated and is no longer
# used.
#
JAVA2 = java -XX:+AggressiveOpts \
	-classpath ../../BUILD/libbzdev.jar:classes

HAS_JS = java -p ../../BUILD -m org.bzdev.scrunner --supportsLanguage ECMAScript


all:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo -------- GLQuadratureTest ------------
	$(JAVA) GLQuadratureTest
	@echo -------- SimpsonsRuleTest ------------
	$(JAVA) SimpsonsRuleTest
	@echo -------- RungeKuttaTest ------------
	$(JAVA) RungeKuttaTest
	@echo -------- RungeKuttaMVTest ------------
	$(JAVA) RungeKuttaMVTest
	@echo -------- RungeKutta2Test ------------
	$(JAVA) RungeKutta2Test
	@echo -------- RKUpdateTest ------------
	$(JAVA) RKUpdateTest
	@echo -------- TriSolveTest ------------
	$(JAVA) TriSolveTest
	@echo -------- PermTest ------------
	$(JAVA) PermTest
	@echo -------- LUDTest ------------
	$(JAVA) LUDTest
	@echo -------- CholeskyTest ------------
	$(JAVA) CholeskyTest
	@echo --------- QRDecomp Test -----
	$(JAVA) QRTest
	@echo --------- SVDecomp Test -----
	$(JAVA) SVDTest
	@echo --------- Eigenvalue Test -----
	$(JAVA) EigenvaluesTest
	@echo -------- RootFinderTest ------------
	$(JAVA) RootFinderTest
	@echo -------- SplineTest ------------
	$(JAVA) SplineTest
	@echo -------- SplineTest2 ------------
	$(JAVA) SplineTest2
	@echo --------- RValuedFunctionTest -------
	$(HAS_JS) && $(JAVA) RValuedFunctionTest || echo ... skipping js
	@echo ... repeat using ESP
	$(JAVA) RValuedFunctionTest esp
	@echo --------- RVFTwoTest -------
	$(HAS_JS) && $(JAVA) RVFTwoTest || echo ... skipping js
	@echo ... repeat using ESP
	$(JAVA) RVFTwoTest esp
	@echo --------- RVFThreeTest -------
	$(HAS_JS) && $(JAVA) RVFThreeTest || echo ... skipping js
	@echo ... repeat using ESP
	$(JAVA) RVFThreeTest esp
	@echo --------- BinomialTest -------
	$(JAVA) BinomialTest
	@echo --------- ConstTest -------
	$(JAVA) ConstTest
	@echo -------- FunctionsTest ------------
	$(JAVA) FunctionsTest
	@echo ------- RVFVATest ------------
	$(HAS_JS) && $(JAVA) RVFVATest || echo ... skipping js
	@echo ... repeat using ESP
	$(JAVA) RVFVATest esp
	@echo -------- BicubicTest ------------
	$(JAVA) BicubicTest
	@echo ------- B3TriangleTest -------
	$(JAVA) B3TriangleTest
	@echo ------- VectorOpsTest --------
	$(JAVA) VectorOpsTest
	@echo ------- AdderTest --------
	$(JAVA) AdderTest
	@echo --------- LMATest -----------
	$(JAVA) LMATest
	@echo ----- LeastSquaresFitTest ---------
	$(JAVA) LeastSquaresFitTest
	@echo ---- BSplineKnotTest -----
	$(JAVA) BSplineKnotTest
	@echo ----- BSplineTest ---------
	$(JAVA) BSplineTest
	@echo ----- BSplineArrayTest ---------
	$(JAVA) BSplineArrayTest
	@echo -------- NurbsTest --------
	$(JAVA) NurbsTest
	@echo ------ MatrixOps Test -----
	$(JAVA) MatrixOpsTest
	@echo ------ Bezout Test -----
	$(JAVA) BezoutTest
	@echo ----- FFT Test ---------
	$(JAVA) FFTTest
	(cd stats ; make)


binomial:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo --------- BinomialTest -------
	$(JAVA) BinomialTest


poly:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo -------- PolynomialTest ------------
	$(JAVA) PolynomialTest

root:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo -------- RootFinderTest ------------
	$(JAVA) RootFinderTest

rootc:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo -------- RootFinderTest - Cubic------------
	$(JAVA) RootFinderCubic

fft:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ----- FFT Test ---------
	$(JAVA) FFTTest

fftTiming:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ----- FFT Timing Test ---------
	$(JAVA) FFTTiming

bezout:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------ Bezout Test -----
	$(JAVA) BezoutTest

matrix:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------ MatrixOps Test -----
	$(JAVA) MatrixOpsTest

bspline:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ----- BSplineTest ---------
	$(JAVA) BSplineTest

bsplinearray:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ----- BSplineArrayTest ---------
	$(JAVA) BSplineArrayTest

bsplineknots:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ---- BSplineKnotTest -----
	$(JAVA) BSplineKnotTest

lud:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo -------- LUDTest ------------
	$(JAVA) LUDTest

cholesky:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo -------- CholeskyTest ------------
	$(JAVA) CholeskyTest

lma:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo --------- LMATest -----------
	$(JAVA) LMATest

rvf:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------- RValuedFunctionTest ------------
	$(HAS_JS) && $(JAVA) RValuedFunctionTest || echo ... skipping js
	@echo ... repeat using ESP
	$(JAVA) RValuedFunctionTest esp

rvf2:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------- RVFTwoTest ------------
	$(HAS_JS) && $(JAVA) RVFTwoTest || echo ... skipping js
	@echo ... repeat using ESP
	$(JAVA) RVFTwoTest esp

rvf3:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------- RVFThreeTest ------------
	$(HAS_JS) && $(JAVA) RVFThreeTest || echo ... skipping js
	@echo ... repeat using ESP
	$(JAVA) RVFThreeTest esp

rvfva:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------- RVFVATest ------------
	$(HAS_JS) && $(JAVA) RVFVATest || echo ... skipping js
	@echo ... repeat using ESP
	$(JAVA) RVFVATest esp

lsq:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ----- LeastSquareFitTest ---------
	$(JAVA) LeastSquaresFitTest

bezier1:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo -------- BezierSplineTest ------------
	$(JAVA) BezierSplineTest

spline1:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo -------- SplineTest ------------
	$(JAVA) SplineTest

spline2:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo -------- SplineTest2 ------------
	$(JAVA) SplineTest2


functions:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo -------- FunctionsTest ------------
	$(JAVA) FunctionsTest

adder:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------- AdderTest --------
	$(JAVA) AdderTest

gauss:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo -------- GLQuadratureTest ------------
	$(JAVA) GLQuadratureTest
	@echo -------- VectorValuedGLQTest ---------
	$(JAVA) VectorValuedGLQTest

vector:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) VectorOpsTest


simpson:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) SimpsonsRuleTest

rk:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo -------- RungeKuttaTest ------------
	$(JAVA) RungeKuttaTest

rkmv:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo -------- RungeKuttaMVTest ------------
	$(JAVA) RungeKuttaMVTest

rk2:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo -------- RungeKutta2Test ------------
	$(JAVA) RungeKutta2Test

rku:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) RKUpdateTest

nurbs:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) NurbsTest

qrtest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) QRTest

svdtest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) SVDTest

eigenvalues:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) EigenvaluesTest

srtest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) StaticRandomTest

# Used for timing estimates and comparing the Rootfiner implementation
# to the java.awt.geom.CubicCurve2ED implementation of solveCubic.
#
cubictest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) CubicTest


clean:
	rm -rf classes
	(cd stats; make clean)
