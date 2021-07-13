# Remove DMClassLoader - in Java 11, -Djava.syste.clas.loader=...
# results in an error.
#
# JAVA = java -classpath ../../../../BUILD/lib/libbzdev.jar:classes \
#	-Djava.system.class.loader=org.bzdev.lang.DMClassLoader

JAVA = java -classpath ../../../../BUILD/libbzdev.jar:classes \

JAVAC = javac -p ../../../../BUILD --add-modules org.bzdev \
		--processor-path ../../../../BUILD/libbzdev.jar

all:
	(cd ../../../.. ; make)
	mkdir -p tmpsrc classes
	$(JAVAC) -s tmpsrc -d classes *.java
	$(JAVA) SomeVisitor

#	rm *.class
#	rm tmpsrc/*
#	rmdir tmpsrc


clean:
	rm -rf tmpsrc classes
