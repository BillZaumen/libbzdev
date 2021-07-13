all:
	(cd .. ; make libs)
	javac -classpath ../../BUILD/libbzdev.jar *.java
	jar cmf plates.mf ../plates.jar *.class
	rm *.class

clean:
	rm -f *.class

