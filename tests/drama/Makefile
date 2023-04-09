JAVA = java -classpath ../../BUILD/libbzdev.jar:classes

HAS_JS = java -p ../../BUILD -m org.bzdev.scrunner --supportsLanguage ECMAScript

all:
	(cd ../.. ; make jars)
	mkdir -p classes tmpsrc
	javac -d classes -classpath ../../BUILD/libbzdev.jar \
		-s tmpsrc  *.java
	@echo Trying Test:
	@$(JAVA) Test
	@echo Trying STraceTest:
	@$(JAVA) STraceTest
	@echo Trying FactoryTest:
	@$(JAVA) FactoryTest
	@echo Trying DomainTest:
	@$(JAVA) DomainTest
	@echo Trying DomainTest2:
	@$(JAVA) DomainTest2
	@echo Trying CommDomainTest --traceFilters
	$(JAVA) CommDomainTest --traceFilters
	@echo Trying CommDomainTest --traceDelays
	$(JAVA) CommDomainTest --traceDelays
	@echo --- FactoryTest implemented using scripts ---
	mkdir -p classes/META-INF/services
	echo TestActorFactory > \
		classes/META-INF/services/org.bzdev.obnaming.NamedObjectFactory
	chmod u+x scrun
	@echo ---- test.js ------
	@$(HAS_JS) && ./scrun --classpathCodebase classes/ \
		--stackTrace test.js || echo ... skipping test.js
	@echo ---- test.esp ------
	@./scrun --classpathCodebase classes/ --stackTrace test.esp
	chmod u-x scrun
	@echo ---- modtest ----
	(cd modtest; make run)

test:
	(cd ../.. ; make jars)
	mkdir -p classes tmpsrc
	javac -d classes -classpath ../../BUILD/libbzdev.jar \
		-s tmpsrc  *.java
	@echo Trying Test:
	@$(JAVA) Test

cdtest:
	(cd ../.. ; make jars)
	mkdir -p classes tmpsrc
	javac -d classes -classpath ../../BUILD/libbzdev.jar \
		-s tmpsrc  *.java
	@echo Trying CommDomainTest --traceFilters
	$(JAVA) CommDomainTest --traceFilters
	@echo Trying CommDomainTest --traceDelays
	$(JAVA) CommDomainTest --traceDelays

esptest:
	(cd ../.. ; make jars)
	mkdir -p classes tmpsrc
	javac -d classes -classpath ../../BUILD/libbzdev.jar \
		-s tmpsrc  *.java
	@echo ---- test.esp ------
	chmod u+x scrun
	@./scrun --classpathCodebase classes/ --stackTrace test.esp
	chmod u-x scrun

esptest2:
	(cd ../.. ; make jars)
	mkdir -p classes tmpsrc
	javac -d classes -classpath ../../BUILD/libbzdev.jar \
		-s tmpsrc  *.java
	@echo ---- test2.esp ------
	chmod u+x scrun
	@./scrun --classpathCodebase classes/ --stackTrace test2.esp
	chmod u-x scrun

testsim:
	(cd ../.. ; make jars)
	mkdir -p classes tmpsrc
	javac -d classes -classpath ../../BUILD/libbzdev.jar \
		-s tmpsrc  *.java
	@echo --- testsim.esp -----
	chmod u+x scrun
	@./scrun --classpathCodebase classes/ --stackTrace testsim.esp
	chmod u-x scrun



domain2:
	(cd ../.. ; make jars)
	mkdir -p classes tmpsrc
	javac -d classes -classpath ../../BUILD/libbzdev.jar \
		-s tmpsrc  *.java
	@echo Trying DomainTest2:
	@$(JAVA) DomainTest2

parent:
	(cd ../.. ; make jars)
	mkdir -p classes tmpsrc
	javac -d classes -classpath ../../BUILD/libbzdev.jar \
		-s tmpsrc  *.java
	@echo Trying ParentTest:
	@$(JAVA) ParentTest

clean:
	rm -rf classes tmpsrc
