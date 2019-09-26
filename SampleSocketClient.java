package com.example.sockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class SampleSocketClient 
{
	Socket server;
	public SampleSocketClient(){}
	
	public void connect(String address, int port)
	{
		try 
		{
			server = new Socket(address, port);
			System.out.println("Client connected");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	public void start()
	{
		if(server == null)
		{
			System.out.println("Server is not set?");
			return;
		}
		System.out.println("Listening for console input...");
		
		try(Scanner si = new Scanner(System.in);
			PrintWriter out = new PrintWriter(server.getOutputStream(), true);	
			BufferedReader in = new BufferedReader(new InputStreamReader(server.getInputStream()));)
		{
			String line = "";
			while(true)
			{
				try
				{
					System.out.println("Waiting for user input..");
					System.out.println("Enter integers: ");
					line = si.nextLine();
					if(!"quit".equalsIgnoreCase(line))
					{
						out.println(line);
					}
					else
					{
						break;
					}
					line = "";
					String fromServer = in.readLine();
					
					if(fromServer != null) {
						System.out.println("Reply from server: " + fromServer);
					}
					else {
						System.out.println("Server disconnected");
						break;
					}
				}
				catch(Exception e)
				{
					System.out.println("Connection dropped");
					break;
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			close();
		}
	}
	
	private void close() 
	{
		if(server != null) 
		{
			try 
			{
				server.close();
				System.out.println("Closed socket");
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args)
	{
		SampleSocketClient client = new SampleSocketClient();
		int port = -1;
		
		try
		{
			port = Integer.parseInt(args[0]);
		}
		catch(Exception e)
		{
			System.out.println("Invalid Port");
		}
		if(port == -1)
		{
			return;
		}
		client.connect("127.0.0.1", port);
		try
		{
			client.start();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
