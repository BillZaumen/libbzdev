all:
	(cd .. ; make libs)
	javac -classpath ../../BUILD/libbzdev.jar *.java
	jar cmf pendulum.mf ../pendulum.jar *.class
	rm *.class

clean:
	rm -f *.class
