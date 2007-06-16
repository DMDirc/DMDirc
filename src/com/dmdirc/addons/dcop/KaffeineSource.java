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

package com.dmdirc.addons.dcop;

import com.dmdirc.addons.nowplaying.MediaSource;

/**
 * Uses DCOP to retrieve now playing info from Kaffeine.
 *
 * @author chris
 */
public class KaffeineSource implements MediaSource {
    
    /** {@inheritDoc} */
    public boolean isRunning() {
        final String result = DcopPlugin.getDcopResult("dcop kaffeine KaffeineIface isPlaying").get(0);
        
        return result.indexOf("failed") == -1;
    }
    
    /** {@inheritDoc} */
    public boolean isPlaying() {
        final String result = DcopPlugin.getDcopResult("dcop kaffeine KaffeineIface isPlaying").get(0);
        
        return Boolean.parseBoolean(result);
    }
    
    /** {@inheritDoc} */
    public String getInformation() {
        return DcopPlugin.getDcopResult("dcop kaffeine KaffeineIface artist").get(0) + " - "
                + DcopPlugin.getDcopResult("dcop kaffeine KaffeineIface title").get(0);
    }
    
    /** {@inheritDoc} */
    public String getName() {
        return "Kaffeine";
    }
    
}
