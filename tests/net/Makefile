JAVA = java -p ../../BUILD -classpath classes --add-modules org.bzdev

all: compile
	$(JAVA) Test
	@echo ---- ICalBuilder Test ------
	@$(JAVA) ICalBuilderTest
	@echo ---- FormDataIterator Test
	$(JAVA) FormDataTest


compile:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -p ../../BUILD  --add-modules org.bzdev *.java

test: compile
	$(JAVA) Test

ical: compile
	@echo ---- ICalBuilder Test ------
	@$(JAVA) ICalBuilderTest

formtest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) FormDataTest


cwtest: compile
	$(JAVA) CWTest

cwtest1: compile
	$(JAVA) CWTest 10

run-cwtest:
	sudo $(JAVA) CWTest

clean:
	rm -rf classes testcal.ics
