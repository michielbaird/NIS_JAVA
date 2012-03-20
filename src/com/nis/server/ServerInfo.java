package com.nis.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Scanner;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import com.google.gson.Gson;

public class ServerInfo {
	public static final String KEYFILE = "server.keys";
	
	private final HashMap<String, InetSocketAddress> userList;
	private final Gson gson;
	private final HashMap<String, SecretKey> userKeys;
	
	public ServerInfo() {
		userList =  new HashMap<String, InetSocketAddress>();
		userKeys = new HashMap<String, SecretKey>();
		gson = new Gson();
		Scanner scanner = null;
		try {
			scanner = new Scanner(new File(KEYFILE));
		} catch (FileNotFoundException e) {
			System.err.println(KEYFILE + " could not be read");
			e.printStackTrace();
		}
		while (scanner.hasNext()) {
			String user = scanner.next();
			String encoded_key = scanner.next();
			byte[] key_bytes = DatatypeConverter.parseBase64Binary(encoded_key);
			//SecretKey is just an interface, the spec converts it into a usable key
			SecretKey key_spec = new SecretKeySpec(key_bytes, "AES");
			userKeys.put(user, key_spec);
		}
	}
	
	public void removeUser(String handle) {
		if (userList.containsValue(handle)) {
			userList.remove(handle);
		}
	}
	
	public void addUser(String handle, InetSocketAddress socketAddress) {
		userList.put(handle, socketAddress);
	}
	
	public String getUserListJson() {
		return gson.toJson(userList);
	}
	
	public SecretKey getUserKey(String handle){
		return userKeys.get(handle);
	}
}
