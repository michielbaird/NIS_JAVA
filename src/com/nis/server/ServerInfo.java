package com.nis.server;

import java.net.InetSocketAddress;
import java.util.HashMap;

import com.google.gson.Gson;

public class ServerInfo {
	public static final String KEYFILE = "server.keys";
	
	private final HashMap<String, InetSocketAddress> userList;
	private final Gson gson;
	
	public ServerInfo() {
		userList =  new HashMap<String, InetSocketAddress>();
		gson = new Gson();
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

}
