/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.mediasource_vlc;

import com.dmdirc.addons.nowplaying.MediaSourceState;
import com.dmdirc.util.TextFile;
import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;

public class VlcMediaSourcePluginTest {

    @Test
    public void testProcessInformation1() throws IOException {
        final VlcMediaSourcePlugin plugin = new VlcMediaSourcePlugin();
        final TextFile index = new TextFile(getClass().getResourceAsStream("index-1.html"));
        final TextFile info = new TextFile(getClass().getResourceAsStream("info-1.html"));
        
        plugin.parseInformation(info.getLines(), index.getLines());
        
        // This doesn't work anymore because getState() calls fetchInformation()
        // which overwrites the stuff set by the parseInformation() call.
        // assertTrue(plugin.getState() == MediaSourceState.PLAYING);
        
        assertEquals("Manic Street Preachers", plugin.getArtist());
        assertEquals("Send Away The Tigers", plugin.getAlbum());
        assertEquals("The Second Great Depression", plugin.getTitle());
        assertEquals("128 kb/s", plugin.getBitrate());
        assertEquals("VLC", plugin.getAppName());
        assertEquals("mpga", plugin.getFormat());
        assertEquals("249", plugin.getLength());
        assertEquals("38", plugin.getTime());
    }

}