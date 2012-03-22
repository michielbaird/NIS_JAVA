package com.nis.shared.requests;

public class ClientWave {
	public int port;
	public String publicKey;
	
	public ClientWave(int port, String publicKey) {
		this.port = port;
		this.publicKey = publicKey;
	}

}
