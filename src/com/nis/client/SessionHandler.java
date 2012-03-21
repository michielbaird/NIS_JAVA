package com.nis.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.crypto.SecretKey;

import com.google.gson.Gson;
import com.google.gson.internal.Pair;

public class SessionHandler {
	private final ClientCallbacks callbacks;
	private final HashMap<String, Pair<Integer,Integer> > nonceMapHandler;
	private final HashMap<String, SecretKey> sessionKeys;
	private final Random random;
	private final Gson gson;
	private final ClientKeys clientKeys;
	
	private Map<String,Object> userList;
	private String clientHandle;

	public SessionHandler(ClientCallbacks callbacks, ClientKeys clientKeys) {
		this.callbacks = callbacks;
		this.clientKeys = clientKeys;
		nonceMapHandler = new HashMap<String, Pair<Integer, Integer>>();
		sessionKeys = new HashMap<String, SecretKey>();
		random = new Random();
		gson = new Gson();
		userList = null;
	}
	
	public SecretKey getMasterKey() {
		return clientKeys.masterkey;
	}
	
	public int getNonceB(String handle, int nonceA) {
		if (sessionKeys.containsKey(handle)){
			sessionKeys.remove(handle);
		}
		if (nonceMapHandler.containsKey(handle)) {
			nonceMapHandler.remove(handle);
		}
		int nonceB = random.nextInt();
		nonceMapHandler.put(handle, new Pair<Integer,Integer>(nonceA,nonceB));
		return nonceB;
	}
	
	public boolean checkNonces(String handle, int nonceA, int nonceB){
		Pair<Integer, Integer> noncepair = new Pair<Integer, Integer>(nonceA, nonceB);
		if (nonceMapHandler.get(handle).equals(noncepair)) {
			nonceMapHandler.remove(handle);
			return true;
		}
		return false;
	}
	
	public boolean hasKey(String handle){
		return sessionKeys.containsKey(handle);
	}
	
	public SecretKey getKey(String handle){
		return sessionKeys.get(handle);
	}
	
	public void addKey(String handle, SecretKey key){
		//removes the old key if it exists
		sessionKeys.put(handle, key);
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
		if (callbacks != null) {
			callbacks.onClientListReceived(userList.keySet());
		}
		System.err.println(userList);
	}
	
	public boolean hasUserList() {
		return userList != null;
	}

	public Set<String> getClientList() {
		return userList.keySet();
	}
	
	public ClientCallbacks getCallbacks() {
		return callbacks;
	}

}
