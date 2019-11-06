
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.function.Consumer;

public class Client {
	Socket server;
	boolean isRunning = false;
	Queue<Payload> outMessages = new LinkedList<Payload>();
	Queue<Payload> inMessages = new LinkedList<Payload>();
	public Client() {
	}
	public void connect(String address, int port) {
		try {
			server = new Socket(address, port);
			System.out.println("Client connected");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void pollMessagesToSend(ObjectOutputStream out) {
		Thread inputThread = new Thread() {
			@Override
			public void run() {
				try {
					Scanner si = new Scanner(System.in);
					int inputX = 0, inputY = 0;
					while(!server.isClosed() && isRunning) {
						System.out.println("Enter x");
						if(si.hasNextInt())
						{
							inputX = si.nextInt();
						}
						System.out.println("Enter y");
						if(si.hasNextInt())
						{
							inputY = si.nextInt();
						}
						
						System.out.println("Broadcast to all or send to one?");
						String input = si.next();
						if(input.equals("all"))
						{
							Payload payload = new Payload(Server.id,PayloadType.BROADCAST,inputX,inputY);
							out.writeObject(payload);
						//	outMessages.add(payload);
						//payload = outMessages.poll();
						}
						else if(input.equals("one"))
						{
							System.out.println("Enter client ID");
							int id = si.nextInt();
							Payload payload = new Payload(id,PayloadType.SINGLE,inputX,inputY);
							out.writeObject(payload);
						//	outMessages.add(payload);
						//	 payload = outMessages.poll();
							
						}
						else
						{
							System.out.println("Invalid");
							return;
						}
					}
				}
				catch(Exception e) {
					e.printStackTrace();
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
					while(!server.isClosed()&& isRunning) {
						Payload p = (Payload)in.readObject();
				//		inMessages.add(p);
						System.out.println("Replay from server: " + p.toString());
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
		try(
				ObjectOutputStream out = new ObjectOutputStream(server.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(server.getInputStream());){
			
			pollMessagesToSend(out);
			listenForServer(in);
	
			while(!server.isClosed() && isRunning) {
				Thread.sleep(50);
			}
			System.out.println("Exited loop");
			System.exit(0);//force close
			//TODO implement cleaner closure when server stops before client
			//currently hangs/waits on the console/scanner input
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			close();
		}
	}
	public Payload getMessage() {
		return inMessages.poll();
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

	public static void main(String[] args) {
		Client client = new Client();
		int port = -1;
		try{
			//not safe but try-catch will get it
			port = Integer.parseInt(args[0]);
		}
		catch(Exception e){
			System.out.println("Invalid port");
		}
		if(port == -1){
			return;
		}
		client.connect("127.0.0.1", port);
		try {
			//if start is private, it's valid here since this main is part of the class
			client.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
