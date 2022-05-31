JAVA = java -classpath ../../BUILD/libbzdev.jar:classes

LSNOF = java -Djava.security.policy=../../BUILD/libbzdev.policy \
	  -classpath ../../BUILD/libbzdev.jar:../../BUILD/lsnof.jar \
	   org.bzdev.bin.lsnof.FactoryPrinter

LSNOF2 = java -Djava.security.policy=../../BUILD/libbzdev.policy \
	 -p ../../BUILD:/usr/share/bzdev/librdanim-1.3.5.jar -m org.bzdev.lsnof/org.bzdev.bin.lsnof.FactoryPrinter

HAS_JS_TEST = \
	`java -Dscrunner.usrconf=test.conf -p ../../BUILD \
		-m org.bzdev.scrunner \
		--supportsLanguage ECMAScript`

all:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo '***** ' Trying Test:
	$(JAVA) Test
	@echo '***** ' Trying TPriority Test:
	$(JAVA) TPriorityTest
	@echo '***** ' Trying Tandem1:
	$(JAVA) Tandem1
	@echo '***** ' Trying Tandem2:
	$(JAVA) Tandem2
	@echo '***** ' Trying Tandem3:
	$(JAVA) Tandem3
	@echo '***** ' Trying Tandem4:
	$(JAVA) Tandem4
	@echo '***** ' Trying ParentTest:
	$(JAVA) ParentTest
	@echo '***** ' Trying QCompare:
	$(JAVA) QCompare
	@echo '***** ' Trying QScript:
	$(JAVA) QScript
	@echo '***** ' Trying STraceTest:
	$(JAVA) STraceTest
	@echo '***** ' Trying Wait-Queue Test:
	$(JAVA) WTest
	@echo '***** ' Trying Task-Queue Test:
	$(JAVA) TQTest
	@echo '***** ' Trying Server-Queue Test:
	$(JAVA) SQTest
	@echo '***** ' Trying Tick Test:
	$(JAVA) TickTest
	@echo '***** ' Trying ScriptTest
	$(JAVA) ScriptTest
	@echo '***** ' Trying ScriptTestESP
	$(JAVA) ScriptTestESP
	@echo '***** ' Trying SimFunctionTest
	$(JAVA) SimFunctionTest
	@echo '***** ' Trying FactoryTest
	$(JAVA) FactoryTest
	@echo '***** ' Trying TraceTest
	$(JAVA) TraceTest
	@echo '***** ' Trying LSNOF test:
	$(LSNOF) --html 'org.bzdev.devqsim.*' > test.html
	@echo '***** ' RVTest:
	$(JAVA) RVTest
	@ if $(HAS_JS_TEST) ; then \
	echo '***** testsim.js *****' ; \
	java -p ../../BUILD -m org.bzdev.scrunner testsim.js ; fi
	@echo '***** testsim.esp *****'
	java -p ../../BUILD -m org.bzdev.scrunner testsim.esp

lsnof1:
	$(LSNOF2) > /dev/null

lsnof2:
	$(LSNOF2) 'org.bzdev.devqsim.*'

tandem1:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) Tandem1

tandem2:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) Tandem2

tandem3:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) Tandem3

tandem4:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) Tandem4

sftest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) SimFunctionTest

stest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) ScriptTest

stest-esp:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) ScriptTestESP

rvtest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) RVTest

test:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo '***** ' Trying Test:
	$(JAVA) Test

testsim1:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) TestSim1

testsim2:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) TestSim2

clean:
	rm -rf classes
