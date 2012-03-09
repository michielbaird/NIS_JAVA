package com.nis.client;

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import com.google.gson.Gson;
import com.nis.shared.Request;
import com.nis.shared.Response;
import com.nis.shared.requests.ClientWave;
import com.nis.shared.requests.GetSessionKey;
import com.nis.shared.requests.Hello;
import com.nis.shared.requests.Wave;
import com.nis.shared.response.GetSessionKeyResult;
import com.nis.shared.response.HelloResult;
import com.nis.shared.response.WaveResult;

public class Client {

	private final static int buf_size = 4096;
	private final static String serverAddress = "localhost";
	private final static int serverPort = 8081;
	
	private final int clientPort;
	private final SessionHandler sessionHandler;
	private ClientListener clientListener;
	private final String clientHandle;
	private final Gson gson;
	private final Random random;
	private int id;
	private final Timer timer;

	public Client(String handle, int port) {
		this.id = 1;
		this.clientPort = port;
		this.clientHandle = handle;
		this.sessionHandler = new SessionHandler();
		this.gson = new Gson();
		this.random = new Random();
		this.timer =  new Timer();
		try {
			this.clientListener = new ClientListener(sessionHandler, port);
		} catch (IOException e) {
			System.err.println("Failed to set up client listener.");
			System.exit(1);
		}
		clientListener.start();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				waveToServer();
			}
		}, 500);
	}

	private void waveToServer() {
		Wave wave =  new Wave(clientHandle,clientPort);
		String result = sendRequest(serverAddress, serverPort, "wave", gson.toJson(wave));
		WaveResult waveResult =  gson.fromJson(result, WaveResult.class);
		boolean waved = sessionHandler.hasUserList();
		sessionHandler.addUserList(waveResult.userListJson);
		if (!waved) {
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					waveToClients();
				}
			}, 100);
		}
		
	}

	private void waveToClients() {
		for (Object client : sessionHandler.getClientList()) {
			String handle = (String)client;
			if (!handle.equals(clientHandle)) {
				InetSocketAddress clientAddress = sessionHandler.getPeerAddress(handle);
				if (clientAddress != null) {
					ClientWave wave = new ClientWave(clientHandle, clientPort);
					sendRequest(clientAddress.getHostName(), 
							clientAddress.getPort(), "client_wave", gson.toJson(wave));
					
				}
			}
		}
	}

	public void Handshake(String handle) {
		InetSocketAddress clientAddress = sessionHandler.getPeerAddress(handle);
		if (clientAddress != null) {
			int nonceA = random.nextInt();
			int nonceB = sayHello(clientAddress.getHostName(), clientAddress.getPort(), nonceA);
			GetSessionKeyResult getSessionKeyResult = getKey(handle, 
					nonceA, nonceB);
		}
		return;
		
	}

	private int sayHello(String address, int port, int nonceA) {
		Hello hello = new Hello(clientHandle, nonceA);
		String result = sendRequest(address, port, "hello",
				gson.toJson(hello));
		HelloResult helloResult = gson.fromJson(result, HelloResult.class);
		return helloResult.nonce;
		
	}

	private GetSessionKeyResult getKey(String handle, int nonceA, int nonceB) {
		GetSessionKey getSessionKey = new GetSessionKey(clientHandle, 
				handle, nonceA, nonceB);
		String result = sendRequest(serverAddress, serverPort, 
				"get_session_key", gson.toJson(getSessionKey));
		GetSessionKeyResult getSessionKeyResult = gson.fromJson(result,
				GetSessionKeyResult.class);
		return getSessionKeyResult;
	}

	private String sendRequest(String address, int port, 
			String method, String params) {
		Request request = new Request(method, id, params);
		String result = null;
		try {
			Socket clientSocket = new Socket();
			clientSocket.connect(new InetSocketAddress(address,port));
			DataOutputStream outToHost = new DataOutputStream(clientSocket.getOutputStream());
			BufferedReader inFromHost = new BufferedReader(
					new InputStreamReader(clientSocket.getInputStream()));
			outToHost.writeBytes(gson.toJson(request) + "\n");
			outToHost.flush();
			String receiveString = inFromHost.readLine();
			Response response = gson.fromJson(receiveString, Response.class);
			clientSocket.close();
			if (response.id != id++) {
				// ThrowID Mismatch.
			} else if (response.error.equals(0)) {
				// Throw Error.
			}
			result = response.result;
			clientSocket.close();
			
		} catch (IOException e) {
		
		}
		return result;
	}

	public static void main(String argv[]) throws Exception
	 {
		Scanner scanner;
		scanner =  new Scanner(System.in);

		int localport;
		String handle;
		System.out.print("Enter the local port: ");
		localport = scanner.nextInt();
		System.out.println("port: " + localport);
		System.out.print("Enter the user handle: ");
		handle = scanner.next();
		System.out.println("handle: " + handle);
		Client client = new Client(handle, localport);

		while (true) {
			String remoteHandle;
			System.out.print("Enter the user handle: ");
			remoteHandle = scanner.next();
			client.Handshake(remoteHandle);
		}
	 }
}
