all:
	(cd .. ; make libs)
	mkdir -p tmpsrc classes
	javac -s tmpsrc -d classes -classpath ../../BUILD/libbzdev.jar \
		 *.java
	jar cmf multi.mf ../multi.jar -C classes .
	rm tmpsrc/*
	rm classes/*
	rmdir tmpsrc classes

clean:
	rm -fr tmpsrc classes
