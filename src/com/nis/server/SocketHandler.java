package com.nis.server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class SocketHandler extends Thread {
	private Socket clientSocket;
	
	public SocketHandler(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}
	
	public void run() {
		try {
	        BufferedReader inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			DataOutputStream outToClient = new DataOutputStream(clientSocket.getOutputStream());
			String clientSentence = inFromClient.readLine();
			System.out.println("Received: " + clientSentence);
			String capitalizedSentence = clientSentence.toUpperCase() + '\n';
			outToClient.writeBytes(capitalizedSentence);
		} catch (Exception e) {
			
		} 
	}

}
