# The environment to use
ENV = 

# directory for the Java command (must end in '/')
BIN =

JAVA = $(ENV) $(BIN)java -classpath ../../BUILD/libbzdev.jar:classes \
	-Djava.security.policy=../../BUILD/libbzdev.policy

JAVA1 = $(ENV) $(BIN)java -classpath ../../BUILD/libbzdev.jar:classes \
	-Djava.security.policy=`pwd`/sectest.policy

HAS_JS = java -p ../../BUILD -m org.bzdev.scrunner --supportsLanguage ECMAScript

JAVA_SKIPPED = echo ... skipping java ...

all:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac  -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) ScriptingTest --sandbox
	$(HAS_JS) && $(JAVA) ScriptingTest  test1.js
	$(JAVA) ScriptingTest  test1.esp
	$(HAS_JS) && $(JAVA) ScriptingTest  --sandbox test1.js
	$(JAVA) ScriptingTest  --sandbox test1.esp
	$(HAS_JS) && $(JAVA) ScriptingTest  --scriptSandbox test1.js
	$(JAVA) ScriptingTest  --scriptSandbox test1.esp
	if $(HAS_JS) ; then \
	$(JAVA) ScriptingTest test2.js ; echo Exit Status = $$? ; fi
	@$(HAS_JS) && echo ... EXCEPTION EXPECTED for test2.js
	$(JAVA) ScriptingTest test2.esp ; echo Exit Status = $$?
	@echo ... EXCEPTION EXPECTED for test2.esp
	if $(HAS_JS) ; then \
	$(JAVA) ScriptingTest --sandbox test2.js ; echo Exit Status = $$? ; fi
	$(JAVA) ScriptingTest --sandbox test2.esp ; echo Exit Status = $$?
	if $(HAS_JS) ; then \
	$(JAVA) ScriptingTest --scriptSandbox test2.js ; \
	echo Exit Status = $$? ; fi
	$(JAVA) ScriptingTest --scriptSandbox test2.esp ; echo Exit Status = $$?
	$(HAS_JS) && $(JAVA) ScriptingTest2 test3.js
	$(JAVA) ScriptingTest2 test3.esp
	$(HAS_JS) && $(JAVA) ScriptingTest2 --sandbox test3.js
	$(JAVA) ScriptingTest2 --sandbox test3.esp
	$(HAS_JS) && $(JAVA) ScriptingTest2 --scriptSandbox test3.js
	$(JAVA) ScriptingTest2 --scriptSandbox test3.esp
	@echo ---- private function test with parent ---
	$(JAVA) ScriptingTest3
	@echo --- permission test ----
	$(JAVA_SKIPPED) PermissionTest
	@echo --- Security Test using permissions ---
	( cd classes; jar cf ../sectest.jar *.class )
	$(HAS_JS) && $(JAVA1) SecTest
	$(JAVA1) ThreadTest
	@echo --- Listener Adapter Test ---
	$(HAS_JS) && $(JAVA) ScriptingTest4 test4.js
	$(JAVA) ScriptingTest4 test4.esp
	@echo --- Listener Adapter in sandbox ---
	$(HAS_JS) && $(JAVA) ScriptingTest4 --sandbox test4.js
	$(JAVA) ScriptingTest4 --sandbox test4.esp
	@echo --- Listener Adapter in scripting sandbox ---
	$(HAS_JS) && $(JAVA) ScriptingTest4 --scriptSandbox test4.js
	$(JAVA) ScriptingTest4 --scriptSandbox test4.esp
	echo ---- Now try code that will fail in a sandbox ---
	if $(HAS_JS) ; then \
	$(JAVA) ScriptingTest4 --sandbox test5.js || echo ... EXCEPTION OK ; fi
	$(JAVA) ScriptingTest4 --sandbox test5.esp || echo ... EXCEPTION OK
	if $(HAS_JS) ; then \
	$(JAVA) ScriptingTest4 --scriptSandbox test5.js \
		||  echo ... EXCEPTION OK ; fi
	$(JAVA) ScriptingTest4 --scriptSandbox test5.esp \
		||  echo ... EXCEPTION OK
	if $(HAS_JS) ; then \
	$(JAVA) ScriptingTest4 --trusted test5.js \
		||  echo ... EXCEPTION NOT EXPECTED ; fi
	$(JAVA) ScriptingTest4 --trusted test5.esp \
		||  echo ... EXCEPTION NOT EXPECTED
	rm test5.out
	@echo --- ESP test using javax.script API ---
	$(JAVA) EPTest
	@$(HAS_JS) && echo --- import test for ECMAScript
	$(HAS_JS) && $(JAVA) ImportTest itest.js
	@echo --- import test for ESP
	$(JAVA) ImportTest itest.esp

#
# Standalone test - no dependency on simulation modules
#
initial:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac  -d classes -classpath ../../BUILD/libbzdev.jar ScriptingTest.java
	$(JAVA) ScriptingTest --sandbox
	$(JAVA) ScriptingTest  test1.js
	$(JAVA) ScriptingTest  --sandbox test1.js
	$(JAVA) ScriptingTest  --scriptSandbox test1.js
	$(JAVA) ScriptingTest test2.js ; echo Exit Status = $$?
	$(JAVA) ScriptingTest --sandbox test2.js ; echo Exit Status = $$?
	$(JAVA) ScriptingTest --scriptSandbox test2.js ; echo Exit Status = $$?

initial0:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac  -d classes -classpath ../../BUILD/libbzdev.jar ScriptingTest.java
	$(JAVA) ScriptingTest  test1.js

sectest:
	@echo --- Security Test using permissions ---
	(cd ../.. ; make jars)
	mkdir -p classes
	javac  -d classes -classpath ../../BUILD/libbzdev.jar *.java
	( cd classes; jar cf ../sectest.jar *.class )
	$(JAVA1) SecTest

sectest2:
	@echo --- Security Test using permissions ---
	(cd ../.. ; make jars)
	mkdir -p classes
	javac  -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA1) SecTest2

script3:
	@echo ---- private function test with parent ---
	(cd ../.. ; make jars)
	mkdir -p classes
	javac  -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) ScriptingTest3

adapter:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac  -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo --- Listener Adapter in sandbox ---
	$(JAVA) ScriptingTest4 --sandbox test4.js
	@echo --- Listener Adapter in scripting sandbox ---
	$(JAVA) ScriptingTest4 --scriptSandbox test4.js

epjavax:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac  -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo --- EP test using javax.script API ---
	$(JAVA) EPTest

itest-js:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac  -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) ImportTest itest.js

itest-esp:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac  -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) ImportTest itest.esp

test1:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac  -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) ScriptingTest  test1.esp

thread:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac  -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA1) ThreadTest

eoltest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac  -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) EOLTest
clean:
	rm -rf classes
