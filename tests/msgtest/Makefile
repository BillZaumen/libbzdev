# Constants for msgtest alternate locale - override if an
# alternate locale is desired by providing the language,
# country, and/or variant strings that Java's Locale constructors
# use:
LANG = null
COUNTRY = null
VARIANT = null
#
# Set this to the scrunner command option needed to add
# a library containing the laguage pack for the second locale, or to
# add any other useful options (.e.g, --stackTrace for debugging):
OPTIONS =
#
# Setting TERSE to false will cause FILE and BUNDLE to be printed
# every time they are set, rather than just when needed. Legal
# values are true or false:
TERSE = true
# setting VERBOSE to true will print more information such as the
# actual error strings (but with fake arguments). Legal values are
# true or false:
VERBOSE = false
#
# Setting TEE will get a snapshot of the script passed to scrunner
# although it might not be complete.

# TEE = /dev/null
#
#
# Setting filter to the a path name, starting, will limit
# the ctest and msgtest output to specific packages or files.
# the default is org/bzdev
#
FILTER = org/bzdev/
#
#
# NOTE: setting TERSE to false and VERBOSE to true is useful for
#        debugging in cases where more information is needed to track
#        down an error.

all: extest msgtest ctest

msgtest:
	(cd ../.. ; make)
	@mkdir -p classes
	@javac -d classes -classpath ../../BUILD/libbzdev.jar BundleTest.java
	@jar cf bundletest.jar -C classes .
	@chmod u+x scrun
	@(cd ../..; echo 'import(BundleTest);'; \
	 echo 'var lang = $(LANG);' ; \
	 echo 'var country = $(COUNTRY);' ; \
	 echo 'var variant = $(VARIANT);' ; \
	 echo 'var exbundle = null;' ; \
	 echo 'BundleTest.setLocale(lang, country, variant);' ; \
	 echo 'BundleTest.setTerse($(TERSE));' ; \
	 echo 'BundleTest.setVerbose($(VERBOSE));' ; \
	 echo 'var arg1 = "arg1"; var arg2 = "arg2"; var arg3 = "arg3";'; \
	 echo 'var arg4 = "arg4"; var arg5 = "arg5"; var arg6 = "arg6";'; \
	 echo 'var arg7 = "arg7"; var arg8 = "arg8"; var arg9 = "arg9";'; \
	 echo 'var arg10 = "arg10"; var arg11 = "arg11"; var arg12 = "arg12";'; \
	 for i in `git grep -l '//@exbundle' | grep $(FILTER)` ; \
	 do grep -o '//@exbundle.*'  $$i | grep -o ' .*' | \
		 sed 's/ \(.*\)/exbundle = "\1";/'; \
	    echo 'BundleTest.setBundle(exbundle, "'$$i'");' ; \
	    grep -o 'errorMsg(["][^;]*;' $$i | \
	    sed \
	      -e 's/"[ 	]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*);/", arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12);/' \
	      -e 's/"[ 	]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*);/", arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11);/' \
	      -e 's/"[ 	]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*);/", arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10);/' \
	      -e 's/"[ 	]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*);/", arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9);/' \
	      -e 's/"[ 	]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*);/", arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8);/' \
	      -e 's/"[ 	]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*);/", arg1, arg2, arg3, arg4, arg5, arg6, arg7);/' \
	      -e 's/"[ 	]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*);/", arg1, arg2, arg3, arg4, arg5, arg6);/' \
	      -e 's/"[ 	]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*);/", arg1, arg2, arg3, arg4, arg5);/' \
	      -e 's/"[ 	]*,[^,]*,[^,]*,[^,]*,[^,]*);/", arg1, arg2, arg3, arg4);/' \
	      -e 's/"[ 	]*,[^,]*,[^,]*,[^,]*);/", arg1, arg2, arg3);/' \
	      -e 's/"[ 	]*,[^,]*,[^,]*);/" , arg1, arg2);/' \
	      -e 's/"[ 	]*,[^,]*);/", arg1);/' \
	      -e 's/"[ 	)]*;/");/' | \
	    sed -e '/arg12)/s/errorMsg/errorMsg12/' \
		-e '/arg11)/s/errorMsg/errorMsg11)/' \
		-e '/arg10)/s/errorMsg/errorMsg10)/' \
		-e '/arg9)/s/errorMsg/errorMsg9/' \
		-e '/arg8)/s/errorMsg/errorMsg8/' \
		-e '/arg7)/s/errorMsg/errorMsg7/' \
		-e '/arg6)/s/errorMsg/errorMsg6/' \
		-e '/arg5)/s/errorMsg/errorMsg5/' \
		-e '/arg4)/s/errorMsg/errorMsg4/' \
	        -e '/arg3)/s/errorMsg/errorMsg3/' \
		-e '/arg2)/s/errorMsg/errorMsg2/' \
		-e '/arg1)/s/errorMsg/errorMsg1/' ; \
	 done; \
	 echo 'BundleTest.checkForErrors();' ;	) | \
	sed -e 's/errorMsg/BundleTest.errorMsg/g' | tee $(TEE) | \
	./scrun --classpathCodebase bundletest.jar $(OPTIONS) -L ESP -- - || \
	  (chmod u-x scrun; exit 1)
	@chmod u-x scrun


extest:
	@for i in `cd ../.. ; git grep -l errorMsg'("'` ; \
	do if `grep // ../../$$i | grep @exbundle > /dev/null` ; \
	then true; else echo @exbundle missing from $$i; exit 1; fi ; done

ctest:
	@lc1=`make VERBOSE=true LANG=null COUNTRY=null VARIANT=null msgtest | \
		 grep '^\[key = ' | wc -l` ; \
	lc2=`(cd ../.. ; git grep errorMsg'("' | grep $(FILTER) | wc -l)` ; \
	if [ $$lc1 -ne $$lc2 ] ; \
	then echo errorMsg call may span multiple lines ; exit 1; fi

clean:
	rm bundletest.jar
	rm -r classes
	chmod u-x scrun

print:
	@(cd ../..; echo 'import(BundleTest);'; \
	 echo 'var lang = $(LANG);' ; \
	 echo 'var country = $(COUNTRY);' ; \
	 echo 'var variant = $(VARIANT);' ; \
	 echo 'BundleTest.setLocale(lang, country, variant);' ; \
	 echo 'BundleTest.setTerse($(TERSE));' ; \
	 echo 'BundleTest.setVerbose($(VERBOSE));' ; \
	 echo 'var arg1 = "arg1"; var arg2 = "arg2"; var arg3 = "arg3";'; \
	 echo 'var arg4 = "arg4"; var arg5 = "arg5"; var arg6 = "arg6";'; \
	 echo 'var arg7 = "arg7"; var arg8 = "arg8"; var arg9 = "arg9";'; \
	 echo 'var arg10 = "arg10"; var arg11 = "arg11"; var arg12 = "arg12";'; \
	 for i in `git grep -l '//@exbundle' | grep $(FILTER)` ; \
	 do grep -o '//@exbundle.*'  $$i | grep -o ' .*' | \
		 sed 's/ \(.*\)/exbundle = "\1";/'; \
	    echo 'BundleTest.setBundle(exbundle, "'$$i'");' ; \
	    grep -o 'errorMsg(["][^;]*;' $$i | \
	    sed \
	      -e 's/"[ 	]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*);/", arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12);/' \
	      -e 's/"[ 	]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*);/", arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11);/' \
	      -e 's/"[ 	]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*);/", arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10);/' \
	      -e 's/"[ 	]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*);/", arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9);/' \
	      -e 's/"[ 	]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*);/", arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8);/' \
	      -e 's/"[ 	]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*);/", arg1, arg2, arg3, arg4, arg5, arg6, arg7);/' \
	      -e 's/"[ 	]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*);/", arg1, arg2, arg3, arg4, arg5, arg6);/' \
	      -e 's/"[ 	]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*);/", arg1, arg2, arg3, arg4, arg5);/' \
	      -e 's/"[ 	]*,[^,]*,[^,]*,[^,]*,[^,]*));/", arg1, arg2, arg3, arg4);/' \
	      -e 's/"[ 	]*,[^,]*,[^,]*,[^,]*);/", arg1, arg2, arg3);/' \
	      -e 's/"[ 	]*,[^,]*,[^,]*);/" , arg1, arg2);/' \
	      -e 's/"[ 	]*,[^,]*);/", arg1);/' \
	      -e 's/"[ 	)]*;/");/' | \
	    sed -e '/arg9)/s/errorMsg/errorMsg9/' \
		-e '/arg8)/s/errorMsg/errorMsg8/' \
		-e '/arg7)/s/errorMsg/errorMsg7/' \
		-e '/arg6)/s/errorMsg/errorMsg6/' \
		-e '/arg5)/s/errorMsg/errorMsg5/' \
		-e '/arg4)/s/errorMsg/errorMsg4/' \
	        -e '/arg3)/s/errorMsg/errorMsg3/' \
		-e '/arg2)/s/errorMsg/errorMsg2/' \
		-e '/arg1)/s/errorMsg/errorMsg1/' ; \
	 done; \
	 echo 'BundleTest.checkForErrors();' ;	) | \
	sed -e 's/errorMsg/BundleTest.errorMsg/g'
