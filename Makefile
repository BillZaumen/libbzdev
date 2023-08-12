# GNU Make file.
#
DATE = (shell date -R)
#
# Set this if  'make install' should install its files into a
# user directory - useful for package systems that will grab
# all the files they see.  Setting this will allow a package
# to be built without requiring root permissions.
#
DESTDIR =

# bzdevjlib version number.  Used to name JAR files.  The files MAJOR
# and MINOR contains the major and minor version numbers, and BUILD is
# set by counting parent commits until the major or minor version
# numbers change (starting from the current commit). Then VERSION is
# set to MAJOR.MINOR.BUILD
#
VERSION = $(shell echo `cat $(JROOT)/MAJOR`.`cat $(JROOT)/MINOR`)$(BUILD)

# lsnof and scrunner version number
UVERSION = $(shell echo `cat $(JROOT)/UMAJOR`.`cat $(JROOT)/UMINOR`)

#
# The PKG_VERSION is the VERSION with an additional string for commits
# that are not along the Git master branch.  The intention is to allow
# one to create test/debug versions of the libary using Git branches.
#
PKG_VERSION = $(VERSION)$(PKG_BUILD)


JROOT := $(shell while [ ! -d src -a `pwd` != / ] ; do cd .. ; done ; pwd)

JAVA_VERSION = 11
JAVAC = javac --release $(JAVA_VERSION)
JAVADOC = javadoc --release $(JAVA_VERSION) -public -protected  \
	-Xdoclint:all,-missing,-html

JAVADOC_VERSION = $(shell javadoc --version | sed -e 's/javadoc //' \
		| sed -e 's/[.].*//')

all: jars

include VersionVars.mk

# manipulate version numbers - must be after default rule.
include MajorMinor.mk

#
# Set DARKMODE to --darkmode to turn on dark mode.
#
DARKMODE =

MIMETYPES_DIR = mimetypes
APPS_DIR = apps
# System directories
#
SYS_APPDIR = /usr/share/applications
SYS_APP_ICON_DIR = $(SYS_ICON_DIR)/scalable/$(APPS_DIR)
SYS_JARDIRECTORY=/usr/share/bzdev
SYS_DOCDIR = /usr/share/doc/libbzdev-java
SYS_LIBJARDIR = /usr/share/java
SYS_API_DOCDIR = /usr/share/doc/libbzdev-doc
SYS_JAVADOCS = $(SYS_API_DOCDIR)/api
SYS_EXAMPLES = $(SYS_API_DOCDIR)/examples
SYS_BINDIR = /usr/bin
SYS_MANDIR = /usr/share/man
SYS_MIMEDIR = /usr/share/mime
SYS_ICON_DIR = /usr/share/icons/hicolor
SYS_POPICON_DIR = /usr/share/icons/Pop
SYS_APP_POPICON_DIR = $(SYS_POPICON_DIR)/scalable/$(APPS_DIR)
SYS_MIME_ICON_DIR = $(SYS_ICON_DIR)/scalable/$(MIMETYPES_DIR)
SYS_MIME_POPICON_DIR = $(SYS_POPICON_DIR)/scalable/$(MIMETYPES_DIR)
SYS_CONFIGDIR = /etc/bzdev
SYS_EMACSLISPDIR = /usr/share/emacs/site-lisp
SYS_EMACSSTARTDIR = /etc/emacs/site-start.d
ICON_WIDTHS = 16 20 22 24 32 36 48 64 72 96 128 192 256 512
ICON_WIDTHS2x = 16 24 32 48 64 128 256 512

POPICON_WIDTHS = 8 16 24 32 48 64 128 256
POPICON_WIDTHS2x = 8 16 24 32 48 64 128 256


#
# Target JARDIRECTORY - where 'make install' actually puts the jar
# file (DESTDIR is not null when creating packages)
#
JARDIRECTORY = $(DESTDIR)$(SYS_JARDIRECTORY)
#
# JARDIRECTORY modified so that it can appear in a sed command
#
JARDIR=$(shell echo $(SYS_JARDIRECTORY) | sed  s/\\//\\\\\\\\\\//g)

#
# Name for ImageSeq.svg based on Media Type as required by
# freedesktop.org - the media type with '/' replaced by '-'
# and ending with the file-type extension.
#
IMAGE_SEQUENCE_ICON = image-vnd.bzdev.image-sequence+zip

SBL_TARGETICON = SBLauncher.svg
SBL_TARGETICON_PNG = SBLauncher.png

SBL_CONF_ICON = application-vnd.bzdev.sblauncher

# Target for the standard Java-library directory
LIBJARDIR = $(DESTDIR)$(SYS_LIBJARDIR)

LIBJARDIR_SED=$(shell echo $(SYS_LIBJARDIR) | sed  s/\\//\\\\\\\\\\//g)

# Other target directories

DOCDIR = $(DESTDIR)$(SYS_DOCDIR)
API_DOCDIR = $(DESTDIR)$(SYS_API_DOCDIR)
JAVADOCS = $(DESTDIR)$(SYS_JAVADOCS)
EXAMPLES = $(DESTDIR)$(SYS_EXAMPLES)
BINDIR = $(DESTDIR)$(SYS_BINDIR)
MANDIR = $(DESTDIR)$(SYS_MANDIR)
MIMEDIR = $(DESTDIR)$(SYS_MIMEDIR)
APPDIR = $(DESTDIR)$(SYS_APPDIR)
MIME_ICON_DIR = $(DESTDIR)$(SYS_MIME_ICON_DIR)
MIME_POPICON_DIR = $(DESTDIR)$(SYS_MIME_POPICON_DIR)
APP_ICON_DIR = $(DESTDIR)$(SYS_APP_ICON_DIR)
APP_POPICON_DIR = $(DESTDIR)$(SYS_APP_POPICON_DIR)
ICON_DIR = $(DESTDIR)$(SYS_ICON_DIR)
POPICON_DIR = $(DESTDIR)$(SYS_POPICON_DIR)
CONFIGDIR = $(DESTDIR)$(SYS_CONFIGDIR)
EMACSLISPDIR=$(DESTDIR)$(SYS_EMACSLISPDIR)
EMACSSTARTDIR = $(DESTDIR)$(SYS_EMACSSTARTDIR)

# Shortcut for package directory names
#
BZDEV = org/bzdev

# Source directories for each module
#
BASE_DIR = ./src/org.bzdev.base
SERVLETS_DIR = ./src/org.bzdev.servlets
ESP_DIR = ./src/org.bzdev.esp
DMETHODS_DIR = ./src/org.bzdev.dmethods
MATH_DIR = ./src/org.bzdev.math
OBNAMING_DIR = ./src/org.bzdev.obnaming
PARMPROC_DIR = ./src/org.bzdev.parmproc
GRAPHICS_DIR = ./src/org.bzdev.graphics
DESKTOP_DIR = ./src/org.bzdev.desktop
DEVQSIM_DIR = ./src/org.bzdev.devqsim
DRAMA_DIR = ./src/org.bzdev.drama
ANIM2D_DIR = ./src/org.bzdev.anim2d
EJWS_DIR = ./src/org.bzdev.ejws
P3D_DIR = ./src/org.bzdev.p3d
LSNOF_DIR = ./src/org.bzdev.lsnof
SCRUNNER_DIR  = ./src/org.bzdev.scrunner
YRUNNER_DIR = ./src/org.bzdev.yrunner
SBL_DIR = ./src/org.bzdev.sbl

SHORT_MODULE_NAMES = base servlets esp dmethods math obnaming parmproc \
	graphics desktop devqsim drama anim2d p3d ejws


# The module-info.java files for each module
#
BASE_MODINFO = $(BASE_DIR)/module-info.java
ESP_MODINFO = $(ESP_DIR)/module-info.java
DMETHODS_MODINFO = $(DMETHODS_DIR)/module-info.java
MATH_MODINFO = $(MATH_DIR)/module-info.java
OBNAMING_MODINFO = $(OBNAMING_DIR)/module-info.java
PARMPROC_MODINFO = $(PARMPROC_DIR)/module-info.java
GRAPHICS_MODINFO = $(GRAPHICS_DIR)/module-info.java
DESKTOP_MODINFO = $(DESKTOP_DIR)/module-info.java
DEVQSIM_MODINFO = $(DEVQSIM_DIR)/module-info.java
DRAMA_MODINFO = $(DRAMA_DIR)/module-info.java
ANIM2D_MODINFO = $(ANIM2D_DIR)/module-info.java
EJWS_MODINFO = $(EJWS_DIR)/module-info.java
P3D_MODINFO = $(P3D_DIR)/module-info.java
LSNOF_MODINFO = $(LSNOF_DIR)/module-info.java
SCRUNNER_MODINFO = $(SCRUNNER_DIR)/module-info.java
YRUNNER_MODINFO = $(YRUNNER_DIR)/module-info.java
SBL_MODINFO = $(SBL_DIR)/module-info.java

# Names of services. There will be corrsponding entries in
# .../META-INF/services and/or a series of provides SERVICE with ...
# statements in a module declaration.
#
ANNOTATION_PROC = javax.annotation.processing.Processor
NOF_SERVICE = org.bzdev.obnaming.NamedObjectFactory
FFT_SERVICE = org.bzdev.math.spi.FFTProvider
GRAPH_SYMBOL_SERVICE = org.bzdev.graphs.spi.SymbolProvider
OSG_SERVICE = org.bzdev.gio.spi.OSGProvider
ONL_SERVICE = org.bzdev.obnaming.spi.ONLauncherProvider
ONLD_SERVICE = org.bzdev.lang.spi.ONLauncherData
SCRIPTING_API_SERVICE = javax.script.ScriptEngineFactory

# Policy file for programs such as scrunner but when placed in
# the BUILD directory for testing.
#
BLDPOLICY = $(JROOT)/BUILD/libbzdev.policy

# The absolute pathname for the BUILD directory, altered so that
# it can be used as a replacement string in a 'sed' script.
#
BUILD_SED = $(shell echo $(JROOT)/BUILD | sed  s/\\//\\\\\\\\\\//g)

# Variables used to build the org.bzdev.base module's jar file
#
BASE_JFILES = $(wildcard $(BASE_DIR)/$(BZDEV)/lang/*.java) \
	$(wildcard $(BASE_DIR)/$(BZDEV)/lang/spi/*.java) \
	$(wildcard $(BASE_DIR)/$(BZDEV)/lang/annotations/*.java) \
	$(wildcard $(BASE_DIR)/$(BZDEV)/io/*.java) \
	$(wildcard $(BASE_DIR)/$(BZDEV)/net/*.java) \
	$(wildcard $(BASE_DIR)/$(BZDEV)/net/calendar/*.java) \
	$(wildcard $(BASE_DIR)/$(BZDEV)/obnaming/annotations/*.java) \
	$(wildcard $(BASE_DIR)/$(BZDEV)/protocols/*.java) \
	$(wildcard $(BASE_DIR)/$(BZDEV)/protocols/resource/*.java) \
	$(wildcard $(BASE_DIR)/$(BZDEV)/protocols/sresource/*.java) \
	$(wildcard $(BASE_DIR)/$(BZDEV)/scripting/*.java) \
	$(wildcard $(BASE_DIR)/$(BZDEV)/util/*.java) \
	$(wildcard $(BASE_DIR)/$(BZDEV)/util/units/*.java)
BASE_JFILES2 = $(BASE_DIR)/$(BZDEV)/io/lpack/DefaultClass.java \
	$(BASE_DIR)/$(BZDEV)/lang/lpack/DefaultClass.java \
	$(BASE_DIR)/$(BZDEV)/net/calendar/lpack/DefaultClass.java \
	$(BASE_DIR)/$(BZDEV)/net/lpack/DefaultClass.java \
	$(BASE_DIR)/$(BZDEV)/protocols/resource/lpack/DefaultClass.java \
	$(BASE_DIR)/$(BZDEV)/protocols/sresource/lpack/DefaultClass.java \
	$(BASE_DIR)/$(BZDEV)/scripting/lpack/DefaultClass.java \
	$(BASE_DIR)/$(BZDEV)/util/lpack/DefaultClass.java


BASE_RESOURCES1 = $(wildcard $(BASE_DIR)/$(BZDEV)/io/lpack/*.properties) \
	$(wildcard $(BASE_DIR)/$(BZDEV)/lang/*.dtd) \
	$(wildcard $(BASE_DIR)/$(BZDEV)/lang/*.xml) \
	$(wildcard $(BASE_DIR)/$(BZDEV)/lang/lpack/*.properties) \
	$(wildcard $(BASE_DIR)/$(BZDEV)/math/lpack/*.properties) \
	$(wildcard $(BASE_DIR)/$(BZDEV)/math/stats/lpack/*.properties) \
	$(wildcard $(BASE_DIR)/$(BZDEV)/net/lpack/*.properties) \
	$(wildcard $(BASE_DIR)/$(BZDEV)/net/calendar/lpack/*.properties) \
	$(wildcard $(BASE_DIR)/$(BZDEV)/protocols/resource/lpack/*.properties) \
	$(wildcard $(BASE_DIR)/$(BZDEV)/protocols/sresource/lpack/*.properties)\
	$(wildcard $(BASE_DIR)/$(BZDEV)/scripting/*.xml) \
	$(wildcard $(BASE_DIR)/$(BZDEV)/scripting/lpack/*.properties) \
	$(wildcard $(BASE_DIR)/$(BZDEV)/util/lpack/espdocs.tpl) \
	$(wildcard $(BASE_DIR)/$(BZDEV)/util/lpack/*.properties) \
	$(wildcard $(BASE_DIR)/$(BZDEV)/util/SharedMimeInfoStart.xml) \
	$(wildcard $(BASE_DIR)/$(BZDEV)/util/SharedMimeInfo.xml) \
	$(wildcard $(BASE_DIR)/$(BZDEV)/util/SharedMimeInfoEnd.xml)


BASE_RESOURCES = $(subst ./src/,,$(BASE_RESOURCES1))

# ones that should be stored with each line ending in CRLF
BASE_RESOURCES2 = $(wildcard $(BASE_DIR)/$(BZDEV)/net/calendar/icalEvents.tpl)

BASE_RESOURCES_CRLF = $(subst ./src/,,$(BASE_RESOURCES2))

SERVLETS_JFILES = $(SERVLETS_DIR)/module-info.java \
	$(SERVLETS_DIR)/$(BZDEV)/net/servlets/package-info.java \
	$(SERVLETS_DIR)/$(BZDEV)/net/servlets/EncapsulatingServlet.java

SERVLETS_BUILD_PATH1 = /usr/share/java/servlet-api.jar
SERVLETS_BUILD_PATH = BUILD/libbzdev-base.jar:$(SERVLETS_BUILD_PATH1)


# Variables used to build the org.bzdev.esp module's jar file
ESP_JFILES = $(wildcard $(ESP_DIR)/$(BZDEV)/providers/esp/*.java)
ESP_JFILES2 = $(ESP_DIR)/$(BZDEV)/providers/esp/lpack/DefaultClass.java

ESP_RESOURCES1 = $(ESP_DIR)/META-INF/services/$(SCRIPTING_API_SERVICE) \
	$(wildcard $(ESP_DIR)/$(BZDEV)/providers/esp/lpack/*.properties)

ESP_RESOURCES = $(subst ./src/,,/$(ESP_RESOURCES1))


# Variables used to build the org.bzdev.dmethod module's jar file
#

DMETHODS_JFILES = $(wildcard $(DMETHODS_DIR)/$(BZDEV)/lang/processor/*.java)

DMETHODS_RESOURCES1=$(wildcard $(DMETHODS_DIR)/$(BZDEV)/lang/processor/*.tpl) \
	$(DMETHODS_DIR)/META-INF/services/$(ANNOTATION_PROC)

DMETHODS_RESOURCES = $(subst ./src/,,$(DMETHODS_RESOURCES1))

# Variables used to build the org.bzdev.math modeule's jar file
MATH_JFILES = $(wildcard $(MATH_DIR)/$(BZDEV)/math/*.java) \
	$(wildcard $(MATH_DIR)/$(BZDEV)/math/spi/*.java) \
	$(wildcard $(MATH_DIR)/$(BZDEV)/math/stats/*.java) \
	$(wildcard $(MATH_DIR)/$(BZDEV)/math/rv/*.java) \
	$(wildcard $(MATH_DIR)/$(BZDEV)/providers/math/fft/*.java)
MATH_JFILES2 = $(MATH_DIR)/$(BZDEV)/providers/math/MathLauncherData.java \
	$(MATH_DIR)/$(BZDEV)/providers/math/StatsLauncherData.java \
	$(MATH_DIR)/$(BZDEV)/providers/math/RVLauncherData.java \
	$(MATH_DIR)/$(BZDEV)/providers/math/lpack/DefaultClass.java \
	$(MATH_DIR)/$(BZDEV)/math/lpack/DefaultClass.java \
	$(MATH_DIR)/$(BZDEV)/math/rv/lpack/DefaultClass.java \
	$(MATH_DIR)/$(BZDEV)/math/stats/lpack/DefaultClass.java

MATH_RESOURCES1 = $(MATH_DIR)/META-INF/services/$(FFT_SERVICE) \
	$(MATH_DIR)/META-INF/services/$(ONLD_SERVICE) \
	$(MATH_DIR)/$(BZDEV)/providers/math/MathLauncherData.yaml \
	$(MATH_DIR)/$(BZDEV)/providers/math/StatsLauncherData.yaml \
	$(MATH_DIR)/$(BZDEV)/providers/math/RVLauncherData.yaml \
	$(wildcard $(MATH_DIR)/$(BZDEV)/providers/math/lpack/*.properties) \
	$(wildcard $(MATH_DIR)/$(BZDEV)/math/lpack/*.properties) \
	$(wildcard $(MATH_DIR)/$(BZDEV)/math/stats/lpack/*.properties) \
	$(wildcard $(MATH_DIR)/$(BZDEV)/math/rv/lpack/*.properties)
MATH_RESOURCES = $(subst ./src/,,$(MATH_RESOURCES1))

BASE_MATH_BUILD_PATH = BUILD/libbzdev-base.jar:BUILD/libbzdev-math.jar

# Variables used to build the org.bzdev.obnaming module's jar file
#
OBNAMING_JFILES = $(wildcard $(OBNAMING_DIR)/$(BZDEV)/obnaming/*.java) \
	$(wildcard $(OBNAMING_DIR)/$(BZDEV)/obnaming/annotations/*.java) \
	$(wildcard $(OBNAMING_DIR)/$(BZDEV)/obnaming/spi/*.java)

OBNAMING_JFILES1 = \
	$(OBNAMING_DIR)/$(BZDEV)/providers/obnaming/DefaultLauncherProvider.java

OBNAMING_JFILES2 = \
	$(OBNAMING_DIR)/$(BZDEV)/obnaming/lpack/DefaultClass.java \
	$(OBNAMING_DIR)/$(BZDEV)/providers/obnaming/lpack/DefaultClass.java

OBNAMING_PROVIDER_LPACK =  $(OBNAMING_DIR)/$(BZDEV)/providers/obnaming/lpack

OBNAMING_RESOURCES1 = \
	$(OBNAMING_DIR)/META-INF/services/$(ONL_SERVICE) \
	$(wildcard $(OBNAMING_DIR)/$(BZDEV)/obnaming/*.xml) \
	$(wildcard $(OBNAMING_DIR)/$(BZDEV)/obnaming/*.yaml) \
	$(OBNAMING_PROVIDER_LPACK)/DefaultLauncher.properties \
	$(wildcard $(OBNAMING_DIR)/$(BZDEV)/obnaming/lpack/*.properties)

OBNAMING_RESOURCES = $(subst ./src/,,$(OBNAMING_RESOURCES1))

# Variables used to build the org.bzdev.parmproc module's jar file
#
PARMPROC_JFILES = \
	$(wildcard $(PARMPROC_DIR)/$(BZDEV)/obnaming/processor/*.java)

PARMPROC_RESOURCES1 = \
	$(wildcard $(PARMPROC_DIR)/$(BZDEV)/obnaming/processor/*.tpl) \
	$(PARMPROC_DIR)/META-INF/services/$(ANNOTATION_PROC)

PARMPROC_RESOURCES = $(subst ./src/,,$(PARMPROC_RESOURCES1))

PROC_PATH1 = BUILD/libbzdev-base.jar
PROC_PATH2 = BUILD/libbzdev-dmethods.jar
PROC_PATH3 = BUILD/libbdev-obnaming.jar
PROC_PATH4 = BUILD/libbzdev-parmproc.jar
PROC_PATH = $(PROC_PATH1):$(PROC_PATH2):$(PROC_PATH3):$(PROC_PATH4)

# Variables used to build the org.bzdev.graphics module's jar file
#
GRAPHICS_JFILES = $(wildcard $(GRAPHICS_DIR)/$(BZDEV)/geom/*.java) \
	$(wildcard $(GRAPHICS_DIR)/$(BZDEV)/gio/*.java) \
	$(wildcard $(GRAPHICS_DIR)/$(BZDEV)/gio/spi/*.java) \
	$(wildcard $(GRAPHICS_DIR)/$(BZDEV)/graphs/*.java) \
	$(wildcard $(GRAPHICS_DIR)/$(BZDEV)/graphs/spi/*.java) \
	$(wildcard $(GRAPHICS_DIR)/$(BZDEV)/graphs/symbols/*.java) \
	$(wildcard $(GRAPHICS_DIR)/$(BZDEV)/imageio/*.java) \
	$(wildcard $(GRAPHICS_DIR)/$(BZDEV)/obnaming/misc/*.java)

GRAPHICS_JFILES2 = \
	$(GRAPHICS_DIR)/$(BZDEV)/geom/lpack/DefaultClass.java \
	$(GRAPHICS_DIR)/$(BZDEV)/providers/graphics/*.java \
	$(GRAPHICS_DIR)/$(BZDEV)/providers/graphics/lpack/DefaultClass.java \
	$(GRAPHICS_DIR)/$(BZDEV)/obnaming/misc/lpack/DefaultClass.java \
	$(GRAPHICS_DIR)/$(BZDEV)/gio/lpack/DefaultClass.java \
	$(GRAPHICS_DIR)/$(BZDEV)/graphs/lpack/DefaultClass.java \
	$(GRAPHICS_DIR)/$(BZDEV)/imageio/lpack/DefaultClass.java

GLD = GraphicsLauncherData

GRAPHICS_RESOURCES1 = $(GRAPHICS_DIR)/META-INF/services/$(OSG_SERVICE) \
	$(GRAPHICS_DIR)/META-INF/services/$(GRAPH_SYMBOL_SERVICE) \
	$(GRAPHICS_DIR)/META-INF/services/$(ONLD_SERVICE) \
	$(GRAPHICS_DIR)/$(BZDEV)/providers/graphics/GraphicsLauncherData.yaml \
	$(GRAPHICS_DIR)/$(BZDEV)/providers/graphics/lpack/$(GLD).properties \
	$(wildcard $(GRAPHICS_DIR)/$(BZDEV)/geom/*.xml) \
	$(wildcard $(GRAPHICS_DIR)/$(BZDEV)/geom/lpack/*.properties) \
	$(wildcard $(GRAPHICS_DIR)/$(BZDEV)/gio/lpack/*.properties) \
	$(wildcard $(GRAPHICS_DIR)/$(BZDEV)/graphs/lpack/*.properties) \
	$(wildcard $(GRAPHICS_DIR)/$(BZDEV)/imageio/lpack/*.properties) \
	$(wildcard $(GRAPHICS_DIR)/$(BZDEV)/obnaming/misc/lpack/*.properties)

GRAPHICS_RESOURCES = $(subst ./src/,,$(GRAPHICS_RESOURCES1))
GRAPHICS_BUILD_PATH = $(BASE_MATH_BUILD_PATH):BUILD/libbzdev-obnaming.jar


# Variables used to build the org.bzdev.desktop module's jar file
#
DESKTOP_JFILES = $(wildcard $(DESKTOP_DIR)/$(BZDEV)/swing/*.java) \
	$(wildcard $(DESKTOP_DIR)/$(BZDEV)/swing/io/*.java) \
	$(wildcard $(DESKTOP_DIR)/$(BZDEV)/swing/keys/*.java) \
	$(wildcard $(DESKTOP_DIR)/$(BZDEV)/swing/proxyconf/*.java) \
	$(wildcard $(DESKTOP_DIR)/$(BZDEV)/swing/table/*.java) \
	$(wildcard $(DESKTOP_DIR)/$(BZDEV)/swing/text/*.java)

DESKTOP_JFILES2 = $(wildcard $(DESKTOP_DIR)/$(BZDEV)/providers/swing/*.java) \
	$(DESKTOP_DIR)/$(BZDEV)/providers/swing/lpack/DefaultClass.java \
	$(DESKTOP_DIR)/$(BZDEV)/swing/io/lpack/DefaultClass.java \
	$(DESKTOP_DIR)/$(BZDEV)/swing/keys/lpack/DefaultClass.java \
	$(DESKTOP_DIR)/$(BZDEV)/swing/lpack/DefaultClass.java \
	$(DESKTOP_DIR)/$(BZDEV)/swing/proxyconf/lpack/DefaultClass.java \
	$(DESKTOP_DIR)/$(BZDEV)/swing/table/lpack/DefaultClass.java

DESKTOP_ICONS_PNG = $(DESKTOP_DIR)/org/bzdev/swing/icons/aleftRVD.png \
	$(DESKTOP_DIR)/org/bzdev/swing/icons/aleftRV.png \
	$(DESKTOP_DIR)/org/bzdev/swing/icons/aleft.png \
	$(DESKTOP_DIR)/org/bzdev/swing/icons/arightRVD.png \
	$(DESKTOP_DIR)/org/bzdev/swing/icons/arightRV.png \
	$(DESKTOP_DIR)/org/bzdev/swing/icons/aright.png \
	$(DESKTOP_DIR)/org/bzdev/swing/icons/cameraRVD.png \
	$(DESKTOP_DIR)/org/bzdev/swing/icons/cameraRV.png \
	$(DESKTOP_DIR)/org/bzdev/swing/icons/camera.png \
	$(DESKTOP_DIR)/org/bzdev/swing/icons/pauseRV.png \
	$(DESKTOP_DIR)/org/bzdev/swing/icons/pause.png \
	$(DESKTOP_DIR)/org/bzdev/swing/icons/playRV.png \
	$(DESKTOP_DIR)/org/bzdev/swing/icons/play.png


DESKTOP_RESOURCES1 = $(wildcard $(DESKTOP_DIR)/$(BZDEV)/swing/*.dtd) \
	$(DESKTOP_DIR)/META-INF/services/$(ONLD_SERVICE) \
	$(wildcard $(DESKTOP_DIR)/$(BZDEV)/providers/swing/*.yaml) \
	$(wildcard $(DESKTOP_DIR)/$(BZDEV)/providers/swing/lpack/*.properties) \
	$(wildcard $(DESKTOP_DIR)/$(BZDEV)/swing/lpack/*.properties) \
	$(wildcard $(DESKTOP_DIR)/$(BZDEV)/swing/icons/*.gif) \
	$(DESKTOP_ICONS_PNG) \
	$(wildcard $(DESKTOP_DIR)/$(BZDEV)/swing/io/lpack/*.properties) \
	$(wildcard $(DESKTOP_DIR)/$(BZDEV)/swing/keys/lpack/*.properties) \
	$(wildcard $(DESKTOP_DIR)/$(BZDEV)/swing/proxyconf/lpack/*.properties) \
	$(wildcard $(DESKTOP_DIR)/$(BZDEV)/swing/proxyconf/lpack/*.png) \
	$(wildcard $(DESKTOP_DIR)/$(BZDEV)/swing/proxyconf/lpack/*.css) \
	$(wildcard $(DESKTOP_DIR)/$(BZDEV)/swing/proxyconf/lpack/*.html) \
	$(wildcard $(DESKTOP_DIR)/$(BZDEV)/swing/table/lpack/*.properties)


DESKTOP_RESOURCES = $(subst ./src/,,$(DESKTOP_RESOURCES1))
DESKTOP_BUILD_PATH = $(GRAPHICS_BUILD_PATH):BUILD/libbzdev-graphics.jar


# Variables used to build the org.bzdev.devqsim module's jar file
#
DEVQSIM_JFILES = $(wildcard $(DEVQSIM_DIR)/$(BZDEV)/devqsim/*.java) \
		$(wildcard $(DEVQSIM_DIR)/$(BZDEV)/devqsim/rv/*.java)

DEVQSIM_JFILES2 = $(wildcard $(DEVQSIM_DIR)/$(BZDEV)/providers/devqsim/*.java) \
	$(DEVQSIM_DIR)/$(BZDEV)/providers/devqsim/lpack/DefaultClass.java \
	$(DEVQSIM_DIR)/$(BZDEV)/devqsim/lpack/DefaultClass.java \
	$(DEVQSIM_DIR)/$(BZDEV)/devqsim/rv/lpack/DefaultClass.java
TMP1 = $(wildcard $(DEVQSIM_DIR)/$(BZDEV)/providers/devqsim/lpack/*.properties)
DEVQSIM_RESOURCES1 = $(DEVQSIM_DIR)/META-INF/services/$(NOF_SERVICE) \
	$(DEVQSIM_DIR)/META-INF/services/$(ONL_SERVICE) \
	$(DEVQSIM_DIR)/META-INF/services/$(ONLD_SERVICE) \
	$(TMP1) \
	$(wildcard $(DEVQSIM_DIR)/$(BZDEV)/devqsim/lpack/*.properties) \
	$(wildcard $(DEVQSIM_DIR)/$(BZDEV)/devqsim/rv/lpack/*.properties) \
	$(DEVQSIM_DIR)/$(BZDEV)/devqsim/SimulationLauncher.yaml \
	$(DEVQSIM_DIR)/$(BZDEV)/providers/devqsim/SimulationRVLauncherData.yaml
DEVQSIM_RESOURCES = $(subst ./src/,,$(DEVQSIM_RESOURCES1))
DEVQSIM_BUILD_PATH =$(BASE_MATH_BUILD_PATH):BUILD/libbzdev-obnaming.jar

DRAMA_JFILES = $(wildcard $(DRAMA_DIR)/$(BZDEV)/drama/*.java) \
	$(wildcard $(DRAMA_DIR)/$(BZDEV)/drama/common/*.java) \
	$(wildcard $(DRAMA_DIR)/$(BZDEV)/drama/generic/*.java)

DRAMA_JFILES2 = $(wildcard $(DRAMA_DIR)/$(BZDEV)/providers/drama/*.java) \
	$(DRAMA_DIR)/$(BZDEV)/drama/common/lpack/DefaultClass.java \
	$(DRAMA_DIR)/$(BZDEV)/drama/generic/lpack/DefaultClass.java \
	$(DRAMA_DIR)/$(BZDEV)/drama/lpack/DefaultClass.java \
	$(DRAMA_DIR)/$(BZDEV)/providers/drama/lpack/DefaultClass.java

DRAMA_RESOURCES1 = $(DRAMA_DIR)/META-INF/services/$(NOF_SERVICE) \
	$(DRAMA_DIR)/META-INF/services/$(ONL_SERVICE) \
	$(wildcard $(DRAMA_DIR)/$(BZDEV)/drama/*.yaml) \
	$(wildcard $(DRAMA_DIR)/$(BZDEV)/drama/lpack/*.properties) \
	$(wildcard $(DRAMA_DIR)/$(BZDEV)/providers/drama/lpack/*.properties) \
	$(wildcard $(DRAMA_DIR)/$(BZDEV)/drama/common/lpack/*.properties) \
	$(wildcard $(DRAMA_DIR)/$(BZDEV)/drama/generic/lpack/*.properties)
DRAMA_RESOURCES = $(subst ./src/,,$(DRAMA_RESOURCES1))
DRAMA_BUILD_PATH=$(DEVQSIM_BUILD_PATH):BUILD/libbzdev-devqsim.jar

# Variables used to build the org.bzdev.anim2d module's jar file
#
ANIM2D_JFILES =  $(wildcard $(ANIM2D_DIR)/$(BZDEV)/anim2d/*.java)
ANIM2D_JFILES2 = $(wildcard $(ANIM2D_DIR)/$(BZDEV)/providers/anim2d/*.java) \
	$(ANIM2D_DIR)/$(BZDEV)/anim2d/lpack/DefaultClass.java \
	$(ANIM2D_DIR)/$(BZDEV)/providers/anim2d/lpack/DefaultClass.java

ANIM2D_RESOURCES1 = $(ANIM2D_DIR)/META-INF/services/$(NOF_SERVICE) \
	$(ANIM2D_DIR)/META-INF/services/$(ONL_SERVICE) \
	$(wildcard $(ANIM2D_DIR)/$(BZDEV)/providers/anim2d/lpack/*.properties) \
	$(wildcard $(ANIM2D_DIR)/$(BZDEV)/anim2d/lpack/*.properties) \
	$(wildcard $(ANIM2D_DIR)/$(BZDEV)/anim2d/Animation2DLauncher.yaml)
ANIM2D_RESOURCES = $(subst ./src/,,$(ANIM2D_RESOURCES1))
ANIM2D_BUILD_PATH1=$(DEVQSIM_BUILD_PATH):BUILD/libbzdev-graphics.jar
ANIM2D_BUILD_PATH = $(ANIM2D_BUILD_PATH1):BUILD/libbzdev-devqsim.jar

# Variables used to build the org.bzdev.p3d module's jar file
#
P3D_JFILES =  $(wildcard $(P3D_DIR)/$(BZDEV)/p3d/*.java)
P3D_JFILES2 = $(wildcard $(P3D_DIR)/$(BZDEV)/providers/p3d/*.java) \
	$(P3D_DIR)/$(BZDEV)/p3d/lpack/DefaultClass.java \
	$(P3D_DIR)/$(BZDEV)/providers/p3d/lpack/DefaultClass.java


P3D_RESOURCES1 = $(P3D_DIR)/META-INF/services/$(NOF_SERVICE) \
	$(P3D_DIR)/META-INF/services/$(ONLD_SERVICE) \
	$(wildcard $(P3D_DIR)/$(BZDEV)/providers/p3d/lpack/*.properties) \
	$(wildcard $(P3D_DIR)/$(BZDEV)/providers/p3d/*.yaml) \
	$(wildcard $(P3D_DIR)/$(BZDEV)/p3d/lpack/*.properties) \
	$(wildcard $(P3D_DIR)/$(BZDEV)/p3d/*.tpl)

P3D_RESOURCES = $(subst ./src/,,$(P3D_RESOURCES1))
P3D_BUILD_PATH=$(ANIM2D_BUILD_PATH):BUILD/libbzdev-anim2d.jar

# Variables used to build the org.bzdev.ejws module's jar file
#
EJWS_JFILES =  $(wildcard $(EJWS_DIR)/$(BZDEV)/ejws/*.java) \
	$(wildcard $(EJWS_DIR)/$(BZDEV)/ejws/maps/*.java)

DEFAULT_CERTS = $(EJWS_DIR)/$(BZDEV)/ejws/defaultCerts

EJWS_RESOURCES1 = \
	$(wildcard $(EJWS_DIR)/$(BZDEV)/ejws/*.tpl) \
	$(wildcard $(EJWS_DIR)/$(BZDEV)/ejws/*.jsp) \
	$(wildcard $(EJWS_DIR)/$(BZDEV)/ejws/lpack/*.properties) \
	$(wildcard $(EJWS_DIR)/$(BZDEV)/ejws/maps/lpack/*.properties) \
	$(DEFAULT_CERTS)

EJWS_RESOURCES = $(subst ./src/,,$(EJWS_RESOURCES1))
# EJWS_BUILD_PATH=BUILD/libbzdev-base.jar:BUILD:libbzdev-desktop.jar
EJWS_BUILD_PATH=BUILD/libbzdev-base.jar

#
# All library source files that are documented (we don't document
# annotation processors because those are seen by the compiler, not
# users).
#
LIB_SOURCE_FILES = $(BASE_JFILES) $(OBNAMING_JFILES) $(DESKTOP_JFILES) \
	$(DEVQSIM_JFILES) $(MATH_JFILES) $(DRAMA_JFILES) $(ANIM2D_JFILES) \
	$(EJWS_JFILES) $(P3D_JFILES)  $(GRAPHICS_JFILES)\
	$(BASE_MODINFO) $(OBNAMING_MODINFO) $(DESKTOP_MODINFO) \
	$(DEVQSIM_MODINFO) $(DRAMA_MODINFO) $(ANIM2D_MODINFO) \
	$(EJWS_MODINFO) $(P3D_MODINFO)


# Variables used to build the org.bzdev.lsnof module's jar file
#
LSNOF_JFILES = $(wildcard $(LSNOF_DIR)/$(BZDEV)/bin/lsnof/*.java)
LSNOF_JFILES2 = $(LSNOF_DIR)/$(BZDEV)/bin/lsnof/lpack/DefaultClass.java

LSNOF_RESOURCES1 = \
	$(wildcard $(LSNOF_DIR)/$(BZDEV)/bin/lsnof/*.html) \
	$(wildcard $(LSNOF_DIR)/$(BZDEV)/bin/lsnof/*.properties) \
	$(wildcard $(LSNOF_DIR)/$(BZDEV)/bin/lsnof/*.tpl) \
	$(wildcard $(LSNOF_DIR)/$(BZDEV)/bin/lsnof/lpack/*.properties)
LSNOF_RESOURCES = $(subst ./src/,,$(LSNOF_RESOURCES1))

# Variables used to build the org.bzdev.scrunner module's jar file
#
SCRUNNER_JFILES = $(wildcard $(SCRUNNER_DIR)/$(BZDEV)/bin/scrunner/*.java)
SCRUNNER_JFILES2 =$(SCRUNNER_DIR)/$(BZDEV)/bin/scrunner/lpack/DefaultClass.java

SCRUNNER_RESOURCES1 = \
	$(wildcard $(SCRUNNER_DIR)/$(BZDEV)/bin/scrunner/lpack/*.properties)
SCRUNNER_RESOURCES = $(subst ./src/,,$(SCRUNNER_RESOURCES1))

# Variable used to build the org.bzdev.yrunner module's jar file
#
YRUNNER_JFILES = $(wildcard $(YRUNNER_DIR)/$(BZDEV)/bin/yrunner/*.java)
YRUNNER_JFILES2 =$(YRUNNER_DIR)/$(BZDEV)/bin/yrunner/lpack/DefaultClass.java

YRUNNER_RESOURCES1 = \
	$(wildcard $(YRUNNER_DIR)/$(BZDEV)/bin/yrunner/*.tpl) \
	$(wildcard $(YRUNNER_DIR)/$(BZDEV)/bin/yrunner/lpack/*.tpl) \
	$(wildcard $(YRUNNER_DIR)/$(BZDEV)/bin/yrunner/lpack/*.properties)
YRUNNER_RESOURCES = $(subst ./src/,,$(YRUNNER_RESOURCES1))

# Variable used to build the org.bzdev.sbl module's jar file
#
SBL_JFILES = $(wildcard $(SBL_DIR)/$(BZDEV)/bin/sbl/*.java)
SBL_JFILES2 =$(SBL_DIR)/$(BZDEV)/bin/sbl/lpack/DefaultClass.java

SBL_RESOURCES1 = \
	$(wildcard $(SBL_DIR)/$(BZDEV)/bin/sbl/lpack/*.properties)
SBL_RESOURCES = $(subst ./src/,,$(SBL_RESOURCES1))



all: jars $(BLDPOLICY)

$(BLDPOLICY): libbzdev.policy
	mkdir -p $(JROOT)/BUILD
	sed -e s/LOCATION/$(BUILD_SED)/ libbzdev.policy > $(BLDPOLICY)

SERVLETS_API = /usr/share/java/servlet-api.jar

RTJARS = BUILD/libbzdev-base.jar BUILD/libbzdev-esp.jar \
	BUILD/libbzdev-math.jar BUILD/libbzdev-obnaming.jar \
	BUILD/libbzdev-graphics.jar BUILD/libbzdev-desktop.jar \
	BUILD/libbzdev-devqsim.jar BUILD/libbzdev-drama.jar \
	BUILD/libbzdev-anim2d.jar BUILD/libbzdev-p3d.jar \
	BUILD/libbzdev-ejws.jar

JARS = BUILD/libbzdev-base.jar BUILD/libbzdev-servlets.jar \
	BUILD/libbzdev-esp.jar BUILD/libbzdev-dmethods.jar \
	BUILD/libbzdev-math.jar BUILD/libbzdev-obnaming.jar \
	BUILD/libbzdev-parmproc.jar BUILD/libbzdev-graphics.jar \
	BUILD/libbzdev-desktop.jar BUILD/libbzdev-devqsim.jar \
	BUILD/libbzdev-drama.jar BUILD/libbzdev-anim2d.jar \
	BUILD/libbzdev-p3d.jar BUILD/libbzdev-ejws.jar

APP_BUILD_PATH1 = $(shell result=START ; for i in $(RTJARS) ; do \
	result=$$result":"$$i ; done;  echo  $$result )
APP_BUILD_PATH = $(subst START:,,$(APP_BUILD_PATH1))

# This rule should be used to build JAR files: the JAR files are
# ordered so that each JAR file is built before it will be used.
#
jars: $(JARS) BUILD/libbzdev.jar BUILD/lsnof.jar BUILD/scrunner.jar \
	BUILD/yrunner.jar  BUILD/sbl.jar

#
# RULES FOR EACH JAR FILE WE NEED TO CREATE
#
BUILD/libbzdev-base.jar: $(BASE_JFILES) $(BASE_RESOURCES1) $(BASE_RESOURCES2) \
		$(BASE_MODINFO)
	mkdir -p mods/org.bzdev.base
	mkdir -p BUILD
	$(JAVAC) -d mods/org.bzdev.base $(BASE_DIR)/module-info.java \
		$(BASE_JFILES) $(BASE_JFILES2)
	for i in $(BASE_RESOURCES) ; do mkdir -p mods/`dirname $$i` ; \
		cp src/$$i mods/$$i ; done
	for i in $(BASE_RESOURCES_CRLF) ; do mkdir -p mods/`dirname $$i` ; \
		sed -e 's/$$//' src/$$i > mods/$$i ; done
	jar --create --file BUILD/libbzdev-base.jar -C mods/org.bzdev.base .

BUILD/libbzdev-servlets.jar: $(SERVLETS_JFILES)
	mkdir -p mods/org.bzdev.servlets
	mkdir -p BUILD
	$(JAVAC) -d mods/org.bzdev.servlets -p $(SERVLETS_BUILD_PATH) \
		$(SERVLETS_JFILES)
	jar --create --file BUILD/libbzdev-servlets.jar \
		--manifest=$(SERVLETS_DIR)/manifest.mf \
		-C mods/org.bzdev.servlets .


BUILD/libbzdev-esp.jar: $(ESP_JFILES) $(ESP_RESOURCES1) $(ESP_MODINFO) \
		$(ESP_JFILES2)
	mkdir -p mods/org.bzdev.esp
	mkdir -p BUILD
	$(JAVAC) -d mods/org.bzdev.esp -p BUILD/libbzdev-base.jar \
		$(ESP_MODINFO) $(ESP_JFILES) $(ESP_JFILES2)
	for i in $(ESP_RESOURCES); do mkdir -p mods/`dirname $$i` ;\
		cp src/$$i mods/$$i ; done
	jar --create --file BUILD/libbzdev-esp.jar -C mods/org.bzdev.esp .

BUILD/libbzdev-dmethods.jar: $(DMETHODS_JFILES) $(DMETHODS_RESOURCES1) \
		$(DMETHODS_MODINFO) $(DMETHODS_DIR)/manifest.mf
	mkdir -p mods/org.bzdev.dmethods
	mkdir -p BUILD
	$(JAVAC) -d mods/org.bzdev.dmethods -p BUILD/libbzdev-base.jar \
		$(DMETHODS_DIR)/module-info.java $(DMETHODS_JFILES)
	for i in $(DMETHODS_RESOURCES) ; do mkdir -p mods/`dirname $$i` ; \
		cp src/$$i mods/$$i ; done
	jar --create --file BUILD/libbzdev-dmethods.jar \
		-m $(DMETHODS_DIR)/manifest.mf \
		-C mods/org.bzdev.dmethods .

BUILD/libbzdev-math.jar: $(MATH_JFILES) $(MATH_JFILES2) \
		$(MATH_RESOURCES1) $(MATH_MODINFO) \
		$(MATH_DIR)/manifest.mf
	mkdir -p mods/org.bzdev.math
	mkdir -p BUILD
	$(JAVAC) -d mods/org.bzdev.math -p BUILD/libbzdev-base.jar \
		$(MATH_DIR)/module-info.java $(MATH_JFILES) $(MATH_JFILES2)
	for i in $(MATH_RESOURCES) ; do mkdir -p mods/`dirname $$i` ; \
		cp src/$$i mods/$$i ; done
	jar --create --file BUILD/libbzdev-math.jar \
	    -m $(MATH_DIR)/manifest.mf -C mods/org.bzdev.math .

BUILD/libbzdev-obnaming.jar: $(OBNAMING_JFILES) $(OBNAMING_JFILES1) \
		$(OBNAMING_JFILES2) $(OBNAMING_RESOURCES1) \
		$(OBNAMING_MODINFO) $(OBNAMING_DIR)/manifest.mf
	mkdir -p mods/org.bzdev.obnaming
	mkdir -p BUILD
	$(JAVAC) -d mods/org.bzdev.obnaming -p $(BASE_MATH_BUILD_PATH) \
		$(OBNAMING_DIR)/module-info.java $(OBNAMING_JFILES) \
		$(OBNAMING_JFILES1) $(OBNAMING_JFILES2)
	for i in $(OBNAMING_RESOURCES) ; do mkdir -p mods/`dirname $$i` ; \
		cp src/$$i mods/$$i ; done
	jar --create --file BUILD/libbzdev-obnaming.jar \
	    -m $(OBNAMING_DIR)/manifest.mf -C mods/org.bzdev.obnaming .

BUILD/libbzdev-parmproc.jar: $(PARMPROC_JFILES) $(PARMPROC_RESOURCES1) \
		$(PARMPROC_MODINFO) $(PARMPROC_DIR)/manifest.mf
	mkdir -p mods/org.bzdev.obnaming
	mkdir -p BUILD
	$(JAVAC) -d mods/org.bzdev.parmproc -p \
		$(BASE_MATH_BUILD_PATH):BUILD/libbzdev-obnaming.jar \
		$(PARMPROC_DIR)/module-info.java $(PARMPROC_JFILES)
	for i in $(PARMPROC_RESOURCES) ; do mkdir -p mods/`dirname $$i` ; \
		cp src/$$i mods/$$i ; done
	jar --create --file BUILD/libbzdev-parmproc.jar \
	    -m $(PARMPROC_DIR)/manifest.mf -C mods/org.bzdev.parmproc .

BUILD/ejwsCerts.jks: $(DEFAULT_CERTS)
	keytool -exportcert -alias ejws -file BUILD/ejwsCert.cer \
		-storepass changeit --keystore $(DEFAULT_CERTS)
	keytool -import -trustcacerts -alias ejws -file BUILD/ejwsCert.cer \
		-keystore BUILD/ejwsCerts.jks -noprompt \
		-keypass changeit -storepass changeit
	rm BUILD/ejwsCert.cer

BUILD/libbzdev-ejws.jar: $(EJWS_JFILES) $(EJWS_RESOURCES1) $(EJWS_MODINFO) \
		$(EJWS_DIR)/manifest.mf BUILD/ejwsCerts.jks
	mkdir -p mods/org.bzdev.ejws
	mkdir -p BUILD
	$(JAVAC) -d mods/org.bzdev.ejws -p $(EJWS_BUILD_PATH) \
		$(EJWS_DIR)/module-info.java $(EJWS_JFILES)
	for i in $(EJWS_RESOURCES) ; do mkdir -p mods/`dirname $$i` ; \
		cp src/$$i mods/$$i ; done
	jar --create --file BUILD/libbzdev-ejws.jar \
	    -m $(EJWS_DIR)/manifest.mf -C mods/org.bzdev.ejws .

$(DEFAULT_CERTS):
	keytool -genkeypair \
		-alias ejws \
		-keyalg RSA \
		-dname "cn=localhost, ou=Unkonwn, o=Unknown, c=Unknown" \
		-validity 36000 \
		-storepass changeit -keypass changeit \
		-keystore $(DEFAULT_CERTS)


BUILD/libbzdev-graphics.jar: $(GRAPHICS_JFILES) $(GRAPHICS_JFILES2) \
		$(GRAPHICS_RESOURCES1) \
		$(GRAPHICS_MODINFO) $(GRAPHICS_DIR)/manifest.mf
	mkdir -p mods/org.bzdev.graphics
	$(JAVAC) -d mods/org.bzdev.graphics -p $(GRAPHICS_BUILD_PATH) \
		$(GRAPHICS_DIR)/module-info.java $(GRAPHICS_JFILES) \
		$(GRAPHICS_JFILES2)
	for i in $(GRAPHICS_RESOURCES) ; do mkdir -p mods/`dirname $$i` ; \
		cp src/$$i mods/$$i ; done
	jar --create --file BUILD/libbzdev-graphics.jar \
		-m $(GRAPHICS_DIR)/manifest.mf -C mods/org.bzdev.graphics .

PROXYHTML = \
	org.bzdev.desktop/org/bzdev/swing/proxyconf/lpack/ProxyComponent.html
PROXYHTMLDM = \
	org.bzdev.desktop/org/bzdev/swing/proxyconf/lpack/ProxyComponent.dm.html


BUILD/libbzdev-desktop.jar: $(DESKTOP_JFILES) $(BASE_JFILES) \
		$(DESKTOP_JFILES2) $(DESKTOP_RESOURCES1) \
		$(DESKTOP_MODINFO) $(DESKTOP_DIR)/manifest.mf
	mkdir -p mods/org.bzdev.desktop
	$(JAVAC) -d mods/org.bzdev.desktop -p $(DESKTOP_BUILD_PATH) \
		$(DESKTOP_DIR)/module-info.java $(DESKTOP_JFILES) \
		$(DESKTOP_JFILES2)
	for i in $(DESKTOP_RESOURCES) ; do mkdir -p mods/`dirname $$i` ; \
		cp src/$$i mods/$$i ; done
	cat src/$(PROXYHTML) | sed -e 's/stylesheet/stylesheet.dm/' \
		| sed -e 's/gui-proxy/gui-proxy-dm/' > mods/$(PROXYHTMLDM)
	jar --create --file BUILD/libbzdev-desktop.jar \
	    -m $(DESKTOP_DIR)/manifest.mf -C mods/org.bzdev.desktop .

BUILD/libbzdev-devqsim.jar: $(DEVQSIM_JFILES) $(DEVQSIM_JFILES1) \
		$(DEVQSIM_JFILES2) $(DEVQSIM_RESOURCES1) \
		$(DEVQSIM_MODINFO) $(DEVQSIM_DIR)/manifest.mf
	mkdir -p mods/org.bzdev.devqsim
	mkdir -p BUILD tmpsrc/org.bzdev.devqsim
	$(JAVAC) -d mods/org.bzdev.devqsim -p $(DEVQSIM_BUILD_PATH) \
		--processor-path $(PROC_PATH) -s tmpsrc/org.bzdev.devqsim \
		$(DEVQSIM_DIR)/module-info.java $(DEVQSIM_JFILES) \
		$(DEVQSIM_JFILES2)
	for i in $(DEVQSIM_RESOURCES) ; do mkdir -p mods/`dirname $$i` ; \
		cp src/$$i mods/$$i ; done
	jar --create --file BUILD/libbzdev-devqsim.jar \
		--manifest=$(DEVQSIM_DIR)/manifest.mf \
		-C mods/org.bzdev.devqsim .

BUILD/libbzdev-drama.jar: $(DRAMA_JFILES) $(DRAMA_JFILES2) \
		$(DRAMA_RESOURCES1) $(DRAMA_MODINFO) \
		$(DRAMA_DIR)/manifest.mf
	mkdir -p mods/org.bzdev.drama
	mkdir -p BUILD tmpsrc/org.bzdev.drama 
	$(JAVAC) -d mods/org.bzdev.drama -p $(DRAMA_BUILD_PATH) \
		--processor-path $(PROC_PATH) -s tmpsrc/org.bzdev.drama \
		$(DRAMA_DIR)/module-info.java $(DRAMA_JFILES) $(DRAMA_JFILES2)
	for i in $(DRAMA_RESOURCES) ; do mkdir -p mods/`dirname $$i` ; \
		cp src/$$i mods/$$i ; done
	jar --create --file BUILD/libbzdev-drama.jar \
		--manifest=$(DRAMA_DIR)/manifest.mf \
		-C mods/org.bzdev.drama .

BUILD/libbzdev-anim2d.jar: $(ANIM2D_JFILES) $(ANIM2D_JFILES2) \
		$(ANIM2D_RESOURCES1) \
		$(ANIM2D_MODINFO) $(ANIM2D_DIR)/manifest.mf
	mkdir -p mods/org.bzdev.anim2d
	mkdir -p BUILD tmpsrc/org.bzdev.anim2d
	$(JAVAC) -d mods/org.bzdev.anim2d -p $(ANIM2D_BUILD_PATH) \
		--processor-path $(PROC_PATH) -s tmpsrc/org.bzdev.anim2d \
		$(ANIM2D_DIR)/module-info.java $(ANIM2D_JFILES) \
		$(ANIM2D_JFILES2)
	for i in $(ANIM2D_RESOURCES) ; do mkdir -p mods/`dirname $$i` ; \
		cp src/$$i mods/$$i ; done
	jar --create --file BUILD/libbzdev-anim2d.jar \
		--manifest=$(ANIM2D_DIR)/manifest.mf \
		-C mods/org.bzdev.anim2d .

BUILD/libbzdev-p3d.jar: $(P3D_JFILES) $(P3D_JFILES2) \
		$(P3D_RESOURCES1) $(P3D_MODINFO) \
		$(P3D_DIR)/manifest.mf
	mkdir -p mods/org.bzdev.p3d
	mkdir -p BUILD tmpsrc/org.bzdev.p3d
	$(JAVAC) -d mods/org.bzdev.p3d -p $(P3D_BUILD_PATH) \
		--processor-path $(PROC_PATH) -s tmpsrc/org.bzdev.p3d \
		$(P3D_DIR)/module-info.java $(P3D_JFILES) $(P3D_JFILES2)
	for i in $(P3D_RESOURCES) ; do mkdir -p mods/`dirname $$i` ; \
		cp src/$$i mods/$$i ; done
	jar --create --file BUILD/libbzdev-p3d.jar \
		--manifest=$(P3D_DIR)/manifest.mf \
		-C mods/org.bzdev.p3d .

# This jar file is a special case - a module with no files. It
# creates a node in the module graph whose descendants are
# org.bzdev.base, org.bzdev.dmethods, org.bzdev.obnmaing,
# org.bzdev.parmproc, org.bzdev.desktop, org.bzdev.ejws,
# org.bzdev.devqsim, org.bzdev.drama, org.bzdev.anim2d, and
# org.bzdev.p3d. All the descendants will be visible.
#
BUILD/libbzdev.jar: src/org.bzdev/module-info.java src/org.bzdev/manifest.mf \
		$(JARS)
	mkdir -p mods/org.bzdev
	$(JAVAC) -d mods/org.bzdev -p BUILD src/org.bzdev/module-info.java
	jar --create --file BUILD/libbzdev.jar -m src/org.bzdev/manifest.mf \
		-C mods/org.bzdev .

# The application jar file for the lsnof program, provided as modular
# jar file (module org.bzdev.lsnof).
#
BUILD/lsnof.jar: $(LSNOF_JFILES) $(LSNOF_RESOURCES1) $(LSNOF_MODINFO) \
		$(LSNOF_DIR)/manifest.mf
	mkdir -p mods/org.bzdev.lsnof
	mkdir -p BUILD
	$(JAVAC) -d mods/org.bzdev.lsnof -p $(APP_BUILD_PATH) \
		$(LSNOF_DIR)/module-info.java $(LSNOF_JFILES) $(LSNOF_JFILES2)
	for i in $(LSNOF_RESOURCES) ; do mkdir -p mods/`dirname $$i` ; \
		cp src/$$i mods/$$i ; done
	jar --create --file BUILD/lsnof.jar \
		--manifest $(LSNOF_DIR)/manifest.mf \
		--main-class=org.bzdev.bin.lsnof.FactoryPrinterCmd \
		-C mods/org.bzdev.lsnof .

# The application jar file for the scrunner program, provided as modular
# jar file (module org.bzdev.scrunner).
#
BUILD/scrunner.jar: $(SCRUNNER_JFILES) $(SCRUNNER_RESOURCES1) \
		$(SCRUNNER_MODINFO) $(SCRUNNER_DIR)/manifest.mf
	mkdir -p mods/org.bzdev.scrunner
	mkdir -p BUILD
	$(JAVAC) -d mods/org.bzdev.scrunner -p $(APP_BUILD_PATH) \
		$(SCRUNNER_DIR)/module-info.java $(SCRUNNER_JFILES) \
		$(SCRUNNER_JFILES2)
	for i in $(SCRUNNER_RESOURCES) ; do mkdir -p mods/`dirname $$i` ; \
		cp src/$$i mods/$$i ; done
	jar --create --file BUILD/scrunner.jar \
		--manifest=$(SCRUNNER_DIR)/manifest.mf \
		--main-class=org.bzdev.bin.scrunner.SCRunnerCmd \
		-C mods/org.bzdev.scrunner .

# The application jar file for the yrunner program, provided as modular
# jar file (module org.bzdev.yrunner).
#
BUILD/yrunner.jar: $(YRUNNER_JFILES) $(YRUNNER_RESOURCES1) \
		$(YRUNNER_MODINFO) $(YRUNNER_DIR)/manifest.mf
	mkdir -p mods/org.bzdev.yrunner
	mkdir -p BUILD
	$(JAVAC) -d mods/org.bzdev.yrunner -p $(APP_BUILD_PATH) \
		$(YRUNNER_DIR)/module-info.java $(YRUNNER_JFILES) \
		$(YRUNNER_JFILES2)
	for i in $(YRUNNER_RESOURCES) ; do mkdir -p mods/`dirname $$i` ; \
		cp src/$$i mods/$$i ; done
	jar --create --file BUILD/yrunner.jar \
		--manifest=$(YRUNNER_DIR)/manifest.mf \
		--main-class=org.bzdev.bin.yrunner.YRunnerCmd \
		-C mods/org.bzdev.yrunner .

# The application jar file for the yrunner program, provided as modular
# jar file (module org.bzdev.yrunner).
#
BUILD/sbl.jar: $(SBL_JFILES) $(SBL_RESOURCES1) \
		$(SBL_MODINFO) $(SBL_DIR)/manifest.mf \
		MediaTypes/sblauncher.svg MediaTypes/sblconf.svg
	mkdir -p mods/org.bzdev.sbl
	mkdir -p BUILD
	$(JAVAC) -d mods/org.bzdev.sbl -p $(APP_BUILD_PATH) \
		$(SBL_DIR)/module-info.java $(SBL_JFILES) \
		$(SBL_JFILES2)
	for i in $(SBL_RESOURCES) ; do mkdir -p mods/`dirname $$i` ; \
		cp src/$$i mods/$$i ; done
	for i in $(ICON_WIDTHS) ; do \
		inkscape -w $$i \
		--export-filename=mods/org.bzdev.sbl/org/bzdev/bin/sbl/sblauncher$${i}.png \
		MediaTypes/sblauncher.svg ; \
		inkscape -w $$i \
		--export-filename=mods/org.bzdev.sbl/org/bzdev/bin/sbl/sblconf$${i}.png \
		MediaTypes/sblconf.svg ; \
		done
	jar --create --file BUILD/sbl.jar \
		--manifest=$(SBL_DIR)/manifest.mf \
		--main-class=org.bzdev.bin.sbl.SBL \
		-C mods/org.bzdev.sbl .


clean:
	rm -f BUILD/libbzdev-*.jar BUILD/libbzdev.jar BUILD/libbzdev.policy
	rm -rf mods tmpsrc
	rm -f esp.elc

# ---------------  DIAGRAM SECTION ----------------

DIAGRAMS = $(ANIM2D_DIR)/org/bzdev/anim2d/doc-files/anim2d.png \
	$(ANIM2D_DIR)/org/bzdev/anim2d/doc-files/basicpath.png \
	$(ANIM2D_DIR)/org/bzdev/anim2d/doc-files/layerpath.png \
	$(DEVQSIM_DIR)/org/bzdev/devqsim/doc-files/devqsim.png \
	$(DEVQSIM_DIR)/org/bzdev/devqsim/doc-files/dqfactories.png \
	$(DEVQSIM_DIR)/org/bzdev/devqsim/doc-files/events.png \
	$(DEVQSIM_DIR)/org/bzdev/devqsim/doc-files/monitoring.png \
	$(DRAMA_DIR)/org/bzdev/drama/doc-files/drama.png \
	$(DRAMA_DIR)/org/bzdev/drama/doc-files/dfactories.png \
	$(DRAMA_DIR)/org/bzdev/drama/common/doc-files/commondrama.png \
	$(DRAMA_DIR)/org/bzdev/drama/generic/doc-files/genericdrama.png \
	$(DRAMA_DIR)/org/bzdev/drama/generic/doc-files/message1.png \
	$(DRAMA_DIR)/org/bzdev/drama/generic/doc-files/message2.png \
	$(DRAMA_DIR)/org/bzdev/drama/generic/doc-files/gdfactories.png \
	$(DRAMA_DIR)/org/bzdev/drama/generic/doc-files/gevents.png \
	$(GRAPHICS_DIR)/org/bzdev/geom/doc-files/geom.png \
	$(GRAPHICS_DIR)/org/bzdev/geom/doc-files/pathbuilder.png \
	$(GRAPHICS_DIR)/org/bzdev/geom/doc-files/barycentric.png \
	$(GRAPHICS_DIR)/org/bzdev/geom/doc-files/planar.png \
	$(GRAPHICS_DIR)/org/bzdev/geom/doc-files/basicbuilder.png \
	$(GRAPHICS_DIR)/org/bzdev/geom/doc-files/bgsphere.png \
	$(GRAPHICS_DIR)/org/bzdev/gio/doc-files/gio.png \
	$(GRAPHICS_DIR)/org/bzdev/graphs/doc-files/graphs.png \
	$(GRAPHICS_DIR)/org/bzdev/graphs/doc-files/axis1.png \
	$(GRAPHICS_DIR)/org/bzdev/graphs/doc-files/axis2.png \
	$(GRAPHICS_DIR)/org/bzdev/graphs/doc-files/axis3.png \
	$(GRAPHICS_DIR)/org/bzdev/graphs/doc-files/axis4.png \
	$(GRAPHICS_DIR)/org/bzdev/graphs/doc-files/axis5.png \
	$(GRAPHICS_DIR)/org/bzdev/graphs/doc-files/axis6.png \
	$(GRAPHICS_DIR)/org/bzdev/graphs/doc-files/clocktime.png \
	$(MATH_DIR)/org/bzdev/math/doc-files/math1.png \
	$(MATH_DIR)/org/bzdev/math/doc-files/math2.png \
	$(MATH_DIR)/org/bzdev/math/stats/doc-files/stats.png \
	$(BASE_DIR)/org/bzdev/net/calendar/doc-files/ICalBuilder.png \
	$(BASE_DIR)/org/bzdev/net/calendar/doc-files/ICalParser.png \
	$(OBNAMING_DIR)/org/bzdev/obnaming/doc-files/example.png \
	$(OBNAMING_DIR)/org/bzdev/obnaming/doc-files/factory.png \
	$(OBNAMING_DIR)/org/bzdev/obnaming/doc-files/obnaming.png \
	$(P3D_DIR)/org/bzdev/p3d/doc-files/sgexample.png \
	$(P3D_DIR)/org/bzdev/p3d/doc-files/sgbuilder.png \
	$(P3D_DIR)/org/bzdev/p3d/doc-files/sgbuilder2.png \
	$(BASE_DIR)/org/bzdev/scripting/doc-files/scripting.png \
	$(DESKTOP_DIR)/org/bzdev/swing/doc-files/swing1.png \
	$(DESKTOP_DIR)/org/bzdev/swing/doc-files/swing2.png \
	$(MATH_DIR)/org/bzdev/math/rv/doc-files/rvtoplevel.png \
	$(MATH_DIR)/org/bzdev/math/rv/doc-files/partial.png \
	$(MATH_DIR)/org/bzdev/math/rv/doc-files/rvrv.png \
	src/doc-files/modules.png \
	src/doc-files/modules2.png \
	src/doc-files/base.png  \
	src/doc-files/math.png \
	src/doc-files/graphics.png \
	src/doc-files/desktop.png \
	src/doc-files/drama.png \
	src/doc-files/ejws.png \
	src/doc-files/devqsim.png \
	src/doc-files/servlets.png \
	src/doc-files/rest.png

diagrams: $(DIAGRAMS)

#
# When dia exports SVG, the background is transparent, but not when
# dia exports PNG.  So, the following first creates an SVG file
# and then uses inkscape to export a PNG file beecause inscape does
# not fill transparent areas.

$(ANIM2D_DIR)/org/bzdev/anim2d/doc-files/anim2d.png: diagrams/anim2d/anim2d.dia
	mkdir -p $(ANIM2D_DIR)/org/bzdev/anim2d/doc-files
	dia -s 700x -e tmp.svg $<
	inkscape -w 700 --export-filename=$@ tmp.svg
	rm tmp.svg

$(ANIM2D_DIR)/org/bzdev/anim2d/doc-files/basicpath.png: \
		diagrams/anim2d/basicpath.dia
	mkdir -p $(ANIM2D_DIR)/org/bzdev/anim2d/doc-files
	dia -s 700x -e tmp.svg $<
	inkscape -w 700 --export-filename=$@ tmp.svg
	rm tmp.svg

$(ANIM2D_DIR)/org/bzdev/anim2d/doc-files/layerpath.png: \
		diagrams/anim2d/layerpath.dia
	mkdir -p $(ANIM2D_DIR)/org/bzdev/anim2d/doc-files
	dia -s 700x -e tmp.svg $<
	inkscape -w 700 --export-filename=$@ tmp.svg
	rm tmp.svg

$(DEVQSIM_DIR)/org/bzdev/devqsim/doc-files/devqsim.png: \
		diagrams/devqsim/devqsim.dia
	mkdir -p $(DEVQSIM_DIR)/org/bzdev/devqsim/doc-files
	dia -s 700x -e tmp.svg $<
	inkscape -w 700 --export-filename=$@ tmp.svg
	rm tmp.svg

$(DEVQSIM_DIR)/org/bzdev/devqsim/doc-files/dqfactories.png: \
		diagrams/devqsim/dqfactories.dia
	mkdir -p $(DEVQSIM_DIR)/org/bzdev/devqsim/doc-files
	dia -s 700x -e tmp.svg $<
	inkscape -w 700 --export-filename=$@ tmp.svg
	rm tmp.svg

$(DEVQSIM_DIR)/org/bzdev/devqsim/doc-files/events.png: \
		diagrams/devqsim/events.dia
	mkdir -p $(DEVQSIM_DIR)/org/bzdev/devqsim/doc-files
	dia -s 600x -e tmp.svg $<
	inkscape -w 600 --export-filename=$@ tmp.svg
	rm tmp.svg

$(DEVQSIM_DIR)/org/bzdev/devqsim/doc-files/monitoring.png: \
		diagrams/devqsim/monitoring.dia
	mkdir -p $(DEVQSIM_DIR)/org/bzdev/devqsim/doc-files
	dia -s 600x -e tmp.svg $<
	inkscape -w 600 --export-filename=$@ tmp.svg
	rm tmp.svg

$(DRAMA_DIR)/org/bzdev/drama/doc-files/drama.png: diagrams/drama/drama.dia
	mkdir -p $(DRAMA_DIR)/org/bzdev/drama/doc-files
	dia -s 650x -e tmp.svg $<
	inkscape -w 650 --export-filename=$@ tmp.svg
	rm tmp.svg

$(DRAMA_DIR)/org/bzdev/drama/doc-files/dfactories.png: \
		diagrams/drama/dfactories.dia
	mkdir -p $(DRAMA_DIR)/org/bzdev/drama/doc-files
	dia -s 650x -e tmp.svg $<
	inkscape -w 650 --export-filename=$@ tmp.svg
	rm tmp.svg

$(DRAMA_DIR)/org/bzdev/drama/common/doc-files/commondrama.png: \
		diagrams/drama/common/commondrama.dia
	mkdir -p $(DRAMA_DIR)/org/bzdev/drama/common/doc-files
	dia -s 650x -e tmp.svg $<
	inkscape -w 650 --export-filename=$@ tmp.svg
	rm tmp.svg

$(DRAMA_DIR)/org/bzdev/drama/generic/doc-files/genericdrama.png: \
		diagrams/drama/generic/genericdrama.dia
	mkdir -p $(DRAMA_DIR)/org/bzdev/drama/generic/doc-files
	dia -s 650x -e tmp.svg $<
	inkscape -w 650 --export-filename=$@ tmp.svg
	rm tmp.svg

$(DRAMA_DIR)/org/bzdev/drama/generic/doc-files/gevents.png: \
		diagrams/drama/generic/gevents.dia
	mkdir -p $(DRAMA_DIR)/org/bzdev/drama/generic/doc-files
	dia -s 650x -e tmp.svg $<
	inkscape -w 650 --export-filename=$@ tmp.svg
	rm tmp.svg

$(DRAMA_DIR)/org/bzdev/drama/generic/doc-files/message1.png: \
		diagrams/drama/generic/message1.dia
	mkdir -p $(DRAMA_DIR)/org/bzdev/drama/generic/doc-files
	dia -s 650x -e tmp.svg $<
	inkscape -w 650 --export-filename=$@ tmp.svg
	rm tmp.svg

$(DRAMA_DIR)/org/bzdev/drama/generic/doc-files/message2.png: \
		diagrams/drama/generic/message2.dia
	mkdir -p $(DRAMA_DIR)/org/bzdev/drama/generic/doc-files
	dia -s 700x -e tmp.svg $<
	inkscape -w 700 --export-filename=$@ tmp.svg
	rm tmp.svg

$(DRAMA_DIR)/org/bzdev/drama/generic/doc-files/gdfactories.png: \
		diagrams/drama/generic/gdfactories.dia
	mkdir -p $(DRAMA_DIR)/org/bzdev/drama/generic/doc-files
	dia -s 650x -e tmp.svg $<
	inkscape -w 650 --export-filename=$@ tmp.svg
	rm tmp.svg

$(GRAPHICS_DIR)/org/bzdev/geom/doc-files/geom.png: diagrams/geom/geom.dia
	mkdir -p $(GRAPHICS_DIR)/org/bzdev/geom/doc-files
	dia -s 600x -e tmp.svg $<
	inkscape -w 600 --export-filename=$@ tmp.svg
	rm tmp.svg

$(GRAPHICS_DIR)/org/bzdev/geom/doc-files/pathbuilder.png: \
		diagrams/geom/pathbuilder.dia
	mkdir -p $(GRAPHICS_DIR)/org/bzdev/geom/doc-files
	dia -s 700x -e tmp.svg $<
	inkscape -w 700 --export-filename=$@ tmp.svg
	rm tmp.svg

BARYCENTRIC=$(GRAPHICS_DIR)/org/bzdev/geom/doc-files/barycentric.png
PLANAR=$(GRAPHICS_DIR)/org/bzdev/geom/doc-files/planar.png

$(GRAPHICS_DIR)/org/bzdev/geom/doc-files/barycentric.png: \
		diagrams/geom/barycentric.svg
	mkdir -p $(GRAPHICS_DIR)/org/bzdev/geom/doc-files
	inkscape -w 329 --export-filename=$(BARYCENTRIC) \
		diagrams/geom/barycentric.svg

$(GRAPHICS_DIR)/org/bzdev/geom/doc-files/planar.png: \
		diagrams/geom/planar.svg
	mkdir -p $(GRAPHICS_DIR)/org/bzdev/geom/doc-files
	inkscape -w 127 --export-filename=$(PLANAR) \
		diagrams/geom/planar.svg


$(GRAPHICS_DIR)/org/bzdev/geom/doc-files/basicbuilder.png: \
		diagrams/geom/basicbuilder.dia
	mkdir -p $(GRAPHICS_DIR)/org/bzdev/geom/doc-files
	dia -s 700x -e tmp.svg $<
	inkscape -w 700 --export-filename=$@ tmp.svg
	rm tmp.svg

$(GRAPHICS_DIR)/org/bzdev/geom/doc-files/bgsphere.png: diagrams/geom/bgsphere.png
	mkdir -p $(GRAPHICS_DIR)/org/bzdev/geom/doc-files
	cp $< $@

$(GRAPHICS_DIR)/org/bzdev/gio/doc-files/gio.png: diagrams/gio/gio.dia
	mkdir -p $(GRAPHICS_DIR)/org/bzdev/gio/doc-files
	dia -s 600x -e tmp.svg $<
	inkscape -w 600 --export-filename=$@ tmp.svg
	rm tmp.svg

$(GRAPHICS_DIR)/org/bzdev/graphs/doc-files/graphs.png: diagrams/graphs/graphs.dia
	mkdir -p $(GRAPHICS_DIR)/org/bzdev/graphs/doc-files
	dia -s 600x -e tmp.svg $<
	inkscape -w 600 --export-filename=$@ tmp.svg
	rm tmp.svg

$(GRAPHICS_DIR)/org/bzdev/graphs/doc-files/axis1.png: diagrams/graphs/axis1.png
	mkdir -p $(GRAPHICS_DIR)/org/bzdev/graphs/doc-files
	convert -transparent white $< $@

$(GRAPHICS_DIR)/org/bzdev/graphs/doc-files/axis2.png: diagrams/graphs/axis2.png
	mkdir -p $(GRAPHICS_DIR)/org/bzdev/graphs/doc-files
	convert -transparent white $< $@

$(GRAPHICS_DIR)/org/bzdev/graphs/doc-files/axis3.png: diagrams/graphs/axis3.png
	mkdir -p $(GRAPHICS_DIR)/org/bzdev/graphs/doc-files
	convert -transparent white $< $@

$(GRAPHICS_DIR)/org/bzdev/graphs/doc-files/axis4.png: diagrams/graphs/axis4.png
	mkdir -p $(GRAPHICS_DIR)/org/bzdev/graphs/doc-files
	convert -transparent white $< $@

$(GRAPHICS_DIR)/org/bzdev/graphs/doc-files/axis5.png: diagrams/graphs/axis5.png
	mkdir -p $(GRAPHICS_DIR)/org/bzdev/graphs/doc-files
	convert -transparent white $< $@

$(GRAPHICS_DIR)/org/bzdev/graphs/doc-files/axis6.png: diagrams/graphs/axis6.png
	mkdir -p $(GRAPHICS_DIR)/org/bzdev/graphs/doc-files
	convert -transparent white $< $@

$(GRAPHICS_DIR)/org/bzdev/graphs/doc-files/clocktime.png: \
		diagrams/graphs/clocktime.png
	mkdir -p $(GRAPHICS_DIR)/org/bzdev/graphs/doc-files
	convert -transparent white $< $@

$(MATH_DIR)/org/bzdev/math/doc-files/math1.png: diagrams/math/math1.dia
	mkdir -p $(MATH_DIR)/org/bzdev/math/doc-files
	dia -s 600x -e tmp.svg $<
	inkscape -w 600 --export-filename=$@ tmp.svg
	rm tmp.svg

$(MATH_DIR)/org/bzdev/math/doc-files/math2.png: diagrams/math/math2.dia
	mkdir -p $(MATH_DIR)/org/bzdev/math/doc-files
	dia -s 600x -e tmp.svg $<
	inkscape -w 600 --export-filename=$@ tmp.svg
	rm tmp.svg

$(MATH_DIR)/org/bzdev/math/stats/doc-files/stats.png: \
		diagrams/math/stats/stats.dia
	mkdir -p $(MATH_DIR)/org/bzdev/math/stats/doc-files
	dia -s 600x -e tmp.svg $<
	inkscape -w 600 --export-filename=$@ tmp.svg
	rm tmp.svg

$(BASE_DIR)/org/bzdev/net/calendar/doc-files/ICalBuilder.png: \
		diagrams/net/ICalBuilder.dia
	mkdir -p $(BASE_DIR)/org/bzdev/net/calendar/doc-files
	dia -s 600x -e tmp.svg $<
	inkscape -w 600 --export-filename=$@ tmp.svg
	rm tmp.svg

$(BASE_DIR)/org/bzdev/net/calendar/doc-files/ICalParser.png: \
		diagrams/net/ICalParser.dia
	mkdir -p $(BASE_DIR)/org/bzdev/net/calendar/doc-files
	dia -s 600x -e tmp.svg $<
	inkscape -w 600 --export-filename=$@ tmp.svg
	rm tmp.svg

$(BASE_DIR)/org/bzdev/scripting/doc-files/scripting.png: \
		diagrams/scripting/scripting.dia
	mkdir -p $(BASE_DIR)/org/bzdev/scripting/doc-files
	dia -s 600x -e tmp.svg $<
	inkscape -w 600 --export-filename=$@ tmp.svg
	rm tmp.svg

$(DESKTOP_DIR)/org/bzdev/swing/doc-files/swing1.png: diagrams/swing/swing1.dia
	mkdir -p $(DESKTOP_DIR)/org/bzdev/swing/doc-files
	dia -s 600x -e tmp.svg $<
	inkscape -w 600 --export-filename=$@ tmp.svg
	rm tmp.svg

$(DESKTOP_DIR)/org/bzdev/swing/doc-files/swing2.png: diagrams/swing/swing2.dia
	mkdir -p $(DESKTOP_DIR)/org/bzdev/swing/doc-files
	dia -s 450x -e tmp.svg $<
	inkscape -w 450 --export-filename=$@ tmp.svg
	rm tmp.svg

$(OBNAMING_DIR)/org/bzdev/obnaming/doc-files/obnaming.png: \
		diagrams/obnaming/obnaming.dia
	mkdir -p $(OBNAMING_DIR)/org/bzdev/obnaming/doc-files
	dia -s 600x -e tmp.svg $<
	inkscape -w 600 --export-filename=$@ tmp.svg
	rm tmp.svg

$(OBNAMING_DIR)/org/bzdev/obnaming/doc-files/example.png: \
		diagrams/obnaming/example.dia
	mkdir -p $(OBNAMING_DIR)/org/bzdev/obnaming/doc-files
	dia -s 600x -e tmp.svg $<
	inkscape -w 600 --export-filename=$@ tmp.svg
	rm tmp.svg

$(OBNAMING_DIR)/org/bzdev/obnaming/doc-files/factory.png: \
		diagrams/obnaming/factory.dia
	mkdir -p $(OBNAMING_DIR)/org/bzdev/obnaming/doc-files
	dia -s 700x -e tmp.svg $<
	inkscape -w 700 --export-filename=$@ tmp.svg
	rm tmp.svg

$(P3D_DIR)/org/bzdev/p3d/doc-files/sgexample.png: diagrams/p3d/sgexample.png
	mkdir -p  $(P3D_DIR)/org/bzdev/p3d/doc-files
	cp $< $@

$(P3D_DIR)/org/bzdev/p3d/doc-files/sgbuilder.png: diagrams/p3d/sgbuilder.png
	mkdir -p  $(P3D_DIR)/org/bzdev/p3d/doc-files
	cp $< $@

$(P3D_DIR)/org/bzdev/p3d/doc-files/sgbuilder2.png: diagrams/p3d/sgbuilder2.png
	mkdir -p  $(P3D_DIR)/org/bzdev/p3d/doc-files
	cp $< $@

$(MATH_DIR)/org/bzdev/math/rv/doc-files/rvtoplevel.png: \
		diagrams/math/rv/rvtoplevel.dia
	mkdir -p $(MATH_DIR)/org/bzdev/math/rv/doc-files
	dia -s 700x -e tmp.svg $<
	inkscape -w 700 --export-filename=$@ tmp.svg
	rm tmp.svg

$(MATH_DIR)/org/bzdev/math/rv/doc-files/partial.png: \
		diagrams/math/rv/partial.dia
	mkdir -p $(MATH_DIR)/org/bzdev/math/rv/doc-files
	dia -s 600x -e tmp.svg $<
	inkscape -w 600 --export-filename=$@ tmp.svg
	rm tmp.svg

$(MATH_DIR)/org/bzdev/math/rv/doc-files/rvrv.png: diagrams/math/rv/rvrv.dia
	mkdir -p $(MATH_DIR)/org/bzdev/math/rv/doc-files
	dia -s 700x -e tmp.svg $<
	inkscape -w 700 --export-filename=$@ tmp.svg
	rm tmp.svg

src/doc-files/bzdev.png: diagrams/bzdev.dia
	mkdir -p src/doc-files
	dia -s 700x -e tmp.svg $<
	inkscape -w 700 --export-filename=$@ tmp.svg
	rm tmp.svg

src/doc-files/modules.png: diagrams/modules.svg
	mkdir -p src/doc-files
	inkscape -w 475 --export-filename=src/doc-files/modules.png \
		diagrams/modules.svg

src/doc-files/modules2.png: diagrams/modules.svg
	mkdir -p src/doc-files
	inkscape  -w 600 --export-filename=src/doc-files/modules2.png \
		diagrams/modules.svg

src/doc-files/base.png: diagrams/modules/base.dia
	mkdir -p src/doc-files
	dia -s 700x -e tmp.svg $<
	inkscape -w 700 --export-filename=$@ tmp.svg
	rm tmp.svg

src/doc-files/math.png: diagrams/modules/math.dia
	mkdir -p src/doc-files
	dia -s 700x -e tmp.svg $<
	inkscape -w 700 --export-filename=$@ tmp.svg
	rm tmp.svg

src/doc-files/graphics.png: diagrams/modules/graphics.dia
	mkdir -p src/doc-files
	dia -s 700x -e tmp.svg $<
	inkscape -w 700 --export-filename=$@ tmp.svg
	rm tmp.svg

src/doc-files/desktop.png: diagrams/modules/desktop.dia
	mkdir -p src/doc-files
	dia -s 700x -e tmp.svg $<
	inkscape -w 700 --export-filename=$@ tmp.svg
	rm tmp.svg

src/doc-files/drama.png: diagrams/modules/drama.dia
	mkdir -p src/doc-files
	dia -s 500x -e tmp.svg $<
	inkscape -w 500 --export-filename=$@ tmp.svg
	rm tmp.svg

src/doc-files/ejws.png: diagrams/modules/ejws.dia
	mkdir -p src/doc-files
	dia -s 300x -e tmp.svg $<
	inkscape -w 300 --export-filename=$@ tmp.svg
	rm tmp.svg

src/doc-files/devqsim.png: diagrams/modules/devqsim.dia
	mkdir -p src/doc-files
	dia -s 500x -e tmp.svg $<
	inkscape -w 500 --export-filename=$@ tmp.svg
	rm tmp.svg

src/doc-files/servlets.png: diagrams/modules/servlets.dia
	mkdir -p src/doc-files
	dia -s 500x -e tmp.svg $<
	inkscape -w 500 --export-filename=$@ tmp.svg
	rm tmp.svg

src/doc-files/rest.png: diagrams/modules/rest.dia
	mkdir -p src/doc-files
	dia -s 500x -e tmp.svg $<
	inkscape -w 500 --export-filename=$@ tmp.svg
	rm tmp.svg

$(DESKTOP_DIR)/org/bzdev/swing/icons/aleft.png: \
		$(DESKTOP_DIR)/org/bzdev/swing/icons/aleft.svg
	inkscape -o $(DESKTOP_DIR)/org/bzdev/swing/icons/aleft.png \
		$(DESKTOP_DIR)/org/bzdev/swing/icons/aleft.svg

$(DESKTOP_DIR)/org/bzdev/swing/icons/aleftRVD.png: \
		$(DESKTOP_DIR)/org/bzdev/swing/icons/aleftRVD.svg
	inkscape -o $(DESKTOP_DIR)/org/bzdev/swing/icons/aleftRVD.png \
		$(DESKTOP_DIR)/org/bzdev/swing/icons/aleftRVD.svg

$(DESKTOP_DIR)/org/bzdev/swing/icons/aleftRV.png: \
		$(DESKTOP_DIR)/org/bzdev/swing/icons/aleftRV.svg
	inkscape -o $(DESKTOP_DIR)/org/bzdev/swing/icons/aleftRV.png \
		$(DESKTOP_DIR)/org/bzdev/swing/icons/aleftRV.svg

$(DESKTOP_DIR)/org/bzdev/swing/icons/aright.png: \
		$(DESKTOP_DIR)/org/bzdev/swing/icons/aright.svg
	inkscape -o $(DESKTOP_DIR)/org/bzdev/swing/icons/aright.png \
		$(DESKTOP_DIR)/org/bzdev/swing/icons/aright.svg

$(DESKTOP_DIR)/org/bzdev/swing/icons/arightRVD.png: \
		$(DESKTOP_DIR)/org/bzdev/swing/icons/arightRVD.svg
	inkscape -o $(DESKTOP_DIR)/org/bzdev/swing/icons/arightRVD.png \
		$(DESKTOP_DIR)/org/bzdev/swing/icons/arightRVD.svg

$(DESKTOP_DIR)/org/bzdev/swing/icons/arightRV.png: \
		$(DESKTOP_DIR)/org/bzdev/swing/icons/arightRV.svg
	inkscape -o $(DESKTOP_DIR)/org/bzdev/swing/icons/arightRV.png \
		$(DESKTOP_DIR)/org/bzdev/swing/icons/arightRV.svg

$(DESKTOP_DIR)/org/bzdev/swing/icons/camera.png: \
		$(DESKTOP_DIR)/org/bzdev/swing/icons/camera.svg
	inkscape -o $(DESKTOP_DIR)/org/bzdev/swing/icons/camera.png \
		$(DESKTOP_DIR)/org/bzdev/swing/icons/camera.svg

$(DESKTOP_DIR)/org/bzdev/swing/icons/cameraRVD.png: \
		$(DESKTOP_DIR)/org/bzdev/swing/icons/cameraRVD.svg
	inkscape -o $(DESKTOP_DIR)/org/bzdev/swing/icons/cameraRVD.png \
		$(DESKTOP_DIR)/org/bzdev/swing/icons/cameraRVD.svg

$(DESKTOP_DIR)/org/bzdev/swing/icons/cameraRV.png: \
		$(DESKTOP_DIR)/org/bzdev/swing/icons/cameraRV.svg
	inkscape -o $(DESKTOP_DIR)/org/bzdev/swing/icons/cameraRV.png \
		$(DESKTOP_DIR)/org/bzdev/swing/icons/cameraRV.svg

$(DESKTOP_DIR)/org/bzdev/swing/icons/pause.png: \
		$(DESKTOP_DIR)/org/bzdev/swing/icons/pause.svg
	inkscape -o $(DESKTOP_DIR)/org/bzdev/swing/icons/pause.png \
		$(DESKTOP_DIR)/org/bzdev/swing/icons/pause.svg

$(DESKTOP_DIR)/org/bzdev/swing/icons/pauseRV.png: \
		$(DESKTOP_DIR)/org/bzdev/swing/icons/pauseRV.svg
	inkscape -o $(DESKTOP_DIR)/org/bzdev/swing/icons/pauseRV.png \
		$(DESKTOP_DIR)/org/bzdev/swing/icons/pauseRV.svg

$(DESKTOP_DIR)/org/bzdev/swing/icons/play.png: \
		$(DESKTOP_DIR)/org/bzdev/swing/icons/play.svg
	inkscape -o $(DESKTOP_DIR)/org/bzdev/swing/icons/play.png \
		$(DESKTOP_DIR)/org/bzdev/swing/icons/play.svg

$(DESKTOP_DIR)/org/bzdev/swing/icons/playRV.png: \
		$(DESKTOP_DIR)/org/bzdev/swing/icons/playRV.svg
	inkscape -o $(DESKTOP_DIR)/org/bzdev/swing/icons/playRV.png \
		$(DESKTOP_DIR)/org/bzdev/swing/icons/playRV.svg

#
# HTML PAGES
#

#
# ------------------  JAVADOCS -------------------
#

JROOT_JAVADOCS = $(JROOT)/BUILD/api
JROOT_ALT_JAVADOCS = $(JROOT)/BUILD/alt-api

jdclean:
	rm -rf BUILD/api
	rm -rf BUILD/alt-api

diaclean:
	rm $(DIAGRAMS)

javadocs: $(JROOT_JAVADOCS)/index.html

altjavadocs: $(JROOT_ALT_JAVADOCS)/index.html

JDOC_MODULES1 = org.bzdev.base,org.bzdev.math,org.bzdev.graphics
JDOC_MODULES2 =	org.bzdev.obnaming,org.bzdev.desktop,org.bzdev.ejws
JDOC_MODULES3 =	org.bzdev.devqsim,org.bzdev.drama,org.bzdev.anim2d,org.bzdev.p3d
JDOC_MODULES4 = org.bzdev.servlets
JDOC_MODULES = \
	$(JDOC_MODULES1),$(JDOC_MODULES2),$(JDOC_MODULES3),$(JDOC_MODULES4)

JDOC_CLASSPATH = `echo $(JARS) | sed -e s/\ /:/g `

JDOC_SOURCEPATH1 = $(BASE_DIR):$(OBNAMING_DIR):$(DESKTOP_DIR):
JDOC_SOURCEPATH2 = $(DEVQSIM_DIR):$(DRAMA_DIR):$(ANIM2D_DIR)
JDOC_SOURCEPATH3 = $(EJWS_DIR):$(P3D_DIR)
JDOC_SOURCEPATH = $(JDOC_SOURCEPATH1):$(JDOC_SOURCEPATH2):$(JDOC_SOURCEPATH3)

JDOC_JFILES = $(BASE_JFILE) $(OBNAMING_JFILES) $(DESKTOP_JFILES) \
	$(DEVQSIM_JFILES) $(DRAMA_JFILES) $(ANIM2D_JFILES) \
	$(EJWS_JFILE) $(P3D_JFILES)

# We needed a dummy class in each of the following packages whose name
# ended in 'lpack' to surpress some compiler warnings about empty packages
# (the lpack directories ideally contain only 'properties' files).
#

EXCLUDE_CMD1 = `for i in src/org.*; \
	do (cd $$i ; [ -d org ] && find org -type d ); done \
	| grep lpack | grep -v providers | sed -e 's/\//./g'`

EXCLUDE_CMD2 = `for i in src/org.*; \
	do (cd $$i ; [ -d org ] && find org -type d ); done \
	| grep providers/ | sed -e 's/\//./g'`

JDOC_EXCLUDE_A = $(shell echo $(EXCLUDE_CMD1) | sed -e 's/ /:/g' )
JDOC_EXCLUDE_B = $(shell echo $(EXCLUDE_CMD2) | sed -e 's/ /:/g' )
JDOC_EXCLUDE = $(JDOC_EXCLUDE_A):$(JDOC_EXCLUDE_B)


RUNLSNOF = java -p BUILD -m org.bzdev.lsnof

DESCR_DIRS = org.bzdev.devqsim/org/bzdev/devqsim/doc-files \
	org.bzdev.base/org/bzdev/lang/doc-files \
	org.bzdev.base/org/bzdev/lang/annotations/doc-files \
	org.bzdev.ejws/org/bzdev/ejws/doc-files \
	org.bzdev.math/org/bzdev/math/doc-files \
	org.bzdev.math/org/bzdev/math/stats/doc-files \
	org.bzdev.base/org/bzdev/scripting/doc-files \
	org.bzdev.base/org/bzdev/util/doc-files \
	org.bzdev.math/org/bzdev/math/rv/doc-files \
	org.bzdev.desktop/org/bzdev/geom/doc-files \
	org.bzdev.desktop/org/bzdev/gio/doc-files \
	org.bzdev.desktop/org/bzdev/graphs/doc-files \
	org.bzdev.desktop/org/bzdev/swing/doc-files \
	org.bzdev.drama/org/bzdev/drama/doc-files \
	org.bzdev.drama/org/bzdev/drama/common/doc-files \
	org.bzdev.drama/org/bzdev/drama/generic/doc-files \
	org.bzdev.obnaming/org/bzdev/obnaming/doc-files \
	org.bzdev.p3d/org/bzdev/p3d/doc-files


DESCR_HTML = $(shell find src -name '*.html' | grep /doc-files/ )

MOD_IMAGES = modules.png modules2.png base.png math.png graphics.png \
	desktop.png devqsim.png drama.png ejws.png servlets.png rest.png

saved:
	mkdir -p $(JROOT_JAVADOCS)/doc-files
	cp src/doc-files/modules.png $(JROOT_JAVADOCS)/doc-files
	for i in $(DESCR_DIRS) ; \
	    do mkdir -p $(JROOT_JAVADOCS)/$$i ; \
	       cp src/$$i/description.html $(JROOT_JAVADOCS)/$$i ; done

$(JROOT_JAVADOCS)/index.html: $(JARS) $(DIAGRAMS) $(BLDPOLICY) $(DESCR_HTML) \
		src/overview.html src/description.html \
		stylesheet$(JAVADOC_VERSION).css description.css \
		src/FactoryOverview.html src/SecureBasic.html \
		src/sbl-example.png
	rm -rf $(JROOT_JAVADOCS)
	mkdir -p $(JROOT_JAVADOCS)
	mkdir -p $(MATH_DIR)/$(BZDEV)/math/doc-files
	cp $(MATH_DIR)/$(BZDEV)/providers/math/fft/DefaultFFT.txt \
		$(MATH_DIR)/$(BZDEV)/math/doc-files/DefaultFFT.txt
	styleoption=`[ -z "$(DARKMODE)" ] && echo \
		|| echo --main-stylesheet stylesheet$(JAVADOC_VERSION).css`; \
	$(JAVADOC) -d $(JROOT_JAVADOCS) \
		--module-path BUILD:$(SERVLETS_BUILD_PATH1) \
		$$styleoption \
		--module-source-path src:tmpsrc \
		--add-modules $(JDOC_MODULES) \
		-link file:///usr/share/doc/openjdk-$(JAVA_VERSION)-doc/api \
		-link \
		https://jakarta.ee/specifications/servlet/4.0/apidocs/ \
		-overview src/overview.html \
		--module $(JDOC_MODULES) \
		-exclude $(JDOC_EXCLUDE) 2>&1 | grep -E -v -e '^Generating' \
		| grep -E -v -e '^Copying file'
	mkdir -p $(JROOT_JAVADOCS)/doc-files
	cp description.css $(JROOT_JAVADOCS)/description.css
	cp stylesheet11.css $(JROOT_JAVADOCS)
	cp stylesheet17.css $(JROOT_JAVADOCS)
	dstylesheet=`[ -z "$(DARKMODE)" ] && echo stylesheet.css \
		|| echo stylesheet$(JAVADOC_VERSION).css` ; \
	sed -e s/stylesheet.css/$$dstylesheet/  src/description.html \
		> $(JROOT_JAVADOCS)/doc-files/description.html ; \
	sed -e s/stylesheet.css/$$dstylesheet/  src/SecureBasic.html \
		> $(JROOT_JAVADOCS)/doc-files/SecureBasic.html
	cp src/sbl-example.png $(JROOT_JAVADOCS)/doc-files/
	for i in $(MOD_IMAGES) ; \
	    do cp src/doc-files/$$i $(JROOT_JAVADOCS)/doc-files ; done
	$(RUNLSNOF) $(DARKMODE) --link \
		file:///usr/share/doc/openjdk-$(JAVA_VERSION)-doc/api/ \
		--overview src/FactoryOverview.html \
		-d $(JROOT_JAVADOCS)/  '**'

$(JROOT_ALT_JAVADOCS)/index.html: $(JROOT_JAVADOCS)/index.html
	rm -rf $(JROOT_ALT_JAVADOCS)
	mkdir -p $(JROOT_ALT_JAVADOCS)
	styleoption=`[ -z "$(DARKMODE)" ] && echo \
		|| echo --main-stylesheet stylesheet$(JAVADOC_VERSION).css`; \
	$(JAVADOC) -d $(JROOT_ALT_JAVADOCS) \
		--module-path BUILD$(SERVLETS_BUILD_PATH) \
		$$styleoption \
		--module-source-path src:tmpsrc \
		--add-modules $(JDOC_MODULES) \
		-link \
	   https://docs.oracle.com/en/java/javase/$(JAVA_VERSION)/docs/api/ \
		-link \
		https://jakarta.ee/specifications/servlet/4.0/apidocs/ \
		-overview src/overview.html \
		--module $(JDOC_MODULES) \
		-exclude $(JDOC_EXCLUDE) 2>&1 | grep -v javax\\.servlet\\.http
	mkdir -p $(JROOT_ALT_JAVADOCS)/doc-files
	cp description.css $(JROOT_ALT_JAVADOCS)/description.css
	cp stylesheet11.css $(JROOT_JAVADOCS)
	cp stylesheet17.css $(JROOT_JAVADOCS)
	cp src/description.html $(JROOT_ALT_JAVADOCS)/doc-files/description.html
	cp src/SecureBasic.html $(JROOT_ALT_JAVADOCS)/doc-files/SecureBasic.html
	cp src/sbl-example.png $(JROOT_ALT_JAVADOCS)/doc-files/
	for i in $(MOD_IMAGES) ; \
	    do cp src/doc-files/$$i $(JROOT_ALT_JAVADOCS)/doc-files ; done
	$(RUNLSNOF) $(DARKMODE) --link-offline \
	    https://docs.oracle.com/en/java/javase/$(JAVA_VERSION)/docs/api/ \
		file:///usr/share/doc/openjdk-$(JAVA_VERSION)-doc/api/ \
		--overview src/FactoryOverview.html \
		-d $(JROOT_ALT_JAVADOCS)/  '**'


#
# -------------------- EXAMPLES ------------------
#

JROOT_EXAMPLES = $(JROOT)/examples

# --------------------- Installation ------------------

# Rules for stand-alone use:

install: install-libs install-lib install-utils install-links \
	install-javadocs install-misc

uninstall: uninstall-misc uninstall-links uninstall-utils uninstall-lib

#
# Note: symbolic links are not installed unless DESTDIR is an empty
# string.  Normally DESTDIR is provided when creating packages and
# package-management systems may have to treat symbolic links specially.
#
install-lib: $(JARS)
	install -d $(LIBJARDIR)
#	for i in $(SHORT_MODULE_NAMES) ; do \
#		install -m 0644 -T BUILD/libbzdev-$$i.jar \
#			$(LIBJARDIR)/libbzdev-$$i-$(VERSION).jar ; \
#	done
	install -m 0644 -T BUILD/libbzdev.jar \
		$(LIBJARDIR)/libbzdev-$(VERSION).jar

install-libs: $(JARS)
	install -d $(LIBJARDIR)
	for i in $(SHORT_MODULE_NAMES) ; do \
		$(MAKE) MODULE_NAME=$$i install-$$i ; \
	done

install-base-jar: $(JARS)
	$(MAKE) MODULE_NAME=base install-base

install-servlet-jar: $(JARS)
	$(MAKE) MODULE_NAME=servlets install-servlets

install-esp-jar: $(JARS)
	$(MAKE) MODULE_NAME=esp install-esp

install-dmethods-jar: $(JARS)
	$(MAKE) MODULE_NAME=dmethods install-dmethods

install-math-jar: $(JARS)
	$(MAKE) MODULE_NAME=math install-math

install-obnaming-jar: $(JARS)
	$(MAKE) MODULE_NAME=obnaming install-obnaming

install-parmproc-jar: $(JARS)
	$(MAKE) MODULE_NAME=parmproc install-parmproc

install-graphics-jar: $(JARS)
	$(MAKE) MODULE_NAME=graphics install-graphics

install-desktop-jar: $(JARS)
	$(MAKE) MODULE_NAME=desktop install-desktop

install-devqsim-jar: $(JARS)
	$(MAKE) MODULE_NAME=devqsim install-devqsim

install-drama-jar: $(JARS)
	$(MAKE) MODULE_NAME=drama install-drama

install-anim2d-jar: $(JARS)
	$(MAKE) MODULE_NAME=anim2d install-anim2d

install-p3d-jar: $(JARS)
	$(MAKE) MODULE_NAME=p3d install-p3d

install-ejws-jar: $(JARS)
	$(MAKE) MODULE_NAME=ejws install-ejws


install-$(MODULE_NAME):  BUILD/libbzdev-$(MODULE_NAME).jar
	install -d $(LIBJARDIR)
	install -m 0644 -T BUILD/libbzdev-$(MODULE_NAME).jar \
		$(LIBJARDIR)/libbzdev-$(MODULE_NAME)-$(VERSION).jar ;

install-utils: BUILD/lsnof.jar BUILD/scrunner.jar BUILD/ejwsCerts.jks \
		BUILD/yrunner.jar BUILD/sbl.jar
	sed s/BZDEVDIR/$(JARDIR)/ scrunner.sh > scrunner.tmp
	sed s/BZDEVDIR/$(JARDIR)/ yrunner.sh > yrunner.tmp
	sed s/BZDEVDIR/$(JARDIR)/ lsnof.sh > lsnof.tmp
	sed s/BZDEVDIR/$(JARDIR)/ sbl.sh > sbl.tmp
	sed s/VERSION/$(UVERSION)/ scrunner.conf.5 | \
		gzip -9 -n > scrunner.conf.5.gz
	sed s/VERSION/$(UVERSION)/ scrunner.1 | gzip -9 -n > scrunner.1.gz
	sed s/VERSION/$(UVERSION)/ lsnof.1 | gzip -9 -n > lsnof.1.gz
	sed s/VERSION/$(UVERSION)/ yrunner.1 | gzip -9 -n > yrunner.1.gz
	sed s/VERSION/$(UVERSION)/ yrunner.5 | gzip -9 -n > yrunner.5.gz
	sed s/VERSION/$(UVERSION)/ sbl.1 | gzip -9 -n > sbl.1.gz
	sed s/VERSION/$(UVERSION)/ sbl.5 | gzip -9 -n > sbl.5.gz
	sed s/LOCATION/$(LIBJARDIR_SED)/ libbzdev.policy \
	    | sed s/SCRLOCATION/$(JARDIR)/ > tmppolicy
	install -d $(JARDIRECTORY)
	install -d $(BINDIR)
	install -d $(MANDIR)
	install -d $(MANDIR)/man1
	install -d $(MANDIR)/man5
	install -d $(CONFIGDIR)
	install -d $(APPDIR)
	install -m 0644 -T BUILD/lsnof.jar $(JARDIRECTORY)/lsnof.jar
	install -m 0644 -T BUILD/scrunner.jar $(JARDIRECTORY)/scrunner.jar
	install -m 0644 -T BUILD/yrunner.jar $(JARDIRECTORY)/yrunner.jar
	install -m 0644 -T BUILD/sbl.jar $(JARDIRECTORY)/sbl.jar
	install -m 0755 -T scrunner.tmp $(BINDIR)/scrunner
	install -m 0755 -T lsnof.tmp $(BINDIR)/lsnof
	install -m 0755 -T sbl.tmp $(BINDIR)/sbl
	install -m 0755 -T yrunner.tmp $(BINDIR)/yrunner
	install -m 0644 -T tmppolicy $(JARDIRECTORY)/libbzdev.policy
	install -m 0644  scrunner.conf $(CONFIGDIR)
	install -m 0644 scrunner.1.gz $(MANDIR)/man1
	install -m 0644 lsnof.1.gz $(MANDIR)/man1
	install -m 0644 yrunner.1.gz $(MANDIR)/man1
	install -m 0644 scrunner.conf.5.gz $(MANDIR)/man5
	install -m 0644 yrunner.5.gz $(MANDIR)/man5
	install -m 0644 sbl.1.gz $(MANDIR)/man1
	install -m 0644 sbl.5.gz $(MANDIR)/man5
	install -m 0644 SBLauncher.desktop $(APPDIR)/SBLauncher.desktop
	rm scrunner.tmp lsnof.tmp yrunner.tmp scrunner.conf.5.gz tmppolicy \
		lsnof.1.gz scrunner.1.gz yrunner.1.gz yrunner.5.gz \
		sbl.tmp sbl.1.gz sbl.5.gz
	install -m 0644 -T BUILD/ejwsCerts.jks \
		$(JARDIRECTORY)/ejwsCerts.jks

#
# Run after "make install-lib install-utils".
# This is provided for the case where one is installing directly without
# using a package-management system. The 'install' rule uses this rule
# as one will use install-lib, install-utils, and install-javadocs when
# building a package.
#
install-links:
	if [ -z "$(DESTDIR)" ] ; then \
	    for i in $(SHORT_MODULE_NAMES) ; do \
		rm -f $(LIBJARDIR)/libbzdev-$$i.jar ; \
	    done ; \
	    rm -f $(LIBJARDIR)/libbzdev.jar ; \
	    for i in $(SHORT_MODULE_NAMES) ; do \
		ln -s $(LIBJARDIR)/libbzdev-$$i-$(VERSION).jar \
			$(LIBJARDIR)/libbzdev-$$i.jar ; \
	    done; \
	    ln -s $(LIBJARDIR)/libbzdev-$(VERSION).jar \
		$(LIBJARDIR)/libbzdev.jar ; \
	    if [ -e $(JARDIRECTORY)/lsnof.jar \
		 -o -e $(JARDIRECTORY)/scrunner.jar \
		 -o -e $(JARDIRECTORY)/yrunner.jar ] ; \
	    then rm -f $(JARDIRECTORY)/libbzdev.jar ; \
		ln -s $(LIBJARDIR)/libbzdev.jar $(JARDIRECTORY)/libbzdev.jar ; \
		for i in $(SHORT_MODULE_NAMES) ; do \
		    rm -f $(JARDIRECTORY)/libbzdev-$$i.jar ; \
		    ln -s $(LIBJARDIR)/libbzdev-$$i.jar \
			$(JARDIRECTORY)/libbzdev-$$i.jar ; \
		done ; \
	    fi; \
	fi

install-javadocs: javadocs 
	install -d $(API_DOCDIR)
	install -d $(JAVADOCS)
	for i in `cd $(JROOT_JAVADOCS); find . -type d -print ` ; \
		do install -d $(JAVADOCS)/$$i ; done
	for i in `cd $(JROOT_JAVADOCS); find . -type f -print ` ; \
		do j=`dirname $$i`; install -m 0644 $(JROOT_JAVADOCS)/$$i \
			$(JAVADOCS)/$$j ; \
		done
	install -d $(EXAMPLES)
	for i in blackhole dplot drama harmonic Legendre lgraph lingraph \
	   lognormal lsqfit math multi p3d path pendulum plates \
	   smartmeter stephan  tandem zipviewer ; \
	do  install -d $(EXAMPLES)/$$i ; \
	    install -m 0644 $(JROOT_EXAMPLES)/$$i/*.java $(EXAMPLES)/$$i ; \
	done
	for i in layer; \
	do  install -d $(EXAMPLES)/$$i ; \
	    install -m 0644 $(JROOT_EXAMPLES)/$$i/*.js $(EXAMPLES)/$$i ; \
	done
	for i in p3d smartmeter ; \
	do install -m 0644 $(JROOT_EXAMPLES)/$$i/*.js $(EXAMPLES)/$$i ; \
	done
	install -d $(EXAMPLES)/epi
	install -d $(EXAMPLES)/epi/emodel
	install -m 0644 $(JROOT_EXAMPLES)/epi/Server.java \
		$(EXAMPLES)/epi
	install -m 0644 $(JROOT_EXAMPLES)/epi/epi.mf \
		$(EXAMPLES)/epi
	install -m 0644 $(JROOT_EXAMPLES)/epi/model.html \
		$(EXAMPLES)/epi
	install -m 0644 $(JROOT_EXAMPLES)/epi/Makefile \
		$(EXAMPLES)/epi
	install -m 0644 $(JROOT_EXAMPLES)/epi/emodel/Adapter.java \
		$(EXAMPLES)/epi/emodel
	install -d $(EXAMPLES/sbl
	install -m 0644 $(JROOT_EXAMPLES)/sbl/Makefile $(EXAMPLES)/sbl/Makefile
	install -m 0644 $(JROOT_EXAMPLES)/sbl/Server.java \
		$(EXAMPLES)/sbl/Server.java
	install -m 0644 $(JROOT_EXAMPLES)/smartmeter/*.properties \
		$(EXAMPLES)/smartmeter
	install -d $(EXAMPLES)/smartmeter/META-INF
	install -d $(EXAMPLES)/smartmeter/META-INF/services
	install -m 0644 -T \
	$(JROOT_EXAMPLES)/smartmeter/META-INF/services/org.bzdev.obnaming.NamedObjectFactory \
	$(EXAMPLES)/smartmeter/META-INF/services/org.bzdev.obnaming.NamedObjectFactory

install-misc:
	install -d $(MIMEDIR)
	install -d $(MIMEDIR)/packages
	install -d $(APP_ICON_DIR)
	install -m 0644 -T MediaTypes/libbzdev.xml \
		$(MIMEDIR)/packages/libbzdev.xml
	install -d $(MIME_ICON_DIR)
	install -m 0644 -T MediaTypes/ImageSeq.svg \
		$(MIME_ICON_DIR)/$(IMAGE_SEQUENCE_ICON).svg
	install -m 0644 -T MediaTypes/sblconf.svg \
		$(MIME_ICON_DIR)/$(SBL_CONF_ICON).svg
	install -m 0644 -T MediaTypes/sblauncher.svg \
		$(APP_ICON_DIR)/$(SBL_TARGETICON)
	for i in $(ICON_WIDTHS) ; do \
	  install -d $(ICON_DIR)/$${i}x$${i}/$(MIMETYPES_DIR) ; \
	  inkscape -w $$i -y 0.0 --export-filename=tmp.png \
		MediaTypes/ImageSeq.svg ; \
	  dir=$(ICON_DIR)/$${i}x$${i}/$(MIMETYPES_DIR) ; \
	  install -m 0644 -T tmp.png $$dir/$(IMAGE_SEQUENCE_ICON).png; \
	  inkscape -w $$i -y 0.0 --export-filename=tmp.png \
		MediaTypes/sblconf.svg ; \
	  install -m 0644 -T tmp.png $$dir/$(SBL_CONF_ICON).png; \
	  install -d $(ICON_DIR)/$${i}x$${i}/$(APPS_DIR) ; \
	  inkscape -w $$i -y 0.0 --export-filename=tmp.png \
		MediaTypes/sblauncher.svg ; \
	  install -m 0644 -T tmp.png \
		$(ICON_DIR)/$${i}x$${i}/$(APPS_DIR)/$(SBL_TARGETICON_PNG) ; \
	  rm tmp.png ; \
	done
	for i in $(ICON_WIDTHS2x) ; do \
	  install -d $(ICON_DIR)/$${i}x$${i}@2x/$(MIMETYPES_DIR) ; \
	  ii=`expr 2 '*' $$i` ; \
	  inkscape -w $$ii -y 0.0 --export-filename=tmp.png \
		MediaTypes/ImageSeq.svg ; \
	  dir=$(ICON_DIR)/$${i}x$${i}@2x/$(MIMETYPES_DIR) ; \
	  install -m 0644 -T tmp.png $$dir/$(IMAGE_SEQUENCE_ICON).png;\
	  inkscape -w $$ii -y 0.0 --export-filename=tmp.png \
		MediaTypes/sblconf.svg ; \
	  install -m 0644 -T tmp.png $$dir/$(SBL_CONF_ICON).png; \
	  install -d $(ICON_DIR)/$${i}x$${i}@2x/$(APPS_DIR) ; \
	  inkscape -w $$ii -y 0.0 --export-filename=tmp.png \
		MediaTypes/sblauncher.svg ; \
	  install -m 0644 -T tmp.png \
		$(ICON_DIR)/$${i}x$${i}@2x/$(APPS_DIR)/$(SBL_TARGETICON_PNG) ; \
	  rm tmp.png ; \
	done

uninstall-misc:
	@rm -f $(MIMEDIR)/packages/libbzdev.xml \
		|| echo ... rm libbzdev.xml FAILED
	@rm -f $(MIME_ICON_DIR)/$(IMAGE_SEQUENCE_ICON).svg || \
		echo ... rm $(IMAGE_SEQUENCE_ICON).svg FAILED
	@rm -f $(MIME_ICON_DIR)/$(SBL_CONF_ICON).svg || \
		echo ... rm $(SBL_CONF_ICON).svg FAILED
	@rm -f $(APP_ICON_DIR)/$(SBL_TARGETICON) || \
		echo ... rm $(SBL_TARGETICON) FAILED
	@(for i in $(ICON_WIDTHS) ; do \
	   dir=$(ICON_DIR)/$${i}x$${i}/$(MIMETYPES_DIR) ; \
	   rm -f $$dir/$(IMAGE_SEQUENCE_ICON).png; \
	  done) || echo ... rm $(IMAGE_SEQUENCE_ICON).png FAILED AT LEAST ONCE
	@(for i in $(ICON_WIDTHS2x) ; do \
	   dir=$(ICON_DIR)/$${i}x$${i}@2x/$(MIMETYPES_DIR) ; \
	   rm -f $$dir/$(IMAGE_SEQUENCE_ICON).png; \
	  done) || echo ... rm $(IMAGE_SEQUENCE_ICON).png FAILED AT LEAST ONCE
	@(for i in $(ICON_WIDTHS) ; do \
	   dir=$(ICON_DIR)/$${i}x$${i}/$(MIMETYPES_DIR) ; \
	   rm -f $$dir/$(SBL_CONF_ICON).png; \
	  done) || echo ... rm $(SBL_CONF_ICON).png FAILED AT LEAST ONCE
	@(for i in $(ICON_WIDTHS2x) ; do \
	   dir=$(ICON_DIR)/$${i}x$${i}@2x/$(MIMETYPES_DIR) ; \
	   rm -f $$dir/$(SBL_CONF_ICON).png; \
	  done) || echo ... rm $(SBL_CONF_ICON).png FAILED AT LEAST ONCE
	@(for i in $(ICON_WIDTHS) ; do \
	   dir=$(ICON_DIR)/$${i}x$${i}/$(APPS_DIR) ; \
	   rm -f $$dir/$(SBL_TARGETICON_PNG) ; \
	   done) || echo ... rm $(SBL_TARGETICON_PNG) FAILED AT LEAST ONCE
	@(for i in $(ICON_WIDTHS) ; do \
	   dir=$(ICON_DIR)/$${i}x$${i}@2x/$(APPS_DIR) ; \
	   rm -f $$dir/$(SBL_TARGETICON_PNG) ; \
	   done) || echo ... rm $(SBL_TARGETICON_PNG) FAILED AT LEAST ONCE

install-pop:
	install -d $(MIME_POPICON_DIR)
	install -m 0644 -T MediaTypes/ImageSeq.svg \
		$(MIME_POPICON_DIR)/$(IMAGE_SEQUENCE_ICON).svg
	install -m 0644 -T MediaTypes/sblconf.svg \
		$(MIME_POPICON_DIR)/$(SBL_CONF_ICON).svg

# We are removing this from install-pop due to a package problem
# that suddenly arose. /usr/share/icons/Pop/NxN, etc. now seem to
# contain only SVG files and we get some problems installing the
# package when these lines are included.
old-install-pop-tail:
	for i in $(POPICON_WIDTHS) ; do \
	  install -d $(POPICON_DIR)/$${i}x$${i}/$(MIMETYPES_DIR) ; \
	  inkscape -w $$i -y 0.0 --export-filename=tmp.png \
		MediaTypes/ImageSeq.svg ; \
	  dir=$(POPICON_DIR)/$${i}x$${i}/$(MIMETYPES_DIR) ; \
	  install -m 0644 -T tmp.png $$dir/$(IMAGE_SEQUENCE_ICON).png;\
	  rm tmp.png ; \
	done
	for i in $(POPICON_WIDTHS2x) ; do \
	  install -d $(POPICON_DIR)/$${i}x$${i}@2x/$(MIMETYPES_DIR) ; \
	  ii=`expr 2 '*' $$i` ; \
	  inkscape -w $$ii -y 0.0 --export-filename=tmp.png \
		MediaTypes/ImageSeq.svg ; \
	  dir=$(POPICON_DIR)/$${i}x$${i}@2x/$(MIMETYPES_DIR) ; \
	  install -m 0644 -T tmp.png $$dir/$(IMAGE_SEQUENCE_ICON).png;\
	  rm tmp.png ; \
	done

uninstall-pop:
	@rm -f $(MIME_POPICON_DIR)/$(IMAGE_SEQUENCE_ICON).svg || \
		echo ... rm $(IMAGE_SEQUENCE_ICON).svg FAILED
	@rm -f $(MIME_POPICON_DIR)/$(SBL_CONF_ICON).svg || \
		echo ... rm $(SBL_CONF_ICON).svg FAILED


old-uninstall-pop-tail:
	@(for i in $(POPICON_WIDTHS) ; do \
	   dir=$(POPICON_DIR)/$${i}x$${i}/$(MIMETYPES_DIR) ; \
	   rm -f $$dir/$(IMAGE_SEQUENCE_ICON).png; \
	  done) || echo ... rm $(IMAGE_SEQUENCE_ICON).png FAILED AT LEAST ONCE
	@(for i in $(POPICON_WIDTHS2x) ; do \
	   dir=$(POPICON_DIR)/$${i}x$${i}@2x/$(MIMETYPES_DIR) ; \
	   rm -f $$dir/$(IMAGE_SEQUENCE_ICON).png; \
	  done) || echo ... rm $(IMAGE_SEQUENCE_ICON).png FAILED AT LEAST ONCE

uninstall-links:
	@rm -f $(JARDIRECTORY)/libbzdev.jar
	@rm -f $(LIBJARDIR)/libbzdev.jar
	@for i in $(SHORT_MODULE_NAMES) ; do \
		rm -f $(JARDIRECTORY)/libbzdev-$$i.jar ; \
		rm -f $(LIBJARDIR)/libbzdev-$$i.jar ; \
	done

uninstall-utils:
	@ rm $(JARDIRECTORY)/scrunner.jar > /dev/null \
		|| echo ... rm scrunner.jar FAILED
	@ rm $(JARDIRECTORY)/lsnof.jar > /dev/null \
		|| echo ... rm lsnof.jar FAILED
	@ rm $(JARDIRECTORY)/yrunner.jar > /dev/null \
		|| echo ... rm yrunner.jar FAILED
	@rmdir $(JARDIRECTORY)  || echo ... rmdir $(JARDIRECTORY) FAILED
	@rm $(CONFIGDIR)/scrunner.conf || echo ... rm scrunner.conf FAILED
	@rmdir $(CONFIGDIR) || true
	@rm $(BINDIR)/lsnof || echo ... rm lsnof FAILED
	@rm $(BINDIR)/scrunner || echo ... rm scrunner FAILED
	@rm $(BINDIR)/yrunner || echo ... rm yrunner FAILED
	@rm $(BINDIR)/sbl || echo ... rm sbl FAILED
	@rm $(MANDIR)/man1/scrunner.1.gz || echo  ... rm scrunner.1.gz FAILED
	@rm $(MANDIR)/man1/lsnof.1.gz || echo ... rm lsnof.1.gz FAILED
	@rm $(MANDIR)/man1/yrunner.1.gz || echo ... rm yrunner.1.gz FAILED
	@rm $(MANDIR)/man5/yrunner.5.gz || echo ... rm yrunner.5.gz FAILED
	@rm $(MANDIR)/man5/scrunner.conf.5.gz \
		|| echo ... rm scrunner.conf.5.gz FAILED

uninstall-lib:
	@rm $(LIBJARDIR)/libbzdev-$(VERSION).jar \
		|| echo ... rm libbzdev-$(VERSION).jar FAILED
	@for i in $(SHORT_MODULE_NAMES) ; do \
	    rm $(LIBJARDIR)/libbzdev-$$i-$(VERSION).jar \
		|| echo ... rm libbzdev-$$i-$(VERSION).jar FAILED ;
	done

uninstall-javadocs:
	@(cd $(API_DOCDIR) && rm -rf examples) || echo ... rm examples FAILED
	@(cd $(API_DOCDIR) && rm -rf api) || echo ... rm api FAILED
	@rmdir $(API_DOCDIR) || echo ... rmdir $(DOCDIR) FAILED


# Rule to set up symbolic links for easy access to source code
# These links do not include the annotation processors.
shortcuts:
	mkdir -p org/bzdev
	mkdir -p org/bzdev/bin
	mkdir -p org/bzdev/providers
	@ln -s -T ../../../src/org.bzdev.lsnof/org/bzdev/bin/lsnof \
		org/bzdev/bin/lsnof \
		2> /dev/null ||	echo ... lsnof exists
	@ln -s -T ../../../src/org.bzdev.scrunner/org/bzdev/bin/scrunner \
		org/bzdev/bin/scrunner \
		2> /dev/null || echo ... scrunner exists
	@ln -s -T ../../../src/org.bzdev.yrunner/org/bzdev/bin/yrunner \
		org/bzdev/bin/yrunner \
		2> /dev/null || echo ... yrunner exists
	@ln -s -T ../../../src/org.bzdev.sbl/org/bzdev/bin/sbl \
		org/bzdev/bin/sbl \
		2> /dev/null || echo ... sbl exists
	@for i in `cd src/org.bzdev.anim2d/org/bzdev; ls -d *` ; \
	    do if [ "$$i" = "providers" ] ; \
	    then ln -s -T \
		../../../src/org.bzdev.anim2d/org/bzdev/providers/anim2d \
		org/bzdev/providers/anim2d 2>/dev/null || \
		echo file providers/anim2d exists ; \
	    else ln -s \
		-T ../../src/org.bzdev.anim2d/org/bzdev/$$i org/bzdev/$$i \
		2> /dev/null || echo ... file $$i exists ; fi; done
	@for i in `cd src/org.bzdev.base/org/bzdev; ls -d *` ; \
	    do \
		if [ "$$i" = "obnaming" ] ; \
		then \
		  ln -s -T \
		    ../../src/org.bzdev.base/org/bzdev/obnaming/annotations \
		    org/bzdev/obnaming-annotations 2> /dev/null \
		    || echo ... file obnaming-annotations exists; \
		else \
		    ln -s -T ../../src/org.bzdev.base/org/bzdev/$$i \
			org/bzdev/$$i 2>/dev/null || echo file $$i exists ; \
		fi ; \
	    done
	@for i in `cd src/org.bzdev.math/org/bzdev; ls -d *` ; \
	    do if [ "$$i" = "providers" ] ; \
	    then ln -s -T \
		../../../src/org.bzdev.math/org/bzdev/providers/math \
		org/bzdev/providers/math  2>/dev/null || \
		echo file providers/math exists ; \
	    else ln -s -T ../../src/org.bzdev.math/org/bzdev/$$i \
		org/bzdev/$$i 2>/dev/null || echo ... file $$i exists ; fi; done
	@for i in `cd src/org.bzdev.graphics/org/bzdev; ls -d *` ; \
	    do \
		if [ "$$i" = "obnaming" ] ; \
		then \
		   ln -s -T \
			../../src/org.bzdev.graphics/org/bzdev/obnaming/misc \
			org/bzdev/obnaming-misc 2> /dev/null \
			|| echo ... file obnaming-misc exists ; \
		elif [ "$$i" = "providers" ] ; \
		then ln -s -T \
		  ../../../src/org.bzdev.graphics/org/bzdev/providers/graphics \
		  org/bzdev/providers/graphics  2>/dev/null || \
		  echo file providers/graphics exists ; \
		else \
		   ln -s -T ../../src/org.bzdev.graphics/org/bzdev/$$i \
			org/bzdev/$$i 2> /dev/null \
			|| echo ... file $$i exists ; \
		fi ; \
	    done
	@for i in `cd src/org.bzdev.desktop/org/bzdev; ls -d *` ; \
	    do if [ "$$i" = "providers" ] ; \
	    then ln -s -T \
		../../../src/org.bzdev.desktop/org/bzdev/providers/swing \
		org/bzdev/providers/swing 2>/dev/null || \
		echo file providers/swing exists ; \
	    else ln -s -T ../../src/org.bzdev.desktop/org/bzdev/$$i \
		org/bzdev/$$i 2>/dev/null || echo ... file $$i exists ; fi; done
	@for i in `cd src/org.bzdev.devqsim/org/bzdev; ls -d *` ; \
	    do if [ "$$i" = "providers" ] ; \
	    then ln -s -T \
		../../../src/org.bzdev.devqsim/org/bzdev/providers/devqsim \
		org/bzdev/providers/devqsim 2>/dev/null || \
		echo file providers/devqsim exists ; \
	    else ln -s -T ../../src/org.bzdev.devqsim/org/bzdev/$$i \
		org/bzdev/$$i 2>/dev/null || echo ... file $$i exists ; fi; done
	@for i in `cd src/org.bzdev.drama/org/bzdev; ls -d *` ; \
	    do if [ "$$i" = "providers" ] ; \
	    then ln -s -T \
		../../../src/org.bzdev.drama/org/bzdev/providers/drama \
		org/bzdev/providers/drama 2>/dev/null || \
		echo file providers/drama exists ; \
	    else ln -s -T \
		../../src/org.bzdev.drama/org/bzdev/$$i org/bzdev/$$i \
		2> /dev/null || echo ... file $$i exists ; fi; done
	@for i in `cd src/org.bzdev.esp/org/bzdev; ls -d *` ; \
	   do if [ "$$i" = "providers" ] ; \
	   then ln -s -T \
		../../../src/org.bzdev.esp/org/bzdev/providers/esp \
		org/bzdev/providers/esp 2> /dev/null || \
		echo file providers/esp exists ; \
	    else ln -s -T \
		../../src/org.bzdev.esp/org/bzdev/$$i org/bzdev/$$i \
		2> /dev/null || echo ... file $$i exists ; fi; done
	@for i in `cd src/org.bzdev.ejws/org/bzdev; ls -d *` ; \
	    do ln -s -T ../../src/org.bzdev.ejws/org/bzdev/$$i org/bzdev/$$i \
		2> /dev/null || echo ... file $$i exists ; done
	@for i in `cd src/org.bzdev.obnaming/org/bzdev; ls -d *` ; \
	    do ln -s -T ../../src/org.bzdev.obnaming/org/bzdev/$$i \
		org/bzdev/$$i 2> /dev/null || echo ... file $$i exists ; done
	@for i in `cd src/org.bzdev.p3d/org/bzdev; ls -d *` ; \
	    do if [ "$$i" = "providers" ] ; \
	    then ln -s -T \
		../../../src/org.bzdev.p3d/org/bzdev/providers/p3d \
		org/bzdev/providers/p3d 2>/dev/null || \
		echo file $$i exists ; \
	    else ln -s -T ../../src/org.bzdev.p3d/org/bzdev/$$i org/bzdev/$$i \
		2> /dev/null || echo ... file $$i exists ; fi; done

#
# Rules for installing an emacs ESP mode.  The implementation will
# automatically use this mode when a file's suffix is .esp or
# when the file starts with #!/usr/bin/scrunner followed by a space and an
# optional scrunner argument or and end of line.  The implementation assumes
# the Debian package emacs-common was installed.

esp.elc: esp.el
	rm -f esp.elc
	emacs --no-splash --batch --eval '(byte-compile-file "esp.el")' --kill

install-emacs-esp: esp.elc esp.el
	install -d $(EMACSLISPDIR)
	install -d $(EMACSSTARTDIR)
	install -m 0644 esp.el $(EMACSLISPDIR)
	install -m 0644 esp.elc $(EMACSLISPDIR)
	install -m 0644 99esp.el $(EMACSSTARTDIR)

uninstall-emacs-esp:
	@rm -f $(EMACSLISPDIR)/esp.elc
	@rm -f $(EMACSLISPDIR)/esp.el
	@rm -f $(EMACSSTARTDIR)/99esp.el
