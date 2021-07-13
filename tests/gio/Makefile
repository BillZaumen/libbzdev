BATIK = $(shell [ -f /usr/share/java/batik.jar ] && \
	echo ":/usr/share/java/batik.jar" || \
	echo "")

JAVA = java  -Djava.security.policy=../../BUILD/libbzdev.policy \
	  -classpath \
		../../BUILD/libbzdev.jar:classes:spitest/foo.jar$(BATIK)

JAVA2 = java  -Djava.security.policy=../../BUILD/libbzdev.policy \
	  -classpath ../../BUILD/libbzdev.jar:classes

all:
	(cd ../.. ; make jars)
	mkdir -p classes
	(cd spitest; make)
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	echo java command starts with: $(JAVA)
	@echo ----- "OSGTest (type list)" -----
	@$(JAVA) OSGTest
	@echo -------- OSGTest 1 ------------
	@$(JAVA) OSGTest jpg
	@echo -------- OSGTest 2 ------------
	@$(JAVA) OSGTest ps
	@echo -------- OSGTest 3 -----------
	@$(JAVA) OSGTest foo
	@echo -------- OSGTest 4 ------------
	@$(JAVA) OSGTest jpg NORMAL
	@$(JAVA) OSGTest png NORMAL
	@echo -------- OSGTest 5 ------------
	@$(JAVA) OSGTest jpg CLOCKWISE90
	@$(JAVA) OSGTest png CLOCKWISE90
	@echo -------- OSGTest 6 ------------
	@$(JAVA) OSGTest jpg COUNTERCLOCKWISE90
	@$(JAVA) OSGTest png COUNTERCLOCKWISE90
	@echo -------- OSGTest 7 ------------
	@$(JAVA) OSGTest ps CLOCKWISE90
	@echo -------- OSGTest 8 ------------
	@$(JAVA) OSGTest ps COUNTERCLOCKWISE90
	@echo ------- OSGTest 9 ----------------
	@( $(JAVA2)  OSGTest svg && (echo Exception not seen; exit 1) ) \
		|| echo Exception Expected
	@echo ------- OSGTest 10 ----------------
	[ -f /usr/share/java/batik.jar ] && $(JAVA) OSGTest svg NORMAL \
		|| echo ... skipping
	@echo ------- OSGTest 10cw ----------------
	[ -f /usr/share/java/batik.jar ] && $(JAVA) OSGTest svg	\
		CLOCKWISE90 || echo ... skipping
	@echo ------- OSGTest 10ccw ----------------
	[ -f /usr/share/java/batik.jar ] && $(JAVA) OSGTest svg \
		COUNTERCLOCKWISE90 || echo ... skipping
	@echo ------- OSGTest 11 ----------------
	@$(JAVA) OSGTest svgz || echo ... skipping
	@echo -------- ISWTest --------------
	@$(JAVA) ISWTest
	@echo -------- SGTest ---------------
	@$(JAVA) SGTest


sgtest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo -------- SGTest ---------------
	@$(JAVA) SGTest


ptest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------- PTest------
	@$(JAVA) PrinterTest

ptest2:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------- PTest------
	@$(JAVA) PrinterTest2

clean:
	rm -rf classes
	rm -f *.jpg *.svg *.svgz *.isq *.foo *.ps
	(cd spitest; make clean)
