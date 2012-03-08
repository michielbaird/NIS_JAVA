package com.nis.client;

import com.google.gson.Gson;
import com.nis.client.handlers.HelloHandler;
import com.nis.shared.Request;
import com.nis.shared.Response;
import com.nis.client.SessionHandler;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;


public class RequestHandler extends Thread {
	public static HashMap<String, Class<? extends Handle>> callMap
		= new HashMap<String,Class<? extends Handle> >();
	static {
		callMap.put("hello", HelloHandler.class);
		
	}
	
	private final static int buf_size = 4096;
	
	private Socket clientSocket;
	private SessionHandler sessionHandler;
	
	public RequestHandler(Socket clientSocket, 
			SessionHandler sessionHandler) throws IOException {
		this.clientSocket = clientSocket;	
		this.sessionHandler = sessionHandler;
	}
	
	public void run() {
		
		Gson gson = new Gson();

		try {
			BufferedReader inFromClient = new BufferedReader(new 
					InputStreamReader(clientSocket.getInputStream()));
			DataOutputStream outToClient = new DataOutputStream(clientSocket.getOutputStream());

		    String receiveString = inFromClient.readLine();
		    
		    Request request = gson.fromJson(receiveString, Request.class);
		    String method = request.method;
		    Class<? extends Handle> handleType 
		    	= RequestHandler.callMap.get(method);
		    Handle handle = handleType.newInstance();
		    String result = handle.handle(sessionHandler, request.params);
		    
		    Response response = new Response(result, request.id, 0);
		    outToClient.writeBytes(gson.toJson(response) + "\n");
		    outToClient.flush();
		    clientSocket.close();
		    
		} catch (IOException e) {
			
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	    
	}
}
