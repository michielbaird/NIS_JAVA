package com.nis.server.handlers;

import java.net.InetSocketAddress;

import com.google.gson.Gson;
import com.nis.server.Handle;
import com.nis.server.ServerInfo;
import com.nis.shared.requests.Wave;
import com.nis.shared.response.WaveResult;

public class WaveHandler implements Handle {

	@Override
	public String handle(String request, InetSocketAddress source, 
			ServerInfo serverInfo) {
		Gson gson = new Gson();
		Wave wave = gson.fromJson(request, Wave.class);
		InetSocketAddress user = new InetSocketAddress(wave.address,wave.port);
		serverInfo.addUser(wave.handle, user);
		System.out.println("Adding user to userList");
		String userListJSon =  serverInfo.getUserListJson();
		WaveResult waveResult = new WaveResult(userListJSon);
		System.out.println("Sending online userList.");
		return gson.toJson(waveResult);
	}

}
