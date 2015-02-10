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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ViessmannKW implements ViessmannProtocol {
	
	
	static Logger log = LoggerFactory.getLogger(ViessmannKW.class);

	private OptolinkInterface optolinkInterface;


	ViessmannKW(OptolinkInterface optolinkInterface) {
		this.optolinkInterface = optolinkInterface;
		log.error("Init Viessmann Optolink Interface, Protokoll KW not implemented jet");

	}
	

	@Override
	public int getData(byte[] buffer, int address, int length) {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public void setData(byte[] buffer, int address, int length) {
		// TODO Auto-generated method stub
		
	}


}
