package com.nis.client.handlers;

import java.net.InetSocketAddress;

import com.google.gson.Gson;
import com.nis.client.Handle;
import com.nis.client.SessionHandler;
import com.nis.shared.requests.Hello;
import com.nis.shared.response.HelloResult;

public class HelloHandler implements Handle {
	
	@Override
	public String handle(SessionHandler sessionHandler, String request,
			InetSocketAddress source) {
		Gson gson = new Gson();
		Hello hello = gson.fromJson(request, Hello.class);
		String handle = hello.handle;
		int nonceA = hello.nonce;
		int nonceB = sessionHandler.getNonceB(handle, nonceA);
		HelloResult result = new HelloResult(nonceB);

		return gson.toJson(result);
	}

}
