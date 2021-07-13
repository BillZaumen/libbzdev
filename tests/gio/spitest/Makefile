foo.jar: foo/*.java META-INF/services/org.bzdev.gio.spi.OSGProvider
	mkdir -p classes
	(cd foo; javac -classpath ../../../../BUILD/libbzdev.jar *.java)
	jar cf foo.jar foo/*.class \
		META-INF/services/org.bzdev.gio.spi.OSGProvider
	rm -rf classes

clean:
	rm -f foo.jar
