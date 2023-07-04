#JAVA = java -Djava.security.policy=test.policy \
#	-classpath ../../BUILD/lib/libbzdev.jar:classes:ltest.jar

JAVA = java -Djava.security.policy=test.policy \
	-p ../../BUILD -cp classes --add-modules org.bzdev.base

JAVA_ESP = java -Dscript.language=ESP -Djava.security.policy=test.policy \
	-p ../../BUILD -cp classes --add-modules org.bzdev.base


JAVA2 = java -Djava.security.policy=test.policy \
	-p ../../BUILD -cp classes:ltest.jar \
	--add-modules org.bzdev.base

JAVAC = javac -p ../../BUILD --add-modules org.bzdev  \
		--processor-path ../../BUILD/libbzdev.jar \
		-classpath classes

JAVAC2 = javac -p ../../BUILD --add-modules org.bzdev

JFILES1 = AbstractFactory.java ATestNamer.java ATestObject1.java \
	ATestObject2Factory.java ATestObject2GF.java ATestObject2.java \
	ATestObject3.java ATestObject4.java ATestObject5.java \
	ATestObject6.java  ATestObject.java ATestObjectSC.java \
	TestFactory.java Test.java TestNamer.java TestObject1.java \
	TestObject2.java TestObject3.java TestObject4.java \
	TestObject5.java TestObject6.java TestObject.java 

JFILES2 = ListTest.java LTest.java

JFILES3 = DefaultTest.java

JFILES4 = JSTest.java


LSNOF = java -Djava.security.policy=../../BUILD/libbzdev.policy \
	  -classpath ../../BUILD/libbzdev.jar:../../BUILD/lsnof.jar \
	   org.bzdev.bin.lsnof.FactoryPrinter

all:
	(cd ../.. ; make jars)	
	(cd misc; make)
	mkdir -p classes tmpsrc
	$(JAVAC) -d classes -s tmpsrc \
		-Xlint:unchecked $(JFILES1) testpkg/*.java
	cp Tip.properties classes
	cp Label.properties classes
	cp Doc.properties classes
	cp ParmSetTips.properties classes
	cp ParmSetLabels.properties classes
	cp ParmSetDocs.properties classes
	(cd ltest; make)
	$(JAVAC2) -d classes -classpath ltest.jar:classes \
		-Xlint:unchecked $(JFILES2)
	$(JAVAC) -d classes LT.java
	$(JAVA2) LT
	for i in default anim2d drama devqsim ; \
	do for j in p3d desktop graphics math stats rv rvfactories ; \
		do $(JAVA2) LT $$i $$j ; done; \
	done
	$(JAVA) Test
	$(JAVA_ESP) Test
	$(JAVA) testpkg.Test
	$(LSNOF) --codebase "classes|ltest.jar" LTestFactory
	$(LSNOF) --codebase "classes|ltest.jar" --html LTestFactory > ltf.html
	$(LSNOF) org.bzdev.drama.DomainFactory
	$(LSNOF) --html org.bzdev.drama.DomainFactory > test.html
	$(LSNOF) --definingClassHTML org.bzdev.drama.DomainFactory \
		> deftest.html
	$(LSNOF) --definingClass org.bzdev.drama.DomainFactory > deftest.txt
	$(JAVA2) LTest
	$(LSNOF) --list --codebase "classes|ltest.jar"
	$(LSNOF) --list --codebase "classes|ltest.jar" '*'
	$(LSNOF) --list --codebase "classes|ltest/classes" '*'
	$(LSNOF) --list --codebase "classes|ltest/classes" '**'
	$(LSNOF) --list --codebase "classes|ltest/classes" 'org.bzdev.**'
	javac -d classes \
		-classpath classes:../../BUILD/libbzdev.jar:ltest.jar \
		-Xlint:unchecked $(JFILES3)
	@echo ---- DefaultTest ----
	$(JAVA) DefaultTest


lt:
	(cd ../.. ; make jars)
	$(JAVAC) -d classes LT.java
	@echo $(JAVA2) LT
	$(JAVA2) LT
	for i in default anim2d drama devqsim ; \
	do for j in p3d desktop graphics math stats rv rvfactories ; \
		do $(JAVA2) LT $$i $$j ; done; \
	done

esptest:
	(cd ../.. ; make jars)
	$(JAVAC) -d classes -s tmpsrc \
		-Xlint:unchecked $(JFILES1) testpkg/*.java
	$(JAVA_ESP) Test

jstest:
	(cd ../.. ; make jars)
	$(JAVAC2) -d classes -classpath classes \
		-Xlint:unchecked $(JFILES4) $(JFILES3)
	$(JAVA) JSTest

list-test:
	(cd ../.. ; make jars)
	$(JAVAC2) -d classes -classpath classes:ltest.jar \
		-Xlint:unchecked $(JFILES2)
	$(JAVA) ListTest org.bzdev.anim2d.GraphViewFactory

clean:
	rm -rf tmpsrc classes ext
	( cd misc; make clean )
	( cd ltest; make clean )
