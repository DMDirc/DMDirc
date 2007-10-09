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

package com.dmdirc.ui.swing.dialogs.wizard.firstrun;

import com.dmdirc.Main;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.ui.interfaces.FirstRunWizard;
import com.dmdirc.ui.swing.MainFrame;
import com.dmdirc.ui.swing.dialogs.ProfileEditorDialog;
import com.dmdirc.ui.swing.dialogs.wizard.Step;
import com.dmdirc.ui.swing.dialogs.wizard.Wizard;
import com.dmdirc.ui.swing.dialogs.wizard.WizardDialog;
import com.dmdirc.util.resourcemanager.ResourceManager;

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
public final class SwingFirstRunWizard implements Wizard, FirstRunWizard {
    
    /** Wizard dialog. */
    private WizardDialog wizardDialog;
    
    /** First run or update. */
    private boolean firstRun = true;
    
    /** Instatiate the wizard. */
    public SwingFirstRunWizard() {
    }
    
    /**
     * Instantiate the wizard.
     *
     * @param firstRun is this the first run or an update?
     */
    public SwingFirstRunWizard(final boolean firstRun) {
        this.firstRun = firstRun;
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
            extractPlugins(resourceManager);
        }
        if (((StepOne) wizardDialog.getStep(0)).getActionsState()) {
            extractActions(resourceManager);
        }
        
        if (firstRun && ((StepTwo) wizardDialog.getStep(1)).getProfileManagerState()) {
            ProfileEditorDialog.showProfileEditorDialog();
        }
    }
    
    /** {@inheritDoc} */
    public void wizardCancelled() {
        wizardDialog.dispose();
    }
    
    /** {@inheritDoc} */
    public void extractPlugins(final ResourceManager resourceManager) {
        extractCorePlugins(resourceManager.getResourceManager());
    }
    
    /**
     * Extracts the core plugins.
     *
     * @param resourceManager ResourceManager to use
     */
    public static void extractCorePlugins(final ResourceManager resourceManager) {
        //Copy plugins
        try {
            resourceManager.extractResources("com/dmdirc/addons",
                    Main.getConfigDir() + "plugins");
            PluginManager.getPluginManager().reloadAllPlugins();
        } catch (IOException ex) {
            Logger.userError(ErrorLevel.LOW, "Failed to extract plugins");
        }
    }
    
    /** {@inheritDoc} */
    public void extractActions(final ResourceManager resourceManager) {
        extractCoreActions(resourceManager.getResourceManager());
    }
    
    /**
     * Extracts the core actions.
     *
     * @param resourceManager ResourceManager to use
     */
    public static void extractCoreActions(final ResourceManager resourceManager) {
        //Copy actions
        final Map<String, byte[]> resources =
                resourceManager.getResourcesStartingWithAsBytes(
                "com/dmdirc/actions/defaults");
        for (Entry<String, byte[]> resource : resources.entrySet()) {
            try {
                final String resourceName = Main.getConfigDir() + "actions"
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
                Logger.userError(ErrorLevel.LOW, "Failed to extract actions");
            }
        }
        ActionManager.loadActions();
    }
    
    /** {@inheritDoc} */
    public void display() {
        final List<Step> steps = new ArrayList<Step>();
        
        if (firstRun) {
            steps.add(new SetupStep());
            steps.add(new StepTwo());
        } else {
            steps.add(new UpdateStep());
        }
        
        wizardDialog = new WizardDialog(firstRun ? "Setup wizard" :
            "Migration wizard", steps, this, true,
                (MainFrame) Main.getUI().getMainWindow());
        
        wizardDialog.setPreferredSize(new Dimension(400, 350));
        
        wizardDialog.display();
    }
}
