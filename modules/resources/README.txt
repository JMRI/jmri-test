# JMRI Resources Module README

The _JMRI Resources Module_ is a temporary placeholder module for resources that need to be distributed to individual modules when the _JMRI Library Module_ is broken into logical modules.

## Contents

This module provides the following five resource directories:
* __help__
Monolithic help contents. These should be broken up across the modules providing the functionality, and rely on the NetBeans help engine for display; this would allow third-party modules to plug their help contents into the application help search and indexes, and keep help contents for disabled or removed modules out of the user help.
* __jython__
Python scripts. These should ideally be shipped with either the Python module or the module who’s functionality the script depends on.
* __resources__
Various resources (icons, images, and sounds) that may or may not make more sense elsewhere.
* __web__
Web server and servlet resources. These need to ship as part of the web server or supporting servlets.
* __xml__
XML-based resources that need to be distributed among the modules they support or shipped in their own module (decoder definitions and signals).

## Building

This module has a strange build process. Because the contents of this module exist in the root of the non-modular JMRI development space, `ant build` copies the five directories into the modular build space, and then packages them. `ant clean` reverses this, and the help, python, resources, web, and xml directories in the build space are ignored by SVN.

Once JMRI commits to a modular design, this will be changed to remove the extra copy step and to retain contents of the modular build space in SVN.