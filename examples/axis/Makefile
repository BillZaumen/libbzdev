all:
	(cd .. ; make libs)
	javac -classpath ../../BUILD/libbzdev.jar *.java
	jar cmf axis.mf ../axis.jar *.class
	rm *.class

clean:
	rm -f *.class
