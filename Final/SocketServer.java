import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.awt.Dimension;
import java.awt.Point;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.Queue;
import java.util.Random;

public class SocketServer{
	int port = -1;
	static int id = 0;
	List<ServerThread> clients = new ArrayList<ServerThread>();
	public static boolean isRunning = true;
	Queue<Payload> outMessages = new LinkedList<Payload>();
	Random random = new Random();
	private ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
	Dimension game = new Dimension(1000,1000);
	Player players = new Player();
	public SocketServer() {
		isRunning = true;
		exec.scheduleAtFixedRate(()->{
		}, 5, 5, TimeUnit.SECONDS);
	}
	
	public synchronized void broadcast(Payload payload, int excludeId) {
		if(payload.payloadType != PayloadType.MOVE_SYNC) {
			//ignore MOVE_SYNC to cut down on log spam
			SocketServer.Output("Sending message to " + clients.size() + " clients");
		}
		for(int i = 0; i < clients.size(); i++) {
			if(clients.get(i).id == excludeId) {
				continue;
			}
			try {
				clients.get(i).send(payload);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		cleanupClients();
	}
	public synchronized void sendToClientById(int target, Payload payload) {
		//TODO for single client
		for(int i = 0; i < clients.size(); i++) {
			if(clients.get(i).id == target) {
				try {
					clients.get(i).send(payload);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			}
		}
		cleanupClients();
	}

	void cleanupClients() {
		if(clients.size() == 0) {
			//we don't need to iterate or spam if we don't have clients
			return;
		}
		//use an iterator here so we can remove elements mid loop/iteration
		Iterator<ServerThread> it = clients.iterator();
		int start = clients.size();
		while(it.hasNext()) {
			ServerThread s = it.next();
			if(s.isClosed()) {
				s.cleanup();
				outMessages.add(
						new Payload(s.id, PayloadType.DISCONNECT)
						);
				s.stopThread();
				it.remove();
			}
		}
		int diff = start - clients.size();
		if(diff != 0) {
			System.out.println("Cleaned up " + diff + " clients");
		}
	}
	
	public static void Output(String s) 
	{
		System.out.println(s);
	}
	
	private void start(int port) {
		this.port = port;
		System.out.println("Waiting for client");
		try(ServerSocket serverSocket = new ServerSocket(port);){
			trySendMessagesToClients();
			runGameLoop();
			listenForConnections(serverSocket);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				isRunning = false;
				Thread.sleep(50);
				System.out.println("closing server socket");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	void trySendMessagesToClients() {
		Thread messageSender = new Thread() {
			@Override
			public void run() {
				System.out.println("Starting Message Sender");
				while(isRunning) {
					Payload payloadOut = outMessages.poll();
					if(payloadOut != null) {
						//TODO send message to client(s)
						if(payloadOut.target > -1) {
							sendToClientById(payloadOut.target, payloadOut);
						}
						else {
							//Note: we're currently not using the exclusion
							broadcast(payloadOut, -1);
						}
					}
					else {
						//if we don't have a message take a rest
						try {
							Thread.sleep(16);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				System.out.println("Message Sender Thread stopping");
			}
		};
		messageSender.start();
	}
	void runGameLoop() {
		Thread gameLoop = new Thread() {
			@Override
			public void run() {
				int syncCounter = 0;
				System.out.println("Server game loop starting");
				while(isRunning) {
					players.movePlayers();
					syncCounter++;
					//every thread.sleep * 20ms force sync all players
					//we don't want to do this too often for bandwidth concerns
					if(syncCounter > 20) {
						syncCounter = 0;
						for(Entry<Integer, Player> p : players.players.entrySet()) {
							outMessages.add(
									new Payload(p.getKey(), PayloadType.MOVE_SYNC,
											p.getValue().getPosition().x, p.getValue().getPosition().y)
									);
						}
					}
					try {
						Thread.sleep(16);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				System.out.println("Server game loop stopping");
			}
		};
		gameLoop.start();
	}
	void listenForConnections(ServerSocket serverSocket) {
		while(isRunning) {
			try {
				//TODO listen for new connections
				Socket client = serverSocket.accept();
				System.out.println("Client connected");
				ServerThread thread = new ServerThread(client, this);
				thread.start();//start client thread
				clients.add(thread);//add to client pool
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public static void main(String[] arg) {
		System.out.println("Starting Server");
		SocketServer server = new SocketServer();
		int port = -1;
		if(arg.length > 0){
			try{
				port = Integer.parseInt(arg[0]);
			}
			catch(Exception e){
				System.out.println("Invalid port: " + arg[0]);
			}		
		}
		if(port > -1){
			System.out.println("Server listening on port " + port);
			server.start(port);
		}
		System.out.println("Server Stopped");
	}
}