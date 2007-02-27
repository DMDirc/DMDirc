/*
 * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack, Gregory Holmes
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package uk.org.ownage.dmdirc;

import java.util.Hashtable;
import uk.org.ownage.dmdirc.commandparser.ServerCommandParser;
import uk.org.ownage.dmdirc.parser.ChannelInfo;
import uk.org.ownage.dmdirc.parser.ServerInfo;
import uk.org.ownage.dmdirc.ui.MainFrame;
import uk.org.ownage.dmdirc.ui.ServerFrame;
import java.util.Vector;
import uk.org.ownage.dmdirc.parser.IRCParser;

/**
 * The Server class represents the client's view of a server. It maintains
 * a list of all channels, queries, etc, and handles parser callbacks pertaining
 * to the server
 * @author chris
 */
public class Server {
    
    /**
     * Open channels that currently exist on the server
     */
    private Hashtable<String,Channel> channels  = new Hashtable<String,Channel>();
    
    /**
     * The ServerFrame corresponding to this server
     */
    private ServerFrame frame;
    
    /**
     * The IRC Parser instance handling this server
     */
    private IRCParser parser;
    
    /**
     * Creates a new instance of Server
     * @param server The hostname/ip of the server to connect to
     * @param port The port to connect to
     * @param password The server password
     */
    public Server(String server, int port, String password) {
        
        ServerManager.getServerManager().registerServer(this);
        
        frame = new ServerFrame(this);
        frame.setTitle(server+":"+port);
        
        MainFrame.getMainFrame().addChild(frame);
        
        frame.addLine("Connecting to "+server+":"+port);
              
        parser = new IRCParser(new ServerInfo(server, port, password));
        
        parser.addChannelSelfJoin(new IRCParser.IChannelSelfJoin() {
            public void onChannelSelfJoin(IRCParser tParser, ChannelInfo cChannel) {
                Server.this.addChannel(cChannel);
            }
            
        });
        
        Raw raw = new Raw(this);
              
        try {           
            Thread thread = new Thread(parser);
            thread.start();
        } catch (Exception ex) {
            frame.addLine("ERROR: "+ex.getMessage());
        }
    }
    
    /**
     * Called on destruction, the server unregisters itself with the ServerManager
     * @throws java.lang.Throwable ...
     */
    protected void finalize() throws Throwable {
        ServerManager.getServerManager().unregisterServer(this);
    }
    
    /**
     * Retrieves the parser used for this connection
     * @return IRCParser this connection's parser
     */
    public IRCParser getParser() {
        return parser;
    }
    
    public void addLine(String line) {
        frame.addLine(line);
    }

    private void addChannel(ChannelInfo chan) {
        channels.put(chan.getName(), new Channel(this, chan));
    }
    
}
