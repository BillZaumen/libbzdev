CWD = $(shell pwd | sed -e s/\\//\\\\\\//g )
JAVA = java -classpath ../../BUILD/libbzdev.jar:classes
JAVA2 = java -classpath ../../BUILD/libbzdev.jar:classes \
	-Djava.security.policy=../../BUILD/libbzdev.policy
JAVA3 = java -classpath ../../BUILD/libbzdev.jar:classes.jar \
	-Djava.security.policy=scpolicy.policy
JAVAM = java -p ../../BUILD  \
	--add-modules org.bzdev.desktop	-classpath classes

ARG1 =
ARG2 =

JFILES = $(shell ls *.java | grep -v DebugTransferTest.java)

all:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	sed -e 's/CWD/$(CWD)/' < toc.xml.tpl > toc.xml
	$(JAVA) DRTest
	$(JAVA) SimpJTextPaneTest
	$(JAVA) HtmlWithTocTest toc.xml
	$(JAVA) ObjTocPaneTest
	$(JAVA) UrlTocPaneTest
	$(JAVA) TimeTextFieldTest
	$(JAVA) ButtonTest
	$(JAVA) ProxyDialogTest
	$(JAVA) AuthPaneTest $(ARG1) $(ARG2)
	$(JAVA) URLTextAreaPaneTest
	$(JAVA) DebugTransferTest
	$(JAVA) DebugTransferTest2
	$(JAVA) ErrorMessageTest
	$(JAVA) ErrorMessageTest2
	$(JAVA) PanelGraphicsTest
	$(JAVA) PanelGraphicsTest2
	$(JAVA) SwingOpsTest
	make ctest ctest1 ctest1f ctest2 ctest3 ctest3sa ctest3sb
	make ctest3a ctest3b ctest3aa ctest3ab ctest3ba ctest3bb
	make ctest4 ctest4a ctest4b ctest4c
	make atest atest2 atest2f atest3 atest3s atest3a atest3b

compile:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java


PROG = AuthPaneTest

scpolicy.policy:
	echo \
	"grant codebase \"file:`cd ../..;pwd`/BUILD/libbzdev-base.jar\" {" \
	> scpolicy.policy
	echo "      permission java.security.AllPermission;" >> scpolicy.policy
	echo "};" >> scpolicy.policy
	echo \
	"grant codebase \"file:`cd ../..;pwd`/BUILD/libbzdev-desktop.jar\" {" \
	> scpolicy.policy
	echo "      permission java.security.AllPermission;" >> scpolicy.policy
	echo "};" >> scpolicy.policy
	echo \
	"grant codebase \"file:`cd ../..;pwd`/BUILD/libbzdev-graphics.jar\" {" \
	>> scpolicy.policy
	echo "      permission java.security.AllPermission;" >> scpolicy.policy
	echo "};" >> scpolicy.policy
	echo "grant codebase \"file:`pwd`/classes.jar\" {" >> scpolicy.policy
	echo "      permission org.bzdev.swing.ExitPermission" \
	>> scpolicy.policy
	echo "                 \"org.bzdev.swing.SimpleConsole\";" \
	>> scpolicy.policy
	echo "      permission org.bzdev.swing.ExitPermission" \
	>> scpolicy.policy
	echo "                 \"org.bzdev.swing.PanelGraphics\";" \
	>> scpolicy.policy
	echo "      permission org.bzdev.swing.ExitPermission" \
	>> scpolicy.policy
	echo "                 \"org.bzdev.swing.AnimatedPanelGraphics\";" \
	>> scpolicy.policy
	echo "};" >> scpolicy.policy

classes.jar: classes/*.class
	( cd classes ; jar cf ../classes.jar . )

config:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	$(JAVA) ConfigTest

config2:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	$(JAVA) ConfigTest config.foo

btest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	$(JAVA) ButtonTest

mtest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	$(JAVA) TextFieldMenuTest

ptest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	$(JAVA) PortTextFieldTest


htest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	$(JAVA) HtmlPaneTest testfile.html

htest2:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	jar cf manual.jar html
	$(JAVA) HtmlPaneTest2 file:`pwd`/html/manual.html

htest2a:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	jar cf manual.jar html
	$(JAVA) HtmlPaneTest2 jar:file:`pwd`/manual.jar!/html/manual.html

htest2b:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	jar cf manual.jar html
	$(JAVA) HtmlPaneTest2  jar:file:`pwd`/manual.jar!/html/manual2.html

htest2c:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	jar cf manual.jar html
	$(JAVA) -Dorg.bzdev.protocols.resource.path=`pwd`/manual.jar \
		HtmlPaneTest2  resource:html/manual2.html

htest2d:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	jar cf manual.jar html
	$(JAVA) HtmlPaneTest2 file:`pwd`/../../../epts/manual/manual.html

EPTS = ../../../epts/jar/epts.jar
htest2e:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	jar cf manual.jar html
	$(JAVA) -Dorg.bzdev.protocols.resource.path=$(EPTS) \
		HtmlPaneTest2 resource:org/bzdev/epts/manual/manual.html

htest2e-dm:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	jar cf manual.jar html
	$(JAVA) -Dorg.bzdev.protocols.resource.path=$(EPTS) \
		HtmlPaneTest2 resource:org/bzdev/epts/manual/manual.dm.html


htest2h:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	jar cf manual.jar html
	$(JAVA) HtmlPaneTest2 http://localhost:8080/manual.html

htest3:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	jar cf manual.jar html
	$(JAVA) HtmlPaneTest3 file:`pwd`/html/manual.html

otoc:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	$(JAVA) ObjTocPaneTest $(BG)


inputtable:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	$(JAVA) InputTablePaneTest

inputtable2:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	$(JAVA) InputTablePaneTest2

inputtable3:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	$(JAVA) InputTablePaneTest3

inputtable4:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	$(JAVA) InputTablePaneTest4

frametests:
	make ctest ctest1 ctest1f ctest2 ctest3 ctest3sa ctest3sb
	make ctest3a ctest3b ctest3aa ctest3ab ctest3ba ctest3bb
	make ctest4 ctest4a ctest4b ctest4c
	make atest atest2 atest2f atest3 atest3s atest3a atest3b


test:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	sed -e 's/CWD/$(CWD)/' < toc.xml.tpl > toc.xml
	$(JAVA) $(PROG) $(ARG1) $(ARG2)


colorchooser:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar \
		ColorChooserTest.java
	$(JAVA) ColorChooserTest

tptest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar \
		SimpJTextPaneTest.java
	$(JAVA) SimpJTextPaneTest

ctest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar \
		SimpleConsoleTest.java
	$(JAVA2) SimpleConsoleTest

ctest1:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar \
		SimpleConsoleTest1.java
	$(JAVA2) SimpleConsoleTest1

ctest1f:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar \
		SimpleConsoleTest1.java
	$(JAVA2) SimpleConsoleTest1 --flush

ctest2:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar \
		SimpleConsoleTest2.java
	$(JAVA2) SimpleConsoleTest2

sctest: ctest3 ctest3sa ctest3sb ctest3a ctest3b \
	ctest3aa ctest3ab ctest3ba ctest3bb

ctest3:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar \
		SimpleConsoleTest3.java
	$(JAVA2) SimpleConsoleTest3

ctest3sa:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar \
		SimpleConsoleTest3.java
	$(JAVA2) SimpleConsoleTest3 --smAfter

ctest3sb:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar \
		SimpleConsoleTest3.java
	$(JAVA2) SimpleConsoleTest3 --smBefore

ctest3a:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar \
		SimpleConsoleTest3.java
	$(JAVA2) SimpleConsoleTest3 --ask

ctest3b:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar \
		SimpleConsoleTest3.java
	$(JAVA2) SimpleConsoleTest3 --always

ctest3aa:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar \
		SimpleConsoleTest3.java
	$(JAVA2) SimpleConsoleTest3 --ask --smAfter

ctest3ab:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar \
		SimpleConsoleTest3.java
	$(JAVA2) SimpleConsoleTest3 --ask --smBefore

ctest3ba:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar \
		SimpleConsoleTest3.java
	$(JAVA2) SimpleConsoleTest3 --always --smAfter

ctest3bb:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar \
		SimpleConsoleTest3.java
	$(JAVA2) SimpleConsoleTest3 --always --smBefore


ctest4:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar \
		SimpleConsoleTest4.java
	$(JAVA2) SimpleConsoleTest4

ctest4a:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar \
		SimpleConsoleTest4.java
	$(JAVA2) SimpleConsoleTest4 --smAfter

ctest4b:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar \
		SimpleConsoleTest4.java
	$(JAVA2) SimpleConsoleTest4 --smBefore

ctest4c: scpolicy.policy
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar \
		SimpleConsoleTest4.java
	make classes.jar
	$(JAVA3) SimpleConsoleTest4 --smBefore


atest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar \
		AnimPanelTest.java Ball.java HParms.java
	$(JAVA) -Djava.security.policy=../../BUILD/libbzdev.policy AnimPanelTest


atest-dm:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar \
		AnimPanelTest.java Ball.java HParms.java
	$(JAVA) -Djava.security.policy=../../BUILD/libbzdev.policy \
		AnimPanelTest --systemUI

atestm:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar \
		AnimPanelTest.java Ball.java HParms.java
	$(JAVAM) -Djava.security.policy=../../BUILD/libbzdev.policy \
		AnimPanelTest

atest2:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar \
		AnimPanelTest2.java Ball.java HParms.java
	$(JAVA) AnimPanelTest2

atest2f:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar \
		AnimPanelTest2.java Ball.java HParms.java
	$(JAVA) -Djava.security.policy=../../BUILD/libbzdev.policy \
		AnimPanelTest2 atest2.isq

atest3: scpolicy.policy
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar \
		AnimPanelTest3.java
	make classes.jar
	$(JAVA3) AnimPanelTest3 --false
	$(JAVA3) AnimPanelTest3 --true
	$(JAVA3) AnimPanelTest3 --accessor
	$(JAVA3) AnimPanelTest3 --false --before
	$(JAVA3) AnimPanelTest3 --true --before
	$(JAVA3) AnimPanelTest3 --accessor --before
	$(JAVA3) AnimPanelTest3 --accessor --after

atest3s:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar \
		AnimPanelTest3.java
	$(JAVA2) AnimPanelTest3 --false
	$(JAVA2) AnimPanelTest3 --true
	$(JAVA2) AnimPanelTest3 --accessor
	$(JAVA2) AnimPanelTest3 --false --before
	$(JAVA2) AnimPanelTest3 --true --before
	$(JAVA2) AnimPanelTest3 --accessor --before
	$(JAVA2) AnimPanelTest3 --accessor --after

hwt:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	sed -e 's/CWD/$(CWD)/' < toc.xml.tpl > toc.xml
	$(JAVA) HtmlWithTocTest toc.xml --no-timeout

hwt2:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar  *.java
	sed -e 's/CWD/$(CWD)/' < toc.xml.tpl > toc.xml
	$(JAVA) HtmlWithTocTest toc.xml --no-timeout --systemUI

geth:
	(cd ../.. ; make jars)
	java -classpath ../../BUILD/libbzdev.jar:../../../geth/geth.jar	\
		HttpHeaders

atest3s-fb:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar \
		AnimPanelTest3.java
	$(JAVA2) AnimPanelTest3 --false --before
	echo ---------
	$(JAVA3) AnimPanelTest3 --false --before


atest3a: scpolicy.policy
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar \
		AnimPanelTest3.java
	make classes.jar
	$(JAVA3) AnimPanelTest3 --false --after
	$(JAVA3) AnimPanelTest3 --true --after
	$(JAVA3) AnimPanelTest3 --accessor --after


atest3b: scpolicy.policy
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar \
		AnimPanelTest3.java
	make classes.jar
	$(JAVA3) AnimPanelTest3 --false --before
	$(JAVA3) AnimPanelTest3 --true --before
	$(JAVA3) AnimPanelTest3 --accessor --before

dtest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar \
		DebugTransferTest.java
	java -classpath ../../BUILD/libbzdev.jar:classes DebugTransferTest

dtest2:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar \
		DebugTransferTest2.java
	java -classpath ../../BUILD/libbzdev.jar:classes DebugTransferTest2

etest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar \
		ErrorMessageTest.java
	java -classpath ../../BUILD/libbzdev.jar:classes ErrorMessageTest


etest2:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar \
		ErrorMessageTest2.java
	java -classpath ../../BUILD/libbzdev.jar:classes ErrorMessageTest2

pgtest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar \
		PanelGraphicsTest.java
	java -classpath ../../BUILD/libbzdev.jar:classes PanelGraphicsTest

pgtest2:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar \
		PanelGraphicsTest2.java
	java -classpath ../../BUILD/libbzdev.jar:classes PanelGraphicsTest2


#
# This rule is not used in the 'all' case because it is interactive: the
# program will not terminate until the user closes it.
#
pgtest3:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar \
		PanelGraphicsTest3.java
	java -classpath ../../BUILD/libbzdev.jar:classes PanelGraphicsTest3

pgtest3-dm:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar \
		PanelGraphicsTest3.java
	java -classpath ../../BUILD/libbzdev.jar:classes PanelGraphicsTest3 \
		--systemUI

pgtest4:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar \
		PanelGraphicsTest4.java
	java -classpath ../../BUILD/libbzdev.jar:classes PanelGraphicsTest4

pgtest5:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar \
		PanelGraphicsTest5.java
	$(JAVA)  -Djava.security.policy=../../BUILD/libbzdev.policy \
		PanelGraphicsTest5

pgtest6: scpolicy.policy
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar \
		PanelGraphicsTest6.java
	make classes.jar
	$(JAVA3) PanelGraphicsTest6 --none
	$(JAVA3) PanelGraphicsTest6 --boolean
	$(JAVA3) PanelGraphicsTest6 --accessor
	$(JAVA3) PanelGraphicsTest6 --none --before
	$(JAVA3) PanelGraphicsTest6 --boolean --before
	$(JAVA3) PanelGraphicsTest6 --accessor --before
	$(JAVA3) PanelGraphicsTest6 --none --after
	$(JAVA3) PanelGraphicsTest6 --boolean --after
	$(JAVA3) PanelGraphicsTest6 --accessor --after

bgtest6a: scpolicy.policy
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar \
		PanelGraphicsTest6.java
	make classes.jar
	$(JAVA3) PanelGraphicsTest6 --boolean --before
	$(JAVA3) PanelGraphicsTest6 --accessor --before
	$(JAVA3) PanelGraphicsTest6 --boolean --after


vtf:
	(cd ../.. ; make jars)
	mkdir -p classes;
	javac -d classes -classpath ../../BUILD/libbzdev.jar \
		VTextFieldTest.java
	$(JAVA) VTextFieldTest

vtf2:
	(cd ../.. ; make jars)
	mkdir -p classes;
	javac -d classes -classpath ../../BUILD/libbzdev.jar \
		VTextFieldTest2.java
	$(JAVA) VTextFieldTest2

wtf:
	(cd ../.. ; make jars)
	mkdir -p classes;
	javac -d classes -classpath ../../BUILD/libbzdev.jar \
		WTextFieldTest.java
	$(JAVA) WTextFieldTest

ptf:
	(cd ../.. ; make jars)
	mkdir -p classes;
	javac -d classes -classpath ../../BUILD/libbzdev.jar \
		PTextFieldTest.java
	$(JAVA) PTextFieldTest

ops:
	(cd ../.. ; make jars)
	mkdir -p classes;
	javac -d classes -classpath ../../BUILD/libbzdev.jar \
		SwingOpsTest.java
	$(JAVA) SwingOpsTest

clean:
	rm -rf classes toc.xml scpolicy.policy manual.jar
