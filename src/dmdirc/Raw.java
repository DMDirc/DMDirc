/*
 * Copyright (c) 2006-2007 Chris Smith, Shane Mc Cormack
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

package dmdirc;

import dmdirc.parser.IRCParser;
import dmdirc.ui.MainFrame;
import dmdirc.ui.ServerFrame;

/**
 *
 * @author chris
 */
public class Raw {
    
    private Server server;
    private ServerFrame frame;
    
    /** Creates a new instance of Raw */
    public Raw(Server server) {
        this.server = server;
        
        frame = new ServerFrame();
        
        MainFrame.getMainFrame().addChild(frame);
        
        server.AddDataIn(new IRCParser.IDataIn() {
            public void onDataIn(IRCParser tParser, String sData) {
                Raw.this.frame.addLine("<<< "+sData);
            }
        });
        
        server.AddDataOut(new IRCParser.IDataOut() {
            public void onDataOut(IRCParser tparser, String sData) {
                Raw.this.frame.addLine(">>> "+sData);
            }
        });
    }
    
}
