
# compile the test program
compile:
	mkdir -p mods/com.foo.mod
	mkdir -p tmpsrc/com.foo.mod
	javac -d mods/com.foo.mod -p /usr/share/bzdev -s tmpsrc/com.foo.mod \
		--processor-module-path /usr/share/bzdev \
		src/com.foo.mod/module-info.java \
		src/com.foo.mod/pkg/*.java
	jar --create --file foo.jar --main-class pkg.Test \
		-C mods/com.foo.mod .

run:
	java -p foo.jar:/usr/share/bzdev -m com.foo.mod

clean:
	rm -f foo.jar
	rm -rf mods tmpsrc
