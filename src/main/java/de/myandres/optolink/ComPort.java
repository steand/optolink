package de.myandres.optolink;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComPort {

	static Logger log = LoggerFactory.getLogger(ComPort.class);

	private OutputStream output;
	private InputStream input;
	private CommPortIdentifier portIdentifier;
	private CommPort commPort;
	private String port;

	ComPort() {
	};

	ComPort(String port, int timeout) throws Exception {

		// constructor with implicit open

		open(port, timeout);
	}

	public synchronized void open(String port, int timeout) throws Exception {

		this.port = port;

		log.debug("Try to open TTY: {}", this.port);
		portIdentifier = CommPortIdentifier.getPortIdentifier(this.port);

		if (portIdentifier.isCurrentlyOwned()) {
			log.error("TTY: {} in use.", this.port);
			throw new IOException();
		}
		commPort = portIdentifier.open(this.getClass().getName(), timeout);
		if (commPort instanceof SerialPort) {
			SerialPort serialPort = (SerialPort) commPort;
			serialPort.setSerialPortParams(4800, SerialPort.DATABITS_8,
					SerialPort.STOPBITS_2, SerialPort.PARITY_EVEN);

			input = serialPort.getInputStream();
			output = serialPort.getOutputStream();
			commPort.enableReceiveTimeout(timeout); // Reading Time-Out
		}
		log.debug("TTY: {} opened", this.port);
	}

	public synchronized void close() {
		log.debug("Try to close TTY", port);
		commPort.close();
		log.debug("TTY: {} closed", port);
	}

	public synchronized void flush() {
		// Flush input Buffer
		try {
			input.skip(input.available());
			log.debug("Input Buffer flushed");
		} catch (IOException e) {
			log.error("Can't flush TTY: {}", port, e);
		}

	}

	public synchronized void write(int by) {
		log.trace("TxD: {}", String.format("%02X", (byte) by));
		try {
			output.write((byte) by);
		} catch (IOException e) {
			log.error("Can't write Data to TTY: {}", port, e);
		}
	}

	public synchronized int read() {
		int i;

		try {
			i = input.read();
			log.trace("RxD: {}", String.format("%02X", i));
			return i;
		} catch (Exception e) {
			log.error("Can't read Data from TTY: {}", port, e);
		}

		return -1; // Ups

	}

}
