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

package com.dmdirc.ui.messages;

import com.dmdirc.config.IdentityManager;
import com.dmdirc.ui.core.util.Utils;
import java.awt.Color;
import java.awt.font.TextAttribute;
import java.text.AttributedCharacterIterator;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.swing.text.DefaultStyledDocument;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class StyliserStylesTest {

    protected String input, output;

    public StyliserStylesTest(String input, String output) {
        IdentityManager.load();
        
        this.input = input;
        this.output = output;
    }
        
    @Test
    public void testStyle() {
        assertEquals(output, style(input));
    }
    
    protected static String style(final String input) {
        final DefaultStyledDocument doc = new DefaultStyledDocument();
        final StringBuilder builder = new StringBuilder();
        Styliser.addStyledString(doc, new String[]{input});
        final AttributedCharacterIterator aci = Utils.getAttributedString(
                new String[]{input, }, IdentityManager.getGlobalConfig()).
                getAttributedString().getIterator();
         
        Map<AttributedCharacterIterator.Attribute, Object> map = null;
        char chr = aci.current();
        
        while (aci.getIndex() < aci.getEndIndex()) {
            if (!aci.getAttributes().equals(map)) {                
                style(aci.getAttributes(), builder);
                map = aci.getAttributes();
            }
            
            builder.append(chr);
            chr = aci.next();
        }
        
        return builder.toString();
    }
    
    protected static void style(final Map<AttributedCharacterIterator.Attribute, Object> map,
            final StringBuilder builder) {                    
        builder.append('<');
        
        String[] entries = new String[9];
        
        for (Map.Entry<AttributedCharacterIterator.Attribute, Object> entry : map.entrySet()) {
        
            if (entry.getKey().equals(TextAttribute.FOREGROUND)) {
                entries[0] = "color=" + toColour(entry.getValue());
            } else if (entry.getKey().equals(TextAttribute.BACKGROUND)) {
                entries[1] = "background=" + toColour(entry.getValue());
            } else if (entry.getKey().equals(TextAttribute.WEIGHT)) { 
                entries[2] = "bold";
            } else if (entry.getKey().equals(TextAttribute.FAMILY)
                    && entry.getValue().equals("monospaced")) {
                entries[3] = "monospace";
            } else if (entry.getKey().equals(TextAttribute.POSTURE)) {
                entries[4] = "italic";
            } else if (entry.getKey().equals(TextAttribute.UNDERLINE)) {
                entries[5] = "underline";
            } else if (entry.getKey().equals(IRCTextAttribute.HYPERLINK)) {
                entries[6] = "hyperlink";
            } else if (entry.getKey().equals(IRCTextAttribute.CHANNEL)) {
                entries[7] = "channel";
            } else if (entry.getKey().equals(IRCTextAttribute.NICKNAME)) {
                entries[8] = "nickname";
            }
        }
        
        int count = 0;
        for (String entry : entries) {
            if (entry != null) {
                builder.append(entry);
                builder.append(',');
                count++;
            }
        }
        
        if (count > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }
        builder.append('>');
    }
    
    protected static String toColour(final Object object) {
        final Color colour = (Color) object;
        
        return colour.getRed() + "," + colour.getGreen() + ","
                + colour.getBlue();
    }
    
    @Parameterized.Parameters
    public static List<String[]> data() {
        final String[][] tests = {
            // No style
            {"Blah blah blah", "<>Blah blah blah"},
            // Bold
            {"Blahblah", "<>Blah<bold>blah"},
            {"Blahblah", "<>Blah<bold>b<>lah"},
            {"Blahblah", "<>Blah<bold>b<>lah"},
            // Bold + Underline
            {"Blahblah", "<>B<underline>lah<bold,underline>b<underline>lah"},
            {"Blahblah", "<>B<underline>lahb<bold>lah"},
            {"Blahblah", "<>B<underline>lahb<bold>l<>ah"},
            // IRC colours
            {"4moo", "<color=255,0,0>moo"},
            {"4moo", "<color=255,0,0>m<>oo"},
            {"4moo", "<color=255,0,0>m<>oo"},
            {"20moo", "<color=255,0,0>m<>oo"}, // Colours wrap around
            {"4,4moo", "<color=255,0,0,background=255,0,0>m<>oo"},
            // Persistant irc colours
            {"4m0oo", "<color=255,0,0>m<color=255,255,255>o<color=255,0,0>o"},
            {"4moo", "<color=255,0,0>moo"},
            {"4,0moo", "<color=255,0,0,background=255,255,255>moo"},
            // Hex colours
            {"FF0000moo", "<color=255,0,0>moo"},
            {"FF0000moo", "<color=255,0,0>m<>oo"},
            {"FF0000moo", "<color=255,0,0>m<>oo"},
            {"QUXmoo", "<>QUXmoo"},
            {"FFFFFQUXmoo", "<>FFFFFQUXmoo"},
            {"FF0000,FF0000moo", "<color=255,0,0,background=255,0,0>m<>oo"},
            // Persistant hex colours
            {"FF0000mFFFFFFoo", "<color=255,0,0>m<color=255,255,255>o<color=255,0,0>o"},
            {"FF0000moo", "<color=255,0,0>moo"},
            {"FF0000,FFFFFFmoo", "<color=255,0,0,background=255,255,255>moo"},
            // Fixed width
            {"Blahblah", "<>Blah<monospace>b<>lah"},
            {"Blahblah", "<>Blah<monospace>b<>lah"}, // Issue 1413
            // Italics
            {"Blahblah", "<>Blah<italic>b<>lah"},
            {"Blahblah", "<>Blah<italic>b<>lah"},
            // Nesting
            {"Blahblahblahblah", "<>Blah<italic>b<bold,italic>lahblah<bold>b<>lah"},
            // Negation
            {"\u0012Blah4FF0000moo", "<>Blahmoo"},
            {"\u0012Blah4FF0000moo\u0012foo", "<>Blahmoo<bold>foo"},
        };

        return Arrays.asList(tests);
    } 

}
