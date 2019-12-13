import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;

public class SocketClient {
	Socket server;
	UI ui;
	public static String host;
	public static int port;
	boolean isRunning = false;
	Queue<Payload> outMessages = new LinkedList<Payload>();
	Queue<Payload> inMessages = new LinkedList<Payload>();
	List<Bullet> bullets = new ArrayList<Bullet>();
/*	public void connect(String address, int port) {
		try {
			//create new socket to destination and port
			server = new Socket(address, port);
			System.out.println("Client connected");
			ui = new UI();
			ui.game(); 
		} catch (IOException e) {
			e.printStackTrace();
		}
	} */
	public static void main(String[] args) {
	//	SocketClient client = new SocketClient();
		UI ui = new UI();
		ui.connectGUI();
	//	client.connect(host,port);
		
	}
	public boolean connect(String address, int port) throws UnknownHostException, IOException {
		server = new Socket(address, port);
		System.out.println("Client connected");
		SocketClient self = this;
		Thread nc = new Thread() {
			@Override
			public void run() {
				try {
					self.start();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		nc.start();
		return true;
	} 

	public void handleQueuedMessages(Consumer<Payload> processFromServer, int messagesToHandle) {
		Payload p = null;
		int processed = 0;
		while((p = this.getMessage()) != null) {
			//call the processFromServer callback with the payload as a parameter
			processFromServer.accept(p);
			//process up to [messagesToHandle] messages per "tick"
			processed++;
			if(processed >= messagesToHandle) {
				break;
			}
		}
	}
	void pollMessagesToSend(ObjectOutputStream out) {
		Thread inputThread = new Thread() {
			@Override
			public void run() {
				try {
					while(!server.isClosed() && isRunning) {
						Payload payload = outMessages.poll();
						if(payload != null) {
							out.writeObject(payload);//send to server
							if(payload.payloadType == PayloadType.DISCONNECT) {
								System.out.println("Stopping input thread");
								break;
							}
						}
					}
				}
				catch(Exception e) {
					System.out.println("Client shutdown");
				}
				finally {
					close();
				}
			}
		};
		inputThread.start();//start the thread
	}
	
	void listenForServer(ObjectInputStream in) {
		//Thread to listen for responses from server so it doesn't block main thread
		Thread fromServerThread = new Thread() {
			@Override
			public void run() {
				try {
					while(!server.isClosed() && isRunning) {
						Payload p = (Payload)in.readObject();
						inMessages.add(p);
						if(p.payloadType != PayloadType.MOVE_SYNC && p.payloadType != PayloadType.ENEMY_SYNC)
						System.out.println("Reply from server: " + p.toString());
					}
					System.out.println("Stopping server listen thread");
				}
				catch (Exception e) {
					if(!server.isClosed()) {
						e.printStackTrace();
						System.out.println("Server closed connection");
					}
					else {
						System.out.println("Connection closed");
					}
				}
				finally {
					close();
				}
			}
		};
		fromServerThread.start();//start the thread
	}
	
	public void start() throws IOException {
		if(server == null) {
			return;
		}
		System.out.println("Client Started");
		isRunning = true;
		//listen to console, server in, and write to server out
		try(
				ObjectOutputStream out = new ObjectOutputStream(server.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(server.getInputStream());){
			pollMessagesToSend(out);
			listenForServer(in);
			
			while(!server.isClosed() && isRunning) {
				Thread.sleep(50);
			}
			System.out.println("Exited loop");
			System.exit(0);
			//Thread to listen for keyboard input so main thread isn't blocked
			/*	Thread inputThread = new Thread() {
				@Override
				public void run() {
					try {
						while(!server.isClosed()) {
							int x = 0, y =0;
							String choice;
							System.out.println("Enter X");
							x = si.nextInt();
							System.out.println("Enter Y");
							y = si.nextInt();
							System.out.println("Send to one or all?");
							choice = si.next();
							
							if(choice.equals("all"))
							{
								out.writeObject(new Payload(PayloadType.MESSAGETOALL, x,y));
							}
							else if(choice.equals("one"))
							{
								System.out.println("Enter index");
								int i = si.nextInt();
								out.writeObject(new Payload(PayloadType.MESSAGETOONE, x,y,i));
							}
							else {
								System.out.println("Stopping input thread");
								//we're quitting so tell server we disconnected so it can broadcast
								out.writeObject(new Payload(PayloadType.DISCONNECT, null));
								break;
							}
						} */
					}
					catch(Exception e) {
						System.out.println("Client shutdown");
					}
					finally {
						close();
					}
	}
			
	void processPayload(Payload p) {
		switch(p.payloadType) {
			case CONNECT:
				System.out.println("A client connected");
				break;
			case DISCONNECT:
				System.out.println("A client disconnected");
				break;
			case MESSAGE:
				System.out.println("Replay from server: " + p.x + "," + p.y);
				break;
			default:
				System.out.println("We aren't handling payloadType " + p.payloadType.toString());
				break;
		}
	}
	private void close() {
		if(server != null && !server.isClosed()) {
			try {
				server.close();
				System.out.println("Closed socket");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void send(int id, PayloadType type) {
		send(id, type, 0,0, null);
	}
//	public void send(int id, PayloadType type, String extra) {
//		send(id, type, 0,0,extra);
//	}
	public void send(int id, PayloadType type, int x, int y) {
		send(id, type, x, y, null);
	}
	public void send(int id, PayloadType type, int x, int y, String extra) {
		if(server != null && !server.isClosed()) {
			System.out.println("Sending " + ((PayloadType)type).toString());
			outMessages.add(new Payload(id, type, x, y, extra));
		}
	}
	
	public Payload getMessage() {
		return inMessages.poll();
	}
	
	public void disconnect(int id) {
		send(id, PayloadType.DISCONNECT);
	}
	
	public void terminate() {
		isRunning = false;
		System.exit(0);
	}
	
	public void setHost(String host)
	{
		SocketClient.host = host;
	}
	public void setPort(int port)
	{
		SocketClient.port = port;
	}
}

