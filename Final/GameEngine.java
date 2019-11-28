
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.function.Consumer;

public class GameEngine {
	Player players = new Player();
	static Dimension playArea = new Dimension(1000,1000);
	static boolean isRunning = false;
	SocketClient client;
	LocalPlayer localPlayer;
	UI ui;
	static GameState gameState = GameState.LOBBY;
	public GameEngine (UI ui, Dimension playArea) {
		this.ui = ui;
		GameEngine.playArea = playArea;
		this.localPlayer = new LocalPlayer();
		//start();
	}
	public static Dimension GetPlayArea() {
		return playArea;
	}
	public static float lerp(float a, float b, float f)
	{
	    return a + f * (b - a);
	}
	public static double distance(
			  double x1, 
			  double y1, 
			  double x2, 
			  double y2) {       
	    return Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
	}
	public void run() {
		Thread gameLoop = new Thread() {
			@Override
			public void run() {
				sendConnectPayload();
				Consumer<Payload> c = payload -> processFromServer(payload);
				while (isRunning) {
					//process messages from server
					client.handleQueuedMessages(c, 10);
					//apply current control state
					Player.applyControls(localPlayer.id, localPlayer.player, client);
					//locally move the players
					players.movePlayers();
					//redraw the UI/players
					ui.repaint();
					try {
						Thread.sleep(16);
					} catch (InterruptedException e) {
						e.printStackTrace();
						break;
					}
				}
				System.out.println("Gameloop exiting");
			}
		};
		gameLoop.start();
		System.out.println("Gameloop starting");
	}
	public boolean doSocketConnect(String host, int port) throws NumberFormatException, UnknownHostException, IOException {
		System.out.println("Connecting " + host + ":" + port);
		if(client == null) {
			client = new SocketClient();
		}
		return client.connect(host, port);
	} 
	public void CloseConnection() {
		//TODO send disc
		client.disconnect(localPlayer.id);
		try {
			//wait for disconnect to get sent to server
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		client.terminate();
	}
	void sendConnectPayload() {
		int id = -1;//temporarily set -1, server will give us an id
		localPlayer.id = id;
		//send 0,0 coords, server will fill and echo back
		client.send(localPlayer.id, PayloadType.CONNECT,0,0, localPlayer.name);
	} 
	public void paint(Graphics2D g2d) {
		players.paintPlayers(g2d);
	}
	void addPlayer(Payload p, boolean isMe) {
		Player newPlayer = new Player(p.name);
		newPlayer.setPosition(p.x, p.y);
		newPlayer.setID(p.id);
		players.addPlayer(p.id, newPlayer);
		if(isMe) {
			localPlayer.player = newPlayer;
			localPlayer.name = newPlayer.getName();
			localPlayer.id = p.id;
			System.out.println("Created local player");
		}
	}
	void syncPlayer(Player sync, Payload p) {
		sync.setPosition(p.x, p.y);
	}
	void addOrSync(Player sync, Payload p) {
		if(sync == null) {
			addPlayer(p, false);
		}
		else {
			syncPlayer(sync, p);
		}
	}
	void updatePlayer(Payload p) {
		//try to update the player with whatever payload we received
		players.updatePlayers(p.id, p.payloadType, p.x, p.y, p.name);
	}
	void processFromServer(Payload p) {
		if(p.id < 0) {
			System.out.println("Heard response from server with invalid id " + p.id);
			return;
		}
		Player sync = players.getPlayer(p.id);
		switch(p.payloadType) {
			case ACK://just local player
				System.out.println("ACK Payload: " + p.toString());
				addPlayer(p, true);
				break;
			case CONNECT://broad cast
				//same as sync so drop down
			case SYNC://broad cast
				addOrSync(sync, p);
				break;
			case DISCONNECT: //broad cast
				//disconnection from server, update local track of players
				players.removePlayer(p.id);
				System.out.println("Removing player for " + p.id);
				break;
	
			default:
				updatePlayer(p);
				break;
		}
	}
}
//TODO LocalPlayer class
class LocalPlayer {
	public int id;
	public String name;
	public Player player;
}