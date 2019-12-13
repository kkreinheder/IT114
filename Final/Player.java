import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.KeyStroke;

public class Player
{
	public Point center = new Point();
	private Point direction = new Point();
	private int radius = 25;
	private int diameter = 2*radius;
//	Enemy enemy = new Enemy();
//	SocketServer server = new SocketServer();
	private int speed = 2;
	int xi, yi, xMin, xMax, yMin, yMax;
	int lastX = 10000, lastY = 10000;
	private String name = "";
	private Dimension nameSize = new Dimension(0,0);
	public Hashtable<Integer, Player> players = new Hashtable<Integer, Player>();
	private ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(3);

	private int id = -1;
	public Player(String name) {
		this.name = name;
		diameter = radius * 2;
		center.x = 100;
		center.y = 100;
		direction.x = 1;
		direction.y = 1;
	}
	public Player()
	{
		
	}

	public String getName() {
		return this.name;
	}
	public void setID(int id) {
		this.id = id;
	}
	public int getID() {
		return this.id;
	}
	public int getRadius() {
		return radius;
	}
	public void setPosition(int x, int y) {
		//check distance for snapping or lerping
		double dist = GameEngine.distance(x, y, center.x, center.y);
		if(dist < 50) {
			float f = 1 - (float)dist/50f;//invert the %
			//apply lerp to easy the x,y to the new coordinate to reduce jitty
			center.x = (int)GameEngine.lerp(x, center.x, f);
			center.y = (int)GameEngine.lerp(y, center.y, f);
		}
		else {
			center.x = x;
			center.y = y;
		}
	}
	public Point getPosition() {
		return center;
	}
	public Point move() {
		/***
		 * Get center coordinates
		 * Check if Direction is set, then apply movement
		 */
		xi = center.x;
		if(direction.x != 0) {
			xi += (direction.x * speed);
		}
		yi = center.y;
		if(direction.y != 0) {
			yi += (direction.y * speed);
		}
	
		xMin = xi - radius;
		xMax = xi + radius;
		yMin = yi - radius;
		yMax = yi + radius;
	
		if(xMin >= 0 && xMax <= GameEngine.GetPlayArea().width) {
			center.x = xi;
		}
		if(yMin >= 0 && yMax <= GameEngine.GetPlayArea().height) {
			center.y = yi;
		}
		lastX = center.x;
		lastY = center.y;
		return center;
	}
	static boolean isValidDirection(int d, int current) {
		if(d == -2) {
			return false;
		}
		if(d >= -1 && d <= 1) {
			if(d != current) {
				return true;
			}
		}
		
		return false;
	}
	

	public boolean setDirection(Point p) {
		boolean changed = false;
		if(isValidDirection(p.x, direction.x)) {
			direction.x = p.x;
			changed = true;
		}
		if(isValidDirection(p.y, direction.y)) {
			direction.y = p.y;
			changed = true;
		}
		return changed;
	}

	public void setDirection(int x, int y) {
		direction.x = x;
		direction.y = y;
	}
	
	private void drawString(Graphics2D g, String text, int x, int y) {
		int i = 0;
        for (String line : text.split("\n")) {
            g.drawString(line, x, y += i * g.getFontMetrics().getHeight());
        	i++;
		}
    }

	protected void paintPlayer(Graphics2D g2d) {
        if (center != null && g2d != null) {
   
            	g2d.fillRect(center.x - radius, center.y - radius, diameter, diameter);
            
            
            g2d.setColor(Color.BLUE);
            if(nameSize.width == 0) {
            	FontMetrics fm = g2d.getFontMetrics();
                nameSize.width = (int) fm.getStringBounds(name, g2d).getWidth();
                nameSize.height = fm.getMaxAscent();
            }
            g2d.setColor(Color.BLACK);
            drawString(g2d, name + "\n(" + id +")", (int) (center.x - (nameSize.width * .51)),
                (int) (center.y + (nameSize.height * .01f)));
            
        }
    }
	
	
	
	
	
	
	public static boolean LEFT_DOWN = false, RIGHT_DOWN = false, UP_DOWN = false, DOWN_DOWN = false, SPACE_DOWN = false;
	private static Point pdd = new Point(-2,-2);
	public Point getDirection() {
		return pdd;
	}
	private static void handleControls() {
		if(!UP_DOWN && !DOWN_DOWN) {
			pdd.y = 0;
		}
		if(!LEFT_DOWN && !RIGHT_DOWN) {
			pdd.x = 0;
		}
		if(LEFT_DOWN) {
			pdd.x = -1;
		}
		if(RIGHT_DOWN) {
			pdd.x = 1;
		}
		if(UP_DOWN) {
			pdd.y = -1;
		}
		if(DOWN_DOWN) {
			pdd.y = 1;
		}
	}
	public static void setKeyBindings(InputMap im, ActionMap am) {
		
		//bind key actions to action map
		im.put(KeyStroke.getKeyStroke("pressed W"), "pW");
		im.put(KeyStroke.getKeyStroke("pressed A"), "pA");
		im.put(KeyStroke.getKeyStroke("pressed S"), "pS");
		im.put(KeyStroke.getKeyStroke("pressed D"), "pD");
	//	im.put(MouseEvent.getButton(), "pL");
		
		im.put(KeyStroke.getKeyStroke("released W"), "rW");
		im.put(KeyStroke.getKeyStroke("released A"), "rA");
		im.put(KeyStroke.getKeyStroke("released S"), "rS");
		im.put(KeyStroke.getKeyStroke("released D"), "rD");
		
		im.put(KeyStroke.getKeyStroke("pressed SPACE"), "SPACE");
		
		am.put("pW", new MoveAction(true,0,-1));
		am.put("pA", new MoveAction(true,-1,0));
		am.put("pS", new MoveAction(true,0,1));
		am.put("pD", new MoveAction(true,1,0));
		
		am.put("rA", new MoveAction(false,-1,0));
		am.put("rW", new MoveAction(false,0,-1));
		am.put("rS", new MoveAction(false,0,1));
		am.put("rD", new MoveAction(false,1,0));
		
		am.put("SPACE", new Bullet());
		
	}
	static void applyControls(int id, Player myPlayer, SocketClient client) {
		if(myPlayer != null) {
			handleControls();
			//apply direction and see if it changed
			if(myPlayer.setDirection(pdd)) {
				//send to server
				Point mp = myPlayer.getDirection();
				System.out.println("Direction: " + mp.toString());
				//TODO Send new Direction over Network
				client.send(id, PayloadType.DIRECTION, mp.x, mp.y);
			}
			
			if(Player.SPACE_DOWN)
			{
				Player.SPACE_DOWN = false;
				client.send(id, PayloadType.BULLET);
			}
		
			}
		}

	public void addPlayer(int id, Player player) {
		if(!players.containsKey(id)) {
			System.out.println("Added " + id + " with name " + player.getName());
			players.put(id, player);
		}
		else {
			System.out.println(id + " with name " + player.getName() + " already connected");
		}
	}
	public Player removePlayer(int id) {
		return players.remove(id);
	}
	public int getIdByIndex(int index) {
		int i = 0;
		for(int id : players.keySet()) {
			if(i == index) {
				return id;
			}
			i++;
		}
		return -1;
	}
	public Player getPlayerByIndex(int index) {
		int i = 0;
		for(Player p : players.values()) {
			if(i == index) {
				return p;
			}
			i++;
		}
		return null;
	}
	public Player getPlayer(int id) {
		return players.get(id);
	}
	public void movePlayers() {
		for ( Player v : players.values() ) {
		    v.move();
		}
	}

	public void paintBullet(Graphics2D g2d) {
		for ( Player v : players.values() ) {
		    v.paintPlayer(g2d);
		}
	}
	public void paintPlayers(Graphics2D g2d) {
		for ( Player v : players.values() ) {
		    v.paintPlayer(g2d);
		}
	}
	public void updatePlayers(int id, PayloadType type, int x, int y, String name) {
		Player player = null;
		if(players.containsKey(id)) {
			player = players.get(id);
			switch(type) {
				case DIRECTION:
					//update player direction
					if(player != null) {
						player.setDirection(x, y);
					}
					break;
				case MOVE_SYNC:
					//update player position
					if(player != null) {
						player.setPosition(x, y);
					}
					break;
				default:
					break;
			}
		}
	}
	public static int calculateDistanceBetweenPoints(
			  Point a, 
			  Point b) {       
	    return (int)Math.sqrt((b.y - a.y) * (b.y - a.y) + (b.x - a.x) * (b.x - a.x));
	}
	public Entry<Integer,Player> checkCollisions(Player p) {
		Entry<Integer,Player> tagged = null;
		synchronized(players) {
			for(Entry<Integer, Player> set : players.entrySet()) {
				if(p.getID() != set.getKey()) {
					System.out.println("Tagger ID: " + p.getID());
					System.out.println("Checking ID: " + set.getKey());
					//get distance between centers
					int dist = calculateDistanceBetweenPoints(p.getPosition(), set.getValue().getPosition());
					//System.out.println("Dist: " + dist);
					//calculate expected distance based on radius
					int rad = (p.getRadius()+set.getValue().getRadius());
					//System.out.println("Rad: " + rad);
					//check if point is within range
					if(dist <= rad) {
						tagged = set;
						break;
					}
				}
			}
		}
		return tagged;
	}
	public Player getID(int i) {
		// TODO Auto-generated method stub
		return null;
	}
}

/*class Bullet
{
	public Bullet()
	{
		
	}
} */
