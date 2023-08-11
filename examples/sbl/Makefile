SHELL = /usr/bin/bash

SERVER = classes/Server.class

BZDEV = /usr/share/bzdev/libbzdev.jar


all: $(SERVER) access.sbl

run: classes/Server.class keystore.jks access.sbl
	java -classpath classes:$(BZDEV) Server


classes/Server.class: Server.java
	mkdir -p classes
	javac -d classes -classpath $(BZDEV) Server.java

keystore.jks:
	keytool -genkey -keyalg EC -groupname secp256r1 \
		-sigalg SHA256withECDSA -keystore keystore.jks \
		-keypass changeit -storepass changeit \
		-dname CN=`hostname` -alias test -validity 36500

access.sbl:
	@echo After entering a GPG user ID, press the Add Entry button
	@echo to create an entry for "'user'".
	make update

#
# Change (or create) a key pair.
#
update:
	bash -c "sbl -r `read -p 'GPG USER ID: ' ; echo $$REPLY` access.sbl"
