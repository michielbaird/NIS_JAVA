package com.nis.client.handlers;

import com.google.gson.Gson;
import com.nis.client.Handle;
import com.nis.shared.requests.ClientWave;
import com.nis.shared.response.ClientWaveResult;

public class ClientWaveHandler implements Handle {

	@Override
	public String handle(HandleParameters parameters) {
		Gson gson = new Gson();
		ClientWave wave = gson.fromJson(parameters.request, ClientWave.class);

		parameters.sessionHandler.addActiveUser(wave.handle, 
				parameters.source.getHostName(), wave.port);
		ClientWaveResult waveResult = new ClientWaveResult();
		
		return gson.toJson(waveResult);
	}

}
