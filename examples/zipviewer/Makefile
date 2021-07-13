all:
	(cd .. ; make libs)
	javac -classpath ../../BUILD/libbzdev.jar *.java
	jar cmf zipviewer.mf ../zipviewer.jar *.class

clean:
	rm -f *.class
