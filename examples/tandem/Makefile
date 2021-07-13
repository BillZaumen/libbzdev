all:
	(cd .. ; make libs)
	javac -classpath ../../BUILD/libbzdev.jar  *.java
	jar cmf tandem1.mf ../tandem1.jar *.class
	jar cmf tandem2.mf ../tandem2.jar *.class
	jar cmf tandem3.mf ../tandem3.jar *.class
	jar cmf tandem4.mf ../tandem4.jar *.class
	rm *.class

clean:
	rm -f *.class
