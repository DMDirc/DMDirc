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

package uk.org.ownage.dmdirc.ui.framemanager;

import javax.swing.JComponent;
import uk.org.ownage.dmdirc.Channel;
import uk.org.ownage.dmdirc.Query;
import uk.org.ownage.dmdirc.Raw;
import uk.org.ownage.dmdirc.Server;

/**
 *
 * @author chris
 */
public interface FrameManager {
    
    public void setParent(JComponent parent);
    
    public void addServer(Server server);
    public void delServer(Server server);
    
    public void addChannel(Server server, Channel channel);
    public void delChannel(Server server, Channel channel);
    
    public void addQuery(Server server, Query query);
    public void delQuery(Server server, Query query);
    
    public void addRaw(Server server, Raw raw);
    public void delRaw(Server server, Raw raw);
    
}
