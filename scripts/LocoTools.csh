#! /bin/csh -f
#
#  short csh script to start LocoTools in java ($Revision: 1.3 $)
#
#  Assumes that the program is being run from the distribution directory
#
#  In this version, the .jar file for the javax.comm package is not included.
#  See http://www.interstice.com/~kevinh/linuxcomm.html for information
#  on creating and installing a version based on RXMX

java -noverify -Djava.security.policy=lib/security.policy -Djava.security.policy=lib/security.policy -cp .:jmri.jar:lib/log4j.jar:lib/collections.jar:lib/crimson.jar:lib/jdom-jdk11.jar apps.LocoTools.LocoTools

