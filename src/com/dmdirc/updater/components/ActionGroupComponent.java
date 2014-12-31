/*
 * Copyright (c) 2006-2015 DMDirc Developers
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

package com.dmdirc.updater.components;

import com.dmdirc.actions.ActionGroup;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.updater.UpdateComponent;
import com.dmdirc.updater.Version;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Update component for action groups.
 */
public class ActionGroupComponent implements UpdateComponent {

    private final ActionManager actionManager;

    public ActionGroupComponent(final ActionManager actionManager,
            final ActionGroup group) {
        this.actionManager = actionManager;
        this.group = group;
    }
    /** The group that this component represents. */
    private final ActionGroup group;

    @Override
    public String getName() {
        return "addon-" + group.getComponent();
    }

    @Override
    public String getFriendlyName() {
        return "Action pack: " + group.getName();
    }

    @Override
    public Version getVersion() {
        return group.getVersion();
    }

    @Override
    public String getFriendlyVersion() {
        return String.valueOf(getVersion());
    }

    @Override
    public boolean requiresRestart() {
        return false;
    }

    @Override
    public boolean requiresManualInstall() {
        return false;
    }

    @Override
    public String getManualInstructions(final Path path) {
        return "";
    }

    @Override
    public boolean doInstall(final Path path) throws IOException {
        actionManager.installActionPack(path);
        return false;
    }

}
