package com.nis.server;

import java.net.ServerSocket;
import java.net.Socket;


public class Server {
	private static ServerSocket incomingSocket;
	
	public static void main(String [] argv) {
		//Initialise Key Map;
		ServerInfo serverInfo = new ServerInfo();
		System.out.println("Starting server hosted on port 8081");
		try {
			incomingSocket = new ServerSocket(8081);
		} catch (Exception e) {
			
		}
		while (true) {
			try {
				Socket clientSocket =  incomingSocket.accept();
				System.out.println("Starting connection with " + clientSocket.getInetAddress());
				SocketHandler handler = new SocketHandler(clientSocket, serverInfo);
				handler.start();
			} catch (Exception e) {
				
			}
		}
	}
}
