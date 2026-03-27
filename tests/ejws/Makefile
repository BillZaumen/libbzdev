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
	$(JAVA) ATest4 ~/.config/sbl/test.sbl foo

atest4a:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) ATest4a ~/.config/sbl/test.sbl foo

atest4-nologin:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) ATest4 ~/.config/sbl/test.sbl foo --nologin


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
	$(JAVA) ATest4 ~/.config/sbl/test.sbl foo --noauth

atest4-open:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) ATest4 ~/.config/sbl/test.sbl foo /open/index.html

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
		../bin/sblconf/test5.sbl user

atest8-cert:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) ATest8 /usr/share/doc/libbzdev-doc/api/ \
		~/.config/sbl/example.sbl testUser thelio

#
#  To use, run make atest8-sbl R=... where R will be a GPG recipient
#  Then start a browser with the URL
#  https://localhost:8080/login.html?user=test-user
#
atest8-sbl:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) ATest8SBL /usr/share/doc/libbzdev-doc/api/ \
		test-gpgdir $(R)

test8a:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) Test8a /usr/share/doc/libbzdev-doc/api/


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

cert3:
	rm -f thelio-ks.jks thelio-ts.jks
	keytool -genkey -keyalg EC -groupname secp256r1 \
		-sigalg SHA256withECDSA -keystore thelio-ks.jks \
		-keypass changeit -storepass changeit \
		-ext bc=ca:true	-dname CN=ca -alias ca -validity 365
	keytool -genkey -keyalg EC -groupname secp256r1 \
		-sigalg SHA256withECDSA -keystore thelio-ks.jks \
		-keypass changeit -storepass changeit \
		-dname CN=wtz-thelio -alias thelio -validity 365
	keytool -keystore thelio-ks.jks -alias thelio \
		-certreq -storepass changeit -keypass changeit | \
		keytool -keystore thelio-ks.jks  -storepass changeit \
		 -alias ca -gencert  -ext san=dns:wtz-thelio | \
		keytool -keystore thelio-ks.jks  -storepass changeit \
		  -alias thelio -importcert
	keytool -importkeystore -srckeystore thelio-ks.jks \
		-destkeystore thelio-ts.jks -srcalias ca -destalias ca \
		-srcstorepass changeit -deststorepass changeit \
		-srckeypass changeit -destkeypass changeit
	keytool -delete -alias ca -keystore thelio-ks.jks -storepass changeit

# From https://www.pixelstech.net/article/1450354633-Using-keytool-to-create-certificate-chain
cert4:
	rm -f test.jks
	keytool -genkeypair -alias ca -keystore test.jks \
		-dname "CN=CA" -storepass password -keypass password \
		 -keyalg rsa
	keytool -genkeypair -alias leaf -keystore test.jks \
		-dname "CN=Leaf" -storepass password -keypass password \
		 -keyalg rsa
	keytool -certreq -keystore test.jks -storepass password \
		-alias leaf -file leaf.csr
	keytool -gencert -keystore test.jks -storepass password \
		-alias ca -infile leaf.csr -outfile leaf.cer
	keytool -importcert -keystore test.jks -storepass password \
		-file leaf.cer -alias leaf

# Test to see if we can set up a trust store without including the
# private key.
cert5:
	rm -f thelio-ks.jks thelio-ts.jks thelio-ca.jks
	keytool -genkey -keyalg EC -groupname secp256r1 \
		-sigalg SHA256withECDSA -keystore thelio-ca.jks \
		-keypass changeit -storepass changeit \
		-ext bc=ca:true	-dname CN=ca -alias ca -validity 365
	keytool -genkey -keyalg EC -groupname secp256r1 \
		-sigalg SHA256withECDSA -keystore thelio-ks.jks \
		-keypass changeit -storepass changeit \
		-dname CN=wtz-thelio -alias thelio -validity 365
	keytool -importkeystore -srckeystore thelio-ks.jks \
		-destkeystore thelio-ca.jks -srcalias thelio -destalias thelio \
		-srcstorepass changeit -deststorepass changeit \
		-srckeypass changeit -destkeypass changeit
	keytool -keystore thelio-ca.jks -alias thelio \
		-certreq -storepass changeit -keypass changeit | \
		keytool -keystore thelio-ca.jks  -storepass changeit \
		 -alias ca -gencert  -ext san=dns:wtz-thelio | \
		keytool -keystore thelio-ca.jks  -storepass changeit \
		  -alias thelio -importcert
	keytool -importkeystore -srckeystore thelio-ca.jks \
		-destkeystore thelio-ks.jks -srcalias thelio -destalias thelio \
		-srcstorepass changeit -deststorepass changeit \
		-srckeypass changeit -destkeypass changeit -noprompt
	keytool -exportcert -keystore thelio-ca.jks -alias ca \
		-storepass changeit | \
		keytool -import -alias ca -keystore thelio-ts.jks \
		-storepass changeit -noprompt
	keytool -delete -alias thelio -keystore thelio-ca.jks \
		-storepass changeit
# chain of length 3.
cert6:
	rm -f thelio-ks.jks thelio-ts.jks root.jks ca.jks
	rm -f root.pem ca.pem server.pem
	keytool -genkey -keyalg EC -groupname secp256r1 \
	  -sigalg SHA256withECDSA -keystore root.jks \
	  -keypass changeit -storepass changeit \
	  -ext bc=ca:true -dname CN=root -alias root -validity 365
	keytool -keystore root.jks -alias root -exportcert -rfc \
	  -storepass changeit -keypass changeit > root.pem
	keytool -keystore thelio-ts.jks -alias root -importcert -file root.pem \
	  -storepass changeit -keypass changeit -noprompt || true
	keytool -genkey -keyalg EC -groupname secp256r1 \
	  -sigalg SHA256withECDSA -keystore ca.jks \
	  -keypass changeit -storepass changeit \
	  -ext bc=ca:true -dname CN=ca -alias ca -validity 365
	keytool -storepass changeit  -keypass changeit -keystore ca.jks \
		-certreq -alias ca | \
	    keytool -storepass changeit -keypass changeit -keystore root.jks \
	    -gencert -alias root -ext BC=0 -rfc > ca.pem
	keytool -keystore ca.jks -importcert -alias ca -file ca.pem \
	    -keypass changeit -storepass changeit  || true
	keytool -genkey -keyalg EC -groupname secp256r1 \
	  -sigalg SHA256withECDSA -keystore thelio-ks.jks \
	  -keypass changeit -storepass changeit \
	  -dname CN=wtz-thelio -alias thelio -validity 90
	keytool -storepass changeit -keypass changeit -keystore thelio-ks.jks \
	  -certreq -alias thelio | \
	    keytool -storepass changeit -keypass changeit -keystore ca.jks\
	     -gencert -alias ca \
	     -ext san=dns:wtz-thelio -ext ku:c=dig,kE -rfc > server.pem
	cat root.pem ca.pem server.pem | \
	keytool -keystore thelio-ks.jks -importcert -alias thelio \
	    -storepass changeit -keypass changeit -noprompt || true



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

authtest:
	(cd ../.. ; make jars)
	mkdir -p classes
	javac -d classes -classpath ../../BUILD/libbzdev.jar *.java
	$(JAVA) AuthTest test-gpgdir gpgkey.asc \
		/usr/share/doc/libbzdev-doc/api/

clean:
	rm -rf classes
	rm -f example.zip
