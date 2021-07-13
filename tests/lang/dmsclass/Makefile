JAVA = java -classpath ../../../BUILD/libbzdev.jar:classes

#JAVAC = javac -d classes -s tmpsrc \
#		-classpath ../../../BUILD/lib/libbzdev.jar:classes

JAVAC = javac -p ../../../BUILD --add-modules org.bzdev \
		--processor-path ../../../BUILD/libbzdev.jar \
		-classpath classes \
		-s tmpsrc \
		-d classes

all:
	(cd ../../.. ; make)
	mkdir -p classes tmpsrc
	$(JAVAC) *.java
	$(JAVA) STest
#	rm tmpsrc/*
#	rm classes/*
#	rmdir tmpsrc classes

clean:
	rm -rf classes tmpsrc
