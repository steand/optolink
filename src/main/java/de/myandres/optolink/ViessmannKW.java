package de.myandres.optolink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ViessmannKW implements ViessmannProtocol {
	
	
	static Logger log = LoggerFactory.getLogger(ViessmannKW.class);

	private ComPort comPort;


	ViessmannKW(String port, int timeout) throws Exception {
		log.debug("Init Viessmann Optolink Interface, Protokoll KW not implemented jet");
//		comPort = new ComPort(port, timeout);
		log.debug("Init compled");
		throw new Exception();

	}
	

	@Override
	public void startSession() {
		// TODO Auto-generated method stub

	}

	@Override
	public void stopSession() {
		// TODO Auto-generated method stub

	}

	@Override
	public int getData(byte[] buffer, int address, int length) {
		// TODO Auto-generated method stub
		return 0;
	}

}
