package com.nis.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Scanner;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.google.gson.Gson;
import com.nis.shared.Base64Coder;
import com.nis.shared.UserData;

public class ServerInfo {
	public static final String KEYFILE = "server.keys";
	
	private final HashMap<String, UserData> userList;
	private final Gson gson;
	private final HashMap<String, SecretKey> userKeys;
	
	public ServerInfo() {
		userList =  new HashMap<String, UserData>();
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
			byte[] key_bytes = Base64Coder.decode(encoded_key);
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
	
	public void addUser(String handle, String address, int port, String publicKey) {
		userList.put(handle, new UserData(address, port, publicKey));
	}
	
	public String getUserListJson() {
		return gson.toJson(userList);
	}
	
	public SecretKey getUserKey(String handle){
		return userKeys.get(handle);
	}
}
