#!/bin/csh -f
#
#  short csh script to start PanelPro in java ($Revision: 1.1 $)
#
#  Assumes that the program is being run from the distribution directory
#

# Use the following to eliminate warnings about meta keys
# xprop -root -remove _MOTIF_DEFAULT_BINDINGS

java -noverify -cp .:jmri.jar:lib/log4j.jar:lib/collections.jar:lib/crimson.jar:lib/jdom-jdk11.jar apps.PanelPro.PanelPro


