package com.nis.client;

import com.google.gson.Gson;
import com.nis.client.handlers.ClientWaveHandler;
import com.nis.client.handlers.HelloHandler;
import com.nis.shared.Request;
import com.nis.shared.Response;
import com.nis.client.SessionHandler;

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;


public class RequestHandler extends Thread {
	public static HashMap<String, Class<? extends Handle>> callMap
		= new HashMap<String,Class<? extends Handle> >();
	static {
		callMap.put("hello", HelloHandler.class);
		callMap.put("client_wave",ClientWaveHandler.class);
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
		char buf[] = new char[buf_size];
		int ret;
		try {
			BufferedReader inFromClient = new BufferedReader(new 
					InputStreamReader(clientSocket.getInputStream()));
			PrintWriter outToClient = new PrintWriter(
					clientSocket.getOutputStream());
		    CharArrayWriter data = new CharArrayWriter();
		    
		    while ((ret = inFromClient.read(buf, 0, buf_size)) != -1)
		    {
		      data.write(buf, 0, ret);
		      if (buf[ret-1] == 0) {
		    	  break;
		      }
		    }
		    String receiveString = data.toString().trim();
		    
		    Request request = gson.fromJson(receiveString, Request.class);
		    String method = request.method;
		    Class<? extends Handle> handleType 
		    	= RequestHandler.callMap.get(method);
		    InetSocketAddress address = new InetSocketAddress(clientSocket.getInetAddress(),
					clientSocket.getPort());
		    Handle handle = handleType.newInstance();
		    String result = handle.handle(sessionHandler, request.params, address);
		    
		    Response response = new Response(result, request.id, 0);
		    outToClient.write(gson.toJson(response));
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
