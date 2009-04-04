/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.harness.parser;

import com.dmdirc.parser.irc.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

public class TestParser extends IRCParser {

    public final List<String> sentLines = new ArrayList<String>();

    public String nick = "nick";
    
    public String network = null;

    public TestParser() {
        super();
        currentSocketState = SocketState.OPEN;
    }

    public TestParser(MyInfo myDetails, ServerInfo serverDetails) {
        super(myDetails, serverDetails);
        currentSocketState = SocketState.OPEN;
    }

    @Override
    protected void doSendString(String line, boolean fromParser) {
        sentLines.add(line);
    }
    
    public String[] getLine(int index) {
        return tokeniseLine(sentLines.get(index));
    }
    
    public void injectLine(String line) {
        processLine(line);
    }
    
    public void injectConnectionStrings() {
        final String[] lines = new String[]{
            "NOTICE AUTH :Blah, blah",
            ":server 001 " + nick + " :Welcome to the Testing IRC Network, " + nick,
            ":server 002 " + nick + " :Your host is server.net, running version foo",
            ":server 003 " + nick + " :This server was created Sun Jan 6 2008 at 17:34:54 CET",
            ":server 004 " + nick + " server.net foo dioswkgxRXInP bRIeiklmnopstvrDcCNuMT bklov",
            ":server 005 " + nick + " WHOX WALLCHOPS WALLVOICES USERIP PREFIX=(ov)@+ " +
                    (network == null ? "" : "NETWORK=" + network + " ") +
                    ":are supported by this server",
            ":server 005 " + nick + " MAXNICKLEN=15 TOPICLEN=250 AWAYLEN=160 MODES=6 " +
                    "CHANMODES=bIeR,k,l,imnpstrDducCNMT :are supported by this server",
        };
        
        sendConnectionStrings();
        
        for (String line : lines) {
            injectLine(line);
        }
        
        sentLines.clear();
    }

    @Override
    protected void pingTimerTask(final Timer timer) {
        // Do nothing
    }

    @Override
    public void run() {
        injectConnectionStrings();
    }
    
    public void runSuper() {
        super.run();
    }

}
