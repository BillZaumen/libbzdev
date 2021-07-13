JAVA = java -p ../../../BUILD -classpath classes --add-modules org.bzdev

all:
	(cd ../../.. ; make jars)
	mkdir -p classes
	javac -d classes -p ../../../BUILD --add-modules org.bzdev \
		-classpath classes *.java
	echo ----- RVTest ------
	$(JAVA) RVTest
	echo ----- GaussianRVsTest ------
	$(JAVA) GaussianRVsTest

grv:
	(cd ../../.. ; make jars)
	mkdir -p classes
	javac -d classes -p ../../../BUILD  --add-modules org.bzdev \
		-classpath classes *.java
	echo ----- GaussianRVsTest ------
	$(JAVA) GaussianRVsTest

clean:
	rm -rf classes
