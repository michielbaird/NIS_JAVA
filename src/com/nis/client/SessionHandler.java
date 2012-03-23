package com.nis.client;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.crypto.SecretKey;

import com.google.gson.Gson;
import com.google.gson.internal.Pair;
import com.nis.shared.Base64Coder;

public class SessionHandler {
	private final ClientCallbacks callbacks;
	private final HashMap<String, Pair<Integer,Integer> > nonceMapHandler;
	private final HashMap<String, SecretKey> sessionKeys;
	private final HashMap<String, PublicKey> publicKeys;
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
		publicKeys = new HashMap<String, PublicKey>();
		random = new Random();
		gson = new Gson();
		userList = null;
	}
	
	public SecretKey getMasterKey() {
		return clientKeys.masterkey;
	}
	
	public PublicKey getPubKey() {
		return clientKeys.keypair.getPublic();
	}
	
	public PrivateKey getPrivKey() {
		return clientKeys.keypair.getPrivate();
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
		for (String handle: userList.keySet()) {
			String publicKey = (String)((Map)userList.get(handle)).get("publicKey");
			byte [] pub_bytes = Base64Coder.decode(publicKey);
			KeyFactory kf = null;
			PublicKey pub = null;
			try {
				kf = KeyFactory.getInstance("RSA");
				X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(pub_bytes);
				pub = kf.generatePublic(pubKeySpec);
			} catch (NoSuchAlgorithmException e) {
				//should not happen
				e.printStackTrace();
			} catch (InvalidKeySpecException e) {
				//should not happen
				e.printStackTrace();
			}
			addPublicKey(handle, pub);
		}
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

	public void addActiveUser(String handle, String hostName, int port, String publicKey) {
		Map<Object,Object> entry = new HashMap<Object,Object>();
		entry.put("address", hostName);
		entry.put("port", new Double(port));
		entry.put("publicKey", publicKey);
		
		userList.put(handle, entry);
		
		byte [] pub_bytes = Base64Coder.decode(publicKey);
		KeyFactory kf = null;
		PublicKey pub = null;
		try {
			kf = KeyFactory.getInstance("RSA");
			X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(pub_bytes);
			pub = kf.generatePublic(pubKeySpec);
		} catch (NoSuchAlgorithmException e) {
			//should not happen
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			//should not happen
			e.printStackTrace();
		}
		addPublicKey(handle, pub);
		
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
	
	public boolean hasPubKey(String handle){
		return publicKeys.containsKey(handle);
	}
	
	public PublicKey getPublicKey(String handle){
		return publicKeys.get(handle);
	}
	
	public void addPublicKey(String handle, PublicKey pub) {
		publicKeys.put(handle, pub);
	}
}
