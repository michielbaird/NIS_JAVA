package com.nis.client;

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.google.gson.Gson;
import com.nis.shared.Request;
import com.nis.shared.Response;
import com.nis.shared.requests.Ping;

public class Client {
	private final static int buf_size = 4096;
	
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
