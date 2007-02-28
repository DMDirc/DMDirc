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

import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import uk.org.ownage.dmdirc.parser.ChannelClientInfo;
import uk.org.ownage.dmdirc.parser.ChannelInfo;
import uk.org.ownage.dmdirc.parser.IRCParser;
import uk.org.ownage.dmdirc.ui.ChannelFrame;
import uk.org.ownage.dmdirc.ui.MainFrame;

/**
 * The Channel class represents the client's view of the channel. It handles
 * callbacks for channel events from the parser, maintains the corresponding
 * ChannelFrame, and handles user input to a ChannelFrame
 * @author chris
 */
public class Channel implements IRCParser.IChannelMessage,
        IRCParser.IChannelGotNames, IRCParser.IChannelTopic,
        IRCParser.IChannelJoin, IRCParser.IChannelPart, IRCParser.IChannelKick,
        IRCParser.IChannelQuit, IRCParser.IChannelAction {
    
    /** The parser's pChannel class */
    private ChannelInfo channelInfo;
    
    /** The server this channel is on */
    private Server server;
    
    /** The ChannelFrame used for this channel */
    private ChannelFrame frame;
    
    /**
     * Creates a new instance of Channel
     * @param server The server object that this channel belongs to
     * @param channelInfo The parser's channel object that corresponds to this channel
     */
    public Channel(Server server, ChannelInfo channelInfo) {
        this.channelInfo = channelInfo;
        this.server = server;
        
        frame = new ChannelFrame(this);
        MainFrame.getMainFrame().addChild(frame);
        
        server.getParser().addChannelMessage(this, channelInfo.getName());
        server.getParser().addChannelTopic(this, channelInfo.getName());
        server.getParser().addChannelGotNames(this, channelInfo.getName());
        server.getParser().addChannelJoin(this, channelInfo.getName());
        server.getParser().addChannelPart(this, channelInfo.getName());
        server.getParser().addChannelQuit(this, channelInfo.getName());
        server.getParser().addChannelKick(this, channelInfo.getName());
        server.getParser().addChannelAction(this, channelInfo.getName());
        
        updateTitle();
    }
    
    public void sendLine(String line) {
        channelInfo.sendMessage(line);
        frame.addLine("> "+line);
    }
    
    public void onChannelMessage(IRCParser tParser, ChannelInfo cChannel,
            ChannelClientInfo cChannelClient, String sMessage, String sHost) {
        if (cChannelClient != null) {
            frame.addLine("<"+cChannelClient.getNickname()+"> "+sMessage);
        }
    }
    
    public void onChannelGotNames(IRCParser tParser, ChannelInfo cChannel) {
        frame.updateNames(channelInfo.getChannelClients());
    }
    
    public void onChannelTopic(IRCParser tParser, ChannelInfo cChannel, boolean bIsJoinTopic) {
        if (bIsJoinTopic) {
            frame.addLine("* Topic is '"+cChannel.getTopic()+"'.");
            frame.addLine("* Set by "+cChannel.getTopicUser()+".");
        } else {
            frame.addLine("* "+cChannel.getTopicUser()+" has changed the topic to '"+cChannel.getTopic()+"'");
        }
        
        updateTitle();
    }
    
    private void updateTitle() {
        frame.setTitle(channelInfo.getName()+" - "+channelInfo.getTopic());
    }
    
    public void onChannelJoin(IRCParser tParser, ChannelInfo cChannel, ChannelClientInfo cChannelClient) {
        frame.addLine("* "+cChannelClient.getNickname()+" has joined the channel");
        frame.addName(cChannelClient);
    }
    
    public void onChannelPart(IRCParser tParser, ChannelInfo cChannel, ChannelClientInfo cChannelClient, String sReason) {
        if (sReason.equals("")) {
            frame.addLine("* "+cChannelClient+" has left the channel");
        } else {
            frame.addLine("* "+cChannelClient+" has left the channel ("+sReason+")");
        }
        frame.removeName(cChannelClient);
    }
    
    public void onChannelKick(IRCParser tParser, ChannelInfo cChannel,
            ChannelClientInfo cKickedClient, ChannelClientInfo cKickedByClient,
            String sReason, String sKickedByHost) {
        String kicker;
        if (cKickedByClient == null) {
            kicker = sKickedByHost;
        } else {
            kicker = cKickedByClient.toString();
        }
        frame.addLine("* "+cKickedClient+" was kicked by "+kicker+": "+sReason);
        frame.removeName(cKickedClient);
    }

    public void onChannelQuit(IRCParser tParser, ChannelInfo cChannel, ChannelClientInfo cChannelClient, String sReason) {
        frame.addLine("* "+cChannelClient+" has quit IRC ("+sReason+")");
        frame.removeName(cChannelClient);
    }

    public void onChannelAction(IRCParser tParser, ChannelInfo cChannel, ChannelClientInfo cChannelClient, String sMessage, String sHost) {
        String source;
        if (cChannelClient == null) {
            source = sHost;
        } else {
            source = cChannelClient.toString();
        }
        frame.addLine("* "+source+" "+sMessage);
    }

    public Server getServer() {
        return server;
    }
    
}
