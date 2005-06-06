#include ../../../../Makefile.common
include ../../Makefile.common
#
# Specfic options
#
TEST_NAME	=test
TOOP_NAME	=toop
PACKAGEDIR 	=org/$(ESTAR_NAME)/$(TOOP_NAME)/$(TEST_NAME)
PACKAGENAME	=org.$(ESTAR_NAME).$(TOOP_NAME).$(TEST_NAME)
JAR_FILE	=org_$(ESTAR_NAME)_$(TOOP_NAME)_$(TEST_NAME).jar
JAVACFLAGS 	= -d $(LIBDIR) -sourcepath ../../../../ -classpath $(LIBDIR):$(CLASSPATH)
DOCSDIR 	= $(ESTAR_DOC_HOME)/javadocs/$(PACKAGEDIR)

SRCS = TestSession.java 
OBJS = $(SRCS:%.java=$(LIBDIR)/$(PACKAGEDIR)/%.class)
DOCS = $(SRCS:%.java=$(DOCSDIR)/$(PACKAGEDIR)/%.html)
CONFIGS = 
CONFIGSBIN = $(CONFIGS:%=$(LIBDIR)/%)

top: jar configs

$(LIBDIR)/$(PACKAGEDIR)/%.class: %.java
	$(JAVAC) $(JAVAC_OPTIONS) $(JAVACFLAGS) $<
jar: $(JARLIBDIR)/$(JAR_FILE)

$(JARLIBDIR)/$(JAR_FILE): $(OBJS)
	(cd $(LIBDIR); $(JAR) $(JAR_OPTIONS) $(JAR_FILE) $(PACKAGEDIR); $(MV) $(JAR_FILE) $(JARLIBDIR))

docs: $(DOCS)

$(DOCSDIR)/$(PACKAGEDIR)/%.html: %.java
	$(JAVADOC) -sourcepath ../../../..:$(CLASSPATH) -d $(DOCSDIR) $(DOCFLAGS) $(PACKAGENAME)

configs: $(CONFIGSBIN)

$(LIBDIR)/%: %
	$(CP) $< $@

checkout:
	$(CO) $(CO_OPTIONS) $(SRCS)

checkin:
	-$(CI) $(CI_OPTIONS) $(SRCS)

depend:
	echo "no depend"

clean:
	-$(RM) $(RM_OPTIONS) $(OBJS) $(TIDY_OPTIONS)

tidy:
	-$(RM) $(RM_OPTIONS) $(TIDY_OPTIONS)

backup: tidy checkin
#
# $Log: not supported by cvs2svn $
#
