JAVA = java -p ../../../BUILD -classpath classes --add-modules org.bzdev

all:
	(cd ../../.. ; make jars)
	mkdir -p classes
	javac -p ../../../BUILD -classpath classes \
		--add-modules org.bzdev \
		-d classes *.java
	@echo ---- Conversions to MKS units -----
	$(JAVA) MKSTest

clean:
	rm -rf classes
