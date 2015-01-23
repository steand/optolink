package de.myandres.optolink;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Optolink {

	static Logger log = LoggerFactory.getLogger(Optolink.class);

	private Config config;

	private OutputStream output;
	private InputStream input;
	private CommPortIdentifier portIdentifier;
	private CommPort commPort;
	private boolean isSession;

	Optolink(Config config) {
		this.config = config;
		isSession = false;
		log.debug("Try to open TTY: {}", config.getTTY());
		try {
			portIdentifier = CommPortIdentifier.getPortIdentifier(config
					.getTTY());

			if (portIdentifier.isCurrentlyOwned()) {
				log.error("TTY: {} in use.", config.getTTY());
			}
			commPort = portIdentifier.open(this.getClass().getName(), 2000);
			if (commPort instanceof SerialPort) {
				SerialPort serialPort = (SerialPort) commPort;
				serialPort.setSerialPortParams(4800, SerialPort.DATABITS_8,
						SerialPort.STOPBITS_2, SerialPort.PARITY_EVEN);

				input = serialPort.getInputStream();
				output = serialPort.getOutputStream();
            	commPort.enableReceiveTimeout(10000); // Reading Time-Out to 2 seconds
			}
		} catch (Exception e) {
			log.error("Can't open TTY: {}", config.getTTY());
			log.error("Diagnostic: {}", e.toString());
			e.printStackTrace();
		}
		
		log.debug("TTY: {} opened", config.getTTY());

	}
	
	protected void finalize( ) {
		log.info("Optolink closed by destructor");
	}
	
    // Communication handling
    public synchronized void close() {
            log.debug("Try to close TTY", config.getTTY());
            commPort.close();
            log.info("TTY: {} closed", config.getTTY());
    }


    // Optolink Session handling
    public synchronized void startSession() {
    	    int in;
    	    
	        log.debug("Open Session to Optolink");
	        if (isSession) {
	        	log.debug("Session to optolink already opened");
	        	return;
	        }    
            flush();                                // Puffer leeren - Notwendig wenn tty nicht sauber geschlossen wurde
            psend(0x04);                            // wenn noch offene Kommunikation vorhanden schließen
            for (int i=1; i<4; i++) {               // drei mal versuchen auf Sync
               psend(0x16);                         // Init senden
               psend(0x00);
               psend(0x00);
               if (pread() == 0x06) {               // Ack abfangen
            	   log.trace(" [ACK]");
            	   log.debug("Session to Optolink opened");
            	   isSession = true;
                   return; // Init war OK
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

            for (int i=1; i<4; i++) {   // drei mal versuchen auf Close
                psend(0x04);                          //  Kommunikation schließen
                ret = pread();
                if (ret == 0x06) {                   // Wenn Session noch offen kommt auf (0x04) ein ACK
        	      log.trace("[ACK] resived");
                        if (pread() == 0x05) {            // Und dann wieder das 0x05
                            log.debug("Session to optolink closed");
                            isSession = false;
                            return; // Close war OK
                        }
                }

               if (ret == 0x05) {                    // Session war schon geschlossen und wieder 0x05
                       log.debug("Session to optolink closed");
                       return; // Close war OK
               }
               log.debug("Closing session to optolink failed");
            }
    }

    private synchronized void flush() {
            // Flush input Buffer
            try {
				input.skip(input.available()); 
				log.debug("Input Buffer flushed");
			} catch (IOException e) {
				log.error("Can't flush TTY: {}", config.getTTY());
				log.error("Diagnostic: {}", e.toString());
			}
           
    }

    // Communication: Abfragen einer Adresse

    public synchronized int getData (byte[] buffer, int address, int length) {
            byte[] lb = new byte[32];
            
            if (!isSession) {
	        	log.error("Session to optolink not opened");
	        	return -1;
	        }   

            log.debug("Get data from Optolink for address: {} ", address);

            // construct TxD

            lb[0] = 0x00;                // Anfrage
            lb[1] = 0x01;                // Daten lesen
            lb[2] = (byte)(address >> 8);    // höherwertiges Byte der Adresse
            lb[3] = (byte)(address & 0xff);  // niederwertiges Byte der Adresse
            lb[4] = (byte)length;           // Anzahl der Erwarteten Antwortbytes


            transmit(lb,5);         // Buffer senden

            // RxD
            int rlen = resive(lb);  // Antwort lesen

            // RxD Auswerten
            int raddr;
            if (lb[0] == 0x03) log.error("Answer Byte is 0x03: Return Error(Wrong Adress,maybe)");
            if (lb[0] != 0x01) log.error("Answer Byte (0x01) expect, but: {} resived", String.format("%#02X", lb[0]));
            if (lb[1] != 0x01) log.error("DataRead Byte (0x01) expect, but: {} resived", String.format("%#02X",buffer[1]));
            raddr = ((lb[2] & 0xFF) << 8) + ((int)lb[3] & 0xFF); // Addresse
            if (raddr != address) log.error(String.format("Adress (%#04X) expect, but: %#04X resived", address, raddr));
            for (int i=0;i<lb[4];i++) buffer[i] =lb[i+5];  // Ergebnis übertragen
            return (rlen-5); // bufferlen
    }

// RxD Telegram
    private synchronized int resive(byte[] buffer) {
            log.debug("Try to resive Data from Optolink");
            int ret=pread();
            if (ret != 0x41) {
            	log.error(String.format("Start Byte (0x41) expect, but: %#02X", ret));
            	this.flush();
            	return 0;
            }

            // Anzahl der Bytes in der Antwort.
            int rlen=pread();
            int rcheck=rlen;

            // Lesen der restlichen Bytes
            for (int i=0;i<rlen;i++){
                    buffer[i]=(byte)pread();
                    rcheck+=buffer[i];
            }
            rcheck = rcheck & 0xFF; // nur  8 low bit's .
            int bcheck=pread();
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
            return rlen; // alles OK
    }

    // TxD Telegram
    private synchronized void transmit(byte[] buffer, int len) {
    	
    	    log.debug("Try to transmit Data to Optolink");

            // TxD Checksumme bilden
            int check = len;
            for (int i=0;i<len;i++) check+=buffer[i];

            psend(0x41);                            // Telegram start Byte
            psend(len);                             // Anzahl nutz byte

            for (int i=0; i<len; i++) {
                    psend(buffer[i]);
            }
            psend(check & 0xFF);                    // Checksumme senden


            //  Wait for Acknowledge (0x06)
            int ret =pread();
            if (ret != 0x06){
                    log.error(String.format("acknowledge (0x06) expect, but: %02X resived", ret));
            }            
            log.debug("Data transmit to Optolink: [OK]");
    }

    public synchronized void psend(int by) {
    	    log.trace("TxD: {}", String.format("%02X", (byte)by));
            try {
				output.write((byte)by);
			} catch (IOException e) {
				log.error("Can't write Data to TTY: {}", config.getTTY());
				log.error("Diagnostic: {}", e.toString());
			}

    }

    public synchronized int pread() {
            int  i;
            
            try {
            	log.trace("Try to RxD: {}");
				i=input.read();
				log.trace("RxD: {}", String.format("%02X", i));
				return i;
			} catch (Exception e) {
				log.error("Can't read Data from TTY: {}", config.getTTY());
				log.error("Diagnostic: {}", e.toString());
			} 
            
            return 0;

    }

}
