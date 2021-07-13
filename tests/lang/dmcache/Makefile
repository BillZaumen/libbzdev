# JAVA = java -classpath ../../../BUILD/lib/libbzdev.jar:classes:

JAVA = java -p ../../../BUILD -cp classes --add-modules org.bzdev.base

#JAVAC = javac -d classes -s tmpsrc \
#		-classpath ../../../BUILD/lib/libbzdev.jar:classes

JAVAC = javac -p ../../../BUILD --add-modules org.bzdev \
		--processor-path ../../../BUILD/libbzdev.jar \
		-classpath classes \
		-s tmpsrc \
		-d classes

JAVAC2 = javac -p ../../../BUILD --add-modules org.bzdev \
		--processor-path ../../../BUILD/libbzdev-base.jar:../../../BUILD/libbzdev-dmethods.jar \
		-classpath classes:../../../BUILD/libbzdev.jar \
		-s tmpsrc \
		-d classes


all:
	(cd ../../.. ; make)
	rm -rf classes tmpsrc
	mkdir -p classes tmpsrc
	$(JAVAC) *.java
	$(JAVA) Test1
	@echo --------------
	$(JAVA) Test2
	@echo -------------------------------
	@echo Test with no locking:
	rm -r classes tmpsrc
	mkdir -p classes tmpsrc
	$(JAVAC) -Aorg.bzdev.lang.annotations.DMethodOptions.lockingMode=NONE \
		 *.java
	$(JAVA) Test1
	@echo ---------------
	$(JAVA) Test2
	@echo -------------------------------
	@echo Test using read-write locks:
	rm -r classes tmpsrc
	mkdir -p classes tmpsrc
	$(JAVAC) \
		-Aorg.bzdev.lang.annotations.DMethodOptions.lockingMode=RWLOCK \
		*.java
	$(JAVA) Test1
	@echo ---------------
	$(JAVA) Test2

ctest:
	rm -rf classes tmpsrc
	mkdir -p classes
	javac --processor-module-path ../../../BUILD \
		-p ../../../BUILD --add-modules org.bzdev \
		-d classes \
		Test1.java

ctest2:
	rm -rf classes tmpsrc
	mkdir -p classes tmpsrc
	javac -p ../../../BUILD --add-modules org.bzdev \
		--processor-path ../../../BUILD/libbzdev.jar \
		-classpath classes \
		-s tmpsrc \
		-d classes \
		Test1.java

ctest3:
	rm -rf classes tmpsrc
	mkdir -p classes tmpsrc
	javac  	-p ../../../BUILD --add-modules org.bzdev \
		--processor-path ../../../BUILD/libbzdev.jar \
		-classpath classes:../../../BUILD/libbzdev.jar \
		-s tmpsrc \
		-d classes \
		Test1.java

ctest4:
	rm -rf classes tmpsrc
	mkdir -p classes tmpsrc
	javac  -Aorg.bzdev.lang.annotations.DMethodOptions.lockingMode=NONE \
		-classpath classes:../../../BUILD/libbzdev.jar \
		-s tmpsrc \
		-d classes \
		Test1.java

clean:
	rm -rf classes tmpsrc
