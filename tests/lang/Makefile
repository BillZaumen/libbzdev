JAVA  = java -classpath ../../BUILD/libbzdev.jar:classes \
	-Djava.security.policy=../../BUILD/libbzdev.policy

JAVA2 = java -classpath ../../BUILD/libbzdev.jar:classes \
	-Djava.security.policy=dtsm.policy

all: singletest
	(cd dmcache ; make)
	(cd dmsclass ; make)
	(cd dmtests ; make)
	(cd multi ; make)

singletest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) CallTest
	$(JAVA) ClassSorterTest ../../BUILD/libbzdev.jar
	$(JAVA) MathOpsTest

calltest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) CallTest

libresource:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) LibResource

mathops:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) MathOpsTest

finder:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) ClassFinderTest
	java -p ../../BUILD -classpath classes ClassFinderTest


clean:
	rm -rf classes
	(cd dmtests ; make clean)
	(cd dmcache ; make clean)
	(cd dmsclass ; make clean)
	(cd multi ; make clean)
