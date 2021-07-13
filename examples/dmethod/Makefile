all:
	(cd .. ; make libs)
	mkdir -p tmpsrc classes
	javac -d classes -s tmpsrc \
		-classpath ../../BUILD/libbzdev.jar *.java
	jar cmf dmethod.mf ../dmethod.jar -C classes .
	rm -f tmpsrc/*
	rm -f classes/*
	rmdir tmpsrc classes

clean:
	rm -fr tmpsrc classes
