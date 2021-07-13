JAVAC = javac -p ../../../BUILD --add-modules org.bzdev \
		--processor-path ../../../BUILD/libbzdev.jar \
		-classpath ../classes:classes

all:
	( cd ../../.. ; make jars)
	mkdir -p classes
	mkdir -p tmpsrc
	$(JAVAC) -d classes -s tmpsrc *.java
	cp LTestTips.properties classes
	cp LTestLabels.properties classes
	cp LTestDocs.properties classes
	mkdir -p classes/META-INF/services
	echo LTestFactory > \
		classes/META-INF/services/org.bzdev.obnaming.NamedObjectFactory
	jar cf ../ltest.jar -C classes .

clean:
	rm -rf classes tmpsrc ../ltest.jar
