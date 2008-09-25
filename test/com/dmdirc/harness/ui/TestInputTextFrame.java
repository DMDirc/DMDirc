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

package com.dmdirc.harness.ui;

import com.dmdirc.WritableFrameContainer;
import com.dmdirc.commandparser.PopupType;
import com.dmdirc.commandparser.parsers.CommandParser;
import com.dmdirc.commandparser.parsers.GlobalCommandParser;
import com.dmdirc.ui.swing.components.InputTextFrame;

import javax.swing.JPopupMenu;

public class TestInputTextFrame extends InputTextFrame {

    public TestInputTextFrame(WritableFrameContainer owner) {
        super(owner);
    }

    @Override
    public void addCustomPopupItems(JPopupMenu popupMenu) {
        return;
    }

    @Override
    public PopupType getChannelPopupType() {
        return PopupType.CHAN_CHANNEL;
    }

    @Override
    public PopupType getHyperlinkPopupType() {
        return PopupType.CHAN_CHANNEL;
    }

    @Override
    public PopupType getNicknamePopupType() {
        return PopupType.CHAN_CHANNEL;
    }

    @Override
    public PopupType getNormalPopupType() {
        return PopupType.CHAN_CHANNEL;
    }

    @Override
    public CommandParser getCommandParser() {
        return GlobalCommandParser.getGlobalCommandParser();
    }

}
