import java.awt.Point;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map.Entry;


class ServerThread extends Thread{
	private Socket client;
	private Player players = new Player();
	private String clientName;
	public int id;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private boolean isRunning = false;
	private SocketServer server;
	public ServerThread(Socket myClient, SocketServer server) throws IOException {
		this.client = myClient;
		this.server = server;
		isRunning = true;
		out = new ObjectOutputStream(client.getOutputStream());
		in = new ObjectInputStream(client.getInputStream());
		System.out.println("Spawned thread for client " + this.id);
	}
	public boolean isClosed() {
		return client.isClosed();
	}
	Point dir = new Point(-2,-2);
	private void createAndSync(int id, String name) {
		this.id = id;
		Player newPlayer = new Player(name);
		newPlayer.setID(id);
		players.addPlayer(id, newPlayer);
		//generate random position and no direction
		//account for radius offset so we don't get stuck in a wall
		int offset = newPlayer.getRadius() + 1;
		int x = server.random.nextInt(server.game.width - offset) + offset;
		int y = server.random.nextInt(server.game.height - offset) + offset;
		newPlayer.setPosition(x, y);
		newPlayer.setDirection(0, 0);
		//update newly connected player's id
		//this only goes to the new player
		server.outMessages.add(
				new Payload(id, PayloadType.ACK, x, y, name, id));
		//Send Player Connect for new client to all clients
		server.outMessages.add(
				new Payload(id, PayloadType.CONNECT,x, y, name)
				);
		//Send Move Sync Payload for new client to all clients
		server.outMessages.add(
				new Payload(id, PayloadType.MOVE_SYNC, x, y, name)
				);
		//Send Direction Payload for new client to all clients
		server.outMessages.add(
				new Payload(id, PayloadType.DIRECTION, 0, 0, name)
				);
		//send sync details for each previously connected client to newly connected client
		players.players.forEach((pid, p)->{
				//NetworkServer.Output("Adding sync message for " + id + " about " + pid);
				//send sync to target client
				server.outMessages.add(
						new Payload(pid, PayloadType.SYNC, p.getPosition().x, 
								p.getPosition().y, p.getName(), id)
						);
				//send direction to target client
				server.outMessages.add(
						new Payload(pid, PayloadType.DIRECTION, p.getDirection().x,
								p.getDirection().y, p.getName(), id)
						);
				
			
		});
	}
	private void handleTagging(int id) throws Exception {
		Player player = server.players.getPlayer(id);
		if(player != null) {
			SocketServer.Output(player.getID() + " is tagging");
			Entry<Integer,Player> tagged = server.players.checkCollisions(player);
			if(tagged != null) {
				Player taggedPlayer = tagged.getValue();
				System.out.println("Tagged " + taggedPlayer.getName() + "(" + taggedPlayer.getID() + ")");
				if(taggedPlayer.getID() == id) {
					throw new Exception("Somehow we tagged ourself");
				}
				//update server state
				server.players.updatePlayers(tagged.getKey(), PayloadType.SET_IT,
						taggedPlayer.getPosition().x,
						taggedPlayer.getPosition().y,
						taggedPlayer.getName());
				//tagged, make player it and tell all clients
				server.outMessages.add(
						new Payload(tagged.getKey(), PayloadType.SET_IT, 0,0, taggedPlayer.getName())
						);
				
				//update stats of both players and sync with all clients
			
			}
			else {
				SocketServer.Output("No collisions for Tag");
			}
		}
	}

	synchronized void processPayload(int id, Payload payloadIn) {
		Player player = null;
		//NetworkServer.Output("Processing payload from " + clientIp);
		//NetworkServer.Output("Type: " + payloadIn.payloadType.toString());
		switch(payloadIn.payloadType) {
			case CONNECT:
				//add player to internal map
				System.out.println("Player connected with name " + payloadIn.name);
		
				SocketServer.id++;
				createAndSync(SocketServer.id, payloadIn.name);
				break;
			case DIRECTION:
				//blindly update direction
				player = players.getPlayer(id);
				dir.x = payloadIn.x;
				dir.y = payloadIn.y;
				if(player.setDirection(dir)) {
					//if direction changed, send to clients
					server.outMessages.add(
							new Payload(id, PayloadType.DIRECTION, dir.x, dir.y)
							);
				}
				break;
			case DISCONNECT:
				//TODO make sure other client can't cause a different client to disconnect
				player = players.removePlayer(id);
				if(player != null) {
					server.outMessages.add(
							new Payload(id, PayloadType.DISCONNECT)
							);
				}
			case COLLISION:
				//check collision and see if tagged
				SocketServer.Output("Handling trigger tag for " + id);
				try {
					handleTagging(id);
				}
				catch(Exception e) {
					SocketServer.Output(e.getMessage());
				}
				break;
			
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
				System.out.println("Client IP: " + fromClient.id);
				processPayload(fromClient.id,fromClient);
			}
		}
		catch(IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		finally {
			System.out.println("Server cleaning up IO for " + clientName);
			server.outMessages.add(
						new Payload(id, PayloadType.DISCONNECT)
						);
			cleanup();
		}
	}
	public void stopThread() {
		isRunning = false;
	}
	public void send(Payload msg) throws IOException {
		out.writeObject(msg);
	}
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
	
}
