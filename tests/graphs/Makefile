BATIK = $(shell [ -f /usr/share/java/batik.jar ] && \
	echo ":/usr/share/java/batik.jar" || \
	echo "")

JAVA = java  -classpath ../../BUILD/libbzdev.jar:classes$(BATIK)

all:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) Test
	$(JAVA) TestExample
	$(JAVA) OverlayTest testGraph3.ps
	$(JAVA) ColorTest
	$(JAVA) ImageTest
	$(JAVA) RotationTest
	$(JAVA) AxisTest axistest.png
	$(JAVA) AxisTest axistest.ps
	$(JAVA) AxisTest axistest2.png scaling
	$(JAVA) AxisTest axistest2.ps scaling
	$(JAVA) AxisBuilderTest
	$(JAVA) LogAxisBuilderTest
	$(JAVA) ClockAxisBuilderTest
	$(JAVA) PanelTest
	$(JAVA) BBTest
	$(JAVA) GridTest
	$(JAVA) PolarTest

text:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) TextTest	

grid:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) GridTest

polar:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) PolarTest

itest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) ImageTest

example:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) TestExample

panel:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) PanelTest

clocktime:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) ClockAxisBuilderTest

abtest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) AxisBuilderTest

logabtest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) LogAxisBuilderTest

axistest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) AxisTest axistest.png
	$(JAVA) AxisTest axistest.ps
	$(JAVA) AxisTest axistest2.png scaling
	$(JAVA) AxisTest axistest2.ps scaling

bbox:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) BBTest

colors:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) ColorTest

clean:
	rm -rf testGraph.ps testGraph.png testGraph2.png \
		abtest*.png abclocktest*.png abclocktest4.ps\
		abclocktest4.eps \
		testReflect*.png testRev*.png \
		test*Graph.png testSymbolGraph?.png \
		testGraph?.ps testGraph?.eps \
		testgrid*.png testpolar*.png \
		exampleGraph.eps exampleGraph.ps exampleGraph.png \
		rotationTest.png TextTest.png \
		axistest2.png axistest2.ps axistest.ps axistest.png \
		logabtest1.png testGraph.svg testImage.png \
		testLogAxis.png testTC.png testTC.ps \
		classes
