package com.nis.shared.response;

public class GetSessionKeyResult {
	public String encryptedKeyA;
	public String encryptedKeyB;
	public GetSessionKeyResult(String encrypedKeyA, String encryptedKeyB) {
		this.encryptedKeyA = encrypedKeyA;
		this.encryptedKeyB = encryptedKeyB;
	}

}
