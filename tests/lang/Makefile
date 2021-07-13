JAVA  = java -classpath ../../BUILD/libbzdev.jar:classes \
	-Djava.security.policy=../../BUILD/libbzdev.policy

JAVA2 = java -classpath ../../BUILD/libbzdev.jar:classes \
	-Djava.security.policy=dtsm.policy

all: singletest dtsmtest
	(cd dmcache ; make)
	(cd dmsclass ; make)
	(cd dmtests ; make)
	(cd multi ; make)

singletest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) DebugSMTest 2>&1 && echo Exit Status = 0
	$(JAVA) DebugSMTest sm 2>&1 || echo Exit Status = $$?
	$(JAVA) DebugSMTest tsm 2>&1 || echo Exit Status = $$?
	$(JAVA) DebugSMTest tsm 2 2>&1 || echo Exit Status = $$?
	$(JAVA) CallTest
	$(JAVA) ClassSorterTest ../../BUILD/libbzdev.jar
	$(JAVA) MathOpsTest

calltest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) CallTest

dtsmtest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	cat /dev/null > dtsm.policy
	echo grant codebase \"file:`cd ../../BUILD; pwd`/libbzdev-base.jar\" '{' \
		>> dtsm.policy
	echo '    ' permission java.security.AllPermission';' >>dtsm.policy
	echo '};' >> dtsm.policy
	echo grant codebase \"file:`pwd`/classes/\" '{' \
		>> dtsm.policy
	echo '    ' permission org.bzdev.lang.DesignatedThreadsPermission';' \
		>> dtsm.policy
	echo '};' >> dtsm.policy
	if $(JAVA) DTSMTest ; then echo No exception seen; exit 1 ; \
	else echo '(Security exception expected)'; fi
	$(JAVA2) DTSMTest

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
