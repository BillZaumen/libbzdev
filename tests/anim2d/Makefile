JAVA = java  -classpath ../../BUILD/libbzdev.jar:classes

SCRUN = java -classpath ../../BUILD/scrunner.jar: \
     org.bzdev.bin.scrunner.SCRunnerCmd "$$@"

HAS_JS = java -p ../../BUILD -m org.bzdev.scrunner --supportsLanguage ECMAScript

all:
	(cd ../.. ; make jars)
	@echo ----- Factory-listing test ----
	java -classpath ../../BUILD/libbzdev.jar:../../BUILD/lsnof.jar \
	    -Djava.security.policy=../../BUILD/libbzdev.policy \
	    org.bzdev.bin.lsnof.FactoryPrinter \
	org.bzdev.anim2d.AnimationPath2DFactory
	@echo ----- compile test programs ----
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo -------- ATest ------------
	@$(JAVA) ATest
	@echo ------- PropTest ----------
	@$(JAVA) PropTest
	@echo -------- GraphView ------------
	@$(JAVA) GraphViewTest
	@echo -------- GraphView1 ------------
	@$(JAVA) GraphViewTest1
	@echo -------- RotationTest ---------
	@$(JAVA) RotationTest
	@echo -------- RotationTest2 ---------
	@$(JAVA) RotationTest2
	@echo -------- KineticObject2D ------------
	@$(JAVA) KinObj2DTest
	@echo ------- CircPath ------
	@$(JAVA) -Djava.security.policy=../../BUILD/libbzdev.policy \
		 CircPathTest
	@echo ------- BackgroundTest ------
	@$(JAVA) BackgroundTest
	@echo ------- RelativeTest --------
	@$(JAVA) RelativeTest
	@echo ------- RelativeTest2 --------
	@$(JAVA) RelativeTest2
	@echo ------- RelativeTest3--------
	@$(JAVA) RelativeTest3
	@echo -------- FactoryTest ---------
	@$(JAVA) FactoryTest
	@echo ------- KinematicOpsTest --------
	@$(JAVA) KinematicOpsTest
	@echo --------- GridTest -----------
	@$(JAVA) GridTest
	@echo --------- PolarTest -----------
	@$(JAVA) PolarTest
	@echo ----- Starting Script Factory Test ----
	$(HAS_JS) && $(SCRUN) scriptTest.js || echo ... skipping scriptTest.js
	@echo ----- Starting Script Factory Test with an ESP script ----
	$(SCRUN) scriptTest.esp
	@echo ----- Starting 2nd Script Factory Test with an ESP script ----
	$(SCRUN) --resourcePath images scriptTest2.esp

atest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@$(JAVA) ATest

listf:
	java -classpath ../../BUILD/libbzdev.jar:../../BUILD/lsnof.jar \
	    -Djava.security.policy=../../BUILD/libbzdev.policy \
	    -Djava.class.loader=org.bzdev.lang.DMClassLoader \
	    org.bzdev.bin.lsnof.FactoryPrinter \
	    'org.bzdev.anim2d.*'

circ:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@$(JAVA) -Djava.security.policy=../../BUILD/libbzdev.policy \
		 CircPathTest

rotation:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@$(JAVA) RotationTest

rotation2:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@$(JAVA) RotationTest2


kinematic:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@$(JAVA) KinematicOpsTest

relative:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@$(JAVA) RelativeTest

relative2:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@$(JAVA) RelativeTest2

relative3:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@$(JAVA) RelativeTest3

directed: 
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@$(JAVA) KinObj2DTest

directed1:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@$(JAVA) Directed1

background:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@$(JAVA) BackgroundTest

background2:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@$(JAVA) BackgroundTest2

background3:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@$(JAVA) BackgroundTest3

circpath:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@$(JAVA) -Djava.security.policy=../../BUILD/libbzdev.policy \
		CircPathTest

grid:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@$(JAVA) GridTest

polar:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@$(JAVA) PolarTest


graphview:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@$(JAVA) GraphViewTest

graphview1:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@$(JAVA) GraphViewTest1

factory:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ---- starting FactoryTest -----
	@time $(JAVA) FactoryTest

launcher:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ---- starting LauncherTest -----
	@time $(JAVA) LauncherTest

launcher1:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ---- starting LauncherTest -----
	@time $(JAVA) LauncherTest1

launcher2:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ---- starting LauncherTest -----
	@time $(JAVA) LauncherTest2


print:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo STARTING PRINT TESTS
	@echo ---- BackgroundTest print
	@$(JAVA) BackgroundTest print
	@echo ---- CircTest print
	@$(JAVA) CircPathTest print
	@echo ---- GraphViewTest print
	@$(JAVA) BackgroundTest print

sfactory:
	(cd ../.. ; make jars)
	mkdir -p classes
	@echo ----- Starting Script Factory Test ----
	$(HAS_JS) && $(SCRUN) scriptTest.js || echo ... skipping scriptTest.js

eptest:
	(cd ../.. ; make jars)
	mkdir -p classes
	@echo ----- Starting Script Factory Test with EP script ----
	$(SCRUN) scriptTest.esp

eptest-err:
	(cd ../.. ; make jars)
	mkdir -p classes
	@echo ----- Starting Script Factory Test with ESP script ----
	$(SCRUN) scriptTestErr.esp

eptest2:
	(cd ../.. ; make jars)
	mkdir -p classes
	@echo ----- Starting Script Factory Test with ESDP script ----
	$(SCRUN) --resourcePath images scriptTest2.esp

cltest2:
	(cd ../.. ; make jars)
	mkdir -p classes
	@echo ----- Checking ConnectingLine2D from script ----
	$(SCRUN) scriptTest3.esp

cltest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar CLTest.java
	$(JAVA) CLTest

clean:
	rm -rf classes
	for i in tmp ?tmp ?tmp? datmp rottmp rottmp2 gvt1 b3tmp ; \
		do [ -d $$i ] && rm -f $$i/*.png ; done
	rm -f test.isq
