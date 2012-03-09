package com.nis.client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;

import com.google.gson.Gson;
import com.nis.client.Handle.HandleParameters;
import com.nis.client.handlers.ClientWaveHandler;
import com.nis.client.handlers.HelloHandler;
import com.nis.client.handlers.SendFileHandler;
import com.nis.shared.ErrorMessages;
import com.nis.shared.Request;
import com.nis.shared.Response;


public class RequestHandler extends Thread {
	public static HashMap<String, Class<? extends Handle>> callMap
		= new HashMap<String,Class<? extends Handle> >();
	static {
		callMap.put("hello", HelloHandler.class);
		callMap.put("client_wave",ClientWaveHandler.class);
		callMap.put("send_file", SendFileHandler.class);
	}

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
			BufferedReader inFromHost = new BufferedReader(new 
					InputStreamReader(clientSocket.getInputStream()));
			DataOutputStream outToHost = new DataOutputStream(
					clientSocket.getOutputStream());

		    String receiveString = inFromHost.readLine();
		    Request request = gson.fromJson(receiveString, Request.class);
		    Response response;
		    
		    String checkSignature = request.from + request.method + request.id 
		    		+ request.params;
		    // TODO(henkjoubert): Verify the signature.
		    boolean isSignatureValid = true;
		    if (isSignatureValid) {
			    String method = request.method;
			    Class<? extends Handle> handleType 
			    	= RequestHandler.callMap.get(method);
			    InetSocketAddress address = new InetSocketAddress(
			    		clientSocket.getInetAddress(), clientSocket.getPort());
			    Handle handle = handleType.newInstance();
			    HandleParameters parameters =  new HandleParameters(request.from,
			    		request.params, address, sessionHandler, inFromHost,
			    		clientSocket.getInputStream(), outToHost);
			    String result = handle.handle(parameters);
			    response = new Response(result, request.id, ErrorMessages.NoError);
		    } else {
		    	response = new Response("", request.id, ErrorMessages.SignatureMismatch);
		    }
		    outToHost.writeBytes(gson.toJson(response) + "\n");
		    outToHost.flush();
		    clientSocket.close();
		    
		} catch (IOException e) {
			
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	    
	}
}
