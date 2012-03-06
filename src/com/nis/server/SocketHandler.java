package com.nis.server;

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;

import com.google.gson.Gson;
import com.nis.server.handlers.GetSessionKeyHandler;
import com.nis.shared.Request;
import com.nis.shared.Response;


public class SocketHandler extends Thread {
	public static HashMap<String, Class<? extends Handle>> callMap
	= new HashMap<String,Class<? extends Handle> >();
	static {
		callMap.put("get_session_key", GetSessionKeyHandler.class);
		
	}
	private final static int buf_size = 4096;
	
	private Socket clientSocket;
	
	public SocketHandler(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}
	
	public void run() {
		try {
			Gson gson = new Gson();
			char buf[] = new char[buf_size];
			int ret;
			
	        BufferedReader inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			DataOutputStream outToClient = new DataOutputStream(clientSocket.getOutputStream());
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
	    		= SocketHandler.callMap.get(method);
			Handle handle = handleType.newInstance();
			String result = handle.handle(request.params);
			
			Response response =  new Response(result, request.id, 0);
			outToClient.writeBytes(gson.toJson(response));
			outToClient.flush();
			outToClient.close();
			
		} catch (Exception e) {
			
		} 
	}

}
