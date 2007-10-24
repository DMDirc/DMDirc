/*
 * License (BSD):
 * ==============
 *
 * Copyright (c) 2004, Mikael Grev, MiG InfoCom AB. (miglayout (at) miginfocom (dot) com)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution.
 * Neither the name of the MiG InfoCom AB nor the names of its contributors may be
 * used to endorse or promote products derived from this software without specific
 * prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 *
 * @version 1.0
 * @author Mikael Grev, MiG InfoCom AB
 *         Date: 2006-sep-08
 */

package net.miginfocom.layout;

import java.beans.Encoder;
import java.beans.Expression;
import java.beans.PersistenceDelegate;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public final class UnitValue implements Serializable {

    private static float dpiAdjFact = 1.0F;
    private static final HashMap<String, Integer> UNIT_MAP =
            new HashMap<String, Integer>(32);
    private static final ArrayList<UnitConverter> CONVERTERS =
            new ArrayList<UnitConverter>();
    /** An operation indicating a static value. */
    public static final int STATIC = 100;
    /** An operation indicating a addition of two sub units. */
    public static final int ADD = 101; // Must have "sub-unit values"
    /** An operation indicating a subtraction of two sub units */
    public static final int SUB = 102; // Must have "sub-unit values"
    /** An operation indicating a multiplication of two sub units.*/
    public static final int MUL = 103; // Must have "sub-unit values"
    /** An operation indicating a division of two sub units.*/
    public static final int DIV = 104; // Must have "sub-unit values"
    /** An operation indicating the minimum of two sub units. */
    public static final int MIN = 105; // Must have "sub-unit values"
    /** An operation indicating the maximum of two sub units. */
    public static final int MAX = 106; // Must have "sub-unit values"
    /** An operation indicating the middle value of two sub units. */
    public static final int MID = 107; // Must have "sub-unit values"
    /** A unit indicating pixels. */
    public static final int PIXEL = 0;
    /** A unit indicating logical horizontal pixels. */
    public static final int LPX = 1;
    /** A unit indicating logical vertical pixels. */
    public static final int LPY = 2;
    /** A unit indicating millimeters. */
    public static final int MM = 3;
    /** A unit indicating centimeters. */
    public static final int CM = 4;
    /** A unit indicating inches. */
    public static final int INCH = 5;
    /** A unit indicating percent. */
    public static final int PERCENT = 6;
    /** A unit indicating points. */
    public static final int PT = 7;
    /** A unit indicating screen width in pixels. */
    public static final int SPX = 8;
    /** A unit indicating screen height in pixels. */
    public static final int SPY = 9;
    /** A unit indicating alignment. */
    public static final int ALIGN = 12;
    /** A unit indicating minimum size. */
    public static final int MIN_SIZE = 13;
    /** A unit indicating preferred size. */
    public static final int PREF_SIZE = 14;
    /** A unit indicating maximum size. */
    public static final int MAX_SIZE = 15;
    /** A unit indicating botton size. */
    public static final int BUTTON = 16;
    /** A unit indicating linking to x. */
    public static final int LINK_X = 18; // First link
    /** A unit indicating linking to y. */
    public static final int LINK_Y = 19;
    /** A unit indicating linking to width. */
    public static final int LINK_W = 20;
    /** A unit indicating linking to height. */
    public static final int LINK_H = 21;
    /** A unit indicating linking to x2. */
    public static final int LINK_X2 = 22;
    /** A unit indicating linking to y2. */
    public static final int LINK_Y2 = 23;
    /** A unit indicating linking to x position on screen. */
    public static final int LINK_XPOS = 24;
    /** A unit indicating linking to y position on screen. */
    public static final int LINK_YPOS = 25; // Last link
    /** A unit indicating a lookup. */
    public static final int LOOKUP = 26;
    /** A unit indicating label alignment. */
    public static final int LABEL_ALIGN = 27;
    private static final int IDENTITY = -1;
    static {
        UNIT_MAP.put("px", new Integer(PIXEL));
        UNIT_MAP.put("lpx", new Integer(LPX));
        UNIT_MAP.put("lpy", new Integer(LPY));
        UNIT_MAP.put("%", new Integer(PERCENT));
        UNIT_MAP.put("cm", new Integer(CM));
        UNIT_MAP.put("in", new Integer(INCH));
        UNIT_MAP.put("spx", new Integer(SPX));
        UNIT_MAP.put("spy", new Integer(SPY));
        UNIT_MAP.put("al", new Integer(ALIGN));
        UNIT_MAP.put("mm", new Integer(MM));
        UNIT_MAP.put("pt", new Integer(PT));
        UNIT_MAP.put("min", new Integer(MIN_SIZE));
        UNIT_MAP.put("minimum", new Integer(MIN_SIZE));
        UNIT_MAP.put("p", new Integer(PREF_SIZE));
        UNIT_MAP.put("pref", new Integer(PREF_SIZE));
        UNIT_MAP.put("max", new Integer(MAX_SIZE));
        UNIT_MAP.put("maximum", new Integer(MAX_SIZE));
        UNIT_MAP.put("button", new Integer(BUTTON));
        UNIT_MAP.put("label", new Integer(LABEL_ALIGN));
    }
    static final UnitValue ZERO =
            new UnitValue(0, null, PIXEL, true, STATIC, null, null, "0px");
    static final UnitValue TOP =
            new UnitValue(0, null, PERCENT, false, STATIC, null, null, "top");
    static final UnitValue LEADING =
            new UnitValue(0, null, PERCENT, true, STATIC, null, null, "leading");
    static final UnitValue LEFT =
            new UnitValue(0, null, PERCENT, true, STATIC, null, null, "left");
    static final UnitValue CENTER =
            new UnitValue(50, null, PERCENT, true, STATIC, null, null, "center");
    static final UnitValue TRAILING =
            new UnitValue(100, null, PERCENT, true, STATIC, null, null,
            "trailing");
    static final UnitValue RIGHT =
            new UnitValue(100, null, PERCENT, true, STATIC, null, null, "right");
    static final UnitValue BOTTOM =
            new UnitValue(100, null, PERCENT, false, STATIC, null, null,
            "bottom");
    static final UnitValue LABEL =
            new UnitValue(0, null, LABEL_ALIGN, false, STATIC, null, null,
            "label");
    static final UnitValue INF =
            new UnitValue(LayoutUtil.INF, null, PIXEL, true, STATIC, null, null,
            "inf");
    static final UnitValue BASELINE_IDENTITY =
            new UnitValue(0, null, IDENTITY, false, STATIC, null, null,
            "baseline");
    private static int DEFAULT_UNIT = PIXEL;
    private final transient float value;
    private final transient int unit;
    private final transient int oper;
    private final transient String unitStr;
    private final transient boolean isHor;
    private final transient UnitValue[] subUnits;

    // Pixel
    public UnitValue(float value) {
        this(value, null, PIXEL, true, STATIC, null, null, value + "px");
    }

    public UnitValue(float value, int unit, String createString) {
        this(value, null, unit, true, STATIC, null, null, createString);
    }

    UnitValue(float value, String unitStr, boolean isHor, int oper,
            String createString) {
        this(value, unitStr, -1, isHor, oper, null, null, createString);
    }

    UnitValue(boolean isHor, int oper, UnitValue sub1,
            UnitValue sub2, String createString) {
        this(0, "", -1, isHor, oper, sub1, sub2, createString);
        if (sub1 == null || sub2 == null) {
            throw new IllegalArgumentException("Sub units is null!");
        }
    }

    private UnitValue(float value, String unitStr, int unit, boolean isHor,
            int oper, UnitValue sub1, UnitValue sub2,
            String createString) {
        if (oper < STATIC || oper > MID) {
            throw new IllegalArgumentException("Unknown Operation: " + oper);
        }
        if (oper >= ADD && oper <= MID && (sub1 == null || sub2 == null)) {
            throw new IllegalArgumentException(oper +
                    " Operation may not have null sub-UnitValues.");
        }
        this.value = value;
        this.oper = oper;
        this.isHor = isHor;
        this.unitStr = unitStr;
        this.unit = unitStr != null ? parseUnitString() : unit;
        this.subUnits =
                sub1 != null && sub2 != null
                ? new UnitValue[]{sub1, sub2} : null;

        LayoutUtil.putCCString(this, createString); // "this" escapes!! Safe though.
    }

    /** Returns the size in pixels rounded.
     * @param refValue The reference value. Normally the size of the parent. For unit {@link #ALIGN} the current size of the component should be sent in.
     * @param parent The parent. May be <code>null</code> for testing the validity of the value, but should normally not and are not
     * required to return any usable value if <code>null</code>.
     * @param comp The component, if any, that the value is for. Might be <code>null</code> if the value is not
     * connected to any component.
     * @return The size in pixels.
     */
    public final int getPixels(float refValue,
            ContainerWrapper parent, ComponentWrapper comp) {
        return Math.round(getPixelsExact(refValue, parent, comp));
    }

    /** Returns the size in pixels.
     * @param refValue The reference value. Normally the size of the parent. For unit {@link #ALIGN} the current size of the component should be sent in.
     * @param parent The parent. May be <code>null</code> for testing the validity of the value, but should normally not and are not
     * required to return any usable value if <code>null</code>.
     * @param comp The component, if any, that the value is for. Might be <code>null</code> if the value is not
     * connected to any component.
     * @return The size in pixels.
     */
    public final float getPixelsExact(float refValue,
            ContainerWrapper parent, ComponentWrapper comp) {
        if (parent == null) {
            return 1;
        }
        if (oper == STATIC) {
            switch (unit) {
                case PIXEL:
                    return value;
                case LPX:
                case LPY:
                    return parent.getPixelUnitFactor(unit == LPX) * value;
                case MM:
                    return dpiAdjFact *
                            (isHor ? parent.getHorizontalScreenDPI()
                            : parent.getVerticalScreenDPI()) * value / 25.4f;
                case CM:
                    return dpiAdjFact *
                            (isHor ? parent.getHorizontalScreenDPI()
                            : parent.getVerticalScreenDPI()) * value / 2.54f;
                case INCH:
                    return dpiAdjFact *
                            (isHor ? parent.getHorizontalScreenDPI()
                            : parent.getVerticalScreenDPI()) * value;
                case PERCENT:
                    return value * refValue * 0.01f;
                case PT:
                    return dpiAdjFact *
                            (isHor ? parent.getHorizontalScreenDPI()
                            : parent.getVerticalScreenDPI()) * value / 72f;
                case SPX:
                case SPY:
                    return (unit == SPX ? parent.getScreenWidth()
                            : parent.getScreenHeight()) * value * 0.01f;
                case ALIGN:
                    Integer st =
                            LinkHandler.getValue(parent.getLayout(), "visual",
                            isHor ? LinkHandler.X
                            : LinkHandler.Y);
                    Integer sz =
                            LinkHandler.getValue(parent.getLayout(), "visual",
                            isHor ? LinkHandler.WIDTH
                            : LinkHandler.HEIGHT);
                    if (st == null || sz == null) {
                        return 0;
                    }
                    return value *
                            (Math.max(0, sz.intValue()) - refValue) +
                            st.intValue();
                case MIN_SIZE:
                    if (comp == null) {
                        return 0;
                    }
                    return isHor ? comp.getMinimumWidth()
                            : comp.getMinimumHeight();
                case PREF_SIZE:
                    if (comp == null) {
                        return 0;
                    }
                    return isHor ? comp.getPreferredWidth()
                            : comp.getPreferredHeight();
                case MAX_SIZE:
                    if (comp == null) {
                        return 0;
                    }
                    return isHor ? comp.getMaximumWidth()
                            : comp.getMaximumHeight();
                case BUTTON:
                    return PlatformDefaults.getMinimumButtonWidth().
                            getPixels(refValue, parent, comp);
                case LINK_X:
                case LINK_Y:
                case LINK_W:
                case LINK_H:
                case LINK_X2:
                case LINK_Y2:
                case LINK_XPOS:
                case LINK_YPOS:
                    Integer v =
                            LinkHandler.getValue(parent.getLayout(),
                            getLinkTargetId(),
                            unit - (unit >= LINK_XPOS ? LINK_XPOS : LINK_X));
                    if (v == null) {
                        return 0;
                    }
                    if (unit == LINK_XPOS) {
                        return parent.getScreenLocationX() + v.intValue();
                    }
                    if (unit == LINK_YPOS) {
                        return parent.getScreenLocationY() + v.intValue();
                    }
                    return v.intValue();
                case LOOKUP:
                    float res = lookup(refValue, parent, comp);
                    if (res != UnitConverter.UNABLE) {
                        return res;
                    }
                    break;
                case LABEL_ALIGN:
                    return PlatformDefaults.getLabelAlignPercentage() * refValue;
                case IDENTITY:
            }
            throw new IllegalArgumentException("Unknown/illegal unit: " + unit +
                    ", unitStr: " + unitStr);
        }

        if (subUnits != null && subUnits.length == 2) {
            float r1 = subUnits[0].getPixelsExact(refValue, parent, comp);
            float r2 = subUnits[1].getPixelsExact(refValue, parent, comp);
            switch (oper) {
                case ADD:
                    return r1 + r2;
                case SUB:
                    return r1 - r2;
                case MUL:
                    return r1 * r2;
                case DIV:
                    return r1 / r2;
                case MIN:
                    return r1 < r2 ? r1 : r2;
                case MAX:
                    return r1 > r2 ? r1 : r2;
                case MID:
                    return (r1 + r2) * 0.5f;
            }
        }

        throw new IllegalArgumentException("Internal: Unknown Oper: " + oper);
    }

    private final float lookup(float refValue,
            ContainerWrapper parent, ComponentWrapper comp) {
        float res = UnitConverter.UNABLE;
        for (int i = CONVERTERS.size() - 1; i >= 0;
                i--) {
            res =   CONVERTERS.get(i).
                    convertToPixels(value, unitStr, isHor, refValue, parent,
                    comp);
            if (res != UnitConverter.UNABLE) {
                return res;
            }
        }
        return PlatformDefaults.convertToPixels(value, unitStr, isHor, refValue,
                parent, comp);
    }

    private final int parseUnitString() {
        int len = unitStr.length();
        if (len == 0) {
            return DEFAULT_UNIT;
        }
        Integer u = UNIT_MAP.get(unitStr);
        if (u != null) {
            return u.intValue();
        }
        if (unitStr.equals("lp")) {
            return isHor ? LPX : LPY;
        }
        if (unitStr.equals("sp")) {
            return isHor ? SPX : SPY;
        }
        if (lookup(0, null, null) != UnitConverter.UNABLE) {
            // To test so we can fail fast
            return LOOKUP;
        }
        if (unitStr.endsWith(".x")) {
            return LINK_X;
        }
        if (unitStr.endsWith(".y")) {
            return LINK_Y;
        }
        if (unitStr.endsWith(".w")) {
            return LINK_W;
        }
        if (unitStr.endsWith(".h")) {
            return LINK_H;
        }
        if (unitStr.endsWith(".x2")) {
            return LINK_X2;
        }
        if (unitStr.endsWith(".y2")) {
            return LINK_Y2;
        }
        if (unitStr.endsWith(".xpos")) {
            return LINK_XPOS;
        }
        if (unitStr.endsWith(".ypos")) {
            return LINK_YPOS;
        }
        throw new IllegalArgumentException("Unknown keyword: " + unitStr);
    }

    final boolean isLinked() {
        return unit >= LINK_X && unit <= LINK_YPOS;
    }

    final boolean isLinkedDeep() {
        if (subUnits == null) {
            return isLinked();
        }
        for (int i = 0; i < subUnits.length;
                i++) {
            if (subUnits[i].isLinkedDeep()) {
                return true;
            }
        }

        return false;
    }

    final String getLinkTargetId() {
        return unitStr.substring(0,
                unitStr.length() -
                (unit < LINK_X2 ? 2 : (unit > LINK_Y2 ? 5 : 3)));
    }

    final UnitValue getSubUnitValue(int i) {
        return subUnits[i];
    }

    final int getSubUnitCount() {
        return subUnits != null ? subUnits.length : 0;
    }

    public final UnitValue[] getSubUnits() {
        return subUnits;
    }

    public final int getUnit() {
        return unit;
    }

    public final String getUnitString() {
        return unitStr;
    }

    public final int getOperation() {
        return oper;
    }

    public final float getValue() {
        return value;
    }

    public final boolean isHorizontal() {
        return isHor;
    }

    public final String toString() {
        return getClass().getName() + ". Value=" + value + ", unit=" + unit +
                ", unitString: " + unitStr + ", oper=" + oper + ", isHor: " +
                isHor;
    }

    /** Returns the creation string for this object. Note that {@link LayoutUtil#setDesignTime(ContainerWrapper, boolean)} must be
     * set to <code>true</code> for the creation strings to be stored.
     * @return The constraint string or <code>null</code> if none is registered.
     */
    public final String getConstraintString() {
        return LayoutUtil.getCCString(this);
    }

    public final int hashCode() {
        return (int) (value * 12345) + (oper >>> 5) + unit >>> 17;
    }

    /** Adds a global unit converter that can convert from some <code>unit</code> to pixels.
     * <p>
     * This converter will be asked before the platform converter so the values for it (e.g. "related" and "unrelated")
     * can be overridden. It is however not possible to override the built in ones (e.g. "mm", "pixel" or "lp").
     * @param conv The converter. Not <code>null</code>.
     */
    public static final synchronized void addGlobalUnitConverter(UnitConverter conv) {
        if (conv == null) {
            throw new NullPointerException();
        }
        CONVERTERS.add(conv);
    }

    /** Removed the converter.
     * @param unit The converter.
     * @return If there was a converter found and thus removed.
     */
    public static final synchronized boolean removeGlobalUnitConverter(String unit) {
        return CONVERTERS.remove(unit);
    }

    /** Returns the global converters currently registered. The platform converter will not be in this list.
     * @return The converters. Never <code>null</code>.
     */
    public static final synchronized UnitConverter[] getGlobalUnitConverters() {
        return CONVERTERS.toArray(new UnitConverter[CONVERTERS.size()]);
    }

    /** Returns the DPI (dots per inch) factor.
     * @return The DPI (dots per inch) factor. Default is 1.0f.
     * @see #setScreenDPIAdjustmentFactor(float).
     */
    public static final float getScreenDPIAdjustmentFactor() {
        return dpiAdjFact;
    }

    /** Sets a number that will be multiplied to all units of types "cm" (Centimeter), "mm" (millimeter), "dot" and "inch". This
     * is for compensating for screens that have the wrong actualy dpi (dots-per-inch) set in the operating system, as for instance the
     * laptop screens with a resolution on 1920x1200. They normally get 96 dpi in Windows, even though they are closer
     * to 120dpi.
     * @param f Will be multipled to the DPI, so somewhere around 1.0f.
     */
    public static final void setScreenDPIAdjustmentFactor(float f) {
        dpiAdjFact = f;
    }

    /** Returns the current default unit. The default unit is the unit used if no unit is set. E.g. "width 10".
     * @return The current default unit.
     */
    public static final int getDefaultUnit() {
        return DEFAULT_UNIT;
    }

    /** Sets the default unit. The default unit is the unit used if no unit is set. E.g. "width 10".
     * @param unit The new default unit.
     */
    public static final void setDefaultUnit(int unit) {
        if (unit < PIXEL || unit > LABEL_ALIGN) {
            throw new IllegalArgumentException("Illegal Unit: " + unit);
        }
        DEFAULT_UNIT = unit;
    }
    static {
        LayoutUtil.setDelegate(UnitValue.class,
                new PersistenceDelegate() {

            protected Expression instantiate(Object oldInstance,
                    Encoder out) {
                UnitValue uv = (UnitValue) oldInstance;
                String cs = uv.getConstraintString();
                if (cs == null) {
                    throw new IllegalStateException("Design time must be on to use XML persistence. See LayoutUtil.");
                }
                return new Expression(oldInstance,
                        ConstraintParser.class, "parseUnitValueOrAlign",
                        new Object[]{uv.getConstraintString(),
                        uv.isHorizontal() ? Boolean.TRUE : Boolean.FALSE, null});
            }
        });
    }
    // ************************************************
    // Persistence Delegate and Serializable combined.
    // ************************************************
    private static final long serialVersionUID = 1L;

    private Object readResolve() throws ObjectStreamException {
        return LayoutUtil.getSerializedObject(this);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        if (getClass() == UnitValue.class) {
            LayoutUtil.writeAsXML(out, this);
        }
    }

    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        LayoutUtil.setSerializedObject(this,
                LayoutUtil.readAsXML(in));
    }
}