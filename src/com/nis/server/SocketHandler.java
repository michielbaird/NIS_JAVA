package com.nis.server;

import com.google.gson.Gson;
import com.nis.server.handlers.PingHandler;
import com.nis.shared.Request;
import com.nis.shared.Response;
import com.nis.shared.requests.Ping;
import com.nis.shared.response.PingResult;

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;


public class SocketHandler extends Thread {
	
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
			
			String result="";
			int error = 1;
			if (request.method.equals("ping")) {
			   PingResult rawResult = PingHandler.handle(gson.fromJson(request.params, Ping.class));
			   result = gson.toJson(rawResult);
			   error = 0;
			} 
			Response response =  new Response(result, request.id, error);
			outToClient.writeBytes(gson.toJson(response));
			outToClient.close();
			
		} catch (Exception e) {
			
		} 
	}

}
