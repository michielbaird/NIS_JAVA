package com.nis.client.handlers;

import com.google.gson.Gson;
import com.nis.client.Handle;
import com.nis.client.SessionHandler;
import com.nis.shared.requests.Message;
import com.nis.shared.response.MessageResult;

public class MessageHandler implements Handle {

	@Override
	public String handle(HandleParameters parameters) {
		Gson gson = new Gson();
		String from = parameters.handle;
		Message messsage = gson.fromJson(parameters.request, Message.class);
		// TODO(Henk): Decrypt Message.
		
		String decryptedMessage =  messsage.encryptedMessage;
		if (parameters.sessionHandler.getCallbacks() != null) {
			parameters.sessionHandler.getCallbacks()
			.onClientMessageRecieved(parameters.handle, decryptedMessage);
		}
		return gson.toJson(new MessageResult());
	}

}
