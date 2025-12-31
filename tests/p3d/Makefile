JAVA = java -Djava.security.policy=../../BUILD/libbzdev.policy \
	-classpath ../../BUILD/libbzdev.jar:classes

JAVA2 = java -Djava.security.policy=../../BUILD/libbzdev.policy \
	 -p ../../BUILD --add-modules org.bzdev -classpath classes

AVCONV = $(shell  which avconv > /dev/null && echo avconv || echo ffmpeg)

all:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ----- LimitTest --------
	@$(JAVA) LimitTest
	@echo ----- CylinderTest -----
	@$(JAVA) CylinderTest
	@echo ----- TwoCylinderTest -----
	@$(JAVA) TwoCylinderTest
	@echo ----- HollowCylinderTest -----
	@$(JAVA) HollowCylinderTest
	@echo ----- HollowCylinderTest2 -----
	@$(JAVA) HollowCylinderTest2
	@echo -------- MTest ---------
	@$(JAVA) MTest true
	@$(JAVA) MTest false
	@echo -------- BTest ---------
	@$(JAVA) BTest
	@echo -------- BTest1 --------
	@$(JAVA) BTest1
	@echo -------- TTest ----------
	@$(JAVA) TTest
	@$(JAVA) TTest1
	@$(JAVA) TTest2
	@$(JAVA) TTest3
	@$(JAVA) TTest4
	@$(JAVA) TTest5
	@echo -------- OpsTest --------
	@$(JAVA) OpsTest
	@echo ------- AnimTest4 -------
	@$(JAVA) AnimTest4
	$(AVCONV) -y -f image2 -r 30 -i ltmp/img-%03d.png -r 30 animtest4.webm
	@echo -------- AnimTest -------
	@$(JAVA) AnimTest
	$(AVCONV) -y -f image2 -r 30 -i ltmp/img-%03d.png -r 30 animtest.webm
	@echo ------- AnimTest2 -------
	@$(JAVA) AnimTest2
	$(AVCONV) -y -f image2 -r 30 -i ltmp/img-%03d.png -r 30 animtest2.webm
	@echo ------- AnimTest3 -------
	@$(JAVA) AnimTest3
	$(AVCONV) -y -f image2 -r 30 -i ltmp/img-%03d.png -r 30 animtest3.webm
	@echo ------- AnimTest5 -------
	@$(JAVA) AnimTest5
	@echo ------- TabTest ------
	@$(JAVA) TabTest
	@echo ------- BracketTest ------
	@$(JAVA) BracketTest
	@echo ------ SGTest1 ------
	@$(JAVA) SGTest1
	@echo ------ SGTest2 ------
	@$(JAVA) SGTest2
	@echo ------ SGTest3 ------
	@$(JAVA) SGTest3
	@echo ------ SGTest4 ------
	@$(JAVA) SGTest4
	@echo ------ SGTest5 ------
	@$(JAVA) SGTest5
	@echo ------ SGTest6 ------
	@$(JAVA) SGTest6
	@echo ------ SGTest7 ------
	@$(JAVA) SGTest7
	@echo ------ SGTest8 ------
	@$(JAVA) SGTest8
	@echo ------ SGTest9 ------
	@$(JAVA) SGTest9
	@echo ------ SGTest10 ------
	@$(JAVA) SGTest10
	@echo ------ SGTest11 ------
	@$(JAVA) SGTest11
	@echo ------ SGTest12 ------
	@$(JAVA) SGTest12
	@echo ------ SGTest12a ------
	@$(JAVA) SGTest12a
	@echo ------ SGTest12b ------
	@$(JAVA) SGTest12b
	@echo ------ SGTest12c ------
	@$(JAVA) SGTest12c
	@echo ------ SGBTest ------
	@$(JAVA) SGBTest
	@echo ------ SGBTest1 ------
	@$(JAVA) SGBTest1
	@echo ------ SGBTest2 ------
	@$(JAVA) SGBTest2
	@echo ------ SGBTest3 ------
	@$(JAVA) SGBTest3
	@echo ------ SGBTest4 ------
	@$(JAVA) SGBTest4
	@echo ------ SGBTest5 ------
	@$(JAVA) SGBTest5
	@echo ------ SGBTest6 ------
	@$(JAVA) SGBTest6
	@echo ------ SGBTest7 ------
	@$(JAVA) SGBTest7
	@echo ------ SGBTest8 ------
	@$(JAVA) SGBTest8
	@echo ------ SGBTest9 ------
	@$(JAVA) SGBTest9
	@echo ------ SGBTest10 ------
	@$(JAVA) SGBTest10
	@echo ------ SGBTest11 ------
	@$(JAVA) SGBTest11
	@echo ------ SGBTest12 ------
	@$(JAVA) SGBTest12
	@echo --- SGB Error Test ----
	$(JAVA2) Cover
	@echo --- TransformTest ---
	@$(JAVA) TransformTest
	@echo ------ TLTest1 ------
	@$(JAVA) TLTest1
	@echo ------ TLTest2 ------
	@$(JAVA) TLTest2
	@echo ------ TLTest3 ------
	@$(JAVA) TLTest3
	@echo ------ TLTest4 ------
	@$(JAVA) TLTest4
	@echo ------ TLTest5 ------
	@$(JAVA) TLTest5
	@echo ------ TLTest6 ------
	@$(JAVA) TLTest6
	@echo ------ TLTest7 ------
	@$(JAVA) TLTest7
	@echo ------ DialectricSphere ------
	@$(JAVA) DielectricSphere
	@echo ------- FakeLock --error ---------
	@$(JAVA) FakeLock --error
	@echo ------- FakeLock ---------
	@$(JAVA) FakeLock

anim5:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------- AnimTest5 -------
	@$(JAVA) AnimTest5

fakelock:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------- FakeLock --error ---------
	@$(JAVA) FakeLock --error
	@echo ------- FakeLock ---------
	@$(JAVA) FakeLock

fakelock2:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------- FakeLock2 ---------
	@$(JAVA) FakeLock2

fakelock3:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------- FakeLock3 ---------
	@$(JAVA) FakeLock3

cptest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------- CPTest ---------
	@$(JAVA) CPTest

fakelock3d1:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------- FakeLock3 ---------
	@$(JAVA) FakeLock3 tessellate1

fakelock3d2:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------- FakeLock3 ---------
	@$(JAVA) FakeLock3 tessellate2

fakelock3d1d2:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------- FakeLock3 ---------
	@$(JAVA) FakeLock3 tessellate1 tessellate2

fakelock3a:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------- FakeLock3 ---------
	@$(JAVA) FakeLock3 hello

fakelock3b:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------- FakeLock3 ---------
	@$(JAVA) FakeLock3 hello close

fakelock3bd1d2:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------- FakeLock3 ---------
	@$(JAVA) FakeLock3 hello close tessellate1 tessellate2

fakelockT:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------- FakeLockT ---------
	@$(JAVA) FakeLockT

fakelockF:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------- FakeLockF ---------
	@$(JAVA) FakeLockF

dsphere:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------ DielectricSphere ------
	@$(JAVA) DielectricSphere

btest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo --- BTest ---
	@$(JAVA) BTest

etest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------- TabTest ------
	@$(JAVA) TabTest
	@echo ------- BracketTest ------
	@$(JAVA) BracketTest
	@echo ------- TTest --------
	@$(JAVA) TTest

ttest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------- TTest --------
	@$(JAVA) TTest
	@echo ------ TTest1 --------
	@$(JAVA) TTest1
	@echo ------ TTest2 --------
	@$(JAVA) TTest2
	@echo ------ TTest3 --------
	@$(JAVA) TTest3
	@echo ------ TTest4 --------
	@$(JAVA) TTest4
	@echo ------ TTest5 --------
	@$(JAVA) TTest5
	@echo ------ TTest6 --------
	@$(JAVA) TTest6

ttest0:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------- TTest --------
	@$(JAVA) TTest

ttest3:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------ TTest3 --------
	@$(JAVA) TTest3


ttest4:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------ TTest4 --------
	@$(JAVA) TTest4

ttest5:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------ TTest5 --------
	@$(JAVA) TTest5

ttest6:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------ TTest6 --------
	@$(JAVA) TTest6

tltest1:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------ TLTest1 ------
	@$(JAVA) TLTest1

tltest2:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------ TLTest2 ------
	@$(JAVA) TLTest2

tltest3:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------ TLTest3 ------
	@$(JAVA) TLTest3

tltest4:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------ TLTest4 ------
	@$(JAVA) TLTest4


tltest5:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------ TLTest5 ------
	@$(JAVA) TLTest5

tltest6:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------ TLTest6 ------
	@$(JAVA) TLTest6

tltest7:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------ TLTest7 ------
	@$(JAVA) TLTest7

tltest8:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------ TLTest8 ------
	@$(JAVA) TLTest8


tabtest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@$(JAVA) TabTest

bracket:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@$(JAVA) BracketTest

volumeTest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ----- CylinderTest -----
	@$(JAVA) CylinderTest
	@echo ----- TwoCylinderTest -----
	@$(JAVA) TwoCylinderTest
	@echo ----- HollowCylinderTest -----
	@$(JAVA) HollowCylinderTest
	@echo ----- HollowCylinderTest2 -----
	@$(JAVA) HollowCylinderTest2

sgtest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------ SGTest1 ------
	@$(JAVA) SGTest1
	@echo ------ SGTest2 ------
	@$(JAVA) SGTest2
	@echo ------ SGTest3 ------
	@$(JAVA) SGTest3
	@echo ------ SGTest4 ------
	@$(JAVA) SGTest4
	@echo ------ SGTest5 ------
	@$(JAVA) SGTest5
	@echo ------ SGTest6 ------
	@$(JAVA) SGTest6
	@echo ------ SGTest7 ------
	@$(JAVA) SGTest7
	@echo ------ SGTest8 ------
	@$(JAVA) SGTest8
	@echo ------ SGTest9 ------
	@$(JAVA) SGTest9
	@echo ------ SGTest10 ------
	@$(JAVA) SGTest10
	@echo ------ SGTest11 ------
	@$(JAVA) SGTest11
	@echo ------ SGTest12 ------
	@$(JAVA) SGTest12
	@echo ------ SGTest12a ------
	@$(JAVA) SGTest12a
	@echo ------ SGTest12b ------
	@$(JAVA) SGTest12b
	@echo ------ SGTest12c ------
	@$(JAVA) SGTest12c
	@echo ------ SGTest12d ------
	@$(JAVA) SGTest12d

sgtest1:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------ SGTest1 ------
	@$(JAVA) SGTest1

sgtest2:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------ SGTest2 ------
	@$(JAVA) SGTest2

sgtest3:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------ SGTest3 ------
	@$(JAVA) SGTest3

sgtest4:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------ SGTest4 ------
	@$(JAVA) SGTest4

sgtest5:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------ SGTest5 ------
	@$(JAVA) SGTest5

sgtest6:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------ SGTest6 ------
	@$(JAVA) SGTest6

sgtest7:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------ SGTest7 ------
	@$(JAVA) SGTest7

sgtest8:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------ SGTest8 ------
	@$(JAVA) SGTest8

sgtest9:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------ SGTest9 ------
	@$(JAVA) SGTest9

sgtest10:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------ SGTest10 ------
	@$(JAVA) SGTest10

sgtest11:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------ SGTest11 ------
	@$(JAVA) SGTest11

sgtest12:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------ SGTest12 ------
	@$(JAVA) SGTest12
	@echo ------ SGTest12a ------
	@$(JAVA) SGTest12a
	@echo ------ SGTest12b ------
	@$(JAVA) SGTest12b
	@echo ------ SGTest12c ------
	@$(JAVA) SGTest12c
	@echo ------ SGTest12d ------
	@$(JAVA) SGTest12d

sgtest12a:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------ SGTest12c ------
	@$(JAVA) SGTest12a

sgbtest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ----- SGBTest --------
	@$(JAVA) SGBTest
	@echo ------ SGBTest1 ------
	@$(JAVA) SGBTest1
	@echo ------ SGBTest2 ------
	@$(JAVA) SGBTest2
	@echo ------ SGBTest3 ------
	@$(JAVA) SGBTest3
	@echo ------ SGBTest4 ------
	@$(JAVA) SGBTest4
	@echo ------ SGBTest5 ------
	@$(JAVA) SGBTest5
	@echo ------ SGBTest6 ------
	@$(JAVA) SGBTest6
	@echo ------ SGBTest7 ------
	@$(JAVA) SGBTest7
	@echo ------ SGBTest8 ------
	@$(JAVA) SGBTest8
	@echo ------ SGBTest9 ------
	@$(JAVA) SGBTest9
	@echo ------ SGBTest10 ------
	@$(JAVA) SGBTest10
	@echo ------ SGBTest11 ------
	@$(JAVA) SGBTest11
	@echo ------ SGBTest12 ------
	@$(JAVA) SGBTest12
	@echo -------- DoubleConeTest --------
	$(JAVA) DoubleConeTest

cover:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo --- SGB Error Test ----
	$(JAVA2) Cover

dcone:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo -------- DoubleConeTest --------
	$(JAVA) DoubleConeTest

sgbtest1:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------ SGBTest1 ------
	@$(JAVA) SGBTest1

sgbtest2:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------ SGBTest2 ------
	@$(JAVA) SGBTest2

sgbtest3:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------ SGBTest3 ------
	@$(JAVA) SGBTest3

sgbtest4:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------ SGBTest4 ------
	@$(JAVA) SGBTest4

sgbtest5:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------ SGBTest5 ------
	@$(JAVA) SGBTest5

sgbtest6:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------ SGBTest6 ------
	@$(JAVA) SGBTest6

sgbtest7:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------ SGBTest7 ------
	@$(JAVA) SGBTest7

sgbtest8:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------ SGBTest8 ------
	@$(JAVA) SGBTest8

sgbtest9:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------ SGBTest9 ------
	@$(JAVA) SGBTest9

sgbtest10:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------ SGBTest10 ------
	@$(JAVA) SGBTest10

sgbtest11:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------ SGBTest11 ------
	@$(JAVA) SGBTest11


sgbtest12:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------ SGBTest12 ------
	@$(JAVA) SGBTest12



transformtest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------ TransformTest ------
	@$(JAVA) TransformTest

ttest1:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------ TTest1 ------
	@$(JAVA) TTest1

ttest2:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo ------ TTest2 ------
	@$(JAVA) TTest2

btest1:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo -------- BTest1 --------
	@$(JAVA) BTest1

btest1a:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo -------- BTest1 --------
	@$(JAVA2)  BTest1

xtest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo -------- XTest --------
	@$(JAVA2)  XTest

cvtest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo -------- CVTest --------
	@$(JAVA2)  CVTest

etest1:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo --------- ETest1 ----------
	@$(JAVA) ETest1

corner:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	@echo --------- CornerInserts ----------
	@$(JAVA) CornerInserts

clean:
	rm -rf classes
	rm -f ltmp/*.png
	rm -f *.webm
