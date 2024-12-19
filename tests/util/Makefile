# JAVA = java -classpath ../../BUILD/lib/libbzdev.jar:classes
JAVA = java -p ../../BUILD -classpath classes --add-modules org.bzdev \
	-Djava.security.policy=../../BUILD/libbzdev.policy

HAS_JS = java -p ../../BUILD -m org.bzdev.scrunner --supportsLanguage ECMAScript

all: compile
	@echo -------- ClassTest ---------
	@$(JAVA) ClassTest
	@echo -------- TemplateProcessorTest ---------
	@$(JAVA) TemplateProcessorTest
	@echo -------- TemplateProcessorTest with useKeyMap ---------
	@$(JAVA) TemplateProcessorTest useKeyMap
	@echo -------- TemplateProcessorTest with useKeyMapList ---------
	@$(JAVA) TemplateProcessorTest useKeyMapList
	@echo -------- StaticRandomTest ---------
	$(JAVA) StaticRandomTest -m
	@echo -------- SetTest ---------
	@$(JAVA) SetTest
	@echo -------- CollectionScannerTest ---------
	@$(JAVA) CollectionScannerTest
	@echo -------- EncapIteratorTest ---------
	@$(JAVA) EncapIteratorTest
	@echo -------- SciFormatterTest --------
	@$(JAVA) SciFormatterTest
	@echo -------- SciFormatter Script Test ----------
	chmod u+x scrun
	@$(HAS_JS) && ./scrun SciFormatterTest.js || echo -n
	@./scrun SciFormatterTest.esp
	chmod u-x scrun
	@echo -------- SafeFormatterTest --------
	@$(JAVA) SafeFormatterTest
	@echo --------- Clone Test --------
	@$(JAVA) CloneTest
	@echo --------- Copy Test --------
	@$(JAVA) CopyTest
	(cd rv; make)
	(cd units; make)
	@echo --------- PoissonTable Test ----------
	@$(JAVA) PTableTest
	@echo ----- Enumeration/Iterator Test ------
	@$(JAVA) IETest
	@echo ----- PrimArrayTest -----
	@$(JAVA) PrimArrayTest
	@echo ----- SuffixArrayTest ------
	@$(JAVA) SuffixArrayTest
	@echo ----- UnsignedSATest ------
	@$(JAVA) UnsignedSATest
	@echo ----- LcplrTest ------
	@$(JAVA) LcplrTest
	@echo ----- LcplrTiming ------
	@$(JAVA) LcplrTest
	@echo ------ VarFormatter Test ----
	@$(JAVA) VarFormatterTest
	@echo ------- JavaIdents Test -------
	@$(JAVA) JavaIdentsTest
	@echo ----- EventList Test -------
	@$(JAVA) EventListTest
	@echo ----- JSTest ------
	@$(JAVA) JSTest
	@echo ----- ExpressionParser Test -------
	@$(JAVA) ExpressionTest
	@echo ----- ExpressionParser Script Test -------
	@$(JAVA) ExprScriptTest
	@echo ----- ExprScriptTestE  -------
	@$(JAVA) ExprScriptTestE
	@echo ----- ... now using the ESPEngine constructor ... -----
	@$(JAVA) ExprScriptTestE engine
	@echo ----- ExpressionParser Script Test '(sandboxed)' -------
	@$(JAVA) ExprScriptTest -s
	@echo ----- ExpressionParser Test '(multithreading)'  -------
	@$(JAVA) ExprScriptTest2
	@echo ----- ExpressionParser Test '(lexical scoping)' -------
	@$(JAVA) ExprScriptTest3
	@echo ----- ExpressionParser Test '(issues from EPTS)' -------
	@$(JAVA) ExprScriptTest4
	@echo ----- ExpressionParser Test '(va list)' -------
	@$(JAVA) ExprScriptTest5
	@echo ----- ExpressionParser Test '(var.identifier)' -------
	@$(JAVA) ExprScriptTest6
	@echo ----- ExpressionParser Test '(second var.identifier)' -------
	@$(JAVA) ExprScriptTest7
	@echo ----- ExpressionParser Test '(functional interface test)' -------
	@$(JAVA) ExprScriptTest8
	@echo - ExpressionParser Test '(functional interface, method ref test)' -
	@$(JAVA) ExprScriptTest9
	@echo ---- ExpressionParser Test '(class for an array)' ----
	@$(JAVA) ExprScriptTest11
	@echo - ExpressionParser Test '(implicit casting)' -
	@$(JAVA) ExprScriptTest12 | cmp - test12.expected || \
		( echo failed && exit 1)
	@echo - ExpressionParser Test '(use of Optional methods)' -
	@$(JAVA) ExprScriptTest13
	@$(JAVA) ExprScriptTest13 test13a.html
	cmp test13.html test13a.html || ( echo failed && exit 1 )
	@echo - ExpressionParser Test '( == in arguments)' -
	@$(JAVA) ExprScriptTest14
	@echo - ExpressionParser Test '( .class.METHOD)' -
	@$(JAVA) ExprScriptTest15
	@echo - ExpressionParser Test '( .foreach())' -
	@$(JAVA) ExprScriptTest16
	@echo ---- try some ESP runtime errors
	chmod u+x scrun
	./scrun bad1.esp || echo EXCEPTION EXPECTED
	./scrun bad2.esp || echo EXCEPTION EXPECTED
	./scrun bad3.esp || echo EXCEPTION EXPECTED
	./scrun bad4.esp || echo EXCEPTION EXPECTED
	./scrun bad5.esp || echo EXCEPTION EXPECTED
	./scrun bad6.esp || echo EXCEPTION EXPECTED
	./scrun nulltest.esp
	./scrun not.esp
	@echo --- docsig test ---
	./scrun --codebase docsig.jar --exit docsig.esp
	./scrun -p docsig.jar --exit docsig.esp
	chmod u-x scrun
	@echo ---- Supplier Test ----
	@$(JAVA) SupplierTest
	@echo ----- UniTreeNode Test -------
	@$(JAVA) UniTreeNodeTest
	@echo --- check generateDocs '(with blockConstructor)' -------
	chmod u+x scrun
	@./scrun checkPrintWriter.esp > testPW.html
	chmod u-x scrun
	@echo --- shared-mime-info test ---
	@$(JAVA) SMITest
	@echo --- run configProp test interactively '(make configProp)' ---

actest: compile
	$(JAVA) ACTest

not: compile
	chmod u+x scrun
	./scrun not.esp
	./scrun not2.esp
	chmod u-x scrun

backquote: compile
	chmod u+x scrun
	./scrun --stackTrace backquote.esp
	chmod u-x scrun

throw: compile
	chmod u+x scrun
	./scrun  throw.esp
	chmod u-x scrun


test7-sc: compile
	chmod u+x scrun
	@./scrun test7-sc.esp
	chmod u-x scrun

compile:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -p ../../BUILD  --add-modules org.bzdev *.java

badesp:
	(cd ../.. ; make jars)
	chmod u+x scrun
	./scrun bad1.esp || echo EXCEPTION EXPECTED
	./scrun bad2.esp || echo EXCEPTION EXPECTED
	./scrun  bad3.esp || echo EXCEPTION EXPECTED
	./scrun  bad4.esp || echo EXCEPTION EXPECTED
	./scrun  bad5.esp || echo EXCEPTION EXPECTED
	./scrun  bad6.esp || echo EXCEPTION EXPECTED
	./scrun bad7.esp || echo EXCEPTION EXPECTED
	./scrun  espResource.esp || echo EXCEPTION EXPECTED
	chmod u-x scrun

badesp2:
	(cd ../.. ; make jars)
	chmod u+x scrun2
	./scrun2 espResource.esp || echo EXCEPTION EXPECTED
	chmod u-x scrun2

bad6:
	(cd ../.. ; make jars)
	chmod u+x scrun
	./scrun  bad6.esp || echo EXCEPTION EXPECTED
	chmod u-x scrun

bad5:
	(cd ../.. ; make jars)
	chmod u+x scrun
	./scrun --stackTrace bad5.esp
	chmod u-x scrun

ctest: compile
	@$(JAVA) ClassTest

patest: compile
	@$(JAVA) PrimArrayTest

jstest: compile
	@$(JAVA) JSTest

ytest: compile
	@echo ---- YTest -----
	@$(JAVA) YTest


lcplr: compile
	@$(JAVA) LcplrTest

lcptiming: compile
	@$(JAVA) LcplrTiming


satest: compile
	@$(JAVA) SuffixArrayTest

satest2: compile
	@$(JAVA) SuffixArrayTest2

samtest: compile
	@$(JAVA) SAMergeTest

unsigned: compile
	@$(JAVA) UnsignedSATest

idents: compile
	@$(JAVA) JavaIdentsTest


ietest: compile
	@$(JAVA) IETest

ptabletest: compile
	@$(JAVA) PTableTest

clonetest: compile
	$(JAVA) CloneTest

sctest:
	(cd ../.. ; make)
	chmod u+x scrun
	@$(HAS_JS) && ./scrun --stackTrace SciFormatterTest.js \
	|| echo ... skipping SciFormatterTest.js
	chmod u-x scrun

sctest-esp:
	(cd ../.. ; make)
	chmod u+x scrun
	@./scrun --stackTrace SciFormatterTest.esp
	chmod u-x scrun

scftest: compile
	echo ------ SciFormatterTest -----
	$(JAVA) SciFormatterTest

vartest: compile
	@echo ------ VarFormatter Test ----
	@$(JAVA) VarFormatterTest

ttest: compile
	@echo --- template test ---
	@$(JAVA) TemplateProcessorTest

tbad: compile
	@echo --- bad template test ---
	@$(JAVA) TemplateProcessorTest2

ical: compile
	@echo ---- ICalBuilder Test ------
	@$(JAVA) ICalBuilderTest

elist: compile
	@echo ----- EventList Test -------
	@$(JAVA) EventListTest

otest: compile
	@echo ----- ObjectParser Test -------
	@$(JAVA) OTest

etests: compile
	@echo ----- ExpressionParser Test -------
	@$(JAVA) ExpressionTest
	@echo ----- ExpressionParser Test '(lexical scoping)' -------
	@$(JAVA) ExprScriptTest3
	@echo ----- ExpressionParser Test '(issues from EPTS)' -------
	@$(JAVA) ExprScriptTest4
	@echo ----- ExpressionParser Test '(va list)' -------
	@$(JAVA) ExprScriptTest5
	@echo ----- ExpressionParser Test '(var.identifier)' -------
	@$(JAVA) ExprScriptTest6
	@echo ----- ExpressionParser Test '(second var.identifier)' -------
	@$(JAVA) ExprScriptTest7
	@echo ----- ExpressionParser Test '(functional interface test)' -------
	@$(JAVA) ExprScriptTest8
	@echo - ExpressionParser Test '(functional interface, method ref test)' -
	@$(JAVA) ExprScriptTest9
	@echo ---- ExpressionParser Test '(class for an array)' ----
	@$(JAVA) ExprScriptTest11
	@echo - ExpressionParser Test '(implicit casting)' -
	@$(JAVA) ExprScriptTest12 | cmp - test12.expected || \
		( echo failed && exit 1)
	@echo - ExpressionParser Test '(use of Optional methods)' -
	@$(JAVA) ExprScriptTest13
	@$(JAVA) ExprScriptTest13 test13a.html
	cmp test13.html test13a.html || ( echo failed && exit 1 )
	@echo - ExpressionParser Test '( == in arguments)' -
	@$(JAVA) ExprScriptTest14
	@echo - ExpressionParser Test '( .class.METHOD)' -
	@$(JAVA) ExprScriptTest15
	@echo - ExpressionParser Test '( .foreach())' -
	@$(JAVA) ExprScriptTest16
	@echo - ExpressionParser Test '( <=> operator)' -
	@$(JAVA) ExprScriptTest17
	@echo - ExpressionParser Test '( ` operator)' -
	@$(JAVA) ExprScriptTest18
	chmod u+x scrun
	@$(HAS_JS) && ./scrun SciFormatterTest.js || echo -n
	@./scrun SciFormatterTest.esp
	./scrun not.esp
	./scrun test7-sc.esp
	./scrun --stackTrace bad5.esp  || echo EXCEPTION EXPECTED
	./scrun  bad6.esp || echo EXCEPTION EXPECTED
	@./scrun --stackTrace SciFormatterTest.esp
	chmod u-x scrun

etest: compile
	@echo ----- ExpressionParser Test -------
	@$(JAVA) ExpressionTest

estest: compile
	@echo ----- ExpressionParser Test '(script)' -------
	@$(JAVA) ExprScriptTest

estestE: compile
	@echo ----- ExprScriptTestE  -------
	@$(JAVA) ExprScriptTestE
	@echo ----- ... now using the ESPEngine constructor ... -----
	@$(JAVA) ExprScriptTestE engine

estest-s: compile
	@echo ----- ExpressionParser Test '(sandboxed script)' -------
	@$(JAVA) ExprScriptTest -s

estest1: compile
	@echo ---- use of fully-qualified fields
	@$(JAVA) ExprScriptTest1

estest2: compile
	@echo ----- ExpressionParser Test '(multithreading)'  -------
	@$(JAVA) ExprScriptTest2

estest3: compile
	@echo ----- ExpressionParser Test '(lexical scoping)' -------
	@$(JAVA) ExprScriptTest3

estest4: compile
	@echo ----- ExpressionParser Test '(issues from EPTS)' -------
	@$(JAVA) ExprScriptTest4

estest5: compile
	@echo ----- ExpressionParser Test '(va list)' -------
	@$(JAVA) ExprScriptTest5

estest6: compile
	@echo ----- ExpressionParser Test '(var.identifier)' -------
	@$(JAVA) ExprScriptTest6

estest7: compile
	@echo ----- ExpressionParser Test '(second var.identifier)' -------
	@$(JAVA) ExprScriptTest7

estest8: compile
	@echo ----- ExpressionParser Test '(functional interface test)' -------
	@$(JAVA) ExprScriptTest8

estest9: compile
	@echo - ExpressionParser Test '(functional interface, method ref test)' -
	@$(JAVA) ExprScriptTest9

estest10: compile
	@echo - ExpressionParser Test '(functional interface, method ref test)' -
	@$(JAVA) ExprScriptTest10

estest11: compile
	@echo - ExpressionParser Test '(class for an array)' -
	@$(JAVA) ExprScriptTest11

estest12: compile
	@echo - ExpressionParser Test '(implicit casting)' -
	@$(JAVA) ExprScriptTest12

estest13: compile
	@echo - ExpressionParser Test '(use of Optional methods)' -
	@$(JAVA) ExprScriptTest13

estest13a: compile
	@echo - ExpressionParser Test '(use of Optional methods)' -
	@$(JAVA) ExprScriptTest13 test13a.html

estest14: compile
	@echo - ExpressionParser Test '( == in arguments)' -
	@$(JAVA) ExprScriptTest14

estest15: compile
	@echo - ExpressionParser Test '( .class.METHOD)' -
	@$(JAVA) ExprScriptTest15

estest16: compile
	@echo - ExpressionParser Test '( stream test)' -
	@$(JAVA) ExprScriptTest16

estest17: compile
	@echo - ExpressionParser Test '( <=> operator)' -
	@$(JAVA) ExprScriptTest17

estest18: compile
	@echo - ExpressionParser Test '( ` operator)' -
	@$(JAVA) ExprScriptTest18

unitree: compile
	@echo ----- UniTreeNode Test -------
	@$(JAVA) UniTreeNodeTest

emsgtest: compile
	@echo ---- Exception  Message Test ----
	@$(JAVA) EOLTest

supplier: compile
	@echo ---- Supplier Test ----
	@$(JAVA) SupplierTest

blockTest:
	@ (cd ../.. ; make jars)
	@ chmod u+x scrun
	./scrun checkPrintWriter.esp > testPW.html
	@chmod u-x scrun

smi: compile
	@echo --- shared-mime-info test ---
	@$(JAVA) SMITest


configProp: compile ../swing/config.foo
	$(JAVA) ConfigPropTest

../swing/config.foo:
	@echo Save a configuration in the file config.foo
	@echo (this is run in ../swing).  You will need a
	@echo GPG key.
	(cd ../swing; make config)

docsig:
	@ (cd ../.. ; make jars)
	java -p ../../BUILD \
		-m org.bzdev.scrunner/org.bzdev.bin.scrunner.SCRunnerCmd \
		--stackTrace -p docsig.jar docsig.esp

docsig-dryrun:
	@ (cd ../.. ; make jars)
	java -p ../../BUILD \
		-m org.bzdev.scrunner/org.bzdev.bin.scrunner.SCRunnerCmd \
		--dryrun \
		--stackTrace --codebase docsig.jar docsig.esp

nulltest:
	@ (cd ../.. ; make jars)
	java -p ../../BUILD:`pwd`/docsig.jar \
		-m org.bzdev.scrunner/org.bzdev.bin.scrunner.SCRunnerCmd \
		--stackTrace  nulltest.esp
ytest0: compile
	@echo ---- YTest0 -----
	@$(JAVA) YTest0

clean:
	rm -rf classes
	(cd rv ; make clean)
	(cd units ; make clean)
	rm -f sequence.txt
	rm -f test.html testPW.html test13.html test13a.html minfo.xml
	@chmod u-x scrun
