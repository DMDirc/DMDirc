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

package uk.org.ownage.dmdirc.ui.messages;

import java.awt.Color;
import uk.org.ownage.dmdirc.logger.ErrorLevel;
import uk.org.ownage.dmdirc.logger.Logger;

/**
 *
 * @author chris
 */
public class ColourManager {
    
    /** Creates a new instance of ColourManager */
    public ColourManager() {
    }
    
    public static Color getColour(int number) {
        switch (number) {
            case 0:
                return Color.WHITE;
            case 1:
                return Color.BLACK;
            case 2:
                return new Color(0, 0, 127);
            case 3:
                return Color.GREEN;
            case 4:
                return Color.RED;
            case 5:
                return new Color(127, 0, 0);
            case 6:
                return new Color(160, 15, 160);
            case 7:
                return new Color(252, 127, 0);
            case 8:
                return Color.YELLOW;
            case 9:
                return new Color(0, 252, 0);
            case 10:
                return Color.CYAN;
            case 11:
                return new Color(0, 255, 255);
            case 12:
                return Color.BLUE;
            case 13:
                return Color.PINK;
            case 14:
                return Color.GRAY;
            case 15:
                return Color.LIGHT_GRAY;
            default:
                Logger.error(ErrorLevel.WARNING, "Invalid colour: "+number);
                return Color.WHITE;
        }
    }
    
}
