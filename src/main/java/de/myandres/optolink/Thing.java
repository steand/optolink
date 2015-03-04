package de.myandres.optolink;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Thing {
	 
	static Logger logger = LoggerFactory.getLogger(Thing.class);

	private String type;
	private String id;
	private String description;
	private List<Channel> channelList;
	
	Thing(String id, String type) {
		logger.trace("Init type: '{}' id: '{}'", type, id );
		channelList = new ArrayList<Channel>();
		this.type = type;
		this.id = id;
		this.description = null;;
	}
	
	Thing(Thing thing) {
		logger.trace("Init type: '{}' id: '{}'", thing.type, thing.id );
		channelList = new ArrayList<Channel>();		
		this.channelList = thing.channelList;
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

	public List<Channel> getChannelList() {
		return channelList;
	}

	public void setChannelList(List<Channel> channelList) {
		this.channelList = channelList;
	}
	
	public void addChannel(Channel channel) {
		channelList.add(new Channel(channel));
	}


}
