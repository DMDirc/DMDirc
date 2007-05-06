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

package uk.org.ownage.dmdirc.plugins.plugins.timeplugin;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import uk.org.ownage.dmdirc.actions.ActionManager;
import uk.org.ownage.dmdirc.plugins.Plugin;

/**
 * Provides various time-related features
 * @author chris
 */
public final class TimePlugin implements Plugin {
    
    /** Is this plugin active? */
    private boolean isActive = false;
    
    /** Have we registered our types already? */
    private static boolean registered;
    
    /** The timer to use for scheduling. */
    private Timer timer;
    
    /** Creates a new instance of TimePlugin. */
    public TimePlugin() {
        
    }
    
    /** {@inheritDoc} */
    public boolean onLoad() {
        if (!registered) {
            ActionManager.registerActionTypes(TimeActionType.values());
            registered = true;
        }
        
        return true;
    }
    
    /** {@inheritDoc} */
    public void onUnload() {
    }
    
    /** {@inheritDoc} */
    public void onActivate() {
        isActive = true;
        
        final int offset = 60 - Calendar.getInstance().get(Calendar.SECOND);
        
        timer = new Timer();
        
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                runTimer();
            }
        }, 1000 * offset, 1000 * 60);
    }
    
    /** Handles a timer event that occurs every minute. */
    public void runTimer() {
        final Calendar cal = Calendar.getInstance();
        
        ActionManager.processEvent(TimeActionType.TIME_MINUTE, null, cal);
        
        if (cal.get(Calendar.MINUTE) == 0) {
            ActionManager.processEvent(TimeActionType.TIME_HOUR, null, cal);
            
            if (cal.get(Calendar.HOUR) == 0) {
                ActionManager.processEvent(TimeActionType.TIME_DAY, null, cal);
            }
        }
    }
    
    /** {@inheritDoc} */
    public boolean isActive() {
        return isActive;
    }
    
    /** {@inheritDoc} */
    public void onDeactivate() {
        isActive = false;
        
        timer.cancel();
        timer = null;
    }
    
    /** {@inheritDoc} */
    public String getVersion() {
        return "0.1";
    }
    
    /** {@inheritDoc} */
    public String getAuthor() {
        return "Chris <chris@dmdirc.com>";
    }
    
    /** {@inheritDoc} */
    public String getDescription() {
        return "Provides time-related actions";
    }
    
    /** {@inheritDoc} */
    public boolean isConfigurable() {
        return false;
    }
    
    /** {@inheritDoc} */
    public void showConfig() {
    }
    
    /** {@inheritDoc} */
    public String toString() {
        return "Time Plugin";
    }
}
