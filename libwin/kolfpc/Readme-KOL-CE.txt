KOL-CE notes.

Main project page:
http://wiki.freepascal.org/KOL-CE

Project pages at SourceForge:
http://sourceforge.net/projects/kol-ce/

Usage
=====

You need to download or create arm-wince cross compiler to be able to use this library.
How to do that read here:
http://wiki.freepascal.org/index.php?title=WinCE_port

Port notes
==========

* To make form fullscreen as most Pocket PC applications dont change the form position and size. If form size and/or position was changed the form will look like dialog with caption and close button. 

Known issues
============

* The following components are not supported: RichEdit, TrayIcon.
* Only gsVertical, gsHorizontal gradient panel styles are supported.
