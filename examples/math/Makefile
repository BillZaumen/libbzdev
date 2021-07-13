all:
	(cd .. ; make libs)
	javac -classpath ../../BUILD/libbzdev.jar *.java
	jar cmf math.mf ../math.jar *.class
	rm *.class

clean:
	rm -f *.class
