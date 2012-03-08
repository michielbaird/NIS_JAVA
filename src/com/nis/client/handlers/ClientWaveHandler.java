package com.nis.client.handlers;

import java.net.InetSocketAddress;

import com.google.gson.Gson;
import com.nis.client.Handle;
import com.nis.client.SessionHandler;
import com.nis.shared.requests.ClientWave;
import com.nis.shared.response.ClientWaveResult;

public class ClientWaveHandler implements Handle {

	@Override
	public String handle(SessionHandler sessionHandler, String request,
			InetSocketAddress source) {
		Gson gson = new Gson();
		ClientWave wave = gson.fromJson(request, ClientWave.class);

		sessionHandler.addActiveUser(wave.handle,source.getHostName(),
				wave.port);
		ClientWaveResult waveResult = new ClientWaveResult();
		
		return gson.toJson(waveResult);
	}

}
