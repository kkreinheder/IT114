
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Map.Entry;
import java.util.Random;
import java.util.function.Consumer;

public class GameEngine {
	Player player = new Player();
	
	Enemy enemy = new Enemy();
	static Dimension playArea = new Dimension(1000,1000);
	static boolean isRunning = false;
	SocketClient client;
	SocketServer server;
	LocalPlayer localPlayer;
	public int id;
//	Player players = player.getPlayer(id);
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
					enemy.moveEnemy();
					player.movePlayers();
				//	checkTouched();
					if(enemy.center.x <= 0 || enemy.center.x >= 1000 || enemy.center.x <= 0 || enemy.center.y >= 1000)
					{
						enemy = null;
					//	enemy = new Enemy();
					}
					if(enemy == null)
					{
						 enemy = new Enemy();
					}
	
				
			//	 checkCollisions(player.getPlayer(id));
					
				//	Point playerPos = player.getPosition();
				//	Point enemyPos = enemy.getPosition();
					
				/*	if(playerPos.equals(enemyPos))
					{
						Random r = new Random();
						//enemy.setPosition();
						enemy.direction.x = r.nextInt(5) + 5;
						enemy.direction.y = -1;
					} */
					//player.center.x == enemy.center.x && player.center.y == player.center.y
					if(enemy.center.y == 0)
					{
						enemy.direction.y = 1;
					} 
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
	public static int calculateDistanceBetweenPoints(
			  Point a, 
			  Point b) {       
	    return (int)Math.sqrt((b.y - a.y) * (b.y - a.y) + (b.x - a.x) * (b.x - a.x));
	}
	Point playerPos = player.getPosition();
	Point enemyPos =  enemy.getPosition();

	public void checkCollisions(Player p) {
	//	Entry<Integer,Player> tagged = null;
		synchronized(player.players) {
			for(Entry<Integer, Player> set : player.players.entrySet()) {
				if(p.getID() != set.getKey()) {
					System.out.println("Tagger ID: " + p.getID());
					System.out.println("Checking ID: " + set.getKey());
					//get distance between centers
					int dist = calculateDistanceBetweenPoints(p.getPosition(), enemyPos);
					//System.out.println("Dist: " + dist);
					//calculate expected distance based on radius
					int rad = (p.getRadius()+enemy.getRadius());
					//System.out.println("Rad: " + rad);
					//check if point is within range
					if(dist <= rad) {
						Random r = new Random();
					enemy.setDirection(r.nextInt(5) + 5, -1);
						break;
					}

//	int dist = calculateDistanceBetweenPoints(playerPos, enemyPos );
	//System.out.println("Dist: " + dist);
	//calculate expected distance based on radius
//	int rad = (player.getRadius()+ enemy.getRadius());
	//System.out.println("Rad: " + rad);
	//check if point is within range
//	if(dist <= 200) {
//		Random r = new Random();
//		enemy.setDirection(r.nextInt(5) + 5, -1);
	//	enemy = null;
//	}
}
			}
		}
	}

	public boolean doSocketConnect(String host, int port) throws NumberFormatException, UnknownHostException, IOException {
		System.out.println("Connecting " + host + ":" + port);
		if(client == null) {
			client = new SocketClient();
		}
		return client.connect(host, port);
	} 
	
	public void checkTouched()
	{
		
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
		player.paintPlayers(g2d);
	}
	public void paintEnemy(Graphics2D g2d) {
		enemy.paintEnemy(g2d);
	}
	void addPlayer(Payload p, boolean isMe) {
		Player newPlayer = new Player(p.name);
		newPlayer.setPosition(p.x, p.y);
		newPlayer.setID(p.id);
		player.addPlayer(p.id, newPlayer);
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
		player.updatePlayers(p.id, p.payloadType, p.x, p.y, p.name);
	}
	void updateEnemy(Payload p) {
		//try to update the player with whatever payload we received
		enemy.updateEnemy(p.id, p.payloadType, p.x, p.y);
	}
	
	void processFromServer(Payload p) {
		if(p.id < 0) {
			System.out.println("Heard response from server with invalid id " + p.id);
			return;
		}
		Player sync = player.getPlayer(p.id);
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
				player.removePlayer(p.id);
				System.out.println("Removing player for " + p.id);
				break;
			case ENEMY_SYNC:
				updateEnemy(p);
	
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