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

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Thing {
	 
	static Logger logger = LoggerFactory.getLogger(Thing.class);

	private String type;
	private String id;
	private String description;
	private Map<String,Channel> channelMap = new HashMap<String,Channel>();
	
	public List<Channel> getChannelMap() {
		return new ArrayList<Channel>(channelMap.values());
	}

	Thing(String id, String type) {
		logger.trace("Init type: '{}' id: '{}'", type, id );
		channelMap.clear();
		this.type = type;
		this.id = id;
		this.description = null;;
	}
	
	Thing(Thing thing) {
		logger.trace("Init type: '{}' id: '{}'", thing.type, thing.id );
		channelMap.clear();		
		this.channelMap = thing.channelMap;
		this.type = thing.type;
		this.id = thing.id;
		this.description = thing.description;

	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void addChannel(Channel channel) {
		channelMap.put(channel.getId(), new Channel(channel));
	}
	
	public Channel getChannel(String id) {
		return channelMap.get(id);	
	}


}
