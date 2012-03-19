package com.nis.client;

import java.io.Serializable;
import java.security.KeyPair;
import javax.crypto.SecretKey;

public class ClientKeys implements Serializable {
	private static final long serialVersionUID = 1L;
	public SecretKey masterkey;
	public KeyPair keypair;
}
