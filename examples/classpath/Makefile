
# compile the test program
compile:
	mkdir -p classes
	mkdir -p tmpsrc
	javac -d classes -classpath /usr/share/bzdev/libbzdev.jar \
		-s tmpsrc pkg/*.java
	jar --create --file foo.jar --main-class pkg.Test \
		-C classes .

run:
	java -classpath foo.jar:/usr/share/bzdev/libbzdev.jar pkg.Test

clean:
	rm -f foo.jar
	rm -rf classes tmpsrc
