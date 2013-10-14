/*
 * Copyright (c) 2006-2013 DMDirc Developers
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

import com.dmdirc.commandparser.parsers.GlobalCommandParser;
import com.dmdirc.config.prefs.PreferencesSetting;
import com.dmdirc.config.prefs.PreferencesType;
import com.dmdirc.interfaces.ActionController;
import com.dmdirc.interfaces.actions.ActionType;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.interfaces.config.IdentityController;
import com.dmdirc.logger.ErrorManager;
import com.dmdirc.logger.Logger;
import com.dmdirc.util.io.ConfigFile;
import com.dmdirc.util.io.InvalidConfigFileException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Provider;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ActionTest {

    @Mock private Provider<GlobalCommandParser> gcpProvider;
    @Mock private ActionSubstitutorFactory substitutorFactory;
    @Mock private ActionController actionController;
    @Mock private ActionGroup actionGroup;
    @Mock private IdentityController identityController;
    @Mock private ConfigProvider configProvider;
    @Mock private AggregateConfigProvider aggregateConfigProvider;
    private Map<String, PreferencesSetting> prefs;
    private String testDirectory;

    @BeforeClass
    public static void stubLogger() {
        Logger.setErrorManager(mock(ErrorManager.class));
    }

    @Before
    public void makeTestDirectory() throws IOException {
        File tempFile = File.createTempFile("dmdirc", "test");
        tempFile.delete();
        tempFile.mkdir();
        testDirectory = tempFile.getAbsolutePath() + File.separator;
    }

    @Before
    public void setupActionController() {
        prefs = new HashMap<>();
        when(actionController.getOrCreateGroup(anyString())).thenReturn(actionGroup);
        when(actionGroup.getSettings()).thenReturn(prefs);
        when(actionController.getType("SERVER_AWAY")).thenReturn(CoreActionType.SERVER_AWAY);
        when(actionController.getComponent("STRING_LENGTH")).thenReturn(CoreActionComponent.STRING_LENGTH);
        when(actionController.getComparison("STRING_CONTAINS")).thenReturn(CoreActionComparison.STRING_CONTAINS);
        when(actionController.getComparison("INT_EQUALS")).thenReturn(CoreActionComparison.INT_EQUALS);
    }

    @Before
    public void setupIdentityController() {
        when(identityController.getGlobalConfiguration()).thenReturn(aggregateConfigProvider);
        when(identityController.getUserSettings()).thenReturn(configProvider);
    }

    @After
    public void killTestDirectory() {
        // TODO: This probably won't work - need to recursively delete files etc.
        new File(testDirectory).delete();
    }

    @Test
    public void testSave() {
        new Action(gcpProvider, substitutorFactory, actionController, identityController,
                testDirectory, "unit-test", "test1", new ActionType[0],
                new String[0], new ArrayList<ActionCondition>(),
                ConditionTree.createConjunction(0), null);
        assertTrue("Action constructor must create new file",
                new File(testDirectory + "unit-test"
                + File.separator + "test1").isFile());
    }

    @Test
    public void testSetGroup() {
        Action action = new Action(gcpProvider, substitutorFactory, actionController,
                identityController, testDirectory,
                "unit-test", "test1", new ActionType[0],
                new String[0], new ArrayList<ActionCondition>(),
                ConditionTree.createConjunction(0), null);
        action.setGroup("unit-test-two");

        assertFalse("setGroup must remove old file",
                new File(testDirectory + "unit-test" + File.separator + "test1").isFile());
        assertTrue("setGroup must create new file",
                new File(testDirectory + "unit-test-two" + File.separator + "test1").isFile());
    }

    @Test
    public void testSetName() {
        Action action = new Action(gcpProvider, substitutorFactory, actionController,
                identityController, testDirectory,
                "unit-test", "test1", new ActionType[0],
                new String[0], new ArrayList<ActionCondition>(),
                ConditionTree.createConjunction(0), null);
        action.setName("test2");

        assertFalse("setName must remove old file",
                new File(testDirectory + "unit-test" + File.separator + "test1").isFile());
        assertTrue("setName must create new file",
                new File(testDirectory + "unit-test" + File.separator + "test2").isFile());
    }

    @Test
    public void testDelete() {
        Action action = new Action(gcpProvider, substitutorFactory, actionController,
                identityController, testDirectory,
                "unit-test", "test1", new ActionType[0],
                new String[0], new ArrayList<ActionCondition>(),
                ConditionTree.createConjunction(0), null);
        action.delete();

        assertFalse("delete must remove file",
                new File(testDirectory + "unit-test"
                + File.separator + "test1").isFile());
    }

    @Test
    public void testRead() throws IOException, InvalidConfigFileException {
        Action action = new Action(gcpProvider, substitutorFactory, actionController,
                identityController, testDirectory,
                "unit-test", "doesn't_exist");
        action.config = new ConfigFile(getClass().getResourceAsStream("action1"));
        action.config.read();
        action.loadActionFromConfig();

        assertTrue(Arrays.equals(action.getTriggers(),
                new ActionType[]{CoreActionType.SERVER_AWAY}));
        assertEquals("(0&1)", action.getConditionTree().toString());
        assertTrue(Arrays.equals(action.getResponse(), new String[]{"/away"}));
        assertEquals(new ActionCondition(1, CoreActionComponent.STRING_LENGTH,
                CoreActionComparison.INT_EQUALS, "0"), action.getConditions().get(0));
        assertEquals(new ActionCondition("foo", CoreActionComparison.STRING_CONTAINS,
                "bar"), action.getConditions().get(1));
    }

    @Test
    public void testMultipleGroups() throws IOException, InvalidConfigFileException {
        Action action = new Action(gcpProvider, substitutorFactory, actionController,
                identityController, testDirectory,
                "unit-test", "doesn't_exist");
        action.config = new ConfigFile(getClass().getResourceAsStream("action_multisettings"));
        action.config.read();
        action.loadActionFromConfig();

        assertEquals(1, prefs.size());

        final PreferencesSetting setting = prefs.values().iterator().next();
        assertEquals(PreferencesType.TEXT, setting.getType());
        assertEquals("Highlight Regex", setting.getTitle());
        assertEquals("Regex to use to detect a highlight", setting.getHelptext());

        verify(actionController, atLeast(1)).registerSetting("highlightregex",
                "(?i).*(shane|dataforce|Q${SERVER_MYNICKNAME}E|(?<![#A-Z])DF).*");
    }

}