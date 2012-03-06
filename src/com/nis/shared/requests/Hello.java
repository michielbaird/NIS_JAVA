package com.nis.shared.requests;

public class Hello {
	public String handle;
	public int nonce;
	
	public Hello(String handle, int nonce) {
		this.handle = handle;
		this.nonce = nonce;
	}
}
