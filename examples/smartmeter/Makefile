all:
	(cd .. ; make libs)
	mkdir -p tmpsrc classes
	javac -s tmpsrc -d classes -classpath ../../BUILD/libbzdev.jar \
		 *.java
	cp *.properties classes
	cp -r META-INF classes
	jar cmf smartmeter.mf ../smartmeter.jar -C classes .
#	rm tmpsrc/*
#	rm classes/*
#	rmdir tmpsrc classes

clean:
	rm -rf tmpsrc classes
