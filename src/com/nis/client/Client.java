package com.nis.client;

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;

import com.google.gson.Gson;
import com.nis.shared.Request;
import com.nis.shared.Response;
import com.nis.shared.SessionHandler;
import com.nis.shared.requests.Hello;
import com.nis.shared.requests.Ping;
import com.nis.shared.response.HelloResult;

public class Client {
	private final static int buf_size = 4096;
	private SessionHandler sessionHandler;
	private ClientListener clientListener;
	private String clientHandle;
	private Gson gson;
	private Random random;
	private int id;
	
	public Client(String handle, int port) {
		this.id = 1;
		this.clientHandle = handle;
		this.sessionHandler = new SessionHandler();
		this.gson = new Gson();
		this.random = new Random();
		try {
			this.clientListener = new ClientListener(sessionHandler, port);
		} catch (IOException e) {
			System.err.println("Failed to set up client listener.");
			System.exit(1);
		}
		clientListener.run();
	}
	
	public void Handshake(String address, int port, String handle) {
		int nonceA = random.nextInt();
		int nonceB = sayHello(address, port, nonceA);
		
		
	}
	
	private int sayHello(String address, int port, int nonceA) {
		Hello hello = new Hello(clientHandle, nonceA);
		String result = sendRequest(address, port, "hello",
				gson.toJson(hello));
		HelloResult helloResult = gson.fromJson(result, HelloResult.class);
		return helloResult.nonce;
		
	}
	
	private String sendRequest(String address, int port, 
			String method, String params) {
		char buf[] = new char[buf_size];
		int ret;
		Request request = new Request(method, id, params);
		String result = null;
		try {
			Socket clientSocket = new Socket(address, port);
			PrintWriter outToClient = new PrintWriter(clientSocket.getOutputStream(), true);
			BufferedReader inFromClient = new BufferedReader(
					new InputStreamReader(clientSocket.getInputStream()));
			outToClient.write(gson.toJson(request) + "\0");
			
			CharArrayWriter data = new CharArrayWriter();
			while ((ret = inFromClient.read(buf, 0, buf_size)) != -1)
		    {
		      data.write(buf, 0, ret);
		    }
			String receiveString = data.toString();
		  	Response response = gson.fromJson(receiveString, Response.class);
		  	clientSocket.close();
		  	if (response.id != id++) {
		  		// ThrowID Mismatch.
		  	} else if (response.error.equals(0)) {
		  		// Throw Error.
		  	}
		  	result = response.result;
		  	
		} catch (IOException e) {
		  
		}
		return result;
	}
	
	
	public static void main(String argv[]) throws Exception
	 {
	  Ping ping =  new Ping();
	  Gson gson = new Gson();
	  char buf[] = new char[buf_size];
	  int ret;
	  Request request = new Request("ping", 1, gson.toJson(ping));
	  
	  Socket clientSocket = new Socket("localhost", 8081);
	  PrintWriter outToServer = new PrintWriter(clientSocket.getOutputStream(), true);
	  BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	  
	  outToServer.write(gson.toJson(request) +"\0");
	  outToServer.flush();
	  //outToServer.close();

	  
	  CharArrayWriter data = new CharArrayWriter();
	  while ((ret = inFromServer.read(buf, 0, buf_size)) != -1)
	    {
	      data.write(buf, 0, ret);
	    }
	  String receiveString = data.toString();
	  
	  Response response = gson.fromJson(receiveString, Response.class);
	  if (response.error.equals(0)) {
		  System.out.println("FROM SERVER: PONG");
	  }
	  clientSocket.close();
	 }
}
