package com.nis.client.handlers;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import com.google.gson.Gson;
import com.nis.client.Handle;
import com.nis.shared.Base64Coder;
import com.nis.shared.Crypter;
import com.nis.shared.SessionKeyEnvelope;
import com.nis.shared.requests.ClientKeyRequest;
import com.nis.shared.response.SendClientKeyResult;

public class SendClientKeyHandler implements Handle {

	@Override
	public String handle(HandleParameters parameters) {
		Gson gson = new Gson();
		String handleA = parameters.handle;
		ClientKeyRequest keyFromA = gson.fromJson(parameters.request, ClientKeyRequest.class);
		String jsonstring = Crypter.decrypt(keyFromA.EncryptedKey, parameters.sessionHandler.getMasterKey());
		SessionKeyEnvelope env = gson.fromJson(jsonstring, SessionKeyEnvelope.class);
		SecretKey key = null;
		boolean validnonces = parameters.sessionHandler.checkNonces(parameters.handle, env.nonceA, env.nonceB);
		if (validnonces) {
			byte [] key_bytes = Base64Coder.decode(env.encodedSessionKey);
			key = new SecretKeySpec(key_bytes, "AES");
			parameters.sessionHandler.addKey(parameters.handle, key);
		}
		return gson.toJson(new SendClientKeyResult());
	}

}
