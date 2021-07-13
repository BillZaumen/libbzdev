all:
	(cd .. ; make libs)
	javac -classpath ../../BUILD/libbzdev.jar *.java
	jar cmf p3d.mf ../lockpart.jar *.class
	rm *.class

clean:
	rm -f *.class
