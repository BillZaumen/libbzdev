JAVA = java -classpath ../../BUILD/libbzdev.jar:classes

all:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) UtilTest "./"
	cat junk
	@echo -------------------------
	zip -r example.zip example
	$(JAVA) UtilTest example.zip example/
	rm example.zip
	cat junk
	@echo -------------------------
	$(JAVA) WebMapTest
	@echo -------------------------
	$(JAVA) WebxmlParserTest example junk
	rm junk

modtest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) ModTest

dirtest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) DirTest

dirtest2:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) DirTest2

test:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) Test

stest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) -Djavax.net.debug=ssl STest

stest1:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) STest1

stest2:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) STest2

stest2a:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) STest2a


stest3a:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) -Djavax.net.debug=ssl STest3 SSL

stest3b:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) -Djavax.net.debug=ssl STest3 TLS


test2:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) Test2

test3:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) Test3

test4:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) Test4


test5:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) Test5

test6:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) Test6

test6a:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) Test6 false


test7:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	rm -f example.zip
	(cd example; zip -r ../example.zip *)
	$(JAVA) Test7
	rm example.zip

test7a:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	rm -f example.zip
	(cd example; zip -r ../example.zip *)
	$(JAVA) Test7 false
	rm example.zip


test8:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	rm -f example.zip
	(cd example; zip -r ../example.zip *)
	$(JAVA) Test8
	rm example.zip

test9:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) Test9

test10:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) Test10

test11:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) Test11

test11-post:
	curl --form txt=text http://localhost:8080/post/

test11-get:
	curl http://localhost:8080/post/?txt1=text&txt2=text2


htest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) HTest


test12:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) Test12

errtest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) ErrorTest

test0:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) Test0

test0a:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) Test0a


etest:
	(cd ../.. ; make jars)
	mkdir -p classes
	mkdir -p classes/manual
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	cp ewstest/epts.html classes/manual
	cp ewstest/*.png classes/manual
	$(JAVA) ETest

wmtest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) WebMapTest

atest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) ATest

atest2:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) ATest2

atest3:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) ATest3 publicKey.pem

atest4:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) ATest4 publicKey.pem

atest4-nologin:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) ATest4 publicKey.pem --nologin


atest5:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) ATest5

atest5a:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) ATest5a


atest4-noauth:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) ATest4 publicKey.pem --noauth

atest4-open:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) ATest4 publicKey.pem /open/index.html

atest4-badlogout:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) ATest4 publicKey.pem /index.html

atest6:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) ATest6 ../bin/sblconf/test.sbl foo

atest6a:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) ATest6a ../bin/sblconf/test.sbl foo

atest7:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) ATest7 /usr/share/doc/libbzdev-doc/api/ \
		~/.config/sbl/example.sbl testUser

atest8:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) ATest8 /usr/share/doc/libbzdev-doc/api/ \
		~/.config/sbl/example.sbl testUser

utest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) UtilTest "./"
	cat junk

cmtest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) CMTest


cert:
	rm -f thelio-ks.jks thelio-ts.jks
	keytool -genkey -keyalg EC -groupname secp256r1 \
		-sigalg SHA256withECDSA -keystore thelio-ks.jks \
		-keypass changeit -storepass changeit \
		-dname CN=wtz-thelio -alias thelio -validity 365
	keytool -keystore thelio-ks.jks -alias thelio -exportcert \
		-storepass changeit -rfc \
	| keytool -importcert -alias thelio  -keystore thelio-ts.jks \
		-keypass changeit -storepass changeit -noprompt

# one day - we want the certificates to expire for testing purposes.
cert2:
	rm -f thelio-ks2.jks thelio-ts2.jks
	keytool -genkey -keyalg EC -groupname secp256r1 \
		-sigalg SHA256withECDSA -keystore thelio-ks2.jks \
		-keypass changeit -storepass changeit \
		-dname CN=wtz-thelio -alias thelio -validity 1
	keytool -keystore thelio-ks2.jks -alias thelio -exportcert \
		-storepass changeit -rfc \
	| keytool -importcert -alias thelio  -keystore thelio-ts2.jks \
		-keypass changeit -storepass changeit -noprompt

cwstest1:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	zip -r example.zip example
	$(JAVA) CWSTest cwstest1.yaml  || rm example.zip
	rm -f example.zip

cwstest2:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	zip -r example.zip example
	$(JAVA) CWSTest2 cwstest2.config  || rm example.zip
	rm -f example.zip

cwstest3:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	zip -r example.zip example
	$(JAVA) CWSTest cwstest2.yaml https://localhost:8443/api/  \
		|| rm example.zip
	rm -f example.zip

cwstest5:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) CWSTest cwstest4.yaml

cwstest1-post:
	curl --data foo=text http://localhost:8080/post/

cwstest1-get:
	curl http://localhost:8080/post/?foo=text

cwstest3-post:
	curl -k --data foo=text https://localhost:8443/post/

cwstest3-get:
	curl -k https://localhost:8443/post/?foo=text

cwstest4:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	zip -r example.zip example
	$(JAVA) CWSTest cwstest3.yaml  \
		|| rm example.zip
	rm -f example.zip

clean:
	rm -rf classes
	rm -f example.zip
