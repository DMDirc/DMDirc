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

package com.dmdirc.ui;

import com.dmdirc.WritableFrameContainer;
import com.dmdirc.commandparser.CommandParser;
import com.dmdirc.ui.components.InputFrame;
import com.dmdirc.ui.input.InputHandler;

/**
 * A custom frame that includes an input field (for use with writable
 * containers).
 * 
 * @author chris
 */
public class CustomInputFrame extends InputFrame {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    
    /** The command parser to use. */
    private final CommandParser commandParser;
    
    /**
     * Creates a new instance of CustomInputFrame.
     * 
     * @param owner The frame container that owns this frame
     * @param commandParser The command parser to use for this frmae
     */
    public CustomInputFrame(final WritableFrameContainer owner,
            final CommandParser commandParser) {
        super(owner);
        
        this.commandParser = commandParser;
        
        setInputHandler(new InputHandler(getInputField(), commandParser, this));
    }

    /** {@inheritDoc} */
    public final CommandParser getCommandParser() {
        return commandParser;
    }
    
}
