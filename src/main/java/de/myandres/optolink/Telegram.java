package de.myandres.optolink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Telegram {

	static Logger log = LoggerFactory.getLogger(Telegram.class);
	
	// Types of Viessmann 
	public final static int INT  =    01; // 4 Byte -> int
	public final static int UINT =    02; // 4 Byte -> int
	public final static int BOOLEAN = 03; // 1 Byte -> boolean
	public final static int FLOAT =   04; // 4 byte -> float
    public final static int DATE =    05; // 8 Byte -> date
    public final static int DUMP =    99; // Dump for unknown Telegram-Type

 	
	public final static int READ =    01; // access="r"
	public final static int WRITE =   02; // access="w"
	public final static int READ_WRITE = 03; // access="r/w"
	

	private String name;
	private int access;
	private int address;
	private int length;
	private int type;
	private float div; 
	private int index;
	private byte[] data = new byte[16];
	
	
	Telegram() {
		name = "";
		access = Telegram.READ;
    	address = 0;
		length = 0;
		type = 0;
		div = 1; 
	}
	
	Telegram(int index) {
		this.index=index;
		name = "";
		access = Telegram.READ;
    	address = 0;
		length = 0;
		type = 0;
		div = 1;  
	
	}
	
	
	Telegram(Telegram telegram) {
		index = telegram.index;
		name = telegram.name;
		access = telegram.access;
    	address = telegram.address;
		length = telegram.length;
		type = telegram.type;
		div = telegram.div; 
	
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
		default: this.access=0;
		}
    	log.trace("Set Access to {} ({})", s, this.access);
	
	}

	public void setAccess(int i) {
		if ((i>=0)&&(i<4)) this.access=i; else this.access=0;
    	log.trace("Set Access to {}", String.format("%04X",i) );
		}
	

	public void setAddress(String address) {
		log.trace("----------------------------------------");
		if (address==null)  log.error("Telegram Address not set");
		else {
		  this.address = Integer.parseInt(address,16);
    	  log.trace("Set Adress to {}", address);
		}
	}

	public void setLength(String length) {
		if (length==null)  log.error("Telegram length not set");
		else {
		    this.length = Integer.parseInt(length);
	    	log.trace("Set Length to {}", length);
		}
	}
	
	public void setLength(int length) {
		this.length = length;
	   	log.trace("Set Length to {}", length);
	}

	public void setType(String type) {
		if (type==null)  log.error("Telegram Type not set");
		switch(type.toLowerCase()) {
		case "int": this.type = INT; break;
		case "uint": this.type = UINT; break;
		case "boolean": this.type = BOOLEAN; break;
		case "float": this.type = FLOAT; break;
		default: this.type=0;

		}	
	   	log.trace("Set Type to {}", type);
	}
	
	public void setDiv(String div) {
		if (div==null) {
			log.info("dividor  not set - set to default: 1"); 
		    this.div=1;
		} else this.div = Float.parseFloat(div);
	   	log.trace("Set Divider to {}", this.div);
	}
	

	public void setName(String name) {
		if (name==null) { 
			log.error("Telegram Name not set");
			name="*unknown";
		}
		this.name = name;
	   	log.trace("Set Name to {}", name);
	}
	
	public void setValue(byte[] data) {
        this.data=data;
        // alle was größer wie Length ist auf 0 -> sicher ist sicher
        for (int i=length; i<8; i++) this.data[i]=0;
        log.trace("Set Value to {}", data);
    }

	public String getValue() {
       long l = 0;
       String s = "";

       switch (type) {
          case INT:  l = data[length - 1]; return String.valueOf((int)(l / div));
          case UINT: l = 0xFF & data[length - 1]; return String.valueOf((int)(l / div));
          case FLOAT: l = data[length - 1]; return String.valueOf(l / div);
          case BOOLEAN: if (data[0]==0) return "true"; else return "false";
          case DATE: return "Type Date not impemented yet";
          case DUMP: for (int i=0; i<length; i++) s=s+String.format("%02X ", data[i]); return s;
          default: return null;
        }
	}
	
	
	public String getName() {
		return name;
	}
	
	public int getIndex() {
		return index;
	}

	public int getAccess() {
		return access;
	}

	public int getAddress() {
		return address;
	}

	public int getLength() {
		return length;
	}

	public int getType() {
		return type;
	}

	public float getFactor() {
		return div;
	}

}
