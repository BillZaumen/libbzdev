AVCONV = $(shell  which avconv > /dev/null && echo avconv || echo ffmpeg)

all: alljars
	make run-axis
	make run-dmethod
	make run-multi
	make run-legendre
	make run-plates
	make run-lgraph
	make run-lingraph
	make run-dplot
	make run-lsqfit
	make run-lsqfit2
	make run-tandem1
	make run-tandem2
	make run-tandem3
	make run-tandem4
	make run-harmonic
	make run-layer
	make run-drama1
	make run-smartmeter
	make run-smartmeter-sc
	make run-smartmeter2
	make run-smartmeter3
	make run-stephantime
	make run-p3d
	make run-p3d-sc
	make run-p3d-scerr
	make run-p3d2
	make run-pendulum
	make run-blackhole
	make run-lognormal
	make run-versions
	make run-zipviewer

alljars: axis.jar dmethod.jar multi.jar legendre.jar lgraph.jar lingraph.jar \
	dplot.jar math.jar path.jar tandem1.jar tandem2.jar \
	zipviewer.jar lognormal.jar pendulum.jar smartmeter.jar \
	harmonic.jar lockpart.jar lockpart2.jar \
	lsqfit.jar lsqfit2.jar epi.jar

libs:
	( cd .. ; make )
	[ ! -e libbzdev.jar ] && ln -s ../BUILD/libbzdev.jar . || true
	[ ! -e libosgbatik.jar ] && \
		ln -s /usr/share/java/libosgbatik.jar . || true
	for i in base dmethods obnaming graphics parmproc desktop devqsim \
		math drama anim2d p3d ejws ; do \
		[ ! -e libbzdev-$$i.jar ] && \
			ln -s ../BUILD/libbzdev-$$i.jar . || true ; \
	done

run-axis: axis.jar
	java -jar axis.jar 1 axis1.png
	java -jar axis.jar 2 axis2.png
	java -jar axis.jar 3 axis3.png
	java -jar axis.jar 4 axis4.png
	java -jar axis.jar 5 axis5.png
	java -jar axis.jar 6 axis6.png

run-axis-ps: axis.jar
	java -jar axis.jar 1 axis1.ps
	java -jar axis.jar 2 axis2.ps
	java -jar axis.jar 3 axis3.ps
	java -jar axis.jar 4 axis4.ps
	java -jar axis.jar 5 axis5.ps
	java -jar axis.jar 6 axis6.ps


run-dmethod: dmethod.jar
	java -jar dmethod.jar

run-multi: multi.jar
	java -jar multi.jar

run-layer:
	mkdir -p latmp
	rm -f latmp/*.png
	@time ./scrun --stackTrace -d:tmpdir:latmp layer/layer.js
	$(AVCONV) -y -f image2 -i latmp/col-%03d.png -r 25 layer.mp4

run-layer-ep:
	mkdir -p latmp
	@time ./scrun --stackTrace -d:tmpdir:latmp layer/layer.esp
	@echo avconv = $(AVCONV)
	$(AVCONV) -y -f image2 -i latmp/col-%03d.png -r 25 layer.mp4


run-legendre: legendre.jar
	java -jar legendre.jar legendre.ps legendre2.ps
	rm -f legendre.eps legendre2.eps
	ps2eps -P legendre.ps
	ps2eps -P legendre2.ps

run-plates: plates.jar
	java -jar plates.jar plates.ps plates2.ps
	rm -f plates.eps plates2.eps
	ps2eps -P plates.ps
	ps2eps -P plates2.ps


run-lgraph : lgraph.jar
	java -jar lgraph.jar lgraph.ps
	rm -f lgraph.eps
	ps2eps -P lgraph.ps

run-lingraph : lingraph.jar
	java -jar lingraph.jar lingraph.ps
	rm -f lingraph.eps
	ps2eps -P lingraph.ps

run-dplot : dplot.jar
	java -jar dplot.jar dplot.ps
	rm -f dplot.eps
	ps2eps -P dplot.ps

run-lsqfit : lsqfit.jar
	java -jar lsqfit.jar lsqfit.ps
	rm -f lsqfit.eps
	ps2eps -P lsqfit.ps

run-lsqfit2: lsqfit2.jar
	java -jar lsqfit2.jar lsqfit.svg

run-math: math.jar
	java -jar math.jar

run-path: path.jar 
	java -jar path.jar

run-tandem1: tandem1.jar
	java -jar tandem1.jar

run-tandem2: tandem2.jar
	java -jar tandem2.jar

run-tandem3: tandem3.jar
	java -jar tandem3.jar
	$(AVCONV) -y -f image2 -i ttmp/img-%03d.png -r 25 tandem3.webm

run-tandem4: tandem4.jar
	java -jar tandem4.jar


run-blackhole: blackhole.jar
	java -jar blackhole.jar
	$(AVCONV) -y -f image2 -r 30 -i bhtmp/img-%05d.png -r 30 blackhole.webm

run-epi: epi.jar
	java -jar epi.jar

run-zipviewer: zipviewer.jar
	java -jar zipviewer.jar tandem3.jar

run-lognormal: lognormal.jar
	java -jar lognormal.jar
	$(AVCONV) -y -f image2 -i atmp/img-%03d.png -r 25 lognormal.webm
	rm atmp/*.png
	rmdir atmp

run-harmonic: harmonic.jar
	java -jar harmonic.jar
	$(AVCONV) -y -f image2 -i htmp/img-%03d.png -r 25 harmonic.webm

run-drama1: drama1.jar
	java -jar drama1.jar

run-drama2: drama1.jar
	java -jar drama2.jar

run-pendulum: pendulum.jar
	java -jar pendulum.jar
	$(AVCONV) -y -r 50 -f image2 -i pndltmp/img%04d.jpg -r 50 pendulum.webm

run-smartmeter: smartmeter.jar
	java -jar smartmeter.jar \
		 --type png smeter.png

run-smartmeter-sc: smartmeter.jar
	chmod a-x scrun
	chmod u+x scrun
	./scrun --classpathCodebase smartmeter.jar \
		-vS:imgtype:png -o:os:smeter-sc.png \
		-J-Xmx1600m smartmeter/script.js

run-smartmeter2: smartmeter.jar
	java -jar smartmeter.jar --usageReduction --type png smeter2.png

run-smartmeter3: smartmeter.jar
	java -jar smartmeter.jar \
		--usageReduction --phaseInReduction \
		--type png smeter3.png

run-stephantime: stephantime.jar
	java -jar stephantime.jar 13 12 20000 stephan.ps
	ps2eps -P -f stephan.ps

run-p3d: lockpart.jar
	java -jar lockpart.jar
	$(AVCONV) -y -f image2 -r 30 -i lptmp/lp-%03d.png -r 30 lockpart.webm
	rm -f lptmp/*.png

run-p3d-sc: lockpart.jar
	rm -f lptmp/*.png
	./scrun -o:stl:lockpart-sc.stl -d:da:lptmp p3d/lockpart.js
	$(AVCONV) -y -f image2 -r 30 -i lptmp/lp-%03d.png -r 30 lockpart-sc.webm
	rm -f lptmp/*.png

run-p3d-esp: lockpart.jar
	rm -f lptmp/*.png
	./scrun -o:stl:lockpart-sc.stl -d:da:lptmp p3d/lockpart.esp
	$(AVCONV) -y -f image2 -r 30 -i lptmp/lp-%03d.png -r 30 lockpart-sc.webm
	rm -f lptmp/*.png


run-p3d-scerr: lockpart.jar
	./scrun  p3d/errtest.js || echo '(error expected)'

run-p3d2: lockpart2.jar
	java -jar lockpart2.jar
	$(AVCONV) -y -f image2 -r 30 -i lptmp2/lp-%03d.png -r 30 lockpart2.webm
	rm -f lptmp/*.png

run-versions: smartmeter.jar
	./scrun --classpathCodebase smartmeter.jar --versions

axis.jar: axis/*.java
	(cd axis; make)

dmethod.jar:
	(cd dmethod ; make)

multi.jar: multi/*.java
	(cd multi ; make)

epi.jar:
	(cd epi; make)

legendre.jar: Legendre/*.java
	(cd Legendre; make)

plates.jar: plates/*.java
	(cd plates; make)

lgraph.jar: lgraph/*.java
	(cd lgraph; make)

lingraph.jar: lingraph/*.java
	(cd lingraph; make)

dplot.jar: dplot/*.java
	(cd dplot; make)

lsqfit.jar: lsqfit/*.java
	(cd lsqfit; make)

lsqfit2.jar: lsqfit/*.java
	(cd lsqfit; make)

math.jar: math/*.java
	(cd math; make)

path.jar: path/*.java
	(cd path; make)

tandem1.jar: tandem/*.java
	(cd tandem; make);

tandem2.jar: tandem/*.java
	(cd tandem; make);

tandem3.jar: tandem/*.java
	(cd tandem; make);

tandem4.jar: tandem/*.java
	(cd tandem; make);

blackhole.jar: blackhole/*.java
	(cd blackhole; make)

zipviewer.jar: zipviewer/*.java
	(cd zipviewer; make)

lognormal.jar: lognormal/*.java
	(cd lognormal; make)

harmonic.jar: harmonic/*.java
	(cd harmonic; make)

drama1.jar: drama/*.java
	(cd drama; make);

drama2.jar: drama/*.java
	(cd drama; make);

pendulum.jar: pendulum/*.java
	(cd pendulum ; make)

smartmeter.jar: smartmeter/*.java smartmeter/*.properties
	(cd smartmeter ; make)	

stephantime.jar: stephan/*.java stephan/stephantime.mf
	(cd stephan; make)


lockpart.jar: p3d/*.java
	(cd p3d ; make)

lockpart2.jar: p3d2/*.java
	(cd p3d2 ; make)

clean:
	rm -f *.jar lognormal.webm
	for i in axis dmethod dplot harmonic Legendre lgraph lingraph \
		lognormal math multi path pendulum smartmeter tandem \
		zipviewer ; \
	do ( cd $$i ; make clean ) done
