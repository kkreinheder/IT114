import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Game extends JPanel
{
	static Dimension playArea = new Dimension(600, 600);
	
	public Game()
	{
		
	}
	public static void main(String[] args0)
	{
		JFrame frame = new JFrame("Game");
		JPanel gameCanvas = new Game();
		gameCanvas.setMaximumSize(playArea);
		gameCanvas.setSize(playArea);
		gameCanvas.setPreferredSize(playArea);
		gameCanvas.setBorder(BorderFactory.createLineBorder(Color.black));
		frame.setVisible(true);
		frame.add(gameCanvas);
		gameCanvas.setVisible(true);
	}
}
