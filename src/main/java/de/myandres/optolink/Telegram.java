package de.myandres.optolink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Telegram {

	static Logger log = LoggerFactory.getLogger(Telegram.class);
	
	// Types of Viessmann 
	public final static byte BOOLEAN =   1; // 1 Byte -> boolean
	public final static byte BYTE    =   2; // 1 Byte -> short
	public final static byte UBYTE   =   3; // 1 Byte -> short
	public final static byte SHORT  =    4; // 2 Byte -> int
	public final static byte USHORT =    5; // 2 Byte -> int
	public final static byte INT =       6; // 4 byte -> long
    public final static byte UINT =      7; // 4 Byte -> long
    public final static byte DATE =      8; // 8 Byte -> date
    public final static byte DUMP =      99; // Dump for unknown Telegram-Type

 	
	public final static byte READ =       1; // access="r"
	public final static byte WRITE =      2; // access="w"
	public final static byte READ_WRITE = 3; // access="r/w"
	

	private String name;
	private byte access;
	private int address; 
	private short length;
	private byte type;
	private short divider; 
	private int index;
	
	
	Telegram() {
		name = "";
		access = Telegram.READ;
    	address = 0;
		length = 0;
		type = Telegram.DUMP;
		divider = 1; 
	}
	
	Telegram(int index) {
		this.index=index;
		name = "";
		access = Telegram.READ;
    	address = 0;
		length = 0;
		type = Telegram.DUMP;
		divider = 1; 
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

	

	public void setType(String type) {
		if (type == null)
			log.error("Telegram Type not set");
		else {
			switch (type.toLowerCase()) {
			case "boolean":
				this.type = Telegram.BOOLEAN;
				length=1;
				break;
			case "byte":
				this.type = Telegram.BYTE;
				length=1;
				break;
			case "ubyte":
				this.type = Telegram.UBYTE;
				length=1;
				break;
			case "short":
				this.type = Telegram.SHORT;
				length=2;
				break;
			case "ushort":
				this.type = Telegram.USHORT;
				length=2;
				break;
			case "int":
				this.type = Telegram.INT;
				length=4;
				break;
			case "uint":
				this.type = Telegram.UINT;
				length=4;
				break;
			case "date":
				this.type = Telegram.DATE;
				length=8;
				break;
			default: {
				log.error("Unknown Type: {}", type);
				this.type = Telegram.DUMP;
				length=0;

			}
			}
		}
		log.trace("Set Type to {}({}) length={}", type, this.type, length);
	}
	
	public void setDivider(String divider) {
		if (divider==null) {
			log.info("divider not set - set to default: 1"); 
		    this.divider=1;
		} else {
		  try {
			this.divider=Short.parseShort(divider);
			} 
			catch (NumberFormatException e) {
				log.error("Invalid  divider format: {} - set to default: 1", divider);
				this.divider=1;
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


	public short getDivider() {
		return divider;
	}

	public byte getType() {
		return type;
	}

}
