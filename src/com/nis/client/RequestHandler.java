package com.nis.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.util.HashMap;

import com.google.gson.Gson;
import com.nis.client.Handle.HandleParameters;
import com.nis.client.handlers.ClientWaveHandler;
import com.nis.client.handlers.HelloHandler;
import com.nis.client.handlers.MessageHandler;
import com.nis.client.handlers.SendClientKeyHandler;
import com.nis.client.handlers.SendFileHandler;
import com.nis.shared.Base64Coder;
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
		callMap.put("client_message",MessageHandler.class);
		callMap.put("client_key", SendClientKeyHandler.class);
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
			PrintWriter outToHost = new PrintWriter(
					clientSocket.getOutputStream());

		    String receiveString = inFromHost.readLine();
		    Request request = gson.fromJson(receiveString, Request.class);
		    Response response;
		    
		    String checkSignature = request.from + request.method + request.id 
		    		+ request.params;
		    // TODO(henkjoubert): Verify the signature.
		    boolean isSignatureValid = true;
		    Signature signature = Signature.getInstance("SHA1withRSA");
		    if (sessionHandler.hasKey(request.from)) {
		    	signature.initVerify(sessionHandler.getPublicKey(request.from));
		    	byte [] toBeVerified = checkSignature.getBytes();
		    	signature.update(toBeVerified);
		    	isSignatureValid = signature.verify(Base64Coder.decode(request.signature));
		    }
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
			    String result = null;
			    try {
			    	result = handle.handle(parameters);
			    } catch (DataTransferException e) {
			    	clientSocket.close();
			    	return;
			    }
			    response = new Response(result, request.id, ErrorMessages.NoError);
		    } else {
		    	response = new Response("", request.id, ErrorMessages.SignatureMismatch);
		    }
		    //sign response
		    String stringToBeSigned = response.id + response.result;
		    signature.initSign(sessionHandler.getPrivKey());
		    byte [] toBeSigned = stringToBeSigned.getBytes();
		    signature.update(toBeSigned);
		    byte [] theSig = signature.sign();
		    response.signature = new String(Base64Coder.encode(theSig));
		    String responseString = gson.toJson(response);
		    outToHost.write(responseString + "\n");
		    outToHost.flush();
		    clientSocket.close();
		    
		} catch (IOException e) {
			
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	}
}
