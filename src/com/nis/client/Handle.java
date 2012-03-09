package com.nis.client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.net.InetSocketAddress;

public interface Handle {
	public class HandleParameters {
		public final String request;
		public final SessionHandler sessionHandler;
		public final InetSocketAddress source;
		public final BufferedReader inFromHost;
		public final DataOutputStream outToHost;
		public HandleParameters(String request, InetSocketAddress source,
				SessionHandler sessionHandler, BufferedReader inFromHost,
				DataOutputStream outToHost){
			this.request = request;
			this.source = source;
			this.sessionHandler = sessionHandler;
			this.inFromHost = inFromHost;
			this.outToHost = outToHost;
			
		}
	}
	public String handle(SessionHandler sessionHandler, String request, InetSocketAddress source);
}
