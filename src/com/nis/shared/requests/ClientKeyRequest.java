package com.nis.shared.requests;

public class ClientKeyRequest {
	public String EncryptedKey;
	public ClientKeyRequest(String EncryptedKey){
		this.EncryptedKey = EncryptedKey;
	}
}