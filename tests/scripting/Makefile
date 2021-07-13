# The environment to use
ENV = 

# directory for the Java command (must end in '/')
BIN =

JAVA = $(ENV) $(BIN)java -classpath ../../BUILD/libbzdev.jar:classes \
	-Djava.security.policy=../../BUILD/libbzdev.policy

JAVA1 = $(ENV) $(BIN)java -classpath ../../BUILD/libbzdev.jar:classes \
	-Djava.security.policy=`pwd`/sectest.policy

all:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac  -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) ScriptingTest --sandbox
	$(JAVA) ScriptingTest  test1.js
	$(JAVA) ScriptingTest  test1.esp
	$(JAVA) ScriptingTest  --sandbox test1.js
	$(JAVA) ScriptingTest  --sandbox test1.esp
	$(JAVA) ScriptingTest  --scriptSandbox test1.js
	$(JAVA) ScriptingTest  --scriptSandbox test1.esp
	$(JAVA) ScriptingTest test2.js ; echo Exit Status = $$?
	@echo ... EXCEPTION EXPECTED for test2.js
	$(JAVA) ScriptingTest test2.esp ; echo Exit Status = $$?
	@echo ... EXCEPTION EXPECTED for test2.esp
	$(JAVA) ScriptingTest --sandbox test2.js ; echo Exit Status = $$?
	$(JAVA) ScriptingTest --sandbox test2.esp ; echo Exit Status = $$?
	$(JAVA) ScriptingTest --scriptSandbox test2.js ; echo Exit Status = $$?
	$(JAVA) ScriptingTest --scriptSandbox test2.esp ; echo Exit Status = $$?
	$(JAVA) ScriptingTest2 test3.js
	$(JAVA) ScriptingTest2 test3.esp
	$(JAVA) ScriptingTest2 --sandbox test3.js
	$(JAVA) ScriptingTest2 --sandbox test3.esp
	$(JAVA) ScriptingTest2 --scriptSandbox test3.js
	$(JAVA) ScriptingTest2 --scriptSandbox test3.esp
	@echo ---- private function test with parent ---
	$(JAVA) ScriptingTest3
	@echo --- permission test ----
	$(JAVA) PermissionTest
	@echo --- Security Test using permissions ---
	( cd classes; jar cf ../sectest.jar *.class )
	$(JAVA1) SecTest
	$(JAVA1) ThreadTest
	@echo --- Listener Adapter Test ---
	$(JAVA) ScriptingTest4 test4.js
	$(JAVA) ScriptingTest4 test4.esp
	@echo --- Listener Adapter in sandbox ---
	$(JAVA) ScriptingTest4 --sandbox test4.js
	$(JAVA) ScriptingTest4 --sandbox test4.esp
	@echo --- Listener Adapter in scripting sandbox ---
	$(JAVA) ScriptingTest4 --scriptSandbox test4.js
	$(JAVA) ScriptingTest4 --scriptSandbox test4.esp
	echo ---- Now try code that will fail in a sandbox ---
	$(JAVA) ScriptingTest4 --sandbox test5.js || echo ... EXCEPTION OK
	$(JAVA) ScriptingTest4 --sandbox test5.esp || echo ... EXCEPTION OK
	$(JAVA) ScriptingTest4 --scriptSandbox test5.js \
		||  echo ... EXCEPTION OK
	$(JAVA) ScriptingTest4 --scriptSandbox test5.esp \
		||  echo ... EXCEPTION OK
	$(JAVA) ScriptingTest4 --trusted test5.js \
		||  echo ... EXCEPTION NOT EXPECTED
	$(JAVA) ScriptingTest4 --trusted test5.esp \
		||  echo ... EXCEPTION NOT EXPECTED
	rm test5.out
	@echo --- ESP test using javax.script API ---
	$(JAVA) EPTest
	@echo --- import test for ECMAScript
	$(JAVA) ImportTest itest.js
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

eoltest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac  -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) EOLTest
clean:
	rm -rf classes
