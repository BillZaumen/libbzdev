HAS_JS_TEST = \
	`java -Dscrunner.usrconf=test.conf -p ../../BUILD \
		-m org.bzdev.scrunner \
		--supportsLanguage ECMAScript`

all:
	( cd ../.. ; make )
	java -Dscrunner.usrconf=test.conf -p ../../BUILD -m org.bzdev.scrunner \
		--supportsLanguage ECMAScript && echo ECMAScript supported \
		|| ECMAScript not supported
	java -Dscrunner.usrconf=test.conf -p ../../BUILD -m org.bzdev.scrunner \
		--supportsLanguage ESP && echo ESP supported
	java -Dscrunner.usrconf=test.conf -p ../../BUILD -m org.bzdev.scrunner \
		--supportsLanguage FOO || \
		echo scripting language FOO not supported as expected
	cp test.js "test 1.js"
	if $(HAS_JS_TEST) ; then \
	java -p ../../BUILD -m org.bzdev.scrunner -Dfoo=bar \
	     --codebase ../../BUILD/lsnof.jar "test 1.js" ; fi
	rm "test 1.js"
	java -p ../../BUILD -m org.bzdev.scrunner \
		--listScriptingLanguages
	if $(HAS_JS_TEST) ; then \
	java -Dscrunner.usrconf=test.conf -p ../../BUILD -m org.bzdev.scrunner \
		-J-server -J-Xmn10M \
		--codebase ../../BUILD/lsnof.jar "test.js" ; fi
	java -Dscrunner.usrconf=test.conf -p ../../BUILD -m org.bzdev.scrunner \
		-J-server -J-Xmn10M \
		--codebase ../../BUILD/lsnof.jar "test.esp"
	java -Dscrunner.usrconf=test.conf -p ../../BUILD -m org.bzdev.scrunner \
		-J-server -J-Xmn10M \
		--codebase .../lsnof.jar --dryrun "test.js"
	if $(HAS_JS_TEST) ; then \
	java -Dscrunner.usrconf=test.conf -p ../../BUILD -m org.bzdev.scrunner \
		-J-server -J-Xmn10M --trustLevel=2 \
		--add-modules org.bzdev.lsnof \
		--codebase .../lsnof.jar   "test10.js" ; fi
	java -Dscrunner.usrconf=test.conf -p ../../BUILD -m org.bzdev.scrunner \
		-J-server -J-Xmn10M --trustLevel=2 \
		--add-modules org.bzdev.lsnof \
		--codebase .../lsnof.jar   "test10.esp"
	java -Dscrunner.usrconf=test.conf -p ../../BUILD -m org.bzdev.scrunner \
		--listScriptingLanguages
	@echo --- list an scrunner codebase ---
	java -Dscrunner.usrconf=test.conf -p ../../BUILD -m org.bzdev.scrunner \
		--listCodeBase --codebase ../../BUILD/lsnof.jar
	@echo --- list an scrunner codebase with a URL that cannot be opened ---
	java -Dscrunner.usrconf=test.conf -p ../../BUILD -m org.bzdev.scrunner \
		--listCodeBase --codebase ../../BUILD/lsnof1234.jar \
	|| echo ... Failure Expected
	@echo ---- list some factories ----
	chmod u+x ./run1
	 ./run1 --list --codebase ../../BUILD/scrunner.jar
	@echo --- test command-line options
	chmod u+x ./run
	./run -vS:foo abc -vS:bar:def
	if $(HAS_JS_TEST) ; then \
	./run -vS:foo abc printfoo.js ; fi
	./run -vS:foo abc printfoo.esp
	if $(HAS_JS_TEST) ; then \
	./run -vD:foo 10 printfoo.js ; \
	./run -vD:foo:20 printfoo.js ; \
	./run -vI:foo 10 printfoo.js ; \
	./run -vI:foo:10 printfoo.js ; \
	./run -vL:foo 10 printfoo.js ; \
	./run -vL:foo:10 printfoo.js ; \
	./run -vB:foo:true printfoo.js ; \
	./run -vB:foo true printfoo.js ; fi
	./run -vD:foo 10 printfoo.esp
	./run -vD:foo:20 printfoo.esp
	./run -vI:foo 10 printfoo.esp
	./run -vI:foo:10 printfoo.esp
	./run -vL:foo 10 printfoo.esp
	./run -vL:foo:10 printfoo.esp
	./run -vB:foo:true printfoo.esp
	./run -vB:foo true printfoo.esp
	@echo ---- actually run programs ----
	./run --listScriptingLanguages
	./run --listScriptingLanguages ECMAScript
	if $(HAS_JS_TEST) ; then \
	./run test2.js ; fi
	./run test2.esp
	if $(HAS_JS_TEST) ; then \
	./run test.js ; fi
	./run test.esp
	if $(HAS_JS_TEST) ; then \
	./run --trustLevel=1 test.js ; \
	./run --trustLevel=2 test.js ; fi
	./run --trustLevel=1 test.esp
	./run --trustLevel=2 test.esp
	@echo ----  trust-level 1 with two tests
	cp test.js "test 1.js"
	if $(HAS_JS_TEST) ; then \
	./run --trustLevel=1 test.js -t "test 1.js" ; fi
	@echo ---- check use of -t
	./run --trustLevel=1 -- -t test.js test.js
	./run --trustLevel=1 -- -t test.esp testA.esp
	@echo hello > junk
	if $(HAS_JS_TEST) ; then \
	./run -i:is junk test3.js ; fi
	./run -i:is junk test3.esp
	@echo ---- check random access file
	if $(HAS_JS_TEST) ; then \
	./run -rw:raf junk test4.js ; fi
	rm junk
	@echo ---- check directory accessor
	mkdir junk
	if $(HAS_JS_TEST) ; then \
	./run -d:da junk test5.js ; fi
	rm -r "test 1.js" junk
	@echo ---- check exception handling
	if $(HAS_JS_TEST) ; then \
	./run --stackTrace test6.js || echo EXCEPTION EXPECTED ; fi
	./run --stackTrace test6.esp || echo EXCEPTION EXPECTED
	@echo --- second exception-handling test
	@echo Generate an error and show that we can print something
	@echo on the error stream before the normal error handling.
	if $(HAS_JS_TEST) ; then \
	./run test7.js > /dev/null || echo EXCEPTION EXPECTED ; fi
	@echo --- third exception-handling test
	@echo Repeat first exception handling test but without a stacktrace
	if $(HAS_JS_TEST) ; then \
	./run test6.js || echo EXCEPTION EXPECTED ; fi
	./run test6.esp || echo EXCEPTION EXPECTED
	@echo throw a javascript exception containing the number 100.0
	if $(HAS_JS_TEST) ; then \
	./run test8.js || echo EXCEPTION EXPECTED ; fi
	./run test8.esp || echo EXCEPTION EXPECTED
	@echo run a script with an uninitialized variable to generate an error
	if $(HAS_JS_TEST) ; then \
	./run test9.js || echo EXCEPTION EXPECTED ; fi
	./run test9.esp || echo EXCEPTION EXPECTED
	@echo print versions of jar files
	./run --versions
	@echo run test11.js to see if scripting.importClass works
	if $(HAS_JS_TEST) ; then \
	./run test11.js ; fi
	./run test11.esp
	@echo run test12.js to see if scripting.importClass works
	if $(HAS_JS_TEST) ; then \
	./run test12.js ; fi
	./run test12.esp
	@echo run test13.js to check createFactory with three args
	if $(HAS_JS_TEST) ; then \
	./run test13.js ; fi
	./run test13.esp
	@echo run test1 4.js to check createFactory with three args
	if $(HAS_JS_TEST) ; then \
	./run test14.js || ./run --stackTrace test14.js ; fi
	./run test14.esp || ./run --stackTrace test14.esp
	@echo test2 with bad codebase
	if $(HAS_JS_TEST) ; then \
	./run --codebase .../foo.xyz test2.js || echo "Exception Expected" ; fi
	./run --codebase .../foo.xyz test2.esp || echo "Exception Expected"
	@echo test2 with no codebase
	if $(HAS_JS_TEST) ; then \
	./run test2.js ; fi
	./run test2.esp
	if $(HAS_JS_TEST) ; then \
	echo  now try running test.es ; \
	./run test.es ; fi
	@echo now try running test.esp
	./run test.esp
	@echo now try running test14.esp
	./run test14.esp
	@echo try ESP on a file that will cause an error
	./run err.esp || echo "Exception Expected"
	@echo try --dryrun to check --resourcePath
	./run --dryrun --resourcePath ../swing'|'../math test10.esp
	chmod u-x ./run ./run1
	@echo try a userconf file whose entries include  non-existent files
	if $(HAS_JS_TEST) ; then \
	java -Dscrunner.usrconf=test2.conf -p ../../BUILD \
		-m org.bzdev.scrunner \
		--codebase .../lsnof.jar "test.js" ;fi
	java -Dscrunner.usrconf=test2.conf -p ../../BUILD \
		-m org.bzdev.scrunner \
		--codebase .../lsnof.jar "test.esp"
	@echo ----- yrunner tests -----
	make ytest

rpath:
	( cd ../.. ; make )
	chmod u+x ./run ./run1
	./run --dryrun --resourcePath ../swing'|'../math test10.esp
	chmod u-x ./run ./run1

test10-dry:
	java -Dscrunner.usrconf=test.conf -p ../../BUILD -m org.bzdev.scrunner \
		-J-server -J-Xmn10M --trustLevel=2  \
		--codebase .../lsnof.jar  --dryrun "test10.js"

test10:
	java -Dscrunner.usrconf=test.conf -p ../../BUILD -m org.bzdev.scrunner \
		-J-server -J-Xmn10M --trustLevel=2  \
		--add-modules org.bzdev.lsnof \
		--codebase .../lsnof.jar "test10.js"

test10-esp:
	java -Dscrunner.usrconf=test.conf -p ../../BUILD -m org.bzdev.scrunner \
		-J-server -J-Xmn10M --trustLevel=2  \
		--add-modules org.bzdev.lsnof \
		--codebase .../lsnof.jar "test10.esp"


test13:
	( cd ../.. ; make )
	chmod u+x ./run
	@echo run test13.js to check createFactory with three args
	./run test13.js
	chmod u-x ./run

test14:
	( cd ../.. ; make )
	chmod u+x ./run
	@echo run test14.js to check createFactories
	./run test14.js
	chmod u-x ./run

eptest14:
	( cd ../.. ; make )
	chmod u+x ./run
	@echo run test14.esp to check createFactories
	./run --stackTrace test14.esp
	chmod u-x ./run

test:
	( cd ../.. ; make )
	chmod u+x ./run
	@echo run test0.js '(to check that we can use ECMASCript)'
	time ./run -Dnashorn.args=--no-deprecation-warning test0.js
	./run test0.esp
	chmod u-x ./run

stest:
	( cd ../.. ; make )
	chmod u+x ./run
	chmod u+x test.esp
	@echo try running test.esp as an executable script.
	./test.esp
	./test.esp -vD:z:20
	./test.esp -vD:z:20 test.esp
	./run test.esp -vD:z:20 test.esp
	chmod u-x test.esp
	chmod u-x ./run


eptest:
	( cd ../.. ; make )
	chmod u+x ./run
	@echo run test.esp '(to check that we can use EP)'
	time ./run test.esp
	chmod u-x ./run

conftest:
	( cd ../.. ; make )
	@echo try a userconf file whose entries include  non-existent files
	java -classpath \
		../../BUILD/libbzdev.jar:../../BUILD/scrunner.jar \
		-Dscrunner.usrconf=test2.conf \
		org.bzdev.bin.scrunner.SCRunnerCmd  \
		--codebase .../lsnof.jar --dryrun "test.js"

ytest:
	(cd ../.. ; make )
	@chmod u+x ./yrun
	./yrun --listLaunchers
	@echo ------- test1h.yaml ---------
	./yrun --stackTrace drama,graphics test1h.yaml
	@echo ------- test1g.yaml ---------
	./yrun -o:gout:tmp.png drama,graphics test1g.yaml
	@echo -------- test1.yaml -------
	./yrun -o:output:test1.isq  --stackTrace anim2d test1.yaml
	@echo -------- test3.yaml -------
	./yrun -o:output:test3.isq  --stackTrace anim2d,math test3.yaml
	@echo -------- test1e.yaml -------
	./yrun -o:output:test1e.isq  anim2d test1e.yaml \
		|| echo EXCEPTION EXPECTED
	@echo --------- test1f.yaml --------
	./yrun anim2d test1f.yaml
	./yrun -o test1f.out -o:err:test1f.eout anim2d test1f.yaml
	cat test1f.out test1f.eout
	rm test1f.out test1f.eout
	@echo ---- test using multiple YAML files ----
	./yrun --stackTrace anim2d testS.yaml testM.yaml testE.yaml
	@chmod u-x ./yrun


ytest-rd:
	(cd ../.. ; make )
	@chmod u+x ./yrun
	./yrun --listLaunchers --codebase ../../../roadanim/BUILD
	@echo -----------
	./yrun  --codebase ../../../roadanim/BUILD  --generateDocs \
		--stackTrace anim2d,rdanim || echo FAILED

	@chmod u-x ./yrun

ytest-math:
	(cd ../.. ; make )
	@chmod u+x ./yrun
	./yrun --listLaunchers --codebase ../../../roadanim/BUILD
	@echo -----------
	./yrun  --codebase ../../../roadanim/BUILD  --generateDocs \
		--stackTrace anim2d,math || echo FAILED
	@chmod u-x ./yrun

otest:
	(cd ../.. ; make )
	mkdir -p classes
	javac -d classes OutTest.java
	jar cf otest.jar -C classes OutTest.class
	chmod u+x ./run
	./run --classpathCodebase otest.jar otest.esp
	chmod u-x ./run
	rm -r classes otest.jar

sctest:
	(cd ../.. ; make )
	mkdir -p classes
	javac -d classes OutTest.java
	jar cf otest.jar -C classes OutTest.class
	chmod u+x ./run
	./run --stackTrace sctest.esp
	chmod u-x ./run
	rm -r classes otest.jar

ecirc:
	(cd ../.. ; make )
	chmod u+x ./run
	chmod u+x ecirc.sh
	./ecirc.sh 10 20
	chmod u-x ecirc.sh
	chmod u+x ecirc2
	./ecirc2 --dryrun 10 20
	./ecirc2  10 20
	chmod u-x ecirc2
	chmod u-x ./run

clean:
	rm -fr junk raf tmp.png
	chmod u-x ./run ./run1
