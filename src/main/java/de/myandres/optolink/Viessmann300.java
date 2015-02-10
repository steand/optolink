package de.myandres.optolink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Viessmann300 implements ViessmannProtocol {

	static Logger log = LoggerFactory.getLogger(Viessmann300.class);

	private ComPort comPort;

	private boolean isSession;

	Viessmann300(String port, int timeout) throws Exception {
		log.debug("Init Viessmann Optolink Interface, Protokoll 300");
		comPort = new ComPort(port, timeout);
		isSession = false;
		log.debug("Init compled");

	}
	
    // Communication handling
    public synchronized void close() {
            log.debug("Try to close: Viessmann Optolink Interface, Protokoll 300" );
            comPort.close();
            log.debug("Viessmann Optolink Interface closed");
    }


    // Optolink Session handling
    public synchronized void startSession() {
 
	        log.debug("Open Session to Optolink");
	        if (isSession) {
	        	log.debug("Session to optolink already opened");
	        	return;
	        }    
            comPort.write(0x04);              // close communication, if open
            comPort.flush();                  // flash Input Buffer        
            for (int i=0; i<5; i++) {         // try 5 times to sync 
               comPort.write(0x16);           // send Init
               comPort.write(0x00);
               comPort.write(0x00);
               if (comPort.read() == 0x06) {               // catch Ack 
            	   log.trace(" [ACK]");
            	   log.debug("Session to Optolink opened");
            	   isSession = true;
                   return; // Init is OK
               }
               log.trace("Open Session to Optolink [ACK] failed");
            }
            log.error("Can't open Session to Optolink");
    }

    public synchronized void stopSession() {
            int ret;
            log.debug("Try to Close Optolink Session");
	        if (!isSession) {
	        	log.debug("Session to optolink already closed");
	        	return;
	        }    

            for (int i=0; i<5; i++) {             // try 5 times to close
                comPort.write(0x04);              //  close communication 
                ret = comPort.read();
                if (ret == 0x06) {               
        	      log.trace("[ACK] resived");
                  log.debug("Session to optolink closed");
                  isSession = false;
                  return; // Close  OK
                }

               if (ret == 0x05) {                    // Session already closed (why, i don't now)
                       log.debug("Session to optolink already closed");
                       return; // Close  OK
               }
               log.error("Closing session to optolink failed");
            }
    }


    public synchronized int getData (byte[] buffer, int address, int length) {
            byte[] lb = new byte[16];
            
            log.debug(String.format("Try to get Data for address %#04X returned ", address)); 
            if (!isSession) {
	        	log.error("Session Optolink not opened");
	        	return -1;
	        }   

            // construct TxD

            lb[0] = 0x00;                    // Request
            lb[1] = 0x01;                    // reading Data
            lb[2] = (byte)(address >> 8);    // upper Byte of address
            lb[3] = (byte)(address & 0xff);  // lower Byte of address
            lb[4] = (byte)length;            // number of expected bytes


            transmit(lb,5);         // send Buffer 

            // RxD
            int rlen = resive(lb);  // read answer 

            // check RxD 
            int raddr;
            if (lb[0] == 0x03) log.error("Answer Byte is 0x03: Return Error(Wrong Adress,maybe)");
            if (lb[0] != 0x01) log.error("Answer Byte (0x01) expect, but: {} resived", String.format("%#02X", lb[0]));
            if (lb[1] != 0x01) log.error("DataRead Byte (0x01) expect, but: {} resived", String.format("%#02X",buffer[1]));
            raddr = ((lb[2] & 0xFF) << 8) + ((int)lb[3] & 0xFF); // Address
            if (raddr != address) log.error(String.format("Adress (%#04X) expect, but: %#04X resived", address, raddr));
            for (int i=0;i<lb[4];i++) buffer[i] =lb[i+5];  // coppy Result 
            log.debug(String.format("getData from Optolink for address %#04X returned: ", address)); 
            return (rlen-5); // buffer length 
    }

// RxD Telegram
    private synchronized int resive(byte[] buffer) {
            log.debug("Try to resive Data from Optolink");
            int ret=comPort.read();
            if (ret != 0x41) {
            	log.error(String.format("Start Byte (0x41) expect, but: %#02X", ret));
            	comPort.flush();
            	return -1;
            }

            // number of bytes in received data
            int rlen=comPort.read();
            int rcheck=rlen;    //Checksum

            // reading all data bytes
            for (int i=0;i<rlen;i++){
                    buffer[i]=(byte)comPort.read();
                    rcheck+=buffer[i];      //count checksum 
            }
            rcheck = rcheck & 0xFF; //expected checksum 8 low bit's .
            int bcheck=comPort.read();    // read checksum 
            if (rcheck != bcheck) log.error(String.format("Checksumme (%#02X) expect, but: %#02X resived", rcheck, bcheck));
            log.debug("Data reseved from Optolink: [OK]");
            if (log.isTraceEnabled()) {
            	// Dump Result
            	log.trace("Dump Data (No. dec hex binary)");
            	int tempI;
            	for (int i = 0; i<rlen;i++) {
            		tempI = buffer[i] & 0xFF;
            		log.trace("[{}] {} {}", i, 
            				String.format("%03d %02X", tempI, tempI),
            				String.format("%8s", Integer.toBinaryString(tempI)).replace(' ', '0'));
            		
            	}
            }
            return rlen; // OK
    }

    // TxD Telegram
    private synchronized void transmit(byte[] buffer, int len) {
    	
    	    log.debug("Try to transmit Data to Optolink");

            // TxD build Checksum
            int check = len;
            for (int i=0;i<len;i++) check+=buffer[i];

            comPort.write(0x41);                            // Telegram start byte
            comPort.write(len);                             // nuber of byte

            for (int i=0; i<len; i++) {                    // send data
                    comPort.write(buffer[i]);
            }
            comPort.write(check & 0xFF);                    // send Checksum


            //  Wait for Acknowledge (0x06)
            int ret =comPort.read();
            if (ret != 0x06){
                    log.error(String.format("acknowledge (0x06) expect, but: %02X resived", ret));
            }            
            log.debug("Data transmit to Optolink: [OK]");
    }

 

}
