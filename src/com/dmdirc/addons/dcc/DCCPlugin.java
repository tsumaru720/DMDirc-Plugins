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

package com.dmdirc.addons.dcc;

import com.dmdirc.Main;
import com.dmdirc.Server;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.actions.interfaces.ActionType;
import com.dmdirc.addons.dcc.actions.DCCActions;
import com.dmdirc.addons.dcc.io.DCC;
import com.dmdirc.addons.dcc.io.DCCChat;
import com.dmdirc.addons.dcc.io.DCCTransfer;
import com.dmdirc.addons.dcc.kde.KFileChooser;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.config.Identity;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.config.prefs.PluginPreferencesCategory;
import com.dmdirc.config.prefs.PreferencesCategory;
import com.dmdirc.config.prefs.PreferencesDialogModel;
import com.dmdirc.config.prefs.PreferencesSetting;
import com.dmdirc.config.prefs.PreferencesType;
import com.dmdirc.interfaces.ActionListener;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.parser.interfaces.ClientInfo;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.plugins.BasePlugin;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.ui.WindowManager;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * This plugin adds DCC to dmdirc.
 */
public final class DCCPlugin extends BasePlugin implements ActionListener {

    /** The DCCCommand we created. */
    private DCCCommand command;
    /** Our DCC Container window. */
    private PlaceholderContainer container;

    /**
     * Ask a question, if the answer is the answer required, then recall
     * handleProcessEvent.
     *
     * @param question Question to ask
     * @param title Title of question dialog
     * @param desiredAnswer Answer required
     * @param type Actiontype to pass back
     * @param format StringBuffer to pass back
     * @param arguments arguments to pass back
     */
    public void askQuestion(final String question, final String title,
            final int desiredAnswer, final ActionType type,
            final StringBuffer format, final Object... arguments) {
        // New thread to ask the question in to stop us locking the UI
        new Thread(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                final int result = JOptionPane.showConfirmDialog(null, question,
                        title, JOptionPane.YES_NO_OPTION);
                if (result == desiredAnswer) {
                    handleProcessEvent(type, format, true, arguments);
                }
            }

        }, "QuestionThread: " + title).start();
    }

    /**
     * Ask the location to save a file, then start the download.
     *
     * @param nickname Person this dcc is from.
     * @param send The DCCSend to save for.
     * @param parser The parser this send was received on
     * @param reverse Is this a reverse dcc?
     * @param token Token used in reverse dcc.
     */
    public void saveFile(final String nickname, final DCCTransfer send,
            final Parser parser, final boolean reverse, final String token) {
        // New thread to ask the user where to save in to stop us locking the UI
        new Thread(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                final JFileChooser jc = KFileChooser.getFileChooser(
                        DCCPlugin.this, IdentityManager.getGlobalConfig()
                        .getOption(getDomain(), "receive.savelocation"));
                int result;
                if (IdentityManager.getGlobalConfig().getOptionBool(getDomain(),
                        "receive.autoaccept")) {
                    result = JFileChooser.APPROVE_OPTION;
                } else {
                    result = showFileChooser(send, jc);
                }
                if (result != JFileChooser.APPROVE_OPTION) {
                    return;
                }
                send.setFileName(jc.getSelectedFile().getPath());
                if (!handleExists(send, jc, nickname, parser,reverse, token)) {
                    return;
                }
                boolean resume = handleResume(jc);
                    if (reverse && !token.isEmpty()) {
                        new TransferContainer(DCCPlugin.this, send,
                                "*Receive: " + nickname, nickname, null);
                        send.setToken(token);
                        if (resume) {
                            if (IdentityManager.getGlobalConfig().getOptionBool(
                                    getDomain(), "receive.reverse.sendtoken")) {
                                parser.sendCTCP(nickname, "DCC", "RESUME "
                                        + send.getShortFileName() + " 0 "
                                        + jc.getSelectedFile().length() + " "
                                        + token);
                            } else {
                                parser.sendCTCP(nickname, "DCC", "RESUME "
                                        + send.getShortFileName() + " 0 "
                                        + jc.getSelectedFile().length());
                            }
                        } else {
                            if (listen(send)) {
                                parser.sendCTCP(nickname, "DCC", "SEND "
                                        + send.getShortFileName() + " "
                                        + DCC.ipToLong(getListenIP(parser))
                                        + " " + send.getPort() + " "
                                        + send.getFileSize() + " " + token);
                            }
                        }
                    } else {
                        new TransferContainer(DCCPlugin.this, send, "Receive: "
                                + nickname, nickname, null);
                        if (resume) {
                            parser.sendCTCP(nickname, "DCC", "RESUME "
                                    + send.getShortFileName() + " "
                                    + send.getPort() + " "
                                    + jc.getSelectedFile().length());
                        } else {
                            send.connect();
                        }
                    }
                }

        }, "saveFileThread: " + send.getShortFileName()).start();
    }

    /**
     * Checks if the selected file exists and prompts the user as required.
     *
     * @param send DCC Transfer
     * @param jc File chooser
     * @param nickname Remote nickname
     * @param parser Parser
     * @param reverse Reverse DCC?
     * @param token DCC token
     *
     * @return true if the user wants to continue, false if they wish to abort
     */
    private boolean handleExists(final DCCTransfer send, final JFileChooser jc,
            final String nickname, final Parser parser, final boolean reverse,
            final String token) {
        if (jc.getSelectedFile().exists() && send.getFileSize() > -1
                && send.getFileSize() <= jc.getSelectedFile().length()) {
            if (IdentityManager.getGlobalConfig().getOptionBool(getDomain(),
                    "receive.autoaccept")) {
                return false;
            } else {
                JOptionPane.showMessageDialog(((SwingController) PluginManager
                        .getPluginManager().getPluginInfoByName("ui_swing")
                        .getPlugin()).getMainFrame(), "This file has already "
                        + "been completed, or is longer than the file you are "
                        + "receiving.\nPlease choose a different file.",
                        "Problem with selected file",
                        JOptionPane.ERROR_MESSAGE);
                saveFile(nickname, send, parser, reverse, token);
                return false;
            }
        }
        return true;
    }

    /**
     * Prompts the user to resume a transfer if required.
     *
     * @param jc File chooser
     *
     * @return true if the user wants to continue the transfer false otherwise
     */
    private boolean handleResume(final JFileChooser jc) {
        if (jc.getSelectedFile().exists()) {
            if (IdentityManager.getGlobalConfig().getOptionBool(getDomain(),
                    "receive.autoaccept")) {
                return true;
            } else {
                final int result = JOptionPane.showConfirmDialog(
                        ((SwingController) PluginManager
                        .getPluginManager().getPluginInfoByName("ui_swing")
                        .getPlugin()).getMainFrame(), "This file exists already"
                        + ", do you want to resume an exisiting download?",
                        "Resume Download?", JOptionPane.YES_NO_OPTION);
                return (result == JOptionPane.YES_OPTION);
            }
        }
        return false;
    }

    /**
     * Sets up and display a file chooser.
     *
     * @param send DCCTransfer object sending the file
     * @param jc File chooser
     *
     * @return   the return state of the file chooser on popdown:
     * <ul>
     * <li>JFileChooser.CANCEL_OPTION
     * <li>JFileChooser.APPROVE_OPTION
     * <li>JFileChooser.ERROR_OPTION if an error occurs or the
     *                               dialog is dismissed
     * </ul>
     */
    private int showFileChooser(final DCCTransfer send, final JFileChooser jc) {
        jc.setDialogTitle("Save " + send.getShortFileName() + " As - DMDirc");
                jc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                jc.setMultiSelectionEnabled(false);
                jc.setSelectedFile(new File(send.getFileName()));
        return jc.showSaveDialog(((SwingController) PluginManager
                .getPluginManager().getPluginInfoByName("ui_swing")
                .getPlugin()).getMainFrame());
    }

    /** {@inheritDoc} */
    @Override
    public void processEvent(final ActionType type, final StringBuffer format,
            final Object... arguments) {
        handleProcessEvent(type, format, false, arguments);
    }

    /**
     * Make the given DCC start listening.
     * This will either call dcc.listen() or dcc.listen(startPort, endPort)
     * depending on config.
     *
     * @param dcc DCC to start listening.
     * @return True if Socket was opened.
     */
    protected boolean listen(final DCC dcc) {
        final boolean usePortRange = IdentityManager.getGlobalConfig()
                .getOptionBool(getDomain(), "firewall.ports.usePortRange");
        try {
            if (usePortRange) {
                final int startPort = IdentityManager.getGlobalConfig()
                        .getOptionInt(getDomain(), "firewall.ports.startPort");
                final int endPort = IdentityManager.getGlobalConfig()
                        .getOptionInt(getDomain(), "firewall.ports.endPort");
                dcc.listen(startPort, endPort);
            } else {
                dcc.listen();
            }
            return true;
        } catch (IOException ioe) {
            return false;
        }
    }

    /**
     * Process an event of the specified type.
     *
     * @param type The type of the event to process
     * @param format Format of messages that are about to be sent. (May be null)
     * @param dontAsk Don't ask any questions, assume yes.
     * @param arguments The arguments for the event
     */
    public void handleProcessEvent(final ActionType type,
            final StringBuffer format, final boolean dontAsk,
            final Object... arguments) {
        if (IdentityManager.getGlobalConfig().getOptionBool(getDomain(),
                "receive.autoaccept") && !dontAsk) {
            handleProcessEvent(type, format, true, arguments);
            return;
        }

        if (type == CoreActionType.SERVER_CTCP) {
            final String[] ctcpData = ((String) arguments[3]).split(" ");
            if ("DCC".equalsIgnoreCase((String) arguments[2])) {
                if ("chat".equalsIgnoreCase(ctcpData[0])
                        && ctcpData.length > 3) {
                    handleChat(type, format, dontAsk, ctcpData, arguments);
                } else if ("send".equalsIgnoreCase(ctcpData[0])
                        && ctcpData.length > 3) {
                    handleSend(type, format, dontAsk, ctcpData, arguments);
                } else if (("resume".equalsIgnoreCase(ctcpData[0])
                        || "accept".equalsIgnoreCase(ctcpData[0]))
                        && ctcpData.length > 2) {
                    handleReceive(ctcpData, arguments);
                }
            }
        }
    }

    /**
     * Handles a DCC chat request.
     *
     * @param type The type of the event to process
     * @param format Format of messages that are about to be sent. (May be null)
     * @param dontAsk Don't ask any questions, assume yes.
     * @param ctcpData CTCP data bits
     * @param arguments The arguments for the event
     */
    private void handleChat(final ActionType type, final StringBuffer format,
            final boolean dontAsk, final String[] ctcpData,
            final Object... arguments) {
        final String nickname = ((ClientInfo) arguments[1]).getNickname();
        if (dontAsk) {
            final DCCChat chat = new DCCChat();
            try {
                chat.setAddress(Long.parseLong(ctcpData[2]),
                        Integer.parseInt(ctcpData[3]));
            } catch (NumberFormatException nfe) {
                return;
            }
            final String myNickname = ((Server) arguments[0]).getParser()
                    .getLocalClient().getNickname();
            final DCCFrameContainer f = new ChatContainer(this, chat,
                    "Chat: " + nickname, myNickname, nickname);
            f.addLine("DCCChatStarting", nickname, chat.getHost(),
                    chat.getPort());
            chat.connect();
        } else {
            ActionManager.getActionManager().triggerEvent(
                    DCCActions.DCC_CHAT_REQUEST, null, ((Server) arguments[0]),
                    nickname);
            askQuestion("User " + nickname + " on "
                    + ((Server) arguments[0]).getName()
                    + " would like to start a DCC Chat with you.\n\n"
                    + "Do you want to continue?",
                    "DCC Chat Request", JOptionPane.YES_OPTION,
                    type, format, arguments);
            return;
        }
    }

    /**
     * Handles a DCC send request.
     *
     * @param type The type of the event to process
     * @param format Format of messages that are about to be sent. (May be null)
     * @param dontAsk Don't ask any questions, assume yes.
     * @param ctcpData CTCP data bits
     * @param arguments The arguments for the event
     */
    private void handleSend(final ActionType type, final StringBuffer format,
            final boolean dontAsk, final String[] ctcpData,
            final Object... arguments) {
        final String nickname = ((ClientInfo) arguments[1]).getNickname();
        final String filename;
        String tmpFilename;
        // Clients tend to put files with spaces in the name in ""
        final StringBuilder filenameBits = new StringBuilder();
        int i;
        final boolean quoted = ctcpData[1].startsWith("\"");
        if (quoted) {
            for (i = 1; i < ctcpData.length; i++) {
                String bit = ctcpData[i];
                if (i == 1) {
                    bit = bit.substring(1);
                }
                if (bit.endsWith("\"")) {
                    filenameBits.append(" ")
                            .append(bit.substring(0, bit.length() - 1));
                    break;
                } else {
                    filenameBits.append(" ").append(bit);
                }
            }
            tmpFilename = filenameBits.toString().trim();
        } else {
            tmpFilename = ctcpData[1];
            i = 1;
        }

        // Try to remove path names if sent.
        // Change file separatorChar from other OSs first
        if (File.separatorChar == '/') {
            tmpFilename = tmpFilename.replace('\\', File.separatorChar);
        } else {
            tmpFilename = tmpFilename.replace('/', File.separatorChar);
        }
        // Then get just the name of the file.
        filename = new File(tmpFilename).getName();

        final String ip = ctcpData[++i];
        final String port = ctcpData[++i];
        long size;
        if (ctcpData.length + 1 > i) {
            try {
                size = Integer.parseInt(ctcpData[++i]);
            } catch (NumberFormatException nfe) {
                size = -1;
            }
        } else {
            size = -1;
        }
        final String token = (ctcpData.length - 1 > i
                && !ctcpData[i + 1].equals("T")) ? ctcpData[++i] : "";

        // Ignore incorrect ports, or non-numeric IP/Port
        try {
            int portInt = Integer.parseInt(port);
            if (portInt > 65535 || portInt < 0) {
                return;
            }
            Long.parseLong(ip);
        } catch (NumberFormatException nfe) {
            return;
        }

        DCCTransfer send = DCCTransfer.findByToken(token);

        if (send == null && !dontAsk) {
            if (!token.isEmpty() && !port.equals("0")) {
                // This is a reverse DCC Send that we no longer care about.
                return;
            } else {
                ActionManager.getActionManager().triggerEvent(
                        DCCActions.DCC_SEND_REQUEST, null,
                        ((Server) arguments[0]), nickname, filename);
                askQuestion("User " + nickname + " on "
                        + ((Server) arguments[0]).getName()
                        + " would like to send you a file over DCC.\n\nFile: "
                        + filename + "\n\nDo you want to continue?",
                        "DCC Send Request", JOptionPane.YES_OPTION, type,
                        format, arguments);
                return;
            }
        } else {
            final boolean newSend = send == null;
            if (newSend) {
                send = new DCCTransfer(IdentityManager.getGlobalConfig()
                        .getOptionInt(getDomain(), "send.blocksize"));
                send.setTurbo(IdentityManager.getGlobalConfig().getOptionBool(
                        getDomain(), "send.forceturbo"));
            }
            try {
                send.setAddress(Long.parseLong(ip), Integer.parseInt(port));
            } catch (NumberFormatException nfe) {
                return;
            }
            if (newSend) {
                send.setFileName(filename);
                send.setFileSize(size);
                saveFile(nickname, send, ((Server) arguments[0]).getParser(),
                        "0".equals(port), token);
            } else {
                send.connect();
            }
        }
    }

    /**
     * Handles a DCC chat request.
     *
     * @param ctcpData CTCP data bits
     * @param arguments The arguments for the event
     */
    private void handleReceive(final String[] ctcpData,
            final Object... arguments) {
        final String filename;
        // Clients tend to put files with spaces in the name in ""
        final StringBuilder filenameBits = new StringBuilder();
        int i;
        final boolean quoted = ctcpData[1].startsWith("\"");
        if (quoted) {
            for (i = 1; i < ctcpData.length; i++) {
                String bit = ctcpData[i];
                if (i == 1) {
                    bit = bit.substring(1);
                }
                if (bit.endsWith("\"")) {
                    filenameBits.append(" ")
                            .append(bit.substring(0, bit.length() - 1));
                    break;
                } else {
                    filenameBits.append(" ").append(bit);
                }
            }
            filename = filenameBits.toString().trim();
        } else {
            filename = ctcpData[1];
            i = 1;
        }

        final int port;
        final int position;
        try {
            port = Integer.parseInt(ctcpData[++i]);
            position = Integer.parseInt(ctcpData[++i]);
            } catch (NumberFormatException nfe) {
                return;
        }
        final String token = (ctcpData.length - 1 > i) ? " "
                + ctcpData[++i] : "";

        // Now look for a dcc that matches.
        for (DCCTransfer send : DCCTransfer.getTransfers()) {
            if (send.getPort() == port && (new File(send.getFileName()))
                    .getName().equalsIgnoreCase(filename)) {
                if ((!token.isEmpty() && !send.getToken().isEmpty())
                        && (!token.equals(send.getToken()))) {
                    continue;
                }
                final Parser parser = ((Server) arguments[0]).getParser();
                final String nick = ((ClientInfo) arguments[1]).getNickname();
                if (ctcpData[0].equalsIgnoreCase("resume")) {
                    parser.sendCTCP(nick, "DCC", "ACCEPT "+ ((quoted) ? "\""
                            + filename + "\"" : filename) + " " + port + " "
                            + send.setFileStart(position) + token);
                } else {
                    send.setFileStart(position);
                    if (port == 0) {
                        // Reverse dcc
                        if (listen(send)) {
                            if (send.getToken().isEmpty()) {
                                parser.sendCTCP(nick, "DCC", "SEND "
                                        + ((quoted) ? "\"" + filename
                                        + "\"" : filename) + " "
                                        + DCC.ipToLong(send.getHost())
                                        + " " + send.getPort()
                                        + " " + send.getFileSize());
                            } else {
                                parser.sendCTCP(nick, "DCC", "SEND "
                                        + ((quoted) ? "\"" + filename
                                        + "\"" : filename)
                                        + " " + DCC.ipToLong(send.getHost())
                                        + " " + send.getPort()
                                        + " " + send.getFileSize() + " "
                                        + send.getToken());
                            }
                        }
                    } else {
                        send.connect();
                    }
                }
            }
        }
    }



    /**
     * Retrieves the container for the placeholder.
     *
     * @since 0.6.4
     * @return This plugin's placeholder container
     */
    public synchronized PlaceholderContainer getContainer() {
        if (container == null) {
            createContainer();
        }

        return container;
    }

    /**
     * Removes the cached container.
     *
     * @since 0.6.4
     */
    public synchronized void removeContainer() {
        container = null;
    }

    /**
     * Create the container window.
     */
    protected void createContainer() {
        container = new PlaceholderContainer(this);
        WindowManager.getWindowManager().addWindow(container);
    }

    /** {@inheritDoc} */
    @Override
    public void domainUpdated() {
        final Identity defaults = IdentityManager.getAddonIdentity();

        defaults.setOption(getDomain(), "receive.savelocation",
                Main.getConfigDir() + "downloads"
                + System.getProperty("file.separator"));
    }

    /**
     * Called when the plugin is loaded.
     */
    @Override
    public void onLoad() {
        final File dir = new File(IdentityManager.getGlobalConfig()
                .getOption(getDomain(), "receive.savelocation"));
        if (dir.exists()) {
            if (!dir.isDirectory()) {
                Logger.userError(ErrorLevel.LOW,
                        "Unable to create download dir (file exists instead)");
            }
        } else {
            try {
                dir.mkdirs();
                dir.createNewFile();
            } catch (IOException ex) {
                Logger.userError(ErrorLevel.LOW,
                        "Unable to create download dir");
            }
        }

        command = new DCCCommand(this);
        CommandManager.getCommandManager().registerCommand(command);

        ActionManager.getActionManager().registerTypes(DCCActions.values());
        ActionManager.getActionManager().registerListener(this,
                CoreActionType.SERVER_CTCP);
    }

    /**
     * Called when this plugin is Unloaded.
     */
    @Override
    public synchronized void onUnload() {
        CommandManager.getCommandManager().unregisterCommand(command);
        ActionManager.getActionManager().unregisterListener(this);
        if (container != null) {
            container.close();
        }
    }

    /**
     * Get the IP Address we should send as our listening IP.
     *
     * @return The IP Address we should send as our listening IP.
     */
    public String getListenIP() {
        return getListenIP(null);
    }

    /**
     * Get the IP Address we should send as our listening IP.
     *
     * @param parser Parser the IRC Parser where this dcc is initiated
     * @return The IP Address we should send as our listening IP.
     */
    public String getListenIP(final Parser parser) {
        final String configIP = IdentityManager.getGlobalConfig().getOption(
                getDomain(), "firewall.ip");
        if (!configIP.isEmpty()) {
            try {
                return InetAddress.getByName(configIP).getHostAddress();
            } catch (UnknownHostException ex) { //NOPMD - handled below
                //Continue below
            }
        }
        if (parser != null) {
            final String myHost = parser.getLocalClient().getHostname();
            if (!myHost.isEmpty()) {
                try {
                    return InetAddress.getByName(myHost).getHostAddress();
                } catch (UnknownHostException e) { //NOPMD - handled below
                    //Continue below
                }
            }
        }
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            // This is almost certainly not what we want, but we can't work out
            // the right one.
            return "127.0.0.1"; //NOPMD
        }
    }

    /** {@inheritDoc} */
    @Override
    public void showConfig(final PreferencesDialogModel manager) {
        final PreferencesCategory general = new PluginPreferencesCategory(
                getPluginInfo(), "DCC", "", "category-dcc");
        final PreferencesCategory firewall = new PluginPreferencesCategory(
                getPluginInfo(), "Firewall", "");
        final PreferencesCategory sending = new PluginPreferencesCategory(
                getPluginInfo(), "Sending", "");
        final PreferencesCategory receiving = new PluginPreferencesCategory(
                getPluginInfo(), "Receiving", "");

        manager.getCategory("Plugins").addSubCategory(general.setInlineAfter());
        general.addSubCategory(firewall.setInline());
        general.addSubCategory(sending.setInline());
        general.addSubCategory(receiving.setInline());

        firewall.addSetting(new PreferencesSetting(PreferencesType.TEXT,
                getDomain(), "firewall.ip", "Forced IP",
                "What IP should be sent as our IP (Blank = work it out)"));
        firewall.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                getDomain(), "firewall.ports.usePortRange", "Use Port Range",
                "Useful if you have a firewall that only forwards specific "
                + "ports"));
        firewall.addSetting(new PreferencesSetting(PreferencesType.INTEGER,
                getDomain(), "firewall.ports.startPort", "Start Port",
                "Port to try to listen on first"));
        firewall.addSetting(new PreferencesSetting(PreferencesType.INTEGER,
                getDomain(), "firewall.ports.endPort", "End Port",
                "Port to try to listen on last"));
        receiving.addSetting(new PreferencesSetting(PreferencesType.DIRECTORY,
                getDomain(), "receive.savelocation", "Default save location",
                "Where the save as window defaults to?"));
        sending.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                getDomain(), "send.reverse", "Reverse DCC",
                "With reverse DCC, the sender connects rather than "
                + "listens like normal dcc"));
        sending.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                getDomain(), "send.forceturbo", "Use Turbo DCC",
                "Turbo DCC doesn't wait for ack packets. this is "
                + "faster but not always supported."));
        receiving.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                getDomain(), "receive.reverse.sendtoken",
                "Send token in reverse receive",
                "If you have problems with reverse dcc receive resume,"
                + " try toggling this."));
        general.addSetting(new PreferencesSetting(PreferencesType.INTEGER,
                getDomain(), "send.blocksize", "Blocksize to use for DCC",
                "Change the block size for send/receive, this can "
                + "sometimes speed up transfers."));
        general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                getDomain(), "general.percentageInTitle",
                "Show percentage of transfers in the window title",
                "Show the current percentage of transfers in the DCC window "
                + "title"));
    }

}
