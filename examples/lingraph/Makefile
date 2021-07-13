all:
	(cd .. ; make libs)
	javac -classpath ../../BUILD/libbzdev.jar *.java
	jar cmf lingraph.mf ../lingraph.jar *.class
	rm *.class

clean:
	rm -f *.class
