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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Channel {
	
	static Logger logger = LoggerFactory.getLogger(Channel.class);
 	
	
	private String id;
	private String description;
	private Telegram telegram;
	
	Channel (Channel channel) {
		logger.trace("Init id: '{}'", channel.getId() );
		this.id = channel.getId();
		this.description = channel.getDescription();
		this.telegram = channel.getTelegram();
	}
	
	Channel (String id ) {
		logger.trace("Init id: '{}'", id );
		this.id = id;
		this.description = null;
		this.telegram = null;
	}
	
	
	public Telegram getTelegram() {
		return telegram;
	}

	public void setTelegram(Telegram telegram) {
		this.telegram = telegram;
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

}
