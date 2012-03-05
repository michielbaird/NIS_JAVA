package com.nis.server.handlers;

import com.nis.shared.requests.Ping;
import com.nis.shared.response.PingResult;

public class PingHandler {
	protected PingHandler() {
		/* Protected Contructor */
	}
	
	public static PingResult handle(Ping ping) {
		return new PingResult();
	}
}
