package com.nis.shared.requests;

public class Wave {
	public String handle;
	public String address;
	public int port;
	public Wave(String handle,String address, int port) {
		this.handle = handle;
		this.address = address;
		this.port = port;
	}

}
