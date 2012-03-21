package com.nis.server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;

import com.google.gson.Gson;
import com.nis.server.handlers.GetSessionKeyHandler;
import com.nis.server.handlers.WaveHandler;
import com.nis.shared.ErrorMessages;
import com.nis.shared.Request;
import com.nis.shared.Response;

public class SocketHandler extends Thread {
	public static HashMap<String, Class<? extends Handle>> callMap
		= new HashMap<String,Class<? extends Handle> >();
	static {
		callMap.put("get_session_key", GetSessionKeyHandler.class);
		callMap.put("wave", WaveHandler.class);
	}

	private final Socket clientSocket;
	private final ServerInfo serverInfo;
	
	public SocketHandler(Socket clientSocket, ServerInfo serverInfo) {
		this.clientSocket = clientSocket;
		this.serverInfo = serverInfo;
	}
	
	public void run() {
		try {
			Gson gson = new Gson();

			BufferedReader inFromClient = new BufferedReader(
					new InputStreamReader(clientSocket.getInputStream()));
			DataOutputStream outToClient = new DataOutputStream(
					clientSocket.getOutputStream());
			String receiveString;
			receiveString = inFromClient.readLine();

			Request request = gson.fromJson(receiveString, Request.class);
			String checkSignature = request.from + request.method + request.id 
					+ request.params;
			// TODO(henkjoubert): Verify the signature.
			boolean isSignatureValid = true;
			Response response;
			if (isSignatureValid) {
				String method = request.method;
				Class<? extends Handle> handleType 
					= SocketHandler.callMap.get(method);

				InetSocketAddress incoming = new InetSocketAddress(
						clientSocket.getInetAddress(), clientSocket.getPort());

				Handle handle = handleType.newInstance();
				String result = handle.handle(request.params, incoming, serverInfo);

				response =  new Response(result, request.id, ErrorMessages.NoError);
			} else {
				response = new Response("", request.id, ErrorMessages.SignatureMismatch);
			}
			outToClient.writeBytes(gson.toJson(response) + "\n");
			outToClient.flush();
			outToClient.close();

		} catch (Exception e) {
			
		} 
	}

}
