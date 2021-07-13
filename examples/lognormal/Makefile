all:
	(cd .. ; make libs)
	javac -classpath ../../BUILD/libbzdev.jar *.java
	jar cmf lognormal.mf ../lognormal.jar *.class
	rm *.class

clean:
	rm -f *.class
