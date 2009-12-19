#! /bin/csh -f
#
# Short csh script to setup and run a JUnit test ($Revision: 1.16.2.1 $)
#
# Assumes that the program is being run from the "java" build directory.
# Do "ant tests" first to build the necessary classes
#

# first, move up to distribution directory to find xml, resources, etc
cd ..

# run the command
java -noverify \
    -Djava.security.policy=lib/security.policy \
    -Djava.rmi.server.codebase=file:java/classes/ \
    -Dsun.java2d.noddraw \
    -Dapple.laf.useScreenMenuBar=true \
    -Dlog4j.ignoreTCL=true \
    -Djava.library.path=.:lib/ \
    -cp .:java/classes:jmriplugins.jar:lib/jmriplugins.jar:jmri.jar:lib/MRJAdapter.jar:lib/RXTXcomm.jar:lib/Serialio.jar:lib/ch.ntb.usb.jar:lib/comm-rxtx.jar:lib/xercesImpl.jar:lib/gluegen-rt.jar:lib/javacsv.jar:lib/jdom.jar:lib/jhall.jar:lib/jinput.jar:lib/jmdns.jar:lib/joal.jar:lib/jython.jar:lib/log4j.jar:lib/servlet.jar:lib/vecmath.jar:lib/activation.jar:lib/mailapi.jar:lib/smtp.jar:lib/ExternalLinkContentViewerUI.jar:java/lib/junit.jar:java/lib/jfcunit.jar:java/lib/jakarta-regexp-1.5.jar:/System/Library/Java \
    $1 $2 $3
