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

package com.dmdirc.ui.dialogs.firstrunwizard;

import com.dmdirc.Config;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.resourcemanager.ResourceManager;
import com.dmdirc.ui.dialogs.ProfileEditorDialog;
import com.dmdirc.ui.dialogs.wizard.Step;
import com.dmdirc.ui.dialogs.wizard.Wizard;
import com.dmdirc.ui.dialogs.wizard.WizardDialog;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * First run wizard, used to initially setup the client for the user.
 */
public final class FirstRunWizard implements Wizard {
    
    /** Wizard dialog. */
    private static WizardDialog wizardDialog;
    
    /** Instatiate the wizard. */
    public FirstRunWizard() {
    }
    
    /** {@inheritDoc} */
    public void stepChanged(final int oldStep, final int newStep) {
        //Ignore
    }
    
    /** {@inheritDoc} */
    public void wizardFinished() {
        final ResourceManager resourceManager =
                ResourceManager.getResourceManager();
        if (resourceManager == null) {
            return;
        }
        if (((StepOne) wizardDialog.getStep(0)).getPluginsState()) {
            //Copy plugins
            try {
                resourceManager.extractResources("com/dmdirc/addons",
                        Config.getConfigDir() + "plugins");
            } catch (IOException ex) {
                Logger.error(ErrorLevel.TRIVIAL,
                        "Failed to extract plugins", ex);
            }
        }
        
        if (((StepOne) wizardDialog.getStep(0)).getActionsState()) {
            //Copy actions
            final Map<String, byte[]> resources =
                    resourceManager.getResourcesStartingWithAsBytes(
                    "com/dmdirc/actions/defaults");
            for (Entry<String, byte[]> resource : resources.entrySet()) {
                try {
                    final String resourceName = Config.getConfigDir() + "actions"
                            + resource.getKey().substring(27, resource.getKey().length());
                    final File newDir = new File(
                            resourceName.substring(0, resourceName.lastIndexOf('/')) + "/");
                    
                    if (!newDir.exists()) {
                        newDir.mkdirs();
                    }
                    
                    final File newFile = new File(newDir,
                            resourceName.substring(resourceName.lastIndexOf('/') + 1,
                            resourceName.length()));
                    
                    if (!newFile.isDirectory()) {
                        resourceManager.resourceToFile(resource.getValue(), newFile);
                    }
                } catch (IOException ex) {
                    Logger.error(ErrorLevel.TRIVIAL,
                            "Failed to extract plugins", ex);
                }
            }
        }
        
        if (((StepTwo) wizardDialog.getStep(1)).getProfileManagerState()) {
            ProfileEditorDialog.showActionsManagerDialog();
        }
    }
    
    /** Displays the First run wizard. */
    public static void display() {
        final List<Step> steps = new ArrayList<Step>();
        
        steps.add(new StepOne());
        steps.add(new StepTwo());
        
        wizardDialog = new WizardDialog("Setup wizard", steps,
                new FirstRunWizard(), true);
        
        wizardDialog.setPreferredSize(new Dimension(400, 350));
        
        wizardDialog.display();
    }
}
