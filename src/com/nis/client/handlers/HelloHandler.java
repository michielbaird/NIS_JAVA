package com.nis.client.handlers;

import com.google.gson.Gson;
import com.nis.shared.Handle;
import com.nis.shared.SessionHandler;
import com.nis.shared.requests.Hello;
import com.nis.shared.response.HelloResult;

public class HelloHandler implements Handle {
	
	@Override
	public String handle(SessionHandler sessionHandler, String request) {
		Gson gson = new Gson();
		Hello hello = gson.fromJson(request, Hello.class);
		String handle = hello.handle;
		int nonceA = hello.nonce;
		int nonceB = sessionHandler.getNonceB(handle, nonceA);
		HelloResult result = new HelloResult(nonceB);

		return gson.toJson(result);
	}

}
