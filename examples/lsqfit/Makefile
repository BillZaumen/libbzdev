all:
	(cd .. ; make libs)
	javac -classpath ../../BUILD/libbzdev.jar *.java
	jar cmf lsqfit.mf ../lsqfit.jar *.class
	jar cmf lsqfit2.mf ../lsqfit2.jar *.class
	rm *.class

clean:
	rm -f *.class

