package com.nis.shared;

public class UserData {
	public String address;
	public int port;
	public String publicKey;
	public UserData(String address, int port, String publicKey) {
		this.address = address;
		this.port = port;
		this.publicKey = publicKey;
	}
}
