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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketHandler  {

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

      public void start()   {
    	  
    	  BroadcastListner broadcastListner = new BroadcastListner(config.getPort(), config.getAdapterID());
    	  
    	  Thread broadcastListnerThread = new Thread(broadcastListner);
    	  broadcastListnerThread.setName("BcListner");
    	  broadcastListnerThread.start();
    	  
    	  
    	  // Wait connection
  	  
    	  while (true) {
              try {
            	  log.info("Listen on port {} for connection", config.getPort());
                  Socket socket = server.accept();
                  log.info("Connection on port {} accept. Remote host {}", config.getPort(), socket.getRemoteSocketAddress());
                  open(socket);
              }

              catch (Exception e) {
      	        log.info("Connection on Socket {} rejected or closed by client", config.getPort());
              } 
          } 
         }
	
	
	private void open(Socket socket) throws Exception {
		
		BufferedReader in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintStream(socket.getOutputStream());

        boolean exit=false;
        
        out.println("#Helo from viessmann");
        
        String command;
        String param;
        while(!exit) {
            String [] inStr = in.readLine().trim().split(" +");
            command=inStr[0];
            if (inStr.length > 1) param = inStr[1]; else param="";
            if (log.isTraceEnabled()) {
                 log.trace("Command): |{}|", command);
                 log.trace("param   : |{}|", param);
            }
            switch (command.toLowerCase()) {
            
            case "list" : list(); break;
            case "get" : getThing(param); break;
            case "set" : set(); break;
            case "exit" : exit=true; break;
            default: log.error("Unknown Client Command:", inStr[0]); 
            } 
        } 
        
        out.println("#Bye from viessmann");
		
	}



	private void set() {
		// TODO Auto-generated method stub
		out.println("set() not implemented jet");
		log.info("set() not implemented jet");
		
	}

	private void getThing(String id) {
		List<Channel> channelList;
		Channel channel;
		
		log.debug("Try to get Thing for ID: {}", id);
		Thing thing = config.getThing(id);
		if (thing != null) {
			out.println("<thing id=\"" + thing.getId() + "\" type=\"" + thing.getType() + "\">");
			channelList = thing.getChannelList();
			for (int i=0; i<channelList.size(); i++) {
				channel = channelList.get(i);
				if (!channel.getId().startsWith("*")) {
					out.println("  <channel id=\""+ channel.getId() +"\" value=\""+ 
				                  viessmannHandler.getValue(channel.getTelegram())+"\"/>");
				}
			}
			out.println("</thing>");
		}
	}
	
	

	private void list() {
		log.debug("List Things for ID");
		List<Thing> thingList = config.getThingList();
		List<Channel> channelList;
		Thing thing;
		Channel channel;
		for (int t=0; t<thingList.size(); t++) {
			thing = thingList.get(t);
		if ((thing != null) && !thing.getId().startsWith("*") ) {
			out.println("<thing id=\"" + thing.getId() + "\" type=\"" + thing.getType() + "\">");
			out.println("  <discribtion>" + thing.getDescribtion() + "</describtion>");
			channelList = thing.getChannelList();
			for (int i=0; i<channelList.size(); i++) {
				channel = channelList.get(i);
				if (!channel.getId().startsWith("*")) {
					out.println("  <channel id=\""+ channel.getId() +"\" type=\""+channel.getType()+"\">");
					out.println("    <discribtion>" + channel.getDescribtion() + "</describtion>");
					out.println("</channel>");
				}
			}
			out.println("</thing>");
		}
    	}

	}


}
