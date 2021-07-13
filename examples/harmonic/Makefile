all:
	(cd .. ; make libs)
	javac -classpath ../../BUILD/libbzdev.jar *.java
	jar cmf harmonic.mf ../harmonic.jar *.class
	rm *.class

clean:
	rm -f *.class
