#! /bin/tcsh -f
#
# Short script to run the nightly build
#
# You can run this directly from the command line in the 
# development (java) directory if you want to check in advance
# that your next commit won't "break the build"
#
# Tasks:
#   Update to HEAD from CVS
#   Compile all from scratch using the clean/init/tests targets
#   Run JUnit tests in jmri.HeadLessTest
#
# Errors, warnings and failures are reported via email 
# to the jmri-builds@lists.sourceforge.net list.
#
#

# Place to send email.  Change this when debugging!
set error_destination = jmri-builds@lists.sourceforge.net

rm -f nightlybuildlog.txt
date > nightlybuildlog.txt

setenv CVS_RSH ssh
if ( { (cd ..; cvs -q update -d >>& java/nightlybuildlog.txt) } ) then
# probably OK

else
# did not terminate OK, mail the log
echo Error in CVS checkout
cat nightlybuildlog.txt | mail -s "Error in CVS checkout" ${error_destination}
exit
endif

if ( { ((ant init clean && ant init tests) >>& nightlybuildlog.txt) } ) then
# probably OK

else
# did not terminate OK, mail the log
echo Did not build successfully
cat nightlybuildlog.txt | mail -s "Did not build successfully" ${error_destination}
exit
endif

if ( { (./runtest.csh jmri.HeadLessTest >>& nightlybuildlog.txt) } ) then
# probably OK

else
# did not terminate OK, mail the log
echo Did not run successfully
cat nightlybuildlog.txt | mail -s "Did not run successfully" ${error_destination}
exit
endif

# check the log for error messages (searches cvs, build log too, but those shouldn't trip the comparison
if ( { grep ERROR nightlybuildlog.txt >/dev/null || grep WARN nightlybuildlog.txt >/dev/null } ) then
# errors found, mail the log
echo Errors found in log
cat nightlybuildlog.txt | mail -s "Errors found in log" ${error_destination}
endif
