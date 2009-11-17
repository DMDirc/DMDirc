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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.UIManager;
import javax.swing.border.Border;

/**
 * Shows the ui defaults
 */
public class UIDefaults {

    public static void main(String[] argv) {
        javax.swing.UIDefaults uidefs = UIManager.getLookAndFeelDefaults();
        for (Map.Entry<Object, Object> entry : uidefs.entrySet()) {
            Object v = entry.getValue();
            CharSequence k = (CharSequence) entry.getKey();
            if (!k.toString().startsWith("Label")) {
                continue;
            }
            if (v instanceof Integer) {
                int intVal = uidefs.getInt(k);
                System.out.println(k + " = " + intVal);
            } else if (v instanceof Boolean) {
                boolean boolVal = uidefs.getBoolean(k);
                System.out.println(k + " = " + boolVal);
            } else if (v instanceof String) {
                String strVal = uidefs.getString(k);
                System.out.println(k + " = " + strVal);
            } else if (v instanceof Dimension) {
                Dimension dimVal = uidefs.getDimension(k);
                System.out.println(k + " = " + dimVal);
            } else if (v instanceof Insets) {
                Insets insetsVal = uidefs.getInsets(k);
                System.out.println(k + " = " + insetsVal);
            } else if (v instanceof Color) {
                Color colorVal = uidefs.getColor(k);
                System.out.println(k + " = " + colorVal);
            } else if (v instanceof Font) {
                Font fontVal = uidefs.getFont(k);
                System.out.println(k + " = " + fontVal);
            } else if (v instanceof Border) {
                Border borderVal = uidefs.getBorder(k);
                System.out.println(k + " = " + borderVal);
            } else if (v instanceof Icon) {
                Icon iconVal = uidefs.getIcon(k);
                System.out.println(k + " = " + iconVal);
            } else if (v instanceof javax.swing.text.JTextComponent.KeyBinding[]) {
                javax.swing.text.JTextComponent.KeyBinding[] keyBindsVal =
                        (javax.swing.text.JTextComponent.KeyBinding[]) uidefs.
                        get(k);
                System.out.println(k + " = " + keyBindsVal);
            } else if (v instanceof InputMap) {
                InputMap imapVal = (InputMap) uidefs.get(k);
                System.out.println(k + " = " + imapVal);
            } else {
                System.out.println("Unknown type: " + v);
            }
        }
    }

    private UIDefaults() {
    }
}
