package com.nis.server.handlers;

import java.net.InetSocketAddress;

import com.google.gson.Gson;
import com.nis.server.Handle;
import com.nis.server.ServerInfo;
import com.nis.shared.requests.GetSessionKey;
import com.nis.shared.response.GetSessionKeyResult;

public class GetSessionKeyHandler implements Handle {

	@Override
	public String handle(String request, InetSocketAddress source, 
			ServerInfo serverInfo) {
		Gson gson = new Gson();
		GetSessionKey getSessionKey = gson.fromJson(request,
				GetSessionKey.class);
		String handleA = getSessionKey.handleA;
		String handleB = getSessionKey.handleB;
		int nonceA = getSessionKey.nonceA;
		int nonceB = getSessionKey.nonceB;
		
		// TODO(henkjoubert) Add encrypted keys;
		String encryptedKeyA = "";
		String encryptedKeyB = "";
		
		GetSessionKeyResult result = new GetSessionKeyResult(encryptedKeyA,
				encryptedKeyB);
		
		return gson.toJson(result);
	}
}
