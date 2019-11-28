import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class UI extends JPanel {
	
	private static final long serialVersionUID = 2L;
	
	static Dimension game = new Dimension(1000, 1000);
	GameEngine gameEngine = null;
//	private String host;
//	private int port;
	static HashMap<String, Component> components = new HashMap<String, Component>();
	
	public void connectGUI()
	{
        JFrame frame = new JFrame("Connect");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(250, 175);

        JPanel panel = new JPanel(); 
        JLabel hl = new JLabel("Enter Host");
        JTextField ht = new JTextField(10); 
        JLabel pl = new JLabel("Enter Port");
        JTextField pt = new JTextField(10); 
        JButton send = new JButton("Send");
        
        send.addActionListener( new ActionListener()
        {
			@Override
			public void actionPerformed(ActionEvent e) 
			{
			//	SocketClient client = new SocketClient();
			//	Interaction interaction = new Interaction();
			//	String host;
			//	int port;
				
		//		if(ht.getText().equals(""))
			//	{
			//		ht.setText("Invalid");
		//		}
		//		if(Integer.parseInt(pt.getText()) <= -1)
		//		{
		//			pt.setText("Invalid");
		//		}
			//	else if(!ht.getText().equals("") && !(Integer.parseInt(pt.getText()) <= -1))
			//	{
				
				//	client.connect(host,port);
					
						try {
						//	String host;
							//int port;
							String host = ht.getText();
							int port = Integer.parseInt(pt.getText());
						//	interaction.connect(host,port);
							game(host,port);
							frame.setVisible(false);
							panel.setVisible(false);
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					
						// TODO Auto-generated catch block
					
					
			//	}
			}  	
        });
        JButton reset = new JButton("Reset");
        reset.addActionListener( new ActionListener()
        {
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				ht.setText("");
				pt.setText("");
			}  	
        });
        
        panel.add(hl, BorderLayout.NORTH);
        panel.add(ht, BorderLayout.NORTH); 
        panel.add(pl);
        panel.add(pt);
        panel.add(send);
        panel.add(reset);

        frame.getContentPane().add(BorderLayout.CENTER, panel);
        frame.setVisible(true);
	}
	public void toggleRunningState(boolean s) {
		if(gameEngine != null) {
			GameEngine.isRunning = s;
		}
	}
	void toggleComponent(String name, boolean toggle) {
		if(components.containsKey(name)) {
			components.get(name).setVisible(toggle);
		}
	}
	void ChangePanels(JFrame frame) {
		switch(GameEngine.gameState) {
			case GAME:
				toggleComponent("lobby", false);
				toggleComponent("game", true);
				break;
			case LOBBY:
				toggleComponent("lobby", true);
				toggleComponent("game", false);
				break;
			default:
				break;
		}
		frame.pack();
        frame.revalidate();
        frame.repaint();
	}
	
/*	public void game()
	{
		System.out.println("hi");
		JFrame gamerArea = new JFrame("Survival");
	    gamerArea.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    gamerArea.setSize(500, 500);
	
	    JPanel panel = new UI();
	    panel.setBorder(BorderFactory.createLineBorder(Color.black));
		panel.setVisible(true);
		panel.setPreferredSize(new Dimension(400,400));
		gamerArea.add(panel);
		gamerArea.pack();
		gamerArea.setVisible(true); 
	    gamerArea.getContentPane().add(BorderLayout.NORTH, panel);
	    
	    ((UI) panel).initialize();
	} */
	public void game(String host,int port) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException ex) {
		} catch (InstantiationException ex) {
		} catch (IllegalAccessException ex) {
		} catch (UnsupportedLookAndFeelException ex) {
		}
		JFrame frame = new JFrame("Survival");
		JPanel gameCanvas = new UI();
		components.put("game", gameCanvas);
		gameCanvas.setMaximumSize(game);
		gameCanvas.setSize(game);
		gameCanvas.setPreferredSize(game);
		gameCanvas.setBorder(BorderFactory.createLineBorder(Color.black));
		gameCanvas.setVisible(false);
		
		JPanel scores = new JPanel();
		JTextArea f = new JTextArea();
		scores.setLayout(new BorderLayout());
		scores.add(f, BorderLayout.PAGE_START);
		f.setPreferredSize(new Dimension(700, 30));
		f.setEditable(false);
		f.setFocusable(false);
		scores.setVisible(false);
		
		JPanel lobby = new JPanel();
		components.put("lobby", lobby);
		lobby.setName("lobby");
		lobby.setBorder(BorderFactory.createLineBorder(Color.black));
		JPanel container = new JPanel();
		JTextField name = new JTextField(20);
		name.setText("");
//		JTextField host = new JTextField(15);
//		host.setText("127.0.0.1");
//		JTextField port = new JTextField(4);
//		port.setText("3111");
		JButton start = new JButton();
		start.setText("Connect");
		JTextField message = new JTextField(60);
		message.setEditable(false); 
		
		components.put("lobby.message", message);
		
		start.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		        //your actions
		    	((UI) gameCanvas).initialize();
		    	GameEngine refGE = ((UI)gameCanvas).gameEngine;
		    	try {
		    		refGE.doSocketConnect(host,port);
		    		System.out.println("Connected");
		    		((JTextField)components.get("lobby.message")).setText("Connected, loading into game");
		    		
			    	int l = (name.getText().length() <7?name.getText().length():7);
			    	refGE.localPlayer.name = name.getText().substring(0, l);
			    	frame.setTitle("Survival - " + refGE.localPlayer.name);
			        try {
						Thread.sleep(50);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
			        gameCanvas.setBorder(BorderFactory.createLineBorder(Color.black));
			        GameEngine.gameState = GameState.GAME;
			    	((UI)gameCanvas).ChangePanels(frame);
			        ((UI)gameCanvas).toggleRunningState(true);
			        refGE.run();
			        
			    	gameCanvas.requestFocusInWindow();
			    	Player.setKeyBindings(gameCanvas.getInputMap(), gameCanvas.getActionMap());
			    	
		    	}
		    	catch(Exception ex) {
		    		GameEngine.isRunning = false;
		    		System.out.println("Client not connected");
		    		((JTextField)components.get("lobby.message")).setText("Failed to connect: " + ex.getMessage());
		    	}
		   }
		});
		container.add(name);
//		container.add(host);
//		container.add(port);
		container.add(start);
		lobby.setLayout(new BoxLayout(lobby, BoxLayout.PAGE_AXIS));
		lobby.add(container);
		lobby.add(message);
		frame.setLayout(new BorderLayout());
		frame.add(lobby, BorderLayout.SOUTH); 
		
		frame.add(gameCanvas, BorderLayout.CENTER);
		
		
		frame.pack();
		frame.setVisible(true);
		GameEngine.gameState = GameState.LOBBY;
		((UI)gameCanvas).ChangePanels(frame);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				// do something
				((UI)gameCanvas).toggleRunningState(false);
				try {
					((UI)gameCanvas).gameEngine.CloseConnection();
				}
				catch(Exception ex) {
					System.out.println("Game Engine was null, safe to ignore");
				}
				try {
					Thread.sleep(50);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				System.exit(0);
			}
		});
		
		
	} 
		
	
	

	boolean flagRenderHint = false;
	Graphics2D g2d;
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g2d = (Graphics2D) g;
		if(!flagRenderHint) {
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		if(gameEngine != null) {
			gameEngine.paint(g2d);
		}
		g2d.dispose();
	}
	
	public void showScores(String str) {
		if(components.containsKey("score")) {
			((JTextArea)components.get("score")).setText(str);
		}
	}
	public void initialize() {
		gameEngine = new GameEngine(this, game);
	}
}

class MoveAction extends AbstractAction{
	private static final long serialVersionUID = 2L;
	int x,y;
	boolean pressed = false;
	MoveAction(boolean pressed, int x, int y){
		this.x = x;
		this.y = y;
		this.pressed = pressed;
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		if (x == -1) {
			Player.LEFT_DOWN = pressed;
		}
		if (x == 1) {
			Player.RIGHT_DOWN = pressed;
		}
		if (y == -1) {
			Player.UP_DOWN = pressed;
		}
		if (y == 1) {
			Player.DOWN_DOWN = pressed;
		}
	}
	
}
class Interaction {
	SocketClient client;
	UI ui;
	public Interaction() {
		
	}
	public void connect(String host, int port) throws IOException{
		//thread just so we don't lock up main UI
		Thread connectionThread = new Thread() {
			@Override
			public void run() {
				ui = new UI();
	//			ui.game(host,port);
		//		client = new SocketClient();
		//		try {
		//			client.connect(host, port);
		//			client.start();
		//		} catch (IOException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}//this terminates when client is closed
		//		
		//		System.out.println("Connection thread finished");
			}
		};
		connectionThread.start();
	}
	
	
}
