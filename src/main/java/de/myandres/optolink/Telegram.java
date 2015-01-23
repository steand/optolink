package de.myandres.optolink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Telegram {

	static Logger log = LoggerFactory.getLogger(Telegram.class);
	
	// Types of Viessmann 
	public final static byte INT  =    01; // 4 Byte -> int
	public final static byte UINT =    02; // 4 Byte -> int
	public final static byte BOOLEAN = 03; // 1 Byte -> boolean
	public final static byte FLOAT =   04; // 4 byte -> float
    public final static byte DATE =    05; // 8 Byte -> date
    public final static byte DUMP =    99; // Dump for unknown Telegram-Type

 	
	public final static byte READ =    01; // access="r"
	public final static byte WRITE =   02; // access="w"
	public final static byte READ_WRITE = 03; // access="r/w"
	

	private String name;
	private byte access;
	private int address; 
	private short length;
	private byte type;
	private float divider; 
	private int index;
	
	
	Telegram() {
		name = "";
		access = Telegram.READ;
    	address = 0;
		length = 0;
		type = Telegram.DUMP;
		divider = 1.0f; 
	}
	
	Telegram(int index) {
		this.index=index;
		name = "";
		access = Telegram.READ;
    	address = 0;
		length = 0;
		type = Telegram.DUMP;
		divider = 1.0f; 
	}
	
	Telegram(Telegram t) {
		this.name = t.name;
		this.access =t.access;
		this.address = t.address; 
		this.length = t.length;
		this.type = t.type;
		this.divider = t.divider; 
		this.index = t.index;
		
	}

    public void setIndex(int index) {
    	this.index=index;
    	log.trace("Set Index to {}", index);
    }
    
	public void setAccess(String s) {
		if (s==null) {
			log.info("Access Methode not set - set to default: r"); 
		    s="r";
		}
		switch (s.toLowerCase()) {
		case "r": this.access=Telegram.READ;  break;
		case "w": this.access=Telegram.WRITE; break;
		case "r/w": this.access=Telegram.READ_WRITE; break;
		default: this.access=Telegram.READ;
		}
    	log.trace("Set Access to {}({})", s, this.access);
	
	}
	

	public void setAddress(String address) {
		log.trace("----------------------------------------");
		if (address==null) {
			log.error("Telegram Address not set") ;
			this.address=0;
		} else {
			try {
				this.address=Integer.parseInt(address,16);
			} catch (NumberFormatException e) {
				log.error("Invalid  Address format: {}", address);
				this.address=0;
			}
		} 
    	log.trace("Set Adress to {}({})", address, this.address);	
	}

	public void setLength(String length) {
		if (length==null) {
			log.error("Telegram length not set");
			this.length=0;
			return;
		}
		try {
			this.length=Short.parseShort(length);
		} 
		catch (NumberFormatException e) {
			log.error("Invalid length format: {}", length);
			length="0";
		}
	    log.trace("Set Length to {}", length);
	}

	

	public void setType(String type) {
		if (type==null)  log.error("Telegram Type not set"); 
		else { 
			switch(type.toLowerCase()) {
			case "int": this.type = Telegram.INT; break;
			case "uint": this.type = Telegram.UINT; break;
			case "boolean": this.type = Telegram.BOOLEAN; break;
			case "float": this.type = Telegram.FLOAT; break;
			case "date": this.type = Telegram.DATE; break;
			case "dump": this.type = Telegram.DUMP; break; // for Diag
			default: {
				log.error("Unknown Type: {}", type);
				this.type=Telegram.DUMP;
				}
			}
		}	
	   	log.trace("Set Type to {}({})", type, this.type);
	}
	
	public void setDivider(String divider) {
		if (divider==null) {
			log.info("divider not set - set to default: 1.0"); 
		    this.divider=1.0f;
		} else {
		  try {
			this.divider=Float.parseFloat(divider);
			} 
			catch (NumberFormatException e) {
				log.error("Invalid  divider format: {} - set to default: 1.0", divider);
				this.divider=1.0f;
			}
		}
		log.trace("Set dividerider to {}", this.divider);
	}
	

	public void setName(String name) {
		if (name==null) { 
			log.error("Telegram Name not set");
			name="*unknown";
		}
		this.name = name;
	   	log.trace("Set Name to {}", name);
	}
	
	
	
	public String getName() {
		return name;
	}
	
	public int getIndex() {
		return index;
	}

	public byte getAccess() {
		return access;
	}
	
	public String getAccessAsString() {
		switch (access) {
		case Telegram.READ: return "r";
		case Telegram.WRITE: return "w";
		case Telegram.READ_WRITE: return "r/w" ;
		}
		return "r" ;
	}

	public int getAddress() {
		return address;
	}
	
	public int getLength() {
		return length;
	}


	public float getDivider() {
		return divider;
	}

	public byte getType() {
		return type;
	}

}
