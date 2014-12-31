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

package com.dmdirc.actions;

import com.dmdirc.DMDircMBassador;
import com.dmdirc.commandline.CommandLineOptionsModule.Directory;
import com.dmdirc.commandline.CommandLineOptionsModule.DirectoryType;
import com.dmdirc.events.UserErrorEvent;
import com.dmdirc.interfaces.ActionController;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.util.io.FileUtils;

import java.io.IOException;
import java.nio.file.Path;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Utility class that can extract bundled actions.
 */
@Singleton
public class CoreActionExtractor {

    /** The action manager to inform when actions are updated. */
    private final ActionController actionManager;
    /** The directory to extract actions to. */
    private final Path actionsDir;
    /** The event bus to post events to. */
    private final DMDircMBassador eventBus;

    /**
     * Creates a new instance of {@link CoreActionExtractor}.
     *
     * @param actionManager  The action manager to inform when actions are updated.
     * @param actionsDir     The directory to extract actions to.
     * @param eventBus       The event bus to post events to.
     */
    @Inject
    public CoreActionExtractor(final ActionController actionManager,
            @Directory(DirectoryType.ACTIONS) final Path actionsDir,
            final DMDircMBassador eventBus) {
        this.actionManager = actionManager;
        this.actionsDir = actionsDir;
        this.eventBus = eventBus;
    }

    /**
     * Extracts actions bundled with DMDirc to the user's profile's actions directory.
     */
    public void extractCoreActions() {
        try {
            FileUtils.copyResourcesContents(getClass().getResource("/com/dmdirc/actions/defaults/"),
                    actionsDir);
            actionManager.loadUserActions();
        } catch (IOException ex) {
            eventBus.publishAsync(new UserErrorEvent(ErrorLevel.LOW, ex,
                    "Failed to extract actions: " + ex.getMessage(), ""));
        }
    }

}
