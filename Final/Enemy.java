import java.awt.Graphics2D;
import java.awt.Point;
import java.util.Random;

public class Enemy 
{
	Random r = new Random();
	public Point center = new Point();
	public Point direction = new Point();
	public int radius = 25;
	public int diameter = 2*radius;
	public int dx = 5;
	public int dy = 5;
	
	public Enemy()
	{
	//	diameter = radius * 2; 
		center.x = r.nextInt(950 - 50 + 1) + 50;
		center.y = 0;
	//	r.nextInt(975 - 950 + 1) + 950;
	//	direction.x = 1;
	//	direction.y = 1;
		direction.x = 0;
		direction.y = 1;
	//	speed = 1;
	}
	//  x += speed * sin(angle);
	  //  y += speed * cos(angle);
	
	public void moveEnemy()
	{
		
		center.x += (direction.x*dx);
		center.y += (direction.y*dy);
	//	repaint();
		
	}
	public Point getPosition() {
		return center;
	}
	public void setDirection(int x, int y) {
		direction.x = x;
		direction.y = y;
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
	protected void paintEnemy(Graphics2D g2d) {
    	g2d.fillOval(center.x - radius, center.y - radius, diameter, diameter);
    	
    	
}

	//int count = 0;
	public void updateEnemy(int id, PayloadType type, int x, int y) {
	//	Player player = null;
	//	if(players.containsKey(id)) {
	//		player = players.get(id);
		//Enemy enemy = null;
		//	switch(type) {
			//	case DIRECTION:
			//		//update player direction
			//		if(enemy != null) {
			//			enemy.setDirection(x, y);
			//		}
			//		break;
			//	case MOVE_SYNC:
			//		//update player position
			//		if(enemy != null) {
		
	//	count++;
	//	if(count > 20)
	//	{
	//		count = 0;
			setPosition(x, y);
			
	//	}
						
			//		}
				//	break;
	//			default:
	//				break;
			}
		
	
	
	
	
}


//center.x += direction.x*speed