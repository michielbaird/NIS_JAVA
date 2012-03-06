package com.nis.client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientListener extends Thread {
	private final static int buf_size = 4096;
	
	private ServerSocket serverSocket;
	private SessionHandler sessionHandler;
	
	public ClientListener(SessionHandler sessionHandler, int port) throws IOException {
		serverSocket = new ServerSocket(port);
		this.sessionHandler = sessionHandler;
	}
	
	public void run() {
		try{
			while (true) {
				Socket clientSocket = serverSocket.accept();
				RequestHandler handler = new RequestHandler(clientSocket, sessionHandler);
				handler.start();
					
			}
		} catch(IOException e) {
			
		}
	}
}
