JAVA = java -Djava.security.policy=../../BUILD/libbzdev.policy \
	-classpath ../../BUILD/libbzdev.jar:classes

CODEBASE = $(shell cd ../..; echo file://`pwd`/BUILD/bzdevlib.jar | \
	sed  s/\\//\\\\\\\\\\//g)
all:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	$(JAVA) ZipDocWriterTest
	unzip -v test.dat
	$(JAVA) DelayTest
	ls -l testdir
	ls -l testdir/nested
	@rm -f test1.dat
	@rm -rf testdir
	@rm test.dat
	$(JAVA) CSVTest
	@rm CSVTest.csv
	$(JAVA) DetabTest DetabTest.txt

line:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	$(JAVA) LineReaderTest

csv:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	$(JAVA) CSVTest

detab:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	$(JAVA) DetabTest DetabTest.txt

# fis:
#	(cd ../.. ; make jars)
#	mkdir -p classes
#	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
#	$(JAVA) FISTest

zdoc:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	$(JAVA) ZipDocWriterTest


clean:
	rm -rf classes ext CSVTest.csv
