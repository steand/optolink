/*******************************************************************************
 * Copyright (c) 2015,  Stefan Andres.  All rights reserved.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 3.0 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *******************************************************************************/
package de.myandres.optolink;

/*
 * Install a Socked Handler for ip communication
 *
 * Server can found via Broadcast
 * Server API Client can connect via TCP
 *
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketHandler {

    static Logger log = LoggerFactory.getLogger(SocketHandler.class);

    private Config config;
    private ServerSocket server;
    private ViessmannHandler viessmannHandler;
    private PrintStream out;

    SocketHandler(Config config, ViessmannHandler viessmannHandler) throws Exception {

        this.config = config;
        this.viessmannHandler = viessmannHandler;

        server = new ServerSocket(config.getPort());
    }

    public void start() {

        BroadcastListner broadcastListner = new BroadcastListner(config.getPort(), config.getAdapterID());

        // Put broadcast listner in background

        Thread broadcastListnerThread = new Thread(broadcastListner);
        broadcastListnerThread.setName("BcListner");
        broadcastListnerThread.start();

        // Wait connection

        while (true) {
            try {
                log.info("Listen on port {} for connection", config.getPort());
                Socket socket = server.accept();
                log.info("Connection on port {} accept. Remote host {}", config.getPort(),
                        socket.getRemoteSocketAddress());
                open(socket);
            }

            catch (Exception e) {
                log.info("Connection on Socket {} rejected or closed by client", config.getPort());
            }
        }
    }

    private void open(Socket socket) throws Exception {

        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintStream(socket.getOutputStream());

        out.println("<!-- #Helo from viessmann -->");
        out.println("<optolink>");

        String inStr;

        while (true) {
            inStr = in.readLine();
            if (inStr.toLowerCase().startsWith("exit")) {
                break;
            }
            if (!inStr.equals(null)) {
                new Thread(new CommandExec(inStr)).start();
            }
        }

        out.println("<!-- #Bye from viessmann -->");

    }

    // Start a thread for each call command: No blocking of caller

    public class CommandExec implements Runnable {

        String param;

        CommandExec(String param) {
            this.param = param;
        }

        @Override
        public void run() {
            String command;
            String param1;
            String param2;

            log.debug("Execute Thread for: '{}'", param);
            String[] inStr = param.trim().split(" +");
            command = inStr[0];
            if (inStr.length > 1) {
                param1 = inStr[1];
            } else {
                param1 = "";
            }
            if (inStr.length > 2) {
                param2 = inStr[2];
            } else {
                param2 = "";
            }
            exec(command, param1, param2);
            log.debug("Thread for: '{}' done", param);
        }

    }

    private synchronized void exec(String command, String param1, String param2) {

        if (log.isTraceEnabled()) {
            log.trace("Queue Command: |{}|", command);
            log.trace("      param1 : |{}|", param1);
            log.trace("      param2 : |{}|", param2);
        }

        switch (command.toLowerCase()) {

            case "list":
                list();
                break;
            case "get":
                if (param2.equals("")) {
                    getThing(param1);
                } else {
                    getThing(param1, param2);
                }
                break;
            case "set":
                set(param1, param2);
                break;
            default:
                log.error("Unknown Client Command:", command);

                log.trace("Queue Command: |{}| done", command);

        }

    }

    private void set(String id, String value) {
        // Format id = <thing>:<channel>

        String[] ids = id.trim().split(":");

        if (ids.length != 2) {
            log.error("Wrong format '{}' of id", id);
            return;
        }
        Telegram telegram = config.getThing(ids[0]).getChannel(ids[1]).getTelegram();
        if (telegram != null) {
            out.println("<data>");
            out.println("  <thing id=\"" + ids[0] + "\">");

            out.println("    <channel id=\"" + ids[1] + "\" value=\""
                    + viessmannHandler.setValue(telegram, value.toUpperCase()) + "\"/>");
            out.println("  </thing>");
            out.println("</data>");
        }

    }

    private void getThing(String id) {
        log.debug("Try to get Thing for ID: {}", id);
        Thing thing = config.getThing(id);
        if (thing != null) {
            out.println("<data>");
            out.println("  <thing id=\"" + thing.getId() + "\">");
            for (Channel channel : thing.getChannelMap()) {
                if (!channel.getId().startsWith("*")) {
                    out.println("    <channel id=\"" + channel.getId() + "\" value=\""
                            + viessmannHandler.getValue(channel.getTelegram()) + "\"/>");
                }
            }
            out.println("  </thing>");
            out.println("</data>");
        }
    }

    private void getThing(String id, String channels) {
        Channel channel;
        log.debug("Try to get Thing for ID: {} channels: {}", id, channels);
        String[] channelList = channels.split(",");
        Thing thing = config.getThing(id);
        if (thing != null) {
            out.println("<data>");
            out.println("  <thing id=\"" + thing.getId() + "\">");
            for (int i = 0; i < channelList.length; i++) {
                channel = thing.getChannel(channelList[i]);
                if (channel != null) {
                    out.println("    <channel id=\"" + channel.getId() + "\" value=\""
                            + viessmannHandler.getValue(channel.getTelegram()) + "\"/>");
                } else {
                    log.error("Channel : {}.{} not define! ", id, channelList[i]);
                }
            }
            out.println("  </thing>");
            out.println("</data>");
        }
    }

    private void list() {
        log.debug("List Things for ID");
        out.println("<define>");
        for (Thing thing : config.getThingList()) {

            if ((thing != null) && !thing.getId().startsWith("*")) {

                out.println("  <thing id=\"" + thing.getId() + "\" type=\"" + thing.getType() + "\">");
                // out.println(" <description" + thing.getDescription() + "</description>");
                for (Channel channel : thing.getChannelMap()) {
                    if (!channel.getId().startsWith("*")) {
                        out.println("    <channel id=\"" + channel.getId() + "\"/>");
                        // out.println(" <description>" + channel.getDescription() + "</description>");
                        // out.println(" </channel>");
                    }
                }
                out.println("  </thing>");

            }
        }
        out.println("</define>");

    }

}
