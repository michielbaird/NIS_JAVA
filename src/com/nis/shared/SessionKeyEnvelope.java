package com.nis.shared;

public class SessionKeyEnvelope {
	public int salt;
	public int nonceA;
	public int nonceB;
	public String handle;
	public String encodedSessionKey;
	
	public SessionKeyEnvelope(int salt, int nonceA, int nonceB, String handle,
			String encodedSessionKey) {
		super();
		this.salt = salt;
		this.nonceA = nonceA;
		this.nonceB = nonceB;
		this.handle = handle;
		this.encodedSessionKey = encodedSessionKey;
	}

}
