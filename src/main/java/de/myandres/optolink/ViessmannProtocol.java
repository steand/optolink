package de.myandres.optolink;

public interface ViessmannProtocol {
	
	public void startSession();
	public void stopSession();
	public int getData  (byte[] buffer, int address, int length);
		

}
