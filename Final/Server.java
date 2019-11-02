import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;

public class Server{
	int port = -1;
	static int id = 0;
	//private Thread clientListenThread = null;
	List<ServerThread> clients = new ArrayList<ServerThread>();
	Queue<Payload> outMessages = new LinkedList<Payload>();
	public static boolean isRunning = true;
	public Server() {
		isRunning = true;
	}
	public static void Output(String str) {
			System.out.println(str);
	}
	public synchronized void sendToAllClientsExcept(Payload payload, int excludeId) {
		//iterate through all clients and attempt to send the message to each
		if(payload.payloadType != PayloadType.MOVE_SYNC) {
			//ignore MOVE_SYNC to cut down on log spam
			Server.Output("Sending message to " + clients.size() + " clients");
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
		cleanupStaleClients();
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
		cleanupStaleClients();
	}
	void cleanupStaleClients() {
		Iterator<ServerThread> it = clients.iterator();
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
	}
	private void start(int port) {
		this.port = port;
		System.out.println("Waiting for client");
		try(ServerSocket serverSocket = new ServerSocket(port);){
			trySendMessagesToClients();
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
						sendToAllClientsExcept(payloadOut, -1);
						//TODO send message to client(s)
						if(payloadOut.target > -1) {
							sendToClientById(payloadOut.target, payloadOut);
						}
						else {
							//Note: we're currently not using the exclusion
							sendToAllClientsExcept(payloadOut, -1);
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
		Server server = new Server();
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
//Class to hold client connection and prevent it from blocking the main thread of the server
class ServerThread extends Thread{
	private Socket client;
	private String clientName;
	public int id;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private boolean isRunning = false;
	private Server server;
	public ServerThread(Socket myClient, Server server) throws IOException {
		this.client = myClient;
		isRunning = true;
		out = new ObjectOutputStream(client.getOutputStream());
		in = new ObjectInputStream(client.getInputStream());
		System.out.println("Spawned thread for client " + this.id);
	}
	synchronized void processPayload(int id, Payload payloadIn) {
		
		server.outMessages.add(payloadIn);
	}
	@Override
	public void run() {
		try{
			Payload fromClient;
			fromClient = (Payload)in.readObject();
			while(isRunning && (fromClient != null)) {
				
				System.out.println("Received: " + fromClient);
				System.out.println("Client ID: " + fromClient.id);
				int x = fromClient.x;
				int y = fromClient.y;
			//	processPayload(fromClient.id,fromClient);
				Payload p = new Payload(id,x,y);
				
			//	server.outMessages.add(p);
		//		Payload p = (Payload)out.writeObject();
				
		
				send(p);
				
				
				break;
			}
		}
		catch(IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			System.out.println("Server cleaning up IO for " + id);
			cleanup();
		}
	}
	public void stopThread() {
		isRunning = false;
	}
	public void send(Payload msg) throws IOException {
		out.writeObject(msg);
	}
	/*public void send(String msg) {
		out.println(msg);
	} */
	void cleanup() {
		if(in != null) {
			try{in.close();}
			catch(Exception e) { System.out.println("Input already closed");}
		}
		if(out != null) {
			try {out.close();}
			catch(Exception e) {System.out.println("Output already closed");}
		}
	}
	
	public boolean isClosed() {
		return client.isClosed();
	}
}