#! /bin/csh
###############################################################################
#
# Invoke the the PacketPro JMRI appliction, which provide a Jython
# command line.
#
# There must be a local jython interpreter and JMRI libraries available
#
# $Revision: 1.4 $ (CVS maintains this line, do not edit please)
#
setenv CLASSPATH .:java/classes:jmriplugins.jar:lib/jmriplugins.jar:jmri.jar:lib/log4j.jar:lib/jhall.jar:lib/crimson.jar:lib/jdom.jar:lib/jython.jar:lib/javacsv.jar:lib/MRJAdapter.jar:lib/vecmath.jar \

/usr/bin/env jython -i $argv jython/PacketPro.py

