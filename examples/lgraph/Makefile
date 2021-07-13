all:
	(cd .. ; make libs)
	javac -classpath ../../BUILD/libbzdev.jar *.java
	jar cmf lgraph.mf ../lgraph.jar *.class
	rm *.class

clean:
	rm -f *.class
