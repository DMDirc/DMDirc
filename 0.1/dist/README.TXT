== Introduction ==

Thanks for taking an interest in DMDirc. This document will explain how to get 
DMDirc 0.1 up and running, how to use it, and what we plan to do in future
versions. If you run into any problems, the authors can all be found in #DMDirc
on quakenet (irc.quakenet.org).

== Getting Started ==

To start using DMDirc 0.1, you will need to download and decompress the zip
file available from the downloads page. As DMDirc is written in Java, you will
also need a Java Runtime Environment (JRE), which can be downloaded from
http://www.java.com. DMDirc requires that you have the 1.5.0 JRE or newer
(also known as 5.0).

Once you have Java installed, and have extracted the DMDirc files from their
zip file, you can start the client by executing the bash or shell script in the
main directory (Windows users: double click on the batch file,
Linux/Unix users: run the .sh script from a terminal). You can also run DMDirc
from the command line, by issuing the command java -jar DMDirc.jar. The main 
DMDirc window should now be open.

== Connecting to a server ==

To connect to a server, click on the File menu, and select 'New server'. Here 
you can enter a server name (or IP) and port, along with a password if the IRC 
server requires it. The default settings should connect you to a Quakenet IRC 
server - feel free to stop by #DMDirc and say hello to us!

When you click on OK, two windows will open - a server window, and a raw 
window. The server window will display messages from the server (such as the 
message of the day) in future versions, but at present it just tells you that 
you are connecting. The raw window shows the raw text that is being sent and 
received by the client. This allows advanced users to see messages or other 
data that is being received but not yet handled by the client.

== Commands and Configuration ==

You may issue commands to DMDirc from any server, raw or channel window. 
Commands by default start with a / character. To view a list of commands that
are applicable to the current window, you can type /help. To join a channel,
you can issue the /join command in a server or raw window (or another channel
window, if you have one open), for example /join #DMDirc will join the #DMDirc
channel. Because there is an obvious shortage in commands in this initial
release, you can also send commands directly to the server by typing them in
the server or raw windows without the initial '/'. For example, to issue a 
whois for a user, you could type WHOIS MD87, or to change a channel topic,
TOPIC #DMDirc :this is the new topic.

The DMDirc's configuration is stored in a file called dmdirc.xml, located in
the '.DMDirc' directory in your home dir (for Linux users: 
/home/yourusername/ by default, and Windows XP users: 
C:\Documents and Settings\yourusername\ - note this is not the same folder as 
"My Documents", it is the parent folder). The XML format should be fairly easy 
to understand. The most useful option for this version is the 
general.defaultnick property, which specifies the nickname to try to use when
connecting to an IRC server. In the future this will be replaced by a more 
robust identity manager.

== Future versions ==

We have many plans for future releases of DMDirc. Version 0.2 will mainly focus
on user interface upgrades - adding tab completion, support for coloured 
messages and log files, and a proper way to switch between windows. 0.2 will
also feature proper support for Queries (private messages to a user), as well
as support for more commands.

Beyond version 0.2, we have plans to integrate plugins, allowing additional
functionality to be implemented seperate from the client, an identity manager,
allowing you to specify different client settings for each server or network,
support for 'Actions' - a way of "scripting" the client without actually having
to write a script, and many more features.

You can see some of our plans on the issues page. If you have any suggestions,
or ideas of your own, please feel free to join the #DMDirc channel on Quakenet
to discuss them with us.
