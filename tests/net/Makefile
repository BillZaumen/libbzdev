JAVA = java -p ../../BUILD -classpath classes --add-modules org.bzdev

all: compile
	$(JAVA) Test
	@echo ---- ICalBuilder Test ------
	@$(JAVA) ICalBuilderTest
	@echo ---- FormDataIterator Test ----
	$(JAVA) FormDataTest
	@echo ----- PEM Test -----
	$(JAVA) PemTest

compile:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -p ../../BUILD  --add-modules org.bzdev *.java

test: compile
	$(JAVA) Test

ical: compile
	@echo ---- ICalBuilder Test ------
	@$(JAVA) ICalBuilderTest

formtest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) FormDataTest


cwtest: compile
	$(JAVA) CWTest

cwtest1: compile
	$(JAVA) CWTest 10

run-cwtest:
	sudo $(JAVA) CWTest

pem-test: compile
	$(JAVA) PemTest

secbasic: compile ecstore.pfx public.pem public.der
	@echo ---- SecureBasicUtilities Test ----
	@$(JAVA) SecureBasicTest

# The following rules were added to check openssl compatibility

eckey.pem:
	openssl ecparam -name prime256v1 -genkey -noout -out eckey.pem

ecstore.pfx: eckey.pem
	openssl req -new -x509 -key eckey.pem -out eccert.pem -days 360
	openssl pkcs12 -export -inkey eckey.pem -in eccert.pem \
	    -name key -out ecstore.pfx

public.pem: eckey.pem
	openssl ec -in eckey.pem -pubout -out public.pem -outform PEM

public.der: eckey.pem
	openssl ec -in eckey.pem -pubout -out public.der -outform DER


sslutil0: compile
	$(JAVA) SSLUtilTest

sslutil1: compile
	$(JAVA) SSLUtilTest -

sslutil2: compile
	$(JAVA) SSLUtilTest tstore

sslutil3: compile
	$(JAVA) SSLUtilTest3

#
# Special case: requires going to ../ejws and running
#    make cwstest3
# in a separate window  before running
#    make sslutil4
#
sslutil4: compile
	$(JAVA) SSLUtilTest4


sslutil121: compile
	$(JAVA) SSLUtilTest - tstore -

sslutil: compile
	$(JAVA) SSLUtilTest2

#

clean:
	rm -rf classes testcal.ics eckey.pem public.pem public.der \
		ecstore.pfx eccert.pem  secbasic.p12
