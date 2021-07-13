JAVA = java -classpath ../../BUILD/libbzdev.jar:classes

all:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------ ImageMIMEInfoTest -----------
	@$(JAVA) ImageMIMEInfoTest
	@echo ------ ObserverTest -----------
	@$(JAVA) ObserverTest

clean:
	rm -rf classes obtest.png
