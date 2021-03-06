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

package com.dmdirc.addons.ui_swing;

import com.dmdirc.FrameContainer;
import com.dmdirc.addons.ui_swing.components.frames.ChannelFrame;
import com.dmdirc.addons.ui_swing.components.frames.CustomFrame;
import com.dmdirc.addons.ui_swing.components.frames.CustomInputFrame;
import com.dmdirc.addons.ui_swing.components.frames.ServerFrame;
import com.dmdirc.addons.ui_swing.components.frames.TextFrame;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.core.components.WindowComponent;
import com.dmdirc.ui.interfaces.FrameListener;
import com.dmdirc.ui.interfaces.Window;
import com.dmdirc.util.ListenerList;
import java.util.Arrays;
import java.util.Collection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Handles creation of windows in the Swing UI.
 *
 * @since 0.6.4
 */
public class SwingWindowFactory implements FrameListener {

    /** A map of known implementations of window interfaces. */
    private static final Map<Collection<String>, Class<? extends Window>> IMPLEMENTATIONS
            = new HashMap<Collection<String>, Class<? extends Window>>();

    /** A map of frame containers to their Swing windows. */
    private final Map<FrameContainer, TextFrame> windows = new HashMap<FrameContainer, TextFrame>();

    static {
        IMPLEMENTATIONS.put(new HashSet<String>(
                Arrays.asList(WindowComponent.TEXTAREA.getIdentifier())),
                CustomFrame.class);
        IMPLEMENTATIONS.put(new HashSet<String>(
                Arrays.asList(WindowComponent.TEXTAREA.getIdentifier(),
                WindowComponent.INPUTFIELD.getIdentifier())),
                CustomInputFrame.class);
        IMPLEMENTATIONS.put(new HashSet<String>(
                Arrays.asList(WindowComponent.TEXTAREA.getIdentifier(),
                WindowComponent.INPUTFIELD.getIdentifier(),
                WindowComponent.CERTIFICATE_VIEWER.getIdentifier())),
                ServerFrame.class);
        IMPLEMENTATIONS.put(new HashSet<String>(
                Arrays.asList(WindowComponent.TEXTAREA.getIdentifier(),
                WindowComponent.INPUTFIELD.getIdentifier(),
                WindowComponent.TOPICBAR.getIdentifier(),
                WindowComponent.USERLIST.getIdentifier())),
                ChannelFrame.class);
    }

    /** The controller that owns this window factory. */
    private final SwingController controller;

    /** Our list of listeners. */
    private final ListenerList listeners = new ListenerList();

    /**
     * Creates a new window factory for the specified controller.
     *
     * @param controller The controller this factory is for
     */
    public SwingWindowFactory(final SwingController controller) {
        this.controller = controller;
    }

    /**
     * Registers a new listener which will be notified about the addition
     * and deletion of all Swing UI windows.
     *
     * @param listener The listener to be added
     */
    public void addWindowListener(final SwingWindowListener listener) {
        listeners.add(SwingWindowListener.class, listener);
    }

    /**
     * Un-registers the specified listener from being notified about the
     * addiction and deletion of all Swing UI windows.
     *
     * @param listener The listener to be removed
     */
    public void removeWindowListener(final SwingWindowListener listener) {
        listeners.remove(SwingWindowListener.class, listener);
    }

    /** {@inheritDoc} */
    @Override
    public void addWindow(final FrameContainer window, final boolean focus) {
        addWindow(null, window, focus);
    }

    /**
     * Creates a new window for the specified container.
     *
     * @param window The container that owns the window
     * @param focus Whether the window should be focused initially
     * @return The created window or null on error
     */
    protected TextFrame doAddWindow(final FrameContainer window,
            final boolean focus) {
        final Class<? extends Window> clazz;

        if (IMPLEMENTATIONS.containsKey(window.getComponents())) {
            clazz = IMPLEMENTATIONS.get(window.getComponents());
        } else {
            clazz = window.getWindowClass();
        }

        try {
            final TextFrame frame = (TextFrame) clazz.getConstructors()[0].newInstance(controller, window);
            windows.put(window, frame);

            return frame;
        } catch (Exception ex) {
            Logger.appError(ErrorLevel.HIGH, "Unable to create window", ex);
            return null;
        }
    }

    /**
     * Retrieves a single Swing UI created window belonging to the specified
     * container. Returns null if the container is null or no such window exists.
     *
     * @param window The container whose windows should be searched
     * @return A relevant window or null
     */
    public TextFrame getSwingWindow(final FrameContainer window) {
        return windows.get(window);
    }

    /** {@inheritDoc} */
    @Override
    public void delWindow(final FrameContainer window) {
        delWindow(null, window);
    }

    /** {@inheritDoc} */
    @Override
    public void addWindow(final FrameContainer parent,
            final FrameContainer window, final boolean focus) {
        UIUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                final TextFrame parentWindow = getSwingWindow(parent);
                final TextFrame childWindow = doAddWindow(window, focus);

                if (childWindow == null) {
                    return;
                }

                for (SwingWindowListener listener : listeners.get(SwingWindowListener.class)) {
                    listener.windowAdded(parentWindow, childWindow);
                }

                if (focus) {
                    controller.requestWindowFocus(childWindow);
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void delWindow(final FrameContainer parent, final FrameContainer window) {
        UIUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                final TextFrame parentWindow = getSwingWindow(parent);
                final TextFrame childWindow = getSwingWindow(window);

                for (SwingWindowListener listener : listeners.get(SwingWindowListener.class)) {
                    listener.windowDeleted(parentWindow, childWindow);
                }

                windows.remove(window);
            }
        });
    }

    /** Disposes of this window factory, removing all listeners. */
    public void dispose() {
        for (SwingWindowListener listener : listeners.get(
                SwingWindowListener.class)) {
            listeners.remove(SwingWindowListener.class, listener);
        }
        for (TextFrame frame : windows.values()) {
            frame.dispose();
        }
    }
}
