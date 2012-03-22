package com.nis.client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;

public interface Handle {
	public class HandleParameters {
		public final String handle;
		public final String request;
		public final SessionHandler sessionHandler;
		public final InetSocketAddress source;
		public final BufferedReader inFromHost;
		public final InputStream inputStream;
		public final PrintWriter outToHost;
		public HandleParameters(String handle, String request, 
				InetSocketAddress source, SessionHandler sessionHandler,
				BufferedReader inFromHost, InputStream inputStream,
				PrintWriter outToHost){
			this.handle = handle;
			this.request = request;
			this.source = source;
			this.sessionHandler = sessionHandler;
			this.inFromHost = inFromHost;
			this.inputStream = inputStream;
			this.outToHost = outToHost;
		}
	}
	public String handle(HandleParameters parameters) throws DataTransferException;
}
