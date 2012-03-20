package com.nis.client;

import java.io.Serializable;
import java.security.KeyPair;
import javax.crypto.SecretKey;

public class ClientKeys implements Serializable {
	private static final long serialVersionUID = 2L;
	public String handle;
	public SecretKey masterkey;
	public KeyPair keypair;
}
