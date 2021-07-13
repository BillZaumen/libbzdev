all:
	(cd ../.. ; make)
	javac -classpath ../../BUILD/libbzdev.jar *.java
	jar cmf pathlength.mf ../pathlength.jar *.class
	rm *.class

clean:
	rm -f *.class
