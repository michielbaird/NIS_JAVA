package com.nis.shared.requests;

public class Wave {
	public String handle;
	public String address;
	public String publicKey;
	public int port;
	public Wave(String handle,String address, int port, String publicKey) {
		this.handle = handle;
		this.address = address;
		this.port = port;
		this.publicKey = publicKey;
	}

}
