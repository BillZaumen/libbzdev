all:
	(cd .. ; make libs)
	javac -classpath ../../BUILD/libbzdev.jar *.java
	jar cmf legendre.mf ../legendre.jar *.class
	rm *.class

clean:
	rm -f *.class

