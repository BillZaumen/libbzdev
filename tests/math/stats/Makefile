JAVA = java -classpath ../../../BUILD/libbzdev.jar:classes

all:
	(cd ../../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../../BUILD/libbzdev.jar *.java
	@echo -------- CovarianceMatrixTest --------
	@$(JAVA) CovarianceMatrixTest
	@echo -------- BasicStatsTest --------
	@$(JAVA) BasicStatsTest
	@echo ------- BasicStatsMVTest -------
	@$(JAVA) BasicStatsMVTest
	@echo --------- GaussianTest ---------
	@$(JAVA) GaussianTest
	@echo --------- ChiSquareTest ---------
	@$(JAVA) ChiSquareTest
	@echo --------- StudentsTest " (and Welsch's t-test)"---------
	@$(JAVA) StudentTest
	@echo --------- KolmogorovTest ---------
	@$(JAVA) KolmogorovTest
	@echo --------- KSTest ---------
	@$(JAVA) KSTest
	@echo ---------- FTest -----------
	@$(JAVA) FTest

cov:
	(cd ../../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../../BUILD/libbzdev.jar *.java
	@echo -------- CovarianceMatrixTest --------
	@$(JAVA) CovarianceMatrixTest

basic:
	(cd ../../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../../BUILD/libbzdev.jar *.java
	@echo -------- BasicStatsTest --------
	@$(JAVA) BasicStatsTest

basic-mv:
	(cd ../../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../../BUILD/libbzdev.jar *.java
	@echo -------- BasicStatsMVTest --------
	@$(JAVA) BasicStatsMVTest

ks:
	(cd ../../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../../BUILD/libbzdev.jar *.java
	@$(JAVA) KolmogorovTest
	@$(JAVA) KolmogorovTest 0.49 10

ftest:
	(cd ../../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../../BUILD/libbzdev.jar *.java
	@echo ---------- FTest -----------
	@$(JAVA) FTest

stest:
	(cd ../../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../../BUILD/libbzdev.jar *.java
	@echo --------- StudentsTest " (and Welsch's t-test)"---------
	@$(JAVA) StudentTest


clean:
	rm -rf classes
