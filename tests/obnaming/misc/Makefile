# JAVA = java -classpath ../../../BUILD/lib/libbzdev.jar:classes

JAVA = java -p ../../../BUILD --add-modules org.bzdev -classpath classes

JAVAC = javac -p ../../../BUILD --add-modules org.bzdev -classpath classes


all:
	(cd ../../.. ; make jars)
	mkdir -p classes
	$(JAVAC) -d classes -Xlint:unchecked *.java
	$(JAVA) InitialTest

clean:
	rm -rf classes
