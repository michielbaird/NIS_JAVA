package com.nis.client;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.internal.Pair;

public class SessionHandler {
	private final HashMap<String, Pair<Integer,Integer> > nonceMapHandler;
	private final HashMap<String, String> sessionKeys;
	private final Random random;
	private final Gson gson;
	
	private Map<String,Object> userList;
	private String clientHandle;

	public SessionHandler() {
		nonceMapHandler = new HashMap<String, Pair<Integer, Integer>>();
		sessionKeys = new HashMap<String, String>();
		random = new Random();
		gson = new Gson();
		userList = null;
	}
	
	public int getNonceB(String handle, int nonceA) {
		if (sessionKeys.containsKey(handle)){
			sessionKeys.remove(handle);
		}
		if (nonceMapHandler.containsKey(handle)) {
			nonceMapHandler.remove(handle);
		}
		int nounceB = random.nextInt();
		nonceMapHandler.put(handle, new Pair<Integer,Integer>(nonceA,nounceB));
		return nounceB;
	}

	@SuppressWarnings("unchecked")
	public void addUserList(String userListJson) {
		userList = gson.fromJson(userListJson, Map.class);
		System.err.println(userList);
	}
	
	@SuppressWarnings("unchecked")
	public Map<String,Object> getPeerAddress(String handle) {
		if (handle != clientHandle && userList != null &&
					userList.containsKey(handle)) {
			@SuppressWarnings("rawtypes")
			Map address = (Map)userList.get(handle);
			return (Map<String,Object>)address;
		} else {
			return null;
		}
	}

	public void addActiveUser(String handle, String hostName, int port) {
		Map<Object,Object> entry = new HashMap<Object,Object>();
		entry.put("addr", hostName);
		entry.put("port", new Double(port));
		
		userList.put(handle, entry);
		System.err.println(userList);
	}
	
	public boolean hasUserList() {
		return userList != null;
	}

	public Set<String> getClientList() {
		return userList.keySet();
	}

}
