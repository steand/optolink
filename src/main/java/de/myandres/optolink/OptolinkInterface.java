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

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OptolinkInterface {

	static Logger log = LoggerFactory.getLogger(OptolinkInterface.class);

	private OutputStream output;
	private InputStream input;
	private CommPortIdentifier portIdentifier;
	private CommPort commPort;
	private String device;


	OptolinkInterface(String device, int timeout) throws Exception {

		// constructor with implicit open

		this.device = device;

		log.debug("Try to open TTY: {}", this.device);
		portIdentifier = CommPortIdentifier.getPortIdentifier(this.device);

		if (portIdentifier.isCurrentlyOwned()) {
			log.error("TTY: {} in use.", this.device);
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
		log.debug("TTY: {} opened", this.device);
	}

	public synchronized void close() {
		log.debug("Try to close TTY", device);
		commPort.close();
		log.debug("TTY: {} closed", device);
	}

	public synchronized void flush() {
		// Flush input Buffer
		try {
			input.skip(input.available());
			log.debug("Input Buffer flushed");
		} catch (IOException e) {
			log.error("Can't flush TTY: {}", device, e);
		}

	}

	public synchronized void write(int data) {
		log.trace("TxD: {}", String.format("%02X", (byte) data));
		try {
			output.write((byte) data);
		} catch (IOException e) {
			log.error("Can't write Data to TTY: {}", device, e);
		}
	}

	public synchronized int read() {
		int data;

		try {
			data = input.read();
			log.trace("RxD: {}", String.format("%02X", data));
			return data;
		} catch (Exception e) {
			log.error("Can't read Data from TTY: {}", device, e);
		}

		return -1; // Ups

	}
	
	public String getDeviceName() {
		return device;
	}

}
