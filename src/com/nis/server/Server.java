package com.nis.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;


public class Server  {
	private static HashMap<String, String> keyMap;
	private static ServerSocket incomingSocket;
	
	public static void main(String [] argv) {
		//Initialise Key Map;
		keyMap = new HashMap<String, String>();
		try {
			incomingSocket = new ServerSocket(8081);
		} catch (Exception e) {
			
		}
		while (true) {
			try {
				Socket clientSocket =  incomingSocket.accept();
				SocketHandler handler =  new SocketHandler(clientSocket);
				handler.start();
			} catch (Exception e) {
				
			}
		}
	}

}
