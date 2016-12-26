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

package controler;

import api.IServerControler;
import api.IServerGui;
import model.Boat;
import model.Joueur;
import model.Room;
import net_utils.HostAdress;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import java.util.Map.Entry;

public class ThreadedServer implements IServerControler {
	private IServerGui serverGui;

	public static final boolean LOG_PUSH = false;
	private static final boolean LOG_ = true;
	private ServerSocket socketEcoute;
	private boolean serverEnd;
	private int port;
	private EcouteThread ecouteThread;
	private List<ServiceThread> services = new ArrayList<ServiceThread>();
	private List<String> model = new ArrayList<String>();
	private boolean broadCast = true;
	private boolean keepConnection = true;
	private boolean stopping;
	private ArrayList<Room> allRooms = new ArrayList<Room>();
	private Map<Joueur, ServiceThread> joueurMap = new HashMap<Joueur, ServiceThread>();
	private Map<Joueur, Room> joueurRoom = new HashMap<Joueur, Room>();

	
	@Override
	public void setServerGui(IServerGui serverGui) {
		this.serverGui = serverGui;
	}

	@Override
	public void start(int port) {
		this.port = port;
		serverEnd = false;
		model.clear();
		ecouteThread = new EcouteThread();
		ecouteThread.start();
	}

	public void addService(ServiceThread s) {
		synchronized (services) {
			services.add(s);
		}
	}

	@Override
	public void end() {
		try {
			stopServer();
		} catch (IOException e) {
			// e.printStackTrace();
			serverGui.setError("[9] " + e.toString());
		}
	}

	private void removeAllServices() {
		synchronized (services) {
			serverGui.log("arr�t des services");
			clog("arr�t des services");
			if (!services.isEmpty()) {
				for (ServiceThread serviceThread : services) {
					serviceThread.close(true);
				}
				List<String> adrs = new ArrayList<String>();
				for (ServiceThread serviceThread : services)
					adrs.add(serviceThread.remoteSocketAddress);
				endServices(adrs);
			}
		}
	}

	private void clog(String mesg) {
		if (LOG_)
			System.out.println(mesg);
	}

	private void stopServer() throws IOException {
		if (!socketEcoute.isClosed()) {
			serverGui.log("arr�t du serveur");
			serverEnd = true;
			removeAllServices();
			serverGui.log("arr�t de l'�coute");
			socketEcoute.close(); // d�bloque socketEcoute.accept() avec une
									// exception et force l'arr�t imm�diat
		}
	}

	@Override
	public List<String> getModel() { // le mod�le est aliment� par les clients
		return model;
	}

	private boolean broadcast(ServiceThread current, String cmd) {
		try {
			synchronized (services) {
				for (ServiceThread service : services)
					if (current == null || service != current)
						service.pushCommand(cmd);
			}
			return true;
		} catch (Exception e) {
			// e.printStackTrace();
			serverGui.setError("[10] " + e.toString());
			return false;
		}
	}

	@Override
	public void setBroadCast(boolean value) {
		if (value != broadCast) {
			broadCast = value;
			if (serverGui != null)
				serverGui.setMode(broadCast, keepConnection);
		}
		if (serverGui != null)
			serverGui.log("broadCast = " + (broadCast ? "true" : "false"));
	}

	@Override
	public void setKeepConnection(boolean value) {
		if (value != keepConnection) {
			keepConnection = value;
			if (!keepConnection) {
				setBroadCast(false);
				removeAllServices();
				// close all connections
			}
			if (serverGui != null)
				serverGui.setMode(broadCast, keepConnection);
		}
		if (serverGui != null)
			serverGui.log("keepConnection = " + (value ? "true" : "false"));

	}

	private void refreshClientList() {
		List<String> clients = new ArrayList<String>();
		synchronized (services) {
			for (ServiceThread service : services)
				clients.add(service.remoteSocketAddress);
		}
		serverGui.setClients(clients);
	}

	private List<ServiceThread> removeCLient(String adr) {
		List<ServiceThread> toRemove = new ArrayList<ServiceThread>();
		for (Iterator i = services.iterator(); i.hasNext();) {
			ServiceThread service = (ServiceThread) i.next();
			if (service.remoteSocketAddress.equals(adr))
				if (!toRemove.contains(service))
					toRemove.add(service);
		}
		return toRemove;
	}

	@Override
	public boolean send(Thread c, String cmd) {
		ServiceThread current = c != null ? ((ServiceThread) c) : null;
		model.add(cmd);
		broadcast(current, cmd);
		broadcast(current, "end");
		return true;
	}

	private void delay(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			// e.printStackTrace();
		}
	}





	class EcouteThread extends Thread {
		@Override
		public void run() {
			try {
				services.clear();
				if (serverGui != null)
					serverGui.setClients(null);
				if (port == -1)
					port = DEFAULT_PORT;

				socketEcoute = new ServerSocket(port);
				String host = HostAdress.getHostAddress();
				//host = "http://192.168.1.45";
				//host = "http://127.0.0.1";
				int port = socketEcoute.getLocalPort();
				if (serverGui != null) {
					serverGui.setHost(host, port);
					serverGui
							.log("[serveur multiclient multisession] d�marr� sur :"
									+ host + ":" + port);
				}
				int connectId = 0;
				while (!serverEnd) {
					Socket socketService = null;
					try {
						socketService = socketEcoute.accept();
					} catch (SocketException e) {
						clog("fin du serveur !");
						socketService = null;
						serverEnd = true;
					}
					if (!serverEnd) {
						if (socketService != null) {
							if (serverGui != null)
								serverGui.log("d�marrage de la session ("
										+ (connectId - 1) + ")");
							ServiceThread s = new ServiceThread(socketService);
							if (serverGui != null)
								serverGui.log("RemoteSocketAddress="
										+ s.remoteSocketAddress);
							addService(s);
							refreshClientList();
							s.start();
						} else if (serverGui != null)
							serverGui.log("socket closed");
					}
				}
				if (!stopping) {
					stopping = true;
					delay(1000);
					stopServer();
				}
			} catch (IOException e) {
				// e.printStackTrace();
				if (serverGui != null)
					serverGui.setError("[2] " + e.getMessage());
			}
		}
	} // class EcouteThread

	class ServiceThread extends Thread {
		
		private Socket socketService;
		private PrintStream output;
		private BufferedReader networkIn;
		private boolean sessionEnd;
		private String remoteSocketAddress = "??";

		public ServiceThread(Socket socketService) {
			super();
			this.socketService = socketService;
			remoteSocketAddress = socketService.getRemoteSocketAddress()
					.toString();
		}

		private boolean open() {
			try {
				output = new PrintStream(socketService.getOutputStream(), true);// autoflush
				networkIn = new BufferedReader(new InputStreamReader(
						socketService.getInputStream()));
			} catch (IOException e) {
				// e.printStackTrace();
				serverGui.setError("[3] " + e.toString());
				return false;
			}
			return true;
		}

		private void close(boolean verbose) {
			try {
				serverGui.log("arr�t du service pour le client "
						+ remoteSocketAddress);
				socketService.close();
				// socketService = null;
			} catch (IOException e) {
				// e.printStackTrace();
				serverGui.setError("[5] " + e.toString());
				if (verbose)
					serverGui
							.log("error while closing service " + e.toString());
			}
		}

		@Override
		public void run() {
			serverGui.log("le client " + remoteSocketAddress
					+ " s'est connect�");
			if (open()) {
				if (broadCast) {
					if (LOG_)
						clog("pushing model to " + remoteSocketAddress);
					pushModel();
					if (LOG_)
						clog("model pushed to " + remoteSocketAddress);
				}
				handleRequests();
				if (socketService != null && socketService.isConnected())
					close(true);
				endService(remoteSocketAddress);
			}
		}

		private void handleRequests() {
			while (checkConnection()) {
				
				String requeteclient = null;
				serverGui.log("attente requete " + remoteSocketAddress);
				try {
					requeteclient = networkIn.readLine();
				} catch (SocketException e) {
					requeteclient = null;
				} catch (Exception e) {
					// e.printStackTrace();
					serverGui.setError("[6] " + e.toString());
				}
				if (requeteclient == null) {
					sessionEnd = true;
					serverGui.log("le client " + remoteSocketAddress
							+ " s'est déconnecté, fin de la session");
					break;
				}
				serverGui.log("le client " + remoteSocketAddress + " demande: "
						+ requeteclient);
				
				
				
				String[] cmd = requeteclient.split(" ");
				System.out.println("request = "+requeteclient+ " cmd0 = " +cmd[0]);
				
				if (cmd[0].equals("connect")) {
					Joueur joueur = new Joueur(cmd[1]);
					Room enteredRoom;
				
					if (!allRooms.isEmpty() && allRooms.get(allRooms.size() - 1).isOpen()) {
						Room latestRoom = allRooms.get(allRooms.size() - 1);
						enteredRoom = latestRoom;
						addPlayerToRoom(latestRoom, joueur);
						Joueur creator = latestRoom.getRoomCreator();
						ServiceThread creatorService = joueurMap.get(creator);
						creatorService.pushCommand("opponent " + joueur.getName());
						creatorService.pushCommand("opponent-has-joined");
						output.println("opponent " + creator.getName());
						
					}
					else {
						enteredRoom = createNewRoom(joueur);
					}
					
					joueurMap.put(joueur, this);
					joueurRoom.put(joueur, enteredRoom);
					output.println("player " + joueur.getName());
					joueur.getDamier().drawBoats();
					output.println("boats " + joueur.getDamier().serializeBoats());
					
					
				} else if (cmd[0].equals("shoot")) {
					
					String[] split = cmd[1].split(",");
					int row = Integer.parseInt(split[0]);
					int col = Integer.parseInt(split[1]);
					Joueur player = null;
					for (Entry<Joueur, ServiceThread> entry : joueurMap.entrySet()) {
						if (entry.getValue() == this) player = entry.getKey();
					}
					Room playerRoom = joueurRoom.get(player);
					String response = playerRoom.play(player, row, col);
					output.println(response + " player");
					
					if (response.startsWith("success")) {
						Joueur opponent = playerRoom.getOpponent(player);
						System.out.println("player = " + player.getName() + " opp = " + opponent.getName());
						ServiceThread opponentService = joueurMap.get(opponent);
						String opponentCmd = response + " opponent";
						opponentService.pushCommand(opponentCmd);
						
						//Check is boat has sunk
						boolean victory = opponent.getDamier().checkVictory();
						Boat sunk = opponent.getDamier().hasSunk(row, col);
						System.out.println("sunk = " + sunk);
						if (victory) {
							opponentService.pushCommand("defeat");
							output.println("victory");
						}
						if (sunk != null) {
							System.out.println(sunk.serialize());
							String serialized = sunk.serialize();
							output.println("you-sank " + serialized);
							opponentService.pushCommand("sunk " + serialized);
						}
					} 
					
				} else if (cmd[0].equals("message")) {
					String receivedMsg = requeteclient.split("::")[1];
					Joueur sender = null;
					for (Entry<Joueur, ServiceThread> entry : joueurMap.entrySet()) {
						if (entry.getValue() == this) sender = entry.getKey();
					}
					Room currentRoom = joueurRoom.get(sender);
					Joueur receiver = currentRoom.getOpponent(sender);
					ServiceThread receiverService = joueurMap.get(receiver);
					receiverService.pushCommand("message ::" + receivedMsg);
					System.out.println("received = " + receivedMsg);
					
					
				}
				
				else if (requeteclient.toLowerCase().contains("stop-server")) {
					// requ�te attendue: stop-server
					output.println("stopping server");
					serverEnd = true;
					sessionEnd = true;
					break;
				} else if (requeteclient.toLowerCase().contains("bye")
						|| requeteclient.toLowerCase().contains("stop")) {
					// requ�te attendue: bye ou stop
					output.println("fin de session pour le client "
							+ remoteSocketAddress);
					sessionEnd = true;
					break;
				} else {
					String[] req = requeteclient.split(" ");
					if (req[0].toLowerCase().equals("date")) {
						// requ�te attendue: date
						Date now = new Date();
						output.println("bonjour " + remoteSocketAddress
								+ " il est " + now.toString());
					}
				}
				if (!keepConnection)
					break;
			}
		}

		private Room createNewRoom(Joueur joueur) {
			Room newRoom = new Room(joueur);
			allRooms.add(newRoom);
			return newRoom;
		}

		private void addPlayerToRoom(Room room, Joueur joueur) {
			room.addPlayer(joueur);
		}

		private void pushModel() {
			synchronized (model) {
				if (model.isEmpty())
					clog("model is empty");
				else
					for (String cmd : model)
						pushCommand(cmd);

				pushCommand("end");
			}
		}

		private boolean checkConnection() {
			return socketService != null && !socketService.isClosed()
					&& !serverEnd && !sessionEnd && !serverGui.isDisposed();
		}

		public void pushCommand(String cmd) {
			if (output != null && !socketService.isClosed()) {
				if (LOG_PUSH)
					serverGui.log(cmd + " pushed to " + remoteSocketAddress);
				if (LOG_)
					clog(cmd + " pushed to " + remoteSocketAddress);
				output.println(cmd);
			} else
				serverGui.setError("unable to push a command");
		}

	} // ServiceThread

	public void endService(final String remoteSocketAddress) {
		new Thread(new Runnable() {
			public void run() {
				List<ServiceThread> toRemove = removeCLient(remoteSocketAddress);
				for (ServiceThread rem : toRemove) {
					services.remove(rem);
				}
				refreshClientList();
			}
		}).start();
	}

	private void endServices(final List<String> adrs) {
		new Thread(new Runnable() {
			public void run() {
				List<ServiceThread> toRemove = new ArrayList<ServiceThread>();
				for (String adr : adrs)
					for (ServiceThread st : removeCLient(adr))
						if (!toRemove.contains(st))
							toRemove.add(st);
				for (ServiceThread rem : toRemove)
					services.remove(rem);
				refreshClientList();
				serverGui.log("raz services");
				services.clear();
				serverGui.setClients(null);
			}
		}).start();
	}

	@Override
	public String[] getUsage() {
		return new String[] { "TODO usage" };
	}

	@Override
	public void dispose() {
		clog("must close " + this.getClass().getSimpleName());
	}

}