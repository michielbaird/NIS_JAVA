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
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.SocketFactory;
import javax.xml.bind.DatatypeConverter;

import com.google.gson.Gson;
import com.nis.client.ClientCallbacks.ConfirmResult;
import com.nis.client.DataTransferCallback.TransferParameters;
import com.nis.shared.Base64Coder;
import com.nis.shared.ErrorMessages;
import com.nis.shared.Request;
import com.nis.shared.Response;
import com.nis.shared.SessionKeyEnvelope;
import com.nis.shared.interactive.SendFileConfirm;
import com.nis.shared.requests.ClientKeyRequest;
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
import com.nis.shared.Crypter;

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

	public Client(String address, int port, String serverAddress, int serverPort,
			ClientCallbacks callbacks, ClientKeys clientKeys) {
		this.id = 1;
		this.clientPort = port;
		this.clientAddress = address;
		this.clientHandle = clientKeys.handle;
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
		this.sessionHandler = new SessionHandler(callbacks, clientKeys);
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
		Wave wave =  new Wave(clientHandle,clientAddress,clientPort,
				new String( Base64Coder.encode(sessionHandler.getPubKey().getEncoded())) );
		String result = sendRequest(serverAddress, serverPort, "wave",
				gson.toJson(wave), null, null);
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
		String publicKey = new String(Base64Coder.encode(sessionHandler.getPubKey().getEncoded()));
		for (Object client : sessionHandler.getClientList()) {
			String handle = (String)client;
			if (!handle.equals(clientHandle)) {
				Map clientAddress = sessionHandler.getPeerAddress(handle);
				if (clientAddress != null) {
					
					ClientWave wave = new ClientWave(clientPort, publicKey);
					sendRequest((String)clientAddress.get("address"), 
							((Double)clientAddress.get("port")).intValue(),
							"client_wave", gson.toJson(wave), null, handle);
				}
			}
		}
	}

	public void sendFileToClient(final String handle, String fileName) {
		if (!sessionHandler.hasKey(handle)) {
			handshake(handle);
		}
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
						
						Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
						byte [] iv_bytes =	{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
						IvParameterSpec ivspec = new IvParameterSpec(iv_bytes);
						c.init(Cipher.ENCRYPT_MODE, sessionHandler.getKey(handle), ivspec);
						CipherOutputStream cos = new CipherOutputStream(parameters.clientSocket.getOutputStream(), c);
						while (fileSizeRemaining > 0) {
							int readSize = fileSizeRemaining > buf_size ? 
									buf_size : (int)fileSizeRemaining;
							fileSizeRemaining -= readSize;
							bis.read(byteArray,0,readSize);
							cos.write(byteArray,0,readSize);
							// TODO(michielbaird): Add progress callback.
						}
						cos.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				} catch (NoSuchPaddingException e) {
					e.printStackTrace();
				} catch (InvalidKeyException e) {
					e.printStackTrace();
				} catch (InvalidAlgorithmParameterException e) {
					e.printStackTrace();
				}
			}
		};
		Map address = sessionHandler.getPeerAddress(handle);
		sendRequest((String)address.get("address"), 
				((Double)address.get("port")).intValue(),
				"send_file", gson.toJson(sendFile), callback, handle);
		System.out.println("File transfer successful.");
	}

	public void handshake(String handle) {
		Map<String,Object> clientAddress = sessionHandler.getPeerAddress(handle);
		if (clientAddress != null) {
			int nonceA = random.nextInt();
			int nonceB = sayHello(handle, (String)clientAddress.get("address"),
					((Double)clientAddress.get("port")).intValue(), nonceA);
			GetSessionKeyResult getSessionKeyResult = getKey(handle, 
					nonceA, nonceB);
			String jsonstring = Crypter.decrypt(getSessionKeyResult.encryptedKeyA, sessionHandler.getMasterKey());
			SessionKeyEnvelope env = gson.fromJson(jsonstring, SessionKeyEnvelope.class);
			SecretKey key = null;
			if (env.nonceA == nonceA && env.nonceB == nonceB) {
				byte [] key_bytes = Base64Coder.decode(env.encodedSessionKey);
				key = new SecretKeySpec(key_bytes, "AES");
				sessionHandler.addKey(handle, key);
				ClientKeyRequest request = new ClientKeyRequest(getSessionKeyResult.encryptedKeyB);
				sendRequest((String)clientAddress.get("address"),((Double)clientAddress.get("port")).intValue(),
						"client_key",gson.toJson(request),null, handle);
			}
		}
		return;
	}
	
	public void sendMessage(String handle, String message) {
		Map<String,Object> clientAddress = sessionHandler.getPeerAddress(handle);
		if (clientAddress != null) {
			SecretKey key = null;
			if (sessionHandler.hasKey(handle)) {
				key = sessionHandler.getKey(handle);
			} else {
				handshake(handle);
				key = sessionHandler.getKey(handle);
			}
			Message messageRequest = new Message(
					Crypter.encrypt(message, key) ); //LOOK I am encrypted now
			sendRequest((String)clientAddress.get("address"), 
					((Double)clientAddress.get("port")).intValue(),
					"client_message", gson.toJson(messageRequest), null, handle);
		}
	}

	private int sayHello(String handle, String address, int port, int nonceA) {
		Hello hello = new Hello(nonceA);
		String result = sendRequest(address, port, "hello",
				gson.toJson(hello),null, handle);
		HelloResult helloResult = gson.fromJson(result, HelloResult.class);
		return helloResult.nonce;
	}

	private GetSessionKeyResult getKey(String handle, int nonceA, int nonceB) {
		GetSessionKey getSessionKey = new GetSessionKey(clientHandle, 
				handle, nonceA, nonceB);
		String result = sendRequest(serverAddress, serverPort, 
				"get_session_key", gson.toJson(getSessionKey), null, null);
		GetSessionKeyResult getSessionKeyResult = gson.fromJson(result,
				GetSessionKeyResult.class);
		return getSessionKeyResult;
	}

	private String sendRequest(String address, int port, 
			String method, String params, DataTransferCallback callback, String handle) {
		Request request = new Request(clientHandle ,method, id, params);
		String result = null;
		try {
			Socket clientSocket = SocketFactory.getDefault().createSocket();
			clientSocket.connect(new InetSocketAddress(address,port));
			DataOutputStream outToHost = new DataOutputStream(clientSocket.getOutputStream());
			BufferedReader inFromHost = new BufferedReader(
					new InputStreamReader(clientSocket.getInputStream()));
			String signatureSrc = request.from + request.method + request.id 
					+ request.params;
			//sign
			Signature signature = Signature.getInstance("SHA1withRSA");
			signature.initSign(sessionHandler.getPrivKey());
			byte [] toBeSigned = signatureSrc.getBytes();
			signature.update(toBeSigned);
			byte [] theSig = signature.sign();
			String sigString = new String(Base64Coder.encode(theSig));
			request.signature = sigString;
			outToHost.writeBytes(gson.toJson(request) + "\n");
			outToHost.flush();
			if (callback != null){
				TransferParameters parameters = new TransferParameters(clientSocket, inFromHost, outToHost);
				callback.transferData(parameters);
				return "";
			}
			String receiveString = inFromHost.readLine();
			Response response = gson.fromJson(receiveString, Response.class);
			clientSocket.close();
			String verificationString = response.id + response.result;
			//verify the signature
			boolean isValidSignature = true;
			if (handle != null) {
				signature.initVerify(sessionHandler.getPublicKey(handle));
				byte [] toBeVerified = verificationString.getBytes();
				signature.update(toBeVerified);
				isValidSignature = signature.verify(Base64Coder.decode(response.signature));
			}
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
		
		} catch (NoSuchAlgorithmException e) {
			//we should have rsa
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		ClientCallbacks callbacks =  new ClientCallbacks() {
			
			@Override
			public ConfirmResult onIncomingFile(SendFile sendFile) {
				ConfirmResult result = new ConfirmResult();
				result.accept= true;
				result.fileName = "copy_" + sendFile.filename;
				return result;
			}
			
			@Override
			public void onFileReceived(String filename) {
				System.out.println("File Received: " + filename);
				
			}
			
			@Override
			public void onClientMessageRecieved(String handle, String message) {
				System.out.println(handle + ": " + message);
			}
			
			@Override
			public void onClientListReceived(Set<String> clientList) {
				System.out.println("Client List Received");
			}
		};
		Client client = new Client(localIP, localport,
				defaultServerAddress,defaultServerPort, callbacks, keys);
		while (true) {
			String option;
			String remoteHandle;
			System.out.print("Enter \"file\" or \"handshake\" or \"message\": ");
			option = scanner.next();
			System.out.print("Enter handle: ");
			remoteHandle = scanner.next();
			if (option.equals("handshake")) {
				client.handshake(remoteHandle);
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
