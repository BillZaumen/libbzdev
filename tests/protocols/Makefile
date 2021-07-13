JAVA = java -classpath ../../BUILD/libbzdev.jar:test.jar \
	-Djava.security.policy=`pwd`/test.policy

JAVA2 = java -p ../../BUILD --add-modules org.bzdev -classpath test.jar \
	-Djava.security.policy=`pwd`/test.policy


all:
	(cd ../.. ; make jars)
	mkdir -p classes
	cat ../../BUILD/libbzdev.policy > test.policy
	echo grant codebase '"file:'`pwd`/test.jar'" {permission org.bzdev.protocols.HandlersEnablePermission "org.bzdev.protocols.enable";};' >> test.policy
	mkdir -p testdir
	echo hello > classes/hello.txt
	echo goodbye > testdir/goodbye.txt
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	jar cf test.jar -C classes .
	@echo ------- Test -------
	$(JAVA) Test
	@echo ------- Repeat Test using Modules -------
	$(JAVA2) Test


clean:
	rm -rf classes testdir test.jar test.policy
