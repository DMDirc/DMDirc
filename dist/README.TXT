= Introduction =

Welcome to DMDirc 0.2. This is the second major release of DMDirc, and we believe that
the client now has enough features that it is usable as a client for basic day-to-day use. 
We still have a long way to go before we're finished, and if you have any ideas for 
improvements, please join us in #DMDirc on Quakenet.

= Getting Started =

To start using DMDirc 0.1, you will need to download and decompress the zip file available
from the downloads page. As DMDirc is written in Java, you will also need a Java Runtime 
Environment (JRE), which can be downloaded from http://www.java.com. DMDirc requires that 
you have the 1.5.0 JRE or newer (also known as 5.0).

Once you have Java installed, and have extracted the DMDirc files from their zip file, you 
can start the client by executing the bash or shell script in the main directory (Windows 
users: double click on the batch file, Linux/Unix users: run the .sh script from a 
terminal). You can also run DMDirc from the command line, by issuing the command java -jar 
DMDirc.jar. The main DMDirc window should now be open. 

= Connecting to a server =

To connect to a server, click on the File menu, and select 'New server'. Here you can 
enter a server name (or IP) and port, along with a password if the IRC server requires it.
The default settings should connect you to a Quakenet IRC server - feel free to stop by 
#DMDirc and say hello to us! If you use the client regularly, you may want to change the
default server, port and password in the client's configuration file - details below.

When you click on OK, two windows will open - a server window, and a raw window. The 
server window will display messages from the server (such as the message of the day) in 
future versions, but at present it provides very little information. The raw window shows
the raw text that is being sent and received by the client. This allows advanced users to 
see messages or other data that is being received but not yet handled by the client. 

= Commands and Configuration =

You may issue commands to DMDirc from any server, raw, channel or query window. Commands 
by default start with a / character. To view a list of commands that are applicable to
the current window, you can type /help. To join a channel, you can issue the
/join command in a server or raw window (or another channel window, if you have one open),
for example /join #DMDirc will join the #DMDirc channel. Because there is an obvious
shortage in commands in this release, you can also send commands directly to the server by
typing them in the server or raw windows without the initial '/'. For example, to issue a
whois for a user, you could type WHOIS MD87, or to change a channel topic, 
TOPIC #DMDirc :this is the new topic.

DMDirc's configuration is stored in a file called dmdirc.xml, located in the '.DMDirc' 
directory in your home dir (for Linux users: /home/yourusername/ by default, and Windows 
XP users: C:\Documents and Settings\yourusername\ - note this is not the same folder as 
"My Documents", it is the parent folder). The XML format should be fairly easy to 
understand. Some of the options that you may be interested in for this version are listed 
below:

  * general.server, general.port, general.password - default server settings
  * general.defaultnick, general.alternatenick - default primary and alternate nickname
  * ui.maximisewindows - whether to start with the internal windows maximised or not
  * ui.backgroundcolour, ui.foregroundcolour - the default colours for windows

In the next version of DMDirc, we will be adding a proper user interface to allow you to 
easily modify these settings.

= Text formatting and advanced text features =

This release of DMDirc introduces support for bold, underlined and coloured text. You 
should be able to see formatting and colours sent by any other IRC client that supports 
them, and you can also send them yourself, by using ctrl+b, ctrl+u and ctrl+k in the input
box. For colours, you need to follow the symbol by a foreground colour and optionally a 
comma followed by a background colour. The colour codes used by DMDirc are the same as 
those used by mIRC (i.e., 0 is white, 1 is black, 4 is red, etc).

This release also includes tab completion, and text history. If you start typing a 
nickname, channel name, or one of DMDirc's commands, and press tab, the client will 
complete it for you. If there is more than one possible match, the client will complete
as much as possible. You can also use the arrow keys to browse previous lines that you
have entered into the window (up goes back in time, down goes forward in time).

We have also introduced a new text formatting ("text theme") engine that allows you
to customise how text appears in the client. You can modify the output by editing the
format.properties file in the client's configuration directory (see above). A sample
file can be found on the DMDirc wiki: http://code.google.com/p/dmdirc/w/FormatExample

= Future versions =

We have many plans for future releases of DMDirc. Version 0.3 will focus on various
immediate usability improvements (such as a preferences dialog), as well as adding
several backend features which are required for some of the more advanced functions we
have planned for later releases.

Beyond version 0.3, we have plans to integrate plugins, allowing additional functionality 
to be implemented seperate from the client, an identity manager, allowing you to specify 
different client settings for each server or network, support for 'Actions' - a way of 
"scripting" the client without actually having to write a script, and many more features.

You can see some of our plans on the issues page 
(http://code.google.com/p/dmdirc/issues/list). If you have any suggestions, or ideas of
your own, please feel free to join the #DMDirc channel on Quakenet to discuss them with 
us. 

