package com.nis.client;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import javax.net.SocketFactory;

import com.google.gson.Gson;
import com.nis.client.DataTransferCallback.TransferParameters;
import com.nis.shared.ErrorMessages;
import com.nis.shared.Request;
import com.nis.shared.Response;
import com.nis.shared.interactive.SendFileConfirm;
import com.nis.shared.requests.ClientWave;
import com.nis.shared.requests.GetSessionKey;
import com.nis.shared.requests.Hello;
import com.nis.shared.requests.Message;
import com.nis.shared.requests.SendFile;
import com.nis.shared.requests.Wave;
import com.nis.shared.response.GetSessionKeyResult;
import com.nis.shared.response.HelloResult;
import com.nis.shared.response.SendFileResult;
import com.nis.shared.response.WaveResult;

public class Client {

	private final static int buf_size = 4096;
	private final static String defaultServerAddress = "localhost";
	private final static int defaultServerPort = 8081;
	
	private final String serverAddress;
	private final int serverPort;
	private final int clientPort;
	private final String clientAddress;
	private final SessionHandler sessionHandler;
	private ClientListener clientListener;
	private final String clientHandle;
	private final Gson gson;
	private final Random random;
	private int id;
	private final ClientKeys clientKeys;

	public Client(String address, int port, String serverAddress, int serverPort,
			ClientCallbacks callbacks, ClientKeys clientKeys) {
		this.id = 1;
		this.clientPort = port;
		this.clientAddress = address;
		this.clientHandle = clientKeys.handle;
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
		this.sessionHandler = new SessionHandler(callbacks);
		this.clientKeys = clientKeys;
		this.gson = new Gson();
		this.random = new Random();
		try {
			this.clientListener = new ClientListener(sessionHandler, port);
			this.clientListener.start();
		} catch (IOException e) {
			System.err.println("Failed to set up client listener.");
			System.exit(1);
		}
		waveToServer();
		
	}

	private void waveToServer() {
		Wave wave =  new Wave(clientHandle,clientAddress,clientPort);
		String result = sendRequest(serverAddress, serverPort, "wave",
				gson.toJson(wave), null);
		WaveResult waveResult =  gson.fromJson(result, WaveResult.class);
		boolean waved = sessionHandler.hasUserList();
		sessionHandler.addUserList(waveResult.userListJson);
		if (!waved) {
			waveToClients();
		}
		if (sessionHandler.getCallbacks() != null) {
			sessionHandler.getCallbacks().onClientListReceived(
					sessionHandler.getClientList());
		}
		
	}

	private void waveToClients() {
		for (Object client : sessionHandler.getClientList()) {
			String handle = (String)client;
			if (!handle.equals(clientHandle)) {
				Map clientAddress = sessionHandler.getPeerAddress(handle);
				if (clientAddress != null) {
					ClientWave wave = new ClientWave(clientPort);
					sendRequest((String)clientAddress.get("addr"), 
							((Double)clientAddress.get("port")).intValue(),
							"client_wave", gson.toJson(wave), null);
					
				}
			}
		}
	}

	public void sendFileToClient(String handle, String fileName) {
		final File file =  new File(fileName);
		SendFile sendFile = new SendFile(file.getName(), file.length());
		DataTransferCallback callback = new DataTransferCallback() {
			@Override
			public void transferData(TransferParameters parameters) {
				String response;
				try {
					response = parameters.inFromHost.readLine();
					SendFileConfirm confirm = gson.fromJson(response, SendFileConfirm.class);
					if (confirm.accept) {
						byte [] byteArray = new byte[buf_size];
						FileInputStream fis = new FileInputStream(file);
						BufferedInputStream bis = new BufferedInputStream(fis);
						long fileSizeRemaining = file.length();
						while (fileSizeRemaining > 0) {
							int readSize = fileSizeRemaining > buf_size ? 
									buf_size : (int)fileSizeRemaining;
							fileSizeRemaining -= readSize;
							bis.read(byteArray,0,readSize);
							parameters.clientSocket.getOutputStream().write(byteArray,0,readSize);
							// TODO(michielbaird): Add progress callback.
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		Map address = sessionHandler.getPeerAddress(handle);
		String result = sendRequest((String)address.get("addr"), 
				((Double)address.get("port")).intValue(),
				"send_file", gson.toJson(sendFile), callback);
		SendFileResult sendFileResult =  gson.fromJson(result, SendFileResult.class);
		if (sendFileResult.equals("sucess")) {
			System.out.println("File transfer successful.");
		}
	}

	public void Handshake(String handle) {
		Map<String,Object> clientAddress = sessionHandler.getPeerAddress(handle);
		if (clientAddress != null) {
			int nonceA = random.nextInt();
			int nonceB = sayHello((String)clientAddress.get("addr"),
					((Double)clientAddress.get("port")).intValue(), nonceA);
			GetSessionKeyResult getSessionKeyResult = getKey(handle, 
					nonceA, nonceB);
		}
		return;
	}
	
	public void sendMessage(String handle, String message) {
		Map<String,Object> clientAddress = sessionHandler.getPeerAddress(handle);
		if (clientAddress != null) {
			// TODO(jouberthenk): check key, negotiate key if not found.
			
			
			//TODO(jouberthenk): encrypt message.
			Message messageRequest = new Message(message);
			
			sendRequest((String)clientAddress.get("addr"), 
					((Double)clientAddress.get("port")).intValue(),
					"client_message", gson.toJson(messageRequest), null);
		}
	}

	private int sayHello(String address, int port, int nonceA) {
		Hello hello = new Hello(nonceA);
		String result = sendRequest(address, port, "hello",
				gson.toJson(hello),null);
		HelloResult helloResult = gson.fromJson(result, HelloResult.class);
		return helloResult.nonce;
	}

	private GetSessionKeyResult getKey(String handle, int nonceA, int nonceB) {
		GetSessionKey getSessionKey = new GetSessionKey(clientHandle, 
				handle, nonceA, nonceB);
		String result = sendRequest(serverAddress, serverPort, 
				"get_session_key", gson.toJson(getSessionKey), null);
		GetSessionKeyResult getSessionKeyResult = gson.fromJson(result,
				GetSessionKeyResult.class);
		return getSessionKeyResult;
	}

	private String sendRequest(String address, int port, 
			String method, String params, DataTransferCallback callback) {
		Request request = new Request(clientHandle ,method, id, params);
		String result = null;
		try {
			Socket clientSocket = SocketFactory.getDefault().createSocket();
			clientSocket.connect(new InetSocketAddress(address,port));
			DataOutputStream outToHost = new DataOutputStream(clientSocket.getOutputStream());
			BufferedReader inFromHost = new BufferedReader(
					new InputStreamReader(clientSocket.getInputStream()));
			outToHost.writeBytes(gson.toJson(request) + "\n");
			outToHost.flush();
			if (callback != null){
				TransferParameters parameters = new TransferParameters(clientSocket, inFromHost, outToHost);
				callback.transferData(parameters);
			}
			String receiveString = inFromHost.readLine();
			Response response = gson.fromJson(receiveString, Response.class);
			clientSocket.close();
			String verificationString = response.id + response.result;
			// TODO(henkjoubert): Verify the signature.
			boolean isValidSignature = true;

			
			if (response.id != id++) {
				// ThrowID Mismatch.
			} else if (response.error.equals(ErrorMessages.SignatureMismatch)) {
				// Throw something wrong error.
			} else if (!isValidSignature) {
				// Throw invalid signature.
			}
			result = response.result;
			clientSocket.close();
			
		} catch (IOException e) {
			System.out.print(e);
		
		}
		return result;
	}

	public static String getLocalIP() { 
		try {
			Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
			for (NetworkInterface interf = nets.nextElement();nets.hasMoreElements();) {
				for (Enumeration<InetAddress> a = interf.getInetAddresses();a.hasMoreElements();) {
					InetAddress addr = a.nextElement();
					if (!addr.isLoopbackAddress() && !addr.getHostAddress().contains(":")) {
						return addr.getHostAddress();
					}
				}
			}
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
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
		String localIP = getLocalIP();
		System.out.println(localIP);
		// get the client keys
		ClientKeys keys = null;
		String keyfile = null;
		try {
			keyfile = handle + ".key";
			FileInputStream fin = new FileInputStream(keyfile);
			ObjectInputStream keyin = new ObjectInputStream(fin);
			keys = (ClientKeys) keyin.readObject();
			keyin.close();
			fin.close();
		} catch (FileNotFoundException e) {
			System.err.println("Could not open " + keyfile);
			e.printStackTrace();
			System.exit(-1);
		}
		Client client = new Client(localIP, localport,
				defaultServerAddress,defaultServerPort, null, keys);

		while (true) {
			String option;
			String remoteHandle;
			System.out.print("Enter \"file\" or \"handshake\" or \"message\": ");
			option = scanner.next();
			System.out.print("Enter handle: ");
			remoteHandle = scanner.next();
			if (option.equals("handshake")) {
				client.Handshake(remoteHandle);
			} else if (option.equals("file")) {
				String fileName;
				System.out.print("Enter filename: ");
				fileName = scanner.next();
				client.sendFileToClient(remoteHandle, fileName);
			} else if (option.equals("message")) {
				System.out.print("Message: ");
				String message = "Hello it's a sucess";
				client.sendMessage(remoteHandle, message);
			}
			
		}
	 }
}
