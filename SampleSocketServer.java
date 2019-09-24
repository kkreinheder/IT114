package com.example.sockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class SampleSocketServer 
{
	int port = 3002;
	
	public SampleSocketServer() 
	{
	}
	public void start(int port)
	{
		this.port = port;
		System.out.println("Waiting for client");
		try (ServerSocket serverSocket = new ServerSocket(port);
				Socket client = serverSocket.accept();
				PrintWriter out = new PrintWriter(client.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));) 
		{
			System.out.println("Client connected, waiting for message");
			String fromClient = "", toClient = "";
			
			while((fromClient = in.readLine()) != null)
			{
				System.out.println("Message from client: " +fromClient);
				if("kill server".equalsIgnoreCase(fromClient))
				{
					System.out.println("Client killed server process");
					break;
				}
				else
				{
					System.out.println("From Client: " + fromClient);
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
	public static void main(String[] arg)
	{
		System.out.println("Starting Server");
		SampleSocketServer server = new SampleSocketServer();
		server.start(3002);
		System.out.println("Server Stopped");
	}
}
