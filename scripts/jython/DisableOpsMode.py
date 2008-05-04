# Sample script to navigate through the GUI and disable
# the Ops Mode button on the main DecoderPro window.
#
# Set up to work with the JMRI 2.1.5 GUI layout.
#
#
# Author: Bob Jacobsen, copyright 2008
# Part of the JMRI distribution
#
# The next line is maintained by CVS, please don't change it
# $Revision: 1.1 $

import apps

# navigate through the window structure
mainWindow = jmri.util.JmriJFrame.getFrame("DecoderPro")

contentPane = mainWindow.getContentPane()

decoderProPane = contentPane.getComponents()[0]

statusPane = decoderProPane.getComponents()[0]

opsButton = statusPane.getComponents()[2]

# now disable it and say why
opsButton.setEnabled(False)
opsButton.setToolTipText("We have disabled this button for the club layout")

# alternately, uncomment the following to have the button not appear at all
#
# opsButton.setVisible(False)
