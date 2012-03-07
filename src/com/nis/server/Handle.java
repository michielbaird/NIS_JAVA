package com.nis.server;

import java.net.InetSocketAddress;

public interface Handle {
	public String handle(String request, InetSocketAddress source, ServerInfo serverInfo);
}
