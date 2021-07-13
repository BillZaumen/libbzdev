all:
	(cd .. ; make libs)
	mkdir -p tmpsrc classes
	javac -s tmpsrc -d classes -classpath ../../BUILD/libbzdev.jar \
		 *.java
	jar cmf drama1.mf ../drama1.jar -C classes .
	jar cmf drama2.mf ../drama2.jar -C classes .
#	rm tmpsrc/*
#	rm classes/*
#	rmdir tmpsrc classes

clean:
	rm -rf tmpsrc classes
