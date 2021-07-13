

ourmod.jar: src/ourmod/ourpkg/*.java \
	META-INF/services/org.bzdev.obnaming.NamedObjectFactory
	mkdir -p mods/ourmod
	mkdir -p tmpsrc/ourmod
	javac -d mods/ourmod -p ../../../BUILD \
	-s tmpsrc/ourmod -Xlint:unchecked \
	--processor-module-path ../../../BUILD \
	src/ourmod/module-info.java \
	src/ourmod/ourpkg/*.java
	mkdir -p mods/ourmod/META-INF/services
	cp META-INF/services/* mods/ourmod/META-INF/services
	jar --create --file ourmod.jar \
	    --main-class=ourpkg.Test -C mods/ourmod .

run: ourmod.jar
	java -p ../../../BUILD:ourmod.jar -m ourmod

clean:
	rm -rf mods tmpsrc ourmod.jar
