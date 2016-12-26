/**
 * Copyright (c) 2015 Laboratoire de Genie Informatique et Ingenierie de Production - Ecole des Mines d'Ales
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Francois Pfister (ISOE-LGI2P) - initial API and implementation
 */

//v6

package api;






/**
 *
 * @author cs2015 Ecole des Mines d'Al�s
 *
 */
public interface IServerControler extends IControler {
	static final int DEFAULT_PORT = 8051;
	//boolean send(String cmd);
	void start(int port);
	void end();
	void setBroadCast(boolean value);
	void setKeepConnection(boolean value);
	void setServerGui(IServerGui serverGui);
	boolean send(Thread current, String cmd);
	
}
