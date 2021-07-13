all:
	(cd .. ; make libs)
	javac -classpath ../../BUILD/libbzdev.jar *.java
	jar cmf blackhole.mf ../blackhole.jar *.class
	rm *.class

clean:
	rm -f *.class
