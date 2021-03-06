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

package com.dmdirc.addons.ui_swing.dialogs.channelsetting;

import com.dmdirc.addons.ui_swing.components.modes.ChannelModesPane;
import com.dmdirc.Channel;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.expandingsettings.SettingsPanel;
import com.dmdirc.addons.ui_swing.dialogs.StandardDialog;
import com.dmdirc.config.Identity;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.config.prefs.PreferencesManager;
import com.dmdirc.ui.interfaces.InputWindow;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

import net.miginfocom.swing.MigLayout;

/**
 * Allows the user to modify channel settings (modes, topics, etc).
 */
public final class ChannelSettingsDialog extends StandardDialog implements
        ActionListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 8;
    /** Channel settings dialogs, semi singleton use. */
    private static volatile ChannelSettingsDialog me;
    /** The channel object that this dialog belongs to. */
    private final Channel channel;
    /** Tabbed pane. */
    private JTabbedPane tabbedPane;
    /** Client settings panel. */
    private SettingsPanel channelSettingsPane;
    /** List modes panel. */
    private ChannelModesPane channelModesPane;
    /** List modes panel. */
    private TopicPane topicModesPane;
    /** List modes panel. */
    private ChannelListModesPane channelListModesPane;
    /** Channel identity file. */
    private final Identity identity;
    /** Channel window. */
    private final InputWindow channelWindow;

    /**
     * Creates a new instance of ChannelSettingsDialog.
     *
     * @param newChannel The channel object that we're editing settings for
     */
    private ChannelSettingsDialog(final Channel newChannel,
            final Window parentWindow, final InputWindow channelWindow) {
        super(parentWindow, ModalityType.MODELESS);

        channel = newChannel;
        identity = IdentityManager.getChannelConfig(channel.getServer().
                getNetwork(), channel.getChannelInfo().getName());
        this.channelWindow = channelWindow;

        initComponents();
        initListeners();
    }

    /**
     * Creates the dialog if one doesn't exist, and displays it.
     *
     * @param channel The channel object that we're editing settings for
     * @param parentWindow Parent window
     * @param channelWindow Channel window
     */
    public static void showChannelSettingsDialog(
            final Channel channel, final Window parentWindow,
            final InputWindow channelWindow) {
        me = getChannelSettingsDialog(channel, parentWindow, channelWindow);

        me.display();
        me.requestFocusInWindow();
    }

    /**
     * Returns the current instance of the ChannelSettingsDialog.
     *
     * @param channel The channel object that we're editing settings for
     * @param parentWindow Parent window
     * @param channelWindow Channel window
     *
     * @return The current ChannelSettingsDialog instance
     */
    public static ChannelSettingsDialog getChannelSettingsDialog(
            final Channel channel, final Window parentWindow,
            final InputWindow channelWindow) {
        synchronized (ChannelSettingsDialog.class) {
            if (me == null) {
                me = new ChannelSettingsDialog(channel, parentWindow,
                        channelWindow);
            }
        }

        return me;
    }

    /** Initialises the main UI components. */
    private void initComponents() {
        tabbedPane = new JTabbedPane();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Channel settings for " + channel.getName());
        setResizable(false);

        orderButtons(new JButton(), new JButton());

        getContentPane().setLayout(new MigLayout(
                "fill, wrap 1, ins panel, hmax 80sp"));
        getContentPane().add(tabbedPane, "growy, pushy, wmin 460, wmax 460");
        getContentPane().add(getLeftButton(), "split 3, right");
        getContentPane().add(getRightButton(), "right");

        initTopicTab();

        initIrcTab();

        initListModesTab();

        initSettingsTab();

        tabbedPane.setSelectedIndex(channel.getConfigManager().
                getOptionInt("dialogstate", "channelsettingsdialog"));
    }

    /** Initialises the Topic tab. */
    private void initTopicTab() {
        topicModesPane = new TopicPane(channel, this, channelWindow);
        tabbedPane.addTab("Topic", topicModesPane);
    }

    /** Initialises the IRC Settings tab. */
    private void initIrcTab() {
        channelModesPane = new ChannelModesPane(channel);

        final JScrollPane channelModesSP = new JScrollPane(channelModesPane);
        channelModesSP.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        channelModesSP.setOpaque(UIUtilities.getTabbedPaneOpaque());
        channelModesSP.getViewport().setOpaque(UIUtilities.getTabbedPaneOpaque());
        channelModesSP.setBorder(null);

        tabbedPane.addTab("Channel Modes", channelModesSP);
    }

    /** Initialises the IRC Settings tab. */
    private void initListModesTab() {
        channelListModesPane = new ChannelListModesPane(channel, this);
        tabbedPane.addTab("List Modes", channelListModesPane);
    }

    /** Initialises the channel Settings (identities) tab. */
    private void initSettingsTab() {
        initSettingsPanel();

        tabbedPane.addTab("Client Settings", channelSettingsPane);
    }

    /** Initialises the channel settings. */
    private void initSettingsPanel() {
        channelSettingsPane = new SettingsPanel(identity,
                "These settings are specific to this channel on this network,"
                + " any settings specified here will overwrite global settings");
        channelSettingsPane.addOption(PreferencesManager
                .getPreferencesManager().getChannelSettings(
                channel.getConfigManager(), identity));
    }

    /** Initialises listeners for this dialog. */
    private void initListeners() {
        getOkButton().addActionListener(this);
        getCancelButton().addActionListener(this);
    }

    /**
     * Called whenever the user clicks on one of the two buttons.
     *
     * @param actionEvent Event generated by this action
     */
    @Override
    public void actionPerformed(final ActionEvent actionEvent) {
        if (getOkButton().equals(actionEvent.getSource())) {
            save();
        } else if (getCancelButton().equals(actionEvent.getSource())) {
            dispose();
        }
    }

    /** Saves the settings. */
    public void save() {
        channelModesPane.save();
        topicModesPane.setChangedTopic();
        channelSettingsPane.save();
        channelListModesPane.save();

        identity.setOption("dialogstate", "channelsettingsdialog",
                String.valueOf(tabbedPane.getSelectedIndex()));

        dispose();
    }

    /** {@inheritDoc} */
    @Override
    public void dispose() {
        if (me == null) {
            return;
        }
        synchronized (me) {
            super.dispose();
            me = null;
        }
    }
}
