JAVAC = javac -p ../../../BUILD --add-modules org.bzdev \
		--processor-path ../../../BUILD/libbzdev.jar

dmtests:
	(cd ../../.. ; make)
	/bin/rm -rf ourclasses ourclasses1
	mkdir -p ourclasses ourclasses1 tmpsrc
	chmod a+r dmtest/a
	$(JAVAC) -d ourclasses -s tmpsrc  \
		-classpath ourclasses \
		dmtest/[ab]/*.java
	chmod u+x run
	./run dmtest.a.SomeVisitor
	for i in `grep -l main dmtest/b/*.java` ; do echo $$i ; \
		./run dmtest.b.`basename $$i .java` ; done
	@echo ---- seperate compilation with a class directory ----
	make -f Makefile2
	@echo ---- seperate compilation with a jar file  ----
	make -f Makefile3
	@echo ---- try test for classes with a null package ---
	(cd nopkgtest ; make)

clean:
	rm -rf tmpsrc ourclasses ourclasses1
	make -f Makefile2 clean
	make -f Makefile3 clean
	(cd nopkgtest; make clean)
