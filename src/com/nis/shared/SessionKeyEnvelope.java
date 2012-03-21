package com.nis.shared;

public class SessionKeyEnvelope {
	public int salt;
	public int nonce;
	public String handle;
	public String encodedSessionKey;

	public SessionKeyEnvelope(int salt, int nonce, String handle,
			String encodedSessionKey) {
		this.salt = salt;
		this.nonce = nonce;
		this.handle = handle;
		this.encodedSessionKey = encodedSessionKey;
	}
}
