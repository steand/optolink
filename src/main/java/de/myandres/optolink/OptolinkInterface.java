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

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortInvalidPortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

import static com.fazecast.jSerialComm.SerialPort.TIMEOUT_READ_BLOCKING;
import static com.fazecast.jSerialComm.SerialPort.TIMEOUT_WRITE_BLOCKING;

public class OptolinkInterface {

	private final static Logger log = LoggerFactory.getLogger(OptolinkInterface.class);

	private OutputStream output;
	private InputStream input;
	private final Config config;
	private SerialPort serialPort;
	private Socket socket = null;


//TODO implement as runnable for URL-based optolinks

	OptolinkInterface(Config config) throws Exception {

		// constructor with implicit open
		this.config = config;
		if (this.config.getTTYType().matches("URL")) { // device is at an URL
			open();
			close();
			log.debug("TTY type URL is present");
		} else { // device is local
			log.debug("Open TTY {} ...", this.config.getTTY());
			try {
				listAvailablePorts();

				serialPort = SerialPort.getCommPort(this.config.getTTY());
				boolean portOpen = serialPort.openPort();

				if (!portOpen) {
					log.error("TTY {} in use.", this.config.getTTY());
					throw new IOException();
				}

				serialPort.setComPortParameters(4800, 8, 2, SerialPort.EVEN_PARITY);
				serialPort.setComPortTimeouts(TIMEOUT_WRITE_BLOCKING | TIMEOUT_READ_BLOCKING, this.config.getTtyTimeOut(), 0);

				input = serialPort.getInputStream();
				output = serialPort.getOutputStream();
				log.debug("TTY {} opened", this.config.getTTY());
			} catch (SerialPortInvalidPortException e)  {
				log.error("Can't open serial port", e);
				throw new IOException();
			}
		}
	}

	public synchronized void close() {
		if (this.config.getTTYType().matches("URL")) {
			log.debug("Close TTY type URL {} ....", this.config.getTTY());
			if (socket != null) {
				try {
					socket.close();
					log.debug("TTY type URL {} closed", this.config.getTTY());
				} catch (IOException e) {
					log.debug("TTY type URL {} can't be closed", this.config.getTTY());
				}
			}
		} else {
			log.debug("Close TTY {} ....", this.config.getTTY());
			serialPort.closePort();
			log.debug("TTY {} closed", this.config.getTTY());
		}
	}

	public synchronized void open() throws Exception {
		if (this.config.getTTYType().matches("URL")) {
			log.debug("Open TTY type URL {}", this.config.getTTY());
			socket = new Socket (this.config.getTTYIP(), this.config.getTTYPort());
			socket.setSoTimeout (this.config.getTtyTimeOut());
			input = socket.getInputStream();
            output = socket.getOutputStream();
			log.debug("TTY type URL is open");
		}
	}

	public synchronized void flush() {
		// Flush input Buffer
		if (this.config.getTTYType().matches("URL")) {
			// We have to wait a certain time. It seems that input.available always has the count 0
			// right after connecting
			try {
				Thread.sleep(30); // 10 ms is too low. 30 ms chosen for a certain fail safe distance
			} catch (InterruptedException e) {
				log.debug("Error while sleeping to wait for buffer flush");
			}
		}
		try {
			input.skip(input.available());
			log.debug("Input Buffer flushed");
		} catch (IOException e) {
			log.error("Can't flush TTY: {}", this.config.getTTY(), e);
		}
	}

	public synchronized void write(int data) {
		log.debug("TxD: {}", String.format("%02X", (byte) data));
		try {
			output.write((byte) data);
		} catch (IOException e) {
			log.error("Can't write Data to TTY {}", this.config.getTTY(), e);
		}
	}

	public synchronized int read() {
		int data = -1;
		try {
			data = input.read();
			log.debug("RxD: {}", String.format("%02X", data));
			if (data == -1) log.trace("Timeout from TTY {}", this.config.getTTY());
			return data;
		} catch (SocketTimeoutException e) {
			log.trace("Timeout from TTY {}", this.config.getTTY());
			return data;
		} catch (Exception e) {
			log.error("Can't read Data from TTY {}", this.config.getTTY(), e);
		}
		return -1; // Ups

	}

	public String getDeviceName() {
		return this.config.getDeviceType();
	}

	private void listAvailablePorts() {
		SerialPort[] serialPorts = SerialPort.getCommPorts();
		log.debug("Available ports:");
		for (SerialPort serialPort : serialPorts) {
			log.debug("{}: {}", serialPort.getPortDescription(), serialPort.getSystemPortName());
		}
	}
}
