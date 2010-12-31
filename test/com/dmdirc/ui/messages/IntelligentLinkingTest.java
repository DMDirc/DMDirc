/*
 * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.FrameContainer;
import com.dmdirc.Server;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.config.InvalidIdentityFileException;
import java.util.Arrays;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(Parameterized.class)
public class IntelligentLinkingTest {
    
    private final String input, expected;
    private final Styliser styliser;
    
    public IntelligentLinkingTest(String input, String expected) {
        this.input = input;
        this.expected = expected;

        final FrameContainer<?> container = mock(FrameContainer.class);
        final ConfigManager manager = mock(ConfigManager.class);
        final Server server = mock(Server.class);
        when(server.getChannelPrefixes()).thenReturn("#&+");
        when(container.getServer()).thenReturn(server);
        when(container.getConfigManager()).thenReturn(manager);
        
        styliser = new Styliser(container);
    }
    
    @BeforeClass
    public static void setUp() throws InvalidIdentityFileException {
        IdentityManager.load();
    }    
    
    @Test
    public void testLink() throws InterruptedException {
        assertEquals(expected, styliser.doLinks(input)
                .replace(Styliser.CODE_HYPERLINK, '~')
                .replace(Styliser.CODE_CHANNEL, '@'));
    }

    @Parameterized.Parameters
    public static List<String[]> data() {
        final String[][] tests = {
            {"no links here!", "no links here!"},
            {"www.google.com", "~www.google.com~"},
            {"www.google.com.", "~www.google.com~."},
            {"'www.google.com'", "'~www.google.com~'"},
            {"www.google.com, foo", "~www.google.com~, foo"},
            {"http://www.google.com", "~http://www.google.com~"},
            {"www.google.com www.google.com", "~www.google.com~ ~www.google.com~"},
            {"http://www.google.com:80/test#flub", "~http://www.google.com:80/test#flub~"},
            {"(www.google.com)", "(~www.google.com~)"},
            {"(foo: www.google.com)", "(foo: ~www.google.com~)"},
            {"(foo: 'www.google.com')", "(foo: '~www.google.com~')"},
            {"foo: www.google.com, bar", "foo: ~www.google.com~, bar"},
            {"www.google.com?", "~www.google.com~?"},
            {"www.google.com!", "~www.google.com~!"},
            {"http://...", "http://..."},
            {"www...", "www..."},
            {"(www.foo.com www.bar.com)", "(~www.foo.com~ ~www.bar.com~)"},
            {"(www.foo.com/)/ www.bar.com)", "(~www.foo.com/)/~ ~www.bar.com~)"},
            {"(\"http://example.org\")->", "(\"~http://example.org~\")->"},
            {"('http://example.org')->", "('~http://example.org~')->"},
            {"('www.foo.com')->ss('http://example.org');",
                     "('~www.foo.com~')->ss('~http://example.org~');"},
            {"svn+ssh://foo@bar", "~svn+ssh://foo@bar~"},
            {"/var/web/www.foo.com/bar", "/var/web/www.foo.com/bar"},
            {"\"foo\" www.google.com \"www.google.com\"",
                     "\"foo\" ~www.google.com~ \"~www.google.com~\""},
            {"www.example.com/blah(foobar)", "~www.example.com/blah(foobar)~"},

            {"000000http://www.google.com/", "000000~http://www.google.com/~"},
            {"ABCDEFhttp://www.google.com/", "ABCDEF~http://www.google.com/~"},

            // ------------ Channels -------------------
            {"#test", "@#test@"},
            {"&test", "@&test@"},
            {"+test", "@+test@"},
            {"(#test)", "(@#test@)"},
            {"\"#test\"", "\"@#test@\""},
            {"'#test'", "'@#test@'"},
            {"(join #test)", "(join @#test@)"},
            {"\"join #test\"", "\"join @#test@\""},
            {"'join #test'", "'join @#test@'"},
            {"#test.", "@#test@."},
            {"#test1,#test2,#test3", "@#test1@,@#test2@,@#test3@"},
            {"(\"join #test!\")", "(\"join @#test@!\")"},
        };

        return Arrays.asList(tests);
    }    

}
