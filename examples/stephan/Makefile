GRAPH = graph.ps

all:
	(cd ..; make libs)
	javac -classpath ../../BUILD/libbzdev.jar *.java
	jar cfm ../stephantime.jar stephantime.mf *.class
	rm *.class

clean:
	rm -f *.class
