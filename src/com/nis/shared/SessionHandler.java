package com.nis.shared;

import java.util.HashMap;
import java.util.Random;

import com.google.gson.internal.Pair;



public class SessionHandler {
	private HashMap<String, Pair<Integer,Integer> > nonceMapHandler;
	private HashMap<String, String> sessionKeys;
	private Random random;
	private String clientHandle;

	public SessionHandler() {
		nonceMapHandler = new HashMap<String, Pair<Integer, Integer>>();
		sessionKeys = new HashMap<String, String>();
		random = new Random();
	}
	
	public int getNonceB(String handle, int nonceA) {
		if (sessionKeys.containsKey(handle)){
			sessionKeys.remove(handle);
		}
		if (sessionKeys.containsKey(handle)) {
			sessionKeys.remove(handle);
		}
		int nounceB = random.nextInt();
		nonceMapHandler.put(handle,  new Pair(nonceA,nounceB));
		return nounceB;
	}

}
