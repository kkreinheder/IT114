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
	public  void broadcast(Payload p) throws IOException {
		for(int i = 0; i < clients.size(); i++) {
			clients.get(i).send(p);
		}
	}
	public void sendToClientById(int target, Payload payload) {
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
						try {
							broadcast(payloadOut);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						//TODO send message to client(s)
						if(payloadOut.target > -1) {
							sendToClientById(payloadOut.target, payloadOut);
						}
						else {
							//Note: we're currently not using the exclusion
							try {
								broadcast(payloadOut);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
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
	synchronized void processPayload(Payload payloadIn) throws IOException {
		switch(payloadIn.payloadType) {
			case BROADCAST:
				server.broadcast(new Payload(PayloadType.BROADCAST, "Coordinates: " + payloadIn.x + payloadIn.y));
				break;
			case SINGLE:
				server.sendToClientById(payloadIn.id,new Payload(PayloadType.SINGLE, "Coordinates: " + payloadIn.x + payloadIn.y));
		default:
			break;		
			
		}
	}
	@Override
	public void run() {
		try{
			Payload fromClient;
			while(isRunning && (fromClient = (Payload)in.readObject()) != null) {
				
				System.out.println("Received: " + fromClient);
				System.out.println("Client ID: " + fromClient.id);
				processPayload(fromClient);
			
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