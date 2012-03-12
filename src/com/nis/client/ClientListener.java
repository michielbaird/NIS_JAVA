package com.nis.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import javax.net.ServerSocketFactory;

public class ClientListener extends Thread {
	
	private ServerSocket serverSocket;
	private SessionHandler sessionHandler;
	
	public ClientListener(SessionHandler sessionHandler, int port) throws IOException {
		serverSocket = ServerSocketFactory.getDefault().createServerSocket(port);
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
