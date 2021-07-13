all:
	(cd .. ; make libs)
	javac -classpath ../../BUILD/libbzdev.jar *.java
	jar cmf dplot.mf ../dplot.jar *.class
	rm *.class

clean:
	rm -f *.class
