package com.nis.client.handlers;

import com.google.gson.Gson;
import com.nis.client.Handle;
import com.nis.shared.requests.Hello;
import com.nis.shared.response.HelloResult;

public class HelloHandler implements Handle {
	
	@Override
	public String handle(HandleParameters parameters) {
		Gson gson = new Gson();
		Hello hello = gson.fromJson(parameters.request, Hello.class);
		String handle = hello.handle;
		int nonceA = hello.nonce;
		int nonceB = parameters.sessionHandler.getNonceB(handle, nonceA);
		HelloResult result = new HelloResult(nonceB);

		return gson.toJson(result);
	}

}
