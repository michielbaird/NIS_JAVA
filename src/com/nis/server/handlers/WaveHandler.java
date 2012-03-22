package com.nis.server.handlers;

import java.net.InetSocketAddress;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import com.google.gson.Gson;
import com.nis.server.Handle;
import com.nis.server.ServerInfo;
import com.nis.shared.Base64Coder;
import com.nis.shared.requests.Wave;
import com.nis.shared.response.WaveResult;

public class WaveHandler implements Handle {

	@Override
	public String handle(String request, InetSocketAddress source, 
			ServerInfo serverInfo) {
		Gson gson = new Gson();
		Wave wave = gson.fromJson(request, Wave.class);
		serverInfo.addUser(wave.handle, wave.address, wave.port, wave.publicKey);
		
		String userListJSon =  serverInfo.getUserListJson();
		WaveResult waveResult = new WaveResult(userListJSon);
		
		return gson.toJson(waveResult);
	}

}
