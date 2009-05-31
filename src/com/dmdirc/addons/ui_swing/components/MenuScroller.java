/**
 * @(#)MenuScroller.java	1.3.0 05/04/09
 */
package com.dmdirc.addons.ui_swing.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.MenuSelectionManager;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 * A class that provides scrolling capabilities to a long menu dropdown or
 * popup menu.  A number of items can optionally be frozen at the top and/or
 * bottom of the menu.
 * <P>
 * <B>Implementation note:</B>  The default number of items to display
 * at a time is 15, and the default scrolling interval is 125 milliseconds.
 * <P>
 * @author Darryl
 */
public class MenuScroller {

   private JMenu menu;
   private JPopupMenu popupMenu;
   private Component[] menuItems;
   private MenuScrollItem upItem;
   private MenuScrollItem downItem;
   private final MenuScrollListener menuListener = new MenuScrollListener();
   private int scrollCount;
   private int interval;
   private int topFixedCount;
   private int bottomFixedCount;
   private int firstIndex = 0;

   /**
    * Registers a menu to be scrolled with the default number of items to
    * display at a time and the default scrolling interval.
    * <P>
    * @param menu the menu
    * @return the MenuScroller
    */
   public static MenuScroller setScrollerFor(JMenu menu) {
      return new MenuScroller(menu);
   }

   /**
    * Registers a popup menu to be scrolled with the default number of items to
    * display at a time and the default scrolling interval.
    * <P>
    * @param menu the popup menu
    * @return the MenuScroller
    */
   public static MenuScroller setScrollerFor(JPopupMenu menu) {
      return new MenuScroller(menu);
   }

   /**
    * Registers a menu to be scrolled with the default number of items to
    * display at a time and the specified scrolling interval.
    * <P>
    * @param menu the menu
    * @param scrollCount the number of items to display at a time
    * @return the MenuScroller
    * @throws IllegalArgumentException if scrollCount is 0 or negative
    */
   public static MenuScroller setScrollerFor(JMenu menu, int scrollCount) {
      return new MenuScroller(menu, scrollCount);
   }

   /**
    * Registers a popup menu to be scrolled with the default number of items to
    * display at a time and the specified scrolling interval.
    * <P>
    * @param menu the popup menu
    * @param scrollCount the number of items to display at a time
    * @return the MenuScroller
    * @throws IllegalArgumentException if scrollCount is 0 or negative
    */
   public static MenuScroller setScrollerFor(JPopupMenu menu, int scrollCount) {
      return new MenuScroller(menu, scrollCount);
   }

   /**
    * Registers a menu to be scrolled, with the specified number of items to
    * display at a time and the specified scrolling interval.
    * <P>
    * @param menu the menu
    * @param scrollCount the number of items to be displayed at a time
    * @param interval the scroll interval, in milliseconds
    * @return the MenuScroller
    * @throws IllegalArgumentException if scrollCount or interval is 0 or negative
    */
   public static MenuScroller setScrollerFor(JMenu menu, int scrollCount,
         int interval) {
      return new MenuScroller(menu, scrollCount, interval);
   }

   /**
    * Registers a popup menu to be scrolled, with the specified number of items to
    * display at a time and the specified scrolling interval.
    * <P>
    * @param menu the popup menu
    * @param scrollCount the number of items to be displayed at a time
    * @param interval the scroll interval, in milliseconds
    * @return the MenuScroller
    * @throws IllegalArgumentException if scrollCount or interval is 0 or negative
    */
   public static MenuScroller setScrollerFor(JPopupMenu menu, int scrollCount,
         int interval) {
      return new MenuScroller(menu, scrollCount, interval);
   }

   /**
    * Registers a menu to be scrolled, with the specified number of items
    * to display in the scrolling region, the specified scrolling interval,
    * and the specified numbers of items fixed at the top and bottom of the
    * menu.
    * <P>
    * @param menu the menu
    * @param scrollCount the number of items to display in the scrolling portion
    * @param interval the scroll interval, in milliseconds
    * @param topFixedCount the number of items to fix at the top.  May be 0.
    * @param bottomFixedCount the number of items to fix at the bottom. May be 0
    * @throws IllegalArgumentException if scrollCount or interval is 0 or
    * negative or if topFixedCount or bottomFixedCount is negative
    * @return the MenuScroller
    */
   public static MenuScroller setScrollerFor(JMenu menu, int scrollCount,
         int interval, int topFixedCount, int bottomFixedCount) {
      return new MenuScroller(menu, scrollCount, interval,
            topFixedCount, bottomFixedCount);
   }

   /**
    * Registers a popup menu to be scrolled, with the specified number of items
    * to display in the scrolling region, the specified scrolling interval,
    * and the specified numbers of items fixed at the top and bottom of the
    * popup menu.
    * <P>
    * @param menu the popup menu
    * @param scrollCount the number of items to display in the scrolling portion
    * @param interval the scroll interval, in milliseconds
    * @param topFixedCount the number of items to fix at the top.  May be 0
    * @param bottomFixedCount the number of items to fix at the bottom.  May be 0
    * @throws IllegalArgumentException if scrollCount or interval is 0 or
    * negative or if topFixedCount or bottomFixedCount is negative
    * @return the MenuScroller
    */
   public static MenuScroller setScrollerFor(JPopupMenu menu, int scrollCount,
         int interval, int topFixedCount, int bottomFixedCount) {
      return new MenuScroller(menu, scrollCount, interval,
            topFixedCount, bottomFixedCount);
   }

   /**
    * Constructs a <code>MenuScroller</code> that scrolls a menu with the
    * default number of items to display at a time, and default scrolling
    * interval.
    * <P>
    * @param menu the menu
    */
   public MenuScroller(JMenu menu) {
      this(menu, 15);
   }

   /**
    * Constructs a <code>MenuScroller</code> that scrolls a popup menu with the
    * default number of items to display at a time, and default scrolling
    * interval.
    * <P>
    * @param menu the popup menu
    */
   public MenuScroller(JPopupMenu menu) {
      this(menu, 15);
   }

   /**
    * Constructs a <code>MenuScroller</code> that scrolls a menu with the
    * specified number of items to display at a time, and default scrolling
    * interval.
    * <P>
    * @param menu the menu
    * @param scrollCount the number of items to display at a time
    * @throws IllegalArgumentException if scrollCount is 0 or negative
    */
   public MenuScroller(JMenu menu, int scrollCount) {
      this(menu, scrollCount, 125);
   }

   /**
    * Constructs a <code>MenuScroller</code> that scrolls a popup menu with the
    * specified number of items to display at a time, and default scrolling
    * interval.
    * <P>
    * @param menu the popup menu
    * @param scrollCount the number of items to display at a time
    * @throws IllegalArgumentException if scrollCount is 0 or negative
    */
   public MenuScroller(JPopupMenu menu, int scrollCount) {
      this(menu, scrollCount, 125);
   }

   /**
    * Constructs a <code>MenuScroller</code> that scrolls a menu with the
    * specified number of items to display at a time, and specified scrolling
    * interval.
    * <P>
    * @param menu the menu
    * @param scrollCount the number of items to display at a time
    * @param interval the scroll interval, in milliseconds
    * @throws IllegalArgumentException if scrollCount or interval is 0 or negative
    */
   public MenuScroller(JMenu menu, int scrollCount, int interval) {
      this(menu, scrollCount, interval, 0, 0);
   }

   /**
    * Constructs a <code>MenuScroller</code> that scrolls a popup menu with the
    * specified number of items to display at a time, and specified scrolling
    * interval.
    * <P>
    * @param menu the popup menu
    * @param scrollCount the number of items to display at a time
    * @param interval the scroll interval, in milliseconds
    * @throws IllegalArgumentException if scrollCount or interval is 0 or negative
    */
   public MenuScroller(JPopupMenu menu, int scrollCount, int interval) {
      this(menu, scrollCount, interval, 0, 0);
   }

   /**
    * Constructs a <code>MenuScroller</code> that scrolls a menu with the
    * specified number of items to display in the scrolling region, the
    * specified scrolling interval, and the specified numbers of items fixed at
    * the top and bottom of the menu.
    * <P>
    * @param menu the menu
    * @param scrollCount the number of items to display in the scrolling portion
    * @param interval the scroll interval, in milliseconds
    * @param topFixedCount the number of items to fix at the top.  May be 0
    * @param bottomFixedCount the number of items to fix at the bottom.  May be 0
    * @throws IllegalArgumentException if scrollCount or interval is 0 or
    * negative or if topFixedCount or bottomFixedCount is negative
    */
   public MenuScroller(JMenu menu, int scrollCount, int interval,
         int topFixedCount, int bottomFixedCount) {
      setValues(scrollCount, interval, topFixedCount, bottomFixedCount);

      this.menu = menu;
      menu.addMenuListener(menuListener);
   }

   /**
    * Constructs a <code>MenuScroller</code> that scrolls a popup menu with the
    * specified number of items to display in the scrolling region, the
    * specified scrolling interval, and the specified numbers of items fixed at
    * the top and bottom of the popup menu.
    * <P>
    * @param menu the popup menu
    * @param scrollCount the number of items to display in the scrolling portion
    * @param interval the scroll interval, in milliseconds
    * @param topFixedCount the number of items to fix at the top.  May be 0
    * @param bottomFixedCount the number of items to fix at the bottom.  May be 0
    * @throws IllegalArgumentException if scrollCount or interval is 0 or
    * negative or if topFixedCount or bottomFixedCount is negative
    */
   public MenuScroller(JPopupMenu menu, int scrollCount, int interval,
         int topFixedCount, int bottomFixedCount) {
      setValues(scrollCount, interval, topFixedCount, bottomFixedCount);
      this.popupMenu = menu;
      menu.addPopupMenuListener(menuListener);
   }

   private void setValues(int scrollCount, int interval,
         int topFixedCount, int bottomFixedCount) {
      if (scrollCount <= 0 || interval <= 0) {
         throw new IllegalArgumentException(
               "scrollCount and interval must be greater than 0");
      }

      if (topFixedCount < 0 || bottomFixedCount < 0) {
         throw new IllegalArgumentException(
               "topFixedCount and bottomFixedCount cannot be negative");
      }
      upItem = new MenuScrollItem(MenuIcon.UP, -1);
      downItem = new MenuScrollItem(MenuIcon.DOWN, +1);
      setScrollCount(scrollCount);
      setInterval(interval);
      setTopFixedCount(topFixedCount);
      setBottomFixedCount(bottomFixedCount);
   }

   /**
    * Returns the scroll interval in milliseconds
    * <P>
    * @return the scroll interval in milliseconds
    */
   public int getInterval() {
      return interval;
   }

   /**
    * Sets the scroll interval in milliseconds
    * <P>
    * @param interval the scroll interval in milliseconds
    * @throws IllegalArgumentException if interval is 0 or negative
    */
   public void setInterval(int interval) {
      if (interval <= 0) {
         throw new IllegalArgumentException(
               "interval must be greater than 0");
      }
      upItem.setInterval(interval);
      downItem.setInterval(interval);
      this.interval = interval;
   }

   /**
    * Returns the number of items in the scrolling portion of the menu.
    * <P>
    * @return the number of items to display at a time
    */
   public int getscrollCount() {
      return scrollCount;
   }

   /**
    * Sets the number of items in the scrolling portion of the menu.
    * <P>
    * @param scrollCount the number of items to display at a time
    * @throws IllegalArgumentException if scrollCount is 0 or negative
    */
   public void setScrollCount(int scrollCount) {
      if (scrollCount <= 0) {
         throw new IllegalArgumentException(
               "scrollCount must be greater than 0");
      }
      this.scrollCount = scrollCount;
      if (menu != null) {
         menu.doClick();
      }
      MenuSelectionManager.defaultManager().clearSelectedPath();
   }

   /**
    * Returns the number of items fixed at the top of the menu or popup menu.
    * <P>
    * @return the number of items
    */
   public int getTopFixedCount() {
      return topFixedCount;
   }

   /**
    * Sets the number of items to fix at the top of the menu or popup menu.
    * <P>
    * @param topFixedCount the number of items
    */
   public void setTopFixedCount(int topFixedCount) {
      firstIndex = Math.max(firstIndex, topFixedCount);
      this.topFixedCount = topFixedCount;
   }

   /**
    * Returns the number of items fixed at the bottom of the menu or popup menu.
    * <P>
    * @return the number of items
    */
   public int getBottomFixedCount() {
      return bottomFixedCount;
   }

   /**
    * Sets the number of items to fix at the bottom of the menu or popup menu.
    * <P>
    * @param bottomFixedCount the number of items
    */
   public void setBottomFixedCount(int bottomFixedCount) {
      this.bottomFixedCount = bottomFixedCount;
   }

   /**
    * Removes this MenuScroller from the associated menu and restores the
    * default behavior of the menu.
    */
   public void dispose() {
      if (menu != null) {
         menu.removeMenuListener(menuListener);
         menu = null;
      }

      if (popupMenu != null) {
         popupMenu.removePopupMenuListener(menuListener);
         popupMenu = null;
      }
   }

   /**
    * Ensures that the <code>dispose</code> method of this MenuScroller is
    * called when there are no more refrences to it.
    * <P>
    * @exception  Throwable if an error occurs.
    * @see MenuScroller#dispose()
    */
   @Override
   public void finalize() throws Throwable {
      dispose();
   }

   private void setMenuItems() {
      if (menu != null) {
         menuItems = menu.getMenuComponents();
      }
      if (popupMenu != null) {
         menuItems = popupMenu.getComponents();
      }
      if (menuItems.length > topFixedCount + scrollCount + bottomFixedCount) {
         refreshMenu();
      }
   }

   private void restoreMenuItems() {
      JComponent container = menu == null ? popupMenu : menu;
      container.removeAll();
      for (Component component : menuItems) {
         container.add(component);
      }
   }

   private void refreshMenu() {
      firstIndex = Math.max(topFixedCount,firstIndex);
      firstIndex = Math.min(menuItems.length - bottomFixedCount - scrollCount,
            firstIndex);

      upItem.setEnabled(firstIndex > topFixedCount);
      downItem.setEnabled(
            firstIndex + scrollCount < menuItems.length - bottomFixedCount);

      JComponent container = menu == null ? popupMenu : menu;
      container.removeAll();
      for (int i = 0; i < topFixedCount; i++) {
         container.add(menuItems[i]);
      }
      if (topFixedCount > 0) {
         container.add(new JSeparator());
      }

      container.add(upItem);
      for (int i = firstIndex; i < scrollCount + firstIndex; i++) {
         container.add(menuItems[i]);
      }
      container.add(downItem);

      if (bottomFixedCount > 0) {
         container.add(new JSeparator());
      }
      for (int i = menuItems.length - bottomFixedCount; i < menuItems.length; i++) {
         container.add(menuItems[i]);
      }
      upItem.getParent().validate();
   }

   private class MenuScrollItem extends JMenuItem
         implements MouseListener, ChangeListener {

      private MenuScrollTimer timer;
      private MouseListener[] mouseListeners;

      public MenuScrollItem(MenuIcon icon, int increment) {
         setIcon(icon);
         setDisabledIcon(icon);
         timer = new MenuScrollTimer(increment, interval);
         mouseListeners = getMouseListeners();
         addMouseListener(this);
         addChangeListener(this);
      }

      public void setInterval(int interval) {
         timer.setDelay(interval);
      }

      // MouseListener methods
      public void mouseClicked(MouseEvent e) {
      }

      public void mousePressed(MouseEvent e) {
      }

      public void mouseReleased(MouseEvent e) {
      }

      public void mouseEntered(MouseEvent e) {
         for (MouseListener mouseListener : mouseListeners) {
            mouseListener.mouseEntered(e);
         }
      }

      public void mouseExited(MouseEvent e) {
         for (MouseListener mouseListener : mouseListeners) {
            mouseListener.mouseExited(e);
         }
      }

      // ChangeListener method
      public void stateChanged(ChangeEvent e) {
         if (isArmed() && !timer.isRunning()) {
            timer.start();
         }
         if (!isArmed() && timer.isRunning()) {
            timer.stop();
         }
      }
   }

   private class MenuScrollTimer extends Timer {

      public MenuScrollTimer(final int increment, int interval) {
         super(interval, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
               firstIndex += increment;
               refreshMenu();
            }
         });
      }
   }

   private class MenuScrollListener
         implements MenuListener, PopupMenuListener {

      // MenuListener methods
      public void menuSelected(MenuEvent e) {
         setMenuItems();
      }

      public void menuDeselected(MenuEvent e) {
         restoreMenuItems();
      }

      public void menuCanceled(MenuEvent e) {
         restoreMenuItems();
      }

      // PopupMenuListener methods
      public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
         setMenuItems();
      }

      public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
         restoreMenuItems();
      }

      public void popupMenuCanceled(PopupMenuEvent e) {
         restoreMenuItems();
      }
   }

   private static enum MenuIcon implements Icon {

      UP(9, 1, 9),
      DOWN(1, 9, 1);
      final int[] xPoints = {1, 5, 9};
      final int[] yPoints;

      MenuIcon(int... yPoints) {
         this.yPoints = yPoints;
      }

      public void paintIcon(Component c, Graphics g, int x, int y) {
         Dimension size = c.getSize();
         Graphics g2 = g.create(size.width / 2 - 5, size.height / 2 - 5, 10, 10);
         g2.setColor(Color.GRAY);
         g2.drawPolygon(xPoints, yPoints, 3);
         if (c.isEnabled()) {
            g2.setColor(Color.BLACK);
            g2.fillPolygon(xPoints, yPoints, 3);
         }
         g2.dispose();
      }

      public int getIconWidth() {
         return 0;
      }

      public int getIconHeight() {
         return 10;
      }
   }
}