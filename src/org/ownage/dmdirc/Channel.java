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

package org.ownage.dmdirc;

import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import org.ownage.dmdirc.parser.ChannelClientInfo;
import org.ownage.dmdirc.parser.ChannelInfo;
import org.ownage.dmdirc.parser.IRCParser;
import org.ownage.dmdirc.ui.ChannelFrame;
import org.ownage.dmdirc.ui.MainFrame;

/**
 * The Channel class represents the client's view of the channel. It handles
 * callbacks for channel events from the parser, maintains the corresponding
 * ChannelFrame, and handles user input to a ChannelFrame
 * @author chris
 */
public class Channel {
    
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
        
        frame = new ChannelFrame();
        MainFrame.getMainFrame().addChild(frame);
        
        // I have no idea what's going on with the indentation here.
        server.getParser().addChannelMessage(
                new IRCParser.IChannelMessage() {
            public void onChannelMessage(IRCParser tParser, ChannelInfo cChannel,
                    ChannelClientInfo cChannelClient, String sMessage, String sHost) {
                Channel.this.frame.addLine("<"+cChannelClient.getNickname()+"> "+sMessage);
            }
            
        }
        , channelInfo.getName());
    }
       
}
