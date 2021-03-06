/*
 * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.osd;

import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.util.ReturnableThread;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Class to manage OSD Windows.
 *
 * @author Simon
 * @since 0.6.3
 */
public class OsdManager {

    /** The Plugin that owns this OSD Manager. */
    private final OsdPlugin plugin;
    /** List of OSD Windows. */
    private final List<OsdWindow> windowList = new ArrayList<OsdWindow>();
    /** List of messages to be queued. */
    private final Queue<QueuedMessage> windowQueue = new LinkedList<QueuedMessage>();

    /**
     * Create a new OSD Manager.
     *
     * @param plugin The plugin that owns this OSD Manager.
     */
    public OsdManager(final OsdPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Add messages to the queue and call displayWindows.
     *
     * @param message Message to be displayed.
     */
    public void showWindow(final int timeout, final String message) {
        windowQueue.add(new QueuedMessage(timeout, message));
        displayWindows();
    }

    /**
     * Displays as many windows as appropriate.
     */
    private synchronized void displayWindows() {
        final Integer maxWindows = IdentityManager.getGlobalConfig().
                getOptionInt(plugin.getDomain(), "maxWindows", false);

        QueuedMessage nextItem;

        while ((maxWindows == null || getWindowCount() < maxWindows)
                && (nextItem = windowQueue.poll()) != null) {
            displayWindow(nextItem.getTimeout(), nextItem.getMessage());
        }
    }

    /**
     * Create a new OSD window with "message".
     * <p>
     * This method needs to be synchronised to ensure that the window list is
     * not modified in between the invocation of
     * {@link OsdPolicy#getYPosition(com.dmdirc.addons.osd.OsdManager, int)}
     * and the point at which the {@link OsdWindow} is added to the windowList.
     *
     * @see OsdPolicy#getYPosition(com.dmdirc.addons.osd.OsdManager, int)
     * @param message Text to display in the OSD window.
     */
    private synchronized void displayWindow(final int timeout, final String message) {
        final OsdPolicy policy = OsdPolicy.valueOf(IdentityManager.
                getGlobalConfig().getOption(plugin.getDomain(), "newbehaviour").
                toUpperCase());
        final int startY = IdentityManager.getGlobalConfig().getOptionInt(
                plugin.getDomain(), "locationY");

        windowList.add(UIUtilities.invokeAndWait(
                new ReturnableThread<OsdWindow>() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                setObject(new OsdWindow(timeout, message, false,
                        IdentityManager.getGlobalConfig().getOptionInt(
                        plugin.getDomain(), "locationX"), policy.getYPosition(
                        OsdManager.this, startY), plugin, OsdManager.this));
            }
        }));
    }

    /**
     * Destroy the given OSD Window and check if the Queue has items, if so
     * Display them.
     *
     * @param window The window that we are destroying.
     */
    public synchronized void closeWindow(final OsdWindow window) {
        final OsdPolicy policy = OsdPolicy.valueOf(IdentityManager.
                getGlobalConfig().getOption(plugin.getDomain(), "newbehaviour").
                toUpperCase());

        int oldY = window.getDesiredY();
        final int closedIndex = windowList.indexOf(window);

        if (closedIndex == -1) {
            return;
        }

        windowList.remove(window);

        UIUtilities.invokeLater(new Runnable() {
            /** {@inheritDoc} */
            @Override
            public void run() {
                window.dispose();
            }
        });

        final List<OsdWindow> newList = getWindowList();
        for (OsdWindow otherWindow : newList.subList(closedIndex, newList.size())) {
            final int currentY = otherWindow.getDesiredY();
            if (policy.changesPosition()) {
                otherWindow.setDesiredLocation(otherWindow.getDesiredX(), oldY);
                oldY = currentY;
            }
        }
        displayWindows();
    }

    /**
     * Destroy all OSD Windows.
     */
    public void closeAll() {
        for (OsdWindow window : getWindowList()) {
            closeWindow(window);
        }
    }

    /**
     * Get the list of current OSDWindows.
     *
     * @return a List of all currently open OSDWindows.
     */
    public List<OsdWindow> getWindowList() {
        return new ArrayList<OsdWindow>(windowList);
    }

    /**
     * Get the count of open windows.
     *
     * @return Current number of OSD Windows open.
     */
    public int getWindowCount() {
        return windowList.size();
    }

    /**
     * Return the current plugin.
     *
     * @return Returns current plugin instance.
     */
    public OsdPlugin getPlugin() {
        return plugin;
    }
}
