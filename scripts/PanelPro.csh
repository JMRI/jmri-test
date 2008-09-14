#! /bin/csh -f
#
# Short csh script to start PanelPro in java ($Revision: 1.10 $)
#
# Assumes that the program is being run from the distribution directory.
# Uncomment and change the following to match the JMRI
# install directory if you would like to run this script 
# without cd'ing into the install directory.
#   cd /usr/local/JMRI
#
# If you need to add any additional Java options or defines,
# include them in the JMRI_OPTIONS environment variable
#
# Use the following to eliminate warnings about meta keys
# xprop -root -remove _MOTIF_DEFAULT_BINDINGS

if ( ! $?JMRI_OPTIONS ) setenv JMRI_OPTIONS ""

java -Xmx200m -noverify -Djava.security.policy=lib/security.policy -Djava.rmi.server.codebase=file:java/classes/ -Dsun.java2d.noddraw \
     -cp .:java/classes:jmriplugins.jar:lib/jmriplugins.jar:jmri.jar:lib/log4j.jar:lib/jhall.jar:lib/crimson.jar:lib/jdom.jar:lib/jython.jar:lib/javacsv.jar:lib/MRJAdapter.jar:lib/jakarta-regexp-1.5.jar:lib/vecmath.jar \
     $JMRI_OPTIONS \
     apps.PanelPro.PanelPro


