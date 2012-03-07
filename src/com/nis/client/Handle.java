package com.nis.client;

import java.net.InetSocketAddress;

public interface Handle {
	public String handle(SessionHandler sessionHandler, String request, InetSocketAddress source);
}
