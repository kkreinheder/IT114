package com.example.sockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class SampleSocketServer 
{
	
	public SampleSocketServer() 
	{
	}
	public void start(int port)
	{
	//	this.port = port;
		System.out.println("Waiting for client");
		try (ServerSocket serverSocket = new ServerSocket(port);
				Socket client = serverSocket.accept();
				PrintWriter out = new PrintWriter(client.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));) 
		{
			System.out.println("Client connected, waiting for message");
			String fromClient = ""; 
			int toClient =0;
			
			while((fromClient = in.readLine()) != null)
			{
				System.out.println("Message from client: " +fromClient);
			//	List<String> reversedInput = Arrays.asList(fromClient.split(""));
			//	Collections.reverse(reversedInput);
			//	toClient = String.join("", reversedInput);
			
				String[] nums = fromClient.split(" ");
				int[] n1 = new int[nums.length];
				for(int n = 0; n < nums.length; n++) 
				{
				   n1[n] = Integer.parseInt(nums[n]);
				}
				
				int max = 0;
				for(int i=0; i<n1.length; i++)
				{
					if(n1[i] > max)
					{
						max = n1[i];
					}
				} 
				toClient = max;
				System.out.println("Sending to client: ");
				
				if("kill server".equalsIgnoreCase(fromClient))
				{
					System.out.println("Client killed server process");
					break;
				}
				else
				{
					out.println("Largest number: " +toClient);
				}
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				System.out.println("Closing server socket");
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args)
	{
		System.out.println("Starting Server");
		SampleSocketServer server = new SampleSocketServer();
		int port = -1;
		if(args.length > 0)
		{
			try
			{
				port = Integer.parseInt(args[0]);
			}
			catch(Exception e)
			{
				System.out.println("Invalid port: " + args[0]);
			}
		}
		if(port > -1)
		{
			System.out.println("Server listening on port: " + port);
			server.start(port);
		}
		System.out.println("Server Stopped");
	}
}
